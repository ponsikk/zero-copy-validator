use criterion::{black_box, criterion_group, criterion_main, BenchmarkId, Criterion};
use json_validator_ffi::*;
use simd_json::prelude::*;
use std::fs;

// Test JSON data
const SIMPLE_JSON: &str = r#"{"name":"John","age":30,"email":"john@example.com","active":true,"website":"https://example.com","phone":"+1-234-567-8900","ip":"192.168.1.1","userId":"550e8400-e29b-41d4-a716-446655440000","createdAt":"2024-12-30T10:30:00Z"}"#;
const NESTED_JSON: &str = r#"{"user":{"name":"Alice","age":25,"email":"alice@example.com","profile":{"bio":"Software Engineer","skills":["Rust","Java","Python"],"website":"https://alice.dev","phone":"+44-20-7946-0958","lastLogin":"2024-12-29T15:45:30Z"}}}"#;
const LARGE_JSON: &str = r#"{"users":[{"id":1,"name":"User1","age":25,"email":"user1@example.com","ip":"10.0.0.1"},{"id":2,"name":"User2","age":30,"email":"user2@example.com","ip":"10.0.0.2"},{"id":3,"name":"User3","age":35,"email":"user3@example.com","ip":"10.0.0.3"},{"id":4,"name":"User4","age":40,"email":"user4@example.com","ip":"10.0.0.4"},{"id":5,"name":"User5","age":45,"email":"user5@example.com","ip":"10.0.0.5"}],"metadata":{"total":5,"page":1}}"#;

// Helper to create raw pointers for FFI
fn prepare_json(json: &str) -> (*const u8, usize) {
    (json.as_ptr(), json.len())
}

fn prepare_path(path: &str) -> (*const u8, usize) {
    (path.as_ptr(), path.len())
}

// Benchmark: Basic validation
fn bench_json_validate(c: &mut Criterion) {
    let mut group = c.benchmark_group("json_validate");

    group.bench_function("simple", |b| {
        let (ptr, len) = prepare_json(SIMPLE_JSON);
        b.iter(|| unsafe { black_box(json_validate(ptr, len)) });
    });

    group.bench_function("nested", |b| {
        let (ptr, len) = prepare_json(NESTED_JSON);
        b.iter(|| unsafe { black_box(json_validate(ptr, len)) });
    });

    group.bench_function("large", |b| {
        let (ptr, len) = prepare_json(LARGE_JSON);
        b.iter(|| unsafe { black_box(json_validate(ptr, len)) });
    });

    group.finish();
}

// Benchmark: Field type validation
fn bench_validate_field_type(c: &mut Criterion) {
    let mut group = c.benchmark_group("validate_field_type");

    group.bench_function("simple_field", |b| {
        let (json_ptr, json_len) = prepare_json(SIMPLE_JSON);
        let (path_ptr, path_len) = prepare_path("name");
        b.iter(|| unsafe {
            black_box(json_validate_field_type(
                json_ptr, json_len, path_ptr, path_len, 3,
            )) // STRING = 3
        });
    });

    group.bench_function("nested_field", |b| {
        let (json_ptr, json_len) = prepare_json(NESTED_JSON);
        let (path_ptr, path_len) = prepare_path("user.profile.bio");
        b.iter(|| unsafe {
            black_box(json_validate_field_type(
                json_ptr, json_len, path_ptr, path_len, 3,
            )) // STRING = 3
        });
    });

    group.finish();
}

// Benchmark: Field exists
fn bench_field_exists(c: &mut Criterion) {
    let mut group = c.benchmark_group("field_exists");

    group.bench_function("exists", |b| {
        let (json_ptr, json_len) = prepare_json(SIMPLE_JSON);
        let (path_ptr, path_len) = prepare_path("email");
        b.iter(|| unsafe { black_box(json_field_exists(json_ptr, json_len, path_ptr, path_len)) });
    });

    group.bench_function("not_exists", |b| {
        let (json_ptr, json_len) = prepare_json(SIMPLE_JSON);
        let (path_ptr, path_len) = prepare_path("nonexistent");
        b.iter(|| unsafe { black_box(json_field_exists(json_ptr, json_len, path_ptr, path_len)) });
    });

    group.finish();
}

