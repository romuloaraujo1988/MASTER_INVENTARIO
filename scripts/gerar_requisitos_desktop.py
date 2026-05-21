"""
Gerador do documento DOCX: Requisitos Funcionais — Aplicação Desktop
Fonte: REQUISITOS_FUNCIONAIS_DESKTOP.md
"""

from docx import Document
from docx.shared import Pt, Inches, RGBColor, Cm
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT
from docx.oxml.ns import qn
from docx.oxml import OxmlElement

# ─────────────────────────────────────────────────────────────────────────────
# PALETA
# ─────────────────────────────────────────────────────────────────────────────
AZUL_ESCURO  = RGBColor(0x1A, 0x37, 0x6C)
AZUL_MEDIO   = RGBColor(0x2E, 0x5E, 0xA8)
AZUL_CLARO   = RGBColor(0xD6, 0xE4, 0xF7)
CINZA_CLARO  = RGBColor(0xF5, 0xF5, 0xF5)
VERDE_STATUS = RGBColor(0x1E, 0x7E, 0x34)
BRANCO       = RGBColor(0xFF, 0xFF, 0xFF)
PRETO        = RGBColor(0x00, 0x00, 0x00)
CINZA_TEXTO  = RGBColor(0x44, 0x44, 0x44)

doc = Document()

# ─────────────────────────────────────────────────────────────────────────────
# PÁGINA A4
# ─────────────────────────────────────────────────────────────────────────────
section = doc.sections[0]
section.page_width    = Cm(21.0)
section.page_height   = Cm(29.7)
section.left_margin   = Cm(2.5)
section.right_margin  = Cm(2.5)
section.top_margin    = Cm(2.5)
section.bottom_margin = Cm(2.0)

# ─────────────────────────────────────────────────────────────────────────────
# ESTILOS
# ─────────────────────────────────────────────────────────────────────────────
styles = doc.styles

def _cfg_style(name, size, bold, color, sb, sa):
    try:
        s = styles[name]
    except KeyError:
        s = styles.add_style(name, 1)
    s.font.name = 'Calibri'
    s.font.size = Pt(size)
    s.font.bold = bold
    s.font.color.rgb = color
    s.paragraph_format.space_before = Pt(sb)
    s.paragraph_format.space_after  = Pt(sa)
    return s

normal = styles['Normal']
normal.font.name = 'Calibri'
normal.font.size = Pt(10)
normal.font.color.rgb = CINZA_TEXTO
normal.paragraph_format.space_after = Pt(4)

h1 = styles['Heading 1']
h1.font.name = 'Calibri'; h1.font.size = Pt(16); h1.font.bold = True
h1.font.color.rgb = AZUL_ESCURO
h1.paragraph_format.space_before = Pt(18); h1.paragraph_format.space_after = Pt(6)

h2 = styles['Heading 2']
h2.font.name = 'Calibri'; h2.font.size = Pt(13); h2.font.bold = True
h2.font.color.rgb = AZUL_MEDIO
h2.paragraph_format.space_before = Pt(14); h2.paragraph_format.space_after = Pt(4)

h3 = styles['Heading 3']
h3.font.name = 'Calibri'; h3.font.size = Pt(11); h3.font.bold = True
h3.font.color.rgb = AZUL_MEDIO
h3.paragraph_format.space_before = Pt(10); h3.paragraph_format.space_after = Pt(3)

# ─────────────────────────────────────────────────────────────────────────────
# HELPERS
# ─────────────────────────────────────────────────────────────────────────────

def par(text, bold=False, italic=False, size=None, color=None,
        align=WD_ALIGN_PARAGRAPH.LEFT, sb=0, sa=4):
    p = doc.add_paragraph()
    p.alignment = align
    p.paragraph_format.space_before = Pt(sb)
    p.paragraph_format.space_after  = Pt(sa)
    run = p.add_run(text)
    run.bold = bold; run.italic = italic
    run.font.name = 'Calibri'
    if size:  run.font.size = Pt(size)
    if color: run.font.color.rgb = color
    return p


def bullet(text, bold_prefix=None):
    p = doc.add_paragraph(style='List Bullet')
    p.paragraph_format.left_indent  = Pt(18)
    p.paragraph_format.space_after  = Pt(2)
    p.paragraph_format.space_before = Pt(1)
    if bold_prefix:
        r1 = p.add_run(bold_prefix); r1.bold = True; r1.font.color.rgb = AZUL_ESCURO
        r2 = p.add_run(text);        r2.font.color.rgb = CINZA_TEXTO
    else:
        run = p.add_run(text); run.font.color.rgb = CINZA_TEXTO
    return p


def numbered(text):
    p = doc.add_paragraph(style='List Number')
    p.paragraph_format.left_indent  = Pt(18)
    p.paragraph_format.space_after  = Pt(2)
    p.paragraph_format.space_before = Pt(1)
    run = p.add_run(text); run.font.color.rgb = CINZA_TEXTO
    return p


def set_cell_bg(cell, hex_color: str):
    tc = cell._tc; tcPr = tc.get_or_add_tcPr()
    shd = OxmlElement('w:shd')
    shd.set(qn('w:val'), 'clear'); shd.set(qn('w:color'), 'auto')
    shd.set(qn('w:fill'), hex_color); tcPr.append(shd)


def set_cell_text(cell, text, bold=False, size=9, color=CINZA_TEXTO,
                  align=WD_ALIGN_PARAGRAPH.LEFT, italic=False):
    cell.text = ''
    p = cell.paragraphs[0]
    p.alignment = align
    p.paragraph_format.space_before = Pt(1)
    p.paragraph_format.space_after  = Pt(1)
    run = p.add_run(text)
    run.bold = bold; run.italic = italic
    run.font.size = Pt(size); run.font.color.rgb = color; run.font.name = 'Calibri'


def set_col_widths(table, widths_cm):
    for row in table.rows:
        for i, cell in enumerate(row.cells):
            if i < len(widths_cm):
                cell.width = Cm(widths_cm[i])


def add_hdr(table, headers, bg='1A376C'):
    hdr = table.rows[0]
    for i, h in enumerate(headers):
        if i < len(hdr.cells):
            set_cell_bg(hdr.cells[i], bg)
            set_cell_text(hdr.cells[i], h, bold=True, size=9,
                          color=BRANCO, align=WD_ALIGN_PARAGRAPH.CENTER)


def req_table(headers, rows, col_widths):
    table = doc.add_table(rows=1 + len(rows), cols=len(headers))
    table.style = 'Table Grid'
    table.alignment = WD_TABLE_ALIGNMENT.CENTER
    set_col_widths(table, col_widths)
    add_hdr(table, headers)
    for r_idx, row_data in enumerate(rows):
        row = table.rows[r_idx + 1]
        bg  = 'F5F5F5' if r_idx % 2 == 1 else 'FFFFFF'
        for c_idx, cell_text in enumerate(row_data):
            set_cell_bg(row.cells[c_idx], bg)
            bold  = c_idx == 0
            color = VERDE_STATUS if '✅' in str(cell_text) else CINZA_TEXTO
            if c_idx == 0: color = AZUL_ESCURO
            set_cell_text(row.cells[c_idx], str(cell_text),
                          bold=bold, size=8.5, color=color)
    doc.add_paragraph()
    return table


def simple_table(headers, rows, col_widths):
    table = doc.add_table(rows=1 + len(rows), cols=len(headers))
    table.style = 'Table Grid'
    table.alignment = WD_TABLE_ALIGNMENT.CENTER
    set_col_widths(table, col_widths)
    add_hdr(table, headers, bg='2E5EA8')
    for r_idx, row_data in enumerate(rows):
        row = table.rows[r_idx + 1]
        bg  = 'F5F5F5' if r_idx % 2 == 1 else 'FFFFFF'
        for c_idx, cell_text in enumerate(row_data):
            set_cell_bg(row.cells[c_idx], bg)
            set_cell_text(row.cells[c_idx], str(cell_text),
                          bold=(c_idx == 0), size=9, color=CINZA_TEXTO)
    doc.add_paragraph()
    return table


