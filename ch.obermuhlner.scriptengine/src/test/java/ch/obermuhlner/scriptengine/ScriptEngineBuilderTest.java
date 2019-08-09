package ch.obermuhlner.scriptengine;

import org.junit.Test;

public class ScriptEngineBuilderTest {
    @Test
    public void testBasics() {
        ScriptEngineBuilder.byName("js")
                .setVar("alpha", 1)
                .setVar("beta", 2)
                .eval("var gamma = alpha+beta")
                .onResult(result -> {
                    System.out.println("Result: " + result);
                })
                .onException(ex -> {
                    System.out.println("Exception: " + ex);
                    ex.printStackTrace();
                })
                .getVar("gamma", (value) -> {
                    System.out.println("Gamma Var: " + value);
                })
                .getVars((key, value) -> {
                    System.out.println("Var: " + key + " = " + value);
                })
                .eval("gamma = gamma * 2")
                .getVar("gamma", (value) -> {
                    System.out.println("Gamma2 Var: " + value);
                });
    }
}
