import os

log_dirs = [r"E:\Infobac\InfoMotel\Log", r"E:\Infobac\InfoMotel\LogSis"]
output_file = r"d:\Área de Trabalho\MotelInteligente\outroSistema\dicas_serial.txt"

print("Rastreando comandos de escrita e leitura serial...")

findings = []

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
                
                # Para logs, vamos ler os arquivos de trás para frente ou buscar o arquivo inteiro
                # se for menor que 5MB, senão pegamos os últimos 5MB.
                read_size = min(size, 5 * 1024 * 1024)
                with open(file_path, "rb") as f:
                    if size > read_size:
                        f.seek(size - read_size)
                    content_bytes = f.read()
                
                content = content_bytes.decode("cp1252", errors="ignore")
                
                lines = content.splitlines()
                for line in lines:
                    line_lower = line.lower()
                    if "escrita na porta serial" in line_lower or "leitura na porta serial" in line_lower:
                        findings.append(f"[{file}] {line.strip()}")
                        
            except Exception as e:
                pass

with open(output_file, "w", encoding="utf-8") as f:
    f.write("\n".join(findings))

print(f"Rastreamento concluído! {len(findings)} comandos salvos em dicas_serial.txt.")
