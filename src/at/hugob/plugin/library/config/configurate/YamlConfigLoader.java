package at.hugob.plugin.library.config.configurate;

import net.kyori.adventure.text.Component;
import org.bukkit.plugin.java.JavaPlugin;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;

/**
 * Creates a Yaml Config Loader using Configurate
 *
 * @param <ConfigClass> the class with the annotation {@link org.spongepowered.configurate.objectmapping.ConfigSerializable} that represents the Config
 */
public class YamlConfigLoader<ConfigClass> {

    private final YamlConfigurationLoader loader;
    private final Class<ConfigClass> configClass;

    /**
     * Creates a YamlConfigLoader.
     *
     * @param plugin      The plugin that uses this builder
     * @param filePath    The path where the file should be saved
     * @param configClass The config that represents this file
     */
    public YamlConfigLoader(JavaPlugin plugin, String filePath, Class<ConfigClass> configClass) {
        this(plugin, filePath, configClass, TypeSerializerCollection.builder().build());

    }

    /**
     * Creates a YamlConfigLoader.
     *
     * @param plugin      The plugin that uses this builder
     * @param filePath    The path where the file should be saved
     * @param configClass The config that represents this file
     * @param serializers Extra Type Serializers
     */
    public YamlConfigLoader(JavaPlugin plugin, String filePath, Class<ConfigClass> configClass, TypeSerializerCollection serializers) {
        var typeSerializers = TypeSerializerCollection.defaults().childBuilder()
            .register(Component.class, ComponentSerializer.INSTANCE)
            .registerAll(serializers).build();

        this.configClass = configClass;
        var file = new File(plugin.getDataFolder(), filePath);
        file.getParentFile().mkdirs();
        loader = YamlConfigurationLoader.builder()
            .defaultOptions(opts -> opts
                .shouldCopyDefaults(true)
                .serializers(typeSerializers)
            )
            .nodeStyle(NodeStyle.BLOCK)
            .commentsEnabled(true)
            .file(file)
            .build();
    }

    /**
     * Reloads the config, optionally saves it if there are changes to the defaults and returns the Config
     *
     * @return The config populated by the Config file values
     * @throws ConfigurateException If there is an error
     */
    public ConfigClass reload() throws ConfigurateException {
        var data = loader.load();
        var copy = data.copy();
        var config = data.get(configClass);
        if (!data.childrenMap().entrySet().equals(copy.childrenMap().entrySet())) {
            loader.save(data);
        }
        return config;
    }
}
