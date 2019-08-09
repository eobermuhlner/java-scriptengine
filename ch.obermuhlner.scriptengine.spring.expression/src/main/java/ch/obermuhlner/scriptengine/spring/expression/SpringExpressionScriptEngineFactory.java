package ch.obermuhlner.scriptengine.spring.expression;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import java.util.Arrays;
import java.util.List;

public class SpringExpressionScriptEngineFactory implements ScriptEngineFactory {
    @Override
    public String getEngineName() {
        return "Spring Expression Language";
    }

    @Override
    public String getEngineVersion() {
        return "0.1.0";
    }

    @Override
    public List<String> getExtensions() {
        return Arrays.asList("spel");
    }

    @Override
    public List<String> getMimeTypes() {
        return Arrays.asList("text/x-spel-source");
    }

    @Override
    public List<String> getNames() {
        return Arrays.asList("spel", "SpEL", "SpringExpression", "ch.obermuhlner:spel-scriptengine", "obermuhlner-spel");
    }

    @Override
    public String getLanguageName() {
        return "Spring Expression Language";
    }

    @Override
    public String getLanguageVersion() {
        return "5.1.9";
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
        s.append(")");
        return s.toString();
    }

    @Override
    public String getOutputStatement(String toDisplay) {
        throw new UnsupportedOperationException();
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
        return new SpringExpressionScriptEngine();
    }
}
