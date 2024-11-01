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
import java.util.concurrent.*;
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

        for (ProductType productType : ProductType.values()) {
            System.out.printf("Collecting catalog items for item type '%s'...%n", productType.getKey());
            int productTypeItems = 0;

            for (Audience audience : Audience.values()) {
                System.out.printf("  Collecting data for audience '%s/%s'...%n", productType.getKey(), audience.getKey());

                System.out.println("    Looking at the first page...");
                LookupResult result = lookupCatalogItems(productType, audience, 1, true);
                if (result == null)
                    return;

                List<SunlightCatalogItem> items = result.items();
                if (items.isEmpty()) {
                    System.out.println("    There are no items on the first page!");
                    continue;
                } else {
                    cache.saveAll(items, SunlightCatalogService::mergeCatalogItems);
                    productTypeItems += items.size();
                }

                int totalPages = result.totalPages();
                System.out.printf("    Fetched metadata: %d item(s) on %d page(s)%n", result.totalItems(), totalPages);

                if (totalPages > 1) {
                    System.out.printf("    Collecting data from other pages (%d)...%n", totalPages - 1);
                    List<CompletableFuture<LookupResult>> futures = IntStream.rangeClosed(2, totalPages)
                            .mapToObj(page -> lookupCatalogItemsAsync(productType, audience, page))
                            .toList();

                    for (CompletableFuture<LookupResult> future : futures) {
                        if (result == null)
                            return;

                        items = result.items();
                        if (items.isEmpty()) {
                            System.err.printf("      Page %d -> no items found!%n", result.page());
                        } else {
                            cache.saveAll(items, SunlightCatalogService::mergeCatalogItems);
                            productTypeItems += items.size();
                        }
                    }
                }
            }

            System.out.printf("  Collected %d item(s) with audiences!%n", productTypeItems);
        }

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

    private CompletableFuture<LookupResult> lookupCatalogItemsAsync(ProductType productType, Audience audience, int page) {
        return CompletableFuture.supplyAsync(() -> lookupCatalogItems(productType, audience, page, false), executorService);
    }

    private LookupResult lookupCatalogItems(ProductType productType, Audience audience, int page, boolean computeMeta) {
        String pageUrl = generatePageableUrl(productType, audience) + page;

        try {
            Document document;
            try {
                semaphore.acquire();
                document = SunlightConnector.download(pageUrl);
            } finally {
                semaphore.release();
            }

            List<SunlightCatalogItem> items = SunlightCatalogItem.lookupItems(productType, audience, document);
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
        return Integer.parseInt(element.selectFirst("meta[itemprop=\"numberOfItems\"]").attr("content"));
    }

    private static String generatePageableUrl(ProductType productType, Audience audience) {
        return "%s/catalog/?product_type=%d&gender_position=%d&page=".formatted(
                BASE_HTML_URL,
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
