package io.github.arkosammy12.jchip;

import com.formdev.flatlaf.FlatDarculaLaf;
import org.tinylog.Logger;

import javax.swing.*;
import java.awt.*;

public class Main {

    public static final int FRAMES_PER_SECOND = 60;
    public static final long FRAME_INTERVAL = 1_000_000_000L / FRAMES_PER_SECOND;

    // Increment this here, in pom.xml and in the version tag in the README.
    public static final String VERSION_STRING = "v4.1.1";

    static void main(String[] args) throws Exception {
        FlatDarculaLaf.setup();
        System.setProperty("flatlaf.menuBarEmbedded", Boolean.FALSE.toString());
        UIManager.put("Component.hideMnemonics", false);
        UIManager.put("FileChooser.readOnly", true);
        JFrame.setDefaultLookAndFeelDecorated(true);
        System.setProperty("sun.awt.noerasebackground", Boolean.TRUE.toString());
        if (Boolean.TRUE.equals(Toolkit.getDefaultToolkit().getDesktopProperty("awt.dynamicLayoutSupported"))) {
            Toolkit.getDefaultToolkit().setDynamicLayout(true);
        }
        Jchip jchip = null;
        try {
            jchip = new Jchip(args);
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