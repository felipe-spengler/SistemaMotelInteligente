@echo off
echo ==================================================
echo   INICIANDO PROCESSO DE RELEASE AUTOMATICA
echo ==================================================
powershell -ExecutionPolicy Bypass -File .\deploy.ps1
pause
