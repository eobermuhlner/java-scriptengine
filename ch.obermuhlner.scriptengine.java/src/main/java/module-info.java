module ch.obermuhlner.scriptengine.java {
    exports ch.obermuhlner.scriptengine.java;
    exports ch.obermuhlner.scriptengine.java.constructor;
    exports ch.obermuhlner.scriptengine.java.execution;
    exports ch.obermuhlner.scriptengine.java.name;
    exports ch.obermuhlner.scriptengine.java.util;

    requires transitive java.scripting;

    requires java.compiler;
}