package ru.moonlight.sunlight.dump.service;

import java.io.Closeable;

public interface DumpService extends Closeable {

    void runService() throws Exception;

}
