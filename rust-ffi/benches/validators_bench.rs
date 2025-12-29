use criterion::{black_box, criterion_group, criterion_main, BenchmarkId, Criterion};
use json_validator_ffi::*;
use simd_json::prelude::*;
use std::fs;

// Test JSON data
const SIMPLE_JSON: &str = r#"{"name":"John","age":30,"email":"john@example.com","active":true}"#;
const NESTED_JSON: &str = r#"{"user":{"name":"Alice","age":25,"email":"alice@example.com","profile":{"bio":"Software Engineer","skills":["Rust","Java","Python"]}}}"#;
const LARGE_JSON: &str = r#"{"users":[{"id":1,"name":"User1","age":25,"email":"user1@example.com"},{"id":2,"name":"User2","age":30,"email":"user2@example.com"},{"id":3,"name":"User3","age":35,"email":"user3@example.com"},{"id":4,"name":"User4","age":40,"email":"user4@example.com"},{"id":5,"name":"User5","age":45,"email":"user5@example.com"}],"metadata":{"total":5,"page":1}}"#;

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
    bench_file_field_extraction
);
criterion_main!(benches);
