# 🔍 Troubleshooting 500 Error - Report Generation

## 📋 Checklist to Fix the Issue

### ✅ Step 1: Check Backend Console Logs

**Look for error messages in your backend terminal.** Common errors:

#### Error Type 1: Table Not Found
```
Table 'lankamed_db.visit' doesn't exist
Table 'lankamed_db.report_audit' doesn't exist
```

**Solution:** Run the migration script

#### Error Type 2: Column Not Found
```
Unknown column 'hospital_id' in 'field list'
Unknown column 'gender' in 'field list'
```

**Solution:** Update your visit table schema

#### Error Type 3: User Not Found / Null
```
NullPointerException at ReportService.java:52
```

**Solution:** Check authentication token

#### Error Type 4: No Data Provider
```
Unknown reportType: PATIENT_VISIT
```

**Solution:** Provider beans not registered

---

### ✅ Step 2: Verify Database Tables

Run these SQL commands in your MySQL:

```sql
-- 1. Check if tables exist
SHOW TABLES LIKE '%visit%';
SHOW TABLES LIKE '%report_audit%';

-- 2. Check visit table structure
DESCRIBE visit;

-- 3. Check if visit table has the required columns
SELECT COLUMN_NAME 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'visit' 
  AND TABLE_SCHEMA = 'lankamed_db'
  AND COLUMN_NAME IN ('hospital_id', 'service_category', 'patient_category', 'gender', 'age');

-- 4. Check if visit table has data
SELECT COUNT(*) FROM visit;

-- 5. Check report_audit structure
DESCRIBE report_audit;
```

**Expected columns in `visit` table:**
- `id` (BIGINT, PRIMARY KEY)
- `patient_id` (BIGINT, NOT NULL)
- `visit_date` (DATETIME, NOT NULL)
- `notes` (TEXT)
- `hospital_id` (VARCHAR(20))
- `service_category` (VARCHAR(50))
- `patient_category` (VARCHAR(50))
- `gender` (VARCHAR(10))
- `age` (INT)

**Expected columns in `report_audit` table:**
- `id` (BIGINT, PRIMARY KEY)
- `user_id` (BIGINT, NOT NULL)
- `report_type` (VARCHAR(100), NOT NULL)
- `criteria_json` (TEXT, NOT NULL)
- `generated_on` (DATETIME, NOT NULL)
- `success` (BOOLEAN, NOT NULL)
- `notes` (TEXT)
- `hospital_id` (VARCHAR(20))
- `service_category` (VARCHAR(50))
- `patient_category` (VARCHAR(50))
- `gender` (VARCHAR(10))
- `min_age` (INT)
- `max_age` (INT)

---

### ✅ Step 3: Add Test Data

If `visit` table is empty, add some test data:

```sql
-- First, check if you have patients
SELECT id FROM patient LIMIT 5;

-- If you have patients, add test visits
INSERT INTO visit (patient_id, visit_date, hospital_id, service_category, patient_category, gender, age, notes)
VALUES 
  (1, '2024-06-15 10:30:00', 'C001', 'OPD', 'OUTPATIENT', 'MALE', 35, 'Regular checkup'),
  (1, '2024-06-20 14:00:00', 'C001', 'LAB', 'OUTPATIENT', 'MALE', 35, 'Blood test'),
  (2, '2024-06-18 09:00:00', 'C001', 'OPD', 'OUTPATIENT', 'FEMALE', 28, 'Consultation'),
  (2, '2024-06-25 11:30:00', 'C002', 'RADIOLOGY', 'OUTPATIENT', 'FEMALE', 28, 'X-ray'),
  (3, '2024-07-01 15:00:00', 'C001', 'SURGERY', 'INPATIENT', 'MALE', 42, 'Minor surgery');

-- Verify data was inserted
SELECT * FROM visit;
```

**Note:** Replace `patient_id` values (1, 2, 3) with actual patient IDs from your database.

---

### ✅ Step 4: Fix Database Schema Issues

If your `visit` table is missing columns, run this:

```sql
-- Add missing columns to visit table
ALTER TABLE visit 
  ADD COLUMN IF NOT EXISTS hospital_id VARCHAR(20),
  ADD COLUMN IF NOT EXISTS service_category VARCHAR(50),
  ADD COLUMN IF NOT EXISTS patient_category VARCHAR(50),
  ADD COLUMN IF NOT EXISTS gender VARCHAR(10),
  ADD COLUMN IF NOT EXISTS age INT;

-- Verify columns were added
DESCRIBE visit;
```

If `report_audit` table is missing columns:

