package me.tastycake

import org.bukkit.Bukkit
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.serialization.ConfigurationSerialization
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.net.URL
import java.nio.file.Files
import java.io.InputStream
import java.nio.file.StandardCopyOption
import java.net.URLClassLoader

/**
 * @author Cupoftea & Tasty Cake
 * @date 9/15/2024
 */


class TastyLoader : JavaPlugin() {
    companion object {
        @JvmStatic
        private var instance: TastyLoader? = null
    }

    override fun onEnable() {
        instance = this
        ConfigurationSerialization.registerClass(Loadable::class.java)

        saveDefaultConfig()
        val config: FileConfiguration = this.config

        val repo = config.getString("repo") ?: throw IllegalStateException("Repo Url is not specified in config")
        val loadables = getLoadablesFromConfig(config)

        val sortedLoadables = loadables.values.sortedBy { it.priority }

        for (loadable in sortedLoadables) {
            if (loadable.enable) {
                try {
                    downloadPlugin(repo, loadable.jarName)
                    loadPlugin(File(dataFolder, "${loadable.jarName}.jar"))
                } catch (e: Exception) {
                    logger.severe("Failed to load plugin ${loadable.jarName}: ${e.message}")
                }
            }
        }
    }

    private fun downloadPlugin(repo: String, jarName: String): File {
        val url = URL("$repo/$jarName.jar")
        val destination = File(dataFolder, "$jarName.jar")
        url.openStream().use { input: InputStream ->
            Files.copy(input, destination.toPath(), StandardCopyOption.REPLACE_EXISTING)
        }
        logger.info("Downloaded $jarName.jar")
        return destination
    }

    private fun loadPlugin(file: File) {
        try {
            val plugin = server.pluginManager.loadPlugin(file)
            if (plugin != null) {
                server.pluginManager.enablePlugin(plugin)
                logger.info("Loaded and enabled ${plugin.name}")
            } else {
                logger.warning("Failed to load plugin from file ${file.name}")
            }
        } catch (e: Exception) {
            logger.severe("Error loading plugin from file ${file.name}: ${e.message}")
        }
    }

    private fun unloadPlugin(pluginName: String) {
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
    }

    fun unloadSpecificPlugin(pluginName: String) {
        unloadPlugin(pluginName)
    }

    override fun onDisable() {
        val config: FileConfiguration = this.config
        val loadables = getLoadablesFromConfig(config)

        val sortedLoadables = loadables.values.sortedBy { it.priority }

        for (loadable in sortedLoadables) {
            if (loadable.enable) {
                unloadPlugin(loadable.jarName)
            }
        }
    }

    private fun getLoadablesFromConfig(config: FileConfiguration): Map<String, Loadable> {
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