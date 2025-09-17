package io.github.arkosammy12.jchip.hardware;

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

public class CanvasDisplay extends AbstractDisplay {

    private final Renderer renderer;
    private final JFrame frame;
    private final DisplayAngle displayAngle;
    private final int pixelScale;

    public CanvasDisplay(EmulatorConfig config, KeyAdapter keyAdapter) {
        super(config);
        this.displayAngle = config.getDisplayAngle();
        int windowWidth;
        int windowHeight;
        this.pixelScale = switch (displayAngle) {
            case DEG_90, DEG_270 -> {
                int scale = chip8Variant == Chip8Variant.CHIP_8 ? 11 : 6;
                windowWidth = this.screenHeight * scale;
                windowHeight = this.screenWidth * scale;
                yield scale;
            }
            default -> {
                int scale = chip8Variant == Chip8Variant.CHIP_8 ? 20 : 10;
                windowWidth = this.screenWidth * scale;
                windowHeight = this.screenHeight * scale;
                yield scale;
            }
        };
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
            g.drawImage(backBuffer, imageTransform, null);
            g.dispose();
            bufferStrategy.show();
        }
    }

}
