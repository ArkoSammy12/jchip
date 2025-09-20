package io.github.arkosammy12.jchip.video;

import io.github.arkosammy12.jchip.Main;
import io.github.arkosammy12.jchip.util.CharacterSpriteFont;
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

public abstract class AbstractDisplay implements Display {

    private final CharacterSpriteFont characterSpriteFont;
    protected final Chip8Variant chip8Variant;

    protected final int screenWidth;
    protected final int screenHeight;
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

    public AbstractDisplay(EmulatorConfig config, KeyAdapter keyAdapter) {

        String romTitle = config.getProgramTitle();
        Chip8Variant chip8Variant = config.getConsoleVariant();

        if (romTitle == null) {
            this.romTitle = "";
        } else {
            this.romTitle = " | " + romTitle;
        }
        this.screenWidth = this.getWidth();
        this.screenHeight = this.getHeight();
        this.chip8Variant = chip8Variant;
        this.characterSpriteFont = new CharacterSpriteFont(chip8Variant);
        this.displayAngle = config.getDisplayAngle();
        this.pixelScale = getPixelScale(this.displayAngle);
        int windowWidth;
        int windowHeight;
        switch (displayAngle) {
            case DEG_90, DEG_270 -> {
                windowWidth = this.screenHeight * this.pixelScale;
                windowHeight = this.screenWidth * this.pixelScale;
            }
            default -> {
                windowWidth = this.screenWidth * this.pixelScale;
                windowHeight = this.screenHeight * this.pixelScale;
            }
        }
        Dimension windowSize = new Dimension(windowWidth, windowHeight);
        this.renderer = new Renderer();
        this.renderer.setPreferredSize(windowSize);
        this.renderer.setMinimumSize(windowSize);
        this.renderer.setMaximumSize(windowSize);
        this.renderer.addKeyListener(keyAdapter);
        this.renderer.setFocusable(true);
        this.renderer.requestFocus();
        this.renderer.setVisible(true);

        this.frame = new JFrame();
        this.frame.add(this.renderer);
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.frame.getContentPane().setPreferredSize(windowSize);
        this.frame.getContentPane().setMinimumSize(windowSize);
        this.frame.getContentPane().setMaximumSize(windowSize);
        this.frame.pack();
        this.frame.setAutoRequestFocus(true);
        this.frame.setLocation(30, 20);
        this.frame.setResizable(false);
        this.frame.setVisible(true);
    }

    @Override
    public CharacterSpriteFont getCharacterSpriteFont() {
        return this.characterSpriteFont;
    }

    protected abstract int getPixelScale(DisplayAngle displayAngle);

    protected abstract void populateDataBuffer(int[] buffer);

    @Override
    public void flush(int currentInstructionsPerFrame) {
        this.renderer.render();
        String title = this.getWindowTitle(currentInstructionsPerFrame);
        if (title != null) {
            this.frame.setTitle(title);
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

    @Override
    public void close() {
        this.frame.dispose();
    }

    private class Renderer extends Canvas {

        private final BufferedImage backBuffer;
        private final AffineTransform imageTransform;

        private Renderer() {
            backBuffer = new BufferedImage(screenWidth, screenHeight, BufferedImage.TYPE_INT_RGB);
            imageTransform = new AffineTransform();
            int scaledWidth = screenWidth * pixelScale;
            int scaledHeight = screenHeight * pixelScale;
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
            populateDataBuffer(buffer);
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
