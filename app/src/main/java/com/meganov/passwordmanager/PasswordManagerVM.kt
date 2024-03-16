package com.meganov.passwordmanager

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
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

class PasswordManagerVM(application: Application) : AndroidViewModel(application) {

    private val service: IconService

    val siteIcon: MutableState<Bitmap?> = mutableStateOf(null)
    private var defaultIcon = true
    private val DEFAULT_NAME = "default_icon.png"

    private val db = Room.databaseBuilder(
        application,
        AppDatabase::class.java,
        "passwords_database"
    ).build()
    private val siteDao = db.siteDao()
    val sites: LiveData<List<Site>> = siteDao.getAll()

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://google.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        service = retrofit.create(IconService::class.java)
    }

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
                val file = File(getApplication<Application>().filesDir, DEFAULT_NAME)
                if (!file.exists()) {
                    val file = File(getApplication<Application>().filesDir, DEFAULT_NAME)
                    FileOutputStream(file).use { out ->
                        siteIcon.value?.compress(Bitmap.CompressFormat.PNG, 100, out)
                    }
                }
                file
            }
            val site = Site(
                login = login,
                name = siteName,
                localIconPath = iconFile.absolutePath,
                password = password
            )
            siteDao.insert(site)
        }
    }

    fun updateSite(site: Site, siteName: String, login: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val iconFile = if (!defaultIcon) {
                val file = File(
                    getApplication<Application>().filesDir,
                    "${siteName.replace('/', '_')}.png"
                )
                FileOutputStream(file).use { out ->
                    siteIcon.value?.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                file
            } else {
                val file = File(getApplication<Application>().filesDir, DEFAULT_NAME)
                if (!file.exists()) {
                    val file = File(getApplication<Application>().filesDir, DEFAULT_NAME)
                    FileOutputStream(file).use { out ->
                        siteIcon.value?.compress(Bitmap.CompressFormat.PNG, 100, out)
                    }
                }
                file
            }
            val updatedSite = site.copy(
                name = siteName,
                login = login,
                localIconPath = iconFile.absolutePath,
                password = password
            )
            siteDao.update(updatedSite)
        }
    }

    fun removeSite(site: Site) {
        viewModelScope.launch(Dispatchers.IO) {
            if (!site.localIconPath.contains(DEFAULT_NAME)) {
                val iconFile = File(site.localIconPath)
                if (iconFile.exists()) {
                    iconFile.delete()
                }
            }
            siteDao.delete(site)
        }
    }

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

    fun loadIconFromDB(site: Site) {
        val bitmap = BitmapFactory.decodeFile(site.localIconPath)
        if (bitmap != null) {
            siteIcon.value = bitmap
        } else {
            siteIcon.value = BitmapFactory.decodeFile(
                File(
                    getApplication<Application>().filesDir,
                    DEFAULT_NAME
                ).toString()
            )
        }
    }

    fun emptyInfo() {
        var bitmap = BitmapFactory.decodeFile(
            File(
                getApplication<Application>().filesDir,
                DEFAULT_NAME
            ).toString()
        )
        siteIcon.value = BitmapFactory.decodeFile(
            File(
                getApplication<Application>().filesDir,
                DEFAULT_NAME
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
}
