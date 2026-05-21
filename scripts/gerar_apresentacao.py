"""
Gerador de apresentação impactante para público adolescente.
Usa python-pptx com API nativa + XML mínimo apenas para transições.
"""

from pptx import Presentation
from pptx.util import Inches, Pt, Emu
from pptx.dml.color import RGBColor
from pptx.enum.text import PP_ALIGN
from pptx.oxml.ns import qn
from lxml import etree

# ─────────────────────────────────────────────────────────────────────────────
# PALETA
# ─────────────────────────────────────────────────────────────────────────────
ROXO_ESCURO  = RGBColor(0x1A, 0x00, 0x3D)
ROXO_MEDIO   = RGBColor(0x4A, 0x00, 0x8F)
ROSA_NEON    = RGBColor(0xFF, 0x00, 0x7F)
CIANO_NEON   = RGBColor(0x00, 0xE5, 0xFF)
AMARELO_NEON = RGBColor(0xFF, 0xE0, 0x00)
VERDE_NEON   = RGBColor(0x39, 0xFF, 0x14)
BRANCO       = RGBColor(0xFF, 0xFF, 0xFF)
CINZA_CLARO  = RGBColor(0xCC, 0xCC, 0xCC)
PRETO        = RGBColor(0x0D, 0x0D, 0x0D)

LARGURA = Inches(13.33)
ALTURA  = Inches(7.5)

prs = Presentation()
prs.slide_width  = LARGURA
prs.slide_height = ALTURA
LAYOUT = prs.slide_layouts[6]  # em branco


# ─────────────────────────────────────────────────────────────────────────────
# HELPERS
# ─────────────────────────────────────────────────────────────────────────────

def fundo_cor(slide, cor: RGBColor):
    """Fundo sólido via API nativa."""
    bg = slide.background
    fill = bg.fill
    fill.solid()
    fill.fore_color.rgb = cor


def fundo_gradiente(slide, cor1: RGBColor, cor2: RGBColor, angulo_graus: int = 90):
    """
    Gradiente linear via API nativa do python-pptx.
    Usa fill.gradient() que é suportado na versão 1.0.
    """
    bg = slide.background
    fill = bg.fill
    fill.gradient()
    fill.gradient_angle = angulo_graus
    stops = fill.gradient_stops
    stops[0].position = 0.0
    stops[0].color.rgb = cor1
    stops[1].position = 1.0
    stops[1].color.rgb = cor2


def retangulo(slide, x, y, w, h, cor: RGBColor, sem_borda=True):
    """Retângulo colorido."""
    from pptx.enum.shapes import MSO_SHAPE_TYPE
    shape = slide.shapes.add_shape(1, x, y, w, h)  # 1 = RECTANGLE
    shape.fill.solid()
    shape.fill.fore_color.rgb = cor
    if sem_borda:
        shape.line.fill.background()
    return shape


def texto(slide, txt, x, y, w, h,
          tamanho=40, cor=BRANCO, negrito=False, italico=False,
          alinhamento=PP_ALIGN.CENTER, fonte="Calibri",
          espacamento_pct=None):
    """Caixa de texto formatada."""
    txBox = slide.shapes.add_textbox(x, y, w, h)
    tf = txBox.text_frame
    tf.word_wrap = True

    p = tf.paragraphs[0]
    p.alignment = alinhamento

    if espacamento_pct:
        pPr = p._p.get_or_add_pPr()
        lnSpc = etree.SubElement(pPr, qn('a:lnSpc'))
        spcPct = etree.SubElement(lnSpc, qn('a:spcPct'))
        spcPct.set('val', str(espacamento_pct))

    run = p.add_run()
    run.text = txt
    run.font.size = Pt(tamanho)
    run.font.color.rgb = cor
    run.font.bold = negrito
    run.font.italic = italico
    run.font.name = fonte
    return txBox


def transicao_fade(slide, duracao_ms=700):
    """Transição fade via XML mínimo e correto."""
    sld = slide._element
    ns_p = 'http://schemas.openxmlformats.org/presentationml/2006/main'
    # Remove transição existente
    for t in sld.findall(qn('p:transition')):
        sld.remove(t)
    xml = (
        f'<p:transition xmlns:p="{ns_p}" dur="{duracao_ms}" advClick="1">'
        f'<p:fade/>'
        f'</p:transition>'
    )
    sld.append(etree.fromstring(xml))


