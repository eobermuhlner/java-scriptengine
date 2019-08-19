package ch.obermuhlner.scriptengine.java.name;

import javax.script.ScriptException;

public interface NameStrategy {
    String getFullName(String script) throws ScriptException;

    static String extractSimpleName(String fullName) {
        int lastDotIndex = fullName.lastIndexOf(".");
        if (lastDotIndex < 0) {
            return fullName;
        }
        return fullName.substring(lastDotIndex + 1);
    }

    static String extractPackageName(String fullName) {
        int lastDotIndex = fullName.lastIndexOf(".");
        if (lastDotIndex < 0) {
            return "";
        }
        return fullName.substring(0, lastDotIndex);
    }
}
