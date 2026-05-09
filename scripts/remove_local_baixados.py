import psycopg2

try:
    conn = psycopg2.connect(
        host="localhost",
        database="sispatrimonio",
        user="postgres",
        password="Romulo@2020",
        port=5432
    )
    cur = conn.cursor()
    
    # Check how many items are "Baixado" and have a room
    cur.execute("SELECT COUNT(*) FROM tabela_patrimonio WHERE status = 'Baixado' AND id_sala IS NOT NULL")
    count = cur.fetchone()[0]
    print(f"Total items 'Baixado' currently allocated to a room: {count}")
    
    if count > 0:
        print("Removing room allocation (setting id_sala = NULL) for all 'Baixado' items...")
        cur.execute("UPDATE tabela_patrimonio SET id_sala = NULL WHERE status = 'Baixado'")
        conn.commit()
        print("Update successful.")
    
    # Check if there are any Baixado items left in Secretaria (id_sala = 14) to confirm
    cur.execute("SELECT COUNT(*) FROM tabela_patrimonio WHERE id_sala = 14 AND status = 'Baixado'")
    secretaria_baixados = cur.fetchone()[0]
    print(f"Items 'Baixado' remaining in Secretaria (id_sala=14): {secretaria_baixados}")

except Exception as e:
    print(f"Error: {e}")
finally:
    if 'conn' in locals() and conn is not None:
        conn.close()
