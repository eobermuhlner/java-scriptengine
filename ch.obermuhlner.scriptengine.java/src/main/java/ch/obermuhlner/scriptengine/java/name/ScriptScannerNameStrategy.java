package ch.obermuhlner.scriptengine.java.name;

import javax.script.ScriptException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// https://gist.github.com/RlonRyan/0bc5bdfcf8d1a304167c4c60523bf617
// (?!(?:abstract|continue|for|new|switch|assert|default|if|package|synchronized|boolean|do|goto|private|this|break|double|implements|protected|throw|byte|else|import|public|throws|case|enum|instanceof|return|transient|catch|extends|int|short|try|char|final|interface|static|void|class|finally|long|strictfp|volatile|const|float|native|super|while)(?=\Z|\s|;))(?<=\A|\s|;)[\p{L}\p{Pc}$^\s][\p{L}\p{Pc}$\p{N}]*

public class ScriptScannerNameStrategy implements NameStrategy {
    private static final Pattern NAME_PATTERN = Pattern.compile("public\\s+class\\s+([A-Za-z][A-Za-z0-9_$]*)");
    private static final Pattern PACKAGE_PATTERN = Pattern.compile("package\\s+([A-Za-z][A-Za-z0-9_$.]*)");

    @Override
    public String getFullName(String script) throws ScriptException {
        String fullPackage = null;
        Matcher packageMatcher = PACKAGE_PATTERN.matcher(script);
        if (packageMatcher.find()) {
            fullPackage = packageMatcher.group(1);
        }

        Matcher nameMatcher = NAME_PATTERN.matcher(script);
        if (nameMatcher.find()) {
            String name = nameMatcher.group(1);
            if (fullPackage == null) {
                return name;
            } else {
                return fullPackage + "." + name;
            }
        }

        throw new ScriptException("Could not determine fully qualified class name");
    }
}
