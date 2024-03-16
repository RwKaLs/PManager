package com.meganov.passwordmanager.data

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Url

interface IconService {
    @GET
    suspend fun getIcon(@Url url: String): ResponseBody?
}
