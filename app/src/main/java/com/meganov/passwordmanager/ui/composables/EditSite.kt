package com.meganov.passwordmanager.ui.composables

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.meganov.passwordmanager.data.Site

/**
 * Edit each field of the site after clicking edit button on the top
 * Reload the image in case of changes
 * Remove site by clicking trash button
 */
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun EditSite(
    siteIcon: MutableState<Bitmap?>,
    site: Site,
    saveSite: (Site, String, String, String) -> Unit,
    removeSite: (Site) -> Unit,
    loadIcon: (String) -> Unit,
    loadIconFromDB: (Site) -> Unit,
    decrypt: (String) -> String,
    navController: NavController
) {
    var siteName by rememberSaveable { mutableStateOf(site.name) }
    var login by rememberSaveable { mutableStateOf(site.login) }
    var password by rememberSaveable { mutableStateOf(decrypt(site.password)) }
    var passwordVisibility by rememberSaveable { mutableStateOf(false) }
    var showDialog by rememberSaveable { mutableStateOf(false) }
    var readOnly by rememberSaveable { mutableStateOf(true) }
    loadIconFromDB(site)
    val clipboardManager =
        LocalContext.current.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopBar(
                title = site.name,
                navController = navController,
                actions = {
                    IconButton(
                        onClick = {
                            if (readOnly) {
                                readOnly = false
                            } else {
                                saveSite(site, siteName, login, password)
                                navController.navigate("password_list")
                            }
                        }
                    ) {
                        Icon(
                            if (readOnly) Icons.Default.Edit else Icons.Default.Check,
                            "Edit fields"
                        )
                    }
                    IconButton(onClick = {
                        showDialog = true
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Remove")
                    }
                }
            )
        }
    ) {
        Column {
            Spacer(modifier = Modifier.padding(top = 56.dp, bottom = 70.dp))
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    siteIcon.value?.asImageBitmap()?.let { it1 ->
                        Image(
                            it1,
                            contentDescription = "Site Icon",
                            modifier = Modifier.size(48.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    OutlinedTextField(
                        value = siteName,
                        readOnly = readOnly,
                        singleLine = true,
                        onValueChange = {
                            siteName = it
                            loadIcon(siteName)
                        },
                        label = { Text("Site") },
                        trailingIcon = {
                            IconButton(onClick = {
                                copyToClipboard(
                                    clipboardManager,
                                    "Site Name",
                                    siteName
                                )
                            }) {
                                Icon(
                                    Icons.Default.ContentCopy,
                                    contentDescription = "Copy Site Name"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(15.dp))
                    OutlinedTextField(
                        value = login,
                        readOnly = readOnly,
                        singleLine = true,
                        onValueChange = { login = it },
                        label = { Text("Login") },
                        trailingIcon = {
                            IconButton(onClick = {
                                copyToClipboard(
                                    clipboardManager,
                                    "Login",
                                    login
                                )
                            }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy Login")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(15.dp))
                    OutlinedTextField(
                        value = password,
                        readOnly = readOnly,
                        singleLine = true,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            Row {
                                IconButton(onClick = { passwordVisibility = !passwordVisibility }) {
                                    Icon(
                                        if (passwordVisibility) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle password visibility"
                                    )
                                }
                                IconButton(onClick = {
                                    copyToClipboard(
                                        clipboardManager,
                                        "Password",
                                        password
                                    )
                                }) {
                                    Icon(
                                        Icons.Default.ContentCopy,
                                        contentDescription = "Copy Password"
                                    )
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(15.dp))
                }
            }
        }
    }
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Confirm Delete") },
            text = { Text("Are you sure you want to delete this site?") },
            confirmButton = {
                Button(onClick = {
                    removeSite(site)
                    navController.navigate("password_list")
                    showDialog = false
                }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

fun copyToClipboard(clipboardManager: ClipboardManager, label: String, text: String) {
    val clip = ClipData.newPlainText(label, text)
    clipboardManager.setPrimaryClip(clip)
}
