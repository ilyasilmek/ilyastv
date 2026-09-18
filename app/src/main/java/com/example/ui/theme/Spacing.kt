package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Modern İlyasTV UI Boyut ve Boşluk (Spacing & Dimensions) Tanımları.
 * Tüm ekranlarda tutarlı padding, margin, radius ve bileşen boyutları sağlar.
 */
data class IlyasTvSpacing(
    val none: Dp = 0.dp,
    val extraSmall: Dp = 4.dp,
    val small: Dp = 8.dp,
    val medium: Dp = 12.dp,
    val default: Dp = 16.dp,
    val large: Dp = 20.dp,
    val extraLarge: Dp = 24.dp,
    val xxLarge: Dp = 32.dp,
    val xxxLarge: Dp = 48.dp,

    // Standart Ekran ve Konteyner Padding'leri
    val screenHorizontalPadding: Dp = 16.dp,
    val screenVerticalPadding: Dp = 12.dp,
    val cardContentPadding: Dp = 14.dp,
    val listItemSpacing: Dp = 10.dp,

    // Köşe Yuvarlama (Corner Radius) Değerleri
    val cardRadius: Dp = 14.dp,
    val chipRadius: Dp = 10.dp,
    val buttonRadius: Dp = 12.dp,
    val dialogRadius: Dp = 20.dp,
    val sheetRadius: Dp = 24.dp,

    // Bileşen Yükseklikleri & Dokunma Alanları
    val minTouchTarget: Dp = 48.dp,
    val buttonHeight: Dp = 48.dp,
    val topBarHeight: Dp = 56.dp,
    val bottomNavHeight: Dp = 64.dp
)

val LocalIlyasTvSpacing = staticCompositionLocalOf { IlyasTvSpacing() }

/**
 * MaterialTheme üzerinden kolay erişim:
 * Örnek: `MaterialTheme.spacing.default`, `MaterialTheme.spacing.small`
 */
val MaterialTheme.spacing: IlyasTvSpacing
    @Composable
    @ReadOnlyComposable
    get() = LocalIlyasTvSpacing.current
