use serde_json::json;
use std::fs::{self, File};
use std::io::Write;
use std::path::Path;

/// Generate test JSON data of various sizes for benchmarking
///
/// Sizes: 1KB, 10KB, 100KB, 1MB, 10MB, 100MB
fn main() {
    println!("🚀 Generating test JSON files for benchmarking...\n");

    // Create output directory if it doesn't exist
    let output_dir = "benches/data";
    fs::create_dir_all(output_dir).expect("Failed to create output directory");

    let sizes = vec![
        ("1kb", 1024),
        ("10kb", 10 * 1024),
        ("100kb", 100 * 1024),
        ("1mb", 1024 * 1024),
        ("10mb", 10 * 1024 * 1024),
        ("100mb", 100 * 1024 * 1024),
    ];

    for (name, target_size) in sizes {
        println!("📝 Generating {}...", name);
        generate_json_file(name, target_size);
    }

    println!("\n✅ All test files generated successfully!");
    println!("📂 Files location: {}/", output_dir);
}

fn generate_json_file(name: &str, target_size: usize) {
    let output_path = format!("benches/data/test_{}.json", name);
    let path = Path::new(&output_path);

    // Create a realistic JSON structure with users
    let mut users = Vec::new();
    let mut current_size = 0;
    let mut user_id = 1;

    // Estimate size of a single user object (~400 bytes with pretty print)
    let estimated_user_size = 400;

    // Generate users until we reach target size
    while current_size < target_size {
        let user = json!({
            "id": user_id,
            "username": format!("user{}", user_id),
            "email": format!("user{}@example.com", user_id),
            "firstName": format!("FirstName{}", user_id),
            "lastName": format!("LastName{}", user_id),
            "age": 20 + (user_id % 50),
            "active": user_id % 2 == 0,
            "balance": (user_id as f64) * 123.45,
            "phone": format!("+1-555-{:04}-{:04}", user_id % 1000, user_id % 10000),
            "address": {
                "street": format!("{} Main Street", user_id * 100),
                "city": match user_id % 5 {
                    0 => "New York",
                    1 => "Los Angeles",
                    2 => "Chicago",
                    3 => "Houston",
                    _ => "Phoenix",
                },
                "state": match user_id % 5 {
                    0 => "NY",
                    1 => "CA",
                    2 => "IL",
                    3 => "TX",
                    _ => "AZ",
                },
                "zipCode": format!("{:05}", 10000 + user_id % 90000),
                "country": "USA",
                "coordinates": {
                    "lat": 40.7128 + (user_id as f64 * 0.001),
                    "lng": -74.0060 + (user_id as f64 * 0.001)
                }
            },
            "tags": vec![
                "developer",
                if user_id % 3 == 0 { "rust" } else { "java" },
                if user_id % 2 == 0 { "python" } else { "javascript" },
                "cloud",
                "backend"
            ],
            "metadata": {
                "createdAt": format!("2024-01-{:02}T10:30:00Z", 1 + (user_id % 28)),
                "updatedAt": "2024-12-29T12:00:00Z",
                "lastLogin": format!("2024-12-{:02}T08:45:30Z", 1 + (user_id % 29)),
                "loginCount": user_id * 10,
                "verified": user_id % 3 == 0,
                "premium": user_id % 5 == 0
            },
            "preferences": {
                "theme": if user_id % 2 == 0 { "dark" } else { "light" },
                "language": match user_id % 4 {
                    0 => "en",
                    1 => "es",
                    2 => "fr",
                    _ => "de",
                },
                "timezone": "America/New_York",
                "notifications": {
                    "email": user_id % 2 == 0,
                    "sms": user_id % 3 == 0,
                    "push": user_id % 4 == 0
                }
            },
            "stats": {
                "postsCount": user_id * 5,
                "followersCount": user_id * 10,
                "followingCount": user_id * 3,
                "likesReceived": user_id * 50
            }
        });

        users.push(user);
        current_size += estimated_user_size;
        user_id += 1;
    }

    // Create final JSON structure
    let data = json!({
        "version": "1.0",
        "generatedAt": "2024-12-29T12:00:00Z",
        "metadata": {
            "totalUsers": users.len(),
            "targetSize": target_size,
            "description": format!("Test JSON data for zero-copy validator benchmarking - {}", name),
            "generator": "zero-copy-validator test data generator"
        },
        "users": users,
        "statistics": {
            "activeUsers": users.iter().filter(|u| u["active"].as_bool().unwrap_or(false)).count(),
            "verifiedUsers": users.iter().filter(|u| u["metadata"]["verified"].as_bool().unwrap_or(false)).count(),
            "premiumUsers": users.iter().filter(|u| u["metadata"]["premium"].as_bool().unwrap_or(false)).count(),
            "totalBalance": users.iter().map(|u| u["balance"].as_f64().unwrap_or(0.0)).sum::<f64>(),
            "averageAge": users.iter().map(|u| u["age"].as_i64().unwrap_or(0)).sum::<i64>() / users.len() as i64,
            "totalPosts": users.iter().map(|u| u["stats"]["postsCount"].as_i64().unwrap_or(0)).sum::<i64>()
        }
    });

    // Write to file (pretty print)
    let json_string = serde_json::to_string_pretty(&data).unwrap();
    let mut file = File::create(path).expect("Failed to create file");
    file.write_all(json_string.as_bytes())
        .expect("Failed to write to file");

    let actual_size = json_string.len();
    let size_mb = actual_size as f64 / (1024.0 * 1024.0);

    println!(
        "  ✓ {} - {} bytes ({:.2} MB) - {} users",
        name,
        actual_size,
        size_mb,
        users.len()
    );
}
