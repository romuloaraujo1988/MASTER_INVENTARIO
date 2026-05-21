"""
Gerador do documento DOCX: Requisitos Funcionais — Servidor Mobile API
Fonte: REQUISITOS_FUNCIONAIS_SERVIDOR_MOBILE.md
"""

from docx import Document
from docx.shared import Pt, Inches, RGBColor, Cm
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT, WD_ALIGN_VERTICAL
from docx.oxml.ns import qn
from docx.oxml import OxmlElement
import copy

# ─────────────────────────────────────────────────────────────────────────────
# PALETA
# ─────────────────────────────────────────────────────────────────────────────
AZUL_ESCURO   = RGBColor(0x1A, 0x37, 0x6C)   # cabeçalhos principais
AZUL_MEDIO    = RGBColor(0x2E, 0x5E, 0xA8)   # cabeçalhos secundários
AZUL_CLARO    = RGBColor(0xD6, 0xE4, 0xF7)   # fundo de cabeçalho de tabela
CINZA_CLARO   = RGBColor(0xF5, 0xF5, 0xF5)   # fundo de linhas alternadas
VERDE_STATUS  = RGBColor(0x1E, 0x7E, 0x34)   # ✅ implementado
BRANCO        = RGBColor(0xFF, 0xFF, 0xFF)
PRETO         = RGBColor(0x00, 0x00, 0x00)
CINZA_TEXTO   = RGBColor(0x44, 0x44, 0x44)

doc = Document()

# ─────────────────────────────────────────────────────────────────────────────
# CONFIGURAÇÃO DE PÁGINA (A4)
# ─────────────────────────────────────────────────────────────────────────────
section = doc.sections[0]
section.page_width  = Cm(21.0)
section.page_height = Cm(29.7)
section.left_margin   = Cm(2.5)
section.right_margin  = Cm(2.5)
section.top_margin    = Cm(2.5)
section.bottom_margin = Cm(2.0)

# ─────────────────────────────────────────────────────────────────────────────
# ESTILOS BASE
# ─────────────────────────────────────────────────────────────────────────────
styles = doc.styles

def set_style(style_name, font_name, font_size, bold=False, color=PRETO,
              space_before=0, space_after=6, alignment=WD_ALIGN_PARAGRAPH.LEFT):
    try:
        s = styles[style_name]
    except KeyError:
        s = styles.add_style(style_name, 1)
    s.font.name = font_name
    s.font.size = Pt(font_size)
    s.font.bold = bold
    s.font.color.rgb = color
    s.paragraph_format.space_before = Pt(space_before)
    s.paragraph_format.space_after  = Pt(space_after)
    s.paragraph_format.alignment    = alignment
    return s

# Normal
normal = styles['Normal']
normal.font.name = 'Calibri'
normal.font.size = Pt(10)
normal.font.color.rgb = CINZA_TEXTO
normal.paragraph_format.space_after = Pt(4)

# Heading 1
h1 = styles['Heading 1']
h1.font.name  = 'Calibri'
h1.font.size  = Pt(16)
h1.font.bold  = True
h1.font.color.rgb = AZUL_ESCURO
h1.paragraph_format.space_before = Pt(18)
h1.paragraph_format.space_after  = Pt(6)

# Heading 2
h2 = styles['Heading 2']
h2.font.name  = 'Calibri'
h2.font.size  = Pt(13)
h2.font.bold  = True
h2.font.color.rgb = AZUL_MEDIO
h2.paragraph_format.space_before = Pt(14)
h2.paragraph_format.space_after  = Pt(4)

# Heading 3
h3 = styles['Heading 3']
h3.font.name  = 'Calibri'
h3.font.size  = Pt(11)
h3.font.bold  = True
h3.font.color.rgb = AZUL_MEDIO
h3.paragraph_format.space_before = Pt(10)
h3.paragraph_format.space_after  = Pt(3)

# ─────────────────────────────────────────────────────────────────────────────
# HELPERS
# ─────────────────────────────────────────────────────────────────────────────

def par(text, style='Normal', bold=False, italic=False, size=None,
        color=None, align=WD_ALIGN_PARAGRAPH.LEFT, space_before=0, space_after=4):
    p = doc.add_paragraph(style=style)
    p.alignment = align
    p.paragraph_format.space_before = Pt(space_before)
    p.paragraph_format.space_after  = Pt(space_after)
    run = p.add_run(text)
    run.bold   = bold
    run.italic = italic
    if size:
        run.font.size = Pt(size)
    if color:
        run.font.color.rgb = color
    return p


def par_mixed(parts, align=WD_ALIGN_PARAGRAPH.LEFT, space_before=0, space_after=4):
    """parts = list of (text, bold, italic, color, size)"""
    p = doc.add_paragraph()
    p.alignment = align
    p.paragraph_format.space_before = Pt(space_before)
    p.paragraph_format.space_after  = Pt(space_after)
    for text, bold, italic, color, size in parts:
        run = p.add_run(text)
        run.bold   = bold
        run.italic = italic
        if color:
            run.font.color.rgb = color
        if size:
            run.font.size = Pt(size)
    return p


def bullet(text, level=0, bold_prefix=None):
    p = doc.add_paragraph(style='List Bullet')
    p.paragraph_format.left_indent   = Pt(18 + level * 18)
    p.paragraph_format.space_after   = Pt(2)
    p.paragraph_format.space_before  = Pt(1)
    if bold_prefix:
        r1 = p.add_run(bold_prefix)
        r1.bold = True
        r1.font.color.rgb = AZUL_ESCURO
        r2 = p.add_run(text)
        r2.font.color.rgb = CINZA_TEXTO
    else:
        run = p.add_run(text)
        run.font.color.rgb = CINZA_TEXTO
    return p


def numbered(text, level=0):
    p = doc.add_paragraph(style='List Number')
    p.paragraph_format.left_indent  = Pt(18 + level * 18)
    p.paragraph_format.space_after  = Pt(2)
    p.paragraph_format.space_before = Pt(1)
    run = p.add_run(text)
    run.font.color.rgb = CINZA_TEXTO
    return p


def set_cell_bg(cell, color_hex: str):
    tc   = cell._tc
    tcPr = tc.get_or_add_tcPr()
    shd  = OxmlElement('w:shd')
    shd.set(qn('w:val'),   'clear')
    shd.set(qn('w:color'), 'auto')
    shd.set(qn('w:fill'),  color_hex)
    tcPr.append(shd)


def set_cell_text(cell, text, bold=False, size=9, color=PRETO,
                  align=WD_ALIGN_PARAGRAPH.LEFT, italic=False):
    cell.text = ''
    p = cell.paragraphs[0]
    p.alignment = align
    p.paragraph_format.space_before = Pt(1)
    p.paragraph_format.space_after  = Pt(1)
    run = p.add_run(text)
    run.bold   = bold
    run.italic = italic
    run.font.size  = Pt(size)
    run.font.color.rgb = color
    run.font.name  = 'Calibri'


def set_col_widths(table, widths_cm):
    for row in table.rows:
        for i, cell in enumerate(row.cells):
            if i < len(widths_cm):
                cell.width = Cm(widths_cm[i])


