package io.github.arkosammy12.jchip.video;

import io.github.arkosammy12.jchip.Main;
import io.github.arkosammy12.jchip.util.HexSpriteFont;
import io.github.arkosammy12.jchip.util.Chip8Variant;
import io.github.arkosammy12.jchip.util.DisplayAngle;
import io.github.arkosammy12.jchip.config.EmulatorConfig;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.Closeable;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public abstract class Display implements Closeable {

    protected final Chip8Variant chip8Variant;
    protected final int displayWidth;
    protected final int displayHeight;

    private final JFrame frame;
    private final Renderer renderer;
    private final DisplayAngle displayAngle;
    private final String romTitle;

    private long lastWindowTitleUpdate = 0;
    private long lastFrameTime = System.nanoTime();
    private int framesSinceLastUpdate = 0;
    private long totalIpfSinceLastUpdate = 0;
    private double totalFrameTimeSinceLastUpdate = 0;

    private final StringBuilder stringBuilder = new StringBuilder(128);

    private volatile boolean running = true;
    private volatile boolean frameRequested = false;

    private final Object renderLock = new Object();
    protected final Object renderBufferLock = new Object();
    private final Thread renderThread;

    public Display(EmulatorConfig config, List<KeyAdapter> keyAdapters) {
        String romTitle = config.getProgramTitle();
        this.romTitle = (romTitle == null) ? "" : " | " + romTitle;

        this.chip8Variant = config.getConsoleVariant();
        this.displayAngle = config.getDisplayAngle();
        this.displayWidth = getImageWidth();
        this.displayHeight = getImageHeight();

        int initialScale = getImageScale(this.displayAngle);
        int windowWidth = displayWidth * initialScale;
        int windowHeight = displayHeight * initialScale;

        this.renderer = new Renderer();
        this.frame = new JFrame();

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        renderer.setPreferredSize(new Dimension(windowWidth, windowHeight));
        renderer.setFocusable(true);
        keyAdapters.forEach(renderer::addKeyListener);

        frame.setBackground(Color.BLACK);
        frame.getContentPane().setBackground(Color.BLACK);
        frame.getContentPane().add(renderer);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setIgnoreRepaint(false);
        frame.pack();

        int x = (screenSize.width - frame.getWidth()) / 2;
        int y = (screenSize.height - frame.getHeight()) / 2;
        frame.setLocation(x, y - 15);
        frame.setMinimumSize(new Dimension(displayWidth * (initialScale / 2), displayHeight * (initialScale / 2)));
        frame.setMaximumSize(screenSize);
        frame.setVisible(true);
        frame.setResizable(true);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                renderer.requestFocusInWindow();
            }
        });

        renderThread = new Thread(this::renderLoop, "jchip-Render-Thread");
        renderThread.setDaemon(true);
        renderThread.start();
    }

    public HexSpriteFont getCharacterSpriteFont() {
        return chip8Variant.getSpriteFont();
    }

    public abstract int getWidth();

    public abstract int getHeight();

    protected abstract int getImageWidth();

    protected abstract int getImageHeight();

    protected abstract int getImageScale(DisplayAngle displayAngle);

    protected abstract void updateRenderBuffer();

    protected abstract void fillImageBuffer(int[] buffer);

    public abstract void clear();

    public void flush(int currentInstructionsPerFrame) {
        this.totalIpfSinceLastUpdate += currentInstructionsPerFrame;
        this.updateRenderBuffer();
        synchronized (this.renderLock) {
            this.frameRequested = true;
            this.renderLock.notify();
        }
    }

    private void renderLoop() {
        try {
            while (this.running) {
                synchronized (this.renderLock) {
                    while (!this.frameRequested) {
                        try {
                            this.renderLock.wait();
                        } catch (InterruptedException ignored) {
                        }
                    }
                    this.frameRequested = false;
                }
                this.renderer.render();
                this.updateWindowTitle();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateWindowTitle() {
        long now = System.nanoTime();
        double lastFrameDuration = now - lastFrameTime;
        lastFrameTime = now;
        totalFrameTimeSinceLastUpdate += lastFrameDuration;
        framesSinceLastUpdate++;

        long deltaTime = now - lastWindowTitleUpdate;
        if (deltaTime < 1_000_000_000L) {
            return;
        }

        double fps = framesSinceLastUpdate / (deltaTime / 1_000_000_000.0);
        long averageIpf = totalIpfSinceLastUpdate / framesSinceLastUpdate;
        double averageFrameTimeMs = (totalFrameTimeSinceLastUpdate / framesSinceLastUpdate) / 1_000_000.0;
        double mips = (averageIpf * fps) / 1_000_000.0;

        framesSinceLastUpdate = 0;
        totalIpfSinceLastUpdate = 0;
        totalFrameTimeSinceLastUpdate = 0;
        lastWindowTitleUpdate = now;

        stringBuilder.append("jchip ").append(Main.VERSION_STRING)
                .append(" | ").append(chip8Variant.getDisplayName())
                .append(romTitle)
                .append(" | IPF: ").append(averageIpf)
                .append(" | MIPS: ").append(String.format("%.2f", mips))
                .append(" | Frame Time: ").append(String.format("%.2f ms", averageFrameTimeMs))
                .append(" | FPS: ").append(String.format("%.2f", fps));

        String title = stringBuilder.toString();
        stringBuilder.setLength(0);

        SwingUtilities.invokeLater(() -> frame.setTitle(title));
    }

    @Override
    public void close() {
        this.running = false;
        synchronized (this.renderLock) {
            this.renderLock.notify();
        }
        SwingUtilities.invokeLater(frame::dispose);
    }

    private class Renderer extends Canvas {
        private final BufferedImage backBuffer;
        private final AffineTransform rotationTransform;
        private final AffineTransform drawTransform = new AffineTransform();
        private int lastWidth = -1;
        private int lastHeight = -1;

        private Renderer() {
            backBuffer = new BufferedImage(displayWidth, displayHeight, BufferedImage.TYPE_INT_ARGB);
            setIgnoreRepaint(false);

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
        }

        private void updateTransformIfNeeded() {
            double windowWidth = getWidth();
            double windowHeight = getHeight();
            if (windowWidth != lastWidth || windowHeight != lastHeight) {
                double logicalWidth = (displayAngle == DisplayAngle.DEG_90 || displayAngle == DisplayAngle.DEG_270)
                        ? displayHeight : displayWidth;
                double logicalHeight = (displayAngle == DisplayAngle.DEG_90 || displayAngle == DisplayAngle.DEG_270)
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
            fillImageBuffer(pixels);
            Graphics2D g = (Graphics2D) bufferStrategy.getDrawGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g.drawImage(backBuffer, drawTransform, null);
            g.dispose();
            bufferStrategy.show();
            Toolkit.getDefaultToolkit().sync();
        }
    }
}
