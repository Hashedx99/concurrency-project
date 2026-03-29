# Concurrent CSV Data Processor
---

## Project Overview

A Spring Boot application that reads employee data from a CSV file and applies salary increments **concurrently** using Java's `Executor` framework. The application enforces three business rules simultaneously across all employee records:

1. **Project completion threshold** — employees below 60% completion receive no raise
2. **Tenure raise** — 2% per completed year of service (min 1 full year)
3. **Role raise** — Director (+5%), Manager (+2%), Employee (+1%)
4. **Bonus multiplier** — employees above 80% completion have their raise multiplied by ×1.5

Results are persisted as JSON documents in a MongoDB-inspired file structure.

---

## Technologies Used

- Java 17
- Spring Boot
- OpenCSV (CSV parsing)
- Jackson Databind + JavaTimeModule (JSON persistence)
- Java `ExecutorService` + `Semaphore` + `AtomicInteger` (concurrency)
- Maven

---

## Project Structure

```
csv-processor/
├── src/main/java/com/ga/csvprocessor/
│   ├── CsvProcessorApplication.java       # Spring Boot entry point
│   ├── config/
│   │   └── ThreadPoolConfig.java          # ExecutorService + Semaphore beans
│   ├── controller/
│   │   └── CsvProcessorController.java    # MVC + REST endpoints
│   ├── dto/
│   │   └── ProcessingBatch.java           # Groups results + summary stats
│   ├── entity/
│   │   ├── Employee.java                  # Employee data + eligibility logic
│   │   └── ProcessingResult.java          # Immutable result record (JSON-mapped)
│   ├── enums/
│   │   ├── EmployeeRole.java              # DIRECTOR / MANAGER / EMPLOYEE
│   │   └── ProcessingStatus.java          # PENDING / PROCESSING / COMPLETED / SKIPPED / FAILED
│   ├── exception/
│   │   ├── CsvFileNotFoundException.java
│   │   └── CsvParseException.java
│   ├── interfaces/
│   │   └── FileHandler.java               # Consistent I/O contract
│   ├── repository/
│   │   ├── CsvFileHandler.java            # Reads CSV → List<Employee>
│   │   └── ResultFileHandler.java         # Persists List<ProcessingResult> → JSON
│   ├── service/
│   │   ├── SalaryCalculator.java          # Stateless, thread-safe calculation logic
│   │   ├── EmployeeProcessorTask.java     # Callable task per employee
│   │   └── CsvProcessorService.java       # Orchestrates thread pool + persistence
│   └── util/
│       └── CommonUtil.java                # Shared formatting utilities
├── src/main/resources/
│   └── application.properties
├── Data/
│   ├── Uploads/
│       └── test_employees.csv                  # Sample CSV for testing; uploading a file with the same name will overwrite it
└── pom.xml
```

---

## Setup & Running

### Prerequisites

- Java 17
- Maven

### Steps

```bash
# Clone / navigate to the project
cd csv-processor

# Build
./mvnw clean package

# Run
./mvnw spring-boot:run
```

use `http://localhost:8080` in your api tool (POSTMAN).

### Using the application
**[Concurrency Project API](https://www.postman.com/hashedx99-2376480/workspace/concurrency-project/collection/50802721-3dcdff07-7e7e-4e03-b520-4f2dcb3485d5)**
1. Upload the `Data/Uploads/test_employees.csv` (or any CSV matching the format below) via the /api/process endpoint
2. View results in the generated JSON file under `Data/ProcessedResults/`
3. Results will also be logged to the console with before/after salary and raise details

#### Endpoint Details
| Endpoint               | Method | Description                       | Usage                                                                                          |
|------------------------|--------|-----------------------------------|------------------------------------------------------------------------------------------------|
| `/api/process`         | POST   | Upload CSV file for processing    | set Content-Type to form-data, and upload the file under the key `file`. set the type to file. |
| `/api/batches`         | GET    | List previously processed batches | -                                                                                              |
| `/api/batches/{batchId}` | GET    | Get batch details                 | -                                                                                              |
| `/api/uploads`         | GET    | Get all uploaded files            | -                                                                                              |


---

## CSV Format

```
id,name,salary,joined_date,role,project_completion_percentage
Alice Johnson,85000,2019-03-15,Director,0.92
Bob Smith,60000,2021-07-01,Manager,0.75
Carol White,45000,2023-01-10,Employee,0.55
```

| Column                          | Type      | Format                                               |
|---------------------------------|-----------|------------------------------------------------------|
| `id`                            | Integer   | Numeric                                              |
| `name`                          | String    | Any                                                  |
| `salary`                        | Double    | Numeric                                              |
| `joined_date`                   | LocalDate | `yyyy-MM-dd`                                         |
| `role`                          | Enum      | `Director`, `Manager`, `Employee` (case-insensitive) |
| `project_completion_percentage` | Double    | `0–1`                                                |

---

## Business Rules

### 1. Project Threshold

| Completion | Outcome |
|---|---|
| < 60% | **No raise** — status: SKIPPED |
| 60–80% | Standard raise applies |
| > 80% | Standard raise × **1.5 bonus multiplier** |

### 2. Tenure Raise

- **+2% per completed year** of service
- Only applied if the employee has served **at least 1 full year**
- Example: 3 years → +6%

### 3. Role Raise

| Role | Raise |
|---|---|
| Director | +5% |
| Manager | +2% |
| Employee | +1% |

### Combined Example

> Alice — Director, 3 years, 92% completion
> - Tenure raise: 3 × 2% = 6%
> - Role raise: 5%
> - Subtotal: 11%
> - Bonus multiplier (>80%): 11% × 1.5 = **16.5% total raise**

---

## Concurrency Design

```
CsvProcessorService
 └── reads all employees from CSV
 └── submits one EmployeeProcessorTask (Callable) per employee
      └── runs on a thread from the FixedThreadPool (CPU cores)
 └── collects List<Future<ProcessingResult>>
 └── ResultFileHandler persists JSON
```

| Component | Role |
|---|---|
| `ExecutorService` (fixed pool) | Concurrent task execution, one thread per CPU core |
| `Semaphore` (permits: 3) | Limits concurrent writes to the result object — prevents race conditions |
| `AtomicInteger` | Lock-free global progress counter across threads |
| `SalaryCalculator` | Stateless — safely shared across all threads with no locking needed |

---

## File Storage

Results are stored as JSON documents, using a NoSQL-inspired design:

```
Data/
 ├── ProcessedResults/   ← one JSON document per batch run
 │    └── 20250101_120000_abc12345.json
 └── Uploads/            ← uploaded CSVs (runtime)
```

Each batch document contains the full list of `ProcessingResult` records including salary before/after, raise %, bonus flag, thread name, and processing timestamp.

---
## Conclusion
This project demonstrates how to design a concurrent data processing application in Java using Spring Boot, while adhering to complex business rules and ensuring thread safety. By leveraging Java's concurrency utilities and a clean architecture, we can efficiently process large datasets while maintaining code clarity and robustness.
