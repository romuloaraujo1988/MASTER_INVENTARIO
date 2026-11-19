#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Gerador da Documentação Completa para Registro de Programa de Computador
(INPI / NIT-IFMT) referente ao aplicativo SIHCP Mobile (InventarioMobile).

Execução recomendada:
    C:\\Users\\Romulo\\AppData\\Local\\Python\\bin\\python.exe gerar_documentacao_registro.py
"""

from __future__ import annotations

import hashlib
import os
import textwrap
import zipfile
from collections import defaultdict
from datetime import datetime
from pathlib import Path
from typing import Any, Dict, Iterable, List, Tuple

from docx import Document
from docx.document import Document as DocxDocument
from docx.enum.table import WD_ALIGN_VERTICAL, WD_TABLE_ALIGNMENT
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.oxml import OxmlElement, parse_xml
from docx.oxml.ns import nsdecls, qn
from docx.shared import Cm, Inches, Pt, RGBColor
from reportlab.lib import colors
from reportlab.lib.enums import TA_CENTER, TA_JUSTIFY, TA_LEFT
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import ParagraphStyle, getSampleStyleSheet
from reportlab.lib.units import cm
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.platypus import (
    PageBreak,
    Paragraph,
    Preformatted,
    SimpleDocTemplate,
    Spacer,
    Table,
    TableStyle,
)

# ==============================================================================
# CONFIGURAÇÕES E CONSTANTES DO PROJETO
# ==============================================================================

BASE_DIR = Path(__file__).resolve().parent
REPO_ROOT = BASE_DIR.parent
MOBILE_DIR = REPO_ROOT / "InventarioMobile"
SRC_MAIN_DIR = MOBILE_DIR / "app" / "src" / "main"
PKG_ROOT_DIR = SRC_MAIN_DIR / "java" / "com" / "inventario" / "mobile"
PACOTE_INPI_DIR = BASE_DIR / "04_Pacote_Codigo_Fonte_Guardado_INPI"

PDF_FILENAME = "SIHCP_Mobile_Documentacao_e_Codigo_Fonte_INPI.pdf"
ZIP_FILENAME = "SIHCP_Mobile_Codigo_Fonte_Completo_INPI.zip"

PROGRAMA_INFO = {
    "titulo_completo": "SIHCP Mobile - Sistema de Histórico e Coleta Patrimonial Mobile",
    "titulo_curto": "SIHCP Mobile",
    "versao_atual": "2.29.8 (Build / versionCode 84)",
    "application_id": "com.inventario.mobile",
    "data_criacao": "18/10/2025",
    "data_publicacao": "09/11/2025",
    "data_producao": "18/11/2025",
    "data_versao_atual": "13/11/2026",
    "linguagens": "Kotlin, XML, SQL, Java",
    "campo_aplicacao": (
        "AD-01 (Administração Geral / Gestão Pública); "
        "AD-03 (Administração Patrimonial / Controle de Bens e Material); "
        "GI-01 (Gerência da Informação / Coleta de Dados); "
        "ED-01 (Educação)"
    ),
    "tipo_programa": (
        "AP-01 (Aplicativo para Dispositivos Móveis); "
        "GI-01 (Gerência de Informação / Banco de Dados Local); "
        "TC-01 (Teleprocessamento / Comunicação e Sincronização de Dados); "
        "UT-01 (Utilitário - Leitura Óptica e Relatórios)"
    ),
    "algoritmo_hash": "SHA-512",
    "titular": "Instituto Federal de Educação, Ciência e Tecnologia de Mato Grosso (IFMT)",
    "autor_principal": "Romulo Rodrigues de Araujo",
    "email_autor": "romulo.araujo88@gmail.com",
    "descricao_resumida": (
        "O SIHCP Mobile (Sistema de Histórico e Coleta Patrimonial Mobile) é um "
        "aplicativo nativo para plataforma Android, desenvolvido em linguagem Kotlin "
        "segundo os princípios de Clean Architecture e padrão arquitetural MVVM, "
        "destinado à automatização e auditoria do inventário físico de bens "
        "patrimoniais em instituições públicas federais. O software permite a leitura "
        "óptica de etiquetas via QR Code e código de barras, coleta manual por número "
        "de tombamento, busca textual por descrição, registro de itens compostos em "
        "lote e cadastramento de bens sem etiqueta com captura fotográfica obrigatória. "
        "Projetado sob abordagem offline-first, armazena dados localmente em banco "
        "SQLite criptografado com SQLCipher (AES-256) e gerencia chaves via Android "
        "Keystore, contando com autenticação JWT e biometria. A sincronização "
        "bidirecional com o servidor ocorre em segundo plano via WorkManager com "
        "retentativa exponencial, além de monitoramento de coletas ao vivo via "
        "WebSocket. O aplicativo detecta divergências cadastrais e de localização "
        "em tempo real, emite relatórios em PDF, Excel e CSV, e atende à IN SEDAP "
        "nº 205/1988 e ao Decreto nº 12.785/2025."
    ),
}

# Arquivos-chave representativos de cada camada para compor o caderno de código do PDF INPI
REPRESENTATIVE_SOURCE_FILES = [
    ("Configuração Gradle Raiz", MOBILE_DIR / "build.gradle"),
    ("Configuração Settings Gradle", MOBILE_DIR / "settings.gradle"),
    ("Configuração App Gradle (Versão e Dependências)", MOBILE_DIR / "app" / "build.gradle"),
    ("Regras R8 / ProGuard", MOBILE_DIR / "app" / "proguard-rules.pro"),
    ("Manifesto Android (Permissões e Activities)", SRC_MAIN_DIR / "AndroidManifest.xml"),
    ("Application & Hilt Entry Point", PKG_ROOT_DIR / "InventarioMobileApplication.kt"),
    ("Segurança — Gerenciador de Chave SQLCipher (Keystore)", PKG_ROOT_DIR / "security" / "SqlCipherKeyManager.kt"),
    ("Segurança — Autenticação por PIN", PKG_ROOT_DIR / "security" / "PinAuthManager.kt"),
    ("Segurança — Biometria", PKG_ROOT_DIR / "security" / "BiometricManager.kt"),
    ("Persistência — Banco Room + SQLCipher (AppDatabase)", PKG_ROOT_DIR / "data" / "local" / "database" / "AppDatabase.kt"),
    ("Persistência — Entidade ColetaEntity", PKG_ROOT_DIR / "data" / "local" / "entity" / "ColetaEntity.kt"),
    ("Persistência — Entidade PatrimonioEntity", PKG_ROOT_DIR / "data" / "local" / "entity" / "PatrimonioEntity.kt"),
    ("Persistência — DAO ColetaDao", PKG_ROOT_DIR / "data" / "local" / "dao" / "ColetaDao.kt"),
    ("Domínio — Modelo Coleta", PKG_ROOT_DIR / "domain" / "model" / "Coleta.kt"),
    ("Domínio — Contrato ColetaRepository", PKG_ROOT_DIR / "domain" / "repository" / "ColetaRepository.kt"),
    ("Domínio — Caso de Uso RegistrarColetaUseCase", PKG_ROOT_DIR / "domain" / "usecase" / "RegistrarColetaUseCase.kt"),
    ("Domínio — Caso de Uso SincronizarColetasPendentesUseCase", PKG_ROOT_DIR / "domain" / "usecase" / "SincronizarColetasPendentesUseCase.kt"),
    ("Domínio — Caso de Uso BuscarColetasComFallbackUseCase", PKG_ROOT_DIR / "domain" / "usecase" / "BuscarColetasComFallbackUseCase.kt"),
    ("Dados — Repositório ColetaRepositoryImpl", PKG_ROOT_DIR / "data" / "repository" / "ColetaRepositoryImpl.kt"),
    ("Dados — Monitor ao Vivo WebSocket", PKG_ROOT_DIR / "data" / "realtime" / "RealtimeColetasManager.kt"),
    ("Dados — Gerador de Relatório PDF", PKG_ROOT_DIR / "data" / "pdf" / "PdfGenerator.kt"),
    ("Dados — Gerador de Planilha Excel", PKG_ROOT_DIR / "data" / "export" / "ExcelGenerator.kt"),
    ("Background — ColetaSyncWorker", PKG_ROOT_DIR / "worker" / "ColetaSyncWorker.kt"),
    ("Background — PhotoSyncWorker", PKG_ROOT_DIR / "worker" / "PhotoSyncWorker.kt"),
    ("Rede — RefreshTokenInterceptor", PKG_ROOT_DIR / "network" / "RefreshTokenInterceptor.kt"),
    ("Apresentação — Configuração Inicial do Servidor", PKG_ROOT_DIR / "presentation" / "setup" / "InitialSetupActivity.kt"),
    ("Apresentação — Login e Biometria", PKG_ROOT_DIR / "presentation" / "login" / "LoginActivity.kt"),
    ("Apresentação — Activity Principal", PKG_ROOT_DIR / "presentation" / "main" / "MainActivity.kt"),
    ("Apresentação — Dashboard Fragment", PKG_ROOT_DIR / "presentation" / "dashboard" / "DashboardFragment.kt"),
    ("Apresentação — Mapeador Puro do Dashboard", PKG_ROOT_DIR / "presentation" / "dashboard" / "DashboardUiMapper.kt"),
    ("Apresentação — Scanner QR/Barcode", PKG_ROOT_DIR / "presentation" / "scanner" / "ScannerActivity.kt"),
    ("Apresentação — Câmera Contínua CustomCaptureActivity", PKG_ROOT_DIR / "presentation" / "scanner" / "CustomCaptureActivity.kt"),
    ("Apresentação — Coleta Manual", PKG_ROOT_DIR / "presentation" / "coleta" / "ManualCollectionActivity.kt"),
    ("Apresentação — Item Sem Etiqueta", PKG_ROOT_DIR / "presentation" / "coleta" / "ItemSemEtiquetaActivity.kt"),
    ("Apresentação — Coletas Unificada", PKG_ROOT_DIR / "presentation" / "coletas" / "ColetasUnificadaActivity.kt"),
]


# ==============================================================================
# COLETA DE MÉTRICAS REAIS DO CÓDIGO-FONTE
# ==============================================================================

def count_lines_safe(file_path: Path) -> int:
    """Conta linhas de um arquivo texto com tolerância a encoding."""
    try:
        with file_path.open("r", encoding="utf-8", errors="ignore") as f:
            return sum(1 for _ in f)
    except OSError:
        return 0


def collect_source_metrics() -> Dict[str, Any]:
    """Extrai métricas reais do código-fonte de InventarioMobile."""
    if not SRC_MAIN_DIR.exists():
        raise FileNotFoundError(f"Diretório não encontrado: {SRC_MAIN_DIR}")

    ext_counts: Dict[str, int] = defaultdict(int)
    ext_lines: Dict[str, int] = defaultdict(int)
    pkg_counts: Dict[str, int] = defaultdict(int)
    pkg_lines: Dict[str, int] = defaultdict(int)
    all_main_files: List[Path] = []
    kt_files: List[Path] = []

    text_exts = {".kt", ".java", ".xml", ".pro", ".gradle", ".txt"}

    for root, _, files in os.walk(SRC_MAIN_DIR):
        for name in sorted(files):
            fpath = Path(root) / name
            all_main_files.append(fpath)
            ext = fpath.suffix.lower() or "<sem_ext>"
            ext_counts[ext] += 1
            if ext in text_exts:
                lines = count_lines_safe(fpath)
                ext_lines[ext] += lines
            else:
                lines = 0

            if ext == ".kt":
                kt_files.append(fpath)
                try:
                    rel = fpath.relative_to(PKG_ROOT_DIR)
                    top_pkg = rel.parts[0] if len(rel.parts) > 1 else "(raiz)"
                except ValueError:
                    top_pkg = "(outros)"
                pkg_counts[top_pkg] += 1
                pkg_lines[top_pkg] += lines

    res_layout_count = len(list((SRC_MAIN_DIR / "res" / "layout").glob("*.xml"))) if (SRC_MAIN_DIR / "res" / "layout").exists() else 0

    # Inclui também testes em app/src/test e app/src/androidTest se existirem
    unit_test_count = sum(1 for _ in (MOBILE_DIR / "app" / "src" / "test").rglob("*.kt")) if (MOBILE_DIR / "app" / "src" / "test").exists() else 0
    android_test_count = sum(1 for _ in (MOBILE_DIR / "app" / "src" / "androidTest").rglob("*.kt")) if (MOBILE_DIR / "app" / "src" / "androidTest").exists() else 0
    test_kt_count = unit_test_count + android_test_count

    activities_count = sum(1 for p in kt_files if "Activity" in p.name)
    fragments_count = sum(1 for p in kt_files if "Fragment" in p.name)
    viewmodels_count = sum(1 for p in kt_files if "ViewModel" in p.name and "Factory" not in p.name)
    usecases_count = len(list((PKG_ROOT_DIR / "domain" / "usecase").glob("*.kt"))) if (PKG_ROOT_DIR / "domain" / "usecase").exists() else 0
    domain_repos_count = len(list((PKG_ROOT_DIR / "domain" / "repository").glob("*.kt"))) if (PKG_ROOT_DIR / "domain" / "repository").exists() else 0
    data_repos_count = len(list((PKG_ROOT_DIR / "data" / "repository").glob("*.kt"))) if (PKG_ROOT_DIR / "data" / "repository").exists() else 0
    workers_count = len(list((PKG_ROOT_DIR / "worker").glob("*.kt"))) if (PKG_ROOT_DIR / "worker").exists() else 0
    apis_count = len(list((PKG_ROOT_DIR / "data" / "remote" / "api").glob("*.kt"))) if (PKG_ROOT_DIR / "data" / "remote" / "api").exists() else 0
    security_count = len(list((PKG_ROOT_DIR / "security").glob("*.kt"))) if (PKG_ROOT_DIR / "security").exists() else 0
    network_count = len(list((PKG_ROOT_DIR / "network").glob("*.kt"))) if (PKG_ROOT_DIR / "network").exists() else 0

    return {
        "total_main_files": len(all_main_files),
        "ext_counts": dict(sorted(ext_counts.items())),
        "ext_lines": dict(sorted(ext_lines.items())),
        "kt_files_count": len(kt_files),
        "kt_lines_total": ext_lines.get(".kt", 0),
        "xml_files_count": ext_counts.get(".xml", 0),
        "xml_lines_total": ext_lines.get(".xml", 0),
        "layout_xml_count": res_layout_count,
        "unit_test_count": unit_test_count,
        "android_test_count": android_test_count,
        "test_kt_count": test_kt_count,
        "total_kt_project": len(kt_files) + test_kt_count,
        "activities_count": activities_count,
        "fragments_count": fragments_count,
        "viewmodels_count": viewmodels_count,
        "usecases_count": usecases_count,
        "domain_repos_count": domain_repos_count,
        "data_repos_count": data_repos_count,
        "workers_count": workers_count,
        "apis_count": apis_count,
        "security_count": security_count,
        "network_count": network_count,
        "pkg_counts": dict(sorted(pkg_counts.items(), key=lambda x: (-x[1], x[0]))),
        "pkg_lines": dict(pkg_lines),
        "all_main_files_rel": sorted(
            str(p.relative_to(MOBILE_DIR)).replace("\\", "/") for p in all_main_files
        ),
        "kt_files_rel": sorted(
            str(p.relative_to(PKG_ROOT_DIR)).replace("\\", "/") for p in kt_files
        ),
    }


def compute_hashes(file_path: Path) -> Dict[str, Any]:
    """Calcula SHA-512, SHA-256 e tamanho em bytes de um arquivo."""
    h512 = hashlib.sha512()
    h256 = hashlib.sha256()
    with file_path.open("rb") as f:
        while True:
            chunk = f.read(1024 * 1024)
            if not chunk:
                break
            h512.update(chunk)
            h256.update(chunk)
    return {
        "filename": file_path.name,
        "size_bytes": file_path.stat().st_size,
        "sha512": h512.hexdigest(),
        "sha256": h256.hexdigest(),
    }


# ==============================================================================
# GERAÇÃO DO PDF OFICIAL PARA DEPÓSITO/GUARDA INPI (REPORTLAB)
# ==============================================================================

def register_pdf_fonts() -> Tuple[str, str, str]:
    """Registra fontes TTF Unicode do Windows se disponíveis; senão usa padrão."""
    fonts_dir = Path("C:/Windows/Fonts")
    arial = fonts_dir / "arial.ttf"
    arial_bd = fonts_dir / "arialbd.ttf"
    consola = fonts_dir / "consola.ttf"

    body_font = "Helvetica"
    bold_font = "Helvetica-Bold"
    mono_font = "Courier"

    try:
        if arial.exists() and arial_bd.exists():
            pdfmetrics.registerFont(TTFont("ArialUni", str(arial)))
            pdfmetrics.registerFont(TTFont("ArialUni-Bold", str(arial_bd)))
            body_font = "ArialUni"
            bold_font = "ArialUni-Bold"
        if consola.exists():
            pdfmetrics.registerFont(TTFont("ConsolasUni", str(consola)))
            mono_font = "ConsolasUni"
    except Exception:
        body_font, bold_font, mono_font = "Helvetica", "Helvetica-Bold", "Courier"

    return body_font, bold_font, mono_font


def xml_escape(text: str) -> str:
    """Escapa caracteres reservados XML para Paragraph do ReportLab."""
    return (
        text.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
    )


def wrap_code_lines(content: str, width: int = 92, max_lines: int = 1200) -> str:
    """Quebra linhas longas de código para caber na página A4 sem truncamento."""
    out: List[str] = []
    raw_lines = content.splitlines()
    for idx, line in enumerate(raw_lines[:max_lines], start=1):
        clean = line.replace("\t", "    ")
        prefix = f"{idx:4d} | "
        if not clean:
            out.append(prefix)
            continue
        wrapped = textwrap.wrap(
            clean,
            width=width,
            replace_whitespace=False,
            drop_whitespace=False,
            break_long_words=True,
            break_on_hyphens=False,
        )
        for j, chunk in enumerate(wrapped):
            out.append((prefix if j == 0 else "     | ") + chunk)
    if len(raw_lines) > max_lines:
        out.append(f"... [arquivo continua além da linha {max_lines}; cópia integral no pacote ZIP] ...")
    return "\n".join(out)


def build_inpi_pdf(pdf_path: Path, metrics: Dict[str, Any]) -> None:
    """Gera o PDF técnico-jurídico completo para cálculo de Hash SHA-512 no INPI."""
    body_font, bold_font, mono_font = register_pdf_fonts()

    doc = SimpleDocTemplate(
        str(pdf_path),
        pagesize=A4,
        leftMargin=1.8 * cm,
        rightMargin=1.8 * cm,
        topMargin=2.0 * cm,
        bottomMargin=1.8 * cm,
        title="SIHCP Mobile - Documentação e Código-Fonte para Registro INPI",
        author=PROGRAMA_INFO["autor_principal"],
    )

    styles = getSampleStyleSheet()
    st_title = ParagraphStyle(
        "InpiTitle",
        parent=styles["Title"],
        fontName=bold_font,
        fontSize=18,
        leading=22,
        alignment=TA_CENTER,
        textColor=colors.HexColor("#0D3B66"),
        spaceAfter=10,
    )
    st_subtitle = ParagraphStyle(
        "InpiSubtitle",
        parent=styles["Normal"],
        fontName=bold_font,
        fontSize=12,
        leading=16,
        alignment=TA_CENTER,
        textColor=colors.HexColor("#1D5B9B"),
        spaceAfter=18,
    )
    st_h1 = ParagraphStyle(
        "InpiH1",
        parent=styles["Heading1"],
        fontName=bold_font,
        fontSize=13,
        leading=17,
        textColor=colors.HexColor("#0D3B66"),
        spaceBefore=12,
        spaceAfter=6,
    )
    st_h2 = ParagraphStyle(
        "InpiH2",
        parent=styles["Heading2"],
        fontName=bold_font,
        fontSize=10.5,
        leading=14,
        textColor=colors.HexColor("#1D5B9B"),
        spaceBefore=8,
        spaceAfter=4,
    )
    st_body = ParagraphStyle(
        "InpiBody",
        parent=styles["Normal"],
        fontName=body_font,
        fontSize=9.5,
        leading=13.5,
        alignment=TA_JUSTIFY,
        spaceAfter=6,
    )
    st_code = ParagraphStyle(
        "InpiCode",
        parent=styles["Code"],
        fontName=mono_font,
        fontSize=6.8,
        leading=8.6,
        leftIndent=0,
        rightIndent=0,
        spaceBefore=2,
        spaceAfter=6,
    )

    def draw_header_footer(canvas, doc_obj):
        canvas.saveState()
        canvas.setFont(body_font, 8)
        canvas.setFillColor(colors.HexColor("#555555"))
        canvas.drawString(
            1.8 * cm,
            A4[1] - 1.2 * cm,
            "REGISTRO DE PROGRAMA DE COMPUTADOR (INPI) - SIHCP MOBILE (IFMT)",
        )
        canvas.line(1.8 * cm, A4[1] - 1.35 * cm, A4[0] - 1.8 * cm, A4[1] - 1.35 * cm)
        canvas.drawString(
            1.8 * cm,
            1.1 * cm,
            f"Versão {PROGRAMA_INFO['versao_atual']} | Titular: IFMT | Autor: {PROGRAMA_INFO['autor_principal']}",
        )
        canvas.drawRightString(A4[0] - 1.8 * cm, 1.1 * cm, f"Página {doc_obj.page}")
        canvas.restoreState()

    story: List[Any] = []

    # Capa
    story.append(Spacer(1, 2.2 * cm))
    story.append(Paragraph("INSTITUTO FEDERAL DE EDUCAÇÃO, CIÊNCIA E TECNOLOGIA DE MATO GROSSO", st_subtitle))
    story.append(Spacer(1, 0.5 * cm))
    story.append(Paragraph("DOCUMENTO DE DOCUMENTAÇÃO TÉCNICA E CÓDIGO-FONTE PARA REGISTRO DE PROGRAMA DE COMPUTADOR (INPI)", st_title))
    story.append(Spacer(1, 0.4 * cm))
    story.append(Paragraph(PROGRAMA_INFO["titulo_completo"], st_subtitle))
    story.append(Spacer(1, 1.0 * cm))

    meta_rows = [
        ["Campo", "Informação Oficial"],
        ["Título do Programa", PROGRAMA_INFO["titulo_completo"]],
        ["Identificador / Pacote", PROGRAMA_INFO["application_id"]],
        ["Versão / Build Code", PROGRAMA_INFO["versao_atual"]],
        ["Data da Criação", PROGRAMA_INFO["data_criacao"]],
        ["Data da Publicação / Produção", f"{PROGRAMA_INFO['data_publicacao']} (Produção IFMT: {PROGRAMA_INFO['data_producao']})"],
        ["Linguagens de Programação", PROGRAMA_INFO["linguagens"]],
        ["Titular dos Direitos", PROGRAMA_INFO["titular"]],
        ["Autor Principal", PROGRAMA_INFO["autor_principal"]],
        ["Base Legal", "Lei nº 9.609/1998 (Lei de Software) e Instrução Normativa INPI/PR nº 099/2019"],
        ["Arquivos Kotlin / XML (src/main)", f"{metrics['kt_files_count']} Kotlin ({metrics['kt_lines_total']:,} linhas) + {metrics['test_kt_count']} testes ({metrics['total_kt_project']} .kt totais) | {metrics['xml_files_count']} XML ({metrics['xml_lines_total']:,} linhas)"],
        ["Componentes Arquiteturais", f"{metrics['activities_count']} Activities · {metrics['fragments_count']} Fragments · {metrics['viewmodels_count']} ViewModels · {metrics['usecases_count']} UseCases · {metrics['workers_count']} Workers"],
    ]
    tbl_data = [
        [Paragraph(f"<b>{xml_escape(r[0])}</b>", st_body), Paragraph(xml_escape(r[1]), st_body)]
        for r in meta_rows
    ]
    tbl = Table(tbl_data, colWidths=[5.5 * cm, 11.9 * cm])
    tbl.setStyle(
        TableStyle(
            [
                ("BACKGROUND", (0, 0), (-1, 0), colors.HexColor("#0D3B66")),
                ("TEXTCOLOR", (0, 0), (-1, 0), colors.white),
                ("GRID", (0, 0), (-1, -1), 0.5, colors.HexColor("#99AACC")),
                ("BACKGROUND", (0, 1), (0, -1), colors.HexColor("#EEF4FA")),
                ("VALIGN", (0, 0), (-1, -1), "TOP"),
                ("TOPPADDING", (0, 0), (-1, -1), 5),
                ("BOTTOMPADDING", (0, 0), (-1, -1), 5),
                ("LEFTPADDING", (0, 0), (-1, -1), 6),
                ("RIGHTPADDING", (0, 0), (-1, -1), 6),
            ]
        )
    )
    story.append(tbl)
    story.append(Spacer(1, 1.0 * cm))
    story.append(
        Paragraph(
            "Nota de Guarda Legal: Este documento consolida o memorial técnico, o inventário "
            "completo de arquivos e trechos estruturais do código-fonte do aplicativo SIHCP Mobile, "
            "constituindo o artefato digital protegido sob guarda do titular para fins de geração de "
            "resumo digital hash (SHA-512) junto ao Instituto Nacional da Propriedade Industrial (INPI).",
            st_body,
        )
    )
    story.append(PageBreak())

    # Seção 1: Descrição e Arquitetura
    story.append(Paragraph("1. DESCRIÇÃO RESUMIDA E ESPECIFICAÇÃO TÉCNICA", st_h1))
    story.append(Paragraph(xml_escape(PROGRAMA_INFO["descricao_resumida"]), st_body))

    story.append(Paragraph("1.1 Arquitetura de Software e Segurança", st_h2))
    arch_paragraphs = [
        "O SIHCP Mobile implementa Clean Architecture combinada ao padrão MVVM (Model-View-ViewModel), "
        "organizada em três camadas principais com inversão de dependências gerenciada via Hilt (Dagger 2.48): "
        "(a) Camada de Apresentação (Activities, Fragments, ViewModels com StateFlow/LiveData, Adapters Paging 3 e Mappers puros de UI); "
        "(b) Camada de Domínio (Modelos Kotlin independentes de framework, interfaces de repositório, validadores e Casos de Uso); e "
        "(c) Camada de Dados e Infraestrutura (Room Database v19 criptografado com SQLCipher 4.6.1 AES-256, clientes Retrofit/OkHttp, "
        "WebSocket para monitor ao vivo em /ws-coletas-live, WorkManager para sincronização resiliente e Android Keystore via SqlCipherKeyManager).",
        "O modelo offline-first garante operação integral em campo mesmo sem cobertura Wi-Fi ou 4G, realizando detecção híbrida de "
        "duplicatas e divergências (comparação de sala encontrada vs. sala cadastrada e estado de conservação), persistência idempotente "
        "com clienteUuid, captura fotográfica obrigatória para itens sem etiqueta e envio em lote (batch sync) com retentativa exponencial.",
    ]
    for p in arch_paragraphs:
        story.append(Paragraph(xml_escape(p), st_body))

    story.append(Paragraph("1.2 Distribuição Real de Código-Fonte por Pacote (src/main)", st_h2))
    pkg_rows = [["Pacote", "Arquivos Kotlin (.kt)", "Linhas de Código"]]
    for pkg, cnt in metrics["pkg_counts"].items():
        pkg_rows.append([pkg, str(cnt), f"{metrics['pkg_lines'].get(pkg, 0):,}"])
    pkg_tbl = Table(pkg_rows, colWidths=[8.0 * cm, 4.7 * cm, 4.7 * cm])
    pkg_tbl.setStyle(
        TableStyle(
            [
                ("BACKGROUND", (0, 0), (-1, 0), colors.HexColor("#1D5B9B")),
                ("TEXTCOLOR", (0, 0), (-1, 0), colors.white),
                ("FONTNAME", (0, 0), (-1, 0), bold_font),
                ("FONTNAME", (0, 1), (-1, -1), body_font),
                ("FONTSIZE", (0, 0), (-1, -1), 8.5),
                ("GRID", (0, 0), (-1, -1), 0.4, colors.HexColor("#CCCCCC")),
                ("ALIGN", (1, 1), (-1, -1), "RIGHT"),
                ("ROWBACKGROUNDS", (0, 1), (-1, -1), [colors.white, colors.HexColor("#F5F8FC")]),
                ("TOPPADDING", (0, 0), (-1, -1), 3),
                ("BOTTOMPADDING", (0, 0), (-1, -1), 3),
            ]
        )
    )
    story.append(pkg_tbl)
    story.append(PageBreak())

    # Seção 2: Inventário completo dos arquivos Kotlin do módulo principal
    story.append(Paragraph("2. INVENTÁRIO COMPLETO DOS ARQUIVOS-FONTE KOTLIN (com.inventario.mobile)", st_h1))
    story.append(
        Paragraph(
            f"Listagem integral dos {len(metrics['kt_files_rel'])} arquivos Kotlin pertencentes ao pacote "
            "com.inventario.mobile em app/src/main/java:",
            st_body,
        )
    )
    kt_listing = "\n".join(f"{i:03d}. {rel}" for i, rel in enumerate(metrics["kt_files_rel"], start=1))
    story.append(Preformatted(kt_listing, st_code))
    story.append(PageBreak())

    # Seção 3: Código-Fonte dos Módulos Principais
    story.append(Paragraph("3. LISTAGEM DE CÓDIGO-FONTE DOS MÓDULOS PRINCIPAIS", st_h1))
    story.append(
        Paragraph(
            "A seguir são apresentados os códigos-fonte integrais (ou blocos iniciais até 1.200 linhas por módulo) "
            "dos principais componentes representativos de todas as camadas do SIHCP Mobile.",
            st_body,
        )
    )

    for label, fpath in REPRESENTATIVE_SOURCE_FILES:
        if not fpath.exists():
            continue
        rel_path = str(fpath.relative_to(MOBILE_DIR)).replace("\\", "/")
        story.append(Paragraph(f"{xml_escape(label)} — {xml_escape(rel_path)}", st_h2))
        content = fpath.read_text(encoding="utf-8", errors="ignore")
        wrapped = wrap_code_lines(content, width=92, max_lines=900)
        story.append(Preformatted(wrapped, st_code))

    doc.build(story, onFirstPage=draw_header_footer, onLaterPages=draw_header_footer)


# ==============================================================================
# GERAÇÃO DO ZIP COMPLETO DE CÓDIGO-FONTE
# ==============================================================================

def build_source_zip(zip_path: Path, pdf_path: Path) -> int:
    """Empacota todo o código-fonte de app/src/main, Gradle, ProGuard e o PDF INPI."""
    added = 0
    extra_files = [
        MOBILE_DIR / "build.gradle",
        MOBILE_DIR / "settings.gradle",
        MOBILE_DIR / "gradle.properties",
        MOBILE_DIR / "app" / "build.gradle",
        MOBILE_DIR / "app" / "proguard-rules.pro",
    ]
    with zipfile.ZipFile(zip_path, "w", compression=zipfile.ZIP_DEFLATED, compresslevel=9) as zf:
        for fpath in extra_files:
            if fpath.exists():
                arcname = f"InventarioMobile/{fpath.relative_to(MOBILE_DIR)}".replace("\\", "/")
                zf.write(fpath, arcname)
                added += 1

        for root, _, files in os.walk(SRC_MAIN_DIR):
            for name in sorted(files):
                fpath = Path(root) / name
                arcname = f"InventarioMobile/{fpath.relative_to(MOBILE_DIR)}".replace("\\", "/")
                zf.write(fpath, arcname)
                added += 1

        if pdf_path.exists():
            zf.write(pdf_path, pdf_path.name)
            added += 1

    return added


# ==============================================================================
# UTILITÁRIOS DE FORMATAÇÃO WORD (.DOCX)
# ==============================================================================

def set_cell_shading(cell, hex_color: str) -> None:
    shading = parse_xml(f'<w:shd {nsdecls("w")} w:fill="{hex_color}"/>')
    cell._tc.get_or_add_tcPr().append(shading)


def set_table_borders(table, color: str = "000000", sz: str = "6") -> None:
    tblPr = table._tbl.tblPr
    borders = parse_xml(
        f'<w:tblBorders {nsdecls("w")}>'
        f'<w:top w:val="single" w:sz="{sz}" w:space="0" w:color="{color}"/>'
        f'<w:left w:val="single" w:sz="{sz}" w:space="0" w:color="{color}"/>'
        f'<w:bottom w:val="single" w:sz="{sz}" w:space="0" w:color="{color}"/>'
        f'<w:right w:val="single" w:sz="{sz}" w:space="0" w:color="{color}"/>'
        f'<w:insideH w:val="single" w:sz="{sz}" w:space="0" w:color="{color}"/>'
        f'<w:insideV w:val="single" w:sz="{sz}" w:space="0" w:color="{color}"/>'
        f"</w:tblBorders>"
    )
    tblPr.append(borders)


def set_cell_margins(cell, top: int = 100, bottom: int = 100, left: int = 140, right: int = 140) -> None:
    tcPr = cell._tc.get_or_add_tcPr()
    tcMar = OxmlElement("w:tcMar")
    for side, val in (("top", top), ("bottom", bottom), ("left", left), ("right", right)):
        node = OxmlElement(f"w:{side}")
        node.set(qn("w:w"), str(val))
        node.set(qn("w:type"), "dxa")
        tcMar.append(node)
    tcPr.append(tcMar)


def configure_doc_margins(doc: DocxDocument, top: float = 2.0, bottom: float = 2.0, left: float = 2.2, right: float = 2.0) -> None:
    for sec in doc.sections:
        sec.top_margin = Cm(top)
        sec.bottom_margin = Cm(bottom)
        sec.left_margin = Cm(left)
        sec.right_margin = Cm(right)
    normal = doc.styles["Normal"]
    normal.font.name = "Calibri"
    normal.font.size = Pt(11)


def style_header_row(row, bg_hex: str = "0D3B66") -> None:
    for cell in row.cells:
        set_cell_shading(cell, bg_hex)
        set_cell_margins(cell, 90, 90, 120, 120)
        cell.vertical_alignment = WD_ALIGN_VERTICAL.CENTER
        for p in cell.paragraphs:
            for r in p.runs:
                r.bold = True
                r.font.color.rgb = RGBColor(255, 255, 255)
                r.font.size = Pt(10)


def add_styled_table(doc: DocxDocument, headers: List[str], rows: List[List[str]], col_widths_cm: List[float]) -> None:
    tbl = doc.add_table(rows=len(rows) + 1, cols=len(headers))
    tbl.alignment = WD_TABLE_ALIGNMENT.CENTER
    set_table_borders(tbl, color="B0C4DE", sz="4")

    for j, h in enumerate(headers):
        cell = tbl.rows[0].cells[j]
        cell.width = Cm(col_widths_cm[j])
        cell.text = h
    style_header_row(tbl.rows[0], "0D3B66")

    for i, row_vals in enumerate(rows, start=1):
        bg = "F5F8FC" if i % 2 == 0 else "FFFFFF"
        for j, val in enumerate(row_vals):
            cell = tbl.rows[i].cells[j]
            cell.width = Cm(col_widths_cm[j])
            cell.text = val
            set_cell_shading(cell, bg)
            set_cell_margins(cell, 70, 70, 110, 110)
            cell.vertical_alignment = WD_ALIGN_VERTICAL.TOP
            for p in cell.paragraphs:
                for r in p.runs:
                    r.font.size = Pt(9.5)
                    if j == 0:
                        r.bold = True
    doc.add_paragraph()


# ==============================================================================
# 01 - FORMULÁRIO DE IDENTIFICAÇÃO DO PROGRAMA DE COMPUTADOR (DOCX + MD)
# ==============================================================================

def generate_formulario_identificacao(
    docx_path: Path,
    md_path: Path,
    pdf_hash_info: Dict[str, Any],
    zip_hash_info: Dict[str, Any],
) -> None:
    """Gera o formulário oficial de identificação reproduzindo a imagem do usuário."""
    doc = Document()
    configure_doc_margins(doc, top=2.0, bottom=2.0, left=2.0, right=2.0)

    # Cabeçalho institucional discreto
    p_inst = doc.add_paragraph()
    p_inst.alignment = WD_ALIGN_PARAGRAPH.CENTER
    r_inst = p_inst.add_run(
        "REGISTRO DE PROGRAMA DE COMPUTADOR — INPI / NIT-IFMT\n"
        "Formulário de Identificação Técnica do Software"
    )
    r_inst.bold = True
    r_inst.font.size = Pt(11)
    r_inst.font.color.rgb = RGBColor(13, 59, 102)

    # Título exato da imagem enviada pelo usuário
    p_h1 = doc.add_paragraph()
    p_h1.paragraph_format.space_before = Pt(10)
    p_h1.paragraph_format.space_after = Pt(8)
    r_h1 = p_h1.add_run("1 . IDENTIFICAÇÃO DO PROGRAMA DE COMPUTADOR")
    r_h1.bold = True
    r_h1.font.name = "Calibri"
    r_h1.font.size = Pt(13)
    r_h1.font.color.rgb = RGBColor(0, 0, 0)

    # Tabela exatamente com os 9 campos da imagem
    rows_form = [
        ("Título do Programa de Computador", PROGRAMA_INFO["titulo_completo"]),
        ("Data da Criação", PROGRAMA_INFO["data_criacao"]),
        ("Data de Publicação", PROGRAMA_INFO["data_publicacao"]),
        ("Tipo de Linguagem*", PROGRAMA_INFO["linguagens"]),
        ("Campo de Aplicação*", PROGRAMA_INFO["campo_aplicacao"]),
        ("Tipo de Programa*", PROGRAMA_INFO["tipo_programa"]),
        ("Algoritmo hash", PROGRAMA_INFO["algoritmo_hash"]),
        ("Resumo digital hash", str(pdf_hash_info["sha512"])),
        (
            "Descrição resumida do programa de computador(até 200 palavras)",
            PROGRAMA_INFO["descricao_resumida"],
        ),
    ]

    tbl = doc.add_table(rows=len(rows_form), cols=2)
    tbl.alignment = WD_TABLE_ALIGNMENT.CENTER
    set_table_borders(tbl, color="000000", sz="6")

    col_w = [Cm(5.6), Cm(11.4)]
    for i, (label, value) in enumerate(rows_form):
        c0 = tbl.rows[i].cells[0]
        c1 = tbl.rows[i].cells[1]
        c0.width, c1.width = col_w[0], col_w[1]
        set_cell_margins(c0, 90, 90, 120, 120)
        set_cell_margins(c1, 90, 90, 120, 120)
        c0.vertical_alignment = WD_ALIGN_VERTICAL.CENTER
        c1.vertical_alignment = WD_ALIGN_VERTICAL.CENTER

        p0 = c0.paragraphs[0]
        p0.paragraph_format.space_after = Pt(0)
        r0 = p0.add_run(label)
        r0.font.size = Pt(10.5)
        r0.font.name = "Calibri"

        p1 = c1.paragraphs[0]
        p1.paragraph_format.space_after = Pt(0)
        r1 = p1.add_run(value)
        if label == "Resumo digital hash":
            r1.font.name = "Consolas"
            r1.font.size = Pt(8.5)
        else:
            r1.font.name = "Calibri"
            r1.font.size = Pt(10)

    # Nota de rodapé da tabela igual à imagem
    p_foot = doc.add_paragraph()
    p_foot.paragraph_format.space_before = Pt(4)
    p_foot.paragraph_format.space_after = Pt(12)
    r_foot = p_foot.add_run("* Podem ser selecionados vários tipos linguagem, programa e aplicação.")
    r_foot.font.size = Pt(9.5)

    # Seção complementar: Referência exata do arquivo hash + alternativa ZIP
    p_h2 = doc.add_paragraph()
    r_h2 = p_h2.add_run("2 . REFERÊNCIA DO ARQUIVO DE GUARDA DIGITAL (HASH)")
    r_h2.bold = True
    r_h2.font.size = Pt(12)
    r_h2.font.color.rgb = RGBColor(13, 59, 102)

    add_styled_table(
        doc,
        ["Artefato Digital (Pasta 04_Pacote_Codigo_Fonte_Guardado_INPI)", "Tamanho / Algoritmo", "Resumo Digital Hash"],
        [
            [
                f"PDF Oficial (utilizado no campo acima):\n{pdf_hash_info['filename']}",
                f"{pdf_hash_info['size_bytes']:,} bytes\nSHA-512",
                str(pdf_hash_info["sha512"]),
            ],
            [
                f"Pacote ZIP Completo (alternativo):\n{zip_hash_info['filename']}",
                f"{zip_hash_info['size_bytes']:,} bytes\nSHA-512",
                str(zip_hash_info["sha512"]),
            ],
            [
                f"SHA-256 de conferência (PDF):\n{pdf_hash_info['filename']}",
                "SHA-256",
                str(pdf_hash_info["sha256"]),
            ],
        ],
        [5.4, 3.2, 8.4],
    )

    # Seção 3: Titularidade e Autoria para protocolo NIT-IFMT
    p_h3 = doc.add_paragraph()
    r_h3 = p_h3.add_run("3 . IDENTIFICAÇÃO DO TITULAR E DOS AUTORES (PARA PROTOCOLO NIT / INPI)")
    r_h3.bold = True
    r_h3.font.size = Pt(12)
    r_h3.font.color.rgb = RGBColor(13, 59, 102)

    add_styled_table(
        doc,
        ["Campo", "Preenchimento / Observação"],
        [
            ["Titular dos Direitos Patrimoniais", f"{PROGRAMA_INFO['titular']} — CNPJ: [Preencher CNPJ do IFMT / Reitoria]"],
            ["Autor Principal (Desenvolvedor)", f"{PROGRAMA_INFO['autor_principal']} — E-mail: {PROGRAMA_INFO['email_autor']} | CPF: [Preencher] | SIAPE/Vínculo: [Preencher]"],
            ["Coautores / Orientação (se aplicável)", "[Listar orientador/colaboradores, se houver, com CPF, e-mail e percentual de participação]"],
            ["Tipo de Vínculo de Criação", "Desenvolvido no âmbito institucional / pesquisa aplicada no IFMT (Lei nº 9.609/1998, art. 4º)"],
            ["Versão / Build Registrada", f"{PROGRAMA_INFO['versao_atual']} (Release de {PROGRAMA_INFO['data_versao_atual']})"],
            ["Título Curto Sugerido no Portal e-Software", PROGRAMA_INFO["titulo_curto"]],
            ["Contagem da Descrição Resumida", f"{len(PROGRAMA_INFO['descricao_resumida'].split())} palavras (limite oficial INPI: até 200 palavras)"],
        ],
        [5.6, 11.4],
    )

    doc.save(str(docx_path))

    # Versão Markdown espelho
    word_count = len(PROGRAMA_INFO["descricao_resumida"].split())
    md_content = f"""# 1 . IDENTIFICAÇÃO DO PROGRAMA DE COMPUTADOR

