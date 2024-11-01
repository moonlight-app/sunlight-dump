package ru.moonlight.sunlight.dump;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.Getter;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import ru.moonlight.sunlight.dump.api.SunlightApi;
import ru.moonlight.sunlight.dump.cache.DumpCache;
import ru.moonlight.sunlight.dump.cli.SunlightDumpConsole;
import ru.moonlight.sunlight.dump.model.SunlightCatalogItem;
import ru.moonlight.sunlight.dump.model.SunlightFullItem;
import ru.moonlight.sunlight.dump.model.SunlightItemDetails;
import ru.moonlight.sunlight.dump.service.SunlightCatalogService;
import ru.moonlight.sunlight.dump.service.SunlightItemDetailsService;

import java.nio.file.Path;
import java.nio.file.Paths;

import static ru.moonlight.sunlight.dump.Constants.BASE_API_URL;

public final class SunlightDump {

    @Getter private final Path dumpDir;
    @Getter private final DumpCache<SunlightCatalogItem> catalogItemsCache;
    @Getter private final DumpCache<SunlightItemDetails> itemDetailsCache;
    @Getter private final DumpCache<SunlightFullItem> fullItemsCache;

    private JsonMapper jsonMapper;
    private Retrofit retrofit;
    private SunlightApi sunlightApi;

    private SunlightCatalogService catalogService;
    private SunlightItemDetailsService itemDetailsService;

    public SunlightDump() {
        this.dumpDir = Paths.get("dump");
        this.catalogItemsCache = new DumpCache<>();
        this.itemDetailsCache = new DumpCache<>();
        this.fullItemsCache = new DumpCache<>();
    }

    public void runApplication() {
        try (SunlightDumpConsole console = new SunlightDumpConsole(this)) {
            console.openAndWait();
            System.out.println("Goodbye!");
        }
    }

    public JsonMapper getJsonMapper() {
        if (jsonMapper == null)
            this.jsonMapper = JsonMapper.builder()
                    .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                    .build();

        return jsonMapper;
    }

    public Retrofit getRetrofit() {
        if (retrofit == null)
            this.retrofit = new Retrofit.Builder()
                    .addConverterFactory(JacksonConverterFactory.create(getJsonMapper()))
                    .baseUrl(BASE_API_URL)
                    .build();

        return retrofit;
    }

    public SunlightApi getSunlightApi() {
        if (sunlightApi == null)
            this.sunlightApi = getRetrofit().create(SunlightApi.class);

        return sunlightApi;
    }

    public SunlightCatalogService getCatalogService() {
        if (catalogService == null)
            this.catalogService = new SunlightCatalogService(this);

        return catalogService;
    }

    public SunlightItemDetailsService getItemDetailsService() {
        if (itemDetailsService == null)
            this.itemDetailsService = new SunlightItemDetailsService(this);

        return itemDetailsService;
    }

    public static void main(String[] args) {
        new SunlightDump().runApplication();
    }

}
