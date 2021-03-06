/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zm.rmqsplugins.interfaces;

import com.zm.rmqsplugins.definitions.ExceptionDefinition;
import com.zm.rmqsplugins.definitions.ModelDefinition;
import java.util.Map;
import org.apache.maven.plugin.MojoExecutionException;

/**
 *
 * @author zmiller
 */
public interface Generatable {
    String generate(String pkg, Map<String, ModelDefinition> models, Map<String, ExceptionDefinition> exceptions, String base) throws MojoExecutionException ;
}
