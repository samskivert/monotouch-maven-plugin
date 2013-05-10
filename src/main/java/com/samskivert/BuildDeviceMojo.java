//
// monotouch-maven-plugin - builds and deploys MonoTouch projects
// http://github.com/samskivert/monotouch-maven-plugin/blob/master/LICENSE

package com.samskivert;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

/**
 * Goal which builds the project for an iOS device.
 */
@Mojo(name="build-device", defaultPhase=LifecyclePhase.NONE,
    requiresDependencyResolution=ResolutionScope.COMPILE_PLUS_RUNTIME)
public class BuildDeviceMojo extends MonoTouchMojo
{
    /**
     * The the build profile to use when building for the device.
     */
    @Parameter(property="device.build", defaultValue="Release")
    public String build;

    public void execute () throws MojoExecutionException {
        build(build, DEVICE);
    }

    protected static final String DEVICE = "iPhone";
}
