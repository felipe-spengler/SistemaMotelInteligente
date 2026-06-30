import os
import sys
import fdb

# O driver fdb exige a DLL cliente do Firebird (fbclient.dll ou gds32.dll)
# Vamos buscar se temos alguma DLL cliente do Firebird instalada nos caminhos comuns
# ou no próprio diretório do Firebird que localizamos na pasta Instaladores.

db_path = r"E:\Infobac\InfoMotel\Base\BASE_VENUS_ACREUNA.FDB"
output_path = r"d:\Área de Trabalho\MotelInteligente\outroSistema\mapeamento_banco.txt"

print("==================================================")
print("=== EXTRAINDO MAPA DE AUTOMACAO DO BANCO FDB ===")

# Vamos tentar conectar usando o driver do Firebird
# O usuário padrão do Firebird é SYSDBA e a senha padrão é masterkey
try:
    conn = fdb.connect(
        database=db_path,
        user="SYSDBA",
        password="masterkey",
        charset="UTF8"
    )
    print("Conectado ao banco Firebird com sucesso!")
    cursor = conn.cursor()
    
    # Lista todas as tabelas para localizarmos as tabelas de automação
    cursor.execute("SELECT rdb$relation_name FROM rdb$relations WHERE rdb$view_source IS NULL AND rdb$system_flag = 0")
    tables = [row[0].strip() for row in cursor.fetchall()]
    
    with open(output_path, "w", encoding="utf-8") as out:
        out.write("=== TABELAS DE AUTOMACAO ENCONTRADAS ===\n")
        print("\nTabelas no banco:")
        for t in tables:
            if "AUTO" in t or "SUITE" in t or "PORT" in t or "CONF" in t:
                print(f" - {t}")
                out.write(f"\n--- TABELA {t} ---\n")
                try:
                    cursor.execute(f"SELECT * FROM {t}")
                    col_names = [desc[0] for desc in cursor.description]
                    out.write(" | ".join(col_names) + "\n")
                    rows = cursor.fetchall()
                    for r in rows:
                        out.write(" | ".join([str(val) for val in r]) + "\n")
                except Exception as ex:
                    out.write(f"Erro ao ler tabela: {ex}\n")
                    
    conn.close()
    print(f"\nMapeamento concluído! Resultados salvos em: {output_path}")
    
except Exception as e:
    print(f"Erro ao conectar ao Firebird FDB: {e}")
    print("Tentando extrair strings brutas de automação direto do arquivo FDB...")
    
    # Backup: se falhar a conexão por falta de DLL, fazemos raspagem de strings no FDB binário
    try:
        with open(db_path, "rb") as f:
            # Lemos blocos em busca de nomes de tabelas e configurações
            content = f.read(50 * 1024 * 1024) # Lemos os primeiros 50MB
            ascii_data = content.decode("ascii", errors="ignore")
            # Procura por padrões comuns de nomes de colunas e dados de automação
            import re
            matches = re.findall(r"([A-Za-z0-9_]{3,30}\s*\|\s*[A-Za-z0-9_]{3,30})", ascii_data)
            print(f"Raspagem de strings concluída! Encontrados {len(matches)} termos.")
    except Exception as ex:
        print(f"Erro na raspagem: {ex}")

print("==================================================")