// Benchmark: Number range validation
fn bench_validate_number_range(c: &mut Criterion) {
    let mut group = c.benchmark_group("validate_number_range");

    group.bench_function("valid_range", |b| {
        let (json_ptr, json_len) = prepare_json(SIMPLE_JSON);
        let (path_ptr, path_len) = prepare_path("age");
        b.iter(|| unsafe {
            black_box(json_validate_number_range(
                json_ptr, json_len, path_ptr, path_len, 0.0, 120.0,
            ))
        });
    });

    group.bench_function("nested_range", |b| {
        let (json_ptr, json_len) = prepare_json(NESTED_JSON);
        let (path_ptr, path_len) = prepare_path("user.age");
        b.iter(|| unsafe {
            black_box(json_validate_number_range(
                json_ptr, json_len, path_ptr, path_len, 0.0, 120.0,
            ))
        });
    });

    group.finish();
}

// Benchmark: String length validation
fn bench_validate_string_length(c: &mut Criterion) {
    let mut group = c.benchmark_group("validate_string_length");

    group.bench_function("simple_string", |b| {
        let (json_ptr, json_len) = prepare_json(SIMPLE_JSON);
        let (path_ptr, path_len) = prepare_path("name");
        b.iter(|| unsafe {
            black_box(json_validate_string_length(
                json_ptr, json_len, path_ptr, path_len, 1, 100,
            ))
        });
    });

    group.bench_function("nested_string", |b| {
        let (json_ptr, json_len) = prepare_json(NESTED_JSON);
        let (path_ptr, path_len) = prepare_path("user.profile.bio");
        b.iter(|| unsafe {
            black_box(json_validate_string_length(
                json_ptr, json_len, path_ptr, path_len, 1, 500,
            ))
        });
    });

    group.finish();
}

// Benchmark: Array size validation
fn bench_validate_array_size(c: &mut Criterion) {
    let mut group = c.benchmark_group("validate_array_size");

    group.bench_function("small_array", |b| {
        let (json_ptr, json_len) = prepare_json(NESTED_JSON);
        let (path_ptr, path_len) = prepare_path("user.profile.skills");
        b.iter(|| unsafe {
            black_box(json_validate_array_size(
                json_ptr, json_len, path_ptr, path_len, 1, 10,
            ))
        });
    });

    group.bench_function("large_array", |b| {
        let (json_ptr, json_len) = prepare_json(LARGE_JSON);
        let (path_ptr, path_len) = prepare_path("users");
        b.iter(|| unsafe {
            black_box(json_validate_array_size(
                json_ptr, json_len, path_ptr, path_len, 1, 100,
            ))
        });
    });

    group.finish();
}

// Benchmark: Field extraction (getString, getNumber, getBoolean)
fn bench_field_extraction(c: &mut Criterion) {
    let mut group = c.benchmark_group("field_extraction");

    group.bench_function("get_string", |b| {
        let (json_ptr, json_len) = prepare_json(SIMPLE_JSON);
        let (path_ptr, path_len) = prepare_path("name");
        let mut buffer = [0u8; 256];
        b.iter(|| unsafe {
            black_box(json_get_string(
                json_ptr,
                json_len,
                path_ptr,
                path_len,
                buffer.as_mut_ptr(),
                buffer.len(),
            ))
        });
    });

    group.bench_function("get_number", |b| {
        let (json_ptr, json_len) = prepare_json(SIMPLE_JSON);
        let (path_ptr, path_len) = prepare_path("age");
        let mut output = 0.0f64;
        b.iter(|| unsafe {
            black_box(json_get_number(
                json_ptr,
                json_len,
                path_ptr,
                path_len,
                &mut output as *mut f64,
            ))
        });
    });

    group.bench_function("get_boolean", |b| {
        let (json_ptr, json_len) = prepare_json(SIMPLE_JSON);
        let (path_ptr, path_len) = prepare_path("active");
        let mut output = false;
        b.iter(|| unsafe {
            black_box(json_get_bool(
                json_ptr,
                json_len,
                path_ptr,
                path_len,
                &mut output as *mut bool,
            ))
        });
    });

    group.finish();
}

