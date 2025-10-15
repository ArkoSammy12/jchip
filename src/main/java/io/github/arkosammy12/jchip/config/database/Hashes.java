package io.github.arkosammy12.jchip.config.database;

import java.util.Map;
import java.util.Optional;

public class Hashes {

    private Map<String, Integer> hashes;

    public Hashes(Map<String, Integer> hashes) {
        this.hashes = hashes;
    }

    public Optional<Map<String, Integer>> getHashes() {
        return Optional.ofNullable(this.hashes);
    }

    public Optional<Integer> getIndexForHash(String hash) {
        if (hash == null) {
            return Optional.empty();
        }
        if (this.hashes == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(this.hashes.get(hash));
    }

}
