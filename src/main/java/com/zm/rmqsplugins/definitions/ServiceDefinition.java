/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zm.rmqsplugins.definitions;

import com.zm.rmqsplugins.base.BaseDefinition;
import java.nio.file.Paths;
import java.util.Map;
import org.apache.maven.plugin.MojoExecutionException;

/**
 *
 * @author zmiller
 */
public class ServiceDefinition extends BaseDefinition {
    public String packageName;
    public ApiDefinition api;
    public ModelDefinition[] models;

    @Override
    public String getName() {
        return packageName;
    }

    @Override
    public void validate(Map<String, ModelDefinition> models) throws MojoExecutionException {}

    @Override
    public String generate(String pkg, Map<String, ModelDefinition> models, String base) 
            throws MojoExecutionException {
        
        // Generate the models
        for(String m : models.keySet()) {
            ModelDefinition model = models.get(m);
            String basePath = Paths.get(base, "model").toString();
            model.validate(models);
            String body = model.generate(pkg + ".model", models, basePath);
            if(body != null) {
                writeJavaFile(basePath, model.getName(), body);
            }
        }
        
        // Generate the API
        api.validate(models);
        String body = api.generate(pkg, models, base);
        if(body != null) {
            writeJavaFile(base, api.getName(), body);
        }
        
        return null;
    }
}