def add_table_header(table, headers, bg_hex='1A376C', text_color=BRANCO):
    hdr = table.rows[0]
    for i, h in enumerate(headers):
        if i < len(hdr.cells):
            set_cell_bg(hdr.cells[i], bg_hex)
            set_cell_text(hdr.cells[i], h, bold=True, size=9,
                          color=text_color, align=WD_ALIGN_PARAGRAPH.CENTER)


def req_table(headers, rows, col_widths, alt_bg='F5F5F5'):
    """Tabela de requisitos com cabeçalho azul e linhas alternadas."""
    table = doc.add_table(rows=1 + len(rows), cols=len(headers))
    table.style = 'Table Grid'
    table.alignment = WD_TABLE_ALIGNMENT.CENTER
    set_col_widths(table, col_widths)
    add_table_header(table, headers)
    for r_idx, row_data in enumerate(rows):
        row = table.rows[r_idx + 1]
        bg  = alt_bg if r_idx % 2 == 1 else 'FFFFFF'
        for c_idx, cell_text in enumerate(row_data):
            set_cell_bg(row.cells[c_idx], bg)
            bold  = c_idx == 0  # ID em negrito
            color = VERDE_STATUS if '✅' in str(cell_text) else CINZA_TEXTO
            if c_idx == 0:
                color = AZUL_ESCURO
            set_cell_text(row.cells[c_idx], str(cell_text),
                          bold=bold, size=8.5, color=color)
    doc.add_paragraph()
    return table


def simple_table(headers, rows, col_widths, alt_bg='F5F5F5'):
    """Tabela simples (glossário, histórico, etc.)."""
    table = doc.add_table(rows=1 + len(rows), cols=len(headers))
    table.style = 'Table Grid'
    table.alignment = WD_TABLE_ALIGNMENT.CENTER
    set_col_widths(table, col_widths)
    add_table_header(table, headers, bg_hex='2E5EA8')
    for r_idx, row_data in enumerate(rows):
        row = table.rows[r_idx + 1]
        bg  = alt_bg if r_idx % 2 == 1 else 'FFFFFF'
        for c_idx, cell_text in enumerate(row_data):
            set_cell_bg(row.cells[c_idx], bg)
            bold = c_idx == 0
            set_cell_text(row.cells[c_idx], str(cell_text),
                          bold=bold, size=9, color=CINZA_TEXTO)
    doc.add_paragraph()
    return table


def section_divider():
    p = doc.add_paragraph()
    p.paragraph_format.space_before = Pt(2)
    p.paragraph_format.space_after  = Pt(2)
    run = p.add_run('─' * 90)
    run.font.color.rgb = RGBColor(0xCC, 0xCC, 0xCC)
    run.font.size = Pt(7)


def criteria_box(items):
    """Caixa de critérios de aceitação."""
    p = doc.add_paragraph()
    p.paragraph_format.space_before = Pt(4)
    p.paragraph_format.space_after  = Pt(2)
    run = p.add_run('Critérios de aceitação:')
    run.bold = True
    run.font.color.rgb = AZUL_ESCURO
    run.font.size = Pt(10)
    for item in items:
        bullet(item)


# ─────────────────────────────────────────────────────────────────────────────
# CAPA
# ─────────────────────────────────────────────────────────────────────────────
doc.add_paragraph()
doc.add_paragraph()

p_inst = doc.add_paragraph()
p_inst.alignment = WD_ALIGN_PARAGRAPH.CENTER
r = p_inst.add_run('INSTITUTO FEDERAL DE EDUCAÇÃO, CIÊNCIA E TECNOLOGIA DE MATO GROSSO')
r.bold = True
r.font.size = Pt(11)
r.font.color.rgb = AZUL_ESCURO
r.font.name = 'Calibri'

p_inst2 = doc.add_paragraph()
p_inst2.alignment = WD_ALIGN_PARAGRAPH.CENTER
r2 = p_inst2.add_run('IFMT')
r2.bold = True
r2.font.size = Pt(11)
r2.font.color.rgb = AZUL_ESCURO
r2.font.name = 'Calibri'

doc.add_paragraph()
doc.add_paragraph()

p_sys = doc.add_paragraph()
p_sys.alignment = WD_ALIGN_PARAGRAPH.CENTER
rs = p_sys.add_run('SIHCP — Sistema de Histórico e Coleta Patrimonial')
rs.bold = True
rs.font.size = Pt(14)
rs.font.color.rgb = AZUL_MEDIO
rs.font.name = 'Calibri'

doc.add_paragraph()

p_title = doc.add_paragraph()
p_title.alignment = WD_ALIGN_PARAGRAPH.CENTER
rt = p_title.add_run('ESPECIFICAÇÃO DE REQUISITOS FUNCIONAIS\nSERVIDOR MOBILE API')
rt.bold = True
rt.font.size = Pt(22)
rt.font.color.rgb = AZUL_ESCURO
rt.font.name = 'Calibri'

doc.add_paragraph()
doc.add_paragraph()

# Ficha técnica
meta = [
    ('Versão do Documento', '2.0.0'),
    ('Versão do Servidor',  '5.1.0'),
    ('Data',                'Maio de 2026'),
    ('Componente',          'Servidor Mobile API (Spring Boot 3.2.0 + Java 21)'),
    ('Porta Padrão',        '8081'),
    ('Contexto',            '/inventario'),
    ('Total de Requisitos', '105 RFS em 18 módulos'),
    ('Status',              '100% Implementado'),
]
tbl_meta = doc.add_table(rows=len(meta), cols=2)
tbl_meta.style = 'Table Grid'
tbl_meta.alignment = WD_TABLE_ALIGNMENT.CENTER
for i, (k, v) in enumerate(meta):
    bg = 'D6E4F7' if i % 2 == 0 else 'FFFFFF'
    set_cell_bg(tbl_meta.rows[i].cells[0], bg)
    set_cell_bg(tbl_meta.rows[i].cells[1], bg)
    set_cell_text(tbl_meta.rows[i].cells[0], k, bold=True, size=10, color=AZUL_ESCURO)
    set_cell_text(tbl_meta.rows[i].cells[1], v, size=10, color=CINZA_TEXTO)
    tbl_meta.rows[i].cells[0].width = Cm(6)
    tbl_meta.rows[i].cells[1].width = Cm(10)

doc.add_page_break()

# ─────────────────────────────────────────────────────────────────────────────
# 1. INTRODUÇÃO
# ─────────────────────────────────────────────────────────────────────────────
doc.add_heading('1. Introdução', level=1)

doc.add_heading('1.1 Propósito', level=2)
par('Este documento especifica, em caráter formal e institucional, os requisitos funcionais do '
    'Servidor Mobile API do Sistema de Histórico e Coleta Patrimonial (SIHCP), desenvolvido sobre '
    'Spring Boot 3.2.0 e Java 21. O servidor expõe serviços REST consumidos pelo aplicativo Android '
    'e pela própria aplicação desktop, atuando como backend centralizado para todas as operações de '
    'coleta em campo.')
