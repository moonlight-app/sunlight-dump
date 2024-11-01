package ru.moonlight.sunlight.dump.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.json.JsonMapper;
import ru.moonlight.sunlight.dump.model.SunlightDumpModel;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiFunction;

import static ru.moonlight.sunlight.dump.Constants.OPEN_OPTIONS;

public final class DumpCache<M extends SunlightDumpModel> {

    private final Map<Long, M> modelStore;
    private final Lock lock;

    public DumpCache() {
        this.modelStore = new HashMap<>();
        this.lock = new ReentrantLock();
    }

    public boolean isEmpty() {
        try {
            lock.lock();
            return modelStore.isEmpty();
        } finally {
            lock.unlock();
        }
    }

    public int size() {
        try {
            lock.lock();
            return modelStore.size();
        } finally {
            lock.unlock();
        }
    }

    public void clear() {
        try {
            lock.lock();
            modelStore.clear();
        } finally {
            lock.unlock();
        }
    }

    public List<M> models() {
        try {
            lock.lock();
            return List.copyOf(modelStore.values());
        } finally {
            lock.unlock();
        }
    }

    public Optional<M> find(long article) {
        try {
            lock.lock();
            return Optional.ofNullable(modelStore.get(article));
        } finally {
            lock.unlock();
        }
    }

    public void save(M model) {
        try {
            lock.lock();
            modelStore.put(model.article(), model);
        } finally {
            lock.unlock();
        }
    }

    public void saveAll(Iterable<? extends M> models) {
        try {
            lock.lock();
            models.forEach(model -> modelStore.put(model.article(), model));
        } finally {
            lock.unlock();
        }
    }

    public void saveAll(Iterable<? extends M> models, BiFunction<M, M, M> mergeFunction) {
        try {
            lock.lock();
            models.forEach(model -> modelStore.merge(model.article(), model, mergeFunction));
        } finally {
            lock.unlock();
        }
    }

    public <T extends M> boolean importDump(Path file, JsonMapper jsonMapper, TypeReference<List<T>> typeRef) throws IOException {
        if (!Files.isRegularFile(file))
            return false;

        List<T> models;
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            models = jsonMapper.readValue(reader, typeRef);
        }

        clear();
        saveAll(models);
        return true;
    }

    public void exportDump(Path file, JsonMapper jsonMapper) throws IOException {
        exportDump(file, jsonMapper, null);
    }

    public void exportDump(Path file, JsonMapper jsonMapper, Comparator<M> comparator) throws IOException {
        if (!Files.isDirectory(file.getParent()))
            Files.createDirectories(file.getParent());

        try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8, OPEN_OPTIONS)) {
            List<M> models = comparator != null ? models().stream().sorted(comparator).toList() : models();
            jsonMapper.writeValue(writer, models);
        }
    }

}
