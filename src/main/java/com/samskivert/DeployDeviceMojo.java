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
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

/**
 * Goal which builds and installs the project on an iOS device.
 */
@Mojo(name="deploy-device", defaultPhase=LifecyclePhase.NONE,
    requiresDependencyResolution=ResolutionScope.COMPILE_PLUS_RUNTIME)
public class DeployDeviceMojo extends BuildDeviceMojo
{
    /**
     * Indicates whether the app should be launched after it is installed.
     */
    @Parameter(property="device.launch", defaultValue="true")
    public boolean launch;

    /**
     * Indicates where the build is output. By default, this is calculated using the provided
     * {@link #solution} and {@link #build}. E.g. {@code bin/iPhone/Debug}.
     */
    @Parameter(property="mtouch.output.dir")
    public File outputDir;

    public void execute () throws MojoExecutionException {
        super.execute();

        if (outputDir == null) {
            // this is where mtouch normally places binaries
            outputDir = new File(solution.getParent() + File.separator + "bin" +
                File.separator + DEVICE + File.separator + build);
        }

        // determine the name and path to our app directory
        String appName = resolveAppName();
        File appDir = new File(outputDir, appName);

        // install it to the device, if desired
        Commandline dcmd = newCommandline(mtouchPath.getPath());
        dcmd.createArg().setValue("--installdev=" + appDir.getAbsolutePath());
        getLog().debug("MTOUCH: " + dcmd);
        invoke("mtouch", dcmd);

        // launch it on the device, if desired
        if (launch) {
            // read in the .xcent file to obtain the app id
            File xcent = new File(outputDir, appName.replaceAll(".app$", ".xcent"));
            Map<String,String> info = parsePlist(xcent);
            String appId = info.get("application-identifier");
            if (appId == null) throw new MojoExecutionException(
                "Failed to extract app-id from " + xcent.getPath());
            // strip off the Apple gobbledygook from the front of the app id
            appId = appId.replaceAll("^[A-Z0-9]+\\.", "");

            // and run mtouch to launch the app
            Commandline lcmd = newCommandline(mtouchPath.getPath());
            lcmd.createArg().setValue("--launchdev=" + appId);
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
