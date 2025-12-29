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
    }
}