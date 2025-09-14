package io.github.arkosammy12.jchip.hardware;

import io.github.arkosammy12.jchip.util.ColorPalette;
import io.github.arkosammy12.jchip.util.Chip8Variant;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

public class CanvasDisplay extends AbstractDisplay {

    private final Renderer renderer;
    private final JFrame frame;
    private final int pixelScale;

    public CanvasDisplay(String romTitle, Chip8Variant chip8Variant, KeyAdapter keyAdapter, ColorPalette colorPalette) {
        super(romTitle, chip8Variant, colorPalette);
        int pixelScale = 20;
        if (chip8Variant != Chip8Variant.CHIP_8) {
            pixelScale = 10;
        }
        this.pixelScale = pixelScale;
        this.renderer = new Renderer();
        Dimension windowSize = new Dimension(this.screenWidth * pixelScale, this.screenHeight * pixelScale);
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
    public void flush(int currentInstructionsPerFrame) {
        this.renderer.render();
        String title = this.getWindowTitle(currentInstructionsPerFrame);
        if (title != null) {
            this.frame.setTitle(title);
        }
    }

    @Override
    public void close() {
        this.frame.dispose();
    }

    private class Renderer extends Canvas {

        private final BufferedImage backBuffer;
        private final int scaledWidth;
        private final int scaledHeight;

        private Renderer() {
            backBuffer = new BufferedImage(screenWidth, screenHeight, BufferedImage.TYPE_INT_RGB);
            this.scaledWidth = screenWidth * pixelScale;
            this.scaledHeight = screenHeight * pixelScale;
            int[] buffer = ((DataBufferInt) backBuffer.getRaster().getDataBuffer()).getData();
            int defaultColor = colorPalette.getIntColor(0);
            for (int y = 0; y < screenHeight; y++) {
                for (int x = 0; x < screenWidth; x++) {
                    buffer[y * screenWidth + x] = defaultColor;
                }
            }
        }

        private void render() {
            int[] buffer = ((DataBufferInt) backBuffer.getRaster().getDataBuffer()).getData();
            for (int y = 0; y < screenHeight; y++) {
                int base = y * screenWidth;
                for (int x = 0; x < screenWidth; x++) {
                    buffer[base + x] = colorPalette.getIntColor(frameBuffer[x][y] & 0xF);
                }
            }
            BufferStrategy bufferStrategy = getBufferStrategy();
            if (bufferStrategy == null) {
                createBufferStrategy(3);
                return;
            }
            Graphics2D g = (Graphics2D) bufferStrategy.getDrawGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g.drawImage(backBuffer, 0, 0, scaledWidth, scaledHeight, null);
            g.dispose();
            bufferStrategy.show();
        }
    }

}
