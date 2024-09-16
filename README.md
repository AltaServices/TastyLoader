## TastyLoader
### What is TastyLoader & Introduction
TastyLoader is a github based plugin loader for maven plugin projects, you need to have a repository that will store all your plugin jars, it will automatically update the plugin's jar on compile and load it into the server
you shouldn't put your plugin jars in the server plugins folder, only the TastyLoader jar, TastyLoader will make sure to load your plugins automatically.

### Config and basic setup
`config.yml` looks like that:
```yml
repo: "https://raw.githubusercontent.com/Tc554/loader-releases/main"
loadables:
  example:
    jarName: "example"
    priority: 1
    enabled: true
```
"repo" is the repository that will store all your plugin jars, make sure to use github's raw content and provide the correct branch.
"loadables" is a list of all the loadable files.
"jarName" is the jar name of the plugin, the stored jar name in the repository without the .jar at the end.
"priority" priority is to set the loading order, higher priority = load faster.
"enabled" is to set if you want the loader to load this plugin or not.

### Setup SSH for your github (one time only, doesnt need to if you have SSH for github set up already!)
1. Enable OpenSSH if its disabled, search for "Services" in your windows search and run as admin, find OpenSSH Authentication Agent, then, Right Click > Properties > Startup type - set to "Automatic" then click "Apply" and "OK" the open this menu again and click on "Start" then you can apply and close.
2. Go to your user folder, example "C:\Users\user" and create a new folder named ".ssh"
3. Make sure to have open ssh folder in your env path, most likely to be this folder: "C:\Windows\System32\OpenSSH"
4. Create the ssh file `ssh-agent`, `ssh-add C:/User/user/.ssh/gitssh`
5. Open your cmd and generate an SSH: `ssh-keygen -t rsa -b 4096 -C "your_email@example.com"`, run `powershell` in your cmd and then run `Get-Content C:\Users\user\.ssh\gitssh.pub` (this will copy your key) go to [GitHub SSH Settings](https://github.com/settings/keys) and click "New SSH Key" give it a name and paste the key then save it
6. Save it to known hosts `ssh-keyscan github.com >> C:/Users/user/.ssh/known_hosts`
7. Test if it works `ssh -T git@github.com` if you got this response `Hi username! You've successfully authenticated...` you are good to go!

### Usage in loadable maven plugin project
#### ! THIS WILL ONLY RUN WHEN YOU DO "mvn install" ! - Can be changed to your preferred command in the <phase></phases> tag.
Maven plugin to use in loadable plugins. Change the "GITHUB_REPO" if needed! (this is the repo that has all your loadable jars)
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-antrun-plugin</artifactId>
    <version>1.8</version>
    <executions>
        <execution>
            <phase>install</phase>
            <goals>
                <goal>run</goal>
            </goals>
            <configuration>
                <target>
                    <!-- Change loader releases repository if needed -->
                    <property name="GITHUB_REPO" value="git@github.com:Tc554/loader-releases"/>

                    <delete dir="repo-dir"/>
    
                    <exec executable="cmd">
                        <arg value="/c"/>
                        <arg value="if not exist repo-dir ( git clone ${GITHUB_REPO} ) else ( cd repo-dir &amp;&amp; git pull ${GITHUB_REPO} &amp;&amp; cd .. )"/>
                    </exec>
    
                    <copy file="target/${project.build.finalName}.jar" tofile="repo-dir/${project.build.finalName}.jar"/>
    
                    <exec executable="cmd">
                        <arg value="/c"/>
                        <arg value="cd repo-dir &amp;&amp; git add . &amp;&amp; git commit -m &quot;Updated jar&quot; &amp;&amp; git push ${GITHUB_REPO}"/>
                    </exec>
                </target>
            </configuration>
        </execution>
    </executions>
</plugin>
```
