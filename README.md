# monotouch-maven-plugin

This Maven plugin invokes `mdtool` and `mtouch` to build and deploy a MonoTouch project to the iOS
simulator or to a device. It was created to make life easier for [PlayN] projects, but there's
nothing particularly PlayN-specific about it.

## Usage

The plugin provides two main goals, one to build and deploy to the simulator and one to build and
deploy to a device. The only required configuration for the plugin is the solution (or csproj).
Adding this in your <build><plugins> section:

    <plugin>
      <groupId>com.samskivert</groupId>
      <artifactId>monotouch-maven-plugin</artifactId>
      <version>1.0</version>
      <configuration>
        <solution>YOUR.sln</solution>
      </configuration>
    </plugin>

will allow you to deploy to device with this simple command line:

    mvn monotouch:deploy-device

or to the simulator

    mvn monotouch:deploy-sim

Additional configuration will probably be needed such as the device family or launch orientation.
This can be specified in the configuration for the plugin or overridden on the command line as
needed.

For a complete description of all the options, look at the source or in the maven help:

    mvn help:describe -Dplugin=com.samskivert:monotouch-maven-plugin -Ddetail

This plugin works nicely with [ikvm-maven-plugin], which converts your Java code to a `.dll` during
the `package` phase. Thus you can compile your Java code, convert it via IKVM, and then build and
test/deploy your MonoTouch project in a single Maven call, where X is deploy-sim or deploy-device:

    mvn package monotouch:deploy-X

NOTE: in older versions, binding to a lifecycle phase was recommended. It is not necessary to do,
but if desired, you can add something like this to the plugin tag:

    <executions>
      <execution>
        <phase>integration-test</phase>
        <goals>
          <goal>deploy-sim</goal>
        </goals>
      </execution>
    </executions>

This will run the plugin on *every* integration test or anything after it in the lifecycle, so
you'll almost certainly want to put this in a manually activated profile.

By default, launching to the simulator is done using `mtouch`, but you can also use the `ios-sim`
app, which is nicer because it sends stdout from your iOS app to stdout on the console (which
`mtouch` does not). You can enable the use of `ios-sim` by providing the path to the `ios-sim`
binary in `<configuration>`:

    <configuration>
      <iossimPath>/usr/local/bin/ios-sim</iossimPath>
    </configuration>

## License

monotouch-maven-plugin is released under the New BSD License, which can be found in the [LICENSE]
file.

[PlayN]: http://code.google.com/p/playn
[ikvm-maven-plugin]: https://github.com/samskivert/ikvm-maven-plugin/
[LICENSE]: https://github.com/samskivert/monotuch-maven-plugin/blob/master/LICENSE
