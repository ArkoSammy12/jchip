package io.github.arkosammy12.jchip.ui.menus;

import io.github.arkosammy12.jchip.Jchip;

import javax.swing.*;
import java.awt.event.KeyEvent;

public class DebugMenu extends JMenu {

    private final JRadioButtonMenuItem showDebuggerButton;
    private final JRadioButtonMenuItem showDisassemblerButton;

    public DebugMenu(Jchip jchip) {
        super("Debug");

        this.setMnemonic(KeyEvent.VK_D);

        this.showDebuggerButton = new JRadioButtonMenuItem("Show debugger");
        this.showDebuggerButton.addActionListener(_ -> jchip.getMainWindow().setDebuggerViewEnabled(this.showDebuggerButton.isSelected()));

        this.showDisassemblerButton = new JRadioButtonMenuItem("Show disassembler");
        this.showDisassemblerButton.addActionListener(_ -> jchip.getMainWindow().setDisassemblyViewEnabled(this.showDisassemblerButton.isSelected()));

        this.add(this.showDebuggerButton);
        this.add(this.showDisassemblerButton);
    }

}
