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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
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
 * Access to app with master password or fingerprint
 */
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MasterPasswordScreen(
    enter: () -> Unit, // keep track of logging user
    navController: NavController,
    decrypt: (String) -> String,
    hasFingerprint: MutableState<Boolean>,
    fragmentActivity: FragmentActivity,
    authenticateWithFingerprint: (Boolean, FragmentActivity, () -> Unit) -> Unit,
    realPassword: String
) {
    var masterPassword by rememberSaveable { mutableStateOf("") }
    var wrongPassword by rememberSaveable { mutableStateOf(false) }
    var passwordVisibility by rememberSaveable { mutableStateOf(false) }
    val decryptedPassword = decrypt(realPassword)
    authenticateWithFingerprint(true, fragmentActivity) {
        navController.navigate("password_list")
    }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 100.dp)
                .padding(8.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Column {
                OutlinedTextField(
                    label = { Text(text = "Master Password") },
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
                    modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                if (wrongPassword) Text(text = "Wrong password", color = Color.Red)
                Spacer(modifier = Modifier.height(8.dp))
                if (hasFingerprint.value) TextButton(
                    modifier = Modifier.align(Alignment.Start),
                    onClick = {
                        authenticateWithFingerprint(true, fragmentActivity) {
                            navController.navigate("password_list")
                        }
                    }
                ) {
                    Text(text = "Use fingerprint")
                }
                Button(modifier = Modifier.align(Alignment.End), onClick = {
                    if (decryptedPassword == masterPassword) {
                        enter()
                        navController.navigate("password_list")
                    } else {
                        wrongPassword = true
                    }
                }) {
                    Text("Submit")
                }
            }
        }
    }
}
