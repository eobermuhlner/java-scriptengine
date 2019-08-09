package ch.obermuhlner.scriptengine.jshell;

import org.junit.Test;

import javax.script.ScriptEngine;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

public class JShellScriptEngineFactoryTest {
    @Test
    public void testGetEngineName() {
        JShellScriptEngineFactory factory = new JShellScriptEngineFactory();
        assertThat(factory.getEngineName()).isEqualTo("JShell ScriptEngine");
    }

    @Test
    public void testGetEngineVersion() {
        JShellScriptEngineFactory factory = new JShellScriptEngineFactory();
        assertThat(factory.getEngineVersion()).isEqualTo("0.1.0");
    }

    @Test
    public void testGetLanguageName() {
        JShellScriptEngineFactory factory = new JShellScriptEngineFactory();
        assertThat(factory.getLanguageName()).isEqualTo("JShell");
    }

    @Test
    public void testGetLanguageVersion() {
        JShellScriptEngineFactory factory = new JShellScriptEngineFactory();
        assertThat(factory.getLanguageVersion()).isEqualTo("9");
    }

    @Test
    public void testGetExtensions() {
        JShellScriptEngineFactory factory = new JShellScriptEngineFactory();
        assertThat(factory.getExtensions()).isEqualTo(Arrays.asList("jsh", "jshell"));
    }

    @Test
    public void testGetMimeTypes() {
        JShellScriptEngineFactory factory = new JShellScriptEngineFactory();
        assertThat(factory.getMimeTypes()).isEqualTo(Arrays.asList("text/x-jshell-source"));
    }

    @Test
    public void testGetNames() {
        JShellScriptEngineFactory factory = new JShellScriptEngineFactory();
        assertThat(factory.getNames()).isEqualTo(Arrays.asList("JShell", "jshell", "ch.obermuhlner:scriptengine-jshell", "obermuhlner-jshell"));
    }

    @Test
    public void testGetParameters() {
        JShellScriptEngineFactory factory = new JShellScriptEngineFactory();
        assertThat(factory.getParameter(ScriptEngine.ENGINE)).isEqualTo(factory.getEngineName());
        assertThat(factory.getParameter(ScriptEngine.ENGINE_VERSION)).isEqualTo(factory.getEngineVersion());
        assertThat(factory.getParameter(ScriptEngine.LANGUAGE)).isEqualTo(factory.getLanguageName());
        assertThat(factory.getParameter(ScriptEngine.LANGUAGE_VERSION)).isEqualTo(factory.getLanguageVersion());
        assertThat(factory.getParameter(ScriptEngine.NAME)).isEqualTo("JShell");
        assertThat(factory.getParameter("unknown")).isEqualTo(null);
    }

    @Test
    public void testGetMethodCallSyntax() {
        JShellScriptEngineFactory factory = new JShellScriptEngineFactory();
        assertThat(factory.getMethodCallSyntax("obj", "method")).isEqualTo("obj.method()");
        assertThat(factory.getMethodCallSyntax("obj", "method", "alpha")).isEqualTo("obj.method(alpha)");
        assertThat(factory.getMethodCallSyntax("obj", "method", "alpha", "beta")).isEqualTo("obj.method(alpha,beta)");
    }

    @Test
    public void testGetOutputStatement() {
        JShellScriptEngineFactory factory = new JShellScriptEngineFactory();
        assertThat(factory.getOutputStatement("alpha")).isEqualTo("System.out.println(alpha)");
    }

    @Test
    public void testGetProgram() {
        JShellScriptEngineFactory factory = new JShellScriptEngineFactory();
        assertThat(factory.getProgram()).isEqualTo("");
        assertThat(factory.getProgram("alpha")).isEqualTo("alpha;\n");
        assertThat(factory.getProgram("alpha", "beta")).isEqualTo("alpha;\nbeta;\n");
    }

    @Test
    public void testGetScriptEngine() {
        JShellScriptEngineFactory factory = new JShellScriptEngineFactory();
        assertThat(factory.getScriptEngine() instanceof JShellScriptEngine).isTrue();
    }

}