def criteria_box(items):
    p = doc.add_paragraph()
    p.paragraph_format.space_before = Pt(4)
    p.paragraph_format.space_after  = Pt(2)
    run = p.add_run('Critérios de aceitação:')
    run.bold = True; run.font.color.rgb = AZUL_ESCURO; run.font.size = Pt(10)
    for item in items:
        bullet(item)


def divider():
    p = doc.add_paragraph()
    p.paragraph_format.space_before = Pt(2)
    p.paragraph_format.space_after  = Pt(2)
    run = p.add_run('─' * 90)
    run.font.color.rgb = RGBColor(0xCC, 0xCC, 0xCC); run.font.size = Pt(7)


# ─────────────────────────────────────────────────────────────────────────────
# CAPA
# ─────────────────────────────────────────────────────────────────────────────
doc.add_paragraph()
doc.add_paragraph()

for txt, sz in [
    ('INSTITUTO FEDERAL DE EDUCAÇÃO, CIÊNCIA E TECNOLOGIA DE MATO GROSSO', 11),
    ('IFMT', 11),
]:
    p = doc.add_paragraph(); p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    r = p.add_run(txt); r.bold = True; r.font.size = Pt(sz)
    r.font.color.rgb = AZUL_ESCURO; r.font.name = 'Calibri'

doc.add_paragraph()
doc.add_paragraph()

p = doc.add_paragraph(); p.alignment = WD_ALIGN_PARAGRAPH.CENTER
r = p.add_run('SIHCP — Sistema de Histórico e Coleta Patrimonial')
r.bold = True; r.font.size = Pt(14); r.font.color.rgb = AZUL_MEDIO; r.font.name = 'Calibri'

doc.add_paragraph()

p = doc.add_paragraph(); p.alignment = WD_ALIGN_PARAGRAPH.CENTER
r = p.add_run('ESPECIFICAÇÃO DE REQUISITOS FUNCIONAIS\nAPLICAÇÃO DESKTOP')
r.bold = True; r.font.size = Pt(22); r.font.color.rgb = AZUL_ESCURO; r.font.name = 'Calibri'

doc.add_paragraph()
doc.add_paragraph()

meta = [
    ('Versão do Documento', '2.0.0'),
    ('Versão do Sistema',   '1.2.0'),
    ('Data',                'Maio de 2026'),
    ('Componente',          'Aplicação Desktop (Java 21 + Swing)'),
    ('Ponto de Entrada',    'com.inventario.SistemaInventarioApplication'),
    ('Total de Requisitos', '98 RFD em 17 módulos'),
    ('Status',              '100% Implementado'),
]
tbl = doc.add_table(rows=len(meta), cols=2)
tbl.style = 'Table Grid'; tbl.alignment = WD_TABLE_ALIGNMENT.CENTER
for i, (k, v) in enumerate(meta):
    bg = 'D6E4F7' if i % 2 == 0 else 'FFFFFF'
    set_cell_bg(tbl.rows[i].cells[0], bg); set_cell_bg(tbl.rows[i].cells[1], bg)
    set_cell_text(tbl.rows[i].cells[0], k, bold=True, size=10, color=AZUL_ESCURO)
    set_cell_text(tbl.rows[i].cells[1], v, size=10, color=CINZA_TEXTO)
    tbl.rows[i].cells[0].width = Cm(6); tbl.rows[i].cells[1].width = Cm(10)

doc.add_page_break()

# ─────────────────────────────────────────────────────────────────────────────
# 1. INTRODUÇÃO
# ─────────────────────────────────────────────────────────────────────────────
doc.add_heading('1. Introdução', level=1)

doc.add_heading('1.1 Propósito', level=2)
par('Este documento especifica, em caráter formal e institucional, os requisitos funcionais da '
    'Aplicação Desktop do Sistema de Histórico e Coleta Patrimonial (SIHCP), desenvolvido para o '
    'Instituto Federal de Mato Grosso (IFMT). Tem por finalidade servir como referência técnica '
    'para as equipes de desenvolvimento, manutenção, homologação e auditoria, bem como subsidiar '
    'processos de verificação de conformidade com a legislação federal aplicável à gestão '
    'patrimonial pública.')
par('A aplicação desktop constitui a interface administrativa central do ecossistema SIHCP, '
    'atendendo aos perfis gestores e operacionais responsáveis pela condução integral do processo '
    'de inventário patrimonial, desde o planejamento até a prestação de contas.')

doc.add_heading('1.2 Escopo', level=2)
par('O escopo deste documento compreende exclusivamente a Aplicação Desktop, construída em Java 21 '
    'com interface gráfica Swing e acesso direto ao banco de dados PostgreSQL (com capacidade de '
    'operação em modo offline via SQLite). Estão abrangidos:')
for item in [
    'Gestão administrativa de usuários, patrimônios, salas, setores, campus e responsáveis;',
    'Gestão do ciclo de vida de inventários (planejamento, execução e encerramento);',
    'Registro de coletas patrimoniais diretamente pela aplicação desktop;',
    'Importação de dados externos (SUAP, Excel, CSV);',
    'Exportação de relatórios e dados no padrão SIADS;',
    'Geração e impressão de etiquetas com QR Code;',
    'Dashboard, gráficos e painéis analíticos (Analytics);',
    'Gerenciamento e monitoramento do servidor da API Mobile;',
    'Operação em modo offline com sincronização bidirecional.',
]:
    bullet(item)
par('Não fazem parte deste documento: os requisitos do Servidor Mobile (vide '
    'REQUISITOS_FUNCIONAIS_SERVIDOR_MOBILE.md) nem os do aplicativo Android. Requisitos não '
    'funcionais estão especificados em REQUISITOS_NAO_FUNCIONAIS_DESKTOP.md.')

doc.add_heading('1.3 Definições e Acrônimos', level=2)
simple_table(
    ['Termo / Sigla', 'Definição'],
    [
        ('RFD',      'Requisito Funcional da aplicação Desktop'),
        ('CRUD',     'Operações de Create, Read, Update e Delete'),
        ('QR Code',  'Quick Response Code — código de barras bidimensional'),
        ('SIADS',    'Sistema Integrado de Administração de Serviços (Governo Federal)'),
        ('SUAP',     'Sistema Unificado de Administração Pública do IFMT'),
        ('TCU',      'Tribunal de Contas da União'),
        ('IN',       'Instrução Normativa'),
        ('MVVM',     'Model-View-ViewModel, padrão arquitetural adotado em módulos refatorados'),
        ('JPA / DAO','Java Persistence API / Data Access Object'),
        ('Fat JAR',  'Empacotamento Java contendo todas as dependências em um único arquivo'),
    ],
    [3.5, 12.5]
)

doc.add_heading('1.4 Classificação de Prioridades', level=2)
simple_table(
    ['Prioridade', 'Descrição'],
    [
        ('Essencial',  'Requisito indispensável. A ausência inviabiliza o funcionamento do módulo ou compromete obrigações legais e operacionais do inventário.'),
        ('Importante', 'Requisito necessário para a operação plena. O sistema funciona sem ele, porém com perda significativa de produtividade, de rastreabilidade ou de conformidade.'),
        ('Desejável',  'Requisito que agrega valor operacional, analítico ou ergonômico. Não é crítico para o fluxo principal.'),
    ],
    [3.0, 13.0]
)

