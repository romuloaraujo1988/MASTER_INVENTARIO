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
            DATE(data_coleta AT TIME ZONE 'UTC' AT TIME ZONE 'America/Cuiaba') AS dia,
            COUNT(*) AS total_coletas
        FROM tabela_coleta
        GROUP BY 1
        ORDER BY 1;
    """)
    rows = cur.fetchall()

    with open('scripts/resultado_dias.txt', 'w', encoding='utf-8') as f:
        f.write(f"Total de dias com coletas: {len(rows)}\n")
        if rows:
            f.write(f"Primeiro dia: {rows[0][0]}\n")
            f.write(f"Ultimo dia: {rows[-1][0]}\n\n")
        for r in rows:
            dia = r[0]
            # Dia da semana em portugues
            dias_semana = ['Segunda','Terca','Quarta','Quinta','Sexta','Sabado','Domingo']
            ds = dias_semana[dia.weekday()]
            uteis = dia.weekday() < 5
            f.write(f"  {dia} ({ds}) - {r[1]} coletas {'[UTIL]' if uteis else '[FIM DE SEMANA]'}\n")

        # Contar dias uteis
        dias_uteis = sum(1 for r in rows if r[0].weekday() < 5)
        f.write(f"\nDias uteis com coletas: {dias_uteis}\n")
        f.write(f"Dias de fim de semana com coletas: {len(rows) - dias_uteis}\n")

    print("Salvo em scripts/resultado_dias.txt")
    cur.close()
    conn.close()

if __name__ == '__main__':
    main()
