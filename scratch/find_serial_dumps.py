import os

log_dirs = [r"E:\Infobac\InfoMotel\Log", r"E:\Infobac\InfoMotel\LogSis"]
output_file = r"d:\Área de Trabalho\MotelInteligente\outroSistema\dicas_serial_dumps.txt"

print("Rastreando dumps seriais nos logs...")

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
                
                read_size = min(size, 5 * 1024 * 1024)
                with open(file_path, "rb") as f:
                    if size > read_size:
                        f.seek(size - read_size)
                    content_bytes = f.read()
                
                content = content_bytes.decode("cp1252", errors="ignore")
                lines = content.splitlines()
                
                for line in lines:
                    line_lower = line.lower()
                    if ";sql;" in line_lower:
                        continue
                    if "serial" in line_lower or "ler" in line_lower or "escrever" in line_lower:
                        findings.append(f"[{file}] {line.strip()}")
                            
            except Exception as e:
                pass

with open(output_file, "w", encoding="utf-8") as f:
    f.write("\n".join(findings))

print(f"Rastreamento concluído! {len(findings)} linhas salvas.")
