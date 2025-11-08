# Plano de Migração: Swing → JavaFX

## 🎯 Objetivo
Migrar a interface desktop de Swing para JavaFX, modernizando a experiência do usuário e aproveitando recursos avançados de UI.

## 📊 Análise da Situação Atual

### Interface Swing Atual
```
✅ Funcional e estável
✅ 25+ frames implementados
✅ Lógica de negócio separada (após refatoração)
❌ Aparência datada
❌ Sem animações
❌ Difícil customização
❌ Não responsivo
```

### Componentes Swing Identificados
- **Frames**: 25+ (JFrame)
- **Dialogs**: 15+ (JDialog)
- **Tables**: JTable com DefaultTableModel
- **Forms**: JTextField, JComboBox, JButton
- **Layouts**: BorderLayout, GridBagLayout, BoxLayout
- **Menus**: JMenuBar, JMenu, JMenuItem

## 🚀 Estratégia de Migração

### Abordagem: **Migração Incremental Híbrida**

**Por quê?**
- ✅ Minimiza riscos
- ✅ Permite testes contínuos
- ✅ Mantém sistema funcionando
- ✅ Facilita rollback se necessário

### Fases da Migração

```
Fase 1: Preparação (2 semanas)
  ↓
Fase 2: Infraestrutura JavaFX (2 semanas)
  ↓
Fase 3: Migração de Telas Simples (3 semanas)
  ↓
Fase 4: Migração de Telas Complexas (4 semanas)
  ↓
Fase 5: Polimento e Otimização (2 semanas)
  ↓
Fase 6: Testes e Validação (1 semana)
```

**Total**: 14 semanas (~3.5 meses)

## 📋 Fase 1: Preparação (2 semanas)

### 1.1 Configurar Dependências Maven

```xml
<!-- pom.xml -->
<properties>
    <javafx.version>21.0.1</javafx.version>
</properties>

<dependencies>
    <!-- JavaFX Core -->
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-controls</artifactId>
        <version>${javafx.version}</version>
    </dependency>
    
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-fxml</artifactId>
        <version>${javafx.version}</version>
    </dependency>
    
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-graphics</artifactId>
        <version>${javafx.version}</version>
    </dependency>
    
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-web</artifactId>
        <version>${javafx.version}</version>
    </dependency>
    
    <!-- Material Design para JavaFX -->
    <dependency>
        <groupId>com.jfoenix</groupId>
        <artifactId>jfoenix</artifactId>
        <version>9.0.10</version>
    </dependency>
    
    <!-- ControlsFX (componentes extras) -->
    <dependency>
        <groupId>org.controlsfx</groupId>
        <artifactId>controlsfx</artifactId>
        <version>11.1.2</version>
    </dependency>
    
    <!-- FontAwesomeFX (ícones) -->
    <dependency>
        <groupId>de.jensd</groupId>
        <artifactId>fontawesomefx-fontawesome</artifactId>
        <version>4.7.0-9.1.2</version>
    </dependency>
</dependencies>

<build>
    <plugins>
        <!-- JavaFX Maven Plugin -->
        <plugin>
            <groupId>org.openjfx</groupId>
            <artifactId>javafx-maven-plugin</artifactId>
            <version>0.0.8</version>
            <configuration>
                <mainClass>com.inventario.MainApplicationFX</mainClass>
            </configuration>
        </plugin>
    </plugins>
</build>
```

### 1.2 Criar Estrutura de Pacotes JavaFX

```
src/main/java/com/inventario/
├── fx/                              # Novo pacote JavaFX
│   ├── MainApplicationFX.java       # Aplicação principal JavaFX
│   ├── controller/                  # Controllers FXML
│   │   ├── MainController.java
│   │   ├── PatrimonioController.java
│   │   ├── InventarioController.java
│   │   └── ...
│   ├── view/                        # Views customizadas
│   │   ├── components/              # Componentes reutilizáveis
│   │   └── dialogs/                 # Dialogs customizados
│   ├── util/                        # Utilitários JavaFX
│   │   ├── FXMLLoader.java
│   │   ├── AlertHelper.java
│   │   └── TableHelper.java
│   └── theme/                       # Temas e estilos
│       ├── ThemeManager.java
│       └── styles/
│           ├── light-theme.css
│           └── dark-theme.css
│
├── view/                            # Swing (manter temporariamente)
└── service/                         # Serviços (compartilhados)

src/main/resources/
├── fxml/                            # Arquivos FXML
│   ├── main.fxml
│   ├── patrimonio.fxml
│   ├── inventario.fxml
│   └── ...
├── css/                             # Estilos CSS
│   ├── application.css
│   ├── components.css
│   └── themes/
├── images/                          # Imagens e ícones
└── fonts/                           # Fontes customizadas
```

