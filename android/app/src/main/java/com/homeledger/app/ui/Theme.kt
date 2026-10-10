package com.homeledger.app.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Forest = Color(0xFF145C43)
private val DeepForest = Color(0xFF0D3B2E)
private val FreshGreen = Color(0xFF35A879)
private val LightColors = lightColorScheme(primary = Forest, onPrimary = Color.White, secondary = FreshGreen, background = Color(0xFFF6F8F6), surface = Color.White, onBackground = Color(0xFF17231D), onSurface = Color(0xFF17231D), outline = Color(0xFFE5EBE6), error = Color(0xFFB42318))
private val DarkColors = darkColorScheme(primary = Color(0xFF74C69D), onPrimary = Color(0xFF0D3B2E), secondary = Color(0xFF86D5AE), background = Color(0xFF101713), surface = Color(0xFF19231D), onBackground = Color(0xFFE6EEE8), onSurface = Color(0xFFE6EEE8), outline = Color(0xFF34443A), error = Color(0xFFFFB4AB), primaryContainer = DeepForest)

@Composable
fun HomeLedgerTheme(preference: String, content: @Composable () -> Unit) {
    val dark = when (preference) { "DARK" -> true; "LIGHT" -> false; else -> isSystemInDarkTheme() }
    MaterialTheme(colorScheme = if (dark) DarkColors else LightColors, content = content)
}
