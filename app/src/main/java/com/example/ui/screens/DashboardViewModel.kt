package com.example.ui.screens

import android.content.Context
import android.provider.ContactsContract
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

// Estructuras de datos para el simulador y la red real
data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: String,
    val content: String,
    val timestamp: String,
    val isForwarded: Boolean = false,
    val isDeletedBySender: Boolean = false,
    val status: MessageStatus = MessageStatus.SENT,
    val originalQuality: Boolean = false,
    val fileSize: String? = null
)

data class DynamicChat(
    val id: String = UUID.randomUUID().toString(),
    val senderName: String,
    val avatarEmoji: String,
    val isGroup: Boolean = false,
    val lastMessage: String = "",
    val timestamp: String = "",
    val unreadCount: Int = 0,
    val hasTicks: Boolean = true,
    val ticksStatus: MessageStatus = MessageStatus.READ,
    val messages: List<ChatMessage> = emptyList(),
    val phoneNumber: String = ""
)

enum class MessageStatus {
    SENT,       // Un check gris
    DELIVERED,  // Doble check gris
    READ        // Doble check azul
}

data class AutoReplyRule(
    val trigger: String,
    val response: String
)

data class ScheduledMessage(
    val id: String = UUID.randomUUID().toString(),
    val recipient: String,
    val content: String,
    val dateTime: String,
    val calendarType: String = "Google Calendar"
)

data class ThemeConfig(
    val primaryColor: Color = Color(0xFF0084FF), // MetaChat Azul Moderno por defecto
    val secondaryColor: Color = Color(0xFF00C6FF),
    val bubbleSenderColor: Color = Color(0xFFDCF8C6),
    val bubbleReceiverColor: Color = Color(0xFFFFFFFF),
    val darkBackground: Color = Color(0xFF101820),
    val forceDark: Boolean = true,
    val fontScale: Float = 1.0f,
    val bubbleStyle: String = "Rounded M3"
)

class DashboardViewModel : ViewModel() {

    // --- Persistencia local DataStore ---
    private var dataStoreManager: MetaChatDataStore? = null
    var metaChatDao: MetaChatDao? = null
        private set

    // --- Estado de Inicio de Sesión ---
    var isLoggedIn by mutableStateOf(false)
    var loginStep by mutableStateOf(LoginStep.WELCOME)
    var selectedCountryName by mutableStateOf("España")
    var selectedCountryPrefix by mutableStateOf("+34")
    var registerPhoneNumber by mutableStateOf("")
    var otpCode by mutableStateOf("")
    var profileName by mutableStateOf("")
    var selectedLanguage by mutableStateOf("Español")

    enum class LoginStep {
        WELCOME,
        PHONE_INPUT,
        OTP_VERIFICATION,
        PROFILE_SETUP,
        LOADING_CHATS
    }

    // --- Configuración de Tema ---
    var themeConfig by mutableStateOf(ThemeConfig())
        private set

    fun updatePrimaryColor(color: Color) {
        themeConfig = themeConfig.copy(primaryColor = color)
    }

    fun updateSecondaryColor(color: Color) {
        themeConfig = themeConfig.copy(secondaryColor = color)
    }

    fun updateBubbleSenderColor(color: Color) {
        themeConfig = themeConfig.copy(bubbleSenderColor = color)
    }

    fun updateForceDark(dark: Boolean) {
        themeConfig = themeConfig.copy(forceDark = dark)
    }

    fun updateBubbleStyle(style: String) {
        themeConfig = themeConfig.copy(bubbleStyle = style)
    }

    // --- Configuración de Privacidad (Modo Fantasma) ---
    var freezeLastSeen by mutableStateOf(false)
    var hideOnlineStatus by mutableStateOf(false)
    var hideTypingStatus by mutableStateOf(false)
    var disableForwardTag by mutableStateOf(true) 
    var hideSecondTick by mutableStateOf(false)    
    var hideBlueTick by mutableStateOf(false)      