// Comparison: Full parse vs zero-copy extraction
fn bench_comparison(c: &mut Criterion) {
    let mut group = c.benchmark_group("comparison");

    group.bench_function("full_parse_simd_json", |b| {
        b.iter(|| {
            let mut bytes = LARGE_JSON.as_bytes().to_vec();
            black_box(simd_json::to_owned_value(&mut bytes).is_ok())
        });
    });

    group.bench_function("zero_copy_validate", |b| {
        let (ptr, len) = prepare_json(LARGE_JSON);
        b.iter(|| unsafe { black_box(json_validate(ptr, len)) });
    });

    group.bench_function("full_parse_extract_field", |b| {
        b.iter(|| {
            let mut bytes = LARGE_JSON.as_bytes().to_vec();
            let result = if let Ok(val) = simd_json::to_owned_value(&mut bytes) {
                val.get("metadata")
                    .and_then(|m: &simd_json::OwnedValue| m.get("total"))
                    .is_some()
            } else {
                false
            };
            black_box(result)
        });
    });

    group.bench_function("zero_copy_extract_field", |b| {
        let (json_ptr, json_len) = prepare_json(LARGE_JSON);
        let (path_ptr, path_len) = prepare_path("metadata.total");
        let mut output = 0.0f64;
        b.iter(|| unsafe {
            black_box(json_get_number(
                json_ptr,
                json_len,
                path_ptr,
                path_len,
                &mut output as *mut f64,
            ))
        });
    });

    group.finish();
}

// Benchmark: File-based validation (different sizes)
fn bench_file_validation(c: &mut Criterion) {
    let mut group = c.benchmark_group("file_validation");

    let sizes = vec!["1kb", "10kb", "100kb", "1mb", "10mb", "100mb"];

    for size in sizes {
        let file_path = format!("benches/data/test_{}.json", size);

        // Check if file exists, skip if not (for local development)
        if !std::path::Path::new(&file_path).exists() {
            eprintln!(
                "⚠️  Skipping {} - file not found (run: cargo run --bin generate_test_data)",
                size
            );
            continue;
        }

        let json_data =
            fs::read_to_string(&file_path).expect(&format!("Failed to read {}", file_path));

        group.bench_with_input(BenchmarkId::from_parameter(size), &json_data, |b, data| {
            let (ptr, len) = prepare_json(data);
            b.iter(|| unsafe { black_box(json_validate(ptr, len)) });
        });
    }

    group.finish();
}

// Benchmark: File-based field extraction
fn bench_file_field_extraction(c: &mut Criterion) {
    let mut group = c.benchmark_group("file_field_extraction");

    let sizes = vec!["1kb", "10kb", "100kb", "1mb"];

    for size in sizes {
        let file_path = format!("benches/data/test_{}.json", size);

        if !std::path::Path::new(&file_path).exists() {
            continue;
        }

        let json_data =
            fs::read_to_string(&file_path).expect(&format!("Failed to read {}", file_path));

        // Extract a field from the JSON
        group.bench_with_input(
            BenchmarkId::new("get_metadata_total", size),
            &json_data,
            |b, data| {
                let (json_ptr, json_len) = prepare_json(data);
                let (path_ptr, path_len) = prepare_path("metadata.totalUsers");
                let mut output = 0.0f64;
                b.iter(|| unsafe {
                    black_box(json_get_number(
                        json_ptr,
                        json_len,
                        path_ptr,
                        path_len,
                        &mut output as *mut f64,
                    ))
                });
            },
        );
    }

    group.finish();
}

// ============================================
// Phase 2.3: Format Validators Benchmarks
// ============================================

