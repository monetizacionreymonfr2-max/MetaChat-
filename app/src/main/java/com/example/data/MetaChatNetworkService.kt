package com.example.data

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.UUID

data class NetworkMessage(
    val id: String = UUID.randomUUID().toString(),
    val senderName: String = "",
    val senderPhone: String = "",
    val content: String = "",
    val timestamp: String = ""
)

object MetaChatNetworkService {

    private var database: FirebaseDatabase? = null

    // Inicializa Firebase usando las credenciales del .env leidas vía BuildConfig
    fun initFirebase(context: Context) {
        if (FirebaseApp.getApps(context).isEmpty()) {
            val dbUrl = BuildConfig.FIREBASE_DATABASE_URL
            val apiKey = BuildConfig.FIREBASE_API_KEY
            val projectId = BuildConfig.FIREBASE_PROJECT_ID
            val appId = BuildConfig.FIREBASE_APP_ID

            if (dbUrl.isNotBlank() && apiKey.isNotBlank() && appId.isNotBlank() && !dbUrl.contains("DEFAULT")) {
                try {
                    val options = FirebaseOptions.Builder()
                        .setDatabaseUrl(dbUrl)
                        .setApiKey(apiKey)
                        .setProjectId(projectId)
                        .setApplicationId(appId)
                        .build()
                    
                    FirebaseApp.initializeApp(context.applicationContext, options)
                    database = FirebaseDatabase.getInstance()
                    Log.d("MetaChatNetwork", "Firebase Realtime Database conectado exitosamente.")
                } catch (e: Exception) {
                    Log.e("MetaChatNetwork", "Error al conectar Firebase", e)
                }
            } else {
                Log.w("MetaChatNetwork", "Faltan credenciales en el archivo .env para conectar con Firebase Realtime DB. Funciones de nube deshabilitadas.")
            }
        } else {
            database = FirebaseDatabase.getInstance()
        }
    }

    // Escucha en tiempo real un buzón ("inbox_phone" o "inbox_global") y limpia los consumidos
    fun listenToInbox(phone: String): Flow<List<NetworkMessage>> = callbackFlow {
        val db = database ?: run {
            close()
            return@callbackFlow
        }
        val ref = db.getReference("inbox_${phone}")
        
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = mutableListOf<NetworkMessage>()
                for (child in snapshot.children) {
                    val msg = child.getValue(NetworkMessage::class.java)
                    if (msg != null) {
                        messages.add(msg)
                    }
                }
                
                if (messages.isNotEmpty()) {
                    trySend(messages.toList())
                    // Una vez recibido, borramos la cola localmente en la nube. 
                    // No vaciamos "global" para no destruir el historial comun.
                    // (Opcionalmente, solo borramos los individuales)
                    if (phone != "global") {
                        ref.removeValue()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MetaChatNetwork", "Error en Firebase: ${error.message}")
            }
        }
        
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    // Envía un mensaje a un buzón específico ("global" o número) empujando a la lista
    fun sendNetworkMessage(phone: String, message: NetworkMessage) {
        val db = database ?: return
        val ref = db.getReference("inbox_${phone}")
        ref.push().setValue(message)
    }
}
