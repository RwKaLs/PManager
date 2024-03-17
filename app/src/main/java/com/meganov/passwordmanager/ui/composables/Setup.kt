package com.meganov.passwordmanager.ui.composables

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController

/**
 * Cold boot screen
 * Initialize the master password and fingerprint (if available)
 */
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SetupScreen(
    enter: () -> Unit,
    saveMasterPassword: (String) -> Unit,
    fragmentActivity: FragmentActivity,
    authenticateWithFingerprint: (Boolean, FragmentActivity, () -> Unit) -> Unit,
    navController: NavController
) {
    var masterPassword by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var passwordVisibility by rememberSaveable { mutableStateOf(false) }
    var infoText by rememberSaveable { mutableStateOf("") }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 70.dp)
                .padding(8.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Column {
                OutlinedTextField(
                    label = { Text(text = "Enter Master Password") },
                    value = masterPassword,
                    singleLine = true,
                    onValueChange = { masterPassword = it },
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
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    label = { Text(text = "Confirm Master Password") },
                    value = confirmPassword,
                    singleLine = true,
                    onValueChange = { confirmPassword = it },
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
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = infoText, color = Color.Red)
                Spacer(modifier = Modifier.height(8.dp))
                Button(modifier = Modifier.align(Alignment.End), onClick = {
                    if (masterPassword == confirmPassword && masterPassword.length >= 6) {
                        saveMasterPassword(masterPassword)
                        authenticateWithFingerprint(false, fragmentActivity) {
                            navController.navigate("password_list")
                        }
                        enter()
                        navController.navigate("password_list")
                    } else if (masterPassword != confirmPassword) {
                        infoText = "Passwords don't match"
                    } else {
                        infoText = "Length should be at least 6 chars"
                    }
                }) {
                    Text("Save")
                }
            }
        }
    }
}
