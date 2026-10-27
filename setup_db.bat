@echo off
echo ========================================================
echo   CampusFace Database Setup Utility
echo ========================================================

if not exist bin (
    mkdir bin
)

echo Compiling database setup utility...
javac -cp "lib/*;src" -d bin src/util/DBConnection.java

if %errorlevel% neq 0 (
    echo [ERROR] Failed to compile DBConnection.java. Check Java installation.
    pause
    exit /b %errorlevel%
)

echo Verifying and initializing configured database...
java -cp "bin;lib/*" util.DBConnection

pause
