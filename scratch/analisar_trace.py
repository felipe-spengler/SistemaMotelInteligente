import os
import sys
import dpkt

etl_txt_path = r"C:\Users\Felipe\Documents\trace5361_extracted\report.txt"
output_log_path = r"C:\Users\Felipe\Documents\log_parser_trace.txt"

print("==================================================")
print("=== PARSER DE LOG DO TRACE DE REDE ===")

# Vamos analisar o arquivo report.txt e extrair as mensagens que possuem strings do Delphi
# como CHANGESUITESUBSITUATION, ASKOPENSUITEGATE, etc., para descobrir seus o opcodes e formatos.

keywords = [
    b"CHANGESUITESUBSITUATION",
    b"SHOWSUITEACCOUNT",
    b"ASKBLOCKSUITE",
    b"ASKOPENSUITEGATE",
    b"ASKCLOSESUITEGATE"
]

if not os.path.exists(etl_txt_path):
    print(f"Erro: Arquivo do trace convertido nao encontrado em: {etl_txt_path}")
    sys.exit(1)

print(f"Lendo trace convertido de: {etl_txt_path}")
print(f"Gerando relatorio de analise em: {output_log_path}")

found_lines = []
with open(etl_txt_path, "r", encoding="utf-8", errors="ignore") as f:
    for line_num, line in enumerate(f, 1):
        # Procura por palavras-chave do protocolo
        for kw in keywords:
            kw_str = kw.decode('ascii')
            if kw_str in line:
                found_lines.append((line_num, line.strip()))
                break

with open(output_log_path, "w", encoding="utf-8") as out:
    out.write("=== ANALISE DE PALAVRAS-CHAVE NO TRACE ===\n")
    if found_lines:
        print(f"Foram encontradas {len(found_lines)} linhas com comandos no trace!")
        for num, text in found_lines:
            out.write(f"Linha {num}: {text}\n")
            # Mostra na tela também
            print(f"Linha {num}: {text[:120]}...")
    else:
        print("Nenhuma linha contendo os comandos do protocolo foi encontrada no trace convertido.")
        out.write("Nenhum comando mapeado no trace.\n")

print("\n=== CONCLUIDO ===")
