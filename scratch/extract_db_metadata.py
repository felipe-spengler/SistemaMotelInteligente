import re

db_path = r"E:\Infobac\InfoMotel\Base\BASE.FDB"
output_file = r"d:\Área de Trabalho\MotelInteligente\outroSistema\dicas_db.txt"

print(f"Lendo base de dados {db_path}...")

with open(db_path, "rb") as f:
    data = f.read()

# Procura strings ASCII de tamanho >= 3
ascii_re = re.compile(b"[A-Z0-9_$#]{3,150}")
strings = ascii_re.findall(data)

decoded_strings = set()
for s in strings:
    try:
        decoded_strings.add(s.decode('ascii'))
    except Exception:
        pass

# Filtra colunas e tabelas importantes
interesting = sorted([s for s in decoded_strings if any(x in s for x in ['T00', 'IND_', 'AUT_', 'SIT_', 'COD_', 'VAL_', 'PORT', 'RELE', 'RELAY'])])

with open(output_file, "w", encoding="utf-8") as f:
    f.write("\n".join(interesting))

print(f"Extração concluída! {len(interesting)} strings salvas em dicas_db.txt.")
