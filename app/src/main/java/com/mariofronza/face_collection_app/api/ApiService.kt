package com.mariofronza.face_collection_app.api

import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface ApiService {

    @POST("sessions")
    suspend fun signIn(@Body request: SessionRequest): Response<Session>

    @POST("refresh-tokens")
    suspend fun refresh(@Body request: RefreshTokenRequest): Response<Session>

    @POST("students")
    suspend fun signUp(@Body request: SignUpRequest): Response<Student>

    @PUT("profiles/student")
    suspend fun updateProfile(@Body request: ProfileRequest): Response<User>

    @GET("photos")
    suspend fun photos(): Response<List<Photo>>

    @Multipart
    @POST("photos")
    suspend fun createPhoto(@Query("photoType") photoType: String, @Part file: MultipartBody.Part): Response<Photo>

    @Multipart
    @PUT("photos/{id}")
    suspend fun replacePhoto(@Path("id") id: Int, @Part file: MultipartBody.Part): Response<Photo>

    @GET("classes/teacher")
    suspend fun lectures(@Query("limit") limit: Int = 100): Response<Page<Lecture>>

    @Multipart
    @POST("recognition/recognize/{classId}")
    suspend fun recognize(@Path("classId") classId: Int, @Part file: MultipartBody.Part): Response<Recognition>

    @Multipart
    @POST("recognition/update/{classId}/student/{studentId}")
    suspend fun teach(
        @Path("classId") classId: Int,
        @Path("studentId") studentId: Int,
        @Part file: MultipartBody.Part,
    ): Response<Unit>

    @PUT("attendances/{id}")
    suspend fun setPresence(@Path("id") attendanceId: Int, @Body request: PresenceRequest): Response<Unit>

    companion object {
        fun create(baseUrl: String, store: SessionStore): ApiService {
            val base = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(1, TimeUnit.MINUTES)
                .writeTimeout(1, TimeUnit.MINUTES)
                .build()

            val client = base.newBuilder()
                .addInterceptor(AuthInterceptor(store))
                .authenticator(TokenAuthenticator(baseUrl, store, base))
                .build()

            return Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
                .create(ApiService::class.java)
        }
    }
}