```sql
-- Add missing columns to report_audit table
ALTER TABLE report_audit 
  ADD COLUMN IF NOT EXISTS hospital_id VARCHAR(20),
  ADD COLUMN IF NOT EXISTS service_category VARCHAR(50),
  ADD COLUMN IF NOT EXISTS patient_category VARCHAR(50),
  ADD COLUMN IF NOT EXISTS gender VARCHAR(10),
  ADD COLUMN IF NOT EXISTS min_age INT,
  ADD COLUMN IF NOT EXISTS max_age INT;

-- Verify columns were added
DESCRIBE report_audit;
```

---

### ✅ Step 5: Fix CORS Error for PDF Download

The CORS error suggests the download endpoint might be crashing before sending headers.

**Check 1: Is the HTML being generated?**
- If `/api/reports/generate` returns HTML, that's good
- The CORS error on download might be because the PDF generation fails

**Check 2: Flying Saucer Dependency**

Make sure Maven downloaded the Flying Saucer library:

```bash
cd backend
mvn dependency:tree | findstr flying-saucer
```

**Expected output:**
```
[INFO] +- org.xhtmlrenderer:flying-saucer-pdf:jar:9.1.22:compile
```

If missing, run:
```bash
mvn clean install -U
```

---

### ✅ Step 6: Test with cURL

Test the endpoints directly to see raw errors:

```bash
# Get your JWT token from browser localStorage
# Then test generate endpoint:

curl -X POST http://localhost:8080/api/reports/generate \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE" \
  -d "{\"reportType\":\"PATIENT_VISIT\",\"criteria\":{\"from\":\"2024-01-01\",\"to\":\"2024-12-31\",\"hospitalId\":\"C001\"},\"filters\":{}}"
```

This will show you the exact error message.

---

### ✅ Step 7: Check Authentication

The error might be because the user isn't found:

```java
// In ReportService.java line 52:
User user = userRepository.findByEmail(email).orElse(null);
audit.setUser(user);
```

**Problem:** If `user` is null, and `report_audit` table has `NOT NULL` constraint on `user_id`, it will fail.

**Check your user:**

```sql
-- Check if your admin user exists
SELECT id, email, first_name, last_name, role 
FROM user 
WHERE role = 'ADMIN';

-- Check what email the JWT token has
-- (Look in your JWT token payload - you can decode it at jwt.io)
```

---

### ✅ Step 8: Common Fixes

#### Fix 1: Make user_id nullable temporarily

If you can't find the user issue immediately:

```java
// In ReportService.java, add null check:
User user = userRepository.findByEmail(email).orElse(null);
if (user != null) {
    audit.setUser(user);
} else {
    // Log warning
    System.out.println("WARNING: User not found for email: " + email);
}
```

#### Fix 2: Check Bean Registration

Make sure data providers are annotated:

```java
@Service("PATIENT_VISIT")  // ← Must match reportType exactly
public class PatientVisitDataProvider implements IReportDataProvider {
    // ...
}

@Service("SERVICE_UTILIZATION")  // ← Must match reportType exactly
public class ServiceUtilizationDataProvider implements IReportDataProvider {
    // ...
}
```

#### Fix 3: Check Visit Entity Mapping

Make sure Visit entity fields match database columns:

```java
@Column(name = "hospital_id")  // ← Must match DB column name
private String hospitalId;

@Column(name = "service_category")
private String serviceCategory;

// etc.
```

---

## 🔍 Quick Diagnostic Script

Run this in your MySQL to check everything:

```sql
-- Full diagnostic check
SELECT 'Checking visit table...' AS step;
SELECT COUNT(*) AS visit_count FROM visit;
SELECT COUNT(*) AS visits_with_filters 
FROM visit 
WHERE hospital_id IS NOT NULL 
  AND service_category IS NOT NULL;

SELECT 'Checking report_audit table...' AS step;
SELECT COUNT(*) AS audit_count FROM report_audit;

SELECT 'Checking user table...' AS step;
SELECT COUNT(*) AS admin_count 
FROM user 
WHERE role = 'ADMIN';

SELECT 'Checking patients...' AS step;
SELECT COUNT(*) AS patient_count FROM patient;
```

---

## 📞 Still Not Working?

### Share Backend Console Output

Copy the **full error stack trace** from your backend console and share it. Look for:

```
java.lang.NullPointerException
  at com.lankamed.health.backend.service.ReportService.createReport
  ...
```

OR

```
org.springframework.dao.DataIntegrityViolationException
  ...
  Column 'user_id' cannot be null
```

OR

```
com.mysql.cj.jdbc.exceptions.MySQLSyntaxErrorException
  Unknown column 'hospital_id' in 'field list'
```

This will tell us exactly what's wrong!

---

## ✅ Expected Success

When everything works, you should see in backend console:

```
Hibernate: select ... from visit where ...
Generated report with X visits
Report audit saved successfully
```

And in frontend:
- Report displays with HTML
- Download button works
- PDF file downloads

---

**Next Step:** Run the SQL diagnostics above and share:
1. Backend console error
2. Visit table row count
3. Report_audit table structure

