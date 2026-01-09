package io.github.arkosammy12.jchip.ui;

import io.github.arkosammy12.jchip.Jchip;
import io.github.arkosammy12.jchip.config.Config;
import io.github.arkosammy12.jchip.config.initializers.ApplicationInitializer;
import io.github.arkosammy12.jchip.config.initializers.EmulatorInitializer;
import io.github.arkosammy12.jchip.config.initializers.EmulatorInitializerConsumer;
import io.github.arkosammy12.jchip.ui.disassembly.DisassemblyPanel;
import io.github.arkosammy12.jchip.ui.util.ToggleableSplitPane;
import net.miginfocom.layout.CC;
import net.miginfocom.layout.LC;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class LeftPanel extends JPanel implements EmulatorInitializerConsumer {

    private final ToggleableSplitPane splitPane;

    public LeftPanel(Jchip jchip, MainWindow mainWindow) {
        MigLayout migLayout = new MigLayout(new LC().insets("0"));
        super(migLayout);
        EmulatorViewport emulatorViewport = new EmulatorViewport(jchip);
        DisassemblyPanel disassemblyPanel = new DisassemblyPanel(jchip, mainWindow);
        this.splitPane = new ToggleableSplitPane(JSplitPane.VERTICAL_SPLIT, emulatorViewport, disassemblyPanel, 5, 0.75);

        this.add(this.splitPane, new CC().grow().push());

        jchip.addShutdownListener(() -> {
            Config config = jchip.getConfig();
            config.setIntegerSettingIfPresent(Config.VIEWPORT_DISASSEMBLER_DIVIDER_LOCATION, this.splitPane.getAbsoluteDividerLocation());
        });
    }

    public void setDisassemblerEnabled(boolean enabled) {
        SwingUtilities.invokeLater(() -> {
            this.splitPane.toggleShowSplit(enabled);
            this.revalidate();
            this.repaint();
        });
    }

    @Override
    public void accept(EmulatorInitializer initializer) {
        if (initializer instanceof ApplicationInitializer applicationInitializer) {
            applicationInitializer.getViewportDisassemblerDividerLocation().ifPresent(this.splitPane::setAbsoluteDividerLocation);
        }
    }
}
