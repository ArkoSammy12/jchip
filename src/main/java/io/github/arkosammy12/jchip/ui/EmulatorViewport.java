package io.github.arkosammy12.jchip.ui;

import io.github.arkosammy12.jchip.video.DisplayRenderer;
import net.miginfocom.layout.LC;
import net.miginfocom.swing.MigLayout;
import org.tinylog.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class EmulatorViewport extends JPanel {

    private DisplayRenderer displayRenderer;

    public EmulatorViewport() {
        MigLayout migLayout = new MigLayout(new LC().insets("0"));
        super(migLayout);
        this.setFocusable(true);
        this.setBackground(Color.BLACK);
        this.setPreferredSize(new Dimension(960, this.getHeight()));

        this.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e){
                SwingUtilities.invokeLater(() -> requestFocusInWindow());
            }

        });

    }

    public void setEmulatorRenderer(DisplayRenderer displayRenderer) {
        SwingUtilities.invokeLater(() -> {
            if (this.displayRenderer != null) {
                this.displayRenderer.close();
                this.remove(this.displayRenderer);
            }
            this.displayRenderer = displayRenderer;

            if (displayRenderer != null) {
                displayRenderer.setFocusable(true);
                displayRenderer.setOpaque(true);
                displayRenderer.setBackground(Color.BLACK);
                displayRenderer.addMouseListener(new MouseAdapter() {

                    @Override
                    public void mousePressed(MouseEvent e) {
                        SwingUtilities.invokeLater(displayRenderer::requestFocusInWindow);
                    }

                });

                this.add(displayRenderer, "grow, push");
                SwingUtilities.invokeLater(displayRenderer::requestFocusInWindow);
            }
            this.revalidate();
            this.repaint();
        });
    }

}
