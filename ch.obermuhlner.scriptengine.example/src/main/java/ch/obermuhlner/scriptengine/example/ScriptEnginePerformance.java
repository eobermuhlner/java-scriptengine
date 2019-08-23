package ch.obermuhlner.scriptengine.example;

import javax.script.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class ScriptEnginePerformance {
    private static final String SCRIPT_FUNCTION_ALPHA_BETA = "" +
            "public class Script implements java.util.function.Supplier<Double> {" +
            "   public double alpha;" +
            "   public double beta;" +
            "   @Override" +
            "   public Double get() {" +
            "       return alpha + beta;" +
            "   }" +
            "}";

    public static void main(String[] args) {
        runCompilePerformance();
    }

    private static void runCompilePerformance() {
        System.out.print("Warmup ");
        for (int i = 0; i < 10; i++) {
            System.out.print(".");
            for (int j = 0; j < 1; j++) {
                runMultiEvalExample(i);
                runCompileMultiEvalExample(i);
            }
        }
        System.out.println();

        System.out.print("Measure ");
        try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("Compile_Multiple_Evaluations.csv")))) {
            out.println("# csv2chart.title=Multiple Eval vs. Single Compile + Multiple Eval");
            out.println("n, Multi Eval, Compile + Multi Eval");
            for (int i = 0; i <= 10; i+=1) {
                System.out.print(".");
                int n = i;
                double millis1 = measure(10, () -> runMultiEvalExample(n));
                double millis2 = measure(10, () -> runCompileMultiEvalExample(n));
                out.println(i + ", " + millis1 + ", " + millis2);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println();

        System.out.print("Finished");
    }

    private static double measure(int runCount, Runnable runnable) {
        double sumElapsed = 0;
        for (int i = 0; i < runCount; i++) {
            double elapsed = measure(runnable);
            sumElapsed += elapsed;
        }
        return sumElapsed / runCount;
    }


    private static double measure(Runnable runnable) {
        long startNanos = System.nanoTime();
        runnable.run();
        long endNanos = System.nanoTime();
        return (endNanos - startNanos) / 1_000_000.0;
    }

    private static void runMultiEvalExample(int n) {
        try {
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName("java");

            for (int i = 0; i < n; i++) {
                engine.put("alpha", 2);
                engine.put("beta", 3);
                Object result = engine.eval(SCRIPT_FUNCTION_ALPHA_BETA);
            }
        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }

    private static void runCompileMultiEvalExample(int n) {
        try {
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName("java");
            Compilable compiler = (Compilable) engine;

            CompiledScript compiledScript = compiler.compile(SCRIPT_FUNCTION_ALPHA_BETA);

            for (int i = 0; i < n; i++) {
                Bindings bindings = engine.createBindings();

                bindings.put("alpha", 2);
                bindings.put("beta", 3);
                Object result = compiledScript.eval(bindings);
            }
        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }

}
