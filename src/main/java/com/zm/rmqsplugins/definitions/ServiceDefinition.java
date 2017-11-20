/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zm.rmqsplugins.definitions;

import java.nio.file.Paths;
import java.util.Map;

import com.zm.rmqsplugins.client.Client;
import com.zm.rmqsplugins.configuration.Configuration;
import com.zm.rmqsplugins.interfaces.Generatable;
import org.apache.maven.plugin.MojoExecutionException;

/**
 *
 * @author zmiller
 */
public class ServiceDefinition extends BaseDefinition implements Generatable {
    public String packageName;
    public ConfigurationDefinition configuration;
    public ApiDefinition api;
    public ModelDefinition[] models;
    public ExceptionDefinition[] exceptions;
    public String name;

    @Override
    public String getName() {
        return packageName;
    }

    @Override
    public void validate(Map<String, ModelDefinition> models, Map<String, ExceptionDefinition> exceptions) throws MojoExecutionException {
        if(configuration == null) {
            throw new MojoExecutionException("Configuration cannot be null");
        }
    }

    @Override
    public String generate(String pkg, Map<String, ModelDefinition> models, Map<String, ExceptionDefinition> exceptions, String base)
            throws MojoExecutionException {
        
        // Generate the models
        for(String m : models.keySet()) {
            ModelDefinition model = models.get(m);
            String basePath = Paths.get(base, "model").toString();
            model.validate(models, exceptions);
            String body = model.generate(pkg + ".model", models, exceptions, basePath);
            if(body != null && !body.isEmpty()) {
                writeJavaFile(basePath, model.getName(), body);
            }
        }

        // Generate the Exceptions
        for(String e : exceptions.keySet()) {
            ExceptionDefinition exception = exceptions.get(e);
            String basePath = Paths.get(base, "exception").toString();
            exception.validate(models, exceptions);
            String body = exception.generate(pkg + ".exception", models, exceptions, basePath);
            if(body != null && !body.isEmpty()) {
                writeJavaFile(basePath, exception.getName(), body);
            }
        }
        
        // Generate the API
        api.setServiceName(name);
        api.validate(models, exceptions);
        String body = api.generate(pkg, models, exceptions, base);
        if(body != null) {
            writeJavaFile(base, api.getName(), body);
        }

        // Generate the configuration
        Configuration config = new Configuration(name, configuration);
        config.validate(models, exceptions);
        String cfg = config.generate(pkg + ".configuration", models, exceptions, base);
        if(cfg != null) {
            writeJavaFile(base + "/configuration", name + "Configuration", cfg);
        }

        // Generate the client
        Client client = new Client(name, api);
        client.validate(models, exceptions);
        String c = client.generate(pkg, models, exceptions, base);
        if(c != null) {
            writeJavaFile(base, name + "Client", c);
        }
        return null;
    }
}
