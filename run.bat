@echo off
echo ========================================================
echo   Launching CampusFace Attendance Management System
echo ========================================================

java -cp "bin;lib/*" Main

if %errorlevel% neq 0 (
    echo.
    echo Application exited with code %errorlevel%.
    pause
)
