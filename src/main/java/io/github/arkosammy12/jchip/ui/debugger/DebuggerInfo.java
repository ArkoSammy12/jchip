package io.github.arkosammy12.jchip.ui.debugger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class DebuggerInfo {

    private final List<TextEntry<?>> textSectionEntries = new ArrayList<>();
    private final List<TextEntry<?>> singleRegistersEntries = new ArrayList<>();
    private final List<TextEntry<?>> registerEntries = new ArrayList<>();
    private final List<TextEntry<?>> stackEntries = new ArrayList<>();
    private Supplier<Integer> scrollAddressSupplier;

    private String textSectionName = "Current Quirks";
    private String singleRegisterSectionName = "Single Registers";
    private String registerSectionName = "Registers";
    private String stackSectionName = "Stack";

    public void setScrollAddressSupplier(Supplier<Integer> scrollAddressSupplier) {
        this.scrollAddressSupplier = scrollAddressSupplier;
    }

    public void setTextSectionName(String name) {
        this.textSectionName = name;
    }

    public void clearTextSectionEntries() {
        this.textSectionEntries.clear();
    }

    public void setSingleRegisterSectionName(String name) {
        this.singleRegisterSectionName = name;
    }

    public void clearSingleRegisterSectionEntries() {
        this.singleRegistersEntries.clear();
    }

    public void setRegisterSectionName(String name) {
        this.registerSectionName = name;
    }

    public void clearRegisterSectionEntries() {
        this.registerEntries.clear();
    }

    public void setStackSectionName(String name) {
        this.stackSectionName = name;
    }

    public void clearStackSectionEntries() {
        this.stackEntries.clear();
    }

    public String getTextSectionName() {
        return this.textSectionName;
    }

    public String getSingleRegisterSectionName() {
        return this.singleRegisterSectionName;
    }

    public String getRegisterSectionName() {
        return this.registerSectionName;
    }

    public String getStackSectionName() {
        return this.stackSectionName;
    }

    public <T> TextEntry<T> createTextSectionEntry() {
        TextEntry<T> entry = new TextEntry<>();
        this.textSectionEntries.add(entry);
        return entry;
    }

    public <T> TextEntry<T> createSingleRegisterSectionEntry() {
        TextEntry<T> entry = new TextEntry<>();
        this.singleRegistersEntries.add(entry);
        return entry;
    }

    public <T> TextEntry<T> createRegisterSectionEntry() {
        TextEntry<T> entry = new TextEntry<>();
        this.registerEntries.add(entry);
        return entry;
    }

    public <T> TextEntry<T> createStackSectionEntry() {
        TextEntry<T> entry = new TextEntry<>();
        this.stackEntries.add(entry);
        return entry;
    }

    List<DebuggerLabel<?>> getTextSectionLabels() {
        return this.textSectionEntries.stream().map(TextEntry::getDebuggerLabel).collect(Collectors.toList());
    }

    List<DebuggerLabel<?>> getSingleRegisterLabels() {
        return this.singleRegistersEntries.stream().map(TextEntry::getDebuggerLabel).collect(Collectors.toList());
    }

    List<DebuggerLabel<?>> getRegisterLabels() {
        return this.registerEntries.stream().map(TextEntry::getDebuggerLabel).collect(Collectors.toList());
    }

    List<DebuggerLabel<?>> getStackLabels() {
        return this.stackEntries.stream().map(TextEntry::getDebuggerLabel).collect(Collectors.toList());
    }

    Optional<Supplier<Integer>> getScrollAddressSupplier() {
        return Optional.ofNullable(this.scrollAddressSupplier);
    }

    public static class TextEntry<T> {

        private String name = null;
        private String description = null;
        private Supplier<T> stateUpdater;
        private Function<T, String> toStringFunction;

        public TextEntry<T> withName(String name) {
            this.name = name;
            return this;
        }

        public TextEntry<T> withDescription(String description) {
            this.description = description;
            return this;
        }

        public TextEntry<T> withStateUpdater(Supplier<T> stateUpdater) {
            this.stateUpdater = stateUpdater;
            return this;
        }

        public TextEntry<T> withToStringFunction(Function<T, String> toStringFunction) {
            this.toStringFunction = toStringFunction;
            return this;
        }

        public Optional<String> getName() {
            return Optional.ofNullable(this.name);
        }

        public Optional<String> getDescription() {
            return Optional.ofNullable(this.description);
        }

        public Optional<Supplier<T>> getStateUpdater() {
            return Optional.ofNullable(this.stateUpdater);
        }

        public Optional<Function<T, String>> getToStringFunction() {
            return Optional.ofNullable(this.toStringFunction);
        }

        DebuggerLabel<T> getDebuggerLabel() {
            return new DebuggerLabel<>(this);
        }

    }

}
