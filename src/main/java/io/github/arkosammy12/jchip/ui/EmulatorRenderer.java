package io.github.arkosammy12.jchip.ui;

import io.github.arkosammy12.jchip.JChip;
import io.github.arkosammy12.jchip.util.Chip8Variant;
import io.github.arkosammy12.jchip.util.DisplayAngle;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.Closeable;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static io.github.arkosammy12.jchip.util.DisplayAngle.*;

public class EmulatorRenderer extends Canvas implements Closeable {

    private final int[][] renderBuffer;
    private final Consumer<int[][]> renderBufferUpdater;
    private final JChip jchip;

    private final int displayWidth;
    private final int displayHeight;
    private final int initialScale;
    private final DisplayAngle displayAngle;
    private final Chip8Variant chip8Variant;
    private final String romTitle;

    private final BufferedImage backBuffer;
    private final AffineTransform rotationTransform;
    private final AffineTransform drawTransform = new AffineTransform();
    private int lastWidth = -1;
    private int lastHeight = -1;

    private final AtomicBoolean running = new AtomicBoolean(true);
    private final AtomicBoolean frameRequested = new AtomicBoolean(false);

    private final Object renderLock = new Object();
    protected final Object renderBufferLock = new Object();

    public EmulatorRenderer(JChip jchip, int displayWidth, int displayHeight, DisplayAngle displayAngle, int initialScale, Consumer<int[][]> renderBufferUpdater, Chip8Variant variant, String romTitle) {
        super();
        this.jchip = jchip;
        this.displayWidth = displayWidth;
        this.displayHeight = displayHeight;
        this.displayAngle = displayAngle;
        this.chip8Variant = variant;
        this.romTitle = romTitle;
        this.initialScale = initialScale;
        this.renderBuffer = new int[displayWidth][displayHeight];
        this.renderBufferUpdater = renderBufferUpdater;

        this.backBuffer = new BufferedImage(displayWidth, displayHeight, BufferedImage.TYPE_INT_ARGB);
        this.rotationTransform = new AffineTransform();
        switch (displayAngle) {
            case DEG_90 -> {
                this.rotationTransform.translate(displayHeight, 0);
                this.rotationTransform.rotate(Math.toRadians(90));
            }
            case DEG_180 -> {
                this.rotationTransform.translate(displayWidth, displayHeight);
                this.rotationTransform.rotate(Math.toRadians(180));
            }
            case DEG_270 -> {
                this.rotationTransform.translate(0, displayWidth);
                this.rotationTransform.rotate(Math.toRadians(270));
            }
            default -> {}
        }

        int windowWidth = displayWidth * initialScale;
        int windowHeight = displayHeight * initialScale;

        this.setPreferredSize(new Dimension(windowWidth, windowHeight));
        this.setFocusable(true);
        setIgnoreRepaint(false);

        this.jchip.getMainWindow().setGameRenderer(this);
    }

    public int getDisplayWidth() {
        return this.displayWidth;
    }

    public int getDisplayHeight() {
        return this.displayHeight;
    }

    public int getInitialScale() {
        return this.initialScale;
    }

    public Chip8Variant getChip8Variant() {
        return this.chip8Variant;
    }

    public String getRomTitle() {
        return this.romTitle;
    }

     public void requestFrame() {
        synchronized (this.renderBufferLock) {
            this.renderBufferUpdater.accept(this.renderBuffer);
        }
        synchronized (this.renderLock) {
            this.frameRequested.set(true);
            this.renderLock.notify();
        }
     }

    @Override
    public void addNotify() {
        super.addNotify();
        initializeAndStart();
    }

    private void renderLoop() {
        try {
            while (this.running.get()) {
                synchronized (this.renderLock) {
                    while (!this.frameRequested.get()) {
                        try {
                            this.renderLock.wait();
                        } catch (InterruptedException ignored) {
                        }
                    }
                    this.frameRequested.set(false);
                }
                this.render();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void updateTransformIfNeeded() {
        double windowWidth = getWidth();
        double windowHeight = getHeight();
        if (windowWidth != lastWidth || windowHeight != lastHeight) {
            double logicalWidth = (displayAngle == DEG_90 || displayAngle == DEG_270)
                    ? displayHeight : displayWidth;
            double logicalHeight = (displayAngle == DEG_90 || displayAngle == DEG_270)
                    ? displayWidth : displayHeight;
            double scale = Math.min(windowWidth / logicalWidth, windowHeight / logicalHeight);
            double scaledWidth = logicalWidth * scale;
            double scaledHeight = logicalHeight * scale;
            double offsetX = (windowWidth - scaledWidth) / 2.0;
            double offsetY = (windowHeight - scaledHeight) / 2.0;
            drawTransform.setToIdentity();
            drawTransform.translate(offsetX, offsetY);
            drawTransform.scale(scale, scale);
            drawTransform.concatenate(rotationTransform);
            lastWidth = (int) windowWidth;
            lastHeight = (int) windowHeight;
        }
    }

    private void render() {
        BufferStrategy bufferStrategy = getBufferStrategy();
        if (bufferStrategy == null) {
            createBufferStrategy(3);
            return;
        }
        updateTransformIfNeeded();
        int[] pixels = ((DataBufferInt) backBuffer.getRaster().getDataBuffer()).getData();
        Arrays.fill(pixels, 0xFF000000);
        synchronized (this.renderBufferLock) {
            for (int y = 0; y < displayHeight; y++) {
                int base = y * displayWidth;
                for (int x = 0; x < displayWidth; x++) {
                    pixels[base + x] = this.renderBuffer[x][y];
                }
            }
        }
        Graphics2D g = (Graphics2D) bufferStrategy.getDrawGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.drawImage(backBuffer, drawTransform, null);
        g.dispose();
        bufferStrategy.show();
        Toolkit.getDefaultToolkit().sync();
    }

    @Override
    public void close() {
        this.running.set(false);
        synchronized (this.renderLock) {
            this.renderLock.notify();
        }
    }

    private void initializeAndStart() {
        if (getBufferStrategy() == null) {
            createBufferStrategy(3);
        }
        Thread renderThread = new Thread(this::renderLoop, "jchip-Render-Thread");
        renderThread.setDaemon(true);
        renderThread.start();
    }

}
