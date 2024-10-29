package ru.moonlight.sunlight.dump;

import ru.moonlight.sunlight.dump.service.SunlightCatalogExplorer;

public final class SunlightDump {

    public static void main(String[] args) throws Exception {
        try (SunlightCatalogExplorer explorer = new SunlightCatalogExplorer()) {
            explorer.collectData();
        }
    }

}
