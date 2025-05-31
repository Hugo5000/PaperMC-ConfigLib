package at.hugob.plugin.library.config;

import com.google.common.base.Charsets;
import net.kyori.adventure.pointer.Pointered;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;
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
    public YamlFileConfig(final JavaPlugin plugin, final String filePath, final @Nullable Supplier<InputStream> inputStream) {
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
        if (!configFile.exists() && inputStream != null) {
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
        if (configFile.exists()) {
            try {
                load(configFile);
            } catch (IOException | InvalidConfigurationException e) {
                plugin.getLogger().log(Level.SEVERE, String.format("Could not load Config from \"%s\"", filePath), e);
                return;
            }
        } else {
            try {
                loadFromString("");
            } catch (InvalidConfigurationException e) {
                throw new RuntimeException(e);
            }
        }
        if (inputStream != null) {
            try (InputStream defConfigStream = inputStream.get()) {
                if (defConfigStream != null) {
                    setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream, Charsets.UTF_8)));
                }
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, String.format("Could not load the Default config for \"%s\"", filePath), e);
            }
        }
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


    private ReentrantLock saveLock = new ReentrantLock();

    /**
     * Saves the config file thread safe
     */
    public void saveSync() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            if (saveLock.hasQueuedThreads()) return; // already someone queued to save
            saveLock.lock();
            try {
                save(configFile);
            } catch (final IOException ex) {
                plugin.getLogger().log(Level.SEVERE, ex, () -> "Could not save config to " + configFile);
            } finally {
                saveLock.unlock();
            }
        });
    }

    /**
     * Gets a Legacy/MiniMessage Hybrid Component at a specific path and also substitutes all placeholders that have values in the config file
     *
     * @param path the path the component
     * @return the Component at the path
     */
    public Component getComponent(@NotNull String path) {
        return getComponent(path, MiniMsgLegacyHybridSerializer.INSTANCE, null, null);
    }

    /**
     * Gets a Legacy/MiniMessage Hybrid Component at a specific path and also substitutes all placeholders that have values in the config file
     *
     * @param path        the path the component
     * @param tagResolver An optional TagResolver to use
     * @return the Component at the path
     */
    public Component getComponent(@NotNull String path, @Nullable TagResolver tagResolver) {
        return getComponent(path, MiniMsgLegacyHybridSerializer.INSTANCE, tagResolver, null);
    }

    /**
     * Gets a Legacy/MiniMessage Hybrid Component at a specific path and also substitutes all placeholders that have values in the config file
     *
     * @param path        the path the component
     * @param tagResolver An optional TagResolver to use
     * @param target      An optional target for the TagResolver to use
     * @return the Component at the path
     */
    public Component getComponent(@NotNull String path, @Nullable TagResolver tagResolver, @Nullable Pointered target) {
        return getComponent(path, MiniMsgLegacyHybridSerializer.INSTANCE, tagResolver, target);
    }

    /**
     * Gets a Message Component at a specific path and also substitutes all placeholders that have values in the config file
     * <p>
     * deserialized with the specified serializer
     *
     * @param path        the path the component
     * @param <T>         The type the deserializer uses
     * @param serializer  the serializer to deserialize the message
     * @param tagResolver An optional Tag resolver
     * @param target      An optional target to use for the TagResolver
     * @return the Component at the path
     */
    public <T extends Component> Component getComponent(String path, ComponentSerializer<Component, T, String> serializer, TagResolver tagResolver, Pointered target) {
        if (!(serializer instanceof MiniMessage miniMessage)) return ConfigUtils.getComponent(this, path, serializer);
        return ConfigUtils.getComponent(this, path, miniMessage, tagResolver, target);
    }

    @Override
    public @Nullable ItemStack getItemStack(@NotNull String path) {
        return ConfigUtils.getItemStack(this, path);
    }

    @Override
    public @Nullable ItemStack getItemStack(@NotNull String path, @Nullable ItemStack def) {
        return Objects.requireNonNullElse(getItemStack(path), def);
    }
}
