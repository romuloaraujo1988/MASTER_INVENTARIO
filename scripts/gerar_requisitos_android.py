"""
Gerador do documento DOCX: Requisitos Funcionais — Aplicativo Android
Fonte: REQUISITOS_FUNCIONAIS_NAO_FUNCIONAIS.md (v3.0.0)
"""

from docx import Document
from docx.shared import Pt, RGBColor, Cm
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT
from docx.oxml.ns import qn
from docx.oxml import OxmlElement

# ─────────────────────────────────────────────────────────────────────────────
# PALETA
# ─────────────────────────────────────────────────────────────────────────────
VERDE_ESCURO  = RGBColor(0x1B, 0x5E, 0x20)
VERDE_MEDIO   = RGBColor(0x2E, 0x7D, 0x32)
VERDE_CLARO   = RGBColor(0xC8, 0xE6, 0xC9)
CINZA_TEXTO   = RGBColor(0x44, 0x44, 0x44)
BRANCO        = RGBColor(0xFF, 0xFF, 0xFF)
PRETO         = RGBColor(0x00, 0x00, 0x00)
VERDE_STATUS  = RGBColor(0x1E, 0x7E, 0x34)
AZUL_LINK     = RGBColor(0x1A, 0x37, 0x6C)

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

normal = styles['Normal']
normal.font.name = 'Calibri'; normal.font.size = Pt(10)
normal.font.color.rgb = CINZA_TEXTO; normal.paragraph_format.space_after = Pt(4)

h1 = styles['Heading 1']
h1.font.name = 'Calibri'; h1.font.size = Pt(16); h1.font.bold = True
h1.font.color.rgb = VERDE_ESCURO
h1.paragraph_format.space_before = Pt(18); h1.paragraph_format.space_after = Pt(6)

h2 = styles['Heading 2']
h2.font.name = 'Calibri'; h2.font.size = Pt(13); h2.font.bold = True
h2.font.color.rgb = VERDE_MEDIO
h2.paragraph_format.space_before = Pt(14); h2.paragraph_format.space_after = Pt(4)

h3 = styles['Heading 3']
h3.font.name = 'Calibri'; h3.font.size = Pt(11); h3.font.bold = True
h3.font.color.rgb = VERDE_MEDIO
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
    run.bold = bold; run.italic = italic; run.font.name = 'Calibri'
    if size:  run.font.size = Pt(size)
    if color: run.font.color.rgb = color
    return p


def bullet(text, bold_prefix=None):
    p = doc.add_paragraph(style='List Bullet')
    p.paragraph_format.left_indent  = Pt(18)
    p.paragraph_format.space_after  = Pt(2)
    p.paragraph_format.space_before = Pt(1)
    if bold_prefix:
        r1 = p.add_run(bold_prefix); r1.bold = True; r1.font.color.rgb = VERDE_ESCURO
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


def add_hdr(table, headers, bg='1B5E20'):
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
        bg  = 'F1F8E9' if r_idx % 2 == 1 else 'FFFFFF'
        for c_idx, cell_text in enumerate(row_data):
            set_cell_bg(row.cells[c_idx], bg)
            bold  = c_idx == 0
            color = VERDE_STATUS if '✅' in str(cell_text) else CINZA_TEXTO
            if c_idx == 0: color = VERDE_ESCURO
            set_cell_text(row.cells[c_idx], str(cell_text),
                          bold=bold, size=8.5, color=color)
    doc.add_paragraph()
    return table


def simple_table(headers, rows, col_widths):
    table = doc.add_table(rows=1 + len(rows), cols=len(headers))
    table.style = 'Table Grid'
    table.alignment = WD_TABLE_ALIGNMENT.CENTER
    set_col_widths(table, col_widths)
    add_hdr(table, headers, bg='2E7D32')
    for r_idx, row_data in enumerate(rows):
        row = table.rows[r_idx + 1]
        bg  = 'F1F8E9' if r_idx % 2 == 1 else 'FFFFFF'
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
    run.bold = True; run.font.color.rgb = VERDE_ESCURO; run.font.size = Pt(10)
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
    r.font.color.rgb = VERDE_ESCURO; r.font.name = 'Calibri'

doc.add_paragraph()
doc.add_paragraph()

p = doc.add_paragraph(); p.alignment = WD_ALIGN_PARAGRAPH.CENTER
r = p.add_run('SIHCP — Sistema de Histórico e Coleta Patrimonial')
r.bold = True; r.font.size = Pt(14); r.font.color.rgb = VERDE_MEDIO; r.font.name = 'Calibri'

doc.add_paragraph()

p = doc.add_paragraph(); p.alignment = WD_ALIGN_PARAGRAPH.CENTER
r = p.add_run('ESPECIFICAÇÃO DE REQUISITOS FUNCIONAIS\nAPLICATIVO ANDROID')
r.bold = True; r.font.size = Pt(22); r.font.color.rgb = VERDE_ESCURO; r.font.name = 'Calibri'

doc.add_paragraph()
doc.add_paragraph()

