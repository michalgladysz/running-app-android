package com.runningapp.app.data.remote.dto

data class RegisterRequestDTO (
    val username: String,
    val email: String,
    val plainPassword: String
)