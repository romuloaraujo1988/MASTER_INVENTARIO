"""
Gera PDF do Manual do Coletor a partir do arquivo Markdown.

Uso:
    python scripts/generate_manual_pdf.py

Saída:
    docs/manuais/MANUAL_COLETOR.pdf
"""
import sys
from pathlib import Path

import markdown
from xhtml2pdf import pisa
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont


PROJECT_ROOT = Path(__file__).resolve().parent.parent
MD_PATH = PROJECT_ROOT / "docs" / "manuais" / "MANUAL_COLETOR.md"
PDF_PATH = PROJECT_ROOT / "docs" / "manuais" / "MANUAL_COLETOR.pdf"


def register_fonts() -> str:
    """
    Registra fontes TTF do Windows para suportar UTF-8 no PDF.
    Retorna o nome da família de fonte registrada (ou "Helvetica" se não conseguir).
    """
    windows_fonts = Path(r"C:\Windows\Fonts")
    candidates = [
        ("DejaVuSans", "DejaVuSans.ttf", "DejaVuSans-Bold.ttf",
         "DejaVuSans-Oblique.ttf", "DejaVuSans-BoldOblique.ttf"),
        ("Arial", "arial.ttf", "arialbd.ttf", "ariali.ttf", "arialbi.ttf"),
        ("Calibri", "calibri.ttf", "calibrib.ttf", "calibrii.ttf", "calibriz.ttf"),
    ]

    for family, regular, bold, italic, bold_italic in candidates:
        regular_path = windows_fonts / regular
        if not regular_path.exists():
            continue
        try:
            pdfmetrics.registerFont(TTFont(family, str(regular_path)))
            bold_path = windows_fonts / bold
            italic_path = windows_fonts / italic
            bi_path = windows_fonts / bold_italic
            if bold_path.exists():
                pdfmetrics.registerFont(TTFont(f"{family}-Bold", str(bold_path)))
            if italic_path.exists():
                pdfmetrics.registerFont(TTFont(f"{family}-Italic", str(italic_path)))
            if bi_path.exists():
                pdfmetrics.registerFont(TTFont(f"{family}-BoldItalic", str(bi_path)))
            # Mapping para que o reportlab saiba quais variantes usar
            from reportlab.pdfbase.pdfmetrics import registerFontFamily
            registerFontFamily(
                family,
                normal=family,
                bold=f"{family}-Bold" if bold_path.exists() else family,
                italic=f"{family}-Italic" if italic_path.exists() else family,
                boldItalic=f"{family}-BoldItalic" if bi_path.exists() else family,
            )
            print(f"Fonte registrada: {family}")
            return family
        except Exception as exc:
            print(f"Falha ao registrar {family}: {exc}")
            continue

    print("AVISO: nenhuma fonte TTF registrada. Usando Helvetica padrão (sem suporte completo a UTF-8).")
    return "Helvetica"


FONT_FAMILY = register_fonts()


CSS = f"""
@page {{
    size: A4;
    margin: 2.5cm 2cm 2cm 2cm;
    @frame footer_frame {{
        -pdf-frame-content: footer_content;
        left: 50pt; width: 512pt; top: 780pt; height: 40pt;
    }}
}}

body {{
    font-family: "{FONT_FAMILY}", Helvetica, Arial, sans-serif;
    font-size: 11pt;
    line-height: 1.5;
    color: #222;
}}

h1 {{
    font-family: "{FONT_FAMILY}", Helvetica, Arial, sans-serif;
    color: #1a4d8c;
    font-size: 22pt;
    border-bottom: 3px solid #1a4d8c;
    padding-bottom: 6px;
    margin-top: 24pt;
    margin-bottom: 12pt;
    page-break-before: always;
}}

h1:first-of-type {{
    page-break-before: avoid;
}}

h2 {{
    font-family: "{FONT_FAMILY}", Helvetica, Arial, sans-serif;
    color: #2c6db5;
    font-size: 16pt;
    border-bottom: 1px solid #c8d6e8;
    padding-bottom: 3px;
    margin-top: 18pt;
    margin-bottom: 10pt;
}}

h3 {{
    font-family: "{FONT_FAMILY}", Helvetica, Arial, sans-serif;
    color: #3a4a5e;
    font-size: 13pt;
    margin-top: 14pt;
    margin-bottom: 6pt;
}}

h4 {{
    font-family: "{FONT_FAMILY}", Helvetica, Arial, sans-serif;
    color: #4a5568;
    font-size: 12pt;
    margin-top: 10pt;
    margin-bottom: 4pt;
}}

p {{
    margin-bottom: 6pt;
    text-align: justify;
}}

ul {{
    margin-left: 18pt;
    margin-bottom: 8pt;
    list-style-type: disc;
}}

ol {{
    margin-left: 18pt;
    margin-bottom: 8pt;
}}

li {{
    margin-bottom: 3pt;
}}

code {{
    background-color: #f0f3f7;
    color: #c0392b;
    padding: 1pt 4pt;
    font-family: "Courier New", monospace;
    font-size: 10pt;
    border-radius: 3px;
}}

pre {{
    background-color: #2d3748;
    color: #e2e8f0;
    padding: 8pt;
    border-radius: 4px;
    font-family: "Courier New", monospace;
    font-size: 9pt;
    line-height: 1.4;
    margin: 8pt 0;
    white-space: pre-wrap;
}}

pre code {{
    background-color: transparent;
    color: inherit;
    padding: 0;
}}

blockquote {{
    border-left: 4px solid #f0ad4e;
    background-color: #fff8e7;
    padding: 8pt 12pt;
    margin: 8pt 0;
    color: #5a4a2c;
    font-style: italic;
}}

table {{
    border-collapse: collapse;
    width: 100%;
    margin: 10pt 0;
    font-size: 10pt;
}}

th {{
    background-color: #1a4d8c;
    color: white;
    padding: 6pt 8pt;
    text-align: left;
    font-weight: bold;
    border: 1px solid #1a4d8c;
}}

td {{
    padding: 5pt 8pt;
    border: 1px solid #c8d6e8;
    vertical-align: top;
}}

tr:nth-child(even) td {{
    background-color: #f5f8fc;
}}

a {{
    color: #2c6db5;
    text-decoration: none;
}}

strong {{
    color: #1a4d8c;
}}

hr {{
    border: none;
    border-top: 1px solid #c8d6e8;
    margin: 12pt 0;
}}

img {{
    max-width: 100%;
    margin: 10pt auto;
    display: block;
}}

/* Mockups de tela de celular (PNGs gerados) — centralizados e médios */
p img {{
    max-width: 280pt;
    margin: 12pt auto;
    display: block;
}}

/* Diagramas usam classe explicita .diagrama (largura maior) */
img.diagrama {{
    max-width: 480pt;
}}

.footer {{
    text-align: center;
    color: #888;
    font-size: 8pt;
}}

.cover {{
    text-align: center;
    margin-top: 100pt;
}}

.cover h1 {{
    font-size: 32pt;
    border: none;
    page-break-before: avoid;
}}

.cover .subtitle {{
    font-size: 16pt;
    color: #4a5568;
    margin-top: 12pt;
}}

.cover .meta {{
    margin-top: 80pt;
    font-size: 11pt;
    color: #4a5568;
}}
"""


