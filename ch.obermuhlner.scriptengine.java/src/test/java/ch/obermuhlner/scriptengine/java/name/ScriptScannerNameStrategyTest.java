package ch.obermuhlner.scriptengine.java.name;

import org.junit.Test;

import javax.script.ScriptException;

import static org.assertj.core.api.Assertions.assertThat;

public class ScriptScannerNameStrategyTest {
    @Test
    public void testSimpleNames() throws ScriptException {
        ScriptScannerNameStrategy nameStrategy = new ScriptScannerNameStrategy();

        assertThat(nameStrategy.getFullName("public class Alpha {}")).isEqualTo("Alpha");
        assertThat(nameStrategy.getFullName("public class Beta$ {}")).isEqualTo("Beta$");
    }

    @Test
    public void testPackagesAndNames() throws ScriptException {
        ScriptScannerNameStrategy nameStrategy = new ScriptScannerNameStrategy();

        assertThat(nameStrategy.getFullName("package com.example; public class Alpha {}")).isEqualTo("com.example.Alpha");
    }

    @Test
    public void testMultipleLines() throws ScriptException {
        ScriptScannerNameStrategy nameStrategy = new ScriptScannerNameStrategy();

        assertThat(nameStrategy.getFullName("" +
                "/* Comment */\n" +
                "package com.example;\n" +
                "public class Alpha {\n" +
                "}")).isEqualTo("com.example.Alpha");
    }
}
