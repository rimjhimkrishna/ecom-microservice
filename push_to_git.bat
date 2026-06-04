@echo off
title Push E-Commerce Microservices to Git
echo ======================================================================
echo          Pushing E-Commerce Microservices Platform to Git
echo ======================================================================
echo.

echo [Step 1] Initializing local Git repository...
git init

echo.
echo [Step 2] Staging all files...
git add .

echo.
echo [Step 3] Creating commit with professional Conventional Commit message...
git commit -m "feat: implement scalable e-commerce microservices platform with Spring Boot, Kafka, Redis, PostgreSQL, MongoDB, and Docker" -m "- Set up Discovery Server (Eureka) & API Gateway (Spring Cloud Gateway)" -m "- Implement User Service with JWT, refresh token rotation, and RBAC" -m "- Create Product catalog service with Redis caching & soft deletes" -m "- Establish Order transaction service with Feign stock updates & Kafka producers" -m "- Set up Notification consumer service with MongoDB storage" -m "- Configure Docker Compose configurations with healthcheck dependencies" -m "- Write comprehensive Postman Collection and README documentation"

echo.
echo [Step 4] Renaming branch to main...
git branch -M main

echo.
echo [Step 5] Adding remote GitHub repository URL...
git remote remove origin >nul 2>&1
echo.
echo Please enter your new GitHub repository URL (e.g., https://github.com/username/new-repo.git)
set /p REPO_URL="Repository URL: "
git remote add origin %REPO_URL%

echo.
echo [Step 6] Pushing code to GitHub...
echo (Note: If your credentials are not cached, Git will prompt you for a sign-in/Personal Access Token)
echo.
git push -u origin main --force

if %ERRORLEVEL% neq 0 (
    echo.
    echo [ERROR] Push failed! Please make sure your repository exists, your credentials are correct, or check your internet connection.
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo ======================================================================
echo [SUCCESS] Code successfully pushed to:
echo %REPO_URL%
echo ======================================================================
echo.
pause
