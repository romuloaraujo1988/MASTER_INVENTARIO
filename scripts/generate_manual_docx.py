"""
Gera arquivo DOCX (Microsoft Word) do Manual do Coletor a partir do Markdown.

Cada elemento do markdown é mapeado para um elemento Word equivalente:
- Cabeçalhos (#, ##, ###) → Heading 1, 2, 3 (estilos nativos do Word)
- Imagens → Inline pictures (clique direito > "Alterar Imagem" para substituir)
- Tabelas → Tabelas Word com estilo Light Grid
- Listas (- *) → Listas com bullets
- Listas numeradas (1. 2.) → Listas numeradas
- Código inline (`x`) → Caractere com fonte Consolas
- Blocos de código → Parágrafo com fundo cinza claro
- Citações (>) → Parágrafo com indentação e itálico
- Negrito (**x**) e itálico (*x*) → Runs formatados

Uso:
    python scripts/generate_manual_docx.py

Saída:
    docs/manuais/MANUAL_COLETOR.docx
"""
from __future__ import annotations

import re
import sys
from pathlib import Path

from docx import Document
from docx.shared import Pt, Cm, RGBColor, Inches
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT
from docx.oxml.ns import qn
from docx.oxml import OxmlElement


PROJECT_ROOT = Path(__file__).resolve().parent.parent
MD_PATH = PROJECT_ROOT / "docs" / "manuais" / "MANUAL_COLETOR.md"
DOCX_PATH = PROJECT_ROOT / "docs" / "manuais" / "MANUAL_COLETOR.docx"

# Cores institucionais do app (mesmas do PDF)
COLOR_PRIMARY = RGBColor(0x1B, 0x5E, 0xA7)
COLOR_PRIMARY_DARK = RGBColor(0x0D, 0x47, 0xA1)
COLOR_TEXT = RGBColor(0x19, 0x1C, 0x20)
COLOR_TEXT_SECONDARY = RGBColor(0x43, 0x47, 0x4E)
COLOR_CODE = RGBColor(0xC0, 0x39, 0x2B)
COLOR_QUOTE = RGBColor(0x5A, 0x4A, 0x2C)
COLOR_GRAY = RGBColor(0x73, 0x77, 0x7F)


# ─── Helpers ──────────────────────────────────────────────────────────────

def set_cell_shading(cell, hex_color: str) -> None:
    """Define cor de fundo de uma célula (Word usa shading via XML)."""
    tc_pr = cell._tc.get_or_add_tcPr()
    shd = OxmlElement("w:shd")
    shd.set(qn("w:val"), "clear")
    shd.set(qn("w:color"), "auto")
    shd.set(qn("w:fill"), hex_color)
    tc_pr.append(shd)


def add_hyperlink(paragraph, url: str, text: str) -> None:
    """Adiciona um hyperlink no parágrafo."""
    part = paragraph.part
    r_id = part.relate_to(
        url,
        "http://schemas.openxmlformats.org/officeDocument/2006/relationships/hyperlink",
        is_external=True,
    )
    hyperlink = OxmlElement("w:hyperlink")
    hyperlink.set(qn("r:id"), r_id)
    new_run = OxmlElement("w:r")
    rPr = OxmlElement("w:rPr")
    color = OxmlElement("w:color")
    color.set(qn("w:val"), "1B5EA7")
    rPr.append(color)
    underline = OxmlElement("w:u")
    underline.set(qn("w:val"), "single")
    rPr.append(underline)
    new_run.append(rPr)
    t = OxmlElement("w:t")
    t.text = text
    new_run.append(t)
    hyperlink.append(new_run)
    paragraph._p.append(hyperlink)


# Regex para inline markdown (negrito, itálico, código, links, imagens)
INLINE_PATTERN = re.compile(
    r"(\*\*[^\*]+\*\*|"          # negrito **x**
    r"\*[^\*]+\*|"               # itálico *x*
    r"`[^`]+`|"                  # código `x`
    r"!\[[^\]]*\]\([^)]+\)|"    # imagem ![alt](src)
    r"\[[^\]]+\]\([^)]+\))"     # link [text](url)
)