par('Tem por finalidade servir como referência técnica para as equipes de desenvolvimento, manutenção, '
    'integração e auditoria, bem como apoiar processos de homologação, testes automatizados e '
    'documentação de API (Swagger/OpenAPI).')

doc.add_heading('1.2 Escopo', level=2)
par('O escopo contempla exclusivamente o Servidor Mobile API, composto por:')
for item in [
    'Controladores REST (controllers) organizados por domínio funcional;',
    'Serviços de negócio (services);',
    'DTOs (Data Transfer Objects) para comunicação cliente-servidor;',
    'Filtros de segurança (autenticação JWT, RBAC, rate limiting);',
    'Gerenciamento de fotos e armazenamento em sistema de arquivos;',
    'Endpoints de monitoramento e diagnóstico.',
]:
    bullet(item)
par('Não fazem parte deste documento: os requisitos da aplicação desktop (vide '
    'REQUISITOS_FUNCIONAIS_DESKTOP.md) nem os do aplicativo Android. Requisitos não funcionais '
    'do próprio servidor estão especificados em REQUISITOS_NAO_FUNCIONAIS_SERVIDOR_MOBILE.md.')

doc.add_heading('1.3 Definições e Acrônimos', level=2)
simple_table(
    ['Termo / Sigla', 'Definição'],
    [
        ('RFS',          'Requisito Funcional do Servidor mobile'),
        ('REST',         'Representational State Transfer'),
        ('JWT',          'JSON Web Token'),
        ('RBAC',         'Role-Based Access Control'),
        ('DTO',          'Data Transfer Object'),
        ('DAO',          'Data Access Object'),
        ('API',          'Application Programming Interface'),
        ('CRUD',         'Operações de criação, leitura, atualização e exclusão'),
        ('Batch',        'Processamento em lote'),
        ('Delta Sync',   'Sincronização apenas de dados alterados desde a última sincronização'),
        ('Rate Limiting','Limitação de frequência de requisições por origem'),
        ('Multipart',    'Formato de upload de arquivos via HTTP'),
    ],
    [5, 11]
)

doc.add_heading('1.4 Classificação de Prioridades', level=2)
simple_table(
    ['Prioridade', 'Descrição'],
    [
        ('Essencial',  'Requisito indispensável para a operação do ecossistema mobile'),
        ('Importante', 'Requisito necessário para a plena operação, cuja ausência impacta significativamente a usabilidade ou o desempenho'),
        ('Desejável',  'Requisito que agrega valor operacional ou analítico, sem ser crítico'),
    ],
    [3.5, 12.5]
)

doc.add_heading('1.5 Convenções de Identificação', level=2)
par('Cada requisito possui identificador único no formato RFS-XXX, imutável entre versões. '
    'Os endpoints são documentados com caminho relativo ao contexto /api/mobile (o contexto '
    'absoluto inclui /inventario como prefixo servidor).')

# ─────────────────────────────────────────────────────────────────────────────
# 2. VISÃO GERAL
# ─────────────────────────────────────────────────────────────────────────────
doc.add_heading('2. Visão Geral do Servidor Mobile', level=1)
par('O Servidor Mobile API é um componente Spring Boot iniciado a partir da aplicação desktop, '
    'conectado ao mesmo banco PostgreSQL compartilhado. A comunicação com clientes ocorre sobre '
    'HTTPS, com autenticação baseada em tokens JWT e controle de acesso granular por perfil.')
par('Características arquiteturais:')
for item in [
    ('Stateless: ', 'Nenhum estado de sessão é mantido no servidor; toda a informação necessária é fornecida no token JWT;'),
    ('RESTful: ',   'Segue princípios REST com verbos HTTP semânticos e códigos de status padronizados;'),
    ('Versionado: ','Endpoints suportam versionamento para evolução sem quebra de compatibilidade;'),
    ('Paginado: ',  'Todas as listagens expõem paginação obrigatória para evitar sobrecarga de memória;'),
    ('Documentado: ','Geração automática de documentação OpenAPI via Springdoc.'),
]:
    bullet(item[1], bold_prefix=item[0])

# ─────────────────────────────────────────────────────────────────────────────
# 3. ORGANIZAÇÃO DOS ENDPOINTS
# ─────────────────────────────────────────────────────────────────────────────
doc.add_heading('3. Organização dos Endpoints', level=1)
par('Os endpoints são organizados em dois grandes grupos, conforme o requisito de autenticação:')

doc.add_heading('3.1 Endpoints Públicos (sem autenticação)', level=2)
for ep in ['POST /auth/login', 'POST /auth/validate', 'GET /health',
           'GET /campus/info', 'GET /v1/connection/**']:
    bullet(ep)

doc.add_heading('3.2 Endpoints Autenticados (JWT obrigatório)', level=2)
for ep in ['POST /auth/refresh, /logout', 'CRUD /coletas', 'GET /patrimonio/**',
           'GET /salas/**', 'GET /setores/**', 'GET /responsaveis/**',
           'GET /inventario/**', 'GET /dashboard/**', 'GET /descricoes/**',
           'GET /sync/**', 'POST /fotos/upload', 'GET /fotos-referencia/**',
           'GET /relatorios/fotos/**', 'GET /usuarios/**', 'CRUD /dispositivos/**',
           'GET /consulta/**']:
    bullet(ep)

# ─────────────────────────────────────────────────────────────────────────────
# 4. CONTROLE DE ACESSO
# ─────────────────────────────────────────────────────────────────────────────
doc.add_heading('4. Controle de Acesso por Perfil', level=1)
par('O servidor implementa RBAC com quatro níveis hierárquicos. O controle é aplicado tanto no '
    'nível da classe (@RequireXXX) quanto no nível de cada método, conforme a granularidade necessária.')
simple_table(
    ['Perfil', 'Escopo'],
    [
        ('ADMIN',     'Acesso integral, incluindo gestão de usuários, dispositivos e exclusões permanentes'),
        ('SUPERVISOR','Listagens gerenciais, relatórios completos e auditoria de coletas'),
        ('COLETOR',   'Registro de coletas, sincronização de dados e operações de campo'),
        ('CONSULTA',  'Apenas leitura de dashboards, consultas e estatísticas'),
    ],
    [3.5, 12.5]
)
par('A hierarquia é cumulativa: um ADMIN herda todas as permissões dos demais perfis; um SUPERVISOR '
    'herda as permissões de COLETOR e CONSULTA; e assim sucessivamente.')

# ─────────────────────────────────────────────────────────────────────────────
# 5. REQUISITOS FUNCIONAIS POR MÓDULO
# ─────────────────────────────────────────────────────────────────────────────
doc.add_heading('5. Requisitos Funcionais por Módulo', level=1)

HDR_REQ = ['ID', 'Requisito', 'Descrição', 'Endpoint', 'Perfil', 'Prioridade', 'Status']
W_REQ   = [1.5, 3.5, 5.5, 3.5, 2.0, 2.0, 2.0]

