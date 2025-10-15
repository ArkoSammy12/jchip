package io.github.arkosammy12.jchip.ui;

import io.github.arkosammy12.jchip.JChip;
import io.github.arkosammy12.jchip.Main;
import io.github.arkosammy12.jchip.exceptions.EmulatorException;
import org.tinylog.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.Closeable;
import java.util.function.Consumer;

public class MainWindow extends JFrame implements Closeable {

    private static final String DEFAULT_TITLE = "jchip " + Main.VERSION_STRING;

    private EmulatorRenderer emulatorRenderer;
    private final SettingsMenu settingsBar;

    private long lastWindowTitleUpdate = 0;
    private long lastFrameTime = System.nanoTime();
    private int framesSinceLastUpdate = 0;
    private long totalIpfSinceLastUpdate = 0;
    private double totalFrameTimeSinceLastUpdate = 0;
    private final StringBuilder stringBuilder = new StringBuilder(128);

    public MainWindow(JChip jchip) {
        super();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setBackground(Color.BLACK);
        this.getContentPane().setBackground(Color.BLACK);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setIgnoreRepaint(false);
        this.pack();
        this.setSize((int) (screenSize.getWidth() / 2), (int) (screenSize.getHeight() / 2));
        this.setLocation((screenSize.width - this.getWidth()) / 2, ((screenSize.height - this.getHeight()) / 2) - 15);
        this.setMaximumSize(screenSize);

        this.getContentPane().setLayout(new BorderLayout());

        JPanel debuggerPanel = new JPanel();
        debuggerPanel.setBackground(Color.DARK_GRAY);
        debuggerPanel.setLayout(new GridLayout(0, 1, 2, 2));
        debuggerPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                "Live Debugger", 0, 0, new Font("Monospaced", Font.BOLD, 12), Color.WHITE));

        JLabel pcLabel = new JLabel("PC: 0x0000");
        JLabel iLabel = new JLabel("I: 0x0000");
        JLabel delayTimerLabel = new JLabel("Delay Timer: 0");
        JLabel soundTimerLabel = new JLabel("Sound Timer: 0");
        JLabel stackLabel = new JLabel("Stack: [0x0000, 0x0000, ...]");

        pcLabel.setForeground(Color.WHITE);
        iLabel.setForeground(Color.WHITE);
        delayTimerLabel.setForeground(Color.WHITE);
        soundTimerLabel.setForeground(Color.WHITE);
        stackLabel.setForeground(Color.WHITE);

        debuggerPanel.add(pcLabel);
        debuggerPanel.add(iLabel);
        debuggerPanel.add(delayTimerLabel);
        debuggerPanel.add(soundTimerLabel);
        debuggerPanel.add(stackLabel);

        this.getContentPane().add(debuggerPanel, BorderLayout.EAST);

        InputMap im = this.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = this.getRootPane().getActionMap();

        im.put(KeyStroke.getKeyStroke("F2"), "resetEmulator");
        am.put("resetEmulator", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    jchip.reset();
                } catch (EmulatorException cause) {
                    Logger.info("Error automatically resetting emulator: {}", cause);
                }
            }
        });

        im.put(KeyStroke.getKeyStroke("ESCAPE"), "stopEmulator");
        am.put("stopEmulator", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    jchip.stop();
                } catch (EmulatorException cause) {
                    Logger.info("Error stopping emulator: {}", cause);
                }
            }
        });

        this.settingsBar = new SettingsMenu(jchip);
        this.setTitle(DEFAULT_TITLE);
        this.setJMenuBar(this.settingsBar);
        this.requestFocusInWindow();
        this.setVisible(true);
        this.setResizable(true);
    }

    public void setGameRenderer(EmulatorRenderer emulatorRenderer) {
        SwingUtilities.invokeLater(() -> {
            if (this.emulatorRenderer != null) {
                this.getContentPane().remove(this.emulatorRenderer);
            }
            if (emulatorRenderer == null) {
                this.getContentPane().repaint();
                this.revalidate();
                this.setTitle(DEFAULT_TITLE);
                return;
            }
            this.emulatorRenderer = emulatorRenderer;
            this.getContentPane().add(emulatorRenderer);
            int displayWidth = emulatorRenderer.getDisplayWidth();
            int displayHeight = emulatorRenderer.getDisplayHeight();
            int initialScale = emulatorRenderer.getInitialScale();
            this.setMinimumSize(new Dimension(displayWidth * (initialScale / 2), displayHeight * (initialScale / 2)));
            this.repaint();
            this.revalidate();
            emulatorRenderer.requestFocusInWindow();
        });
    }

    public EmulatorRenderer getGameRenderer() {
        return this.emulatorRenderer;
    }

    public SettingsMenu getSettingsBar() {
        return this.settingsBar;
    }

    public void updateWindowTitle(int currentInstructionsPerFrame) {
        SwingUtilities.invokeLater(() -> {
            ifGameRendererSet(renderer -> {
                this.totalIpfSinceLastUpdate += currentInstructionsPerFrame;
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

                stringBuilder.append(DEFAULT_TITLE)
                        .append(" | ").append(renderer.getChip8Variant().getDisplayName())
                        .append(renderer.getRomTitle())
                        .append(" | IPF: ").append(averageIpf)
                        .append(" | MIPS: ").append(String.format("%.2f", mips))
                        .append(" | Frame Time: ").append(String.format("%.2f ms", averageFrameTimeMs))
                        .append(" | FPS: ").append(String.format("%.2f", fps));

                String title = stringBuilder.toString();
                stringBuilder.setLength(0);

                this.setTitle(title);
            });
        });
    }

    private void ifGameRendererSet(Consumer<EmulatorRenderer> consumer) {
        if (this.emulatorRenderer != null) {
            consumer.accept(this.emulatorRenderer);
        }
    }

    @Override
    public void close() {
        SwingUtilities.invokeLater(() -> {
            this.emulatorRenderer.close();
            this.dispose();
        });
    }
}