def add_inline_runs(paragraph, text: str) -> None:
    """
    Processa texto inline com formatação markdown e adiciona runs ao parágrafo.

    Suporta: **negrito**, *itálico*, `código`, [link](url).
    Imagens inline são ignoradas (devem estar em parágrafo próprio).
    """
    if not text:
        return

    parts = INLINE_PATTERN.split(text)
    for part in parts:
        if not part:
            continue

        if part.startswith("**") and part.endswith("**"):
            run = paragraph.add_run(part[2:-2])
            run.bold = True
        elif part.startswith("*") and part.endswith("*") and not part.startswith("**"):
            run = paragraph.add_run(part[1:-1])
            run.italic = True
        elif part.startswith("`") and part.endswith("`"):
            run = paragraph.add_run(part[1:-1])
            run.font.name = "Consolas"
            run.font.size = Pt(10)
            run.font.color.rgb = COLOR_CODE
        elif part.startswith("![") and "](" in part:
            # Imagem inline: ignora (deve estar em linha própria)
            pass
        elif part.startswith("[") and "](" in part and part.endswith(")"):
            m = re.match(r"\[([^\]]+)\]\(([^)]+)\)", part)
            if m:
                add_hyperlink(paragraph, m.group(2), m.group(1))
            else:
                paragraph.add_run(part)
        else:
            paragraph.add_run(part)


# ─── Parser de markdown linha-a-linha ────────────────────────────────────

def parse_markdown(md_text: str) -> list[dict]:
    """
    Converte o markdown em uma lista de blocos para conversão.

    Cada bloco é um dict com 'type' e dados específicos.
    Tipos: heading, paragraph, image, list_item, code_block, blockquote,
           table, hr, blank.
    """
    lines = md_text.splitlines()
    blocks = []
    i = 0

    while i < len(lines):
        line = lines[i]

        # ─── Linha em branco
        if not line.strip():
            blocks.append({"type": "blank"})
            i += 1
            continue

        # ─── Bloco de código ```
        if line.strip().startswith("```"):
            lang = line.strip()[3:].strip()
            code_lines = []
            i += 1
            while i < len(lines) and not lines[i].strip().startswith("```"):
                code_lines.append(lines[i])
                i += 1
            blocks.append({"type": "code_block", "language": lang, "content": "\n".join(code_lines)})
            i += 1
            continue

        # ─── Cabeçalhos
        m = re.match(r"^(#{1,6})\s+(.+)$", line)
        if m:
            level = len(m.group(1))
            blocks.append({"type": "heading", "level": level, "text": m.group(2).strip()})
            i += 1
            continue

        # ─── Régua horizontal
        if re.match(r"^[-*_]{3,}\s*$", line):
            blocks.append({"type": "hr"})
            i += 1
            continue

        # ─── Tabela (header | --- | rows)
        if "|" in line and i + 1 < len(lines) and re.match(r"^\s*\|?[\s:|-]+\|?\s*$", lines[i + 1]):
            header = [c.strip() for c in line.strip().strip("|").split("|")]
            i += 2  # pular header e separator
            rows = []
            while i < len(lines) and "|" in lines[i] and lines[i].strip():
                rows.append([c.strip() for c in lines[i].strip().strip("|").split("|")])
                i += 1
            blocks.append({"type": "table", "header": header, "rows": rows})
            continue

        # ─── Citação (blockquote)
        if line.strip().startswith(">"):
            quote_lines = []
            while i < len(lines) and lines[i].strip().startswith(">"):
                quote_lines.append(lines[i].strip().lstrip(">").strip())
                i += 1
            blocks.append({"type": "blockquote", "text": "\n".join(quote_lines)})
            continue

        # ─── Lista numerada
        m = re.match(r"^\s*(\d+)\.\s+(.+)$", line)
        if m:
            blocks.append({"type": "list_item", "ordered": True, "text": m.group(2)})
            i += 1
            continue

        # ─── Lista com bullets
        m = re.match(r"^\s*[-*+]\s+(.+)$", line)
        if m:
            blocks.append({"type": "list_item", "ordered": False, "text": m.group(1)})
            i += 1
            continue

        # ─── Imagem em linha própria: ![alt](src)
        m = re.match(r"^!\[([^\]]*)\]\(([^)]+)\)\s*$", line)
        if m:
            blocks.append({"type": "image", "alt": m.group(1), "src": m.group(2)})
            i += 1
            continue

        # ─── Parágrafo normal (pode ter múltiplas linhas até próxima blank)
        para_lines = [line]
        i += 1
        while i < len(lines) and lines[i].strip() and not re.match(
            r"^(#{1,6}\s|[-*_]{3,}\s*$|\s*[-*+]\s|\s*\d+\.\s|>|```|!\[)", lines[i]
        ):
            para_lines.append(lines[i])
            i += 1
        blocks.append({"type": "paragraph", "text": " ".join(l.strip() for l in para_lines)})

    return blocks


# ─── Renderização para DOCX ──────────────────────────────────────────────

