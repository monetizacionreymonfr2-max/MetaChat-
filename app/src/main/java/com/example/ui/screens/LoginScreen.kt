package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

// Datos de países para el registro internacional
data class CountryInfo(val name: String, val code: String, val flag: String)

@Composable
fun LoginScreen(viewModel: DashboardViewModel) {
    val isDark = viewModel.themeConfig.forceDark
    val primaryColor = viewModel.themeConfig.primaryColor
    val backgroundColor = if (isDark) viewModel.themeConfig.darkBackground else Color(0xFFFFFFFF)
    val onBgColor = if (isDark) Color.White else Color(0xFF1E262C)
    val subTextColor = if (isDark) Color.LightGray else Color.Gray

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        when (viewModel.loginStep) {
            DashboardViewModel.LoginStep.WELCOME -> {
                WelcomeView(
                    viewModel = viewModel,
                    primaryColor = primaryColor,
                    onBgColor = onBgColor,
                    subTextColor = subTextColor
                )
            }
            DashboardViewModel.LoginStep.PHONE_INPUT -> {
                PhoneInputView(
                    viewModel = viewModel,
                    primaryColor = primaryColor,
                    onBgColor = onBgColor,
                    subTextColor = subTextColor,
                    isDark = isDark
                )
            }
            DashboardViewModel.LoginStep.OTP_VERIFICATION -> {
                OtpVerificationView(
                    viewModel = viewModel,
                    primaryColor = primaryColor,
                    onBgColor = onBgColor,
                    subTextColor = subTextColor
                )
            }
            DashboardViewModel.LoginStep.PROFILE_SETUP -> {
                ProfileSetupView(
                    viewModel = viewModel,
                    primaryColor = primaryColor,
                    onBgColor = onBgColor,
                    subTextColor = subTextColor,
                    isDark = isDark
                )
            }
            DashboardViewModel.LoginStep.LOADING_CHATS -> {
                LoadingChatsView(
                    viewModel = viewModel,
                    primaryColor = primaryColor,
                    onBgColor = onBgColor,
                    subTextColor = subTextColor
                )
            }
        }
    }
}

