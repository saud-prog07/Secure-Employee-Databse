-- Migration: Add Holiday table for Workday Statistics

CREATE TABLE holidays (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    date DATE NOT NULL,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    year INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_date (date),
    INDEX idx_year (year)
);

-- Insert 2026 holidays (United States - Common holidays)
INSERT INTO holidays (date, name, description, year) VALUES
('2026-01-01', 'New Year Day', 'New Year holiday', 2026),
('2026-01-19', 'MLK Jr. Day', 'Martin Luther King Jr. Birthday', 2026),
('2026-02-16', 'Presidents Day', 'Presidents Day', 2026),
('2026-03-17', 'St. Patricks Day', 'St. Patrick''s Day', 2026),
('2026-05-25', 'Memorial Day', 'Memorial Day', 2026),
('2026-07-04', 'Independence Day', 'Independence Day', 2026),
('2026-09-07', 'Labor Day', 'Labor Day', 2026),
('2026-10-12', 'Columbus Day', 'Columbus Day', 2026),
('2026-11-11', 'Veterans Day', 'Veterans Day', 2026),
('2026-11-26', 'Thanksgiving', 'Thanksgiving Day', 2026),
('2026-11-27', 'Day After Thanksgiving', 'Day after Thanksgiving', 2026),
('2026-12-25', 'Christmas', 'Christmas Day', 2026);

-- Insert 2025 holidays
INSERT INTO holidays (date, name, description, year) VALUES
('2025-01-01', 'New Year Day', 'New Year holiday', 2025),
('2025-01-20', 'MLK Jr. Day', 'Martin Luther King Jr. Birthday', 2025),
('2025-02-17', 'Presidents Day', 'Presidents Day', 2025),
('2025-03-17', 'St. Patricks Day', 'St. Patrick''s Day', 2025),
('2025-05-26', 'Memorial Day', 'Memorial Day', 2025),
('2025-07-04', 'Independence Day', 'Independence Day', 2025),
('2025-09-01', 'Labor Day', 'Labor Day', 2025),
('2025-10-13', 'Columbus Day', 'Columbus Day', 2025),
('2025-11-11', 'Veterans Day', 'Veterans Day', 2025),
('2025-11-27', 'Thanksgiving', 'Thanksgiving Day', 2025),
('2025-11-28', 'Day After Thanksgiving', 'Day after Thanksgiving', 2025),
('2025-12-25', 'Christmas', 'Christmas Day', 2025);

-- Insert 2024 holidays
INSERT INTO holidays (date, name, description, year) VALUES
('2024-01-01', 'New Year Day', 'New Year holiday', 2024),
('2024-01-15', 'MLK Jr. Day', 'Martin Luther King Jr. Birthday', 2024),
('2024-02-19', 'Presidents Day', 'Presidents Day', 2024),
('2024-03-17', 'St. Patricks Day', 'St. Patrick''s Day', 2024),
('2024-05-27', 'Memorial Day', 'Memorial Day', 2024),
('2024-07-04', 'Independence Day', 'Independence Day', 2024),
('2024-09-02', 'Labor Day', 'Labor Day', 2024),
('2024-10-14', 'Columbus Day', 'Columbus Day', 2024),
('2024-11-11', 'Veterans Day', 'Veterans Day', 2024),
('2024-11-28', 'Thanksgiving', 'Thanksgiving Day', 2024),
('2024-11-29', 'Day After Thanksgiving', 'Day after Thanksgiving', 2024),
('2024-12-25', 'Christmas', 'Christmas Day', 2024);

-- Insert 2027 holidays
INSERT INTO holidays (date, name, description, year) VALUES
('2027-01-01', 'New Year Day', 'New Year holiday', 2027),
('2027-01-18', 'MLK Jr. Day', 'Martin Luther King Jr. Birthday', 2027),
('2027-02-15', 'Presidents Day', 'Presidents Day', 2027),
('2027-03-17', 'St. Patricks Day', 'St. Patrick''s Day', 2027),
('2027-05-31', 'Memorial Day', 'Memorial Day', 2027),
('2027-07-04', 'Independence Day', 'Independence Day', 2027),
('2027-09-06', 'Labor Day', 'Labor Day', 2027),
('2027-10-11', 'Columbus Day', 'Columbus Day', 2027),
('2027-11-11', 'Veterans Day', 'Veterans Day', 2027),
('2027-11-25', 'Thanksgiving', 'Thanksgiving Day', 2027),
('2027-11-26', 'Day After Thanksgiving', 'Day after Thanksgiving', 2027),
('2027-12-25', 'Christmas', 'Christmas Day', 2027);