meta = [
    ('Versão do Documento', '1.0.0'),
    ('Versão do App',        '2.29.2'),
    ('Data',                 'Maio de 2026'),
    ('Componente',           'Aplicativo Android (Kotlin + Jetpack)'),
    ('Linguagem',            'Kotlin 1.9.x'),
    ('SDK Alvo',             'Android 14 (API 34) — mínimo API 23'),
    ('Arquitetura',          'Clean Architecture + MVVM'),
    ('Total de Requisitos',  '56 RFA em 9 módulos'),
    ('Status',               '100% Implementado'),
]
tbl = doc.add_table(rows=len(meta), cols=2)
tbl.style = 'Table Grid'; tbl.alignment = WD_TABLE_ALIGNMENT.CENTER
for i, (k, v) in enumerate(meta):
    bg = 'C8E6C9' if i % 2 == 0 else 'FFFFFF'
    set_cell_bg(tbl.rows[i].cells[0], bg); set_cell_bg(tbl.rows[i].cells[1], bg)
    set_cell_text(tbl.rows[i].cells[0], k, bold=True, size=10, color=VERDE_ESCURO)
    set_cell_text(tbl.rows[i].cells[1], v, size=10, color=CINZA_TEXTO)
    tbl.rows[i].cells[0].width = Cm(6); tbl.rows[i].cells[1].width = Cm(10)

doc.add_page_break()

# ─────────────────────────────────────────────────────────────────────────────
# 1. INTRODUÇÃO
# ─────────────────────────────────────────────────────────────────────────────
doc.add_heading('1. Introdução', level=1)

doc.add_heading('1.1 Propósito', level=2)
par('Este documento especifica os requisitos funcionais do Aplicativo Android do Sistema de '
    'Histórico e Coleta Patrimonial (SIHCP), desenvolvido para o Instituto Federal de Mato '
    'Grosso (IFMT). O aplicativo é a ferramenta de coleta em campo utilizada pelos coletores '
    'durante o processo de inventário patrimonial, com suporte completo a modo offline, '
    'banco local criptografado e sincronização automática em background.')
par('Este documento é derivado da especificação consolidada REQUISITOS_FUNCIONAIS_NAO_FUNCIONAIS.md '
    '(v3.0.0) e detalha exclusivamente os requisitos pertinentes ao componente Android, '
    'complementando-os com informações de implementação específicas da plataforma.')

doc.add_heading('1.2 Escopo', level=2)
par('O escopo compreende o aplicativo Android nativo, construído em Kotlin com arquitetura '
    'Clean Architecture + MVVM. Estão abrangidos:')
for item in [
    'Autenticação JWT com renovação automática e suporte biométrico;',
    'Coleta patrimonial por QR Code, digitação manual e por descrição;',
    'Registro de itens sem etiqueta com foto obrigatória;',
    'Modo offline completo com banco Room criptografado (SQLCipher + Android Keystore);',
    'Sincronização automática em background via WorkManager (batch + retry);',
    'Dashboard com estatísticas do inventário ativo;',
    'Exportação de relatórios em PDF, Excel e CSV;',
    'Histórico de scans, diagnóstico de rede e busca por voz;',
    'Gerenciamento de coletas pendentes com diagnóstico de erros.',
]:
    bullet(item)
par('Não fazem parte deste documento: os requisitos da aplicação desktop '
    '(REQUISITOS_FUNCIONAIS_DESKTOP.md) nem os do servidor mobile '
    '(REQUISITOS_FUNCIONAIS_SERVIDOR_MOBILE.md).')

doc.add_heading('1.3 Definições e Acrônimos', level=2)
simple_table(
    ['Termo / Sigla', 'Definição'],
    [
        ('RFA',      'Requisito Funcional do Aplicativo Android'),
        ('MVVM',     'Model-View-ViewModel — padrão arquitetural adotado'),
        ('Room',     'Biblioteca de persistência local do Android (abstração sobre SQLite)'),
        ('SQLCipher','Extensão do SQLite com criptografia AES-256'),
        ('Hilt',     'Framework de injeção de dependência para Android (Google)'),
        ('WorkManager','API do Jetpack para tarefas em background confiáveis'),
        ('StateFlow','Fluxo reativo do Kotlin Coroutines para estado da UI'),
        ('JWT',      'JSON Web Token — token de autenticação'),
        ('QR Code',  'Quick Response Code — código de barras bidimensional'),
        ('Offline-First','Estratégia em que dados são salvos localmente antes de sincronizar'),
        ('Batch Sync','Sincronização de múltiplas coletas em uma única requisição HTTP'),
        ('ESP',      'EncryptedSharedPreferences — armazenamento seguro de preferências'),
    ],
    [3.5, 12.5]
)

doc.add_heading('1.4 Classificação de Prioridades', level=2)
simple_table(
    ['Prioridade', 'Descrição'],
    [
        ('Essencial',  'Requisito indispensável. A ausência inviabiliza o uso do aplicativo em campo.'),
        ('Importante', 'Requisito necessário para operação plena. O app funciona sem ele, mas com perda de produtividade ou rastreabilidade.'),
        ('Desejável',  'Requisito que agrega valor ergonômico ou analítico. Não é crítico para o fluxo principal de coleta.'),
    ],
    [3.0, 13.0]
)