# 5.1 Autenticação
doc.add_heading('5.1 Módulo de Autenticação (/api/mobile/auth)', level=2)
req_table(HDR_REQ, [
    ('RFS-001','Login com credenciais','Autenticar usuário e retornar access token e refresh token JWT','POST /login','Público','Essencial','✅'),
    ('RFS-002','Validação de token','Validar assinatura e expiração de token JWT existente','POST /validate','Público','Essencial','✅'),
    ('RFS-003','Logout com invalidação','Invalidar token ativo inserindo-o em blacklist até expiração natural','POST /logout','Autenticado','Essencial','✅'),
    ('RFS-004','Renovação de access token','Gerar novo access token a partir de refresh token válido','POST /refresh','Autenticado','Essencial','✅'),
    ('RFS-005','Verificação de saúde','Expor endpoint leve para verificação de disponibilidade','GET /health','Público','Essencial','✅'),
    ('RFS-006','Retornar inventário ativo no login','Incluir na resposta de login o inventário atualmente ativo','POST /login (resp.)','Público','Essencial','✅'),
    ('RFS-007','Registrar dispositivo no login','Registrar o deviceId do cliente no ato da autenticação','POST /login','Público','Importante','✅'),
    ('RFS-008','Rate limiting no login','Limitar tentativas de login a 10 por minuto, por IP','POST /login','Público','Essencial','✅'),
    ('RFS-009','Alerta de força bruta','Gerar alerta quando 3+ bloqueios ocorrerem em 5 minutos','POST /login','Público','Essencial','✅'),
], W_REQ)
criteria_box([
    'Tokens JWT devem ter prazo configurável (padrão: 2 dias para access, 14 dias para refresh);',
    'Em caso de credenciais inválidas, a resposta deve ser genérica, sem informar se o usuário existe;',
    'Tokens revogados por logout não devem ser aceitos mesmo se estiverem dentro do prazo de validade.',
])

# 5.2 Coletas
doc.add_heading('5.2 Módulo de Coletas (/api/mobile/coletas)', level=2)
req_table(HDR_REQ, [
    ('RFS-010','Registrar coleta individual','Registrar coleta unitária com validação de integridade','POST /','COLETOR','Essencial','✅'),
    ('RFS-011','Registrar coletas em lote','Registrar múltiplas coletas em uma única requisição (batch)','POST /batch','COLETOR','Essencial','✅'),
    ('RFS-012','Verificar duplicata','Informar se patrimônio já foi coletado no inventário ativo','POST /verificar-duplicata','COLETOR','Essencial','✅'),
    ('RFS-013','Listar coletas paginado','Expor listagem paginada de coletas','GET /?page=&size=','CONSULTA','Essencial','✅'),
    ('RFS-014','Buscar coleta por ID','Retornar dados completos de uma coleta específica','GET /{id}','CONSULTA','Essencial','✅'),
    ('RFS-015','Listar coletas pendentes','Retornar patrimônios do escopo ainda não coletados','GET /pendentes','COLETOR','Essencial','✅'),
    ('RFS-016','Histórico de coletas','Retornar histórico completo, opcionalmente filtrado por usuário','GET /historico','CONSULTA','Importante','✅'),
    ('RFS-017','Atualizar coleta','Atualizar campos de uma coleta (observações, estado, localização)','PUT /{id}','COLETOR','Importante','✅'),
    ('RFS-018','Excluir coleta','Exclusão lógica de coleta (apenas ADMIN)','DELETE /{id}','ADMIN','Importante','✅'),
    ('RFS-019','Listar descrições pendentes','Retornar descrições únicas de patrimônios ainda não coletados','GET /descricoes-pendentes','COLETOR','Importante','✅'),
    ('RFS-020','Sincronização incremental','Retornar apenas coletas modificadas desde um timestamp','GET /incremental','COLETOR','Importante','✅'),
    ('RFS-021','Listar salas com coletas','Retornar salas que possuem coletas registradas','GET /salas-com-coletas','CONSULTA','Desejável','✅'),
    ('RFS-022','Listar todas (deprecated)','Manter endpoint de compatibilidade sem paginação','GET /all','CONSULTA','Desejável','✅ (deprecated)'),
    ('RFS-023','Validar descrição sem etiqueta','Validar comprimento (3 a 255 chars) da descrição','POST /, POST /batch','COLETOR','Essencial','✅'),
    ('RFS-024','Resultado estruturado do batch','Retornar por item: índice, status e ID da coleta gerada','POST /batch','COLETOR','Essencial','✅'),
], W_REQ)
criteria_box([
    'O batch deve suportar até 500 coletas por requisição;',
    'A resposta do batch deve ser estruturada permitindo retomada parcial em caso de falha de alguns itens;',
    'Coletas duplicadas no mesmo inventário devem ser rejeitadas com mensagem clara;',
    'O sistema deve registrar automaticamente divergência quando a sala encontrada diverge da cadastrada.',
])

# 5.3 Patrimônios
doc.add_heading('5.3 Módulo de Patrimônios (/api/mobile/patrimonio)', level=2)
req_table(HDR_REQ, [
    ('RFS-025','Listar patrimônios paginado','Expor listagem paginada de patrimônios','GET /?page=&size=','CONSULTA','Essencial','✅'),
    ('RFS-026','Listar com metadados','Listagem com metadados (PagedResponse) para UIs otimizadas','GET /paged','CONSULTA','Importante','✅'),
    ('RFS-027','Buscar por ID','Retornar dados do patrimônio pelo ID interno','GET /{id}','CONSULTA','Essencial','✅'),
    ('RFS-028','Buscar por número de tombamento','Localizar patrimônio pelo número de tombamento','GET /numero/{numero}','CONSULTA','Essencial','✅'),
    ('RFS-029','Buscar por QR Code','Decodificar conteúdo de QR Code e localizar patrimônio','GET /qr/{qrCode}','CONSULTA','Essencial','✅'),
    ('RFS-030','Listar por sala','Retornar paginado os patrimônios de uma sala com filtro por status','GET /sala/{salaId}','CONSULTA','Essencial','✅'),
    ('RFS-031','Listar por setor','Retornar patrimônios de um setor','GET /setor/{setorId}','CONSULTA','Essencial','✅'),
    ('RFS-032','Listar por responsável','Retornar patrimônios sob responsabilidade de um servidor','GET /responsavel/{id}','CONSULTA','Importante','✅'),
    ('RFS-033','Contar por responsável','Retornar contagem total de patrimônios de um responsável','GET /responsavel/{id}/count','CONSULTA','Importante','✅'),
    ('RFS-034','Busca textual rápida','Busca por texto livre com possibilidade de filtro adicional','GET /buscar?query=&filtro=','CONSULTA','Importante','✅'),
    ('RFS-035','Verificar status de coleta','Informar se patrimônio foi coletado no inventário ativo','GET /numero/{n}/coletado','CONSULTA','Importante','✅'),
    ('RFS-036','Validar para coleta','Validar se patrimônio existe, está ativo e pode ser coletado','GET /numero/{n}/validar','CONSULTA','Essencial','✅'),
    ('RFS-037','Buscar por descrição não coletados','Retornar patrimônios cuja descrição corresponde e não foram coletados','GET /descricao/{desc}','CONSULTA','Importante','✅'),
], W_REQ)
criteria_box([
    'Buscas textuais devem ser insensíveis a acentuação e maiúsculas/minúsculas;',
    'A resposta deve seguir o contrato ApiResponse<T> padronizado;',
    'Consultas com mais de 100 itens devem forçar paginação obrigatória.',
])

