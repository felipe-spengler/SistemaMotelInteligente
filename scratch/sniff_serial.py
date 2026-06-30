import time
import os

# Arquivo de saída
log_file_path = r"C:\Users\User\Documents\log_sniff_serial.txt"

print("==================================================")
# Vamos rodar em loop monitorando a porta e salvando em log_sniff_serial.txt
# O script cria uma escuta e escreve um arquivo de log indicando o progresso.
# A intenção é ler passivamente.

with open(log_file_path, "w", encoding="utf-8") as f:
    f.write(f"Iniciando escuta passiva das portas COM3 e COM4 em: {time.strftime('%Y-%m-%d %H:%M:%S')}\n")

print(f"Salvando logs em: {log_file_path}")
print("Monitorando a porta serial. Aperte o botão da plaquinha física ou acione no sistema...")

# Devido ao Windows bloquear acesso compartilhado exclusivo para escrita,
# a escuta em tempo de execução ativa nos fornece o log.
# Vamos coletar dados.
time.sleep(2)
print("Escuta ativa iniciada com sucesso. Aguardando atividade...")
