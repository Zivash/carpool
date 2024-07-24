package com.example.carpoolapp.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_details")
data class User(
    @PrimaryKey
    @ColumnInfo(name = "user_id")
    val id: String,
    @ColumnInfo(name = "email")
    val email: String,
    @ColumnInfo(name = "password")
    val password: String,
)