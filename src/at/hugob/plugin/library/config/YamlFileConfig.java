package at.hugob.plugin.library.config;

import com.google.common.base.Charsets;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.function.Supplier;
import java.util.logging.Level;

/**
 * An extension for Bukkits YamlConfiguration
 */
public class YamlFileConfig extends YamlConfiguration {

    private final String filePath;
    private final File configFile;
    private final JavaPlugin plugin;
    private final Supplier<InputStream> inputStream;

    private final HashMap<String, Component> chatComponentCache = new HashMap<>();

    /**
     * Creates a YamlFileConfiguration at the specified path inside the plugins folder
     *
     * @param plugin   the Plugin that creates the config
     * @param filePath the path to the config
     */
    public YamlFileConfig(final JavaPlugin plugin, final String filePath) {
        this(plugin, filePath, () -> plugin.getResource(filePath));
    }

    /**
     * Creates a YamlFileConfiguration at the specified path inside the plugins folder
     * with a specific input stream for where the default file comes from
     *
     * @param plugin      the Plugin that creates the config
     * @param filePath    the path to the config
     * @param inputStream the input stream that gets the default config file
     */
    public YamlFileConfig(final JavaPlugin plugin, final String filePath, final Supplier<InputStream> inputStream) {
        this.inputStream = inputStream;
        this.filePath = filePath;
        configFile = new File(plugin.getDataFolder(), filePath);
        this.plugin = plugin;
        reload();
    }

    /**
     * Reload the config file from the disc or copies the default config file to the config location and loads that
     */
    public void reload() {
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            try {
                configFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create Config file: " + filePath, e);
                return;
            }
            try (var in = inputStream.get(); var out = new FileOutputStream(configFile)) {
                if (in == null) {
                    plugin.getLogger().log(Level.SEVERE, "Resource in Jar not found: " + filePath);
                    return;
                }
                out.write(in.readAllBytes());
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not write the default Config to the newly created config file: " + filePath, e);
                return;
            }
        }
        try {
            load(configFile);

        } catch (IOException | InvalidConfigurationException e) {
            plugin.getLogger().log(Level.SEVERE, String.format("Could not load Config from \"%s\"", filePath), e);
            return;
        }
        try (InputStream defConfigStream = inputStream.get()) {
            if (defConfigStream != null) {
                setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream, Charsets.UTF_8)));
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, String.format("Could not load the Default config for \"%s\"", filePath), e);
        }
        chatComponentCache.clear();
    }

    /**
     * saves the config file to the disc
     */
    public void save() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                save(configFile);
            } catch (final IOException ex) {
                plugin.getLogger().log(Level.SEVERE, ex, () -> "Could not save config to " + configFile);
            }
        });
    }

    /**
     * Gets a Message Component at a specific path and also substitutes all placeholders that have values in the config file
     *
     * @param path the path the component
     * @return the Component at the path
     */
    public Component getComponent(String path) {

        if (chatComponentCache.containsKey(path))
            return chatComponentCache.get(path);

        Component result = ConfigUtils.getComponent(this, path);
        chatComponentCache.put(path, result);
        return result;
    }

}
