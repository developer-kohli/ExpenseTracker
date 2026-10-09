# Expense Tracker PowerShell One-Click Runner
$ErrorActionPreference = "Stop"
Set-Location -Path $PSScriptRoot

Write-Host "===================================================" -ForegroundColor Cyan
Write-Host "           Starting Expense Tracker App" -ForegroundColor Cyan
Write-Host "===================================================" -ForegroundColor Cyan

# 1. Check Java
try {
    $javaVer = & java -version 2>&1
    Write-Host "[OK] Java runtime detected." -ForegroundColor Green
} catch {
    Write-Host "[ERROR] Java is not installed or not in PATH." -ForegroundColor Red
    Write-Host "Please install JDK 17 or higher from https://adoptium.net/"
    Read-Host "Press Enter to exit..."
    exit 1
}

# 2. Compile if needed
if (-not (Test-Path "out")) {
    New-Item -ItemType Directory -Path "out" | Out-Null
}

Write-Host "Compiling Java source files..." -ForegroundColor Yellow
$compileCmd = & javac -cp "lib\mysql-connector-j-8.3.0.jar" -d out (Get-ChildItem -Recurse -Filter *.java -Path src | Select-Object -ExpandProperty FullName) 2>&1

if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] Compilation failed:" -ForegroundColor Red
    Write-Host $compileCmd
    Read-Host "Press Enter to exit..."
    exit $LASTEXITCODE
}

Write-Host "[SUCCESS] Build successful. Launching UI..." -ForegroundColor Green
Start-Process "javaw" -ArgumentList "-cp `"out;lib\mysql-connector-j-8.3.0.jar`" Main"
