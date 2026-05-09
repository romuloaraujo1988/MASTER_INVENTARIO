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

    # Patrimônios cadastrados na sala Secretaria (id=14)
    # e onde foram encontrados
    cur.execute("""
        SELECT column_name FROM information_schema.columns
        WHERE table_name = 'tabela_patrimonio' ORDER BY ordinal_position LIMIT 10;
    """)
    cols = [r[0] for r in cur.fetchall()]
    print("Colunas tabela_patrimonio (primeiras 10):", cols)

    # Verificar coleta por localizacao_encontrada = Secretaria
    cur.execute("""
        SELECT 
            localizacao_encontrada,
            COUNT(*) AS total,
            SUM(CASE WHEN divergencia THEN 1 ELSE 0 END) AS div,
            ROUND(SUM(CASE WHEN divergencia THEN 1 ELSE 0 END)*100.0/COUNT(*),1) AS taxa
        FROM tabela_coleta
        WHERE LOWER(localizacao_encontrada) LIKE '%secret%'
           OR LOWER(localizacao_atual) LIKE '%secret%'
        GROUP BY 1
        ORDER BY taxa DESC;
    """)
    rows = cur.fetchall()
    print("\nColetas envolvendo 'secretaria':")
    for r in rows:
        print(f"  encontrado em: '{r[0]}' | coletas:{r[1]} | div:{r[2]} | taxa:{r[3]}%")
    if not rows:
        print("  (nenhuma)")

    cur.close()
    conn.close()

if __name__ == '__main__':
    main()
