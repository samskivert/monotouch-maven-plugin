//
// monotouch-maven-plugin - builds and deploys MonoTouch projects
// http://github.com/samskivert/monotouch-maven-plugin/blob/master/LICENSE

package com.samskivert;

import org.apache.maven.plugins.annotations.Parameter;

public abstract class BindingsMojo extends MonoTouchMojo
{
    /**
     * The the build profile to use when building bindings.
     */
    @Parameter(property="bindings.build", defaultValue="Debug")
    public String build;

    protected static final String DEVICE = "iPhone";
}
