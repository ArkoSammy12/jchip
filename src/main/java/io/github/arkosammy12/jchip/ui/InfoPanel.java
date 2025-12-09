package io.github.arkosammy12.jchip.ui;

import io.github.arkosammy12.jchip.emulators.Emulator;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class InfoPanel extends JPanel {

    private final JLabel variantLabel = new JLabel();
    private final JLabel romTitleLabel = new JLabel();
    private final JLabel ipfLabel = new JLabel();
    private final JLabel mipsLabel = new JLabel();
    private final JLabel frameTimeLabel = new JLabel();
    private final JLabel fpsLabel = new JLabel();

    private long lastWindowTitleUpdate = 0;
    private long lastFrameTime = System.nanoTime();
    private int framesSinceLastUpdate = 0;
    private long totalIpfSinceLastUpdate = 0;
    private double totalFrameTimeSinceLastUpdate = 0;

    public InfoPanel() {
        super();

        this.setLayout(new GridLayout(1, 6, 0, 0));

        JPanel variantPanel = new JPanel(new GridLayout(1, 0, 0, 0));
        variantPanel.add(this.variantLabel);
        variantPanel.setToolTipText("The variant being used to run the current ROM.");

        JPanel romTitlePanel = new JPanel(new GridLayout(1, 0, 0, 0));
        romTitlePanel.add(this.romTitleLabel);
        romTitlePanel.setToolTipText("The name of the running ROM, or the file name.");

        JPanel ipfPanel = new JPanel(new GridLayout(1, 0, 0, 0));
        ipfPanel.add(this.ipfLabel);
        ipfPanel.setToolTipText("The current IPF value average.");

        JPanel mipsPanel = new JPanel(new GridLayout(1, 0, 0, 0));
        mipsPanel.add(mipsLabel);
        mipsPanel.setToolTipText("The current MIPS (millions of instructions per second) value average.");

        JPanel frameTimePanel = new JPanel(new GridLayout(1, 0, 0, 0));
        frameTimePanel.add(frameTimeLabel);
        frameTimePanel.setToolTipText("The current frame time value average, in milliseconds.");

        JPanel fpsPanel = new JPanel(new GridLayout(1, 0, 0, 0));
        fpsPanel.add(fpsLabel);
        fpsPanel.setToolTipText("The current frames per second value average.");

        this.setPreferredSize(new Dimension(100, 22));

        this.add(new JScrollPane(variantPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER));
        this.add(new JScrollPane(romTitlePanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER));
        this.add(new JScrollPane(ipfPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER));
        this.add(new JScrollPane(mipsPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER));
        this.add(new JScrollPane(frameTimePanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER));
        this.add(new JScrollPane(fpsPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER));

    }

    public void onFrame(Emulator emulator) {
        String romTitle = emulator.getEmulatorSettings().getRomTitle().orElse("N/A");
        String variantName = emulator.getVariant().getDisplayName();

        boolean updateTitleNow = false;
        boolean updateStatsNow = false;

        if (!Objects.equals(this.romTitleLabel.getText(), romTitle) || !Objects.equals(this.variantLabel.getText(), variantName)) {
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
                    romTitleLabel.setText(fRomTitle);
                    romTitleLabel.setToolTipText(fRomTitle);
                    variantLabel.setText(fVariantName);
                }

                if (fUpdateStats) {
                    this.ipfLabel.setText("IPF: " + fAverageIpf);
                    this.mipsLabel.setText("MIPS: " + String.format("%.2f", fMips));
                    this.frameTimeLabel.setText("Frame time: " + String.format("%.2f ms", fAverageFrameTimeMs));
                    this.fpsLabel.setText("FPS: " + String.format("%.2f", fFps));
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
            this.variantLabel.setText("");

            this.romTitleLabel.setText("");
            this.romTitleLabel.setToolTipText("The name of the running ROM, or the file name.");

            this.ipfLabel.setText("");
            this.mipsLabel.setText("");
            this.frameTimeLabel.setText("");
            this.fpsLabel.setText("");
            this.revalidate();
            this.repaint();
        });

    }

}
