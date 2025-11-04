package io.github.arkosammy12.jchip.ui;

import io.github.arkosammy12.jchip.JChip;
import io.github.arkosammy12.jchip.config.PrimarySettingsProvider;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.KeyEvent;
import java.nio.file.Path;

public class FileMenu extends JMenu {

    private static final String[] FILE_EXTENSIONS = {"ch8", "c8x", "sc8", "sc11", "scm", "xo8", "mc8"};

    private Path romPath;

    public FileMenu(JChip jchip) {
        super("File");

        this.setMnemonic(KeyEvent.VK_F);

        JMenuItem openItem = new JMenuItem("Load ROM");
        openItem.addActionListener(_ -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter("CHIP-8 ROMs", FILE_EXTENSIONS));
            Action details = chooser.getActionMap().get("viewTypeDetails");
            details.actionPerformed(null);
            if (chooser.showOpenDialog(this.getParent()) == JFileChooser.APPROVE_OPTION) {
                this.romPath = chooser.getSelectedFile().toPath().toAbsolutePath();
            }
        });

        this.add(openItem);

    }

    public Path getRomPath() {
        return romPath;
    }

    public void initializeSettings(PrimarySettingsProvider primarySettingsProvider) {
        this.romPath = primarySettingsProvider.getRomPath();
    }

}
