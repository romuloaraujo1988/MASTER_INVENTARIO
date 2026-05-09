"""
query_recalc_divergencia_correta.py
Recalcula divergências com a lógica correta:
- Remove sufixos como '(IFMT - PDL)', '(BLOCO DE LABORATORIOS)', '(PREDIO ANTIGO)' etc.
- Compara apenas os nomes de sala normalizados
- Mostra os números CORRETOS pós-ajuste
- Gera o UPDATE para corrigir o banco se necessário
"""
import sys
import re
import psycopg2

if hasattr(sys.stdout, 'reconfigure'):
    sys.stdout.reconfigure(encoding='utf-8')

CONN = dict(
    host='localhost',
    database='sispatrimonio',
    user='postgres',
    password='Romulo@2020',
    port=5432,
)
SEP = "=" * 70
SEP2 = "-" * 70


def run(cur, sql):
    cur.execute(sql)
    return cur.fetchall()


def main():
    conn = psycopg2.connect(**CONN)
    cur = conn.cursor()

    NORMALIZE_SQL = r"""
        LOWER(TRIM(REGEXP_REPLACE(
            REGEXP_REPLACE({col}, '\s*\(.*?\)\s*$', '', 'g'),
            '\s+', ' ', 'g'
        )))
    """

    norm_atual     = NORMALIZE_SQL.format(col='localizacao_atual')
    norm_encontrada = NORMALIZE_SQL.format(col='localizacao_encontrada')

    print(SEP)
    print("DIVERGENCIAS CORRIGIDAS — logica pos-ajuste do id_sala")
    print("Normalizacao: remove sufixos como '(IFMT - PDL)' antes de comparar")
    print(SEP)

    # ------------------------------------------------------------------
    # 1. Totais gerais — antes e depois
    # ------------------------------------------------------------------
    print()
    print(SEP2)
    print("1. TOTAIS GERAIS — flag atual vs. logica corrigida")
    print(SEP2)
    rows = run(cur, f"""
        SELECT
            COUNT(*)                                                AS total_coletas,
            SUM(CASE WHEN divergencia THEN 1 ELSE 0 END)           AS div_flag_atual,
            SUM(CASE WHEN {norm_atual} <> {norm_encontrada} THEN 1 ELSE 0 END)
                                                                    AS div_corrigida,
            SUM(CASE
                WHEN divergencia AND {norm_atual} = {norm_encontrada}
                THEN 1 ELSE 0 END)                                  AS falsos_positivos,
            SUM(CASE
                WHEN NOT divergencia AND {norm_atual} <> {norm_encontrada}
                THEN 1 ELSE 0 END)                                  AS falsos_negativos
        FROM tabela_coleta;
    """)
    r = rows[0]
    print(f"  Total coletas     : {r[0]}")
    print(f"  Flag atual        : {r[1]}  ({round(r[1]*100/r[0],2)}%)")
    print(f"  Logica corrigida  : {r[2]}  ({round(r[2]*100/r[0],2)}%)")
    print(f"  Falsos positivos  : {r[3]}  (flagados como div, mas sala e a mesma)")
    print(f"  Falsos negativos  : {r[4]}  (nao flagados, mas sala e diferente)")

    # ------------------------------------------------------------------
    # 2. Auditório e Mezanino especificamente
    # ------------------------------------------------------------------
    print()
    print(SEP2)
    print("2. AUDITORIO e MEZANINO — flag atual vs. corrigida")
    print(SEP2)
    rows = run(cur, f"""
        SELECT
            localizacao_atual                                      AS sala,
            COUNT(*)                                               AS coletas,
            SUM(CASE WHEN divergencia THEN 1 ELSE 0 END)           AS div_flag,
            SUM(CASE WHEN {norm_atual} <> {norm_encontrada} THEN 1 ELSE 0 END)
                                                                    AS div_corrigida,
            SUM(CASE
                WHEN divergencia AND {norm_atual} = {norm_encontrada}
                THEN 1 ELSE 0 END)                                  AS falsos_positivos
        FROM tabela_coleta
        WHERE LOWER(localizacao_atual) LIKE '%%audit%%'
           OR LOWER(localizacao_atual) LIKE '%%mezanino%%'
        GROUP BY 1
        ORDER BY coletas DESC;
    """)
    for r in rows:
        print(f"  '{r[0]}'")
        print(f"    Coletas         : {r[1]}")
        print(f"    Flag atual      : {r[2]}  ({round(int(r[2] or 0)*100/r[1],2)}%)")
        print(f"    Logica corrigida: {r[3]}  ({round(int(r[3] or 0)*100/r[1],2)}%)")
        print(f"    Falsos positivos: {r[4]}  (seriam REMOVIDOS pelo UPDATE)")

    # ------------------------------------------------------------------
    # 3. Ranking completo — taxa corrigida (min 10 coletas)
    # ------------------------------------------------------------------
    print()
    print(SEP2)
    print("3. RANKING CORRIGIDO — Top 20 por taxa de divergencia real")
    print(SEP2)
    rows = run(cur, f"""
        SELECT
            localizacao_atual                                      AS sala,
            COUNT(*)                                               AS coletas,
            SUM(CASE WHEN {norm_atual} <> {norm_encontrada} THEN 1 ELSE 0 END)
                                                                    AS div_corrigida,
            ROUND(
                SUM(CASE WHEN {norm_atual} <> {norm_encontrada} THEN 1 ELSE 0 END)
                *100.0/NULLIF(COUNT(*),0), 2
            )                                                       AS taxa_corr_pct
        FROM tabela_coleta
        GROUP BY 1
        HAVING COUNT(*) >= 10
        ORDER BY taxa_corr_pct DESC
        LIMIT 20;
    """)
    print(f"  {'#':>2}  {'Sala':<50} {'Col':>5} {'Div':>5} {'Taxa':>7}")
    print(f"  {'--':>2}  {'-'*50} {'-'*5} {'-'*5} {'-'*7}")
    for i, r in enumerate(rows, 1):
        sala = (r[0] or '?')[:50]
        print(f"  {i:2}.  {sala:<50} {r[1]:>5} {int(r[2] or 0):>5} {r[3]:>6}%")

    # ------------------------------------------------------------------
    # 4. Script UPDATE para corrigir o banco
    # ------------------------------------------------------------------
    print()
    print(SEP2)
    print("4. UPDATE PARA CORRIGIR o campo divergencia no banco")
    print(SEP2)
    update_sql = f"""
-- Recalcula o campo divergencia usando nomes de sala normalizados
-- Remove sufixos entre parenteses antes de comparar
UPDATE tabela_coleta
SET divergencia = (
    {norm_atual} <> {norm_encontrada}
);
"""
    print("  SQL gerado:")
    print(update_sql)

    rows = run(cur, f"""
        SELECT COUNT(*)
        FROM tabela_coleta
        WHERE divergencia <> ({norm_atual} <> {norm_encontrada});
    """)
    print(f"  Registros que seriam alterados: {rows[0][0]}")
    print()
    print("  Para aplicar: copie o SQL acima e execute no psql ou pgAdmin.")
    print("  OU: responda 'SIM' para executar agora via este script.")

    conn.close()


if __name__ == '__main__':
    main()
