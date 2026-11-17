package ma.projet.restclient.config;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

public final class RetrofitClient {

    public static final String FORMAT_JSON = "JSON";
    public static final String FORMAT_XML = "XML";

    private static final String BASE_URL = "http://10.0.2.2:8082/";

    private static Retrofit retrofit = null;
    private static String currentFormat = null;

    private RetrofitClient() {
    }

    public static synchronized Retrofit getClient(String converterType) {
        if (retrofit != null && converterType.equals(currentFormat)) {
            return retrofit;
        }

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC);

        OkHttpClient httpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .addInterceptor(new NetworkMetrics())
                .addInterceptor(logging)
                .build();

        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(httpClient);

        if (FORMAT_XML.equals(converterType)) {
            builder.addConverterFactory(SimpleXmlConverterFactory.createNonStrict());
        } else {
            builder.addConverterFactory(GsonConverterFactory.create());
        }

        currentFormat = converterType;
        retrofit = builder.build();
        return retrofit;
    }
}
