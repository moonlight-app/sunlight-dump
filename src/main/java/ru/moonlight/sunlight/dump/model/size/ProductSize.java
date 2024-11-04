package ru.moonlight.sunlight.dump.model.size;

public interface ProductSize {

    static ProductSize asStatic(float value) {
        return new StaticProductSize(value);
    }

    static ProductSize asRange(float from, float to, float step) {
        return new RangeProductSize(from, to, step);
    }

    float[] asSequence();

}