| Campo | Valor |
|---|---|
| **Título do Programa de Computador** | {PROGRAMA_INFO["titulo_completo"]} |
| **Data da Criação** | {PROGRAMA_INFO["data_criacao"]} |
| **Data de Publicação** | {PROGRAMA_INFO["data_publicacao"]} |
| **Tipo de Linguagem\\*** | {PROGRAMA_INFO["linguagens"]} |
| **Campo de Aplicação\\*** | {PROGRAMA_INFO["campo_aplicacao"]} |
| **Tipo de Programa\\*** | {PROGRAMA_INFO["tipo_programa"]} |
| **Algoritmo hash** | {PROGRAMA_INFO["algoritmo_hash"]} |
| **Resumo digital hash** | `{pdf_hash_info["sha512"]}` |
| **Descrição resumida do programa de computador(até 200 palavras)** | {PROGRAMA_INFO["descricao_resumida"]} |

\\* Podem ser selecionados vários tipos linguagem, programa e aplicação.

> **Contagem de palavras da descrição resumida:** {word_count} palavras (limite máximo: 200 palavras).

---

## 2 . REFERÊNCIA DO ARQUIVO DE GUARDA DIGITAL (HASH)

- **Arquivo principal vinculado ao hash acima (PDF Oficial):** `04_Pacote_Codigo_Fonte_Guardado_INPI/{pdf_hash_info["filename"]}` ({pdf_hash_info["size_bytes"]:,} bytes)
  - **SHA-512:** `{pdf_hash_info["sha512"]}`
  - **SHA-256:** `{pdf_hash_info["sha256"]}`