doc.add_heading('1.5 Convenções de Identificação', level=2)
par('Cada requisito possui identificador único no formato RFD-XXX, onde:')
bullet('RFD = Requisito Funcional Desktop;')
bullet('XXX = número sequencial de três dígitos, atribuído por ordem histórica de especificação.')
par('Os identificadores são imutáveis e não são reutilizados, preservando a rastreabilidade entre '
    'versões deste documento.')

# ─────────────────────────────────────────────────────────────────────────────
# 2. VISÃO GERAL
# ─────────────────────────────────────────────────────────────────────────────
doc.add_heading('2. Visão Geral da Aplicação Desktop', level=1)
par('A aplicação desktop organiza suas funcionalidades em menus hierárquicos, com visibilidade '
    'condicional conforme o perfil do usuário autenticado. A estrutura principal de navegação '
    'compreende os seguintes grupos de menus:')
for item in [
    ('Administração (ADMIN): ', 'Usuários e Configuração de Banco;'),
    ('Inventário: ', 'Gerenciar Inventários, Salas, Setores, Campus, Responsáveis, Patrimônios, Itens Compostos, Coleta de Dados, Fotos de Referência, Importar SUAP e Reconciliação;'),
    ('Relatórios: ', 'Relatório de Inventário, Relatório de Divergências, Relatório de Itens Compostos e Exportar SIADS;'),
    ('Dashboard: ', 'Dashboard de Coleta, Status das Salas e Consultar Coleta;'),
    ('Analytics (ADMIN/GESTOR): ', 'Dashboard de KPIs, Análise de Divergências e Métricas de Tempo;'),
    ('Sistema: ', 'Modo Offline e Servidor Mobile.'),
]:
    bullet(item[1], bold_prefix=item[0])

# ─────────────────────────────────────────────────────────────────────────────
# 3. CONTROLE DE ACESSO
# ─────────────────────────────────────────────────────────────────────────────
doc.add_heading('3. Controle de Acesso por Perfil', level=1)
par('A aplicação desktop implementa controle de acesso baseado em papéis (Role-Based Access '
    'Control — RBAC), com quatro perfis hierárquicos:')
simple_table(
    ['Perfil', 'Abrangência de Acesso', 'Uso Típico'],
    [
        ('ADMIN',    'Acesso integral a todos os menus, incluindo Administração, Analytics e Sistema', 'Responsável técnico e gestor do patrimônio'),
        ('GESTOR',   'Inventário, Relatórios, Dashboard e Analytics', 'Coordenação, supervisão e fiscalização'),
        ('COLETOR',  'Inventário, Relatórios e Dashboard (sem Analytics e Administração)', 'Equipe de campo operando também pelo desktop'),
        ('CONSULTA', 'Somente Dashboard (visualização)', 'Auditores externos e consulta pública interna'),
    ],
    [2.5, 7.5, 6.0]
)
par('A visibilidade dos menus é definida dinamicamente em MainFrame no momento da autenticação, '
    'com base no atributo perfil do usuário retornado por AutenticacaoService.')

# ─────────────────────────────────────────────────────────────────────────────
# 4. REQUISITOS FUNCIONAIS POR MÓDULO
# ─────────────────────────────────────────────────────────────────────────────
doc.add_heading('4. Requisitos Funcionais por Módulo', level=1)

HDR = ['ID', 'Requisito', 'Descrição', 'Prioridade', 'Tela / Componente', 'Status']
W   = [1.5, 3.5, 6.5, 2.0, 3.5, 1.5]

# 4.1 Autenticação
doc.add_heading('4.1 Módulo de Autenticação e Sessão', level=2)
req_table(HDR, [
    ('RFD-001','Login com usuário e senha','Autenticar o usuário mediante login e senha, consultando o banco configurado e validando credenciais via hash BCrypt','Essencial','JLogin','✅'),
    ('RFD-002','Instância única do sistema','Impedir a execução simultânea de mais de uma instância por máquina, mediante file lock no sistema operacional','Essencial','SingleInstanceManager','✅'),
    ('RFD-003','Configuração de banco na tela de login','Permitir alternar host, porta, banco, usuário e senha da conexão diretamente a partir do diálogo de login','Essencial','ConfiguracaoBancoDialog','✅'),
    ('RFD-004','Alteração de senha do próprio usuário','Permitir que o usuário autenticado altere sua senha mediante informação da senha atual e confirmação da nova','Importante','AlterarSenhaDialog','✅'),
    ('RFD-005','Logout com retorno à tela de login','Encerrar a sessão ativa, descartando o usuário em memória e reapresentando a tela de autenticação','Essencial','MainFrame','✅'),
    ('RFD-006','Controle de acesso por perfil','Habilitar ou ocultar dinamicamente os menus da janela principal com base no perfil do usuário autenticado','Essencial','MainFrame','✅'),
], W)
criteria_box([
    'Tentativas de acesso com credenciais inválidas devem exibir mensagem genérica, sem revelar se o login existe;',
    'Após cinco tentativas inválidas consecutivas em 30 minutos, o usuário deve ser bloqueado temporariamente;',
    'A execução de uma segunda instância deve encerrar-se de forma silenciosa, trazendo ao primeiro plano a instância ativa.',
])

# 4.2 Usuários
doc.add_heading('4.2 Módulo de Gestão de Usuários (Perfil ADMIN)', level=2)
req_table(HDR, [
    ('RFD-007','Listar usuários','Apresentar a relação de usuários cadastrados, com paginação e indicação de perfil e situação','Essencial','UsuarioFrame','✅'),
    ('RFD-008','Cadastrar usuário com perfil','Permitir o cadastro de novo usuário, atribuindo perfil, login, nome completo, e-mail e senha inicial','Essencial','UsuarioFormDialog','✅'),
    ('RFD-009','Editar usuário','Permitir a edição de dados cadastrais do usuário, preservando o histórico de alterações','Essencial','UsuarioFormDialog','✅'),
    ('RFD-010','Ativar e desativar usuário','Permitir a inativação lógica do usuário, impedindo novos logins sem exclusão dos registros históricos','Importante','UsuarioFrame','✅'),
    ('RFD-011','Redefinir senha de usuário','Permitir ao administrador redefinir a senha do usuário, forçando alteração no próximo acesso','Importante','UsuarioFrame','✅'),
    ('RFD-012','Atribuir perfil hierárquico','Permitir atribuir um dos perfis previstos: ADMIN, GESTOR, COLETOR ou CONSULTA','Essencial','UsuarioFormDialog','✅'),
], W)
criteria_box([
    'Somente perfis ADMIN podem acessar o módulo;',
    'Senhas devem ser armazenadas com hash BCrypt;',
    'A desativação não deve remover fisicamente o registro, preservando a rastreabilidade de coletas e auditorias.',
])

