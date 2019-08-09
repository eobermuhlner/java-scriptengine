package ch.obermuhlner.scriptengine.jshell;

import org.junit.Test;

import javax.script.ScriptEngine;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JShellScriptEngineFactoryTest {
    @Test
    public void testGetEngineName() {
        JShellScriptEngineFactory factory = new JShellScriptEngineFactory();
        assertEquals("JShell ScriptEngine", factory.getEngineName());
    }

    @Test
    public void testGetEngineVersion() {
        JShellScriptEngineFactory factory = new JShellScriptEngineFactory();
        assertEquals("0.1.0", factory.getEngineVersion());
    }

    @Test
    public void testGetLanguageName() {
        JShellScriptEngineFactory factory = new JShellScriptEngineFactory();
        assertEquals("JShell", factory.getLanguageName());
    }

    @Test
    public void testGetLanguageVersion() {
        JShellScriptEngineFactory factory = new JShellScriptEngineFactory();
        assertEquals("9", factory.getLanguageVersion());
    }

    @Test
    public void testGetExtensions() {
        JShellScriptEngineFactory factory = new JShellScriptEngineFactory();
        assertEquals(Arrays.asList("jsh", "jshell"), factory.getExtensions());
    }

    @Test
    public void testGetMimeTypes() {
        JShellScriptEngineFactory factory = new JShellScriptEngineFactory();
        assertEquals(Arrays.asList("text/x-jshell-source"), factory.getMimeTypes());
    }

    @Test
    public void testGetNames() {
        JShellScriptEngineFactory factory = new JShellScriptEngineFactory();
        assertEquals(Arrays.asList("JShell", "jshell", "ch.obermuhlner:scriptengine-jshell", "obermuhlner-jshell"), factory.getNames());
    }

    @Test
    public void testGetParameters() {
        JShellScriptEngineFactory factory = new JShellScriptEngineFactory();
        assertEquals(factory.getEngineName(), factory.getParameter(ScriptEngine.ENGINE));
        assertEquals(factory.getEngineVersion(), factory.getParameter(ScriptEngine.ENGINE_VERSION));
        assertEquals(factory.getLanguageName(), factory.getParameter(ScriptEngine.LANGUAGE));
        assertEquals(factory.getLanguageVersion(), factory.getParameter(ScriptEngine.LANGUAGE_VERSION));
        assertEquals("JShell", factory.getParameter(ScriptEngine.NAME));
        assertEquals(null, factory.getParameter("unknown"));
    }

    @Test
    public void testGetMethodCallSyntax() {
        JShellScriptEngineFactory factory = new JShellScriptEngineFactory();
        assertEquals("obj.method()", factory.getMethodCallSyntax("obj", "method"));
        assertEquals("obj.method(alpha)", factory.getMethodCallSyntax("obj", "method", "alpha"));
        assertEquals("obj.method(alpha,beta)", factory.getMethodCallSyntax("obj", "method", "alpha", "beta"));
    }

    @Test
    public void testGetOutputStatement() {
        JShellScriptEngineFactory factory = new JShellScriptEngineFactory();
        assertEquals("System.out.println(alpha)", factory.getOutputStatement("alpha"));
    }

    @Test
    public void testGetProgram() {
        JShellScriptEngineFactory factory = new JShellScriptEngineFactory();
        assertEquals("", factory.getProgram());
        assertEquals("alpha;\n", factory.getProgram("alpha"));
        assertEquals("alpha;\nbeta;\n", factory.getProgram("alpha", "beta"));
    }

    @Test
    public void testGetScriptEngine() {
        JShellScriptEngineFactory factory = new JShellScriptEngineFactory();
        assertTrue(factory.getScriptEngine() instanceof JShellScriptEngine);
    }

}
