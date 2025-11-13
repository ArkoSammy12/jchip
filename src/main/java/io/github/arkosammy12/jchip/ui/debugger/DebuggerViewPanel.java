package io.github.arkosammy12.jchip.ui.debugger;

import io.github.arkosammy12.jchip.JChip;
import io.github.arkosammy12.jchip.config.EmulatorSettings;
import io.github.arkosammy12.jchip.cpu.Chip8Processor;
import io.github.arkosammy12.jchip.emulators.Chip8Emulator;
import io.github.arkosammy12.jchip.emulators.Emulator;
import io.github.arkosammy12.jchip.memory.Chip8Memory;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class DebuggerViewPanel extends JPanel {

    private final JChip jchip;

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

    private final MemoryTable memoryTable;

    private final int[] shownRegisters = new int[16];
    private final int[] shownStack = new int[16];

    public DebuggerViewPanel(JChip jchip) {
        super();
        this.jchip = jchip;

        this.doVFResetLabel = new DebuggerLabel<>("VF Reset");
        this.doIncrementIndexLabel = new DebuggerLabel<>("Increment I");
        this.doDisplayWaitLabel = new DebuggerLabel<>("Display Wait");
        this.doClippingLabel = new DebuggerLabel<>("Clipping");
        this.doShiftVXInPlaceLabel = new DebuggerLabel<>("Shift VX In Place");
        this.doJumpWithVXLabel = new DebuggerLabel<>("Jump With VX");

        this.programCounterLabel = new DebuggerLabel<>("PC");
        this.programCounterLabel.setToStringFunction(val -> Integer.toHexString(val).toUpperCase());
        this.programCounterLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 15));

        this.indexRegisterLabel = new DebuggerLabel<>("I");
        this.indexRegisterLabel.setToStringFunction(val -> Integer.toHexString(val).toUpperCase());
        this.indexRegisterLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 15));

        this.delayTimerLabel = new DebuggerLabel<>("DT");
        this.delayTimerLabel.setToStringFunction(val -> String.format("%02X", val));
        this.delayTimerLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 15));

        this.soundTimerLabel = new DebuggerLabel<>("ST");
        this.soundTimerLabel.setToStringFunction(val -> String.format("%02X", val));
        this.soundTimerLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 15));

        this.stackPointerLabel = new DebuggerLabel<>("SP");
        this.stackPointerLabel.setToStringFunction(val -> String.format("%02X", val));
        this.stackPointerLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 15));

        for (int i = 0; i < 16; i++) {
            String hexDigit = Integer.toHexString(i).toUpperCase();
            DebuggerLabel<Integer> registerLabel = new DebuggerLabel<>("V" + hexDigit);
            DebuggerLabel<Integer> stackLabel = new DebuggerLabel<>(hexDigit);

            registerLabel.setToStringFunction(val -> String.format("%02X", val));
            stackLabel.setToStringFunction(val -> String.format("%02X", val));

            registerLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 15));
            stackLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 15));

            this.registerLabels.add(registerLabel);
            this.stackLabels.add(stackLabel);
        }

        JPanel currentQuirksPanel = new JPanel(new GridLayout(0, 1, 1, 1));
        currentQuirksPanel.add(this.doVFResetLabel);
        currentQuirksPanel.add(this.doIncrementIndexLabel);
        currentQuirksPanel.add(this.doDisplayWaitLabel);
        currentQuirksPanel.add(this.doClippingLabel);
        currentQuirksPanel.add(this.doShiftVXInPlaceLabel);
        currentQuirksPanel.add(this.doJumpWithVXLabel);

        JPanel singleRegistersPanel = new JPanel(new GridLayout(0, 2, 1, 1));
        singleRegistersPanel.add(this.programCounterLabel);
        singleRegistersPanel.add(this.indexRegisterLabel);
        singleRegistersPanel.add(this.delayTimerLabel);
        singleRegistersPanel.add(this.soundTimerLabel);
        singleRegistersPanel.add(this.stackPointerLabel);

        JPanel registersPanel = new JPanel(new GridLayout(0, 2, 5, 1));
        JPanel stackPanel = new JPanel(new GridLayout(0, 2, 5, 1));

        int numRows = 8;
        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < 2; col++) {
                int index = col * numRows + row;
                registersPanel.add(this.registerLabels.get(index));
                stackPanel.add(this.stackLabels.get(index));
            }
        }

        this.memoryTable = new MemoryTable();

        JScrollPane currentQuirksScrollPane = new JScrollPane(currentQuirksPanel);
        currentQuirksScrollPane.setPreferredSize(new Dimension(currentQuirksScrollPane.getSize().width, 20));
        currentQuirksScrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2, true), "Current Quirks", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION));

        JScrollPane singleRegisterScrollPane = new JScrollPane(singleRegistersPanel);
        singleRegisterScrollPane.setPreferredSize(new Dimension(singleRegisterScrollPane.getSize().width, 10));
        singleRegisterScrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2, true), "Single Registers", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION));

        JScrollPane registersScrollPane = new JScrollPane(registersPanel);
        registersScrollPane.setPreferredSize(new Dimension(registersScrollPane.getSize().width, 130));
        registersScrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2, true), "Registers", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION));

        JScrollPane stackScrollPane = new JScrollPane(stackPanel);
        stackScrollPane.setPreferredSize(new Dimension(stackScrollPane.getSize().width, 130));
        stackScrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2, true), "Stack", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION));

        JScrollPane memoryScrollPane = new JScrollPane(memoryTable);
        memoryScrollPane.setPreferredSize(new Dimension(memoryScrollPane.getSize().width, 280));
        memoryScrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2, true), "Memory", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION));

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setPreferredSize(new Dimension(150, leftPanel.getSize().height));
        leftPanel.add(currentQuirksScrollPane);
        leftPanel.add(singleRegisterScrollPane);
        leftPanel.add(registersScrollPane);
        leftPanel.add(stackScrollPane);

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setPreferredSize(new Dimension(215 ,rightPanel.getSize().height));
        rightPanel.add(memoryScrollPane);

        this.setPreferredSize(new Dimension(500, this.getSize().height));
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2, true), "Live Debugger", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION));

        this.add(leftPanel);
        this.add(rightPanel);
    }

    public void clear() {
        SwingUtilities.invokeLater(() -> {
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
            this.stackPointerLabel.setState(null);

            for (int i = 0; i < 16; i++) {
                this.registerLabels.get(i).setState(null);
                this.stackLabels.get(i).setState(null);
            }

            this.memoryTable.clear();

        });
    }

    public void update(Emulator emulator) {
        EmulatorSettings config = emulator.getEmulatorSettings();

        /*
        Chip8Processor<?> processor = emulator.getProcessor();
        int pc = processor.getProgramCounter();
        int I = processor.getIndexRegister();
        int dt = processor.getDelayTimer();
        int st = processor.getSoundTimer();
        int sp = processor.getStackPointer();
        processor.getRegisterView(this.shownRegisters);
        processor.getStackView(this.shownStack);
        Chip8Memory memory = emulator.getMemory();
        this.memoryTable.update(emulator);

        SwingUtilities.invokeLater(() -> {

            this.doVFResetLabel.setState(config.doVFReset());
            this.doIncrementIndexLabel.setState(config.doIncrementIndex());
            this.doDisplayWaitLabel.setState(config.doDisplayWait());
            this.doClippingLabel.setState(config.doClipping());
            this.doShiftVXInPlaceLabel.setState(config.doShiftVXInPlace());
            this.doJumpWithVXLabel.setState(config.doJumpWithVX());

            String formatSpecifier = "%0" + hexDigitCount(memory.getMemoryBoundsMask()) + "X";

            this.programCounterLabel.setToStringFunction(val -> String.format(formatSpecifier, val));
            this.programCounterLabel.setState(pc);

            this.indexRegisterLabel.setToStringFunction(val -> String.format(formatSpecifier, val));
            this.indexRegisterLabel.setState(I);

            this.delayTimerLabel.setState(dt);
            this.soundTimerLabel.setState(st);
            this.stackPointerLabel.setState(sp);

            for (int i = 0; i < 16; i++) {
                this.registerLabels.get(i).setState(this.shownRegisters[i]);

                this.stackLabels.get(i).setToStringFunction(val -> String.format(formatSpecifier, val));
                this.stackLabels.get(i).setState(this.shownStack[i]);
            }

            switch (this.jchip.getMainWindow().getSettingsBar().getDebuggerSettingsMenu().getCurrentMemoryFollowMode()) {
                case FOLLOW_PC -> this.memoryTable.scrollToAddress(pc);
                case FOLLOW_I -> this.memoryTable.scrollToAddress(I);
            }

        });

         */

    }

    private static int hexDigitCount(int x) {
        if (x == 0) {
            return 1;
        }
        return (32 - Integer.numberOfLeadingZeros(x) + 3) >>> 2;
    }

}