def setup_document(doc: Document) -> None:
    """Configura estilos e margens do documento."""
    # Margens
    for section in doc.sections:
        section.top_margin = Cm(2.5)
        section.bottom_margin = Cm(2.0)
        section.left_margin = Cm(2.5)
        section.right_margin = Cm(2.5)

    # Estilo Normal: Calibri 11pt
    normal = doc.styles["Normal"]
    normal.font.name = "Calibri"
    normal.font.size = Pt(11)

    # Customizar Heading 1 e 2 com cor primária
    for style_name in ("Heading 1", "Heading 2", "Heading 3", "Heading 4"):
        try:
            style = doc.styles[style_name]
            style.font.color.rgb = COLOR_PRIMARY
            style.font.name = "Calibri"
            if style_name == "Heading 1":
                style.font.size = Pt(20)
                style.font.bold = True
            elif style_name == "Heading 2":
                style.font.size = Pt(16)
                style.font.bold = True
            elif style_name == "Heading 3":
                style.font.size = Pt(13)
                style.font.bold = True
            elif style_name == "Heading 4":
                style.font.size = Pt(12)
                style.font.bold = True
        except KeyError:
            pass


def add_cover(doc: Document) -> None:
    """Adiciona a capa do manual."""
    # Espaço no topo
    for _ in range(4):
        doc.add_paragraph()

    title = doc.add_paragraph()
    title.alignment = WD_ALIGN_PARAGRAPH.CENTER
    run = title.add_run("Manual do Coletor")
    run.font.size = Pt(32)
    run.font.bold = True
    run.font.color.rgb = COLOR_PRIMARY

    sub = doc.add_paragraph()
    sub.alignment = WD_ALIGN_PARAGRAPH.CENTER
    run = sub.add_run("SIHCP Mobile — Aplicativo Android para Coleta Patrimonial em Campo")
    run.font.size = Pt(14)
    run.font.color.rgb = COLOR_TEXT_SECONDARY

    for _ in range(4):
        doc.add_paragraph()

    meta_items = [
        ("Versão do app:", "2.29.5 (Build 81)"),
        ("Sistema:", "Sistema de Histórico e Coleta Patrimonial"),
        ("Instituição:", "Instituto Federal de Mato Grosso (IFMT)"),
        ("Data:", "Maio de 2026"),
    ]
    for label, value in meta_items:
        p = doc.add_paragraph()
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER
        r1 = p.add_run(f"{label} ")
        r1.bold = True
        r1.font.size = Pt(12)
        r1.font.color.rgb = COLOR_TEXT
        r2 = p.add_run(value)
        r2.font.size = Pt(12)
        r2.font.color.rgb = COLOR_TEXT

    # Quebra de página
    doc.add_page_break()


def render_block(doc: Document, block: dict, base_dir: Path) -> None:
    """Renderiza um único bloco no documento."""
    btype = block["type"]

    if btype == "heading":
        level = block["level"]
        # Cabeçalhos h1 começam em página nova (exceto o primeiro)
        if level == 1 and len(doc.paragraphs) > 1:
            doc.add_page_break()
        doc.add_heading(block["text"], level=min(level, 4))

    elif btype == "paragraph":
        p = doc.add_paragraph()
        add_inline_runs(p, block["text"])

    elif btype == "image":
        src = block["src"]
        # Resolver caminho relativo
        if src.startswith(("http://", "https://")):
            # Imagem remota — adiciona texto alternativo
            p = doc.add_paragraph()
            p.alignment = WD_ALIGN_PARAGRAPH.CENTER
            r = p.add_run(f"[Imagem remota: {src}]")
            r.italic = True
            r.font.color.rgb = COLOR_GRAY
            return

        img_path = (base_dir / src).resolve()
        if not img_path.exists():
            p = doc.add_paragraph()
            p.alignment = WD_ALIGN_PARAGRAPH.CENTER
            r = p.add_run(f"[Imagem não encontrada: {src}]")
            r.italic = True
            r.font.color.rgb = COLOR_GRAY
            return

        # Diagramas (largura maior) vs mockups de tela (largura menor)
        is_diagram = "diagrama" in img_path.name.lower()
        width = Inches(5.5) if is_diagram else Inches(2.6)

        p = doc.add_paragraph()
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER
        run = p.add_run()
        run.add_picture(str(img_path), width=width)

        # Legenda (alt text como caption)
        if block.get("alt"):
            cap = doc.add_paragraph()
            cap.alignment = WD_ALIGN_PARAGRAPH.CENTER
            r = cap.add_run(block["alt"])
            r.italic = True
            r.font.size = Pt(9)
            r.font.color.rgb = COLOR_GRAY

    elif btype == "list_item":
        style = "List Number" if block["ordered"] else "List Bullet"
        try:
            p = doc.add_paragraph(style=style)
        except KeyError:
            p = doc.add_paragraph()
            p.add_run("• " if not block["ordered"] else "")
        add_inline_runs(p, block["text"])

    elif btype == "code_block":
        p = doc.add_paragraph()
        p.paragraph_format.left_indent = Cm(0.5)
        run = p.add_run(block["content"])
        run.font.name = "Consolas"
        run.font.size = Pt(9)
        run.font.color.rgb = RGBColor(0x2D, 0x37, 0x48)

    elif btype == "blockquote":
        p = doc.add_paragraph()
        p.paragraph_format.left_indent = Cm(1.0)
        # Borda esquerda âmbar (simulada com itálico)
        r = p.add_run("⚠ ")
        r.font.color.rgb = RGBColor(0xF0, 0xAD, 0x4E)
        r.bold = True
        add_inline_runs(p, block["text"])
        for run in p.runs[1:]:
            run.italic = True
            run.font.color.rgb = COLOR_QUOTE

    elif btype == "table":
        header = block["header"]
        rows = block["rows"]
        n_cols = len(header)

        table = doc.add_table(rows=1 + len(rows), cols=n_cols)
        table.style = "Light Grid Accent 1"
        table.alignment = WD_TABLE_ALIGNMENT.CENTER

        # Header
        hdr_cells = table.rows[0].cells
        for i, cell_text in enumerate(header):
            cell = hdr_cells[i]
            cell.text = ""
            p = cell.paragraphs[0]
            run = p.add_run(cell_text)
            run.bold = True
            run.font.color.rgb = RGBColor(0xFF, 0xFF, 0xFF)
            run.font.size = Pt(10)
            set_cell_shading(cell, "1B5EA7")

        # Dados
        for r_idx, row_data in enumerate(rows, start=1):
            cells = table.rows[r_idx].cells
            for c_idx, cell_text in enumerate(row_data):
                if c_idx < n_cols:
                    cell = cells[c_idx]
                    cell.text = ""
                    p = cell.paragraphs[0]
                    add_inline_runs(p, cell_text)
                    for run in p.runs:
                        run.font.size = Pt(10)
                    if r_idx % 2 == 0:
                        set_cell_shading(cell, "F5F8FC")

    elif btype == "hr":
        p = doc.add_paragraph()
        p.paragraph_format.space_after = Pt(0)
        run = p.add_run("─" * 60)
        run.font.color.rgb = RGBColor(0xC8, 0xD6, 0xE8)

    elif btype == "blank":
        # Pular linhas em branco redundantes (já há espaço entre parágrafos)
        pass


