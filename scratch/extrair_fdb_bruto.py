import os
import re

db_path = r"E:\Infobac\InfoMotel\Base\BASE_VENUS_ACREUNA.FDB"
output_path = r"d:\Área de Trabalho\MotelInteligente\outroSistema\mapeamento_bruto.txt"

print("==================================================")
print("=== RASPAGEM BINARIA DE CONFIGURACOES DE AUTOMACAO ===")
print(f"Lendo banco FDB em: {db_path}")

if not os.path.exists(db_path):
    print("Erro: O banco FDB nao existe no caminho especificado do HD externo.")
    sys.exit(1)

# Ler o banco inteiro (172MB eh pequeno para a memoria RAM de PCs modernos)
with open(db_path, "rb") as f:
    data = f.read()

print(f"Tamanho do arquivo carregado: {len(data) / (1024*1024):.2f} MB")

# Vamos procurar por padroes estruturados das tabelas de automacao no binario
# Como nomes de suites (ex: 101, 102, 105, 9, 8) proximos a termos como "LP", "LB", "LD"
# ou strings com o nome da tabela AUTOMACAOCONFIGURACAO ou SUITE
# Vamos salvar as strings legiveis ASCII que contem informacoes uteis
ascii_strings = []
print("Buscando padroes de texto no banco...")

# Extrai strings ASCII de tamanho maior que 6
ascii_regex = re.compile(b"[\\x20-\\x7E\\x80-\\xFF]{6,100}")
matches = ascii_regex.findall(data)

findings = []
for m in matches:
    try:
        s = m.decode("cp1252", errors="ignore").strip()
        # Filtra por termos chave que indicam portas, suites, COM ou enderecos
        s_lower = s.lower()
        if any(kw in s_lower for kw in ["automac", "suite", "suite", "lp000", "lb032", "ld064", "com3", "com4", "wch"]):
            findings.append(s)
    except:
        pass

# Elimina duplicatas mantendo a ordem
unique_findings = []
seen = set()
for f in findings:
    if f not in seen:
        seen.add(f)
        unique_findings.append(f)

with open(output_path, "w", encoding="utf-8") as out:
    out.write("\n".join(unique_findings))

print(f"Raspagem concluida! Encontradas {len(unique_findings)} referencias salvas em: {output_path}")
print("==================================================")