    val dbPresenceStatus: String
        get() = when {
            hideOnlineStatus -> "Offline (Oculto)"
            else -> "Online"
        }

    val dbLastSeenTime: String
        get() = when {
            freezeLastSeen -> "Congelado: 12/06/2026 07:13"
            else -> "Actualizándose en tiempo real"
        }

    // --- StateFlows Reactivos Backed por Room ---
    private val _chats = MutableStateFlow<List<DynamicChat>>(emptyList())
    val chats: StateFlow<List<DynamicChat>> = _chats.asStateFlow()

    var activeChatId by mutableStateOf<String?>(null)

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    // --- Inicializador de Base de Datos Real (Llamado desde MainActivity) ---
    fun initDatabase(context: Context) {
        val appCtx = context.applicationContext
        dataStoreManager = MetaChatDataStore(appCtx)

        // Cargar estado persistente
        viewModelScope.launch {
            dataStoreManager?.isLoggedInFlow?.firstOrNull()?.let { isLoggedIn = it }
            dataStoreManager?.countryNameFlow?.firstOrNull()?.let { selectedCountryName = it }
            dataStoreManager?.countryPrefixFlow?.firstOrNull()?.let { selectedCountryPrefix = it }
            dataStoreManager?.phoneNumberFlow?.firstOrNull()?.let { registerPhoneNumber = it }
            dataStoreManager?.profileNameFlow?.firstOrNull()?.let { profileName = it }
        }

        val db = MetaChatDatabase.getDatabase(appCtx)
        val dao = db.metaChatDao()
        metaChatDao = dao

        // Recolectar reactivamente desde Room DB
        viewModelScope.launch {
            dao.getAllContactsFlow().collectLatest { contactsList ->
                val dynamicChatsList = mutableListOf<DynamicChat>()
                for (contact in contactsList) {
                    val messagesList = dao.getMessagesForChatFlow(contact.id).first()
                    dynamicChatsList.add(
                        DynamicChat(
                            id = contact.id,
                            senderName = contact.senderName,
                            avatarEmoji = contact.avatarEmoji,
                            isGroup = contact.isGroup,
                            lastMessage = contact.lastMessage,
                            timestamp = contact.timestamp,
                            unreadCount = contact.unreadCount,
                            hasTicks = contact.hasTicks,
                            ticksStatus = MessageStatus.valueOf(contact.ticksStatus),
                            phoneNumber = contact.phoneNumber,
                            messages = messagesList.map { msg ->
                                ChatMessage(
                                    id = msg.id,
                                    sender = msg.sender,
                                    content = msg.content,
                                    timestamp = msg.timestamp,
                                    isForwarded = msg.isForwarded,
                                    isDeletedBySender = msg.isDeletedBySender,
                                    status = MessageStatus.valueOf(msg.status),
                                    originalQuality = msg.originalQuality,
                                    fileSize = msg.fileSize
                                )
                            }
                        )
                    )
                }

                if (dynamicChatsList.isEmpty()) {
                    preloadMockData(dao)
                } else {
                    _chats.value = dynamicChatsList
                    
                    // Auto-seleccionar primer chat si es nulo
                    if (activeChatId == null && dynamicChatsList.isNotEmpty()) {
                        selectChat(dynamicChatsList.first().id)
                    } else if (activeChatId != null) {
                        val activeChat = dynamicChatsList.find { it.id == activeChatId }
                        _messages.value = activeChat?.messages ?: emptyList()
                    }
                }
            }
        }

        // Iniciar Sync en tiempo real con Firebase (Push bidireccional)
        MetaChatNetworkService.initFirebase(appCtx)
        startFirebaseSync()
    }