### 1.3 Criar Aplicação Principal JavaFX

```java
package com.inventario.fx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.inventario.fx.theme.ThemeManager;

public class MainApplicationFX extends Application {
    
    private static Stage primaryStage;
    private static ThemeManager themeManager;
    
    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        themeManager = new ThemeManager();
        
        // Carregar tela principal
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/fxml/main.fxml")
        );
        Parent root = loader.load();
        
        // Criar cena
        Scene scene = new Scene(root, 1280, 720);
        
        // Aplicar tema
        themeManager.applyTheme(scene, ThemeManager.Theme.LIGHT);
        
        // Configurar stage
        stage.setTitle("Sistema de Inventário - IFMT");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }
    
    public static Stage getPrimaryStage() {
        return primaryStage;
    }
    
    public static ThemeManager getThemeManager() {
        return themeManager;
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
```

### 1.4 Criar Sistema de Temas

```java
package com.inventario.fx.theme;

import javafx.scene.Scene;

public class ThemeManager {
    
    public enum Theme {
        LIGHT("/css/themes/light-theme.css"),
        DARK("/css/themes/dark-theme.css");
        
        private final String cssPath;
        
        Theme(String cssPath) {
            this.cssPath = cssPath;
        }
        
        public String getCssPath() {
            return cssPath;
        }
    }
    
    private Theme currentTheme = Theme.LIGHT;
    
    public void applyTheme(Scene scene, Theme theme) {
        // Remover temas anteriores
        scene.getStylesheets().clear();
        
        // Adicionar CSS base
        scene.getStylesheets().add(
            getClass().getResource("/css/application.css").toExternalForm()
        );
        
        // Adicionar tema
        scene.getStylesheets().add(
            getClass().getResource(theme.getCssPath()).toExternalForm()
        );
        
        this.currentTheme = theme;
    }
    
    public void toggleTheme(Scene scene) {
        Theme newTheme = (currentTheme == Theme.LIGHT) ? Theme.DARK : Theme.LIGHT;
        applyTheme(scene, newTheme);
    }
    
    public Theme getCurrentTheme() {
        return currentTheme;
    }
}
```

## 📋 Fase 2: Infraestrutura JavaFX (2 semanas)

### 2.1 Criar Componentes Base

#### BaseController
```java
package com.inventario.fx.controller;

import javafx.fxml.Initializable;
import com.inventario.service.ServiceFactory;

public abstract class BaseController implements Initializable {
    
    protected ServiceFactory serviceFactory;
    
    public BaseController() {
        this.serviceFactory = ServiceFactory.getInstance();
    }
    
    protected void showError(String title, String message) {
        AlertHelper.showError(title, message);
    }
    
    protected void showSuccess(String title, String message) {
        AlertHelper.showSuccess(title, message);
    }
    
    protected void showWarning(String title, String message) {
        AlertHelper.showWarning(title, message);
    }
    
    protected boolean showConfirmation(String title, String message) {
        return AlertHelper.showConfirmation(title, message);
    }
}
```

#### AlertHelper
```java
package com.inventario.fx.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import java.util.Optional;

public class AlertHelper {
    
    public static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public static void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public static void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public static boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
}
```

#### TableHelper
```java
package com.inventario.fx.util;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class TableHelper {
    
    public static <T> void setupColumn(
        TableColumn<T, ?> column, 
        String property, 
        double width
    ) {
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        column.setPrefWidth(width);
    }
    
    public static <T> void makeTableEditable(TableView<T> table) {
        table.setEditable(true);
    }
    
    public static <T> void autoResizeColumns(TableView<T> table) {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }
}
```

### 2.2 Criar CSS Base

