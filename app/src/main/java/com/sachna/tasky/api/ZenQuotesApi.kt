package com.sachna.tasky.api

import retrofit2.Call
import retrofit2.http.GET


interface ZenQuotesApi {
    @GET("random") // El endpoint para obtener una cita aleatoria.
    fun getDailyQuote(): Call<List<QuoteResponse>> // La API devuelve un array de citas.

}