# 4.3 Patrimônios
doc.add_heading('4.3 Módulo de Gestão de Patrimônios', level=2)
req_table(HDR, [
    ('RFD-013','Listar patrimônios com paginação','Apresentar a listagem de patrimônios com paginação e ordenação por coluna','Essencial','PatrimonioFrame','✅'),
    ('RFD-014','Cadastrar patrimônio','Permitir o cadastro de novo patrimônio com todos os atributos obrigatórios (número, descrição, sala, responsável, estado, valor)','Essencial','PatrimonioFormDialog','✅'),
    ('RFD-015','Editar patrimônio','Permitir a edição dos dados cadastrais com registro de histórico','Essencial','PatrimonioFormDialog','✅'),
    ('RFD-016','Excluir patrimônio (exclusão lógica)','Permitir a inativação do patrimônio, sem exclusão física, preservando os vínculos históricos','Importante','PatrimonioFrame','✅'),
    ('RFD-017','Busca textual livre','Permitir busca por número, descrição ou sala, com resposta inferior a 500 ms','Essencial','PatrimonioFrame','✅'),
    ('RFD-018','Filtros combinados','Permitir filtrar por status, sala, setor e responsável, de forma combinada','Importante','PatrimonioFrame','✅'),
    ('RFD-019','Gerar QR Code do patrimônio','Gerar QR Code individual para cada patrimônio, com dados codificados em padrão previamente definido','Essencial','QrCodeGenerator','✅'),
    ('RFD-020','Imprimir etiquetas com QR Code','Permitir a impressão de etiquetas com o QR Code, unitária ou em lote, com preview prévio','Importante','ImpressaoEtiquetasDialog','✅'),
    ('RFD-021','Ações em lote','Permitir a seleção múltipla de patrimônios para operações coletivas (impressão, transferência, inativação)','Importante','AcoesPatrimonioDialog','✅'),
    ('RFD-022','Histórico de coletas do patrimônio','Exibir todas as coletas realizadas para um patrimônio em diferentes inventários','Desejável','HistoricoColetaDialog','✅'),
    ('RFD-023','Vincular patrimônio a sala e responsável','Permitir associar patrimônio a uma sala e a um responsável, com validação de integridade referencial','Essencial','PatrimonioFormDialog','✅'),
], W)
criteria_box([
    'O número de patrimônio deve ser único no sistema (RN012);',
    'Operações CRUD devem ser transacionais;',
    'A geração de QR Code deve concluir em menos de 100 ms por item.',
])

# 4.4 Itens Compostos
doc.add_heading('4.4 Módulo de Gestão de Itens Compostos', level=2)
par('Patrimônios compostos (conjuntos formados por múltiplos componentes vinculados) demandam '
    'tratamento diferenciado, sobretudo para fins de coleta e baixa.')
req_table(HDR, [
    ('RFD-024','Listar itens compostos','Apresentar a relação de conjuntos cadastrados e seus respectivos componentes','Importante','ItemCompostoFrame','✅'),
    ('RFD-025','Cadastrar item composto','Permitir o registro de novo conjunto com descrição, patrimônio principal e componentes','Importante','ItemCompostoFrame','✅'),
    ('RFD-026','Adicionar componentes ao conjunto','Permitir associar patrimônios existentes como componentes de um conjunto','Importante','ComponenteDialog','✅'),
    ('RFD-027','Visualizar detalhes do conjunto','Apresentar todos os componentes e o histórico associado ao conjunto','Importante','DetalheItemCompostoDialog','✅'),
    ('RFD-028','Registrar coleta de componente individual','Permitir registrar a coleta isolada de um componente, preservando o vínculo com o conjunto','Importante','RegistroComponenteDialog','✅'),
    ('RFD-029','Coleta específica de itens compostos','Prover fluxo próprio para coleta de conjuntos, conforme a RN025','Essencial','ColetaItemCompostoFrame','✅'),
], W)
criteria_box([
    'Patrimônios compostos não devem aparecer no fluxo de coleta regular (RN025);',
    'A tentativa de coleta por via comum deve apresentar alerta explicativo ao operador.',
])

# 4.5 Inventários
doc.add_heading('4.5 Módulo de Gestão de Inventários', level=2)
req_table(HDR, [
    ('RFD-030','Listar inventários','Apresentar a relação de inventários (passados, em andamento e planejados) com seus respectivos períodos e status','Essencial','InventarioFrame','✅'),
    ('RFD-031','Criar inventário com período','Permitir a criação de novo inventário informando data de início, data de fim e campus','Essencial','InventarioFormDialog','✅'),
    ('RFD-032','Editar inventário','Permitir editar dados do inventário enquanto este estiver em status PLANEJADO','Importante','InventarioFormDialog','✅'),
    ('RFD-033','Ativar e encerrar inventário','Permitir a transição de status (PLANEJADO → EM_ANDAMENTO → ENCERRADO) observando as regras de negócio aplicáveis','Essencial','InventarioFrame','✅'),
    ('RFD-034','Definir escopo do inventário','Permitir selecionar quais salas e setores integram o inventário','Essencial','InventarioFormDialog','✅'),
    ('RFD-035','Cadastrar participantes','Permitir atribuir coletores (participantes) ao inventário, com possível vinculação a salas específicas','Essencial','InventarioFrame','✅'),
    ('RFD-036','Visualizar estatísticas do inventário','Apresentar, em tempo real, o total de itens no escopo, coletados, pendentes e o percentual de conclusão','Essencial','InventarioFrame','✅'),
], W)
criteria_box([
    'Apenas um inventário pode estar ativo por vez em um mesmo campus (RN007);',
    'A transição para ENCERRADO deve ser irreversível e restrita a ADMIN ou GESTOR.',
])

# 4.6 Coleta Desktop
doc.add_heading('4.6 Módulo de Coleta de Dados (pelo Desktop)', level=2)
par('A aplicação desktop provê interface de coleta direta, utilizada em cenários em que o uso do '
    'aplicativo móvel é inviável (ex.: salas com pouca conectividade, digitação em massa de '
    'patrimônios antigos).')
req_table(HDR, [
    ('RFD-037','Registrar coleta manual','Permitir registrar a coleta de um patrimônio mediante digitação do número','Essencial','ColetaFrame_v2','✅'),
    ('RFD-038','Buscar patrimônio para coleta','Oferecer busca por número ou descrição na janela de coleta','Essencial','BuscaItemInventarioDialog','✅'),
    ('RFD-039','Registrar estado de conservação','Permitir selecionar o estado de conservação encontrado, conforme classificação oficial','Essencial','ColetaFrame_v2','✅'),
    ('RFD-040','Registrar localização encontrada','Permitir informar a sala em que o patrimônio foi efetivamente localizado','Essencial','ColetaFrame_v2','✅'),
    ('RFD-041','Registrar observações','Permitir a inclusão de observações livres em até 500 caracteres','Importante','ColetaFrame_v2','✅'),
    ('RFD-042','Consultar coleta por patrimônio','Permitir consultar se um patrimônio específico foi coletado no inventário ativo, exibindo coletor, data e localização','Importante','ConsultaColetaFrame','✅'),
], W)
criteria_box([
    'A coleta só deve ser aceita quando houver inventário ativo (RN011);',
    'A detecção de divergência de localização (RN019) deve ocorrer automaticamente no ato do registro.',
])

# 4.7 Cadastros Auxiliares
doc.add_heading('4.7 Módulo de Cadastros Auxiliares', level=2)
req_table(HDR, [
    ('RFD-043','CRUD de Salas','Permitir cadastrar, editar, inativar e listar salas','Essencial','SalaFrame + SalaFormDialog','✅'),
    ('RFD-044','CRUD de Setores','Permitir cadastrar, editar, inativar e listar setores','Essencial','SetorFrame + SetorFormDialog','✅'),
    ('RFD-045','CRUD de Campus','Permitir cadastrar, editar e listar campus','Importante','CampusFrame + CampusFormDialog','✅'),
    ('RFD-046','CRUD de Responsáveis','Permitir cadastrar, editar, inativar e listar responsáveis','Essencial','ResponsavelFrame + ResponsavelFormDialog','✅'),
    ('RFD-047','Vincular sala a setor','Permitir associar cada sala a um setor de localização','Essencial','SalaFormDialog','✅'),
    ('RFD-048','Vincular setor a campus','Permitir associar cada setor a um campus','Essencial','SetorFormDialog','✅'),
], W)
criteria_box([
    'A inativação deve preservar integridade referencial;',
    'É vedado inativar registros que possuam patrimônios ativos vinculados.',
])

# 4.8 Importação
doc.add_heading('4.8 Módulo de Importação de Dados', level=2)
par('A importação em massa é fundamental para carga inicial e para sincronização com o SUAP '
    '(fonte autoritativa de dados patrimoniais do IFMT).')
