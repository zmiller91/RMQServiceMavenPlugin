/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zm.rmqsplugins.definitions;

import com.google.inject.internal.util.Lists;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.zm.rmqsplugins.interfaces.Generatable;
import org.apache.maven.plugin.MojoExecutionException;

/**
 *
 * @author zmiller
 */
public class ApiDefinition extends BaseDefinition implements Generatable {
    public MethodDefinition[] methods;
    private String serviceName;

    public void setServiceName(String name) {
        this.serviceName = name;
    }

    @Override
    public String getName() {
        return serviceName + "Api";
    }

    @Override
    public void validate(Map<String, ModelDefinition> models, Map<String, ExceptionDefinition> exceptions) throws MojoExecutionException {
        
        Set<String> methodNames = new HashSet<>();
        for(MethodDefinition md : methods) {
            if(methodNames.contains(md.name)) {
                throw new MojoExecutionException("Api has methods with the same name");
            }
            methodNames.add(md.name);
        }
        
        for(MethodDefinition m : methods) {
            m.validate(models, exceptions);
        }
    }

    @Override
    public String generate( String pkg, Map<String, ModelDefinition> models, Map<String, ExceptionDefinition> exceptions, String base) throws MojoExecutionException {
        StringBuilder sb = new StringBuilder();
        
        // Generate the imports
        Set<String> imports = new HashSet<String>();
        imports.add(createImportStatement(pkg + ".model.*"));
        for(ModelDefinition m : models.values()) {
            imports.addAll(m.getImports());
        }

        imports.add(createImportStatement("com.zm.rabbitmqservice.client.ClientException"));
        imports.add(createImportStatement("com.zm.rabbitmqservice.service.ServiceException"));
        imports.add(createImportStatement("com.zm.rabbitmqservice.service.ServiceUnavailableException"));
        imports.add(createImportStatement("java.util.concurrent.TimeoutException"));
        imports.add(createImportStatement("java.io.IOException"));
        if(exceptions != null && !exceptions.isEmpty()) {
            imports.add(createImportStatement(pkg + ".exception.*"));
            for (ExceptionDefinition e : exceptions.values()) {
                imports.addAll(e.getImports());
            }
        }
        
        // Build the package definition and import statements
        sb.append(String.format("package %s;\n\n", pkg));
        appendAll(sb, Lists.newArrayList(imports), "", "\n");
        
        // Create interface defnition
        sb.append(String.format("\npublic interface %s {", getName()));
        
        // Create method stubs
        sb.append("\n");
        for(MethodDefinition m : methods) {
            sb.append("    ");
            sb.append(m.generate(pkg, models, exceptions, base));
        }
        
        return sb.append("}").toString();
    }
}
