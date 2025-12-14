package io.github.arkosammy12.jchip.ui;

import io.github.arkosammy12.jchip.video.EmulatorRenderer;
import net.miginfocom.layout.LC;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

public class EmulatorViewport extends JPanel {

    private EmulatorRenderer emulatorRenderer;

    public EmulatorViewport() {
        MigLayout migLayout = new MigLayout(new LC().insets("0"));
        super(migLayout);
        this.setBackground(Color.BLACK);
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
            this.add(emulatorRenderer, "grow, push");
            this.emulatorRenderer.requestFocusInWindow();
            this.revalidate();
            this.repaint();

        });
    }
}
