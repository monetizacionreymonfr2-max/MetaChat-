package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val dbViewModel: DashboardViewModel = viewModel()
            val themeConfig = dbViewModel.themeConfig

            // Inicializar base de datos local persistente Room y restaurar sesión guardada
            LaunchedEffect(Unit) {
                dbViewModel.initDatabase(this@MainActivity)
            }

            // Crear esquema de colores altamente reactivo según selección del usuario
            val customColorScheme = if (themeConfig.forceDark) {
                darkColorScheme(
                    primary = themeConfig.primaryColor,
                    secondary = themeConfig.secondaryColor,
                    background = themeConfig.darkBackground,
                    surface = Color(0xFF1E262C),
                    surfaceVariant = Color(0xFF252D32),
                    onBackground = Color.White,
                    onSurface = Color.White,
                    onSurfaceVariant = Color.LightGray
                )
            } else {
                lightColorScheme(
                    primary = themeConfig.primaryColor,
                    secondary = themeConfig.secondaryColor,
                    background = Color(0xFFF7F8FA),
                    surface = Color.White,
                    surfaceVariant = Color(0xFFECEFF1),
                    onBackground = Color.Black,
                    onSurface = Color.Black,
                    onSurfaceVariant = Color.DarkGray
                )
            }

            MaterialTheme(colorScheme = customColorScheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (dbViewModel.isLoggedIn) {
                        GBWhatsAppHomeScreen(dbViewModel)
                    } else {
                        LoginScreen(dbViewModel)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchitectureDashboard(viewModel: DashboardViewModel) {
    var activeTab by remember { mutableStateOf(0) }
    val themeConfig = viewModel.themeConfig

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "GBWhatsApp Engine v3.2",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Arquitectura de Sistemas & Privacidad",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                },
                actions = {
                    // Modo oscuro rápido interactivo
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = if (themeConfig.forceDark) "MODO OBSCURO" else "MODO CLARO",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Switch(
                            checked = themeConfig.forceDark,
                            onCheckedChange = { viewModel.updateForceDark(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = themeConfig.primaryColor,
                                uncheckedThumbColor = Color.LightGray
                            )
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = themeConfig.primaryColor
                )
            )
        },
        bottomBar = {
            // M3 Navigation Bar que respeta insets de forma automática
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                NavigationBarItem(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    icon = { Icon(Icons.Default.List, contentDescription = "BD") },
                    label = { Text("Estructura", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = themeConfig.primaryColor,
                        indicatorColor = themeConfig.primaryColor
                    )
                )

                NavigationBarItem(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    icon = { Icon(Icons.Default.Lock, contentDescription = "Fantasma") },
                    label = { Text("Fantasma", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = themeConfig.primaryColor,
                        indicatorColor = themeConfig.primaryColor
                    )
                )

                NavigationBarItem(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    icon = { Icon(Icons.Default.Warning, contentDescription = "Seguridad") },
                    label = { Text("Inmunidad", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = themeConfig.primaryColor,
                        indicatorColor = themeConfig.primaryColor
                    )
                )

                NavigationBarItem(
                    selected = activeTab == 3,
                    onClick = { activeTab = 3 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Diseño") },
                    label = { Text("Diseño", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = themeConfig.primaryColor,
                        indicatorColor = themeConfig.primaryColor
                    )
                )

                NavigationBarItem(
                    selected = activeTab == 4,
                    onClick = { activeTab = 4 },
                    icon = { Icon(Icons.Default.Share, contentDescription = "Servicios") },
                    label = { Text("Servicios", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = themeConfig.primaryColor,
                        indicatorColor = themeConfig.primaryColor
                    )
                )
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing // Se previene clipping del notch superior
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeTab) {
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
