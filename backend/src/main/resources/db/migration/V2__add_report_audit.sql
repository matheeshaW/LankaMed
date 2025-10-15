-- Create report_audit table
CREATE TABLE report_audit (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    report_type VARCHAR(100) NOT NULL,
    criteria_json TEXT NOT NULL,
    generated_on DATETIME NOT NULL,
    success BOOLEAN NOT NULL,
    notes TEXT,
    CONSTRAINT fk_report_audit_user FOREIGN KEY (user_id) REFERENCES user(id)
);

-- Create minimal visit table for reporting/demo (optional, for extensibility)
CREATE TABLE visit (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    service_category_id BIGINT,
    visit_date DATETIME NOT NULL,
    notes TEXT,
    CONSTRAINT fk_visit_patient FOREIGN KEY (patient_id) REFERENCES patient(id),
    CONSTRAINT fk_visit_service_category FOREIGN KEY (service_category_id) REFERENCES service_category(id)
);

-- Demo seed for visit
INSERT INTO visit (patient_id, service_category_id, visit_date, notes)
SELECT p.id, s.id, NOW(), 'Demo visit for seed'
FROM patient p, service_category s
LIMIT 1;