package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

enum class AppThemeSetting(val title: String, val subtitle: String = "") {
    SYSTEM("Sistem Varsayılanı", "Cihazın açık/koyu moduna göre otomatik değişir"),
    DARK("Karanlık Mod", "Her zaman koyu tema kullanılır"),
    LIGHT("Aydınlık Mod", "Her zaman açık tema kullanılır")
}

enum class ViewModeSetting(val title: String) {
    EPG("EPG Rehber"),
    GRID("Küçük Resim"),
    LIST("Kompakt Liste")
}

/**
 * Tema Marka Stili Seçeneği:
 * Kullanıcının isteği üzerine önceki tema yapısı korunmuştur.
 * Beğenilmezse tek bir ayarla önceki klasik StreamFlow temasına anında geri dönülebilir!
 */
enum class ThemeBrandStyle(val title: String, val description: String) {
    MODERN_ILYAS_TV("Modern İlyasTV", "Sinematik kırmızı ve obsidian koyu tema"),
    CLASSIC_STREAMFLOW("Klasik Tema", "Önceki mavi ve arduvaz tema (Geri dönüş için korundu)")
}

// Aktif varsayılan marka stili (Kullanıcı dilediğinde CLASSIC_STREAMFLOW yapılabilir)
var activeThemeBrandStyle = ThemeBrandStyle.MODERN_ILYAS_TV

// ============================================================================
// 1. MODERN İLYASTV RENK ŞEMALARI (SİNEMATİK KIRMIZI & OBSIDIAN)
// ============================================================================
val IlyasTvDarkColorScheme = darkColorScheme(
    primary = IlyasTvDarkPrimary,
    onPrimary = IlyasTvDarkOnPrimary,
    primaryContainer = IlyasTvDarkPrimaryContainer,
    onPrimaryContainer = IlyasTvDarkOnPrimaryContainer,
    secondary = IlyasTvDarkSecondary,
    onSecondary = IlyasTvDarkOnSecondary,
    secondaryContainer = IlyasTvDarkSecondaryContainer,
    onSecondaryContainer = IlyasTvDarkOnSecondaryContainer,
    tertiary = IlyasTvDarkTertiary,
    onTertiary = IlyasTvDarkOnTertiary,
    tertiaryContainer = IlyasTvDarkTertiaryContainer,
    onTertiaryContainer = IlyasTvDarkOnTertiaryContainer,
    error = IlyasTvDarkError,
    onError = IlyasTvDarkOnError,
    errorContainer = IlyasTvDarkErrorContainer,
    onErrorContainer = IlyasTvDarkOnErrorContainer,
    background = IlyasTvDarkBackground,
    onBackground = IlyasTvDarkOnBackground,
    surface = IlyasTvDarkSurface,
    onSurface = IlyasTvDarkOnSurface,
    surfaceVariant = IlyasTvDarkSurfaceVariant,
    onSurfaceVariant = IlyasTvDarkOnSurfaceVariant,
    surfaceContainer = IlyasTvDarkSurfaceContainer,
    surfaceContainerHigh = IlyasTvDarkSurfaceContainerHigh,
    surfaceContainerHighest = IlyasTvDarkSurfaceContainerHighest,
    surfaceContainerLow = IlyasTvDarkSurfaceContainerLow,
    surfaceContainerLowest = IlyasTvDarkSurfaceContainerLowest,
    surfaceBright = IlyasTvDarkSurfaceBright,
    surfaceDim = IlyasTvDarkSurfaceDim,
    outline = IlyasTvDarkOutline,
    outlineVariant = IlyasTvDarkOutlineVariant,
)

