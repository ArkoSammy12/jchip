package io.github.arkosammy12.jchip;

import com.formdev.flatlaf.FlatLightLaf;
import org.tinylog.Logger;

import javax.swing.*;
import java.awt.*;

public class Main {

    public static final int FRAMES_PER_SECOND = 60;
    public static final long FRAME_INTERVAL = 1_000_000_000L / FRAMES_PER_SECOND;
    public static final String VERSION_STRING = "v3.0.0";

    static void main(String[] args) {
        System.setProperty("flatlaf.menuBarEmbedded", "false");
        FlatLightLaf.setup();
        JFrame.setDefaultLookAndFeelDecorated(true);
        System.setProperty("sun.awt.noerasebackground", "true");
        if (Boolean.TRUE.equals(Toolkit.getDefaultToolkit().getDesktopProperty("awt.dynamicLayoutSupported"))) {
            Toolkit.getDefaultToolkit().setDynamicLayout(true);
        }
        JChip jchip = null;
        try {
            jchip = new JChip(args);
            jchip.start();
        } catch (Throwable t) {
            Logger.error("jchip has crashed!");
            throw new RuntimeException(t);
        } finally {
            if (jchip != null) {
                jchip.onShutdown();
            }
        }
    }
}