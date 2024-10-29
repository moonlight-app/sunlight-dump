package ru.moonlight.sunlight.dump.service;

import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;

public interface DumpService extends AutoCloseable {

    String BASE_URL = "https://ekb.sunlight.net";
    String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:131.0) Gecko/20100101 Firefox/131.0";
    OpenOption[] OPEN_OPTIONS = { StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING };

    void collectData() throws Exception;

}
