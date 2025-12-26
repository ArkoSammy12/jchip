package io.github.arkosammy12.jchip.ui.disassembly;

import io.github.arkosammy12.jchip.emulators.Emulator;

import javax.swing.*;

public class DisassemblyPanel extends JPanel {

    public DisassemblyPanel() {

        this.add(new JLabel("Disassembly work in progress"));


    }

    public void onFrame(Emulator emulator) {

    }

    public void onStopped() {

    }

}
