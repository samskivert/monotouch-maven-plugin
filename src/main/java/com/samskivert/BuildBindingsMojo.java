package com.samskivert;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

/**
 * A compile phase which uses {@code mdtool} to compile an ios bindings project. A {@code DLL}
 * file is generated for later consumption by the {@link PackageBindingsMojo}.
 * <p>IMPORTANT: requires the plugin to be registered as an extension so it can run in place
 * of javac for the compile phase</p>
 */
@Mojo(name="ios-compile-bindings", defaultPhase=LifecyclePhase.COMPILE,
    requiresDependencyResolution=ResolutionScope.COMPILE)
public class BuildBindingsMojo extends BindingsMojo
{
    public void execute () throws MojoExecutionException {
        build(build, DEVICE);
    }
}
