/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zm.rmqsplugins.definitions;

import java.util.Arrays;
import java.util.Map;

import com.zm.rmqsplugins.interfaces.Generatable;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.StringUtils;

/**
 *
 * @author zmiller
 */
public class MethodDefinition extends BaseDefinition implements Generatable {
    public String name;
    public String result;
    public PropertyDefinition[] params;
    public String[] exceptions;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void validate(Map<String, ModelDefinition> models, Map<String, ExceptionDefinition> exceptions) throws MojoExecutionException {
        if(name == null) {
            throw new MojoExecutionException("Method name cannot be null");
        }

        // Result must reference a model
        if(result != null && !models.containsKey(result)) {
            throw new MojoExecutionException(String.format(
                    "Model (%s) has not been defined", result
            ));
        }

        // Result must not be a standard java Object
        if(result != null && !(models.get(result).javaType == null && models.get(result).javaClass == null)) {
            throw new MojoExecutionException(String.format(
                    "Method (%s) cannot return a standard Java object, return values must be custom objects defined in definition.json", name
            ));
        }

        // Throwable must reference an exception
        if(this.exceptions != null) {
            for (String t : this.exceptions) {
                if (exceptions != null && !exceptions.containsKey(t)) {
                    throw new MojoExecutionException(t + " is not defined in the exceptions block");
                }
            }
        }
    
        for(PropertyDefinition p : params) {
            p.validate(models, exceptions);
        }
    }

    @Override
    public String generate(String pkg, Map<String, ModelDefinition> models, Map<String, ExceptionDefinition> exceptions, String base) throws MojoExecutionException {
        
        // Create the method signature
        StringBuilder sb = new StringBuilder();
        String returnType = result == null ? "void" : ModelDefinition.getType(models.get(result));
        sb.append(String.format("public %s %s(", returnType, name));
        
        // Create the method parameters
        if(params != null) {
            for(PropertyDefinition p : params) {
                sb.append(p.generate(pkg, models, exceptions, base));
                sb.append(", ");
            }
            
            // Remove the last ", "
            if(params.length > 0) {
                sb.delete(sb.length() - 2, sb.length());
            }
        }
        
        // Close the method definition
        sb.append(String.format(") throws TimeoutException, IOException, ServiceUnavailableException, ServiceException, ClientException"
                + (this.exceptions != null && this.exceptions.length > 0 ? ", " + StringUtils.join(this.exceptions, ",") : "")
                + ";\n", returnType, name));

        return sb.toString();
    }

    public String generateMethodBody(String pkg, Map<String, ModelDefinition> models, Map<String, ExceptionDefinition> exceptions, String base) throws MojoExecutionException {

        String[] allExceptions = new String[5];
        if(this.exceptions != null && this.exceptions.length != 0) {
            allExceptions = Arrays.copyOf(this.exceptions, this.exceptions.length + 5);
        }

        allExceptions[allExceptions.length - 5] = "IOException";
        allExceptions[allExceptions.length - 4] = "TimeoutException";
        allExceptions[allExceptions.length - 3] = "ServiceUnavailableException";
        allExceptions[allExceptions.length - 2] = "ServiceException";
        allExceptions[allExceptions.length - 1] = "ClientException";

        String throwsStmt = StringUtils.join(allExceptions, ", ");
        String catchStmt = StringUtils.join(allExceptions, " | ");
        String returnType = result == null ? "void" : ModelDefinition.getType(models.get(result));
        String returnStmt = (result == null ? "" : "return ") +
                String.format("this.call(\"%s\", json, %s.class);\n",
                name, ModelDefinition.getResultClass(pkg, models.get(result)));

        // Create the method parameters and param JsonTree
        StringBuilder params = new StringBuilder();
        StringBuilder paramJsonTree = new StringBuilder();
        if(this.params != null) {
            for(int i = 0; i < this.params.length; i++)  {

                PropertyDefinition p = this.params[i];
                params.append(p.generate(pkg, models, exceptions, base));
                params.append(", ");
                paramJsonTree.append((i != 0 ? "            " : "") + String.format("json.add(gson.toJsonTree(%s));\n", p.name));
            }

            // Remove the last ", "
            if(this.params.length > 0) {
                params.delete(params.length() - 2, params.length());
            }
        }

        if(paramJsonTree.length() == 0) {
            paramJsonTree.append("\n");
        }

        return String.format(
                "    @Override\n" +
                "    public %s %s(%s) throws %s {\n" +
                "        try {\n" +
                "            Gson gson = new Gson();\n" +
                "            JsonArray json = new JsonArray();\n" +
                "            %s" +
                "            %s" +
                "        } catch (%s e) {\n" +
                "            throw e;\n" +
                "        } catch (Throwable t) { \n" +
                "            throw new ClientException(\"Unknown exception thrown: \" + t.getMessage(), t);\n" +
                "        }\n" +
                "    }\n",
                returnType, name, params.toString(), throwsStmt, paramJsonTree.toString(), returnStmt, catchStmt);
    }
}
