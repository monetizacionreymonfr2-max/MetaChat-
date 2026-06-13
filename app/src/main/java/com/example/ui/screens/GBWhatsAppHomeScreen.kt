package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.net.Uri

// Modelo de datos para las conversaciones del screenshot
data class ChatItem(
    val senderName: String,
    val lastMessage: String,
    val timestamp: String,
    val unreadCount: Int = 0,
    val hasTicks: Boolean = true,
    val ticksStatus: MessageStatus = MessageStatus.READ, // SENT (1 check gris), DELIVERED (2 check gris), READ (2 check azul)
    val reactionEmoji: String? = null,
    val isGroup: Boolean = false,
    val avatarEmoji: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GBWhatsAppHomeScreen(viewModel: DashboardViewModel) {
    val context = LocalContext.current
    var permissionGranted by remember {
        mutableStateOf(
            androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.READ_CONTACTS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        permissionGranted = isGranted
        if (isGranted) {
            val imported = viewModel.importDeviceContacts(context)
            if (imported > 0) {
                Toast.makeText(context, "¡Éxito! Se importaron $imported contactos reales.", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "Agenda sincronizada. No se encontraron contactos nuevos.", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(context, "GBWhatsApp necesita permiso de contactos para importar tu lista.", Toast.LENGTH_LONG).show()
        }
    }

    val isDark = viewModel.themeConfig.forceDark
    val primaryColor = viewModel.themeConfig.primaryColor
    val backgroundColor = if (isDark) viewModel.themeConfig.darkBackground else Color(0xFFF4F5F7)
    val surfaceColor = if (isDark) Color(0xFF1F2C34) else Color.White
    val onBgColor = if (isDark) Color.White else Color(0xFF111B21)
    val subTextColor = if (isDark) Color(0xFF8696A0) else Color(0xFF667781)

    // Estados de navegación e interacción
    var selectedTab by remember { mutableStateOf(0) } // 0: Chats, 1: Grupos, 2: Novedades, 3: Llamadas, 4: Comunidades
    var activeFilter by remember { mutableStateOf("Todos") }
    var searchQuery by remember { mutableStateOf("") }
    var showMenuDropdown by remember { mutableStateOf(false) }

    // Overlays interactivos
    var showConfigDashboard by remember { mutableStateOf(false) }
    var activeDashboardTab by remember { mutableStateOf(0) }
    var activeChatDetail by remember { mutableStateOf<DynamicChat?>(null) }
    var showGhostModeInfo by remember { mutableStateOf(false) }
    var showNewChatDialog by remember { mutableStateOf(false) }

    // Generar la lista de chats dinámicos reales del servidor/ViewModel
    val chatsList by viewModel.chats.collectAsState()

    // Filtrar chats según búsqueda o pill
    val filteredChats = chatsList.filter {
        val matchesSearch = it.senderName.contains(searchQuery, ignoreCase = true) || it.lastMessage.contains(searchQuery, ignoreCase = true)
        val matchesFilter = when (activeFilter) {
            "Todos" -> true
            "No leídos" -> it.unreadCount > 0
            "Favoritos" -> it.senderName.contains("Madre", ignoreCase = true) || it.senderName.contains("Hijos", ignoreCase = true)
            "Grupos" -> it.isGroup || it.senderName.contains("Hijos", ignoreCase = true)
            else -> true
        }
        matchesSearch && matchesFilter
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isDark) Color(0xFF1F2C34) else primaryColor)
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                // Fila Superior: Logo y Controles Clásicos de GBWhatsApp
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "GBWhatsApp",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // 1. Wifi / DND Mode (Modo Avión de GB)
                        Text(
                            text = if (viewModel.hideOnlineStatus) "📶" else "🌐",
                            fontSize = 18.sp,
                            modifier = Modifier
                                .clickable {
                                    viewModel.hideOnlineStatus = !viewModel.hideOnlineStatus
                                    viewModel.hideTypingStatus = viewModel.hideOnlineStatus
                                }
                                .padding(2.dp)
                        )

                        // 2. Sun/Moon Toggle Theme
                        Text(
                            text = if (isDark) "☀️" else "🌙",
                            fontSize = 18.sp,
                            modifier = Modifier
                                .clickable {
                                    viewModel.updateForceDark(!isDark)
                                }
                                .padding(2.dp)
                        )

                        // 3. Ghost Mode Toggle
                        Text(
                            text = if (viewModel.freezeLastSeen) "👻" else "👤",
                            fontSize = 18.sp,
                            modifier = Modifier
                                .clickable {
                                    viewModel.freezeLastSeen = !viewModel.freezeLastSeen
                                    showGhostModeInfo = true
                                }
                                .padding(2.dp)
                        )

                        // 4. Cámara
                        Icon(
                            imageVector = Icons.Default.PlayArrow, // Simulación de icono / cámara
                            contentDescription = "Cámara",
                            tint = Color.White,
                            modifier = Modifier
                                .size(20.dp)
                                .clickable {
                                    chatsList.getOrNull(5)?.let {
                                        viewModel.selectChat(it.id)
                                        activeChatDetail = it
                                    }
                                }
                        )

                        // 5. Menú contextual tres puntitos
                        Box {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Menú",
                                tint = Color.White,
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable { showMenuDropdown = true }
                            )

                            DropdownMenu(
                                expanded = showMenuDropdown,
                                onDismissRequest = { showMenuDropdown = false },
                                modifier = Modifier.background(surfaceColor)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("🛠️ Configuración GB", color = onBgColor, fontWeight = FontWeight.Bold) },
                                    onClick = {
                                        showConfigDashboard = true
                                        showMenuDropdown = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("👤 Mi Perfil (${viewModel.profileName})", color = onBgColor) },
                                    onClick = {
                                        showMenuDropdown = false
                                        viewModel.loginStep = DashboardViewModel.LoginStep.PROFILE_SETUP
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("🔒 Privacidad / Ticks", color = onBgColor) },
                                    onClick = {
                                        showConfigDashboard = true
                                        activeDashboardTab = 1
                                        showMenuDropdown = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("🎨 Motor de Temas", color = onBgColor) },
                                    onClick = {
                                        showConfigDashboard = true
                                        activeDashboardTab = 3
                                        showMenuDropdown = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("📱 Sincronizar Contactos", color = onBgColor) },
                                    onClick = {
                                        showMenuDropdown = false
                                        if (permissionGranted) {
                                            val imported = viewModel.importDeviceContacts(context)
                                            if (imported > 0) {
                                                Toast.makeText(context, "Sincronizado: Se importaron $imported contactos reales.", Toast.LENGTH_LONG).show()
                                            } else {
                                                Toast.makeText(context, "Lista de contactos ya sincronizada al 100%.", Toast.LENGTH_SHORT).show()
                                            }
                                        } else {
                                            launcher.launch(android.Manifest.permission.READ_CONTACTS)
                                        }
                                    }
                                )
                                Divider(color = subTextColor.copy(alpha = 0.3f))
                                DropdownMenuItem(
                                    text = { Text("🚪 Cerrar Sesión", color = Color.Red, fontWeight = FontWeight.Bold) },
                                    onClick = {
                                        showMenuDropdown = false
                                        viewModel.saveLoginState(false, "", "", "", "")
                                        viewModel.loginStep = DashboardViewModel.LoginStep.WELCOME
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Fila Central: Barra de búsqueda redondeada `Buscar...` igual al screenshot
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(if (isDark) Color(0xFF2A3942) else Color.White.copy(alpha = 0.9f))
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar",
                        tint = if (isDark) Color(0xFF8696A0) else Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Buscar...", fontSize = 15.sp, color = if (isDark) Color(0xFF8696A0) else Color.Gray) },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        singleLine = true,
                        textStyle = TextStyle(
                            color = if (isDark) Color.White else Color.Black,
                            fontSize = 15.sp
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Fila Inferior: Chips para filtrar ("Todos", "No leídos", "Favoritos", "Grupos") con "+" chip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val filterOptions = listOf("Todos", "No leídos", "Favoritos", "Grupos")
                    filterOptions.forEach { filter ->
                        val isSelected = activeFilter == filter
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (isSelected) {
                                        if (isDark) Color(0xFF00A884).copy(alpha = 0.25f) else Color.White.copy(alpha = 0.3f)
                                    } else {
                                        if (isDark) Color(0xFF2A3942) else Color.White.copy(alpha = 0.15f)
                                    }
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) {
                                        if (isDark) Color(0xFF00A884) else Color.White
                                    } else Color.Transparent,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable { activeFilter = filter }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = filter,
                                color = if (isSelected) {
                                    if (isDark) Color(0xFF00A884) else Color.White
                                } else {
                                    if (isDark) Color(0xFF8696A0) else Color.White.copy(alpha = 0.8f)
                                },
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }

                    // Botón "+" rápido al final de los pills
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Color(0xFF2A3942) else Color.White.copy(alpha = 0.15f))
                            .clickable {
                                activeFilter = "Todos"
                                showConfigDashboard = true
                                activeDashboardTab = 0
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("+", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        bottomBar = {
            // M3 Bottom Navigation Bar exacto con insets automáticos
            NavigationBar(
                containerColor = if (isDark) Color(0xFF1F2C34) else Color.White,
                tonalElevation = 8.dp,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                // Chats
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        Box {
                            Icon(Icons.Default.Menu, contentDescription = "Chats")
                            // Pequeño globtito detector de no leídos
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 6.dp, y = (-2).dp)
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF25D366)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("1", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    },
                    label = { Text("Chats", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = if (isDark) Color(0xFF00A884) else primaryColor,
                        selectedTextColor = if (isDark) Color(0xFF00A884) else primaryColor,
                        unselectedIconColor = subTextColor,
                        unselectedTextColor = subTextColor,
                        indicatorColor = (if (isDark) Color(0xFF00A884) else primaryColor).copy(alpha = 0.12f)
                    )
                )

                // Grupos
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Face, contentDescription = "Grupos") },
                    label = { Text("Grupos", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = if (isDark) Color(0xFF00A884) else primaryColor,
                        selectedTextColor = if (isDark) Color(0xFF00A884) else primaryColor,
                        unselectedIconColor = subTextColor,
                        unselectedTextColor = subTextColor,
                        indicatorColor = (if (isDark) Color(0xFF00A884) else primaryColor).copy(alpha = 0.12f)
                    )
                )

                // Novedades
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Star, contentDescription = "Novedades") },
                    label = { Text("Novedades", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = if (isDark) Color(0xFF00A884) else primaryColor,
                        selectedTextColor = if (isDark) Color(0xFF00A884) else primaryColor,
                        unselectedIconColor = subTextColor,
                        unselectedTextColor = subTextColor,
                        indicatorColor = (if (isDark) Color(0xFF00A884) else primaryColor).copy(alpha = 0.12f)
                    )
                )

                // Llamadas
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Call, contentDescription = "Llamadas") },
                    label = { Text("Llamadas", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = if (isDark) Color(0xFF00A884) else primaryColor,
                        selectedTextColor = if (isDark) Color(0xFF00A884) else primaryColor,
                        unselectedIconColor = subTextColor,
                        unselectedTextColor = subTextColor,
                        indicatorColor = (if (isDark) Color(0xFF00A884) else primaryColor).copy(alpha = 0.12f)
                    )
                )

                // Comunidades
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = { Icon(Icons.Default.Share, contentDescription = "Comunidades") },
                    label = { Text("Comunidades", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = if (isDark) Color(0xFF00A884) else primaryColor,
                        selectedTextColor = if (isDark) Color(0xFF00A884) else primaryColor,
                        unselectedIconColor = subTextColor,
                        unselectedTextColor = subTextColor,
                        indicatorColor = (if (isDark) Color(0xFF00A884) else primaryColor).copy(alpha = 0.12f)
                    )
                )
            }
        },
        floatingActionButton = {
            // FAB Verde Redondeado WhatsApp "+"
            FloatingActionButton(
                onClick = {
                    showNewChatDialog = true
                },
                containerColor = Color(0xFF25D366),
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .padding(bottom = 8.dp, end = 8.dp)
                    .size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Nuevo Chat o Comando",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { innerPadding ->
        // Contenido Principal
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(innerPadding)
        ) {
            if (filteredChats.isEmpty()) {
                // Empty state con guía
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("👻", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No se encontraron chats que coincidan con la búsqueda.",
                        fontSize = 14.sp,
                        color = subTextColor,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(filteredChats) { chat ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.selectChat(chat.id)
                                    activeChatDetail = chat
                                    viewModel.forceReadTicks()
                                }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Avatar con color de iniciales
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(if (isDark) Color(0xFF2A3942) else Color(0xFFE9EBED)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = chat.avatarEmoji, fontSize = 24.sp)
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            // Nombre, Detalles y Mensaje
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = chat.senderName,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = onBgColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = chat.timestamp,
                                        fontSize = 12.sp,
                                        color = if (chat.unreadCount > 0) Color(0xFF25D366) else subTextColor,
                                        fontWeight = if (chat.unreadCount > 0) FontWeight.Bold else FontWeight.Normal
                                    )
                                }

                                Spacer(modifier = Modifier.height(3.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Ticks (Double check gris o azul)
                                    if (chat.hasTicks) {
                                        val tickColor = if (chat.ticksStatus == MessageStatus.READ) Color(0xFF53BDEB) else subTextColor
                                        Text(
                                            text = "✔✔ ",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = tickColor
                                        )
                                    }

                                    // Mensaje corto
                                    Text(
                                        text = chat.lastMessage,
                                        fontSize = 14.sp,
                                        color = subTextColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )

                                    // Globito verde de no leído o reacción
                                    if (chat.unreadCount > 0) {
                                        Box(
                                            modifier = Modifier
                                                .size(18.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF25D366)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = chat.unreadCount.toString(),
                                                fontSize = 10.sp,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Divider(
                            color = subTextColor.copy(alpha = 0.15f),
                            modifier = Modifier.padding(start = 78.dp, end = 14.dp)
                        )
                    }
                }
            }
        }
    }

    // --- DIALOG: INFO GHOST MODE ---
    if (showGhostModeInfo) {
        AlertDialog(
            onDismissRequest = { showGhostModeInfo = false },
            title = { Text("Modo Fantasma de GBWhatsApp", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Has ${if (viewModel.freezeLastSeen) "ACTIVADO" else "DESACTIVADO"} el Modo Fantasma.\n\n" +
                            "Esto congelará tu última conexión de manera ficticia y desactivará las notificaciones de 'En línea' para todos tus contactos de forma asíncrona."
                )
            },
            confirmButton = {
                TextButton(onClick = { showGhostModeInfo = false }) {
                    Text("ENTENDIDO", color = primaryColor, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // --- DIALOG: CREAR NUEVO CHAT ---
    if (showNewChatDialog) {
        var newChatName by remember { mutableStateOf("") }
        var newChatPhone by remember { mutableStateOf("") }
        var newChatAvatar by remember { mutableStateOf("👩") }
        var isNewChatGroup by remember { mutableStateOf(false) }

        val avatarOptions = listOf("👩", "👱‍♀️", "👩‍🦰", "🧒", "👦", "👨", "🏪", "📱", "💼", "🤖")

        AlertDialog(
            onDismissRequest = { showNewChatDialog = false },
            title = { Text("Nueva Conversación / Importar", fontWeight = FontWeight.Bold, color = onBgColor) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Botón para sincronizar agenda de contactos reales
                    Button(
                        onClick = {
                            if (permissionGranted) {
                                val imported = viewModel.importDeviceContacts(context)
                                if (imported > 0) {
                                    Toast.makeText(context, "Sincronizado: Se importaron $imported contactos reales.", Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(context, "Agenda sincronizada. No se encontraron nuevos contactos.", Toast.LENGTH_SHORT).show()
                                }
                                showNewChatDialog = false
                            } else {
                                launcher.launch(android.Manifest.permission.READ_CONTACTS)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Filled.Person, contentDescription = "Sincronizar", tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sincronizar Agenda Real", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = subTextColor.copy(alpha = 0.2f))

                    Text(
                        text = "O agrega un contacto manual asignándole su teléfono real:",
                        style = MaterialTheme.typography.bodySmall,
                        color = subTextColor
                    )

                    OutlinedTextField(
                        value = newChatName,
                        onValueChange = { newChatName = it },
                        label = { Text("Nombre del Contacto o Grupo") },
                        placeholder = { Text("Ej. Juan Pérez") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newChatPhone,
                        onValueChange = { newChatPhone = it },
                        label = { Text("Número de Teléfono (Opcional)") },
                        placeholder = { Text("Ej. +584121234567") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Seleccionar Avatar Emoji:",
                        style = MaterialTheme.typography.labelMedium,
                        color = onBgColor,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        avatarOptions.forEach { emoji ->
                            val isSelected = newChatAvatar == emoji
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) primaryColor else Color.Transparent)
                                    .clickable { newChatAvatar = emoji }
                                    .border(1.dp, if (isSelected) primaryColor else Color.Gray.copy(alpha = 0.5f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = emoji, fontSize = 16.sp)
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = isNewChatGroup,
                            onCheckedChange = { isNewChatGroup = it }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("¿Es un grupo de chat?", color = onBgColor)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newChatName.isNotBlank()) {
                            viewModel.createNewChat(
                                senderName = newChatName,
                                avatarEmoji = newChatAvatar,
                                isGroup = isNewChatGroup,
                                phoneNumber = newChatPhone
                            )
                            showNewChatDialog = false
                        }
                    }
                ) {
                    Text("CREAR CHAT", color = primaryColor, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewChatDialog = false }) {
                    Text("CANCELAR", color = Color.Gray)
                }
            },
            properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
        )
    }

    // --- DIALOG: SIMULADOR DE CHAT INTERACTIVO (WhatsApp Original) ---
    if (activeChatDetail != null) {
        val chat = chatsList.find { it.id == activeChatDetail!!.id } ?: activeChatDetail!!
        Dialog(
            onDismissRequest = { activeChatDetail = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = backgroundColor
            ) {
                // Sincronizar logs del chat actual
                val messageLogs by viewModel.messages.collectAsState()
                var textInput by remember { mutableStateOf("") }

                Column(modifier = Modifier.fillMaxSize()) {
                    // Header de Chat estilo WhatsApp
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (isDark) Color(0xFF1F2C34) else primaryColor)
                            .windowInsetsPadding(WindowInsets.statusBars)
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { activeChatDetail = null }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = Color.White)
                        }

                        // Avatar del destinatario
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = chat.avatarEmoji, fontSize = 20.sp)
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = chat.senderName,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (viewModel.hideOnlineStatus) "últ. vez hoy 07:13" else "En línea",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 11.sp
                            )
                        }

                        // Iconos rápidos WhatsApp
                        Text(
                            text = "👻 ",
                            fontSize = 18.sp,
                            modifier = Modifier
                                .clickable {
                                    viewModel.freezeLastSeen = !viewModel.freezeLastSeen
                                }
                                .padding(4.dp)
                        )
                        if (chat.phoneNumber.isNotBlank()) {
                            IconButton(onClick = {
                                val cleanNumber = chat.phoneNumber.replace(Regex("[^0-9]"), "")
                                try {
                                    val uri = Uri.parse("https://api.whatsapp.com/send?phone=$cleanNumber")
                                    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                                        setPackage("com.whatsapp")
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    try {
                                        val uri = Uri.parse("https://api.whatsapp.com/send?phone=$cleanNumber")
                                        val intent = Intent(Intent.ACTION_VIEW, uri)
                                        context.startActivity(intent)
                                    } catch (ex: Exception) {
                                        Toast.makeText(context, "No se pudo abrir WhatsApp.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }) {
                                Icon(Icons.Default.Call, contentDescription = "Abrir WhatsApp Real", tint = Color.White)
                            }
                        }
                        IconButton(onClick = { viewModel.simulateMessageReception() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Simular recibir", tint = Color.White)
                        }
                    }

                    // Fondo de chat clasificado WhatsApp
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(if (isDark) Color(0xFF0B141A) else Color(0xFFEFE7DE))
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp)
                        ) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "🔒 Las de llamadas y mensajes de este chat están cifradas de extremo a extremo.",
                                        fontSize = 11.sp,
                                        color = if (isDark) Color.Yellow.copy(alpha = 0.8f) else Color.Gray,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isDark) Color(0xFF1E262C) else Color(0xFFFFFDDF))
                                            .padding(6.dp),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            items(messageLogs) { msg ->
                                val isMe = msg.sender == "Tú"
                                val bubbleBg = if (isMe) viewModel.themeConfig.bubbleSenderColor else viewModel.themeConfig.bubbleReceiverColor
                                val alignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    contentAlignment = alignment
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .widthIn(max = 280.dp)
                                            .clip(
                                                RoundedCornerShape(
                                                    topStart = 12.dp,
                                                    topEnd = 12.dp,
                                                    bottomStart = if (isMe) 12.dp else 0.dp,
                                                    bottomEnd = if (isMe) 0.dp else 12.dp
                                                )
                                            )
                                            .background(bubbleBg)
                                            .padding(8.dp)
                                    ) {
                                        Text(
                                            text = msg.content,
                                            fontSize = 14.sp,
                                            color = Color.Black
                                        )
                                        Row(
                                            modifier = Modifier.align(Alignment.End),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = msg.timestamp,
                                                fontSize = 9.sp,
                                                color = Color.DarkGray
                                            )
                                            if (isMe) {
                                                Spacer(modifier = Modifier.width(3.dp))
                                                val tickText = when (msg.status) {
                                                    MessageStatus.SENT -> "✔"
                                                    MessageStatus.DELIVERED -> "✔✔"
                                                    MessageStatus.READ -> "✔✔"
                                                }
                                                val tickColor = if (msg.status == MessageStatus.READ) Color(0xFF53BDEB) else Color.Gray
                                                Text(text = tickText, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = tickColor)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Barra de Envío WhatsApp
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = textInput,
                            onValueChange = { textInput = it },
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(24.dp)),
                            placeholder = { Text("Escribe un mensaje...") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = surfaceColor,
                                unfocusedContainerColor = surfaceColor,
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        if (chat.phoneNumber.isNotBlank() && textInput.isNotBlank()) {
                            IconButton(
                                onClick = {
                                    val cleanNumber = chat.phoneNumber.replace(Regex("[^0-9]"), "")
                                    try {
                                        val uri = Uri.parse("https://api.whatsapp.com/send?phone=$cleanNumber&text=${Uri.encode(textInput)}")
                                        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                                            setPackage("com.whatsapp")
                                        }
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        try {
                                            val uri = Uri.parse("https://api.whatsapp.com/send?phone=$cleanNumber&text=${Uri.encode(textInput)}")
                                            val intent = Intent(Intent.ACTION_VIEW, uri)
                                            context.startActivity(intent)
                                        } catch (ex: Exception) {
                                            Toast.makeText(context, "No se pudo abrir WhatsApp.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .size(46.dp)
                                    .background(Color(0xFF25D366), CircleShape)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = "Enviar por WhatsApp real", tint = Color.White)
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        FloatingActionButton(
                            onClick = {
                                if (textInput.isNotBlank()) {
                                    viewModel.sendMessage(textInput)
                                    textInput = ""
                                }
                            },
                            containerColor = primaryColor,
                            shape = CircleShape,
                            modifier = Modifier.size(46.dp)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Enviar", tint = Color.White)
                        }
                    }
                }
            }
        }
    }

    // --- DIALOG: CONFIGURACIÓN INTEGRADA AVANZADA (Con las 5 pestañas de control original!) ---
    if (showConfigDashboard) {
        Dialog(
            onDismissRequest = { showConfigDashboard = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = backgroundColor
            ) {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Column {
                                    Text("Motores GBWhatsApp Engine v3.2", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    Text("Estructura de Datos, Inmunidad & Diseño", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                                }
                            },
                            navigationIcon = {
                                IconButton(onClick = { showConfigDashboard = false }) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = Color.White)
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(containerColor = primaryColor)
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = surfaceColor,
                            modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                        ) {
                            NavigationBarItem(
                                selected = activeDashboardTab == 0,
                                onClick = { activeDashboardTab = 0 },
                                icon = { Icon(Icons.Default.List, contentDescription = "BD") },
                                label = { Text("Estructura", fontSize = 10.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.White,
                                    selectedTextColor = primaryColor,
                                    indicatorColor = primaryColor
                                )
                            )

                            NavigationBarItem(
                                selected = activeDashboardTab == 1,
                                onClick = { activeDashboardTab = 1 },
                                icon = { Icon(Icons.Default.Lock, contentDescription = "Fantasma") },
                                label = { Text("Fantasma", fontSize = 10.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.White,
                                    selectedTextColor = primaryColor,
                                    indicatorColor = primaryColor
                                )
                            )

                            NavigationBarItem(
                                selected = activeDashboardTab == 2,
                                onClick = { activeDashboardTab = 2 },
                                icon = { Icon(Icons.Default.Warning, contentDescription = "Seguridad") },
                                label = { Text("Inmunidad", fontSize = 10.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.White,
                                    selectedTextColor = primaryColor,
                                    indicatorColor = primaryColor
                                )
                            )

                            NavigationBarItem(
                                selected = activeDashboardTab == 3,
                                onClick = { activeDashboardTab = 3 },
                                icon = { Icon(Icons.Default.Settings, contentDescription = "Diseño") },
                                label = { Text("Diseño", fontSize = 10.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.White,
                                    selectedTextColor = primaryColor,
                                    indicatorColor = primaryColor
                                )
                            )

                            NavigationBarItem(
                                selected = activeDashboardTab == 4,
                                onClick = { activeDashboardTab = 4 },
                                icon = { Icon(Icons.Default.Share, contentDescription = "Servicios") },
                                label = { Text("Servicios", fontSize = 10.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.White,
                                    selectedTextColor = primaryColor,
                                    indicatorColor = primaryColor
                                )
                            )
                        }
                    }
                ) { padding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                    ) {
                        when (activeDashboardTab) {
                            0 -> DataArchitectureScreen(viewModel)
                            1 -> GhostModeScreen(viewModel)
                            2 -> SecurityAntiDeleteScreen(viewModel)
                            3 -> ThemeEngineScreen(viewModel)
                            4 -> AutomationReportsScreen(viewModel)
                            else -> DataArchitectureScreen(viewModel)
                        }
                    }
                }
            }
        }
    }
}
