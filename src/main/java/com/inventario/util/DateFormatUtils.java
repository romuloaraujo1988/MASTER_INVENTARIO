package com.inventario.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.sql.Timestamp;

/**
 * Classe utilitária centralizada para formatação e parsing de datas
 * Padroniza o tratamento de datas em todo o sistema
 * 
 * @author Sistema de Inventário IFMT
 * @version 1.0.0
 */
public class DateFormatUtils {
    
    // Locale brasileiro para formatação
    private static final Locale LOCALE_BR = Locale.forLanguageTag("pt-BR");
    
    // Formatos de data padronizados
    public static final String PATTERN_DATE = "dd/MM/yyyy";
    public static final String PATTERN_DATETIME = "dd/MM/yyyy HH:mm";
    public static final String PATTERN_DATETIME_FULL = "dd/MM/yyyy HH:mm:ss";
    public static final String PATTERN_API = "yyyy-MM-dd HH:mm:ss";
    public static final String PATTERN_API_DATE = "yyyy-MM-dd";
    public static final String PATTERN_FILENAME = "yyyyMMdd_HHmmss";
    
    // Formatadores thread-safe para LocalDateTime
    public static final DateTimeFormatter FORMATTER_DATE = DateTimeFormatter.ofPattern(PATTERN_DATE);
    public static final DateTimeFormatter FORMATTER_DATETIME = DateTimeFormatter.ofPattern(PATTERN_DATETIME);
    public static final DateTimeFormatter FORMATTER_DATETIME_FULL = DateTimeFormatter.ofPattern(PATTERN_DATETIME_FULL);
    public static final DateTimeFormatter FORMATTER_API = DateTimeFormatter.ofPattern(PATTERN_API);
    public static final DateTimeFormatter FORMATTER_API_DATE = DateTimeFormatter.ofPattern(PATTERN_API_DATE);
    public static final DateTimeFormatter FORMATTER_FILENAME = DateTimeFormatter.ofPattern(PATTERN_FILENAME);
    
    // Formatadores SimpleDateFormat para Date/Timestamp (não thread-safe - usar com cuidado)
    private static final ThreadLocal<SimpleDateFormat> DATE_FORMAT = 
        ThreadLocal.withInitial(() -> new SimpleDateFormat(PATTERN_DATE, LOCALE_BR));
    private static final ThreadLocal<SimpleDateFormat> DATETIME_FORMAT = 
        ThreadLocal.withInitial(() -> new SimpleDateFormat(PATTERN_DATETIME, LOCALE_BR));
    private static final ThreadLocal<SimpleDateFormat> DATETIME_FULL_FORMAT = 
        ThreadLocal.withInitial(() -> new SimpleDateFormat(PATTERN_DATETIME_FULL, LOCALE_BR));
    private static final ThreadLocal<SimpleDateFormat> API_FORMAT = 
        ThreadLocal.withInitial(() -> new SimpleDateFormat(PATTERN_API, LOCALE_BR));
    private static final ThreadLocal<SimpleDateFormat> API_DATE_FORMAT = 
        ThreadLocal.withInitial(() -> new SimpleDateFormat(PATTERN_API_DATE, LOCALE_BR));
    private static final ThreadLocal<SimpleDateFormat> FILENAME_FORMAT = 
        ThreadLocal.withInitial(() -> new SimpleDateFormat(PATTERN_FILENAME, LOCALE_BR));
    
    // Construtor privado para classe utilitária
    private DateFormatUtils() {
        throw new UnsupportedOperationException("Classe utilitária não deve ser instanciada");
    }
    
    // ==================== FORMATAÇÃO DE LocalDateTime ====================
    
    /**
     * Formata LocalDateTime para data (dd/MM/yyyy)
     */
    public static String formatDate(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(FORMATTER_DATE) : "";
    }
    