// Benchmark: Email validation
fn bench_validate_email(c: &mut Criterion) {
    let mut group = c.benchmark_group("validate_email");

    group.bench_function("valid_email_simple", |b| {
        let (json_ptr, json_len) = prepare_json(SIMPLE_JSON);
        let (path_ptr, path_len) = prepare_path("email");
        b.iter(|| unsafe {
            black_box(json_validate_email(json_ptr, json_len, path_ptr, path_len))
        });
    });

    group.bench_function("valid_email_nested", |b| {
        let (json_ptr, json_len) = prepare_json(NESTED_JSON);
        let (path_ptr, path_len) = prepare_path("user.email");
        b.iter(|| unsafe {
            black_box(json_validate_email(json_ptr, json_len, path_ptr, path_len))
        });
    });

    group.bench_function("invalid_email", |b| {
        let invalid_json = r#"{"email":"not-an-email"}"#;
        let (json_ptr, json_len) = prepare_json(invalid_json);
        let (path_ptr, path_len) = prepare_path("email");
        b.iter(|| unsafe {
            black_box(json_validate_email(json_ptr, json_len, path_ptr, path_len))
        });
    });

    group.finish();
}

// Benchmark: URL validation
fn bench_validate_url(c: &mut Criterion) {
    let mut group = c.benchmark_group("validate_url");

    group.bench_function("valid_url_https", |b| {
        let (json_ptr, json_len) = prepare_json(SIMPLE_JSON);
        let (path_ptr, path_len) = prepare_path("website");
        b.iter(|| unsafe { black_box(json_validate_url(json_ptr, json_len, path_ptr, path_len)) });
    });

    group.bench_function("valid_url_nested", |b| {
        let (json_ptr, json_len) = prepare_json(NESTED_JSON);
        let (path_ptr, path_len) = prepare_path("user.profile.website");
        b.iter(|| unsafe { black_box(json_validate_url(json_ptr, json_len, path_ptr, path_len)) });
    });

    group.bench_function("invalid_url", |b| {
        let invalid_json = r#"{"website":"not-a-url"}"#;
        let (json_ptr, json_len) = prepare_json(invalid_json);
        let (path_ptr, path_len) = prepare_path("website");
        b.iter(|| unsafe { black_box(json_validate_url(json_ptr, json_len, path_ptr, path_len)) });
    });

    group.finish();
}

// Benchmark: ISO Date validation
fn bench_validate_iso_date(c: &mut Criterion) {
    let mut group = c.benchmark_group("validate_iso_date");

    group.bench_function("valid_datetime", |b| {
        let (json_ptr, json_len) = prepare_json(SIMPLE_JSON);
        let (path_ptr, path_len) = prepare_path("createdAt");
        b.iter(|| unsafe {
            black_box(json_validate_iso_date(
                json_ptr, json_len, path_ptr, path_len,
            ))
        });
    });

    group.bench_function("valid_datetime_nested", |b| {
        let (json_ptr, json_len) = prepare_json(NESTED_JSON);
        let (path_ptr, path_len) = prepare_path("user.profile.lastLogin");
        b.iter(|| unsafe {
            black_box(json_validate_iso_date(
                json_ptr, json_len, path_ptr, path_len,
            ))
        });
    });

    group.bench_function("valid_date_only", |b| {
        let date_json = r#"{"date":"2024-12-30"}"#;
        let (json_ptr, json_len) = prepare_json(date_json);
        let (path_ptr, path_len) = prepare_path("date");
        b.iter(|| unsafe {
            black_box(json_validate_iso_date(
                json_ptr, json_len, path_ptr, path_len,
            ))
        });
    });

    group.bench_function("invalid_date", |b| {
        let invalid_json = r#"{"date":"30-12-2024"}"#;
        let (json_ptr, json_len) = prepare_json(invalid_json);
        let (path_ptr, path_len) = prepare_path("date");
        b.iter(|| unsafe {
            black_box(json_validate_iso_date(
                json_ptr, json_len, path_ptr, path_len,
            ))
        });
    });

    group.finish();
}

