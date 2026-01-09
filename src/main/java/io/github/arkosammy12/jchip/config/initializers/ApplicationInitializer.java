package io.github.arkosammy12.jchip.config.initializers;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public interface ApplicationInitializer extends EmulatorInitializer {

    Optional<List<Path>> getRecentFiles();

    Optional<String> getCurrentDirectory();

    Optional<Integer> getVolume();

    Optional<Boolean> getMuted();

    Optional<Boolean> getShowingInfoBar();

    Optional<Boolean> getShowingDebugger();

    Optional<Boolean> getShowingDisassembler();

    Optional<Boolean> getDebuggerFollowing();

    Optional<Boolean> getDisassemblerFollowing();

    Optional<Integer> getMainWindowWidth();

    Optional<Integer> getMainWindowHeight();

    Optional<Integer> getMainWindowX();

    Optional<Integer> getMainWindowY();

    Optional<Integer> getMainSplitDividerLocation();

    Optional<Integer> getViewportDisassemblerDividerLocation();

    Optional<Integer> getDebuggerMemoryDividerLocation();

    Optional<Integer> getDebuggerDividerLocation1();

    Optional<Integer> getDebuggerDividerLocation2();

    Optional<Integer> getDebuggerDividerLocation3();

}