- **Pacote ZIP completo alternativo:** `04_Pacote_Codigo_Fonte_Guardado_INPI/{zip_hash_info["filename"]}` ({zip_hash_info["size_bytes"]:,} bytes)
  - **SHA-512:** `{zip_hash_info["sha512"]}`
  - **SHA-256:** `{zip_hash_info["sha256"]}`

> **Importante:** Conforme o Art. 2º da IN INPI/PR nº 099/2019, o titular deve manter sob guarda inviolável (sem alterar nenhum byte) exatamente o arquivo cujo hash foi submetido no registro.

---

## 3 . IDENTIFICAÇÃO DO TITULAR E DOS AUTORES (PARA PROTOCOLO NIT / INPI)

| Campo | Preenchimento / Observação |
|---|---|
| **Titular dos Direitos Patrimoniais** | {PROGRAMA_INFO["titular"]} — CNPJ: `[Preencher]` |
| **Autor Principal** | {PROGRAMA_INFO["autor_principal"]} — `{PROGRAMA_INFO["email_autor"]}` — CPF: `[Preencher]` |
| **Coautores / Orientadores** | `[Adicionar se aplicável]` |
| **Versão Registrada** | {PROGRAMA_INFO["versao_atual"]} ({PROGRAMA_INFO["data_versao_atual"]}) |
| **Produção / Primeira Liberação** | 1ª Release v1.0.0: 09/11/2025 · Início em produção IFMT: 18/11/2025 |
"""
    md_path.write_text(md_content, encoding="utf-8")


# ==============================================================================
# 02 - MEMORIAL DESCRITIVO E ARQUITETURA (DOCX + MD)
# ==============================================================================

def generate_memorial_descritivo(
    docx_path: Path,
    md_path: Path,
    metrics: Dict[str, Any],
    pdf_hash_info: Dict[str, Any],
) -> None:
    """Gera o Memorial Descritivo completo do SIHCP Mobile em DOCX e Markdown."""
    doc = Document()
    configure_doc_margins(doc, top=2.2, bottom=2.0, left=2.4, right=2.0)

    # Capa / Cabeçalho
    p_title = doc.add_paragraph()
    p_title.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_title.paragraph_format.space_before = Pt(24)
    r_title = p_title.add_run("MEMORIAL DESCRITIVO E ESPECIFICAÇÃO ARQUITETURAL\nDO PROGRAMA DE COMPUTADOR")
    r_title.bold = True
    r_title.font.size = Pt(16)
    r_title.font.color.rgb = RGBColor(13, 59, 102)

    p_sub = doc.add_paragraph()
    p_sub.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_sub.paragraph_format.space_after = Pt(18)
    r_sub = p_sub.add_run(
        f"{PROGRAMA_INFO['titulo_completo']}\n"
        f"Versão {PROGRAMA_INFO['versao_atual']} | Pacote: {PROGRAMA_INFO['application_id']}\n"
        f"Instituto Federal de Educação, Ciência e Tecnologia de Mato Grosso (IFMT)"
    )
    r_sub.font.size = Pt(11)
    r_sub.font.color.rgb = RGBColor(60, 60, 60)

    def add_h1(text: str) -> None:
        p = doc.add_paragraph()
        p.paragraph_format.space_before = Pt(14)
        p.paragraph_format.space_after = Pt(6)
        r = p.add_run(text)
        r.bold = True
        r.font.size = Pt(13)
        r.font.color.rgb = RGBColor(13, 59, 102)

    def add_h2(text: str) -> None:
        p = doc.add_paragraph()
        p.paragraph_format.space_before = Pt(10)
        p.paragraph_format.space_after = Pt(4)
        r = p.add_run(text)
        r.bold = True
        r.font.size = Pt(11)
        r.font.color.rgb = RGBColor(29, 91, 155)

    def add_p(text: str) -> None:
        p = doc.add_paragraph(text)
        p.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
        p.paragraph_format.space_after = Pt(6)

    def add_bullets(items: Iterable[str]) -> None:
        for item in items:
            p = doc.add_paragraph(style="List Bullet")
            p.paragraph_format.space_after = Pt(3)
            p.add_run(item)

    # 1. Visão Geral
    add_h1("1. IDENTIFICAÇÃO E VISÃO GERAL DO SOFTWARE")
    add_p(
        "O SIHCP Mobile (Sistema de Histórico e Coleta Patrimonial Mobile) é um aplicativo nativo "
        "para dispositivos móveis Android, integrante do ecossistema SIHCP desenvolvido para modernizar, "
        "automatizar e auditar o inventário físico de bens patrimoniais no Instituto Federal de Mato Grosso (IFMT). "
        "Concebido sob a abordagem offline-first e estruturado segundo os princípios de Clean Architecture e "
        "padrão arquitetural MVVM (Model-View-ViewModel), o software elimina planilhas manuais e formulários "
        "em papel, prevenindo duplicidades e divergências em campo."
    )
    add_styled_table(
        doc,
        ["Atributo Técnico", "Especificação Verificada"],
        [
            ["Nome / Sigla", PROGRAMA_INFO["titulo_completo"]],
            ["Application ID / Package", PROGRAMA_INFO["application_id"]],
            ["Versão Atual / versionCode", PROGRAMA_INFO["versao_atual"]],
            ["Data de Criação / 1ª Release", f"{PROGRAMA_INFO['data_criacao']} / {PROGRAMA_INFO['data_publicacao']}"],
            ["Compatibilidade Android", "Min SDK 23 (Android 6.0) · Target/Compile SDK 34 (Android 14)"],
            ["Linguagem / Build", "Kotlin 1.9.10, XML, SQL, Java · Gradle 8.1.4 + KSP"],
            ["Volume de Código", f"{metrics['kt_files_count']} Kotlin em src/main ({metrics['kt_lines_total']:,} linhas), {metrics['test_kt_count']} testes ({metrics['total_kt_project']} .kt totais), {metrics['xml_files_count']} XML ({metrics['xml_lines_total']:,} linhas; {metrics['layout_xml_count']} layouts)"],
            ["Componentes Arquiteturais", f"{metrics['activities_count']} Activities · {metrics['fragments_count']} Fragments · {metrics['viewmodels_count']} ViewModels · {metrics['usecases_count']} UseCases · {metrics['workers_count']} Workers"],
            ["Hash SHA-512 do Pacote PDF INPI", str(pdf_hash_info["sha512"])],
        ],
        [5.5, 11.5],
    )

    # 2. Problema Resolvido e Justificativa
    add_h1("2. PROBLEMA RESOLVIDO E INOVAÇÃO ORGANIZACIONAL")
    add_p(
        "Em instituições públicas federais com acervos distribuídos em múltiplos campi e salas, o inventário "
        "patrimonial tradicional sofria com lentidão na conferência física, erros de transcrição manual, "
        "impossibilidade de detecção imediata de duplicidades e ausência de rastreabilidade auditável. "
        "O SIHCP Mobile atua diretamente sobre esses gargalos ao permitir que servidores e comissões de "
        "inventário realizem coletas móveis rápidas via leitura óptica, mesmo em salas e galpões sem cobertura "
        "Wi-Fi ou rede móvel, mantendo sincronização resiliente com o servidor institucional Spring Boot / PostgreSQL."
    )
    add_bullets([
        "Redução drástica do tempo operacional por bem inventariado por meio de leitura contínua de QR Code e código de barras.",
        "Prevenção de coletas duplicadas com validação híbrida (servidor como fonte de verdade quando online; cache Room local quando offline).",
        "Detecção automática de divergência física (localização encontrada diferente da sala cadastrada e alteração do estado de conservação).",
        "Rastreabilidade completa com registro de usuário, data/hora, método de coleta, tipo de scan, coordenadas GPS opcionais, clienteUuid idempotente e trilha de auditoria local.",
    ])

    # 3. Arquitetura do Sistema
    add_h1("3. ARQUITETURA DE SOFTWARE (CLEAN ARCHITECTURE + MVVM)")
    add_p(
        "O aplicativo segue separação estrita de responsabilidades em camadas concêntricas, com injeção de "
        "dependências gerenciada por Hilt (Dagger 2.48) e programação reativa/assíncrona com Kotlin Coroutines e StateFlow:"
    )
    add_styled_table(
        doc,
        ["Camada", "Pacotes Principais", "Responsabilidades"],
        [
            [
                "Apresentação (Presentation)",
                "presentation/setup, login, main, dashboard, scanner, coleta, coletas, sala, descricao, detail, export, filtros, historico, inventario, search, settings, statistics, sync",
                f"{metrics['activities_count']} Activities, {metrics['fragments_count']} Fragments, {metrics['viewmodels_count']} ViewModels (@HiltViewModel), adapters com Paging 3, estados imutáveis e mappers puros de UI (ex.: DashboardUiMapper).",
            ],
            [
                "Domínio (Domain)",
                "domain/model, domain/repository, domain/usecase, domain/validator",
                f"Modelos de domínio em Kotlin puro, {metrics['domain_repos_count']} interfaces de repositório, validadores de negócio (ColetaValidator) e {metrics['usecases_count']} casos de uso especializados.",
            ],
            [
                "Dados (Data)",
                "data/local (database, dao, entity), data/remote (api, dto), data/repository, data/mapper, data/realtime, data/pdf, data/export, data/paging, data/cache",
                f"Persistência offline em Room v19 (15 migrações) criptografado com SQLCipher, {metrics['apis_count']} APIs Retrofit, {metrics['data_repos_count']} repositórios/adaptadores, WebSocket ao vivo e geradores PDF/Excel/CSV.",
            ],
            [
                "Segurança e Infraestrutura",
                "security, network, worker, sync, di, utils",
                f"{metrics['security_count']} classes de segurança (SqlCipherKeyManager, KeystoreRecoveryActivity, Biometric, PIN), {metrics['network_count']} classes de rede (RefreshTokenInterceptor, OfflineFallback) e {metrics['workers_count']} Workers (ColetaSyncWorker unificado).",
            ],
        ],
        [3.6, 5.6, 7.8],
    )

    # 4. Funcionalidades Principais
    add_h1("4. MÓDULOS E FUNCIONALIDADES PRINCIPAIS")
    add_bullets([
        "Configuração Inicial Dinâmica (InitialSetupActivity): permite configurar e testar o IP/porta do servidor (/api/mobile/health) na primeira abertura do aplicativo, eliminando endereço fixo embutido.",
        "Autenticação Multicontrole: login com credenciais via JWT (Access Token de 1h + Refresh Token de 14 dias com renovação transparente no RefreshTokenInterceptor), suporte a Biometria (AndroidX Biometric) e PIN local de 6 dígitos.",
        "Múltiplos Modos de Coleta: leitura contínua de QR Code e código de barras (ZXing / CameraX com troca de câmera e lanterna/flash), coleta manual por número de tombamento, coleta por descrição com sugestões e foto de referência, e cadastro de item sem etiqueta com fotografia obrigatória.",
        "Gestão de Itens Compostos e Divergências: registro de estado de conservação, sala encontrada, observações e cálculo automático de divergências em relação ao cadastro patrimonial.",
        "Dashboard Gerencial e Monitoramento ao Vivo: KPIs em tempo real, gráfico de evolução (MPAndroidChart), ranking de coletores, projeção de término e feed em tempo real via WebSocket (/ws-coletas-live) através de RealtimeColetasManager.",
        "Sincronização Inteligente em Segundo Plano: envio em lote (batch sync) com fallback individual e retentativa exponencial via WorkManager (ColetaSyncWorker, PhotoSyncWorker), além de limpeza automática de coletas excluídas no servidor.",
        "Relatórios e Exportação: geração e compartilhamento de relatórios gerais em PDF (iText 7), Excel (TSV/XLSX) e CSV, além de relatório fotográfico para itens sem etiqueta (perfis Supervisor/Admin).",
    ])

    # 5. Banco de Dados Local e Segurança
    add_h1("5. PERSISTÊNCIA LOCAL E SEGURANÇA DA INFORMAÇÃO")
    add_p(
        "O banco de dados local AppDatabase (arquivo inventario_offline_secure.db, versão 19) utiliza SQLite via "
        "AndroidX Room 2.6.1 integrado à biblioteca net.zetetic:sqlcipher-android:4.6.1 com criptografia AES-256. "
        "A passphrase de 256 bits é gerada aleatoriamente por instalação com SecureRandom e armazenada em "
        "EncryptedSharedPreferences protegida por MasterKey no Android Keystore (SqlCipherKeyManager). "
        "Em compilações de produção (release), o código é minificado e ofuscado com R8/ProGuard e os logs sensíveis são sanitizados."
    )
    add_styled_table(
        doc,
        ["Entidade Room (AppDatabase v19)", "Finalidade no Aplicativo"],
        [
            ["PatrimonioEntity", "Cache local do acervo patrimonial para validação instantânea e buscas offline."],
            ["SalaEntity / ResponsavelEntity", "Estrutura organizacional de salas e servidores responsáveis."],
            ["ColetaEntity", "Registro das coletas realizadas (etiqueta, sala encontrada, estado, divergência, fotos, clienteUuid idempotente e status de sincronização)."],
            ["SincronizacaoEntity / SyncLogEntity", "Controle de lotes, tentativas de reenvio e diagnósticos de sincronização."],
            ["LogColetaEntity", "Trilha de auditoria local das operações de coleta."],
            ["HistoricoScanEntity", "Histórico cronológico de leituras de QR Code, código de barras e consultas."],
            ["FotoReferenciaEntity / SugestaoDescricaoEntity", "Fotos de referência e sugestões de descrição para apoio em campo sem internet."],
        ],
        [5.5, 11.5],
    )

    # 6. Stack Tecnológica
    add_h1("6. STACK TECNOLÓGICA E BIBLIOTECAS")
    add_styled_table(
        doc,
        ["Categoria", "Tecnologia / Biblioteca", "Versão"],
        [
            ["Linguagem e Assincronismo", "Kotlin, Coroutines & Flow", "1.9.10 / 1.7.3"],
            ["Interface Gráfica", "AndroidX AppCompat, Material Design 3, ConstraintLayout, MPAndroidChart", "1.6.1 / 1.10.0 / v3.1.0"],
            ["Injeção de Dependência", "Dagger Hilt + Hilt Work", "2.48 / 1.1.0"],
            ["Persistência e Paginação", "Room + KSP + Paging 3", "2.6.1 / 3.2.1"],
            ["Criptografia e Autenticação", "SQLCipher Android, Security Crypto (Tink), AndroidX Biometric", "4.6.1 / 1.1.0-alpha06 / 1.1.0"],
            ["Rede e Tempo Real", "Retrofit 2 + Gson + OkHttp 4 (REST e WebSocket)", "2.9.0 / 4.12.0"],
            ["Captura Óptica e Câmera", "ZXing Android Embedded + Core + CameraX", "4.3.0 / 3.5.2 / 1.3.0"],
            ["Background e Imagens", "WorkManager + Glide + ThreeTenABP", "2.9.0 / 4.16.0 / 1.4.6"],
            ["Geração de Documentos", "iText 7 Core (PDF)", "7.2.5"],
            ["Qualidade e Testes", "JUnit 4, Mockito, MockK, Kotest, jqwik (Property-Based Testing)", "4.13.2 / 5.8.0 / 1.8.4"],
        ],
        [4.5, 9.0, 3.5],
    )

    # 7. Conformidade Legal e Normativa
    add_h1("7. CONFORMIDADE NORMATIVA E PROPRIEDADE INTELECTUAL")
    add_bullets([
        "Lei nº 9.609/1998 e Decreto nº 2.556/1998: proteção jurídica de programa de computador e titularidade institucional.",
        "Instrução Normativa INPI/PR nº 099/2019: documentação técnica estruturada para vínculo criptográfico via resumo digital hash SHA-512.",
        "Instrução Normativa SEDAP nº 205/1988: conformidade com normas de administração e inventário de bens móveis no serviço público federal.",
        "Decreto nº 12.785/2025 (sucessor do Decreto nº 9.373/2018): suporte à classificação de estado de conservação e rastreabilidade patrimonial.",
    ])

    doc.save(str(docx_path))

    # Espelho Markdown
    md_text = f"""# Memorial Descritivo e Especificação Arquitetural — {PROGRAMA_INFO["titulo_curto"]}

