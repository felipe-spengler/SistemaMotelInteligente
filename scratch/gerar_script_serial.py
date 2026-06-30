import sys
import os

ps_script = """
# Script para controle de relés e placas RS485 no Motel
# Libera as portas fechando temporariamente o gateway e reabre após o envio!

param (
    [string]$PortName = "COM3",
    [byte]$BoardAddress = 1,
    [byte]$Channel = 32,      # LB032 = 32, LD064 = 64, LP000 = 0
    [byte]$RelayIndex = 1,    # Ex: Relé 1
    [byte]$State = 1          # 1 = Ligar/Abrir, 0 = Desligar/Fechar
)

Write-Host "=================================================="
Write-Host "=== CONTROLE DE AUTOMACAO RS485 (PORTA SERIAL) ==="
Write-Host "=================================================="
Write-Host "Porta COM: $PortName"
Write-Host "Placa (ID): $BoardAddress"
Write-Host "Canal (LB/LD/LP): $Channel"
Write-Host "Relé: $RelayIndex"
Write-Host "Estado: $State"
Write-Host "--------------------------------------------------"

# 1. Fechar temporariamente o gateway para liberar a porta COM
$processName = "InfoAutomacaoDispositivo"
$process = Get-Process | Where-Object { $_.Name -like "*$processName*" }
$restartPath = ""

if ($process) {
    Write-Host "Detectado processo ativo: $($process.Name) (PID: $($process.Id))" -ForegroundColor Yellow
    # Armazena o caminho do executável para reiniciar depois
    try {
        $restartPath = $process.Path
        if (!$restartPath) {
            $restartPath = "C:\\Infobac\\InfoMotel\\InfoAutomacaoDispositivo.exe"
        }
    } catch {
        $restartPath = "C:\\Infobac\\InfoMotel\\InfoAutomacaoDispositivo.exe"
    }
    
    Write-Host "Finalizando temporariamente para liberar a porta serial..." -ForegroundColor Cyan
    Stop-Process -Id $process.Id -Force
    Start-Sleep -Seconds 2
} else {
    Write-Host "Nenhum gateway ativo segurando as portas COM. Prosseguindo..." -ForegroundColor Gray
}

try {
    # 2. Conectar na Porta Serial e enviar o comando
    $port = New-Object System.IO.Ports.SerialPort($PortName, 9600, "None", 8, "One")
    $port.Open()
    Write-Host "Porta $PortName aberta com sucesso!" -ForegroundColor Green

    $payload = [byte[]]@($BoardAddress, $Channel, $RelayIndex, $State)
    
    # Cálculo do Checksum (LRC clássico do Delphi - soma acumulada invertida de dois complementos)
    $checksum = 0
    foreach ($b in $payload) {
        $checksum = ($checksum + $b) -band 0xFF
    }
    $checksum = (([byte](-$checksum)) -band 0xFF)
    
    $packet = [byte[]]@($BoardAddress, $Channel, $RelayIndex, $State, $checksum)
    
    $hexPacket = ($packet | ForEach-Object { "{0:X2}" -f $_ }) -join " "
    Write-Host "Enviando bytes físicos na rede: $hexPacket" -ForegroundColor Green

    $port.Write($packet, 0, $packet.Length)
    
    # Aguarda breve resposta da placa
    Start-Sleep -Milliseconds 250
    if ($port.BytesToRead -gt 0) {
        $response = New-Object byte[] $port.BytesToRead
        $port.Read($response, 0, $port.BytesToRead)
        $hexResponse = ($response | ForEach-Object { "{0:X2}" -f $_ }) -join " "
        Write-Host "Resposta da placa: $hexResponse" -ForegroundColor Yellow
    } else {
        Write-Host "Nenhum retorno recebido da placa (Normal para RS485 unidirecional)." -ForegroundColor DarkYellow
    }

    $port.Close()
    Write-Host "Porta $PortName fechada."
} catch {
    Write-Host "ERRO ao enviar dados na serial: $_" -ForegroundColor Red
}

# 3. Reiniciar o gateway de automação automaticamente se ele estava rodando
if ($restartPath) {
    Write-Host "Reiniciando o gateway de automação ($restartPath)..." -ForegroundColor Cyan
    try {
        # Executa em segundo plano sem travar o console do terminal
        Start-Process -FilePath $restartPath -WindowStyle Minimized
        Write-Host "Gateway iniciado com sucesso!" -ForegroundColor Green
    } catch {
        Write-Host "ATENCAO: Nao foi possivel iniciar o gateway automaticamente. Caminho: $restartPath" -ForegroundColor Red
        Write-Host "Inicie-o manualmente abrindo a pasta C:\\Infobac\\InfoMotel\\InfoAutomacaoDispositivo.exe." -ForegroundColor Yellow
    }
}
Write-Host "=================================================="
"""

output_path = r"d:\Área de Trabalho\MotelInteligente\outroSistema\teste_controle_serial.ps1"
with open(output_path, "w", encoding="utf-8") as f:
    f.write(ps_script)

print("Script de controle serial atualizado com sucesso com liberação e reinicialização automática do gateway!")
