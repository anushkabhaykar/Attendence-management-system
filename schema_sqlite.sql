-- =======================================================
-- Database Schema for Face Recognition Attendance Management System
-- College Project: B.Tech Computer Science & Engineering
-- Database: SQLite 3
-- =======================================================

PRAGMA foreign_keys = ON;

-- -------------------------------------------------------
-- 1. Table: users
-- Stores administrative and teaching staff credentials
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    full_name TEXT NOT NULL,
    role TEXT NOT NULL DEFAULT 'TEACHER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- -------------------------------------------------------
-- 2. Table: students
-- Stores student demographic and academic details
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS students (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    roll_no TEXT NOT NULL UNIQUE,
    name TEXT NOT NULL,
    gender TEXT NOT NULL,
    dob TEXT NOT NULL,
    department TEXT NOT NULL,
    course TEXT NOT NULL,
    year INTEGER NOT NULL,
    division TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE,
    phone TEXT NOT NULL,
    face_reference TEXT DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_department ON students(department);
CREATE INDEX IF NOT EXISTS idx_year_div ON students(year, division);

-- -------------------------------------------------------
-- 3. Table: face_data
-- Stores biometric face feature encodings (LBPH / feature vectors)
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS face_data (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    student_id INTEGER NOT NULL,
    encoding TEXT NOT NULL,
    sample_count INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_student_face ON face_data(student_id);

-- -------------------------------------------------------
-- 4. Table: attendance
-- Records attendance events with duplicate prevention
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS attendance (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    student_id INTEGER NOT NULL,
    attendance_date TEXT NOT NULL,
    attendance_time TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'PRESENT' CHECK (status IN ('PRESENT', 'ABSENT', 'LATE')),
    method TEXT NOT NULL DEFAULT 'Face Recognition',
    session_id TEXT NOT NULL DEFAULT 'Morning Session',
    marked_by TEXT NOT NULL DEFAULT 'System',
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    UNIQUE (student_id, attendance_date, session_id)
);

CREATE INDEX IF NOT EXISTS idx_attendance_date ON attendance(attendance_date);
CREATE INDEX IF NOT EXISTS idx_attendance_student ON attendance(student_id);

-- -------------------------------------------------------
-- Seed Initial Users (Password: admin123 and teacher123 with SHA-256)
-- Hash for 'admin123': 240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9
-- Hash for 'teacher123': ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f
-- -------------------------------------------------------
INSERT OR IGNORE INTO users (id, username, password_hash, full_name, role) VALUES
(1, 'admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'Administrator', 'ADMIN'),
(2, 'teacher', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'Prof. Sharma', 'TEACHER');
