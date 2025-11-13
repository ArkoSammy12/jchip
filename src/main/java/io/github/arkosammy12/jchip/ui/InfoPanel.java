package io.github.arkosammy12.jchip.ui;

import io.github.arkosammy12.jchip.emulators.Chip8Emulator;
import io.github.arkosammy12.jchip.emulators.Emulator;
import io.github.arkosammy12.jchip.video.Display;
import io.github.arkosammy12.jchip.video.EmulatorRenderer;

import javax.swing.*;
import java.awt.*;

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

        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        JPanel variantPanel = new JPanel(new GridLayout(1, 0, 0, 0));
        variantPanel.add(this.variantLabel);
        variantPanel.setToolTipText("The CHIP-8 being used to run the current ROM.");

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
        frameTimePanel.setToolTipText("The current frame time value average, in miliseconds.");

        JPanel fpsPanel = new JPanel(new GridLayout(1, 0, 0, 0));
        fpsPanel.add(fpsLabel);
        fpsPanel.setToolTipText("The current frames per second value average.");

        this.setPreferredSize(new Dimension(100, 20));

        this.add(new JScrollPane(variantPanel));
        this.add(new JScrollPane(romTitlePanel));
        this.add(new JScrollPane(ipfPanel));
        this.add(new JScrollPane(mipsPanel));
        this.add(new JScrollPane(frameTimePanel));
        this.add(new JScrollPane(fpsPanel));

    }

    public void update(Emulator emulator) {
        this.totalIpfSinceLastUpdate += emulator.getCurrentInstructionsPerFrame();
        long now = System.nanoTime();
        double lastFrameDuration = now - lastFrameTime;
        lastFrameTime = now;
        totalFrameTimeSinceLastUpdate += lastFrameDuration;
        framesSinceLastUpdate++;

        long deltaTime = now - lastWindowTitleUpdate;
        if (deltaTime < 1_000_000_000L) {
            return;
        }

        double fps = framesSinceLastUpdate / (deltaTime / 1_000_000_000.0);
        long averageIpf = totalIpfSinceLastUpdate / framesSinceLastUpdate;
        double averageFrameTimeMs = (totalFrameTimeSinceLastUpdate / framesSinceLastUpdate) / 1_000_000.0;
        double mips = (averageIpf * fps) / 1_000_000.0;

        framesSinceLastUpdate = 0;
        totalIpfSinceLastUpdate = 0;
        totalFrameTimeSinceLastUpdate = 0;
        lastWindowTitleUpdate = now;

        Display<?> display = emulator.getDisplay();
        EmulatorRenderer renderer = display.getEmulatorRenderer();

        SwingUtilities.invokeLater(() -> {
            this.variantLabel.setText(emulator.getChip8Variant().getDisplayName());
            this.romTitleLabel.setText(renderer.getRomTitle());
            this.ipfLabel.setText("IPF: " + averageIpf);
            this.mipsLabel.setText("MIPS: " + String.format("%.2f", mips));
            this.frameTimeLabel.setText("Frame time: " + String.format("%.2f ms", averageFrameTimeMs));
            this.fpsLabel.setText("FPS: " + String.format("%.2f", fps));
        });

    }

    public void clear() {
        lastWindowTitleUpdate = 0;
        lastFrameTime = System.nanoTime();
        framesSinceLastUpdate = 0;
        totalIpfSinceLastUpdate = 0;
        totalFrameTimeSinceLastUpdate = 0;
        SwingUtilities.invokeLater(() -> {
            this.variantLabel.setText("");
            this.romTitleLabel.setText("");
            this.ipfLabel.setText("");
            this.mipsLabel.setText("");
            this.frameTimeLabel.setText("");
            this.fpsLabel.setText("");
        });

    }

}