```css
/* application.css */

/* Cores principais */
.root {
    -fx-primary: #007bff;
    -fx-secondary: #6c757d;
    -fx-success: #28a745;
    -fx-danger: #dc3545;
    -fx-warning: #ffc107;
    -fx-info: #17a2b8;
    
    -fx-font-family: "Segoe UI", Arial, sans-serif;
    -fx-font-size: 14px;
}

/* Botões */
.button {
    -fx-background-radius: 4px;
    -fx-padding: 8px 16px;
    -fx-cursor: hand;
}

.button:hover {
    -fx-opacity: 0.9;
}

.button-primary {
    -fx-background-color: -fx-primary;
    -fx-text-fill: white;
}

.button-success {
    -fx-background-color: -fx-success;
    -fx-text-fill: white;
}

.button-danger {
    -fx-background-color: -fx-danger;
    -fx-text-fill: white;
}

/* Tabelas */
.table-view {
    -fx-background-color: white;
}

.table-view .column-header {
    -fx-background-color: #f8f9fa;
    -fx-font-weight: bold;
}

.table-row-cell:hover {
    -fx-background-color: #e9ecef;
}

.table-row-cell:selected {
    -fx-background-color: -fx-primary;
    -fx-text-fill: white;
}

/* Campos de texto */
.text-field {
    -fx-background-radius: 4px;
    -fx-border-color: #ced4da;
    -fx-border-radius: 4px;
    -fx-padding: 8px;
}

.text-field:focused {
    -fx-border-color: -fx-primary;
    -fx-border-width: 2px;
}

/* Cards */
.card {
    -fx-background-color: white;
    -fx-background-radius: 8px;
    -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);
    -fx-padding: 20px;
}
```

## 📋 Fase 3: Migração de Telas Simples (3 semanas)

### Ordem de Migração (do mais simples ao mais complexo)

1. **Login** (1 dia)
2. **Dashboard** (2 dias)
3. **SetorFormDialog** (1 dia)
4. **CampusFrame** (1 dia)
5. **ResponsavelFormDialog** (2 dias)
6. **SalaFormDialog** (2 dias)
7. **UsuarioFormDialog** (2 dias)
8. **AlterarSenhaDialog** (1 dia)

### Exemplo: Migração de SetorFormDialog

#### Antes (Swing)
```java
public class SetorFormDialog extends JDialog {
    private JTextField campoNome;
    private JButton btnSalvar;
    
    private void initComponents() {
        setLayout(new BorderLayout());
        // ... código Swing
    }
}
```

#### Depois (JavaFX)

**setor-form.fxml**:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<?import javafx.scene.control.*?>
<?import javafx.scene.layout.*?>
<?import javafx.geometry.Insets?>

<VBox xmlns:fx="http://javafx.com/fxml"
      fx:controller="com.inventario.fx.controller.SetorFormController"
      spacing="15" styleClass="card">
    
    <padding>
        <Insets top="20" right="20" bottom="20" left="20"/>
    </padding>
    
    <Label text="Cadastro de Setor" styleClass="title"/>
    
    <VBox spacing="10">
        <Label text="Nome:"/>
        <TextField fx:id="campoNome" promptText="Digite o nome do setor"/>
    </VBox>
    
    <VBox spacing="10">
        <Label text="Descrição:"/>
        <TextArea fx:id="campoDescricao" promptText="Digite a descrição" 
                  prefRowCount="3"/>
    </VBox>
    
    <VBox spacing="10">
        <Label text="Responsável:"/>
        <TextField fx:id="campoResponsavel" promptText="Nome do responsável"/>
    </VBox>
    
    <HBox spacing="10" alignment="CENTER_RIGHT">
        <Button text="Cancelar" onAction="#handleCancelar" 
                styleClass="button-secondary"/>
        <Button text="Salvar" onAction="#handleSalvar" 
                styleClass="button-primary"/>
    </HBox>
</VBox>
```

**SetorFormController.java**:
```java
package com.inventario.fx.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import com.inventario.model.Setor;
import com.inventario.service.SetorService;
import com.inventario.service.BusinessException;
import java.net.URL;
import java.util.ResourceBundle;

public class SetorFormController extends BaseController {
    
    @FXML private TextField campoNome;
    @FXML private TextArea campoDescricao;
    @FXML private TextField campoResponsavel;
    
