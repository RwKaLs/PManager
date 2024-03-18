package com.meganov.passwordmanager.ui.composables

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import com.meganov.passwordmanager.data.Site

/**
 * List of passwords
 * By clicking on password card edit screen appears
 */
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun PasswordList(
    sites: List<Site>?,
    fragmentActivity: FragmentActivity,
    decrypt: (String) -> String,
    realPassword: String,
    authenticateWithFingerprint: (Boolean, FragmentActivity, () -> Unit) -> Unit,
    navController: NavController
) {
    var showDialog by rememberSaveable { mutableStateOf(false) }
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navController.navigate("new_site")
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Site")
            }
        },
        topBar = {
            TopBar(
                title = "Passwords",
                navController = navController,
                showBackButton = false,
                actions = {
                    // Go to change master password screen
                    IconButton(onClick = {
                        showDialog = true
                        authenticateWithFingerprint(false, fragmentActivity) {
                            showDialog = false
                            navController.navigate("change_master")
                        }
                    }) {
                        Icon(Icons.Default.Key, contentDescription = "Change master")
                    }
                }
            )
        }
    ) {
        Column {
            Spacer(modifier = Modifier.height(56.dp))
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(3.dp)
            ) {
                items(sites ?: emptyList()) { site ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable(onClick = { navController.navigate("edit_site/${site.id}")})) {
                            val bitmap = BitmapFactory.decodeFile(site.localIconPath)
                            if (bitmap != null)
                                Image(
                                    bitmap.asImageBitmap(),
                                    contentDescription = "Site Icon",
                                    modifier = Modifier.size(48.dp)
                                )
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 8.dp)
                            ) {
                                Text(
                                    site.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 1
                                )
                                Text(
                                    site.login,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = "Show Password",
                                modifier = Modifier.padding(3.dp)
                            )
                        }
                    }
                }
            }
        }
    }
    var password by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf(false) }
    val decryptedPassword = decrypt(realPassword)
    Log.d("AAA", "PasswordList: $decryptedPassword")
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Enter Password") },
            text = {
                TextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    isError = error,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (password == decryptedPassword) {
                        error = false
                        navController.navigate("change_master")
                    } else {
                        error = true
                    }
                }) {
                    Text("Submit")
                }
            }
        )
    }
}
