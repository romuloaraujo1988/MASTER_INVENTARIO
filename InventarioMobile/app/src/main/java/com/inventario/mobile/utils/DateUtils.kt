package com.inventario.mobile.utils

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Utilitários para manipulação de datas
 */
object DateUtils {
    
    // Formatos de data
    private const val FORMAT_DATE_TIME = "dd/MM/yyyy HH:mm:ss"
    private const val FORMAT_DATE = "dd/MM/yyyy"
    private const val FORMAT_TIME = "HH:mm:ss"
    private const val FORMAT_DATE_TIME_SHORT = "dd/MM/yy HH:mm"
    private const val FORMAT_API = "yyyy-MM-dd HH:mm:ss"
    private const val FORMAT_API_DATE = "yyyy-MM-dd"
    private const val FORMAT_FILE_NAME = "yyyyMMdd_HHmmss"
    
    private val locale = Locale("pt", "BR")
    
    /**
     * Formata data e hora completa
     */
    fun formatDateTime(date: Date): String {
        return SimpleDateFormat(FORMAT_DATE_TIME, locale).format(date)
    }
    
    /**
     * Formata apenas a data
     */
    fun formatDate(date: Date): String {
        return SimpleDateFormat(FORMAT_DATE, locale).format(date)
    }
    
    /**
     * Formata apenas a hora
     */
    fun formatTime(date: Date): String {
        return SimpleDateFormat(FORMAT_TIME, locale).format(date)
    }
    
    /**
     * Formata data e hora de forma resumida
     */
    fun formatDateTimeShort(date: Date): String {
        return SimpleDateFormat(FORMAT_DATE_TIME_SHORT, locale).format(date)
    }
    
    /**
     * Formata data para API (formato ISO)
     */
    fun formatForApi(date: Date): String {
        return SimpleDateFormat(FORMAT_API, Locale.US).format(date)
    }
    
    /**
     * Formata apenas data para API
     */
    fun formatDateForApi(date: Date): String {
        return SimpleDateFormat(FORMAT_API_DATE, Locale.US).format(date)
    }
    
    /**
     * Formata data para nome de arquivo
     */
    fun formatForFileName(date: Date): String {
        return SimpleDateFormat(FORMAT_FILE_NAME, Locale.US).format(date)
    }
    
