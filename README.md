# CampusFace - Face Recognition Attendance Management System
**Academic Project for B.Tech Computer Science & Engineering**

---

## 📌 Project Overview
**CampusFace** is an automated biometric desktop attendance management system engineered in **Java (Swing)**, powered by a relational **SQLite 3** database (with optional **MySQL** support) via **JDBC**, and featuring real-time **Face Detection and Local Binary Pattern Histogram (LBPH) Face Recognition**.

The application eliminates proxy attendance, saves class lecture time, and provides transparent academic monitoring for college teachers, administrators, and students.

> 📖 **Comprehensive Project Documentation:** For detailed mathematical models (LBPH, Chi-Square, YCbCr), pipeline diagrams, exception handling, and viva Q&A, see [PROJECT_DOCUMENTATION.md](file:///c:/Users/anush/Attendence%20management%20system/PROJECT_DOCUMENTATION.md).

---

## 🚀 Key Highlights & Architectural Strengths
- **Zero-Setup Embedded SQLite Database**: No XAMPP, MySQL server, or database installation required! Automatically creates and seeds the database file (`attendance_system.db`) on first launch.
- **Dual-Database Engine Architecture**: Defaults to SQLite for portable plug-and-play execution, while preserving complete support for MySQL through simple switching in `db.properties`.
- **Pure Java Core with Native Drivers**: Runs seamlessly on Windows/macOS/Linux.
- **Hardware & Virtual Dual-Feed Camera**: Auto-detects physical webcams; provides an interactive test image feed and virtual camera fallback for viva demonstrations and evaluation labs without webcam hardware.
- **Biometric Multi-Sample Enrollment**: Captures 10 face sample angles, computes normalized 1024-dimensional feature vectors, and persists encodings in the database.
- **Strict Duplicate Attendance Prevention**: Enforced both at business service layer and at the database relational engine via composite unique keys (`student_id`, `attendance_date`, `session_id`).
- **Clean MVC Architecture**: Strict separation between Data Models, Data Access Objects (DAO), Business Services, Event Controllers, and Swing Views.
- **Modern College Aesthetics**: Powered by FlatLaf with navy blue and emerald accents, card panels, biometric HUD bounding box overlays, and live statistical donut charts.
- **1-Click CSV Reporting**: Instantly exports daily, weekly, monthly, and student-wise attendance records to CSV for spreadsheet analysis.

---

## 📂 Directory Layout
```
├── lib/                             # External library JAR files
│   ├── sqlite-jdbc-3.45.1.0.jar     # SQLite JDBC Driver (Embedded Database)
│   ├── mysql-connector-j-8.3.0.jar  # MySQL JDBC Connector (Optional)
│   ├── flatlaf-3.4.1.jar            # Modern Swing Look and Feel
│   ├── webcam-capture-0.3.12.jar    # Hardware Camera DirectShow Driver
│   ├── bridj-0.7.0.jar              # Native C/C++ Bridge
│   ├── slf4j-api-1.7.2.jar          # Logging API
│   └── slf4j-simple-1.7.2.jar       # Simple Logger
├── src/
│   ├── model/                       # Entity Domain Models
│   │   ├── User.java
│   │   ├── Student.java
│   │   ├── FaceData.java
│   │   ├── Attendance.java
│   │   └── AttendanceReport.java
│   ├── dao/                         # Data Access Objects (JDBC PreparedStatements)
│   │   ├── UserDAO.java
│   │   ├── StudentDAO.java
│   │   ├── FaceDataDAO.java
│   │   └── AttendanceDAO.java
│   ├── service/                     # Business Logic Layer
│   │   ├── AuthenticationService.java
│   │   ├── FaceRecognitionService.java
│   │   └── AttendanceService.java
│   ├── controller/                  # MVC Controllers & Navigation
│   │   ├── LoginController.java
│   │   ├── DashboardController.java
│   │   ├── StudentController.java
│   │   └── AttendanceController.java
│   ├── view/                        # Modern Swing UI Frames
│   │   ├── LoginFrame.java
│   │   ├── DashboardFrame.java
│   │   ├── StudentFrame.java
│   │   ├── FaceEnrollmentFrame.java
│   │   ├── RecognitionFrame.java
│   │   ├── AttendanceFrame.java
│   │   └── ReportsFrame.java
│   ├── util/                        # Reusable Utilities & Biometrics
│   │   ├── DBConnection.java        # Cross-Database JDBC Manager (SQLite / MySQL)
│   │   ├── PasswordUtil.java
│   │   ├── ValidationUtil.java
│   │   ├── FaceRecognitionEngine.java
│   │   ├── CameraManager.java
│   │   └── UIUtils.java
│   ├── test/                        # Automated Verification Test Suite
│   │   └── SystemVerificationTest.java
│   └── Main.java                    # Application Entry Point
├── data/
│   └── faces/                       # Enrolled biometric face crops & signatures
├── attendance_system.db             # Auto-generated SQLite database file
├── schema_sqlite.sql                # SQLite Database Schema and Seed Data
├── schema.sql                       # MySQL Database Schema and Seed Data
├── db.properties                    # Database connection credentials (sqlite/mysql)
├── build.bat                        # 1-Click Compilation Script
├── run.bat                          # 1-Click Application Launcher
├── test.bat                         # 1-Click System Test Suite
├── setup_sqlite.bat                 # 1-Click SQLite Reset/Initializer
└── setup_db.bat                     # Unified Database Diagnostics & Setup
```

---

## ⚡ Quick Start Instructions

### 1. Database Setup (Zero Configuration!)
By default, the application uses **SQLite** and **automatically creates and initializes** the database (`attendance_system.db`) and seed users on the very first run! No manual setup or server is required.

To manually re-initialize or reset the database at any time:
- Double-click `setup_sqlite.bat` or run:
  ```cmd
  java -cp "bin;lib/*" util.DBConnection --reset
  ```

*(Default credentials created: `admin` / `admin123` and `teacher` / `teacher123`)*

#### Switching to MySQL (Optional)
If you prefer running against a MySQL server (e.g., via XAMPP):
1. Open `db.properties` and change:
   ```properties
   db.type=mysql
   ```
2. Import `schema.sql` into MySQL.

### 2. Compilation
Double-click `build.bat` or run:
```cmd
javac -cp "lib/*;src" -d bin src/model/*.java src/dao/*.java src/util/*.java src/service/*.java src/controller/*.java src/view/*.java src/Main.java src/test/*.java
```

### 3. Run Automated Tests
Double-click `test.bat` or run:
```cmd
java -cp "bin;lib/*" test.SystemVerificationTest
```

### 4. Launch Application
Double-click `run.bat` or run:
```cmd
run.bat
```
*(or run `start.bat`)*

---

## 📸 Face Attendance Quick Guide (Simple 1-2-3 Workflow)

### 1. Launch & Instant Access (No Password Required!)
When you launch the application, you have instant access right on the welcome screen:
* Click **`📸 START FACE ATTENDANCE (SCANNER)`** to open the live camera scanner immediately!
* Click **`➕ Quick Register New Student Face`** to add a student in 5 seconds!
*(Or login with `admin` / `admin123` to access the full administration dashboard).*

---

### 2. How to Register a New Student Face (Takes 5 Seconds)
1. In the Face Scanner window, click **`➕ Quick Register Student & Face (New)`** (or click the green **`➕ Register Face`** button at the top).
2. Enter:
   - **Student Full Name** (e.g. `Anushka`)
   - **Roll Number** (e.g. `101`)
   - **Department** (e.g. `Computer Science`)
3. Click **`📸 Capture Live Face & Register`** (or click *Upload Photo* if testing from a saved photo).
4. The system automatically extracts biometric facial features, saves the student to the database, and loads their template into memory!

---

### 3. Taking Attendance by Scanning Faces
1. As the student looks into the camera:
   - The AI identifies their face in real-time.
   - The HUD paints a glowing emerald green bracket with **`✔ PRESENT | [Name] ([Roll No]) | Match %`**.
   - A bold emerald banner lights up: **`✔ PRESENT - MARKED FOR TODAY`**.
   - A pleasant confirmation chime plays.
   - Attendance is permanently recorded in the database for **Today's Date**.
   - The student instantly appears in the **"Today's Verified Attendees"** table on screen!
2. **Continuous Class Mode**: The scanner remains open continuously, allowing every student in class to be scanned one after another without closing or stopping.

---

### 4. Viewing Regular Attendance by Date
* In the Face Scanner window, click **`📅 View Attendance Logs by Date`** (or open **Attendance** from the dashboard).
* Use **`◀ Prev Day`** and **`▶ Next Day`** to navigate between dates, or click **`📅 All Dates (Regular)`** to see full historical roll calls.
* Click **`📥 Export CSV`** anytime for spreadsheet reporting.
