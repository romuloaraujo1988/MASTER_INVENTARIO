# Categorias de Bens de Patrimônio

Este documento apresenta as categorias de patrimônio utilizadas no Sistema de Inventário, baseadas na análise do código fonte e dados existentes.

## Categorias Principais

### 1. INFORMÁTICA
**Descrição:** Equipamentos de tecnologia da informação e comunicação

**Exemplos de itens:**
- Computadores (desktop, all-in-one)
- Notebooks e laptops
- Monitores e displays
- Impressoras (jato de tinta, laser, multifuncionais)
- Scanners
- Servidores
- Switches e roteadores
- Tablets
- CPUs e componentes
- Teclados e mouses
- Webcams
- Projetores e datashows
- HDs, SSDs e dispositivos de armazenamento
- Memórias e placas
- Fontes de alimentação

**Quantidade no sistema:** 2.034 itens

---

### 2. MOBILIÁRIO
**Descrição:** Móveis e equipamentos de escritório e ambientes

**Exemplos de itens:**
- Mesas e escrivaninhas
- Cadeiras e poltronas
- Armários e guarda-roupas
- Estantes e prateleiras
- Arquivos e gaveteiros
- Sofás e estofados
- Bancadas de trabalho
- Balcões e recepções
- Racks e suportes
- Móveis em geral

**Quantidade no sistema:** 2.516 itens

---

### 3. ELETRODOMÉSTICOS
**Descrição:** Aparelhos elétricos para uso doméstico e comercial

**Exemplos de itens:**
- Geladeiras e refrigeradores
- Microondas
- Ar condicionado e climatizadores
- Ventiladores
- Bebedouros e purificadores
- Cafeteiras
- Fogões e fornos
- Freezers
- Aquecedores
- Liquidificadores e batedeiras

**Quantidade no sistema:** 112 itens

---

### 4. VEÍCULOS
**Descrição:** Meios de transporte e locomoção

**Exemplos de itens:**
- Carros e automóveis
- Caminhões e caminhonetes
- Motocicletas e motos
- Bicicletas
- Ônibus e vans
- Tratores
- Pickups
- Veículos utilitários

**Quantidade no sistema:** 132 itens

---

### 5. EQUIPAMENTOS DE LABORATÓRIO
**Descrição:** Instrumentos científicos e equipamentos especializados

**Exemplos de itens:**
- Microscópios
- Balanças de precisão
- Centrífugas
- Estufas e fornos laboratoriais
- Autoclaves
- Pipetas e micropipetas
- Vidrarias (béqueres, provetas, buretas, erlenmeyers)
- Equipamentos científicos
- Instrumentos de medição
- Analisadores
- Medidores especializados

---

### 6. EQUIPAMENTOS DE ÁUDIO E VÍDEO
**Descrição:** Dispositivos para reprodução e gravação audiovisual

**Exemplos de itens:**
- Televisões e TVs
- Sistemas de som
- Caixas de som e amplificadores
- Microfones
- Câmeras fotográficas e filmadoras
- Players de DVD e Blu-ray
- Rádios
- Equalizadores
- Equipamentos de sonorização

---

### 7. FERRAMENTAS
**Descrição:** Instrumentos para manutenção e trabalhos manuais

**Exemplos de itens:**
- Furadeiras e parafusadeiras
- Martelos e marretas
- Chaves (fenda, phillips, inglesa)
- Alicates e alicates especiais
- Serras (manual, elétrica)
- Brocas e bits
- Equipamentos de manutenção
- Soldadores
- Morsas e grampos
- Ferramentas manuais em geral

---

### 8. EQUIPAMENTOS DE SEGURANÇA
**Descrição:** Dispositivos para proteção e monitoramento

**Exemplos de itens:**
- Extintores de incêndio
- Câmeras de segurança
- Sistemas de alarme
- Sensores diversos
- Detectores (fumaça, movimento)
- Equipamentos de monitoramento
- Sistemas de vigilância
- EPIs (Equipamentos de Proteção Individual)

---

### 9. MATERIAL BIBLIOGRÁFICO
**Descrição:** Acervo de livros, publicações e material didático

**Exemplos de itens:**
- Livros técnicos e acadêmicos
- Periódicos e revistas
- Manuais e apostilas
- Material didático
- Enciclopédias
- Dicionários
- Obras de referência
- Publicações científicas

**Quantidade no sistema:** 3.975 itens

---

### 10. EQUIPAMENTOS DIVERSOS
**Descrição:** Equipamentos que não se enquadram nas categorias específicas

**Exemplos de itens:**
- Equipamentos especializados
- Instrumentos específicos de área
- Aparelhos diversos
- Equipamentos de uso geral

**Quantidade no sistema:** 61 itens

---

### 11. OUTROS
**Descrição:** Categoria padrão para itens não classificados nas demais categorias

**Características:**
- Bens que não se enquadram em categorias específicas
- Itens únicos ou raros
- Patrimônios aguardando classificação
- Bens diversos sem categoria definida

**Quantidade no sistema:** 1.769 itens

---

## Resumo Estatístico

| Categoria | Quantidade | Percentual |
|-----------|------------|------------|
| Material Bibliográfico | 3.975 | 37,8% |
| Mobiliário | 2.516 | 23,9% |
| Equipamentos de Informática | 2.034 | 19,3% |
| Outros | 1.769 | 16,8% |
| Veículos | 132 | 1,3% |
| Eletrodomésticos | 112 | 1,1% |
| Equipamentos Diversos | 61 | 0,6% |
| **TOTAL** | **10.599** | **100%** |

---

## Observações Técnicas

### Sistema de Categorização
- O sistema utiliza **Machine Learning** para categorização automática
- Implementa **padrões regex** para identificação por palavras-chave
- Possui **fallback** para categoria "OUTROS" quando não identificada
- Permite **recategorização** manual quando necessário

### Critérios de Classificação
- **Descrição do item:** Principal critério para categorização
- **Palavras-chave:** Sistema identifica termos específicos
- **Contexto de uso:** Considera a finalidade do equipamento
- **Características técnicas:** Analisa especificações quando disponíveis

### Manutenção das Categorias
- **Treinamento contínuo:** IA aprende com novos dados
- **Validação manual:** Revisão periódica das categorizações
- **Atualização automática:** Sistema atualiza categorias conforme padrões
- **Relatórios de categorização:** Acompanhamento da distribuição

---

*Documento gerado automaticamente baseado na análise do Sistema de Inventário*  
*Última atualização: Dezembro 2024*