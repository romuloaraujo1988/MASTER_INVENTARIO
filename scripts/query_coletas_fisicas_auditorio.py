"""
query_coletas_fisicas_auditorio.py
Distingue claramente:
  - Itens CADASTRADOS no auditório/mezanino (localizacao_atual)
  - Itens FISICAMENTE COLETADOS no auditório/mezanino (localizacao_encontrada)
"""
import sys
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

    def pr(line=""):
        print(line)

    # ------------------------------------------------------------------
    # 1. Itens CADASTRADOS no Mezanino (localizacao_atual)
    #    Quantos patrimônios têm o Mezanino como sala de origem no cadastro
    # ------------------------------------------------------------------
    pr(SEP)
    pr("1. CADASTRADOS no Mezanino (localizacao_atual = mezanino)")
    pr("   Esses sao os patrimonios que o sistema registra como sendo do Mezanino")
    pr(SEP)
    rows = run(cur, """
        SELECT
            localizacao_atual,
            COUNT(*)                                            AS qtd_cadastrada,
            SUM(CASE WHEN divergencia THEN 1 ELSE 0 END)       AS div,
            ROUND(SUM(CASE WHEN divergencia THEN 1 ELSE 0 END)
                  *100.0/NULLIF(COUNT(*),0),2)                  AS taxa_pct
        FROM tabela_coleta
        WHERE LOWER(localizacao_atual) LIKE '%%mezanino%%'
        GROUP BY 1;
    """)
    for r in rows:
        pr(f"  Cadastrados em: '{r[0]}'")
        pr(f"    Patrimonios registrados: {r[1]}")
        pr(f"    Destes, com divergencia: {r[2]} ({r[3]}%)")
        pr(f"    Encontrados NO LUGAR CERTO: {r[1]-r[2]}")

    # ------------------------------------------------------------------
    # 2. Coletas FÍSICAS no Mezanino (localizacao_encontrada)
    #    Quantos itens foram FISICAMENTE encontrados/coletados no Mezanino
    # ------------------------------------------------------------------
    pr()
    pr(SEP)
    pr("2. FISICAMENTE COLETADOS no Mezanino (localizacao_encontrada = mezanino)")
    pr("   Esses sao os itens que o coletor ENCONTROU FISICAMENTE no Mezanino")
    pr(SEP)
    rows = run(cur, """
        SELECT
            localizacao_encontrada,
            COUNT(*)                                            AS qtd_encontrada,
            SUM(CASE WHEN divergencia THEN 1 ELSE 0 END)       AS div,
            ROUND(SUM(CASE WHEN divergencia THEN 1 ELSE 0 END)
                  *100.0/NULLIF(COUNT(*),0),2)                  AS taxa_pct
        FROM tabela_coleta
        WHERE LOWER(localizacao_encontrada) LIKE '%%mezanino%%'
        GROUP BY 1;
    """)
    for r in rows:
        pr(f"  Encontrados em: '{r[0]}'")
        pr(f"    Total fisicamente coletados: {r[1]}")
        pr(f"    Com divergencia (de outras salas): {r[2]} ({r[3]}%)")
        pr(f"    Estavam cadastrados corretamente: {r[1]-r[2]}")

    # ------------------------------------------------------------------
    # 3. Origem dos itens encontrados fisicamente no Mezanino
    # ------------------------------------------------------------------
    pr()
    pr(SEP)
    pr("3. DE ONDE VIERAM os itens fisicamente coletados no Mezanino?")
    pr(SEP)
    rows = run(cur, """
        SELECT
            localizacao_atual                                   AS sala_cadastro,
            COUNT(*)                                            AS qtd,
            SUM(CASE WHEN divergencia THEN 1 ELSE 0 END)       AS div
        FROM tabela_coleta
        WHERE LOWER(localizacao_encontrada) LIKE '%%mezanino%%'
        GROUP BY 1
        ORDER BY qtd DESC;
    """)
    for r in rows:
        status = "divergencia" if r[2] and r[2] > 0 else "correto (cadastrado aqui mesmo)"
        pr(f"  '{r[0]}' -> {r[1]} itens [{status}]")

    # ------------------------------------------------------------------
    # 4. Idem para o AUDITÓRIO (mesmo esquema)
    # ------------------------------------------------------------------
    pr()
    pr(SEP)
    pr("4. AUDITORIO — CADASTRADOS vs FISICAMENTE COLETADOS")
    pr(SEP)

    rows = run(cur, """
        SELECT
            localizacao_atual,
            COUNT(*)                                            AS cadastrados,
            SUM(CASE WHEN divergencia THEN 1 ELSE 0 END)       AS div,
            ROUND(SUM(CASE WHEN divergencia THEN 1 ELSE 0 END)
                  *100.0/NULLIF(COUNT(*),0),2)                  AS taxa_pct
        FROM tabela_coleta
        WHERE LOWER(localizacao_atual) LIKE '%%audit%%'
          AND LOWER(localizacao_atual) NOT LIKE '%%mezanino%%'
        GROUP BY 1;
    """)
    pr("a) Patrimonios CADASTRADOS no Auditorio:")
    for r in rows:
        pr(f"   '{r[0]}': {r[1]} cadastrados | {r[2]} divergencias ({r[3]}%)")
        pr(f"   Encontrados no lugar correto: {r[1]-r[2]}")

    pr()
    rows = run(cur, """
        SELECT
            localizacao_encontrada,
            COUNT(*)                                            AS encontrados,
            SUM(CASE WHEN divergencia THEN 1 ELSE 0 END)       AS div
        FROM tabela_coleta
        WHERE LOWER(localizacao_encontrada) LIKE '%%audit%%'
          AND LOWER(localizacao_encontrada) NOT LIKE '%%mezanino%%'
        GROUP BY 1
        ORDER BY encontrados DESC;
    """)
    total_fis = sum(r[1] for r in rows)
    total_div = sum(r[2] for r in rows if r[2])
    pr(f"b) Itens FISICAMENTE coletados no Auditorio: {total_fis}")
    pr(f"   Destes com divergencia (de outras salas): {total_div}")
    pr(f"   Corretos (cadastrados no proprio auditorio): {total_fis - total_div}")

    cur.close()
    conn.close()


if __name__ == '__main__':
    main()
