package io.github.arkosammy12.jchip.video;

import io.github.arkosammy12.jchip.util.HexSpriteFont;
import io.github.arkosammy12.jchip.util.Chip8Variant;
import io.github.arkosammy12.jchip.util.DisplayAngle;
import io.github.arkosammy12.jchip.config.EmulatorInitializer;

import java.awt.event.*;
import java.io.Closeable;
import java.util.List;

public abstract class Display implements Closeable {

    private final EmulatorRenderer emulatorRenderer;

    protected final Chip8Variant chip8Variant;
    private final DisplayAngle displayAngle;
    protected final int imageWidth;
    protected final int imageHeight;

    public Display(EmulatorInitializer config, List<KeyAdapter> keyAdapters) {
        this.chip8Variant = config.getVariant();
        this.displayAngle = config.getDisplayAngle();
        this.imageWidth = getImageWidth();
        this.imageHeight = getImageHeight();
        this.emulatorRenderer = new EmulatorRenderer(config.getJChip(), this, keyAdapters, config.getProgramTitle());
    }

    public Chip8Variant getChip8Variant() {
        return this.chip8Variant;
    }

    public HexSpriteFont getCharacterSpriteFont() {
        return chip8Variant.getSpriteFont();
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

    public abstract void clear();

    public void flush() {
        this.emulatorRenderer.requestFrame();
    }

    @Override
    public void close() {
        this.emulatorRenderer.close();
    }

}