**Programa:** {PROGRAMA_INFO["titulo_completo"]}  
**Versão:** {PROGRAMA_INFO["versao_atual"]}  
**Pacote Android:** `{PROGRAMA_INFO["application_id"]}`  
**Titular:** {PROGRAMA_INFO["titular"]}  
**Autor Principal:** {PROGRAMA_INFO["autor_principal"]}  
**Hash SHA-512 (PDF INPI):** `{pdf_hash_info["sha512"]}`

---

## 1. Visão Geral

{PROGRAMA_INFO["descricao_resumida"]}

## 2. Ficha Técnica Consolidada

| Atributo | Especificação |
|---|---|
| **Plataforma** | Android (Min SDK 23 / Android 6.0 · Target SDK 34 / Android 14) |
| **Linguagens** | {PROGRAMA_INFO["linguagens"]} |
| **Arquitetura** | Clean Architecture + MVVM + Offline-First |
| **Injeção de Dependência** | Hilt 2.48 (`@HiltAndroidApp`, `@HiltViewModel`, `@HiltWorker`) |
| **Banco Local** | Room 2.6.1 (v19, `inventario_offline_secure.db`) + SQLCipher 4.6.1 (AES-256) |
| **Segurança** | Android Keystore (`SqlCipherKeyManager`), `EncryptedSharedPreferences`, JWT + Refresh Token, Biometria, PIN, R8/ProGuard |
| **Sincronização** | WorkManager (`ColetaSyncWorker`, `PhotoSyncWorker`), Batch Sync + fallback individual + backoff exponencial |
| **Tempo Real** | WebSocket nativo `/ws-coletas-live` (`RealtimeColetasManager`) |
| **Leitura Óptica** | ZXing 4.3.0 / Core 3.5.2 + CameraX 1.3.0 |
| **Exportação** | PDF (iText 7.2.5), Excel (TSV/XLSX) e CSV |
| **Métricas Verificadas** | {metrics["kt_files_count"]} Kotlin em `src/main` ({metrics["kt_lines_total"]:,} linhas) + {metrics["test_kt_count"]} testes (`{metrics["total_kt_project"]}` `.kt` totais) · {metrics["xml_files_count"]} XML ({metrics["xml_lines_total"]:,} linhas; {metrics["layout_xml_count"]} layouts) |
| **Componentes** | {metrics["activities_count"]} Activities · {metrics["fragments_count"]} Fragments · {metrics["viewmodels_count"]} ViewModels · {metrics["usecases_count"]} UseCases · {metrics["domain_repos_count"]}/{metrics["data_repos_count"]} Repositories · {metrics["workers_count"]} Workers |

