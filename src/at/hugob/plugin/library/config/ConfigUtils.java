package at.hugob.plugin.library.config;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.ComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Configuration Utils
 */
public class ConfigUtils {

    private final static Pattern PLACE_HOLDER_PATTERN = Pattern.compile("%([^%\\s]+)%");

    private ConfigUtils() {
    }

    /**
     * Tries to convert an Object to an {@code ConfigurationSection}
     *
     * @param object The object that should be turned into a {@code ConfigurationSection}
     * @return A {@code ConfigurationSection} if successful else null
     */
    @SuppressWarnings("unchecked")
    public static ConfigurationSection objectToConfigurationSection(final Object object) {
        if (object instanceof ConfigurationSection) {
            return (ConfigurationSection) object;
        } else if (object instanceof Map) {
            final MemoryConfiguration result = new MemoryConfiguration();
            result.addDefaults((Map<String, Object>) object);
            return result.getDefaultSection();
        } else {
            Bukkit.getLogger().warning("couldn't parse Config of type: " + object.getClass().getSimpleName());
            return null;
        }
    }

    /**
     * Converts a {@code List<String>} in an {@code ConfigurationSection} to an {@code EnumSet<Material>}
     *
     * @param config the {@code ConfigurationSection} where the {@code List<String>} is in
     * @param path   the path to the {@code List<String>} in the {@code ConfigurationSection}
     * @return the created {@code EnumSet<Material>}, {@code null} when the {@code List<String>} wasn't found
     */
    public static @Nullable EnumSet<Material> getMaterialSet(@NotNull final ConfigurationSection config, @NotNull final String path) {
        if (!config.isList(path))
            return null;
        final EnumSet<Material> result = EnumSet.noneOf(Material.class);
        for (final String materialName : config.getStringList(path)) {
            final Material material = getMaterial(materialName);
            if (material != null)
                result.add(material);
        }
        return result;
    }


    /**
     * Gets an {@code Material} from an {@code ConfigurationSection} at the specified Path
     *
     * @param config the {@code ConfigurationSection} where the {@code Material} is in
     * @param path   to the {@code Material}
     * @return corresponding {@code Material}, {@code null} when the {@code Material} does not exist
     */
    public static @Nullable Material getMaterial(@NotNull final ConfigurationSection config, @NotNull final String path) {
        String materialName = config.getString(path);
        if (materialName == null) return null;
        return getMaterial(materialName);
    }

    /**
     * Converts a {@code String} to the corresponding {@code Material}
     *
     * @param materialName {@code String} to convert
     * @return corresponding {@code Material}, {@code null} when the
     * {@code Material} does not exist
     */
    public static @Nullable Material getMaterial(@NotNull final String materialName) {
        final Material material = Material.matchMaterial(materialName);
        if (material == null) {
            Bukkit.getLogger().warning(() -> String.format("\"%s\" is not a valid Material name!", materialName));
        }
        return material;
    }

    /**
     * Gets an {@code Integer} from an {@code ConfigurationSection} at the specified Path
     *
     * @param config the {@code ConfigurationSection} where the {@code Integer} is in
     * @param path   to the {@code Integer}
     * @return corresponding {@code Integer}, {@code null} when the {@code Integer} does not exist
     */
    public static @Nullable Integer getInteger(@NotNull final ConfigurationSection config, @NotNull final String path) {
        return config.isInt(path) ? config.getInt(path) : null;
    }

    /**
     * Gets an {@code Double} from an {@code ConfigurationSection} at the specified Path
     *
     * @param config the {@code ConfigurationSection} where the {@code Double} is in
     * @param path   to the {@code Double}
     * @return corresponding {@code Double}, {@code null} when the {@code Double} does not exist
     */
    public static @Nullable Double getDouble(@NotNull final ConfigurationSection config, @NotNull final String path) {
        return config.isDouble(path) || config.isInt(path) ? config.getDouble(path) : null;
    }

    /**
     * Gets an {@code Boolean} from an {@code ConfigurationSection} at the specified Path
     *
     * @param config the {@code ConfigurationSection} where the {@code Boolean} is in
     * @param path   to the {@code Boolean}
     * @return corresponding {@code Boolean}, {@code null} when the {@code Boolean} does not exist
     */
    public static @Nullable Boolean getBoolean(@NotNull final ConfigurationSection config, @NotNull final String path) {
        return config.isBoolean(path) ? config.getBoolean(path) : null;
    }

    /**
     * Gets an {@code String} from an {@code ConfigurationSection} at the specified Path
     *
     * @param config the {@code ConfigurationSection} where the {@code String} is in
     * @param path   to the {@code String}
     * @return corresponding {@code String}, {@code null} when the {@code String} does not exist
     */
    public static @Nullable String getString(@NotNull final ConfigurationSection config, @NotNull final String path) {
        return config.getString(path);
    }

    /**
     * Gets an {@code UUID} from an {@code ConfigurationSection} at the specified Path
     *
     * @param config the {@code ConfigurationSection} where the {@code UUID} is in
     * @param path   to the {@code UUID}
     * @return corresponding {@code UUID}, {@code null} when the {@code UUID} does not exist
     */
    public static @Nullable UUID getUUID(@NotNull final ConfigurationSection config, @NotNull final String path) {
        String uuid = config.getString(path);
        return uuid == null ? null : UUID.fromString(uuid);
    }

