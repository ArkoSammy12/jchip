package io.github.arkosammy12.jchip.ui.menus;

import com.formdev.flatlaf.icons.*;
import com.formdev.flatlaf.util.SystemFileChooser;
import io.github.arkosammy12.jchip.config.EmulatorSettings;
import io.github.arkosammy12.jchip.config.PrimarySettingsProvider;
import io.github.arkosammy12.jchip.ui.MainWindow;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class FileMenu extends JMenu {

    private static final String[] FILE_EXTENSIONS = {"ch8", "c8x", "sc8", "sc11", "scm", "xo8", "mc8", "hc8", "cos", "bin"};

    private final MainWindow mainWindow;
    private final AtomicReference<Path> romPath = new AtomicReference<>(null);
    private final AtomicReference<byte[]> rawRom = new AtomicReference<>(null);
    private Path currentDirectory;

    public FileMenu(MainWindow mainWindow) {
        super("File");
        this.mainWindow = mainWindow;

        this.setMnemonic(KeyEvent.VK_F);

        JMenuItem openItem = new JMenuItem("Load ROM");
        openItem.addActionListener(_ -> {
            SystemFileChooser chooser = new SystemFileChooser();
            chooser.setFileFilter(new SystemFileChooser.FileNameExtensionFilter("ROMs", FILE_EXTENSIONS));
            if (this.currentDirectory != null) {
                chooser.setCurrentDirectory(this.currentDirectory.toFile());
            }
            if (chooser.showOpenDialog(SwingUtilities.getWindowAncestor(this)) == JFileChooser.APPROVE_OPTION) {
                Path selectedFilePath =  chooser.getSelectedFile().toPath();
                this.currentDirectory = selectedFilePath.getParent();
                loadFile(selectedFilePath);
            }
        });
        openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK, true));
        openItem.setIcon(new FlatFileViewFileIcon());
        openItem.setToolTipText("Load binary ROM data from a file.");

        this.add(openItem);

        mainWindow.setTransferHandler(new TransferHandler() {

            @Override
            public boolean canImport(TransferSupport support) {
                return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
            }

            @Override
            @SuppressWarnings("unchecked")
            public boolean importData(TransferSupport support) {
                if (!canImport(support)) {
                    return false;
                }
                try {
                    List<File> files = (List<File>) support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    loadFile(files.getFirst().toPath());
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }
        });

        this.mainWindow.setTitle(MainWindow.DEFAULT_TITLE + " | No file selected");

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

    private void loadFile(Path filePath) {
        romPath.set(filePath);
        rawRom.set(EmulatorSettings.readRawRom(romPath.get()));
        this.mainWindow.setTitle(MainWindow.DEFAULT_TITLE + " | Selected File: " + filePath.getFileName());
    }

}
