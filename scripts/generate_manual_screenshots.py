"""
Gera mockups ilustrativos das telas do app SIHCP Mobile para o manual.

Cada mockup é uma imagem PNG de tela de celular (392x780px, formato 9:18)
com elementos do Material Design 3, baseada nas cores reais do app.

Saída:
    docs/manuais/imagens/*.png

Uso:
    python scripts/generate_manual_screenshots.py
"""
from __future__ import annotations

import sys
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont


# ─── Configuração ────────────────────────────────────────────────────────

PROJECT_ROOT = Path(__file__).resolve().parent.parent
OUT_DIR = PROJECT_ROOT / "docs" / "manuais" / "imagens"

# Dimensões do mockup (proporção típica de celular Android)
W, H = 392, 780

# Paleta de cores do app (extraída de colors.xml)
COLOR_PRIMARY = "#1B5EA7"
COLOR_PRIMARY_LIGHT = "#D6E3FF"
COLOR_PRIMARY_DARK = "#0D47A1"
COLOR_ON_PRIMARY = "#FFFFFF"
COLOR_SURFACE = "#FFFFFF"
COLOR_SURFACE_VARIANT = "#F5F8FC"
COLOR_OUTLINE = "#C8D6E8"
COLOR_TEXT_PRIMARY = "#191C20"
COLOR_TEXT_SECONDARY = "#43474E"
COLOR_TEXT_HINT = "#73777F"
COLOR_SUCCESS = "#2E7D32"
COLOR_WARNING = "#F57C00"
COLOR_ERROR = "#C62828"

# Fontes do Windows (usadas no PDF também)
FONT_DIR = Path(r"C:\Windows\Fonts")


def get_font(weight: str = "regular", size: int = 14) -> ImageFont.FreeTypeFont:
    """Retorna uma fonte Arial do Windows no peso e tamanho desejados."""
    font_files = {
        "regular": "arial.ttf",
        "bold": "arialbd.ttf",
        "italic": "ariali.ttf",
        "bold_italic": "arialbi.ttf",
    }
    font_path = FONT_DIR / font_files.get(weight, "arial.ttf")
    if font_path.exists():
        return ImageFont.truetype(str(font_path), size=size)
    return ImageFont.load_default()


# ─── Helpers de desenho ──────────────────────────────────────────────────

def new_canvas() -> Image.Image:
    """Cria uma tela vazia com fundo padrão."""
    return Image.new("RGB", (W, H), color=COLOR_SURFACE)


def draw_status_bar(draw: ImageDraw.ImageDraw) -> None:
    """Desenha a barra de status simulada do Android."""
    draw.rectangle((0, 0, W, 24), fill=COLOR_PRIMARY_DARK)
    f = get_font("bold", 11)
    draw.text((12, 5), "9:41", fill=COLOR_ON_PRIMARY, font=f)
    # Ícones simulados (bateria, sinal, wifi)
    draw.text((W - 95, 5), "▲ Wi-Fi  ▮▮▮▮", fill=COLOR_ON_PRIMARY, font=f)


def draw_app_bar(draw: ImageDraw.ImageDraw, title: str, with_back: bool = False) -> None:
    """Desenha a app bar (toolbar) no topo da tela."""
    draw.rectangle((0, 24, W, 80), fill=COLOR_PRIMARY)
    f = get_font("bold", 18)
    if with_back:
        # Ícone de voltar
        draw.text((16, 42), "<", fill=COLOR_ON_PRIMARY, font=get_font("bold", 24))
        draw.text((48, 47), title, fill=COLOR_ON_PRIMARY, font=f)
    else:
        # Menu hamburger
        draw.text((16, 47), "≡", fill=COLOR_ON_PRIMARY, font=get_font("bold", 22))
        draw.text((48, 47), title, fill=COLOR_ON_PRIMARY, font=f)


