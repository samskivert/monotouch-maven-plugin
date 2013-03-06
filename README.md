# monotouch-maven-plugin

This Maven plugin invokes `mdtool` and `mtouch` to build and deploy a MonoTouch project to the iOS
simulator or to a device. It was created to make life easier for [PlayN] projects, but there's
nothing particularly PlayN-specific about it.

## Usage

The plugin provides two goals, one two build and deploy to the simulator and one to build and
deploy to a device. You should bind these phases to the `integration-test` and `install` phases
respectively:

    <plugin>
      <groupId>com.samskivert</groupId>
      <artifactId>monotouch-maven-plugin</artifactId>
      <version>1.0</version>
      <configuration>
        <solution>YOUR.sln</solution>
      </configuration>
      <executions>
        <execution>
          <id>deploy-sim</id>
          <phase>integration-test</phase>
          <goals><goal>deploy-sim</goal></goals>
        </execution>
        <execution>
          <id>deploy-device</id>
          <phase>install</phase>
          <goals><goal>deploy-device</goal></goals>
        </execution>
      </executions>
    </plugin>

By binding to these phases, this plugin can be made to work nicely with [ikvm-maven-plugin] which
converts your Java code to a `.dll` during the `package` phase (which runs prior to the
aforementioned phases), thus you can compile your Java code, convert it via IKVM, and then build
and test/deploy your MonoTouch project in a single Maven call to `mvn integration-test` or `mvn
install`.

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