// Benchmark: UUID validation
fn bench_validate_uuid(c: &mut Criterion) {
    let mut group = c.benchmark_group("validate_uuid");

    group.bench_function("valid_uuid_v4", |b| {
        let (json_ptr, json_len) = prepare_json(SIMPLE_JSON);
        let (path_ptr, path_len) = prepare_path("userId");
        b.iter(|| unsafe { black_box(json_validate_uuid(json_ptr, json_len, path_ptr, path_len)) });
    });

    group.bench_function("valid_uuid_no_hyphens", |b| {
        let uuid_json = r#"{"uuid":"550e8400e29b41d4a716446655440000"}"#;
        let (json_ptr, json_len) = prepare_json(uuid_json);
        let (path_ptr, path_len) = prepare_path("uuid");
        b.iter(|| unsafe { black_box(json_validate_uuid(json_ptr, json_len, path_ptr, path_len)) });
    });

    group.bench_function("invalid_uuid", |b| {
        let invalid_json = r#"{"uuid":"not-a-uuid"}"#;
        let (json_ptr, json_len) = prepare_json(invalid_json);
        let (path_ptr, path_len) = prepare_path("uuid");
        b.iter(|| unsafe { black_box(json_validate_uuid(json_ptr, json_len, path_ptr, path_len)) });
    });

    group.finish();
}

// Benchmark: Phone Number validation
fn bench_validate_phone_number(c: &mut Criterion) {
    let mut group = c.benchmark_group("validate_phone_number");

    group.bench_function("valid_phone_e164", |b| {
        let (json_ptr, json_len) = prepare_json(SIMPLE_JSON);
        let (path_ptr, path_len) = prepare_path("phone");
        b.iter(|| unsafe {
            black_box(json_validate_phone_number(
                json_ptr, json_len, path_ptr, path_len,
            ))
        });
    });

    group.bench_function("valid_phone_nested", |b| {
        let (json_ptr, json_len) = prepare_json(NESTED_JSON);
        let (path_ptr, path_len) = prepare_path("user.profile.phone");
        b.iter(|| unsafe {
            black_box(json_validate_phone_number(
                json_ptr, json_len, path_ptr, path_len,
            ))
        });
    });

    group.bench_function("valid_phone_plain", |b| {
        let phone_json = r#"{"phone":"+12345678900"}"#;
        let (json_ptr, json_len) = prepare_json(phone_json);
        let (path_ptr, path_len) = prepare_path("phone");
        b.iter(|| unsafe {
            black_box(json_validate_phone_number(
                json_ptr, json_len, path_ptr, path_len,
            ))
        });
    });

    group.bench_function("invalid_phone", |b| {
        let invalid_json = r#"{"phone":"123"}"#;
        let (json_ptr, json_len) = prepare_json(invalid_json);
        let (path_ptr, path_len) = prepare_path("phone");
        b.iter(|| unsafe {
            black_box(json_validate_phone_number(
                json_ptr, json_len, path_ptr, path_len,
            ))
        });
    });

    group.finish();
}

// Benchmark: IP Address validation
fn bench_validate_ip_address(c: &mut Criterion) {
    let mut group = c.benchmark_group("validate_ip_address");

    group.bench_function("valid_ipv4_simple", |b| {
        let (json_ptr, json_len) = prepare_json(SIMPLE_JSON);
        let (path_ptr, path_len) = prepare_path("ip");
        b.iter(|| unsafe {
            black_box(json_validate_ip_address(
                json_ptr, json_len, path_ptr, path_len,
            ))
        });
    });

    group.bench_function("valid_ipv4_array", |b| {
        let (json_ptr, json_len) = prepare_json(LARGE_JSON);
        let (path_ptr, path_len) = prepare_path("users.0.ip");
        b.iter(|| unsafe {
            black_box(json_validate_ip_address(
                json_ptr, json_len, path_ptr, path_len,
            ))
        });
    });

    group.bench_function("valid_ipv6", |b| {
        let ipv6_json = r#"{"ip":"2001:0db8:85a3::8a2e:0370:7334"}"#;
        let (json_ptr, json_len) = prepare_json(ipv6_json);
        let (path_ptr, path_len) = prepare_path("ip");
        b.iter(|| unsafe {
            black_box(json_validate_ip_address(
                json_ptr, json_len, path_ptr, path_len,
            ))
        });
    });

    group.bench_function("valid_ipv6_compressed", |b| {
        let ipv6_json = r#"{"ip":"::1"}"#;
        let (json_ptr, json_len) = prepare_json(ipv6_json);
        let (path_ptr, path_len) = prepare_path("ip");
        b.iter(|| unsafe {
            black_box(json_validate_ip_address(
                json_ptr, json_len, path_ptr, path_len,
            ))
        });
    });

    group.bench_function("invalid_ip", |b| {
        let invalid_json = r#"{"ip":"999.999.999.999"}"#;
        let (json_ptr, json_len) = prepare_json(invalid_json);
        let (path_ptr, path_len) = prepare_path("ip");
        b.iter(|| unsafe {
            black_box(json_validate_ip_address(
                json_ptr, json_len, path_ptr, path_len,
            ))
        });
    });

    group.finish();
}

