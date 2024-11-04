package ru.moonlight.sunlight.dump.model.size;

record StaticProductSize(float value) implements ProductSize {

    @Override
    public float[] asSequence() {
        return new float[] { value };
    }

}
