package io.github.arkosammy12.jchip;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class Main {

    public static final int FRAMES_PER_SECOND = 60;
    public static final long FRAME_INTERVAL = 1_000_000_000L / FRAMES_PER_SECOND;
    public static final String VERSION_STRING = "v3.0.0";

    static void main(String[] args) throws IOException {
        System.setProperty("flatlaf.menuBarEmbedded", "false");
        FlatLightLaf.setup();
        JFrame.setDefaultLookAndFeelDecorated(true);
        System.setProperty("sun.awt.noerasebackground", "true");
        if (Boolean.TRUE.equals(Toolkit.getDefaultToolkit().getDesktopProperty("awt.dynamicLayoutSupported"))) {
            Toolkit.getDefaultToolkit().setDynamicLayout(true);
        }
        JChip JChip = new JChip(args);
        JChip.start();
    }
}