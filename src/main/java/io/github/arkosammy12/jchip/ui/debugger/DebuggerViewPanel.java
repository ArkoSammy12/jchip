package io.github.arkosammy12.jchip.ui.debugger;

import io.github.arkosammy12.jchip.JChip;
import io.github.arkosammy12.jchip.emulators.Emulator;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DebuggerViewPanel extends JPanel {

    private final JChip jchip;

    private final JPanel textPanel;
    private final JPanel singleRegistersPanel;
    private final JPanel registersPanel;
    private final JPanel stackPanel;

    private final JScrollPane textScrollPane;
    private final JScrollPane singleRegistersScrollPane;
    private final JScrollPane registersScrollPane;
    private final JScrollPane stackScrollPane;

    private DebuggerInfo debuggerInfo;

    private final List<DebuggerLabel<?>> textPanelLabels = new ArrayList<>();
    private final List<DebuggerLabel<?>> singleRegisterLabels = new ArrayList<>();
    private final List<DebuggerLabel<?>> registerLabels = new ArrayList<>();
    private final List<DebuggerLabel<?>> stackLabels = new ArrayList<>();

    private final MemoryTable memoryTable;

    public DebuggerViewPanel(JChip jchip) {
        super();
        this.jchip = jchip;

        this.textPanel = new JPanel(new GridLayout(0, 1, 1, 1));
        this.singleRegistersPanel = new JPanel(new GridLayout(0, 2, 1, 1));
        this.registersPanel = new JPanel(new GridLayout(0, 2, 5, 1));
        this.stackPanel = new JPanel(new GridLayout(0, 2, 5, 1));

        this.memoryTable = new MemoryTable();


        this.textScrollPane = new JScrollPane(textPanel);
        textScrollPane.setPreferredSize(new Dimension(textScrollPane.getSize().width, 20));
        textScrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2, true), "Current Quirks", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION));

        this.singleRegistersScrollPane = new JScrollPane(singleRegistersPanel);
        singleRegistersScrollPane.setPreferredSize(new Dimension(singleRegistersScrollPane.getSize().width, 10));
        singleRegistersScrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2, true), "Single Registers", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION));

        this.registersScrollPane = new JScrollPane(registersPanel);
        registersScrollPane.setPreferredSize(new Dimension(registersScrollPane.getSize().width, 130));
        registersScrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2, true), "Registers", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION));

        this.stackScrollPane = new JScrollPane(stackPanel);
        stackScrollPane.setPreferredSize(new Dimension(stackScrollPane.getSize().width, 130));
        stackScrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2, true), "Stack", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION));

        JScrollPane memoryScrollPane = new JScrollPane(memoryTable);
        memoryScrollPane.setPreferredSize(new Dimension(memoryScrollPane.getSize().width, 280));
        memoryScrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2, true), "Memory", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION));

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setPreferredSize(new Dimension(150, leftPanel.getSize().height));
        leftPanel.add(textScrollPane);
        leftPanel.add(singleRegistersScrollPane);
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
            this.clearState();
            this.textScrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2, true), "", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION));
            this.singleRegistersScrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2, true), "", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION));
            this.registersScrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2, true), "", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION));
            this.stackScrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2, true), "", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION));
            this.repaint();
            this.revalidate();
        });
    }

    public void update(Emulator emulator) {
        DebuggerInfo debuggerInfo = emulator.getDebuggerInfo();
        SwingUtilities.invokeLater(() -> {
            if (!Objects.equals(debuggerInfo, this.debuggerInfo)) {
                this.initializeDebuggerPanel(debuggerInfo);
            }
            this.textPanelLabels.forEach(DebuggerLabel::updateState);
            this.singleRegisterLabels.forEach(DebuggerLabel::updateState);
            this.registerLabels.forEach(DebuggerLabel::updateState);
            this.stackLabels.forEach(DebuggerLabel::updateState);
            this.memoryTable.update(emulator);
            this.debuggerInfo.getScrollAddressSupplier().ifPresent(supplier -> {
                if (this.jchip.getMainWindow().getSettingsBar().getDebuggerSettingsMenu().isMemoryFollowEnabled()) {
                    this.memoryTable.scrollToAddress(supplier.get());
                }
            });
        });
    }

    private void initializeDebuggerPanel(DebuggerInfo debuggerInfo) {
        this.clearState();
        this.debuggerInfo = debuggerInfo;

        this.textPanelLabels.addAll(this.debuggerInfo.getTextSectionLabels());
        this.singleRegisterLabels.addAll(this.debuggerInfo.getSingleRegisterLabels());
        this.registerLabels.addAll(this.debuggerInfo.getRegisterLabels());
        this.stackLabels.addAll(this.debuggerInfo.getStackLabels());

        this.textScrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2, true), this.debuggerInfo.getTextSectionName(), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION));
        this.singleRegistersScrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2, true), this.debuggerInfo.getSingleRegisterSectionName(), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION));
        this.registersScrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2, true), this.debuggerInfo.getRegisterSectionName(), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION));
        this.stackScrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2, true), this.debuggerInfo.getStackSectionName(), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION));

        this.textPanelLabels.forEach(this.textPanel::add);

        for (DebuggerLabel<?> label : this.singleRegisterLabels) {
            label.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 15));
            this.singleRegistersPanel.add(label);
        }

        int registerLabelRows = (int) Math.ceil((double) this.registerLabels.size() / 2);
        for (int row = 0; row < registerLabelRows; row++) {
            for (int col = 0; col < 2; col++) {
                int index = col * registerLabelRows + row;
                if (index < 0 || index >= this.registerLabels.size()) {
                    continue;
                }
                DebuggerLabel<?> registerLabel = this.registerLabels.get(index);
                registerLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 15));
                registersPanel.add(this.registerLabels.get(index));
            }
        }

        int stackLabelsRows = (int) Math.ceil((double) this.registerLabels.size() / 2);
        for (int row = 0; row < stackLabelsRows; row++) {
            for (int col = 0; col < 2; col++) {
                int index = col * stackLabelsRows + row;
                if (index < 0 || index >= this.stackLabels.size()) {
                    continue;
                }
                DebuggerLabel<?> stackLabel = this.stackLabels.get(index);
                stackLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 15));
                stackPanel.add(stackLabel);
            }
        }

        this.repaint();
        this.revalidate();

    }

    private void clearState() {
        this.debuggerInfo = null;

        this.textPanelLabels.forEach(this.textPanel::remove);
        this.singleRegisterLabels.forEach(this.singleRegistersPanel::remove);
        this.registerLabels.forEach(this.registersPanel::remove);
        this.stackLabels.forEach(this.stackPanel::remove);

        this.textPanelLabels.clear();
        this.singleRegisterLabels.clear();
        this.registerLabels.clear();
        this.stackLabels.clear();

        this.memoryTable.clear();
    }

}
