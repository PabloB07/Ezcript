package dev.ezcript.runtime;

import fr.mrmicky.fastboard.FastBoard;

import java.util.List;
import java.util.Objects;

final class FastBoardSupport {
    private FastBoardSupport() { }

    static String[] toLines(List<?> lines) {
        Objects.requireNonNull(lines, "Scoreboard lines cannot be null");
        return lines.stream()
                .map(line -> Objects.requireNonNull(line, "Scoreboard line cannot be null").toString())
                .toArray(String[]::new);
    }

    static void updateLines(FastBoard board, List<?> lines) {
        Objects.requireNonNull(board, "FastBoard cannot be null").updateLines(toLines(lines));
    }
}
