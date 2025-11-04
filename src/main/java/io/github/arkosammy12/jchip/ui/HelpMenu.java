package io.github.arkosammy12.jchip.ui;

import io.github.arkosammy12.jchip.JChip;
import io.github.arkosammy12.jchip.Main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.net.URI;

public class HelpMenu extends JMenu {

    public HelpMenu(JChip jchip) {
        super("Help");

        this.setMnemonic(KeyEvent.VK_H);

        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.setMnemonic(KeyEvent.VK_A);
        aboutItem.addActionListener(_ -> JOptionPane.showMessageDialog(
                jchip.getMainWindow(),
                String.format("JChip Emulator\nVersion %s\n\nBy ArkoSammy12", Main.VERSION_STRING),
                "About JChip",
                JOptionPane.INFORMATION_MESSAGE)
        );
        aboutItem.setToolTipText("Show the current jchip version.");

        JMenuItem sourceItem = new JMenuItem("Source");
        sourceItem.setMnemonic(KeyEvent.VK_S);
        sourceItem.addActionListener(_ -> {
            try {
                Desktop.getDesktop().browse(new URI("https://github.com/ArkoSammy12/jchip"));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(jchip.getMainWindow(), "Unable to open source link.");
            }
        });
        sourceItem.setToolTipText("Open jchip's Github repository link.");

        JMenuItem reportItem = new JMenuItem("Report a Bug");
        reportItem.setMnemonic(KeyEvent.VK_R);
        reportItem.addActionListener(_ -> {
            try {
                Desktop.getDesktop().browse(new URI("https://github.com/ArkoSammy12/jchip/issues"));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(jchip.getMainWindow(), "Unable to open issues link.");
            }
        });
        reportItem.setToolTipText("Open jchip's issue report page link.");

        this.add(aboutItem);
        this.add(sourceItem);
        this.add(reportItem);

    }

}
