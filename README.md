# TastyLoader

TastyLoader is a GitHub-based plugin loader for Bukkit/Spigot servers. It streamlines the process of managing and updating plugins by automatically downloading, loading, and unloading plugin JARs from a specified GitHub repository. This plugin loader will get updates and improvements in the future. Please note there may be bugs and issues.

## Table of Contents
- [Key Features](#key-features)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Installation](#installation)
- [Configuration](#configuration)
- [How It Works](#how-it-works)
- [Usage in Loadable Maven Plugin Project](#usage-in-loadable-maven-plugin-project)
- [License](#license)

## Key Features

- Automatic plugin download from GitHub repositories
- Priority-based loading order
- Dynamic plugin management (load/unload at runtime)
- Support for private repositories with GitHub token authentication
- Configurable through YAML file

## Getting Started

### Prerequisites

- Bukkit/Spigot server (Tested on 1.20.4)
- Java Development Kit (JDK) 8 or higher
- Kotlin runtime (included in the plugin JAR)

### Installation

1. Download the latest TastyLoader JAR from the releases page.
2. Place the TastyLoader JAR in your server's `plugins` folder.
3. Start your server to generate the default configuration file.
4. Configure the `config.yml` file in the `plugins/TastyLoader` directory.

## Configuration

TastyLoader uses a `config.yml` file for its configuration. Here's an example:

```yaml
repo: "https://raw.githubusercontent.com/yourusername/yourrepository/main"
github_token: "your_github_token_here"
loadables:
  example:
    jarName: "example"
    priority: 1
    enabled: true
```

- `repo`: The GitHub repository URL that stores your plugin JARs. Use the raw content URL and provide the correct branch.
- `github_token`: Your GitHub Personal Access Token for authentication when downloading plugins from private repositories.
- `loadables`: A list of all the plugins to be managed by TastyLoader.
  - `jarName`: The name of the plugin JAR file without the .jar extension.
  - `priority`: Sets the loading order (lower numbers load first).
  - `enabled`: Determines if TastyLoader should load this plugin.

## How It Works

1. **Plugin Download**: 
   - TastyLoader downloads plugin JARs from the specified GitHub repository.
   - If a GitHub token is provided in the config, it's used for authentication, allowing access to private repositories.

2. **Loading Process**: 
   - Plugins are loaded in order of their specified priority.
   - Downloaded JARs are stored in temporary files.
   - The plugin uses Bukkit's plugin manager to load and enable each JAR.

3. **Unloading Process**: 
   - On server shutdown, TastyLoader unloads all plugins it has loaded.
   - Temporary files are cleaned up.

4. **Error Handling**: 
   - TastyLoader logs errors for failed downloads or loading attempts.
   - It continues to load other plugins even if one fails.

5. **Dynamic Management**: 
   - The `deactivateSpecificPlugin` function allows for runtime unloading of specific plugins if needed.

## Usage in Loadable Maven Plugin Project

For plugin developers who want their plugins to be compatible with TastyLoader, ensure your plugin follows standard Bukkit plugin structure and is compiled as a JAR file.

### Maven Plugin Configuration

Add the following plugin configuration to your `pom.xml` file. This will automatically update your plugin JAR in the specified GitHub repository when you run `mvn install`.

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
                        <arg value="if not exist repo-dir ( git clone ${GITHUB_REPO} repo-dir ) else ( cd repo-dir &amp;&amp; git pull ${GITHUB_REPO} &amp;&amp; cd .. )"/>
                    </exec>
    
                    <delete file="repo-dir/${project.build.finalName}.jar"/>
    
                    <copy file="target/${project.build.finalName}.jar" tofile="repo-dir/${project.build.finalName}.jar"/>
    
                    <exec executable="cmd">
                        <arg value="/c"/>
                        <arg value="cd repo-dir &amp;&amp; git rm --cached ${project.build.finalName}.jar &amp;&amp; git add ${project.build.finalName}.jar"/>
                    </exec>
    
                    <exec executable="cmd">
                        <arg value="/c"/>
                        <arg value="cd repo-dir &amp;&amp; git commit -m &quot;Updated jar&quot; &amp;&amp; git push ${GITHUB_REPO}"/>
                    </exec>
                </target>
            </configuration>
        </execution>
    </executions>
</plugin>
```

**Note**: This will only run when you execute `mvn install`. You can change this to your preferred command by modifying the `<phase>` tag.

### Setup SSH for GitHub (One-time setup)

If you haven't set up SSH for GitHub already, follow these steps:

1. Enable OpenSSH if it's disabled:
   - Search for "Services" in your Windows search and run as admin.
   - Find "OpenSSH Authentication Agent".
   - Right-click > Properties > Startup type - set to "Automatic".
   - Click "Apply" and "OK", then open this menu again and click "Start".
   - Apply and close.

2. Create an `.ssh` folder in your user directory (e.g., `C:\Users\user\.ssh`).

3. Ensure the OpenSSH folder is in your environment path (typically `C:\Windows\System32\OpenSSH`).

4. Create the SSH agent:
   ```
   ssh-agent
   ssh-add C:/User/user/.ssh/gitssh
   ```

5. Generate an SSH key:
   ```
   ssh-keygen -t rsa -b 4096 -C "your_email@example.com"
   ```

6. Copy your public key:
   ```
   powershell
   Get-Content C:\Users\user\.ssh\gitssh.pub
   ```

7. Add the key to your GitHub account:
   - Go to [GitHub SSH Settings](https://github.com/settings/keys)
   - Click "New SSH Key"
   - Give it a name and paste the key, then save

8. Add GitHub to known hosts:
   ```
   ssh-keyscan github.com >> C:/Users/user/.ssh/known_hosts
   ```

9. Test the connection:
   ```
   ssh -T git@github.com
   ```
   If you see "Hi username! You've successfully authenticated...", you're good to go.

10. If it still doesn't work, create a `config` file in your `.ssh` folder with the following content:
    ```
    Host github.com
       HostName github.com
       User git
       IdentityFile ~/.ssh/gitssh
       IdentitiesOnly yes
    ```

## License

This project is licensed under the MIT License. See the LICENSE file for more details.