doc.add_heading('1.5 Stack Tecnológico', level=2)
simple_table(
    ['Tecnologia', 'Versão', 'Finalidade'],
    [
        ('Kotlin',              '1.9.x',    'Linguagem principal'),
        ('Android SDK',         'API 34',   'Target SDK (Android 14); mínimo API 23'),
        ('Hilt',                '2.48',     'Injeção de dependência'),
        ('Room',                '2.6.1',    'Banco de dados local'),
        ('SQLCipher',           '4.6.1',    'Criptografia AES-256 do banco Room'),
        ('Retrofit',            '2.9.0',    'Cliente HTTP para API REST'),
        ('Coroutines + Flow',   '1.7.3',    'Programação assíncrona'),
        ('WorkManager',         '2.9.0',    'Sincronização em background'),
        ('Paging 3',            '3.2.1',    'Paginação reativa de listas'),
        ('Jetpack Security',    '1.1.0',    'EncryptedSharedPreferences'),
        ('ZXing (embedded)',    '4.3.0',    'Leitura de QR Code e códigos de barras'),
        ('MPAndroidChart',      '3.1.0',    'Gráficos e visualizações'),
        ('Glide',               '4.16.0',   'Carregamento de imagens'),
        ('Biometric',           '1.1.0',    'Autenticação biométrica'),
    ],
    [4.5, 2.5, 9.0]
)

# ─────────────────────────────────────────────────────────────────────────────
# 2. ARQUITETURA
# ─────────────────────────────────────────────────────────────────────────────
doc.add_heading('2. Arquitetura do Aplicativo', level=1)
par('O aplicativo segue Clean Architecture + MVVM, organizado em três camadas independentes:')
for item in [
    ('Presentation (UI): ', 'Activities, Fragments e ViewModels. A UI observa StateFlow do ViewModel e delega ações para Use Cases. Nunca acessa repositórios diretamente.'),
    ('Domain (Negócio): ',  'Use Cases com regras de negócio puras (sem dependências Android). Retornam Result<T> para tratamento de erros.'),
    ('Data (Dados): ',      'Repositories implementando estratégia Offline-First: salva localmente no Room (SQLCipher) e sincroniza com a API REST via Retrofit.'),
]:
    bullet(item[1], bold_prefix=item[0])

par('Injeção de dependência via Hilt garante desacoplamento entre camadas. '
    'Todos os ViewModels são anotados com @HiltViewModel e as Activities com @AndroidEntryPoint.')

doc.add_heading('2.1 Segurança do Banco Local', level=2)
par('O banco de dados local utiliza três camadas de proteção:')
for item in [
    ('SQLCipher (AES-256): ', 'banco Room criptografado em repouso;'),
    ('Android Keystore: ',    'passphrase gerada aleatoriamente e armazenada em hardware-backed keystore quando disponível;'),
    ('EncryptedSharedPreferences: ', 'tokens JWT e credenciais protegidos em preferências persistentes.'),
]:
    bullet(item[1], bold_prefix=item[0])
par('Nota: EncryptedSharedPreferences foi deprecado pelo Google. Versões futuras devem '
    'considerar migração para DataStore com criptografia manual.',
    italic=True, color=RGBColor(0x77, 0x77, 0x77))

# ─────────────────────────────────────────────────────────────────────────────
# 3. REQUISITOS FUNCIONAIS POR MÓDULO
# ─────────────────────────────────────────────────────────────────────────────
doc.add_heading('3. Requisitos Funcionais por Módulo', level=1)

HDR = ['ID', 'Requisito', 'Descrição', 'Prioridade', 'Tela / Componente', 'Status']
W   = [1.5, 3.2, 6.8, 2.0, 3.5, 1.5]

# 3.1 Autenticação
doc.add_heading('3.1 Módulo de Autenticação e Sessão', level=2)
req_table(HDR, [
    ('RFA-001','Login com usuário e senha','Autenticar o usuário via login e senha, obtendo access token e refresh token JWT do servidor','Essencial','LoginActivity','✅'),
    ('RFA-002','Renovação automática de token','Renovar o access token automaticamente antes da expiração, sem intervenção do usuário, via RefreshTokenInterceptor','Essencial','RefreshTokenInterceptor','✅'),
    ('RFA-003','Autenticação biométrica','Suportar autenticação por impressão digital ou reconhecimento facial como alternativa ao login por senha (opcional)','Desejável','LoginActivity + BiometricPrompt','✅'),
    ('RFA-004','Logout seguro','Encerrar a sessão, invalidar tokens locais e retornar à tela de login','Essencial','MainActivity','✅'),
    ('RFA-005','Bloqueio por tentativas inválidas','Bloquear o acesso após 5 tentativas de login inválidas consecutivas','Importante','LoginActivity','✅'),
    ('RFA-006','Controle de acesso por perfil','Exibir ou ocultar funcionalidades conforme o perfil do usuário (ADMIN, SUPERVISOR, COLETOR, CONSULTA)','Essencial','MainActivity + Navigation','✅'),
    ('RFA-007','Armazenamento seguro de credenciais','Armazenar tokens JWT em EncryptedSharedPreferences; passphrase do banco no Android Keystore','Essencial','PreferencesManager + KeystoreManager','✅'),
    ('RFA-008','Configuração do servidor','Permitir ao usuário configurar o endereço IP e porta do servidor da API','Essencial','SettingsActivity','✅'),
], W)
criteria_box([
    'Tokens devem ser armazenados exclusivamente em EncryptedSharedPreferences, nunca em SharedPreferences simples;',
    'A renovação automática deve ser transparente — o usuário não deve perceber a troca de token;',
    'Após bloqueio, o usuário deve ser informado do tempo restante para desbloqueio.',
])