    private suspend fun preloadMockData(dao: MetaChatDao) {
        val mockContacts = listOf(
            LocalContactEntity(
                id = "chat_global",
                senderName = "Foro Mundial METACHAT 🌍",
                avatarEmoji = "📢",
                isGroup = true,
                lastMessage = "¡Bienvenidos a METACHAT! Chatea en vivo mundialmente.",
                timestamp = "Ahora",
                unreadCount = 0,
                hasTicks = false,
                ticksStatus = MessageStatus.READ.name,
                phoneNumber = "global"
            ),
            LocalContactEntity(
                id = "chat_delvalle",
                senderName = "Delvalle Madre Mia",
                avatarEmoji = "👩",
                isGroup = false,
                lastMessage = "Ok esta bien",
                timestamp = "9:26",
                unreadCount = 1,
                hasTicks = true,
                ticksStatus = MessageStatus.DELIVERED.name,
                phoneNumber = "+58414999999"
            ),
            LocalContactEntity(
                id = "chat_katy",
                senderName = "Katy Prima",
                avatarEmoji = "👱‍♀️",
                isGroup = false,
                lastMessage = "Como va todo",
                timestamp = "9:05",
                unreadCount = 0,
                hasTicks = true,
                ticksStatus = MessageStatus.READ.name,
                phoneNumber = "+58414888888"
            )
        )

        val mockMessages = listOf(
            LocalMessageEntity(
                id = "msg_global_1",
                chatId = "chat_global",
                sender = "MetaChat Support",
                content = "¡Bienvenidos a la red social METACHAT! Chatea en vivo aquí. Este es un foro compartido con todos los usuarios activos del planeta.",
                timestamp = "Hoy",
                isForwarded = false,
                isDeletedBySender = false,
                status = MessageStatus.READ.name,
                originalQuality = false,
                fileSize = null
            ),
            LocalMessageEntity(
                id = "msg_del_1",
                chatId = "chat_delvalle",
                sender = "Delvalle Madre Mia",
                content = "Hola hijo, ¿cómo estás?",
                timestamp = "9:15 AM",
                isForwarded = false,
                isDeletedBySender = false,
                status = MessageStatus.DELIVERED.name,
                originalQuality = false,
                fileSize = null
            ),
            LocalMessageEntity(
                id = "msg_del_2",
                chatId = "chat_delvalle",
                sender = "Tú",
                content = "Hola mami, todo muy bien por aquí. Saliendo de trabajar.",
                timestamp = "9:18 AM",
                isForwarded = false,
                isDeletedBySender = false,
                status = MessageStatus.READ.name,
                originalQuality = false,
                fileSize = null
            ),
            LocalMessageEntity(
                id = "msg_del_3",
                chatId = "chat_delvalle",
                sender = "Delvalle Madre Mia",
                content = "Qué bueno, que Dios te cuide.",
                timestamp = "9:20 AM",
                isForwarded = false,
                isDeletedBySender = false,
                status = MessageStatus.DELIVERED.name,
                originalQuality = false,
                fileSize = null
            ),
            LocalMessageEntity(
                id = "msg_del_4",
                chatId = "chat_delvalle",
                sender = "Delvalle Madre Mia",
                content = "Ok esta bien",
                timestamp = "9:26 AM",
                isForwarded = false,
                isDeletedBySender = false,
                status = MessageStatus.DELIVERED.name,
                originalQuality = false,
                fileSize = null
            )
        )

        for (contact in mockContacts) {
            dao.insertContact(contact)
        }
        for (msg in mockMessages) {
            dao.insertMessage(msg)
        }
    }

    fun saveLoginState(isLogged: Boolean, country: String, prefix: String, phone: String, name: String) {
        isLoggedIn = isLogged
        selectedCountryName = country
        selectedCountryPrefix = prefix
        registerPhoneNumber = phone
        profileName = name

        viewModelScope.launch {
            dataStoreManager?.saveLoginState(isLogged, country, prefix, phone, name)
            
            metaChatDao?.insertContact(
                LocalContactEntity(
                    id = "chat_mio",
                    senderName = "Mio Movistar ($name)",
                    avatarEmoji = "📱",
                    isGroup = false,
                    lastMessage = "Tel: $phone",
                    timestamp = "Hoy",
                    unreadCount = 0,
                    hasTicks = true,
                    ticksStatus = MessageStatus.READ.name,
                    phoneNumber = phone
                )
            )
        }
    }

