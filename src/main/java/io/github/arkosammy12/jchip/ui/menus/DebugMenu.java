package io.github.arkosammy12.jchip.ui.menus;

import io.github.arkosammy12.jchip.ui.MainWindow;

import javax.swing.*;
import java.awt.event.KeyEvent;

public class DebugMenu extends JMenu {

    public DebugMenu(MainWindow mainWindow) {
        super("Debug");

        this.setMnemonic(KeyEvent.VK_D);

        JRadioButtonMenuItem showDebuggerButton = new JRadioButtonMenuItem("Show debugger");
        showDebuggerButton.addActionListener(_ -> mainWindow.setDebuggerEnabled(showDebuggerButton.isSelected()));

        JRadioButtonMenuItem showDisassemblerButton = new JRadioButtonMenuItem("Show disassembler");
        showDisassemblerButton.addActionListener(_ -> mainWindow.setDisassemblerEnabled(showDisassemblerButton.isSelected()));

        this.add(showDebuggerButton);
        this.add(showDisassemblerButton);
    }

}