# 5.4 Inventário
doc.add_heading('5.4 Módulo de Inventário (/api/mobile/inventario)', level=2)
req_table(HDR_REQ, [
    ('RFS-038','Buscar inventário ativo','Retornar o inventário em andamento','GET /ativo','CONSULTA','Essencial','✅'),
    ('RFS-039','Buscar por ID','Retornar dados completos de um inventário','GET /{id}','CONSULTA','Essencial','✅'),
    ('RFS-040','Listar todos os inventários','Retornar relação histórica de inventários','GET /','SUPERVISOR','Importante','✅'),
    ('RFS-041','Estatísticas do inventário','Retornar estatísticas detalhadas (total, coletados, pendentes, %)','GET /{id}/estatisticas','CONSULTA','Essencial','✅'),
], W_REQ)
criteria_box([
    'As estatísticas devem ser calculadas em tempo real, sem cache persistente;',
    'Somente um inventário por vez pode estar em status EM_ANDAMENTO.',
])

# 5.5 Dashboard
doc.add_heading('5.5 Módulo de Dashboard (/api/mobile/dashboard)', level=2)
req_table(HDR_REQ, [
    ('RFS-042','Estatísticas gerais (KPIs)','Expor KPIs consolidados do inventário ativo','GET /stats','CONSULTA','Essencial','✅'),
    ('RFS-043','Evolução temporal','Retornar série temporal das coletas para gráfico de linhas','GET /evolucao','CONSULTA','Importante','✅'),
    ('RFS-044','Top itens coletados','Retornar patrimônios mais coletados para gráfico de barras','GET /top-itens','CONSULTA','Desejável','✅'),
    ('RFS-045','Estatísticas por status','Retornar distribuição por status de coleta para gráfico de pizza','GET /status','CONSULTA','Importante','✅'),
    ('RFS-046','Distribuição por sala','Retornar distribuição de coletas por sala para gráfico de barras','GET /distribuicao-sala','CONSULTA','Importante','✅'),
], W_REQ)
criteria_box([
    'Todas as respostas devem ser compatíveis com bibliotecas de gráficos do Android (MPAndroidChart);',
    'O tempo de resposta dos endpoints de dashboard deve ser inferior a 1 segundo (P95).',
])

# 5.6 Sincronização
doc.add_heading('5.6 Módulo de Sincronização (/api/mobile/sync)', level=2)
req_table(HDR_REQ, [
    ('RFS-047','Sincronizar patrimônios','Fornecer patrimônios paginados para cache local do aplicativo','GET /patrimonios?page=&size=','COLETOR','Essencial','✅'),
    ('RFS-048','Sincronizar salas','Fornecer todas as salas para o cache local','GET /salas','COLETOR','Essencial','✅'),
    ('RFS-049','Estatísticas de sincronização','Retornar contadores para validação de integridade pós-sync','GET /stats','COLETOR','Importante','✅'),
    ('RFS-050','Dados offline completos','Fornecer em uma requisição todos os dados para operação offline','GET /offline-data','COLETOR','Essencial','✅'),
    ('RFS-051','Patrimônios paginados offline','Fornecer patrimônios em páginas otimizadas para cache offline','GET /offline-data/patrimonios','COLETOR','Importante','✅'),
    ('RFS-052','Status de sincronização','Retornar apenas contadores para verificação rápida de status','GET /status','COLETOR','Desejável','✅'),
], W_REQ)
criteria_box([
    'A sincronização completa inicial deve concluir em menos de 30 segundos para até 5.000 patrimônios;',
    'Os endpoints de delta sync devem reduzir em ao menos 80% o volume de dados em relação à sincronização completa.',
])

# 5.7 Salas
doc.add_heading('5.7 Módulo de Salas (/api/mobile/salas)', level=2)
req_table(HDR_REQ, [
    ('RFS-053','Listar todas as salas','Retornar relação completa de salas','GET /','CONSULTA','Essencial','✅'),
    ('RFS-054','Sincronização incremental','Retornar somente salas alteradas desde o último sync','GET /sync?lastSync=','CONSULTA','Importante','✅'),
    ('RFS-055','Listar com progresso','Retornar salas com percentual de coletas concluídas','GET /com-progresso','CONSULTA','Importante','✅'),
    ('RFS-056','Buscar por ID','Retornar dados de uma sala específica','GET /{id}','CONSULTA','Essencial','✅'),
], W_REQ)

# 5.8 Descrições
doc.add_heading('5.8 Módulo de Descrições (/api/mobile/descricoes)', level=2)
req_table(HDR_REQ, [
    ('RFS-057','Listar descrições agrupadas','Retornar descrições únicas de patrimônios com contagem','GET /','COLETOR','Importante','✅'),
    ('RFS-058','Buscar por termo','Retornar descrições compatíveis com o termo informado','GET /buscar?termo=','COLETOR','Importante','✅'),
    ('RFS-059','Listar não coletadas','Retornar descrições ainda pendentes no inventário ativo','GET /nao-coletadas','COLETOR','Essencial','✅'),
    ('RFS-060','Sugestões paginadas','Prover autocomplete paginado de descrições','GET /sugestoes?q=&page=&size=','COLETOR','Desejável','✅'),
], W_REQ)

# 5.9 Fotos de Coleta
doc.add_heading('5.9 Módulo de Fotos de Coleta (/api/mobile/fotos)', level=2)
req_table(HDR_REQ, [
    ('RFS-061','Upload de foto (multipart)','Receber fotos de coleta via upload multipart','POST /upload','COLETOR','Essencial','✅'),
    ('RFS-062','Download de foto','Permitir download da foto associada a uma coleta','GET /{coletaId}','CONSULTA','Importante','✅'),
    ('RFS-063','Remover foto','Permitir remoção lógica e física da foto associada','DELETE /{coletaId}','COLETOR','Importante','✅'),
    ('RFS-064','Organização hierárquica','Armazenar fotos em estrutura {inventario}/{ano-mês}/{tipo}/','POST /upload','COLETOR','Importante','✅'),
], W_REQ)
criteria_box([
    'Tamanho máximo por foto: 5 MB;',
    'Formatos aceitos: JPEG e PNG;',
    'A remoção deve apagar o arquivo físico e atualizar a referência no banco.',
])

# 5.10 Fotos de Referência
doc.add_heading('5.10 Módulo de Fotos de Referência (/api/mobile/fotos-referencia)', level=2)
par('Fotos de referência são associadas a descrições de patrimônios, auxiliando o coletor na '
    'identificação visual durante a coleta em campo.')
