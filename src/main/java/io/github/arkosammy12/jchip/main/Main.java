package io.github.arkosammy12.jchip.main;

import com.formdev.flatlaf.util.SystemInfo;
import org.tinylog.Logger;

import java.awt.*;
import java.awt.desktop.QuitStrategy;

public class Main {

    public static final int MAIN_FRAMERATE = 60;

    // Increment this here, in pom.xml and in the version tag in the README.
    public static final String VERSION_STRING = "v5.1.2";

    static void main(String[] args) throws Exception {

        if (SystemInfo.isMacOS) {
            System.setProperty("apple.laf.useScreenMenuBar", Boolean.TRUE.toString());
            System.setProperty("apple.awt.application.appearance", "system");
            System.setProperty("apple.awt.application.name", "jchip");

            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.APP_QUIT_HANDLER)) {
                desktop.setQuitStrategy(QuitStrategy.CLOSE_ALL_WINDOWS);
                desktop.setQuitHandler((_, response) -> response.performQuit());
            }
        }

        System.setProperty("sun.awt.noerasebackground", Boolean.TRUE.toString());
        System.setProperty("flatlaf.uiScale.allowScaleDown", Boolean.TRUE.toString());
        System.setProperty("flatlaf.menuBarEmbedded", Boolean.FALSE.toString());

        Jchip jchip = null;
        try {
            jchip = new Jchip(args);
            jchip.start();
        } catch (Throwable t) {
            Logger.error("jchip has crashed! {}", t);
        } finally {
            if (jchip != null) {
                jchip.onShutdown();
            }
        }

    }
}