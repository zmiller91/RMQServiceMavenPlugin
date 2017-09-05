/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zm.rmqsplugins;

import java.util.Map;
import org.apache.maven.plugin.MojoExecutionException;

/**
 *
 * @author zmiller
 */
public class MethodDefinition extends BaseGenerator implements Definition{
    public String name;
    public String result;
    public PropertyDefinition[] params;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void validate(Map<String, ModelDefinition> models) throws MojoExecutionException {
        if(name == null) {
            throw new MojoExecutionException("Method name cannot be null");
        }
        
        if(result != null && !models.containsKey(result)) {
            throw new MojoExecutionException(String.format(
                    "Model (%s) has not been defined", result
            ));
        }
    
        for(PropertyDefinition p : params) {
            p.validate(models);
        }
    }

    @Override
    public String generate(String pkg, Map<String, ModelDefinition> models, String base) throws MojoExecutionException {
        
        StringBuilder sb = new StringBuilder();
        String returnType = result == null ? "void" 
                : ModelDefinition.getType(models.get(result));
        
        sb.append(String.format("public %s %s(", returnType, name));
        // Create the method parameters
        if(params != null) {
            for(PropertyDefinition p : params) {
                System.out.println(p.getName());
                ModelDefinition model = models.get(p.ref);
                String type = ModelDefinition.getType(model);
                sb.append(String.format("%s %s, ", type, p.name));
            }
            
            // Remove the last ", "
            if(params.length > 0) {
                sb.delete(sb.length() - 2, sb.length());
            }
        }
        
        sb.append(String.format(");\n", returnType, name));
        return sb.toString();
    }
}
