package ch.obermuhlner.scriptengine.java;

import org.junit.Ignore;
import org.junit.Test;

import javax.script.*;

import java.io.Reader;
import java.io.StringReader;

import static org.assertj.core.api.Assertions.*;

public class JavaScriptEngineTest {
    @Test
    public void testSupplier() throws ScriptException {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("java");

        Object result = engine.eval("" +
                "public class Script implements java.util.function.Supplier<String> {" +
                "   public String get() {" +
                "       return \"Hello\";" +
                "   } " +
                "   public int ignore() {" +
                "       return -1;" +
                "   }" +
                "}");
        assertThat(result).isEqualTo("Hello");
    }

    @Test
    public void testRunnable() throws ScriptException {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("java");

        Object result = engine.eval("" +
                "public class Script implements java.lang.Runnable {" +
                "   public String message;" +
                "   public void run() {" +
                "       message = \"Hello\";" +
                "   } " +
                "   public int ignore() {" +
                "       return -1;" +
                "   }" +
                "}");
        assertThat(result).isEqualTo(null);
    }

    @Test
    public void testCallableMethod() throws ScriptException {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("java");

        Object result = engine.eval("" +
                "public class Script {" +
                "   public String getMessage() {" +
                "       return \"Hello\";" +
                "   } " +
                "}");
        assertThat(result).isEqualTo("Hello");
    }
}
