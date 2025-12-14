package io.github.arkosammy12.jchip.ui.debugger;

import io.github.arkosammy12.jchip.Jchip;
import io.github.arkosammy12.jchip.emulators.Emulator;
import io.github.arkosammy12.jchip.ui.util.DebuggerLabelTable;
import io.github.arkosammy12.jchip.ui.util.StartingDividerLocationSplitPane;
import net.miginfocom.layout.CC;
import net.miginfocom.layout.LC;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DebuggerPanel extends JPanel {

    public static final String DEFAULT_TEXT_SECTION_NAME = "Current Quirks";
    public static final String DEFAULT_CPU_REGISTERS_SECTION_NAME = "CPU Registers";
    public static final String DEFAULT_GENERAL_PURPOSE_REGISTERS_SECTION_NAME = "General Purpose Registers";
    public static final String DEFAULT_STACK_SECTION_NAME = "Stack";

    private final Jchip jchip;
    private Debugger debugger;

    private final List<DebuggerLabel<?>> textPanelLabels = new ArrayList<>();
    private final List<DebuggerLabel<?>> cpuRegisterLabels = new ArrayList<>();
    private final List<DebuggerLabel<?>> generalPurposeRegisterLabels = new ArrayList<>();
    private final List<DebuggerLabel<?>> stackLabels = new ArrayList<>();

    private final JTextArea textArea;

    private final JScrollPane textScrollPane;
    private final JScrollPane cpuRegistersScrollPane;
    private final JScrollPane generalPurposeRegistersScrollPane;
    private final JScrollPane stackScrollPane;

    private final DebuggerLabelTable cpuRegistersTable;
    private final DebuggerLabelTable generalPurposeRegistersTable;
    private final DebuggerLabelTable stackTable;
    private final MemoryTable memoryTable;

    public DebuggerPanel(Jchip jchip) {
        super();
        this.jchip = jchip;

        this.textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFocusable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setKeymap(null);
        textArea.setOpaque(false);
        DefaultCaret caret = (DefaultCaret)textArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        caret.setVisible(false);

        MigLayout migLayout = new MigLayout(new LC().insets("0"));

        this.setLayout(migLayout);
        this.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2, true),
                "Debugger",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                this.getFont().deriveFont(Font.BOLD)));

        this.cpuRegistersTable = new DebuggerLabelTable(this.cpuRegisterLabels, 2);
        this.generalPurposeRegistersTable = new DebuggerLabelTable(this.generalPurposeRegisterLabels, 2, true);
        this.stackTable = new DebuggerLabelTable(this.stackLabels, 2, true);
        this.memoryTable = new MemoryTable();

        this.textScrollPane = new JScrollPane(textArea);
        textScrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2, true),
                DEFAULT_TEXT_SECTION_NAME,
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                this.textScrollPane.getFont().deriveFont(Font.BOLD)));

        this.cpuRegistersScrollPane = new JScrollPane(this.cpuRegistersTable);
        cpuRegistersScrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2, true),
                DEFAULT_CPU_REGISTERS_SECTION_NAME,
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                this.cpuRegistersScrollPane.getFont().deriveFont(Font.BOLD)));

        this.generalPurposeRegistersScrollPane = new JScrollPane(this.generalPurposeRegistersTable);
        generalPurposeRegistersScrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2, true),
                DEFAULT_GENERAL_PURPOSE_REGISTERS_SECTION_NAME,
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                this.generalPurposeRegistersScrollPane.getFont().deriveFont(Font.BOLD)));

        this.stackScrollPane = new JScrollPane(this.stackTable);
        stackScrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2, true),
                DEFAULT_STACK_SECTION_NAME,
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                this.stackScrollPane.getFont().deriveFont(Font.BOLD)));

        JScrollPane memoryScrollPane = new JScrollPane(memoryTable);
        memoryScrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2, true),
                "Memory",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION, memoryScrollPane.getFont().deriveFont(Font.BOLD)));

        JSplitPane firstSplit = new StartingDividerLocationSplitPane(JSplitPane.VERTICAL_SPLIT, textScrollPane, cpuRegistersScrollPane, 0.7);
        firstSplit.setDividerSize(3);
        firstSplit.setResizeWeight(0.5);
        firstSplit.setContinuousLayout(true);

        JSplitPane secondSplit = new StartingDividerLocationSplitPane(JSplitPane.VERTICAL_SPLIT, firstSplit, generalPurposeRegistersScrollPane, 0.8);
        secondSplit.setDividerSize(3);
        secondSplit.setResizeWeight(0.5);
        secondSplit.setContinuousLayout(true);

        JSplitPane thirdSplit = new StartingDividerLocationSplitPane(JSplitPane.VERTICAL_SPLIT, secondSplit, stackScrollPane, 1);
        thirdSplit.setDividerSize(3);
        thirdSplit.setResizeWeight(0.5);
        thirdSplit.setContinuousLayout(true);

        MigLayout leftPanelLayout = new MigLayout("insets 0");
        JPanel leftPanel = new JPanel(leftPanelLayout);
        leftPanel.add(thirdSplit, new CC().grow().push().width("150"));

        MigLayout rightPanelLayout = new MigLayout("insets 0");
        JPanel rightPanel = new JPanel(rightPanelLayout);
        rightPanel.add(memoryScrollPane, new CC().grow().push().width("215"));

        JSplitPane mainSplit = new StartingDividerLocationSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel, 0.235);
        mainSplit.setDividerSize(5);
        mainSplit.setResizeWeight(0.5);
        mainSplit.setContinuousLayout(true);
        mainSplit.setOneTouchExpandable(true);

        this.add(mainSplit, new CC().grow().push().width("500"));

    }

    public void onStopped() {
        SwingUtilities.invokeLater(() -> {
            this.clearState();
            this.textArea.setText("");

            this.textScrollPane.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2, true),
                    DEFAULT_TEXT_SECTION_NAME,
                    TitledBorder.DEFAULT_JUSTIFICATION,
                    TitledBorder.DEFAULT_POSITION,
                    this.textScrollPane.getFont().deriveFont(Font.BOLD)));

            this.cpuRegistersScrollPane.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2, true),
                    DEFAULT_CPU_REGISTERS_SECTION_NAME,
                    TitledBorder.DEFAULT_JUSTIFICATION,
                    TitledBorder.DEFAULT_POSITION,
                    this.cpuRegistersScrollPane.getFont().deriveFont(Font.BOLD)));

            this.generalPurposeRegistersScrollPane.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2, true),
                    DEFAULT_GENERAL_PURPOSE_REGISTERS_SECTION_NAME,
                    TitledBorder.DEFAULT_JUSTIFICATION,
                    TitledBorder.DEFAULT_POSITION,
                    this.generalPurposeRegistersScrollPane.getFont().deriveFont(Font.BOLD)));

            this.stackScrollPane.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2, true),
                    DEFAULT_STACK_SECTION_NAME,
                    TitledBorder.DEFAULT_JUSTIFICATION,
                    TitledBorder.DEFAULT_POSITION,
                    this.stackScrollPane.getFont().deriveFont(Font.BOLD)));

            this.revalidate();
            this.repaint();
        });
    }

    public void onFrame(Emulator emulator) {
        Debugger debugger = emulator.getDebugger();
        SwingUtilities.invokeLater(() -> {
            if (!Objects.equals(debugger, this.debugger)) {
                this.initializeDebuggerPanel(debugger);
            }

            textArea.setText("");
            for (int i = 0; i < this.textPanelLabels.size(); i++) {
                DebuggerLabel<?> label = this.textPanelLabels.get(i);
                label.updateState();
                textArea.append(label.getText() + (i == this.textPanelLabels.size() - 1 ? "" : "\n"));
            }

            this.cpuRegisterLabels.forEach(DebuggerLabel::updateState);
            this.generalPurposeRegisterLabels.forEach(DebuggerLabel::updateState);
            this.stackLabels.forEach(DebuggerLabel::updateState);

            this.cpuRegistersTable.update();
            this.generalPurposeRegistersTable.update();
            this.stackTable.update();

            this.memoryTable.update(emulator);
            this.debugger.getScrollAddressSupplier().ifPresent(supplier -> {
                if (this.jchip.getMainWindow().getSettingsBar().getDebuggerSettingsMenu().isMemoryFollowEnabled()) {
                    this.memoryTable.scrollToAddress(supplier.get());
                }
            });

        });

    }

    private void initializeDebuggerPanel(Debugger debugger) {
        this.clearState();
        this.debugger = debugger;

        this.textPanelLabels.addAll(this.debugger.getTextSectionLabels());
        this.cpuRegisterLabels.addAll(this.debugger.getCpuRegisterLabels());
        this.generalPurposeRegisterLabels.addAll(this.debugger.getGeneralPurposeRegisterLabels());
        this.stackLabels.addAll(this.debugger.getStackLabels());

        this.textPanelLabels.forEach(label -> label.setFont(label.getFont().deriveFont(Font.BOLD).deriveFont(15f)));
        this.cpuRegisterLabels.forEach(label -> label.setFont(new Font(Font.MONOSPACED, Font.BOLD, 15)));
        this.generalPurposeRegisterLabels.forEach(label -> label.setFont(new Font(Font.MONOSPACED, Font.BOLD, 15)));
        this.stackLabels.forEach(label -> label.setFont(new Font(Font.MONOSPACED, Font.BOLD, 15)));

        this.textScrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2, true),
                this.debugger.getTextSectionName(),
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                this.textScrollPane.getFont().deriveFont(Font.BOLD)));

        this.cpuRegistersScrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2, true),
                this.debugger.getCpuRegistersSectionName(),
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                this.cpuRegistersScrollPane.getFont().deriveFont(Font.BOLD)));

        this.generalPurposeRegistersScrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2, true),
                this.debugger.getGeneralPurposeRegistersSectionName(),
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                this.generalPurposeRegistersScrollPane.getFont().deriveFont(Font.BOLD)));

        this.stackScrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2, true),
                this.debugger.getStackSectionName(),
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                this.stackScrollPane.getFont().deriveFont(Font.BOLD)));

        this.revalidate();
        this.repaint();
    }

    private void clearState() {
        this.debugger = null;

        this.textPanelLabels.clear();

        this.cpuRegisterLabels.clear();
        this.generalPurposeRegisterLabels.clear();
        this.stackLabels.clear();

        this.cpuRegistersTable.update();
        this.generalPurposeRegistersTable.update();
        this.stackTable.update();

        this.memoryTable.clear();
    }

}