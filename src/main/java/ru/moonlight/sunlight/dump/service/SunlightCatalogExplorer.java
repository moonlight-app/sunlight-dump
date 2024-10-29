package ru.moonlight.sunlight.dump.service;

import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import ru.moonlight.sunlight.dump.exception.SunlightParseException;
import ru.moonlight.sunlight.dump.model.SunlightCatalogItem;
import ru.moonlight.sunlight.dump.model.SunlightSupremeItem;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.stream.IntStream;

public final class SunlightCatalogExplorer implements DumpService {

    private final Semaphore semaphore;
    private final ExecutorService executorService;
    private final ObjectWriter jsonWriter;
    private final Path outputDir;

    private final Map<SunlightSupremeItem, List<SunlightCatalogItem>> collectedItems;

    public SunlightCatalogExplorer() {
        this.semaphore = new Semaphore(16);
        this.executorService = Executors.newVirtualThreadPerTaskExecutor();

        this.jsonWriter = JsonMapper.builder()
                .build()
                .writerWithDefaultPrettyPrinter();

        this.outputDir = Paths.get("dump").resolve("catalog");

        this.collectedItems = new HashMap<>();
    }

    @Override
    public void collectData() throws IOException {
        for (SunlightSupremeItem supremeItem : SunlightSupremeItem.values()) {
            System.out.printf("Collecting catalog items for item type '%s'...%n", supremeItem.name());

            String catalogUrl = supremeItem.getUrl();
            String baseCatalogUrl = catalogUrl.endsWith(".html") ? catalogUrl.substring(0, catalogUrl.length() - 5) : catalogUrl;
            System.out.printf("  Collecting data on %d page(s)...%n", supremeItem.getTotalPages());

            List<CompletableFuture<LookupResult>> futures = IntStream.rangeClosed(1, supremeItem.getTotalPages())
                    .mapToObj(page -> CompletableFuture.supplyAsync(() -> lookupCatalogItems(baseCatalogUrl, page), executorService))
                    .toList();

            List<SunlightCatalogItem> collectedItems = new ArrayList<>();
            for (CompletableFuture<LookupResult> future : futures) {
                LookupResult result = future.join();
                if (result == null)
                    continue;

                if (result.items().isEmpty()) {
                    System.err.printf("    Page %d -> no items found!%n", result.page());
                } else {
                    System.out.printf("    Page %d -> %d item(s) found!%n", result.page(), result.items().size());
                    collectedItems.addAll(result.items());
                }
            }

            System.out.printf("  Collected %d item(s) from %d page(s)%n", collectedItems.size(), supremeItem.getTotalPages());
            System.out.println("  Exporting results...");
            collectedItems.sort(Comparator.comparingInt(SunlightCatalogItem::index));

            Path outputFile = outputDir.resolve("%s-items.json".formatted(supremeItem.getKey()));

            if (!Files.isDirectory(outputFile.getParent()))
                Files.createDirectories(outputFile.getParent());

            try (Writer writer = Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8, OPEN_OPTIONS)) {
                jsonWriter.writeValue(writer, collectedItems);
            }

            System.out.println();
            this.collectedItems.put(supremeItem, collectedItems);
        }
    }

    @Override
    public void close() {
        executorService.shutdownNow();
    }

    public List<SunlightCatalogItem> getCollectedItems(SunlightSupremeItem supremeItem) {
        return collectedItems.get(supremeItem);
    }

    private LookupResult lookupCatalogItems(String baseCatalogUrl, int page) {
        String pageUrl = baseCatalogUrl + "/page-" + page + "/";

        try {
            semaphore.acquire();
            Document document = Jsoup.connect(pageUrl).userAgent(USER_AGENT).get();
            List<SunlightCatalogItem> items = SunlightCatalogItem.lookupItems(document);
            return new LookupResult(page, pageUrl, items);
        } catch (InterruptedException ignored) {
        } catch (IOException ex) {
            throw new SunlightParseException(ex);
        } finally {
            semaphore.release();
        }

        return null;
    }

    private record LookupResult(
            int page,
            String pageUrl,
            List<SunlightCatalogItem> items
    ) { }

}
