# AMEX Lumi – Data Ingestion Case Study

## Overview

AMEX Lumi is an end-to-end employee data ingestion platform that accepts employee data files, validates and cleanses records, encrypts sensitive fields, orchestrates processing, and loads valid records into PostgreSQL.

The project is implemented in three phases:

- **Phase 1:** File ingestion, Apache Beam processing, validation, cleansing, encryption, metadata enrichment, error handling, Airflow orchestration, and PostgreSQL loading.
- **Phase 2:** Large-file processing using PySpark to split files into dynamically generated chunks for parallel ingestion.
- **Phase 3:** Control-file based record-count validation to ensure the expected number of records were loaded.

A Spring Boot API also provides employee retrieval endpoints with decryption of protected fields.

---

## Table of Contents

- [Overview](#overview).
- [Technology Stack](#technology-stack).
- [High-Level Architecture](#high-level-architecture).
- [Project Objectives](#project-objectives).
- [Project Structure](#project-structure).
- [Phase 1 – Basic Ingestion](#phase-1--basic-ingestion).
- [End-to-End Flow](#end-to-end-flow).
- [Spring Boot Ingestion API](#spring-boot-ingestion-api).
- [Ingestion Endpoint](#ingestion-endpoint).
- [Apache Airflow](#apache-airflow).
- [Apache Beam Pipeline](#apache-beam-pipeline).
- [Validation and Partial Error Handling](#validation-and-partial-error-handling).
- [Data Cleansing](#data-cleansing).
- [Encryption](#encryption).
- [Encryption Algorithm](#encryption-algorithm).
- [Key Derivation](#key-derivation).
- [IV Generation](#iv-generation).
- [Why AES-GCM?](#why-aes-gcm).
- [Decryption](#decryption).
- [Flow](#flow).
- [Phase 2 – Large File Processing](#phase-2--large-file-processing).
- [Why PySpark?](#why-pyspark).
- [Parallel Processing](#parallel-processing).
- [Phase 3 – Record Count Validation](#phase-3--record-count-validation).
- [Control File](#control-file).
- [Validation](#validation).
- [Automatic Database Initialization](#automatic-database-initialization).
- [PostgreSQL](#postgresql).
- [Employee Retrieval APIs](#employee-retrieval-apis).
- [Get All Employees](#get-all-employees).
- [Get Employee by ID](#get-employee-by-id).
- [Error Handling](#error-handling).
- [Database and Environment Configuration](#database-and-environment-configuration).
- [Testing](#testing).
- [Spring Boot Tests](#spring-boot-tests).
- [Beam Tests](#beam-tests).
- [JaCoCo](#jacoco).
- [Running the Project](#running-the-project).
- [Example Control File](#example-control-file).
- [Example Scenarios](#example-scenarios).
- [Key Design Decisions](#key-design-decisions).
- [Production-Level Improvements](#production-level-improvements).
- [Project Summary](#project-summary).
- [Author](#author).

---

## Technology Stack

| Technology | Purpose |
|---|---|
| Java 17 | Backend and Apache Beam implementation |
| Spring Boot | REST API and ingestion orchestration |
| Apache Beam | Parsing, validation, cleansing, encryption, metadata, and database loading |
| Apache Airflow | Workflow orchestration |
| PySpark / Spark 3.5.6 | Large-file splitting |
| PostgreSQL | Target database |
| Maven | Java build and dependency management |
| JUnit 5 | Unit testing |
| Mockito | Mocking dependencies |
| JaCoCo | Code coverage |
| Python | Airflow DAG and PySpark splitter |

---

# High-Level Architecture

```text
                         ┌──────────────────────┐
                         │    Client / User      │
                         └──────────┬───────────┘
                                    │
                                    │ Multipart REST API
                                    ▼
                         ┌──────────────────────┐
                         │     Spring Boot      │
                         │    Ingestion API     │
                         └──────────┬───────────┘
                                    │
                              File Size Check
                                    │
                         ┌──────────┴──────────┐
                         │                     │
                    Small File             Large File
                         │                     │
                         │                     ▼
                         │                 PySpark
                         │              File Splitting
                         │                     │
                         │                Chunks
                         │                     │
                         └──────────┬──────────┘
                                    │
                                    ▼
                         ┌──────────────────────┐
                         │     Apache Airflow   │
                         │  lumi_ingestion_dag  │
                         └──────────┬───────────┘
                                    │
                                    ▼
                         ┌──────────────────────┐
                         │     Apache Beam      │
                         │  Ingestion Pipeline  │
                         └──────────┬───────────┘
                                    │
                    ┌───────────────┼────────────────┐
                    │               │                │
                  Parse          Validate         Metadata
                                    │
                           ┌────────┴────────┐
                           │                 │
                         Valid             Invalid
                           │                 │
                           ▼                 ▼
                        Cleanse         Error File
                           │
                           ▼
                       Encrypt
                           │
                           ▼
                      Add Metadata
                           │
                           ▼
                    ┌───────────────┐
                    │  PostgreSQL   │
                    │   employees   │
                    └───────┬───────┘
                            │
                            ▼
                  Record Count Validation
                            │
                     ┌──────┴──────┐
                     │             │
                   Match        Mismatch
                     │             │
                  Success        Failure
```

---

# Project Objectives

The platform addresses the following requirements:

1. Ingest employee data from supported file formats.
2. Validate and cleanse employee records.
3. Continue ingestion when individual records are invalid.
4. Write invalid records to an error file.
5. Add ingestion metadata to valid records.
6. Encrypt sensitive employee fields before storing them.
7. Orchestrate ingestion using Airflow.
8. Split large files using PySpark.
9. Load valid records into PostgreSQL.
10. Validate the final database record count against a control file.
11. Provide employee retrieval APIs through Spring Boot.

---

# Project Structure

```text
Major_Capstone_Amex_Lumi/
│
├── amex-lumi-ingestion-api/
│   └── src/
│       ├── main/
│       │   └── java/
│       │       └── com/amex/lumi/ingestion/
│       │           ├── controller/
│       │           ├── service/
│       │           ├── repository/
│       │           ├── client/
│       │           ├── dto/
│       │           └── exception/
│       │
│       └── test/
│
├── beam-ingestion/
│   └── src/
│       ├── main/
│       │   └── java/
│       │       └── com/amex/lumi/beam/
│       │           ├── pipeline/
│       │           ├── parser/
│       │           ├── validation/
│       │           ├── cleansing/
│       │           ├── model/
│       │           └── config/
│       │
│       └── test/
│
├── airflow/
│   ├── dags/
│   │   └── lumi_ingestion_dag.py
│   ├── airflow_505/
│   |── .env
|   |__ docker-compose.yaml
│   |__ DockerFile
|
├── database/
│   └── schema.sql
│
├── pyspark/
│   └── split_file.py
│
└── README.md
```

---

# Phase 1 – Basic Ingestion

## End-to-End Flow

```text
Input CSV/JSON
      |
      v
Spring Boot POST /api/v1/ingestions
      |
      v
Generate Execution ID
      |
      v
Trigger Airflow DAG
      |
      v
Apache Beam
      |
      +--> Parse
      |
      +--> Trim Whitespace
      |
      +--> Validate
              |
              +--> Invalid -> Error File
              |
              +--> Valid
                     |
                     +--> Cleanse
                     |
                     +--> Encrypt Sensitive Fields
                     |
                     +--> Add Metadata
                     |
                     +--> PostgreSQL
```

The current Spring API accepts CSV and JSON employee input files and a `.properties` control file.

---

# Spring Boot Ingestion API

## Ingestion Endpoint

```http
POST /api/v1/ingestions
```

The request uses `multipart/form-data` and contains:

- Employee input file
- Control file

The service:

1. Validates the input file.
2. Validates the control file.
3. Saves the uploaded files.
4. Generates a unique execution ID.
5. Checks the input file size against the configured threshold.
6. Triggers the appropriate Airflow flow.

---

# Apache Airflow

The main DAG is:

```text
lumi_ingestion_dag
```

Spring Boot triggers the DAG through the Airflow REST API.

For a small file, the DAG receives:

```json
{
  "file_location": "...",
  "control_file_location": "...",
  "execution_id": "..."
}
```

For a large file, the DAG receives a list of generated chunk locations:

```json
{
  "file_locations": [
    ".../part-00000.csv",
    ".../part-00001.csv"
  ],
  "control_file_location": "...",
  "execution_id": "..."
}
```

---

# Apache Beam Pipeline

Apache Beam performs the core employee record processing.

The major transformations are:

```text
Read Input
   |
   v
Parse Employee
   |
   v
Trim Whitespace
   |
   v
Validate Employee
   |
   +---- Invalid ----> Error File
   |
   +---- Valid
          |
          v
       Cleanse
          |
          v
       Encrypt
          |
          v
       Metadata
          |
          v
       PostgreSQL
```

## Validation and Partial Error Handling

Invalid records do not cause the complete ingestion to fail.

The validation stage separates records into valid and invalid outputs:

```text
                Validate
                   |
            +------+------+
            |             |
          Valid         Invalid
            |             |
            v             v
       Continue       Error File
```

This allows valid records to continue processing while invalid records are captured separately.

---

# Data Cleansing

The Beam pipeline performs cleansing before database loading.

One important cleansing operation is whitespace trimming, which removes unnecessary leading and trailing whitespace from input values before further processing.

---

# Encryption

Sensitive employee fields are encrypted before they are stored in PostgreSQL.

Currently protected fields include:

- `phone_number`
- `salary`
- `emergency_contact_phone`

## Encryption Algorithm

The implementation uses:

```text
AES-256-GCM
```

AES is a symmetric encryption algorithm and GCM is an authenticated encryption mode.

### Key Derivation

The configured secret is processed using SHA-256:

```text
Secret String
      |
      v
   SHA-256
      |
      v
256-bit AES Key
```

SHA-256 is used for key derivation in this implementation; it is not the actual data-encryption algorithm.

### IV Generation

A new random 12-byte initialization vector is generated for each encryption operation.

```text
Plaintext + AES Key + Random IV
              |
              v
           AES-GCM
              |
              v
Ciphertext + Authentication Tag
```

The IV and encrypted data are Base64 encoded and stored together:

```text
Base64(IV):Base64(EncryptedData)
```

### Why AES-GCM?

AES-GCM provides confidentiality along with authentication/integrity protection.

Generating a fresh IV for each encryption also means that encrypting the same plaintext multiple times does not normally produce the same ciphertext.

---

# Decryption

The Spring Boot application decrypts sensitive fields when employee data is retrieved.

## Flow

```text
PostgreSQL
    |
    v
Encrypted Value
    |
    v
EmployeeService
    |
    v
DecryptionService
    |
    +--> Extract IV
    |
    +--> Base64 Decode
    |
    +--> Derive Same AES Key
    |
    +--> AES-GCM Decrypt
    |
    v
Plaintext
    |
    v
API Response
```

The decryption service extracts the IV and encrypted data, derives the same AES key from the configured secret, and performs AES-GCM decryption.

---

# Phase 2 – Large File Processing

Phase 2 introduces PySpark for large input files.

## Why PySpark?

Large files can be split into smaller chunks so that chunks can be processed independently.

```text
Large File
    |
    v
Spring Boot File Size Check
    |
    v
PySpark
    |
    v
Dynamic Chunks
    |
    v
Airflow
    |
    v
Beam
    |
    v
PostgreSQL
```

The large-file threshold is configurable through Spring properties.

Example:

```properties
ingestion.large-file-threshold=1MB
```

The implementation does not assume a fixed number of chunks. PySpark creates partitions/chunks based on the input processing, and Spring dynamically discovers the generated `part-*` files.

## Parallel Processing

The implementation uses:

```text
MAX_PARALLEL_CHUNKS = 4
```

This is a concurrency limit, not a limit on the total number of chunks.

For example, if PySpark generates 10 chunks:

```text
10 chunks generated
       |
       v
Maximum 4 processed concurrently
       |
       v
All 10 eventually processed
```

---

# Phase 3 – Record Count Validation

Phase 3 introduces a control-file based record-count check.

## Control File

The control file is a properties file containing:

```properties
record_count=10000
```

This represents the expected number of records.

## Validation

After ingestion, Airflow queries PostgreSQL using the current execution ID:

```sql
SELECT COUNT(*)
FROM employees
WHERE execution_id = %s;
```

The actual count is compared with the expected count.

```text
Expected Count
      |
      +------ Compare ------+
                            |
                     Actual DB Count
                            |
                 +----------+----------+
                 |                     |
               Match               Mismatch
                 |                     |
              Success               Failure
```

A mismatch produces a clear error containing both values, for example:

```text
Record count mismatch.
Expected: 10000
Actual: 9997
```

The execution ID ensures that records from the current ingestion run are counted.

---

# Automatic Database Initialization

The project keeps the PostgreSQL table definition in:

```text
database/schema.sql
```

The schema file is mounted into the Airflow environment at:

```text
/opt/airflow/database/schema.sql
```

`DatabaseInitializer` executes the existing schema file before the PostgreSQL write operation.

The schema uses:

```sql
CREATE TABLE IF NOT EXISTS employees (...)
```

Therefore, when the table is absent, it can be created automatically during Beam setup.

The table-creation SQL is maintained in `schema.sql` rather than being duplicated inside `WriteToPostgresDoFn`.

---

# PostgreSQL

The target table is:

```text
employees
```

The table stores employee information and ingestion metadata.

Important metadata fields are:

- `execution_id`
- `ingestion_timestamp`
- `source_creation_time`

The execution ID is used to associate records with a particular ingestion run and to perform the Phase 3 record-count validation.

---

# Employee Retrieval APIs

The Spring Boot application exposes read APIs.

## Get All Employees

```http
GET /api/v1/employees
```

Returns all employees.

If there are no records:

```json
[]
```

## Get Employee by ID

```http
GET /api/v1/employees/{employeeId}
```

Example:

```http
GET /api/v1/employees/EMP1001
```

The response contains decrypted sensitive fields.

If the employee does not exist, the API returns HTTP `404 Not Found` with an appropriate error message.

---

# Error Handling

The application uses custom exceptions:

- `IngestionException`
- `InvalidInputFileException`
- `ControlFileException`
- `RecordCountMismatchException`
- `EmployeeNotFoundException`

`GlobalExceptionHandler` converts application exceptions into consistent HTTP responses.

For example:

```text
EmployeeNotFoundException
          |
          v
GlobalExceptionHandler
          |
          v
HTTP 404 Not Found
```

---

# Database and Environment Configuration

Beam database configuration is supplied through environment variables:

```text
DB_HOST
DB_PORT
DB_NAME
DB_USER
DB_PASSWORD
```

The Spring application has its own database configuration for the employee GET APIs.

Credentials should not be committed to source control.

---

# Testing

The project contains unit tests for Spring Boot and Beam components.

## Spring Boot Tests

Tests cover areas including:

- Input validation
- Control-file validation
- Small-file ingestion
- Employee service
- Employee controller
- Employee retrieval
- Decryption
- Airflow Client

## Beam Tests

Tests cover components including:

- CSV parsing
- JSON parsing
- Employee cleansing
- Employee validation
- Employee parsing
- Encryption
- Metadata processing

The Beam-to-PostgreSQL flow has also been verified through end-to-end ingestion testing.

## JaCoCo

JaCoCo is configured for coverage reporting.

Current report:

```text
test Coverage: 64%
```

Report location:

```text
target/site/jacoco/index.html
```

---

# Running the Project

## 1. Start Airflow

Start the project's Docker Compose based Airflow environment.

Verify that the Airflow web interface is available.

## 2. Start PostgreSQL

Ensure PostgreSQL is running and the target database is available.

Database:

```text
amex_lumi
```

## 3. Build Beam

From the Beam project:

```bash
mvn clean package
```

Place the generated Beam JAR in the location used by the Airflow ingestion task.

## 4. Start Spring Boot

Run the Spring Boot application from IntelliJ IDEA.

The API runs on:

```text
http://localhost:8080
```

## 5. Trigger Ingestion

Use:

```http
POST /api/v1/ingestions
```

and provide:

- Employee CSV/JSON file
- Control `.properties` file

---

# Example Control File

```properties
record_count=10000
```

---

# Example Scenarios

## Small File

```text
File
 ↓
Spring Boot
 ↓
Size <= Threshold
 ↓
Airflow
 ↓
Beam
 ↓
PostgreSQL
```

## Large File

```text
File
 ↓
Spring Boot
 ↓
Size > Threshold
 ↓
PySpark
 ↓
Dynamic Chunks
 ↓
Airflow
 ↓
Beam
 ↓
PostgreSQL
```

## Invalid Records

```text
Input
 ↓
Beam Validation
 ├── Valid → Processing → PostgreSQL
 └── Invalid → Error File
```

## Count Mismatch

```text
Control File: 10000
Database:      9997
        ↓
Ingestion fails
        ↓
Expected and actual counts reported
```

---

# Key Design Decisions

### Spring Boot

Used as the API layer and ingestion entry point. It validates files, generates execution IDs, checks file size, and triggers Airflow.

### Airflow

Used to orchestrate the ingestion workflow and coordinate the Beam processing steps.

### Apache Beam

Used for record-level parsing, validation, cleansing, encryption, metadata enrichment, and PostgreSQL loading.

### PySpark

Used for large-file splitting before Beam processing.

### PostgreSQL

Used as the target relational database.

### AES-256-GCM

Used for protecting sensitive employee fields.

### Execution ID

A unique execution ID is generated for every ingestion. It is stored with loaded records and used to isolate records for count validation.

### Schema Initialization

Database initialization is kept separate from the write SQL. `DatabaseInitializer` executes the central `schema.sql` file.

---

# Production-Level Improvements

Possible future improvements include:

- External secret management using a secret manager or KMS
- Encryption key rotation
- More comprehensive integration testing
- Better abstraction around external PySpark process execution
- More repository and Airflow client test coverage
- Structured monitoring and alerting
- Production deployment configuration
- Additional input formats if required

---

# Project Summary

AMEX Lumi is an end-to-end employee data ingestion platform combining Spring Boot, Apache Airflow, Apache Beam, PySpark, and PostgreSQL.

The system provides:

- CSV and JSON ingestion
- Control-file support
- Input validation
- Partial invalid-record handling
- Data cleansing
- AES-256-GCM encryption
- Spring-based decryption
- Metadata enrichment
- Large-file splitting using PySpark
- Dynamic chunk handling
- Airflow orchestration
- PostgreSQL loading
- Automatic database schema initialization
- Execution-based record-count validation
- Employee GET APIs
- Unit and end-to-end testing
- JaCoCo coverage reporting

---

# Author

**Mahak Dhanotiya**