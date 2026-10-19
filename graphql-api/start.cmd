@echo off
setlocal
cd /d "%~dp0"
call ..\mvnw.cmd -f pom.xml clean package
if errorlevel 1 exit /b 1
java -jar target\graphql-api.jar
