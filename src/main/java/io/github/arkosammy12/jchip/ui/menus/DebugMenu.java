package io.github.arkosammy12.jchip.ui.menus;

import io.github.arkosammy12.jchip.Jchip;

import javax.swing.*;
import java.awt.event.KeyEvent;

public class DebugMenu extends JMenu {

    private final JRadioButtonMenuItem showDebuggerButton;

    public DebugMenu(Jchip jchip) {
        super("Debug");

        this.setMnemonic(KeyEvent.VK_D);

        this.showDebuggerButton = new JRadioButtonMenuItem("Show debugger");
        this.showDebuggerButton.addActionListener(_ -> jchip.getMainWindow().setDebuggerViewEnabled(this.showDebuggerButton.isSelected()));


        this.add(showDebuggerButton);
    }

}