## 3. Camadas Arquiteturais

1. **Presentation (`presentation/*`):** {metrics["activities_count"]} Activities, {metrics["fragments_count"]} Fragments, {metrics["viewmodels_count"]} ViewModels com `StateFlow`/`LiveData`, `DashboardUiMapper`, Paging 3 adapters e componentes reutilizáveis.
2. **Domain (`domain/*`):** Modelos puros em Kotlin, {metrics["domain_repos_count"]} interfaces de repositório, `ColetaValidator` e {metrics["usecases_count"]} casos de uso de coleta, validação, sincronização, consulta e exportação.
3. **Data (`data/*`):** `AppDatabase` v19 (10 entidades ativas, 11 DAOs ativos, 15 migrações), {metrics["apis_count"]} APIs Retrofit, DTOs, mappers, cache em memória, `RealtimeColetasManager` e geradores de relatórios.
4. **Infraestrutura (`security/*`, `network/*`, `worker/*`, `sync/*`, `di/*`):** {metrics["security_count"]} classes de segurança, {metrics["network_count"]} classes de monitoramento/interceptação de rede e {metrics["workers_count"]} Workers em segundo plano (`ColetaSyncWorker` unificado via `SyncScheduler`).

## 4. Conformidade Normativa

- **Lei nº 9.609/1998** e **IN INPI/PR nº 099/2019** (Registro de Programa de Computador).
- **IN SEDAP nº 205/1988** e **Decreto nº 12.785/2025** (Administração patrimonial na esfera federal).
"""
    md_path.write_text(md_text, encoding="utf-8")


# ==============================================================================
# 03 - INVENTÁRIO DE CÓDIGO-FONTE E MÉTRICAS
# ==============================================================================

def generate_inventario_codigo(md_path: Path, metrics: Dict[str, Any]) -> None:
    """Gera o relatório detalhado com métricas e listagem real dos arquivos-fonte."""
    ext_rows = "\n".join(
        f"| `{ext}` | {cnt} | {metrics['ext_lines'].get(ext, 0):,} |"
        for ext, cnt in metrics["ext_counts"].items()
    )
    pkg_rows = "\n".join(
        f"| `com.inventario.mobile.{pkg}` | {cnt} | {metrics['pkg_lines'].get(pkg, 0):,} |"
        for pkg, cnt in metrics["pkg_counts"].items()
    )
    kt_list = "\n".join(f"{i:03d}. `{rel}`" for i, rel in enumerate(metrics["kt_files_rel"], start=1))

    content = f"""# Inventário de Código-Fonte e Métricas — SIHCP Mobile

