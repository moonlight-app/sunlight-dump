package ru.moonlight.sunlight.dump.service;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import ru.moonlight.sunlight.dump.SunlightDump;
import ru.moonlight.sunlight.dump.cache.DumpCache;
import ru.moonlight.sunlight.dump.exception.SunlightParseException;
import ru.moonlight.sunlight.dump.model.SunlightCatalogItem;
import ru.moonlight.sunlight.dump.model.attribute.Audience;
import ru.moonlight.sunlight.dump.model.attribute.ProductType;
import ru.moonlight.sunlight.dump.util.SunlightConnector;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.stream.IntStream;

import static ru.moonlight.sunlight.dump.Constants.*;

public final class SunlightCatalogService implements DumpService {

    private static final int SUNLIGHT_ITEMS_PER_PAGE = 60;

    private final SunlightDump application;
    private final Semaphore semaphore;
    private final ExecutorService executorService;
    private final DumpCache<SunlightCatalogItem> cache;

    public SunlightCatalogService(SunlightDump application) {
        this.application = application;
        this.semaphore = new Semaphore(MAX_SIMULTANEOUS_CONNECTIONS);
        this.executorService = Executors.newVirtualThreadPerTaskExecutor();
        this.cache = application.getCatalogItemsCache();
    }

    @Override
    public void runService() throws IOException {
        cache.clear();
        long start = System.currentTimeMillis();

        for (Audience audience : Audience.values()) {
            System.out.printf("Collecting catalog items for audience '%s'...%n", audience.getKey());

            System.out.println("  Looking at the first page...");
            LookupResult result = lookupCatalogItems(audience, 1, true);
            if (result == null)
                continue;

            List<SunlightCatalogItem> items = result.items();
            if (items.isEmpty()) {
                System.out.println("  There are no items on the first page!");
                continue;
            } else {
                cache.saveAll(items, SunlightCatalogService::mergeCatalogItems);
            }

            int totalPages = result.totalPages();
            System.out.printf("  Fetched metadata: %d item(s) on %d page(s)%n", result.totalItems(), totalPages);

            if (totalPages > 1) {
                System.out.printf("  Collecting data from other pages (%d)...%n", totalPages - 1);
                List<CompletableFuture<LookupResult>> futures = IntStream.rangeClosed(2, totalPages)
                        .mapToObj(page -> lookupCatalogItemsAsync(audience, page))
                        .toList();

                for (CompletableFuture<LookupResult> future : futures) {
                    result = future.join();
                    if (result == null)
                        continue;

                    items = result.items();
                    if (items.isEmpty()) {
                        System.err.printf("    Page %d -> no items found!%n", result.page());
                    } else {
                        cache.saveAll(items, SunlightCatalogService::mergeCatalogItems);
                    }
                }
            }
        }

        System.out.println();
        System.out.println("Exporting results...");
        Path outputFile = application.getDumpDir().resolve(DUMP_NAME_CATALOG_ITEMS);
        cache.exportDump(outputFile, application.getJsonMapper());

        long timeTook = System.currentTimeMillis() - start;
        System.out.printf("Found %d item(s) in Sunlight catalogs, time took: %.2f sec%n", cache.size(), timeTook / 1000D);
    }

    @Override
    public void close() {
        executorService.shutdownNow();
    }

    private CompletableFuture<LookupResult> lookupCatalogItemsAsync(Audience audience, int page) {
        return CompletableFuture.supplyAsync(() -> lookupCatalogItems(audience, page, false), executorService);
    }

    private LookupResult lookupCatalogItems(Audience audience, int page, boolean computeMeta) {
        String pageUrl = generatePageUrl(audience, page);

        try {
            Document document;
            try {
                semaphore.acquire();
                document = SunlightConnector.download(pageUrl);
            } finally {
                semaphore.release();
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
        }

        return null;
    }

    private static SunlightCatalogItem mergeCatalogItems(SunlightCatalogItem oldItem, SunlightCatalogItem newItem) {
        oldItem.audiences().addAll(newItem.audiences());
        return oldItem;
    }

    private static int lookupNumberOfItems(Element element) {
        return Integer.parseInt(Objects.requireNonNull(element.selectFirst("meta[itemprop=\"numberOfItems\"]")).attr("content"));
    }

    private static String generatePageUrl(Audience audience, int page) {
        if (audience != Audience.ALL) {
            return "%s/catalog/?product_type=%s&gender_position=%d&page=%d".formatted(
                    BASE_HTML_URL,
                    ProductType.ALL_SUPPORTED,
                    audience.getSunlightId(),
                    page
            );
        } else {
            return "%s/catalog/?product_type=%s&page=%d".formatted(
                    BASE_HTML_URL,
                    ProductType.ALL_SUPPORTED,
                    page
            );
        }
    }

    private record LookupResult(
            int page,
            String pageUrl,
            int totalItems,
            int totalPages,
            List<SunlightCatalogItem> items
    ) { }

}
