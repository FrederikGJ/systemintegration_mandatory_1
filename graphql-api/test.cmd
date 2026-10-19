@echo off
setlocal
cd /d "%~dp0"
call ..\mvnw.cmd -f pom.xml test
