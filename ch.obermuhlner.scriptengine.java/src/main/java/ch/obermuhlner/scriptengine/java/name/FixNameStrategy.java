package ch.obermuhlner.scriptengine.java.name;

public class FixNameStrategy implements NameStrategy {
    private final String fullName;

    public FixNameStrategy(String fullName) {
        this.fullName = fullName;
    }

    @Override
    public String getFullName(String script) {
        return fullName;
    }
}
