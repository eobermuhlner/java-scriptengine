package ch.obermuhlner.scriptengine.jshell;

import org.junit.Ignore;
import org.junit.Test;

import javax.script.*;

import java.io.Reader;
import java.io.StringReader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;


public class JShellScriptEngineTest {
    @Test
    public void testEmpty() throws ScriptException {
        assertScript("", null);
    }

    @Test
    public void testSimple() throws ScriptException {
        assertScript("2+3", 5);
    }

    @Test
    public void testSimpleDeclareVariable() throws ScriptException {
        assertScript("var alpha = 123", 123);
    }

    @Ignore("lastValue() has no access to default value of declarations")
    @Test
    public void testSimpleDeclareIntVariable() throws ScriptException {
        assertScript("int alpha", 123);
    }

    @Test
    public void testFailUnknownVariable() {
        assertScriptThrows("unknown", ScriptException.class);
    }

    @Test
    public void testFailIncompleteScript() {
        assertScriptThrows("foo(", ScriptException.class);
    }

    @Test
    public void testFailSameVariable() {
        assertScriptThrows("" +
                "var alpha = 0;" +
                "var alpha = 1;",
                ScriptException.class);
    }

    @Test
    public void testFailEvalDivByZero() {
        assertScriptThrows("1/0", ScriptException.class);
    }

    @Test
    public void testFailEvalNullPointerException() {
        assertScriptThrows("" +
                "Object foo = null;" +
                "foo.toString()",
                ScriptException.class);
    }

    @Test
    public void testBindingsExistingVariable() throws ScriptException {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("jshell");
        engine.put("alpha", 2);
        engine.put("beta", 3);
        engine.put("gamma", 0);

        Object result = engine.eval("gamma = alpha + beta");

        assertThat(result).isEqualTo(5);
        assertThat(engine.get("gamma")).isEqualTo(5);
    }

    @Test
    public void testBindingsNewVariable() throws ScriptException {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("jshell");
        engine.put("alpha", 2);
        engine.put("beta", 3);

        Object result = engine.eval("var gamma = alpha + beta");
        assertThat(result).isEqualTo(5);
        assertThat(engine.get("gamma")).isEqualTo(5);
    }

    @Test
    public void testBindingsMultipleEval() throws ScriptException {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("jshell");
        engine.put("alpha", 2);
        engine.put("beta", 3);
        engine.put("gamma", 0);

        Object result = engine.eval("gamma = alpha + beta");
        assertThat(result).isEqualTo(5);
        assertThat(engine.get("gamma")).isEqualTo(5);

        Object result2 = engine.eval("gamma = alpha + beta + gamma");
        assertThat(result2).isEqualTo(10);
        assertThat(engine.get("gamma")).isEqualTo(10);
    }

    @Test
    public void testBindingsGlobalVariable() throws ScriptException {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("jshell");
        Bindings globalBindings = engine.getBindings(ScriptContext.GLOBAL_SCOPE);
        globalBindings.put("alpha", 2);
        globalBindings.put("beta", 3);
        globalBindings.put("gamma", 0);

        Object result = engine.eval("gamma = alpha + beta");
        assertThat(result).isEqualTo(5);
        assertThat(engine.get("gamma")).isEqualTo(null);
        assertThat(globalBindings.get("gamma")).isEqualTo(5);
    }

    @Test
    public void testBindingsOverrideGlobalVariable() throws ScriptException {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("jshell");
        Bindings globalBindings = engine.getBindings(ScriptContext.GLOBAL_SCOPE);
        globalBindings.put("alpha", 2);
        globalBindings.put("beta", 3);
        globalBindings.put("gamma", 0);

        engine.put("gamma", 999);

        assertThat(engine.get("gamma")).isEqualTo(999);
        assertThat(globalBindings.get("gamma")).isEqualTo(0);

        Object result = engine.eval("gamma = alpha + beta");
        assertThat(result).isEqualTo(5);
        assertThat(engine.get("gamma")).isEqualTo(5);
        assertThat(globalBindings.get("gamma")).isEqualTo(0);
    }

    @Test
    public void testBindingsPublicClass() throws ScriptException {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("jshell");
        PublicClass publicClass = new PublicClass();
        publicClass.message = "hello";
        engine.put("alpha", publicClass);

        Object result = engine.eval("var message = alpha.message");
        assertThat(result).isEqualTo("hello");
        assertThat(engine.get("alpha")).isSameAs(publicClass);
        assertThat(engine.get("message")).isEqualTo("hello");
    }