    /**
     * Formata LocalDateTime para data e hora (dd/MM/yyyy HH:mm)
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(FORMATTER_DATETIME) : "";
    }
    
    /**
     * Formata LocalDateTime para data e hora completa (dd/MM/yyyy HH:mm:ss)
     */
    public static String formatDateTimeFull(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(FORMATTER_DATETIME_FULL) : "";
    }
    
    /**
     * Formata LocalDateTime para API (yyyy-MM-dd HH:mm:ss)
     */
    public static String formatForApi(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(FORMATTER_API) : "";
    }
    
    /**
     * Formata LocalDateTime para nome de arquivo (yyyyMMdd_HHmmss)
     */
    public static String formatForFilename(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(FORMATTER_FILENAME) : "";
    }
    
    // ==================== FORMATAÇÃO DE Date/Timestamp ====================
    
    /**
     * Formata Date para data (dd/MM/yyyy)
     */
    public static String formatDate(Date date) {
        return date != null ? DATE_FORMAT.get().format(date) : "";
    }
    
    /**
     * Formata Date para data e hora (dd/MM/yyyy HH:mm)
     */
    public static String formatDateTime(Date date) {
        return date != null ? DATETIME_FORMAT.get().format(date) : "";
    }
    
    /**
     * Formata Date para data e hora completa (dd/MM/yyyy HH:mm:ss)
     */
    public static String formatDateTimeFull(Date date) {
        return date != null ? DATETIME_FULL_FORMAT.get().format(date) : "";
    }
    
    /**
     * Formata Timestamp para data (dd/MM/yyyy)
     */
    public static String formatDate(Timestamp timestamp) {
        return timestamp != null ? DATE_FORMAT.get().format(timestamp) : "";
    }
    
    /**
     * Formata Timestamp para data e hora (dd/MM/yyyy HH:mm)
     */
    public static String formatDateTime(Timestamp timestamp) {
        return timestamp != null ? DATETIME_FORMAT.get().format(timestamp) : "";
    }
    
    /**
     * Formata Timestamp para data e hora completa (dd/MM/yyyy HH:mm:ss)
     */
    public static String formatDateTimeFull(Timestamp timestamp) {
        return timestamp != null ? DATETIME_FULL_FORMAT.get().format(timestamp) : "";
    }
    
    /**
     * Formata Date para API (yyyy-MM-dd HH:mm:ss)
     */
    public static String formatForApi(Date date) {
        return date != null ? API_FORMAT.get().format(date) : "";
    }
    
    /**
     * Formata Date para nome de arquivo (yyyyMMdd_HHmmss)
     */
    public static String formatForFilename(Date date) {
        return date != null ? FILENAME_FORMAT.get().format(date) : "";
    }
    
    // ==================== PARSING DE DATAS ====================
    
