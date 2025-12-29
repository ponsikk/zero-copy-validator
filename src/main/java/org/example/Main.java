package org.example;

import io.zerocopy.json.JsonValidator;
import io.zerocopy.json.ValidationResult;

/**
 * Demo application showing JsonValidator usage.
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("=== Zero-Copy JSON Validator Demo ===\n");

        // Example 1: Simple validation
        String validJson = "{\"name\":\"John\",\"age\":30,\"city\":\"New York\"}";
        boolean isValid = JsonValidator.validate(validJson);
        System.out.println("Valid JSON: " + isValid);

        // Example 2: Invalid JSON
        String invalidJson = "{\"name\":\"John\",\"age\":}";
        isValid = JsonValidator.validate(invalidJson);
        System.out.println("Invalid JSON: " + isValid);

        // Example 3: Detailed validation
        ValidationResult result = JsonValidator.validateDetailed(invalidJson);
        System.out.println("\nDetailed validation:");
        System.out.println("  Valid: " + result.isValid());
        System.out.println("  Error code: " + result.getErrorCode());
        System.out.println("  Error message: " + result.getErrorMessage());

        // Example 4: Complex valid JSON
        String complexJson = """
            {
                "users": [
                    {"id": 1, "name": "Alice", "email": "alice@example.com"},
                    {"id": 2, "name": "Bob", "email": "bob@example.com"}
                ],
                "metadata": {
                    "total": 2,
                    "page": 1,
                    "hasMore": false
                }
            }
            """;
        result = JsonValidator.validateDetailed(complexJson);
        System.out.println("\nComplex JSON validation: " + result.isValid());

        // Example 5: Zero-copy field extraction (FAST!)
        System.out.println("\n=== Zero-Copy Field Extraction Demo ===");

        // Extract string field
        String userName = JsonValidator.getString(complexJson, "users.0.name");
        System.out.println("First user name: " + userName);

        String userEmail = JsonValidator.getString(complexJson, "users.1.email");
        System.out.println("Second user email: " + userEmail);

        // Extract number field
        double totalCount = JsonValidator.getNumber(complexJson, "metadata.total");
        System.out.println("Total count: " + totalCount);

        // Extract boolean field
        boolean hasMore = JsonValidator.getBoolean(complexJson, "metadata.hasMore");
        System.out.println("Has more: " + hasMore);

        // Example 6: Nested extraction
        String productJson = """
            {
                "product": {
                    "name": "Laptop",
                    "price": 1299.99,
                    "inStock": true,
                    "specs": {
                        "cpu": "Intel i7",
                        "ram": "16GB"
                    }
                }
            }
            """;

        System.out.println("\n=== Nested Field Extraction ===");
        String productName = JsonValidator.getString(productJson, "product.name");
        double price = JsonValidator.getNumber(productJson, "product.price");
        boolean inStock = JsonValidator.getBoolean(productJson, "product.inStock");
        String cpu = JsonValidator.getString(productJson, "product.specs.cpu");

        System.out.println("Product: " + productName);
        System.out.println("Price: $" + price);
        System.out.println("In stock: " + inStock);
        System.out.println("CPU: " + cpu);

        System.out.println("\n✅ Demo completed!");
        System.out.println("Version: " + JsonValidator.getVersion());

        // Example 7: Phase 2 - Advanced Validators
        System.out.println("\n=== Phase 2: Advanced Validators ===");

        String validationJson = """
            {
                "user": {
                    "name": "Alice",
                    "age": 30,
                    "email": "alice@example.com",
                    "middleName": null
                }
            }
            """;

        // Type validation
        boolean isString = JsonValidator.validateFieldType(validationJson, "user.name", io.zerocopy.json.JsonType.STRING);
        boolean isNumber = JsonValidator.validateFieldType(validationJson, "user.age", io.zerocopy.json.JsonType.NUMBER);
        System.out.println("Name is STRING: " + isString);
        System.out.println("Age is NUMBER: " + isNumber);

        // Field existence
        boolean hasEmail = JsonValidator.fieldExists(validationJson, "user.email");
        boolean hasPhone = JsonValidator.fieldExists(validationJson, "user.phone");
        System.out.println("Has email field: " + hasEmail);
        System.out.println("Has phone field: " + hasPhone);

        // Null check
        boolean middleNameIsNull = JsonValidator.fieldIsNull(validationJson, "user.middleName");
        boolean nameIsNull = JsonValidator.fieldIsNull(validationJson, "user.name");
        System.out.println("Middle name is null: " + middleNameIsNull);
        System.out.println("Name is null: " + nameIsNull);

        System.out.println("\n✅ Phase 2 validators working!");

        // Example 8: Phase 2.2 - Range Validators
        System.out.println("\n=== Phase 2.2: Range Validators ===");

        String orderJson = """
            {
                "order": {
                    "id": 12345,
                    "customerAge": 25,
                    "items": [
                        {"name": "Laptop", "price": 999.99},
                        {"name": "Mouse", "price": 29.99},
                        {"name": "Keyboard", "price": 79.99}
                    ],
                    "notes": "Express shipping"
                }
            }
            """;

        // Validate number range (age should be 18-120)
        boolean ageValid = JsonValidator.validateNumberRange(orderJson, "order.customerAge", 18, 120);
        System.out.println("Customer age in valid range (18-120): " + ageValid);

        // Validate number range (price should be 0-10000)
        boolean priceValid = JsonValidator.validateNumberRange(orderJson, "order.items.0.price", 0, 10000);
        System.out.println("First item price in valid range (0-10000): " + priceValid);

        // Validate string length (notes should be 5-100 characters)
        boolean notesValid = JsonValidator.validateStringLength(orderJson, "order.notes", 5, 100);
        System.out.println("Notes length in valid range (5-100): " + notesValid);

        // Validate array size (items should have 1-10 elements)
        boolean itemsValid = JsonValidator.validateArraySize(orderJson, "order.items", 1, 10);
        System.out.println("Items array size in valid range (1-10): " + itemsValid);

        // Test with invalid data
        String invalidOrderJson = """
            {
                "order": {
                    "customerAge": 200,
                    "items": []
                }
            }
            """;

        boolean invalidAge = JsonValidator.validateNumberRange(invalidOrderJson, "order.customerAge", 18, 120);
        boolean emptyItems = JsonValidator.validateArraySize(invalidOrderJson, "order.items", 1, 10);
        System.out.println("\nInvalid age (200) rejected: " + !invalidAge);
        System.out.println("Empty items array rejected: " + !emptyItems);

        System.out.println("\n✅ Phase 2.2 range validators working!");
    }
}