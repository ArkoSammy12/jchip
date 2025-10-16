package io.github.arkosammy12.jchip.video;

import io.github.arkosammy12.jchip.ui.EmulatorRenderer;
import io.github.arkosammy12.jchip.JChip;
import io.github.arkosammy12.jchip.util.HexSpriteFont;
import io.github.arkosammy12.jchip.util.Chip8Variant;
import io.github.arkosammy12.jchip.util.DisplayAngle;
import io.github.arkosammy12.jchip.config.EmulatorConfig;

import java.awt.event.*;
import java.io.Closeable;
import java.util.List;
import java.util.function.Consumer;

public abstract class Display implements Closeable {

    private final EmulatorRenderer emulatorRenderer;
    private final JChip jchip;

    protected final Chip8Variant chip8Variant;
    private final DisplayAngle displayAngle;
    protected final int imageWidth;
    protected final int imageHeight;

    public Display(EmulatorConfig config, List<KeyAdapter> keyAdapters) {
        this.jchip = config.getJChip();
        String romTitle = config.getProgramTitle();
        this.chip8Variant = config.getVariant();
        this.displayAngle = config.getDisplayAngle();
        this.imageWidth = getImageWidth();
        this.imageHeight = getImageHeight();
        this.emulatorRenderer = new EmulatorRenderer(this.jchip, this, this.getRenderBufferUpdater(), this.chip8Variant, (romTitle == null) ? "" : " | " + romTitle);
        keyAdapters.forEach(emulatorRenderer::addKeyListener);
        this.emulatorRenderer.setVisible(true);
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

    protected abstract Consumer<int[][]> getRenderBufferUpdater();

    public abstract void clear();

    public void flush(int currentInstructionsPerFrame) {
        this.emulatorRenderer.requestFrame();
        this.jchip.getMainWindow().updateWindowTitle(currentInstructionsPerFrame);
    }

    @Override
    public void close() {
        this.emulatorRenderer.close();
    }

}
