//
// monotouch-maven-plugin - builds and deploys MonoTouch projects
// http://github.com/samskivert/monotouch-maven-plugin/blob/master/LICENSE

package com.samskivert;

import java.io.File;

import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * Mojo bits shared between simulator and device deployers.
 */
public abstract class MonoTouchMojo extends AbstractMojo
{
    /**
     * Location of {@code mdtool} binary.
     */
    @Parameter(defaultValue="/Applications/Xamarin Studio.app/Contents/MacOS/mdtool",
            property="mdtool.path")
    public File mdtoolPath;

    /**
     * Location of {@code mtouch} binary.
     */
    @Parameter(defaultValue="/Developer/MonoTouch/usr/bin/mtouch", property="mtouch.path")
    public File mtouchPath;

    /**
     * The path to the project's {@code sln} or {@code csproj}. For example {@code foo.sln}
     * (for a solution that is in the top-level project directory).
     */
    @Parameter(property="solution", alias="csproj", required=true)
    public File solution;

    /**
     * The name of the directory that contains your built app, for example: {@code foo.app}. This
     * defaults to {@link #solution} with {@code sln} switched to {@code app}, but if your app is
     * special, you can override it.
     */
    @Parameter(property="appName")
    public String appName;

    protected Commandline newCommandline (String exe) {
        return new Commandline(StringUtils.quoteAndEscape(exe, '"'));
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

    protected String resolveAppName () {
        return appName != null ? appName :
            solution.getName().replaceAll(".sln$", ".app").replaceAll(".csproj$", ".app");
    }

    @Parameter(defaultValue="${project}")
    protected MavenProject _project;
}
