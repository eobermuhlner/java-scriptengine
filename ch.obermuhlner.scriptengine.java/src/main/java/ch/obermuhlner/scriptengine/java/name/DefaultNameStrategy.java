package ch.obermuhlner.scriptengine.java.name;

public class DefaultNameStrategy implements NameStrategy {
    private final String fullName;

    public DefaultNameStrategy(String fullName) {
        this.fullName = fullName;
    }

    @Override
    public String getFullName(String script) {
        return fullName;
    }
}