req_table(HDR_REQ, [
    ('RFS-065','Listar com delta sync','Retornar apenas fotos atualizadas desde um timestamp','GET /?ultimaAtualizacao=','COLETOR','Importante','✅'),
    ('RFS-066','Buscar por ID','Retornar a foto pelo ID interno','GET /{id}','CONSULTA','Importante','✅'),
    ('RFS-067','Buscar por descrição','Retornar fotos associadas a uma descrição','GET /descricao?q=','CONSULTA','Importante','✅'),
    ('RFS-068','Buscar múltiplas (batch)','Requisição de múltiplas fotos em uma única chamada','POST /batch','COLETOR','Desejável','✅'),
    ('RFS-069','Verificar atualizações','Informar se há fotos novas desde determinado timestamp','GET /check-updates','COLETOR','Importante','✅'),
    ('RFS-070','Estatísticas de fotos','Retornar total e tamanho ocupado por fotos de referência','GET /stats','CONSULTA','Desejável','✅'),
    ('RFS-071','Listar metadados','Retornar metadados sem conteúdo binário para economizar banda','GET /metadata','COLETOR','Importante','✅'),
], W_REQ)

# 5.11 Relatórios
doc.add_heading('5.11 Módulo de Relatórios (/api/mobile/relatorios/fotos)', level=2)
req_table(HDR_REQ, [
    ('RFS-072','Gerar relatório fotográfico em PDF','Gerar PDF consolidado com fotos de coletas de um inventário','GET /{inventarioId}','SUPERVISOR','Importante','✅'),
    ('RFS-073','Informações do relatório','Retornar contagem de fotos por tipo antes da geração','GET /{inventarioId}/info','CONSULTA','Importante','✅'),
], W_REQ)
criteria_box([
    'O PDF deve incluir, para cada foto, metadados de coleta (patrimônio, coletor, data, localização);',
    'A geração deve concluir em menos de 60 segundos para inventários com até 500 fotos.',
])

# 5.12 Usuários
doc.add_heading('5.12 Módulo de Usuários (/api/mobile/usuarios)', level=2)
req_table(HDR_REQ, [
    ('RFS-074','Listar todos os usuários','Retornar relação completa de usuários cadastrados','GET /','ADMIN','Importante','✅'),
    ('RFS-075','Buscar por ID','Retornar dados de um usuário','GET /{id}','ADMIN','Importante','✅'),
    ('RFS-076','Buscar por login','Permitir consulta por login (único)','GET /login/{login}','ADMIN','Importante','✅'),
    ('RFS-077','Obter perfil do usuário autenticado','Expor endpoint para o próprio usuário consultar seus dados','GET /me','CONSULTA','Essencial','✅'),
], W_REQ)
criteria_box([
    'Em nenhuma resposta a senha (mesmo o hash) deve ser retornada;',
    'O endpoint /me não deve requerer perfil específico além de autenticação válida.',
])

# 5.13 Dispositivos
doc.add_heading('5.13 Módulo de Dispositivos (/api/mobile/dispositivos)', level=2)
par('O controle de dispositivos autorizados é fundamental para evitar uso não autorizado do '
    'aplicativo em aparelhos não homologados pela instituição.')
req_table(HDR_REQ, [
    ('RFS-078','Registrar dispositivo','Registrar novo dispositivo mediante solicitação autenticada','POST /registrar','Autenticado','Essencial','✅'),
    ('RFS-079','Verificar status','Informar estado atual do dispositivo (pendente, aprovado, bloqueado)','GET /status/{deviceId}','Autenticado','Essencial','✅'),
    ('RFS-080','Verificar autorização','Informar se o dispositivo pode operar','GET /autorizado/{deviceId}','Autenticado','Essencial','✅'),
    ('RFS-081','Listar todos','Retornar relação de todos os dispositivos registrados','GET /','ADMIN','Importante','✅'),
    ('RFS-082','Listar pendentes','Retornar dispositivos aguardando aprovação','GET /pendentes','ADMIN','Importante','✅'),
    ('RFS-083','Aprovar dispositivo','Permitir aprovação por ADMIN','PUT /{id}/aprovar','ADMIN','Importante','✅'),
    ('RFS-084','Bloquear dispositivo','Permitir o bloqueio de dispositivo','PUT /{id}/bloquear','ADMIN','Importante','✅'),
    ('RFS-085','Desbloquear dispositivo','Permitir o desbloqueio de dispositivo','PUT /{id}/desbloquear','ADMIN','Importante','✅'),
    ('RFS-086','Remover dispositivo','Permitir exclusão permanente de registro de dispositivo','DELETE /{id}','ADMIN','Importante','✅'),
], W_REQ)
criteria_box([
    'Dispositivos bloqueados não devem poder autenticar-se, mesmo com credenciais válidas;',
    'A aprovação deve registrar o ADMIN responsável e o timestamp para auditoria.',
])

# 5.14 Consulta Avançada
doc.add_heading('5.14 Módulo de Consulta Avançada (/api/mobile/consulta)', level=2)
req_table(HDR_REQ, [
    ('RFS-087','Buscar por código parcial','Retornar patrimônios cujo número contém o termo','GET /buscar-por-codigo','Autenticado','Importante','✅'),
    ('RFS-088','Buscar por descrição','Retornar patrimônios cuja descrição contém o termo','GET /buscar-por-descricao','Autenticado','Importante','✅'),
    ('RFS-089','Detalhes completos','Retornar dados consolidados incluindo coletas, fotos e histórico','GET /patrimonio/{id}/detalhes','Autenticado','Importante','✅'),
    ('RFS-090','Busca avançada multi-critério','Permitir combinação de múltiplos filtros','GET /buscar-avancada','Autenticado','Importante','✅'),
], W_REQ)

# 5.15 Monitoramento
doc.add_heading('5.15 Módulo de Monitoramento (/api/mobile/health)', level=2)
req_table(HDR_REQ, [
    ('RFS-091','Status geral','Expor status consolidado (banco de dados e memória)','GET /','Público','Essencial','✅'),
    ('RFS-092','Status do pool de conexões','Informar métricas do HikariCP (ativas, ociosas, total)','GET /pool','Público','Importante','✅'),
    ('RFS-093','Status de memória da JVM','Informar uso de memória (heap e non-heap)','GET /memory','Público','Importante','✅'),
    ('RFS-094','Forçar garbage collection','Permitir trigger manual do GC para fins de diagnóstico','POST /gc','Público','Desejável','✅'),
], W_REQ)
criteria_box([
    'Os endpoints de monitoramento não devem requerer autenticação, para permitir uso por sistemas externos (Prometheus, Zabbix);',
    'O trigger manual de GC não deve estar acessível em produção se a política de segurança institucional assim exigir.',
])

# 5.16 Conexões
doc.add_heading('5.16 Módulo de Conexões (/api/mobile/v1/connection)', level=2)
req_table(HDR_REQ, [
    ('RFS-095','Listar conexões ativas','Retornar dispositivos atualmente conectados','GET /active','Público','Importante','✅'),
    ('RFS-096','Estatísticas de conexões','Retornar métricas agregadas (total, por tipo, por perfil)','GET /stats','Público','Importante','✅'),
    ('RFS-097','Informação de dispositivo','Retornar estado de um dispositivo específico','GET /{deviceId}','Público','Importante','✅'),
    ('RFS-098','Desconectar dispositivo','Permitir desconexão forçada de um dispositivo','DELETE /{deviceId}','Público','Importante','✅'),
    ('RFS-099','Limpeza de conexões inativas','Remover conexões ociosas além do prazo configurado','POST /cleanup','Público','Importante','✅'),
], W_REQ)