val IlyasTvLightColorScheme = lightColorScheme(
    primary = IlyasTvLightPrimary,
    onPrimary = IlyasTvLightOnPrimary,
    primaryContainer = IlyasTvLightPrimaryContainer,
    onPrimaryContainer = IlyasTvLightOnPrimaryContainer,
    secondary = IlyasTvLightSecondary,
    onSecondary = IlyasTvLightOnSecondary,
    secondaryContainer = IlyasTvLightSecondaryContainer,
    onSecondaryContainer = IlyasTvLightOnSecondaryContainer,
    tertiary = IlyasTvLightTertiary,
    onTertiary = IlyasTvLightOnTertiary,
    tertiaryContainer = IlyasTvLightTertiaryContainer,
    onTertiaryContainer = IlyasTvLightOnTertiaryContainer,
    error = IlyasTvLightError,
    onError = IlyasTvLightOnError,
    errorContainer = IlyasTvLightErrorContainer,
    onErrorContainer = IlyasTvLightOnErrorContainer,
    background = IlyasTvLightBackground,
    onBackground = IlyasTvLightOnBackground,
    surface = IlyasTvLightSurface,
    onSurface = IlyasTvLightOnSurface,
    surfaceVariant = IlyasTvLightSurfaceVariant,
    onSurfaceVariant = IlyasTvLightOnSurfaceVariant,
    surfaceContainer = IlyasTvLightSurfaceContainer,
    surfaceContainerHigh = IlyasTvLightSurfaceContainerHigh,
    surfaceContainerHighest = IlyasTvLightSurfaceContainerHighest,
    surfaceContainerLow = IlyasTvLightSurfaceContainerLow,
    surfaceContainerLowest = IlyasTvLightSurfaceContainerLowest,
    surfaceBright = IlyasTvLightSurfaceBright,
    surfaceDim = IlyasTvLightSurfaceDim,
    outline = IlyasTvLightOutline,
    outlineVariant = IlyasTvLightOutlineVariant,
)

// ============================================================================
// 2. ÖNCEKİ TEMA RENK ŞEMALARI (KORUNAN KLASİK STREAMFLOW TEMASI)
// ============================================================================
val LegacyStreamFlowDarkColorScheme = darkColorScheme(
    primary = LegacyStreamFlowPrimary,
    onPrimary = LegacyStreamFlowOnPrimary,
    primaryContainer = LegacyStreamFlowPrimaryContainer,
    onPrimaryContainer = LegacyStreamFlowOnPrimaryContainer,
    secondary = LegacyStreamFlowSecondary,
    onSecondary = LegacyStreamFlowOnSecondary,
    secondaryContainer = LegacyStreamFlowSecondaryContainer,
    onSecondaryContainer = LegacyStreamFlowOnSecondaryContainer,
    tertiary = LegacyStreamFlowTertiary,
    onTertiary = LegacyStreamFlowOnTertiary,
    tertiaryContainer = LegacyStreamFlowTertiaryContainer,
    error = LegacyStreamFlowError,
    onError = LegacyStreamFlowOnError,
    errorContainer = LegacyStreamFlowErrorContainer,
    onErrorContainer = LegacyStreamFlowOnErrorContainer,
    background = LegacyStreamFlowBackground,
    onBackground = LegacyStreamFlowOnBackground,
    surface = LegacyStreamFlowSurface,
    onSurface = LegacyStreamFlowOnSurface,
    surfaceVariant = LegacyStreamFlowSurfaceVariant,
    onSurfaceVariant = LegacyStreamFlowOnSurfaceVariant,
    surfaceContainer = LegacyStreamFlowSurfaceContainer,
    surfaceContainerHigh = LegacyStreamFlowSurfaceContainerHigh,
    surfaceContainerHighest = LegacyStreamFlowSurfaceContainerHighest,
    surfaceContainerLow = LegacyStreamFlowSurfaceContainerLow,
    surfaceContainerLowest = LegacyStreamFlowSurfaceContainerLowest,
    surfaceBright = LegacyStreamFlowSurfaceBright,
    surfaceDim = LegacyStreamFlowSurfaceDim,
    outline = LegacyStreamFlowOutline,
    outlineVariant = LegacyStreamFlowOutlineVariant,
)

