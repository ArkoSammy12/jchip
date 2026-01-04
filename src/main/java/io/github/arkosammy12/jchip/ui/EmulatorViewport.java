package io.github.arkosammy12.jchip.ui;

import io.github.arkosammy12.jchip.Jchip;
import io.github.arkosammy12.jchip.video.DisplayRenderer;
import net.miginfocom.layout.LC;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class EmulatorViewport extends JPanel {

    private DisplayRenderer displayRenderer;

    public EmulatorViewport(Jchip jchip) {
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

        jchip.addStateChangedListener((emulator, _, newState) -> {
            if (emulator == null) {
                this.setDisplayRenderer(null);
                return;
            }
            if (newState.isStopping()) {
                this.setDisplayRenderer(null);
            } else if (newState.isResetting()) {
                this.setDisplayRenderer(emulator.getDisplay().getDisplayRenderer());
            }
        });
        jchip.addFrameListener(emulator -> {
            if (emulator != null) {
                emulator.getDisplay().getDisplayRenderer().requestFrame();
            }
        });
        jchip.addShutdownListener(() -> this.setDisplayRenderer(null));
    }

    private DisplayRenderer getDisplayRenderer() {
        return this.displayRenderer;
    }

    private void setDisplayRenderer(DisplayRenderer newRenderer) {
        SwingUtilities.invokeLater(() -> {
            DisplayRenderer currentRenderer = this.getDisplayRenderer();
            if (currentRenderer != null) {
                currentRenderer.close();
                this.remove(currentRenderer);
            }
            this.displayRenderer = newRenderer;

            if (newRenderer != null) {
                newRenderer.setFocusable(true);
                newRenderer.setOpaque(true);
                newRenderer.setBackground(Color.BLACK);
                newRenderer.addMouseListener(new MouseAdapter() {

                    @Override
                    public void mousePressed(MouseEvent e) {
                        SwingUtilities.invokeLater(newRenderer::requestFocusInWindow);
                    }

                });

                this.add(newRenderer, "grow, push");
                SwingUtilities.invokeLater(newRenderer::requestFocusInWindow);
            }
            this.revalidate();
            this.repaint();
        });
    }

}
