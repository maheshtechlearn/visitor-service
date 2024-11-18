CREATE TABLE visitors (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    contact_number VARCHAR(20),
    email VARCHAR(25)
    purpose VARCHAR(255),
    check_in DATETIME,
    check_out DATETIME,
    duration BIGINT,
    approved BOOLEAN
);



INSERT INTO visitors (name, contact_number,email, purpose, check_in, check_out, duration, approved)
VALUES
    ('John Doe', '9876543210','test@yopmail.com', 'Business Meeting', '2024-11-12 09:00:00', '2024-11-12 17:00:00', 8, true),
    ('Jane Smith', '9123456789','test@yopmail.com', 'Interview', '2024-11-12 10:00:00', '2024-11-12 12:00:00', 2, false),
    ('Michael Brown', '9345678901','test@yopmail.com', 'Conference', '2024-11-12 08:00:00', '2024-11-12 18:00:00', 10, true);
