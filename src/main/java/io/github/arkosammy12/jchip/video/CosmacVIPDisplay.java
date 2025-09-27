package io.github.arkosammy12.jchip.video;

import io.github.arkosammy12.jchip.util.DisplayAngle;
import io.github.arkosammy12.jchip.util.EmulatorConfig;

import java.awt.event.KeyAdapter;

public class CosmacVIPDisplay extends AbstractDisplay {

    public CosmacVIPDisplay(EmulatorConfig config, KeyAdapter keyAdapter) {
        super(config, keyAdapter);
    }

    @Override
    protected int getPixelScale(DisplayAngle displayAngle) {
        return 0;
    }

    @Override
    protected void populateDataBuffer(int[] buffer) {

    }

    @Override
    public int getWidth() {
        return 64;
    }

    @Override
    public int getHeight() {
        return 128;
    }

    @Override
    public void clear() {

    }
}
