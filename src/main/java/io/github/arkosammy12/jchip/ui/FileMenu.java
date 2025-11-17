package io.github.arkosammy12.jchip.ui;

import com.formdev.flatlaf.icons.*;
import io.github.arkosammy12.jchip.Jchip;
import io.github.arkosammy12.jchip.config.EmulatorSettings;
import io.github.arkosammy12.jchip.config.PrimarySettingsProvider;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class FileMenu extends JMenu {

    private static final String[] FILE_EXTENSIONS = {"ch8", "c8x", "sc8", "sc11", "scm", "xo8", "mc8", "hc8", "cos", "bin"};

    private final AtomicReference<Path> romPath = new AtomicReference<>(null);
    private final AtomicReference<byte[]> rawRom = new AtomicReference<>(null);
    private Path currentDirectory;

    public FileMenu(Jchip jchip) {
        super("File");

        this.setMnemonic(KeyEvent.VK_F);

        JMenuItem openItem = new JMenuItem("Load ROM");
        openItem.addActionListener(_ -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setPreferredSize(new Dimension(700, 400));
            chooser.setFileFilter(new FileNameExtensionFilter("ROMs", FILE_EXTENSIONS));
            chooser.getActionMap().get("viewTypeDetails").actionPerformed(null);
            if (this.currentDirectory != null) {
                chooser.setCurrentDirectory(this.currentDirectory.toFile());
            }
            if (chooser.showOpenDialog(SwingUtilities.getWindowAncestor(this)) == JFileChooser.APPROVE_OPTION) {
                Path selectedFilePath =  chooser.getSelectedFile().toPath();
                this.currentDirectory = selectedFilePath.getParent();
                this.romPath.set(selectedFilePath);
                this.rawRom.set(EmulatorSettings.readRawRom(this.romPath.get()));
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
        primarySettingsProvider.getRomPath().map(Path::toAbsolutePath).ifPresent(this.romPath::set);
    }

}
