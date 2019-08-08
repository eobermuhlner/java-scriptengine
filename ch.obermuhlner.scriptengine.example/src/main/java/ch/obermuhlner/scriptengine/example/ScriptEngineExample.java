package ch.obermuhlner.scriptengine.example;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class ScriptEngineExample {
    public static void main(String[] args) {
        runExample("nashorn", "2+3");
        runExample("jshell", "2+3");

        runBindingExample();
        runVisibleClassesExample();
    }

    private static void runExample(String engineName, String script) {
        try {
            System.out.println("Engine: " + engineName);
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName(engineName);
            Object result = engine.eval(script);
            System.out.println("Result: " + result);
        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }

    private static void runBindingExample() {
        try {
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName("jshell");

            String script = "" +
                    "System.out.println(\"Input A: \" + inputA);" +
                    "System.out.println(\"Input B: \" + inputB);" +
                    "var output = inputA + inputB;" +
                    "1000 + output;";

            engine.put("inputA", 2);
            engine.put("inputB", 3);

            Object result = engine.eval(script);
            System.out.println("Result: " + result);

            Object output = engine.get("output");
            System.out.println("Output Variable: " + output);

        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }

    private static void runVisibleClassesExample() {
        try {
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName("jshell");

            String script = "" +
                    "import ch.obermuhlner.scriptengine.example.Person;" +
                    "var person = new Person();" +
                    "person.name = \"Eric\";" +
                    "person.birthYear = 1967;";

            Object result = engine.eval(script);
            System.out.println("Result: " + result);

            Object person = engine.get("person");
            System.out.println("Person Variable: " + person);

        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }

}
