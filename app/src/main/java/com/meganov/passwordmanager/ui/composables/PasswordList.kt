package com.meganov.passwordmanager.ui.composables

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.meganov.passwordmanager.PasswordManagerVM
import com.meganov.passwordmanager.data.Site

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun PasswordList(sites: List<Site>?, navController: NavController) {
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
                    IconButton(onClick = { /* TODO: Navigate to change master password screen */ }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More options")
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
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
                            IconButton(onClick = {
                                navController.navigate("edit_site/${site.id}")
                            }) {
                                Icon(Icons.Default.Visibility, "Show Password")
                            }
                        }
                    }
                }
            }
        }
    }
}
