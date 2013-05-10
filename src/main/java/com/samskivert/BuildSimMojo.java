//
// monotouch-maven-plugin - builds and deploys MonoTouch projects
// http://github.com/samskivert/monotouch-maven-plugin/blob/master/LICENSE

package com.samskivert;

import java.io.File;

import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

/**
 * Goal which builds and runs the project on the iOS simulator.
 */
@Mojo(name="deploy-sim", defaultPhase=LifecyclePhase.NONE,
    requiresDependencyResolution=ResolutionScope.TEST)
public class BuildSimMojo extends MonoTouchMojo
{
    /**
     * Location of {@code ios-sim} binary. If this is set, it will be used to launch the simulator
     * in place of {@code mtouch}. {@code ios-sim} sends logs to stdout and {@code mtouch} does
     * not, so it is recommended to use it.
     */
    @Parameter(property="iossim.path")
    public File iossimPath;

    /**
     * The the build profile to use when building for the simulator.
     */
    @Parameter(defaultValue="Debug", property="simulator.build")
    public String build;

    /**
     * Which device family to use (iphone or ipad).
     */
    @Parameter(defaultValue="iphone", property="simulator.family")
    public String family;

    /**
     * Whether to use the Retina or non-Retina simulator. Currently only supported when using
     * {@code ios-sim}.
     */
    @Parameter(property="simulator.retina", defaultValue="true")
    public boolean retina;

    /**
     * Whether to use the tall (iPhone 5) or normal (iPhone 4, etc.) simulator mode. Currently only
     * supported when using {@code ios-sim}.
     */
    @Parameter(property="simulator.tall", defaultValue="true")
    public boolean tall;

    public void execute () throws MojoExecutionException {
        // create the command line for building the app
        Commandline bcmd = newCommandline(mdtoolPath.getPath());
        bcmd.createArgument().setValue("build");
        bcmd.createArgument().setValue("-c:" + build + "|" + DEVICE);
        bcmd.createArgument().setValue(solution.getPath());

        // log our full build command for great debuggery
        getLog().debug("BUILD: " + bcmd);

        // now invoke the build process
        invoke("mdtool", bcmd);

        // determine the name and path to our app directory
        String appName = resolveAppName();
        File appDir = new File(_project.getBuild().getDirectory() + File.separator + DEVICE +
                               File.separator + build + File.separator + appName);

        // next invoke ios-sim or mdtouch to launch the simulator
        if (iossimPath != null) {
            Commandline dcmd = newCommandline(iossimPath.getPath());
            dcmd.createArgument().setValue("launch");
            dcmd.createArgument().setValue(appDir.getAbsolutePath());
            dcmd.createArgument().setValue("--family");
            dcmd.createArgument().setValue(family);
            if (retina) dcmd.createArgument().setValue("--retina");
            if (tall) dcmd.createArgument().setValue("--tall");
            getLog().debug("IOSSIM: " + dcmd);
            // the ios-sim output is wonky, so we clean it up here
            invoke("iossim", dcmd, new StreamConsumer() {
                public void consumeLine (String line) {
                    if (line.trim().length() > 0) getLog().info(line);
                }
            }, new StreamConsumer() {
                public void consumeLine (String line) {
                    if (line.trim().length() > 0) getLog().info(line);
                }
            });

        } else {
            Commandline dcmd = newCommandline(mtouchPath.getPath());
            dcmd.createArgument().setValue("--launchsim=" + appDir.getAbsolutePath());
            dcmd.createArgument().setValue("--device=" + family);
            // TODO: how to support?
            // if (retina) dcmd.createArgument().setValue("--retina");
            // if (tall) dcmd.createArgument().setValue("--tall");
            getLog().debug("MTOUCH: " + dcmd);
            invoke("mtouch", dcmd);
        }
    }

    protected static final String DEVICE = "iPhoneSimulator";
}
