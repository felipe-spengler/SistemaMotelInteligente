import os

log_dirs = [r"E:\Infobac\InfoMotel\Log", r"E:\Infobac\InfoMotel\LogSis"]
output_file = r"d:\Área de Trabalho\MotelInteligente\outroSistema\dicas_mapeamento.txt"

keywords = ["definição", "definicao", "peso", "porta", "suite", "suíte", "comando"]
findings = []

print("Buscando mapeamento de suítes nos logs...")

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
                
                # Vamos ler os arquivos inteiros se forem pequenos, ou últimos 5MB se forem grandes
                read_size = min(size, 5 * 1024 * 1024)
                with open(file_path, "rb") as f:
                    if size > read_size:
                        f.seek(size - read_size)
                    content_bytes = f.read()
                
                content = content_bytes.decode("cp1252", errors="ignore")
                lines = content.splitlines()
                
                for line in lines:
                    line_lower = line.lower()
                    # Filtra linhas irrelevantes
                    if ";sql;" in line_lower:
                        continue
                    if "thread container" in line_lower and "desregistrada" in line_lower:
                        continue
                    if "thread container" in line_lower and "registrada" in line_lower:
                        continue
                    
                    # Se contiver termos de mapeamento
                    if any(kw in line_lower for kw in ["porta", "peso", "defini", "mapea"]):
                        if any(kw in line_lower for kw in ["suíte", "suite"]):
                            findings.append(f"[{file}] {line.strip()}")
                            
            except Exception as e:
                pass

with open(output_file, "w", encoding="utf-8") as f:
    f.write("\n".join(findings))

print(f"Varredura concluída! {len(findings)} linhas de mapeamento salvas em dicas_mapeamento.txt.")
