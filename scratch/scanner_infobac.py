import os

target_dir = r"E:\Infobac\InfoMotel"
output_file = r"d:\Área de Trabalho\MotelInteligente\outroSistema\dicas.txt"

print(f"Iniciando escaneamento otimizado em {target_dir}...")

keywords = [
    b"COM1", b"COM2", b"COM3", b"COM4", b"COM5", b"COM6", b"COM7", b"COM8", b"COM9",
    b"BaudRate", b"BaudRate", b"9600", b"19200", b"38400", b"115200",
    b"Modbus", b"modbus", b"serial", b"SerialPort", b"Device", b"Placa", b"Rele", b"Relay",
    b"Conectar", b"socket", b"Socket", b"TCP", b"UDP", b"Porta", b"TX", b"RX"
]

findings = []
ignored_dirs = {"log", "logsis", "backup", "temp", "imagem_camera", "update"}

# Procura arquivos exe, bpl, dll
for root, dirs, files in os.walk(target_dir):
    # Modifica dirs in-place para que o os.walk pule as pastas de logs/backup/temp
    dirs[:] = [d for d in dirs if d.lower() not in ignored_dirs]
    
    for file in files:
        if file.lower().endswith(('.exe', '.dll', '.bpl', '.icf')):
            file_path = os.path.join(root, file)
            try:
                size = os.path.getsize(file_path)
                if size > 15 * 1024 * 1024:
                    continue
                
                with open(file_path, "rb") as f:
                    content = f.read()
                    
                file_matches = []
                for kw in keywords:
                    if kw in content:
                        idx = content.index(kw)
                        start = max(0, idx - 45)
                        end = min(len(content), idx + len(kw) + 45)
                        chunk = content[start:end]
                        chunk_str = "".join([chr(b) if 32 <= b <= 126 else "." for b in chunk])
                        file_matches.append(f"  - Termo '{kw.decode()}' encontrado no contexto: ...{chunk_str}...")
                
                if file_matches:
                    findings.append(f"\n[Arquivo: {file} ({file_path})]")
                    findings.extend(file_matches)
                    
            except Exception as e:
                pass

# Escreve os resultados no dicas.txt
header = """=============================================================================
RESULTADOS DA ENGENHARIA REVERSA AUTOMATIZADA - HARDWARE INFOBAC
=============================================================================
Este arquivo contém as strings e indícios encontrados dentro dos arquivos
do sistema Infobac referentes à comunicação com as placas de relé/corredor.

"""

try:
    with open(output_file, "r", encoding="utf-8") as f:
        existing_content = f.read()
except Exception:
    existing_content = ""

# Se já houver um bloco de descobertas automáticas anterior, removemos para evitar duplicação
if "DESCOBERTAS TÉCNICAS DO VARREDOR AUTOMÁTICO:" in existing_content:
    parts = existing_content.split("DESCOBERTAS TÉCNICAS DO VARREDOR AUTOMÁTICO:")
    existing_content = parts[0] # mantém apenas o planejamento superior

divider = "\n\n=============================================================================\nDESCOBERTAS TÉCNICAS DO VARREDOR AUTOMÁTICO:\n=============================================================================\n"
new_content = header + divider + "\n".join(findings) + "\n\n" + existing_content

with open(output_file, "w", encoding="utf-8") as f:
    f.write(new_content)

print(f"Varredura concluída com sucesso! Encontrados {len(findings)} indícios.")
