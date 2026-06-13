package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Estructura de modelo para el visor de Base de Datos
data class DbField(
    val key: String,
    val type: String,
    val description: String,
    val example: String
)

data class DbCollection(
    val name: String,
    val description: String,
    val primaryIdType: String,
    val iconName: String,
    val fields: List<DbField>,
    val subcollections: List<DbCollection> = emptyList()
)

@Composable
fun DataArchitectureScreen(viewModel: DashboardViewModel) {
    val schema = remember { getMockDatabaseSchema() }
    var selectedCollection by remember { mutableStateOf<DbCollection?>(schema.firstOrNull()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Info",
                    tint = viewModel.themeConfig.primaryColor,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Especificación de SQLite Room & Cloud Sync (METACHAT)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Colecciones milimétricas con mapas de privacidad avanzada y logs de auditoría.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Selector horizontal de colecciones
        Text(
            text = "Colecciones Principales",
            style = MaterialTheme.typography.labelLarge.copy(color = viewModel.themeConfig.primaryColor),
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            schema.forEach { coll ->
                val isSelected = selectedCollection?.name == coll.name
                val color = if (isSelected) viewModel.themeConfig.primaryColor else MaterialTheme.colorScheme.surface
                val textColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(color)
                        .clickable { selectedCollection = coll }
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = when(coll.name) {
                                "users" -> Icons.Default.Person
                                "chats" -> Icons.Default.Menu
                                "states" -> Icons.Default.PlayArrow
                                else -> Icons.Default.Info
                            },
                            contentDescription = coll.name,
                            tint = if (isSelected) Color.White else viewModel.themeConfig.primaryColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = coll.name.uppercase(),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            color = textColor
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Detalle de la colección seleccionada
        selectedCollection?.let { coll ->
            Column(modifier = Modifier.weight(1f)) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Colección: ",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "/${coll.name}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = viewModel.themeConfig.secondaryColor
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = coll.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Primary Key (ID): ${coll.primaryIdType}",
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Esquema de Atributos & Subcolecciones",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(coll.fields) { field ->
                        FieldListItem(field, viewModel.themeConfig.secondaryColor)
                    }

                    // Renderizar subcolecciones si existen
                    if (coll.subcollections.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Subcolección Anidada de /${coll.name}",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = viewModel.themeConfig.primaryColor
                                )
                            )
                        }

                        items(coll.subcollections) { subcoll ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "/${coll.name}/{id}/${subcoll.name}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = viewModel.themeConfig.secondaryColor
                                    )
                                    Text(
                                        text = subcoll.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )

                                    subcoll.fields.take(4).forEach { sf ->
                                        Row(
                                            modifier = Modifier.padding(vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = sf.key,
                                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "(${sf.type})",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = viewModel.themeConfig.primaryColor
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = sf.description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1
                                            )
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
fun FieldListItem(field: DbField, typeColor: Color) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        )
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (field.type.contains("map")) Icons.Default.Settings else Icons.Default.Info,
                        contentDescription = null,
                        tint = typeColor.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = field.key,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Text(
                    text = field.type,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        color = typeColor,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(typeColor.copy(alpha = 0.12f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = field.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    Spacer(modifier = Modifier.height(6.dp))
                    Row {
                        Text(
                            text = "Valor ejemplo: ",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = field.example,
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

fun getMockDatabaseSchema(): List<DbCollection> {
    return listOf(
        DbCollection(
            name = "users",
            description = "Contiene perfiles de usuario, metadatos de presencia real y mapas exhaustivos que determinan la privacidad avanzada en el lado del servidor y cliente.",
            primaryIdType = "userID (String)",
            iconName = "person",
            fields = listOf(
                DbField("phone", "string", "Número internacional del usuario para validar sesiones.", "+34600123456"),
                DbField("isOnline", "boolean", "Estado en tiempo real actualizado vía Realtime Database sincronizada.", "false"),
                DbField("lastSeen", "timestamp", "Timestamp estático real. No se altera externamente si está congelado.", "12/06/2026 10:15:33"),
                DbField("ghostSettings", "map (Settings)", "Parámetros avanzados deGBWhatsApp para Modo Fantasma y privacidad.", "{ freezeLastSeen: true, hideOnline: true }"),
                DbField("ghostSettings.freezeLastSeen", "boolean", "Switches de privacidad: si es true, interceptores ignoran actualización de lastSeen.", "true"),
                DbField("ghostSettings.hideBlueTicks", "boolean", "Retrasa la escritura de lecturas de mensajes (blue check) hasta la respuesta.", "false"),
                DbField("ghostSettings.hideSecondTick", "boolean", "No emite evento de entrega (delivered) al emisor.", "true"),
                DbField("autoReplySettings", "map (Reply)", "Reglas guardadas locales o sincronizadas para Bot de mensajería.", "{ active: true, responseDelaySeconds: 2 }")
            )
        ),
        DbCollection(
            name = "chats",
            description = "Canales de chats directos o grupales. Controla permisos de lectura y retención de eventos.",
            primaryIdType = "chatID (String)",
            iconName = "menu",
            fields = listOf(
                DbField("type", "string", "Determina si el chat es individual ('dm') o grupal ('group').", "group"),
                DbField("title", "string", "Nombre legible del grupo o alias.", "Grupo de Desarrollo"),
                DbField("createdAt", "timestamp", "Fecha exacta de creación del chat.", "10/05/2026 14:00:00"),
                DbField("rolesMap", "map (userID -> role)", "Roles técnicos de administración: 'owner', 'admin', 'member'.", "{ 'usr_912': 'admin' }"),
                DbField("antiDeletionActive", "boolean", "Activa retención persistente de mensajes borrados para este chat.", "true")
            ),
            subcollections = listOf(
                DbCollection(
                    name = "messages",
                    description = "Mensajería persistente con tags avanzados de auditoría, reenvíos sin límite y calidad de multimedia original.",
                    primaryIdType = "messageID (String)",
                    iconName = "mail",
                    fields = listOf(
                        DbField("senderId", "string", "ID del emisor del mensaje.", "usr_823"),
                        DbField("content", "string", "Cuerpo textual del mensaje o descripción multimedia.", "Hola a todos!"),
                        DbField("timestamp", "timestamp", "Fecha y hora real de guardado en el servidor.", "12/06/2026 10:22:15"),
                        DbField("isForwarded", "boolean", "Propiedad que define si el mensaje proviene de otro chat.", "false"),
                        DbField("forwardCounter", "int", "Contador acumulado de veces reenviado.", "1"),
                        DbField("statusMap", "map (userId -> status)", "Ticks en tiempo real para cada participante (sent/delivered/read)", "{ 'usr_912': 'delivered' }"),
                        DbField("isAuditTombstone", "boolean", "Flag de Anti-Eliminación: true si el emisor original ejecutó 'Borrar para todos'.", "true"),
                        DbField("originalRepliedMessage", "map (Ref)", "Referencia del mensaje respondido para árbol de conversación.", "{ msg_id: '4392' }"),
                        DbField("mediaMetadata", "map", "Campos multimedia para envíos de archivos RAW o de hasta 1GB sin compresión.", "{ filename: 'planos.raw', size: 850000000 }")
                    )
                )
            )
        ),
        DbCollection(
            name = "states",
            description = "Colección de Estados (Historias de 24h) con logs de borrado manual y retención post-expiración para Modo Fantasma.",
            primaryIdType = "statusID (String)",
            iconName = "play",
            fields = listOf(
                DbField("authorId", "string", "ID del usuario que publicó el estado.", "usr_823"),
                DbField("mediaUrl", "string", "Firebase Storage uri del material publicado.", "https://storage.googleapis/..."),
                DbField("publishedAt", "timestamp", "Hora del post.", "12/06/2026 01:00:00"),
                DbField("isManuallyDeleted", "boolean", "Permite que continúe visible a usuarios con Antieliminación activa aunque haya sido borrado.", "true"),
                DbField("viewsCount", "int", "Totalizador de visualizaciones registradas.", "15"),
                DbField("viewersExcludingGhost", "array [String]", "Visualizadores (Los usuarios en Modo Fantasma omiten escribir aquí).", "['usr_33', 'usr_81']")
            )
        )
    )
}
