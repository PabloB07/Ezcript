package dev.ezcript.runtime;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ColorFormatter {
    private static final Pattern HEX_COLOR = Pattern.compile("(?i)&?#([0-9a-f]{6})");
    private static final String LEGACY_CODES = "0123456789abcdefklmnor";

    private ColorFormatter() { }

    public static String colorize(String text) {
        if (text == null) {
            throw new IllegalArgumentException("Text to colorize cannot be null");
        }

        Matcher matcher = HEX_COLOR.matcher(text);
        StringBuffer expanded = new StringBuffer();
        while (matcher.find()) {
            StringBuilder replacement = new StringBuilder("\u00a7x");
            String hex = matcher.group(1);
            for (int index = 0; index < hex.length(); index++) {
                replacement.append("\u00a7").append(hex.charAt(index));
            }
            matcher.appendReplacement(expanded, Matcher.quoteReplacement(replacement.toString()));
        }
        matcher.appendTail(expanded);

        StringBuilder output = new StringBuilder(expanded.length());
        for (int index = 0; index < expanded.length(); index++) {
            char current = expanded.charAt(index);
            if (current == '&' && index + 1 < expanded.length()) {
                char code = Character.toLowerCase(expanded.charAt(index + 1));
                if (LEGACY_CODES.indexOf(code) >= 0 || code == 'x') {
                    output.append('\u00a7').append(code);
                    index++;
                    continue;
                }
            }
            output.append(current);
        }
        return output.toString();
    }
}
