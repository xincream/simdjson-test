module org.simdjson {
    requires jdk.incubator.vector;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;
    requires jmh.core;
    requires jmh.generator.annprocess;
    requires jdk.unsupported;

    exports org.test.jmh_generated;
}