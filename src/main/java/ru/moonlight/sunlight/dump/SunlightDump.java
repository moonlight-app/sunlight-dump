package ru.moonlight.sunlight.dump;

import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import ru.moonlight.sunlight.dump.api.SunlightApi;
import ru.moonlight.sunlight.dump.model.api.OrderData;

public final class SunlightDump {

    public static void main(String[] args) throws Exception {
        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(JacksonConverterFactory.create())
                .baseUrl("https://api.sunlight.net/v6/")
                .build();

        SunlightApi sunlightApi = retrofit.create(SunlightApi.class);
        Response<OrderData> response = sunlightApi.fetchOrderData(57830, 8).execute();
        System.out.printf("%d (%s)%n", response.code(), response.message());
        System.out.println(response.body());

//        try (SunlightItemDetailsService service = new SunlightItemDetailsService()) {
//            service.collectData();
//        }

//        try (SunlightCatalogService explorer = new SunlightCatalogService()) {
//            explorer.collectData();
//        }
    }

}