# 5.17 Campus
doc.add_heading('5.17 Módulo de Campus (/api/mobile/campus)', level=2)
req_table(HDR_REQ, [
    ('RFS-100','Informações públicas do campus','Retornar dados básicos do campus para a tela inicial do aplicativo','GET /info','Público','Importante','✅'),
], W_REQ)

# 5.18 Setores e Responsáveis
doc.add_heading('5.18 Módulo de Setores e Responsáveis', level=2)
req_table(HDR_REQ, [
    ('RFS-101','Listar setores','Retornar relação completa de setores','GET /setores','CONSULTA','Essencial','✅'),
    ('RFS-102','Buscar setor por ID','Retornar dados de um setor','GET /setores/{id}','CONSULTA','Essencial','✅'),
    ('RFS-103','Listar setores por campus','Retornar setores vinculados a um campus','GET /setores/campus/{id}','CONSULTA','Importante','✅'),
    ('RFS-104','Listar responsáveis','Retornar relação completa de responsáveis','GET /responsaveis','CONSULTA','Essencial','✅'),
    ('RFS-105','Buscar responsável por ID','Retornar dados de um responsável','GET /responsaveis/{id}','CONSULTA','Essencial','✅'),
], W_REQ)

# ─────────────────────────────────────────────────────────────────────────────
# 6. CENÁRIOS DE VALIDAÇÃO
# ─────────────────────────────────────────────────────────────────────────────
doc.add_heading('6. Cenários de Validação', level=1)

doc.add_heading('6.1 Cenário: Autenticação e Emissão de Token', level=2)
par('Pré-condições: Usuário cadastrado e ativo no banco.', bold=True)
par('Fluxo principal:')
for step in [
    'Aplicativo envia POST /api/mobile/auth/login com credenciais (RFS-001);',
    'Servidor valida credenciais contra o hash BCrypt;',
    'Servidor verifica o rate limit (RFS-008);',
    'Servidor registra o dispositivo (RFS-007);',
    'Servidor gera access token (2 dias) e refresh token (14 dias);',
    'Servidor retorna tokens, dados do usuário e o inventário ativo (RFS-006);',
    'Aplicativo armazena tokens no EncryptedSharedPreferences.',
]:
    numbered(step)
par('Critério de sucesso: Tokens válidos emitidos; inventário ativo retornado na resposta.', italic=True)
par('Fluxo alternativo A1 (credenciais inválidas): Servidor retorna HTTP 401 com mensagem genérica.')
par('Fluxo alternativo A2 (rate limit excedido): Servidor retorna HTTP 429 e registra evento de auditoria (RFS-009).')

doc.add_heading('6.2 Cenário: Sincronização Offline Completa', level=2)
par('Pré-condições: Usuário autenticado; aplicativo em primeira execução ou após reset do cache.', bold=True)
par('Fluxo principal:')
for step in [
    'Aplicativo chama GET /api/mobile/sync/offline-data (RFS-050);',
    'Servidor consulta todos os patrimônios, salas, setores, responsáveis e descrições;',
    'Servidor retorna resposta consolidada em até 30 segundos;',
    'Aplicativo persiste os dados no Room Database local;',
    'Aplicativo registra o timestamp da sincronização.',
]:
    numbered(step)
par('Critério de sucesso: Todos os dados armazenados localmente com integridade referencial.', italic=True)

doc.add_heading('6.3 Cenário: Registro de Coleta em Lote', level=2)
par('Pré-condições: Aplicativo com coletas pendentes acumuladas durante período offline.', bold=True)
par('Fluxo principal:')
for step in [
    'Ao detectar conexão, o SyncWorker agrupa coletas em lote;',
    'Aplicativo envia POST /api/mobile/coletas/batch (RFS-011);',
    'Servidor valida cada coleta individualmente (patrimônio existe, inventário ativo, não duplicada);',
    'Servidor processa o batch em transação única quando possível;',
    'Servidor retorna resultado estruturado por índice (RFS-024);',
    'Aplicativo marca localmente as coletas bem-sucedidas como sincronizadas.',
]:
    numbered(step)
par('Critério de sucesso: Coletas válidas registradas; falhas parciais identificadas por índice; nenhuma duplicação.', italic=True)

doc.add_heading('6.4 Cenário: Upload de Foto de Coleta', level=2)
par('Pré-condições: Coleta previamente registrada; aplicativo com foto capturada.', bold=True)
par('Fluxo principal:')
for step in [
    'PhotoSyncWorker envia POST /api/mobile/fotos/upload (multipart) (RFS-061);',
    'Servidor valida tamanho (máximo 5 MB) e formato (JPEG/PNG);',
    'Servidor armazena a foto em data/fotos/inventario_{id}/{YYYY-MM}/{tipo}/;',
    'Servidor atualiza referência (FOTO_PATH) no registro de coleta;',
    'Servidor retorna HTTP 200 com identificador da foto.',
]:
    numbered(step)
par('Critério de sucesso: Foto armazenada fisicamente com referência íntegra no banco; aplicativo marca a foto como sincronizada.', italic=True)

# ─────────────────────────────────────────────────────────────────────────────
# 7. RISCOS E MITIGAÇÕES
# ─────────────────────────────────────────────────────────────────────────────
doc.add_heading('7. Riscos e Mitigações', level=1)
simple_table(
    ['Risco', 'Impacto', 'Probabilidade', 'Mitigação'],
    [
        ('Vazamento de tokens JWT','Alto','Baixa','Token com prazo curto; blacklist no logout; HTTPS obrigatório'),
        ('Ataque de força bruta no login','Alto','Alta','Rate limiting (10 req/min); alerta após 3 bloqueios; bloqueio após 5 tentativas'),
        ('Sobrecarga em sincronização em massa','Alto','Média','Paginação obrigatória; endpoint dedicado /offline-data; compressão GZIP'),
        ('Incompatibilidade entre versões','Médio','Média','Campos opcionais e defaults; documentação OpenAPI; testes de retrocompatibilidade'),
        ('Armazenamento de fotos esgotando disco','Alto','Média','Organização hierárquica; limpeza automática de órfãos; limite configurável'),
        ('Falha na blacklist de tokens','Médio','Baixa','Persistência em ConcurrentHashMap com TTL; monitoramento contínuo'),
        ('Requisição com payload excessivo','Médio','Média','Limite configurável em spring.servlet.multipart.max-file-size e em batches'),
        ('Dispositivo comprometido enviando dados falsos','Alto','Baixa','Controle de aprovação de dispositivos (RFS-078 a RFS-086); auditoria de coletas'),
        ('Endpoints de monitoramento explorados','Médio','Média','Restrição de acesso por IP na rede corporativa; não expor /health/gc em produção'),
    ],
    [4.5, 1.8, 2.2, 7.5]
)

# ─────────────────────────────────────────────────────────────────────────────
# 8. MATRIZ DE RASTREABILIDADE
# ─────────────────────────────────────────────────────────────────────────────
doc.add_heading('8. Matriz de Rastreabilidade', level=1)

