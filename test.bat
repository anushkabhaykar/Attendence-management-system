@echo off
echo ========================================================
echo   Running CampusFace Automated Verification Test Suite
echo ========================================================

java -cp "bin;lib/*" test.SystemVerificationTest
echo.
java -cp "bin;lib/*" test.FaceVerificationAttendanceTest

pause