req_table(HDR, [
    ('RFD-049','Importar Excel do SUAP','Permitir importar planilha Excel exportada do SUAP, com validação de cabeçalhos','Importante','ImportacaoCSVFrame','✅'),
    ('RFD-050','Importar arquivo CSV','Permitir importar arquivo CSV em formato padronizado, com escolha de encoding e delimitador','Importante','ImportacaoCSVFrame','✅'),
    ('RFD-051','Validação de dados na importação','Validar cada linha antes da persistência, reportando erros com número de linha e coluna','Essencial','DataImportService','✅'),
    ('RFD-052','Log de erros da importação','Gerar relatório de erros exportável em formato de texto','Importante','ImportacaoCSVFrame','✅'),
    ('RFD-053','Cancelamento de importação em andamento','Permitir ao usuário cancelar importação em progresso, sem corromper os dados já persistidos','Importante','ImportacaoCSVFrame','✅'),
    ('RFD-054','Importar dados para modo offline','Permitir carregar os dados necessários para operação offline no SQLite local','Essencial','ImportacaoDadosDialog','✅'),
], W)
criteria_box([
    'Arquivos com até 10.000 linhas devem ser processados em menos de 60 segundos;',
    'O cancelamento deve ser atômico ao nível da transação.',
])

# 4.9 Relatórios
doc.add_heading('4.9 Módulo de Relatórios', level=2)
req_table(HDR, [
    ('RFD-055','Relatório de inventário','Gerar relatório consolidado com itens encontrados, pendentes e divergentes, organizado por sala ou responsável','Essencial','RelatorioFrame','✅'),
    ('RFD-056','Relatório de divergências','Gerar relatório específico dos patrimônios cuja localização encontrada difere da cadastrada','Essencial','RelatorioDivergenciasFrame','✅'),
    ('RFD-057','Relatório de itens compostos','Gerar relatório dos conjuntos e do status de cada componente','Importante','RelatorioItensCompostosFrame','✅'),
    ('RFD-058','Exportar relatório em PDF','Exportar qualquer relatório em PDF, com cabeçalho institucional','Essencial','RelatorioPDFGenerator','✅'),
    ('RFD-059','Exportar relatório em Excel','Exportar relatórios em formato XLSX, com formatação tabular','Essencial','RelatorioExcelGenerator','✅'),
    ('RFD-060','Exportar relatório em CSV','Exportar relatórios em CSV, para processamento externo','Importante','CsvExcelGenerator','✅'),
    ('RFD-061','Filtros combinados de relatório','Permitir filtrar por período, setor, sala e responsável de forma combinada','Importante','RelatorioFrame','✅'),
    ('RFD-062','Resumo executivo','Apresentar, nos relatórios, um resumo executivo com KPIs consolidados','Importante','RelatorioFrame','✅'),
    ('RFD-063','Exportar no formato SIADS','Gerar arquivo compatível com o padrão SIADS para prestação de contas','Essencial','SiadsExportDialog','✅'),
], W)
criteria_box([
    'Relatórios em PDF com até 1.000 páginas devem ser gerados em menos de 30 segundos;',
    'Exportações SIADS devem estar em conformidade com as especificações federais vigentes.',
])

# 4.10 Dashboard
doc.add_heading('4.10 Módulo de Dashboard e Visualização', level=2)
req_table(HDR, [
    ('RFD-064','Dashboard de coleta','Apresentar painel com KPIs do inventário ativo: total, coletados, pendentes e percentual','Essencial','DashboardColetaFrame','✅'),
    ('RFD-065','Status das salas','Apresentar o progresso de coleta por sala, com realce visual de pendências','Importante','StatusSalasFrame','✅'),
    ('RFD-066','Consulta de coleta por patrimônio','Permitir consulta direta do status de coleta de um patrimônio','Importante','ConsultaColetaFrame','✅'),
    ('RFD-067','Gráficos de evolução','Apresentar gráfico temporal da evolução de coletas','Importante','InventarioChartFactory','✅'),
    ('RFD-068','Gráficos por setor e sala','Apresentar gráficos agrupados por setor e por sala','Importante','InventarioChartService','✅'),
], W)
criteria_box([
    'Atualização dos painéis deve ocorrer automaticamente a cada 30 segundos, ou sob demanda do usuário;',
    'Gráficos devem ser exportáveis como imagem.',
])

# 4.11 Analytics
doc.add_heading('4.11 Módulo de Analytics (ADMIN e GESTOR)', level=2)
par('Módulo destinado à gestão estratégica, com painéis de indicadores avançados.')
req_table(HDR, [
    ('RFD-069','Dashboard de KPIs','Apresentar painel consolidado com indicadores-chave de desempenho do processo de inventário','Importante','AnalyticsDashboardFrame','✅'),
    ('RFD-070','Análise de divergências','Apresentar análise qualitativa e quantitativa das divergências detectadas','Importante','DivergenciasAnalyticsFrame','✅'),
    ('RFD-071','Métricas de tempo de coleta','Apresentar tempo médio, mínimo e máximo de coleta por coletor e por sala','Desejável','MetricasTempoAnalyticsFrame','✅'),
    ('RFD-072','Cartões de KPI com indicadores visuais','Utilizar cards com indicadores de tendência (↑ ↓ =) para destacar desvios','Desejável','KPICard','✅'),
], W)
criteria_box([
    'Indicadores devem ser calculados sobre o inventário selecionado;',
    'Usuários CONSULTA e COLETOR não devem ter acesso a este módulo.',
])

# 4.12 Fotos de Referência
doc.add_heading('4.12 Módulo de Fotos de Referência', level=2)
req_table(HDR, [
    ('RFD-073','Gerenciar fotos por descrição','Permitir associar uma foto de referência a cada descrição de patrimônio','Desejável','FotoReferenciaFrame','✅'),
    ('RFD-074','Carregar foto para descrição','Permitir o upload de imagem (JPEG ou PNG) com limite de 5 MB','Desejável','FotoReferenciaFrame','✅'),
    ('RFD-075','Visualizar foto de referência','Exibir a foto em janela ampliada quando solicitado','Desejável','FotoReferenciaFrame','✅'),
], W)
criteria_box([
    'Fotos de referência são utilizadas pelo aplicativo móvel para auxiliar coletores (vide RF095).',
])

# 4.13 Reconciliação
doc.add_heading('4.13 Módulo de Reconciliação', level=2)
par('A reconciliação é o processo de correspondência entre itens cadastrados e não encontrados, '
    'com itens fisicamente localizados sem etiqueta de identificação.')
req_table(HDR, [
    ('RFD-076','Correspondência manual de itens','Permitir relacionar manualmente itens não encontrados com itens sem etiqueta','Importante','ReconciliacaoFrame','✅'),
    ('RFD-077','Sugerir correspondências automaticamente','Sugerir correspondências por similaridade de descrição e localização','Importante','ReconciliacaoFrame','✅'),
    ('RFD-078','Confirmar ou rejeitar correspondência','Registrar a decisão do operador, com log de auditoria','Importante','ReconciliacaoFrame','✅'),
], W)
criteria_box([
    'Cada decisão deve ser logada com usuário, data e justificativa (quando rejeitada);',
    'As sugestões devem utilizar algoritmo de similaridade com limiar configurável.',
])

