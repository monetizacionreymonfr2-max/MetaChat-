package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun SecurityAntiDeleteScreen(viewModel: DashboardViewModel) {
    val messagesState by viewModel.messages.collectAsStateWithLifecycle()
    var isStateDeletedSimulated by remember { mutableStateOf(false) }
    var activeStateContent by remember { mutableStateOf("¡Disfrutando de la playa! 🏝️") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Seguridad e Inmunidad Anti-Eliminación",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = viewModel.themeConfig.primaryColor
        )
        Text(
            text = "Previene que otros usuarios borren rastros de mensajes o estados de tu pantalla.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        // --- Demo 1: ANTI-ELIMINACIÓN DE MENSAJES ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Prueba 1: Anti-Eliminación de Mensajes",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = viewModel.themeConfig.secondaryColor
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Lanza un mensaje entrante simulado desde el servidor y haz que el autor use 'Eliminar para todos'. Verás cómo tu APK lo intercepta conservándolo.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.simulateMessageReception() },
                        colors = ButtonDefaults.buttonColors(containerColor = viewModel.themeConfig.primaryColor),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("1. Recibir Mensaje", fontSize = 11.sp, color = Color.White)
                    }

                    Button(
                        onClick = { viewModel.simulateSenderDeletesLastMessage() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("2. Simular Borrado", fontSize = 11.sp, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Chat local con inmunidad
                Text(
                    text = "Historial local con Escudo Antidelete:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFFFAF3))
                        .border(1.dp, Color.LightGray.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(messagesState) { msg ->
                            val isMe = msg.sender == "Tú"
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (msg.isDeletedBySender) Color(0xFFFFECEF)
                                            else if (isMe) viewModel.themeConfig.bubbleSenderColor
                                            else viewModel.themeConfig.bubbleReceiverColor
                                        )
                                        .border(
                                            width = if (msg.isDeletedBySender) 1.dp else 0.dp,
                                            color = if (msg.isDeletedBySender) Color.Red.copy(alpha = 0.5f) else Color.Transparent,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .padding(8.dp)
                                ) {
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "${msg.sender}: ",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = if (isMe) viewModel.themeConfig.primaryColor else viewModel.themeConfig.secondaryColor
                                            )
                                            if (msg.isDeletedBySender) {
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(Color.Red.copy(alpha = 0.15f))
                                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red, modifier = Modifier.size(10.dp))
                                                        Spacer(modifier = Modifier.width(2.dp))
                                                        Text("ANTI-DEV", fontSize = 7.sp, color = Color.Red, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = if (msg.isDeletedBySender) "${msg.content} (Original)" else msg.content,
                                            fontSize = 11.sp,
                                            color = if (msg.isDeletedBySender) Color.DarkGray else Color.Black
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Demo 2: ANTI-ELIMINACIÓN DE ESTADOS ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Prueba 2: Anti-Eliminación de Estados (Stories)",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = viewModel.themeConfig.secondaryColor
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Los estados de 24h normalmente expiran o se borran. Un cliente regular los oculta de inmediato. GBWhatsApp los retiene en el caché local de base de datos.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Círculo de estado
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isStateDeletedSimulated) Color.Gray.copy(alpha = 0.3f)
                                    else viewModel.themeConfig.secondaryColor
                                )
                                .padding(3.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "🏖️", fontSize = 20.sp)
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Estado de María",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isStateDeletedSimulated) "Borrado por María en el Servidor" else "Activo (Publicado)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isStateDeletedSimulated) Color.Red else Color.Gray
                                )
                                if (isStateDeletedSimulated) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("🛡️ Visible para ti", style = MaterialTheme.typography.labelSmall.copy(color = viewModel.themeConfig.primaryColor, fontWeight = FontWeight.Bold))
                                }
                            }
                        }
                    }

                    Button(
                        onClick = { isStateDeletedSimulated = !isStateDeletedSimulated },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isStateDeletedSimulated) viewModel.themeConfig.primaryColor else Color.Red
                        )
                    ) {
                        Text(
                            text = if (isStateDeletedSimulated) "Restaurar" else "María Borra Estado",
                            fontSize = 10.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Flujo técnico Anti-Borrado ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = viewModel.themeConfig.primaryColor, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Explicación de Flujo: Anti-Borrado",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = viewModel.themeConfig.primaryColor
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Cuando el emisor ejecuta 'Borrar para todos', Firebase Firestore recibe un update de campos con isAuditTombstone=true. El cliente normal ve este flag y sobreescribe el texto por 'Mensaje eliminado'. El motor de GBWhatsApp detecta isAuditTombstone pero ignora la directiva de repintar la UI a blanco. Mantiene el contenido original cargado de SQLite y le añade un indicador visual rojo para que el usuario sepa que fue borrado.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }
    }
}
