package com.example.kindergarten.Networking;


import com.example.kindergarten.Object.AddFaceResponse;
import com.example.kindergarten.Object.FaceData;
import com.example.kindergarten.Object.ListOfStudent;
import com.example.kindergarten.Object.User;
import com.example.kindergarten.Object.attendant;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;


public interface ApiConfig {
    @Multipart
    @POST("api/v1/front/login")
    Call<User> login(@Part("email") RequestBody email,
                     @Part("password") RequestBody password);

    @POST("api/v1/auth/logout/")
    Call<ResponseBody> logout();

    @Headers({"Content-Type: application/json"})
    @POST("api/v1/add-face")
    Call<ResponseBody> addFace(@Body FaceData faceData);

    @Headers({"Content-Type: application/json"})
    @POST("api/v1/user-present")
    Call<ResponseBody> attendance(@Body attendant faceData);


    @GET("api/v1/children/")
    Call<ListOfStudent> getListOfStudent(@Query("fields") String fields);

}