// ============================================
// NEW: Detailed Validation with Location Info
// ============================================

// Benchmark: json_validate_with_location (returns line/column/byte_offset)
fn bench_validate_with_location(c: &mut Criterion) {
    let mut group = c.benchmark_group("validate_with_location");

    group.bench_function("valid_simple", |b| {
        let (ptr, len) = prepare_json(SIMPLE_JSON);
        let mut error_out = json_validator_ffi::types::DetailedError {
            code: 0,
            line: 0,
            column: 0,
            byte_offset: 0,
        };
        b.iter(|| unsafe {
            json_validate_with_location(ptr, len, &mut error_out as *mut _);
            black_box(&error_out)
        });
    });

    group.bench_function("invalid_json_with_location", |b| {
        let invalid_json = r#"{"name":"John","age":30,"broken"}"#;
        let (ptr, len) = prepare_json(invalid_json);
        let mut error_out = json_validator_ffi::types::DetailedError {
            code: 0,
            line: 0,
            column: 0,
            byte_offset: 0,
        };
        b.iter(|| unsafe {
            json_validate_with_location(ptr, len, &mut error_out as *mut _);
            black_box(&error_out)
        });
    });

    group.bench_function("valid_nested", |b| {
        let (ptr, len) = prepare_json(NESTED_JSON);
        let mut error_out = json_validator_ffi::types::DetailedError {
            code: 0,
            line: 0,
            column: 0,
            byte_offset: 0,
        };
        b.iter(|| unsafe {
            json_validate_with_location(ptr, len, &mut error_out as *mut _);
            black_box(&error_out)
        });
    });

    group.finish();
}

// ============================================
// NEW: Dynamic Buffer String Extraction
// ============================================

// Benchmark: json_get_string_size (2-step approach for dynamic buffer)
fn bench_get_string_size(c: &mut Criterion) {
    let mut group = c.benchmark_group("get_string_size");

    group.bench_function("simple_string_size", |b| {
        let (json_ptr, json_len) = prepare_json(SIMPLE_JSON);
        let (path_ptr, path_len) = prepare_path("name");
        b.iter(|| unsafe {
            black_box(json_get_string_size(json_ptr, json_len, path_ptr, path_len))
        });
    });

    group.bench_function("nested_string_size", |b| {
        let (json_ptr, json_len) = prepare_json(NESTED_JSON);
        let (path_ptr, path_len) = prepare_path("user.profile.bio");
        b.iter(|| unsafe {
            black_box(json_get_string_size(json_ptr, json_len, path_ptr, path_len))
        });
    });

    group.bench_function("email_string_size", |b| {
        let (json_ptr, json_len) = prepare_json(SIMPLE_JSON);
        let (path_ptr, path_len) = prepare_path("email");
        b.iter(|| unsafe {
            black_box(json_get_string_size(json_ptr, json_len, path_ptr, path_len))
        });
    });

    group.finish();
}

