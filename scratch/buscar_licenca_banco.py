import os
import re

db_path = r"E:\Infobac\InfoMotel\Base\BASE_VENUS_ACREUNA.FDB"
output_path = r"d:\Área de Trabalho\MotelInteligente\outroSistema\licenca_banco.txt"

print("==================================================")
print("=== RASPAGEM DE LICENCAS DO BANCO FDB ===")

if not os.path.exists(db_path):
    print("Erro: O banco FDB nao existe.")
    sys.exit(1)

with open(db_path, "rb") as f:
    # Lemos os primeiros 100MB do banco
    data = f.read(100 * 1024 * 1024)

# Vamos extrair todas as sequencias de numeros com padrao de chave de licenca ou data de validade
# O formato de licenca da Infobac costuma ser de 4 blocos de 4 numeros (Ex: XXXX XXXX XXXX XXXX)
# Ou strings em formato de hash hexadecimal longo
ascii_regex = re.compile(b"[\\x20-\\x7E\\x80-\\xFF]{6,100}")
matches = ascii_regex.findall(data)

licences = []
for m in matches:
    try:
        s = m.decode("cp1252", errors="ignore").strip()
        # Filtra strings que se parecem com chaves, hash de licenca ou ativacao
        if re.search(r"\d{4}\s\d{4}\s\d{4}\s\d{4}", s):
            licences.append(f"Chave Encontrada: {s}")
        elif "licence" in s.lower() or "licenca" in s.lower() or "registro" in s.lower():
            if len(s) < 150:
                licences.append(f"Contexto Licenca: {s}")
    except:
        pass

unique_licences = list(set(licences))

with open(output_path, "w", encoding="utf-8") as out:
    out.write("\n".join(unique_licences))

print(f"Pesquisa concluida! Salvo em: {output_path}")
print(f"Total de chaves/licencas encontradas no banco: {len(unique_licences)}")
print("==================================================")
