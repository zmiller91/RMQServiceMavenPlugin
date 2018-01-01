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
        imports.add(createImportStatement(pkg + ".configuration." + serviceName + "Configuration"));
        imports.add(createImportStatement(pkg + ".model.*"));
        for(ModelDefinition m : models.values()) {
            imports.addAll(m.getImports());
        }

        imports.add(createImportStatement("com.google.gson.Gson"));
        imports.add(createImportStatement("com.google.gson.JsonArray"));
        imports.add(createImportStatement("com.zm.rabbitmqservice.client.RMQClient"));
        imports.add(createImportStatement("com.zm.rabbitmqservice.client.ClientException"));
        imports.add(createImportStatement("com.zm.rabbitmqservice.service.ServiceException"));
        imports.add(createImportStatement("com.zm.rabbitmqservice.service.ServiceUnavailableException"));
        imports.add(createImportStatement("java.util.concurrent.TimeoutException"));
        imports.add(createImportStatement("java.util.concurrent.ExecutorService"));
        imports.add(createImportStatement("java.io.IOException"));
        if(exceptions != null && !exceptions.isEmpty()) {
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
                "    public %sClient(%sConfiguration.Host host, %sConfiguration.Channel channel) {\n" +
                "        super(host.getValue(), channel.getValue());\n" +
                "    }\n",
                serviceName, api.getName(), serviceName, serviceName, serviceName, serviceName, serviceName, serviceName));

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