    fun selectChat(chatId: String) {
        activeChatId = chatId
        val chat = _chats.value.find { it.id == chatId }
        _messages.value = chat?.messages ?: emptyList()
        viewModelScope.launch {
            metaChatDao?.resetUnreadCount(chatId)
        }
    }

    fun createNewChat(senderName: String, avatarEmoji: String, isGroup: Boolean = false, phoneNumber: String = "") {
        val newId = if (phoneNumber.isNotBlank()) "contact_${phoneNumber.filter { it.isDigit() }}" else UUID.randomUUID().toString()
        val defaultGreeting = if (isGroup) "Grupo creado con éxito" else "¡Hola! Bienvenido a mi MetaChat."
        
        viewModelScope.launch {
            metaChatDao?.insertContact(
                LocalContactEntity(
                    id = newId,
                    senderName = senderName,
                    avatarEmoji = avatarEmoji,
                    isGroup = isGroup,
                    lastMessage = if (phoneNumber.isNotBlank()) "Tel: $phoneNumber" else defaultGreeting,
                    timestamp = "Hoy",
                    unreadCount = 0,
                    hasTicks = false,
                    ticksStatus = MessageStatus.READ.name,
                    phoneNumber = phoneNumber
                )
            )

            metaChatDao?.insertMessage(
                LocalMessageEntity(
                    id = UUID.randomUUID().toString(),
                    chatId = newId,
                    sender = if (isGroup) "Sistema" else senderName,
                    content = if (phoneNumber.isNotBlank()) "Conversación iniciada con $senderName. Teléfono: $phoneNumber" else defaultGreeting,
                    timestamp = "Ahora",
                    isForwarded = false,
                    isDeletedBySender = false,
                    status = MessageStatus.READ.name,
                    originalQuality = false,
                    fileSize = null
                )
            )
        }

        selectChat(newId)
    }

    fun sendMessage(content: String, isForwarded: Boolean = false, originalQuality: Boolean = false, fileSize: String? = null) {
        val activeId = activeChatId ?: return
        val currentChat = _chats.value.find { it.id == activeId } ?: return

        val msgId = UUID.randomUUID().toString()
        val statusVal = calculateInitialStatus()

        viewModelScope.launch {
            val dao = metaChatDao ?: return@launch
            
            // Insertar localmente en Room
            dao.insertMessage(
                LocalMessageEntity(
                    id = msgId,
                    chatId = activeId,
                    sender = "Tú",
                    content = content,
                    timestamp = "Hoy",
                    isForwarded = isForwarded,
                    isDeletedBySender = false,
                    status = statusVal.name,
                    originalQuality = originalQuality,
                    fileSize = fileSize
                )
            )

            // Actualizar estado del contacto
            dao.updateContactStatus(
                chatId = activeId,
                lastMsg = content,
                time = "Hoy",
                hasTicks = true,
                status = statusVal.name
            )

            // Si tiene número de teléfono o es Foro Mundial, propagar por la red real
            if (activeId == "chat_global" || currentChat.phoneNumber.isNotBlank()) {
                sendNetworkMessage(activeId, content, currentChat.phoneNumber)
            }
        }

        checkAutoReply(content)
    }

    private fun calculateInitialStatus(): MessageStatus {
        return when {
            hideSecondTick -> MessageStatus.SENT
            hideBlueTick -> MessageStatus.DELIVERED
            else -> MessageStatus.READ
        }
    }

