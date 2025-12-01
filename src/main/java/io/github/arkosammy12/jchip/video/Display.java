package io.github.arkosammy12.jchip.video;

import io.github.arkosammy12.jchip.emulators.Emulator;
import io.github.arkosammy12.jchip.util.DisplayAngle;

import java.io.Closeable;

public abstract class Display<E extends Emulator> implements Closeable {

    protected final E emulator;
    private final EmulatorRenderer emulatorRenderer;

    private final DisplayAngle displayAngle;
    protected final int imageWidth;
    protected final int imageHeight;

    public Display(E emulator) {
        this.emulator = emulator;
        this.displayAngle = emulator.getEmulatorSettings().getDisplayAngle();
        this.imageWidth = getImageWidth();
        this.imageHeight = getImageHeight();
        this.emulatorRenderer = new EmulatorRenderer(emulator.getEmulatorSettings().getJchip(), this, emulator.getKeyAdapters());
    }

    public EmulatorRenderer getEmulatorRenderer() {
        return this.emulatorRenderer;
    }

    public DisplayAngle getDisplayAngle() {
        return this.displayAngle;
    }

    public abstract int getWidth();

    public abstract int getHeight();

    protected abstract int getImageWidth();

    protected abstract int getImageHeight();

    protected abstract int getImageScale(DisplayAngle displayAngle);

    protected abstract void populateRenderBuffer(int[][] renderBuffer);

    public void flush() {
        this.emulatorRenderer.updateRenderBuffer();
    }

    @Override
    public void close() {
        this.emulatorRenderer.close();
    }

}