doc.add_heading('8.1 Distribuição por Módulo', level=2)
simple_table(
    ['Módulo', 'Intervalo de IDs', 'Quantidade'],
    [
        ('Autenticação',         'RFS-001 a RFS-009',  '9'),
        ('Coletas',              'RFS-010 a RFS-024', '15'),
        ('Patrimônios',          'RFS-025 a RFS-037', '13'),
        ('Inventário',           'RFS-038 a RFS-041',  '4'),
        ('Dashboard',            'RFS-042 a RFS-046',  '5'),
        ('Sincronização',        'RFS-047 a RFS-052',  '6'),
        ('Salas',                'RFS-053 a RFS-056',  '4'),
        ('Descrições',           'RFS-057 a RFS-060',  '4'),
        ('Fotos de Coleta',      'RFS-061 a RFS-064',  '4'),
        ('Fotos de Referência',  'RFS-065 a RFS-071',  '7'),
        ('Relatórios',           'RFS-072 a RFS-073',  '2'),
        ('Usuários',             'RFS-074 a RFS-077',  '4'),
        ('Dispositivos',         'RFS-078 a RFS-086',  '9'),
        ('Consulta Avançada',    'RFS-087 a RFS-090',  '4'),
        ('Monitoramento',        'RFS-091 a RFS-094',  '4'),
        ('Conexões',             'RFS-095 a RFS-099',  '5'),
        ('Campus',               'RFS-100',             '1'),
        ('Setores e Responsáveis','RFS-101 a RFS-105', '5'),
        ('TOTAL',                '',                  '105'),
    ],
    [6, 5, 2.5]
)

doc.add_heading('8.2 Cobertura de Implementação', level=2)
simple_table(
    ['Situação', 'Quantidade', 'Percentual'],
    [
        ('Implementado',      '105', '100%'),
        ('Em desenvolvimento', '0',   '0%'),
        ('Planejado',          '0',   '0%'),
    ],
    [6, 3, 3]
)

doc.add_heading('8.3 Distribuição por Prioridade', level=2)
simple_table(
    ['Prioridade', 'Quantidade', 'Percentual'],
    [
        ('Essencial',  '45', '43%'),
        ('Importante', '52', '49%'),
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
        ('Access Token',   'Token de curta duração usado para autorizar requisições à API'),
        ('Refresh Token',  'Token de longa duração usado para obter novos access tokens'),
        ('Blacklist',      'Lista de tokens invalidados antes do prazo de expiração natural'),
        ('Endpoint',       'Recurso REST acessível por URL específica'),
        ('Payload',        'Conteúdo enviado ou recebido no corpo de uma requisição HTTP'),
        ('Rate Limiting',  'Técnica que limita a frequência de requisições de uma origem'),
        ('Batch',          'Conjunto de operações enviadas em uma única requisição'),
        ('Delta Sync',     'Sincronização apenas de dados alterados'),
        ('Multipart',      'Formato HTTP para envio de arquivos'),
        ('Stateless',      'Arquitetura sem manutenção de estado entre requisições'),
        ('Pool',           'Conjunto pré-criado e reutilizável de recursos (conexões, threads)'),
        ('Token Bucket',   'Algoritmo clássico de rate limiting baseado em fichas'),
    ],
    [4, 12]
)

# ─────────────────────────────────────────────────────────────────────────────
# APÊNDICE B — HISTÓRICO DE REVISÕES
# ─────────────────────────────────────────────────────────────────────────────
doc.add_heading('Apêndice B — Histórico de Revisões', level=1)
simple_table(
    ['Versão', 'Data', 'Autor', 'Descrição'],
    [
        ('1.0.0','Mai/2026','Equipe IFMT','Versão inicial com 105 requisitos em 18 módulos'),
        ('2.0.0','Mai/2026','Equipe IFMT','Reescrita com formalidade institucional: introdução, propósito e escopo, prioridades explícitas, critérios de aceitação mensuráveis, referências cruzadas, cenários de validação, riscos e mitigações, glossário formal'),
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
        ('REQUISITOS_FUNCIONAIS_NAO_FUNCIONAIS.md',      'Especificação consolidada do ecossistema SIHCP'),
        ('REQUISITOS_FUNCIONAIS_DESKTOP.md',             'Requisitos funcionais da aplicação desktop'),
        ('REQUISITOS_NAO_FUNCIONAIS_DESKTOP.md',         'Requisitos não funcionais da aplicação desktop'),
        ('REQUISITOS_NAO_FUNCIONAIS_SERVIDOR_MOBILE.md', 'Requisitos não funcionais da API mobile'),
        ('ADEQUACAO_NORMAS_FEDERAIS.md',                 'Conformidade com a legislação federal aplicável'),
    ],
    [7, 9]
)

doc.add_heading('C.2 Mapeamento com Requisitos do Documento Integrado', level=2)
simple_table(
    ['RFS Servidor', 'RF Integrado Correspondente'],
    [
        ('RFS-001 a RFS-009', 'RF001 a RF009'),
        ('RFS-010 a RFS-024', 'RF031 a RF047, RF092 a RF094'),
        ('RFS-025 a RFS-037', 'RF011 a RF022, RF092, RF093'),
        ('RFS-038 a RFS-041', 'RF023 a RF028'),
        ('RFS-042 a RFS-046', 'RF070 a RF077'),
        ('RFS-047 a RFS-052', 'RF048 a RF058'),
        ('RFS-053 a RFS-056', 'RF078'),
        ('RFS-057 a RFS-060', 'RF093, RF094'),
        ('RFS-061 a RFS-064', 'RF038, RF039'),
        ('RFS-065 a RFS-071', 'RF095'),
        ('RFS-072 a RFS-073', 'RF096'),
        ('RFS-074 a RFS-077', 'RF081'),
        ('RFS-101 a RFS-105', 'RF079, RF080'),
    ],
    [5, 11]
)

# ─────────────────────────────────────────────────────────────────────────────
# RODAPÉ FINAL
# ─────────────────────────────────────────────────────────────────────────────
doc.add_paragraph()
section_divider()
p_fim = doc.add_paragraph()
p_fim.alignment = WD_ALIGN_PARAGRAPH.CENTER
r_fim = p_fim.add_run(
    'Versão do documento: 2.0.0  |  Versão do servidor: 5.1.0  |  Maio de 2026\n'
    'Instituto Federal de Educação, Ciência e Tecnologia de Mato Grosso (IFMT)'
)
r_fim.font.size = Pt(9)
r_fim.font.color.rgb = RGBColor(0x88, 0x88, 0x88)
r_fim.font.italic = True

# ─────────────────────────────────────────────────────────────────────────────
# SALVAR
# ─────────────────────────────────────────────────────────────────────────────
caminho = (r'c:\Users\Romulo\Documents\PROJETOS\MASTER_INVENTARIO'
           r'\docs\arquitetura\REQUISITOS_FUNCIONAIS_SERVIDOR_MOBILE.docx')
doc.save(caminho)
print(f'✅ Documento salvo em:\n   {caminho}')
