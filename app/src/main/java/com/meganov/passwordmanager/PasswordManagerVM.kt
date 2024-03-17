package com.meganov.passwordmanager

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.util.Base64
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.meganov.passwordmanager.data.AppDatabase
import com.meganov.passwordmanager.data.IconService
import com.meganov.passwordmanager.data.Site
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileOutputStream
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class PasswordManagerVM(application: Application) : AndroidViewModel(application) {

    private val service: IconService // to download icons from the internet

    // icon
    val siteIcon: MutableState<Bitmap?> = mutableStateOf(null)
    private var defaultIcon = true // if not found using service use default
    private val defaultName = "default_icon.png" // default icon name

    // DB
    private val db = Room.databaseBuilder(
        application, AppDatabase::class.java, "passwords_database"
    ).build()
    private val siteDao = db.siteDao()
    val sites: LiveData<List<Site>> = siteDao.getAll()

    // security
    private val secretKey: SecretKey // secret key to encrypt passwords and master password
    private var entered = false // if user entered the app (input proper master password)
    var hasFingerprint: MutableState<Boolean> =
        mutableStateOf(false) // if user register a fingerprint

    init {
        val retrofit = Retrofit.Builder().baseUrl("https://google.com")
            .addConverterFactory(GsonConverterFactory.create()).build()
        service = retrofit.create(IconService::class.java)

        val aesKey = getAesKey()
        secretKey = if (aesKey != null) {
            aesKey
        } else {
            val key = generateAESKey()
            val sharedPref =
                getApplication<Application>().getSharedPreferences("MyPref", Context.MODE_PRIVATE)
            with(sharedPref.edit()) {
                putString("aes_key", Base64.encodeToString(key.encoded, Base64.DEFAULT))
                apply()
            }
            key
        }

        val sharedPref =
            getApplication<Application>().getSharedPreferences("MyPref", Context.MODE_PRIVATE)
        hasFingerprint.value = sharedPref.getBoolean("has_fingerprint", false)
    }

    /**
     * Save site to local DB and download icon locally
     */
    fun saveSite(siteName: String, login: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val iconFile = if (!defaultIcon) {
                val file = File(
                    getApplication<Application>().filesDir,
                    "${siteName.replace('/', '_')}${sites.value?.size}.png"
                )
                FileOutputStream(file).use { out ->
                    siteIcon.value?.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                file
            } else {
                val file = File(getApplication<Application>().filesDir, defaultName)
                if (!file.exists()) {
                    val newFile = File(getApplication<Application>().filesDir, defaultName)
                    FileOutputStream(newFile).use { out ->
                        siteIcon.value?.compress(Bitmap.CompressFormat.PNG, 100, out)
                    }
                }
                file
            }
            val site = Site(
                login = login,
                name = siteName,
                localIconPath = iconFile.absolutePath,
                password = aesEncrypt(password)
            )
            siteDao.insert(site)
        }
    }

    /**
     * Update site after changes
     */
    fun updateSite(site: Site, siteName: String, login: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val iconFile = if (!defaultIcon) {
                val file = File(
                    getApplication<Application>().filesDir, "${siteName.replace('/', '_')}.png"
                )
                FileOutputStream(file).use { out ->
                    siteIcon.value?.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                file
            } else {
                val file = File(getApplication<Application>().filesDir, defaultName)
                if (!file.exists()) {
                    val newFile = File(getApplication<Application>().filesDir, defaultName)
                    FileOutputStream(newFile).use { out ->
                        siteIcon.value?.compress(Bitmap.CompressFormat.PNG, 100, out)
                    }
                }
                file
            }
            val updatedSite = site.copy(
                name = siteName,
                login = login,
                localIconPath = iconFile.absolutePath,
                password = aesEncrypt(password)
            )
            siteDao.update(updatedSite)
        }
    }

    /**
     * Remove site from the DB
     */
    fun removeSite(site: Site) {
        viewModelScope.launch(Dispatchers.IO) {
            if (!site.localIconPath.contains(defaultName)) {
                val iconFile = File(site.localIconPath)
                if (iconFile.exists()) {
                    iconFile.delete()
                }
            }
            siteDao.delete(site)
        }
    }

    /**
     * Load icon from the internet (update siteIcon field)
     */
    fun loadIcon(siteUrl: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val iconUrl = "https://www.google.com/s2/favicons?sz=64&domain=$siteUrl"
            try {
                val response = service.getIcon(iconUrl)
                val iconBytes = response?.bytes()
                var bitmap = BitmapFactory.decodeByteArray(iconBytes, 0, iconBytes?.size ?: 0)
                bitmap = Bitmap.createScaledBitmap(bitmap, 100, 100, true)
                withContext(Dispatchers.Main) {
                    siteIcon.value = bitmap
                }
                defaultIcon = false
            } catch (e: Exception) {
                Log.d("Loading Error", "loadIcon: ${e.message} Use default image.")
                val defaultImageDrawable =
                    ContextCompat.getDrawable(getApplication(), R.drawable.default_image)
                val defaultBitmap = Bitmap.createScaledBitmap(
                    (defaultImageDrawable as BitmapDrawable).bitmap, 100, 100, true
                )
                withContext(Dispatchers.Main) {
                    siteIcon.value = defaultBitmap
                }
                defaultIcon = true
            }
        }
    }

    /**
     * Load icon from local storage (update siteIcon field)
     */
    fun loadIconFromDB(site: Site) {
        val bitmap = BitmapFactory.decodeFile(site.localIconPath)
        if (bitmap != null) {
            siteIcon.value = bitmap
        } else {
            siteIcon.value = BitmapFactory.decodeFile(
                File(
                    getApplication<Application>().filesDir, defaultName
                ).toString()
            )
        }
    }

    /**
     * Remove from VM site-related icon, set to default
     */
    fun emptyInfo() {
        var bitmap = BitmapFactory.decodeFile(
            File(
                getApplication<Application>().filesDir, defaultName
            ).toString()
        )
        siteIcon.value = BitmapFactory.decodeFile(
            File(
                getApplication<Application>().filesDir, defaultName
            ).toString()
        )
        if (bitmap == null) {
            val defaultImageDrawable =
                ContextCompat.getDrawable(getApplication(), R.drawable.default_image)
            bitmap = Bitmap.createScaledBitmap(
                (defaultImageDrawable as BitmapDrawable).bitmap, 100, 100, true
            )
        }
        siteIcon.value = bitmap
        defaultIcon = true
    }

    /**
     * Get master password from shared preferences
     */
    fun getMasterPassword(): String? {
        val sharedPref =
            getApplication<Application>().getSharedPreferences("MyPref", Context.MODE_PRIVATE)
        return sharedPref.getString("master_password", null)
    }

    /**
     * Save master password into shared preferences
     */
    fun saveMasterPassword(masterPassword: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val encryptedMasterPassword = aesEncrypt(masterPassword)
            val sharedPref =
                getApplication<Application>().getSharedPreferences("MyPref", Context.MODE_PRIVATE)
            with(sharedPref.edit()) {
                putString("master_password", encryptedMasterPassword)
                apply()
            }
        }
    }

    /**
     * Check if the device has fingerprint sensor
     */
    private fun hasFingerprintSensor(fragmentActivity: FragmentActivity): Boolean {
        val biometricManager = BiometricManager.from(fragmentActivity)
        return when (biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG
                or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }

    /**
     * Auth method with fingerprint using BiometricPrompt
     */
    fun authenticateWithFingerprint(
        enter: Boolean,
        fragmentActivity: FragmentActivity,
        success: () -> Unit
    ) {
        if (!hasFingerprintSensor(fragmentActivity) || (entered && enter) || (enter && !hasFingerprint.value)) {
            return
        }

        val executor = ContextCompat.getMainExecutor(fragmentActivity)
        val biometricPrompt = BiometricPrompt(
            fragmentActivity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    entered = true
                    hasFingerprint.value = true
                    val sharedPref =
                        getApplication<Application>().getSharedPreferences(
                            "MyPref",
                            Context.MODE_PRIVATE
                        )
                    with(sharedPref.edit()) {
                        putBoolean("has_fingerprint", true)
                        apply()
                    }
                    success()
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Fingerprint Authentication")
            .setSubtitle("Please place your finger on the sensor.")
            .setNegativeButtonText("Cancel")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    fun enter() {
        entered = true
    }

    /**
     * Generate secretkey using AES
     */
    private fun generateAESKey(keySize: Int = 256): SecretKey {
        val keyGenerator = KeyGenerator.getInstance("AES")
        keyGenerator.init(keySize)
        return keyGenerator.generateKey()
    }

    /**
     * Encryption
     */
    private fun aesEncrypt(data: String): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val ivParameterSpec = IvParameterSpec(ByteArray(16))
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec)
        val encrypted = cipher.doFinal(data.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(encrypted, Base64.DEFAULT)
    }

    /**
     * Decryption
     */
    fun aesDecrypt(encryptedData: String): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val ivParameterSpec = IvParameterSpec(ByteArray(16))
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec)
        val decrypted = cipher.doFinal(Base64.decode(encryptedData, Base64.DEFAULT))
        return String(decrypted, Charsets.UTF_8)
    }

    /**
     * Returns aes saved in shared preferences
     */
    private fun getAesKey(): SecretKey? {
        val sharedPref =
            getApplication<Application>().getSharedPreferences("MyPref", Context.MODE_PRIVATE)
        val keyString = sharedPref.getString("aes_key", null) ?: return null
        val decodedKey = Base64.decode(keyString, Base64.DEFAULT)
        return SecretKeySpec(decodedKey, 0, decodedKey.size, "AES")
    }
}
