package com.zm.rmqsplugins.definitions;

import com.google.inject.internal.util.Lists;
import com.zm.rmqsplugins.interfaces.Importable;
import org.apache.maven.plugin.MojoExecutionException;

import java.util.*;

public class ExceptionDefinition extends BaseDefinition implements Importable {
    public String name;
    public PropertyDefinition[] properties;
    public String javaClass;
    public String javaType;
    public Set<String> imports;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void validate(Map<String, ModelDefinition> models, Map<String, ExceptionDefinition> exceptions) throws MojoExecutionException {

        // Name is required
        if(name == null) {
            throw new MojoExecutionException("Exception name not set");
        }

        // Cant be a custom class and a java class
        if(properties != null && (javaClass != null || javaType != null)) {
            throw new MojoExecutionException(String.format(
                    "Exception (%s) cannot have a (properties and (javaClass or javaType)) property", name));
        }

        // Property must reference a model
        if(properties != null) {
            for(PropertyDefinition pd : properties) {
                pd.validate(models, exceptions);
            }
        }
    }

    @Override
    public String generate(String pkg, Map<String, ModelDefinition> models, Map<String, ExceptionDefinition> exceptions, String base) throws MojoExecutionException {

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
                props.add("private " + pd.generate(pkg, models, exceptions, base) + ";");
                setters.add(createSetter(pd.name, type));
                getters.add(createGetter(pd.name, type));
                localImports.addAll(models.get(pd.ref).getImports());
            }

            // Build the model
            sb.append(String.format("package %s;\n\n", pkg));
            appendAll(sb, Lists.newArrayList(localImports), "", "\n");
            sb.append(String.format("\npublic class %s extends Exception {\n", this.name));
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
        return model.javaClass != null ? model.javaClass : null;
    }

    @Override
    public Set<String> getImports() {
        Set<String> ret = new HashSet<>();
        if(javaClass != null) {
            ret.add(createImportStatement(javaClass));
        }

        if(imports != null) {
            for(String s : imports) {
                ret.add(createImportStatement(s));
            }
        }

        return ret;
    }
}