    fun simulateMessageReception() {
        val activeId = activeChatId ?: return
        val chat = _chats.value.find { it.id == activeId } ?: return

        val remoteContent = when (chat.senderName) {
            "Delvalle Madre Mia" -> "Dios te bendiga hijo mío, avísame cuando estés libre."
            "Katy Prima" -> "Ah bueno prima, me avisas cualquier cosa!"
            "Glendy Hermana" -> "Sí hermano, ya lo recibí, gracias."
            "Tyrone Y Camila Hijos Míos" -> "¡Papá, ya terminamos la tarea! ¿Cuándo vienes?"
            "Bibi Store" -> "Hola, ya tu pedido fue despachado."
            "Mio Movistar (Tú)" -> "Actualización de saldo: exitoso."
            "Eduardo 16" -> "¡Jajaja en serio tienes que ver esto!"
            else -> "¡Hola! ¿Cómo estás?"
        }

        viewModelScope.launch {
            val dao = metaChatDao ?: return@launch
            dao.insertMessage(
                LocalMessageEntity(
                    id = UUID.randomUUID().toString(),
                    chatId = activeId,
                    sender = chat.senderName,
                    content = remoteContent,
                    timestamp = "Hoy",
                    isForwarded = false,
                    isDeletedBySender = false,
                    status = MessageStatus.READ.name,
                    originalQuality = false,
                    fileSize = null
                )
            )

            dao.updateContactStatus(
                chatId = activeId,
                lastMsg = remoteContent,
                time = "Hoy",
                hasTicks = false,
                status = MessageStatus.READ.name
            )
        }
    }

    fun simulateSenderDeletesLastMessage() {
        val activeId = activeChatId ?: return
        val chat = _chats.value.find { it.id == activeId } ?: return
        val lastRemoteMsg = chat.messages.findLast { it.sender == chat.senderName }
        if (lastRemoteMsg != null) {
            viewModelScope.launch {
                val dao = metaChatDao ?: return@launch
                dao.markMessageDeleted(lastRemoteMsg.id)
                dao.updateContactStatus(
                    chatId = activeId,
                    lastMsg = "🚫 Este mensaje fue eliminado",
                    time = "Hoy",
                    hasTicks = false,
                    status = MessageStatus.READ.name
                )
            }
        }
    }

    fun forceReadTicks() {
        viewModelScope.launch {
            val dao = metaChatDao ?: return@launch
            // Optimizar ticks de lectura localmente
            val unreadMessagesForChat = dao.getMessagesForChatFlow(activeChatId ?: return@launch).first()
            for (msg in unreadMessagesForChat) {
                if (msg.sender == "Tú" && msg.status != MessageStatus.READ.name) {
                    dao.insertMessage(msg.copy(status = MessageStatus.READ.name))
                }
            }
        }
    }

    // --- Respuestas Automáticas ---
    var autoReplyRules = mutableStateOf(
        listOf(
            AutoReplyRule("hola", "¡Hola! Bienvenido a METACHAT, una red social real. Estoy ausente pero grabaré tu mensaje."),
            AutoReplyRule("precio", "Hola! METACHAT es 100% gratis y de código abierto."),
            AutoReplyRule("reunion", "De acuerdo, ya he registrado nuestro encuentro en el calendario local.")
        )
    )

    fun addAutoReplyRule(trigger: String, response: String) {
        if (trigger.isNotBlank() && response.isNotBlank()) {
            autoReplyRules.value = autoReplyRules.value + AutoReplyRule(trigger.lowercase(), response)
        }
    }

    private fun checkAutoReply(content: String) {
        val lowerContent = content.lowercase()
        val match = autoReplyRules.value.find { lowerContent.contains(it.trigger) }
        val activeId = activeChatId ?: return
        if (match != null) {
            viewModelScope.launch {
                delay(1200)
                val replyContent = "🤖 [Auto-Respuesta]: ${match.response}"
                val dao = metaChatDao ?: return@launch
                dao.insertMessage(
                    LocalMessageEntity(
                        id = UUID.randomUUID().toString(),
                        chatId = activeId,
                        sender = "Meta-Bot 🤖",
                        content = replyContent,
                        timestamp = "Ahora",
                        isForwarded = false,
                        isDeletedBySender = false,
                        status = MessageStatus.READ.name,
                        originalQuality = false,
                        fileSize = null
                    )
                )
                dao.updateContactStatus(
                    chatId = activeId,
                    lastMsg = replyContent,
                    time = "Ahora",
                    hasTicks = false,
                    status = MessageStatus.READ.name
                )
            }
        }
    }

