package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "metachat_settings")

class MetaChatDataStore(private val context: Context) {

    companion object {
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val COUNTRY_NAME = stringPreferencesKey("country_name")
        val COUNTRY_PREFIX = stringPreferencesKey("country_prefix")
        val PHONE_NUMBER = stringPreferencesKey("phone_num")
        val PROFILE_NAME = stringPreferencesKey("profile_name")
    }

    val isLoggedInFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_LOGGED_IN] ?: false
    }

    val countryNameFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[COUNTRY_NAME] ?: "España"
    }

    val countryPrefixFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[COUNTRY_PREFIX] ?: "+34"
    }

    val phoneNumberFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PHONE_NUMBER] ?: ""
    }

    val profileNameFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PROFILE_NAME] ?: "Usuario MetaChat"
    }

    suspend fun saveLoginState(isLogged: Boolean, country: String, prefix: String, phone: String, name: String) {
        context.dataStore.edit { preferences ->
            preferences[IS_LOGGED_IN] = isLogged
            preferences[COUNTRY_NAME] = country
            preferences[COUNTRY_PREFIX] = prefix
            preferences[PHONE_NUMBER] = phone
            preferences[PROFILE_NAME] = name
        }
    }
}