# 3.2 Dashboard
doc.add_heading('3.2 Módulo de Dashboard', level=2)
req_table(HDR, [
    ('RFA-009','Dashboard com KPIs do inventário','Exibir painel com total de patrimônios, coletados, pendentes e percentual de conclusão do inventário ativo','Essencial','DashboardFragment + DashboardViewModelClean','✅'),
    ('RFA-010','Gráfico de evolução de coletas','Exibir gráfico de linha com a evolução temporal das coletas','Importante','DashboardFragment + MPAndroidChart','✅'),
    ('RFA-011','Indicador de status de conexão','Exibir indicador permanente de conectividade (online/offline) e quantidade de coletas pendentes','Essencial','MainActivity + StatusBar','✅'),
    ('RFA-012','Busca por voz','Suportar busca por voz no dashboard e telas de listagem via SpeechRecognizer','Desejável','DashboardFragment + VoiceSearchManager','✅'),
    ('RFA-013','Atualização automática do dashboard','Atualizar os KPIs automaticamente ao retornar ao dashboard ou após sincronização','Importante','DashboardViewModelClean','✅'),
], W)
criteria_box([
    'O dashboard deve funcionar offline, exibindo dados do banco local;',
    'A atualização automática não deve bloquear a UI — deve ocorrer em background via coroutine.',
])

# 3.3 Coleta por QR Code
doc.add_heading('3.3 Módulo de Coleta por QR Code', level=2)
req_table(HDR, [
    ('RFA-014','Leitura de QR Code via câmera','Abrir câmera e ler QR Code ou código de barras do patrimônio usando ZXing','Essencial','ScannerActivity + CustomCaptureActivity','✅'),
    ('RFA-015','Validação do patrimônio escaneado','Validar se o patrimônio existe, está ativo e ainda não foi coletado no inventário ativo','Essencial','ScannerViewModel + BuscarPatrimonioUseCase','✅'),
    ('RFA-016','Exibir dados do patrimônio','Exibir número, descrição, sala cadastrada, responsável e estado após leitura bem-sucedida','Essencial','ScannerActivity (bottom sheet)','✅'),
    ('RFA-017','Registrar estado de conservação','Permitir selecionar o estado de conservação encontrado (BOM, OCIOSO, RECUPERÁVEL, ANTIECONÔMICO, IRRECUPERÁVEL)','Essencial','EstadoPatrimonioDialog','✅'),
    ('RFA-018','Registrar localização encontrada','Registrar a sala em que o patrimônio foi efetivamente localizado','Essencial','ScannerActivity','✅'),
    ('RFA-019','Feedback sonoro e tátil','Emitir som e vibração ao detectar QR Code e ao confirmar coleta','Importante','SoundUtils + VibrationHelper','✅'),
    ('RFA-020','Modo de estado fixo','Permitir fixar o estado de conservação para agilizar coletas em série na mesma sala','Importante','ScannerActivity + PreferencesManager','✅'),
    ('RFA-021','Contador de coletas na câmera','Exibir contador de coletas da sessão e último patrimônio coletado diretamente na tela da câmera','Importante','CustomCaptureActivity','✅'),
    ('RFA-022','Flash/lanterna na câmera','Permitir ligar e desligar a lanterna do dispositivo durante o scan, com detecção automática de disponibilidade','Importante','CustomCaptureActivity','✅'),
    ('RFA-023','Zoom na câmera','Suportar zoom via botões e gesto de pinch-to-zoom durante o scan','Desejável','CustomCaptureActivity','✅'),
    ('RFA-024','Alerta de patrimônio já coletado','Exibir aviso quando o patrimônio escaneado já foi coletado no inventário ativo, com dados da coleta anterior','Essencial','ScannerActivity','✅'),
], W)
criteria_box([
    'A leitura do QR Code deve concluir em menos de 1 segundo;',
    'O scanner deve permanecer aberto indefinidamente (sem timeout) para coleta contínua em campo;',
    'Patrimônios compostos devem exibir alerta específico e não permitir coleta pelo fluxo normal (RN025).',
])

# 3.4 Coleta Manual
doc.add_heading('3.4 Módulo de Coleta Manual', level=2)
req_table(HDR, [
    ('RFA-025','Coleta por digitação do número','Permitir registrar coleta digitando o número de tombamento do patrimônio','Essencial','ManualCollectionActivity','✅'),
    ('RFA-026','Autocomplete na busca manual','Exibir sugestões de patrimônios conforme o usuário digita o número','Importante','ManualCollectionActivity','✅'),
    ('RFA-027','Validação em tempo real','Validar o patrimônio em tempo real enquanto o usuário digita, exibindo feedback imediato','Importante','ManualCollectionActivity + BuscarPatrimonioUseCase','✅'),
    ('RFA-028','Coleta por descrição','Permitir selecionar patrimônio por descrição (para itens sem número visível), com filtro acento/caso-insensível','Importante','DescricaoSelectionActivity','✅'),
    ('RFA-029','Sugestões de descrição com cache offline','Exibir sugestões de descrição com cache local, atualizado automaticamente após sincronização','Desejável','DescricaoSelectionActivity + BuscarDescricoesNaoColetadasUseCase','✅'),
], W)
criteria_box([
    'A busca local (Room) deve responder em menos de 100 ms;',
    'O filtro de descrições deve ser insensível a acentuação e maiúsculas/minúsculas.',
])

