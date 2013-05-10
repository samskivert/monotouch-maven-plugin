package com.samskivert;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.codehaus.plexus.util.FileUtils;

/**
 * A package phase which copies a DLL output by an ios bindings compilation to a maven artifact
 * with dll packaging type. This relies on some extra maven configuration in the {@code
 * plexus/components.xml} file.
 * <p>IMPORTANT: requires the plugin to be registered as an extension so it can run in place
 * of jar for packaging phase</p>
 */
@Mojo(name="ios-package-bindings", defaultPhase=LifecyclePhase.PACKAGE,
    requiresDependencyResolution=ResolutionScope.COMPILE)
public class PackageBindingsMojo extends BindingsMojo
{
    /** The name of the generated library. By default, this is the name of the solution or csproj
     * with a {@code .dll} extension. */
    @Parameter(property="libName")
    private String libName;

    public void execute () throws MojoExecutionException {
        // figure out the output name
        String outputName = libName != null ? libName :
            solution.getName().replaceAll("\\.sln$", "").replaceAll("\\.csproj$", "");

        String buildDir = _project.getBuild().getDirectory();

        // Find the output dll file, assume it's under the specified build, e.g. bin/Debug
        File output = new File(new File(buildDir, build), outputName + ".dll");
        if (!output.exists()) throw new MojoExecutionException(
            "Bindings output file not found: " + output);
        getLog().debug("Package bindings output: " + output);

        // setup the artifact file
        File artifact = new File(buildDir, _project.getBuild().getFinalName() + ".dll");
        getLog().debug("Package bingings artifact: " + artifact);

        // copy the artifact
        try {
            FileUtils.copyFile(output, artifact);
        } catch (IOException ex) {
            throw new MojoExecutionException("", ex);
        }

        // package complete!
        _project.getArtifact().setFile(artifact);
    }
}
