package io.github.arkosammy12.jchip.hardware;

import io.github.arkosammy12.jchip.util.ColorPalette;
import io.github.arkosammy12.jchip.util.ConsoleVariant;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Arrays;

public class BufferedImageDisplay extends AbstractDisplay {

    private final DisplayCanvas displayCanvas;
    private final JFrame frame;
    private final int pixelScale;

    private long lastTitleUpdate = 0;
    private long lastFrameTime = System.nanoTime();
    private int framesSinceLastUpdate = 0;
    private double lastFPS = 0;
    private double lastFrameDurationMs = 0;

    public BufferedImageDisplay(String title, ConsoleVariant consoleVariant, KeyAdapter keyAdapter, ColorPalette colorPalette) {
        super(title, consoleVariant, colorPalette);
        int pixelScale = 20;
        if (consoleVariant != ConsoleVariant.CHIP_8) {
            pixelScale = 10;
        }
        this.pixelScale = pixelScale;
        this.displayCanvas = new DisplayCanvas();
        Dimension windowSize = new Dimension(this.screenWidth * pixelScale, this.screenHeight * pixelScale);
        this.displayCanvas.setPreferredSize(windowSize);
        this.displayCanvas.setMinimumSize(windowSize);
        this.displayCanvas.setMaximumSize(windowSize);
        this.displayCanvas.addKeyListener(keyAdapter);
        this.displayCanvas.setVisible(true);

        this.frame = new JFrame();
        this.frame.add(this.displayCanvas);
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
    public void flush(int currentInstructionsPerFrame) {
        this.displayCanvas.render();
        long now = System.nanoTime();
        lastFrameDurationMs = (now - lastFrameTime) / 1_000_000.0;
        lastFrameTime = now;
        framesSinceLastUpdate++;
        long deltaTime = System.currentTimeMillis() - lastTitleUpdate;
        if (deltaTime >= 1000) {
            lastFPS = framesSinceLastUpdate / ((deltaTime) / 1000.0);
            framesSinceLastUpdate = 0;
            double mips = (double) (currentInstructionsPerFrame * 60) / 1_000_000;
            this.frame.setTitle(
                    String.format("%s %s| IPF: %d | MIPS: %.2f | Frame Time: %.2f ms | FPS: %.2f",
                            this.consoleVariant.getDisplayName(),
                            this.title != null ? "| " + title + " " : "",
                            currentInstructionsPerFrame,
                            mips,
                            lastFrameDurationMs,
                            lastFPS));
            lastTitleUpdate = System.currentTimeMillis();
        }
    }

    @Override
    public void close() {
        this.frame.dispose();
    }

    private class DisplayCanvas extends Canvas {
        private final BufferedImage backBuffer;

        private DisplayCanvas() {
            backBuffer = new BufferedImage(screenWidth, screenHeight, BufferedImage.TYPE_INT_RGB);
            int[] buffer = ((DataBufferInt) backBuffer.getRaster().getDataBuffer()).getData();
            int defaultColor = colorPalette.getRawColor(0);
            for (int y = 0; y < screenHeight; y++) {
                for (int x = 0; x < screenWidth; x++) {
                    buffer[y * screenWidth + x] = defaultColor;
                }
            }
        }

        private void render() {
            int[] buffer = ((DataBufferInt) backBuffer.getRaster().getDataBuffer()).getData();
            for (int y = 0; y < screenHeight; y++) {
                for (int x = 0; x < screenWidth; x++) {
                    int current = frameBuffer[x][y] & 0xF;
                    int previous = previousFrameBuffer[x][y] & 0xF;
                    if (current == previous) {
                        continue;
                    }
                    int color = colorPalette.getRawColor(current);
                    buffer[y * screenWidth + x] = color;
                    previousFrameBuffer[x][y] = current;
                }
            }
            BufferStrategy bufferStrategy = getBufferStrategy();
            if (bufferStrategy == null) {
                createBufferStrategy(3);
                return;
            }
            Graphics2D g = (Graphics2D) bufferStrategy.getDrawGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g.drawImage(backBuffer, 0, 0, screenWidth * pixelScale, screenHeight * pixelScale, null);
            g.dispose();
            bufferStrategy.show();
            Toolkit.getDefaultToolkit().sync();
        }
    }

}