# 4.14 Etiquetas
doc.add_heading('4.14 Módulo de Impressão de Etiquetas', level=2)
req_table(HDR, [
    ('RFD-079','Gerar etiquetas com QR Code','Gerar etiquetas com QR Code legível, número e descrição reduzida','Essencial','EtiquetaGenerator','✅'),
    ('RFD-080','Imprimir em impressora térmica','Suportar impressão em impressoras térmicas padrão ESC/POS','Importante','ImpressoraTermicaService','✅'),
    ('RFD-081','Configurar layout de etiqueta','Permitir configurar o tamanho, margens e conteúdo da etiqueta','Importante','EtiquetaConfig','✅'),
    ('RFD-082','Impressão em lote','Permitir imprimir etiquetas para múltiplos patrimônios em uma única operação','Importante','ImpressaoEtiquetasDialog','✅'),
], W)
criteria_box([
    'Os QR Codes devem ser legíveis por câmera de dispositivo Android mesmo após impressão térmica a 203 DPI;',
    'O layout deve preservar as dimensões configuradas independentemente da impressora.',
])

# 4.15 Módulo de Monitoramento do Servidor Mobile
doc.add_heading('4.15 Módulo de Monitoramento do Servidor Mobile', level=2)

# Nota de arquitetura
p_nota = doc.add_paragraph()
p_nota.paragraph_format.space_before = Pt(4)
p_nota.paragraph_format.space_after  = Pt(6)
p_nota.paragraph_format.left_indent  = Pt(12)
# Fundo amarelo via XML
from docx.oxml import OxmlElement
from docx.oxml.ns import qn as _qn
pPr = p_nota._p.get_or_add_pPr()
pBdr = OxmlElement('w:pBdr')
left = OxmlElement('w:left')
left.set(_qn('w:val'), 'single'); left.set(_qn('w:sz'), '6')
left.set(_qn('w:space'), '4'); left.set(_qn('w:color'), 'E6A817')
pBdr.append(left); pPr.append(pBdr)
r_nota = p_nota.add_run(
    '⚠️  Nota de arquitetura (Mai/2026): O servidor da API Mobile não é mais integrado à '
    'aplicação desktop. O servidor é iniciado e gerenciado de forma independente (processo '
    'separado). A aplicação desktop mantém apenas uma interface de monitoramento passivo, '
    'cujas funcionalidades de visualização em tempo real ainda não estão funcionais nesta versão.'
)
r_nota.font.size = Pt(9.5)
r_nota.font.color.rgb = RGBColor(0x7B, 0x4F, 0x00)
r_nota.font.italic = True
r_nota.font.name = 'Calibri'

AMARELO_BG = RGBColor(0xFF, 0xF3, 0xCD)

# Tabela com status diferenciados
table = doc.add_table(rows=7, cols=6)
table.style = 'Table Grid'
table.alignment = WD_TABLE_ALIGNMENT.CENTER
set_col_widths(table, [1.5, 3.5, 6.5, 2.0, 3.5, 1.5])
add_hdr(table, ['ID', 'Requisito', 'Descrição', 'Prioridade', 'Tela / Componente', 'Status'])

rows_data = [
    ('RFD-083', 'Iniciar servidor (REMOVIDO)',
     'Descontinuado — o servidor é agora um processo independente, não iniciado pelo desktop.',
     '—', 'MobileServerPanel', '❌ Removido'),
    ('RFD-084', 'Parar servidor (REMOVIDO)',
     'Descontinuado — o encerramento do servidor é feito externamente ao desktop.',
     '—', 'MobileServerPanel', '❌ Removido'),
    ('RFD-085', 'Monitorar status do servidor',
     'Exibir indicadores de estado do servidor externo (em execução, parado, com falha), '
     'consultando o endpoint /health.',
     'Importante', 'MobileServerPanel', '⚠️ Não funcional'),
    ('RFD-086', 'Monitorar dispositivos conectados',
     'Apresentar a relação de dispositivos móveis autenticados, com IP e última atividade.',
     'Importante', 'MobileMonitorFrameV2', '⚠️ Não funcional'),
    ('RFD-087', 'Visualizar requisições em tempo real',
     'Apresentar as requisições recentes com endpoint, método, dispositivo e tempo de resposta.',
     'Desejável', 'MobileMonitorFrameV2', '⚠️ Não funcional'),
    ('RFD-088', 'Desconectar dispositivo',
     'Permitir a desconexão forçada de um dispositivo específico via chamada REST.',
     'Desejável', 'MobileMonitorFrameV2', '⚠️ Não funcional'),
]

for r_idx, row_data in enumerate(rows_data):
    row = table.rows[r_idx + 1]
    removido = '❌' in row_data[5]
    bg = 'FFF3CD' if '⚠️' in row_data[5] else ('FDECEA' if removido else 'FFFFFF')
    for c_idx, cell_text in enumerate(row_data):
        set_cell_bg(row.cells[c_idx], bg)
        if c_idx == 0:
            color = RGBColor(0x99, 0x00, 0x00) if removido else AZUL_ESCURO
        elif '❌' in str(cell_text):
            color = RGBColor(0x99, 0x00, 0x00)
        elif '⚠️' in str(cell_text):
            color = RGBColor(0x7B, 0x4F, 0x00)
        else:
            color = CINZA_TEXTO
        bold = c_idx == 0
        italic = removido and c_idx in (1, 2)
        set_cell_text(row.cells[c_idx], str(cell_text),
                      bold=bold, size=8.5, color=color, italic=italic)

doc.add_paragraph()

par('Situação atual: RFD-083 e RFD-084 foram descontinuados com a separação arquitetural do '
    'servidor. Os requisitos RFD-085 a RFD-088 permanecem no escopo como funcionalidades de '
    'monitoramento passivo, porém ainda não estão operacionais nesta versão e serão '
    'implementados em iteração futura.',
    italic=True, color=RGBColor(0x55, 0x55, 0x55))

criteria_box([
    'O painel deve consultar o endpoint /health do servidor externo para exibir o estado;',
    'A desconexão forçada deve chamar o endpoint REST correspondente e invalidar o token ativo do dispositivo.',
])

# 4.16 Modo Offline
doc.add_heading('4.16 Módulo de Operação em Modo Offline', level=2)
req_table(HDR, [
    ('RFD-089','Detectar perda de conexão','Detectar automaticamente a indisponibilidade do banco PostgreSQL','Essencial','ConnectivityManager','✅'),
    ('RFD-090','Alternar para modo offline (SQLite)','Alternar, de forma transparente, para o banco local SQLite quando em modo offline','Essencial','OfflineManager','✅'),
    ('RFD-091','Sincronizar PostgreSQL para SQLite','Sincronizar os dados necessários do banco central para o banco local','Essencial','SyncPostgresToSQLiteV2','✅'),
    ('RFD-092','Forçar modo offline manualmente','Permitir ao usuário forçar o modo offline mesmo com conexão ativa','Importante','MainFrame','✅'),
    ('RFD-093','Tentar reconectar ao banco online','Permitir ao usuário tentar o retorno ao modo online mediante solicitação explícita','Importante','MainFrame','✅'),
    ('RFD-094','Indicador visual de status de conexão','Exibir indicador permanente na barra de status (Online / Offline / Reconectando)','Essencial','StatusBarPanel','✅'),
], W)
criteria_box([
    'A alternância de modo deve ser transparente para o usuário, preservando o estado da tela ativa;',
    'A sincronização para SQLite deve preservar a integridade referencial;',
    'A reconciliação deve ocorrer automaticamente ao retornar ao modo online.',
])

# 4.17 Configuração
doc.add_heading('4.17 Módulo de Configuração', level=2)
req_table(HDR, [
    ('RFD-095','Configurar conexão com o banco','Permitir a configuração de host, porta, banco, usuário e senha','Essencial','ConfiguracaoBancoDialog','✅'),
    ('RFD-096','Testar conexão com o banco','Permitir testar a conectividade antes de salvar a configuração','Essencial','ConfiguracaoBancoDialog','✅'),
    ('RFD-097','Persistir configuração em arquivo JSON','Salvar a configuração em arquivo no diretório do usuário, com criptografia da senha','Essencial','DatabaseConfigManager','✅'),
    ('RFD-098','Status do sistema','Apresentar informações de uso de memória, número de conexões ativas e versão','Desejável','MainFrame','✅'),
], W)
criteria_box([
    'A senha no arquivo JSON deve estar cifrada;',
    'O teste de conexão não deve persistir credenciais em caso de falha;',
    'O arquivo JSON deve ficar em ~/.sihcp/configuracao_banco.json.',
])