    // --- Calendario & Mensajes Programados ---
    var scheduledMessages = mutableStateOf(
        listOf(
            ScheduledMessage(recipient = "Inversión MetaChat", content = "Reporte de sincronización y base de datos local OK", dateTime = "15/06/2026 09:00", calendarType = "Google Calendar")
        )
    )

    fun scheduleMessage(recipient: String, content: String, dateTime: String, calendar: String) {
        if (recipient.isNotBlank() && content.isNotBlank()) {
            scheduledMessages.value = scheduledMessages.value + ScheduledMessage(
                recipient = recipient,
                content = content,
                dateTime = dateTime,
                calendarType = calendar
            )
        }
    }

    fun deleteScheduledMessage(id: String) {
        scheduledMessages.value = scheduledMessages.value.filter { it.id != id }
    }

    // --- Reportes de Sistema ---
    var exportedReportsCount by mutableStateOf(5)

    // --- Importar contactos de la agenda real del dispositivo ---
    fun importDeviceContacts(context: Context): Int {
        var importedCount = 0
        try {
            val contentResolver = context.contentResolver
            val cursor = contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER
                ),
                null,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
            )

            val contactsToInsert = mutableListOf<LocalContactEntity>()

            cursor?.use { c ->
                val nameIndex = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numberIndex = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

                while (c.moveToNext()) {
                    val name = if (nameIndex >= 0) c.getString(nameIndex) else "Sin Nombre"
                    val number = if (numberIndex >= 0) c.getString(numberIndex) else ""

                    if (number.isNotBlank()) {
                        val cleanNum = number.replace(Regex("[^0-9+]"), "")
                        if (cleanNum.isNotBlank()) {
                            val emojis = listOf("👩", "👱‍♀️", "👩‍🦰", "🧒", "👦", "👨", "👴", "👵", "🧔", "🦊")
                            val avatar = emojis[Math.abs(name.hashCode()) % emojis.size]
                            val chatId = "contact_${cleanNum.filter { it.isDigit() }}"

                            contactsToInsert.add(
                                LocalContactEntity(
                                    id = chatId,
                                    senderName = name,
                                    avatarEmoji = avatar,
                                    isGroup = false,
                                    lastMessage = "Tel: $number",
                                    timestamp = "Info",
                                    unreadCount = 0,
                                    hasTicks = false,
                                    ticksStatus = MessageStatus.READ.name,
                                    phoneNumber = cleanNum
                                )
                            )
                        }
                    }
                }
            }

