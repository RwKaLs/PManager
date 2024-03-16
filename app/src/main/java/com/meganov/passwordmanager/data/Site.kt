package com.meganov.passwordmanager.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Site(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val login: String,
    val localIconPath: String,
    val password: String
)
