package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AutomationReportsScreen(viewModel: DashboardViewModel) {
    var rawFileSize by remember { mutableStateOf("450 MB") }
    var useOriginalRaw by remember { mutableStateOf(true) }

    // Campos de Automatización
    var triggerWord by remember { mutableStateOf("") }
    var responseText by remember { mutableStateOf("") }

    // Campos de Calendario/Programación
    var selectedDay by remember { mutableStateOf(15) } // Día seleccionado por defecto para el calendario mensual
    var recipientName by remember { mutableStateOf("") }
    var scheduledText by remember { mutableStateOf("") }
    var calendarSelection by remember { mutableStateOf("Google Calendar") }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Para reporte
    var reportExportedMessage by remember { mutableStateOf("") }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // EXPLICACIÓN TITULAR
                item {
                    Text(
                        text = "Multimedia, Automatización & Reportes v3.2",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = viewModel.themeConfig.primaryColor
                    )
                    Text(
                        text = "Centraliza envíos originales Raw de 1GB, programa mensajes vinculados a calendarios externos y descarga reportes.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // MULTIMEDIA SIN LÍMITES CARD
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Share, contentDescription = null, tint = viewModel.themeConfig.secondaryColor)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Gestión Multimedia de Gran Tamaño (Hasta 1GB RAW)",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = viewModel.themeConfig.primaryColor
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "El cliente normal comprime imágenes y limita vídeos a 16MB. GBWhatsApp sube archivos de hasta 1GB directo a Firebase Storage con tokens unificados en la subcolección de logs sin pérdida de calidad (Bit-a-Bit RAW).",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = useOriginalRaw,
                                        onCheckedChange = { useOriginalRaw = it },
                                        colors = CheckboxDefaults.colors(checkedColor = viewModel.themeConfig.primaryColor)
                                    )
                                    Text("Calidad Original (Desactivar Compresión)", style = MaterialTheme.typography.bodySmall)
                                }

                                Text(
                                    text = "Ficheros: RAW / MKV / ISO",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = viewModel.themeConfig.secondaryColor
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        viewModel.sendMessage(
                                            content = "Adjunto planos arquitectónicos sin comprimir ($rawFileSize)",
                                            originalQuality = true,
                                            fileSize = rawFileSize
                                        )
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Transmitiendo planos v4.1 RAW directos a Storage...")
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = viewModel.themeConfig.secondaryColor)
                                ) {
                                    Text("Simular Envío Masivo ($rawFileSize)", fontSize = 11.sp, color = Color.White)
                                }

                                OutlinedButton(
                                    onClick = {
                                        rawFileSize = if (rawFileSize == "450 MB") "1.0 GB" else "450 MB"
                                    }
                                ) {
                                    Text("Cambiar Tamaño", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }

                // RESPUESTAS AUTOMÁTICAS
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Lógica de Respuestas Automáticas (Fuzzy Bot local)",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = viewModel.themeConfig.primaryColor
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "El motor intercepta Strings locales. Si coincide con una keyword, el receptor manda respuesta transcurrida una latencia simulada.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                TextField(
                                    value = triggerWord,
                                    onValueChange = { triggerWord = it },
                                    label = { Text("Si contiene...", fontSize = 10.sp) },
                                    modifier = Modifier.weight(1f)
                                )

                                TextField(
                                    value = responseText,
                                    onValueChange = { responseText = it },
                                    label = { Text("Responder...", fontSize = 10.sp) },
                                    modifier = Modifier.weight(1.5f)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = {
                                    if (triggerWord.isNotBlank() && responseText.isNotBlank()) {
                                        viewModel.addAutoReplyRule(triggerWord, responseText)
                                        triggerWord = ""
                                        responseText = ""
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Regla de respuesta agregada exitosamente.")
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = viewModel.themeConfig.primaryColor),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Guardar Regla de Auto-Respuesta", fontSize = 11.sp, color = Color.White)
                            }
                        }
                    }
                }

                // PROGRAMACIÓN DE MENSAJES & CALENDARIO EXTERNO
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Mensajes Programados & Calendarios Externos",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = viewModel.themeConfig.primaryColor
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Planifica tareas unificadas con Google Calendar o Outlook. Un Cron Job de Cloud Functions echa mano de los triggers listados cada minuto para gatillar notificaciones FCM.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Miniature Monthly Calendar View
                            Text(
                                text = "Junio 2026",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(0.5.dp, Color.LightGray, RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                                    .padding(8.dp)
                            ) {
                                // Dias de la semana
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    listOf("L", "M", "M", "J", "V", "S", "D").forEach { d ->
                                        Text(text = d, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(28.dp).wrapContentWidth())
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // Días simulados (Mes de Junio 2026 empieza el Lunes 1)
                                val days = (1..30).toList()
                                FlowRow(
                                    maxItemsInEachRow = 7,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    days.forEach { day ->
                                        val isSelected = selectedDay == day
                                        val hasTasks = day == 15 || day == 18
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(
                                                    if (isSelected) viewModel.themeConfig.primaryColor
                                                    else if (hasTasks) viewModel.themeConfig.secondaryColor.copy(alpha = 0.2f)
                                                    else Color.Transparent
                                                )
                                                .clickable { selectedDay = day }
                                                .wrapContentSize(Alignment.Center)
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(
                                                    text = day.toString(),
                                                    fontSize = 10.sp,
                                                    fontWeight = if (isSelected || hasTasks) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isSelected) Color.White else Color.Black
                                                )
                                                if (hasTasks && !isSelected) {
                                                    Box(modifier = Modifier.size(3.dp).clip(RoundedCornerShape(1.5f)).background(viewModel.themeConfig.secondaryColor))
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Tareas apuntadas para el día $selectedDay de Junio:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = viewModel.themeConfig.secondaryColor
                            )

                            // Listar mensajes programados
                            val dayText = if (selectedDay < 10) "0$selectedDay" else "$selectedDay"
                            val dayString = "$dayText/06/2026"
                            val matchingTasks = viewModel.scheduledMessages.value.filter { it.dateTime.startsWith(dayString) }

                            if (matchingTasks.isEmpty()) {
                                Text("Sin mensajes programados para esta fecha.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            } else {
                                matchingTasks.forEach { task ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(viewModel.themeConfig.secondaryColor.copy(alpha = 0.08f))
                                            .padding(6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("Destino: ${task.recipient}", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            Text("Ref: ${task.content}", fontSize = 10.sp, color = Color.DarkGray)
                                            Text("Integra: ${task.calendarType}", fontSize = 8.sp, color = viewModel.themeConfig.primaryColor)
                                        }
                                        IconButton(
                                            onClick = { viewModel.deleteScheduledMessage(task.id) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Formulario para añadir mensaje programado
                            Text("Añadir Mensaje Programado:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))

                            TextField(
                                value = recipientName,
                                onValueChange = { recipientName = it },
                                placeholder = { Text("Nombre de contacto/grupo", fontSize = 10.sp) },
                                modifier = Modifier.fillMaxWidth().height(44.dp),
                                textStyle = TextStyle(fontSize = 10.sp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            TextField(
                                value = scheduledText,
                                onValueChange = { scheduledText = it },
                                placeholder = { Text("Mensaje programado", fontSize = 10.sp) },
                                modifier = Modifier.fillMaxWidth().height(44.dp),
                                textStyle = TextStyle(fontSize = 10.sp)
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Calendars Choice
                                val calendars = listOf("Google Calendar", "Outlook Calendar")
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    calendars.forEach { cal ->
                                        val isCalSelected = calendarSelection == cal
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(if (isCalSelected) viewModel.themeConfig.primaryColor else Color.LightGray.copy(alpha = 0.4f))
                                                .clickable { calendarSelection = cal }
                                                .padding(horizontal = 6.dp, vertical = 4.dp)
                                        ) {
                                            Text(text = cal, fontSize = 8.sp, color = if (isCalSelected) Color.White else Color.Black)
                                        }
                                    }
                                }

                                Button(
                                    onClick = {
                                        if (recipientName.isNotBlank() && scheduledText.isNotBlank()) {
                                            viewModel.scheduleMessage(recipientName, scheduledText, "$dayString 12:00", calendarSelection)
                                            recipientName = ""
                                            scheduledText = ""
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("Programación guardada y sincronizada.")
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = viewModel.themeConfig.primaryColor),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("Agregar", fontSize = 10.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }

                // EXPORTADOR DE REPORTES
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = viewModel.themeConfig.primaryColor)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Exportación Analítica de Rendimiento",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = viewModel.themeConfig.primaryColor
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Extrae y renderiza el histórico de mensajería, rendimiento del bot de auto-respuesta y logs de ancho de banda multimedia.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Tabla de métricas simulada
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(0.5.dp, Color.Gray.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                    .padding(6.dp)
                            ) {
                                MetricRowSimulated("Total Mensajes Enviados", "1,250 msgs")
                                MetricRowSimulated("Intercepciones de Sabor Fantasma", "430 veces")
                                MetricRowSimulated("Transferencia Firebase RAW", "14.2 GB")
                                MetricRowSimulated("Tasa Desconexión Realtime DB", "98.5%")
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        reportExportedMessage = "Rep_Analitycs_GB_2026.csv generado correctamente. 512 líneas escritas."
                                        viewModel.exportedReportsCount++
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = viewModel.themeConfig.secondaryColor),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Exportar CSV", fontSize = 11.sp, color = Color.White)
                                }

                                Button(
                                    onClick = {
                                        reportExportedMessage = "Rep_Analitycs_GB_2026.pdf renderizado exitosamente con vector tables. listo para distribución."
                                        viewModel.exportedReportsCount++
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = viewModel.themeConfig.primaryColor),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Exportar PDF", fontSize = 11.sp, color = Color.White)
                                }
                            }

                            AnimatedVisibility(visible = reportExportedMessage.isNotEmpty()) {
                                Column(modifier = Modifier.padding(top = 8.dp)) {
                                    Text(
                                        text = "Resultado de Exportación:",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = viewModel.themeConfig.primaryColor
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color.Yellow.copy(alpha = 0.15f))
                                            .padding(6.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Info, contentDescription = null, tint = Color.DarkGray, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(text = reportExportedMessage, fontSize = 9.sp, color = Color.DarkGray)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricRowSimulated(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 9.sp, color = Color.Gray)
        Text(text = value, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
    }
}