            if (contactsToInsert.isNotEmpty()) {
                viewModelScope.launch {
                    val dao = metaChatDao ?: return@launch
                    // Insertar todos de una vez en Room
                    val existingContacts = dao.getAllContactsFlow().first()
                    val existingIds = existingContacts.map { it.id }.toSet()

                    val uniqueNewOnes = contactsToInsert
                        .associateBy { it.id }
                        .values
                        .filter { it.id !in existingIds }

                    for (c in uniqueNewOnes) {
                        dao.insertContact(c)
                        dao.insertMessage(
                            LocalMessageEntity(
                                id = UUID.randomUUID().toString(),
                                chatId = c.id,
                                sender = c.senderName,
                                content = "¡Hola! Soy tu contacto real de la agenda. Iniciemos chat en METACHAT.",
                                timestamp = "Ahora",
                                isForwarded = false,
                                isDeletedBySender = false,
                                status = MessageStatus.READ.name,
                                originalQuality = false,
                                fileSize = null
                            )
                        )
                    }
                    importedCount = uniqueNewOnes.size
                    if (uniqueNewOnes.isNotEmpty()) {
                        selectChat(uniqueNewOnes.first().id)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return importedCount
    }

    // --- Métodos de sincronización con la nube (Multi-Peer Cloud Messaging) ---
    private fun startFirebaseSync() {
        viewModelScope.launch(Dispatchers.IO) {
            // Un pequeño delay inicial para que el DataStore recupere el número de teléfono
            delay(1000)
            val cleanMyPhone = registerPhoneNumber.replace(Regex("[^0-9]"), "")
            val dao = metaChatDao ?: return@launch
            
            // 1. Escuchar buzón privado (Push en tiempo real)
            if (cleanMyPhone.isNotBlank()) {
                launch {
                    MetaChatNetworkService.listenToInbox(cleanMyPhone).collectLatest { messages ->
                        for (msg in messages) {
                            val chatId = "contact_${msg.senderPhone.filter { it.isDigit() }}"
                            val cleanSenderName = msg.senderName
                            
                            dao.insertContact(
                                LocalContactEntity(
                                    id = chatId,
                                    senderName = cleanSenderName,
                                    avatarEmoji = "👩",
                                    isGroup = false,
                                    lastMessage = msg.content,
                                    timestamp = "Ahora",
                                    unreadCount = if (activeChatId == chatId) 0 else 1,
                                    hasTicks = false,
                                    ticksStatus = MessageStatus.READ.name,
                                    phoneNumber = msg.senderPhone
                                )
                            )

                            dao.insertMessage(
                                LocalMessageEntity(
                                    id = msg.id,
                                    chatId = chatId,
                                    sender = cleanSenderName,
                                    content = msg.content,
                                    timestamp = msg.timestamp,
                                    isForwarded = false,
                                    isDeletedBySender = false,
                                    status = MessageStatus.READ.name,
                                    originalQuality = false,
                                    fileSize = null
                                )
                            )
                        }
                    }
                }
            }

            // 2. Escuchar foro global (Push en tiempo real)
            launch {
                MetaChatNetworkService.listenToInbox("global").collectLatest { globalMessages ->
                    if (globalMessages.isNotEmpty()) {
                        dao.insertContact(
                            LocalContactEntity(
                                id = "chat_global",
                                senderName = "Foro Mundial METACHAT \uD83C\uDF10",
                                avatarEmoji = "\uD83D\uDCE2",
                                isGroup = true,
                                lastMessage = globalMessages.last().content,
                                timestamp = "Ahora",
                                unreadCount = 0,
                                hasTicks = false,
                                ticksStatus = MessageStatus.READ.name,
                                phoneNumber = "global"
                            )
                        )

                        val existingMsgs = dao.getMessagesForChatFlow("chat_global").firstOrNull() ?: emptyList()
                        val existingIds = existingMsgs.map { it.id }.toSet()

                        for (msg in globalMessages) {
                            if (msg.id !in existingIds) {
                                dao.insertMessage(
                                    LocalMessageEntity(
                                        id = msg.id,
                                        chatId = "chat_global",
                                        sender = msg.senderName,
                                        content = msg.content,
                                        timestamp = msg.timestamp,
                                        isForwarded = false,
                                        isDeletedBySender = false,
                                        status = MessageStatus.READ.name,
                                        originalQuality = false,
                                        fileSize = null
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun sendNetworkMessage(chatId: String, content: String, recipientPhone: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val cleanRecipient = recipientPhone.replace(Regex("[^0-9]"), "")
            val newMsg = NetworkMessage(
                id = UUID.randomUUID().toString(),
                senderName = profileName,
                senderPhone = registerPhoneNumber,
                content = content,
                timestamp = "Hoy"
            )

            if (chatId == "chat_global" || cleanRecipient == "global") {
                MetaChatNetworkService.sendNetworkMessage("global", newMsg)
            } else if (cleanRecipient.isNotBlank()) {
                MetaChatNetworkService.sendNetworkMessage(cleanRecipient, newMsg)
            }
        }
    }
}
