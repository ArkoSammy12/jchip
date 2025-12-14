package io.github.arkosammy12.jchip.video;

import io.github.arkosammy12.jchip.Jchip;
import io.github.arkosammy12.jchip.util.DisplayAngle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.Closeable;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.*;
import java.awt.*;

public class EmulatorRenderer extends JPanel implements Closeable {

    private final Display<?> display;
    private final int[][] renderBuffer;

    private final int displayWidth;
    private final int displayHeight;
    private final int initialScale;
    private final DisplayAngle displayAngle;

    private final BufferedImage bufferedImage;
    private final AffineTransform rotationTransform = new AffineTransform();
    private final AffineTransform drawTransform = new AffineTransform();

    private int lastWidth = -1;
    private int lastHeight = -1;

    private final AtomicBoolean running = new AtomicBoolean(true);
    private final AtomicBoolean frameRequested = new AtomicBoolean(false);

    private final Object renderLock = new Object();
    protected final Object renderBufferLock = new Object();

    private Thread renderThread;

    public EmulatorRenderer(
            Jchip jchip,
            Display<?> display,
            List<KeyAdapter> keyAdapters
    ) {
        this.display = display;
        this.displayWidth = display.getImageWidth();
        this.displayHeight = display.getImageHeight();
        this.displayAngle = display.getDisplayAngle();
        this.initialScale = display.getImageScale(displayAngle);

        this.renderBuffer = new int[displayWidth][displayHeight];
        this.bufferedImage = new BufferedImage(
                displayWidth,
                displayHeight,
                BufferedImage.TYPE_INT_ARGB
        );

        switch (displayAngle) {
            case DEG_90 -> {
                rotationTransform.translate(displayHeight, 0);
                rotationTransform.rotate(Math.toRadians(90));
            }
            case DEG_180 -> {
                rotationTransform.translate(displayWidth, displayHeight);
                rotationTransform.rotate(Math.toRadians(180));
            }
            case DEG_270 -> {
                rotationTransform.translate(0, displayWidth);
                rotationTransform.rotate(Math.toRadians(270));
            }
        }

        this.setFocusable(true);
        this.setOpaque(true);
        this.setBackground(Color.BLACK);
        keyAdapters.forEach(this::addKeyListener);
        SwingUtilities.invokeLater(() -> jchip.getMainWindow().setEmulatorRenderer(this));
        this.startRenderThread();
    }

    public int getDisplayWidth() {
        return displayWidth;
    }

    public int getDisplayHeight() {
        return displayHeight;
    }

    public int getInitialScale() {
        return initialScale;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        this.updateTransformIfNeeded();
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g2.drawImage(bufferedImage, drawTransform, null);
        } finally {
            g2.dispose();
        }
    }

    private void updateTransformIfNeeded() {
        int w = getWidth();
        int h = getHeight();

        if (w == lastWidth && h == lastHeight) {
            return;
        }
        double logicalWidth = (displayAngle == DisplayAngle.DEG_90 || displayAngle == DisplayAngle.DEG_270)
                        ? displayHeight
                        : displayWidth;

        double logicalHeight = (displayAngle == DisplayAngle.DEG_90 || displayAngle == DisplayAngle.DEG_270)
                        ? displayWidth
                        : displayHeight;

        double scale = Math.min(w / logicalWidth, h / logicalHeight);

        double scaledWidth = logicalWidth * scale;
        double scaledHeight = logicalHeight * scale;

        double offsetX = (w - scaledWidth) / 2.0;
        double offsetY = (h - scaledHeight) / 2.0;

        this.drawTransform.setToIdentity();
        this.drawTransform.translate(offsetX, offsetY);
        this.drawTransform.scale(scale, scale);
        this.drawTransform.concatenate(rotationTransform);

        this.lastWidth = w;
        this.lastHeight = h;
    }

    protected void updateRenderBuffer() {
        synchronized (renderBufferLock) {
            display.populateRenderBuffer(renderBuffer);
        }
    }

    public void requestFrame() {
        synchronized (renderLock) {
            frameRequested.set(true);
            renderLock.notify();
        }
    }

    private void startRenderThread() {
        this.renderThread = new Thread(this::renderLoop, "jchip-render-thread");
        this.renderThread.setDaemon(true);
        this.renderThread.start();
    }

    private void renderLoop() {
        while (this.running.get()) {
            synchronized (this.renderLock) {
                while (this.running.get() && !this.frameRequested.get()) {
                    try {
                        this.renderLock.wait();
                    } catch (InterruptedException _) {}
                }
                this.frameRequested.set(false);
            }
            this.renderFrame();
        }
    }

    private void renderFrame() {
        int[] pixels = ((DataBufferInt) bufferedImage.getRaster().getDataBuffer()).getData();
        synchronized (renderBufferLock) {
            for (int y = 0; y < displayHeight; y++) {
                int base = y * displayWidth;
                for (int x = 0; x < displayWidth; x++) {
                    pixels[base + x] = renderBuffer[x][y];
                }
            }
        }
        SwingUtilities.invokeLater(this::repaint);
    }

    @Override
    public void close() {
        running.set(false);
        synchronized (renderLock) {
            renderLock.notifyAll();
        }
        if (renderThread != null) {
            try {
                renderThread.join();
            } catch (InterruptedException ignored) {}
        }
    }

}
