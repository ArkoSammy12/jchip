package io.github.arkosammy12.jchip.ui;

import io.github.arkosammy12.jchip.main.Jchip;
import io.github.arkosammy12.jchip.config.DataManager;
import io.github.arkosammy12.jchip.config.initializers.EmulatorInitializer;
import io.github.arkosammy12.jchip.config.initializers.EmulatorInitializerConsumer;
import io.github.arkosammy12.jchip.main.MainWindow;
import io.github.arkosammy12.jchip.ui.disassembly.DisassemblyPanel;
import io.github.arkosammy12.jchip.ui.util.ToggleableSplitPane;
import io.github.arkosammy12.jchip.ui.renderer.EmulatorViewport;
import net.miginfocom.layout.CC;
import net.miginfocom.layout.LC;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

import static io.github.arkosammy12.jchip.config.DataManager.tryOptional;

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
            DataManager dataManager = jchip.getDataManager();
            dataManager.putPersistent("ui.viewport_disassembler_divider_location", String.valueOf(this.splitPane.getAbsoluteDividerLocation()));
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
        if (initializer instanceof DataManager dataManager) {
            dataManager.getPersistent("ui.viewport_disassembler_divider_location").flatMap(v -> tryOptional(() -> Integer.valueOf(v))).ifPresent(this.splitPane::setAbsoluteDividerLocation);
        }
    }
}
