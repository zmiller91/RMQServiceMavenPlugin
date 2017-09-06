/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zm.rmqsplugins.definitions;

import com.zm.rmqsplugins.base.BaseDefinition;
import com.zm.rmqsplugins.base.Importable;
import com.google.inject.internal.util.Lists;
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
public class ModelDefinition extends BaseDefinition implements Importable {
    public String name;
    public PropertyDefinition[] properties;
    public String javaImport;
    public String javaType;
    public Set<String> dependencyImports;

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
        if(properties != null && (javaImport != null || javaType != null)) {
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
        
        StringBuilder sb = new StringBuilder();
        if(properties != null) {
            List<String> props = new ArrayList<>();
            List<String> setters = new ArrayList<>();
            List<String> getters = new ArrayList<>();
            Set<String> localImports = new HashSet<>();
            
            // Generate imports, create getters and setters
            localImports.addAll(getImports());
            for(PropertyDefinition pd : properties) {
                String type = getType(models.get(pd.ref));
                props.add("private " + pd.generate(pkg, models, base) + ";");
                setters.add(createSetter(pd.name, type));
                getters.add(createGetter(pd.name, type));
                localImports.addAll(models.get(pd.ref).getImports());
            }
            
            // Build the model
            sb.append(String.format("package %s;\n\n", pkg));
            appendAll(sb, Lists.newArrayList(localImports), "", "\n");
            sb.append(String.format("\npublic class %s {\n", this.name));
            appendAll(sb, props, "    ", "\n");
            appendAll(sb, setters, "    ", "\n");
            appendAll(sb, getters, "    ", "\n");
            sb.append("}");
        }
        
        return sb.toString();
    }
    
    public static String getType(ModelDefinition model) {
        return model.javaType != null ? model.javaType : model.name;
    }
    
    public static String getJavaImport(ModelDefinition model) {
        return model.javaImport != null ? model.javaImport : null;
    }

    @Override
    public Set<String> getImports() {
        Set<String> ret = new HashSet<>();
        if(javaImport != null) {
            ret.add(createImportStatement(javaImport));
        }
        
        if(dependencyImports != null) {
            for(String s : dependencyImports) {
            ret.add(createImportStatement(s));
            }
        }
        
        return ret;
    }
}
