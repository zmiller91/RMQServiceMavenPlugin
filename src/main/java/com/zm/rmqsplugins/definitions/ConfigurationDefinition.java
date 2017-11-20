package com.zm.rmqsplugins.definitions;

import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.MojoExecutionException;

import java.util.Map;

public class ConfigurationDefinition extends BaseDefinition {

    public Channel channels;
    public Host hosts;

    @Override
    public String getName() {
        return null;
    }

    @Override
    public void validate(Map<String, ModelDefinition> models, Map<String, ExceptionDefinition> exceptions) throws MojoExecutionException {
        if ( channels == null || hosts == null ){
            throw new MojoExecutionException("'channels' and 'hosts' configuration cannot be null");
        }

        if(channels.prod == null || channels.test == null) {
            throw new MojoExecutionException("'prod' and 'test' channel configuration cannot be null");
        }

        if(hosts.prod == null || hosts.test == null) {
            throw new MojoExecutionException("'prod' and 'test' host configuration cannot be null");
        }
    }

    public class Channel {
        public String test;
        public String prod;
    }

    public class Host {
        public String test;
        public String prod;
    }
}
