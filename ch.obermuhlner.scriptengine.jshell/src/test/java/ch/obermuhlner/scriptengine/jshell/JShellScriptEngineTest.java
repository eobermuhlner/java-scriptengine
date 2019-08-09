package ch.obermuhlner.scriptengine.jshell;

import org.junit.Ignore;
import org.junit.Test;

import javax.script.*;

import java.io.Reader;
import java.io.StringReader;

import static org.junit.Assert.*;

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
        assertEquals(5, result);
        assertEquals(5, engine.get("gamma"));
    }

    @Test
    public void testBindingsNewVariable() throws ScriptException {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("jshell");
        engine.put("alpha", 2);
        engine.put("beta", 3);

        Object result = engine.eval("var gamma = alpha + beta");
        assertEquals(5, result);
        assertEquals(5, engine.get("gamma"));
    }

    @Test
    public void testBindingsMultipleEval() throws ScriptException {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("jshell");
        engine.put("alpha", 2);
        engine.put("beta", 3);
        engine.put("gamma", 0);

        Object result = engine.eval("gamma = alpha + beta");
        assertEquals(5, result);
        assertEquals(5, engine.get("gamma"));

        Object result2 = engine.eval("gamma = alpha + beta + gamma");
        assertEquals(10, result2);
        assertEquals(10, engine.get("gamma"));
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
        assertEquals(5, result);
        assertEquals(null, engine.get("gamma"));
        assertEquals(5, globalBindings.get("gamma"));
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

        assertEquals(999, engine.get("gamma"));
        assertEquals(0, globalBindings.get("gamma"));

        Object result = engine.eval("gamma = alpha + beta");
        assertEquals(5, result);
        assertEquals(5, engine.get("gamma"));
        assertEquals(0, globalBindings.get("gamma"));
    }

    @Test
    public void testBindingsPublicClass() throws ScriptException {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("jshell");
        PublicClass publicClass = new PublicClass();
        publicClass.message = "hello";
        engine.put("alpha", publicClass);

        Object result = engine.eval("var message = alpha.message");
        assertEquals("hello", result);
        assertSame(publicClass, engine.get("alpha"));
        assertEquals("hello", engine.get("message"));
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
        assertSame(privateClass, engine.get("alpha"));
        assertSame(privateClass, engine.get("beta"));
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
        assertSame(protectedClass, engine.get("alpha"));
        assertSame(protectedClass, engine.get("beta"));
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
        assertEquals(1234, result);
    }

    @Test
    public void testEvalReaderContext() throws ScriptException {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("jshell");

        ScriptContext context = new SimpleScriptContext();
        context.getBindings(ScriptContext.ENGINE_SCOPE).put("alpha", 1000);
        Reader reader = new StringReader("alpha+999");
        Object result = engine.eval(reader, context);
        assertEquals(1999, result);
    }

    @Test
    public void testEvalReaderBindings() throws ScriptException {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("jshell");

        SimpleBindings bindings = new SimpleBindings();
        bindings.put("alpha", 1000);
        Reader reader = new StringReader("alpha+321");
        Object result = engine.eval(reader, bindings);
        assertEquals(1321, result);
    }

    @Test
    public void testGetFactory() throws ScriptException {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("jshell");

        ScriptEngineFactory factory = engine.getFactory();
        assertSame(JShellScriptEngineFactory.class, factory.getClass());
    }

    private void assertScript(String script, Object expectedResult) throws ScriptException {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("jshell");
        Object result = engine.eval(script);
        assertEquals(expectedResult, result);
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
