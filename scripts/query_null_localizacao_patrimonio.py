"""
query_null_localizacao_patrimonio.py
Faz JOIN tabela_coleta (NULL) → tabela_patrimonio → tabela_sala
para descobrir a sala cadastrada dos 747 itens sem localizacao_atual.
"""
import sys, psycopg2
if hasattr(sys.stdout, 'reconfigure'): sys.stdout.reconfigure(encoding='utf-8')

conn = psycopg2.connect(host='localhost', database='sispatrimonio',
                        user='postgres', password='Romulo@2020', port=5432)
cur = conn.cursor()
SEP = "-" * 70

# PK de tabela_sala
cur.execute("""
    SELECT column_name FROM information_schema.table_constraints tc
    JOIN information_schema.key_column_usage kcu
      ON kcu.constraint_name = tc.constraint_name
    WHERE tc.table_name = 'tabela_sala' AND tc.constraint_type = 'PRIMARY KEY';
""")
pk_sala = cur.fetchone()[0]
print(f"PK tabela_sala: {pk_sala}")

# Colunas tabela_sala
cur.execute("SELECT column_name FROM information_schema.columns WHERE table_name='tabela_sala' ORDER BY ordinal_position;")
sala_cols = [r[0] for r in cur.fetchall()]
print(f"tabela_sala cols: {sala_cols}")
print()

# Campo de descrição/nome da sala
desc_col = next((c for c in sala_cols if 'descr' in c.lower() or 'nome' in c.lower()), sala_cols[1])
print(f"Coluna de nome da sala: {desc_col}")
print()

print(SEP)
print("AMOSTRA — 20 itens com localizacao_atual=NULL via tabela_patrimonio+tabela_sala")
print(SEP)

cur.execute(f"""
    SELECT
        tp.numero                           AS num_patrimonio,
        tp.descricao_resumida,
        ts.{desc_col}                       AS sala_cadastro_patrimonio,
        tp.id_sala,
        tc.localizacao_encontrada,
        tc.estado_encontrado,
        tc.data_coleta::date
    FROM tabela_coleta tc
    JOIN tabela_patrimonio tp ON tp.id = tc.id_patrimonio
    LEFT JOIN tabela_sala ts ON ts.{pk_sala} = tp.id_sala
    WHERE tc.localizacao_atual IS NULL
    ORDER BY ts.{desc_col}, tc.estado_encontrado
    LIMIT 20;
""")
rows = cur.fetchall()
col_names = [d[0] for d in cur.description]
print("  " + " | ".join(f"{c[:22]:<22}" for c in col_names))
print("  " + "-" * (25 * len(col_names)))
for r in rows:
    print("  " + " | ".join(f"{str(v or '')[:22]:<22}" for v in r))

print()
print(SEP)
print("RESUMO — distribuição por sala cadastrada no patrimônio")
print(SEP)

cur.execute(f"""
    SELECT
        COALESCE(ts.{desc_col}, 'SEM SALA NO CADASTRO') AS sala_cadastro,
        COUNT(*)                                           AS qtd,
        SUM(CASE WHEN UPPER(TRIM(tc.estado_encontrado)) IN
            ('IRRECUPERAVEL','IRRECUPERÁVEL') THEN 1 ELSE 0 END) AS irrec,
        SUM(CASE WHEN UPPER(TRIM(tc.estado_encontrado)) = 'BOM'
            THEN 1 ELSE 0 END)                             AS bom,
        COUNT(DISTINCT tc.localizacao_encontrada)          AS destinos
    FROM tabela_coleta tc
    JOIN tabela_patrimonio tp ON tp.id = tc.id_patrimonio
    LEFT JOIN tabela_sala ts ON ts.{pk_sala} = tp.id_sala
    WHERE tc.localizacao_atual IS NULL
    GROUP BY 1
    ORDER BY 2 DESC;
""")
rows = cur.fetchall()
print(f"  {'Sala (cadastro patrimonial)':<50} {'Qtd':>5} {'Irrec':>6} {'Bom':>5} {'Destinos':>9}")
print(f"  {'-'*50} {'-'*5} {'-'*6} {'-'*5} {'-'*9}")
total = 0
for r in rows:
    total += r[1]
    print(f"  {str(r[0] or '?'):<50} {r[1]:>5} {r[2]:>6} {r[3]:>5} {r[4]:>9}")
print(f"  {'TOTAL':<50} {total:>5}")

print()
print(SEP)
print("DESTINOS — para onde foram os 747 itens (localizacao_encontrada)")
print(SEP)
cur.execute(f"""
    SELECT tc.localizacao_encontrada,
           COUNT(*) AS qtd,
           SUM(CASE WHEN UPPER(TRIM(tc.estado_encontrado)) IN
               ('IRRECUPERAVEL','IRRECUPERÁVEL') THEN 1 ELSE 0 END) AS irrec
    FROM tabela_coleta tc
    JOIN tabela_patrimonio tp ON tp.id = tc.id_patrimonio
    LEFT JOIN tabela_sala ts ON ts.{pk_sala} = tp.id_sala
    WHERE tc.localizacao_atual IS NULL
    GROUP BY 1
    ORDER BY 2 DESC;
""")
for r in cur.fetchall():
    print(f"  '{r[0]}'  qtd:{r[1]:4d}  irrec:{r[2]:4d}")

cur.close()
conn.close()
