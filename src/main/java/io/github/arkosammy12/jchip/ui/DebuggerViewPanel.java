package io.github.arkosammy12.jchip.ui;

import io.github.arkosammy12.jchip.JChip;
import io.github.arkosammy12.jchip.config.EmulatorConfig;
import io.github.arkosammy12.jchip.cpu.Chip8Processor;
import io.github.arkosammy12.jchip.emulators.Chip8Emulator;
import io.github.arkosammy12.jchip.util.Chip8Variant;

import javax.swing.*;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DebuggerViewPanel extends JPanel {

    private final JChip jchip;

    private final DebuggerLabel<Chip8Variant> currentVariant;
    private final DebuggerLabel<Boolean> doVFResetLabel;
    private final DebuggerLabel<Boolean> doIncrementIndexLabel;
    private final DebuggerLabel<Boolean> doDisplayWaitLabel;
    private final DebuggerLabel<Boolean> doClippingLabel;
    private final DebuggerLabel<Boolean> doShiftVXInPlaceLabel;
    private final DebuggerLabel<Boolean> doJumpWithVXLabel;

    private final DebuggerLabel<Integer> programCounterLabel;
    private final DebuggerLabel<Integer> indexRegisterLabel;
    private final DebuggerLabel<Integer> delayTimerLabel;
    private final DebuggerLabel<Integer> soundTimerLabel;
    private final DebuggerLabel<Integer> stackPointerLabel;

    private final List<DebuggerLabel<Integer>> registerLabels = new ArrayList<>();
    private final List<DebuggerLabel<Integer>> stackLabels = new ArrayList<>();

    private final MemoryTableModel memoryTableModel;

    private final int[] shownRegisters = new int[16];
    private final int[] shownStack = new int[16];

    private final int[] memoryView = new int[TOTAL_SHOWN_BYTES];

    private static final int TOTAL_SHOWN_BYTES = 0xFFFF + 1;
    private static final int BYTES_PER_ROW = 8;
    private static final int BYTES_ROW_NUM = (int) Math.ceil((double) TOTAL_SHOWN_BYTES / BYTES_PER_ROW);

    public DebuggerViewPanel(JChip jchip) {
        super();
        this.jchip = jchip;

        this.currentVariant = new DebuggerLabel<>("Variant");
        this.currentVariant.setToStringFunction(Chip8Variant::getDisplayName);

        this.doVFResetLabel = new DebuggerLabel<>("VF Reset");
        this.doIncrementIndexLabel = new DebuggerLabel<>("Increment I");
        this.doDisplayWaitLabel = new DebuggerLabel<>("Display Wait");
        this.doClippingLabel = new DebuggerLabel<>("Clipping");
        this.doShiftVXInPlaceLabel = new DebuggerLabel<>("Shift VX In Place");
        this.doJumpWithVXLabel = new DebuggerLabel<>("Jump With VX");

        this.programCounterLabel = new DebuggerLabel<>("PC");
        this.programCounterLabel.setToStringFunction(val -> Integer.toHexString(val).toUpperCase());
        this.programCounterLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        this.indexRegisterLabel = new DebuggerLabel<>("I");
        this.indexRegisterLabel.setToStringFunction(val -> Integer.toHexString(val).toUpperCase());
        this.indexRegisterLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        this.delayTimerLabel = new DebuggerLabel<>("DT");
        this.delayTimerLabel.setToStringFunction(val -> Integer.toHexString(val).toUpperCase());
        this.delayTimerLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        this.soundTimerLabel = new DebuggerLabel<>("ST");
        this.soundTimerLabel.setToStringFunction(val -> Integer.toHexString(val).toUpperCase());
        this.soundTimerLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        this.stackPointerLabel = new DebuggerLabel<>("SP");
        this.stackPointerLabel.setToStringFunction(val -> Integer.toHexString(val).toUpperCase());
        this.stackPointerLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        for (int i = 0; i < 16; i++) {
            String hexDigit = Integer.toHexString(i).toUpperCase();
            DebuggerLabel<Integer> registerLabel = new DebuggerLabel<>("V" + hexDigit);
            DebuggerLabel<Integer> stackLabel = new DebuggerLabel<>(hexDigit);

            registerLabel.setToStringFunction(val -> Integer.toHexString(val).toUpperCase());
            stackLabel.setToStringFunction(val -> Integer.toHexString(val).toUpperCase());

            registerLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
            stackLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));

            this.registerLabels.add(registerLabel);
            this.stackLabels.add(stackLabel);
        }

        JPanel currentSettingsPanel = new JPanel(new GridLayout(0, 1, 0, 1));
        currentSettingsPanel.add(this.currentVariant);
        currentSettingsPanel.add(this.doVFResetLabel);
        currentSettingsPanel.add(this.doIncrementIndexLabel);
        currentSettingsPanel.add(this.doDisplayWaitLabel);
        currentSettingsPanel.add(this.doClippingLabel);
        currentSettingsPanel.add(this.doShiftVXInPlaceLabel);
        currentSettingsPanel.add(this.doJumpWithVXLabel);

        JPanel singleRegistersPanel = new JPanel(new GridLayout(0, 2, 0, 1));
        singleRegistersPanel.add(this.programCounterLabel);
        singleRegistersPanel.add(this.indexRegisterLabel);
        singleRegistersPanel.add(this.delayTimerLabel);
        singleRegistersPanel.add(this.soundTimerLabel);
        singleRegistersPanel.add(this.stackPointerLabel);
        //singleRegistersPanel.setPreferredSize(new Dimension(singleRegistersPanel.getPreferredSize().width, 60));

        JPanel registersPanel = new JPanel(new GridLayout(0, 4, 0, 1));

        JPanel stackPanel = new JPanel(new GridLayout(0, 2, 0, 1));

        for (int i = 0; i < 16; i++) {
            registersPanel.add(this.registerLabels.get(i));
        }

        int numRows = 8;
        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < 2; col++) {
                int index = col * numRows + row;
                stackPanel.add(this.stackLabels.get(index));
            }
        }

        this.memoryTableModel = new MemoryTableModel();
        JTable memoryTable = new JTable(memoryTableModel);
        memoryTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        memoryTable.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
        memoryTable.setRowHeight(16);
        memoryTable.getTableHeader().setReorderingAllowed(false);
        memoryTable.getTableHeader().setResizingAllowed(false);
        memoryTable.setTableHeader(null);

        TableColumn addressColumn = memoryTable.getColumnModel().getColumn(0);
        addressColumn.setPreferredWidth(35);
        addressColumn.setMinWidth(35);
        addressColumn.setMaxWidth(35);

        for (int i = 1; i < memoryTable.getColumnModel().getColumnCount(); i++) {
            TableColumn column = memoryTable.getColumnModel().getColumn(i);
            column.setPreferredWidth(19);
            column.setMinWidth(19);
            column.setMaxWidth(19);
        }


        this.setBackground(Color.WHITE);
        //this.setLayout(new GridLayout(0, 1, 0, 0));

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK, 2, true), "Live Debugger", 0, 0));

        JScrollPane settingsScrollPane = new JScrollPane(currentSettingsPanel);
        settingsScrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2, true), "Current Settings", 0, 0));

        JScrollPane singleRegisterScrollPane = new JScrollPane(singleRegistersPanel);
        singleRegisterScrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2, true), "Single Registers", 0, 0));

        JScrollPane registersScrollPane = new JScrollPane(registersPanel);
        registersScrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2, true), "Registers", 0, 0));

        JScrollPane stackScrollPane = new JScrollPane(stackPanel);
        stackScrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2, true), "Stack", 0, 0));

        JScrollPane memoryScrollPane = new JScrollPane(memoryTable);
        memoryScrollPane.setPreferredSize(new Dimension(memoryScrollPane.getSize().width, 300));
        memoryScrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2, true), "Memory", 0, 0));

        this.add(settingsScrollPane);
        this.add(singleRegisterScrollPane);
        this.add(registersScrollPane);
        this.add(stackScrollPane);
        //this.add(memoryScrollPane);

    }

    public void clear() {
        this.currentVariant.setState(null);
        this.doVFResetLabel.setState(null);
        this.doIncrementIndexLabel.setState(null);
        this.doDisplayWaitLabel.setState(null);
        this.doClippingLabel.setState(null);
        this.doShiftVXInPlaceLabel.setState(null);
        this.doJumpWithVXLabel.setState(null);

        this.programCounterLabel.setState(null);
        this.indexRegisterLabel.setState(null);
        this.delayTimerLabel.setState(null);
        this.soundTimerLabel.setState(null);
        this.stackPointerLabel.setFont(null);

        for (int i = 0; i < 16; i++) {
            this.registerLabels.get(i).setState(null);
            this.stackLabels.get(i).setState(null);
        }

        this.memoryTableModel.clear();

    }

    public void tick() {

        SwingUtilities.invokeLater(() -> {
            Optional<Chip8Emulator<?, ?>> optionalChip8Emulator = this.jchip.getCurrentEmulator();
            if (optionalChip8Emulator.isEmpty()) {
                this.clear();
                return;
            }

            Chip8Emulator<?, ?> emulator = optionalChip8Emulator.get();
            EmulatorConfig config = emulator.getEmulatorConfig();

            Chip8Processor<?, ?, ?> processor = emulator.getProcessor();
            int pc = processor.getProgramCounter();
            int I = processor.getIndexRegister();
            int dt = processor.getDelayTimer();
            int st = processor.getSoundTimer();
            int sp = processor.getStackPointer();
            processor.getRegisters(this.shownRegisters);
            processor.getStack(this.shownStack);
            this.memoryTableModel.updateState(emulator);
            this.currentVariant.setState(config.getVariant());
            this.doVFResetLabel.setState(config.doVFReset());
            this.doIncrementIndexLabel.setState(config.doIncrementIndex());
            this.doDisplayWaitLabel.setState(config.doDisplayWait());
            this.doClippingLabel.setState(config.doClipping());
            this.doShiftVXInPlaceLabel.setState(config.doShiftVXInPlace());
            this.doJumpWithVXLabel.setState(config.doJumpWithVX());

            this.programCounterLabel.setState(pc);
            this.indexRegisterLabel.setState(I);
            this.delayTimerLabel.setState(dt);
            this.soundTimerLabel.setState(st);
            this.stackPointerLabel.setState(sp);

            for (int i = 0; i < 16; i++) {
                this.registerLabels.get(i).setState(this.shownRegisters[i]);
                this.stackLabels.get(i).setState(this.shownStack[i]);
            }

        });

    }

}
