//
// monotouch-maven-plugin - builds and deploys MonoTouch projects
// http://github.com/samskivert/monotouch-maven-plugin/blob/master/LICENSE

package com.samskivert;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.codehaus.plexus.util.Expand;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

/**
 * Goal which builds and runs the project on the iOS simulator.
 *
 * @requiresDependencyResolution package
 * @goal deploy-sim
 * @phase integration-test
 */
public class BuildSimMojo extends AbstractMojo
{
    /**
     * Location of {@code mdtool} binary.
     * @parameter expression="${mdtool.path}" default-value="/Applications/MonoDevelop.app/Contents/MacOS/mdtool"
     */
    public File mdtoolPath;

    /**
     * Location of {@code mtouch} binary.
     * @parameter expression="${mtouch.path}" default-value="/Developer/MonoTouch/usr/bin/mtouch"
     */
    public File mtouchPath;

    /**
     * Location of {@code ios-sim} binary. If this is set, it will be used to launch the simulator
     * in place of {@code mtouch}. {@code ios-sim} sends logs to stdout and {@code mtouch} does
     * not, so it is recommended to use it.
     * @parameter expression="${iossim.path}"
     */
    public File iossimPath;

    /**
     * The the build profile to use when building for the simulator.
     * @parameter expression="${simulator.build}" default-value="Debug"
     */
    public String build;

    /**
     * The path to the project's {@code sln} file. For example {@code foo.sln} (for a solution that
     * is in the top-level project directory).
     * @parameter expression="${solution}"
     */
    public File solution;

    /**
     * The name of the directory that contains your built app, for example: {@code foo.app}. This
     * defaults to {@link #solution} with {@code sln} switched to {@code app}, but if your app is
     * special, you can override it.
     */
    public String appName;

    /**
     * Which device family to use (iphone or ipad).
     * @parameter expression="${simulator.family}" default-value="iphone"
     */
    public String family;

    /**
     * Whether to use the Retina or non-Retina simulator. Currently only supported when using
     * {@code ios-sim}.
     * @parameter expression="${simulator.retina}" default-value="true"
     */
    public boolean retina;

    /**
     * Whether to use the tall (iPhone 5) or normal (iPhone 4, etc.) simulator mode. Currently only
     * supported when using {@code ios-sim}.
     * @parameter expression="${simulator.tall}" default-value="true"
     */
    public boolean tall;

    public void execute () throws MojoExecutionException {
        requireParameter("solution", solution);

        // create the command line for building the app
        Commandline bcmd = new Commandline(mdtoolPath.getPath());
        bcmd.createArgument().setValue("build");
        bcmd.createArgument().setValue("-c:\"" + build + "|" + DEVICE + "\"");
        bcmd.createArgument().setValue(solution.getPath());

        // log our full build command for great debuggery
        getLog().debug("BUILD: " + bcmd);

        // now invoke the build process
        invoke("mdtool", bcmd);

        // determine the name and path to our app directory
        String appName = this.appName;
        if (appName == null) appName = solution.getName().replaceAll(".sln$", ".app");
        File appDir = new File(_project.getBuild().getDirectory() + File.separator + DEVICE +
                               File.separator + build + File.separator + appName);

        // next invoke ios-sim or mdtouch to launch the simulator
        if (iossimPath != null) {
            Commandline dcmd = new Commandline(iossimPath.getPath());
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
            Commandline dcmd = new Commandline(mtouchPath.getPath());
            dcmd.createArgument().setValue("--launchsim=" + appDir.getAbsolutePath());
            dcmd.createArgument().setValue("--device=" + family);
            // TODO: how to support?
            // if (retina) dcmd.createArgument().setValue("--retina");
            // if (tall) dcmd.createArgument().setValue("--tall");
            getLog().debug("MTOUCH: " + dcmd);
            invoke("mtouch", dcmd);
        }
    }

    private void requireParameter (String name, Object ref) throws MojoExecutionException {
        if (ref == null) throw new MojoExecutionException("Missing required parameter: " + name);
    }

    private void invoke (String command, Commandline cli) throws MojoExecutionException {
        invoke(command, cli, new StreamConsumer() {
            public void consumeLine (String line) {
                getLog().info(line);
            }
        }, new StreamConsumer() {
            public void consumeLine (String line) {
                getLog().warn(line);
            }
        });
    }

    private void invoke (String command, Commandline cli, StreamConsumer stdout,
                         StreamConsumer stderr) throws MojoExecutionException {
        try {
            int rv = CommandLineUtils.executeCommandLine(cli, null, stdout, stderr);
            if (rv != 0) throw new MojoExecutionException(command + " failed; see above output.");
        } catch (CommandLineException clie) {
            throw new MojoExecutionException(command + " execution failed", clie);
        }
    }

    /** @parameter default-value="${project}" */
    private MavenProject _project;

    protected static final String DEVICE = "iPhoneSimulator";
}
