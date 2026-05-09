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
    rows = cur.fetchall()

    cur.execute("SELECT COUNT(*) FROM tabela_coleta_componente;")
    total_comp = cur.fetchone()[0]

    cur.execute("SELECT COUNT(*) FROM tabela_coleta;")
    total_col = cur.fetchone()[0]

    with open('scripts/estados_atual.txt', 'w', encoding='utf-8') as f:
        f.write(f"tabela_coleta: {total_col}\n")
        f.write(f"tabela_coleta_componente: {total_comp}\n")
        f.write(f"Total: {total_col + total_comp}\n\n")
        for r in rows:
            f.write(f"  {r[0]}: {r[1]}\n")

    print("Salvo em scripts/estados_atual.txt")
    cur.close()
    conn.close()

if __name__ == '__main__':
    main()
