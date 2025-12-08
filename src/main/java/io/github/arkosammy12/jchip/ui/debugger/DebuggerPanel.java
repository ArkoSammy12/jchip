package io.github.arkosammy12.jchip.ui.debugger;

import io.github.arkosammy12.jchip.Jchip;
import io.github.arkosammy12.jchip.emulators.Emulator;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DebuggerPanel extends JPanel {

    private final Jchip jchip;

    private final JTextArea textArea;

    private final JPanel singleRegistersPanel;
    private final JPanel registersPanel;
    private final JPanel stackPanel;

    private final JScrollPane textScrollPane;
    private final JScrollPane singleRegistersScrollPane;
    private final JScrollPane registersScrollPane;
    private final JScrollPane stackScrollPane;

    private Debugger debugger;

    private final List<DebuggerLabel<?>> textPanelLabels = new ArrayList<>();
    private final List<DebuggerLabel<?>> singleRegisterLabels = new ArrayList<>();
    private final List<DebuggerLabel<?>> registerLabels = new ArrayList<>();
    private final List<DebuggerLabel<?>> stackLabels = new ArrayList<>();

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

        this.singleRegistersPanel = new JPanel(new GridLayout(0, 2, 1, 1));
        this.registersPanel = new JPanel(new GridLayout(0, 2, 5, 1));
        this.stackPanel = new JPanel(new GridLayout(0, 2, 5, 1));

        this.memoryTable = new MemoryTable();

        this.textScrollPane = new JScrollPane(textArea);
        textScrollPane.setPreferredSize(new Dimension(textScrollPane.getSize().width, 20));
        textScrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2, true),
                "",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION));

        this.singleRegistersScrollPane = new JScrollPane(singleRegistersPanel);
        singleRegistersScrollPane.setPreferredSize(new Dimension(singleRegistersScrollPane.getSize().width, 10));
        singleRegistersScrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2, true),
                "",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION));

        this.registersScrollPane = new JScrollPane(registersPanel);
        registersScrollPane.setPreferredSize(new Dimension(registersScrollPane.getSize().width, 130));
        registersScrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2, true),
                "",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION));

        this.stackScrollPane = new JScrollPane(stackPanel);
        stackScrollPane.setPreferredSize(new Dimension(stackScrollPane.getSize().width, 130));
        stackScrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2, true),
                "",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION));

        JScrollPane memoryScrollPane = new JScrollPane(memoryTable);
        memoryScrollPane.setPreferredSize(new Dimension(memoryScrollPane.getSize().width, 280));
        memoryScrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2, true),
                "Memory",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION));

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setPreferredSize(new Dimension(150, leftPanel.getSize().height));
        leftPanel.add(textScrollPane);
        leftPanel.add(singleRegistersScrollPane);
        leftPanel.add(registersScrollPane);
        leftPanel.add(stackScrollPane);

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setPreferredSize(new Dimension(215, rightPanel.getSize().height));
        rightPanel.add(memoryScrollPane);

        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        mainSplit.setResizeWeight(0.5);

        this.setPreferredSize(new Dimension(500, this.getSize().height));
        this.setLayout(new BorderLayout());
        this.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2, true),
                "Live Debugger",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION));

        this.add(mainSplit, BorderLayout.CENTER);
    }


    public void clear() {
        SwingUtilities.invokeLater(() -> {
            this.clearState();
            this.textArea.setText("");

            this.textScrollPane.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2, true),
                    ""));
            this.singleRegistersScrollPane.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2, true),
                    ""));
            this.registersScrollPane.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2, true),
                    ""));
            this.stackScrollPane.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2, true),
                    ""));

            this.repaint();
            this.revalidate();
        });
    }

    public void update(Emulator emulator) {
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

            this.singleRegisterLabels.forEach(DebuggerLabel::updateState);
            this.registerLabels.forEach(DebuggerLabel::updateState);
            this.stackLabels.forEach(DebuggerLabel::updateState);

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
        this.singleRegisterLabels.addAll(this.debugger.getSingleRegisterLabels());
        this.registerLabels.addAll(this.debugger.getRegisterLabels());
        this.stackLabels.addAll(this.debugger.getStackLabels());

        this.textScrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2, true),
                this.debugger.getTextSectionName()));

        this.singleRegistersScrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2, true),
                this.debugger.getSingleRegisterSectionName()));

        this.registersScrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2, true),
                this.debugger.getRegisterSectionName()));

        this.stackScrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2, true),
                this.debugger.getStackSectionName()));

        for (DebuggerLabel<?> label : this.singleRegisterLabels) {
            label.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 15));
            this.singleRegistersPanel.add(label);
        }

        int registerLabelRows = (int) Math.ceil((double) this.registerLabels.size() / 2);
        for (int row = 0; row < registerLabelRows; row++) {
            for (int col = 0; col < 2; col++) {
                int index = col * registerLabelRows + row;
                if (index >= this.registerLabels.size()) {
                    continue;
                }
                DebuggerLabel<?> registerLabel = this.registerLabels.get(index);
                registerLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 15));
                registersPanel.add(registerLabel);
            }
        }

        int stackLabelsRows = (int) Math.ceil((double) this.registerLabels.size() / 2);
        for (int row = 0; row < stackLabelsRows; row++) {
            for (int col = 0; col < 2; col++) {
                int index = col * stackLabelsRows + row;
                if (index >= this.stackLabels.size()) {
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
        this.debugger = null;

        this.textPanelLabels.clear();
        this.singleRegisterLabels.forEach(this.singleRegistersPanel::remove);
        this.registerLabels.forEach(this.registersPanel::remove);
        this.stackLabels.forEach(this.stackPanel::remove);

        this.singleRegisterLabels.clear();
        this.registerLabels.clear();
        this.stackLabels.clear();

        this.memoryTable.clear();
    }
}