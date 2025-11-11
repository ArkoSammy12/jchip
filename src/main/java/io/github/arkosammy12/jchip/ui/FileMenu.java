package io.github.arkosammy12.jchip.ui;

import com.formdev.flatlaf.icons.*;
import io.github.arkosammy12.jchip.JChip;
import io.github.arkosammy12.jchip.config.EmulatorSettings;
import io.github.arkosammy12.jchip.config.PrimarySettingsProvider;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class FileMenu extends JMenu {

    private static final String[] FILE_EXTENSIONS = {"ch8", "c8x", "sc8", "sc11", "scm", "xo8", "mc8"};

    private final AtomicReference<Path> romPath = new AtomicReference<>(null);
    private final AtomicReference<byte[]> rawRom = new AtomicReference<>(null);

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
                this.romPath.set(chooser.getSelectedFile().toPath().toAbsolutePath());
                this.rawRom.set(EmulatorSettings.getRawRom(this.romPath.get()));
            }
        });
        openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK, true));
        openItem.setIcon(new FlatFileViewFileIcon());
        openItem.setToolTipText("Load binary ROM data from a file.");

        this.add(openItem);
    }

    public Optional<Path> getRomPath() {
        return Optional.ofNullable(this.romPath.get());
    }

    public Optional<byte[]> getRawRom() {
        byte[] val = this.rawRom.get();
        if (val == null) {
            return Optional.empty();
        }
        return Optional.of(Arrays.copyOf(val, val.length));
    }

    public void initializeSettings(PrimarySettingsProvider primarySettingsProvider) {
        primarySettingsProvider.getRawRom().ifPresent(rawRom -> this.rawRom.set(Arrays.copyOf(rawRom, rawRom.length)));
    }

}
