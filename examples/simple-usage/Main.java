package examples;

// TODO: Этот код заработает после Phase 1
// Сейчас это демонстрация будущего API

import io.zerocopy.json.JsonValidator;
import io.zerocopy.json.ValidationResult;

/**
 * Пример использования Zero-Copy JSON Validator
 *
 * Требования:
 * - Java 22+
 * - JVM флаги: --enable-preview --enable-native-access=ALL-UNNAMED
 */
public class Main {

    public static void main(String[] args) {
        // Пример 1: Простая валидация
        simpleValidation();

        // Пример 2: Детальная валидация с ошибками
        detailedValidation();

        // Пример 3: Массовая валидация
        batchValidation();
    }

    /**
     * Простая валидация - возвращает true/false
     */
    private static void simpleValidation() {
        System.out.println("=== Simple Validation ===");

        String validJson = """
            {
                "name": "John Doe",
                "age": 30,
                "email": "john@example.com"
            }
            """;

        String invalidJson = "{\"name\":\"incomplete\"";

        // Валидация занимает ~2-5 микросекунд
        boolean isValid1 = JsonValidator.validate(validJson);
        boolean isValid2 = JsonValidator.validate(invalidJson);

        System.out.println("Valid JSON: " + isValid1);     // true
        System.out.println("Invalid JSON: " + isValid2);   // false
        System.out.println();
    }

    /**
     * Детальная валидация - возвращает код ошибки и сообщение
     */
    private static void detailedValidation() {
        System.out.println("=== Detailed Validation ===");

        String brokenJson = """
            {
                "users": [
                    {"id": 1, "name": "Alice"},
                    {"id": 2, "name": "Bob"
                ]
            }
            """;

        ValidationResult result = JsonValidator.validateDetailed(brokenJson);

        if (result.isValid()) {
            System.out.println("✅ JSON is valid");
        } else {
            System.out.printf("❌ Validation failed:%n");
            System.out.printf("   Error code: %d%n", result.getErrorCode());
            System.out.printf("   Message: %s%n", result.getErrorMessage());
        }
        System.out.println();
    }

    /**
     * Массовая валидация - обработка нескольких JSON
     */
    private static void batchValidation() {
        System.out.println("=== Batch Validation ===");

        String[] testCases = {
            "{}",
            "[]",
            "{\"valid\":true}",
            "[1,2,3]",
            "{invalid}",
            "[1,2,3,]",  // trailing comma
            "null",
            "true",
            "\"string\""
        };

        int validCount = 0;
        for (String json : testCases) {
            boolean valid = JsonValidator.validate(json);
            System.out.printf("%-20s → %s%n",
                json.length() > 20 ? json.substring(0, 17) + "..." : json,
                valid ? "✅" : "❌");
            if (valid) validCount++;
        }

        System.out.printf("%nValid JSONs: %d/%d%n", validCount, testCases.length);
    }
}
