package io.github.arkosammy12.jchip.video;

import io.github.arkosammy12.jchip.Main;
import io.github.arkosammy12.jchip.util.SpriteFont;
import io.github.arkosammy12.jchip.util.Chip8Variant;
import io.github.arkosammy12.jchip.util.DisplayAngle;
import io.github.arkosammy12.jchip.util.EmulatorConfig;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.Closeable;
import java.lang.reflect.InvocationTargetException;

public abstract class Display implements Closeable {

    private final SpriteFont spriteFont;
    protected final Chip8Variant chip8Variant;

    protected final int displayWidth;
    protected final int displayHeight;
    private final int pixelScale;

    private final Renderer renderer;
    private final JFrame frame;
    private final DisplayAngle displayAngle;

    private final String romTitle;
    private long lastWindowTitleUpdate = 0;
    private long lastFrameTime = System.nanoTime();
    private int framesSinceLastUpdate = 0;
    private long totalIpfSinceLastUpdate = 0;
    private double totalFrameTimeSinceLastUpdate = 0;

    private final StringBuilder stringBuilder = new StringBuilder(128);

    public Display(EmulatorConfig config, KeyAdapter keyAdapter) {
        String romTitle = config.getProgramTitle();
        Chip8Variant chip8Variant = config.getConsoleVariant();

        if (romTitle == null) {
            this.romTitle = "";
        } else {
            this.romTitle = " | " + romTitle;
        }

        this.displayWidth = this.getRenderWidth();
        this.displayHeight = this.getRenderHeight();
        this.chip8Variant = chip8Variant;
        this.spriteFont = new SpriteFont(chip8Variant);
        this.displayAngle = config.getDisplayAngle();
        this.pixelScale = getPixelRenderScale(this.displayAngle);

        int windowWidth;
        int windowHeight;
        switch (displayAngle) {
            case DEG_90, DEG_270 -> {
                windowWidth = this.displayHeight * this.pixelScale;
                windowHeight = this.displayWidth * this.pixelScale;
            }
            default -> {
                windowWidth = this.displayWidth * this.pixelScale;
                windowHeight = this.displayHeight * this.pixelScale;
            }
        }

        Dimension windowSize = new Dimension(windowWidth, windowHeight);
        this.renderer = new Renderer();
        JFrame tempFrame = new JFrame();

        try {
            SwingUtilities.invokeAndWait(() -> {

                renderer.setPreferredSize(windowSize);
                renderer.setMinimumSize(windowSize);
                renderer.setMaximumSize(windowSize);
                renderer.addKeyListener(keyAdapter);
                renderer.setFocusable(true);
                renderer.requestFocus();
                renderer.setVisible(true);

                tempFrame.add(renderer);
                tempFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                tempFrame.getContentPane().setPreferredSize(windowSize);
                tempFrame.getContentPane().setMinimumSize(windowSize);
                tempFrame.getContentPane().setMaximumSize(windowSize);
                tempFrame.pack();
                tempFrame.setAutoRequestFocus(true);
                tempFrame.setLocation(30, 20);
                tempFrame.setResizable(false);
                tempFrame.setVisible(true);
            });
        } catch (InterruptedException | InvocationTargetException e) {
            throw new RuntimeException("Failed to initialize display", e);
        }
        this.frame = tempFrame;
    }

    public SpriteFont getCharacterSpriteFont() {
        return this.spriteFont;
    }

    public abstract int getWidth();

    public abstract int getHeight();

    protected abstract int getRenderWidth();

    protected abstract int getRenderHeight();

    protected abstract int getPixelRenderScale(DisplayAngle displayAngle);

    protected abstract void fillRenderBuffer(int[] buffer);

    public abstract void clear();

    public void flush(int currentInstructionsPerFrame) {
        this.renderer.render();
        String title = this.getWindowTitle(currentInstructionsPerFrame);
        if (title != null) {
            SwingUtilities.invokeLater(() -> this.frame.setTitle(title));
        }
    }

    private String getWindowTitle(int currentInstructionsPerFrame) {
        long now = System.nanoTime();
        double lastFrameDuration = now - this.lastFrameTime;
        this.lastFrameTime = now;
        this.totalFrameTimeSinceLastUpdate += lastFrameDuration;
        this.totalIpfSinceLastUpdate += currentInstructionsPerFrame;
        this.framesSinceLastUpdate++;
        long deltaTime = now - lastWindowTitleUpdate;
        if (deltaTime < 1_000_000_000L) {
            return null;
        }
        double lastFps = this.framesSinceLastUpdate / (deltaTime / 1_000_000_000.0);
        long averageInstructionsPerFrame = this.totalIpfSinceLastUpdate / this.framesSinceLastUpdate;
        double averageFrameTimeMs = (this.totalFrameTimeSinceLastUpdate / this.framesSinceLastUpdate) / 1_000_000.0;
        double mips = (averageInstructionsPerFrame * lastFps) / 1_000_000;
        this.framesSinceLastUpdate = 0;
        this.totalFrameTimeSinceLastUpdate = 0;
        this.totalIpfSinceLastUpdate = 0;
        this.lastWindowTitleUpdate = now;

        stringBuilder.append("jchip ").append(Main.VERSION_STRING)
                .append(" | ").append(chip8Variant.getDisplayName())
                .append(romTitle)
                .append(" | IPF: ").append(averageInstructionsPerFrame)
                .append(" | MIPS: ").append((long)(mips * 100) / 100.0)  // round to 2 decimals
                .append(" | Frame Time: ").append((long)(averageFrameTimeMs * 100) / 100.0)
                .append(" ms | FPS: ").append((long)(lastFps * 100) / 100.0);

        String titleString = stringBuilder.toString();
        stringBuilder.setLength(0);
        return titleString;
    }

    public void close() {
        SwingUtilities.invokeLater(this.frame::dispose);
    }

    private class Renderer extends Canvas {

        private final BufferedImage backBuffer;
        private final AffineTransform imageTransform;

        private Renderer() {
            backBuffer = new BufferedImage(displayWidth, displayHeight, BufferedImage.TYPE_INT_ARGB);
            imageTransform = new AffineTransform();
            int scaledWidth = displayWidth * pixelScale;
            int scaledHeight = displayHeight * pixelScale;
            switch (displayAngle) {
                case DEG_90 -> {
                    imageTransform.translate(scaledHeight, 0);
                    imageTransform.rotate(Math.toRadians(90));
                }
                case DEG_180 -> {
                    imageTransform.translate(scaledWidth, scaledHeight);
                    imageTransform.rotate(Math.toRadians(180));
                }
                case DEG_270 -> {
                    imageTransform.translate(0, scaledWidth);
                    imageTransform.rotate(Math.toRadians(270));
                }
            }
            imageTransform.scale(pixelScale, pixelScale);
        }

        private void render() {
            int[] buffer = ((DataBufferInt) backBuffer.getRaster().getDataBuffer()).getData();
            fillRenderBuffer(buffer);
            BufferStrategy bufferStrategy = getBufferStrategy();
            if (bufferStrategy == null) {
                createBufferStrategy(3);
                return;
            }
            Graphics2D g = (Graphics2D) bufferStrategy.getDrawGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g.drawImage(backBuffer, imageTransform, null);
            g.dispose();
            bufferStrategy.show();
        }
    }

}