# 3.5 Itens sem etiqueta
doc.add_heading('3.5 Módulo de Itens Sem Etiqueta', level=2)
req_table(HDR, [
    ('RFA-030','Registrar item sem etiqueta','Permitir registrar item encontrado sem etiqueta de identificação, com formulário completo','Essencial','ItemSemEtiquetaActivity','✅'),
    ('RFA-031','Foto obrigatória para item sem etiqueta','Exigir captura de foto como evidência obrigatória para itens sem etiqueta, validada no ViewModel','Essencial','ItemSemEtiquetaActivity + ItemSemEtiquetaViewModel','✅'),
    ('RFA-032','Categorização do item','Permitir selecionar categoria do item (mobiliário, equipamento, etc.)','Essencial','ItemSemEtiquetaActivity','✅'),
    ('RFA-033','Foto de referência para identificação','Exibir foto de referência associada à descrição selecionada para auxiliar na identificação visual','Desejável','DescricaoSelectionActivity','✅'),
    ('RFA-034','Coleta similar (sem etiqueta)','Permitir registrar item similar ao último coletado sem precisar preencher o formulário novamente','Importante','ScannerActivity (FAB)','✅'),
], W)
criteria_box([
    'A foto é obrigatória e deve ser validada antes de permitir o envio do formulário;',
    'A descrição deve ter entre 3 e 255 caracteres (validação no ViewModel e no servidor).',
])

# 3.6 Sincronização
doc.add_heading('3.6 Módulo de Sincronização', level=2)
req_table(HDR, [
    ('RFA-035','Modo offline completo','Funcionar completamente sem conexão com internet para todas as operações de coleta','Essencial','ColetaRepositoryImpl (offline-first)','✅'),
    ('RFA-036','Armazenamento local criptografado','Salvar todas as coletas no banco Room com criptografia SQLCipher antes de sincronizar','Essencial','AppDatabase + SQLCipher','✅'),
    ('RFA-037','Sincronização automática em background','Sincronizar coletas pendentes automaticamente a cada 30 minutos via WorkManager','Essencial','SyncWorker + SyncManager','✅'),
    ('RFA-038','Sincronização em lote (batch)','Enviar múltiplas coletas em uma única requisição HTTP para reduzir consumo de dados','Importante','SincronizarColetasPendentesUseCase + ColetaApi.registrarColetasEmLote()','✅'),
    ('RFA-039','Retry automático com backoff exponencial','Retentar sincronização em caso de falha, com intervalo crescente entre tentativas','Essencial','SyncWorker (WorkManager constraints)','✅'),
    ('RFA-040','Fallback para sync individual','Se o batch falhar, tentar sincronizar cada coleta individualmente','Importante','ColetaRepositoryImpl','✅'),
    ('RFA-041','Sincronização manual sob demanda','Permitir ao usuário iniciar sincronização manualmente pela tela de sincronização','Essencial','SyncActivity + SyncViewModel','✅'),
    ('RFA-042','Sincronização de fotos','Sincronizar fotos de coleta separadamente via PhotoSyncWorker, preferencialmente em Wi-Fi','Importante','PhotoSyncWorker + FotoColetaApi','✅'),
    ('RFA-043','Indicador de coletas pendentes','Exibir quantidade de coletas pendentes de sincronização na tela principal e na tela de sync','Importante','SyncActivity + SyncViewModel','✅'),
    ('RFA-044','Download de dados para offline','Baixar patrimônios, salas, setores e responsáveis do servidor para o banco local','Essencial','SincronizarDadosUseCase','✅'),
], W)
criteria_box([
    'O sync em background deve respeitar constraints: internet disponível e bateria não baixa;',
    'O batch deve suportar até 500 coletas por requisição;',
    'Fotos devem ser sincronizadas preferencialmente em Wi-Fi para economizar dados móveis.',
])

# 3.7 Relatórios e Exportação
doc.add_heading('3.7 Módulo de Relatórios e Exportação', level=2)
req_table(HDR, [
    ('RFA-045','Exportar relatório em PDF','Gerar relatório do inventário em formato PDF com dados de coleta','Essencial','ExportFragment + GerarRelatorioUseCase','✅'),
    ('RFA-046','Exportar relatório em Excel (TSV)','Gerar relatório em formato TSV compatível com Excel (sem Apache POI, para compatibilidade com minSdk 23)','Importante','ExcelGenerator','✅'),
    ('RFA-047','Exportar relatório em CSV','Gerar relatório em formato CSV para processamento externo','Importante','CsvGenerator','✅'),
    ('RFA-048','Filtros de exportação','Permitir filtrar o relatório por status (todos, coletados, não coletados) e por sala','Importante','ExportFragment','✅'),
    ('RFA-049','Compartilhar arquivo gerado','Permitir compartilhar o arquivo exportado via apps do dispositivo (e-mail, Drive, etc.)','Importante','ExportFragment (Intent.ACTION_SEND)','✅'),
], W)
criteria_box([
    'O formato Excel usa TSV (Tab-Separated Values) por compatibilidade com minSdk 23 — Apache POI requer minSdk 26;',
    'O arquivo gerado deve ser acessível via FileProvider para compartilhamento seguro.',
])