val LegacyStreamFlowLightColorScheme = lightColorScheme(
    primary = LegacyStreamFlowLightPrimary,
    onPrimary = LegacyStreamFlowLightOnPrimary,
    primaryContainer = LegacyStreamFlowLightPrimaryContainer,
    onPrimaryContainer = LegacyStreamFlowLightOnPrimaryContainer,
    secondary = LegacyStreamFlowLightSecondary,
    onSecondary = LegacyStreamFlowLightOnSecondary,
    secondaryContainer = LegacyStreamFlowLightSecondaryContainer,
    onSecondaryContainer = LegacyStreamFlowLightOnSecondaryContainer,
    tertiary = LegacyStreamFlowTertiary,
    onTertiary = LegacyStreamFlowOnTertiary,
    tertiaryContainer = LegacyStreamFlowTertiaryContainer,
    error = LegacyStreamFlowError,
    onError = LegacyStreamFlowOnError,
    errorContainer = LegacyStreamFlowErrorContainer,
    onErrorContainer = LegacyStreamFlowOnErrorContainer,
    background = LegacyStreamFlowLightBackground,
    onBackground = LegacyStreamFlowLightOnBackground,
    surface = LegacyStreamFlowLightSurface,
    onSurface = LegacyStreamFlowLightOnSurface,
    surfaceVariant = LegacyStreamFlowLightSurfaceVariant,
    onSurfaceVariant = LegacyStreamFlowLightOnSurfaceVariant,
    surfaceContainer = LegacyStreamFlowLightSurfaceContainer,
    surfaceContainerHigh = LegacyStreamFlowLightSurfaceContainerHigh,
    surfaceContainerHighest = LegacyStreamFlowLightSurfaceContainerHighest,
    surfaceContainerLow = LegacyStreamFlowLightSurfaceContainerLow,
    surfaceContainerLowest = LegacyStreamFlowLightSurfaceContainerLowest,
    surfaceBright = LegacyStreamFlowLightSurfaceBright,
    surfaceDim = LegacyStreamFlowLightSurfaceDim,
    outline = LegacyStreamFlowLightOutline,
    outlineVariant = LegacyStreamFlowLightOutlineVariant,
)

// Geriye dönük uyumluluk için referanslar
val StreamFlowDarkColorScheme = IlyasTvDarkColorScheme
val StreamFlowLightColorScheme = IlyasTvLightColorScheme

/**
 * Modern İlyasTV Ana Uygulama Teması Composable Fonksiyonu.
 * Material Design 3 renkleri, tipografi hiyerarşisi ve ekran boyutlandırma (Spacing)
 * sağlayıcısını içerir.
 */
@Composable
fun IlyasTvTheme(
    themeSetting: AppThemeSetting = AppThemeSetting.DARK,
    brandStyle: ThemeBrandStyle = activeThemeBrandStyle,
    content: @Composable () -> Unit,
) {
    val isDark = when (themeSetting) {
        AppThemeSetting.DARK -> true
        AppThemeSetting.LIGHT -> false
        AppThemeSetting.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = when (brandStyle) {
        ThemeBrandStyle.MODERN_ILYAS_TV -> if (isDark) IlyasTvDarkColorScheme else IlyasTvLightColorScheme
        ThemeBrandStyle.CLASSIC_STREAMFLOW -> if (isDark) LegacyStreamFlowDarkColorScheme else LegacyStreamFlowLightColorScheme
    }

    CompositionLocalProvider(
        LocalIlyasTvSpacing provides IlyasTvSpacing()
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

/**
 * Mevcut çağrıların aynen çalışmaya devam etmesi için uyumluluk katmanı
 */
@Composable
fun MyApplicationTheme(
    themeSetting: AppThemeSetting = AppThemeSetting.DARK,
    brandStyle: ThemeBrandStyle = activeThemeBrandStyle,
    content: @Composable () -> Unit,
) {
    IlyasTvTheme(
        themeSetting = themeSetting,
        brandStyle = brandStyle,
        content = content
    )
}



