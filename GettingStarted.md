How do I use ivymeetsvn?

# What you'll need #
  * Apache Ant 1.7 [Ant Home](http://ant.apache.org/)
  * (Apache) Ivy 1.4.1 (wasn't released under apache) [Ivy Home](http://ant.apache.org/ivy/)
  * IvyMeetSvn :)
  * A Subversion repository accessible via WebDav

# The IvyConf #

**An Example IvyConf**
```
<ivyconf>
        <conf defaultResolver="default"/>
        <typedef name="svn" classname="org.ivymeet.svn.SvnResolver"/>
        <include url="${ivy.default.conf.dir}/ivyconf-public.xml"/>
        <include url="${ivy.default.conf.dir}/ivyconf-shared.xml"/>
        <include url="${ivy.default.conf.dir}/ivyconf-local.xml"/>

        <resolvers>
            <chain name="main" returnFirst="true">
              <resolve ref="local"/>
              <svn name="artifacts" username="XXXX" password="XXXXX">
                <ivy pattern="http://svn/artifacts/[organization]/[module]/ivy-[revision].xml"/>
                <artifact pattern="http://svn/artifacts/[organization]/[module]/[type]s/[artifact]-[revision].[ext]"/>
              </svn>
            </chain>
        </resolvers>

        <include url="${ivy.default.conf.dir}/ivyconf-default-chain.xml"/>
</ivyconf>
```

You can also use the new 'secure' feature if you dont want to publish your svn login in your ivyconf.xml. The idea being you might have multiple svns you want to use, so, it takes a name to keep login information stored seperately. It stores it via base64 in your home directory.. so, not really that safe, but, enough if you keep a tight ship.

```
  <svn name="something" secure="sommethingelse">
```

# The Build.xml #
```
        <target name="-check-for-ivy">
                <available property="have.ivy" resource="fr/jayasoft/ivy/ant/antlib.xml" />
        </target>

        <!-- create ivy taskdef if ivy is available -->
        <target name="-ivy-define" depends="-check-for-ivy" unless="have.ivy">
                <taskdef resource="fr/jayasoft/ivy/ant/antlib.xml" uri="antlib:fr.jayasoft.ivy.ant">
                        <classpath>
                                <fileset dir="./${master.tools.ivy.path}">
                                        <include name="*.jar" />
                                        <include name="lib/*.jar" />
                                </fileset>
                        </classpath>
                </taskdef>

                <ivy:configure file="${basedir}/ivyconf.xml" />
        </target>

        <!-- Runs the ivy resolver -->
        <target name="resolve.dependencies" depends="-ivy-define">
                <ivy:resolve conf="default" />
                <ivy:cachefileset setid="ivy.cachefileset" conf="default" />

                <mkdir dir="${local.scratch.path}" />
                <ivy:report todir="${local.scratch.path}" />
        </target>

        <target name="-getVersionNumber"> 
                <!-- get the version number and generate the release ivy file -->
                <input addProperty="deliver.version.number">Deliver Version Number : </input>
        </target>

         <target name="release" depends="compile,-getVersionNumber" description="Creates the project's distribution">
                <tstamp>
                        <format property="build.date" pattern="yyyyMMddHHmmss" locale="en" />
                </tstamp>
                <ivy:publish resolver="artifacts" artifactspattern="${local.scratch.path}/${master.project.build.deliverables-path}/[artifact].[ext]" pubdate="${build.date}" pubrevision="${deliver.version.number}" overwrite="true"/>
        </target>
```

Note that the resolver is artifacts on the release target.. this means that Ivy will publish to our svn resolver. Ivy can publish to most any resolver that supports the put operation.