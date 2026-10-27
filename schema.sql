-- =======================================================
-- Database Schema for Face Recognition Attendance Management System
-- College Project: B.Tech Computer Science & Engineering
-- Database: MySQL
-- =======================================================

DROP DATABASE IF EXISTS attendance_system;
CREATE DATABASE attendance_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE attendance_system;

-- -------------------------------------------------------
-- 1. Table: users
-- Stores administrative and teaching staff credentials
-- -------------------------------------------------------
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'TEACHER', -- 'ADMIN' or 'TEACHER'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- -------------------------------------------------------
-- 2. Table: students
-- Stores student demographic and academic details
-- -------------------------------------------------------
CREATE TABLE students (
    id INT AUTO_INCREMENT PRIMARY KEY,
    roll_no VARCHAR(30) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    gender VARCHAR(15) NOT NULL,
    dob DATE NOT NULL,
    department VARCHAR(50) NOT NULL,
    course VARCHAR(50) NOT NULL,
    year INT NOT NULL,
    division VARCHAR(10) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20) NOT NULL,
    face_reference VARCHAR(255) DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_department (department),
    INDEX idx_year_div (year, division)
) ENGINE=InnoDB;

-- -------------------------------------------------------
-- 3. Table: face_data
-- Stores biometric face feature encodings (LBPH / feature vectors)
-- -------------------------------------------------------
CREATE TABLE face_data (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    encoding MEDIUMTEXT NOT NULL,
    sample_count INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    INDEX idx_student_face (student_id)
) ENGINE=InnoDB;

-- -------------------------------------------------------
-- 4. Table: attendance
-- Records attendance events with duplicate prevention
-- -------------------------------------------------------
CREATE TABLE attendance (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    attendance_date DATE NOT NULL,
    attendance_time TIME NOT NULL,
    status ENUM('PRESENT', 'ABSENT', 'LATE') NOT NULL DEFAULT 'PRESENT',
    method ENUM('Face Recognition', 'Manual') NOT NULL DEFAULT 'Face Recognition',
    session_id VARCHAR(50) NOT NULL DEFAULT 'Morning Session',
    marked_by VARCHAR(50) NOT NULL DEFAULT 'System',
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    UNIQUE KEY uq_attendance (student_id, attendance_date, session_id),
    INDEX idx_attendance_date (attendance_date),
    INDEX idx_attendance_student (student_id)
) ENGINE=InnoDB;

-- -------------------------------------------------------
-- Seed Initial Users (Password: admin123 and teacher123 with SHA-256)
-- Hash for 'admin123': 240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9
-- Hash for 'teacher123': ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f
-- -------------------------------------------------------
INSERT INTO users (username, password_hash, full_name, role) VALUES
('admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'Administrator', 'ADMIN'),
('teacher', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'Prof. Sharma', 'TEACHER');

-- -------------------------------------------------------
-- Seed Sample Students
-- -------------------------------------------------------
INSERT INTO students (roll_no, name, gender, dob, department, course, year, division, email, phone, face_reference) VALUES
('CS202601', 'Rahul Sharma', 'Male', '2004-05-14', 'Computer Science', 'B.Tech', 3, 'A', 'rahul.sharma@college.edu', '9876543210', 'face_CS202601.dat'),
('CS202602', 'Priya Patel', 'Female', '2004-08-22', 'Computer Science', 'B.Tech', 3, 'A', 'priya.patel@college.edu', '9876543211', 'face_CS202602.dat'),
('CS202603', 'Aman Verma', 'Male', '2003-12-05', 'Computer Science', 'B.Tech', 3, 'B', 'aman.verma@college.edu', '9876543212', NULL),
('IT202601', 'Sneha Kulkarni', 'Female', '2004-03-19', 'Information Tech', 'B.Tech', 2, 'A', 'sneha.k@college.edu', '9876543213', NULL),
('EC202601', 'Vikram Singh', 'Male', '2003-11-28', 'Electronics', 'B.Tech', 4, 'A', 'vikram.singh@college.edu', '9876543214', NULL);

-- -------------------------------------------------------
-- Seed Sample Attendance (Today's date and past dates)
-- -------------------------------------------------------
INSERT INTO attendance (student_id, attendance_date, attendance_time, status, method, session_id, marked_by) VALUES
(1, CURDATE(), '09:05:12', 'PRESENT', 'Face Recognition', 'Morning Session', 'Face AI System'),
(2, CURDATE(), '09:07:44', 'PRESENT', 'Face Recognition', 'Morning Session', 'Face AI System'),
(3, CURDATE(), '09:15:00', 'ABSENT', 'Manual', 'Morning Session', 'Prof. Sharma'),
(4, CURDATE(), '09:12:30', 'PRESENT', 'Manual', 'Morning Session', 'Prof. Sharma');
