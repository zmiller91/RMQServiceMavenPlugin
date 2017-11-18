/*
 * Copyright (C) 2017 zmiller
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.zm.rmqsplugins;

import com.zm.rmqsplugins.definitions.ExceptionDefinition;
import com.zm.rmqsplugins.definitions.ModelDefinition;
import com.zm.rmqsplugins.definitions.ServiceDefinition;
import com.zm.rmqsplugins.interfaces.Definition;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 *
 * @author zmiller
 */
@Mojo( name = "build-service")
public class BuildService extends AbstractMojo {

    @Parameter(readonly = true, defaultValue = "${project}")
    private MavenProject project;
    
    @Parameter( property = "build-service.definition", defaultValue = "src/main/resources/definition.json" )
    private String definition;
    
    @Parameter(defaultValue="${project.build.directory}/generated-sources/rmqsplugin", required=true)
    private File targetFolder;
    
    private final Gson gson = new Gson();
    
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {

            // Create the service definition from the definition.json
            String root = project.getBasedir().getAbsolutePath();
            Path definitionPath = Paths.get(root, definition);
            ServiceDefinition service = deserialize(definitionPath, ServiceDefinition.class);

            // Create a model cache
            Map<String, ModelDefinition> models = new HashMap<>();
            collectModels(service.models, models);

            // Create an exception cache
            Map<String, ExceptionDefinition> exceptions = new HashMap<>();
            collectExceptions(service.exceptions, exceptions);

            // Create the output path
            String sources = targetFolder.getAbsolutePath();
            String packagePath = service.packageName.replaceAll("\\.", "/");
            Path outputPath = Paths.get(sources, packagePath);

            // Run the plugin
            service.generate(service.packageName, models, exceptions, outputPath.toString());
        }
        
        catch(MojoExecutionException e) {
            throw e;
        }
        
        catch(Exception e) {
            throw new MojoExecutionException("Execution fialed", e);
        }  
    }
    
    private void collectModels(ModelDefinition[] models, Map<String, ModelDefinition> map) throws MojoExecutionException {
        for(ModelDefinition md : models) {
            if(map.containsKey(md.name)) {
                throw new MojoExecutionException("Duplicate model name found: " + md.getName());
            }
            map.put(md.getName(), md);
        }
    }

    private void collectExceptions(ExceptionDefinition[] exceptions, Map<String, ExceptionDefinition> map) throws MojoExecutionException {
        for(ExceptionDefinition ed : exceptions) {
            if(map.containsKey(ed.name)) {
                throw new MojoExecutionException("Duplicate exception name found: " + ed.getName());
            }
            map.put(ed.getName(), ed);
        }
    }
    
    private <T> T deserialize(Path path, Class<T> clazz) 
            throws MojoExecutionException {
        try {
            JsonReader jsonReader = new JsonReader(new FileReader(path.toString()));
            return gson.fromJson(jsonReader, clazz);
        } 
        catch (FileNotFoundException e) {
            throw new MojoExecutionException("Could not find definition file", e);
        }
    }
}
