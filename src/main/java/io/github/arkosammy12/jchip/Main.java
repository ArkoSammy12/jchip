package io.github.arkosammy12.jchip;

import com.formdev.flatlaf.intellijthemes.FlatOneDarkIJTheme;
import com.formdev.flatlaf.util.SystemInfo;
import org.tinylog.Logger;

import javax.swing.*;
import java.awt.*;

public class Main {

    public static final int FRAMES_PER_SECOND = 60;
    public static final long FRAME_INTERVAL = 1_000_000_000L / FRAMES_PER_SECOND;

    // Increment this here, in pom.xml and in the version tag in the README.
    public static final String VERSION_STRING = "v4.1.3";

    static void main(String[] args) throws Exception {

        FlatOneDarkIJTheme.setUseNativeWindowDecorations(true);
        FlatOneDarkIJTheme.setup();

        ToolTipManager toolTipManager = ToolTipManager.sharedInstance();
        toolTipManager.setLightWeightPopupEnabled(false);
        toolTipManager.setInitialDelay(700);
        toolTipManager.setReshowDelay(700);
        toolTipManager.setDismissDelay(4000);

        JFrame.setDefaultLookAndFeelDecorated(false);
        JDialog.setDefaultLookAndFeelDecorated(false);
        UIManager.put("TitlePane.useWindowDecorations", false);
        System.setProperty("apple.laf.useScreenMenuBar", Boolean.TRUE.toString());
        System.setProperty("apple.awt.application.appearance", "system");

        UIManager.put("Component.arc", 8);
        UIManager.put("Button.arc", 8);
        UIManager.put("Component.hideMnemonics", false);
        UIManager.put("FileChooser.readOnly", true);
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