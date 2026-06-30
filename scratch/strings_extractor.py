import re

exe_path = r"E:\Infobac\InfoMotel\InfoAutomacaoDispositivo.exe"
output_file = r"d:\Área de Trabalho\MotelInteligente\outroSistema\dicas.txt"

print("Extraindo strings do executável de automação...")

def extract_strings(file_path):
    with open(file_path, "rb") as f:
        data = f.read()
    
    # Procura strings ASCII de tamanho >= 4
    ascii_re = re.compile(b"[ -~]{4,}")
    ascii_strings = [s.decode('ascii', errors='ignore') for s in ascii_re.findall(data)]
    
    # Procura strings UTF-16 de tamanho >= 4
    utf16_re = re.compile(b"(?:[ -~]\\x00){4,}")
    utf16_strings = [s.decode('utf-16le', errors='ignore') for s in utf16_re.findall(data)]
    
    return ascii_strings + utf16_strings

all_strings = extract_strings(exe_path)

# Filtra strings interessantes para a automação e banco de dados
interesting_patterns = [
    r"(?i)com\d+", r"(?i)baud", r"(?i)port", r"(?i)rele", r"(?i)relay", r"(?i)placa",
    r"(?i)select", r"(?i)update", r"(?i)insert", r"(?i)delete", r"(?i)from", r"(?i)where",
    r"T00\d", r"T0\d\d", r"T\d\d\d", r"AUT_", r"IND_", r"SIT_", r"COD_", r"DSL_",
    r"tabela", r"dispositivo", r"porta", r"serial", r"abrir", r"fechar"
]

compiled_patterns = [re.compile(p) for p in interesting_patterns]
filtered_strings = set()

for s in all_strings:
    s_clean = s.strip()
    if not s_clean:
        continue
    for pattern in compiled_patterns:
        if pattern.search(s_clean):
            # Limita tamanho para não poluir
            if len(s_clean) < 150:
                filtered_strings.add(s_clean)
            break

# Ordena e escreve no dicas.txt
sorted_strings = sorted(list(filtered_strings))

divider = "\n\n=============================================================================\nSTRINGS EXTRAÍDAS DO EXECUTÁVEL DE AUTOMAÇÃO (DELPHI):\n=============================================================================\n"

try:
    with open(output_file, "r", encoding="utf-8") as f:
        existing_content = f.read()
except Exception:
    existing_content = ""

# Se já houver um bloco de strings anterior, removemos para evitar duplicação
if "STRINGS EXTRAÍDAS DO EXECUTÁVEL DE AUTOMAÇÃO (DELPHI):" in existing_content:
    parts = existing_content.split("STRINGS EXTRAÍDAS DO EXECUTÁVEL DE AUTOMAÇÃO (DELPHI):")
    existing_content = parts[0]

new_content = existing_content + divider + "\n".join(sorted_strings) + "\n\n"

with open(output_file, "w", encoding="utf-8") as f:
    f.write(new_content)

print(f"Extração concluída! {len(sorted_strings)} strings relevantes adicionadas a dicas.txt.")