# 3.8 Histórico e Consultas
doc.add_heading('3.8 Módulo de Histórico e Consultas', level=2)
req_table(HDR, [
    ('RFA-050','Visualizar coletas realizadas','Exibir lista de coletas realizadas pelo usuário, com filtros por status, sala e data','Importante','ColetasUnificadaActivity + CollectionViewActivity','✅'),
    ('RFA-051','Histórico de scans','Manter histórico de patrimônios escaneados/consultados com tipo de acesso e timestamp','Desejável','HistoricoScansActivity + RegistrarAcessoPatrimonioUseCase','✅'),
    ('RFA-052','Verificar status de coleta (online)','Consultar no servidor se um patrimônio foi coletado no inventário ativo; exibir mensagem de indisponibilidade se offline','Importante','ValidationViewModel + VerificarSePatrimonioFoiColetadoUseCase','✅'),
    ('RFA-053','Coletas pendentes com diagnóstico','Exibir coletas pendentes de sincronização com detalhes de erro e opções de retry ou remoção individual','Importante','PendingCollectionsActivity','✅'),
    ('RFA-054','Diagnóstico de rede','Oferecer tela de diagnóstico de conectividade para troubleshooting de problemas de conexão com o servidor','Desejável','NetworkDiagnosticActivity','✅'),
], W)
criteria_box([
    'A verificação de status de coleta (RFA-052) deve obrigatoriamente consultar o servidor — nunca exibir dados locais potencialmente desatualizados (RN026);',
    'Se offline, exibir mensagem clara: "Sem conexão — não é possível verificar o status".',
])

# 3.9 Configurações e Utilitários
doc.add_heading('3.9 Módulo de Configurações e Utilitários', level=2)
req_table(HDR, [
    ('RFA-055','Configurações do aplicativo','Permitir configurar endereço do servidor, preferências de vibração, som e sincronização','Importante','SettingsActivity','✅'),
    ('RFA-056','Seleção de sala antes da coleta','Exibir tela de seleção de sala antes de iniciar o scanner, garantindo que toda coleta tenha sala associada','Essencial','SalaSelectionActivity','✅'),
], W)

# ─────────────────────────────────────────────────────────────────────────────
# 4. CENÁRIOS DE VALIDAÇÃO
# ─────────────────────────────────────────────────────────────────────────────
doc.add_heading('4. Cenários de Validação', level=1)

doc.add_heading('4.1 Cenário: Coleta por QR Code em Campo', level=2)
par('Pré-condições: Usuário autenticado; inventário ativo; sala selecionada.', bold=True)
par('Fluxo principal:')
for step in [
    'Coletor seleciona a sala na SalaSelectionActivity (RFA-056);',
    'Coletor abre o scanner (ScannerActivity) — câmera inicia sem timeout;',
    'Coletor aponta para o QR Code do patrimônio;',
    'Sistema lê e decodifica o QR Code em menos de 1 segundo (RFA-014);',
    'Sistema valida o patrimônio: existe, está ativo, não foi coletado (RFA-015);',
    'Sistema exibe dados do patrimônio no bottom sheet (RFA-016);',
    'Coletor confirma o estado de conservação (RFA-017);',
    'Sistema registra a coleta no banco local (Room + SQLCipher) — RFA-036;',
    'Sistema emite vibração e som de sucesso (RFA-019);',
    'Scanner retorna automaticamente para nova leitura;',
    'WorkManager sincroniza a coleta em background (RFA-037).',
]:
    numbered(step)
par('Critério de sucesso: Coleta registrada localmente em menos de 2 segundos; '
    'sincronização ocorre em background sem bloquear o coletor.', italic=True)

doc.add_heading('4.2 Cenário: Operação Offline Completa', level=2)
par('Pré-condições: Dados sincronizados previamente; dispositivo sem conexão.', bold=True)
par('Fluxo principal:')
for step in [
    'Dispositivo perde conexão com o servidor;',
    'Indicador de status exibe "Offline" (RFA-011);',
    'Coletor realiza coletas normalmente — dados salvos no Room local (RFA-035);',
    'Coletas ficam com status PENDENTE no banco local (RFA-043);',
    'Ao reconectar, WorkManager detecta conexão e inicia sync automático (RFA-037);',
    'Coletas pendentes são enviadas em batch para o servidor (RFA-038);',
    'Coletas bem-sucedidas são marcadas como SINCRONIZADO;',
    'Indicador retorna para "Online".',
]:
    numbered(step)
par('Critério de sucesso: Nenhuma coleta é perdida; o coletor pode trabalhar '
    'indefinidamente offline sem perda de dados.', italic=True)

doc.add_heading('4.3 Cenário: Registro de Item Sem Etiqueta', level=2)
par('Pré-condições: Usuário autenticado; inventário ativo; sala selecionada.', bold=True)
par('Fluxo principal:')
for step in [
    'Coletor aciona o FAB "Coletar sem etiqueta" na tela do scanner (RFA-034);',
    'Sistema valida que há sala selecionada e inventário ativo;',
    'Sistema abre DescricaoSelectionActivity com sugestões de descrição (RFA-028);',
    'Coletor seleciona ou digita a descrição do item;',
    'Sistema abre ItemSemEtiquetaActivity com formulário completo;',
    'Coletor preenche categoria, estado e localização (obrigatórios);',
    'Coletor captura foto do item — obrigatória (RFA-031);',
    'Sistema valida todos os campos e a foto;',
    'Sistema registra o item localmente e agenda sync da foto (RFA-042).',
]:
    numbered(step)
par('Critério de sucesso: Item registrado com foto; descrição entre 3 e 255 caracteres; '
    'foto sincronizada via PhotoSyncWorker quando em Wi-Fi.', italic=True)

