@echo off
echo ========================================================
echo   Building CampusFace Attendance Management System
echo ========================================================

if not exist bin (
    mkdir bin
)

echo Compiling Java source files...
javac -cp "lib/*;src" -d bin src/model/*.java src/dao/*.java src/util/*.java src/service/*.java src/controller/*.java src/view/*.java src/Main.java src/test/*.java

if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Compilation failed! Check error messages above.
    pause
    exit /b %errorlevel%
)

echo.
echo [SUCCESS] Compilation finished successfully. All class files placed in bin/
pause
