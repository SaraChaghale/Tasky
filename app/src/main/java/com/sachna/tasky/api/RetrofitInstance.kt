package com.sachna.tasky.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object RetrofitInstance {
    private const val BASE_URL = "https://zenquotes.io/api/random/"

    // Creamos la instancia de Retrofit una sola vez, usando "lazy" para que se cree solo cuando se necesite.
    val api: ZenQuotesApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL) // La URL base de la API.
            .addConverterFactory(GsonConverterFactory.create()) // Convierte JSON en objetos Kotlin automáticamente.
            .build()
            .create(ZenQuotesApi::class.java) // Vinculamos la interfaz que define las llamadas a la API.
    }
}