**Diretório analisado:** `InventarioMobile/app/src/main`  
**Versão:** {PROGRAMA_INFO["versao_atual"]}  
**Data da extração:** {datetime.now().strftime("%d/%m/%Y %H:%M:%S")}

---

## 1. Resumo Quantitativo Real

- **Total de arquivos em `app/src/main`:** {metrics["total_main_files"]}
- **Arquivos Kotlin (`.kt`) em `src/main`:** {metrics["kt_files_count"]} ({metrics["kt_lines_total"]:,} linhas)
- **Arquivos de teste Kotlin:** {metrics["unit_test_count"]} testes unitários (`src/test`) + {metrics["android_test_count"]} instrumentados (`src/androidTest`) = {metrics["test_kt_count"]} arquivos (**{metrics["total_kt_project"]} arquivos `.kt` no projeto**)
- **Arquivos XML (`.xml`) em `src/main`:** {metrics["xml_files_count"]} ({metrics["xml_lines_total"]:,} linhas, sendo {metrics["layout_xml_count"]} layouts de tela em `res/layout`)
- **Componentes Arquiteturais:** {metrics["activities_count"]} Activities · {metrics["fragments_count"]} Fragments · {metrics["viewmodels_count"]} ViewModels · {metrics["usecases_count"]} UseCases · {metrics["domain_repos_count"]} Domain Repositories · {metrics["data_repos_count"]} Data Repositories · {metrics["workers_count"]} Workers · {metrics["apis_count"]} Remote APIs · {metrics["security_count"]} Security · {metrics["network_count"]} Network