@Composable
fun WelcomeView(
    viewModel: DashboardViewModel,
    primaryColor: Color,
    onBgColor: Color,
    subTextColor: Color
) {
    var showLanguageMenu by remember { mutableStateOf(false) }
    val languages = listOf("Español", "English", "Português", "Français", "Deutsch")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Parte Superior: Selector de Idioma
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopEnd) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(primaryColor.copy(alpha = 0.15f))
                    .clickable { showLanguageMenu = true }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = viewModel.selectedLanguage,
                    color = primaryColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(16.dp)
                )
            }

            DropdownMenu(
                expanded = showLanguageMenu,
                onDismissRequest = { showLanguageMenu = false }
            ) {
                languages.forEach { language ->
                    DropdownMenuItem(
                        text = { Text(language) },
                        onClick = {
                            viewModel.selectedLanguage = language
                            showLanguageMenu = false
                        }
                    )
                }
            }
        }

        // Parte Central: Logo circular y bienvenida estilo WhatsApp
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .clip(CircleShape)
                    .background(primaryColor.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                // Dibujamos un mapamundi circular elegante con ondas simulando chats globales
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "💬",
                        fontSize = 72.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Te damos la bienvenida a WhatsApp",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = onBgColor,
                textAlign = TextAlign.Center,
                lineHeight = 30.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Lee nuestra Política de privacidad. Toca 'Aceptar y continuar' para aceptar las Condiciones del servicio.",
                fontSize = 13.sp,
                color = subTextColor,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }

        // Parte Inferior: Botón de aceptación
        Button(
            onClick = { viewModel.loginStep = DashboardViewModel.LoginStep.PHONE_INPUT },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
            shape = RoundedCornerShape(24.dp)
        ) {
            Text(
                text = "ACEPTAR Y CONTINUAR",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneInputView(
    viewModel: DashboardViewModel,
    primaryColor: Color,
    onBgColor: Color,
    subTextColor: Color,
    isDark: Boolean
) {
    val countries = remember {
        listOf(
            CountryInfo("España", "+34", "🇪🇸"),
            CountryInfo("Venezuela", "+58", "🇻🇪"),
            CountryInfo("México", "+52", "🇲🇽"),
            CountryInfo("Colombia", "+57", "🇨🇴"),
            CountryInfo("Argentina", "+54", "🇦🇷"),
            CountryInfo("Perú", "+51", "🇵🇪"),
            CountryInfo("Estados Unidos", "+1", "🇺🇸"),
            CountryInfo("Chile", "+56", "🇨🇱"),
            CountryInfo("Ecuador", "+593", "🇪🇨"),
            CountryInfo("Guatemala", "+502", "🇬🇹"),
            CountryInfo("Costa Rica", "+506", "🇨🇷"),
            CountryInfo("República Dominicana", "+1-809", "🇩🇴"),
            CountryInfo("Honduras", "+504", "🇭🇳"),
            CountryInfo("El Salvador", "+503", "🇸🇻"),
            CountryInfo("Panamá", "+507", "🇵🇦"),
            CountryInfo("Uruguay", "+598", "🇺🇾")
        )
    }

    var showCountrySelector by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var inputPhone by remember { mutableStateOf(viewModel.registerPhoneNumber) }
    var errorText by remember { mutableStateOf("") }

    val filteredCountries = remember(searchQuery) {
        if (searchQuery.isBlank()) countries
        else countries.filter { it.name.contains(searchQuery, ignoreCase = true) || it.code.contains(searchQuery) }
    }

    if (showCountrySelector) {
        // Modal completo o overlay para selección de país con buscador real
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = if (isDark) viewModel.themeConfig.darkBackground else MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header del selector de país
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { showCountrySelector = false }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = onBgColor)
                    }
                    Text(
                        text = "Elige un país",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = onBgColor,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Buscador
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar país o prefijo...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = if (isDark) Color(0xFF1E262C) else Color(0xFFF7F8FA),
                        unfocusedContainerColor = if (isDark) Color(0xFF1E262C) else Color(0xFFF7F8FA),
                        disabledContainerColor = Color.Transparent
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(filteredCountries) { country ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.selectedCountryName = country.name
                                    viewModel.selectedCountryPrefix = country.code
                                    showCountrySelector = false
                                }
                                .padding(horizontal = 24.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = country.flag, fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = country.name,
                                fontSize = 16.sp,
                                color = onBgColor,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = country.code,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryColor
                            )
                        }
                        Divider(
                            color = if (isDark) Color(0xFF2C353D) else Color(0xFFEFEFEF),
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }
            }
        }
    } else {
        // Vista de ingreso de teléfono original de WhatsApp
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Introduce tu número de teléfono",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "WhatsApp enviará un mensaje SMS para verificar tu número de teléfono. Introduce el prefijo de tu país y tu número telefónico.",
                    fontSize = 13.sp,
                    color = subTextColor,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(36.dp))

                // Selector de País táctil
                Row(
                    modifier = Modifier
                        .width(260.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showCountrySelector = true }
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = viewModel.selectedCountryName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = onBgColor
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Cambiar país",
                        tint = primaryColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Divider(
                    color = primaryColor,
                    modifier = Modifier.width(260.dp),
                    thickness = 1.dp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Campo de Código y Número
                Row(
                    modifier = Modifier.width(260.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Prefijo de país de lectura (bloqueado o editable via selección)
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .padding(end = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = viewModel.selectedCountryPrefix,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = onBgColor
                        )
                    }

                    // Entrada de número de celular real
                    OutlinedTextField(
                        value = inputPhone,
                        onValueChange = {
                            if (it.length <= 15) {
                                inputPhone = it
                                viewModel.registerPhoneNumber = it
                                errorText = ""
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        placeholder = { Text("número de teléfono", fontSize = 14.sp) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = primaryColor.copy(alpha = 0.5f)
                        )
                    )
                }

                if (errorText.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = errorText,
                        color = Color.Red,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Botón Siguiente
            Button(
                onClick = {
                    if (inputPhone.trim().length >= 6) {
                        viewModel.loginStep = DashboardViewModel.LoginStep.OTP_VERIFICATION
                    } else {
                        errorText = "Por favor introduce un número de teléfono válido."
                    }
                },
                modifier = Modifier
                    .width(140.dp)
                    .height(44.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                shape = RoundedCornerShape(22.dp)
            ) {
                Text("SIGUIENTE", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
            }
        }
    }
}

@Composable
fun OtpVerificationView(
    viewModel: DashboardViewModel,
    primaryColor: Color,
    onBgColor: Color,
    subTextColor: Color
) {
    var otpInput by remember { mutableStateOf("") }
    var countdownSeconds by remember { mutableStateOf(59) }
    var errorText by remember { mutableStateOf("") }

    LaunchedEffect(key1 = true) {
        while (countdownSeconds > 0) {
            delay(1000)
            countdownSeconds--
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Verifica tu número celular",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = primaryColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Hemos enviado un SMS de confirmación al número celular\n ${viewModel.selectedCountryPrefix} ${viewModel.registerPhoneNumber}.",
                fontSize = 13.sp,
                color = subTextColor,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Introduce el código de verificación de 6 dígitos.",
                fontSize = 13.sp,
                color = subTextColor,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Campo OTP bonito de WhatsApp
            OutlinedTextField(
                value = otpInput,
                onValueChange = {
                    if (it.length <= 6) {
                        otpInput = it
                        viewModel.otpCode = it
                        errorText = ""
                        if (it.length == 6) {
                            // Validar auto-avanzar
                            viewModel.loginStep = DashboardViewModel.LoginStep.PROFILE_SETUP
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                placeholder = { Text("--- ---", fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center) },
                textStyle = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center,
                    color = primaryColor
                ),
                modifier = Modifier.width(180.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryColor,
                    unfocusedBorderColor = primaryColor.copy(alpha = 0.5f)
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Helper de código de prueba rápido interactivo
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(primaryColor.copy(alpha = 0.1f))
                    .clickable {
                        otpInput = "123456"
                        viewModel.otpCode = "123456"
                        viewModel.loginStep = DashboardViewModel.LoginStep.PROFILE_SETUP
                    }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "💡 Usar código demo: 123456 (Toca aquí para auto-llenar)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )
            }

            if (errorText.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = errorText,
                    color = Color.Red,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            if (countdownSeconds > 0) {
                Text(
                    text = "Solicitar un código nuevo por SMS en ${countdownSeconds}s",
                    fontSize = 13.sp,
                    color = subTextColor
                )
            } else {
                Text(
                    text = "Reenviar código SMS",
                    fontSize = 13.sp,
                    color = primaryColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        countdownSeconds = 59
                    }
                )
            }
        }

        // Botones de acción inferior
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = { viewModel.loginStep = DashboardViewModel.LoginStep.PHONE_INPUT }) {
                Text("CAMBIAR NÚMERO", color = primaryColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            Button(
                onClick = {
                    if (otpInput.length == 6) {
                        viewModel.loginStep = DashboardViewModel.LoginStep.PROFILE_SETUP
                    } else {
                        errorText = "Introduce el código completo de 6 dígitos."
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier.height(40.dp)
            ) {
                Text("VERIFICAR", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
fun ProfileSetupView(
    viewModel: DashboardViewModel,
    primaryColor: Color,
    onBgColor: Color,
    subTextColor: Color,
    isDark: Boolean
) {
    var nameInput by remember { mutableStateOf(viewModel.profileName) }
    var errorText by remember { mutableStateOf("") }
    var selectedAvatarIndex by remember { mutableStateOf(0) }

    val avatarEmojis = listOf("😎", "🤖", "🚀", "📱", "🧑‍💻", "👩‍💻", "🎨", "🔥")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Información de perfil",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = primaryColor
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Por favor, proporciona tu nombre y una foto de perfil opcional para terminar la configuración.",
                fontSize = 13.sp,
                color = subTextColor,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Selector del Avatar o Icono del usuario
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(primaryColor.copy(alpha = 0.15f))
                    .clickable {
                        selectedAvatarIndex = (selectedAvatarIndex + 1) % avatarEmojis.size
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = avatarEmojis[selectedAvatarIndex],
                    fontSize = 48.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Toca para cambiar ilustración",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = primaryColor
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Campo de Nombre
            OutlinedTextField(
                value = nameInput,
                onValueChange = {
                    if (it.length <= 25) {
                        nameInput = it
                        viewModel.profileName = it
                        errorText = ""
                    }
                },
                singleLine = true,
                placeholder = { Text("Escribe tu nombre aquí...", fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = primaryColor) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryColor,
                    unfocusedBorderColor = primaryColor.copy(alpha = 0.5f)
                )
            )

            if (errorText.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = errorText,
                    color = Color.Red,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Botón Terminar
        Button(
            onClick = {
                if (nameInput.isNotBlank()) {
                    viewModel.loginStep = DashboardViewModel.LoginStep.LOADING_CHATS
                } else {
                    errorText = "Por favor, introduce tu nombre."
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
            shape = RoundedCornerShape(24.dp)
        ) {
            Text("SIGUIENTE", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
        }
    }
}

@Composable
fun LoadingChatsView(
    viewModel: DashboardViewModel,
    primaryColor: Color,
    onBgColor: Color,
    subTextColor: Color
) {
    var loadingPercent by remember { mutableStateOf(0) }
    var currentStepText by remember { mutableStateOf("Conectando con el servidor de GBWhatsApp...") }

    LaunchedEffect(key1 = true) {
        delay(800)
        loadingPercent = 35
        currentStepText = "Generando claves criptográficas de privacidad..."
        delay(1200)
        loadingPercent = 70
        currentStepText = "Inyectando Inmunidad Anti-Eliminación v3.2..."
        delay(900)
        loadingPercent = 100
        currentStepText = "Iniciando tu sesión de forma segura!"
        delay(500)
        viewModel.saveLoginState(
            isLogged = true,
            country = viewModel.selectedCountryName,
            prefix = viewModel.selectedCountryPrefix,
            phone = viewModel.registerPhoneNumber,
            name = viewModel.profileName
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            progress = loadingPercent / 100f,
            color = primaryColor,
            strokeWidth = 6.dp,
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "$loadingPercent%",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = primaryColor
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = currentStepText,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = onBgColor,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Por favor espera un momento, no cierres la aplicación.",
            fontSize = 12.sp,
            color = subTextColor,
            textAlign = TextAlign.Center
        )
    }
}
