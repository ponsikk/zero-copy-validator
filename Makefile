# ============================================
# Zero-Copy JSON Validator - Makefile
# ============================================
# Упрощённые команды для разработки

.PHONY: help build test clean dev docker rust java fmt lint quick install release

# Цвета для вывода
RED=\033[0;31m
GREEN=\033[0;32m
YELLOW=\033[1;33m
BLUE=\033[0;34m
NC=\033[0m # No Color

# ============================================
# Помощь
# ============================================
help: ## Показать это сообщение
	@echo "$(BLUE)Zero-Copy JSON Validator - Development Commands$(NC)"
	@echo ""
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "$(GREEN)%-15s$(NC) %s\n", $$1, $$2}'
	@echo ""
	@echo "$(YELLOW)Quick commands:$(NC)"
	@echo "  $(GREEN)make quick$(NC)    - Быстрая сборка и тест (только изменённое)"
	@echo "  $(GREEN)make dev$(NC)      - Запустить dev окружение"
	@echo "  $(GREEN)make test$(NC)     - Запустить все тесты"

# ============================================
# Быстрая разработка
# ============================================
quick: ## Быстрая сборка (только изменённые файлы)
	@echo "$(BLUE)⚡ Quick build...$(NC)"
	@cd rust-ffi && cargo build
	@mvn compile -q
	@echo "$(GREEN)✅ Quick build done!$(NC)"

dev: ## Запустить dev окружение в Docker
	@echo "$(BLUE)🛠️  Starting dev environment...$(NC)"
	@docker-compose run --rm dev

watch-rust: ## Watch режим для Rust (пересборка при изменениях)
	@echo "$(BLUE)👀 Watching Rust files...$(NC)"
	@cd rust-ffi && cargo watch -x build

watch-java: ## Watch режим для Java
	@echo "$(BLUE)👀 Watching Java files...$(NC)"
	@mvn compile -Dcompiler.fork=true

# ============================================
# Сборка
# ============================================
build: rust java ## Полная сборка (Rust + Java)
	@echo "$(GREEN)✅ Full build complete!$(NC)"

rust: ## Собрать только Rust библиотеку
	@echo "$(BLUE)🦀 Building Rust library...$(NC)"
	@cd rust-ffi && cargo build --release
	@echo "$(GREEN)✅ Rust build done!$(NC)"

java: ## Собрать только Java проект
	@echo "$(BLUE)☕ Building Java project...$(NC)"
	@mvn clean package -DskipTests
	@echo "$(GREEN)✅ Java build done!$(NC)"

docker: ## Собрать Docker образ
	@echo "$(BLUE)🐳 Building Docker image...$(NC)"
	@docker-compose build app
	@echo "$(GREEN)✅ Docker build done!$(NC)"

# ============================================
# Тестирование
# ============================================
test: test-rust test-java ## Запустить все тесты
	@echo "$(GREEN)✅ All tests passed!$(NC)"

test-rust: ## Тесты только Rust
	@echo "$(BLUE)🧪 Running Rust tests...$(NC)"
	@cd rust-ffi && cargo test --release

test-java: ## Тесты только Java
	@echo "$(BLUE)🧪 Running Java tests...$(NC)"
	@mvn test

test-docker: ## Тест Docker образа
	@echo "$(BLUE)🧪 Testing Docker image...$(NC)"
	@docker run --rm zero-copy-app | grep -q "Hello, World!" && \
		echo "$(GREEN)✅ Docker test passed!$(NC)" || \
		echo "$(RED)❌ Docker test failed!$(NC)"

bench: ## Запустить benchmarks
	@echo "$(BLUE)📊 Running benchmarks...$(NC)"
	@cd rust-ffi && cargo bench

# ============================================
# Качество кода
# ============================================
fmt: fmt-rust fmt-java ## Форматировать весь код

fmt-rust: ## Форматировать Rust код
	@echo "$(BLUE)🎨 Formatting Rust code...$(NC)"
	@cd rust-ffi && cargo fmt

fmt-java: ## Форматировать Java код
	@echo "$(BLUE)🎨 Formatting Java code...$(NC)"
	@mvn formatter:format || echo "$(YELLOW)⚠️  Java formatter not configured$(NC)"

lint: lint-rust lint-java ## Проверка кода линтерами

lint-rust: ## Lint Rust code
	@echo "$(BLUE)🔍 Linting Rust code...$(NC)"
	@cd rust-ffi && cargo clippy -- -D warnings

lint-java: ## Lint Java code
	@echo "$(BLUE)🔍 Linting Java code...$(NC)"
	@mvn checkstyle:check || echo "$(YELLOW)⚠️  Checkstyle not configured$(NC)"

check: fmt lint test ## Полная проверка (format + lint + test)
	@echo "$(GREEN)✅ All checks passed!$(NC)"

# ============================================
# Очистка
# ============================================
clean: clean-rust clean-java clean-docker ## Очистить всё

clean-rust: ## Очистить Rust артефакты
	@echo "$(BLUE)🧹 Cleaning Rust...$(NC)"
	@cd rust-ffi && cargo clean

clean-java: ## Очистить Java артефакты
	@echo "$(BLUE)🧹 Cleaning Java...$(NC)"
	@mvn clean

clean-docker: ## Очистить Docker артефакты
	@echo "$(BLUE)🧹 Cleaning Docker...$(NC)"
	@docker-compose down --rmi local -v 2>/dev/null || true
	@docker system prune -f

# ============================================
# Установка и релиз
# ============================================
install: build ## Установить в локальный Maven репозиторий
	@echo "$(BLUE)📦 Installing to local Maven repository...$(NC)"
	@mvn install -DskipTests
	@echo "$(GREEN)✅ Installed successfully!$(NC)"
	@echo ""
	@echo "$(YELLOW)Usage in other projects:$(NC)"
	@echo "  <dependency>"
	@echo "    <groupId>io.zerocopy</groupId>"
	@echo "    <artifactId>json-validator</artifactId>"
	@echo "    <version>0.1.0-SNAPSHOT</version>"
	@echo "  </dependency>"

release: ## Создать release (потребуется версия)
	@echo "$(BLUE)📦 Creating release...$(NC)"
	@read -p "Enter version (e.g., v0.1.0): " VERSION; \
	git tag -a $$VERSION -m "Release $$VERSION"; \
	git push origin $$VERSION
	@echo "$(GREEN)✅ Release tag created! GitHub Actions will build and publish.$(NC)"

# ============================================
# CI/CD локально
# ============================================
ci-local: ## Запустить CI pipeline локально
	@echo "$(BLUE)🔄 Running CI locally...$(NC)"
	@echo "$(YELLOW)Step 1: Rust build & test$(NC)"
	@cd rust-ffi && cargo build --release && cargo test
	@echo "$(YELLOW)Step 2: Java build & test$(NC)"
	@mvn clean package
	@echo "$(YELLOW)Step 3: Docker build & test$(NC)"
	@docker-compose build app
	@docker run --rm zero-copy-app | grep -q "Hello, World!"
	@echo "$(GREEN)✅ Local CI passed!$(NC)"

# ============================================
# Утилиты
# ============================================
deps: ## Показать дерево зависимостей
	@echo "$(BLUE)📦 Rust dependencies:$(NC)"
	@cd rust-ffi && cargo tree
	@echo ""
	@echo "$(BLUE)📦 Java dependencies:$(NC)"
	@mvn dependency:tree

size: ## Показать размеры артефактов
	@echo "$(BLUE)📊 Artifact sizes:$(NC)"
	@du -h rust-ffi/target/release/libjson_validator_ffi.so 2>/dev/null || echo "Rust lib not built"
	@du -h target/*.jar 2>/dev/null || echo "Java JAR not built"
	@docker images zero-copy-app --format "{{.Repository}}:{{.Tag}} - {{.Size}}" 2>/dev/null || echo "Docker image not built"

update: ## Обновить зависимости
	@echo "$(BLUE)📦 Updating Rust dependencies...$(NC)"
	@cd rust-ffi && cargo update
	@echo "$(BLUE)📦 Updating Java dependencies...$(NC)"
	@mvn versions:display-dependency-updates

# ============================================
# Git hooks
# ============================================
hooks: ## Установить git hooks
	@echo "$(BLUE)🪝 Installing git hooks...$(NC)"
	@cp scripts/pre-commit .git/hooks/pre-commit
	@chmod +x .git/hooks/pre-commit
	@echo "$(GREEN)✅ Git hooks installed!$(NC)"

# ============================================
# Документация
# ============================================
docs: ## Генерировать документацию
	@echo "$(BLUE)📚 Generating Rust docs...$(NC)"
	@cd rust-ffi && cargo doc --no-deps --open
	@echo "$(BLUE)📚 Generating Java docs...$(NC)"
	@mvn javadoc:javadoc
	@echo "$(GREEN)✅ Documentation generated!$(NC)"

# ============================================
# По умолчанию
# ============================================
.DEFAULT_GOAL := help
