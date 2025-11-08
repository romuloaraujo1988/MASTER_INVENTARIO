package com.inventario.mobile.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.math.*

/**
 * Utilitários para funcionalidades de localização e GPS
 */
object LocationUtils {
    
    private const val EARTH_RADIUS_KM = 6371.0
    private const val EARTH_RADIUS_M = 6371000.0
    
    /**
     * Verifica se o GPS está habilitado
     */
    fun isGpsEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }
    
    /**
     * Verifica se a localização de rede está habilitada
     */
    fun isNetworkLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
    
    /**
     * Verifica se algum provedor de localização está habilitado
     */
    fun isLocationEnabled(context: Context): Boolean {
        return isGpsEnabled(context) || isNetworkLocationEnabled(context)
    }
    
    /**
     * Verifica se as permissões de localização foram concedidas
     */
    fun hasLocationPermission(context: Context): Boolean {
        val fineLocationPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val coarseLocationPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        
        return fineLocationPermission == PermissionChecker.PERMISSION_GRANTED ||
                coarseLocationPermission == PermissionChecker.PERMISSION_GRANTED
    }
    
    /**
     * Verifica se pode obter localização
     */
    fun canGetLocation(context: Context): Boolean {
        return hasLocationPermission(context) && isLocationEnabled(context)
    }
    
    /**
     * Abre configurações de localização
     */
    fun openLocationSettings(context: Context) {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }
    
    /**
     * Calcula distância entre duas coordenadas usando fórmula de Haversine
     */
    fun calculateDistance(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double,
        unit: DistanceUnit = DistanceUnit.METERS
    ): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        
        return when (unit) {
            DistanceUnit.KILOMETERS -> EARTH_RADIUS_KM * c
            DistanceUnit.METERS -> EARTH_RADIUS_M * c
            DistanceUnit.MILES -> (EARTH_RADIUS_KM * c) * 0.621371
        }
    }
    
    /**
     * Calcula distância entre duas localizações
     */
    fun calculateDistance(
        location1: Location,
        location2: Location,
        unit: DistanceUnit = DistanceUnit.METERS
    ): Double {
        return calculateDistance(
            location1.latitude,
            location1.longitude,
            location2.latitude,
            location2.longitude,
            unit
        )
    }
    
    /**
     * Calcula distância entre coordenadas e uma localização
     */
    fun calculateDistance(
        lat: Double,
        lon: Double,
        location: Location,
        unit: DistanceUnit = DistanceUnit.METERS
    ): Double {
        return calculateDistance(lat, lon, location.latitude, location.longitude, unit)
    }
    
    /**
     * Formata distância para exibição
     */
    fun formatDistance(distanceInMeters: Double): String {
        return when {
            distanceInMeters < 1000 -> "${distanceInMeters.roundToInt()}m"
            distanceInMeters < 10000 -> String.format("%.1fkm", distanceInMeters / 1000)
            else -> "${(distanceInMeters / 1000).roundToInt()}km"
        }
    }
    
    /**
     * Calcula bearing (direção) entre duas coordenadas
     */
    fun calculateBearing(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val dLon = Math.toRadians(lon2 - lon1)
        val lat1Rad = Math.toRadians(lat1)
        val lat2Rad = Math.toRadians(lat2)
        
        val y = sin(dLon) * cos(lat2Rad)
        val x = cos(lat1Rad) * sin(lat2Rad) - sin(lat1Rad) * cos(lat2Rad) * cos(dLon)
        
        val bearing = Math.toDegrees(atan2(y, x))
        return (bearing + 360) % 360
    }
    
    /**
     * Converte bearing para direção cardinal
     */
    fun bearingToCardinalDirection(bearing: Double): String {
        val directions = arrayOf(
            "N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE",
            "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW"
        )
        val index = ((bearing + 11.25) / 22.5).toInt() % 16
        return directions[index]
    }
    
    /**
     * Converte bearing para direção cardinal em português
     */
    fun bearingToCardinalDirectionPt(bearing: Double): String {
        val directions = arrayOf(
            "N", "NNE", "NE", "ENE", "L", "LSE", "SE", "SSE",
            "S", "SSO", "SO", "OSO", "O", "ONO", "NO", "NNO"
        )
        val index = ((bearing + 11.25) / 22.5).toInt() % 16
        return directions[index]
    }
    
    /**
     * Verifica se uma coordenada está dentro de um raio
     */
    fun isWithinRadius(
        centerLat: Double,
        centerLon: Double,
        pointLat: Double,
        pointLon: Double,
        radiusInMeters: Double
    ): Boolean {
        val distance = calculateDistance(centerLat, centerLon, pointLat, pointLon)
        return distance <= radiusInMeters
    }
    
    /**
     * Verifica se uma localização está dentro de um raio
     */
    fun isWithinRadius(
        center: Location,
        point: Location,
        radiusInMeters: Double
    ): Boolean {
        return isWithinRadius(
            center.latitude,
            center.longitude,
            point.latitude,
            point.longitude,
            radiusInMeters
        )
    }
    
    /**
     * Obtém endereço a partir de coordenadas (Geocoding reverso)
     */
    suspend fun getAddressFromCoordinates(
        context: Context,
        latitude: Double,
        longitude: Double,
        maxResults: Int = 1
    ): List<Address>? {
        return withContext(Dispatchers.IO) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    // Para Android 13+, usar o método assíncrono
                    null // Implementar callback se necessário
                } else {
                    @Suppress("DEPRECATION")
                    val geocoder = Geocoder(context, Locale.getDefault())
                    geocoder.getFromLocation(latitude, longitude, maxResults)
                }
            } catch (e: Exception) {
                null
            }
        }
    }
    
    /**
     * Obtém coordenadas a partir de endereço (Geocoding)
     */
    suspend fun getCoordinatesFromAddress(
        context: Context,
        address: String,
        maxResults: Int = 1
    ): List<Address>? {
        return withContext(Dispatchers.IO) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    // Para Android 13+, usar o método assíncrono
                    null // Implementar callback se necessário
                } else {
                    @Suppress("DEPRECATION")
                    val geocoder = Geocoder(context, Locale.getDefault())
                    geocoder.getFromLocationName(address, maxResults)
                }
            } catch (e: Exception) {
                null
            }
        }
    }
    
    /**
     * Formata coordenadas para exibição
     */
    fun formatCoordinates(
        latitude: Double,
        longitude: Double,
        format: CoordinateFormat = CoordinateFormat.DECIMAL_DEGREES
    ): String {
        return when (format) {
            CoordinateFormat.DECIMAL_DEGREES -> {
                "${String.format("%.6f", latitude)}, ${String.format("%.6f", longitude)}"
            }
            CoordinateFormat.DEGREES_MINUTES_SECONDS -> {
                val latDMS = decimalToDMS(latitude, true)
                val lonDMS = decimalToDMS(longitude, false)
                "$latDMS, $lonDMS"
            }
            CoordinateFormat.DEGREES_DECIMAL_MINUTES -> {
                val latDDM = decimalToDDM(latitude, true)
                val lonDDM = decimalToDDM(longitude, false)
                "$latDDM, $lonDDM"
            }
        }
    }
    
    /**
     * Converte decimal para graus, minutos e segundos
     */
    private fun decimalToDMS(decimal: Double, isLatitude: Boolean): String {
        val abs = abs(decimal)
        val degrees = abs.toInt()
        val minutesDecimal = (abs - degrees) * 60
        val minutes = minutesDecimal.toInt()
        val seconds = (minutesDecimal - minutes) * 60
        
        val direction = if (isLatitude) {
            if (decimal >= 0) "N" else "S"
        } else {
            if (decimal >= 0) "E" else "W"
        }
        
        return String.format("%d°%02d'%05.2f\"%s", degrees, minutes, seconds, direction)
    }
    
    /**
     * Converte decimal para graus e minutos decimais
     */
    private fun decimalToDDM(decimal: Double, isLatitude: Boolean): String {
        val abs = abs(decimal)
        val degrees = abs.toInt()
        val minutes = (abs - degrees) * 60
        
        val direction = if (isLatitude) {
            if (decimal >= 0) "N" else "S"
        } else {
            if (decimal >= 0) "E" else "W"
        }
        
        return String.format("%d°%07.4f'%s", degrees, minutes, direction)
    }
    
    /**
     * Valida coordenadas
     */
    fun isValidCoordinates(latitude: Double, longitude: Double): Boolean {
        return ValidationUtils.isValidLatitude(latitude) && ValidationUtils.isValidLongitude(longitude)
    }
    
    /**
     * Cria URL do Google Maps
     */
    fun createGoogleMapsUrl(
        latitude: Double,
        longitude: Double,
        zoom: Int = 15,
        label: String? = null
    ): String {
        val baseUrl = "https://www.google.com/maps"
        val coords = "$latitude,$longitude"
        val labelParam = if (label != null) "($label)" else ""
        return "$baseUrl?q=$coords$labelParam&z=$zoom"
    }
    
    /**
     * Abre localização no Google Maps
     */
    fun openInGoogleMaps(
        context: Context,
        latitude: Double,
        longitude: Double,
        label: String? = null
    ) {
        val uri = if (label != null) {
            Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude($label)")
        } else {
            Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude")
        }
        
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.google.android.apps.maps")
        
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            // Fallback para navegador
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(createGoogleMapsUrl(latitude, longitude, label = label)))
            context.startActivity(webIntent)
        }
    }
    
    /**
     * Cria intent para navegação
     */
    fun createNavigationIntent(
        latitude: Double,
        longitude: Double,
        label: String? = null
    ): Intent {
        val uri = if (label != null) {
            Uri.parse("google.navigation:q=$latitude,$longitude($label)")
        } else {
            Uri.parse("google.navigation:q=$latitude,$longitude")
        }
        
        return Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.apps.maps")
        }
    }
    
    /**
     * Obtém melhor provedor de localização disponível
     */
    fun getBestLocationProvider(context: Context): String? {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        
        return when {
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
            locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER) -> LocationManager.PASSIVE_PROVIDER
            else -> null
        }
    }
    
    /**
     * Verifica se a localização é recente
     */
    fun isLocationRecent(location: Location, maxAgeMillis: Long = 5 * 60 * 1000): Boolean {
        val locationAge = System.currentTimeMillis() - location.time
        return locationAge <= maxAgeMillis
    }
    
    /**
     * Verifica se a localização tem precisão aceitável
     */
    fun hasAcceptableAccuracy(location: Location, maxAccuracyMeters: Float = 100f): Boolean {
        return location.hasAccuracy() && location.accuracy <= maxAccuracyMeters
    }
    
    /**
     * Verifica se a localização é válida para uso
     */
    fun isLocationValid(
        location: Location,
        maxAgeMillis: Long = 5 * 60 * 1000,
        maxAccuracyMeters: Float = 100f
    ): Boolean {
        return isValidCoordinates(location.latitude, location.longitude) &&
                isLocationRecent(location, maxAgeMillis) &&
                hasAcceptableAccuracy(location, maxAccuracyMeters)
    }
    
    /**
     * Cria objeto Location a partir de coordenadas
     */
    fun createLocation(
        latitude: Double,
        longitude: Double,
        provider: String = "manual",
        accuracy: Float? = null,
        time: Long = System.currentTimeMillis()
    ): Location {
        return Location(provider).apply {
            this.latitude = latitude
            this.longitude = longitude
            this.time = time
            accuracy?.let { this.accuracy = it }
        }
    }
    
    /**
     * Calcula centro geográfico de uma lista de coordenadas
     */
    fun calculateCenter(locations: List<Location>): Location? {
        if (locations.isEmpty()) return null
        
        var x = 0.0
        var y = 0.0
        var z = 0.0
        
        locations.forEach { location ->
            val lat = Math.toRadians(location.latitude)
            val lon = Math.toRadians(location.longitude)
            
            x += cos(lat) * cos(lon)
            y += cos(lat) * sin(lon)
            z += sin(lat)
        }
        
        val total = locations.size
        x /= total
        y /= total
        z /= total
        
        val centralLon = atan2(y, x)
        val centralSquareRoot = sqrt(x * x + y * y)
        val centralLat = atan2(z, centralSquareRoot)
        
        return createLocation(
            Math.toDegrees(centralLat),
            Math.toDegrees(centralLon)
        )
    }
    
    /**
     * Calcula bounds (limites) de uma lista de coordenadas
     */
    fun calculateBounds(locations: List<Location>): LocationBounds? {
        if (locations.isEmpty()) return null
        
        var minLat = Double.MAX_VALUE
        var maxLat = Double.MIN_VALUE
        var minLon = Double.MAX_VALUE
        var maxLon = Double.MIN_VALUE
        
        locations.forEach { location ->
            minLat = minOf(minLat, location.latitude)
            maxLat = maxOf(maxLat, location.latitude)
            minLon = minOf(minLon, location.longitude)
            maxLon = maxOf(maxLon, location.longitude)
        }
        
        return LocationBounds(
            southwest = createLocation(minLat, minLon),
            northeast = createLocation(maxLat, maxLon)
        )
    }
    
    /**
     * Enums e Data Classes
     */
    enum class DistanceUnit {
        METERS, KILOMETERS, MILES
    }
    
    enum class CoordinateFormat {
        DECIMAL_DEGREES,
        DEGREES_MINUTES_SECONDS,
        DEGREES_DECIMAL_MINUTES
    }
    
    data class LocationBounds(
        val southwest: Location,
        val northeast: Location
    ) {
        fun contains(location: Location): Boolean {
            return location.latitude >= southwest.latitude &&
                    location.latitude <= northeast.latitude &&
                    location.longitude >= southwest.longitude &&
                    location.longitude <= northeast.longitude
        }
        
        fun getCenter(): Location {
            val centerLat = (southwest.latitude + northeast.latitude) / 2
            val centerLon = (southwest.longitude + northeast.longitude) / 2
            return createLocation(centerLat, centerLon)
        }
    }
    
    data class LocationInfo(
        val location: Location,
        val address: String? = null,
        val accuracy: String,
        val provider: String,
        val timestamp: String
    )
    
    /**
     * Cria informações detalhadas da localização
     */
    fun createLocationInfo(location: Location, address: String? = null): LocationInfo {
        val accuracy = if (location.hasAccuracy()) {
            "±${location.accuracy.roundToInt()}m"
        } else {
            "Desconhecida"
        }
        
        val provider = when (location.provider) {
            LocationManager.GPS_PROVIDER -> "GPS"
            LocationManager.NETWORK_PROVIDER -> "Rede"
            LocationManager.PASSIVE_PROVIDER -> "Passivo"
            else -> location.provider ?: "Desconhecido"
        }
        
        val timestamp = DateUtils.formatDateTime(Date(location.time))
        
        return LocationInfo(
            location = location,
            address = address,
            accuracy = accuracy,
            provider = provider,
            timestamp = timestamp
        )
    }
}