def draw_bottom_nav(draw: ImageDraw.ImageDraw, active: int = 0) -> None:
    """Desenha a barra de navegação inferior com 4 itens."""
    nav_h = 60
    nav_y = H - nav_h
    draw.rectangle((0, nav_y, W, H), fill=COLOR_SURFACE)
    draw.line((0, nav_y, W, nav_y), fill=COLOR_OUTLINE, width=1)

    items = [("Dashboard", "🏠"), ("Coletas", "≡"), ("Scanner", "■"), ("Sync", "⟲")]
    item_w = W // len(items)
    for i, (label, icon) in enumerate(items):
        x = i * item_w
        cx = x + item_w // 2
        color = COLOR_PRIMARY if i == active else COLOR_TEXT_HINT
        draw.text((cx - 8, nav_y + 8), icon, fill=color, font=get_font("bold", 20))
        f = get_font("regular" if i != active else "bold", 10)
        bbox = draw.textbbox((0, 0), label, font=f)
        tw = bbox[2] - bbox[0]
        draw.text((cx - tw // 2, nav_y + 36), label, fill=color, font=f)


def draw_button(
    draw: ImageDraw.ImageDraw,
    x: int, y: int, w: int, h: int,
    text: str,
    style: str = "filled",  # filled, outlined, text
    enabled: bool = True,
) -> None:
    """Desenha um botão Material Design."""
    if style == "filled":
        bg = COLOR_PRIMARY if enabled else "#9CB7D6"
        fg = COLOR_ON_PRIMARY
        draw.rounded_rectangle((x, y, x + w, y + h), radius=h // 2, fill=bg)
    elif style == "outlined":
        bg = COLOR_SURFACE
        fg = COLOR_PRIMARY if enabled else COLOR_TEXT_HINT
        outline = COLOR_PRIMARY if enabled else COLOR_TEXT_HINT
        draw.rounded_rectangle((x, y, x + w, y + h), radius=h // 2, fill=bg, outline=outline, width=2)
    else:  # text
        bg = None
        fg = COLOR_PRIMARY
        if bg:
            draw.rounded_rectangle((x, y, x + w, y + h), radius=h // 2, fill=bg)

    f = get_font("bold", 14)
    bbox = draw.textbbox((0, 0), text, font=f)
    tw, th = bbox[2] - bbox[0], bbox[3] - bbox[1]
    draw.text((x + (w - tw) // 2, y + (h - th) // 2 - 2), text, fill=fg, font=f)


def draw_input(
    draw: ImageDraw.ImageDraw,
    x: int, y: int, w: int, h: int,
    label: str,
    value: str = "",
    helper: str = "",
    icon: str = "",
) -> None:
    """Desenha um campo de entrada estilo Material Outlined TextField."""
    # Caixa
    draw.rounded_rectangle((x, y, x + w, y + h), radius=8, outline=COLOR_OUTLINE, width=2)
    # Label flutuante
    f_label = get_font("regular", 11)
    label_bg_w = draw.textbbox((0, 0), label, font=f_label)[2] + 8
    draw.rectangle((x + 12, y - 6, x + 12 + label_bg_w, y + 4), fill=COLOR_SURFACE)
    draw.text((x + 16, y - 8), label, fill=COLOR_TEXT_SECONDARY, font=f_label)
    # Ícone à esquerda (opcional)
    text_x = x + 16
    if icon:
        draw.text((x + 12, y + h // 2 - 10), icon, fill=COLOR_PRIMARY, font=get_font("bold", 16))
        text_x = x + 40
    # Valor
    if value:
        draw.text((text_x, y + h // 2 - 9), value, fill=COLOR_TEXT_PRIMARY, font=get_font("regular", 14))
    # Helper text (abaixo)
    if helper:
        draw.text((x + 12, y + h + 4), helper, fill=COLOR_TEXT_HINT, font=get_font("regular", 10))


def draw_card(
    draw: ImageDraw.ImageDraw,
    x: int, y: int, w: int, h: int,
    fill: str = COLOR_SURFACE_VARIANT,
    outline: str | None = None,
) -> None:
    """Desenha um card Material."""
    draw.rounded_rectangle(
        (x, y, x + w, y + h),
        radius=12,
        fill=fill,
        outline=outline,
        width=1 if outline else 0,
    )


def draw_chip(
    draw: ImageDraw.ImageDraw,
    x: int, y: int,
    text: str,
    selected: bool = False,
) -> int:
    """Desenha um chip de filtro. Retorna a largura usada."""
    f = get_font("bold" if selected else "regular", 12)
    bbox = draw.textbbox((0, 0), text, font=f)
    tw = bbox[2] - bbox[0]
    w = tw + 24
    h = 28
    bg = COLOR_PRIMARY_LIGHT if selected else COLOR_SURFACE
    fg = COLOR_PRIMARY if selected else COLOR_TEXT_SECONDARY
    outline = COLOR_PRIMARY if selected else COLOR_OUTLINE
    draw.rounded_rectangle((x, y, x + w, y + h), radius=14, fill=bg, outline=outline, width=1)
    draw.text((x + 12, y + 7), text, fill=fg, font=f)
    return w


# ─── Telas individuais ───────────────────────────────────────────────────

def screen_initial_setup() -> Image.Image:
    """Tela de Configuração Inicial (v2.29.5)."""
    img = new_canvas()
    d = ImageDraw.Draw(img)
    draw_status_bar(d)

    # Título no topo (sem app bar — primeira tela do app)
    cy = 70
    f_title = get_font("bold", 22)
    f_sub = get_font("regular", 13)

    # Ícone (placeholder de servidor)
    icon_y = cy
    d.rounded_rectangle((W // 2 - 40, icon_y, W // 2 + 40, icon_y + 80),
                        radius=8, outline=COLOR_PRIMARY, width=3)
    d.rectangle((W // 2 - 30, icon_y + 20, W // 2 + 30, icon_y + 30), fill=COLOR_PRIMARY)
    d.rectangle((W // 2 - 30, icon_y + 45, W // 2 + 30, icon_y + 55), fill=COLOR_PRIMARY)

    cy = icon_y + 100
    bbox = d.textbbox((0, 0), "Configuração Inicial", font=f_title)
    d.text(((W - bbox[2]) // 2, cy), "Configuração Inicial", fill=COLOR_TEXT_PRIMARY, font=f_title)
    cy += 32

    sub = "Antes de começar, informe o endereço"
    bbox = d.textbbox((0, 0), sub, font=f_sub)
    d.text(((W - bbox[2]) // 2, cy), sub, fill=COLOR_TEXT_SECONDARY, font=f_sub)
    cy += 18
    sub = "do servidor SIHCP"
    bbox = d.textbbox((0, 0), sub, font=f_sub)
    d.text(((W - bbox[2]) // 2, cy), sub, fill=COLOR_TEXT_SECONDARY, font=f_sub)
    cy += 30

    # Card de informação
    card_y = cy
    card_h = 70
    draw_card(d, 24, card_y, W - 48, card_h, fill=COLOR_PRIMARY_LIGHT)
    d.text((36, card_y + 10), "ⓘ", fill=COLOR_PRIMARY, font=get_font("bold", 18))
    info = "Solicite ao administrador o IP e a"
    d.text((60, card_y + 12), info, fill=COLOR_TEXT_PRIMARY, font=get_font("regular", 12))
    d.text((60, card_y + 28), "porta do servidor SIHCP. Esta", fill=COLOR_TEXT_PRIMARY, font=get_font("regular", 12))
    d.text((60, card_y + 44), "configuração é necessária só agora.", fill=COLOR_TEXT_PRIMARY, font=get_font("regular", 12))
    cy = card_y + card_h + 24

    # Campo IP
    draw_input(d, 24, cy, W - 48, 56, "IP do Servidor", "192.168.1.100",
               helper="Exemplo: 192.168.1.100 ou 10.14.250.214", icon="🌐")
    cy += 92

    # Campo Porta
    draw_input(d, 24, cy, W - 48, 56, "Porta", "8081", helper="Padrão: 8081", icon="⚙")
    cy += 92

    # Status do teste (sucesso)
    d.text((24, cy), "✓", fill=COLOR_SUCCESS, font=get_font("bold", 16))
    d.text((44, cy + 1), "Conexão estabelecida (142 ms)", fill=COLOR_SUCCESS, font=get_font("bold", 13))
    cy += 32

    # Botão Testar Conexão (outlined)
    draw_button(d, 24, cy, W - 48, 44, "Testar Conexão", style="outlined")
    cy += 56

    # Botão Continuar (filled)
    draw_button(d, 24, cy, W - 48, 44, "Continuar →", style="filled", enabled=True)

    return img


def screen_login() -> Image.Image:
    """Tela de Login."""
    img = new_canvas()
    d = ImageDraw.Draw(img)
    draw_status_bar(d)

    cy = 60

    # Logo
    d.rounded_rectangle((W // 2 - 40, cy, W // 2 + 40, cy + 80),
                        radius=12, fill=COLOR_PRIMARY)
    f_logo = get_font("bold", 28)
    bbox = d.textbbox((0, 0), "S", font=f_logo)
    d.text(((W - bbox[2]) // 2, cy + 20), "S", fill=COLOR_ON_PRIMARY, font=f_logo)
    cy += 100

    # Título
    f_title = get_font("bold", 22)
    bbox = d.textbbox((0, 0), "SIHCP Mobile", font=f_title)
    d.text(((W - bbox[2]) // 2, cy), "SIHCP Mobile", fill=COLOR_TEXT_PRIMARY, font=f_title)
    cy += 30

    sub = "Faça login para continuar"
    f_sub = get_font("regular", 13)
    bbox = d.textbbox((0, 0), sub, font=f_sub)
    d.text(((W - bbox[2]) // 2, cy), sub, fill=COLOR_TEXT_SECONDARY, font=f_sub)
    cy += 40

    # Campo IP do Servidor
    draw_input(d, 24, cy, W - 48, 56, "IP do Servidor", "10.14.250.214", icon="🌐")
    cy += 76

    # Campo Login
    draw_input(d, 24, cy, W - 48, 56, "Login", "joao.silva", icon="👤")
    cy += 76

    # Campo Senha
    draw_input(d, 24, cy, W - 48, 56, "Senha", "••••••••", icon="🔒")
    cy += 76

    # Botão Entrar
    draw_button(d, 24, cy, W - 48, 48, "Entrar", style="filled")
    cy += 64

    # Botão Biometria (FAB-like)
    fab_x = W - 80
    fab_y = cy + 10
    d.ellipse((fab_x, fab_y, fab_x + 56, fab_y + 56), fill=COLOR_PRIMARY)
    d.text((fab_x + 18, fab_y + 14), "👆", fill=COLOR_ON_PRIMARY, font=get_font("bold", 24))

    # Versão (rodapé)
    f_ver = get_font("regular", 10)
    bbox = d.textbbox((0, 0), "Versão 2.29.5", font=f_ver)
    d.text(((W - bbox[2]) // 2, H - 30), "Versão 2.29.5", fill=COLOR_TEXT_HINT, font=f_ver)

    return img


def screen_dashboard() -> Image.Image:
    """Tela inicial (Dashboard)."""
    img = new_canvas()
    d = ImageDraw.Draw(img)
    draw_status_bar(d)
    draw_app_bar(d, "Dashboard")

    cy = 96

    # Card do Inventário Ativo
    draw_card(d, 16, cy, W - 32, 80, fill=COLOR_PRIMARY_LIGHT)
    d.text((28, cy + 10), "📋 Inventário Ativo", fill=COLOR_PRIMARY, font=get_font("bold", 12))
    d.text((28, cy + 30), "Inventário 2026", fill=COLOR_TEXT_PRIMARY, font=get_font("bold", 16))
    d.text((28, cy + 54), "Iniciado em 15/04/2026 — Em andamento",
           fill=COLOR_TEXT_SECONDARY, font=get_font("regular", 11))
    cy += 96

    # Estatísticas (3 cards lado a lado)
    card_w = (W - 48) // 3
    stats = [
        ("Hoje", "23", COLOR_SUCCESS),
        ("Total", "187", COLOR_PRIMARY),
        ("Pendentes", "5", COLOR_WARNING),
    ]
    for i, (label, value, color) in enumerate(stats):
        x = 16 + i * (card_w + 8)
        draw_card(d, x, cy, card_w, 80, outline=COLOR_OUTLINE)
        f_val = get_font("bold", 24)
        bbox = d.textbbox((0, 0), value, font=f_val)
        tw = bbox[2] - bbox[0]
        d.text((x + (card_w - tw) // 2, cy + 12), value, fill=color, font=f_val)
        f_lbl = get_font("regular", 11)
        bbox = d.textbbox((0, 0), label, font=f_lbl)
        tw = bbox[2] - bbox[0]
        d.text((x + (card_w - tw) // 2, cy + 50), label, fill=COLOR_TEXT_SECONDARY, font=f_lbl)
    cy += 96

    # Progresso do inventário
    draw_card(d, 16, cy, W - 32, 70, outline=COLOR_OUTLINE)
    d.text((28, cy + 10), "Progresso do Inventário", fill=COLOR_TEXT_PRIMARY, font=get_font("bold", 13))
    # Barra de progresso
    bar_x = 28
    bar_y = cy + 38
    bar_w = W - 56
    bar_h = 10
    d.rounded_rectangle((bar_x, bar_y, bar_x + bar_w, bar_y + bar_h),
                        radius=5, fill=COLOR_OUTLINE)
    pct = 0.62
    d.rounded_rectangle((bar_x, bar_y, bar_x + int(bar_w * pct), bar_y + bar_h),
                        radius=5, fill=COLOR_PRIMARY)
    d.text((bar_x, bar_y + 18), "62% — 1.245 / 2.010 patrimônios",
           fill=COLOR_TEXT_SECONDARY, font=get_font("regular", 11))
    cy += 86

    # Botões grandes de ação
    actions = [
        ("📷 Nova Coleta (QR Code)", COLOR_PRIMARY),
        ("⊞ Coleta Manual", COLOR_PRIMARY),
        ("🏷 Item Sem Etiqueta", COLOR_WARNING),
    ]
    for label, color in actions:
        draw_card(d, 16, cy, W - 32, 56, outline=COLOR_OUTLINE)
        d.text((28, cy + 18), label, fill=color, font=get_font("bold", 14))
        d.text((W - 36, cy + 18), "›", fill=color, font=get_font("bold", 18))
        cy += 64

    draw_bottom_nav(d, active=0)
    return img


def screen_scanner() -> Image.Image:
    """Tela do Scanner QR Code."""
    img = new_canvas()
    d = ImageDraw.Draw(img)
    draw_status_bar(d)
    draw_app_bar(d, "Scanner QR Code", with_back=True)

    # Fundo escuro simulando câmera
    d.rectangle((0, 80, W, H - 60), fill="#1a1a1a")

    # Frame de leitura
    frame_size = 240
    fx = (W - frame_size) // 2
    fy = 200
    # Cantos do frame
    corner = 30
    d.line((fx, fy, fx + corner, fy), fill=COLOR_PRIMARY, width=4)
    d.line((fx, fy, fx, fy + corner), fill=COLOR_PRIMARY, width=4)
    d.line((fx + frame_size, fy, fx + frame_size - corner, fy), fill=COLOR_PRIMARY, width=4)
    d.line((fx + frame_size, fy, fx + frame_size, fy + corner), fill=COLOR_PRIMARY, width=4)
    d.line((fx, fy + frame_size, fx + corner, fy + frame_size), fill=COLOR_PRIMARY, width=4)
    d.line((fx, fy + frame_size, fx, fy + frame_size - corner), fill=COLOR_PRIMARY, width=4)
    d.line((fx + frame_size, fy + frame_size, fx + frame_size - corner, fy + frame_size),
           fill=COLOR_PRIMARY, width=4)
    d.line((fx + frame_size, fy + frame_size, fx + frame_size, fy + frame_size - corner),
           fill=COLOR_PRIMARY, width=4)

    # QR Code simulado dentro do frame
    qr_pad = 30
    qr_x, qr_y = fx + qr_pad, fy + qr_pad
    qr_s = frame_size - 2 * qr_pad
    d.rectangle((qr_x, qr_y, qr_x + qr_s, qr_y + qr_s), fill="#FFFFFF")
    # Padrão simples de QR
    cell = qr_s // 12
    pattern = [
        "1110111000101",
        "1010101100111",
        "1110100011001",
        "0001110100110",
        "1101001001101",
        "1010111110010",
        "0110100101101",
        "1011010011001",
        "0101111100110",
        "1110101100011",
        "1011110011010",
        "0110010101111",
    ]
    for r, row in enumerate(pattern[:12]):
        for c, ch in enumerate(row[:12]):
            if ch == "1":
                d.rectangle(
                    (qr_x + c * cell, qr_y + r * cell,
                     qr_x + (c + 1) * cell, qr_y + (r + 1) * cell),
                    fill="#000000",
                )

    # Linha de scan animada (simulada)
    d.line((fx + 10, fy + frame_size // 2, fx + frame_size - 10, fy + frame_size // 2),
           fill=COLOR_PRIMARY, width=2)

    # Texto de instrução
    f_inst = get_font("bold", 14)
    inst = "Aponte para o QR Code do patrimônio"
    bbox = d.textbbox((0, 0), inst, font=f_inst)
    d.text(((W - bbox[2]) // 2, 130), inst, fill=COLOR_ON_PRIMARY, font=f_inst)

    # Botões inferiores (lanterna, manual)
    btn_y = fy + frame_size + 40
    # Lanterna
    d.ellipse((W // 2 - 80, btn_y, W // 2 - 30, btn_y + 50), fill="#333333", outline="#555555", width=1)
    d.text((W // 2 - 65, btn_y + 12), "💡", fill=COLOR_ON_PRIMARY, font=get_font("bold", 22))
    # Coleta manual
    d.ellipse((W // 2 + 30, btn_y, W // 2 + 80, btn_y + 50), fill="#333333", outline="#555555", width=1)
    d.text((W // 2 + 45, btn_y + 12), "✎", fill=COLOR_ON_PRIMARY, font=get_font("bold", 22))

    # Labels dos botões
    f_lbl = get_font("regular", 10)
    bbox = d.textbbox((0, 0), "Lanterna", font=f_lbl)
    d.text((W // 2 - 55 - bbox[2] // 2, btn_y + 56), "Lanterna",
           fill=COLOR_ON_PRIMARY, font=f_lbl)
    bbox = d.textbbox((0, 0), "Manual", font=f_lbl)
    d.text((W // 2 + 55 - bbox[2] // 2, btn_y + 56), "Manual",
           fill=COLOR_ON_PRIMARY, font=f_lbl)

    draw_bottom_nav(d, active=2)
    return img


def screen_sala_selection() -> Image.Image:
    """Seleção de sala."""
    img = new_canvas()
    d = ImageDraw.Draw(img)
    draw_status_bar(d)
    draw_app_bar(d, "Selecione a Sala", with_back=True)

    cy = 96
    # Switch "Fixar Estado"
    draw_card(d, 16, cy, W - 32, 56, outline=COLOR_OUTLINE)
    d.text((28, cy + 18), "🎯 Fixar Estado", fill=COLOR_TEXT_PRIMARY, font=get_font("bold", 13))
    d.text((W - 80, cy + 8), "Bom", fill=COLOR_TEXT_SECONDARY, font=get_font("regular", 11))
    # Switch ON
    sw_x = W - 56
    sw_y = cy + 22
    d.rounded_rectangle((sw_x, sw_y, sw_x + 32, sw_y + 18), radius=9, fill=COLOR_PRIMARY)
    d.ellipse((sw_x + 16, sw_y + 1, sw_x + 32, sw_y + 17), fill=COLOR_SURFACE)
    cy += 72

    # Campo de busca
    draw_input(d, 16, cy, W - 32, 48, "🔍 Buscar sala", "")
    cy += 72

    # Lista de salas
    salas = [
        ("📌 Sala 101 — Laboratório de Informática", True),
        ("Sala 102 — Biblioteca", False),
        ("Sala 201 — Diretoria", False),
        ("Sala 203 — Coordenação Pedagógica", False),
        ("Sala 301 — Departamento de TI", False),
        ("Sala 302 — Sala dos Professores", False),
    ]
    for sala, fixed in salas:
        bg = COLOR_PRIMARY_LIGHT if fixed else COLOR_SURFACE
        outline = COLOR_PRIMARY if fixed else COLOR_OUTLINE
        draw_card(d, 16, cy, W - 32, 50, fill=bg, outline=outline)
        f = get_font("bold" if fixed else "regular", 12)
        color = COLOR_PRIMARY if fixed else COLOR_TEXT_PRIMARY
        d.text((28, cy + 18), sala, fill=color, font=f)
        d.text((W - 36, cy + 18), "›", fill=color, font=get_font("bold", 14))
        cy += 58
        if cy > H - 100:
            break

    return img


def screen_collection_form() -> Image.Image:
    """Formulário de coleta após escanear QR Code."""
    img = new_canvas()
    d = ImageDraw.Draw(img)
    draw_status_bar(d)
    draw_app_bar(d, "Registrar Coleta", with_back=True)

    cy = 96

    # Card com dados do patrimônio identificado
    draw_card(d, 16, cy, W - 32, 110, fill=COLOR_PRIMARY_LIGHT)
    d.text((28, cy + 10), "✓ Patrimônio identificado", fill=COLOR_SUCCESS, font=get_font("bold", 12))
    d.text((28, cy + 30), "Nº 361143", fill=COLOR_TEXT_PRIMARY, font=get_font("bold", 18))
    d.text((28, cy + 56), "Notebook Dell Latitude 7400", fill=COLOR_TEXT_PRIMARY, font=get_font("regular", 13))
    d.text((28, cy + 76), "Sala original: 301 — TI", fill=COLOR_TEXT_SECONDARY, font=get_font("regular", 11))
    d.text((28, cy + 92), "Resp.: Maria Santos", fill=COLOR_TEXT_SECONDARY, font=get_font("regular", 11))
    cy += 126

    # Campo Estado de Conservação (dropdown)
    draw_input(d, 16, cy, W - 32, 56, "Estado de Conservação", "Bom ▾", icon="📊")
    cy += 76

    # Campo Localização Encontrada
    draw_input(d, 16, cy, W - 32, 56, "Localização Encontrada", "Sala 101", icon="📍")
    cy += 76

    # Campo Observação
    draw_input(d, 16, cy, W - 32, 80, "Observação (opcional)",
               "Equipamento em uso pelo professor", icon="✎")
    cy += 100

    # Botão Foto (opcional)
    draw_button(d, 16, cy, (W - 40) // 2, 44, "📷 Tirar Foto", style="outlined")
    # Botão Registrar
    draw_button(d, 16 + (W - 40) // 2 + 8, cy, (W - 40) // 2, 44, "✓ Registrar", style="filled")

    return img


def screen_collections_list() -> Image.Image:
    """Lista de coletas com filtros."""
    img = new_canvas()
    d = ImageDraw.Draw(img)
    draw_status_bar(d)
    draw_app_bar(d, "Coletas")

    cy = 96
    # Estatísticas resumidas
    d.text((16, cy), "Total: 187   |   Pendentes: 5", fill=COLOR_TEXT_SECONDARY,
           font=get_font("bold", 12))
    cy += 22

    # Chips de filtro
    chip_x = 16
    chip_y = cy
    chips = [("Todos", True), ("Sincronizados", False), ("Pendentes", False), ("Sem Etiqueta", False)]
    for label, sel in chips:
        used = draw_chip(d, chip_x, chip_y, label, selected=sel)
        chip_x += used + 6
    cy += 42

    # Spinner de sala
    draw_input(d, 16, cy, W - 32, 44, "Sala", "Sala 101 — Laboratório ▾", icon="🏢")
    cy += 60

    # Lista de coletas
    coletas = [
        ("361143", "Notebook Dell Latitude 7400", "Bom", "Sincronizado", COLOR_SUCCESS, "✓"),
        ("361200", "Cadeira giratória preta", "Bom", "Sincronizado", COLOR_SUCCESS, "✓"),
        ("361341", "Mesa de reunião", "Regular", "Sincronizado", COLOR_SUCCESS, "✓"),
        ("361428", "Monitor LG 24''", "Bom", "Pendente", COLOR_WARNING, "⏱"),
        ("361354", "Impressora HP M404", "Bom", "Pendente", COLOR_WARNING, "⏱"),
        ("Sem etiq.", "Cadeira sem identificação", "Regular", "Pendente", COLOR_WARNING, "🏷"),
    ]
    for num, desc, estado, status, color, icon in coletas:
        if cy > H - 110:
            break
        draw_card(d, 16, cy, W - 32, 64, outline=COLOR_OUTLINE)
        d.text((28, cy + 8), num, fill=COLOR_PRIMARY, font=get_font("bold", 13))
        d.text((28, cy + 26), desc[:32], fill=COLOR_TEXT_PRIMARY, font=get_font("regular", 12))
        d.text((28, cy + 44), f"Estado: {estado}", fill=COLOR_TEXT_SECONDARY, font=get_font("regular", 10))
        d.text((W - 50, cy + 8), icon, fill=color, font=get_font("bold", 16))
        f_st = get_font("regular", 9)
        bbox = d.textbbox((0, 0), status, font=f_st)
        tw = bbox[2] - bbox[0]
        d.text((W - 36 - tw, cy + 30), status, fill=color, font=f_st)
        cy += 72

    draw_bottom_nav(d, active=1)
    return img


def screen_sync() -> Image.Image:
    """Tela de Sincronização."""
    img = new_canvas()
    d = ImageDraw.Draw(img)
    draw_status_bar(d)
    draw_app_bar(d, "Sincronização")

    cy = 96

    # Status da conexão
    draw_card(d, 16, cy, W - 32, 56, fill="#E8F5E9", outline=COLOR_SUCCESS)
    d.text((28, cy + 18), "✓ Conectado ao servidor",
           fill=COLOR_SUCCESS, font=get_font("bold", 14))
    cy += 72

    # Pendências
    draw_card(d, 16, cy, W - 32, 130, outline=COLOR_OUTLINE)
    d.text((28, cy + 12), "Coletas Pendentes", fill=COLOR_TEXT_SECONDARY, font=get_font("bold", 12))
    f_big = get_font("bold", 36)
    d.text((28, cy + 30), "5", fill=COLOR_WARNING, font=f_big)
    d.text((68, cy + 50), "coletas aguardando envio",
           fill=COLOR_TEXT_SECONDARY, font=get_font("regular", 11))
    d.text((28, cy + 96), "Última sync: 14/05/2026 às 09:23",
           fill=COLOR_TEXT_HINT, font=get_font("regular", 11))
    cy += 146

    # Botão sincronizar
    draw_button(d, 16, cy, W - 32, 48, "⟲  Sincronizar Agora", style="filled")
    cy += 64

    # Estatísticas
    draw_card(d, 16, cy, W - 32, 100, outline=COLOR_OUTLINE)
    d.text((28, cy + 12), "Estatísticas", fill=COLOR_TEXT_PRIMARY, font=get_font("bold", 13))
    d.text((28, cy + 36), "✓ Total enviado hoje:", fill=COLOR_TEXT_SECONDARY, font=get_font("regular", 11))
    d.text((180, cy + 36), "23 coletas", fill=COLOR_TEXT_PRIMARY, font=get_font("bold", 11))
    d.text((28, cy + 56), "✗ Falhas:", fill=COLOR_TEXT_SECONDARY, font=get_font("regular", 11))
    d.text((180, cy + 56), "0", fill=COLOR_SUCCESS, font=get_font("bold", 11))
    d.text((28, cy + 76), "📊 Taxa de sucesso:", fill=COLOR_TEXT_SECONDARY, font=get_font("regular", 11))
    d.text((180, cy + 76), "100%", fill=COLOR_SUCCESS, font=get_font("bold", 11))
    cy += 116

    # Sync automático
    draw_card(d, 16, cy, W - 32, 56, outline=COLOR_OUTLINE)
    d.text((28, cy + 8), "🔄 Sync automática", fill=COLOR_TEXT_PRIMARY, font=get_font("bold", 12))
    d.text((28, cy + 28), "A cada 30 minutos com Wi-Fi",
           fill=COLOR_TEXT_SECONDARY, font=get_font("regular", 11))
    # Switch ON
    sw_x = W - 56
    sw_y = cy + 22
    d.rounded_rectangle((sw_x, sw_y, sw_x + 32, sw_y + 18), radius=9, fill=COLOR_PRIMARY)
    d.ellipse((sw_x + 16, sw_y + 1, sw_x + 32, sw_y + 17), fill=COLOR_SURFACE)

    draw_bottom_nav(d, active=3)
    return img


def screen_item_sem_etiqueta() -> Image.Image:
    """Coleta de item sem etiqueta."""
    img = new_canvas()
    d = ImageDraw.Draw(img)
    draw_status_bar(d)
    draw_app_bar(d, "Item Sem Etiqueta", with_back=True)

    cy = 96

    # Aviso
    draw_card(d, 16, cy, W - 32, 56, fill="#FFF3E0", outline=COLOR_WARNING)
    d.text((28, cy + 8), "⚠  Foto obrigatória",
           fill=COLOR_WARNING, font=get_font("bold", 13))
    d.text((28, cy + 30), "Itens sem etiqueta exigem evidência",
           fill=COLOR_TEXT_SECONDARY, font=get_font("regular", 11))
    cy += 72

    # Foto preview
    draw_card(d, 16, cy, W - 32, 100, fill=COLOR_SURFACE_VARIANT, outline=COLOR_OUTLINE)
    d.text((W // 2 - 30, cy + 36), "📷", fill=COLOR_TEXT_HINT, font=get_font("bold", 36))
    d.text((W // 2 - 50, cy + 78), "Tirar foto", fill=COLOR_TEXT_HINT, font=get_font("regular", 11))
    cy += 116

    # Campos
    draw_input(d, 16, cy, W - 32, 56, "Descrição *", "Cadeira giratória preta", icon="✎")
    cy += 76

    draw_input(d, 16, cy, W - 32, 56, "Categoria *", "Mobiliário ▾", icon="📂")
    cy += 76

    draw_input(d, 16, cy, W - 32, 56, "Estado *", "Bom ▾", icon="📊")
    cy += 76

    draw_input(d, 16, cy, W - 32, 56, "Localização *", "Sala 101", icon="📍")
    cy += 76

    # Botão
    draw_button(d, 16, cy, W - 32, 44, "✓ Registrar", style="filled")

    return img


# ─── Diagrama de fluxo de uso ────────────────────────────────────────────

def diagram_first_use_flow() -> Image.Image:
    """Diagrama de fluxo da primeira utilização."""
    DW, DH = 900, 600
    img = Image.new("RGB", (DW, DH), color=COLOR_SURFACE)
    d = ImageDraw.Draw(img)

    # Título
    f_title = get_font("bold", 22)
    title = "Fluxo de Primeira Utilização (v2.29.5)"
    bbox = d.textbbox((0, 0), title, font=f_title)
    d.text(((DW - bbox[2]) // 2, 20), title, fill=COLOR_PRIMARY, font=f_title)

    # Caixas do fluxo
    boxes = [
        (50, 90, 200, 60, "Splash 5s", COLOR_PRIMARY_LIGHT, COLOR_PRIMARY),
        (300, 90, 200, 60, "Servidor configurado?", "#FFF3E0", COLOR_WARNING),
        (550, 30, 320, 100, "InitialSetupActivity\n• Digita IP/Porta\n• Testa Conexão",
         "#E8F5E9", COLOR_SUCCESS),
        (550, 160, 320, 60, "LoginActivity", COLOR_PRIMARY_LIGHT, COLOR_PRIMARY),
        (300, 280, 200, 60, "Login + Senha", COLOR_PRIMARY_LIGHT, COLOR_PRIMARY),
        (300, 380, 200, 60, "Biometria opcional", "#FFF3E0", COLOR_WARNING),
        (300, 480, 200, 60, "Dashboard", COLOR_PRIMARY_LIGHT, COLOR_PRIMARY),
    ]

    f_box = get_font("bold", 14)
    for x, y, w, h, text, fill, outline in boxes:
        d.rounded_rectangle((x, y, x + w, y + h), radius=10, fill=fill, outline=outline, width=2)
        # Texto centralizado, suportando \n
        lines = text.split("\n")
        line_h = 18
        total_h = len(lines) * line_h
        ty = y + (h - total_h) // 2
        for line in lines:
            bbox = d.textbbox((0, 0), line, font=f_box)
            tw = bbox[2] - bbox[0]
            d.text((x + (w - tw) // 2, ty), line, fill=COLOR_TEXT_PRIMARY,
                   font=get_font("bold" if line == lines[0] else "regular", 13))
            ty += line_h

    # Setas
    arrows = [
        # (x1, y1, x2, y2)
        (250, 120, 300, 120),  # Splash -> Configurado?
        (500, 120, 550, 80),   # Configurado? NÃO -> Setup
        (500, 120, 550, 190),  # Configurado? SIM -> Login
        (710, 130, 710, 160),  # Setup -> Login
        (710, 220, 600, 280),  # Login -> Login+Senha (volta para o centro)
        (400, 340, 400, 380),  # Login+Senha -> Biometria
        (400, 440, 400, 480),  # Biometria -> Dashboard
    ]
    for x1, y1, x2, y2 in arrows:
        d.line((x1, y1, x2, y2), fill=COLOR_PRIMARY, width=2)
        # Cabeça da seta
        ah = 6
        if x2 > x1:
            d.polygon([(x2, y2), (x2 - ah, y2 - ah), (x2 - ah, y2 + ah)], fill=COLOR_PRIMARY)
        elif x2 < x1:
            d.polygon([(x2, y2), (x2 + ah, y2 - ah), (x2 + ah, y2 + ah)], fill=COLOR_PRIMARY)
        elif y2 > y1:
            d.polygon([(x2, y2), (x2 - ah, y2 - ah), (x2 + ah, y2 - ah)], fill=COLOR_PRIMARY)
        else:
            d.polygon([(x2, y2), (x2 - ah, y2 + ah), (x2 + ah, y2 + ah)], fill=COLOR_PRIMARY)

    # Labels nas setas de decisão
    f_dec = get_font("bold", 11)
    d.text((520, 60), "NÃO", fill=COLOR_ERROR, font=f_dec)
    d.text((520, 170), "SIM", fill=COLOR_SUCCESS, font=f_dec)

    return img


def diagram_offline_online_sync() -> Image.Image:
    """Diagrama da estratégia híbrida de duplicatas."""
    DW, DH = 900, 600
    img = Image.new("RGB", (DW, DH), color=COLOR_SURFACE)
    d = ImageDraw.Draw(img)

    # Título
    f_title = get_font("bold", 22)
    title = "Verificação de Duplicatas — Online vs Offline"
    bbox = d.textbbox((0, 0), title, font=f_title)
    d.text(((DW - bbox[2]) // 2, 20), title, fill=COLOR_PRIMARY, font=f_title)

    # Caixa de tentativa de coleta (topo)
    d.rounded_rectangle((350, 80, 550, 130), radius=10, fill=COLOR_PRIMARY_LIGHT,
                        outline=COLOR_PRIMARY, width=2)
    f = get_font("bold", 14)
    bbox = d.textbbox((0, 0), "Tentativa de Coleta", font=f)
    d.text((450 - bbox[2] // 2, 100), "Tentativa de Coleta", fill=COLOR_TEXT_PRIMARY, font=f)

    # Decisão online/offline
    d.polygon([(450, 170), (550, 220), (450, 270), (350, 220)],
              fill="#FFF3E0", outline=COLOR_WARNING, width=2)
    bbox = d.textbbox((0, 0), "Online?", font=f)
    d.text((450 - bbox[2] // 2, 215), "Online?", fill=COLOR_TEXT_PRIMARY, font=f)

    # Branch ONLINE (esquerda)
    d.rounded_rectangle((50, 310, 350, 410), radius=10, fill="#E8F5E9",
                        outline=COLOR_SUCCESS, width=2)
    d.text((90, 320), "🌐 ONLINE", fill=COLOR_SUCCESS, font=get_font("bold", 14))
    d.text((70, 350), "Consulta servidor", fill=COLOR_TEXT_PRIMARY, font=get_font("regular", 12))
    d.text((70, 370), "(VerificarDuplicataUseCase)", fill=COLOR_TEXT_SECONDARY, font=get_font("regular", 10))
    d.text((70, 390), "→ Permite recoletar excluídos", fill=COLOR_TEXT_PRIMARY, font=get_font("bold", 11))

    # Branch OFFLINE (direita)
    d.rounded_rectangle((550, 310, 850, 410), radius=10, fill="#FFE8E8",
                        outline=COLOR_ERROR, width=2)
    d.text((590, 320), "📵 OFFLINE", fill=COLOR_ERROR, font=get_font("bold", 14))
    d.text((570, 350), "Consulta cache local (Room)", fill=COLOR_TEXT_PRIMARY, font=get_font("regular", 12))
    d.text((570, 370), "(buscarColetaExistente)", fill=COLOR_TEXT_SECONDARY, font=get_font("regular", 10))
    d.text((570, 390), "→ Bloqueia duplicatas reais", fill=COLOR_TEXT_PRIMARY, font=get_font("bold", 11))

    # Caixa de resultado (bottom)
    d.rounded_rectangle((250, 470, 650, 530), radius=10, fill=COLOR_PRIMARY_LIGHT,
                        outline=COLOR_PRIMARY, width=2)
    bbox = d.textbbox((0, 0), "Coleta Registrada Localmente", font=f)
    d.text((450 - bbox[2] // 2, 492), "Coleta Registrada Localmente",
           fill=COLOR_TEXT_PRIMARY, font=f)

    # Setas
    def arrow(x1, y1, x2, y2, color=COLOR_PRIMARY):
        d.line((x1, y1, x2, y2), fill=color, width=2)
        ah = 6
        if x2 > x1 and y2 > y1:
            d.polygon([(x2, y2), (x2 - ah, y2 - ah // 2), (x2 - ah // 2, y2 - ah)], fill=color)
        elif x2 < x1 and y2 > y1:
            d.polygon([(x2, y2), (x2 + ah, y2 - ah // 2), (x2 + ah // 2, y2 - ah)], fill=color)
        elif y2 > y1:
            d.polygon([(x2, y2), (x2 - ah, y2 - ah), (x2 + ah, y2 - ah)], fill=color)

    arrow(450, 130, 450, 170)        # Tentativa -> Decisão
    arrow(350, 230, 200, 310)         # Decisão -> Online (esquerda)
    arrow(550, 230, 700, 310)         # Decisão -> Offline (direita)
    arrow(200, 410, 350, 470)         # Online -> Resultado
    arrow(700, 410, 550, 470)         # Offline -> Resultado

    # Labels SIM/NÃO
    f_dec = get_font("bold", 12)
    d.text((250, 260), "SIM", fill=COLOR_SUCCESS, font=f_dec)
    d.text((640, 260), "NÃO", fill=COLOR_ERROR, font=f_dec)

    # Nota inferior
    note = "Reconexão: SincronizarColetasDoServidorUseCase remove coletas excluídas no servidor do cache local"
    f_note = get_font("italic", 10)
    bbox = d.textbbox((0, 0), note, font=f_note)
    d.text(((DW - bbox[2]) // 2, 555), note, fill=COLOR_TEXT_SECONDARY, font=f_note)

    return img


# ─── Geração principal ───────────────────────────────────────────────────

def main() -> int:
    OUT_DIR.mkdir(parents=True, exist_ok=True)

    screens = {
        "01_configuracao_inicial.png": screen_initial_setup,
        "02_login.png": screen_login,
        "03_dashboard.png": screen_dashboard,
        "04_scanner.png": screen_scanner,
        "05_selecao_sala.png": screen_sala_selection,
        "06_formulario_coleta.png": screen_collection_form,
        "07_lista_coletas.png": screen_collections_list,
        "08_sincronizacao.png": screen_sync,
        "09_item_sem_etiqueta.png": screen_item_sem_etiqueta,
        "10_diagrama_primeiro_uso.png": diagram_first_use_flow,
        "11_diagrama_online_offline.png": diagram_offline_online_sync,
    }

    for filename, fn in screens.items():
        out_path = OUT_DIR / filename
        try:
            img = fn()
            img.save(out_path, "PNG", optimize=True)
            size_kb = out_path.stat().st_size / 1024
            print(f"OK  {filename}  ({size_kb:.1f} KB)")
        except Exception as exc:
            print(f"ERR {filename}: {exc}")
            import traceback
            traceback.print_exc()

    print(f"\n{len(screens)} imagens geradas em {OUT_DIR}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
