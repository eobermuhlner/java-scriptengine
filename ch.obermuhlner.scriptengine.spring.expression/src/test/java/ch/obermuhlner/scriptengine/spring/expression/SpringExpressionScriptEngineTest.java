package ch.obermuhlner.scriptengine.spring.expression;

import org.junit.Test;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import static org.assertj.core.api.Assertions.assertThat;

public class SpringExpressionScriptEngineTest {
    @Test
    public void testBindingVariables() throws ScriptException {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("spel");

        engine.put("alpha", 2);
        engine.put("beta", 3);
        Object result = engine.eval("#alpha + #beta");

        assertThat(result).isEqualTo(5);
    }

    @Test
    public void testBindingRoot() throws ScriptException {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("spel");

        PublicClass root = new PublicClass();
        root.message = "hello";

        engine.put(SpringExpressionScriptEngine.ROOT, root);
        Object result = engine.eval("message");

        assertThat(result).isEqualTo("hello");
    }

    @Test
    public void testBindingChangedVariables() throws ScriptException {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("spel");

        engine.put("alpha", 2);
        Object result = engine.eval("#alpha = 3");
        assertThat(result).isEqualTo(3);

        Object alpha = engine.get("alpha");
        assertThat(alpha).isEqualTo(3);
    }

    @Test
    public void testConstructingReturnValue() throws ScriptException {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("spel");

        PublicClass result = (PublicClass) engine.eval("new ch.obermuhlner.scriptengine.spring.expression.SpringExpressionScriptEngineTest.PublicClass(\"test\")");
        System.out.println(result.getClass());
        assertThat(result.message).isEqualTo("test");
    }

    public static class PublicClass {
        public String message;

        public PublicClass() {
            this("nothing");
        }

        public PublicClass(String message) {
            this.message = message;
        }
    }

}
