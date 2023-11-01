package com.example.medicaredoctor.Interface

import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface TwilioApiService {
    @FormUrlEncoded
    @POST("2010-04-01/Accounts/{accountSid}/Messages.json")
    fun sendSMS(
        @Path("accountSid") accountSid: String,
        @Field("To") toPhoneNumber: String, // Replace @SafeParcelable.Field with @Field
        @Field("From") fromPhoneNumber: String,
        @Field("Body") message: String
    ): Call<Void>
}

fun createTwilioApiService(): TwilioApiService {
    val accountSid = "ACade57ae65880ce3b2511b46976c6e3f0"
    val authToken = "1bbf43a7e99968e916acf6e669024360"

    val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val original = chain.request()
            val requestBuilder = original.newBuilder()
                .header("Authorization", okhttp3.Credentials.basic(accountSid, authToken))
            val request = requestBuilder.build()
            chain.proceed(request)
        }
        .build()

    val retrofit = Retrofit.Builder()
        .baseUrl("https://api.twilio.com/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    return retrofit.create(TwilioApiService::class.java)
}
