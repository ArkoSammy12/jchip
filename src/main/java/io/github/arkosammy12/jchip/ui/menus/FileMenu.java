package io.github.arkosammy12.jchip.ui.menus;

import com.formdev.flatlaf.icons.*;
import com.formdev.flatlaf.util.SystemFileChooser;
import io.github.arkosammy12.jchip.config.EmulatorSettings;
import io.github.arkosammy12.jchip.config.PrimarySettingsProvider;
import io.github.arkosammy12.jchip.ui.MainWindow;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.tinylog.Logger;

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

    private final JMenu openRecentMenu;
    private final JMenuItem clearRecentButton;
    private final CircularFifoQueue<Path> recentFilePaths = new CircularFifoQueue<>(10);

    public FileMenu(MainWindow mainWindow) {
        super("File");
        this.mainWindow = mainWindow;

        this.setMnemonic(KeyEvent.VK_F);

        JMenuItem openItem = new JMenuItem("Open");
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
                addRecentFilePath(selectedFilePath);
            }
        });
        openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK, true));
        openItem.setIcon(new FlatFileViewFileIcon());
        openItem.setToolTipText("Load binary ROM data from a file.");

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
                    Path filePath = files.getFirst().toPath();
                    loadFile(filePath);
                    addRecentFilePath(filePath);
                    return true;
                } catch (Exception e) {
                    Logger.error("Failed to accept drag-and-drop file! {}", e);
                    return false;
                }
            }

        });

        this.openRecentMenu = new JMenu("Open Recent");

        this.clearRecentButton = new JMenuItem("Clear all recents");
        this.clearRecentButton.setEnabled(false);
        this.clearRecentButton.addActionListener(_ -> {
            this.recentFilePaths.clear();
            this.rebuildOpenRecentMenu();
        });

        this.mainWindow.setTitleSection(1, "No file selected");

        this.openRecentMenu.add(this.clearRecentButton);
        this.add(openItem);
        this.add(openRecentMenu);
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
        this.romPath.set(filePath);
        this.rawRom.set(EmulatorSettings.readRawRom(filePath));
        this.mainWindow.setTitleSection(1, filePath.getFileName().toString());
    }

    private void addRecentFilePath(Path filePath) {
        if (this.recentFilePaths.contains(filePath)) {
            return;
        }
        this.recentFilePaths.offer(filePath);
        this.rebuildOpenRecentMenu();
    }

    private void rebuildOpenRecentMenu() {
        this.openRecentMenu.removeAll();
        for (Path recentFilePath : this.recentFilePaths.stream().toList().reversed()) {
            JMenuItem recentFileItem = new JMenuItem(recentFilePath.getFileName().toString());
            recentFileItem.setToolTipText(recentFilePath.toString());
            recentFileItem.addActionListener(_ -> this.loadFile(recentFilePath));
            this.openRecentMenu.add(recentFileItem);
        }
        if (!this.recentFilePaths.isEmpty()) {
            this.openRecentMenu.addSeparator();
            this.clearRecentButton.setEnabled(true);
        } else {
            this.clearRecentButton.setEnabled(false);
        }
        this.openRecentMenu.add(this.clearRecentButton);
        this.openRecentMenu.revalidate();
        this.openRecentMenu.repaint();
    }

}
