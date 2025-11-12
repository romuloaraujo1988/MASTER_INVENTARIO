# 📊 Implementação de Gráficos no Sistema de Inventário

## Visão Geral

Sistema completo de gráficos estatísticos usando **JFreeChart 1.5.5** para visualização de dados do inventário.

---

## 🏗️ Arquitetura

```
┌─────────────────────────────────────────────────────────┐
│                    DashboardFrame                        │
│                   (Apresentação)                         │
└──────────────────────┬──────────────────────────────────┘
                       │ usa
                       ▼
┌─────────────────────────────────────────────────────────┐
│              InventarioChartService                      │
│              (Lógica de Negócio)                         │
└──────────────────────┬──────────────────────────────────┘
                       │ usa
                       ▼
┌─────────────────────────────────────────────────────────┐
│                  ChartFactory                            │
│              (Criação de Gráficos)                       │
└─────────────────────────────────────────────────────────┘
                       │ usa
                       ▼
┌─────────────────────────────────────────────────────────┐
│                  ChartDataDAO                            │
│              (Acesso a Dados)                            │
└─────────────────────────────────────────────────────────┘
```

---

## 📦 Componentes Criados

### 1. ChartFactory
**Localização**: `com.inventario.chart.ChartFactory`

Factory para criação e estilização de gráficos JFreeChart.

**Métodos Principais**:
- `createPieChart()` - Gráfico de pizza
- `createBarChart()` - Gráfico de barras vertical
- `createHorizontalBarChart()` - Gráfico de barras horizontal
- `createLineChart()` - Gráfico de linhas
- `createCategoryDataset()` - Dataset simples
- `createMultiSeriesDataset()` - Dataset com múltiplas séries

**Cores Padrão**:
```java
PRIMARY_COLOR = #3498db (azul)
SUCCESS_COLOR = #2ecc71 (verde)
WARNING_COLOR = #f1c40f (amarelo)
DANGER_COLOR = #e74c3c (vermelho)
INFO_COLOR = #9b59b6 (roxo)
SECONDARY_COLOR = #95a5a6 (cinza)
```

### 2. ChartDataDAO
**Localização**: `com.inventario.dao.ChartDataDAO`

DAO especializado para buscar dados estatísticos.

**Métodos**:
- `contarPorStatus(String status)` - Conta patrimônios por status
- `contarTodosPatrimonios()` - Total de patrimônios
- `contarPatrimoniosPorSetor()` - Quantidade por setor (top 10)
- `contarColetadosPorInventario(Integer id)` - Total coletado
- `buscarEvolucaoDiaria(Integer id)` - Evolução acumulada por dia
- `buscarTop10Descricoes(Integer id)` - Itens mais coletados
- `buscarEstatisticasGerais(Integer id)` - Estatísticas consolidadas

### 3. InventarioChartService
**Localização**: `com.inventario.chart.InventarioChartService`

Serviço que coordena a criação de gráficos específicos do sistema.

**Gráficos Disponíveis**:

#### 📊 Status dos Patrimônios (Pizza)
```java
JFreeChart chart = chartService.createPatrimonioStatusChart();
```
Mostra distribuição por status: Ativos, Inativos, Manutenção, Baixados.

#### 📊 Patrimônios por Setor (Barras)
```java
JFreeChart chart = chartService.createPatrimoniosPorSetorChart();
```
Top 10 setores com mais patrimônios.

#### 📊 Progresso da Coleta (Barras)
```java
JFreeChart chart = chartService.createProgressoColetaChart(idInventario);
```
Compara coletados vs pendentes.

#### 📊 Evolução das Coletas (Linhas)
```java
JFreeChart chart = chartService.createEvolucaoColetasChart(idInventario);
```
Quantidade acumulada por dia (últimos 30 dias).

#### 📊 Top 10 Itens Coletados (Barras Horizontais)
```java
JFreeChart chart = chartService.createTop10DescricoesChart(idInventario);
```
Descrições mais coletadas no inventário.

### 4. DashboardFrame
**Localização**: `com.inventario.view.DashboardFrame`

Frame completo com dashboard de gráficos.

**Uso**:
```java
// Sem inventário específico (apenas gráficos gerais)
DashboardFrame dashboard = new DashboardFrame(usuario);
dashboard.setVisible(true);

// Com inventário ativo (todos os gráficos)
DashboardFrame dashboard = new DashboardFrame(usuario, idInventario);
dashboard.setVisible(true);

// Atualizar inventário dinamicamente
dashboard.setInventarioAtivo(novoIdInventario);

// Recarregar gráficos
dashboard.refreshCharts();
```

---

## 🚀 Como Usar

### Exemplo 1: Adicionar Dashboard ao Menu Principal

```java
// No MainFrame.java
private void criarMenus() {
    JMenuBar menuBar = new JMenuBar();
    
    JMenu menuRelatorios = new JMenu("Relatórios");
    
    JMenuItem itemDashboard = new JMenuItem("📊 Dashboard");
    itemDashboard.addActionListener(e -> abrirDashboard());
    
    menuRelatorios.add(itemDashboard);
    menuBar.add(menuRelatorios);
    
    setJMenuBar(menuBar);
}

private void abrirDashboard() {
    // Buscar inventário ativo (se houver)
    Integer idInventario = buscarInventarioAtivo();
    
    DashboardFrame dashboard = new DashboardFrame(usuarioLogado, idInventario);
    dashboard.setVisible(true);
}
```

