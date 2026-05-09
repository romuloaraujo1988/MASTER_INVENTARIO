"""
query_schema_sala.py
Investiga o schema atual de tabela_sala e o impacto da remoção do campo ID
nas divergências — especialmente para o Auditório.
"""
import sys
import psycopg2

# Forçar UTF-8 no stdout para evitar UnicodeEncodeError no Windows
if hasattr(sys.stdout, 'reconfigure'):
    sys.stdout.reconfigure(encoding='utf-8')

CONN = dict(
    host='localhost',
    database='sispatrimonio',
    user='postgres',
    password='Romulo@2020',
    port=5432,
)
SEP = "-" * 70


def run(cur, sql):
    cur.execute(sql)
    return cur.fetchall()


def main():
    conn = psycopg2.connect(**CONN)
    cur = conn.cursor()

    lines = []

    def pr(*args):
        line = " ".join(str(a) for a in args)
        print(line)
        lines.append(line)

    # ------------------------------------------------------------------
    # 1. Schema atual de tabela_sala
    # ------------------------------------------------------------------
    pr(SEP)
    pr("1. SCHEMA ATUAL — tabela_sala (todas as colunas)")
    pr(SEP)
    rows = run(cur, """
        SELECT column_name, data_type, is_nullable
        FROM information_schema.columns
        WHERE table_name = 'tabela_sala'
        ORDER BY ordinal_position;
    """)
    for r in rows:
        pr(f"  {r[0]:30s} | tipo: {r[1]:20s} | nullable: {r[2]}")

    # ------------------------------------------------------------------
    # 2. Amostra dos dados de tabela_sala
    # ------------------------------------------------------------------
    pr()
    pr(SEP)
    pr("2. AMOSTRA — primeiros 10 registros de tabela_sala")
    pr(SEP)
    rows = run(cur, "SELECT * FROM tabela_sala LIMIT 10;")
    for r in rows:
        pr(f"  {r}")

    # ------------------------------------------------------------------
    # 3. Como a divergência é calculada — lógica atual
    # ------------------------------------------------------------------
    pr()
    pr(SEP)
    pr("3. LÓGICA DE DIVERGÊNCIA — como o campo 'divergencia' é gerado")
    pr(SEP)
    # Verificar se divergencia é coluna existente e seu tipo
    rows = run(cur, """
        SELECT column_name, data_type
        FROM information_schema.columns
        WHERE table_name = 'tabela_coleta'
          AND column_name IN ('divergencia', 'id_sala', 'localizacao_atual',
                              'localizacao_encontrada', 'id_sala_encontrada')
        ORDER BY ordinal_position;
    """)
    pr("  Colunas relevantes em tabela_coleta:")
    for r in rows:
        pr(f"    {r[0]:35s} | {r[1]}")

    # ------------------------------------------------------------------
    # 4. Conferir divergência manualmente pela comparação de salas
    #    (sem depender do campo divergencia pré-calculado)
    # ------------------------------------------------------------------
    pr()
    pr(SEP)
    pr("4. DIVERGÊNCIA RECALCULADA — comparando localizacao_atual vs localizacao_encontrada")
    pr(SEP)
    rows = run(cur, """
        SELECT
            localizacao_atual                                           AS sala_cadastrada,
            COUNT(*)                                                    AS total,
            SUM(CASE
                WHEN LOWER(TRIM(localizacao_atual)) <>
                     LOWER(TRIM(localizacao_encontrada))
                THEN 1 ELSE 0
            END)                                                        AS div_recalc,
            SUM(CASE WHEN divergencia THEN 1 ELSE 0 END)               AS div_flag,
            ROUND(SUM(CASE
                WHEN LOWER(TRIM(localizacao_atual)) <>
                     LOWER(TRIM(localizacao_encontrada))
                THEN 1 ELSE 0
            END)*100.0 / NULLIF(COUNT(*),0), 2)                        AS taxa_recalc_pct
        FROM tabela_coleta
        WHERE LOWER(localizacao_atual) LIKE '%%audit%%'
           OR LOWER(localizacao_atual) LIKE '%%mezanino%%'
        GROUP BY 1
        ORDER BY total DESC;
    """)
    pr("  Auditório + Mezanino:")
    for r in rows:
        diff = int(r[2] or 0) - int(r[3] or 0)
        pr(f"  Sala: '{r[0]}'")
        pr(f"    Total coletas  : {r[1]}")
        pr(f"    Div recalculada: {r[2]}  ({r[4]}%)")
        pr(f"    Div flag (campo): {r[3]}")
        pr(f"    Diferenca      : {diff:+d}  {'DIVERGEM!' if diff != 0 else 'OK (campo correto)'}")

    # ------------------------------------------------------------------
    # 5. Total geral de divergências (campo flag vs recalculado)
    # ------------------------------------------------------------------
    pr()
    pr(SEP)
    pr("5. TOTAIS GERAIS — flag vs recalculado")
    pr(SEP)
    rows = run(cur, """
        SELECT
            COUNT(*)                                                    AS total_coletas,
            SUM(CASE WHEN divergencia THEN 1 ELSE 0 END)               AS div_flag,
            SUM(CASE
                WHEN LOWER(TRIM(localizacao_atual)) <>
                     LOWER(TRIM(localizacao_encontrada))
                THEN 1 ELSE 0
            END)                                                        AS div_recalc
        FROM tabela_coleta;
    """)
    r = rows[0]
    pr(f"  Total coletas     : {r[0]}")
    pr(f"  Div (flag campo)  : {r[1]}  ({round(r[1]*100/r[0],2)}%)")
    pr(f"  Div (recalculada) : {r[2]}  ({round(r[2]*100/r[0],2)}%)")
    diff = int(r[1] or 0) - int(r[2] or 0)
    pr(f"  Diferenca total   : {diff:+d}  {'campos divergem do recalculo' if diff != 0 else 'consistente'}")

    # ------------------------------------------------------------------
    # 6. Top 10 salas com MAIOR discrepância entre flag e recalculo
    # ------------------------------------------------------------------
    pr()
    pr(SEP)
    pr("6. TOP 10 SALAS — maior discrepância entre flag e recálculo")
    pr(SEP)
    rows = run(cur, """
        SELECT
            localizacao_atual,
            COUNT(*) AS total,
            SUM(CASE WHEN divergencia THEN 1 ELSE 0 END) AS div_flag,
            SUM(CASE
                WHEN LOWER(TRIM(localizacao_atual)) <>
                     LOWER(TRIM(localizacao_encontrada))
                THEN 1 ELSE 0
            END) AS div_recalc,
            ABS(
                SUM(CASE WHEN divergencia THEN 1 ELSE 0 END) -
                SUM(CASE
                    WHEN LOWER(TRIM(localizacao_atual)) <>
                         LOWER(TRIM(localizacao_encontrada))
                    THEN 1 ELSE 0
                END)
            ) AS discrepancia
        FROM tabela_coleta
        GROUP BY 1
        HAVING COUNT(*) >= 5
        ORDER BY discrepancia DESC
        LIMIT 10;
    """)
    for i, r in enumerate(rows, 1):
        pr(f"  {i:2}. '{r[0]}'")
        pr(f"      Total: {r[1]} | Flag: {r[2]} | Recalc: {r[3]} | Discrepancia: {int(r[4] or 0):+d}")

    out_path = 'scripts/resultado_schema_sala.txt'
    with open(out_path, 'w', encoding='utf-8') as f:
        f.write("\n".join(lines))
    print(f"\nSalvo em: {out_path}")

    cur.close()
    conn.close()


if __name__ == '__main__':
    main()
