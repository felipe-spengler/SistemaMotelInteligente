@echo off
title Sistema de Teste Serial Controtec
echo ==================================================
echo =   INICIANDO SERVIDOR DE TESTE SERVIÇO JAVA     =
echo ==================================================
echo.

:: Verifica se a porta 8080 está em uso e encerra para evitar conflitos
for /f "tokens=5" %%a in ('netstat -aon ^| findstr ":8080"') do (
    echo A porta 8080 estava ocupada, liberando...
    taskkill /F /PID %%a 2>nul
)

:: Executa o Maven wrapper para iniciar o servidor Spring Boot local
call mvnw.cmd spring-boot:run

pause
