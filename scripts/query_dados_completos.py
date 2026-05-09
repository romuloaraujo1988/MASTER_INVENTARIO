"""
query_dados_completos.py
Extrai TODOS os indicadores necessários para revisar o documento
ANALISE_DADOS_INVENTARIO_2025.md com dados atuais do banco.
Salva resultado em scripts/dados_atuais_completos.txt
"""
import sys
import psycopg2

if hasattr(sys.stdout, 'reconfigure'):
    sys.stdout.reconfigure(encoding='utf-8')

CONN = dict(host='localhost', database='sispatrimonio',
            user='postgres', password='Romulo@2020', port=5432)

SEP  = "=" * 70
SEP2 = "-" * 70

def run(cur, sql):
    cur.execute(sql)
    return cur.fetchall()

lines = []
def pr(line=""):
    print(line)
    lines.append(str(line))

conn = psycopg2.connect(**CONN)
cur  = conn.cursor()

# ============================================================
# 1. TOTAIS GERAIS
# ============================================================
pr(SEP); pr("1. TOTAIS GERAIS"); pr(SEP)

r = run(cur, "SELECT COUNT(*) FROM tabela_patrimonio;")[0]
total_cad = r[0]
pr(f"  Total cadastrados        : {total_cad}")

r = run(cur, "SELECT COUNT(*) FROM tabela_coleta;")[0]
total_col = r[0]
pr(f"  Total coletas principais : {total_col}")

r = run(cur, "SELECT COUNT(*) FROM tabela_coleta_componente;")[0]
pr(f"  Total coletas compostas  : {r[0]}")

r = run(cur, "SELECT COUNT(DISTINCT id_patrimonio) FROM tabela_coleta;")[0]
unicos_principal = r[0]
pr(f"  Unicos coleta principal  : {unicos_principal}")

r = run(cur, """
    SELECT COUNT(DISTINCT id_patrimonio) FROM (
        SELECT id_patrimonio FROM tabela_coleta
        UNION
        SELECT tic.id_patrimonio_principal
        FROM tabela_coleta_componente tcc
        JOIN tabela_item_composto tic ON tic.id = tcc.id_item_composto
    ) c;
""")[0]
unicos_total = r[0]
nao_loc = total_cad - unicos_total
pr(f"  Unicos total (union)     : {unicos_total}  ({round(unicos_total*100/total_cad,2)}%)")
pr(f"  Nao localizados          : {nao_loc}  ({round(nao_loc*100/total_cad,2)}%)")

r = run(cur, "SELECT COUNT(*), SUM(CASE WHEN divergencia THEN 1 ELSE 0 END) FROM tabela_coleta;")[0]
total_div = r[1]
pr(f"  Total divergencias       : {total_div}  ({round(total_div*100/r[0],2)}%)")

r = run(cur, "SELECT COUNT(DISTINCT id_coletor) FROM tabela_coleta;")[0]
pr(f"  Coletores distintos      : {r[0]}")

r = run(cur, "SELECT MIN(data_coleta), MAX(data_coleta) FROM tabela_coleta;")[0]
pr(f"  Periodo                  : {r[0].strftime('%d/%m/%Y')} a {r[1].strftime('%d/%m/%Y')}")

r = run(cur, "SELECT COUNT(DISTINCT data_coleta::date) FROM tabela_coleta;")[0]
pr(f"  Dias com coleta          : {r[0]}")

r = run(cur, "SELECT ROUND(AVG(tempo_coleta_segundos)::numeric, 2) FROM tabela_coleta WHERE tempo_coleta_segundos > 0;")[0]
pr(f"  Tempo medio coleta (s)   : {r[0]}")

r = run(cur, "SELECT COUNT(*) FROM tabela_coleta WHERE sem_etiqueta = TRUE;")[0]
pr(f"  Sem etiqueta             : {r[0]}  ({round(r[0]*100/total_col,2)}%)")

