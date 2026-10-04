package dev.ezcript.runtime;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ColorFormatterTest {
    @Test
    void translatesLegacyAndHexCodes() {
        assertEquals("\u00a7aGreen \u00a7x\u00a7f\u00a7f\u00a7a\u00a70\u00a70\u00a70Gold",
                ColorFormatter.colorize("&aGreen &#ffa000Gold"));
    }

    @Test
    void leavesUnrecognizedAmpersandsUntouched() {
        assertEquals("&z", ColorFormatter.colorize("&z"));
    }
}
