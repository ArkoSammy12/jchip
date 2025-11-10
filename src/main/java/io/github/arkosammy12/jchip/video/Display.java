package io.github.arkosammy12.jchip.video;

import io.github.arkosammy12.jchip.util.DisplayAngle;
import io.github.arkosammy12.jchip.config.EmulatorSettings;

import java.awt.event.*;
import java.io.Closeable;
import java.util.List;

public abstract class Display implements Closeable {

    private final EmulatorRenderer emulatorRenderer;

    private final DisplayAngle displayAngle;
    protected final int imageWidth;
    protected final int imageHeight;

    public Display(EmulatorSettings emulatorSettings, List<KeyAdapter> keyAdapters) {
        this.displayAngle = emulatorSettings.getDisplayAngle();
        this.imageWidth = getImageWidth();
        this.imageHeight = getImageHeight();
        this.emulatorRenderer = new EmulatorRenderer(emulatorSettings.getJChip(), this, keyAdapters, emulatorSettings.getProgramTitle());
    }

    public EmulatorRenderer getEmulatorRenderer() {
        return this.emulatorRenderer;
    }

    public DisplayAngle getDisplayAngle() {
        return this.displayAngle;
    }

    public abstract int getWidth();

    public abstract int getHeight();

    public abstract int getImageWidth();

    public abstract int getImageHeight();

    public abstract int getImageScale(DisplayAngle displayAngle);

    protected abstract void populateRenderBuffer(int[][] renderBuffer);

    protected abstract void clear();

    public void flush() {
        this.emulatorRenderer.updateRenderBuffer();
    }

    @Override
    public void close() {
        this.emulatorRenderer.close();
    }

}
