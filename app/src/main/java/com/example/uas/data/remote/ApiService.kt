package com.example.uas.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    @FormUrlEncoded
    @POST("users.php?action=login")
    fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<LoginResponse>

    @FormUrlEncoded
    @POST("users.php?action=register")
    fun register(
        @Field("email") email: String,
        @Field("password") password: String,
        @Field("name") name: String,
        @Field("phone") phone: String,
        @Field("address") address: String
    ): Call<RegisterResponse>

    @GET("users.php?action=me")
    fun getUser(@Query("email") email: String): Call<UserResponse>

    @FormUrlEncoded
    @POST("users.php?action=update")
    fun updateUser(
        @Field("email") email: String,
        @Field("name") name: String?,
        @Field("phone") phone: String?,
        @Field("address") address: String?
    ): Call<BaseResponse>
}

data class LoginResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("error") val error: String?,
    @SerializedName("user_id") val userId: Int?,
    @SerializedName("email") val email: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("phone") val phone: String?,
    @SerializedName("address") val address: String?
)

data class RegisterResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("error") val error: String?,
    @SerializedName("user_id") val userId: Int?
)

data class UserResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("error") val error: String?,
    @SerializedName("user_id") val userId: Int?,
    @SerializedName("email") val email: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("phone") val phone: String?,
    @SerializedName("address") val address: String?
)

data class BaseResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("error") val error: String?
)
