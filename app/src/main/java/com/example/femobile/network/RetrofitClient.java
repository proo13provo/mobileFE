package com.example.femobile.network;

import android.content.Context;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import com.example.femobile.*;
import com.example.femobile.service.api.AlbumApi;
import com.example.femobile.service.api.AuthApi;
import com.example.femobile.service.api.PlayListApi;
import com.example.femobile.service.api.SongApi;
import com.example.femobile.service.api.ArtistAPI;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;


public class RetrofitClient {
    private static Retrofit retrofit = null;


    public static Retrofit getClient(Context context) {
        String baseUrl = BuildConfig.BASE_URL;

        if (retrofit == null) {
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(new AuthInterceptor(context))  // Thêm Interceptor
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BuildConfig.BASE_URL)
                    .client(getUnsafeOkHttpClient(context))
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

        }
        return retrofit;
    }

    // Hàm này giúp lấy service API dễ dàng hơn
    public static AuthApi getAuthService(Context context) {
        return getClient(context).create(AuthApi.class);
    }

    private static OkHttpClient getUnsafeOkHttpClient(Context context) {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(X509Certificate[] chain, String authType) {}
                        @Override
                        public void checkServerTrusted(X509Certificate[] chain, String authType) {}
                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[]{};
                        }
                    }
            };

            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new SecureRandom());

            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
            builder.hostnameVerifier((hostname, session) -> true); // Bỏ kiểm tra hostname
            builder.addInterceptor(new AuthInterceptor(context));

            return builder.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public static SongApi getApiService(Context context) {
        return getClient(context).create(SongApi.class);
    }
    public static AlbumApi getApialbum(Context context){
        return getClient(context).create(AlbumApi.class);
    }
    public static ArtistAPI getArtistApi(Context context){
        return getClient(context).create(ArtistAPI.class);
    }
    public static PlayListApi getplayListApi(Context context){
        return getClient(context).create(PlayListApi.class);
    }
}
