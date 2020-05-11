package br.com.petgramapp.retrofit;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitMessage {
    public static String BASE_URL = "https://fcm.googleapis.com/fcm/";

    public static Retrofit getMessageRetrofit(){
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }
}
