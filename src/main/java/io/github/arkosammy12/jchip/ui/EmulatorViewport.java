package io.github.arkosammy12.jchip.ui;

import io.github.arkosammy12.jchip.video.DisplayRenderer;
import net.miginfocom.layout.LC;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

public class EmulatorViewport extends JPanel {

    private DisplayRenderer displayRenderer;

    public EmulatorViewport() {
        MigLayout migLayout = new MigLayout(new LC().insets("0"));
        super(migLayout);
        this.setFocusable(false);
        this.setBackground(Color.BLACK);
    }

    public void setEmulatorRenderer(DisplayRenderer displayRenderer) {
        SwingUtilities.invokeLater(() -> {
            if (this.displayRenderer != null) {
                this.displayRenderer.close();
                this.remove(this.displayRenderer);
            }
            this.displayRenderer = displayRenderer;

            if (displayRenderer == null) {
                this.revalidate();
                this.repaint();
                return;
            }
            this.add(displayRenderer, "grow, push");
            this.revalidate();
            this.repaint();

        });
    }
}