COVER_HTML = """
<div class="cover">
    <h1>Manual do Coletor</h1>
    <div class="subtitle">SIHCP Mobile — Aplicativo Android para Coleta Patrimonial em Campo</div>
    <div class="meta">
        <p><strong>Versão do app:</strong> 2.29.5 (Build 81)</p>
        <p><strong>Sistema:</strong> Sistema de Histórico e Coleta Patrimonial</p>
        <p><strong>Instituição:</strong> Instituto Federal de Mato Grosso (IFMT)</p>
        <p><strong>Data:</strong> Maio de 2026</p>
    </div>
</div>
<div style="page-break-after: always;"></div>
"""


FOOTER_HTML = """
<div id="footer_content" class="footer">
    Manual do Coletor — SIHCP Mobile v2.29.5 — IFMT — Página <pdf:pagenumber/>
</div>
"""


def main() -> int:
    if not MD_PATH.exists():
        print(f"ERRO: arquivo não encontrado: {MD_PATH}")
        return 1

    print(f"Lendo: {MD_PATH}")
    md_text = MD_PATH.read_text(encoding="utf-8")

    # Remover o cabeçalho original (substituído pela capa)
    # O markdown começa com "# Manual do Coletor — SIHCP Mobile" — vamos pular as
    # primeiras linhas até "## Sumário" para manter o conteúdo a partir do sumário.
    if "## Sumário" in md_text:
        idx = md_text.index("## Sumário")
        md_text = md_text[idx:]

    # Converter markdown para HTML
    html_body = markdown.markdown(
        md_text,
        extensions=["extra", "tables", "fenced_code", "toc", "sane_lists"],
    )

    # Adicionar classe .diagrama em imagens cujo src contém "diagrama"
    # (usado para que o CSS aplique largura maior nesses casos)
    import re
    html_body = re.sub(
        r'<img([^>]*?src="[^"]*diagrama[^"]*"[^>]*?)>',
        r'<img class="diagrama"\1>',
        html_body,
    )

    # Montar HTML completo
    html_full = f"""
    <!DOCTYPE html>
    <html lang="pt-BR">
    <head>
        <meta charset="utf-8" />
        <title>Manual do Coletor — SIHCP Mobile</title>
        <style>{CSS}</style>
    </head>
    <body>
        {COVER_HTML}
        {html_body}
        {FOOTER_HTML}
    </body>
    </html>
    """

    print(f"Gerando: {PDF_PATH}")
    PDF_PATH.parent.mkdir(parents=True, exist_ok=True)

    # link_callback resolve caminhos relativos das imagens
    # (xhtml2pdf precisa de caminhos absolutos no filesystem)
    def link_callback(uri: str, rel: str) -> str:
        if uri.startswith(("http://", "https://", "data:")):
            return uri
        # Caminho relativo: resolver a partir da pasta do markdown
        candidate = (MD_PATH.parent / uri).resolve()
        if candidate.exists():
            return str(candidate)
        # Fallback: tentar a partir da raiz do projeto
        candidate2 = (PROJECT_ROOT / uri).resolve()
        if candidate2.exists():
            return str(candidate2)
        return uri

    with PDF_PATH.open("wb") as fp:
        result = pisa.CreatePDF(
            html_full,
            dest=fp,
            encoding="utf-8",
            link_callback=link_callback,
        )

    if result.err:
        print(f"ERRO: {result.err} erro(s) ao gerar PDF")
        return 2

    size_kb = PDF_PATH.stat().st_size / 1024
    print(f"OK — PDF gerado ({size_kb:.1f} KB)")
    print(f"Arquivo: {PDF_PATH}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