# ─────────────────────────────────────────────────────────────────────────────
# 5. CENÁRIOS DE VALIDAÇÃO
# ─────────────────────────────────────────────────────────────────────────────
doc.add_heading('5. Cenários de Validação', level=1)

doc.add_heading('5.1 Cenário: Execução de Inventário Completo', level=2)
par('Pré-condições: Usuário ADMIN autenticado; banco PostgreSQL disponível; dados do SUAP '
    'previamente importados.', bold=True)
par('Fluxo principal:')
for step in [
    'ADMIN acessa o menu Inventário → Gerenciar Inventários e cria um novo inventário (RFD-031);',
    'Define o escopo de salas e setores (RFD-034) e os participantes (RFD-035);',
    'Ativa o inventário, alterando o status para EM_ANDAMENTO (RFD-033);',
    'O servidor mobile passa a disponibilizar este inventário como ativo para os coletores;',
    'Durante o período, acompanha o progresso pelo Dashboard (RFD-064) e Status das Salas (RFD-065);',
    'Ao término, executa reconciliação dos itens sem etiqueta (RFD-076 a RFD-078);',
    'Gera relatórios finais em PDF (RFD-055, RFD-058) e exporta para SIADS (RFD-063);',
    'Encerra o inventário (RFD-033), alterando o status para ENCERRADO.',
]:
    numbered(step)
par('Critério de sucesso: Inventário encerrado com 100% dos itens classificados (encontrados, '
    'pendentes ou justificados), com trilha de auditoria completa.', italic=True)

doc.add_heading('5.2 Cenário: Falha de Conexão durante Operação', level=2)
par('Pré-condições: Aplicação em execução com conexão ao PostgreSQL; banco de contingência '
    'SQLite sincronizado.', bold=True)
par('Fluxo principal:')
for step in [
    'Ocorre queda de conexão com o PostgreSQL;',
    'ConnectivityManager detecta a falha em até 10 segundos (RFD-089);',
    'Sistema alterna automaticamente para o banco SQLite local (RFD-090);',
    'Indicador na barra de status passa de Online para Offline (RFD-094);',
    'Usuário prossegue com a operação utilizando os dados locais;',
    'Quando a conexão é restabelecida, o sistema oferece a opção de reconectar (RFD-093);',
    'Ao reconectar, os dados locais são sincronizados para o PostgreSQL.',
]:
    numbered(step)
par('Critério de sucesso: Nenhum dado é perdido na transição; o usuário é informado do estado '
    'de operação a todo momento.', italic=True)

doc.add_heading('5.3 Cenário: Importação Inicial de Dados do SUAP', level=2)
par('Pré-condições: Arquivo Excel exportado do SUAP disponível; ADMIN autenticado.', bold=True)
par('Fluxo principal:')
for step in [
    'ADMIN acessa Inventário → Importar SUAP (RFD-049);',
    'Seleciona o arquivo Excel e confirma;',
    'Sistema valida cabeçalhos, formatos e integridade (RFD-051);',
    'Exibe relatório prévio com total de linhas válidas e inválidas;',
    'ADMIN confirma a importação;',
    'Linhas válidas são persistidas em lotes transacionais;',
    'Ao término, sistema exibe total importado e oferece download do log de erros (RFD-052).',
]:
    numbered(step)
par('Critério de sucesso: Arquivos com até 10.000 linhas são importados em menos de 60 segundos; '
    'zero inserções duplicadas por número de patrimônio (RN012).', italic=True)

doc.add_heading('5.4 Cenário: Geração de Etiquetas em Lote', level=2)
par('Pré-condições: Patrimônios cadastrados sem etiqueta; impressora térmica configurada.', bold=True)
par('Fluxo principal:')
for step in [
    'Usuário acessa PatrimonioFrame e filtra patrimônios sem etiqueta (RFD-018);',
    'Seleciona múltiplos itens e aciona a ação "Imprimir etiquetas" (RFD-021);',
    'Sistema abre ImpressaoEtiquetasDialog exibindo preview;',
    'Usuário confirma o envio para a impressora térmica (RFD-082);',
    'Sistema gera QR Codes (RFD-019) e formata conforme o layout configurado (RFD-081);',
    'Etiquetas são impressas sequencialmente.',
]:
    numbered(step)
par('Critério de sucesso: Todas as etiquetas devem ser legíveis por câmera Android; geração de '
    'QR Code inferior a 100 ms por item.', italic=True)

# ─────────────────────────────────────────────────────────────────────────────
# 6. RISCOS E MITIGAÇÕES
# ─────────────────────────────────────────────────────────────────────────────
doc.add_heading('6. Riscos e Mitigações', level=1)
simple_table(
    ['Risco', 'Impacto', 'Probabilidade', 'Mitigação'],
    [
        ('Perda de conexão com o banco durante operação crítica','Alto','Média','Modo offline com SQLite (RFD-089 a RFD-094); sincronização bidirecional'),
        ('Corrupção do banco local SQLite','Alto','Baixa','Backups periódicos do arquivo; checksums de integridade no startup'),
        ('Inconsistência entre dados SUAP e SIHCP','Médio','Alta','Validação na importação (RFD-051); relatório de erros detalhado'),
        ('Acesso indevido a funcionalidades administrativas','Alto','Baixa','Controle RBAC rígido (RFD-006); menus condicionais; auditoria de acessos'),
        ('Execução simultânea de múltiplas instâncias gerando conflitos','Médio','Média','SingleInstanceManager com file lock (RFD-002)'),
        ('Importação cancelada deixando dados parcialmente inseridos','Médio','Baixa','Transações atômicas com rollback automático (RFD-053)'),
        ('Incompatibilidade de formato SIADS após atualização federal','Alto','Média','Monitoramento das especificações federais; módulo SiadsExportService parametrizável'),
        ('Impressoras térmicas com drivers incompatíveis','Médio','Média','Suporte nativo ao padrão ESC/POS; testes em múltiplos modelos'),
        ('Falhas na reconciliação automática de itens sem etiqueta','Médio','Média','Sugestões por similaridade com limiar configurável; confirmação humana obrigatória (RFD-078)'),
    ],
    [4.5, 1.8, 2.2, 7.5]
)

# ─────────────────────────────────────────────────────────────────────────────
# 7. MATRIZ DE RASTREABILIDADE
# ─────────────────────────────────────────────────────────────────────────────
doc.add_heading('7. Matriz de Rastreabilidade', level=1)

doc.add_heading('7.1 Distribuição por Módulo', level=2)
simple_table(
    ['Módulo', 'Intervalo de IDs', 'Quantidade'],
    [
        ('Autenticação e Sessão',    'RFD-001 a RFD-006',  '6'),
        ('Gestão de Usuários',       'RFD-007 a RFD-012',  '6'),
        ('Gestão de Patrimônios',    'RFD-013 a RFD-023', '11'),
        ('Itens Compostos',          'RFD-024 a RFD-029',  '6'),
        ('Gestão de Inventários',    'RFD-030 a RFD-036',  '7'),
        ('Coleta de Dados (Desktop)','RFD-037 a RFD-042',  '6'),
        ('Cadastros Auxiliares',     'RFD-043 a RFD-048',  '6'),
        ('Importação de Dados',      'RFD-049 a RFD-054',  '6'),
        ('Relatórios',               'RFD-055 a RFD-063',  '9'),
        ('Dashboard e Visualização', 'RFD-064 a RFD-068',  '5'),
        ('Analytics',                'RFD-069 a RFD-072',  '4'),
        ('Fotos de Referência',      'RFD-073 a RFD-075',  '3'),
        ('Reconciliação',            'RFD-076 a RFD-078',  '3'),
        ('Impressão de Etiquetas',   'RFD-079 a RFD-082',  '4'),
        ('Servidor Mobile',          'RFD-083 a RFD-088',  '4 (2 removidos, 4 parciais)'),
        ('Modo Offline',             'RFD-089 a RFD-094',  '6'),
        ('Configuração',             'RFD-095 a RFD-098',  '4'),
        ('TOTAL',                    '',                   '98'),
    ],
    [6, 5, 2.5]
)

