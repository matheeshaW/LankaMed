-- ==========================================
-- QUICK FIX: Statistical Reports Database
-- ==========================================
-- Run this to fix common database issues

USE lankamed_db;

-- ==========================================
-- 1. FIX VISIT TABLE STRUCTURE
-- ==========================================

-- Check current structure first
SELECT 'Current visit table structure:' AS info;
DESCRIBE visit;

-- Add missing columns (ignore errors if they already exist)
-- Run each ALTER TABLE separately

SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
   WHERE TABLE_SCHEMA = DATABASE() 
   AND TABLE_NAME = 'visit' 
   AND COLUMN_NAME = 'hospital_id') = 0,
  'ALTER TABLE visit ADD COLUMN hospital_id VARCHAR(20)',
  'SELECT ''Column hospital_id already exists'' AS info'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
   WHERE TABLE_SCHEMA = DATABASE() 
   AND TABLE_NAME = 'visit' 
   AND COLUMN_NAME = 'service_category') = 0,
  'ALTER TABLE visit ADD COLUMN service_category VARCHAR(50)',
  'SELECT ''Column service_category already exists'' AS info'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
   WHERE TABLE_SCHEMA = DATABASE() 
   AND TABLE_NAME = 'visit' 
   AND COLUMN_NAME = 'patient_category') = 0,
  'ALTER TABLE visit ADD COLUMN patient_category VARCHAR(50)',
  'SELECT ''Column patient_category already exists'' AS info'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
   WHERE TABLE_SCHEMA = DATABASE() 
   AND TABLE_NAME = 'visit' 
   AND COLUMN_NAME = 'gender') = 0,
  'ALTER TABLE visit ADD COLUMN gender VARCHAR(10)',
  'SELECT ''Column gender already exists'' AS info'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
   WHERE TABLE_SCHEMA = DATABASE() 
   AND TABLE_NAME = 'visit' 
   AND COLUMN_NAME = 'age') = 0,
  'ALTER TABLE visit ADD COLUMN age INT',
  'SELECT ''Column age already exists'' AS info'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Verify structure after changes
SELECT 'Updated visit table structure:' AS info;
DESCRIBE visit;

-- ==========================================
-- 2. FIX REPORT_AUDIT TABLE STRUCTURE
-- ==========================================

-- Check current structure first
SELECT 'Current report_audit table structure:' AS info;
DESCRIBE report_audit;

-- Add missing columns (ignore errors if they already exist)

SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
   WHERE TABLE_SCHEMA = DATABASE() 
   AND TABLE_NAME = 'report_audit' 
   AND COLUMN_NAME = 'hospital_id') = 0,
  'ALTER TABLE report_audit ADD COLUMN hospital_id VARCHAR(20)',
  'SELECT ''Column hospital_id already exists in report_audit'' AS info'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
   WHERE TABLE_SCHEMA = DATABASE() 
   AND TABLE_NAME = 'report_audit' 
   AND COLUMN_NAME = 'service_category') = 0,
  'ALTER TABLE report_audit ADD COLUMN service_category VARCHAR(50)',
  'SELECT ''Column service_category already exists in report_audit'' AS info'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
   WHERE TABLE_SCHEMA = DATABASE() 
   AND TABLE_NAME = 'report_audit' 
   AND COLUMN_NAME = 'patient_category') = 0,
  'ALTER TABLE report_audit ADD COLUMN patient_category VARCHAR(50)',
  'SELECT ''Column patient_category already exists in report_audit'' AS info'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
   WHERE TABLE_SCHEMA = DATABASE() 
   AND TABLE_NAME = 'report_audit' 
   AND COLUMN_NAME = 'gender') = 0,
  'ALTER TABLE report_audit ADD COLUMN gender VARCHAR(10)',
  'SELECT ''Column gender already exists in report_audit'' AS info'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
   WHERE TABLE_SCHEMA = DATABASE() 
   AND TABLE_NAME = 'report_audit' 
   AND COLUMN_NAME = 'min_age') = 0,
  'ALTER TABLE report_audit ADD COLUMN min_age INT',
  'SELECT ''Column min_age already exists in report_audit'' AS info'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
   WHERE TABLE_SCHEMA = DATABASE() 
   AND TABLE_NAME = 'report_audit' 
   AND COLUMN_NAME = 'max_age') = 0,
  'ALTER TABLE report_audit ADD COLUMN max_age INT',
  'SELECT ''Column max_age already exists in report_audit'' AS info'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Verify structure after changes
SELECT 'Updated report_audit table structure:' AS info;
DESCRIBE report_audit;

