@echo off
title CampusFace Attendance System
cd /d "%~dp0"

:: Check if MySQL is requested in db.properties
findstr /i "db.type=mysql" db.properties >nul 2>&1
if %errorlevel% equ 0 (
    echo [Notice] MySQL mode active in db.properties. Checking MySQL server...
    netstat -ano | findstr 3306 >nul
    if %errorlevel% neq 0 (
        if exist "C:\xampp\mysql\bin\mysqld.exe" (
            echo Starting MySQL server...
            start /b "" "C:\xampp\mysql\bin\mysqld.exe" --defaults-file="C:\xampp\mysql\bin\my.ini" --standalone
            timeout /t 2 /nobreak >nul
        )
    )
) else (
    echo [CampusFace] Running with zero-configuration SQLite database.
)

:: Launch the Application
echo Launching CampusFace...
java -cp "bin;lib/*" Main
