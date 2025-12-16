package io.github.arkosammy12.jchip.ui;

import io.github.arkosammy12.jchip.Jchip;
import io.github.arkosammy12.jchip.Main;
import io.github.arkosammy12.jchip.emulators.Emulator;
import io.github.arkosammy12.jchip.ui.debugger.DebuggerPanel;
import io.github.arkosammy12.jchip.ui.util.ToggleableSplitPane;
import io.github.arkosammy12.jchip.video.DisplayRenderer;
import net.miginfocom.layout.AC;
import net.miginfocom.layout.CC;
import net.miginfocom.layout.LC;
import net.miginfocom.swing.MigLayout;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.Closeable;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainWindow extends JFrame implements Closeable {

    public static final String DEFAULT_TITLE = "jchip " + Main.VERSION_STRING;

    private final ToggleableSplitPane mainSplitPane;
    private final EmulatorViewport emulatorViewport;
    private final SettingsBar settingsBar;
    private final DebuggerPanel debuggerPanel;
    private final InfoPanel infoPanel;

    private final AtomicBoolean showingDebuggerPanel = new AtomicBoolean(false);

    public MainWindow(Jchip jchip) {
        super(DEFAULT_TITLE);
        this.setBackground(Color.BLACK);
        this.getRootPane().putClientProperty("apple.awt.fullscreenable", true);

        MigLayout migLayout = new MigLayout(new LC().insets("0"), new AC(), new AC().gap("0"));
        this.setLayout(migLayout);
        this.setBackground(Color.BLACK);

        this.emulatorViewport = new EmulatorViewport();
        this.settingsBar = new SettingsBar(jchip, this);
        this.infoPanel = new InfoPanel();
        this.debuggerPanel = new DebuggerPanel(jchip);
        this.mainSplitPane = new ToggleableSplitPane(JSplitPane.HORIZONTAL_SPLIT, this.emulatorViewport, this.debuggerPanel, 5, 0.5);

        this.setJMenuBar(this.settingsBar);
        this.add(this.mainSplitPane, new CC().grow().push().wrap());
        this.add(this.infoPanel, new CC().grow().pushX().dockSouth().height("28!"));

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        this.requestFocusInWindow();
        this.setResizable(true);
        this.setPreferredSize(new Dimension((int) (screenSize.getWidth() / 1.5), (int) (screenSize.getHeight() / 1.5)));
        this.pack();
        this.setLocationRelativeTo(null);
    }

    public SettingsBar getSettingsBar() {
        return this.settingsBar;
    }

    public void onFrame(@Nullable Emulator emulator) {
        if (emulator == null) {
            return;
        }
        if (this.showingDebuggerPanel.get()) {
            this.debuggerPanel.onFrame(emulator);
        }
        this.infoPanel.onFrame(emulator);
        emulator.getDisplay().getEmulatorRenderer().requestFrame();
    }

    public void onStopped() {
        this.infoPanel.onStopped();
        this.debuggerPanel.onStopped();
        this.settingsBar.onStopped();
    }

    public void setEmulatorRenderer(DisplayRenderer displayRenderer) {
        this.emulatorViewport.setEmulatorRenderer(displayRenderer);
    }

    public void setDebuggerViewEnabled(boolean enabled) {
        this.showingDebuggerPanel.set(enabled);
        SwingUtilities.invokeLater(() -> {
            if (mainSplitPane.isSplitVisible()) {
                this.mainSplitPane.hideRightPanel();
            } else {
                this.mainSplitPane.showSplit();
            }
            this.revalidate();
            this.repaint();
        });
    }

    public void setInfoPanelEnabled(boolean enabled) {
        SwingUtilities.invokeLater(() -> {
            this.infoPanel.setVisible(enabled);
            this.revalidate();
            this.repaint();
        });
    }

    public void showExceptionDialog(Exception e) {
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this,
                e.getClass().getSimpleName() + ": " + e.getMessage(),
                "Emulation has stopped unexpectedly!",
                JOptionPane.ERROR_MESSAGE
        ));
    }

    @Override
    public void close() {
        SwingUtilities.invokeLater(this::dispose);
    }

}
