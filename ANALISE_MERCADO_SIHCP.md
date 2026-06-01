# Análise de Mercado — SIHCP vs. Soluções de Gestão Patrimonial

**Data:** 29/05/2026
**Objetivo:** Posicionar o SIHCP frente às principais soluções de gestão de
ativos/patrimônio com app móvel disponíveis no mercado.

> Conteúdo reformulado a partir de fontes públicas para conformidade com
> licenciamento. Fontes citadas inline.

---

## 1. Panorama do mercado

O mercado de *fixed asset management* / *mobile asset tracking* é maduro e
competitivo. Os principais players globais em 2026 são:

- **Asset Panda** — líder recorrente; forte em customização sem código e
  captura offline ([gitnux](https://gitnux.org/best/mobile-asset-management-software/))
- **EZOfficeInventory (EZO)** — gestão de equipamentos, RFID, reservas, ticketing
- **Sortly** — simplicidade, QR Code, PMEs
- **Snipe-IT** — open-source, foco em ativos de TI
- **AssetTiger** — gratuito até certo volume, QR Code
- **GoCodes** — QR/GPS/Bluetooth/NFC, rastreamento físico
- **UpKeep / Fiix / Limble / MaintainX** — especializados em manutenção (CMMS)
- **Sage Fixed Assets / NetSuite / QuickBooks** — foco contábil/depreciação

Categorias e capacidades comuns no mercado: scan QR/código de barras, captura
offline, RBAC, trilha de auditoria, agendamento de manutenção, fotos e
checklists como evidência ([worldmetrics](https://worldmetrics.org/best/mobile-asset-management-software),
[gitnux](https://gitnux.org/best/mobile-asset-management-software/)).

---

## 2. Tabela comparativa

Legenda: ✅ nativo/forte · 🟡 parcial/limitado · ❌ ausente · 💲 pago recorrente

| Capacidade | SIHCP | Asset Panda | EZOfficeInventory | Snipe-IT | Sortly |
|---|---|---|---|---|---|
| Scan QR/código de barras (mobile) | ✅ (ZXing) | ✅ | ✅ | ✅ | ✅ |
| Coleta offline + sync | ✅ (Room + batch sync) | ✅ | 🟡 | 🟡 | 🟡 |
| Controle de acesso por perfil (RBAC) | ✅ (4 perfis) | ✅ | ✅ | ✅ | 🟡 |
| Registro fotográfico | ✅ | ✅ | ✅ | 🟡 | ✅ |
| Trilha de auditoria | ✅ (AuditService) | ✅ | ✅ | ✅ | 🟡 |
| **Monitor de coletas AO VIVO (WebSocket)** | ✅ **diferencial** | 🟡 | ❌ | ❌ | ❌ |
| Dashboard / relatórios | ✅ (JFreeChart, PDF, Excel) | ✅ | ✅ | 🟡 | ✅ |
| Customização sem código | 🟡 | ✅ **forte** | ✅ | 🟡 | 🟡 |
| Manutenção preventiva / ordens de serviço | ❌ | ✅ | ✅ | ❌ | ❌ |
| Integrações prontas (contábil, SSO, helpdesk) | 🟡 (SUAP) | ✅ | ✅ | ✅ | 🟡 |
| Depreciação contábil avançada | 🟡 | ✅ | 🟡 | ❌ | ❌ |
| RFID / GPS / Bluetooth | ❌ | 🟡 | ✅ (RFID) | ❌ | ❌ |
| **Adequação ao patrimônio público BR** | ✅ **forte** | ❌ | ❌ | ❌ | ❌ |
| **Integração SUAP (IFMT)** | ✅ | ❌ | ❌ | ❌ | ❌ |
| Coleta de item sem etiqueta + decisão auditada | ✅ | 🟡 | 🟡 | ❌ | 🟡 |
| On-premise / soberania de dados | ✅ | 🟡 (SaaS) | 🟡 | ✅ (open-source) | ❌ |
| Custo de licença recorrente | ✅ sem custo | 💲 | 💲 | ✅ grátis (self-host) | 💲 |

*Avaliação dos concorrentes baseada em material público das fontes citadas;
avaliação do SIHCP baseada no código-fonte do projeto.*

---

## 3. Onde o mercado supera o SIHCP

É justo reconhecer as vantagens dos produtos consolidados:

1. **Customização sem código** — Asset Panda permite moldar campos e fluxos
   sem desenvolvimento ([assetpanda](https://www.assetpanda.com/resource-center/compare/asset-panda-vs-microsoft-access-full-comparison-guide/)).
   No SIHCP, mudanças exigem alterar o código.
2. **Manutenção preventiva / CMMS** — UpKeep, Fiix, Limble, MaintainX são
   especializados; o SIHCP não cobre ordens de serviço.
3. **Integrações de ecossistema** — SSO (Okta/Ping), helpdesk (Zendesk/Jira),
   contábil, storage ([ezo.io](https://ezo.io/ezofficeinventory/blog/ezo-comparison-blog/)).
4. **Depreciação contábil avançada** — Sage/NetSuite/QuickBooks.
5. **Rastreamento físico** — RFID/GPS/Bluetooth/NFC (GoCodes, EZO).
6. **Maturidade** — anos de mercado, suporte dedicado, apps muito polidos.

---

## 4. Onde o SIHCP se diferencia (e ganha no nicho)

1. **Verticalização para patrimônio público brasileiro** — conformidade com
   regras do setor público federal, terminologia e fluxos próprios
   (inventário com participantes, coletores, situação "não encontrado",
   estados de conservação padronizados). Nenhum produto global modela isso
   de fábrica.
2. **Integração com SUAP** — específica do IFMT, impossível em produto de
   prateleira sem desenvolvimento sob medida.
3. **Monitor de coletas ao vivo (WebSocket)** — supervisor acompanha coletas
   chegando em tempo real, sem refresh. Recurso incomum mesmo entre os
   grandes, que trabalham com check-in/check-out e relatórios pós-fato.
4. **Arquitetura híbrida desktop + API mobile + app nativo** — a maioria dos
   concorrentes é SaaS puro. Modelo com app desktop administrativo + servidor
   próprio oferece soberania de dados (on-premise, sem dado em nuvem terceira).
5. **Custo** — sem mensalidade por usuário. Em instituições com dezenas/centenas
   de operadores, o custo recorrente dos SaaS escala rápido.
6. **Fluxos desenhados para a realidade de campo** — coleta de item sem
   etiqueta, decisão auditada sobre duplicatas, sync em lote com fallback,
   retry automático — adaptados ao processo real de inventário público.

---

## 5. Concorrentes brasileiros

A pesquisa por produtos comerciais brasileiros específicos de patrimônio
público (ex.: integrados a SUAP/SIAFI) **não retornou players claros e
dominantes** nas fontes públicas consultadas — o resultado foi dominado por
páginas governamentais genéricas e projetos acadêmicos/open-source isolados
(ex.: repositórios no GitHub de sistemas de inventário escolar).

Isso sugere duas leituras:
- O nicho de **gestão patrimonial pública brasileira com app de coleta** é
  pouco coberto por produtos comerciais consolidados — espaço onde o SIHCP
  tem vantagem competitiva real.
- Existem ERPs e módulos patrimoniais brasileiros (em suítes de gestão
  pública), mas raramente com app de coleta offline + monitor ao vivo no
  nível do SIHCP.

> Observação: esta seção reflete o que foi encontrado em busca pública e não
> esgota o mercado nacional. Uma análise definitiva exigiria levantamento
> direto de fornecedores de ERP público (pregões/licitações).

---

## 6. Veredito

- **Em amplitude de features genéricas:** Asset Panda e EZOfficeInventory são
  mais completos — mais integrações, manutenção preventiva, RFID/GPS,
  customização sem código, maturidade.
- **Em adequação ao problema específico** (inventário patrimonial de
  instituição pública brasileira, offline-first, SUAP, monitor ao vivo, sem
  licença recorrente, dados on-premise): **o SIHCP é mais completo que
  qualquer um deles para esse caso de uso.**

Não é "melhor" ou "pior" em abstrato. O SIHCP é um sistema **verticalizado**
que, no nicho dele, entrega o que os generalistas globais não entregam de
fábrica — ao custo de não ter a maturidade de integrações e a cobertura de
manutenção/depreciação que eles possuem.

---

## 7. Recomendações de evolução (se o objetivo for fechar gaps)

Priorizadas por relação valor/esforço para o contexto do SIHCP:

1. **Manutenção/ocorrências de bens** — registrar avarias/ocorrências por
   patrimônio (passo em direção a CMMS leve).
2. **Customização de campos** — campos personalizáveis por inventário sem
   recompilar.
3. **Depreciação** — cálculo automático conforme regras do setor público.
4. **Exportações/integrações** — APIs para SIAFI/sistemas contábeis.
5. **RFID** (longo prazo) — leitura em massa para inventários grandes, já
   citada como ganho de eficiência ([checkpointsystems](https://checkpointsystems.com/pt/blog/tecnologia-rfid-nas-bibliotecas/)).

---

**Fontes principais:**
- [gitnux — Mobile Asset Management Software 2026](https://gitnux.org/best/mobile-asset-management-software/)
- [worldmetrics — Mobile Asset Management Software 2026](https://worldmetrics.org/best/mobile-asset-management-software)
- [gocodes — Best Fixed Asset Management Softwares](https://gocodes.com/fixed-asset-management/best-fixed-asset-management-softwares/)
- [assetpanda — comparativos](https://www.assetpanda.com/resource-center/blog/how-asset-panda-compares-to-ezofficeinventory)
- [ezo.io — EZO vs Asset Panda vs Cheqroom](https://ezo.io/ezofficeinventory/blog/ezo-comparison-blog/)
- [worldmetrics — Hardware Tracking Software](https://worldmetrics.org/best/hardware-tracking-software/)

*Nota: conteúdo das fontes foi parafraseado e resumido para conformidade com
restrições de licenciamento.*
