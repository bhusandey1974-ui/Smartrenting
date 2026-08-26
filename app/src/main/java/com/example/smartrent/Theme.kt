package com.example.smartrent

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import java.text.NumberFormat
import java.util.Locale

// Brand Colors
val BrandPrimary = Color(0xFF1E3A8A)       // Deep Indigo/Navy
val BrandSecondary = Color(0xFF0284C7)     // Ocean Blue
val BrandAccent = Color(0xFF0D9488)        // Teal
val BrandBackground = Color(0xFFF8FAFC)    // Crisp Slate White
val BrandSurface = Color(0xFFFFFFFF)       // Pure White
val BrandDarkNavy = Color(0xFF0F172A)      // Dark Slate

// Semantic Indicator Colors
val SuccessGreen = Color(0xFF16A34A)       // 🟢 Vacant
val WarningRed = Color(0xFFDC2626)         // 🔴 Occupied / Overdue
val PendingOrange = Color(0xFFD97706)      // 🟡 Unpaid / Due Soon

// Currency Helper
fun formatCurrency(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    return formatter.format(amount).replace(".00", "")
}

private val LightColorScheme = lightColorScheme(
    primary = BrandPrimary,
    secondary = BrandSecondary,
    tertiary = BrandAccent,
    background = BrandBackground,
    surface = BrandSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = BrandDarkNavy,
    onSurface = BrandDarkNavy
)

@Composable
fun SmartRentTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}

