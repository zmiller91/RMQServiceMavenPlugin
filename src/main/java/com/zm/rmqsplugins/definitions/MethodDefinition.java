/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zm.rmqsplugins.definitions;

import com.zm.rmqsplugins.base.BaseDefinition;
import java.util.Map;
import org.apache.maven.plugin.MojoExecutionException;

/**
 *
 * @author zmiller
 */
public class MethodDefinition extends BaseDefinition{
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
        
        // Create the method signature
        StringBuilder sb = new StringBuilder();
        String returnType = result == null ? "void" : ModelDefinition.getType(models.get(result));
        sb.append(String.format("public %s %s(", returnType, name));
        
        // Create the method parameters
        if(params != null) {
            for(PropertyDefinition p : params) {
                sb.append(p.generate(pkg, models, base));
                sb.append(", ");
            }
            
            // Remove the last ", "
            if(params.length > 0) {
                sb.delete(sb.length() - 2, sb.length());
            }
        }
        
        // Close the method definition
        sb.append(String.format(");\n", returnType, name));
        return sb.toString();
    }
}
