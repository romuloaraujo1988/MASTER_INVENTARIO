package com.inventario.sihcp.util;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.text.ParseException;

/**
 * Utilitário para operações comuns em formulários
 * Elimina lógica repetida de manipulação de campos
 */
public class FormUtils {
    
    /**
     * Limpa todos os campos de texto de um container
     */
    public static void clearFields(Container container) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JTextField) {
                ((JTextField) comp).setText("");
            } else if (comp instanceof JTextArea) {
                ((JTextArea) comp).setText("");
            } else if (comp instanceof JComboBox) {
                ((JComboBox<?>) comp).setSelectedIndex(0);
            } else if (comp instanceof JCheckBox) {
                ((JCheckBox) comp).setSelected(false);
            } else if (comp instanceof Container) {
                clearFields((Container) comp);
            }
        }
    }
    
    /**
     * Habilita/desabilita todos os campos de um container
     */
    public static void setFieldsEnabled(Container container, boolean enabled) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JTextField || 
                comp instanceof JTextArea || 
                comp instanceof JComboBox || 
                comp instanceof JCheckBox ||
                comp instanceof JButton) {
                comp.setEnabled(enabled);
            } else if (comp instanceof Container) {
                setFieldsEnabled((Container) comp, enabled);
            }
        }
    }
    
    /**
     * Valida se todos os campos obrigatórios estão preenchidos
     */
    public static boolean validateRequiredFields(Component parent, JTextField... fields) {
        for (JTextField field : fields) {
            if (field.getText().trim().isEmpty()) {
                DialogUtils.showWarning(parent, "Todos os campos obrigatórios devem ser preenchidos");
                field.requestFocus();
                return false;
            }
        }
        return true;
    }
    
    /**
     * Cria campo de texto formatado para números
     */
    public static JFormattedTextField createNumberField() {
        NumberFormatter formatter = new NumberFormatter();
        formatter.setValueClass(Integer.class);
        formatter.setAllowsInvalid(false);
        formatter.setMinimum(0);
        
        return new JFormattedTextField(formatter);
    }
    
    /**
     * Cria campo de texto formatado para decimais
     */
    public static JFormattedTextField createDecimalField() {
        NumberFormatter formatter = new NumberFormatter(
            java.text.NumberFormat.getNumberInstance()
        );
        formatter.setValueClass(Double.class);
        formatter.setAllowsInvalid(false);
        formatter.setMinimum(0.0);
        
        return new JFormattedTextField(formatter);
    }
    
    /**
     * Cria campo de texto formatado para CPF
     */
    public static JFormattedTextField createCPFField() {
        try {
            MaskFormatter formatter = new MaskFormatter("###.###.###-##");
            formatter.setPlaceholderCharacter('_');
            return new JFormattedTextField(formatter);
        } catch (ParseException e) {
            return new JFormattedTextField();
        }
    }
    
    /**
     * Cria campo de texto formatado para telefone
     */
    public static JFormattedTextField createPhoneField() {
        try {
            MaskFormatter formatter = new MaskFormatter("(##) #####-####");
            formatter.setPlaceholderCharacter('_');
            return new JFormattedTextField(formatter);
        } catch (ParseException e) {
            return new JFormattedTextField();
        }
    }
    
    /**
     * Cria campo de texto formatado para CEP
     */
    public static JFormattedTextField createCEPField() {
        try {
            MaskFormatter formatter = new MaskFormatter("#####-###");
            formatter.setPlaceholderCharacter('_');
            return new JFormattedTextField(formatter);
        } catch (ParseException e) {
            return new JFormattedTextField();
        }
    }
    
    /**
     * Limita caracteres de um JTextField
     */
    public static void setMaxLength(JTextField field, int maxLength) {
        ((AbstractDocument) field.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) 
                    throws BadLocationException {
                if ((fb.getDocument().getLength() + string.length()) <= maxLength) {
                    super.insertString(fb, offset, string, attr);
                }
            }
            
            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) 
                    throws BadLocationException {
                int currentLength = fb.getDocument().getLength();
                int newLength = currentLength - length + (text != null ? text.length() : 0);
                if (newLength <= maxLength) {
                    super.replace(fb, offset, length, text, attrs);
                }
            }
        });
    }
    
    /**
     * Permite apenas números em um JTextField
     */
    public static void setNumericOnly(JTextField field) {
        ((AbstractDocument) field.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) 
                    throws BadLocationException {
                if (string.matches("\\d+")) {
                    super.insertString(fb, offset, string, attr);
                }
            }
            
            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) 
                    throws BadLocationException {
                if (text == null || text.matches("\\d+")) {
                    super.replace(fb, offset, length, text, attrs);
                }
            }
        });
    }
    
    /**
     * Permite apenas letras em um JTextField
     */
    public static void setAlphaOnly(JTextField field) {
        ((AbstractDocument) field.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) 
                    throws BadLocationException {
                if (string.matches("[a-zA-ZÀ-ÿ\\s]+")) {
                    super.insertString(fb, offset, string, attr);
                }
            }
            
            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) 
                    throws BadLocationException {
                if (text == null || text.matches("[a-zA-ZÀ-ÿ\\s]+")) {
                    super.replace(fb, offset, length, text, attrs);
                }
            }
        });
    }
    
    /**
     * Adiciona placeholder a um JTextField
     */
    public static void setPlaceholder(JTextField field, String placeholder) {
        field.putClientProperty("JTextField.placeholderText", placeholder);
    }
    
    /**
     * Foca no primeiro campo vazio
     */
    public static void focusFirstEmptyField(JTextField... fields) {
        for (JTextField field : fields) {
            if (field.getText().trim().isEmpty()) {
                field.requestFocus();
                return;
            }
        }
    }
    
    /**
     * Obtém valor de campo como Integer (null se vazio)
     */
    public static Integer getIntegerValue(JTextField field) {
        String text = field.getText().trim();
        if (text.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * Obtém valor de campo como Double (null se vazio)
     */
    public static Double getDoubleValue(JTextField field) {
        String text = field.getText().trim();
        if (text.isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * Define valor em campo (trata null)
     */
    public static void setValue(JTextField field, Object value) {
        field.setText(value != null ? value.toString() : "");
    }
    
    /**
     * Adiciona listener de Enter para executar ação
     */
    public static void addEnterKeyListener(JTextField field, Runnable action) {
        field.addActionListener(e -> action.run());
    }
}
