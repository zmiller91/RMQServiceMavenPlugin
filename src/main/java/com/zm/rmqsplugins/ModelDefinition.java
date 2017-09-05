/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zm.rmqsplugins;

import com.google.inject.internal.util.Lists;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.maven.plugin.MojoExecutionException;

/**
 *
 * @author zmiller
 */
public class ModelDefinition extends BaseGenerator implements Definition {
    public String name;
    public PropertyDefinition[] properties;
    public String javaPackage;
    public String javaType;
    public Set<String> imports;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void validate(Map<String, ModelDefinition> models) throws MojoExecutionException {
        
        // Name is required
        if(name == null) {
            throw new MojoExecutionException("Model name not set"); 
        }
        
        // Cant be a custom class and a java class
        if(properties != null && (javaPackage != null || javaType != null)) {
            throw new MojoExecutionException(String.format(
                    "Model (%s) cannot have a (properties and (javaPackage or javaType)) propety", name));
        }
        
        // Property must reference a model
        if(properties != null) {
            for(PropertyDefinition pd : properties) {
                pd.validate(models);
            }
        }
    }

    @Override
    public String generate(String pkg, Map<String, ModelDefinition> models, String base) throws MojoExecutionException {
        if(properties != null) {
            
            Set<String> localImports = new HashSet<>();
            if(imports != null) {
                localImports.addAll(imports);
            }
            List<String> props = new ArrayList<>();
            List<String> setters = new ArrayList<>();
            List<String> getters = new ArrayList<>();
            
            for(PropertyDefinition pd : properties) {
                
                String type = getType(models.get(pd.ref));
                props.add(createProperty(pd.name, type));
                setters.add(createSetter(pd.name, type));
                getters.add(createGetter(pd.name, type));
                
                // Create import statement
                ModelDefinition model = models.get(pd.ref);
                String modelImport = getPackage(model);
                if(modelImport != null) {
                    localImports.add(createImportStatement(modelImport));
                    if(model.imports != null) {
                        for(String extra : model.imports) {
                            localImports.add(createImportStatement(extra));
                        }
                    }
                }
            }
            
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("package %s;\n\n", pkg));
            appendAll(sb, Lists.newArrayList(localImports), "", "\n");
            sb.append(String.format("\npublic class %s {\n", this.name));
            appendAll(sb, props, "    ", "\n");
            appendAll(sb, setters, "    ", "\n");
            appendAll(sb, getters, "    ", "\n");
            sb.append("}");
            
            return sb.toString();
        }
        
        return null;
    }
    
    public static String getType(ModelDefinition model) {
        return model.javaType != null ? model.javaType : model.name;
    }
    
    public static String getPackage(ModelDefinition model) {
        return model.javaPackage != null ? model.javaPackage : null;
    }
}
