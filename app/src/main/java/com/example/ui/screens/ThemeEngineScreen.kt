package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Paletas de colores pre-configuradas para pruebas
data class ColorPalette(
    val name: String,
    val primary: Color,
    val secondary: Color,
    val bubbleSender: Color
)

@Composable
fun ThemeEngineScreen(viewModel: DashboardViewModel) {
    val presets = remember {
        listOf(
            ColorPalette("WhatsApp Clásico", Color(0xFF075E54), Color(0xFF128C7E), Color(0xFFE1FFC7)),
            ColorPalette("Freesia Yellow", Color(0xFFFBC02D), Color(0xFFF57F17), Color(0xFFFFF9C4)),
            ColorPalette("Teal Oceanic", Color(0xFF00796B), Color(0xFF004D40), Color(0xFFE0F2F1)),
            ColorPalette("Slate Minimalist", Color(0xFF37474F), Color(0xFF212121), Color(0xFFECEFF1)),
            ColorPalette("Crimson Stealth", Color(0xFFC62828), Color(0xFF8E0000), Color(0xFFFFEBEE)),
            ColorPalette("Cosmic Amethyst", Color(0xFF6A1B9A), Color(0xFF4A148C), Color(0xFFF3E5F5))
        )
    }

    var selectedStyle by remember { mutableStateOf(viewModel.themeConfig.bubbleStyle) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Motor de Temas y Estilos Dinámicos",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = viewModel.themeConfig.primaryColor
        )
        Text(
            text = "Prueba a cambiar colores e imitar cómo la app muta su árbol de de componentes basado en un esquema JSON local.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        // --- SELECCIONAR PALETAS ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Ajuste de Paleta de Colores",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.height(130.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(presets) { palette ->
                        val isSelected = viewModel.themeConfig.primaryColor == palette.primary
                        val borderCol = if (isSelected) viewModel.themeConfig.primaryColor else Color.Transparent

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .border(2.dp, borderCol, RoundedCornerShape(8.dp))
                                .clickable {
                                    viewModel.updatePrimaryColor(palette.primary)
                                    viewModel.updateSecondaryColor(palette.secondary)
                                    viewModel.updateBubbleSenderColor(palette.bubbleSender)
                                }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(palette.primary)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = palette.name,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- ESTILO DE BURBUJAS ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Estilo Visual del Globo de Chat",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val styles = listOf("Rounded M3", "WhatsApp Classic", "Brutalist (Borders)")
                    styles.forEach { st ->
                        val active = selectedStyle == st
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (active) viewModel.themeConfig.primaryColor
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable {
                                    selectedStyle = st
                                    viewModel.updateBubbleStyle(st)
                                }
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = st,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (active) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- JSON DINÁMICO REFLEJADO ---
        Text(
            text = "Payload JSON de Configuración (Tema local persistent)",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = viewModel.themeConfig.secondaryColor
        )
        Text(
            text = "Este JSON se guarda en SharedPreferences o Room locaux. El renderizador de Compose lo lee para inyectar los tokens sin compilar la app.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(6.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(containerColor = viewModel.themeConfig.darkBackground)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "gb_theme_config.json",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, color = Color.Green)
                    )
                    IconButton(
                        onClick = { /* Copiar simulación */ },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Build, contentDescription = "Copy", tint = Color.LightGray, modifier = Modifier.size(14.dp))
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Box(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = generateThemeJson(viewModel.themeConfig, selectedStyle),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = Color(0xFFD4D4D4),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

fun generateThemeJson(config: ThemeConfig, bubbleStyle: String): String {
    val primaryHex = String.format("#%06X", 0xFFFFFF and config.primaryColor.toArgb())
    val secHex = String.format("#%06X", 0xFFFFFF and config.secondaryColor.toArgb())
    val senderHex = String.format("#%06X", 0xFFFFFF and config.bubbleSenderColor.toArgb())
    val darkBg = String.format("#%06X", 0xFFFFFF and config.darkBackground.toArgb())

    return """{
  "theme_name": "GBWhatsApp_Custom_User",
  "meta": {
    "version": "4.2",
    "creator_id": "usr_823",
    "last_modified": "2026-06-12T07:13"
  },
  "colors": {
    "primary_color": "$primaryHex",
    "secondary_color": "$secHex",
    "background_dark": "$darkBg",
    "chat_bubble_sender": "$senderHex",
    "chat_bubble_receiver": "#FFFFFF",
    "notification_badge_tint": "$primaryHex",
    "navigation_pill_accent": "$secHex"
  },
  "styles": {
    "bubble_render_mode": "$bubbleStyle",
    "border_radius_bubbles_dp": 12,
    "font_profile": "Space Grotesk & JetBrains Mono",
    "custom_headers_height_dp": 56,
    "icon_notification_resource": "ic_gb_shield"
  },
  "flags": {
    "force_system_dark_integration": true,
    "support_dynamic_color_palette": false
  }
}"""
}