# ============================================================
# 2. ESTADO DE CONSERVAÇÃO
# ============================================================
pr(); pr(SEP); pr("2. ESTADO DE CONSERVACAO"); pr(SEP)
rows = run(cur, """
    SELECT
        CASE
            WHEN UPPER(TRIM(estado_encontrado)) IN ('IRRECUPERAVEL','IRRECUPERÁVEL') THEN 'IRRECUPERÁVEL'
            WHEN UPPER(TRIM(estado_encontrado)) IN ('RECUPERAVEL','RECUPERÁVEL')     THEN 'RECUPERÁVEL'
            WHEN estado_encontrado IS NULL OR TRIM(estado_encontrado)=''             THEN 'N/A'
            ELSE UPPER(TRIM(estado_encontrado))
        END AS estado,
        COUNT(*) AS qtd,
        ROUND(COUNT(*)*100.0/(SELECT COUNT(*) FROM tabela_coleta),2) AS pct
    FROM tabela_coleta
    GROUP BY 1 ORDER BY 2 DESC;
""")
for r in rows:
    pr(f"  {r[0]:20s} : {r[1]:6d}  ({r[2]}%)")

# ============================================================
# 3. TOP 15 SALAS POR VOLUME
# ============================================================
pr(); pr(SEP); pr("3. TOP 15 SALAS POR VOLUME (localizacao_atual)"); pr(SEP)
rows = run(cur, """
    SELECT
        localizacao_atual, COUNT(*) AS coletas,
        ROUND(COUNT(*)*100.0/(SELECT COUNT(*) FROM tabela_coleta),2) AS pct,
        ROUND(AVG(CASE WHEN UPPER(TRIM(estado_encontrado)) IN ('IRRECUPERAVEL','IRRECUPERÁVEL')
                       THEN 1.0 ELSE 0.0 END)*100,2) AS pct_irrec,
        SUM(CASE WHEN divergencia THEN 1 ELSE 0 END) AS div,
        ROUND(SUM(CASE WHEN divergencia THEN 1 ELSE 0 END)*100.0/COUNT(*),2) AS taxa_div
    FROM tabela_coleta
    GROUP BY 1 ORDER BY 2 DESC LIMIT 15;
""")
pr(f"  {'Sala':<55} {'Col':>6} {'%Tot':>6} {'%Irr':>6} {'Div':>5} {'%Div':>6}")
pr(f"  {'-'*55} {'-'*6} {'-'*6} {'-'*6} {'-'*5} {'-'*6}")
for r in rows:
    pr(f"  {(r[0] or '?')[:55]:<55} {r[1]:>6} {r[2]:>5}% {r[3]:>5}% {int(r[4] or 0):>5} {r[5]:>5}%")

# ============================================================
# 4. TOP 20 SALAS POR DIVERGÊNCIA (taxa)
# ============================================================
pr(); pr(SEP); pr("4. TOP 20 SALAS — taxa de divergencia (min 10 coletas)"); pr(SEP)
rows = run(cur, """
    SELECT localizacao_atual, COUNT(*) AS col,
           SUM(CASE WHEN divergencia THEN 1 ELSE 0 END) AS div,
           ROUND(SUM(CASE WHEN divergencia THEN 1 ELSE 0 END)*100.0/COUNT(*),2) AS taxa,
           ROUND(SUM(CASE WHEN divergencia THEN 1 ELSE 0 END)*100.0/
                 (SELECT SUM(CASE WHEN divergencia THEN 1 ELSE 0 END) FROM tabela_coleta),2) AS impacto
    FROM tabela_coleta GROUP BY 1 HAVING COUNT(*) >= 10
    ORDER BY div DESC LIMIT 20;
""")
pr(f"  {'#':>2}  {'Sala':<52} {'Col':>5} {'Div':>5} {'Taxa':>7} {'Imp':>6}")
for i, r in enumerate(rows, 1):
    pr(f"  {i:2}.  {(r[0] or '?')[:52]:<52} {r[1]:>5} {int(r[2] or 0):>5} {r[3]:>6}%  {r[4]:>5}%")

