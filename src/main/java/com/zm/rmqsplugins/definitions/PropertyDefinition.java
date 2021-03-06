/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zm.rmqsplugins.definitions;

import java.util.Map;

import com.zm.rmqsplugins.interfaces.Generatable;
import org.apache.maven.plugin.MojoExecutionException;

/**
 *
 * @author zmiller
 */
public class PropertyDefinition extends BaseDefinition implements Generatable {
    public String name;
    public String ref;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void validate(Map<String, ModelDefinition> models, Map<String, ExceptionDefinition> exceptions) throws MojoExecutionException {
        
        if(name == null || ref == null) {
            throw new MojoExecutionException("Property has null name or ref");
        }
        
        if(!models.containsKey(ref)) {
            throw new MojoExecutionException(String.format(
                    "Model (%s) has not been defined", ref
            ));
        }
    }

    @Override
    public String generate(String pkg, Map<String, ModelDefinition> models, Map<String, ExceptionDefinition> exceptions, String base) throws MojoExecutionException {
        return String.format("%s %s", ModelDefinition.getType(models.get(ref)), name);
    }
}