doc.add_heading('4.4 Cenário: Sincronização em Lote após Período Offline', level=2)
par('Pré-condições: Dispositivo com 50+ coletas pendentes; conexão restabelecida.', bold=True)
par('Fluxo principal:')
for step in [
    'WorkManager detecta conexão disponível e bateria não baixa (RFA-039);',
    'SyncWorker busca todas as coletas com status PENDENTE;',
    'Sistema agrupa coletas em lote de até 500 itens (RFA-038);',
    'Sistema envia POST /api/mobile/coletas/batch para o servidor;',
    'Servidor valida cada coleta e retorna resultado estruturado por índice;',
    'Sistema marca coletas bem-sucedidas como SINCRONIZADO;',
    'Se o batch falhar, sistema tenta sync individual como fallback (RFA-040);',
    'SyncActivity exibe resumo: X sincronizadas, Y com erro.',
]:
    numbered(step)
par('Critério de sucesso: 50 coletas sincronizadas em menos de 5 segundos; '
    'falhas parciais identificadas por índice sem perda das demais.', italic=True)

# ─────────────────────────────────────────────────────────────────────────────
# 5. REGRAS DE NEGÓCIO APLICÁVEIS
# ─────────────────────────────────────────────────────────────────────────────
doc.add_heading('5. Regras de Negócio Aplicáveis ao App', level=1)
simple_table(
    ['ID', 'Regra', 'Descrição', 'Impacto no App'],
    [
        ('RN011','Coleta em inventário ativo','Coletas só podem ser registradas quando há inventário ativo','App deve verificar inventário ativo antes de abrir o scanner'),
        ('RN015','Patrimônio coletável','Apenas patrimônios com status ATIVO ou PENDENTE podem ser coletados','Validação em BuscarPatrimonioUseCase'),
        ('RN017','Coleta única por inventário','Cada patrimônio deve ser coletado apenas uma vez por inventário','Alerta exibido ao tentar recoleta; coleta bloqueada'),
        ('RN019','Divergência automática','Divergência detectada quando localização encontrada ≠ localização cadastrada','Registrada automaticamente no ato da coleta'),
        ('RN020','Foto obrigatória (sem etiqueta)','Foto é obrigatória para itens sem etiqueta','Validação no ViewModel antes de permitir envio'),
        ('RN023','Localização obrigatória','Localização encontrada é campo obrigatório na coleta','Campo obrigatório no formulário de coleta'),
        ('RN024','Estado obrigatório','Estado de conservação encontrado é campo obrigatório','Campo obrigatório no EstadoPatrimonioDialog'),
        ('RN025','Patrimônio composto','Patrimônios compostos não podem ser coletados pelo fluxo normal','Alerta específico exibido; coleta bloqueada'),
        ('RN026','Verificação somente online','Consulta de status de coleta deve obrigatoriamente consultar o servidor','Se offline, exibir mensagem de indisponibilidade'),
        ('RN031','Constraints de sync','Sync em background apenas com internet e bateria > 20%','Configurado nos constraints do WorkManager'),
    ],
    [1.5, 3.5, 5.5, 5.5]
)

# ─────────────────────────────────────────────────────────────────────────────
# 6. RISCOS E MITIGAÇÕES
# ─────────────────────────────────────────────────────────────────────────────
doc.add_heading('6. Riscos e Mitigações', level=1)
simple_table(
    ['Risco', 'Impacto', 'Probabilidade', 'Mitigação'],
    [
        ('Perda de dados por falha do banco local','Alto','Baixa','SQLCipher + Room com transações atômicas; backup automático'),
        ('Falha na câmera ao abrir o scanner','Alto','Média','Detecção de FEATURE_CAMERA_FLASH; fallback para coleta manual; tratamento de RuntimeException no zoom'),
        ('Token expirado durante coleta offline','Médio','Média','RefreshTokenInterceptor renova token automaticamente; coletas salvas localmente independente do token'),
        ('Sincronização parcial deixando dados inconsistentes','Alto','Baixa','Batch com resultado por índice; fallback individual; status PENDENTE preservado em caso de falha'),
        ('Dispositivo sem flash tentando ligar lanterna','Baixo','Alta','Verificação de FEATURE_CAMERA_FLASH; botão oculto se não disponível'),
        ('Foto muito grande causando falha no upload','Médio','Média','Compressão JPEG antes do upload; limite de 5 MB validado no servidor'),
        ('Banco corrompido após atualização do app','Alto','Baixa','Room Migrations versionadas; fallbackToDestructiveMigration removido (CR-01)'),
        ('Passphrase do banco perdida após troca de lock screen','Médio','Baixa','KeystoreRecoveryActivity para recuperação; Android Keystore hardware-backed quando disponível'),
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
        ('Autenticação e Sessão',       'RFA-001 a RFA-008',  '8'),
        ('Dashboard',                   'RFA-009 a RFA-013',  '5'),
        ('Coleta por QR Code',          'RFA-014 a RFA-024', '11'),
        ('Coleta Manual',               'RFA-025 a RFA-029',  '5'),
        ('Itens Sem Etiqueta',          'RFA-030 a RFA-034',  '5'),
        ('Sincronização',               'RFA-035 a RFA-044', '10'),
        ('Relatórios e Exportação',     'RFA-045 a RFA-049',  '5'),
        ('Histórico e Consultas',       'RFA-050 a RFA-054',  '5'),
        ('Configurações e Utilitários', 'RFA-055 a RFA-056',  '2'),
        ('TOTAL',                       '',                   '56'),
    ],
    [6, 5, 2.5]
)

