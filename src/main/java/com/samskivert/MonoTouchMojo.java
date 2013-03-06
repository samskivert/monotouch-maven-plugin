//
// monotouch-maven-plugin - builds and deploys MonoTouch projects
// http://github.com/samskivert/monotouch-maven-plugin/blob/master/LICENSE

package com.samskivert;

import java.io.File;

import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

/**
 * Mojo bits shared between simulator and device deployers.
 */
public abstract class MonoTouchMojo extends AbstractMojo
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

    protected void requireParameter (String name, Object ref) throws MojoExecutionException {
        if (ref == null) throw new MojoExecutionException("Missing required parameter: " + name);
    }

    protected void invoke (String command, Commandline cli) throws MojoExecutionException {
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

    protected void invoke (String command, Commandline cli, StreamConsumer stdout,
                           StreamConsumer stderr) throws MojoExecutionException {
        try {
            int rv = CommandLineUtils.executeCommandLine(cli, null, stdout, stderr);
            if (rv != 0) throw new MojoExecutionException(command + " failed; see above output.");
        } catch (CommandLineException clie) {
            throw new MojoExecutionException(command + " execution failed", clie);
        }
    }

    /** @parameter default-value="${project}" */
    protected MavenProject _project;
}
