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
    
    # Update item 478668 to SALA DE ROBÓTICA JÚNIOR (id_sala = 73)
    print("Updating item 478668 to 'SALA DE ROBÓTICA' (id_sala=73)...")
    cur.execute("UPDATE tabela_patrimonio SET id_sala = 73 WHERE numero = '478668'")
    if cur.rowcount > 0:
        print("Item 478668 updated successfully.")
    else:
        print("Item 478668 not found.")
        
    conn.commit()

except Exception as e:
    print(f"Error: {e}")
finally:
    if 'conn' in locals() and conn is not None:
        conn.close()
