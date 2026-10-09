@echo off
title Expense Tracker Launcher
cd /d "%~dp0"

echo ===================================================
echo           Starting Expense Tracker App
echo ===================================================
echo.

:: Check Java Installation
java -version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Java is not installed or not found in system PATH.
    echo Please install JDK 17 or higher from https://adoptium.net/
    echo.
    pause
    exit /b 1
)

:: Ensure output directory exists
if not exist "out" mkdir out

:: Compile Java Source Code
echo Compiling source files...
javac -cp "lib\mysql-connector-j-8.3.0.jar" -d out src\model\*.java src\dao\*.java src\service\*.java src\util\*.java src\ui\*.java src\Main.java

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Compilation failed. Please check the errors above.
    echo.
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo [SUCCESS] Compilation completed successfully.
echo Launching application...
echo.

:: Launch the Application (runs with terminal or standalone window)
start javaw -cp "out;lib\mysql-connector-j-8.3.0.jar" Main

exit 0
