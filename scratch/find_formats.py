with open(r"E:\Infobac\InfoMotel\InfoAutomacaoDispositivo.exe", "rb") as f:
    data = f.read()

import re
ascii_strings = re.findall(b"[ -~]{3,}", data)
utf16_strings = re.findall(b"(?:[ -~]\\x00){3,}", data)

all_strings = set()
for s in ascii_strings:
    all_strings.add(s.decode('ascii', errors='ignore'))
for s in utf16_strings:
    all_strings.add(s.decode('utf-16le', errors='ignore'))

# Filtra por strings com padrão de comando de automação
# Ex: duas letras maiúsculas seguidas de formato numérico (ex: VN%3.3d)
pattern = re.compile(r'\b[A-Z]{2,}%[0-9\.]*[dD]\b')
matches = []

for s in all_strings:
    s_clean = s.strip()
    if pattern.search(s_clean):
        matches.append(s_clean)

# Vamos também procurar por strings curtas de 3 a 8 caracteres que tenham letras maiúsculas e números,
# que são os comandos reais enviados
short_cmds = []
for s in all_strings:
    s_clean = s.strip()
    if len(s_clean) >= 2 and len(s_clean) <= 10:
        if s_clean.isupper() and any(c.isdigit() for c in s_clean):
            short_cmds.append(s_clean)

print("--- PADRÕES DE COMANDO ENCONTRADOS ---")
for m in sorted(matches):
    print(f"  - {m}")

print("\n--- POSSÍVEIS COMANDOS CURTOS (HEX/ASCII) ENCONTRADOS ---")
for cmd in sorted(list(set(short_cmds)))[:50]:
    print(f"  - {cmd}")
