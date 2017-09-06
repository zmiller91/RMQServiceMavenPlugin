/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zm.rmqsplugins;

import com.google.inject.internal.util.Lists;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.maven.plugin.MojoExecutionException;

/**
 *
 * @author zmiller
 */
public class ApiDefinition extends BaseGenerator implements Definition {
    public String name;
    public MethodDefinition[] methods;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void validate(Map<String, ModelDefinition> models) throws MojoExecutionException {
        if(name == null) {
            throw new MojoExecutionException("Api name is null");
        }
        
        Set<String> methodNames = new HashSet<>();
        for(MethodDefinition md : methods) {
            if(methodNames.contains(md.name)) {
                throw new MojoExecutionException("Api has methods with the same name");
            }
            methodNames.add(md.name);
        }
        
        for(MethodDefinition m : methods) {
            m.validate(models);
        }
    }

    @Override
    public String generate(String pkg, Map<String, ModelDefinition> models, String base) throws MojoExecutionException {
        
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("package %s;\n\n", pkg));
        
        // Create import statements
        Set<String> imports = new HashSet<String>();
        imports.add(createImportStatement(pkg + ".model.*"));
        for(ModelDefinition m : models.values()) {
            imports.addAll(m.getImports());
        }
        
        appendAll(sb, Lists.newArrayList(imports), "", "\n");
        
        // Create interface defnition
        sb.append(String.format("\npublic interface %s {", name));
        
        // Create method stubs
        sb.append("\n");
        for(MethodDefinition m : methods) {
            sb.append("    ");
            sb.append(m.generate(pkg, models, base));
        }
        
        return sb.append("}").toString();
    }
}
