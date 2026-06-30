import os

log_dirs = [r"E:\Infobac\InfoMotel\Log", r"E:\Infobac\InfoMotel\LogSis"]
output_file = r"d:\Área de Trabalho\MotelInteligente\outroSistema\dicas.txt"

keywords = ["porta serial", "escrita", "leitura", "comm", "serialport", "com1", "com2", "com3", "com4", "com5", "com6"]
findings = []

print("Iniciando escaneamento de logs...")

for log_dir in log_dirs:
    if not os.path.exists(log_dir):
        continue
    for root, dirs, files in os.walk(log_dir):
        for file in files:
            if not file.lower().endswith(".txt"):
                continue
            file_path = os.path.join(root, file)
            try:
                size = os.path.getsize(file_path)
                if size == 0:
                    continue
                
                # Para arquivos grandes, lê apenas os últimos 2MB
                read_size = min(size, 2 * 1024 * 1024)
                with open(file_path, "rb") as f:
                    if size > read_size:
                        f.seek(size - read_size)
                    content_bytes = f.read()
                
                # Tenta decodificar como ANSI (cp1252/latin1) que é o padrão do Delphi no Windows
                content = content_bytes.decode("cp1252", errors="ignore")
                
                lines = content.splitlines()
                file_matches = []
                for line in lines:
                    line_lower = line.lower()
                    # Ignora linhas puramente SQL para reduzir ruído
                    if ";sql;" in line_lower:
                        continue
                    for kw in keywords:
                        if kw in line_lower:
                            # Limita o tamanho da linha gravada
                            if len(line) < 300:
                                file_matches.append(line.strip())
                            break
                
                if file_matches:
                    # Pega no máximo 20 correspondências por arquivo para não inflar o arquivo
                    limited_matches = file_matches[-20:]
                    findings.append(f"\n[Arquivo de Log: {file} ({file_path})]")
                    for match in limited_matches:
                        findings.append(f"  - {match}")
                        
            except Exception as e:
                pass

divider = "\n\n=============================================================================\nREGISTROS DE COMUNICAÇÃO SERIAL EXTRAÍDOS DOS LOGS:\n=============================================================================\n"

try:
    with open(output_file, "r", encoding="utf-8") as f:
        existing_content = f.read()
except Exception:
    existing_content = ""

# Remove bloco anterior se existir
if "REGISTROS DE COMUNICAÇÃO SERIAL EXTRAÍDOS DOS LOGS:" in existing_content:
    parts = existing_content.split("REGISTROS DE COMUNICAÇÃO SERIAL EXTRAÍDOS DOS LOGS:")
    existing_content = parts[0]

new_content = existing_content + divider + "\n".join(findings) + "\n\n"

with open(output_file, "w", encoding="utf-8") as f:
    f.write(new_content)

print(f"Varredura de logs concluída! {len(findings)} arquivos continham correspondências relevantes.")
