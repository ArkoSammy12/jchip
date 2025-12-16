package io.github.arkosammy12.jchip.ui.menus;

import com.formdev.flatlaf.util.SystemInfo;
import io.github.arkosammy12.jchip.Jchip;
import io.github.arkosammy12.jchip.Main;

import javax.swing.*;
import java.awt.*;
import java.awt.desktop.QuitStrategy;
import java.awt.event.KeyEvent;
import java.net.URI;

public class HelpMenu extends JMenu {

    public HelpMenu(Jchip jchip) {
        super("Help");

        this.setMnemonic(KeyEvent.VK_H);

        Runnable showAboutDialog = () -> JOptionPane.showMessageDialog(
                jchip.getMainWindow(),
                String.format("Jchip\nVersion %s\n\nBy ArkoSammy12", Main.VERSION_STRING),
                "About Jchip",
                JOptionPane.INFORMATION_MESSAGE);

        Runnable addAboutItem = () -> {
            JMenuItem aboutItem = new JMenuItem("About");
            aboutItem.setMnemonic(KeyEvent.VK_A);
            aboutItem.addActionListener(_ -> showAboutDialog.run());
            aboutItem.setToolTipText("Show the current jchip version.");
            this.add(aboutItem);
        };

        if (SystemInfo.isMacOS) {
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.APP_ABOUT)) {
                desktop.setAboutHandler(_ -> showAboutDialog.run());
            } else {
                addAboutItem.run();
            }
        } else {
            addAboutItem.run();
        }

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

        this.add(sourceItem);
        this.add(reportItem);

    }

}
