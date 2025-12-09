package io.github.arkosammy12.jchip.ui;

import io.github.arkosammy12.jchip.video.EmulatorRenderer;

import javax.swing.*;
import java.awt.*;

public class EmulatorViewport extends JPanel {

    private EmulatorRenderer emulatorRenderer;

    public EmulatorViewport() {
        super(new BorderLayout());
        this.setBackground(Color.BLACK);
    }

    @Override
    public Dimension getPreferredSize() {
        if (emulatorRenderer != null) {
            return emulatorRenderer.getPreferredSize();
        }
        return new Dimension(300, 300);
    }

    public void setEmulatorRenderer(EmulatorRenderer emulatorRenderer) {
        SwingUtilities.invokeLater(() -> {
            if (this.emulatorRenderer != null) {
                this.emulatorRenderer.close();
                this.remove(this.emulatorRenderer);
            }
            this.emulatorRenderer = emulatorRenderer;

            if (emulatorRenderer == null) {
                this.revalidate();
                this.repaint();
                return;
            }
            this.add(emulatorRenderer, BorderLayout.CENTER);

            int w = emulatorRenderer.getDisplayWidth();
            int h = emulatorRenderer.getDisplayHeight();
            int scale = emulatorRenderer.getInitialScale();

            Dimension scaled = new Dimension(w * scale, h * scale);
            this.setPreferredSize(scaled);
            this.setMinimumSize(new Dimension(w * (scale / 2), h * (scale / 2)));

            this.emulatorRenderer.requestFocusInWindow();
            this.revalidate();
            this.repaint();

        });
    }
}
