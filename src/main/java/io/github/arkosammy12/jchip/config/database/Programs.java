package io.github.arkosammy12.jchip.config.database;

import java.util.List;
import java.util.Optional;

class Programs {

    List<ProgramEntry> programEntries;

    public Programs(List<ProgramEntry> programEntries) {
        this.programEntries = programEntries;
    }

    public Optional<List<ProgramEntry>> getProgramEntries() {
        return Optional.ofNullable(this.programEntries);
    }

    public Optional<ProgramEntry> getProgramEntryAt(int index) {
        if (this.programEntries == null) {
            return Optional.empty();
        }
        if (index < 0 || index >= this.programEntries.size()) {
            return Optional.empty();
        }
        return Optional.of(this.programEntries.get(index));
    }

}
