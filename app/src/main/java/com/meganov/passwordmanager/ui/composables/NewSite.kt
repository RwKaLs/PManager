package com.meganov.passwordmanager.ui.composables

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.drawable.Icon
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.currentCompositionLocalContext
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.meganov.passwordmanager.PasswordManagerVM
import com.meganov.passwordmanager.data.Site
import kotlin.coroutines.coroutineContext

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun NewSite(
    siteIcon: MutableState<Bitmap?>,
    loadIcon: (String) -> Unit,
    saveSite: (String, String, String) -> Unit,
    navController: NavController
) {
    var site by rememberSaveable { mutableStateOf("") }
    var login by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisibility by rememberSaveable { mutableStateOf(false) }
    var emptyText by rememberSaveable { mutableStateOf("") }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopBar(
                title = "Add New",
                navController = navController,
                actions = {
                    IconButton(onClick = {
                        if (site.isNotEmpty() || login.isNotEmpty() || password.isNotEmpty()) {
                            emptyText = ""
                            saveSite(site, login, password)
                            navController.navigate("password_list")
                        } else {
                            emptyText = "Fields are empty"
                        }
                    }) {
                        Icon(Icons.Default.Check, contentDescription = "Save")
                    }
                }
            )
        }
    ) {
        Column {
            Spacer(modifier = Modifier.padding(top = 56.dp, bottom = 70.dp))
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                contentAlignment = Alignment.Center
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    siteIcon.value?.asImageBitmap()
                        ?.let { it1 -> Image(bitmap = it1, contentDescription = "Site Icon") }
                    Spacer(modifier = Modifier.height(20.dp))
                    TextField(
                        value = site,
                        singleLine = true,
                        onValueChange = {
                            site = it
                            if (site.isNotEmpty()) loadIcon(site)
                        },
                        label = { Text("Site") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(15.dp))
                    TextField(
                        value = login,
                        singleLine = true,
                        onValueChange = { login = it },
                        label = { Text("Login") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(15.dp))
                    TextField(
                        value = password,
                        singleLine = true,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisibility = !passwordVisibility }) {
                                Icon(
                                    if (passwordVisibility) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle password visibility"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(text = emptyText)
                }
            }
        }
    }
}