    private Setor setor;
    private SetorService setorService;
    private boolean confirmado = false;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.setorService = serviceFactory.getSetorService();
    }
    
    public void setSetor(Setor setor) {
        this.setor = setor;
        if (setor != null) {
            preencherCampos();
        }
    }
    
    private void preencherCampos() {
        campoNome.setText(setor.getNome());
        campoDescricao.setText(setor.getDescricao());
        campoResponsavel.setText(setor.getResponsavelSetor());
    }
    
    @FXML
    private void handleSalvar() {
        try {
            // Validar
            if (campoNome.getText().trim().isEmpty()) {
                showWarning("Validação", "Nome é obrigatório");
                return;
            }
            
            // Criar/atualizar setor
            if (setor == null) {
                setor = new Setor();
            }
            
            setor.setNome(campoNome.getText().trim());
            setor.setDescricao(campoDescricao.getText().trim());
            setor.setResponsavelSetor(campoResponsavel.getText().trim());
            
            // Salvar
            setorService.salvar(setor);
            
            showSuccess("Sucesso", "Setor salvo com sucesso!");
            confirmado = true;
            fecharDialog();
            
        } catch (BusinessException e) {
            showWarning("Validação", e.getMessage());
        } catch (Exception e) {
            showError("Erro", "Erro ao salvar setor: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleCancelar() {
        fecharDialog();
    }
    
    private void fecharDialog() {
        Stage stage = (Stage) campoNome.getScene().getWindow();
        stage.close();
    }
    
    public boolean isConfirmado() {
        return confirmado;
    }
}
```

## 📋 Fase 4: Migração de Telas Complexas (4 semanas)

### Telas Complexas

1. **PatrimonioFrame** (3 dias)
2. **PatrimonioFormDialog** (3 dias)
3. **InventarioFrame** (3 dias)
4. **InventarioFormDialog** (4 dias)
5. **ColetaFrame_v2** (5 dias) - MAIS COMPLEXA
6. **RelatorioFrame** (4 dias)
7. **DashboardColetaFrame** (3 dias)

### Desafios Específicos

#### 1. Tabelas Complexas (JTable → TableView)
```java
// Swing
DefaultTableModel model = new DefaultTableModel();
JTable table = new JTable(model);

// JavaFX
TableView<Patrimonio> table = new TableView<>();
TableColumn<Patrimonio, String> colNumero = new TableColumn<>("Número");
colNumero.setCellValueFactory(new PropertyValueFactory<>("numero"));
table.getColumns().add(colNumero);

ObservableList<Patrimonio> data = FXCollections.observableArrayList();
table.setItems(data);
```

#### 2. Gráficos (JFreeChart → JavaFX Charts)
```java
// Swing
JFreeChart chart = ChartFactory.createPieChart(...);
ChartPanel panel = new ChartPanel(chart);

// JavaFX
PieChart chart = new PieChart();
PieChart.Data slice = new PieChart.Data("Label", value);
chart.getData().add(slice);
```

#### 3. Menus (JMenuBar → MenuBar)
```java
// Swing
JMenuBar menuBar = new JMenuBar();
JMenu menu = new JMenu("Arquivo");
JMenuItem item = new JMenuItem("Sair");

// JavaFX
MenuBar menuBar = new MenuBar();
Menu menu = new Menu("Arquivo");
MenuItem item = new MenuItem("Sair");
menu.getItems().add(item);
menuBar.getMenus().add(menu);
```

## 📋 Fase 5: Polimento e Otimização (2 semanas)

### 5.1 Animações e Transições

```java
// Fade in
FadeTransition fade = new FadeTransition(Duration.millis(300), node);
fade.setFromValue(0.0);
fade.setToValue(1.0);
fade.play();

// Slide in
TranslateTransition slide = new TranslateTransition(Duration.millis(300), node);
slide.setFromX(-200);
slide.setToX(0);
slide.play();
```

### 5.2 Tema Escuro

```css
/* dark-theme.css */
.root {
    -fx-base: #2b2b2b;
    -fx-background: #1e1e1e;
    -fx-control-inner-background: #3c3c3c;
    -fx-text-fill: #e0e0e0;
}

.table-view {
    -fx-background-color: #2b2b2b;
}

.table-row-cell:hover {
    -fx-background-color: #3c3c3c;
}
```

### 5.3 Ícones Modernos

```java
// FontAwesome
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;

FontAwesomeIconView icon = new FontAwesomeIconView(FontAwesomeIcon.SAVE);
icon.setSize("20");
button.setGraphic(icon);
```

## 📋 Fase 6: Testes e Validação (1 semana)

### 6.1 Testes de UI (TestFX)

```xml
<dependency>
    <groupId>org.testfx</groupId>
    <artifactId>testfx-junit5</artifactId>
    <version>4.0.17</version>
    <scope>test</scope>
</dependency>
```

```java
public class SetorFormTest extends ApplicationTest {
    
    @Override
    public void start(Stage stage) throws Exception {
        // Carregar tela
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/fxml/setor-form.fxml")
        );
        Parent root = loader.load();
        stage.setScene(new Scene(root));
        stage.show();
    }
    
    @Test
    public void testSalvarSetor() {
        // Preencher campos
        clickOn("#campoNome").write("Setor Teste");
        clickOn("#campoDescricao").write("Descrição teste");
        
        // Clicar em salvar
        clickOn("Salvar");
        
        // Verificar sucesso
        verifyThat(".alert", NodeMatchers.isVisible());
    }
}
```

## 📊 Comparação: Swing vs JavaFX

| Aspecto | Swing | JavaFX | Vantagem |
|---------|-------|--------|----------|
| **Aparência** | Datada | Moderna | JavaFX |
| **CSS** | Não | Sim | JavaFX |
| **Animações** | Limitadas | Nativas | JavaFX |
| **Gráficos** | JFreeChart | Charts nativos | JavaFX |
| **FXML** | Não | Sim | JavaFX |
| **Temas** | Difícil | Fácil | JavaFX |
| **Performance** | Boa | Melhor | JavaFX |
| **Responsivo** | Não | Sim | JavaFX |
| **Maturidade** | Alta | Média | Swing |
| **Comunidade** | Menor | Maior | JavaFX |

## 💰 Estimativa de Esforço

| Fase | Duração | Complexidade |
|------|---------|--------------|
| 1. Preparação | 2 semanas | Média |
| 2. Infraestrutura | 2 semanas | Alta |
| 3. Telas Simples | 3 semanas | Média |
| 4. Telas Complexas | 4 semanas | Alta |
| 5. Polimento | 2 semanas | Média |
| 6. Testes | 1 semana | Média |
| **TOTAL** | **14 semanas** | **~3.5 meses** |

## 🎯 Benefícios Esperados

### Interface
- ✅ Aparência moderna e profissional
- ✅ Animações suaves
- ✅ Tema claro/escuro
- ✅ Responsividade

### Desenvolvimento
- ✅ FXML para separação UI/lógica
- ✅ CSS para estilização
- ✅ Componentes reutilizáveis
- ✅ Melhor testabilidade

### Usuário
- ✅ Experiência melhorada (+200%)
- ✅ Interface intuitiva
- ✅ Feedback visual
- ✅ Acessibilidade

## 🚨 Riscos e Mitigações

### Risco 1: Curva de aprendizado JavaFX
**Mitigação**: 
- Treinamento da equipe
- Documentação detalhada
- Migração incremental

### Risco 2: Bugs em produção
**Mitigação**:
- Testes extensivos
- Período de convivência Swing/JavaFX
- Rollback planejado

### Risco 3: Performance
**Mitigação**:
- Profiling contínuo
- Otimizações específicas
- Lazy loading

## 📝 Checklist de Migração

### Por Tela
- [ ] Criar arquivo FXML
- [ ] Criar Controller
- [ ] Migrar lógica de negócio
- [ ] Aplicar estilos CSS
- [ ] Adicionar animações
- [ ] Testar funcionalidade
- [ ] Testar UI (TestFX)
- [ ] Validar com usuários
- [ ] Documentar mudanças

### Geral
- [ ] Configurar dependências
- [ ] Criar estrutura de pacotes
- [ ] Implementar infraestrutura
- [ ] Criar sistema de temas
- [ ] Migrar todas as telas
- [ ] Testes de integração
- [ ] Testes de performance
- [ ] Documentação completa
- [ ] Treinamento de usuários

## 🎓 Conclusão

### Recomendação
**SIM**, migrar para JavaFX é altamente recomendado:

**Quando?**
- Após completar refatoração de views (100%)
- Após implementar testes automatizados
- Quando houver 3.5 meses disponíveis

**Por quê?**
- Interface moderna e profissional
- Melhor experiência do usuário
- Facilita manutenção futura
- Tecnologia mais atual

**Como?**
- Migração incremental
- Manter Swing funcionando em paralelo
- Testes contínuos
- Validação com usuários

### ROI Esperado
- **Satisfação do usuário**: +200%
- **Aparência**: +300%
- **Manutenibilidade**: +50%
- **Produtividade dev**: +30%

---

**Prioridade**: MÉDIA-ALTA  
**Esforço**: 14 semanas  
**ROI**: Excelente  
**Risco**: Médio (mitigável)
