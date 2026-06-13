package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun GhostModeScreen(viewModel: DashboardViewModel) {
    var senderInputText by remember { mutableStateOf("") }
    var senderIsTypingSimulated by remember { mutableStateOf(false) }
    var showTechLog by remember { mutableStateOf(true) }

    // Generar flujos de escritura automáticos simulados para la explicación
    LaunchedEffect(senderInputText) {
        if (senderInputText.isNotEmpty()) {
            senderIsTypingSimulated = true
            delay(1500)
            senderIsTypingSimulated = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // --- Controles de Modo Fantasma ---
        Text(
            text = "Modo Fantasma (Privacidad Avanzada)",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = viewModel.themeConfig.primaryColor
        )
        Text(
            text = "Simula cómo interactúa el cliente GBWhatsApp modificando los eventos en tiempo real.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Interruptores de Eventos",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Congelar Última Vez
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Congelar 'Última Vez' (Last Seen)", style = MaterialTheme.typography.bodyMedium)
                        Text("Mantiene un timestamp estático inmutable para los demás.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = viewModel.freezeLastSeen,
                        onCheckedChange = { viewModel.freezeLastSeen = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = viewModel.themeConfig.primaryColor)
                    )
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)

                // Invisible Online
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Invisible (Ocultar 'En línea')", style = MaterialTheme.typography.bodyMedium)
                        Text("Presencia desconectada en Realtime Database.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = viewModel.hideOnlineStatus,
                        onCheckedChange = { viewModel.hideOnlineStatus = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = viewModel.themeConfig.primaryColor)
                    )
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)

                // Ocultar Escribiendo/Grabando
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Ocultar 'Escribiendo...' / 'Grabando...'", style = MaterialTheme.typography.bodyMedium)
                        Text("Cancela el envío local de estados tipográficos temporales.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = viewModel.hideTypingStatus,
                        onCheckedChange = { viewModel.hideTypingStatus = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = viewModel.themeConfig.primaryColor)
                    )
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)

                // Ocultar Segundo Tick & Ticks Azules
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Ocultar Segundo Tick y Tick Azul", style = MaterialTheme.typography.bodyMedium)
                        Text("Sólo marca leído/entregado en la base de datos hasta que respondes.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = viewModel.hideBlueTick,
                        onCheckedChange = { 
                            viewModel.hideBlueTick = it
                            viewModel.hideSecondTick = it
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = viewModel.themeConfig.primaryColor)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Simulador De Teléfonos (Visualización Cruzada) ---
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Teléfono de la otra persona (Observadora María)
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = "VISTA DE MARÍA",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = viewModel.themeConfig.secondaryColor)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Tú status: ${if (viewModel.hideOnlineStatus) "Desconectado" else "En línea"}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Tú última vez: ${if (viewModel.freezeLastSeen) "Ayer a las 10:22 PM" else "Hoy a las 7:13 AM"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    if (senderIsTypingSimulated && !viewModel.hideTypingStatus) {
                        Text(
                            text = "✍️ Escribiendo...",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = viewModel.themeConfig.primaryColor,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    } else {
                        Text(
                            text = " ",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Chat simplificado de María
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFEFE7DE))
                    ) {
                        LazyColumn(
                            modifier = Modifier.padding(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            item {
                                ChatBubbleSimulated(
                                    text = "Hola! ¿Te llegó el plano?",
                                    isBack = true,
                                    time = "10:15",
                                    status = null,
                                    primaryColor = viewModel.themeConfig.primaryColor,
                                    bubbleSenderColor = viewModel.themeConfig.bubbleSenderColor,
                                    bubbleReceiverColor = viewModel.themeConfig.bubbleReceiverColor
                                )
                            }

                            item {
                                val showTicks = if (viewModel.hideSecondTick) MessageStatus.SENT else if (viewModel.hideBlueTick) MessageStatus.DELIVERED else MessageStatus.READ
                                ChatBubbleSimulated(
                                    text = "Sí, ya lo revisé.",
                                    isBack = false,
                                    time = "10:16",
                                    status = showTicks,
                                    primaryColor = viewModel.themeConfig.primaryColor,
                                    bubbleSenderColor = viewModel.themeConfig.bubbleSenderColor,
                                    bubbleReceiverColor = viewModel.themeConfig.bubbleReceiverColor
                                )
                            }
                        }
                    }
                }
            }

            // Teléfono Súper Usuario (Tú)
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                border = BorderStroke(1.dp, viewModel.themeConfig.primaryColor.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "VISTA DE TU CHAT",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = viewModel.themeConfig.primaryColor)
                        )
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.Green)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "María: En línea",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Ghost: Activo 🛡️",
                        style = MaterialTheme.typography.bodySmall,
                        color = viewModel.themeConfig.primaryColor
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Chat simplificado del usuario
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFEFE7DE))
                    ) {
                        Column(modifier = Modifier.padding(4.dp)) {
                            Box(modifier = Modifier.weight(1f)) {
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    item {
                                        ChatBubbleSimulated(
                                            text = "Hola! ¿Te llegó el plano?",
                                            isBack = true,
                                            time = "10:15",
                                            status = null,
                                            primaryColor = viewModel.themeConfig.primaryColor,
                                            bubbleSenderColor = viewModel.themeConfig.bubbleSenderColor,
                                            bubbleReceiverColor = viewModel.themeConfig.bubbleReceiverColor
                                        )
                                    }

                                    item {
                                        ChatBubbleSimulated(
                                            text = "Sí, ya lo revisé.",
                                            isBack = false,
                                            time = "10:16",
                                            status = MessageStatus.READ, // Yo siempre lo veo leído
                                            primaryColor = viewModel.themeConfig.primaryColor,
                                            bubbleSenderColor = viewModel.themeConfig.bubbleSenderColor,
                                            bubbleReceiverColor = viewModel.themeConfig.bubbleReceiverColor
                                        )
                                    }
                                }
                            }

                            // Input bar
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextField(
                                    value = senderInputText,
                                    onValueChange = { senderInputText = it },
                                    placeholder = { Text("Escribir...", fontSize = 10.sp) },
                                    modifier = Modifier.weight(1f).height(36.dp),
                                    textStyle = TextStyle(fontSize = 10.sp),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White
                                    )
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Button(
                                    onClick = {
                                        if (senderInputText.isNotEmpty()) {
                                            viewModel.sendMessage(senderInputText)
                                            senderInputText = ""
                                        }
                                    },
                                    modifier = Modifier.size(36.dp),
                                    contentPadding = PaddingValues(0.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = viewModel.themeConfig.primaryColor)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = "Send", modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Registro Log Explicativo Técnico ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = viewModel.themeConfig.darkBackground)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Detalle del Flujo Lógico en Servidor",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = Color.Green),
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = if (showTechLog) "Ocultar -" else "Ver +",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.White),
                        modifier = Modifier.clickable { showTechLog = !showTechLog }
                    )
                }

                if (showTechLog) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = getTechnicalGhostExplanation(
                            freeze = viewModel.freezeLastSeen,
                            hideOnline = viewModel.hideOnlineStatus,
                            hideTyping = viewModel.hideTypingStatus,
                            ticks = viewModel.hideBlueTick
                        ),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFFD4D4D4),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun ChatBubbleSimulated(
    text: String,
    isBack: Boolean,
    time: String,
    status: MessageStatus?,
    primaryColor: Color,
    bubbleSenderColor: Color,
    bubbleReceiverColor: Color
) {
    val align = if (isBack) Alignment.Start else Alignment.End
    val bgColor = if (isBack) bubbleReceiverColor else bubbleSenderColor

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalAlignment = align
    ) {
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 8.dp,
                        topEnd = 8.dp,
                        bottomStart = if (isBack) 0.dp else 8.dp,
                        bottomEnd = if (isBack) 8.dp else 0.dp
                    )
                )
                .background(bgColor)
                .border(0.5.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Column {
                Text(text = text, fontSize = 11.sp, color = Color.Black)
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = time,
                        fontSize = 8.sp,
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.Bottom)
                    )
                    if (status != null) {
                        Spacer(modifier = Modifier.width(3.dp))
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Tick",
                            tint = if (status == MessageStatus.READ) Color(0xFF34B7F1) else Color.Gray,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }
            }
        }
    }
}

