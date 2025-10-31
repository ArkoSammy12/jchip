package io.github.arkosammy12.jchip.ui;

import javax.swing.*;
import java.util.Optional;

public class QuirkSubMenu extends JMenu {

    private final JRadioButtonMenuItem unspecifiedItem;
    private final JRadioButtonMenuItem enabledItem;
    private final JRadioButtonMenuItem disabledItem;

    private Boolean state;

    public QuirkSubMenu(String name) {
        super(name);

        this.unspecifiedItem = new JRadioButtonMenuItem("Unspecified");
        this.enabledItem = new JRadioButtonMenuItem("Enabled");
        this.disabledItem = new JRadioButtonMenuItem("Disabled");

        unspecifiedItem.setSelected(true);

        unspecifiedItem.addActionListener(_ -> this.setState(null));
        enabledItem.addActionListener(_ -> this.setState(true));
        disabledItem.addActionListener(_ -> this.setState(false));

        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(this.unspecifiedItem);
        buttonGroup.add(this.enabledItem);
        buttonGroup.add(this.disabledItem);

        this.add(this.unspecifiedItem);
        this.add(this.enabledItem);
        this.add(this.disabledItem);
    }

    public void setState(Boolean val) {
        this.state = val;
        this.unspecifiedItem.setSelected(false);
        this.enabledItem.setSelected(false);
        this.disabledItem.setSelected(false);
        if (val == null) {
            this.unspecifiedItem.setSelected(true);
        } else if (val) {
            this.enabledItem.setSelected(true);
        } else {
            this.disabledItem.setSelected(true);
        }
    }

     public Optional<Boolean> getState() {
        return Optional.ofNullable(this.state);
     }

}
