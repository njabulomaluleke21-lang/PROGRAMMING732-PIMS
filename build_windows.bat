@echo off
setlocal
where mvn >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
 echo Maven was not found.
 pause
 exit /b 1
)
if not exist dist mkdir dist
call mvn -q clean package
if %ERRORLEVEL% NEQ 0 (
 echo Build failed.
 pause
 exit /b 1
)
copy /Y target\pims-1.0.0.jar dist\njabs_pims.jar >nul
echo JAR created: dist\njabs_pims.jar
pause
