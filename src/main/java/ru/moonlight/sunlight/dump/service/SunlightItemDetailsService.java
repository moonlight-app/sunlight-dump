package ru.moonlight.sunlight.dump.service;

import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import ru.moonlight.sunlight.dump.model.SunlightItemDetails;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public final class SunlightItemDetailsService implements DumpService {

    private final Semaphore semaphore;
    private final ExecutorService executorService;
    private final ObjectWriter jsonWriter;
    private final Path outputDir;

    public SunlightItemDetailsService() {
        this.semaphore = new Semaphore(MAX_SIMULTANEOUS_CONNECTIONS);
        this.executorService = Executors.newVirtualThreadPerTaskExecutor();

        this.jsonWriter = JsonMapper.builder()
                .build()
                .writerWithDefaultPrettyPrinter();

        this.outputDir = Paths.get("dump").resolve("details");
    }

    @Override
    public void collectData() throws Exception {
        Document document = Jsoup.connect("https://sunlight.net/catalog/ring_130995.html")
                .userAgent(USER_AGENT)
                .get();

        SunlightItemDetails itemDetails = SunlightItemDetails.fromElement(document);
        System.out.println(itemDetails);
    }

    @Override
    public void close() throws Exception {

    }

}