def add_footer(doc: Document) -> None:
    """Adiciona rodapé com paginação em todas as páginas."""
    for section in doc.sections:
        footer = section.footer
        p = footer.paragraphs[0]
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER
        r = p.add_run("Manual do Coletor — SIHCP Mobile v2.29.5 — IFMT — Página ")
        r.font.size = Pt(8)
        r.font.color.rgb = COLOR_GRAY

        # Campo de número de página
        fld_char1 = OxmlElement("w:fldChar")
        fld_char1.set(qn("w:fldCharType"), "begin")
        instr_text = OxmlElement("w:instrText")
        instr_text.set(qn("xml:space"), "preserve")
        instr_text.text = "PAGE"
        fld_char2 = OxmlElement("w:fldChar")
        fld_char2.set(qn("w:fldCharType"), "end")

        run_xml = OxmlElement("w:r")
        rPr = OxmlElement("w:rPr")
        sz = OxmlElement("w:sz")
        sz.set(qn("w:val"), "16")
        rPr.append(sz)
        run_xml.append(rPr)
        run_xml.append(fld_char1)
        run_xml.append(instr_text)
        run_xml.append(fld_char2)
        p._p.append(run_xml)


# ─── Execução ────────────────────────────────────────────────────────────

def main() -> int:
    if not MD_PATH.exists():
        print(f"ERRO: arquivo não encontrado: {MD_PATH}")
        return 1

    print(f"Lendo: {MD_PATH}")
    md_text = MD_PATH.read_text(encoding="utf-8")

    # Pular o cabeçalho original (será substituído pela capa)
    if "## Sumário" in md_text:
        idx = md_text.index("## Sumário")
        md_text = md_text[idx:]

    print("Parseando markdown...")
    blocks = parse_markdown(md_text)
    print(f"  {len(blocks)} blocos identificados")

    print("Criando DOCX...")
    doc = Document()
    setup_document(doc)
    add_cover(doc)

    base_dir = MD_PATH.parent
    for block in blocks:
        render_block(doc, block, base_dir)

    add_footer(doc)

    print(f"Salvando: {DOCX_PATH}")
    DOCX_PATH.parent.mkdir(parents=True, exist_ok=True)
    doc.save(str(DOCX_PATH))

    size_kb = DOCX_PATH.stat().st_size / 1024
    print(f"OK — DOCX gerado ({size_kb:.1f} KB)")
    print(f"Arquivo: {DOCX_PATH}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
