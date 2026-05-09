import codecs

with codecs.open(r'c:\Users\Romulo\Documents\PROJETOS\MASTER_INVENTARIO\stats_final.txt', 'r', encoding='utf-8') as f:
    lines = f.readlines()

for line in lines:
    print(line.rstrip())
