import psycopg2

CONN = dict(
    host='localhost',
    database='sispatrimonio',
    user='postgres',
    password='Romulo@2020',
    port=5432,
)

SEP = "-" * 70

def run(cur, sql, params=None):
    cur.execute(sql, params or ())
    return cur.fetchall()


def main():
    conn = psycopg2.connect(**CONN)
    cur = conn.cursor()

    output_lines = []

    def pr(*args):
        line = " ".join(str(a) for a in args)
        print(line)
        output_lines.append(line)

    # ------------------------------------------------------------------
    # 1. RESUMO GERAL DO AUDITÓRIO (sala cadastrada = AUDITÓRIO)
    # ------------------------------------------------------------------
    pr(SEP)
    pr("1. RESUMO GERAL — coletas onde a sala CADASTRADA é AUDITÓRIO")
    pr(SEP)
    rows = run(cur, """
        SELECT
            localizacao_atual                                            AS sala_cadastrada,
            COUNT(*)                                                     AS total_coletas,
            SUM(CASE WHEN divergencia THEN 1 ELSE 0 END)                AS divergencias,
            ROUND(
                SUM(CASE WHEN divergencia THEN 1 ELSE 0 END)*100.0
                / NULLIF(COUNT(*), 0), 2
            )                                                            AS taxa_pct
        FROM tabela_coleta
        WHERE LOWER(localizacao_atual) LIKE '%%audit%%'
        GROUP BY 1
        ORDER BY total_coletas DESC;
    """)
    for r in rows:
        pr(f"  Sala: '{r[0]}' | Coletas: {r[1]} | Divergências: {r[2]} | Taxa: {r[3]}%")

    # ------------------------------------------------------------------
    # 2. ONDE FORAM ENCONTRADOS os itens cadastrados no Auditório
    # ------------------------------------------------------------------
    pr()
    pr(SEP)
    pr("2. DESTINO — onde foram ENCONTRADOS os itens cadastrados no Auditório")
    pr(SEP)
    rows = run(cur, """
        SELECT
            localizacao_encontrada,
            COUNT(*)                                                     AS qtd,
            SUM(CASE WHEN divergencia THEN 1 ELSE 0 END)                AS div,
            ROUND(
                SUM(CASE WHEN divergencia THEN 1 ELSE 0 END)*100.0
                / NULLIF(COUNT(*), 0), 2
            )                                                            AS taxa_pct
        FROM tabela_coleta
        WHERE LOWER(localizacao_atual) LIKE '%%audit%%'
        GROUP BY 1
        ORDER BY qtd DESC
        LIMIT 20;
    """)
    for r in rows:
        marker = " <- divergencia" if r[2] and r[2] > 0 else ""
        pr(f"  Encontrado em: '{r[0]}' | Qtd: {r[1]} | Div: {r[2]} | Taxa: {r[3]}%{marker}")

    # ------------------------------------------------------------------
    # 3. ORIGENS — quem foi encontrado DENTRO do Auditório (divergências recebidas)
    # ------------------------------------------------------------------
    pr()
    pr(SEP)
    pr("3. ORIGENS — itens de OUTRAS salas encontrados DENTRO do Auditorio")
    pr(SEP)
    rows = run(cur, """
        SELECT
            localizacao_atual                                            AS sala_origem,
            COUNT(*)                                                     AS qtd,
            SUM(CASE WHEN divergencia THEN 1 ELSE 0 END)                AS div
        FROM tabela_coleta
        WHERE LOWER(localizacao_encontrada) LIKE '%%audit%%'
          AND NOT LOWER(localizacao_atual) LIKE '%%audit%%'
        GROUP BY 1
        ORDER BY qtd DESC
        LIMIT 20;
    """)
    for r in rows:
        pr(f"  Origem: '{r[0]}' | Qtd: {r[1]} | Div: {r[2]}")
    if not rows:
        pr("  (nenhuma divergencia de outras salas para o Auditorio)")

    # ------------------------------------------------------------------
    # 4. COMPLEXO AUDITÓRIO (auditório + mezanino) — visão consolidada
    # ------------------------------------------------------------------
    pr()
    pr(SEP)
    pr("4. COMPLEXO AUDITORIO + MEZANINO — coletas encontradas no local")
    pr(SEP)
    rows = run(cur, """
        SELECT
            localizacao_encontrada,
            COUNT(*)                                                     AS total,
            SUM(CASE WHEN divergencia THEN 1 ELSE 0 END)                AS divergencias,
            ROUND(
                SUM(CASE WHEN divergencia THEN 1 ELSE 0 END)*100.0
                / NULLIF(COUNT(*), 0), 2
            )                                                            AS taxa_pct
        FROM tabela_coleta
        WHERE LOWER(localizacao_encontrada) LIKE '%%audit%%'
           OR LOWER(localizacao_encontrada) LIKE '%%mezanino%%'
        GROUP BY 1
        ORDER BY total DESC;
    """)
    total_found = sum(r[1] for r in rows)
    total_div   = sum(r[2] for r in rows if r[2])
    for r in rows:
        pr(f"  Local: '{r[0]}' | Total: {r[1]} | Div: {r[2]} | Taxa: {r[3]}%")
    pr()
    pr(f"  TOTAL no complexo : {total_found} coletas")
    pr(f"  TOTAL divergencias: {total_div} ({round(total_div*100/total_found,2) if total_found else 0}%)")

    # ------------------------------------------------------------------
    # 5. TOP SALAS COM MAIOR TAXA DE DIVERGÊNCIA (ranking geral atualizado)
    # ------------------------------------------------------------------
    pr()
    pr(SEP)
    pr("5. RANKING ATUAL — Top 20 salas por taxa de divergencia (min. 10 coletas)")
    pr(SEP)
    rows = run(cur, """
        SELECT
            localizacao_atual                                            AS sala,
            COUNT(*)                                                     AS coletas,
            SUM(CASE WHEN divergencia THEN 1 ELSE 0 END)                AS div,
            ROUND(
                SUM(CASE WHEN divergencia THEN 1 ELSE 0 END)*100.0
                / NULLIF(COUNT(*), 0), 2
            )                                                            AS taxa_pct
        FROM tabela_coleta
        GROUP BY 1
        HAVING COUNT(*) >= 10
        ORDER BY taxa_pct DESC
        LIMIT 20;
    """)
    for i, r in enumerate(rows, 1):
        pr(f"  {i:2}. '{r[0]}' | Coletas: {r[1]} | Div: {r[2]} | Taxa: {r[3]}%")

    # ------------------------------------------------------------------
    # Salvar resultado
    # ------------------------------------------------------------------
    out_path = 'scripts/resultado_auditorio.txt'
    with open(out_path, 'w', encoding='utf-8') as f:
        f.write("\n".join(output_lines))
    print(f"\nResultado salvo em: {out_path}")

    cur.close()
    conn.close()


if __name__ == '__main__':
    main()
