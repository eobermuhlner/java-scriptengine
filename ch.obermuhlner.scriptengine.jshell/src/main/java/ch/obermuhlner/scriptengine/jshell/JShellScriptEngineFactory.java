package ch.obermuhlner.scriptengine.jshell;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import java.util.Arrays;
import java.util.List;

public class JShellScriptEngineFactory implements ScriptEngineFactory {
    @Override
    public String getEngineName() {
        return "JShell ScriptEngine";
    }

    @Override
    public String getEngineVersion() {
        return "0.1";
    }

    @Override
    public List<String> getExtensions() {
        return Arrays.asList("jsh", "jshell");
    }

    @Override
    public List<String> getMimeTypes() {
        return Arrays.asList("text/x-jshell-source");
    }

    @Override
    public List<String> getNames() {
        return Arrays.asList("JShell", "jshell", "obermuhlner-jshell");
    }

    @Override
    public String getLanguageName() {
        return "JShell";
    }

    @Override
    public String getLanguageVersion() {
        return "9";
    }

    @Override
    public Object getParameter(String key) {
        switch (key) {
            case ScriptEngine.ENGINE:
                return getEngineName();
            case ScriptEngine.ENGINE_VERSION:
                return getEngineVersion();
            case ScriptEngine.LANGUAGE:
                return getLanguageName();
            case ScriptEngine.LANGUAGE_VERSION:
                return getLanguageVersion();
            case ScriptEngine.NAME:
                return getNames().get(0);
        }
        return null;
    }

    @Override
    public String getMethodCallSyntax(String obj, String method, String... args) {
        StringBuilder s = new StringBuilder();
        s.append(obj);
        s.append(".");
        s.append(method);
        s.append("(");
        for(int i = 0; i < args.length; i++) {
            if (i > 0) {
                s.append(",");
            }
            s.append(args[i]);
        }
        s.append(");");
        return s.toString();
    }

    @Override
    public String getOutputStatement(String toDisplay) {
        String escapedToDisplay = toDisplay
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\t", "\\t");
        return "System.out.println(" + escapedToDisplay + ")";
    }

    @Override
    public String getProgram(String... statements) {
        StringBuilder s = new StringBuilder();
        for(String statement : statements) {
            s.append(statement);
            s.append(";\n");
        }
        return s.toString();
    }

    @Override
    public ScriptEngine getScriptEngine() {
        return new JShellScriptEngine();
    }
}