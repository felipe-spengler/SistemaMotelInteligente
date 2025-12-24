@echo off
setlocal enabledelayedexpansion

echo Encerrando processo javaw.exe...
taskkill /IM javaw.exe /F >nul 2>&1
:waitJavaw
tasklist /FI "IMAGENAME eq javaw.exe" | find /I "javaw.exe" >nul
if %ERRORLEVEL% equ 0 (
    timeout /t 2 >nul
    goto waitJavaw
)

if exist "C:\Users\inten\OneDrive\Desktop\.\MotelInteligente-20251216-012223.jar" (
    del /f /q "C:\Users\inten\OneDrive\Desktop\.\MotelInteligente-20251216-012223.jar"
)

copy /y "C:\Users\inten\Documents\logs\MotelInteligente-20251216-185520.jar" "C:\Users\inten\OneDrive\Desktop\.\MotelInteligente-20251216-185520.jar" >nul
del /f /q "C:\Users\inten\Documents\logs\MotelInteligente-20251216-185520.jar" >nul

echo Iniciando nova versão...
start "" /D "." javaw -jar "C:\Users\inten\OneDrive\Desktop\.\MotelInteligente-20251216-185520.jar"
timeout /t 2 >nul
del "%~f0" >nul 2>&1
endlocal
exit