doc.add_heading('7.2 Mapeamento com Requisitos do Documento Integrado', level=2)
simple_table(
    ['RFA Android', 'RF Integrado Correspondente'],
    [
        ('RFA-001 a RFA-008', 'RF001 a RF010'),
        ('RFA-009 a RFA-013', 'RF071, RF072, RF074, RF075, RF098'),
        ('RFA-014 a RFA-024', 'RF031, RF034, RF035, RF036, RF037, RF043, RF044, RF047, RF097'),
        ('RFA-025 a RFA-029', 'RF032, RF093, RF094'),
        ('RFA-030 a RFA-034', 'RF033, RF039, RF095'),
        ('RFA-035 a RFA-044', 'RF048 a RF058'),
        ('RFA-045 a RFA-049', 'RF065 a RF069, RF096'),
        ('RFA-050 a RFA-054', 'RF045, RF046, RF092, RF099, RF100'),
        ('RFA-055 a RFA-056', 'RF065 (configuração)'),
    ],
    [5, 11]
)

doc.add_heading('7.3 Cobertura de Implementação', level=2)
simple_table(
    ['Situação', 'Quantidade', 'Percentual'],
    [
        ('Implementado',       '56', '100%'),
        ('Em desenvolvimento',  '0',   '0%'),
        ('Planejado',           '0',   '0%'),
    ],
    [6, 3, 3]
)

doc.add_heading('7.4 Distribuição por Prioridade', level=2)
simple_table(
    ['Prioridade', 'Quantidade', 'Percentual'],
    [
        ('Essencial',  '24', '43%'),
        ('Importante', '22', '39%'),
        ('Desejável',  '10', '18%'),
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
        ('Coleta',        'Registro da verificação física de um patrimônio durante o inventário'),
        ('Divergência',   'Diferença entre a localização cadastrada e a localização encontrada'),
        ('Item Composto', 'Conjunto formado por vários componentes tratados como unidade patrimonial'),
        ('Offline-First', 'Estratégia em que dados são salvos localmente antes de sincronizar com o servidor'),
        ('Batch Sync',    'Envio de múltiplas coletas em uma única requisição HTTP'),
        ('Room',          'Biblioteca de persistência local do Android (abstração sobre SQLite)'),
        ('SQLCipher',     'Extensão do SQLite com criptografia AES-256 para proteção de dados em repouso'),
        ('WorkManager',   'API do Jetpack para execução de tarefas em background com garantias de execução'),
        ('StateFlow',     'Fluxo reativo do Kotlin Coroutines para gerenciamento de estado da UI'),
        ('Use Case',      'Classe de domínio que encapsula uma regra de negócio específica'),
        ('ViewModel',     'Componente do Jetpack que sobrevive a mudanças de configuração e gerencia o estado da UI'),
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
        ('1.0.0','Mai/2026','Equipe IFMT',
         'Versão inicial: 56 RFAs em 9 módulos, derivados do documento consolidado v3.0.0; '
         'inclui stack tecnológico, cenários de validação, regras de negócio, riscos e '
         'matriz de rastreabilidade completa'),
    ],
    [1.5, 2.0, 3.0, 9.5]
)

# ─────────────────────────────────────────────────────────────────────────────
# APÊNDICE C — REFERÊNCIAS CRUZADAS
# ─────────────────────────────────────────────────────────────────────────────
doc.add_heading('Apêndice C — Documentos Relacionados', level=1)
simple_table(
    ['Documento', 'Escopo'],
    [
        ('REQUISITOS_FUNCIONAIS_NAO_FUNCIONAIS.md',      'Especificação consolidada do ecossistema SIHCP (v3.0.0) — fonte deste documento'),
        ('REQUISITOS_FUNCIONAIS_DESKTOP.md',             'Requisitos funcionais da aplicação desktop'),
        ('REQUISITOS_FUNCIONAIS_SERVIDOR_MOBILE.md',     'Requisitos funcionais da API REST mobile'),
        ('REQUISITOS_NAO_FUNCIONAIS_SERVIDOR_MOBILE.md', 'Requisitos não funcionais da API mobile'),
        ('ADEQUACAO_NORMAS_FEDERAIS.md',                 'Conformidade com a legislação federal aplicável'),
    ],
    [7, 9]
)

# ─────────────────────────────────────────────────────────────────────────────
# RODAPÉ
# ─────────────────────────────────────────────────────────────────────────────
doc.add_paragraph()
divider()
p = doc.add_paragraph(); p.alignment = WD_ALIGN_PARAGRAPH.CENTER
r = p.add_run(
    'Versão do documento: 1.0.0  |  Versão do app: 2.29.2  |  Maio de 2026\n'
    'Instituto Federal de Educação, Ciência e Tecnologia de Mato Grosso (IFMT)'
)
r.font.size = Pt(9); r.font.color.rgb = RGBColor(0x88, 0x88, 0x88)
r.font.italic = True; r.font.name = 'Calibri'

# ─────────────────────────────────────────────────────────────────────────────
# SALVAR
# ─────────────────────────────────────────────────────────────────────────────
caminho = (r'c:\Users\Romulo\Documents\PROJETOS\MASTER_INVENTARIO'
           r'\docs\arquitetura\REQUISITOS_FUNCIONAIS_ANDROID.docx')
doc.save(caminho)
print(f'✅ Documento salvo em:\n   {caminho}')
