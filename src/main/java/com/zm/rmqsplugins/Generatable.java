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
public interface Generatable {
    public String generate(String pkg, Map<String, ModelDefinition> models, String base) throws MojoExecutionException;;
}
