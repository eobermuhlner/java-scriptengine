package ch.obermuhlner.scriptengine.java.execution;

import org.junit.Test;

import javax.script.ScriptException;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class DefaultExecutionStrategyTest {

    @Test
    public void testNullExecution() throws ScriptException {
        DefaultExecutionStrategy executionStrategy = new DefaultExecutionStrategy(TestSupplierExecution.class);
        Object result = executionStrategy.execute(null);

        assertThat(result).isNull();
    }

    @Test
    public void testSupplierExecution() throws ScriptException {
        DefaultExecutionStrategy executionStrategy = new DefaultExecutionStrategy(TestSupplierExecution.class);
        TestSupplierExecution instance = new TestSupplierExecution();
        Object result = executionStrategy.execute(instance);

        assertThat(result).isEqualTo("Supplier");
    }

    @Test
    public void testRunnableExecution() throws ScriptException {
        DefaultExecutionStrategy executionStrategy = new DefaultExecutionStrategy(TestRunnableExecution.class);
        TestRunnableExecution instance = new TestRunnableExecution();
        Object result = executionStrategy.execute(instance);

        assertThat(result).isNull();
        assertThat(instance.counter).isEqualTo(1);
    }

    @Test
    public void testMethodExecution() throws ScriptException {
        DefaultExecutionStrategy executionStrategy = new DefaultExecutionStrategy(TestMethodExecution.class);
        TestMethodExecution instance = new TestMethodExecution();
        Object result = executionStrategy.execute(instance);

        assertThat(result).isEqualTo("success");
    }

    @Test
    public void testNoMethodExecution() {
        DefaultExecutionStrategy executionStrategy = new DefaultExecutionStrategy(TestNoMethodExecution.class);
        TestNoMethodExecution instance = new TestNoMethodExecution();
        assertThatThrownBy(() -> {
            executionStrategy.execute(instance);
        }).isInstanceOf(ScriptException.class);
    }

    @Test
    public void testAmbiguousMethodExecution() {
        DefaultExecutionStrategy executionStrategy = new DefaultExecutionStrategy(TestAmbiguousMethodExecution.class);
        TestAmbiguousMethodExecution instance = new TestAmbiguousMethodExecution();
        assertThatThrownBy(() -> {
            executionStrategy.execute(instance);
        }).isInstanceOf(ScriptException.class);
    }

    public static class TestSupplierExecution implements Supplier<String> {
        @Override
        public String get() {
            return "Supplier";
        }
    }

    public static class TestRunnableExecution implements Runnable {
        public int counter = 0;
        @Override
        public void run() {
            counter++;
        }
    }

    public static class TestMethodExecution {
        public String getSuccess() {
            return "success";
        }
        public String getFailure(int value) {
            return "failure-" + value;
        }
    }

    public static class TestNoMethodExecution {
        public String getFailure(int value) {
            return "failure-" + value;
        }
    }

    public static class TestAmbiguousMethodExecution {
        public String getFailure1() {
            return "failure1";
        }
        public String getFailure2() {
            return "failure2";
        }
    }
}
