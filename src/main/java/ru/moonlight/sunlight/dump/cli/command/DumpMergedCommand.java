package ru.moonlight.sunlight.dump.cli.command;

import lombok.AllArgsConstructor;
import ru.moonlight.sunlight.dump.SunlightDump;
import ru.moonlight.sunlight.dump.cache.DumpCache;
import ru.moonlight.sunlight.dump.cli.CommandExecutor;
import ru.moonlight.sunlight.dump.model.SunlightCatalogItem;
import ru.moonlight.sunlight.dump.model.SunlightFullItem;
import ru.moonlight.sunlight.dump.model.SunlightItemDetails;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static ru.moonlight.sunlight.dump.Constants.DUMP_NAME_MERGED_ITEMS;

@AllArgsConstructor
public final class DumpMergedCommand implements CommandExecutor {

    private final SunlightDump application;

    @Override
    public void execute() throws Exception {
        List<SunlightCatalogItem> catalogItems = application.getCatalogItemsCache().models();
        if (catalogItems.isEmpty()) {
            System.err.println("Catalog items cache is empty!");
            return;
        }

        DumpCache<SunlightItemDetails> itemDetailsCache = application.getItemDetailsCache();
        if (itemDetailsCache.isEmpty()) {
            System.err.println("Item details cache is empty!");
            return;
        }

        System.out.printf("Merging datasets: %d item(s)...%n", catalogItems.size());
        DumpCache<SunlightFullItem> cache = application.getFullItemsCache();

        for (SunlightCatalogItem item : catalogItems) {
            Optional<SunlightItemDetails> details = itemDetailsCache.find(item.article());
            if (details.isEmpty()) {
                System.err.printf("  Details not found for item #%d (%s)%n", item.article(), item.name());
                continue;
            }

            cache.save(SunlightFullItem.merge(item, details.get()));
        }

        System.out.println("Exporting results...");
        Path outputFile = application.getDumpDir().resolve(DUMP_NAME_MERGED_ITEMS);
        cache.exportDump(outputFile, application.getJsonMapper());

        System.out.println("Done!");
    }

}
