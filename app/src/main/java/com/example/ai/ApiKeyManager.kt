package com.example.ai

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig
import com.example.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class GeminiModelOption(
    val id: String,
    val name: String,
    val description: String,
    val tag: String = ""
)

data class PresetAvatarOption(
    val id: String,
    val name: String,
    val resId: Int
)

object ApiKeyManager {
    private const val PREFS_NAME = "crux_tutor_prefs"
    private const val KEY_CUSTOM_GEMINI_KEY = "custom_gemini_api_key"
    private const val KEY_SELECTED_MODEL = "selected_gemini_model"
    private const val KEY_GOOGLE_SIGNED_IN = "google_signed_in"
    private const val KEY_GOOGLE_TOKEN = "google_token"
    private const val KEY_USER_NAME = "user_name"
    private const val KEY_USER_EMAIL = "user_email"
    private const val KEY_LOGIN_TYPE = "login_type" // "GOOGLE" or "EMAIL"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"
    private const val KEY_AVATAR_TYPE = "avatar_type" // "PRESET" or "GALLERY"
    private const val KEY_AVATAR_PRESET_ID = "avatar_preset_id"
    private const val KEY_AVATAR_GALLERY_URI = "avatar_gallery_uri"
    private const val KEY_FIRST_LAUNCH_PROMPTED = "first_launch_prompted"

    const val SUPPORT_EMAIL = "cruxsuport@gmail.com"
    const val DEFAULT_MODEL = "gemini-3.6-flash"

    val AVAILABLE_MODELS = listOf(
        GeminiModelOption("gemini-3.6-flash", "Gemini 3.6 Flash", "Modelo rápido, altamente inteligente e de nova geração", "3.6 Flash"),
        GeminiModelOption("gemini-3.1-flash-lite", "Gemini 3.1 Flash-Lite", "Ultra-rápido, otimizado e altamente eficiente", "3.1 Lite")
    )

    private const val KEY_APP_LANGUAGE = "app_language"
    private val _appLanguageFlow = MutableStateFlow("en")
    val appLanguageFlow: StateFlow<String> = _appLanguageFlow.asStateFlow()

    fun getLanguage(): String = prefs?.getString(KEY_APP_LANGUAGE, "en") ?: "en"

    fun setLanguage(lang: String) {
        prefs?.edit()?.putString(KEY_APP_LANGUAGE, lang)?.apply()
        _appLanguageFlow.value = lang
    }

    val PRESET_AVATARS = listOf(
        PresetAvatarOption("nano_banana_lite", "Nano Banana Lite", R.drawable.nano_banana_lite_avatar_1786283042806),
        PresetAvatarOption("minimalist_star", "Crux Minimalist Star", R.drawable.minimalist_star_avatar_1786283059409),
        PresetAvatarOption("nano_banana", "Nano Banana 3D", R.drawable.nano_banana_avatar_1786279403049),
        PresetAvatarOption("astro_banana", "Astro Banana", R.drawable.astro_banana_avatar_1786279416965),
        PresetAvatarOption("cyber_star", "Cyber Owl Scholar", R.drawable.cyber_star_avatar_1786279432047),
        PresetAvatarOption("crux_logo", "Crux Star Logo", R.drawable.ic_crux_logo_1786280288578)
    )

    private var prefs: SharedPreferences? = null
    private val _apiKeyFlow = MutableStateFlow<String>("")
    val apiKeyFlow: StateFlow<String> = _apiKeyFlow.asStateFlow()

    private val _selectedModelFlow = MutableStateFlow<String>(DEFAULT_MODEL)
    val selectedModelFlow: StateFlow<String> = _selectedModelFlow.asStateFlow()

    private val _userNameFlow = MutableStateFlow<String>("Estudante Crux")
    val userNameFlow: StateFlow<String> = _userNameFlow.asStateFlow()

    private val _userEmailFlow = MutableStateFlow<String>("aluno.crux@gmail.com")
    val userEmailFlow: StateFlow<String> = _userEmailFlow.asStateFlow()

