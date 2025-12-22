package io.github.arkosammy12.jchip.ui.util;

import io.github.arkosammy12.jchip.util.DisplayNameProvider;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class EnumMenu<E extends Enum<E> & DisplayNameProvider> extends JMenu {

    private final Map<E, JRadioButtonMenuItem> buttonMap = new HashMap<>();
    private final JRadioButtonMenuItem unspecifiedItem;
    private final AtomicReference<E> state = new AtomicReference<>(null);

    public EnumMenu(String name, Class<E> enumClass, boolean withUnspecifiedOption) {
        super(name);

        ButtonGroup buttonGroup = new ButtonGroup();
        if (withUnspecifiedOption) {
            JRadioButtonMenuItem unspecifiedItem = new JRadioButtonMenuItem("Unspecified");
            unspecifiedItem.addActionListener(_ -> this.state.set(null));
            unspecifiedItem.setSelected(true);
            buttonGroup.add(unspecifiedItem);
            this.unspecifiedItem = unspecifiedItem;
            this.add(unspecifiedItem);
        } else {
            this.unspecifiedItem = null;
        }
        for (E enumConstant : enumClass.getEnumConstants()) {
            JRadioButtonMenuItem item = new JRadioButtonMenuItem(enumConstant.getDisplayName());
            item.addActionListener(_ -> this.state.set(enumConstant));
            buttonGroup.add(item);
            this.buttonMap.put(enumConstant, item);
            this.add(item);
        }
    }

    public void setState(E val) {
        this.state.set(val);
        for (Map.Entry<E, JRadioButtonMenuItem> map : this.buttonMap.entrySet()) {
            map.getValue().setSelected(false);
        }
        if (this.unspecifiedItem != null) {
            this.unspecifiedItem.setSelected(false);
        }
        if (val == null) {
            if (this.unspecifiedItem != null) {
                this.unspecifiedItem.setSelected(true);
            }
            return;
        }
        Optional.ofNullable(this.buttonMap.get(val)).ifPresent(item -> item.setSelected(true));
    }

    public Optional<E> getState() {
        return Optional.ofNullable(this.state.get());
    }

}
