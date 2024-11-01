package ru.moonlight.sunlight.dump.service;

import org.jsoup.nodes.Document;
import retrofit2.Call;
import ru.moonlight.sunlight.dump.Constants;
import ru.moonlight.sunlight.dump.SunlightDump;
import ru.moonlight.sunlight.dump.cache.DumpCache;
import ru.moonlight.sunlight.dump.exception.SunlightParseException;
import ru.moonlight.sunlight.dump.model.OrderData;
import ru.moonlight.sunlight.dump.model.SunlightCatalogItem;
import ru.moonlight.sunlight.dump.model.SunlightItemDetails;
import ru.moonlight.sunlight.dump.util.SunlightConnector;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.*;

import static ru.moonlight.sunlight.dump.Constants.*;

public final class SunlightItemDetailsService implements DumpService {

    private final SunlightDump application;
    private final Semaphore semaphore;
    private final ExecutorService executorService;
    private final DumpCache<SunlightItemDetails> cache;

    public SunlightItemDetailsService(SunlightDump application) {
        this.application = application;
        this.semaphore = new Semaphore(MAX_SIMULTANEOUS_CONNECTIONS);
        this.executorService = Executors.newVirtualThreadPerTaskExecutor();
        this.cache = application.getItemDetailsCache();
    }

    @Override
    public void runService() throws Exception {
        DumpCache<SunlightCatalogItem> itemsCache = application.getCatalogItemsCache();
        List<SunlightCatalogItem> catalogItems = itemsCache.models();
        if (catalogItems.isEmpty()) {
            System.err.println("Catalog items cache is empty!");
            return;
        }

        cache.clear();
        long start = System.currentTimeMillis();

        int fetchedCount = 0;
        int totalCount = catalogItems.size();

        System.out.printf("Fetching details for %d catalog item(s)...%n", totalCount);
        List<CompletableFuture<SunlightItemDetails>> futures = catalogItems.stream()
                .map(item -> lookupItemDetailsAsync(item.article(), item.pageUrl()))
                .toList();

        for (CompletableFuture<SunlightItemDetails> future : futures) {
            SunlightItemDetails result = null;

            try {
                result = future.get(1L, TimeUnit.MINUTES);
            } catch (TimeoutException ex) {
                System.err.println("An item lookup has been skipped due to wait timeout!");
                continue;
            } catch (InterruptedException ignored) {
            } catch (ExecutionException ex) {
                Throwable cause = ex.getCause();
                if (cause instanceof SunlightParseException cast)
                    cause = cast.getCause();

                if (cause instanceof SocketTimeoutException) {
                    System.err.println("An item lookup has been skipped due to read timeout!");
                    continue;
                }

                System.err.println("An item lookup has been failed:");
                ex.printStackTrace(System.err);
            }

            if (result == null)
                continue;

            cache.save(result);
            System.out.printf(
                    "  [%d / %d]: Fetched details for item #%d (%s)%n",
                    ++fetchedCount, totalCount, result.article(), result.type().getKey()
            );
        }

        System.out.println();
        System.out.println("Exporting results...");
        Path outputFile = application.getDumpDir().resolve(DUMP_NAME_ITEM_DETAILS);
        cache.exportDump(outputFile, application.getJsonMapper());

        long timeTook = System.currentTimeMillis() - start;
        System.out.printf("Detailed %d item(s) from Sunlight catalogs, time took: %.2f sec%n", fetchedCount, timeTook / 1000D);
    }

    @Override
    public void close() {
        executorService.shutdownNow();
    }

    private CompletableFuture<SunlightItemDetails> lookupItemDetailsAsync(long article, String pageUrl) {
        return CompletableFuture.supplyAsync(() -> lookupItemDetails(article, pageUrl), executorService);
    }

    private SunlightItemDetails lookupItemDetails(long article, String pageUrl) {
        pageUrl = Constants.BASE_HTML_URL + pageUrl;

        try {
            semaphore.acquire();
            Document document  = SunlightConnector.download(pageUrl);
            return SunlightItemDetails.fromElement(document, article, () -> lookupItemSizes(article));
        } catch (InterruptedException ignored) {
        } catch (IOException ex) {
            throw new SunlightParseException(ex);
        } finally {
            semaphore.release();
        }

        return null;
    }

    private float[] lookupItemSizes(long article) {
        Call<OrderData> call = application.getSunlightApi().fetchOrderData(article, CITY_ID_PERM);
        OrderData orderData = SunlightConnector.executeCall(call);
        return orderData != null ? orderData.getSizes() : null;
    }

}