def transicao(slide, tipo='fade', duracao_ms=700):
    """
    Transições suportadas: fade, push, wipe, zoom, split, random
    """
    sld = slide._element
    ns_p = 'http://schemas.openxmlformats.org/presentationml/2006/main'
    for t in sld.findall(qn('p:transition')):
        sld.remove(t)

    inner = {
        'fade':   '<p:fade/>',
        'push':   '<p:push dir="l"/>',
        'wipe':   '<p:wipe dir="l"/>',
        'zoom':   '<p:zoom dir="in"/>',
        'split':  '<p:split orient="horz" dir="in"/>',
        'random': '<p:random/>',
        'box':    '<p:box dir="in"/>',
        'circle': '<p:circle dir="in"/>',
        'dissolve': '<p:dissolve/>',
        'strips': '<p:strips dir="lu"/>',
        'wheel':  '<p:wheel spokes="4"/>',
        'blinds': '<p:blinds dir="horz"/>',
        'checkerboard': '<p:checkerboard dir="across"/>',
        'cover':  '<p:cover dir="l"/>',
        'uncover': '<p:uncover dir="l"/>',
        'cut':    '<p:cut/>',
    }.get(tipo, '<p:fade/>')

    xml = (
        f'<p:transition xmlns:p="{ns_p}" dur="{duracao_ms}" advClick="1">'
        f'{inner}'
        f'</p:transition>'
    )
    try:
        sld.append(etree.fromstring(xml))
    except Exception as e:
        print(f"  Aviso transição '{tipo}': {e}")
        transicao_fade(slide, duracao_ms)


# ─────────────────────────────────────────────────────────────────────────────
# SLIDES
# ─────────────────────────────────────────────────────────────────────────────
print("Gerando apresentação...")

# ── SLIDE 1: Capa ─────────────────────────────────────────────────────────────
print("  Slide 1: Capa")
s1 = prs.slides.add_slide(LAYOUT)
fundo_gradiente(s1, ROXO_ESCURO, ROXO_MEDIO, angulo_graus=135)

# Faixas decorativas horizontais
retangulo(s1, Inches(0), Inches(5.6), Inches(13.33), Inches(0.14), ROSA_NEON)
retangulo(s1, Inches(0), Inches(5.85), Inches(13.33), Inches(0.07), CIANO_NEON)

# Bloco de cor atrás do título
retangulo(s1, Inches(0), Inches(1.6), Inches(13.33), Inches(2.6),
          RGBColor(0x00, 0x00, 0x00))  # sombra escura

texto(s1, "SEU TÍTULO AQUI",
      Inches(0.5), Inches(1.7), Inches(12.3), Inches(2.4),
      tamanho=80, cor=BRANCO, negrito=True,
      alinhamento=PP_ALIGN.CENTER, fonte="Impact")

texto(s1, "Uma palestra que vai mudar sua visão",
      Inches(0.5), Inches(4.3), Inches(12.3), Inches(0.9),
      tamanho=28, cor=CIANO_NEON, italico=True,
      alinhamento=PP_ALIGN.CENTER)

texto(s1, "2026  •  Seu Nome / Organização",
      Inches(0.5), Inches(6.4), Inches(12.3), Inches(0.7),
      tamanho=18, cor=CINZA_CLARO,
      alinhamento=PP_ALIGN.CENTER)

transicao(s1, 'fade', 1000)


# ── SLIDE 2: Pergunta provocadora ────────────────────────────────────────────
print("  Slide 2: Pergunta")
s2 = prs.slides.add_slide(LAYOUT)
fundo_gradiente(s2, PRETO, ROXO_ESCURO, angulo_graus=180)

# Barra lateral esquerda
retangulo(s2, Inches(0), Inches(0), Inches(0.45), Inches(7.5), ROSA_NEON)

texto(s2,
      "E SE TUDO QUE\nVOCÊ ACREDITA\nESTIVESSE ERRADO?",
      Inches(0.8), Inches(0.7), Inches(11.5), Inches(5.8),
      tamanho=64, cor=BRANCO, negrito=True,
      alinhamento=PP_ALIGN.LEFT, fonte="Impact",
      espacamento_pct=120000)

texto(s2, "🤔",
      Inches(10.5), Inches(5.5), Inches(2.3), Inches(1.5),
      tamanho=72, alinhamento=PP_ALIGN.CENTER)

transicao(s2, 'push', 600)


# ── SLIDE 3: Estatística impactante ──────────────────────────────────────────
print("  Slide 3: Estatística")
s3 = prs.slides.add_slide(LAYOUT)
fundo_cor(s3, ROXO_ESCURO)

texto(s3, "87%",
      Inches(0.5), Inches(0.3), Inches(12.3), Inches(4.2),
      tamanho=160, cor=ROSA_NEON, negrito=True,
      alinhamento=PP_ALIGN.CENTER, fonte="Impact")

retangulo(s3, Inches(2), Inches(4.6), Inches(9.3), Inches(0.08), CIANO_NEON)

texto(s3, "dos jovens nunca receberam orientação sobre isso",
      Inches(0.5), Inches(4.9), Inches(12.3), Inches(1.2),
      tamanho=30, cor=BRANCO, alinhamento=PP_ALIGN.CENTER)

texto(s3, "Fonte: Pesquisa Nacional 2025",
      Inches(0.5), Inches(6.5), Inches(12.3), Inches(0.6),
      tamanho=14, cor=CINZA_CLARO, alinhamento=PP_ALIGN.CENTER)

transicao(s3, 'zoom', 500)


# ── SLIDE 4: 3 pontos / cards ─────────────────────────────────────────────────
print("  Slide 4: 3 pontos")
s4 = prs.slides.add_slide(LAYOUT)
fundo_gradiente(s4, RGBColor(0x0A, 0x0A, 0x2E), ROXO_ESCURO, angulo_graus=90)

