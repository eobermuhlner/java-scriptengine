package ch.obermuhlner.scriptengine.java.name;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DefaultNameStrategyTest {
    @Test
    public void testSimpleName() {
        String script = "script";
        DefaultNameStrategy nameStrategy = new DefaultNameStrategy("Test");
        String fullName = nameStrategy.getFullName(script);

        assertThat(fullName).isEqualTo("Test");
        assertThat(NameStrategy.extractSimpleName(fullName)).isEqualTo("Test");
        assertThat(NameStrategy.extractPackageName(fullName)).isEqualTo("");
    }

    @Test
    public void testFullyQualifiedName() {
        String script = "script";
        DefaultNameStrategy nameStrategy = new DefaultNameStrategy("com.example.Test");
        String fullName = nameStrategy.getFullName(script);

        assertThat(fullName).isEqualTo("com.example.Test");
        assertThat(NameStrategy.extractSimpleName(fullName)).isEqualTo("Test");
        assertThat(NameStrategy.extractPackageName(fullName)).isEqualTo("com.example");
    }

}
