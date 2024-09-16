# TastyLoader

TastyLoader is a GitHub-based plugin loader for Bukkit/Spigot servers. It streamlines the process of managing and updating plugins by automatically downloading, loading, and unloading plugin JARs from a specified GitHub repository. This plugin loader will get updates and improvements in the future. Please note there may be bugs and issues.

## Table of Contents
- [Key Features](#key-features)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Installation](#installation)
- [Configuration](#configuration)
- [Environment Variables](#environment-variables)
- [How It Works](#how-it-works)
- [Usage in Loadable Maven Plugin Project](#usage-in-loadable-maven-plugin-project)
- [License](#license)

## Getting Started

### Prerequisites

- Bukkit/Spigot server (Tested on 1.20.4)
- Java Development Kit (JDK) 8 or higher
- Access to set environment variables on your server

### Installation

1. Download the TastyLoader JAR file from the [releases page](https://github.com/vapournet/TastyLoader/releases).
2. Place the TastyLoader JAR in your server's `plugins` folder.
3. Set up the required environment variable (see [Environment Variables](#environment-variables) section).
4. Start your server to generate the default configuration file.

## Configuration

TastyLoader uses a `config.yml` file for its configuration. Here's an example:

```yaml
repo: "https://raw.githubusercontent.com/YourGitHubUsername/your-plugin-repo/main"
loadables:
  example-plugin:
    jarName: "ExamplePlugin"
    priority: 1
    enabled: true
  another-plugin:
    jarName: "AnotherPlugin"
    priority: 2
    enabled: false
```

- `repo`: The GitHub repository URL that stores your plugin JARs. Use the raw content URL.
- `loadables`: A list of all the plugins to be managed by TastyLoader.
  - `jarName`: The name of the plugin JAR file without the .jar extension.
  - `priority`: Sets the loading order (higher priority = load faster).
  - `enabled`: Determines if TastyLoader should load this plugin.

## Environment Variables

TastyLoader uses environment variables for sensitive information:

- `GITHUB_TOKEN`: Your GitHub Personal Access Token for authentication when downloading plugins from private repositories.

Make sure to set this environment variable before starting your Minecraft server.

## How It Works

1. **Plugin Download**: 
   - TastyLoader downloads plugin JARs from the specified GitHub repository.
   - If a GitHub token is provided via the `GITHUB_TOKEN` environment variable, it's used for authentication, allowing access to private repositories.

2. **Loading Process**: 
   - Plugins are loaded in order of their specified priority.
   - Downloaded JARs are stored in the `plugins/TastyLoader/loaded` directory.
   - The plugin uses Bukkit's plugin manager to load and enable each JAR.

3. **Unloading Process**: 
   - On server shutdown, TastyLoader unloads all plugins it has loaded.
   - The `loaded` directory is cleaned up to remove downloaded JARs.

4. **Error Handling**: 
   - TastyLoader logs errors for failed downloads or loading attempts.
   - It continues to load other plugins even if one fails.

5. **Dynamic Management**: 
   - The `unloadSpecificPlugin` function allows for runtime unloading of specific plugins if needed.

## Usage in Loadable Maven Plugin Project

For plugin developers who want their plugins to be compatible with TastyLoader, ensure your plugin follows standard Bukkit plugin structure and is compiled as a JAR file.

## License

This project is licensed under the MIT License. See the LICENSE file for more details.