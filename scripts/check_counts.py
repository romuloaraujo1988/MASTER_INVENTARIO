import psycopg2
import sys

def main():
    try:
        conn = psycopg2.connect(
            host='localhost',
            database='sispatrimonio',
            user='postgres',
            password='Romulo@2020',
            port=5432
        )
        cur = conn.cursor()
        
        cur.execute("SELECT count(*) FROM tabela_patrimonio;")
        count_patrimonio = cur.fetchone()[0]
        
        cur.execute("SELECT count(*) FROM tabela_item_composto;")
        count_composto = cur.fetchone()[0]
        
        print(f"Total rows in 'tabela_patrimonio': {count_patrimonio}")
        print(f"Total rows in 'tabela_item_composto': {count_composto}")
        
        cur.close()
        conn.close()
    except Exception as e:
        print(f"Error connecting to DB: {e}", file=sys.stderr)

if __name__ == '__main__':
    main()
