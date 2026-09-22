@echo off
setlocal
title Employee Leave Management System

echo =========================================================================
echo  Starting Employee Leave Management System (Spring Boot + Web Dashboard)
echo =========================================================================
echo.

:: Automatically free port 8080 if an existing background process is using it
powershell -NoProfile -Command "Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue | ForEach-Object { Stop-Process -Id $_.OwningProcess -Force -ErrorAction SilentlyContinue }" >nul 2>&1

echo Application will be available at:
echo   - Web Dashboard:     http://localhost:8080/
echo   - Swagger 3 UI:      http://localhost:8080/swagger-ui.html
echo   - H2 Web Console:    http://localhost:8080/h2-console
echo.
echo Launching application using Maven Wrapper...
echo.

if exist "mvnw.cmd" (
    call mvnw.cmd spring-boot:run
) else (
    call mvn spring-boot:run
)

pause
