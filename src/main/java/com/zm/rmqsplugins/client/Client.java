package com.zm.rmqsplugins.client;

import com.google.inject.internal.util.Lists;
import com.zm.rmqsplugins.definitions.*;
import com.zm.rmqsplugins.interfaces.Generatable;
import org.apache.maven.plugin.MojoExecutionException;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Client extends BaseDefinition implements Generatable {

    private ApiDefinition api;
    private String serviceName;
    public Client(String serviceName, ApiDefinition api) {
        this.serviceName = serviceName;
        this.api = api;
    }

    @Override
    public String generate(String pkg, Map<String, ModelDefinition> models, Map<String, ExceptionDefinition> exceptions, String base) throws MojoExecutionException {

        // Generate the imports
        Set<String> imports = new HashSet<String>();
        imports.add(createImportStatement(pkg + ".model.*"));
        for(ModelDefinition m : models.values()) {
            imports.addAll(m.getImports());
        }

        imports.add(createImportStatement("com.google.gson.Gson"));
        imports.add(createImportStatement("com.google.gson.JsonArray"));
        imports.add(createImportStatement("com.zm.rabbitmqservice.ClientException"));
        imports.add(createImportStatement("com.zm.rabbitmqservice.ServiceException"));
        imports.add(createImportStatement("com.zm.rabbitmqservice.RMQClient"));
        if(exceptions != null) {
            imports.add(createImportStatement(pkg + ".exception.*"));
            for (ExceptionDefinition e : exceptions.values()) {
                imports.addAll(e.getImports());
            }
        }

        // Build the package definition and import statements
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("package %s;\n\n", pkg));
        appendAll(sb, Lists.newArrayList(imports), "", "\n");
        sb.append(String.format(
                "\npublic class %sClient extends RMQClient implements %s {\n" +
                "    public %sClient(String host, String queue, int executorPoolSize) throws ClientException {\n" +
                "        super(host, queue, executorPoolSize);\n" +
                "    }\n",
                serviceName, api.getName(), serviceName));

        // Create method stubs
        for(MethodDefinition m : api.methods) {
            sb.append("\n");
            sb.append(m.generateMethodBody(pkg, models, exceptions, base));
        }

        return sb.append("}").toString();
    }

    @Override
    public String getName() {
        return serviceName + "Client";
    }

    @Override
    public void validate(Map<String, ModelDefinition> models, Map<String, ExceptionDefinition> exceptions) throws MojoExecutionException {

    }
}
