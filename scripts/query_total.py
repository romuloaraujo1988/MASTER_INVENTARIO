import psycopg2

def main():
    conn = psycopg2.connect(
        host='localhost',
        database='sispatrimonio',
        user='postgres',
        password='Romulo@2020',
        port=5432
    )
    cur = conn.cursor()

    cur.execute("SELECT COUNT(*) FROM tabela_coleta;")
    total_coleta = cur.fetchone()[0]

    cur.execute("SELECT COUNT(*) FROM tabela_coleta_componente;")
    total_componente = cur.fetchone()[0]

    # Estados de conservação unificados (coleta + componente)
    cur.execute("""
        SELECT
            CASE
                WHEN UPPER(TRIM(estado_encontrado)) IN ('IRRECUPERAVEL','IRRECUPERÁVEL') THEN 'IRRECUPERÁVEL'
                WHEN UPPER(TRIM(estado_encontrado)) IN ('RECUPERAVEL','RECUPERÁVEL') THEN 'RECUPERÁVEL'
                WHEN estado_encontrado IS NULL OR TRIM(estado_encontrado) = '' THEN 'N/A'
                ELSE UPPER(TRIM(estado_encontrado))
            END AS estado,
            COUNT(*) AS qtd
        FROM tabela_coleta
        GROUP BY 1
        ORDER BY 2 DESC;
    """)
    rows_coleta = cur.fetchall()

    # Colunas de tabela_coleta_componente
    cur.execute("""
        SELECT column_name FROM information_schema.columns
        WHERE table_name = 'tabela_coleta_componente'
        ORDER BY ordinal_position;
    """)
    cols = [r[0] for r in cur.fetchall()]

    cur.execute("SELECT COUNT(*) FROM tabela_coleta_componente;")
    count_comp = cur.fetchone()[0]

    with open('scripts/resultado_total.txt', 'w', encoding='utf-8') as f:
        f.write(f"tabela_coleta: {total_coleta}\n")
        f.write(f"tabela_coleta_componente: {total_componente}\n")
        f.write(f"Total combinado: {total_coleta + total_componente}\n\n")
        f.write(f"Colunas tabela_coleta_componente: {cols}\n\n")
        f.write("Estados em tabela_coleta:\n")
        for r in rows_coleta:
            f.write(f"  {r[0]}: {r[1]}\n")

    print("Salvo em scripts/resultado_total.txt")
    cur.close()
    conn.close()

if __name__ == '__main__':
    main()