// Benchmark: 2-step dynamic buffer extraction (get_string_size + get_string)
fn bench_dynamic_string_extraction(c: &mut Criterion) {
    let mut group = c.benchmark_group("dynamic_string_extraction");

    group.bench_function("two_step_simple", |b| {
        let (json_ptr, json_len) = prepare_json(SIMPLE_JSON);
        let (path_ptr, path_len) = prepare_path("name");
        b.iter(|| unsafe {
            // Step 1: Get size
            let size = json_get_string_size(json_ptr, json_len, path_ptr, path_len);
            if size > 0 {
                // Step 2: Allocate exact buffer and extract
                let mut buffer = vec![0u8; (size + 1) as usize];
                json_get_string(
                    json_ptr,
                    json_len,
                    path_ptr,
                    path_len,
                    buffer.as_mut_ptr(),
                    buffer.len(),
                );
                black_box(buffer)
            } else {
                black_box(size)
            }
        });
    });

    group.bench_function("two_step_long_string", |b| {
        // JSON with a long string (>256 chars to test dynamic buffer benefit)
        let long_json = r#"{"description":"This is a very long description that exceeds the typical fixed buffer size. It contains multiple sentences and lots of information about the product, its features, benefits, and use cases. This string is intentionally long to benchmark the dynamic buffer allocation approach versus a fixed-size buffer approach. The dynamic approach should handle this without any issues or truncation."}"#;
        let (json_ptr, json_len) = prepare_json(long_json);
        let (path_ptr, path_len) = prepare_path("description");
        b.iter(|| unsafe {
            let size = json_get_string_size(json_ptr, json_len, path_ptr, path_len);
            if size > 0 {
                let mut buffer = vec![0u8; (size + 1) as usize];
                json_get_string(
                    json_ptr,
                    json_len,
                    path_ptr,
                    path_len,
                    buffer.as_mut_ptr(),
                    buffer.len(),
                );
                black_box(buffer)
            } else {
                black_box(size)
            }
        });
    });

    group.finish();
}

// Comparison: Fixed buffer vs Dynamic buffer (old vs new approach)
fn bench_buffer_comparison(c: &mut Criterion) {
    let mut group = c.benchmark_group("buffer_comparison");

    // OLD: Fixed 4KB buffer (always allocates 4KB even for small strings)
    group.bench_function("fixed_buffer_4kb", |b| {
        let (json_ptr, json_len) = prepare_json(SIMPLE_JSON);
        let (path_ptr, path_len) = prepare_path("name");
        let mut buffer = [0u8; 4096]; // Fixed 4KB buffer
        b.iter(|| unsafe {
            json_get_string(
                json_ptr,
                json_len,
                path_ptr,
                path_len,
                buffer.as_mut_ptr(),
                buffer.len(),
            );
            black_box(&buffer)
        });
    });

    // NEW: Dynamic buffer (allocates exact size needed)
    group.bench_function("dynamic_buffer", |b| {
        let (json_ptr, json_len) = prepare_json(SIMPLE_JSON);
        let (path_ptr, path_len) = prepare_path("name");
        b.iter(|| unsafe {
            let size = json_get_string_size(json_ptr, json_len, path_ptr, path_len);
            if size > 0 {
                let mut buffer = vec![0u8; (size + 1) as usize];
                json_get_string(
                    json_ptr,
                    json_len,
                    path_ptr,
                    path_len,
                    buffer.as_mut_ptr(),
                    buffer.len(),
                );
                black_box(buffer)
            } else {
                black_box(size)
            }
        });
    });

    group.finish();
}

criterion_group!(
    benches,
    bench_json_validate,
    bench_validate_field_type,
    bench_field_exists,
    bench_validate_number_range,
    bench_validate_string_length,
    bench_validate_array_size,
    bench_field_extraction,
    bench_comparison,
    bench_file_validation,
    bench_file_field_extraction,
    // Phase 2.3: Format Validators
    bench_validate_email,
    bench_validate_url,
    bench_validate_iso_date,
    bench_validate_uuid,
    bench_validate_phone_number,
    bench_validate_ip_address,
    // NEW: Zero-copy integration features
    bench_validate_with_location,
    bench_get_string_size,
    bench_dynamic_string_extraction,
    bench_buffer_comparison
);
criterion_main!(benches);