    /**
     * Converte string para Date usando formato dd/MM/yyyy
     */
    public static Date parseDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty() || dateString.equals("-")) {
            return null;
        }
        try {
            return DATE_FORMAT.get().parse(dateString.trim());
        } catch (ParseException e) {
            return null;
        }
    }
    
    /**
     * Converte string para Date usando formato dd/MM/yyyy HH:mm
     */
    public static Date parseDateTime(String dateTimeString) {
        if (dateTimeString == null || dateTimeString.trim().isEmpty() || dateTimeString.equals("-")) {
            return null;
        }
        try {
            return DATETIME_FORMAT.get().parse(dateTimeString.trim());
        } catch (ParseException e) {
            return null;
        }
    }
    
    /**
     * Converte string para Date usando formato dd/MM/yyyy HH:mm:ss
     */
    public static Date parseDateTimeFull(String dateTimeString) {
        if (dateTimeString == null || dateTimeString.trim().isEmpty() || dateTimeString.equals("-")) {
            return null;
        }
        try {
            return DATETIME_FULL_FORMAT.get().parse(dateTimeString.trim());
        } catch (ParseException e) {
            return null;
        }
    }
    
    /**
     * Converte string da API para Date usando formato yyyy-MM-dd HH:mm:ss
     */
    public static Date parseFromApi(String apiDateString) {
        if (apiDateString == null || apiDateString.trim().isEmpty()) {
            return null;
        }
        try {
            return API_FORMAT.get().parse(apiDateString.trim());
        } catch (ParseException e) {
            return null;
        }
    }
    
    /**
     * Converte string da API para Date usando formato yyyy-MM-dd
     */
    public static Date parseDateFromApi(String apiDateString) {
        if (apiDateString == null || apiDateString.trim().isEmpty()) {
            return null;
        }
        try {
            return API_DATE_FORMAT.get().parse(apiDateString.trim());
        } catch (ParseException e) {
            return null;
        }
    }
    
    /**
     * Converte string para LocalDateTime usando formato dd/MM/yyyy HH:mm
     */
    public static LocalDateTime parseToLocalDateTime(String dateTimeString) {
        if (dateTimeString == null || dateTimeString.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateTimeString.trim(), FORMATTER_DATETIME);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Converte string da API para LocalDateTime usando formato yyyy-MM-dd HH:mm:ss
     */
    public static LocalDateTime parseFromApiToLocalDateTime(String apiDateString) {
        if (apiDateString == null || apiDateString.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(apiDateString.trim(), FORMATTER_API);
        } catch (Exception e) {
            return null;
        }
    }
    
    // ==================== CONVERSÕES ====================
    
    /**
     * Converte Date para LocalDateTime
     */
    public static LocalDateTime toLocalDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return new Timestamp(date.getTime()).toLocalDateTime();
    }
    
    /**
     * Converte LocalDateTime para Date
     */
    public static Date toDate(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return Timestamp.valueOf(localDateTime);
    }
    
    /**
     * Converte LocalDateTime para Timestamp
     */
    public static Timestamp toTimestamp(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return Timestamp.valueOf(localDateTime);
    }
    
    /**
     * Converte Timestamp para LocalDateTime
     */
    public static LocalDateTime toLocalDateTime(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return timestamp.toLocalDateTime();
    }
    
    // ==================== UTILITÁRIOS ====================
    
    /**
     * Obtém data/hora atual como LocalDateTime
     */
    public static LocalDateTime now() {
        return LocalDateTime.now();
    }
    
    /**
     * Obtém data/hora atual como Timestamp
     */
    public static Timestamp nowAsTimestamp() {
        return new Timestamp(System.currentTimeMillis());
    }
    
    /**
     * Obtém data/hora atual como Date
     */
    public static Date nowAsDate() {
        return new Date();
    }
    
    /**
     * Valida se uma string representa uma data válida no formato especificado
     */
    public static boolean isValidDate(String dateString, String pattern) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return false;
        }
        try {
            SimpleDateFormat format = new SimpleDateFormat(pattern, LOCALE_BR);
            format.setLenient(false);
            format.parse(dateString.trim());
            return true;
        } catch (ParseException e) {
            return false;
        }
    }
    
    /**
     * Valida se uma string representa uma data válida no formato dd/MM/yyyy
     */
    public static boolean isValidDate(String dateString) {
        return isValidDate(dateString, PATTERN_DATE);
    }
    
    /**
     * Valida se uma string representa uma data/hora válida no formato dd/MM/yyyy HH:mm
     */
    public static boolean isValidDateTime(String dateTimeString) {
        return isValidDate(dateTimeString, PATTERN_DATETIME);
    }
    
    /**
     * Retorna mensagem de "não informado" para datas nulas
     */
    public static String formatWithDefault(LocalDateTime dateTime, String defaultValue) {
        return dateTime != null ? formatDateTime(dateTime) : defaultValue;
    }
    
    /**
     * Retorna mensagem de "não informado" para datas nulas
     */
    public static String formatWithDefault(Date date, String defaultValue) {
        return date != null ? formatDateTime(date) : defaultValue;
    }
    
    /**
     * Retorna mensagem de "não informado" para timestamps nulos
     */
    public static String formatWithDefault(Timestamp timestamp, String defaultValue) {
        return timestamp != null ? formatDateTime(timestamp) : defaultValue;
    }
}