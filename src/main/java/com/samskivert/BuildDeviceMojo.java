//
// monotouch-maven-plugin - builds and deploys MonoTouch projects
// http://github.com/samskivert/monotouch-maven-plugin/blob/master/LICENSE

package com.samskivert;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.plexus.util.cli.Commandline;

import org.apache.maven.plugin.MojoExecutionException;

/**
 * Goal which builds and installs the project on an iOS device.
 *
 * @requiresDependencyResolution package
 * @goal deploy-device
 * @phase install
 */
public class BuildDeviceMojo extends MonoTouchMojo
{
    /**
     * The the build profile to use when building for the device.
     * @parameter expression="${device.build}" default-value="Release"
     */
    public String build;

    /**
     * Indicates whether the app should be installed after the IPA is built. This defaults to true,
     * but this configuration exists to allow you to disable this behavior for things like
     * automated builds.
     * @parameter expression="${device.install}" default-value="true"
     */
    public boolean install;

    /**
     * Indicates whether the app should be launched after it is installed. This is only used if
     * {@link #install} is true. If {@link #install} is false, this is implicitly false as well.
     * @parameter expression="${device.launch}" default-value="true"
     */
    public boolean launch;

    public void execute () throws MojoExecutionException {
        requireParameter("solution", solution);

        // create the command line for building the app
        Commandline bcmd = new Commandline(mdtoolPath.getPath());
        bcmd.createArgument().setValue("build");
        bcmd.createArgument().setValue("-c:" + build + "|" + DEVICE);
        bcmd.createArgument().setValue(solution.getPath());

        // log our full build command for great debuggery
        getLog().debug("BUILD: " + bcmd);

        // now invoke the build process
        invoke("mdtool", bcmd);

        // make a note of the mdtool build output directory
        File buildDir = new File(_project.getBuild().getDirectory() +
                                 File.separator + DEVICE + File.separator + build);

        // determine the name and path to our app directory
        String appName = this.appName;
        if (appName == null) appName = solution.getName().replaceAll(".sln$", ".app");
        File appDir = new File(buildDir, appName);

        // install it to the device, if desired
        if (install) {
            Commandline dcmd = new Commandline(mtouchPath.getPath());
            dcmd.createArgument().setValue("--installdev=" + appDir.getAbsolutePath());
            getLog().debug("MTOUCH: " + dcmd);
            invoke("mtouch", dcmd);
        }

        // launch it on the device, if desired
        if (install && launch) {
            // read in the .xcent file to obtain the app id
            File xcent = new File(buildDir, appName.replaceAll(".app$", ".xcent"));
            Map<String,String> info = parsePlist(xcent);
            String appId = info.get("application-identifier");
            if (appId == null) throw new MojoExecutionException(
                "Failed to extract app-id from " + xcent.getPath());
            // strip off the Apple gobbledygook from the front of the app id
            appId = appId.replaceAll("^[A-Z0-9]+\\.", "");

            // and run mtouch to launch the app
            Commandline lcmd = new Commandline(mtouchPath.getPath());
            lcmd.createArgument().setValue("--launchdev=" + appId);
            getLog().debug("MTOUCH: " + lcmd);
            invoke("mtouch", lcmd);
        }
    }

    // this only matches simple <key>/<string> pairs, but that suffices for our purposes
    protected Map<String,String> parsePlist (File file) throws MojoExecutionException {
        try {
            Map<String,String> info = new HashMap<String,String>();
            Pattern pat = Pattern.compile("<(.*)>(.*)</(.*)>");
            BufferedReader in = new BufferedReader(new FileReader(file));
            String line;
            String prevKey = null;
            while ((line = in.readLine()) != null) {
                Matcher m = pat.matcher(line.trim());
                if (m.matches()) {
                    String type = m.group(1);
                    String value = m.group(2);
                    if (prevKey == null && type.equals("key")) prevKey = value;
                    else if (prevKey != null && type.equals("string")) info.put(prevKey, value);
                    else prevKey = null;
                }
            }
            return info;
        } catch (IOException ioe) {
            throw new MojoExecutionException("Failed to parse " + file.getPath(), ioe);
        }
    }

    protected static final String DEVICE = "iPhone";
}
