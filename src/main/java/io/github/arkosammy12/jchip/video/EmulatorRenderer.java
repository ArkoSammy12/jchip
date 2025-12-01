package io.github.arkosammy12.jchip.video;

import io.github.arkosammy12.jchip.Jchip;
import io.github.arkosammy12.jchip.util.DisplayAngle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.Closeable;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.github.arkosammy12.jchip.util.DisplayAngle.*;

public class EmulatorRenderer extends Canvas implements Closeable {

    private final Display<?> display;
    private final int[][] renderBuffer;

    private final int displayWidth;
    private final int displayHeight;
    private final int initialScale;
    private final DisplayAngle displayAngle;

    private final BufferedImage bufferedImage;
    private final AffineTransform rotationTransform;
    private final AffineTransform drawTransform = new AffineTransform();
    private int lastWidth = -1;
    private int lastHeight = -1;

    private final AtomicBoolean running = new AtomicBoolean(true);
    private final AtomicBoolean frameRequested = new AtomicBoolean(false);

    private Thread renderThread;
    private final Object renderLock = new Object();
    protected final Object renderBufferLock = new Object();

    public EmulatorRenderer(Jchip jchip, Display<?> display, List<KeyAdapter> keyAdapters) {
        super();
        this.displayWidth = display.getImageWidth();
        this.displayHeight = display.getImageHeight();
        this.displayAngle = display.getDisplayAngle();
        this.initialScale = display.getImageScale(this.displayAngle);
        this.renderBuffer = new int[displayWidth][displayHeight];
        this.display = display;

        this.bufferedImage = new BufferedImage(displayWidth, displayHeight, BufferedImage.TYPE_INT_ARGB);
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
        }

        int windowWidth = displayWidth * initialScale;
        int windowHeight = displayHeight * initialScale;

        SwingUtilities.invokeLater(() -> {
            this.setPreferredSize(new Dimension(windowWidth, windowHeight));
            keyAdapters.forEach(this::addKeyListener);
            this.setFocusable(true);
            jchip.getMainWindow().setEmulatorRenderer(this);
        });
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

    protected void updateRenderBuffer() {
        synchronized (this.renderBufferLock) {
            this.display.populateRenderBuffer(this.renderBuffer);
        }
    }

    public void requestFrame() {
        synchronized (this.renderLock) {
            this.frameRequested.set(true);
            this.renderLock.notify();
        }
    }

    @Override
    public void addNotify() {
        super.addNotify();
        this.initializeAndStart();
    }

    private void renderLoop() {
        while (this.running.get()) {
            synchronized (this.renderLock) {
                while (this.running.get() && !this.frameRequested.get()) {
                    try {
                        this.renderLock.wait();
                    } catch (InterruptedException ignored) {}
                }
                if (!this.running.get()) {
                    break;
                }
                this.frameRequested.set(false);
            }
            this.render();
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
        int[] pixels = ((DataBufferInt) bufferedImage.getRaster().getDataBuffer()).getData();
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
        g.drawImage(bufferedImage, drawTransform, null);
        g.dispose();
        bufferStrategy.show();
        Toolkit.getDefaultToolkit().sync();
    }

    @Override
    public void close() {
        this.running.set(false);
        synchronized (this.renderLock) {
            this.renderLock.notifyAll();
        }
        if (this.renderThread != null && this.renderThread.isAlive()) {
            try {
                renderThread.join();
            } catch (InterruptedException ignored) {}
        }
    }

    private void initializeAndStart() {
        if (getBufferStrategy() == null) {
            createBufferStrategy(3);
        }
        this.renderThread = new Thread(this::renderLoop, "jchip-Render-Thread");
        this.renderThread.start();
    }

}