doc.add_heading('7.2 Cobertura de Implementação', level=2)
simple_table(
    ['Situação', 'Quantidade', 'Percentual'],
    [
        ('Implementado',             '96', '98%'),
        ('Parcial / Não funcional',   '4',  '4%'),
        ('Removido (descontinuado)',  '2',  '2%'),
    ],
    [6, 3, 3]
)

doc.add_heading('7.3 Distribuição por Prioridade', level=2)
simple_table(
    ['Prioridade', 'Quantidade', 'Percentual'],
    [
        ('Essencial',  '48', '49%'),
        ('Importante', '42', '43%'),
        ('Desejável',   '8',  '8%'),
    ],
    [6, 3, 3]
)

# ─────────────────────────────────────────────────────────────────────────────
# APÊNDICE A — GLOSSÁRIO
# ─────────────────────────────────────────────────────────────────────────────
doc.add_heading('Apêndice A — Glossário de Termos', level=1)
simple_table(
    ['Termo', 'Definição'],
    [
        ('Patrimônio',    'Bem móvel pertencente à instituição, identificado por número de tombamento'),
        ('Tombamento',    'Registro oficial de um bem no acervo patrimonial da instituição'),
        ('Inventário',    'Levantamento físico periódico dos bens patrimoniais'),
        ('Coleta',        'Registro da verificação física de um patrimônio durante o inventário'),
        ('Divergência',   'Diferença entre a localização cadastrada e a localização encontrada'),
        ('Item Composto', 'Conjunto formado por vários componentes tratados como unidade patrimonial'),
        ('Reconciliação', 'Processo de correspondência entre itens pendentes e itens sem etiqueta localizados'),
        ('Modo Offline',  'Operação com banco SQLite local, ativada quando o PostgreSQL não está disponível'),
        ('QR Code',       'Código de barras bidimensional utilizado para identificação rápida'),
        ('SIADS',         'Sistema Integrado de Administração de Serviços (padrão federal)'),
        ('SUAP',          'Sistema Unificado de Administração Pública (IFMT)'),
        ('Fat JAR',       'Arquivo .jar autocontido com todas as dependências Java'),
    ],
    [4, 12]
)

# ─────────────────────────────────────────────────────────────────────────────
# APÊNDICE B — HISTÓRICO
# ─────────────────────────────────────────────────────────────────────────────
doc.add_heading('Apêndice B — Histórico de Revisões', level=1)
simple_table(
    ['Versão', 'Data', 'Autor', 'Descrição'],
    [
        ('1.0.0','Mai/2026','Equipe IFMT','Versão inicial com 98 requisitos funcionais organizados em 17 módulos'),
        ('2.0.0','Mai/2026','Equipe IFMT','Reescrita com formalidade institucional: introdução, propósito e escopo, prioridades explícitas, critérios de aceitação, referências cruzadas, cenários de validação, riscos e mitigações, glossário formal'),
        ('2.1.0','Mai/2026','Equipe IFMT','Atualização arquitetural: módulo 4.15 renomeado para Monitoramento do Servidor Mobile; RFD-083 e RFD-084 descontinuados (servidor agora é processo independente); RFD-085 a RFD-088 marcados como parciais/não funcionais; matriz atualizada'),
    ],
    [1.5, 2.0, 3.0, 9.5]
)

# ─────────────────────────────────────────────────────────────────────────────
# APÊNDICE C — REFERÊNCIAS CRUZADAS
# ─────────────────────────────────────────────────────────────────────────────
doc.add_heading('Apêndice C — Referências Cruzadas', level=1)

doc.add_heading('C.1 Documentos Relacionados', level=2)
simple_table(
    ['Documento', 'Escopo'],
    [
        ('REQUISITOS_FUNCIONAIS_NAO_FUNCIONAIS.md',      'Especificação consolidada do ecossistema SIHCP (RFs e RNFs)'),
        ('REQUISITOS_NAO_FUNCIONAIS_DESKTOP.md',         'Requisitos não funcionais da aplicação desktop'),
        ('REQUISITOS_FUNCIONAIS_SERVIDOR_MOBILE.md',     'Requisitos funcionais da API mobile'),
        ('REQUISITOS_NAO_FUNCIONAIS_SERVIDOR_MOBILE.md', 'Requisitos não funcionais da API mobile'),
        ('ADEQUACAO_NORMAS_FEDERAIS.md',                 'Conformidade com a legislação federal aplicável'),
        ('ARQUITETURA_RECONCILIACAO.md',                 'Arquitetura do módulo de reconciliação'),
    ],
    [7, 9]
)

doc.add_heading('C.2 Mapeamento com Requisitos do Documento Integrado', level=2)
simple_table(
    ['RFD Desktop', 'RF Integrado Correspondente'],
    [
        ('RFD-001, RFD-004, RFD-005', 'RF001, RF007, RF010'),
        ('RFD-006',                   'RF005, RF006'),
        ('RFD-007 a RFD-012',         'RF081 (CRUD de Usuários)'),
        ('RFD-013 a RFD-023',         'RF011 a RF022'),
        ('RFD-030 a RFD-036',         'RF023 a RF030'),
        ('RFD-037 a RFD-042',         'RF032, RF034 a RF037, RF041'),
        ('RFD-043 a RFD-048',         'RF078 a RF085'),
        ('RFD-049 a RFD-054',         'RF016 a RF018, RF058'),
        ('RFD-055 a RFD-063',         'RF059 a RF069'),
        ('RFD-064 a RFD-068',         'RF070, RF072, RF073, RF075'),
        ('RFD-069 a RFD-072',         'RF076, RF077'),
        ('RFD-079 a RFD-082',         'RF020, RF021'),
        ('RFD-083 a RFD-088',         'RF086 a RF091'),
        ('RFD-089 a RFD-094',         'RF048, RF049, RF058'),
    ],
    [5, 11]
)

# ─────────────────────────────────────────────────────────────────────────────
# RODAPÉ
# ─────────────────────────────────────────────────────────────────────────────
doc.add_paragraph()
divider()
p = doc.add_paragraph(); p.alignment = WD_ALIGN_PARAGRAPH.CENTER
r = p.add_run(
    'Versão do documento: 2.1.0  |  Versão do sistema: 1.2.0  |  Maio de 2026\n'
    'Instituto Federal de Educação, Ciência e Tecnologia de Mato Grosso (IFMT)'
)
r.font.size = Pt(9); r.font.color.rgb = RGBColor(0x88, 0x88, 0x88)
r.font.italic = True; r.font.name = 'Calibri'

# ─────────────────────────────────────────────────────────────────────────────
# SALVAR
# ─────────────────────────────────────────────────────────────────────────────
caminho = (r'c:\Users\Romulo\Documents\PROJETOS\MASTER_INVENTARIO'
           r'\docs\arquitetura\REQUISITOS_FUNCIONAIS_DESKTOP.docx')
doc.save(caminho)
print(f'✅ Documento salvo em:\n   {caminho}')