    @Test(expected = ScriptException.class)
    public void testBindingsPrivateClassFail() throws ScriptException {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("jshell");
        PrivateClass privateClass = new PrivateClass();
        engine.put("alpha", privateClass);

        Object result = engine.eval("ch.obermuhlner.scriptengine.jshell.PrivateClass beta = alpha");
    }

    @Test
    public void testBindingsPrivateClassAsObject() throws ScriptException {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("jshell");
        PrivateClass privateClass = new PrivateClass();
        engine.put("alpha", privateClass);

        Object result = engine.eval("Object beta = alpha");
        assertThat(engine.get("alpha")).isSameAs(privateClass);
        assertThat(engine.get("beta")).isSameAs(privateClass);
    }

    @Test(expected = ScriptException.class)
    public void testBindingsProtectedClassFail() throws ScriptException {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("jshell");
        PrivateClass privateClass = new PrivateClass();
        engine.put("alpha", privateClass);

        Object result = engine.eval("ch.obermuhlner.scriptengine.jshell.ProtectedClass beta = alpha");
    }

    @Test
    public void testBindingsProtectedClassAsObject() throws ScriptException {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("jshell");
        ProtectedClass protectedClass = new ProtectedClass();
        engine.put("alpha", protectedClass);

        Object result = engine.eval("Object beta = alpha");
        assertThat(engine.get("alpha")).isSameAs(protectedClass);
        assertThat(engine.get("beta")).isSameAs(protectedClass);
    }

    @Test(expected = ScriptException.class)
    public void testBindingsIllegalVariable() throws ScriptException {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("jshell");
        engine.put("illegal with spaces", 2);

        Object result = engine.eval("var message = alpha.message");
    }

    @Test
    public void testEvalReader() throws ScriptException {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("jshell");

        Reader reader = new StringReader("1234");
        Object result = engine.eval(reader);
        assertThat(result).isEqualTo(1234);
    }

    @Test
    public void testEvalReaderContext() throws ScriptException {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("jshell");

        ScriptContext context = new SimpleScriptContext();
        context.getBindings(ScriptContext.ENGINE_SCOPE).put("alpha", 1000);
        Reader reader = new StringReader("alpha+999");
        Object result = engine.eval(reader, context);
        assertThat(result).isEqualTo(1999);
    }

    @Test
    public void testEvalReaderBindings() throws ScriptException {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("jshell");

        SimpleBindings bindings = new SimpleBindings();
        bindings.put("alpha", 1000);
        Reader reader = new StringReader("alpha+321");
        Object result = engine.eval(reader, bindings);
        assertThat(result).isEqualTo(1321);
    }

    @Test
    public void testSetGetContext() throws ScriptException {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("jshell");

        assertThat(engine.getContext()).isNotNull();

        SimpleScriptContext context = new SimpleScriptContext();
        engine.setContext(context);
        assertThat(engine.getContext()).isSameAs(context);
    }

    @Test(expected = NullPointerException.class)
    public void testSetContextFail() throws ScriptException {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("jshell");

        engine.setContext(null);
    }

    @Test
    public void testGetFactory() throws ScriptException {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("jshell");

        ScriptEngineFactory factory = engine.getFactory();
        assertThat(factory.getClass()).isSameAs(JShellScriptEngineFactory.class);
    }

    private void assertScript(String script, Object expectedResult) throws ScriptException {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("jshell");
        Object result = engine.eval(script);
        assertThat(result).isEqualTo(expectedResult);
    }

    private void assertScriptThrows(String script, Class<? extends Throwable> throwableClass) {
        try {
            assertScript(script, "Should never reach the result");

            fail("Expected throwing: " + throwableClass.getName());
        } catch (Throwable throwable) {
            System.out.println(throwable.getClass().getName() + " : " + throwable.getMessage());
            if (!throwable.getClass().isAssignableFrom(throwableClass)) {
                fail("Expected throwing: " + throwableClass.getName() + " but was thrown: " + throwable.getClass().getName());
            }
        }
    }

    public static class PublicClass {
        public String message;
    }

    private static class PrivateClass {
    }

    protected static class ProtectedClass {
    }
}