-- ==========================================
-- 3. ADD TEST DATA FOR VISITS
-- ==========================================

-- First, check if we have any patients
SELECT 'Checking for patients...' AS status;
SELECT COUNT(*) AS patient_count FROM patient;

-- Get first 3 patient IDs
SELECT id AS patient_id FROM patient LIMIT 3;

-- ⚠️ IMPORTANT: Replace the patient_id values below with actual IDs from your patient table!
-- You can see them from the query above

-- Add test visits (ADJUST patient_id values as needed)
INSERT INTO visit (patient_id, visit_date, hospital_id, service_category, patient_category, gender, age, notes)
SELECT * FROM (
  SELECT 1 AS patient_id, '2024-06-15 10:30:00' AS visit_date, 'C001' AS hospital_id, 'OPD' AS service_category, 'OUTPATIENT' AS patient_category, 'MALE' AS gender, 35 AS age, 'Regular checkup' AS notes UNION ALL
  SELECT 1, '2024-06-20 14:00:00', 'C001', 'LAB', 'OUTPATIENT', 'MALE', 35, 'Blood test' UNION ALL
  SELECT 1, '2024-07-05 09:00:00', 'C001', 'OPD', 'OUTPATIENT', 'MALE', 35, 'Follow-up' UNION ALL
  SELECT 2, '2024-06-18 09:00:00', 'C001', 'OPD', 'OUTPATIENT', 'FEMALE', 28, 'Consultation' UNION ALL
  SELECT 2, '2024-06-25 11:30:00', 'C002', 'RADIOLOGY', 'OUTPATIENT', 'FEMALE', 28, 'X-ray' UNION ALL
  SELECT 2, '2024-07-10 15:00:00', 'C001', 'LAB', 'OUTPATIENT', 'FEMALE', 28, 'Lab tests' UNION ALL
  SELECT 3, '2024-07-01 15:00:00', 'C001', 'SURGERY', 'INPATIENT', 'MALE', 42, 'Minor surgery' UNION ALL
  SELECT 3, '2024-07-15 10:00:00', 'C001', 'OPD', 'OUTPATIENT', 'MALE', 42, 'Post-surgery checkup' UNION ALL
  SELECT 3, '2024-08-01 14:30:00', 'C003', 'PHARMACY', 'OUTPATIENT', 'MALE', 42, 'Medication refill'
) AS temp
WHERE (SELECT COUNT(*) FROM patient WHERE id = temp.patient_id) > 0
LIMIT 100;

-- ==========================================
-- 4. VERIFY DATA WAS INSERTED
-- ==========================================

SELECT 'Checking visit data...' AS status;
SELECT COUNT(*) AS total_visits FROM visit;
SELECT COUNT(*) AS visits_with_filters 
FROM visit 
WHERE hospital_id IS NOT NULL;

SELECT 'Sample visits:' AS status;
SELECT 
  id,
  patient_id,
  visit_date,
  hospital_id,
  service_category,
  patient_category,
  gender,
  age
FROM visit
LIMIT 10;

-- ==========================================
-- 5. CHECK ADMIN USERS
-- ==========================================

SELECT 'Checking admin users...' AS status;
SELECT id, email, CONCAT(first_name, ' ', last_name) AS name, role 
FROM user 
WHERE role = 'ADMIN';

-- ==========================================
-- 6. DIAGNOSTIC SUMMARY
-- ==========================================

SELECT '=== DIAGNOSTIC SUMMARY ===' AS info;

SELECT 
  'Visit Table' AS table_name,
  COUNT(*) AS row_count,
  COUNT(DISTINCT hospital_id) AS distinct_hospitals,
  COUNT(DISTINCT service_category) AS distinct_services,
  MIN(visit_date) AS earliest_visit,
  MAX(visit_date) AS latest_visit
FROM visit

UNION ALL

SELECT 
  'Report Audit' AS table_name,
  COUNT(*) AS row_count,
  COUNT(DISTINCT report_type) AS distinct_types,
  NULL AS distinct_services,
  MIN(generated_on) AS earliest,
  MAX(generated_on) AS latest
FROM report_audit

UNION ALL

SELECT 
  'Users' AS table_name,
  COUNT(*) AS row_count,
  SUM(CASE WHEN role = 'ADMIN' THEN 1 ELSE 0 END) AS admin_count,
  NULL,
  NULL,
  NULL
FROM user;

-- ==========================================
-- DONE! Now restart your backend and test
-- ==========================================

SELECT 'Database setup complete! Restart backend and try generating a report.' AS message;

