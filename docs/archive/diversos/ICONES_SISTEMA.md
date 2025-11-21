# 🎨 Sistema de Ícones - SIHCP

## Visão Geral

O sistema agora possui ícones personalizados que substituem o ícone padrão do Java (xícara de café).

## Classe IconManager

Localização: `src/main/java/com/inventario/util/IconManager.java`

### Funcionalidades

#### 1. Ícone Padrão da Aplicação
```java
// Obter ícone como ImageIcon
ImageIcon icon = IconManager.getAppIcon();

// Obter ícone como Image (para setIconImage)
Image image = IconManager.getAppImage();
```

#### 2. Definir Ícone em Janelas

**Para JFrame:**
```java
IconManager.setFrameIcon(frame);
// ou
frame.setIconImages(IconManager.getAppIconImages());
```

**Para JDialog:**
```java
IconManager.setDialogIcon(dialog);
```

**Para Window genérico:**
```java
IconManager.setWindowIcon(window);
```

#### 3. Múltiplos Tamanhos

O sistema gera automaticamente ícones em 5 tamanhos diferentes:
- 16x16 (barra de tarefas)
- 32x32 (janela pequena)
- 48x48 (janela média)
- 64x64 (janela grande)
- 128x128 (alta resolução)

```java
List<Image> icons = IconManager.getAppIconImages();
frame.setIconImages(icons);
```

## Design do Ícone

### Ícone Padrão (SIHCP)
- **Formato**: Círculo com gradiente azul
- **Texto**: "SI" (grande) + "HC" (pequeno)
- **Cores**: 
  - Gradiente: #3498DB → #2980B9
  - Texto: Branco (#FFFFFF)
  - Borda: #2980B9

### Ícone Alternativo (Inventário)
```java
ImageIcon inventoryIcon = IconManager.createInventoryIcon();
```
- **Formato**: Círculo verde com símbolo de checklist
- **Cores**: 
  - Gradiente: #2ECC71 → #27AE60
  - Símbolo: Branco

## Janelas Atualizadas

As seguintes janelas já possuem o ícone configurado:

✅ **JLogin** - Tela de login
✅ **MainFrame** - Janela principal
✅ **InventarioFrame** - Gerenciamento de inventários

## Como Adicionar em Novas Janelas

### Para JFrame
```java
public class MinhaJanela extends JFrame {
    public MinhaJanela() {
        // ... outras configurações ...
        
        // Adicionar ícone
        setIconImages(IconManager.getAppIconImages());
    }
}
```

### Para JDialog
```java
public class MeuDialog extends JDialog {
    public MeuDialog(Frame parent) {
        super(parent, "Título", true);
        
        // ... outras configurações ...
        
        // Adicionar ícone
        IconManager.setDialogIcon(this);
    }
}
```

### Para Todas as Janelas
```java
// Aplicar ícone a todas as janelas abertas
IconManager.setIconForAllWindows();
```

## Personalização

### Usar Ícone de Arquivo

1. Criar arquivo `app-icon.png` em `src/main/resources/icons/`
2. O IconManager tentará carregar automaticamente
3. Se não encontrar, usa o ícone programático

### Criar Ícone Personalizado

```java
public class CustomIconManager {
    public static ImageIcon createCustomIcon() {
        int size = 64;
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        
        // Configurar anti-aliasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                            RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Desenhar seu ícone personalizado aqui
        g2d.setColor(Color.BLUE);
        g2d.fillOval(0, 0, size, size);
        
        g2d.dispose();
        return new ImageIcon(image);
    }
}
```

## Estrutura de Diretórios

```
src/main/resources/
└── icons/
    ├── app-icon.png          (opcional - ícone principal)
    ├── app-icon-16.png       (opcional - 16x16)
    ├── app-icon-32.png       (opcional - 32x32)
    ├── app-icon-48.png       (opcional - 48x48)
    ├── app-icon-64.png       (opcional - 64x64)
    └── app-icon-128.png      (opcional - 128x128)
```

## Benefícios

✅ **Identidade Visual**: Ícone personalizado reforça a marca do sistema
✅ **Profissionalismo**: Aparência mais profissional que o ícone padrão
✅ **Reconhecimento**: Fácil identificação na barra de tarefas
✅ **Múltiplas Resoluções**: Suporte automático para diferentes tamanhos
✅ **Fallback Automático**: Se arquivo não existir, gera ícone programaticamente

## Exemplos Visuais

### Ícone na Barra de Tarefas
```
┌─────────────────────────────┐
│ [SI/HC] SIHCP - Login       │  ← Ícone aparece aqui
└─────────────────────────────┘
```

### Ícone na Janela
```
┌─[SI/HC]─ SIHCP ─────────────┐
│                              │
│  Conteúdo da janela          │
│                              │
└──────────────────────────────┘
```

### Ícone no Alt+Tab (Windows)
```
┌──────┐  ┌──────┐  ┌──────┐
│ SI   │  │ SI   │  │ SI   │
│ HC   │  │ HC   │  │ HC   │
└──────┘  └──────┘  └──────┘
 Login     Main    Inventário
```

## Troubleshooting

### Ícone não aparece
1. Verificar se `IconManager.getAppIconImages()` está sendo chamado
2. Verificar logs de erro no console
3. Testar com ícone programático (padrão)

### Ícone distorcido
1. Usar múltiplos tamanhos via `getAppIconImages()`
2. Verificar qualidade da imagem fonte
3. Usar formato PNG com transparência

### Ícone não atualiza
1. Limpar cache do sistema operacional
2. Reiniciar a aplicação
3. Verificar se está usando `setIconImages()` (plural)

## Referências

- [Java Icon Tutorial](https://docs.oracle.com/javase/tutorial/uiswing/components/frame.html)
- [BufferedImage Documentation](https://docs.oracle.com/javase/8/docs/api/java/awt/image/BufferedImage.html)
- [Graphics2D Documentation](https://docs.oracle.com/javase/8/docs/api/java/awt/Graphics2D.html)

---

**Versão**: 2.0.0  
**Última Atualização**: 08/11/2025
