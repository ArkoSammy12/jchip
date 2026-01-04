package io.github.arkosammy12.jchip.ui;

import io.github.arkosammy12.jchip.Jchip;
import io.github.arkosammy12.jchip.Main;
import io.github.arkosammy12.jchip.ui.debugger.DebuggerPanel;
import io.github.arkosammy12.jchip.ui.util.ToggleableSplitPane;
import io.github.arkosammy12.jchip.ui.util.WindowTitleManager;
import net.miginfocom.layout.AC;
import net.miginfocom.layout.CC;
import net.miginfocom.layout.LC;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.io.Closeable;

public class MainWindow extends JFrame implements Closeable {

    public static final String DEFAULT_TITLE = "jchip " + Main.VERSION_STRING;

    private final WindowTitleManager windowTitleManager;
    private final ToggleableSplitPane mainSplitPane;
    private final LeftPanel leftPanel;
    private final SettingsBar settingsBar;
    private final InfoBar infoBar;

    private final CC infoBarConstraints;

    public MainWindow(Jchip jchip) {
        super(DEFAULT_TITLE);
        this.windowTitleManager = new WindowTitleManager(this);
        this.setTitleSection(0, DEFAULT_TITLE);
        this.setBackground(Color.BLACK);
        this.getRootPane().putClientProperty("apple.awt.fullscreenable", true);

        MigLayout migLayout = new MigLayout(new LC().insets("0"), new AC(), new AC().gap("0"));
        this.setLayout(migLayout);
        this.setBackground(Color.BLACK);

        this.leftPanel = new LeftPanel(jchip, this);
        this.settingsBar = new SettingsBar(jchip, this);
        this.infoBar = new InfoBar(jchip);
        DebuggerPanel debuggerPanel = new DebuggerPanel(jchip, this);
        this.mainSplitPane = new ToggleableSplitPane(JSplitPane.HORIZONTAL_SPLIT, this.leftPanel, debuggerPanel, 5, 0.5);

        this.infoBarConstraints = new CC().grow().pushX().dockSouth().height("28!");

        this.setJMenuBar(this.settingsBar);
        this.add(this.mainSplitPane, new CC().grow().push().wrap());
        this.add(this.infoBar, this.infoBarConstraints);

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

    public void setTitleSection(int index, String text) {
        this.windowTitleManager.setSection(index, text);
    }

    public void setDebuggerEnabled(boolean enabled) {
        SwingUtilities.invokeLater(() -> {
            this.mainSplitPane.toggleShowSplit(enabled);
            this.revalidate();
            this.repaint();
        });
    }

    public void setDisassemblerEnabled(boolean enabled) {
        this.leftPanel.setDisassemblerEnabled(enabled);
    }

    public void setInfoBarEnabled(boolean enabled) {
        SwingUtilities.invokeLater(() -> {
            this.infoBar.setVisible(enabled);
            this.infoBarConstraints.setHideMode(enabled ? 0 : 3);
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

    public void onBreakpoint() {
        this.settingsBar.onBreakpoint();
    }

    @Override
    public void close() {
        SwingUtilities.invokeLater(this::dispose);
    }

    public static void fireVisibleRowsUpdated(JTable table) {
        Rectangle visibleRect = table.getVisibleRect();

        int firstRow = table.rowAtPoint(visibleRect.getLocation());
        int lastRow = table.rowAtPoint(new Point(visibleRect.x, visibleRect.y + visibleRect.height));

        if (firstRow < 0) {
            firstRow = 0;
        }
        if (lastRow < 0) {
            lastRow = table.getRowCount() - 1;
        }

        int modelFirstRow = table.convertRowIndexToModel(firstRow);
        int modelLastRow = table.convertRowIndexToModel(lastRow);

        ((AbstractTableModel) table.getModel()).fireTableRowsUpdated(modelFirstRow, modelLastRow);
    }

}
