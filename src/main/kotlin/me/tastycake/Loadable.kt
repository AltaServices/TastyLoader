package me.tastycake

import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.SerializableAs

/**
 * @author Cupoftea & Tasty Cake
 * @date 9/15/2024
 */


@SerializableAs("Loadable")
class Loadable(
    val jarName: String,
    val priority: Int,
    val enable: Boolean,
) : ConfigurationSerializable {
    companion object {
        @JvmStatic
        fun deserialize(map: Map<String, Any>): Loadable {
            return Loadable(
                jarName = map["jarName"] as? String ?: throw IllegalArgumentException("jarName is required"),
                priority = map["priority"] as? Int ?: 0,
                enable = map["enabled"] as? Boolean ?: true,
            )
        }
    }

    override fun serialize(): MutableMap<String, Any> {
        return mutableMapOf(
            "jarName" to jarName,
            "priority" to priority,
            "enabled" to enable,
        )
    }

    override fun toString(): String {
        return "Loadable(jarName='$jarName', priority=$priority, enabled=$enable)"
    }
}