    /**
     * Converte string da API para Date
     */
    fun parseFromApi(dateString: String): Date? {
        return try {
            SimpleDateFormat(FORMAT_API, Locale.US).parse(dateString)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Converte string de data da API para Date
     */
    fun parseDateFromApi(dateString: String): Date? {
        return try {
            SimpleDateFormat(FORMAT_API_DATE, Locale.US).parse(dateString)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Converte timestamp para Date
     */
    fun fromTimestamp(timestamp: Long): Date {
        return Date(timestamp)
    }
    
    /**
     * Converte Date para timestamp
     */
    fun toTimestamp(date: Date): Long {
        return date.time
    }
    
    /**
     * Obtém data atual
     */
    fun now(): Date {
        return Date()
    }
    
    /**
     * Obtém timestamp atual
     */
    fun nowTimestamp(): Long {
        return System.currentTimeMillis()
    }
    
    /**
     * Verifica se uma data é hoje
     */
    fun isToday(date: Date): Boolean {
        val today = Calendar.getInstance()
        val dateCalendar = Calendar.getInstance().apply { time = date }
        
        return today.get(Calendar.YEAR) == dateCalendar.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == dateCalendar.get(Calendar.DAY_OF_YEAR)
    }
    
    /**
     * Verifica se uma data é ontem
     */
    fun isYesterday(date: Date): Boolean {
        val yesterday = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
        }
        val dateCalendar = Calendar.getInstance().apply { time = date }
        
        return yesterday.get(Calendar.YEAR) == dateCalendar.get(Calendar.YEAR) &&
                yesterday.get(Calendar.DAY_OF_YEAR) == dateCalendar.get(Calendar.DAY_OF_YEAR)
    }
    
    /**
     * Calcula diferença em dias entre duas datas
     */
    fun daysBetween(startDate: Date, endDate: Date): Long {
        val diffInMillis = endDate.time - startDate.time
        return TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS)
    }
    
    /**
     * Calcula diferença em horas entre duas datas
     */
    fun hoursBetween(startDate: Date, endDate: Date): Long {
        val diffInMillis = endDate.time - startDate.time
        return TimeUnit.HOURS.convert(diffInMillis, TimeUnit.MILLISECONDS)
    }
    
    /**
     * Calcula diferença em minutos entre duas datas
     */
    fun minutesBetween(startDate: Date, endDate: Date): Long {
        val diffInMillis = endDate.time - startDate.time
        return TimeUnit.MINUTES.convert(diffInMillis, TimeUnit.MILLISECONDS)
    }
    
    /**
     * Formata tempo relativo (ex: "há 2 horas", "ontem", etc.)
     */
    fun formatRelativeTime(date: Date): String {
        val now = Date()
        val diffInMillis = now.time - date.time
        
        return when {
            diffInMillis < 0 -> "no futuro"
            diffInMillis < TimeUnit.MINUTES.toMillis(1) -> "agora"
            diffInMillis < TimeUnit.HOURS.toMillis(1) -> {
                val minutes = TimeUnit.MINUTES.convert(diffInMillis, TimeUnit.MILLISECONDS)
                "há ${minutes}min"
            }
            diffInMillis < TimeUnit.DAYS.toMillis(1) -> {
                val hours = TimeUnit.HOURS.convert(diffInMillis, TimeUnit.MILLISECONDS)
                "há ${hours}h"
            }
            isYesterday(date) -> "ontem"
            diffInMillis < TimeUnit.DAYS.toMillis(7) -> {
                val days = TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS)
                "há ${days} dias"
            }
            else -> formatDate(date)
        }
    }
    
    /**
     * Obtém início do dia
     */
    fun startOfDay(date: Date): Date {
        val calendar = Calendar.getInstance().apply {
            time = date
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.time
    }
    
    /**
     * Obtém fim do dia
     */
    fun endOfDay(date: Date): Date {
        val calendar = Calendar.getInstance().apply {
            time = date
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return calendar.time
    }
    
    /**
     * Adiciona dias a uma data
     */
    fun addDays(date: Date, days: Int): Date {
        val calendar = Calendar.getInstance().apply {
            time = date
            add(Calendar.DAY_OF_YEAR, days)
        }
        return calendar.time
    }
    
    /**
     * Adiciona horas a uma data
     */
    fun addHours(date: Date, hours: Int): Date {
        val calendar = Calendar.getInstance().apply {
            time = date
            add(Calendar.HOUR_OF_DAY, hours)
        }
        return calendar.time
    }
    
    /**
     * Adiciona minutos a uma data
     */
    fun addMinutes(date: Date, minutes: Int): Date {
        val calendar = Calendar.getInstance().apply {
            time = date
            add(Calendar.MINUTE, minutes)
        }
        return calendar.time
    }
    
    /**
     * Verifica se uma data está dentro de um intervalo
     */
    fun isDateInRange(date: Date, startDate: Date, endDate: Date): Boolean {
        return date.time >= startDate.time && date.time <= endDate.time
    }
    
    /**
     * Obtém o primeiro dia do mês
     */
    fun firstDayOfMonth(date: Date): Date {
        val calendar = Calendar.getInstance().apply {
            time = date
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.time
    }
    
    /**
     * Obtém o último dia do mês
     */
    fun lastDayOfMonth(date: Date): Date {
        val calendar = Calendar.getInstance().apply {
            time = date
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return calendar.time
    }
    
    /**
     * Obtém nome do mês
     */
    fun getMonthName(date: Date): String {
        return SimpleDateFormat("MMMM", locale).format(date)
    }
    
    /**
     * Obtém nome do dia da semana
     */
    fun getDayOfWeekName(date: Date): String {
        return SimpleDateFormat("EEEE", locale).format(date)
    }
    
    /**
     * Valida se uma string é uma data válida
     */
    fun isValidDate(dateString: String, format: String = FORMAT_DATE): Boolean {
        return try {
            SimpleDateFormat(format, locale).parse(dateString)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Converte milissegundos para formato legível
     */
    fun formatDuration(durationMillis: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(durationMillis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(durationMillis) % 60
        
        return when {
            hours > 0 -> String.format("%02d:%02d:%02d", hours, minutes, seconds)
            minutes > 0 -> String.format("%02d:%02d", minutes, seconds)
            else -> String.format("00:%02d", seconds)
        }
    }
}
