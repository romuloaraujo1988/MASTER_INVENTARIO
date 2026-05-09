"""
apply_update_divergencia.py
Aplica o UPDATE que recalcula o campo divergencia usando nomes de sala
normalizados (remove sufixos como '(IFMT - PDL)' antes de comparar).
Após o UPDATE, exibe os totais corrigidos.
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

NORMALIZE = r"""
    LOWER(TRIM(REGEXP_REPLACE(
        REGEXP_REPLACE({col}, '\s*\(.*?\)\s*$', '', 'g'),
        '\s+', ' ', 'g'
    )))
"""

norm_a = NORMALIZE.format(col='localizacao_atual')
norm_e = NORMALIZE.format(col='localizacao_encontrada')

UPDATE_SQL = f"""
UPDATE tabela_coleta
SET divergencia = (
    {norm_a} <> {norm_e}
);
"""

SELECT_ANTES = f"""
SELECT
    COUNT(*) AS total,
    SUM(CASE WHEN divergencia THEN 1 ELSE 0 END) AS div_antes
FROM tabela_coleta;
"""

SELECT_DEPOIS = f"""
SELECT
    COUNT(*) AS total,
    SUM(CASE WHEN divergencia THEN 1 ELSE 0 END) AS div_depois,
    ROUND(SUM(CASE WHEN divergencia THEN 1 ELSE 0 END)*100.0/COUNT(*), 2) AS taxa_pct
FROM tabela_coleta;
"""

SELECT_AUDITORIO = f"""
SELECT
    localizacao_atual AS sala,
    COUNT(*) AS coletas,
    SUM(CASE WHEN divergencia THEN 1 ELSE 0 END) AS div,
    ROUND(SUM(CASE WHEN divergencia THEN 1 ELSE 0 END)*100.0/NULLIF(COUNT(*),0), 2) AS taxa_pct
FROM tabela_coleta
WHERE LOWER(localizacao_atual) LIKE '%%audit%%'
   OR LOWER(localizacao_atual) LIKE '%%mezanino%%'
GROUP BY 1
ORDER BY coletas DESC;
"""

SELECT_TOP15 = f"""
SELECT
    localizacao_atual AS sala,
    COUNT(*) AS coletas,
    SUM(CASE WHEN divergencia THEN 1 ELSE 0 END) AS div,
    ROUND(SUM(CASE WHEN divergencia THEN 1 ELSE 0 END)*100.0/NULLIF(COUNT(*),0), 2) AS taxa_pct,
    ROUND(SUM(CASE WHEN divergencia THEN 1 ELSE 0 END)*100.0/
        (SELECT SUM(CASE WHEN divergencia THEN 1 ELSE 0 END) FROM tabela_coleta), 2) AS impacto_pct
FROM tabela_coleta
GROUP BY 1
HAVING COUNT(*) >= 10
ORDER BY div DESC
LIMIT 15;
"""


def main():
    conn = psycopg2.connect(**CONN)
    cur = conn.cursor()

    # -- Antes --
    cur.execute(SELECT_ANTES)
    r = cur.fetchone()
    total, div_antes = r[0], r[1]
    print(SEP)
    print(f"ANTES  | Total: {total} | Divergencias: {div_antes} ({round(div_antes*100/total,2)}%)")

    # -- UPDATE --
    print(SEP)
    print("Aplicando UPDATE...")
    cur.execute(UPDATE_SQL)
    alterados = cur.rowcount
    conn.commit()
    print(f"  UPDATE concluido. Registros alterados: {alterados}")

    # -- Depois: totais gerais --
    cur.execute(SELECT_DEPOIS)
    r = cur.fetchone()
    print(SEP)
    print(f"DEPOIS | Total: {r[0]} | Divergencias: {r[1]} ({r[2]}%)")
    print(f"        Variacao: {r[1] - div_antes:+d} registros")

    # -- Auditório / Mezanino --
    print()
    print(SEP)
    print("AUDITORIO e MEZANINO — dados corrigidos")
    print(SEP)
    cur.execute(SELECT_AUDITORIO)
    rows = cur.fetchall()
    for rr in rows:
        print(f"  '{rr[0]}' | Coletas: {rr[1]} | Div: {rr[2]} | Taxa: {rr[3]}%")

    # -- Top 15 salas por volume de divergências --
    print()
    print(SEP)
    print("TOP 15 SALAS — maior volume de divergencias (corrigido)")
    print(SEP)
    cur.execute(SELECT_TOP15)
    rows = cur.fetchall()
    print(f"  {'#':>2}  {'Sala':<52} {'Col':>5} {'Div':>5} {'Taxa':>7} {'Impacto':>8}")
    print(f"  {'--':>2}  {'-'*52} {'-'*5} {'-'*5} {'-'*7} {'-'*8}")
    for i, rr in enumerate(rows, 1):
        sala = (rr[0] or '?')[:52]
        print(f"  {i:2}.  {sala:<52} {rr[1]:>5} {int(rr[2] or 0):>5} {rr[3]:>6}%  {rr[4]:>6}%")

    cur.close()
    conn.close()
    print()
    print("Concluido. Banco atualizado com sucesso.")


if __name__ == '__main__':
    main()
