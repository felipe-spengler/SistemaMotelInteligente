@echo off
title Diagnostico Serial Controtec
powershell -NoProfile -ExecutionPolicy Bypass -Command "$code = Get-Content '%~f0'; $ps = $code[6..($code.Length-1)] -join [char]10; Invoke-Expression $ps"
pause
exit /b

# === CÓDIGO POWERSHELL EMBUTIDO ===
Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host "=== MODO ESCUTA (SNIFFER) SERIAL CONTROTEC (COM3 E COM4) ===" -ForegroundColor Cyan
Write-Host "======================================================================`n"

# 1. Verificar portas físicas presentes no Windows
$portasNoSistema = [System.IO.Ports.SerialPort]::GetPortNames()
Write-Host "Portas COM detectadas no Windows: [ $([string]::Join(', ', $portasNoSistema)) ]`n" -ForegroundColor Gray

$serial3 = $null
$serial4 = $null

# Tentar inicializar COM3
if ($portasNoSistema -contains "COM3") {
    $serial3 = New-Object System.IO.Ports.SerialPort("COM3", 9600, "None", 8, "One")
    try {
        $serial3.Open()
        Write-Host "[OK] Escuta iniciada na COM3 (9600 bps)" -ForegroundColor Green
    } catch {
        Write-Host "[ERRO] Não foi possível abrir a porta COM3. Motivo: $_" -ForegroundColor Red
        $serial3 = $null
    }
} else {
    Write-Host "[AVISO] COM3 não encontrada física no computador." -ForegroundColor Yellow
}

# Tentar inicializar COM4
if ($portasNoSistema -contains "COM4") {
    $serial4 = New-Object System.IO.Ports.SerialPort("COM4", 9600, "None", 8, "One")
    try {
        $serial4.Open()
        Write-Host "[OK] Escuta iniciada na COM4 (9600 bps)" -ForegroundColor Green
    } catch {
        Write-Host "[ERRO] Não foi possível abrir a porta COM4. Motivo: $_" -ForegroundColor Red
        $serial4 = $null
    }
} else {
    Write-Host "[AVISO] COM4 não encontrada física no computador." -ForegroundColor Yellow
}

if ($null -eq $serial3 -and $null -eq $serial4) {
    Write-Host "`n[ERRO] Nenhuma das portas (COM3 ou COM4) pôde ser aberta para escuta!" -ForegroundColor Red
    Write-Host "Verifique se o Gateway Java ou outro programa de automação está rodando e ocupando as portas." -ForegroundColor Yellow
    exit
}

Write-Host "`n==============================================================" -ForegroundColor Cyan
Write-Host " ESCUTANDO PORTAS COM EM TEMPO REAL..." -ForegroundColor Green
Write-Host " -> Vá no corredor e pressione o botão físico que ativa a placa." -ForegroundColor Yellow
Write-Host " -> Qualquer byte recebido será exibido na tela abaixo." -ForegroundColor Yellow
Write-Host " -> Pressione [Ctrl + C] para parar a escuta a qualquer momento." -ForegroundColor Yellow
Write-Host "==============================================================`n"

try {
    while ($true) {
        # Verificar dados na COM3
        if ($null -ne $serial3 -and $serial3.IsOpen -and $serial3.BytesToRead -gt 0) {
            $buffer = New-Object byte[] $serial3.BytesToRead
            $read = $serial3.Read($buffer, 0, $buffer.Length)
            
            $hexStr = ""
            for ($i = 0; $i -lt $read; $i++) { $hexStr += "0x{0:X2} " -f $buffer[$i] }
            
            Write-Host "[$(Get-Date -Format 'HH:mm:ss.fff')] COM3 <- [ $hexStr]" -ForegroundColor Green
        }

        # Verificar dados na COM4
        if ($null -ne $serial4 -and $serial4.IsOpen -and $serial4.BytesToRead -gt 0) {
            $buffer = New-Object byte[] $serial4.BytesToRead
            $read = $serial4.Read($buffer, 0, $buffer.Length)
            
            $hexStr = ""
            for ($i = 0; $i -lt $read; $i++) { $hexStr += "0x{0:X2} " -f $buffer[$i] }
            
            Write-Host "[$(Get-Date -Format 'HH:mm:ss.fff')] COM4 <- [ $hexStr]" -ForegroundColor Yellow
        }

        # Evitar uso de CPU 100%
        Start-Sleep -Milliseconds 50
    }
} catch {
    # Captura interrupções graciosamente
} finally {
    if ($null -ne $serial3) { $serial3.Close(); Write-Host "`nPorta COM3 fechada graciosamente." -ForegroundColor Gray }
    if ($null -ne $serial4) { $serial4.Close(); Write-Host "Porta COM4 fechada graciosamente." -ForegroundColor Gray }
}
