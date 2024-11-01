package ru.moonlight.sunlight.dump;

import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;

public final class Constants {

    public static final String BASE_API_URL = "https://api.sunlight.net/v6/";
    public static final String BASE_HTML_URL = "https://ekb.sunlight.net";
    public static final int CITY_ID_PERM = 16;
    public static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:131.0) Gecko/20100101 Firefox/131.0";
    public static final String USER_AGENT_HEADER = "User-Agent: " + USER_AGENT;

    // this value may be increased up to 24, but for bigger value Sunlight servers
    // returns 503 too often, but the sunlight-dump uses retrying schema :)
    public static final int MAX_SIMULTANEOUS_CONNECTIONS = 24;

    public static final String DUMP_NAME_CATALOG_ITEMS = "catalog-items.json";
    public static final String DUMP_NAME_ITEM_DETAILS = "item-details.json";
    public static final String DUMP_NAME_MERGED_ITEMS = "merged-items.json";

    public static final OpenOption[] OPEN_OPTIONS = { StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING };

}
