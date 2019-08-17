package ch.obermuhlner.scriptengine.example;

import javax.script.*;

public class ScriptEngineExample {
    public static void main(String[] args) {
        runExamples();
    }

    private static void runExamples() {
        //runHelloWorldExample();
        //runCompileHelloWorldExample();
        runCompileBindingsExample();
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

    private static void runCompileBindingsExample() {
        try {
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName("java");
            Compilable compiler = (Compilable) engine;

            CompiledScript compiledScript = compiler.compile("" +
                    "public class Script {" +
                    "   public String message = \"Counting\";" +
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
}
