-- ==========================================
-- SIMPLE DATABASE FIX (No IF statements)
-- ==========================================
-- Run this if the other script gives errors
-- Note: You'll see "Duplicate column" errors if columns already exist - that's OK!

USE lankamed_db;

-- ==========================================
-- Add columns to visit table
-- (Ignore "Duplicate column name" errors)
-- ==========================================

ALTER TABLE visit ADD COLUMN hospital_id VARCHAR(20);
ALTER TABLE visit ADD COLUMN service_category VARCHAR(50);
ALTER TABLE visit ADD COLUMN patient_category VARCHAR(50);
ALTER TABLE visit ADD COLUMN gender VARCHAR(10);
ALTER TABLE visit ADD COLUMN age INT;

-- ==========================================
-- Add columns to report_audit table
-- (Ignore "Duplicate column name" errors)
-- ==========================================

ALTER TABLE report_audit ADD COLUMN hospital_id VARCHAR(20);
ALTER TABLE report_audit ADD COLUMN service_category VARCHAR(50);
ALTER TABLE report_audit ADD COLUMN patient_category VARCHAR(50);
ALTER TABLE report_audit ADD COLUMN gender VARCHAR(10);
ALTER TABLE report_audit ADD COLUMN min_age INT;
ALTER TABLE report_audit ADD COLUMN max_age INT;

-- ==========================================
-- Verify tables
-- ==========================================

SELECT 'Visit table structure:' AS info;
DESCRIBE visit;

SELECT 'Report audit table structure:' AS info;
DESCRIBE report_audit;

-- ==========================================
-- Add test data
-- (Change patient_id to match your patients)
-- ==========================================

-- First check your patient IDs
SELECT 'Your patient IDs:' AS info;
SELECT id FROM patient LIMIT 5;

-- Add test visits - REPLACE patient_id values with your actual IDs!
INSERT INTO visit (patient_id, visit_date, hospital_id, service_category, patient_category, gender, age, notes)
VALUES 
  (1, '2024-06-15 10:30:00', 'C001', 'OPD', 'OUTPATIENT', 'MALE', 35, 'Regular checkup'),
  (1, '2024-06-20 14:00:00', 'C001', 'LAB', 'OUTPATIENT', 'MALE', 35, 'Blood test'),
  (1, '2024-07-05 09:00:00', 'C001', 'OPD', 'OUTPATIENT', 'MALE', 35, 'Follow-up'),
  (1, '2024-06-18 09:00:00', 'C002', 'RADIOLOGY', 'OUTPATIENT', 'FEMALE', 28, 'X-ray'),
  (1, '2024-07-10 15:00:00', 'C001', 'LAB', 'OUTPATIENT', 'FEMALE', 28, 'Lab tests'),
  (2, '2024-07-01 15:00:00', 'C001', 'SURGERY', 'INPATIENT', 'MALE', 42, 'Minor surgery'),
  (2, '2024-07-15 10:00:00', 'C001', 'OPD', 'OUTPATIENT', 'MALE', 42, 'Post-surgery checkup'),
  (2, '2024-08-01 14:30:00', 'C003', 'PHARMACY', 'OUTPATIENT', 'MALE', 42, 'Medication refill'),
  (3, '2024-08-10 11:00:00', 'C001', 'OPD', 'OUTPATIENT', 'FEMALE', 55, 'Annual checkup'),
  (3, '2024-08-20 16:00:00', 'C002', 'RADIOLOGY', 'OUTPATIENT', 'FEMALE', 55, 'Scan');

-- ==========================================
-- Verify data
-- ==========================================

SELECT 'Total visits:' AS info, COUNT(*) AS count FROM visit;
SELECT 'Visits with filter data:' AS info, COUNT(*) AS count FROM visit WHERE hospital_id IS NOT NULL;

SELECT 'Sample visit data:' AS info;
SELECT id, patient_id, DATE(visit_date) AS date, hospital_id, service_category, gender, age FROM visit LIMIT 5;

SELECT 'Database ready!' AS status;

