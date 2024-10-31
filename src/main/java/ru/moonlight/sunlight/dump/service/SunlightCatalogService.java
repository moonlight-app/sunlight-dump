package ru.moonlight.sunlight.dump.service;

import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import ru.moonlight.sunlight.dump.exception.SunlightParseException;
import ru.moonlight.sunlight.dump.model.SunlightCatalogItem;
import ru.moonlight.sunlight.dump.model.attribute.Audience;
import ru.moonlight.sunlight.dump.model.attribute.ProductType;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.stream.IntStream;

public final class SunlightCatalogService implements DumpService {

    private static final int SUNLIGHT_ITEMS_PER_PAGE = 60;

    private final Semaphore semaphore;
    private final ExecutorService executorService;
    private final ObjectWriter jsonWriter;
    private final Path outputDir;

    public SunlightCatalogService() {
        this.semaphore = new Semaphore(MAX_SIMULTANEOUS_CONNECTIONS);
        this.executorService = Executors.newVirtualThreadPerTaskExecutor();

        this.jsonWriter = JsonMapper.builder()
                .build()
                .writerWithDefaultPrettyPrinter();

        this.outputDir = Paths.get("dump").resolve("catalog");
    }

    @Override
    public void collectData() throws IOException {
        long start = System.currentTimeMillis();
        int totalItems = 0;

        for (ProductType productType : ProductType.values()) {
            System.out.printf("Collecting catalog items for item type '%s'...%n", productType.getKey());
            Map<UUID, SunlightCatalogItem> collectedItems = new HashMap<>();

            for (Audience audience : Audience.values()) {
                System.out.printf("  Collecting data for audience '%s/%s'...%n", productType.getKey(), audience.getKey());
                String pageableUrl = generatePageableUrl(productType, audience);

                System.out.println("    Looking at the first page...");
                LookupResult result = lookupCatalogItems(audience, pageableUrl, 1, true);
                if (result == null)
                    return;

                List<SunlightCatalogItem> items = result.items();
                if (items.isEmpty()) {
                    System.err.println("    There are no items on the first page!");
                    continue;
                } else {
                    pickUpCatalogItems(collectedItems, items);
                }

                int totalPages = result.totalPages();
                System.out.printf("    Fetched metadata: %d item(s) on %d page(s)%n", result.totalItems(), totalPages);

                if (totalPages > 1) {
                    System.out.printf("    Collecting data from other pages (%d)...%n", totalPages - 1);
                    List<CompletableFuture<LookupResult>> futures = IntStream.rangeClosed(2, totalPages)
                            .mapToObj(page -> lookupCatalogItemsAsync(audience, pageableUrl, page))
                            .toList();

                    for (CompletableFuture<LookupResult> future : futures) {
                        result = future.join();
                        if (result == null)
                            return;

                        items = result.items();
                        if (items.isEmpty()) {
                            System.err.printf("      Page %d -> no items found!%n", result.page());
                        } else {
                            pickUpCatalogItems(collectedItems, items);
                        }
                    }
                }
            }

            System.out.printf("  Collected %d item(s) with audiences!%n", collectedItems.size());

            System.out.println("  Exporting results...");
            Path outputFile = outputDir.resolve("%s-items.json".formatted(productType.getKey()));

            if (!Files.isDirectory(outputFile.getParent()))
                Files.createDirectories(outputFile.getParent());

            try (Writer writer = Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8, OPEN_OPTIONS)) {
                jsonWriter.writeValue(writer, collectedItems);
            }

            System.out.println();
            totalItems += collectedItems.size();
            collectedItems.clear();
        }

        long timeTook = System.currentTimeMillis() - start;
        System.out.printf("Found %d item(s) in Sunlight catalogs, time took: %.2f sec%n", totalItems, timeTook / 1000D);
    }

    @Override
    public void close() {
        executorService.shutdownNow();
    }

    private CompletableFuture<LookupResult> lookupCatalogItemsAsync(Audience audience, String pageableUrl, int page) {
        return CompletableFuture.supplyAsync(() -> lookupCatalogItems(audience, pageableUrl, page, false), executorService);
    }

    private LookupResult lookupCatalogItems(Audience audience, String pageableUrl, int page, boolean computeMeta) {
        String pageUrl = pageableUrl + page;

        try {
            semaphore.acquire();

            Document document;
            while (true) {
                try {
                    document = Jsoup.connect(pageUrl).userAgent(USER_AGENT).get();
                    break;
                } catch (HttpStatusException ex) {
                    if (ex.getStatusCode() == 503) {
                        System.err.printf("Sunlight server returned 503! Retrying request to '%s' in 3 seconds...%n", ex.getUrl());
                        Thread.sleep(3000L);
                        continue;
                    }

                    throw ex;
                }
            }

            List<SunlightCatalogItem> items = SunlightCatalogItem.lookupItems(audience, document);
            int totalItems = 0, totalPages = 0;

            if (computeMeta) {
                totalItems = lookupNumberOfItems(document);
                totalPages = totalItems / SUNLIGHT_ITEMS_PER_PAGE;
                totalPages += totalItems % SUNLIGHT_ITEMS_PER_PAGE != 0 ? 1 : 0;
            }

            return new LookupResult(page, pageUrl, totalItems, totalPages, items);
        } catch (InterruptedException ignored) {
        } catch (IOException ex) {
            throw new SunlightParseException(ex);
        } finally {
            semaphore.release();
        }

        return null;
    }

    private static void pickUpCatalogItems(Map<UUID, SunlightCatalogItem> collectedItems, List<SunlightCatalogItem> items) {
        for (SunlightCatalogItem item : items) {
            UUID id = item.id();
            collectedItems.merge(id, item, (oldItem, newItem) -> {
                oldItem.audiences().addAll(newItem.audiences());
                return oldItem;
            });
        }
    }

    private static int lookupNumberOfItems(Element element) {
        return Integer.parseInt(element.selectFirst("meta[itemprop=\"numberOfItems\"]").attr("content"));
    }

    private static String generatePageableUrl(ProductType productType, Audience audience) {
        return "%s/catalog/?product_type=%d&gender_position=%d&page=".formatted(
                BASE_URL,
                productType.getSunlightId(),
                audience.getSunlightId()
        );
    }

    private record LookupResult(
            int page,
            String pageUrl,
            int totalItems,
            int totalPages,
            List<SunlightCatalogItem> items
    ) { }

}