# ============================================================
# 5. TOP 15 DESTINOS DAS DIVERGÊNCIAS
# ============================================================
pr(); pr(SEP); pr("5. TOP 15 DESTINOS — onde foram encontrados os itens divergentes"); pr(SEP)
rows = run(cur, """
    SELECT localizacao_encontrada,
           SUM(CASE WHEN divergencia THEN 1 ELSE 0 END) AS div,
           ROUND(SUM(CASE WHEN divergencia THEN 1 ELSE 0 END)*100.0/
                 (SELECT SUM(CASE WHEN divergencia THEN 1 ELSE 0 END) FROM tabela_coleta),2) AS pct,
           COUNT(DISTINCT localizacao_atual) AS origens
    FROM tabela_coleta WHERE divergencia
    GROUP BY 1 ORDER BY 2 DESC LIMIT 15;
""")
for r in rows:
    pr(f"  {(r[0] or '?')[:55]:<55} | div: {int(r[1] or 0):5} ({r[2]}%) | {r[3]} origens")

# ============================================================
# 6. DIVERGÊNCIAS POR ESTADO DE CONSERVAÇÃO
# ============================================================
pr(); pr(SEP); pr("6. DIVERGENCIAS X ESTADO DE CONSERVACAO"); pr(SEP)
rows = run(cur, """
    SELECT
        CASE WHEN UPPER(TRIM(estado_encontrado)) IN ('IRRECUPERAVEL','IRRECUPERÁVEL') THEN 'IRRECUPERÁVEL'
             WHEN UPPER(TRIM(estado_encontrado)) IN ('RECUPERAVEL','RECUPERÁVEL')     THEN 'RECUPERÁVEL'
             WHEN estado_encontrado IS NULL OR TRIM(estado_encontrado)=''             THEN 'N/A'
             ELSE UPPER(TRIM(estado_encontrado)) END AS estado,
        COUNT(*) AS total,
        SUM(CASE WHEN divergencia THEN 1 ELSE 0 END) AS div,
        ROUND(SUM(CASE WHEN divergencia THEN 1 ELSE 0 END)*100.0/COUNT(*),2) AS taxa,
        ROUND(SUM(CASE WHEN divergencia THEN 1 ELSE 0 END)*100.0/
              (SELECT SUM(CASE WHEN divergencia THEN 1 ELSE 0 END) FROM tabela_coleta),2) AS impacto
    FROM tabela_coleta GROUP BY 1 ORDER BY total DESC;
""")
pr(f"  {'Estado':<20} {'Total':>7} {'Div':>6} {'Taxa':>7} {'Impacto':>8}")
for r in rows:
    pr(f"  {r[0]:<20} {r[1]:>7} {int(r[2] or 0):>6} {r[3]:>6}%  {r[4]:>6}%")

# ============================================================
# 7. PERFORMANCE DOS COLETORES
# ============================================================
pr(); pr(SEP); pr("7. PERFORMANCE DOS COLETORES"); pr(SEP)
rows = run(cur, """
    SELECT column_name FROM information_schema.columns
    WHERE table_name = 'tabela_coletor' ORDER BY ordinal_position;
""")
coletor_cols = [r[0] for r in rows]
pr(f"  Colunas tabela_coletor: {coletor_cols}")

# Detectar coluna de nome
nome_col = next((c for c in coletor_cols if 'nome' in c.lower()), coletor_cols[0])
pr(f"  Usando coluna: {nome_col}")

rows = run(cur, f"""
    SELECT tc.{nome_col} AS coletor, COUNT(*) AS coletas,
           SUM(CASE WHEN t.divergencia THEN 1 ELSE 0 END) AS div,
           ROUND(SUM(CASE WHEN t.divergencia THEN 1 ELSE 0 END)*100.0/COUNT(*),2) AS taxa,
           ROUND(COUNT(*)*100.0/(SELECT COUNT(*) FROM tabela_coleta),2) AS participacao
    FROM tabela_coleta t
    JOIN tabela_coletor tc ON tc.id = t.id_coletor
    GROUP BY 1 ORDER BY 2 DESC;
""")
pr(f"  {'Coletor':<35} {'Col':>6} {'Div':>5} {'Taxa':>7} {'Part':>7}")
for r in rows:
    pr(f"  {(r[0] or '?')[:35]:<35} {r[1]:>6} {int(r[2] or 0):>5} {r[3]:>6}%  {r[4]:>5}%")

