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
    protected final int displayWidth;
    protected final int displayHeight;

    public Display(EmulatorConfig config, List<KeyAdapter> keyAdapters) {
        this.jchip = config.getJChip();
        String romTitle = config.getProgramTitle();
        String romTitle1 = (romTitle == null) ? "" : " | " + romTitle;
        this.chip8Variant = config.getConsoleVariant();
        DisplayAngle displayAngle = config.getDisplayAngle();
        this.displayWidth = getImageWidth();
        this.displayHeight = getImageHeight();
        int initialScale = getImageScale(displayAngle);
        this.emulatorRenderer = new EmulatorRenderer(this.jchip, this.displayWidth, this.displayHeight, displayAngle, initialScale, this.getRenderBufferUpdater(), this.chip8Variant, romTitle1);
        keyAdapters.forEach(emulatorRenderer::addKeyListener);
        this.emulatorRenderer.setVisible(true);

    }

    public HexSpriteFont getCharacterSpriteFont() {
        return chip8Variant.getSpriteFont();
    }

    public abstract int getWidth();

    public abstract int getHeight();

    protected abstract int getImageWidth();

    protected abstract int getImageHeight();

    protected abstract int getImageScale(DisplayAngle displayAngle);

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