    private val _isGoogleSignedInFlow = MutableStateFlow<Boolean>(false)
    val isGoogleSignedInFlow: StateFlow<Boolean> = _isGoogleSignedInFlow.asStateFlow()

    private val _avatarUriOrResFlow = MutableStateFlow<Any>(R.drawable.nano_banana_avatar_1786279403049)
    val avatarUriOrResFlow: StateFlow<Any> = _avatarUriOrResFlow.asStateFlow()

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            _apiKeyFlow.value = getApiKey()
            _selectedModelFlow.value = getSelectedModel()
            _userNameFlow.value = getUserName()
            _userEmailFlow.value = getUserEmail()
            _appLanguageFlow.value = getLanguage()
            _isGoogleSignedInFlow.value = isGoogleSignedIn()
            updateAvatarFlow()
        }
    }

    fun getApiKey(): String {
        val customKey = prefs?.getString(KEY_CUSTOM_GEMINI_KEY, "")?.trim() ?: ""
        if (customKey.isNotBlank()) {
            return customKey
        }
        val buildKey = BuildConfig.GEMINI_API_KEY
        return if (buildKey.isBlank() || buildKey == "MY_GEMINI_API_KEY") {
            ""
        } else {
            buildKey
        }
    }

    fun getCustomApiKeyOnly(): String {
        return prefs?.getString(KEY_CUSTOM_GEMINI_KEY, "")?.trim() ?: ""
    }

    fun setCustomApiKey(key: String) {
        prefs?.edit()?.putString(KEY_CUSTOM_GEMINI_KEY, key.trim())?.apply()
        _apiKeyFlow.value = getApiKey()
    }

    fun clearCustomApiKey() {
        prefs?.edit()?.remove(KEY_CUSTOM_GEMINI_KEY)?.apply()
        _apiKeyFlow.value = getApiKey()
    }

    // Model selection
    fun getSelectedModel(): String {
        val saved = prefs?.getString(KEY_SELECTED_MODEL, DEFAULT_MODEL) ?: DEFAULT_MODEL
        return if (AVAILABLE_MODELS.any { it.id == saved }) saved else DEFAULT_MODEL
    }

    fun setSelectedModel(modelId: String) {
        prefs?.edit()?.putString(KEY_SELECTED_MODEL, modelId)?.apply()
        _selectedModelFlow.value = modelId
    }

    // Google & Email Authentication
    fun isLoggedIn(): Boolean {
        return prefs?.getBoolean(KEY_IS_LOGGED_IN, false) == true || prefs?.getBoolean(KEY_GOOGLE_SIGNED_IN, false) == true
    }

    fun isGoogleSignedIn(): Boolean {
        return prefs?.getBoolean(KEY_GOOGLE_SIGNED_IN, false) == true
    }

    fun isFirstLaunchPrompted(): Boolean {
        return prefs?.getBoolean(KEY_FIRST_LAUNCH_PROMPTED, false) ?: false
    }

    fun markFirstLaunchPrompted() {
        prefs?.edit()?.putBoolean(KEY_FIRST_LAUNCH_PROMPTED, true)?.apply()
    }

    fun getGoogleToken(): String {
        return prefs?.getString(KEY_GOOGLE_TOKEN, "") ?: ""
    }

    fun getLoginType(): String {
        return prefs?.getString(KEY_LOGIN_TYPE, if (isGoogleSignedIn()) "GOOGLE" else "EMAIL") ?: "NONE"
    }

    fun saveGoogleLogin(name: String = "Bernardo Ivanowski", email: String = "bernardoivanowski79@gmail.com") {
        prefs?.edit()
            ?.putBoolean(KEY_IS_LOGGED_IN, true)
            ?.putBoolean(KEY_GOOGLE_SIGNED_IN, true)
            ?.putString(KEY_LOGIN_TYPE, "GOOGLE")
            ?.putString(KEY_USER_NAME, name.ifBlank { "Bernardo Ivanowski" })
            ?.putString(KEY_USER_EMAIL, email.ifBlank { "bernardoivanowski79@gmail.com" })
            ?.apply()

        _isGoogleSignedInFlow.value = true
        _userNameFlow.value = getUserName()
        _userEmailFlow.value = getUserEmail()
            _appLanguageFlow.value = getLanguage()
    }

    fun saveEmailLogin(email: String, name: String = "") {
        val calculatedName = if (name.isNotBlank()) name else {
            val prefix = email.substringBefore("@").replace(".", " ").replace("_", " ")
            prefix.split(" ").joinToString(" ") { word -> word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() } }
        }
        prefs?.edit()
            ?.putBoolean(KEY_IS_LOGGED_IN, true)
            ?.putBoolean(KEY_GOOGLE_SIGNED_IN, false)
            ?.putString(KEY_LOGIN_TYPE, "EMAIL")
            ?.putString(KEY_USER_NAME, calculatedName.ifBlank { "Estudante Crux" })
            ?.putString(KEY_USER_EMAIL, email.ifBlank { "aluno@cruxtutor.com" })
            ?.apply()

        _isGoogleSignedInFlow.value = false
        _userNameFlow.value = getUserName()
        _userEmailFlow.value = getUserEmail()
            _appLanguageFlow.value = getLanguage()
    }

    fun signOut() {
        prefs?.edit()
            ?.putBoolean(KEY_IS_LOGGED_IN, false)
            ?.putBoolean(KEY_GOOGLE_SIGNED_IN, false)
            ?.remove(KEY_GOOGLE_TOKEN)
            ?.putString(KEY_LOGIN_TYPE, "NONE")
            ?.apply()

        _isGoogleSignedInFlow.value = false
        _userNameFlow.value = "Estudante Crux"
        _userEmailFlow.value = "aluno@cruxtutor.com"
    }

    fun signOutGoogle() {
        signOut()
    }

    fun getUserName(): String {
        return prefs?.getString(KEY_USER_NAME, "Estudante Crux") ?: "Estudante Crux"
    }

    fun getUserEmail(): String {
        return prefs?.getString(KEY_USER_EMAIL, "aluno.crux@gmail.com") ?: "aluno.crux@gmail.com"
    }

    fun updateProfileNameAndEmail(name: String, email: String) {
        prefs?.edit()
            ?.putString(KEY_USER_NAME, name)
            ?.putString(KEY_USER_EMAIL, email)
            ?.apply()
        _userNameFlow.value = name
        _userEmailFlow.value = email
    }

    // Avatar management
    fun setPresetAvatar(presetId: String) {
        prefs?.edit()
            ?.putString(KEY_AVATAR_TYPE, "PRESET")
            ?.putString(KEY_AVATAR_PRESET_ID, presetId)
            ?.remove(KEY_AVATAR_GALLERY_URI)
            ?.apply()
        updateAvatarFlow()
    }

    fun setGalleryAvatarUri(uriString: String) {
        prefs?.edit()
            ?.putString(KEY_AVATAR_TYPE, "GALLERY")
            ?.putString(KEY_AVATAR_GALLERY_URI, uriString)
            ?.apply()
        updateAvatarFlow()
    }

    private fun updateAvatarFlow() {
        val type = prefs?.getString(KEY_AVATAR_TYPE, "PRESET") ?: "PRESET"
        if (type == "GALLERY") {
            val galleryUri = prefs?.getString(KEY_AVATAR_GALLERY_URI, "") ?: ""
            if (galleryUri.isNotBlank()) {
                _avatarUriOrResFlow.value = galleryUri
                return
            }
        }
        val presetId = prefs?.getString(KEY_AVATAR_PRESET_ID, "nano_banana") ?: "nano_banana"
        val found = PRESET_AVATARS.firstOrNull { it.id == presetId } ?: PRESET_AVATARS[0]
        _avatarUriOrResFlow.value = found.resId
    }
}
