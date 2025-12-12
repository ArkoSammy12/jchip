package io.github.arkosammy12.jchip.ui.menus;

import io.github.arkosammy12.jchip.emulators.Emulator;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class InfoPanel extends JPanel {

    private final JTextField variantField = createField();
    private final JTextField romTitleField = createField();
    private final JTextField ipfField = createField();
    private final JTextField mipsField = createField();
    private final JTextField frameTimeField = createField();
    private final JTextField fpsField = createField();

    private long lastWindowTitleUpdate = 0;
    private long lastFrameTime = System.nanoTime();
    private int framesSinceLastUpdate = 0;
    private long totalIpfSinceLastUpdate = 0;
    private double totalFrameTimeSinceLastUpdate = 0;

    public InfoPanel() {
        super(new GridLayout(1, 6, 0, 0));
        this.setPreferredSize(new Dimension(100, 28));

        this.add(createScrollPanel(variantField, "The variant used by the currently running ROM."));
        this.add(createScrollPanel(romTitleField, "The name or file name of the currently running ROM."));
        this.add(createScrollPanel(ipfField, "The current IPF value average."));
        this.add(createScrollPanel(mipsField, "The current MIPS value average."));
        this.add(createScrollPanel(frameTimeField, "The current frame time value average, in milliseconds."));
        this.add(createScrollPanel(fpsField, "The current frames per second value average."));
    }

    private static JTextField createField() {
        JTextField field = new JTextField();
        field.setEditable(false);
        field.setBorder(null);
        field.setFocusable(false);
        field.setBackground(null);
        return field;
    }

    private static JScrollPane createScrollPanel(JTextField field, String tooltip) {
        field.setToolTipText(tooltip);
        return new JScrollPane(field,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    }

    public void onFrame(Emulator emulator) {
        String romTitle = emulator.getEmulatorSettings().getRomTitle().orElse("N/A");
        String variantName = emulator.getVariant().getDisplayName();

        boolean updateTitleNow = false;
        boolean updateStatsNow = false;

        if (!Objects.equals(this.romTitleField.getText(), romTitle) || !Objects.equals(this.variantField.getText(), variantName)) {
            updateTitleNow = true;
        }

        this.totalIpfSinceLastUpdate += emulator.getCurrentInstructionsPerFrame();
        long now = System.nanoTime();
        double lastFrameDuration = now - lastFrameTime;
        lastFrameTime = now;
        totalFrameTimeSinceLastUpdate += lastFrameDuration;
        framesSinceLastUpdate++;

        long deltaTime = now - lastWindowTitleUpdate;

        double fps = 0;
        long averageIpf = 0;
        double averageFrameTimeMs = 0;
        double mips = 0;

        if (deltaTime >= 1_000_000_000L) {
            updateStatsNow = true;

            fps = framesSinceLastUpdate / (deltaTime / 1_000_000_000.0);
            averageIpf = totalIpfSinceLastUpdate / framesSinceLastUpdate;
            averageFrameTimeMs = (totalFrameTimeSinceLastUpdate / framesSinceLastUpdate) / 1_000_000.0;
            mips = (averageIpf * fps) / 1_000_000.0;

            framesSinceLastUpdate = 0;
            totalIpfSinceLastUpdate = 0;
            totalFrameTimeSinceLastUpdate = 0;
            lastWindowTitleUpdate = now;
        }

        if (updateTitleNow || updateStatsNow) {
            final boolean fUpdateTitle = updateTitleNow;
            final boolean fUpdateStats = updateStatsNow;
            final String fRomTitle = romTitle;
            final String fVariantName = variantName;
            final double fFps = fps;
            final long fAverageIpf = averageIpf;
            final double fAverageFrameTimeMs = averageFrameTimeMs;
            final double fMips = mips;

            SwingUtilities.invokeLater(() -> {
                if (fUpdateTitle) {
                    romTitleField.setText(fRomTitle);
                    variantField.setText(fVariantName);
                }

                if (fUpdateStats) {
                    this.ipfField.setText("IPF: " + fAverageIpf);
                    this.mipsField.setText("MIPS: " + String.format("%.2f", fMips));
                    this.frameTimeField.setText("Frame time: " + String.format("%.2f ms", fAverageFrameTimeMs));
                    this.fpsField.setText("FPS: " + String.format("%.2f", fFps));
                }

                this.revalidate();
                this.repaint();
            });
        }
    }


    public void onStopped() {
        lastWindowTitleUpdate = 0;
        lastFrameTime = System.nanoTime();
        framesSinceLastUpdate = 0;
        totalIpfSinceLastUpdate = 0;
        totalFrameTimeSinceLastUpdate = 0;
        SwingUtilities.invokeLater(() -> {
            this.variantField.setText("");

            this.romTitleField.setText("");

            this.ipfField.setText("");
            this.mipsField.setText("");
            this.frameTimeField.setText("");
            this.fpsField.setText("");
            this.revalidate();
            this.repaint();
        });

    }
}
