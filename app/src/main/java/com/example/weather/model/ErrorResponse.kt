package com.example.weather.model

data class ErrorResponse(
    val error: ErrorNested
)

data class ErrorNested(
    val code: Int,
    val message: String
)