texto(s4, "3 COISAS QUE NINGUÉM TE CONTOU",
      Inches(0.5), Inches(0.3), Inches(12.3), Inches(1.0),
      tamanho=36, cor=AMARELO_NEON, negrito=True,
      alinhamento=PP_ALIGN.CENTER)

retangulo(s4, Inches(0.5), Inches(1.4), Inches(12.3), Inches(0.06), ROSA_NEON)

cards = [
    ("01", "VOCÊ TEM MAIS\nPODER DO QUE\nIMAGINA",    ROSA_NEON,   Inches(0.4)),
    ("02", "O SISTEMA NÃO\nFOI FEITO PRA\nTE AJUDAR", CIANO_NEON,  Inches(4.7)),
    ("03", "MAS VOCÊ PODE\nMUDAR ISSO\nAGORA",        VERDE_NEON,  Inches(9.0)),
]

for num_str, txt_card, cor_num, x in cards:
    texto(s4, num_str,
          x, Inches(1.6), Inches(3.5), Inches(1.3),
          tamanho=64, cor=cor_num, negrito=True,
          alinhamento=PP_ALIGN.CENTER, fonte="Impact")
    retangulo(s4, x + Inches(0.3), Inches(2.85), Inches(2.9), Inches(0.07), cor_num)
    texto(s4, txt_card,
          x, Inches(3.0), Inches(3.5), Inches(3.8),
          tamanho=22, cor=BRANCO, negrito=True,
          alinhamento=PP_ALIGN.CENTER, espacamento_pct=130000)

transicao(s4, 'wipe', 500)


# ── SLIDE 5: Citação ──────────────────────────────────────────────────────────
print("  Slide 5: Citação")
s5 = prs.slides.add_slide(LAYOUT)
fundo_gradiente(s5, ROSA_NEON, ROXO_MEDIO, angulo_graus=45)

# Aspas decorativas grandes
texto(s5, "\u201c",
      Inches(0.2), Inches(-0.5), Inches(3), Inches(3.5),
      tamanho=200, cor=RGBColor(0xFF, 0xFF, 0xFF),
      alinhamento=PP_ALIGN.LEFT, fonte="Georgia")

texto(s5,
      "A mudança começa\nquando você decide\nque já é suficiente.",
      Inches(1.0), Inches(1.0), Inches(11.3), Inches(4.2),
      tamanho=48, cor=BRANCO, negrito=True,
      alinhamento=PP_ALIGN.CENTER, fonte="Georgia",
      espacamento_pct=130000)

texto(s5, "— Autor / Palestrante",
      Inches(1.0), Inches(5.8), Inches(11.3), Inches(0.8),
      tamanho=22, cor=AMARELO_NEON, italico=True,
      alinhamento=PP_ALIGN.CENTER)

transicao(s5, 'fade', 800)


# ── SLIDE 6: Call to Action ───────────────────────────────────────────────────
print("  Slide 6: Call to Action")
s6 = prs.slides.add_slide(LAYOUT)
fundo_gradiente(s6, ROXO_ESCURO, PRETO, angulo_graus=180)

# Faixas neon topo e base
retangulo(s6, Inches(0), Inches(0),    Inches(13.33), Inches(0.18), ROSA_NEON)
retangulo(s6, Inches(0), Inches(7.32), Inches(13.33), Inches(0.18), CIANO_NEON)

texto(s6, "E AGORA?",
      Inches(0.5), Inches(0.7), Inches(12.3), Inches(1.6),
      tamanho=88, cor=AMARELO_NEON, negrito=True,
      alinhamento=PP_ALIGN.CENTER, fonte="Impact")

texto(s6, "A escolha é sua.\nO momento é agora.",
      Inches(0.5), Inches(2.5), Inches(12.3), Inches(2.0),
      tamanho=40, cor=BRANCO, negrito=True,
      alinhamento=PP_ALIGN.CENTER, espacamento_pct=130000)

# Botão de ação
retangulo(s6, Inches(3.5), Inches(5.0), Inches(6.3), Inches(1.0), ROSA_NEON)
texto(s6, "COMECE HOJE  →",
      Inches(3.5), Inches(5.05), Inches(6.3), Inches(0.9),
      tamanho=28, cor=BRANCO, negrito=True,
      alinhamento=PP_ALIGN.CENTER)

texto(s6, "@seuperfil  •  www.seusite.com.br",
      Inches(0.5), Inches(6.3), Inches(12.3), Inches(0.6),
      tamanho=18, cor=CINZA_CLARO, alinhamento=PP_ALIGN.CENTER)

transicao(s6, 'fade', 1000)


# ─────────────────────────────────────────────────────────────────────────────
# SALVAR
# ─────────────────────────────────────────────────────────────────────────────
caminho = r"c:\Users\Romulo\Documents\PROJETOS\MASTER_INVENTARIO\apresentacao_exemplo.pptx"
prs.save(caminho)
print(f"\n✅ Apresentação salva em:\n   {caminho}")
print(f"   6 slides | Gradientes nativos | Transições | Design neon/dark")
