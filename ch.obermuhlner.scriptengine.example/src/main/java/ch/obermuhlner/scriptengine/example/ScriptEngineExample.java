package ch.obermuhlner.scriptengine.example;

import ch.obermuhlner.scriptengine.java.Isolation;
import ch.obermuhlner.scriptengine.java.JavaCompiledScript;
import ch.obermuhlner.scriptengine.java.JavaScriptEngine;
import ch.obermuhlner.scriptengine.java.constructor.DefaultConstructorStrategy;
import ch.obermuhlner.scriptengine.java.execution.MethodExecutionStrategy;

import javax.script.*;

public class ScriptEngineExample {
    public static void main(String[] args) {
        runExamples();
    }

    private static void runExamples() {
        runHelloWorldExample();
        runCompileHelloWorldExample();
        runCompileEngineBindingsExample();
        runCompileGlobalBindingsExample();
        runConstructorStrategyMatchingArgumentsExample();
        runMethodExecutionStrategyFactoryMatchingArgumentsExample();
        runCompiledMethodExecutionStrategyMatchingArgumentsExample();
        runIsolationCallerClassLoaderClassesExample();
        runIsolationIsolatedClassesExample();
        runDangerousCode1Example();
        runDangerousCode2Example();
    }

    private static void runHelloWorldExample() {
        try {
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName("java");

            Object result = engine.eval("" +
                    "public class Script {" +
                    "   public String getMessage() {" +
                    "       return \"Hello World\";" +
                    "   } " +
                    "}");
            System.out.println("Result: " + result);
        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }

    private static void runCompileHelloWorldExample() {
        try {
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName("java");
            Compilable compiler = (Compilable) engine;

            CompiledScript compiledScript = compiler.compile("" +
                    "public class Script {" +
                    "   private int counter = 1;" +
                    "   public String getMessage() {" +
                    "       return \"Hello World #\" + counter++;" +
                    "   } " +
                    "}");

            Object result1 = compiledScript.eval();
            System.out.println("Result1: " + result1);

            Object result2 = compiledScript.eval();
            System.out.println("Result2: " + result2);
        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }

    private static void runCompileEngineBindingsExample() {
        try {
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName("java");
            Compilable compiler = (Compilable) engine;

            CompiledScript compiledScript = compiler.compile("" +
                    "public class Script {" +
                    "   public static String message = \"Counting\";" +
                    "   public int counter = 1;" +
                    "   public String getMessage() {" +
                    "       return message + \" #\" + counter++;" +
                    "   } " +
                    "}");

            {
                Bindings bindings = engine.createBindings();

                Object result = compiledScript.eval(bindings);

                System.out.println("Result1: " + result);
                System.out.println("Variable1 message: " + bindings.get("message"));
                System.out.println("Variable1 counter: " + bindings.get("counter"));
            }

            {
                Bindings bindings = engine.createBindings();
                bindings.put("message", "Hello world");

                Object result = compiledScript.eval(bindings);

                System.out.println("Result2: " + result);
                System.out.println("Variable2 message: " + bindings.get("message"));
                System.out.println("Variable2 counter: " + bindings.get("counter"));
            }
        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }


    private static void runCompileGlobalBindingsExample() {
        try {
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName("java");
            Compilable compiler = (Compilable) engine;

            String script = "" +
                    "public class Script {" +
                    "   public String message = \"Counting\";" +
                    "   public int counter = 1;" +
                    "   public String getMessage() {" +
                    "       return message + \" #\" + counter++;" +
                    "   } " +
                    "}";

            CompiledScript compiledScript1 = compiler.compile(script);
            CompiledScript compiledScript2 = compiler.compile(script);

            Bindings engineBindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
            Bindings globalBindings = engine.getBindings(ScriptContext.GLOBAL_SCOPE);

            {
                Object result = compiledScript1.eval();

                System.out.println("Result1: " + result);
                System.out.println("Variable1 message: " + globalBindings.get("message"));
                System.out.println("Variable1 counter: " + engineBindings.get("counter"));
            }

            globalBindings.put("message", "Hello static world");

            {
                Object result = compiledScript2.eval();

                System.out.println("Result2: " + result);
                System.out.println("Variable2 message: " + globalBindings.get("message"));
                System.out.println("Variable2 counter: " + engineBindings.get("counter"));
            }

            {
                Object result = compiledScript1.eval();

                System.out.println("Result1: " + result);
                System.out.println("Variable1 message: " + globalBindings.get("message"));
                System.out.println("Variable1 counter: " + engineBindings.get("counter"));
            }
        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }

    private static void runConstructorStrategyMatchingArgumentsExample() {
        try {
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName("java");
            JavaScriptEngine javaScriptEngine = (JavaScriptEngine) engine;

            javaScriptEngine.setConstructorStrategy(DefaultConstructorStrategy.byMatchingArguments("Hello", 42));

            Object result = engine.eval("" +
                    "public class Script {" +
                    "   private final String message;" +
                    "   private final int value;" +
                    "   public Script(String message, int value) {" +
                    "       this.message = message;" +
                    "       this.value = value;" +
                    "   }" +
                    "   public String getMessage() {" +
                    "       return \"Message: \" + message + value;" +
                    "   }" +
                    "}");

            System.out.println("Result: " + result);
        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }


    private static void runMethodExecutionStrategyFactoryMatchingArgumentsExample() {
        try {
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName("java");
            JavaScriptEngine javaScriptEngine = (JavaScriptEngine) engine;

            javaScriptEngine.setExecutionStrategyFactory((clazz) -> {
                return MethodExecutionStrategy.byMatchingArguments(
                        clazz,
                        "getMessage",
                        "Hello", 42);
            });

            Object result = engine.eval("" +
                    "public class Script {" +
                    "   public String getMessage(Object message, int value) {" +
                    "       return \"Message: \" + message + value;" +
                    "   } " +
                    "}");

            System.out.println("Result: " + result);
        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }

    private static void runCompiledMethodExecutionStrategyMatchingArgumentsExample() {
        try {
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName("java");
            JavaScriptEngine javaScriptEngine = (JavaScriptEngine) engine;

            javaScriptEngine.setExecutionStrategyFactory((clazz) -> {
                return MethodExecutionStrategy.byMatchingArguments(
                        clazz,
                        "getMessage",
                        "Hello", 42);
            });

            JavaCompiledScript compiledScript = javaScriptEngine.compile("" +
                    "public class Script {" +
                    "   public String getMessage(Object message, int value) {" +
                    "       return \"Message: \" + message + value;" +
                    "   } " +
                    "}");

            compiledScript.setExecutionStrategy(MethodExecutionStrategy.byMatchingArguments(
                    compiledScript.getCompiledClass(),
                    "getMessage",
                    "Hello", 42));

            Object result = compiledScript.eval();

            System.out.println("Result: " + result);
        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }

    private static void runIsolationCallerClassLoaderClassesExample() {
        try {
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName("java");
            JavaScriptEngine javaScriptEngine = (JavaScriptEngine) engine;

            JavaCompiledScript compiledScript = javaScriptEngine.compile("" +
                    "import ch.obermuhlner.scriptengine.example.Person;" +
                    "public class Script {" +
                    "   public Person getPerson() {" +
                    "       Person person = new Person();" +
                    "       person.name = \"Eric\";" +
                    "       person.birthYear = 1967;" +
                    "       return person;" +
                    "   } " +
                    "}");

            Object result = compiledScript.eval();
            System.out.println("Result: " + result);
        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }

    private static void runIsolationIsolatedClassesExample() {
        try {
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName("java");
            JavaScriptEngine javaScriptEngine = (JavaScriptEngine) engine;

            javaScriptEngine.setIsolation(Isolation.IsolatedClassLoader);

            JavaCompiledScript compiledScript = javaScriptEngine.compile("" +
                    "import ch.obermuhlner.scriptengine.example.Person;" +
                    "public class Script {" +
                    "   public Object getPerson() {" +
                    "       return 123;" +
                    "   } " +
                    "}");

            Object result = compiledScript.eval();
            System.out.println("Result: " + result);
        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }


    private static void runDangerousCode1Example() {
        try {
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName("java");
            JavaScriptEngine javaScriptEngine = (JavaScriptEngine) engine;

            javaScriptEngine.setIsolation(Isolation.IsolatedClassLoader);

            JavaCompiledScript compiledScript = javaScriptEngine.compile("" +
                    "import ch.obermuhlner.scriptengine.example.Person;" +
                    "public class Script {" +
                    "   public String getJavaHome() {" +
                    "       return System.getProperty(\"java.home\");" +
                    "   } " +
                    "}");

            Object result = compiledScript.eval();
            System.out.println("Result: " + result);
        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }

    private static void runDangerousCode2Example() {
        try {
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName("java");
            JavaScriptEngine javaScriptEngine = (JavaScriptEngine) engine;

            javaScriptEngine.setIsolation(Isolation.IsolatedClassLoader);

            JavaCompiledScript compiledScript = javaScriptEngine.compile("" +
                    "import ch.obermuhlner.scriptengine.example.Person;" +
                    "public class Script {" +
                    "   public Object getPerson() {" +
                    "       System.out.println(\"Calling System.exit(111)\");" +
                    "       System.exit(111);" +
                    "       return 123;" +
                    "   } " +
                    "}");

            Object result = compiledScript.eval();
            System.out.println("Result: " + result);
        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }
}