### 1.1 Distribuição por Extensão (`app/src/main`)

| Extensão | Quantidade de Arquivos | Linhas Totais |
|---|---:|---:|
{ext_rows}

### 1.2 Distribuição dos Arquivos Kotlin por Pacote (`com.inventario.mobile`)

| Pacote | Arquivos `.kt` | Linhas de Código |
|---|---:|---:|
{pkg_rows}

---

## 2. Lista Integral dos Arquivos Kotlin (`src/main/java/com/inventario/mobile`)

{kt_list}
"""
    md_path.write_text(content, encoding="utf-8")


# ==============================================================================
# 04 - COMPROVANTES DE HASH E 00 - GUIA PASSO A PASSO
# ==============================================================================

def generate_hash_receipts(pdf_info: Dict[str, Any], zip_info: Dict[str, Any]) -> None:
    """Grava comprovantes TXT e MD com os hashes reais para guarda e conferência."""
    now_str = datetime.now().strftime("%d/%m/%Y %H:%M:%S")
    txt = f"""================================================================================
COMPROVANTE DE RESUMO DIGITAL HASH PARA REGISTRO DE SOFTWARE (INPI)
================================================================================
Programa   : {PROGRAMA_INFO["titulo_completo"]}
Versão     : {PROGRAMA_INFO["versao_atual"]}
Titular    : {PROGRAMA_INFO["titular"]}
Autor      : {PROGRAMA_INFO["autor_principal"]}
Algoritmo  : SHA-512 (oficial INPI) / SHA-256 (conferência adicional)
Gerado em  : {now_str}