# ============================================================
# 8. TOP 10 DIAS COM MAIOR TAXA DE DIVERGÊNCIA
# ============================================================
pr(); pr(SEP); pr("8. TOP 10 DIAS — maior taxa de divergencia"); pr(SEP)
rows = run(cur, """
    SELECT data_coleta::date AS dia,
           COUNT(*) AS col,
           SUM(CASE WHEN divergencia THEN 1 ELSE 0 END) AS div,
           ROUND(SUM(CASE WHEN divergencia THEN 1 ELSE 0 END)*100.0/COUNT(*),2) AS taxa
    FROM tabela_coleta
    GROUP BY 1 HAVING COUNT(*) >= 20
    ORDER BY taxa DESC LIMIT 10;
""")
for r in rows:
    pr(f"  {r[0]}  col:{r[1]:5d}  div:{int(r[2] or 0):5d}  taxa:{r[3]}%")

# ============================================================
# 9. SALA DE DESFAZIMENTO
# ============================================================
pr(); pr(SEP); pr("9. SALA DE DESFAZIMENTO"); pr(SEP)
rows = run(cur, """
    SELECT localizacao_atual, COUNT(*) AS col,
           SUM(CASE WHEN UPPER(TRIM(estado_encontrado)) IN ('IRRECUPERAVEL','IRRECUPERÁVEL') THEN 1 ELSE 0 END) AS irrec,
           SUM(CASE WHEN UPPER(TRIM(estado_encontrado)) = 'BOM' THEN 1 ELSE 0 END) AS bom,
           SUM(CASE WHEN UPPER(TRIM(estado_encontrado)) = 'OCIOSO' THEN 1 ELSE 0 END) AS ocioso,
           SUM(CASE WHEN divergencia THEN 1 ELSE 0 END) AS div
    FROM tabela_coleta
    WHERE LOWER(localizacao_atual) LIKE '%%desfaz%%'
    GROUP BY 1 ORDER BY 2 DESC;
""")
for r in rows:
    col = r[1]
    pr(f"  '{r[0]}'")
    pr(f"    Total: {col} | Irrec: {r[2]} ({round(r[2]*100/col,2) if col else 0}%) | "
       f"Bom: {r[3]} ({round(r[3]*100/col,2) if col else 0}%) | "
       f"Ocioso: {r[4]} ({round(r[4]*100/col,2) if col else 0}%) | "
       f"Div: {r[5]} ({round(r[5]*100/col,2) if col else 0}%)")

# ============================================================
# 10. SECRETARIA
# ============================================================
pr(); pr(SEP); pr("10. SECRETARIA"); pr(SEP)
rows = run(cur, """
    SELECT localizacao_atual, COUNT(*) AS col,
           SUM(CASE WHEN divergencia THEN 1 ELSE 0 END) AS div,
           ROUND(SUM(CASE WHEN divergencia THEN 1 ELSE 0 END)*100.0/COUNT(*),2) AS taxa
    FROM tabela_coleta
    WHERE LOWER(localizacao_atual) LIKE '%%secret%%'
    GROUP BY 1 ORDER BY 2 DESC;
""")
for r in rows:
    pr(f"  '{r[0]}' | Col: {r[1]} | Div: {r[2]} ({r[3]}%)")

# ============================================================
# Salvar
# ============================================================
out = 'scripts/dados_atuais_completos.txt'
with open(out, 'w', encoding='utf-8') as f:
    f.write('\n'.join(lines))
pr(); pr(f"Salvo em: {out}")

cur.close()
conn.close()
