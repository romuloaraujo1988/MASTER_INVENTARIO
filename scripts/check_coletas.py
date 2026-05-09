import json
import psycopg2
import sys

def main():
    try:
        with open('configuracao_banco.json', 'r', encoding='utf-8') as f:
            config = json.load(f)['postgresql']
            
        conn = psycopg2.connect(
            host=config['host'],
            database=config['database'],
            user=config['user'],
            password=config['password'],
            port=config['port']
        )
        cur = conn.cursor()
        
        cur.execute("SELECT table_name FROM information_schema.tables WHERE table_schema='public';")
        tables = [t[0] for t in cur.fetchall()]
        print("Tables in public schema:")
        print(", ".join(tables))
        print("-" * 40)
        
        table_to_check = 'coleta' if 'coleta' in tables else ('coletas' if 'coletas' in tables else None)
        
        if table_to_check:
            cur.execute(f"SELECT count(*) FROM {table_to_check};")
            count = cur.fetchone()[0]
            print(f"Total items collected in '{table_to_check}': {count}")
        else:
            print("Could not find generic table 'coleta' or 'coletas'.")
            
        cur.close()
        conn.close()
    except Exception as e:
        print(f"Error connecting to DB: {e}", file=sys.stderr)

if __name__ == '__main__':
    main()