1) ARQUIVO PRINCIPAL VINCULADO AO FORMULÁRIO (PDF OFICIAL):
   Nome       : {pdf_info["filename"]}
   Tamanho    : {pdf_info["size_bytes"]} bytes
   SHA-512    : {pdf_info["sha512"]}
   SHA-256    : {pdf_info["sha256"]}

2) PACOTE COMPLETO DE CÓDIGO-FONTE (ZIP ALTERNATIVO):
   Nome       : {zip_info["filename"]}
   Tamanho    : {zip_info["size_bytes"]} bytes
   SHA-512    : {zip_info["sha512"]}
   SHA-256    : {zip_info["sha256"]}

3) COMANDO POWERSHELL PARA CONFERÊNCIA INDEPENDENTE:
   Get-FileHash -Algorithm SHA512 .\\{pdf_info["filename"]}
   Get-FileHash -Algorithm SHA512 .\\{zip_info["filename"]}

4) AVISO LEGAL DE GUARDA (IN INPI/PR nº 099/2019):
   NÃO edite, recomprima ou salve novamente o arquivo cujo hash foi submetido.
   Mantenha uma cópia intacta em repositório institucional / nuvem / mídia segura.
================================================================================
"""
    (PACOTE_INPI_DIR / "COMPROVANTE_HASH_INPI.txt").write_text(txt, encoding="utf-8")

    md = f"""# Comprovante de Resumo Digital Hash — INPI (SIHCP Mobile)

**Data de Geração:** {now_str}  
**Programa:** {PROGRAMA_INFO["titulo_completo"]} (`{PROGRAMA_INFO["versao_atual"]}`)

## Resumos Digitais Calculados

| Arquivo | Tamanho (bytes) | SHA-512 (Oficial INPI) | SHA-256 |
|---|---:|---|---|
| `{pdf_info["filename"]}` | {pdf_info["size_bytes"]:,} | `{pdf_info["sha512"]}` | `{pdf_info["sha256"]}` |
| `{zip_info["filename"]}` | {zip_info["size_bytes"]:,} | `{zip_info["sha512"]}` | `{zip_info["sha256"]}` |

## Conferência no Windows PowerShell

```powershell
Get-FileHash -Algorithm SHA512 "{pdf_info["filename"]}"
Get-FileHash -Algorithm SHA512 "{zip_info["filename"]}"
```

> **Regra de Ouro da Guarda Legal:** nunca altere ou gere novamente o arquivo cujo hash SHA-512 foi protocolado no INPI, pois qualquer alteração de 1 único bit muda completamente o resumo digital.
"""
    (PACOTE_INPI_DIR / "COMPROVANTE_HASH_INPI.md").write_text(md, encoding="utf-8")


def generate_readme_guide(
    readme_path: Path,
    pdf_info: Dict[str, Any],
    zip_info: Dict[str, Any],
) -> None:
    """Gera o guia de uso e submissão ao NIT-IFMT / Portal e-Software do INPI."""
    content = f"""# Pacote de Registro de Software — SIHCP Mobile (INPI / NIT-IFMT)

Esta pasta reúne toda a documentação técnica, jurídica e o pacote de código-fonte com resumo digital criptográfico (**SHA-512**) necessários para o pedido de Registro de Programa de Computador do **SIHCP Mobile** junto ao **NIT/IFMT** e ao **INPI** (sistema e-Software).

---

## Estrutura desta Pasta

| Arquivo / Subpasta | Finalidade |
|---|---|
| `00_LEIA_ME_GUIA_REGISTRO_INPI.md` | Este guia com o roteiro completo de conferência e protocolo. |
| `01_Formulario_Identificacao_INPI_SIHCP_Mobile.docx` | Tabela **1. IDENTIFICAÇÃO DO PROGRAMA DE COMPUTADOR** pronta para entrega ao NIT/IFMT ou preenchimento no e-Software, com o hash **SHA-512** real já preenchido. |
| `01_Formulario_Identificacao_INPI_SIHCP_Mobile.md` | Espelho em Markdown do formulário de identificação. |
| `02_Memorial_Descritivo_e_Arquitetura_SIHCP_Mobile.docx` | Memorial Descritivo completo (arquitetura Clean/MVVM, funcionalidades, banco SQLCipher, segurança, stack e conformidade legal). |
| `02_Memorial_Descritivo_e_Arquitetura_SIHCP_Mobile.md` | Espelho em Markdown do memorial descritivo. |
| `03_Inventario_Codigo_Fonte_e_Metricas.md` | Inventário quantitativo e listagem completa dos arquivos-fonte reais de `InventarioMobile`. |
| `04_Pacote_Codigo_Fonte_Guardado_INPI/` | Pasta contendo os arquivos originais imutáveis de guarda (`PDF` e `ZIP`) e o `COMPROVANTE_HASH_INPI.txt`. |

---

## Qual arquivo devo guardar para o Hash SHA-512?

No registro do INPI, **você NÃO envia o código-fonte ao INPI** — você informa apenas o **Resumo Digital Hash (SHA-512)** no formulário e **guarda o arquivo original** sob sigilo/segurança do titular (IFMT / autor).

1. **Opção Padrão (já preenchida no Formulário 01):**
   - **Arquivo:** `04_Pacote_Codigo_Fonte_Guardado_INPI/{pdf_info["filename"]}`
   - **SHA-512:** `{pdf_info["sha512"]}`
   - *Vantagem:* formato padrão mais aceito pelos NITs universitários/federais, por conter capa, memorial, inventário e listagem de código em documento único e legível.

2. **Opção Alternativa (Pacote ZIP integral com todo `src/main` + PDF):**
   - **Arquivo:** `04_Pacote_Codigo_Fonte_Guardado_INPI/{zip_info["filename"]}`
   - **SHA-512:** `{zip_info["sha512"]}`
   - *Quando usar:* caso o NIT do IFMT solicite o hash calculado sobre o arquivo `.zip` completo contendo todos os XMLs e classes Kotlin. Basta copiar este hash para a linha *Resumo digital hash* do Formulário `01`.

---

## Checklist Antes de Protocolar no NIT / INPI

1. Abra `01_Formulario_Identificacao_INPI_SIHCP_Mobile.docx` e preencha os dados pessoais faltantes na Seção 3 (**CPF**, **SIAPE**, **CNPJ do IFMT** e eventuais **coautores/orientadores**).
2. Confirme se o NIT solicita o campo **Data de Publicação** como `09/11/2025` (1ª release APK v1.0.0) ou `18/11/2025` (início oficial do inventário em produção no IFMT).
3. No portal e-Software do INPI (ou formulário do NIT), verifique a seleção exata dos códigos de **Linguagem**, **Campo de Aplicação** e **Tipo de Programa** conforme a tabela oficial vigente no sistema do INPI:
   - Linguagens: `Kotlin`, `XML`, `SQL`, `Java`
   - Campo de Aplicação: `AD-01`, `AD-03`, `GI-01`, `ED-01`
   - Tipo de Programa: `AP-01`, `GI-01`, `TC-01`, `UT-01`
4. Faça backup da pasta `04_Pacote_Codigo_Fonte_Guardado_INPI/` em pelo menos dois locais seguros (ex.: servidor/Drive institucional do IFMT + mídia externa) **sem reabrir/resalvar o PDF ou o ZIP**.
"""
    readme_path.write_text(content, encoding="utf-8")


# ==============================================================================
# EXECUÇÃO PRINCIPAL
# ==============================================================================

def main() -> None:
    BASE_DIR.mkdir(parents=True, exist_ok=True)
    PACOTE_INPI_DIR.mkdir(parents=True, exist_ok=True)

    print("[1/6] Coletando métricas reais do código-fonte em InventarioMobile...")
    metrics = collect_source_metrics()
    print(
        f"      -> {metrics['kt_files_count']} arquivos Kotlin ({metrics['kt_lines_total']} linhas), "
        f"{metrics['xml_files_count']} XML ({metrics['xml_lines_total']} linhas)"
    )

    pdf_path = PACOTE_INPI_DIR / PDF_FILENAME
    zip_path = PACOTE_INPI_DIR / ZIP_FILENAME

    print(f"[2/6] Gerando PDF oficial para guarda INPI: {pdf_path.name}...")
    build_inpi_pdf(pdf_path, metrics)

    print(f"[3/6] Gerando pacote ZIP completo de código-fonte: {zip_path.name}...")
    zip_count = build_source_zip(zip_path, pdf_path)
    print(f"      -> {zip_count} arquivos empacotados no ZIP.")

    print("[4/6] Calculando hashes SHA-512 e SHA-256 reais...")
    pdf_info = compute_hashes(pdf_path)
    zip_info = compute_hashes(zip_path)
    generate_hash_receipts(pdf_info, zip_info)

    print("[5/6] Gerando 01_Formulario_Identificacao_INPI_SIHCP_Mobile (.docx e .md)...")
    generate_formulario_identificacao(
        BASE_DIR / "01_Formulario_Identificacao_INPI_SIHCP_Mobile.docx",
        BASE_DIR / "01_Formulario_Identificacao_INPI_SIHCP_Mobile.md",
        pdf_info,
        zip_info,
    )

    print("[6/6] Gerando Memorial Descritivo, Inventário e Guia...")
    generate_memorial_descritivo(
        BASE_DIR / "02_Memorial_Descritivo_e_Arquitetura_SIHCP_Mobile.docx",
        BASE_DIR / "02_Memorial_Descritivo_e_Arquitetura_SIHCP_Mobile.md",
        metrics,
        pdf_info,
    )
    generate_inventario_codigo(
        BASE_DIR / "03_Inventario_Codigo_Fonte_e_Metricas.md",
        metrics,
    )
    generate_readme_guide(
        BASE_DIR / "00_LEIA_ME_GUIA_REGISTRO_INPI.md",
        pdf_info,
        zip_info,
    )

    print("\nConcluído com sucesso!")
    print(f"PDF SHA-512: {pdf_info['sha512']}")
    print(f"ZIP SHA-512: {zip_info['sha512']}")


if __name__ == "__main__":
    main()