### Exemplo 2: Gráfico Individual em Relatório

```java
// Adicionar gráfico a um relatório existente
public class RelatorioFrame extends JFrame {
    
    private void adicionarGraficoProgresso() {
        InventarioChartService chartService = new InventarioChartService();
        JFreeChart chart = chartService.createProgressoColetaChart(idInventario);
        
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(600, 400));
        
        painelGraficos.add(chartPanel);
    }
}
```

### Exemplo 3: Criar Gráfico Customizado

```java
// Usando ChartFactory diretamente
Map<String, Number> dados = new LinkedHashMap<>();
dados.put("Janeiro", 120);
dados.put("Fevereiro", 150);
dados.put("Março", 180);

JFreeChart chart = ChartFactory.createBarChart(
    "Coletas por Mês",
    "Mês",
    "Quantidade",
    ChartFactory.createCategoryDataset(dados, "Coletas")
);

ChartPanel panel = new ChartPanel(chart);
```

### Exemplo 4: Exportar Gráfico como Imagem

```java
import org.jfree.chart.ChartUtils;
import java.io.File;

// Salvar como PNG
JFreeChart chart = chartService.createPatrimonioStatusChart();
File arquivo = new File("grafico_status.png");
ChartUtils.saveChartAsPNG(arquivo, chart, 800, 600);

// Salvar como JPEG
ChartUtils.saveChartAsJPEG(arquivo, chart, 800, 600);
```

---

## 🎨 Personalização

### Alterar Cores do Gráfico

```java
// No ChartFactory.java, modificar:
private static final Color[] CHART_COLORS = {
    new Color(52, 152, 219),   // Azul
    new Color(46, 204, 113),   // Verde
    new Color(241, 196, 15),   // Amarelo
    // Adicionar mais cores...
};
```

### Customizar Estilo de Gráfico

```java
JFreeChart chart = chartService.createBarChart(...);

// Acessar o plot
CategoryPlot plot = chart.getCategoryPlot();

// Customizar grid
plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
plot.setDomainGridlinesVisible(true);

// Customizar renderer
BarRenderer renderer = (BarRenderer) plot.getRenderer();
renderer.setBarPainter(new StandardBarPainter()); // Sem gradiente
renderer.setShadowVisible(false);
```

---

## 📊 Queries SQL dos Gráficos

### Status dos Patrimônios
```sql
SELECT COUNT(*) as total 
FROM patrimonio 
WHERE UPPER(status) = UPPER('ATIVO');
```

### Patrimônios por Setor
```sql
SELECT 
    COALESCE(s.nome, 'Sem Setor') as setor,
    COUNT(p.id) as quantidade
FROM patrimonio p
LEFT JOIN sala sa ON p.id_sala = sa.id
LEFT JOIN setor s ON sa.id_setor = s.id
GROUP BY s.nome
ORDER BY quantidade DESC
LIMIT 10;
```

### Evolução Diária
```sql
SELECT 
    TO_CHAR(data_coleta, 'DD/MM') as data,
    COUNT(*) as quantidade
FROM coleta
WHERE id_inventario = ?
GROUP BY DATE(data_coleta), TO_CHAR(data_coleta, 'DD/MM')
ORDER BY DATE(data_coleta)
LIMIT 30;
```

### Top 10 Descrições
```sql
SELECT 
    p.descricao,
    COUNT(c.id) as quantidade
FROM coleta c
INNER JOIN patrimonio p ON c.id_patrimonio = p.id
WHERE c.id_inventario = ?
GROUP BY p.descricao
ORDER BY quantidade DESC
LIMIT 10;
```

---

## ✅ Checklist de Implementação

- [x] Dependência JFreeChart no pom.xml
- [x] ChartFactory criado
- [x] ChartDataDAO criado
- [x] InventarioChartService criado
- [x] DashboardFrame criado
- [ ] Adicionar menu no MainFrame
- [ ] Testar com dados reais
- [ ] Adicionar exportação de gráficos
- [ ] Documentar para usuários finais

---

## 🐛 Troubleshooting

### Gráfico não aparece
- Verificar se JFreeChart está no classpath
- Verificar conexão com banco de dados
- Verificar logs de erro no console

### Dados não aparecem
- Verificar se há dados no banco
- Verificar queries SQL no ChartDataDAO
- Usar dados de exemplo (fallback já implementado)

### Performance lenta
- Adicionar índices nas tabelas (status, id_inventario, data_coleta)
- Limitar quantidade de dados (LIMIT nas queries)
- Usar cache para gráficos estáticos

---

## 📚 Referências

- [JFreeChart Documentation](https://www.jfree.org/jfreechart/)
- [JFreeChart API](https://www.jfree.org/jfreechart/api/javadoc/index.html)
- [Chart Examples](https://www.jfree.org/jfreechart/samples.html)

---

**Versão**: 1.0.0  
**Data**: 11/11/2025  
**Autor**: Sistema de Inventário IFMT
