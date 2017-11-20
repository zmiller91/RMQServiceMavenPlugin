package com.zm.rmqsplugins.configuration;

import com.google.inject.internal.util.Lists;
import com.zm.rmqsplugins.definitions.*;
import com.zm.rmqsplugins.interfaces.Generatable;
import org.apache.maven.plugin.MojoExecutionException;

import java.util.Map;

public class Configuration extends BaseDefinition implements Generatable {

    private String serviceName;
    private ConfigurationDefinition config;
    public Configuration(String serviceName, ConfigurationDefinition config) {
        this.serviceName = serviceName;
        this.config = config;
    }
    @Override
    public String getName() {
        return null;
    }

    @Override
    public void validate(Map<String, ModelDefinition> models, Map<String, ExceptionDefinition> exceptions) throws MojoExecutionException {
        config.validate(models, exceptions);
    }

    @Override
    public String generate(String pkg, Map<String, ModelDefinition> models, Map<String, ExceptionDefinition> exceptions, String base) throws MojoExecutionException {

        // Build the package definition and import statements
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("package %s;\n\n", pkg));
        sb.append(String.format(
                "\npublic class %sConfiguration {\n" +
                "    public enum Channel {\n" +
                "        TEST(\"%s\"),\n" +
                "        PROD(\"%s\");\n\n" +

                "        private final String value;\n" +
                "        private Channel(String value) {\n" +
                "                this.value = value;\n" +
                "            }\n\n" +

                "        public String getValue() {\n" +
                "            return value;\n" +
                "        }\n" +
                "    }\n\n" +
                "    public enum Host {\n" +
                "        TEST(\"%s\"),\n" +
                "        PROD(\"%s\");\n\n" +

                "        private final String value;\n" +
                "        private Host(String value) {\n" +
                "                this.value = value;\n" +
                "            }\n\n" +

                "        public String getValue() {\n" +
                "            return value;\n" +
                "        }\n" +
                "    }\n" +
                "}",
                serviceName, config.channels.test, config.channels.prod, config.hosts.test, config.hosts.prod));

        return sb.toString();
    }
}
