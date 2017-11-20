/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zm.rmqsplugins.definitions;

import com.zm.rmqsplugins.definitions.ModelDefinition;
import com.zm.rmqsplugins.interfaces.Definition;
import com.zm.rmqsplugins.interfaces.Definition;
import com.zm.rmqsplugins.interfaces.Generatable;
import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import org.apache.maven.plugin.MojoExecutionException;

/**
 *
 * @author zmiller
 */
public abstract class BaseDefinition implements Definition {
    
    
    protected void writeJavaFile(String path, String name, String body) throws MojoExecutionException {
        try {
            File f = new File(path);
            if(!f.exists() && !f.mkdirs()) {
                throw new MojoExecutionException("Could not create directory " + path);
            }

            String file = Paths.get(path, name + ".java").toString();
            f = new File(file);
            f.delete();
            if(!f.createNewFile()) {
                throw new MojoExecutionException("Could not create file " + file);
            }

            PrintWriter out = new PrintWriter(file);
            out.print(body);
            out.close();
        }
        catch(Exception e) {
                throw new MojoExecutionException("Failed to write java file.", e);
        }
    }
    
    protected String createImportStatement(String pkg) {
        return String.format("import %s;", pkg);
    }
    
    protected String createProperty(String name, String type) {
        return String.format("private %s %s;", type, name);
    }
    
    protected String createSetter(String name, String type) {
        return String.format(
                "public void set%s(%s %s) {" + "this.%s = %s;}", 
                capitalize(name), type, name, name, name);
    }
    
    protected String createGetter(String name, String type) {
        return String.format(
                "public %s get%s() {" + "return this.%s;}", 
                type, capitalize(name), name);
    }
    
    protected String capitalize(String input) {
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }
    
    protected void appendAll(StringBuilder sb, List<String> strings, String prefix, String suffix) {
        for(String s : strings) {
            sb.append(prefix);
            sb.append(s);
            sb.append(suffix);
        }
    }
}