    /**
     * Gets an {@code Component} from an {@code ConfigurationSection} at the specified Path
     * it deserializes with the legacy component serializer
     *
     * @param config the {@code ConfigurationSection} where the {@code Component} is in
     * @param path   to the {@code Component}
     * @return corresponding {@code Component}, {@code Component.empty()} when the {@code Component} does not exist
     */
    public static @NotNull Component getComponent(@NotNull final ConfigurationSection config, @NotNull final String path) {
        return getComponent(config, path, LegacyComponentSerializer.legacyAmpersand());
    }

    /**
     * Gets an {@code Component} from an {@code ConfigurationSection} at the specified Path
     * <p>
     * deserialized with the specified serializer
     *
     * @param config the {@code ConfigurationSection} where the {@code Component} is in
     * @param path   to the {@code Component}
     * @param <T> the type that the deserializer uses
     * @param serializer  the serializer to deserialize the message
     * @return corresponding {@code Component}, {@code Component.empty()} when the {@code Component} does not exist
     */
    public static @NotNull <T extends Component> Component getComponent(@NotNull final ConfigurationSection config, @NotNull final String path, @NotNull final ComponentSerializer<Component, T, String> serializer) {
        Component result;
        if (config.isString(path)) {
            result = serializer.deserialize(config.getString(path));
        } else if (config.isList(path)) {
            List<String> strings = config.getStringList(path);
            if (strings.isEmpty()) return Component.empty();

            Iterator<T> componentIterator = strings.stream().map(serializer::deserialize).iterator();
            result = Component.empty().append(componentIterator.next());
            while (componentIterator.hasNext()) {
                result = result.append(Component.newline()).append(componentIterator.next());
            }
        } else {
            return Component.empty();
        }
        result = result.replaceText(TextReplacementConfig.builder()
                .match(PLACE_HOLDER_PATTERN)
                .replacement((match, componentBuilder) -> {
                    final String componentKey = match.group(1);
                    if (componentKey.equals(path)) return componentBuilder.build();

                    final Component replacement = getComponent(config, componentKey, serializer);
                    if (!replacement.equals(Component.empty())) {
                        return replacement;
                    } else {
                        return componentBuilder.build();
                    }
                }).build());
        return result;
    }

    /**
     * Gets an {@code ItemStack} from an {@code ConfigurationSection} at the specified Path
     *
     * @param config the {@code ConfigurationSection} where the {@code ItemStack} is in
     * @param path   to the {@code ItemStack}
     * @return the {@code ItemStack} for the corresponding {@code ConfigurationSection}
     */
    public static ItemStack getItemStack(@NotNull final ConfigurationSection config, @NotNull final String path) {
        final ConfigurationSection itemConfig = config.getConfigurationSection(path);
        if (itemConfig == null) return null;
        return getItemStack(itemConfig);
    }

    /**
     * Profile texture property name
     */
    private static final String TEXTURES = "textures";

    /**
     * Gets an {@code ItemStack} from an {@code ConfigurationSection}
     *
     * @param config the {@code ConfigurationSection} where the {@code ItemStack} is in
     * @return the {@code ItemStack} for the corresponding {@code ConfigurationSection}
     */
    public static ItemStack getItemStack(@NotNull final ConfigurationSection config) {
        final Material material = getMaterial(config, "material");
        if (material == null || material.isAir() || !material.isItem()) return null;
        final ItemStack itemStack = new ItemStack(material, config.getInt("amount", 1));
        final ItemMeta itemMeta = itemStack.getItemMeta();
        if (config.isString("name"))
            itemMeta.displayName(Component.empty().decoration(TextDecoration.ITALIC, false).append(ConfigUtils.getComponent(config, "name")));
        if (config.isList("lore"))
            itemMeta.lore(config.getStringList("lore").stream()
                    .map(LegacyComponentSerializer.legacyAmpersand()::deserialize)
                    .map(component -> Component.empty().color(NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false).append(component))
                    .collect(Collectors.toList()));
        if (config.isInt("custom-model"))
            itemMeta.setCustomModelData(config.getInt("custom-model"));
        if (config.isBoolean("unbreakable"))
            itemMeta.setUnbreakable(config.getBoolean("unbreakable"));
        // Apply Head Meta if applicable
        if (itemMeta instanceof final SkullMeta skullMeta) {
            final UUID headUuid = ConfigUtils.getUUID(config, "texture.uuid");
            if (headUuid != null)
                skullMeta.setPlayerProfile(Bukkit.createProfile(headUuid));
            if (config.isString("texture.data")) {
                final ProfileProperty headTexture;
                if (config.isString("texture.signature")) {
                    headTexture = new ProfileProperty(TEXTURES, config.getString("texture.data"), config.getString("texture.signature"));
                } else {
                    headTexture = new ProfileProperty(TEXTURES, config.getString("texture.data"));
                }
                PlayerProfile profile = skullMeta.getPlayerProfile();
                if (profile == null) {
                    profile = Bukkit.createProfile(null, null);
                }
                profile.setProperty(headTexture);
                skullMeta.setPlayerProfile(profile);
            }
        }
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }
}
