-- ==========================================
-- COMPLETE DUMMY DATA FOR LANKAMED SYSTEM
-- ==========================================
-- This script inserts realistic test data for all tables

USE lankamed_db;

-- ==========================================
-- 1. CREATE VISIT TABLE (if it doesn't exist)
-- ==========================================

CREATE TABLE IF NOT EXISTS visit (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    visit_date DATETIME NOT NULL,
    notes TEXT,
    hospital_id VARCHAR(20),
    service_category VARCHAR(50),
    patient_category VARCHAR(50),
    gender VARCHAR(10),
    age INT,
    CONSTRAINT fk_visit_patient FOREIGN KEY (patient_id) REFERENCES patients(patient_id)
);

-- ==========================================
-- 2. HOSPITALS
-- ==========================================

INSERT INTO hospitals (name, address, contact_number) VALUES
('Colombo National Hospital', '123 Regent Street, Colombo 07', '+94 11 269 1111'),
('Castle Street Women''s Hospital', '23 Castle Street, Colombo 08', '+94 11 269 4221'),
('Sri Jayewardenepura General Hospital', 'Thalapathpitiya, Nugegoda', '+94 11 277 7111'),
('Asiri Central Hospital', '114 Norris Canal Road, Colombo 10', '+94 11 466 5500'),
('Lanka Hospitals', '578 Elvitigala Mawatha, Colombo 05', '+94 11 554 3000'),
('Nawaloka Hospital', '23 Sri Saugathhodaya Mawatha, Colombo 02', '+94 11 554 4444');

-- Get hospital IDs for reference
SET @hospital1 = (SELECT hospital_id FROM hospitals WHERE name = 'Colombo National Hospital' LIMIT 1);
SET @hospital2 = (SELECT hospital_id FROM hospitals WHERE name = 'Castle Street Women''s Hospital' LIMIT 1);
SET @hospital3 = (SELECT hospital_id FROM hospitals WHERE name = 'Sri Jayewardenepura General Hospital' LIMIT 1);

-- ==========================================
-- 3. SERVICE CATEGORIES
-- ==========================================

INSERT INTO service_categories (name, description) VALUES
('OPD', 'Outpatient Department - General consultations'),
('LAB', 'Laboratory Services - Blood tests, diagnostics'),
('SURGERY', 'Surgical Procedures'),
('PHARMACY', 'Pharmacy and Medication'),
('RADIOLOGY', 'Radiology - X-rays, CT scans, MRI'),
('EMERGENCY', 'Emergency Department'),
('CARDIOLOGY', 'Cardiology and Heart Care'),
('PEDIATRICS', 'Pediatrics - Child Care'),
('ORTHOPEDICS', 'Orthopedics - Bone and Joint Care'),
('DENTISTRY', 'Dental Services');

-- Get service category IDs
SET @opd = (SELECT category_id FROM service_categories WHERE name = 'OPD' LIMIT 1);
SET @lab = (SELECT category_id FROM service_categories WHERE name = 'LAB' LIMIT 1);
SET @surgery = (SELECT category_id FROM service_categories WHERE name = 'SURGERY' LIMIT 1);
SET @pharmacy = (SELECT category_id FROM service_categories WHERE name = 'PHARMACY' LIMIT 1);
SET @radiology = (SELECT category_id FROM service_categories WHERE name = 'RADIOLOGY' LIMIT 1);

-- ==========================================
-- 4. USERS (Admin, Doctors, Staff, Patients)
-- ==========================================

-- Admin User
INSERT INTO users (first_name, last_name, email, password_hash, role) VALUES
('Admin', 'System', 'admin@lankamed.lk', '$2a$10$8JRPqmQeH1XdQ5DPUG5QBujpbY6F.1p8hUdVqKTN2X8YHh9QVNX.u', 'ADMIN');
-- Password: admin123

-- Doctors
INSERT INTO users (first_name, last_name, email, password_hash, role) VALUES
('Dr. Saman', 'Perera', 'saman.perera@lankamed.lk', '$2a$10$8JRPqmQeH1XdQ5DPUG5QBujpbY6F.1p8hUdVqKTN2X8YHh9QVNX.u', 'DOCTOR'),
('Dr. Nimal', 'Fernando', 'nimal.fernando@lankamed.lk', '$2a$10$8JRPqmQeH1XdQ5DPUG5QBujpbY6F.1p8hUdVqKTN2X8YHh9QVNX.u', 'DOCTOR'),
('Dr. Kumari', 'Silva', 'kumari.silva@lankamed.lk', '$2a$10$8JRPqmQeH1XdQ5DPUG5QBujpbY6F.1p8hUdVqKTN2X8YHh9QVNX.u', 'DOCTOR');

-- Patients (20 patients for good test data)
INSERT INTO users (first_name, last_name, email, password_hash, role) VALUES
('Kamal', 'Wickramasinghe', 'kamal.w@gmail.com', '$2a$10$8JRPqmQeH1XdQ5DPUG5QBujpbY6F.1p8hUdVqKTN2X8YHh9QVNX.u', 'PATIENT'),
('Nimali', 'Jayawardena', 'nimali.j@gmail.com', '$2a$10$8JRPqmQeH1XdQ5DPUG5QBujpbY6F.1p8hUdVqKTN2X8YHh9QVNX.u', 'PATIENT'),
('Sunil', 'Ranasinghe', 'sunil.r@gmail.com', '$2a$10$8JRPqmQeH1XdQ5DPUG5QBujpbY6F.1p8hUdVqKTN2X8YHh9QVNX.u', 'PATIENT'),
('Priya', 'Gunawardena', 'priya.g@gmail.com', '$2a$10$8JRPqmQeH1XdQ5DPUG5QBujpbY6F.1p8hUdVqKTN2X8YHh9QVNX.u', 'PATIENT'),
('Ravi', 'Dissanayake', 'ravi.d@gmail.com', '$2a$10$8JRPqmQeH1XdQ5DPUG5QBujpbY6F.1p8hUdVqKTN2X8YHh9QVNX.u', 'PATIENT'),
('Chamali', 'Rathnayake', 'chamali.r@gmail.com', '$2a$10$8JRPqmQeH1XdQ5DPUG5QBujpbY6F.1p8hUdVqKTN2X8YHh9QVNX.u', 'PATIENT'),
('Dinesh', 'Bandara', 'dinesh.b@gmail.com', '$2a$10$8JRPqmQeH1XdQ5DPUG5QBujpbY6F.1p8hUdVqKTN2X8YHh9QVNX.u', 'PATIENT'),
('Sanduni', 'Wijesinghe', 'sanduni.w@gmail.com', '$2a$10$8JRPqmQeH1XdQ5DPUG5QBujpbY6F.1p8hUdVqKTN2X8YHh9QVNX.u', 'PATIENT'),
('Mahesh', 'Rajapaksa', 'mahesh.r@gmail.com', '$2a$10$8JRPqmQeH1XdQ5DPUG5QBujpbY6F.1p8hUdVqKTN2X8YHh9QVNX.u', 'PATIENT'),
('Shalini', 'Mendis', 'shalini.m@gmail.com', '$2a$10$8JRPqmQeH1XdQ5DPUG5QBujpbY6F.1p8hUdVqKTN2X8YHh9QVNX.u', 'PATIENT'),
('Ajith', 'Peiris', 'ajith.p@gmail.com', '$2a$10$8JRPqmQeH1XdQ5DPUG5QBujpbY6F.1p8hUdVqKTN2X8YHh9QVNX.u', 'PATIENT'),
('Malini', 'De Silva', 'malini.ds@gmail.com', '$2a$10$8JRPqmQeH1XdQ5DPUG5QBujpbY6F.1p8hUdVqKTN2X8YHh9QVNX.u', 'PATIENT'),
('Lakshan', 'Amarasinghe', 'lakshan.a@gmail.com', '$2a$10$8JRPqmQeH1XdQ5DPUG5QBujpbY6F.1p8hUdVqKTN2X8YHh9QVNX.u', 'PATIENT'),
('Ishara', 'Gamage', 'ishara.g@gmail.com', '$2a$10$8JRPqmQeH1XdQ5DPUG5QBujpbY6F.1p8hUdVqKTN2X8YHh9QVNX.u', 'PATIENT'),
('Chandana', 'Senanayake', 'chandana.s@gmail.com', '$2a$10$8JRPqmQeH1XdQ5DPUG5QBujpbY6F.1p8hUdVqKTN2X8YHh9QVNX.u', 'PATIENT'),
('Dilini', 'Liyanage', 'dilini.l@gmail.com', '$2a$10$8JRPqmQeH1XdQ5DPUG5QBujpbY6F.1p8hUdVqKTN2X8YHh9QVNX.u', 'PATIENT'),
('Asanka', 'Pathirana', 'asanka.p@gmail.com', '$2a$10$8JRPqmQeH1XdQ5DPUG5QBujpbY6F.1p8hUdVqKTN2X8YHh9QVNX.u', 'PATIENT'),
('Buddhika', 'Karunaratne', 'buddhika.k@gmail.com', '$2a$10$8JRPqmQeH1XdQ5DPUG5QBujpbY6F.1p8hUdVqKTN2X8YHh9QVNX.u', 'PATIENT'),
('Tharanga', 'Kumarasinghe', 'tharanga.k@gmail.com', '$2a$10$8JRPqmQeH1XdQ5DPUG5QBujpbY6F.1p8hUdVqKTN2X8YHh9QVNX.u', 'PATIENT'),
('Nilmini', 'Weerasinghe', 'nilmini.w@gmail.com', '$2a$10$8JRPqmQeH1XdQ5DPUG5QBujpbY6F.1p8hUdVqKTN2X8YHh9QVNX.u', 'PATIENT');

-- Get some user IDs for reference
SET @admin_id = (SELECT user_id FROM users WHERE email = 'admin@lankamed.lk' LIMIT 1);
SET @doctor1_id = (SELECT user_id FROM users WHERE email = 'saman.perera@lankamed.lk' LIMIT 1);
SET @doctor2_id = (SELECT user_id FROM users WHERE email = 'nimal.fernando@lankamed.lk' LIMIT 1);
SET @doctor3_id = (SELECT user_id FROM users WHERE email = 'kumari.silva@lankamed.lk' LIMIT 1);

-- ==========================================
-- 5. STAFF DETAILS (for doctors)
-- ==========================================

INSERT INTO staff_details (staff_id, hospital_id, service_category_id, specialization)
SELECT user_id, @hospital1, @opd, 'General Medicine'
FROM users WHERE email = 'saman.perera@lankamed.lk';

INSERT INTO staff_details (staff_id, hospital_id, service_category_id, specialization)
SELECT user_id, @hospital1, @surgery, 'General Surgery'
FROM users WHERE email = 'nimal.fernando@lankamed.lk';

INSERT INTO staff_details (staff_id, hospital_id, service_category_id, specialization)
SELECT user_id, @hospital2, @opd, 'Gynecology'
FROM users WHERE email = 'kumari.silva@lankamed.lk';

-- ==========================================
-- 6. PATIENTS (with demographics)
-- ==========================================

-- Patient 1: Kamal (Male, 35 years old)
INSERT INTO patients (patient_id, date_of_birth, gender, contact_number, address)
SELECT user_id, '1989-03-15', 'Male', '+94 77 123 4567', '45 Galle Road, Colombo 03'
FROM users WHERE email = 'kamal.w@gmail.com';

-- Patient 2: Nimali (Female, 28 years old)
INSERT INTO patients (patient_id, date_of_birth, gender, contact_number, address)
SELECT user_id, '1996-07-22', 'Female', '+94 77 234 5678', '78 Kandy Road, Kaduwela'
FROM users WHERE email = 'nimali.j@gmail.com';

-- Patient 3: Sunil (Male, 42 years old)
INSERT INTO patients (patient_id, date_of_birth, gender, contact_number, address)
SELECT user_id, '1982-11-08', 'Male', '+94 77 345 6789', '12 Temple Road, Nugegoda'
FROM users WHERE email = 'sunil.r@gmail.com';

-- Patient 4: Priya (Female, 55 years old)
INSERT INTO patients (patient_id, date_of_birth, gender, contact_number, address)
SELECT user_id, '1969-05-18', 'Female', '+94 77 456 7890', '90 Lake Drive, Boralesgamuwa'
FROM users WHERE email = 'priya.g@gmail.com';

-- Patient 5: Ravi (Male, 65 years old)
INSERT INTO patients (patient_id, date_of_birth, gender, contact_number, address)
SELECT user_id, '1959-02-25', 'Male', '+94 77 567 8901', '33 Station Road, Dehiwala'
FROM users WHERE email = 'ravi.d@gmail.com';

-- Continue with remaining patients
INSERT INTO patients (patient_id, date_of_birth, gender, contact_number, address)
SELECT user_id, '1992-09-10', 'Female', '+94 77 678 9012', '56 Park Avenue, Mount Lavinia'
FROM users WHERE email = 'chamali.r@gmail.com';

INSERT INTO patients (patient_id, date_of_birth, gender, contact_number, address)
SELECT user_id, '1985-12-30', 'Male', '+94 77 789 0123', '21 Beach Road, Moratuwa'
FROM users WHERE email = 'dinesh.b@gmail.com';

INSERT INTO patients (patient_id, date_of_birth, gender, contact_number, address)
SELECT user_id, '1998-04-05', 'Female', '+94 77 890 1234', '67 Hill Street, Kotte'
FROM users WHERE email = 'sanduni.w@gmail.com';

INSERT INTO patients (patient_id, date_of_birth, gender, contact_number, address)
SELECT user_id, '1975-08-14', 'Male', '+94 77 901 2345', '88 Main Road, Maharagama'
FROM users WHERE email = 'mahesh.r@gmail.com';

INSERT INTO patients (patient_id, date_of_birth, gender, contact_number, address)
SELECT user_id, '1990-01-20', 'Female', '+94 77 012 3456', '34 School Lane, Piliyandala'
FROM users WHERE email = 'shalini.m@gmail.com';

INSERT INTO patients (patient_id, date_of_birth, gender, contact_number, address)
SELECT user_id, '1987-06-12', 'Male', '+94 76 123 4567', '45 Market Street, Kelaniya'
FROM users WHERE email = 'ajith.p@gmail.com';

INSERT INTO patients (patient_id, date_of_birth, gender, contact_number, address)
SELECT user_id, '1993-10-28', 'Female', '+94 76 234 5678', '12 Church Road, Wattala'
FROM users WHERE email = 'malini.ds@gmail.com';

INSERT INTO patients (patient_id, date_of_birth, gender, contact_number, address)
SELECT user_id, '1980-03-03', 'Male', '+94 76 345 6789', '89 Railway Avenue, Ragama'
FROM users WHERE email = 'lakshan.a@gmail.com';

INSERT INTO patients (patient_id, date_of_birth, gender, contact_number, address)
SELECT user_id, '1995-11-15', 'Female', '+94 76 456 7890', '23 Hospital Road, Gampaha'
FROM users WHERE email = 'ishara.g@gmail.com';

INSERT INTO patients (patient_id, date_of_birth, gender, contact_number, address)
SELECT user_id, '1972-07-09', 'Male', '+94 76 567 8901', '56 Bridge Street, Negombo'
FROM users WHERE email = 'chandana.s@gmail.com';

INSERT INTO patients (patient_id, date_of_birth, gender, contact_number, address)
SELECT user_id, '1997-02-14', 'Female', '+94 76 678 9012', '78 Sea View, Panadura'
FROM users WHERE email = 'dilini.l@gmail.com';

INSERT INTO patients (patient_id, date_of_birth, gender, contact_number, address)
SELECT user_id, '1983-09-22', 'Male', '+94 76 789 0123', '34 Garden Road, Kalutara'
FROM users WHERE email = 'asanka.p@gmail.com';

INSERT INTO patients (patient_id, date_of_birth, gender, contact_number, address)
SELECT user_id, '1988-05-07', 'Male', '+94 76 890 1234', '90 Temple Lane, Horana'
FROM users WHERE email = 'buddhika.k@gmail.com';

INSERT INTO patients (patient_id, date_of_birth, gender, contact_number, address)
SELECT user_id, '1991-12-19', 'Female', '+94 76 901 2345', '12 Station Road, Bandaragama'
FROM users WHERE email = 'tharanga.k@gmail.com';

INSERT INTO patients (patient_id, date_of_birth, gender, contact_number, address)
SELECT user_id, '1994-08-25', 'Female', '+94 76 012 3456', '45 Market Place, Mathugama'
FROM users WHERE email = 'nilmini.w@gmail.com';

-- ==========================================
-- 7. VISITS (Lots of test data for reports!)
-- ==========================================

-- Get patient IDs
SET @p1 = (SELECT user_id FROM users WHERE email = 'kamal.w@gmail.com');
SET @p2 = (SELECT user_id FROM users WHERE email = 'nimali.j@gmail.com');
SET @p3 = (SELECT user_id FROM users WHERE email = 'sunil.r@gmail.com');
SET @p4 = (SELECT user_id FROM users WHERE email = 'priya.g@gmail.com');
SET @p5 = (SELECT user_id FROM users WHERE email = 'ravi.d@gmail.com');
SET @p6 = (SELECT user_id FROM users WHERE email = 'chamali.r@gmail.com');
SET @p7 = (SELECT user_id FROM users WHERE email = 'dinesh.b@gmail.com');
SET @p8 = (SELECT user_id FROM users WHERE email = 'sanduni.w@gmail.com');
SET @p9 = (SELECT user_id FROM users WHERE email = 'mahesh.r@gmail.com');
SET @p10 = (SELECT user_id FROM users WHERE email = 'shalini.m@gmail.com');

-- Insert visits with proper filter data (50+ visits for good testing)
INSERT INTO visit (patient_id, visit_date, hospital_id, service_category, patient_category, gender, age, notes) VALUES
-- January 2024
(@p1, '2024-01-05 09:30:00', 'C001', 'OPD', 'OUTPATIENT', 'MALE', 35, 'Regular checkup'),
(@p2, '2024-01-08 10:00:00', 'C001', 'LAB', 'OUTPATIENT', 'FEMALE', 28, 'Blood test'),
(@p3, '2024-01-10 14:30:00', 'C001', 'OPD', 'OUTPATIENT', 'MALE', 42, 'Consultation'),
(@p4, '2024-01-12 11:00:00', 'C002', 'RADIOLOGY', 'OUTPATIENT', 'FEMALE', 55, 'X-ray'),
(@p5, '2024-01-15 16:00:00', 'C001', 'PHARMACY', 'OUTPATIENT', 'MALE', 65, 'Medication refill'),

-- February 2024
(@p6, '2024-02-03 09:00:00', 'C001', 'OPD', 'OUTPATIENT', 'FEMALE', 32, 'Follow-up'),
(@p7, '2024-02-07 10:30:00', 'C001', 'LAB', 'OUTPATIENT', 'MALE', 39, 'Lab tests'),
(@p8, '2024-02-10 15:00:00', 'C002', 'OPD', 'OUTPATIENT', 'FEMALE', 26, 'General consultation'),
(@p9, '2024-02-14 11:30:00', 'C001', 'SURGERY', 'INPATIENT', 'MALE', 49, 'Minor surgery'),
(@p10, '2024-02-18 14:00:00', 'C001', 'OPD', 'OUTPATIENT', 'FEMALE', 34, 'Checkup'),

-- March 2024
(@p1, '2024-03-05 09:30:00', 'C001', 'LAB', 'OUTPATIENT', 'MALE', 35, 'Follow-up test'),
(@p2, '2024-03-08 10:00:00', 'C002', 'RADIOLOGY', 'OUTPATIENT', 'FEMALE', 28, 'Scan'),
(@p3, '2024-03-12 14:30:00', 'C001', 'OPD', 'OUTPATIENT', 'MALE', 42, 'Medication review'),
(@p4, '2024-03-15 11:00:00', 'C001', 'PHARMACY', 'OUTPATIENT', 'FEMALE', 55, 'Prescription refill'),
(@p5, '2024-03-20 16:00:00', 'C003', 'OPD', 'OUTPATIENT', 'MALE', 65, 'Consultation'),

-- April 2024
(@p6, '2024-04-02 09:00:00', 'C001', 'OPD', 'OUTPATIENT', 'FEMALE', 32, 'Annual checkup'),
(@p7, '2024-04-05 10:30:00', 'C001', 'LAB', 'OUTPATIENT', 'MALE', 39, 'Blood work'),
(@p8, '2024-04-08 15:00:00', 'C001', 'OPD', 'OUTPATIENT', 'FEMALE', 26, 'Follow-up'),
(@p9, '2024-04-12 11:30:00', 'C001', 'OPD', 'OUTPATIENT', 'MALE', 49, 'Post-surgery checkup'),
(@p10, '2024-04-16 14:00:00', 'C002', 'RADIOLOGY', 'OUTPATIENT', 'FEMALE', 34, 'X-ray'),

-- May 2024
(@p1, '2024-05-03 09:30:00', 'C001', 'OPD', 'OUTPATIENT', 'MALE', 35, 'General consultation'),
(@p2, '2024-05-07 10:00:00', 'C001', 'LAB', 'OUTPATIENT', 'FEMALE', 28, 'Lab tests'),
(@p3, '2024-05-10 14:30:00', 'C003', 'OPD', 'OUTPATIENT', 'MALE', 42, 'Checkup'),
(@p4, '2024-05-14 11:00:00', 'C001', 'OPD', 'OUTPATIENT', 'FEMALE', 55, 'Follow-up'),
(@p5, '2024-05-18 16:00:00', 'C001', 'PHARMACY', 'OUTPATIENT', 'MALE', 65, 'Medication'),

-- June 2024
(@p6, '2024-06-01 09:00:00', 'C001', 'OPD', 'OUTPATIENT', 'FEMALE', 32, 'Consultation'),
(@p7, '2024-06-05 10:30:00', 'C001', 'LAB', 'OUTPATIENT', 'MALE', 39, 'Blood test'),
(@p8, '2024-06-08 15:00:00', 'C002', 'OPD', 'OUTPATIENT', 'FEMALE', 26, 'Checkup'),
(@p9, '2024-06-12 11:30:00', 'C001', 'OPD', 'OUTPATIENT', 'MALE', 49, 'Review'),
(@p10, '2024-06-16 14:00:00', 'C001', 'RADIOLOGY', 'OUTPATIENT', 'FEMALE', 34, 'Scan'),

-- July 2024
(@p1, '2024-07-02 09:30:00', 'C001', 'OPD', 'OUTPATIENT', 'MALE', 35, 'Regular visit'),
(@p2, '2024-07-06 10:00:00', 'C001', 'LAB', 'OUTPATIENT', 'FEMALE', 28, 'Tests'),
(@p3, '2024-07-09 14:30:00', 'C001', 'SURGERY', 'INPATIENT', 'MALE', 42, 'Procedure'),
(@p4, '2024-07-13 11:00:00', 'C002', 'OPD', 'OUTPATIENT', 'FEMALE', 55, 'Consultation'),
(@p5, '2024-07-17 16:00:00', 'C001', 'PHARMACY', 'OUTPATIENT', 'MALE', 65, 'Refill'),

-- August 2024
(@p6, '2024-08-01 09:00:00', 'C001', 'OPD', 'OUTPATIENT', 'FEMALE', 32, 'Checkup'),
(@p7, '2024-08-05 10:30:00', 'C001', 'LAB', 'OUTPATIENT', 'MALE', 39, 'Lab work'),
(@p8, '2024-08-08 15:00:00', 'C001', 'OPD', 'OUTPATIENT', 'FEMALE', 26, 'Visit'),
(@p9, '2024-08-12 11:30:00', 'C003', 'RADIOLOGY', 'OUTPATIENT', 'MALE', 49, 'Imaging'),
(@p10, '2024-08-16 14:00:00', 'C001', 'OPD', 'OUTPATIENT', 'FEMALE', 34, 'Follow-up'),

-- September 2024
(@p1, '2024-09-03 09:30:00', 'C001', 'OPD', 'OUTPATIENT', 'MALE', 35, 'Consultation'),
(@p2, '2024-09-07 10:00:00', 'C002', 'LAB', 'OUTPATIENT', 'FEMALE', 28, 'Blood work'),
(@p3, '2024-09-10 14:30:00', 'C001', 'OPD', 'OUTPATIENT', 'MALE', 42, 'Checkup'),
(@p4, '2024-09-14 11:00:00', 'C001', 'PHARMACY', 'OUTPATIENT', 'FEMALE', 55, 'Medication'),
(@p5, '2024-09-18 16:00:00', 'C001', 'OPD', 'OUTPATIENT', 'MALE', 65, 'Visit'),

-- October 2024 (recent)
(@p6, '2024-10-02 09:00:00', 'C001', 'OPD', 'OUTPATIENT', 'FEMALE', 32, 'Regular checkup'),
(@p7, '2024-10-06 10:30:00', 'C001', 'LAB', 'OUTPATIENT', 'MALE', 39, 'Tests'),
(@p8, '2024-10-09 15:00:00', 'C001', 'RADIOLOGY', 'OUTPATIENT', 'FEMALE', 26, 'X-ray'),
(@p9, '2024-10-13 11:30:00', 'C002', 'OPD', 'OUTPATIENT', 'MALE', 49, 'Consultation'),
(@p10, '2024-10-15 14:00:00', 'C001', 'OPD', 'OUTPATIENT', 'FEMALE', 34, 'Follow-up');

-- ==========================================
-- 8. VERIFICATION & SUMMARY
-- ==========================================

SELECT '========================================' AS separator;
SELECT 'DATABASE POPULATED SUCCESSFULLY!' AS status;
SELECT '========================================' AS separator;

SELECT 'Admin Login Credentials:' AS info;
SELECT 'Email: admin@lankamed.lk' AS email, 'Password: admin123' AS password;

SELECT '========================================' AS separator;
SELECT 'SUMMARY:' AS section;

SELECT 'Hospitals' AS table_name, COUNT(*) AS count FROM hospitals
UNION ALL
SELECT 'Service Categories', COUNT(*) FROM service_categories
UNION ALL
SELECT 'Users', COUNT(*) FROM users
UNION ALL
SELECT 'Patients', COUNT(*) FROM patients
UNION ALL
SELECT 'Staff', COUNT(*) FROM staff_details
UNION ALL
SELECT 'Visits', COUNT(*) FROM visit;

SELECT '========================================' AS separator;
SELECT 'VISIT BREAKDOWN:' AS section;

SELECT 
  hospital_id AS hospital,
  service_category AS service,
  COUNT(*) AS visit_count
FROM visit
GROUP BY hospital_id, service_category
ORDER BY hospital_id, service_category;

SELECT '========================================' AS separator;
SELECT 'GENDER DISTRIBUTION:' AS section;

SELECT 
  gender,
  COUNT(*) AS visit_count,
  ROUND(AVG(age), 1) AS avg_age
FROM visit
GROUP BY gender;

SELECT '========================================' AS separator;
SELECT 'Ready to test reports! Use date range: 2024-01-01 to 2024-12-31' AS message;
SELECT '========================================' AS separator;

