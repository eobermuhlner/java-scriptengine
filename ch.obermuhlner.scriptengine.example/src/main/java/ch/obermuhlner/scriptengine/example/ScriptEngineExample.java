package ch.obermuhlner.scriptengine.example;

import javax.script.*;

public class ScriptEngineExample {
    public static void main(String[] args) {
        runExamples();
    }

    private static void runExamples() {
        //runHelloWorldExample();
        //runCompileHelloWorldExample();
        runCompileEngineBindingsExample();
        //runCompileGlobalBindingsExample();
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
}