fun getTechnicalGhostExplanation(freeze: Boolean, hideOnline: Boolean, hideTyping: Boolean, ticks: Boolean): String {
    return buildString {
        append("1. SUSCRIPCIÓN MÚLTIPLE DE PRESENCIA:\n")
        if (hideOnline) {
            append("   - El cliente intercepta el socket de Firebase Realtime Database. Al conectarse, NO ejerce la transacción setOnline() ni el '.onDisconnect()' en '/status/usr_823'. Permanece desconectado para el backend.\n")
        } else {
            append("   - Conexión normal. setOnline() escribe true en '/status/usr_823'. Notificación instantánea vía Firebase.\n")
        }

        append("\n2. CONTROL DE TIMESTAMP (LAST SEEN):\n")
        if (freeze) {
            append("   - Al activar 'Congelar', el cliente guarda el timestamp actual local. Cada envío de mensaje normal omitirá mutar '/users/usr_823/lastSeen' en Firestore, enviando un flag de bypass en cada transacción.\n")
        } else {
            append("   - El backend (Cloud Functions) actualiza /users/usr_823/lastSeen con un FieldValue.serverTimestamp() tras cada mensaje enviado.\n")
        }

        append("\n3. SEÑALES TIPOGRÁFICAS (ESCRIBIR / AUDIO):\n")
        if (hideTyping) {
            append("   - Intercepción de TextWatcher local. Se suprime el envío del evento transitorio '/chats/chat_12/statusMap/usr_823' = 'typing'. María no recibe señal socket alguna.\n")
        } else {
            append("   - TextWatcher normal emite evento debounced cada 1500ms al servidor para notificar 'escribiendo...'.\n")
        }

        append("\n4. DECOUPLED ACKNOWLEDGEMENT FLOW (TICKS):\n")
        if (ticks) {
            append("   - Al recibir María un mensaje, el cliente descarga el contenido pero NO escribe en el statusMap del mensaje ('read'/ 'delivered').\n")
            append("   - Solo cuando el receptor responde o clickea 'Enviar', el APK ejecuta una de escritura batch actualizando el tick retrasado a true en Firestore.")
        } else {
            append("   - Flujo automatizado: Al descargar -> Ticks de Entregado. Al ver -> Ticks Azules inmediatos en Firestore.")
        }
    }
}
