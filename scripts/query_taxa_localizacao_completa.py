"""
query_taxa_localizacao_completa_v2.py
Calcula taxa de localização incluindo coletas compostas (tabela_coleta_componente
via tabela_item_composto.id_patrimonio_principal).
"""
import sys
import psycopg2

if hasattr(sys.stdout, 'reconfigure'):
    sys.stdout.reconfigure(encoding='utf-8')

conn = psycopg2.connect(host='localhost', database='sispatrimonio',
                        user='postgres', password='Romulo@2020', port=5432)
cur = conn.cursor()
SEP = "-" * 70


def q(sql):
    cur.execute(sql)
    return cur.fetchall()


print(SEP)
print("TAXA DE LOCALIZACAO — coleta principal + coletas compostas")
print(SEP)

# Total cadastrado
r = q("SELECT COUNT(*) FROM tabela_patrimonio;")[0]
total_cad = r[0]
print(f"\nTotal patrimonios cadastrados : {total_cad}")

# 1. Coletas principais
r = q("SELECT COUNT(DISTINCT id_patrimonio) FROM tabela_coleta;")[0]
unicos_principal = r[0]
print(f"Unicos coleta principal       : {unicos_principal}  ({round(unicos_principal*100/total_cad,2)}%)")

# 2. Schema de tabela_item_composto
r = q("""SELECT column_name FROM information_schema.columns
         WHERE table_name='tabela_item_composto' ORDER BY ordinal_position;""")
print(f"\ntabela_item_composto cols: {[x[0] for x in r]}")

# 3. Patrimonios alcancados via componente
r = q("""
    SELECT COUNT(DISTINCT tic.id_patrimonio_principal)
    FROM tabela_coleta_componente tcc
    JOIN tabela_item_composto tic ON tic.id = tcc.id_item_composto;
""")[0]
via_componente = r[0]
print(f"Patrimonios via componente    : {via_componente}")

# 4. Exclusivos via componente (nao na coleta principal)
r = q("""
    SELECT COUNT(DISTINCT tic.id_patrimonio_principal)
    FROM tabela_coleta_componente tcc
    JOIN tabela_item_composto tic ON tic.id = tcc.id_item_composto
    WHERE NOT EXISTS (
        SELECT 1 FROM tabela_coleta tc
        WHERE tc.id_patrimonio = tic.id_patrimonio_principal
    );
""")[0]
exclusivos = r[0]
print(f"Exclusivos via componente     : {exclusivos}  (nao estao na coleta principal)")

# 5. Total combinado
r = q("""
    SELECT COUNT(DISTINCT id_patrimonio) FROM (
        SELECT id_patrimonio FROM tabela_coleta
        UNION
        SELECT tic.id_patrimonio_principal AS id_patrimonio
        FROM tabela_coleta_componente tcc
        JOIN tabela_item_composto tic ON tic.id = tcc.id_item_composto
    ) combinado;
""")[0]
total_combinado = r[0]
taxa_combinada = round(total_combinado * 100 / total_cad, 2)

print()
print(SEP)
print("RESULTADO FINAL")
print(SEP)
print(f"  Coleta principal          : {unicos_principal}  ({round(unicos_principal*100/total_cad,2)}%)")
print(f"  + Exclusivos compostos    : +{exclusivos}")
print(f"  = Total combinado         : {total_combinado}  ({taxa_combinada}%)")
print(f"  Nao localizados           : {total_cad - total_combinado}  ({round((total_cad-total_combinado)*100/total_cad,2)}%)")

# 6. Detalhamento dos compostos
print()
print(SEP)
print("STATUS DOS COMPONENTES")
print(SEP)
rows = q("""
    SELECT
        tcc.status_componente,
        COUNT(*) AS qtd,
        COUNT(DISTINCT tcc.id_item_composto) AS itens_distintos
    FROM tabela_coleta_componente tcc
    GROUP BY 1
    ORDER BY 2 DESC;
""")
for r in rows:
    print(f"  {r[0]:20s} | registros: {r[1]:5d} | itens distintos: {r[2]}")

cur.close()
conn.close()
print("\nConcluido.")
