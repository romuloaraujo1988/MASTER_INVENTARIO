"""
query_divergencia_normalizada.py
Recalcula divergências normalizando os nomes de sala antes de comparar,
removendo sufixos como '(IFMT - PDL)', '(BLOCO DE LABORATORIOS)', etc.
Assim a comparação texto reflete a lógica real do campo divergencia.
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
    # 1. RESUMO GERAL — campo divergencia (flag do banco, correto)
    # ------------------------------------------------------------------
    pr(SEP)
    pr("1. RESUMO GERAL — usando campo divergencia (fonte oficial do sistema)")
    pr(SEP)
    rows = run(cur, """
        SELECT
            COUNT(*)                                                AS total_coletas,
            SUM(CASE WHEN divergencia THEN 1 ELSE 0 END)           AS total_div,
            ROUND(
                SUM(CASE WHEN divergencia THEN 1 ELSE 0 END)*100.0
                / NULLIF(COUNT(*), 0), 2
            )                                                       AS taxa_pct
        FROM tabela_coleta;
    """)
    r = rows[0]
    pr(f"  Total coletas     : {r[0]}")
    pr(f"  Total divergencias: {r[1]}  ({r[2]}%)")
    pr(f"  Sem divergencia   : {r[0]-r[1]}  ({round((r[0]-r[1])*100/r[0],2)}%)")

    # ------------------------------------------------------------------
    # 2. AUDITÓRIO — dados atuais pelo campo flag (normalizado)
    # ------------------------------------------------------------------
    pr()
    pr(SEP)
    pr("2. AUDITORIO — dados atuais pelo campo divergencia (oficial)")
    pr(SEP)
    rows = run(cur, """
        SELECT
            localizacao_atual                                       AS sala,
            COUNT(*)                                               AS coletas,
            SUM(CASE WHEN divergencia THEN 1 ELSE 0 END)           AS div,
            ROUND(
                SUM(CASE WHEN divergencia THEN 1 ELSE 0 END)*100.0
                / NULLIF(COUNT(*), 0), 2
            )                                                       AS taxa_pct
        FROM tabela_coleta
        WHERE LOWER(localizacao_atual) LIKE '%%audit%%'
           OR LOWER(localizacao_atual) LIKE '%%mezanino%%'
        GROUP BY 1
        ORDER BY coletas DESC;
    """)
    total_c = sum(r[1] for r in rows)
    total_d = sum(r[2] for r in rows if r[2])
    for r in rows:
        pr(f"  '{r[0]}' | Coletas: {r[1]} | Div: {r[2]} | Taxa: {r[3]}%")
    pr()
    pr(f"  COMPLEXO TOTAL : {total_c} coletas | {total_d} divergencias ({round(total_d*100/total_c,2) if total_c else 0}%)")

    # ------------------------------------------------------------------
    # 3. RECALCULO NORMALIZADO — remove sufixos para comparar nomes de sala
    # ------------------------------------------------------------------
    pr()
    pr(SEP)
    pr("3. RECALCULO NORMALIZADO — removendo sufixos '(IFMT - PDL)' etc antes de comparar")
    pr(SEP)
    rows = run(cur, r"""
        SELECT
            localizacao_atual                                       AS sala,
            COUNT(*)                                               AS coletas,
            SUM(CASE WHEN divergencia THEN 1 ELSE 0 END)           AS div_flag,
            SUM(CASE
                WHEN LOWER(TRIM(REGEXP_REPLACE(localizacao_atual,
                        '\s*\(.*?\)\s*$', '', 'g')))
                  <> LOWER(TRIM(REGEXP_REPLACE(localizacao_encontrada,
                        '\s*\(.*?\)\s*$', '', 'g')))
                THEN 1 ELSE 0
            END)                                                    AS div_normaliz,
            ROUND(
                SUM(CASE WHEN divergencia THEN 1 ELSE 0 END)*100.0
                / NULLIF(COUNT(*), 0), 2
            )                                                       AS taxa_flag_pct,
            ROUND(SUM(CASE
                WHEN LOWER(TRIM(REGEXP_REPLACE(localizacao_atual,
                        '\s*\(.*?\)\s*$', '', 'g')))
                  <> LOWER(TRIM(REGEXP_REPLACE(localizacao_encontrada,
                        '\s*\(.*?\)\s*$', '', 'g')))
                THEN 1 ELSE 0
            END)*100.0 / NULLIF(COUNT(*), 0), 2)                   AS taxa_norm_pct
        FROM tabela_coleta
        WHERE LOWER(localizacao_atual) LIKE '%%audit%%'
           OR LOWER(localizacao_atual) LIKE '%%mezanino%%'
        GROUP BY 1
        ORDER BY coletas DESC;
    """)
    for r in rows:
        delta = int(r[3] or 0) - int(r[2] or 0)
        pr(f"  '{r[0]}'")
        pr(f"    Coletas : {r[1]}")
        pr(f"    Flag    : {r[2]} ({r[4]}%)")
        pr(f"    Normaliz: {r[3]} ({r[5]}%)")
        pr(f"    Delta   : {delta:+d}  {'<<< sufixo causava falso positivo' if delta < 0 else ('>>> nomes identicos nao resolvem' if delta > 0 else 'consistente')}")

    # ------------------------------------------------------------------
    # 4. TOP 20 SALAS — ranking atualizado pelo campo flag
    # ------------------------------------------------------------------
    pr()
    pr(SEP)
    pr("4. TOP 20 SALAS — ranking por divergencia (campo oficial, min. 10 coletas)")
    pr(SEP)
    rows = run(cur, """
        SELECT
            localizacao_atual                                       AS sala,
            COUNT(*)                                               AS coletas,
            SUM(CASE WHEN divergencia THEN 1 ELSE 0 END)           AS div,
            ROUND(
                SUM(CASE WHEN divergencia THEN 1 ELSE 0 END)*100.0
                / NULLIF(COUNT(*), 0), 2
            )                                                       AS taxa_pct,
            ROUND(
                SUM(CASE WHEN divergencia THEN 1 ELSE 0 END)*100.0
                / (SELECT SUM(CASE WHEN divergencia THEN 1 ELSE 0 END)
                   FROM tabela_coleta), 2
            )                                                       AS impacto_total_pct
        FROM tabela_coleta
        GROUP BY 1
        HAVING COUNT(*) >= 10
        ORDER BY taxa_pct DESC
        LIMIT 20;
    """)
    pr(f"  {'#':>2}  {'Sala':<50} {'Coletas':>7} {'Div':>6} {'Taxa':>7} {'Impacto':>8}")
    pr(f"  {'-'*2}  {'-'*50} {'-'*7} {'-'*6} {'-'*7} {'-'*8}")
    for i, r in enumerate(rows, 1):
        sala = r[0][:50] if r[0] else '?'
        pr(f"  {i:2}.  {sala:<50} {r[1]:>7} {r[2]:>6} {r[3]:>6}%  {r[4]:>6}%")

    out_path = 'scripts/resultado_divergencia_normalizada.txt'
    with open(out_path, 'w', encoding='utf-8') as f:
        f.write("\n".join(lines))
    print(f"\nSalvo em: {out_path}")

    cur.close()
    conn.close()


if __name__ == '__main__':
    main()
