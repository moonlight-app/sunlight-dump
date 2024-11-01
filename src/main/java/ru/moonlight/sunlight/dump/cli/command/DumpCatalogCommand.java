package ru.moonlight.sunlight.dump.cli.command;

import ru.moonlight.sunlight.dump.SunlightDump;
import ru.moonlight.sunlight.dump.cli.CommandExecutor;
import ru.moonlight.sunlight.dump.service.SunlightCatalogService;

public final class DumpCatalogCommand implements CommandExecutor {

    private final SunlightCatalogService catalogService;

    public DumpCatalogCommand(SunlightDump application) {
        this.catalogService = application.getCatalogService();
    }

    @Override
    public void execute() throws Exception {
        catalogService.runService();
    }

}
