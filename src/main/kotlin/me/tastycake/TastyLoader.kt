package me.tastycake

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.internal.wait
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.serialization.ConfigurationSerialization
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLClassLoader
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * TastyLoader: A plugin loader for developer quality of life
 * @author Cupoftea & Tasty Cake
 * @date 9/17/2024
 */


class TastyLoader : JavaPlugin() {
    companion object {
        @JvmStatic
        private var instance: TastyLoader? = null
    }

    private lateinit var githubToken: String
    private val managedPlugins = ConcurrentHashMap<String, File>()

    override fun onEnable() {
        instance = this
        ConfigurationSerialization.registerClass(Loadable::class.java)

        // Load configuration
        saveDefaultConfig()
        val config: FileConfiguration = this.config

        githubToken = config.getString("github_token") ?: ""

        val repoUrl = config.getString("repo") ?: throw IllegalStateException("Repository URL is not specified in config")
        val pluginConfigs = parsePluginConfigs(config)

        // Sort plugins by priority and load them
        val sortedPluginConfigs = pluginConfigs.values.sortedBy { it.priority }

        for (pluginConfig in sortedPluginConfigs) {
            if (pluginConfig.enable) {
                try {
                    fetchAndInitializePlugin(repoUrl, pluginConfig.jarName)
                        .thenAccept { /* Plugin loaded successfully */ }
                        .exceptionally { e ->
                            logger.severe("Failed to load plugin ${pluginConfig.jarName}: ${e.message}")
                            null
                        }
                } catch (e: Exception) {
                    logger.severe("Failed to initiate loading of plugin ${pluginConfig.jarName}: ${e.message}")
                }
            }
        }
    }

    // Fetch plugin JAR from repository and initialize it
    private fun fetchAndInitializePlugin(repoUrl: String, jarName: String): CompletableFuture<Unit> {
        return fetchPluginJar(repoUrl, jarName)
            .thenCompose { jarBytes ->
                val tempFile = createTemporaryJarFile(jarName, jarBytes)
                managedPlugins[jarName] = tempFile
                initializePlugin(tempFile)
            }
    }

    // Download plugin JAR file from the repository
    private fun fetchPluginJar(repoUrl: String, jarName: String): CompletableFuture<ByteArray> {
        val future = CompletableFuture<ByteArray>()
        object : BukkitRunnable() {
            override fun run() {
                val url = URL("$repoUrl/$jarName.jar")
                val connection = url.openConnection() as HttpURLConnection
                if (githubToken.isNotEmpty()) {
                    val authHeader = "Bearer $githubToken"
                    connection.setRequestProperty("Authorization", authHeader)
                }
                connection.connectTimeout = 10000
                connection.readTimeout = 10000

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val bytes = connection.inputStream.use { it.readBytes() }
                    logger.info("Downloaded $jarName.jar")
                    future.complete(bytes)
                } else {
                    logger.severe("HTTP error code: $responseCode")
                    throw IllegalStateException("Failed to download file: HTTP error code $responseCode")
                }
            }
        }.runTaskAsynchronously(this)

        return future
    }

    // Create a temporary file for the downloaded JAR
    private fun createTemporaryJarFile(jarName: String, jarBytes: ByteArray): File {
        val tempFile = Files.createTempFile("tastyloader_", "_$jarName.jar").toFile()
        tempFile.deleteOnExit()
        Files.write(tempFile.toPath(), jarBytes)
        return tempFile
    }

    // Load and enable the plugin from the JAR file
    private fun initializePlugin(file: File) : CompletableFuture<Unit> {
        val future = CompletableFuture<Unit>()

        object : BukkitRunnable() {
            override fun run() {
                try {
                    val plugin = server.pluginManager.loadPlugin(file)
                    if (plugin != null) {
                        future.complete(server.pluginManager.enablePlugin(plugin))
                        logger.info("Loaded and enabled ${plugin.name}")
                    } else {
                        logger.warning("Failed to load plugin from file ${file.name}")
                    }
                } catch (e: Exception) {
                    logger.severe("Error loading plugin from file ${file.name}: ${e.message}")
                }
            }
        }.runTaskAsynchronously(this)

        return future
    }

    // Unload and disable a specific plugin
    private fun deactivatePlugin(pluginName: String) {
        val plugin = server.pluginManager.getPlugin(pluginName)
        if (plugin != null) {
            server.pluginManager.disablePlugin(plugin)
            try {
                val classLoader = plugin.javaClass.classLoader
                if (classLoader is URLClassLoader) {
                    classLoader.close()
                }
                logger.info("Unloaded plugin $pluginName")
            } catch (e: Exception) {
                logger.severe("Error unloading plugin $pluginName: ${e.message}")
            }
        } else {
            logger.warning("Plugin $pluginName not found or already unloaded")
        }
        managedPlugins.remove(pluginName)?.delete()
    }

    // Public method to unload a specific plugin
    fun deactivateSpecificPlugin(pluginName: String) {
        deactivatePlugin(pluginName)
    }

    override fun onDisable() {
        // Clean up temporary files
        val folder = File("$dataFolder/loaded")
        folder.deleteRecursively()

        // Unload all managed plugins
        val config: FileConfiguration = this.config
        val pluginConfigs = parsePluginConfigs(config)

        for (pluginConfig in pluginConfigs.values) {
            if (pluginConfig.enable) {
                deactivatePlugin(pluginConfig.jarName)
            }
        }

        managedPlugins.values.forEach { it.delete() }
        managedPlugins.clear()
    }

    // Parse plugin configurations from the config file
    private fun parsePluginConfigs(config: FileConfiguration): Map<String, Loadable> {
        val loadablesSection = config.getConfigurationSection("loadables")
        return loadablesSection?.getKeys(false)?.associateWith { key ->
            val section = loadablesSection.getConfigurationSection(key)
            Loadable(
                jarName = section?.getString("jarName") ?: "",
                priority = section?.getInt("priority") ?: 0,
                enable = section?.getBoolean("enabled") ?: true
            )
        } ?: emptyMap()
    }
}