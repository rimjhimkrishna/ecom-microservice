@echo off
title Build E-Commerce Microservices
echo ======================================================================
echo          Building E-Commerce Microservices Platform
echo ======================================================================
echo.

echo Running Maven Clean Package...
call mvn clean package -DskipTests

if %ERRORLEVEL% neq 0 (
    echo.
    echo [ERROR] Build failed! Please check the logs above.
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo ======================================================================
echo [SUCCESS] All microservice modules built successfully!
echo ======================================================================
echo.
pause
