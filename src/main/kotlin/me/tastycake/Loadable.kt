package me.tastycake

import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.SerializableAs

/**
 * TastyLoader: A plugin loader for developer quality of life
 * @author Cupoftea & Tasty Cake
 * @date 9/17/2024
 */

@SerializableAs("Loadable")
class Loadable(
    val jarName: String,  // Name of the plugin JAR file
    val priority: Int,    // Loading priority (lower numbers load first)
    val enable: Boolean,  // Whether the plugin should be loaded
) : ConfigurationSerializable {
    companion object {
        /**
         * Deserialize a Loadable object from a map
         * This is used when loading from the config file
         */
        @JvmStatic
        fun deserialize(map: Map<String, Any>): Loadable {
            return Loadable(
                jarName = map["jarName"] as? String ?: throw IllegalArgumentException("jarName is required"),
                priority = map["priority"] as? Int ?: 0,
                enable = map["enabled"] as? Boolean ?: true,
            )
        }
    }

    /**
     * Serialize the Loadable object to a map
     * This is used when saving to the config file
     */
    override fun serialize(): MutableMap<String, Any> {
        return mutableMapOf(
            "jarName" to jarName,
            "priority" to priority,
            "enabled" to enable,
        )
    }

    /**
     * String representation of the Loadable object
     */
    override fun toString(): String {
        return "Loadable(jarName='$jarName', priority=$priority, enabled=$enable)"
    }
}