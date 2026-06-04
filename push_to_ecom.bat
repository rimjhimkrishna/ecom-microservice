@echo off
title Push to ecom-microservice Repository
echo ======================================================================
echo          Pushing E-Commerce Microservices to ecom-microservice
echo ======================================================================
echo.

echo [Step 1] Initializing Git and staging files...
git init
git add .

echo.
echo [Step 2] Committing files...
git commit -m "feat: initial commit for e-commerce microservices platform" >nul 2>&1

echo.
echo [Step 3] Setting branch to main...
git branch -M main

echo.
echo [Step 4] Linking to new ecom-microservice repository...
git remote remove origin >nul 2>&1
git remote add origin https://github.com/rimjhimkrishna/ecom-microservice.git

echo.
echo [Step 5] Pushing to GitHub...
echo (Note: If your credentials are not cached, Git will prompt you for sign-in)
git push -u origin main --force

if %ERRORLEVEL% neq 0 (
    echo.
    echo [ERROR] Push failed!
    echo Please make sure you have created the repository "ecom-microservice" on GitHub first:
    echo https://github.com/new (Name: ecom-microservice)
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo ======================================================================
echo [SUCCESS] Code successfully pushed to:
echo https://github.com/rimjhimkrishna/ecom-microservice
echo ======================================================================
echo.
pause
