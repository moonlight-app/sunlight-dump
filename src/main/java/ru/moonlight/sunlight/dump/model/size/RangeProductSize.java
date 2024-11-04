package ru.moonlight.sunlight.dump.model.size;

import java.util.Arrays;
import java.util.Objects;

final class RangeProductSize implements ProductSize {

    private final float from;
    private final float to;
    private final float step;
    private final float[] sequence;

    RangeProductSize(float from, float to, float step) {
        this.from = from;
        this.to = to;
        this.step = step;
        this.sequence = generateSequence(from, to, step);
    }

    @Override
    public float[] asSequence() {
        return sequence;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RangeProductSize that = (RangeProductSize) o;
        return Float.compare(from, that.from) == 0
                && Float.compare(to, that.to) == 0
                && Float.compare(step, that.step) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to, step);
    }

    @Override
    public String toString() {
        return "RangeProductSize{" +
                "from=" + from +
                ", to=" + to +
                ", step=" + step +
                ", sequence=" + Arrays.toString(sequence) +
                '}';
    }

    private static float[] generateSequence(float from, float to, float step) {
        float[] sequence = new float[(int) ((Math.abs(to - from)) / step) + 1];

        int index = 0;
        for (float value = from; value <= to; value += step)
            sequence[index++] = value;

        return sequence;
    }

}
