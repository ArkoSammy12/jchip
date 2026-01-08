package io.github.arkosammy12.jchip.ui.menus;

import io.github.arkosammy12.jchip.Jchip;
import io.github.arkosammy12.jchip.config.initializers.ApplicationInitializer;
import io.github.arkosammy12.jchip.config.Config;
import io.github.arkosammy12.jchip.config.initializers.EmulatorInitializer;
import io.github.arkosammy12.jchip.config.initializers.EmulatorInitializerConsumer;
import io.github.arkosammy12.jchip.ui.MainWindow;

import javax.swing.*;
import java.awt.event.KeyEvent;

public class DebugMenu extends JMenu implements EmulatorInitializerConsumer {

    private final JRadioButtonMenuItem showDebuggerButton;
    private final JRadioButtonMenuItem showDisassemblerButton;

    public DebugMenu(Jchip jchip, MainWindow mainWindow) {
        super("Debug");

        this.setMnemonic(KeyEvent.VK_D);

        this.showDebuggerButton = new JRadioButtonMenuItem("Show debugger");
        this.showDebuggerButton.addChangeListener(_ -> mainWindow.setDebuggerEnabled(this.showDebuggerButton.isSelected()));

        this.showDisassemblerButton = new JRadioButtonMenuItem("Show disassembler");
        this.showDisassemblerButton.addChangeListener(_ -> mainWindow.setDisassemblerEnabled(this.showDisassemblerButton.isSelected()));

        this.add(this.showDebuggerButton);
        this.add(this.showDisassemblerButton);

        jchip.addShutdownListener(() -> {
            Config config = jchip.getConfig();

            config.setBooleanSettingIfPresent(Config.SHOW_DEBUGGER, this.showDebuggerButton.isSelected());
            config.setBooleanSettingIfPresent(Config.SHOW_DISASSEMBLER, this.showDisassemblerButton.isSelected());
        });
    }

    @Override
    public void accept(EmulatorInitializer initializer) {
        if (initializer instanceof ApplicationInitializer applicationInitializer) {
            applicationInitializer.isShowingDebugger().ifPresent(this.showDebuggerButton::setSelected);
            applicationInitializer.isShowingDisassembler().ifPresent(this.showDisassemblerButton::setSelected);
        }
    }
}
