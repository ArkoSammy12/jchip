package io.github.arkosammy12.jchip.hardware;

import io.github.arkosammy12.jchip.util.ColorPalette;
import io.github.arkosammy12.jchip.util.ConsoleVariant;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Arrays;

public class Graphics2DDisplay extends AbstractDisplay {

    private final DisplayCanvas displayCanvas;
    private final JFrame frame;
    private long lastTitleUpdate = 0;
    private final int pixelScale;

    public Graphics2DDisplay(String title, ConsoleVariant consoleVariant, KeyAdapter keyAdapter, ColorPalette colorPalette) {
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
        this.frame.setResizable(false);
        this.frame.setVisible(true);
    }

    @Override
    public void flush(int currentInstructionsPerFrame) {
        this.displayCanvas.render();
        long now = System.currentTimeMillis();
        if (now - lastTitleUpdate >= 1000) {
            double mips = (double) (currentInstructionsPerFrame * 60) / 1_000_000;
                this.frame.setTitle(
                        String.format("%s %s| IPF: %d | Mips: %f",
                                this.consoleVariant.getDisplayName(),
                                this.title != null ? "| " + title + " " : "",
                                currentInstructionsPerFrame,
                                mips));
            lastTitleUpdate = now;
        }
    }

    @Override
    public void close() {
        this.frame.dispose();
    }

    private class DisplayCanvas extends Canvas {

        private final BufferedImage backBuffer;

        private DisplayCanvas() {
            backBuffer = new BufferedImage(screenWidth * pixelScale, screenHeight * pixelScale, BufferedImage.TYPE_INT_RGB);
            int defaultColor = colorPalette.getRGB(0);
            int[] pixels = ((DataBufferInt) backBuffer.getRaster().getDataBuffer()).getData();
            Arrays.fill(pixels, defaultColor);
        }

        private void render() {
            int scaledWidth = screenWidth * pixelScale;
            int[] buffer = ((DataBufferInt) backBuffer.getRaster().getDataBuffer()).getData();
            for (int y = 0; y < screenHeight; y++) {
                for (int x = 0; x < screenWidth; x++) {
                    int current = frameBuffer[x][y] & 0xF;
                    int previous = previousFrameBuffer[x][y] & 0xF;
                    if (current == previous) {
                        continue;
                    }
                    // index = (y * screenWidth) + x
                    // index = (y * screenWidth) + (x * pixelScale)
                    // index = ((y * pixelScale) * screenWidth) + (x * pixelScale)
                    // index = ((y * pixelScale) * (screenWidth * pixelScale)) + (x * pixelScale)
                    // index = ((y * pixelScale + pixelOffset) * (screenWidth * pixelScale)) + (x * pixelScale)
                    int color = colorPalette.getRGB(current);
                    int verticalRowOffset = y * pixelScale;
                    for (int verticalPixelOffset = 0; verticalPixelOffset < pixelScale; verticalPixelOffset++) {
                        int rowOffset = (verticalRowOffset + verticalPixelOffset) * scaledWidth;
                        int offset = rowOffset + x * pixelScale;
                        Arrays.fill(buffer, offset, offset + pixelScale, color);
                    }
                    previousFrameBuffer[x][y] = current;
                }
            }
            repaint();
        }

        @Override
        public void paint(Graphics g) {
            g.drawImage(this.backBuffer, 0, 0, null);
        }

        @Override
        public void update(Graphics g) {
            paint(g);
        }
    }

}
