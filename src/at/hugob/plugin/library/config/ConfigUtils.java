package at.hugob.plugin.library.config;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.pointer.Pointered;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
     * Converts a {@code List<String>} in an {@code ConfigurationSection} to an {@code EnumSet<Material>}
     *
     * @param config the {@code ConfigurationSection} where the {@code List<String>} is in
     * @param path   the path to the {@code List<String>} in the {@code ConfigurationSection}
     * @return the created {@code EnumSet<Material>}, {@code null} when the {@code List<String>} wasn't found
     */
    public static @Nullable Collection<ItemType> getItemTypes(@NotNull final ConfigurationSection config, @NotNull final String path) {
        if (!config.isList(path))
            return null;
        final List<ItemType> result = new LinkedList<>();
        for (final String itemName : config.getStringList(path)) {
            result.addAll(getItemTypes(itemName));
        }
        return new ArrayList<>(result);
    }


    /**
     * Gets an {@code ItemType} from an {@code ConfigurationSection} at the specified Path
     *
     * @param config the {@code ConfigurationSection} where the {@code Material} is in
     * @param path   to the {@code ItemType}
     * @return corresponding {@code ItemType}, {@code null} when the {@code ItemType} does not exist
     */
    public static @Nullable ItemType getItemType(@NotNull final ConfigurationSection config, @NotNull final String path) {
        String itemName = config.getString(path);
        if (itemName == null) return null;
        return getItemType(itemName);
    }


    /**
     * Converts a {@code String} Tag or ItemType Key to the corresponding {@code Collection<ItemType>}
     *
     * @param itemName {@code String} to convert
     * @return corresponding {@code Collection<ItemType>}, an empty {@code Collection<ItemType>} when the
     * Tag does not exist
     */
    public static @Nullable Collection<ItemType> getItemTypes(@NotNull final String itemName) {
        if (itemName.startsWith("#")) {
            var key = ItemTypeTagKeys.create(parseKey(itemName.substring(1)));
            final Collection<ItemType> itemType = Registry.ITEM.getTag(key).resolve(Registry.ITEM).stream().sorted(Comparator.comparing(i -> i.getKey().getKey())).toList();
            if (itemType.isEmpty()) {
                Bukkit.getLogger().warning(() -> String.format("\"%s\" is not a valid Tag name!", itemName));
            }
            return itemType;
        } else if (itemName.startsWith("@")) {
            switch (formattedKey(itemName).substring(1)) {
                case "foods", "edible": return itemTypes().filter(itemType -> itemType.isEdible()).toList();
                case "records": return itemTypes().filter(itemType -> itemType.isRecord()).toList();
                case "damageable", "durability": return itemTypes().filter(itemType -> itemType.hasDefaultData(DataComponentTypes.MAX_DAMAGE)).toList();
                case "block": return itemTypes().filter(ItemType::hasBlockType).toList();
                case "item": return itemTypes().filter(itemType -> !itemType.hasBlockType()).toList();
                default:
                    Bukkit.getLogger().warning(() -> String.format("\"%s\" is not a valid meta Tag!", itemName));
                    return Collections.emptyList();
            }
        } else {
            var formatted = formattedKey(itemName);
            if (Key.parseable(formatted)) {
                Key key = Key.key(formatted);
                var itemType = Registry.ITEM.get(key);
                if (itemType == null) return Collections.emptyList();
                return Collections.singleton(itemType);
            } else {
                var regex = "^" + formatted + "$";
                return Registry.ITEM.stream().filter(i -> i.getKey().getKey().matches(regex)).sorted(Comparator.comparing(i -> i.getKey().getKey())).toList();
            }
        }
    }

    private static Stream<ItemType> itemTypes() {
        return Registry.ITEM.stream().filter(itemType -> itemType != ItemType.AIR);
    }

    /**
     * Converts a {@code String} to the corresponding {@code ItemType}
     *
     * @param itemName {@code String} to convert
     * @return corresponding {@code ItemType}, {@code null} when the
     * {@code ItemType} does not exist
     */
    public static @Nullable ItemType getItemType(@NotNull final String itemName) {
        final var key = parseKey(itemName);
        final var itemType = Registry.ITEM.get(key);
        if (itemType == null) {
            Bukkit.getLogger().warning(() -> String.format("\"%s\" is not a valid Item name!", itemType));
        }
        return itemType;
    }

    /**
     * Parses a {@code String} into a Key
     *
     * @param key the key to parse in the format abc:xyz
     * @return The parsed {@code Key} or null if it could not be parsed
     */
    public static @Nullable Key parseKey(@NotNull final String key) {
        final var formattedKey = formattedKey(key);
        if (!Key.parseable(formattedKey)) {
            return null;
        }
        return Key.key(formattedKey);
    }

    private static @NotNull String formattedKey(@NotNull String key) {
        return key.toLowerCase().replace(' ', '_');
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
     * <p>
     * deserialized with the specified serializer
     *
     * @param config     the {@code ConfigurationSection} where the {@code Component} is in
     * @param path       to the {@code Component}
     * @param <T>        the type that the deserializer uses
     * @param serializer the serializer to deserialize the message
     * @return corresponding {@code Component}, {@code Component.empty()} when the {@code Component} does not exist
     */
    public static @NotNull <T extends Component> Component getComponent(
        @NotNull final ConfigurationSection config, @NotNull final String path,
        @NotNull final ComponentSerializer<Component, T, String> serializer
    ) {
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
     * Parses a component from the config at a specific path
     *
     * @param config The config to parse from
     * @param path   The path where the component should be parse from
     * @return The component that was parsed or Component.empty()
     */
    public static @NotNull Component getComponent(
        @NotNull final ConfigurationSection config, @NotNull final String path
    ) {
        return getComponent(config, path, null, null);
    }

    /**
     * Parses a component from the config at a specific path
     *
     * @param config      The config to parse from
     * @param path        The path where the component should be parse from
     * @param tagResolver A tag resolver
     * @return The component that was parsed or Component.empty()
     */
    public static @NotNull Component getComponent(
        @NotNull final ConfigurationSection config, @NotNull final String path,
        final @Nullable TagResolver tagResolver
    ) {
        return getComponent(config, path, tagResolver, null);
    }

    /**
     * Parses a component from the config at a specific path
     *
     * @param config      The config to parse from
     * @param path        The path where the component should be parse from
     * @param tagResolver A tag resolver
     * @param target      A target to use for the tag resolvers
     * @return The component that was parsed or Component.empty()
     */
    public static @NotNull Component getComponent(
        @NotNull final ConfigurationSection config, @NotNull final String path,
        final @Nullable TagResolver tagResolver, @Nullable Pointered target
    ) {
        return getComponent(config, path, MiniMsgLegacyHybridSerializer.INSTANCE, tagResolver, target);
    }

    /**
     * Parses a component from the config at a specific path
     *
     * @param config      The config to parse from
     * @param path        The path where the component should be parse from
     * @param serializer  The serializer to use
     * @param tagResolver A tag resolver
     * @param target      A target to use for the tag resolvers
     * @return The component that was parsed or Component.empty()
     */
    public static @NotNull Component getComponent(
        @NotNull final ConfigurationSection config, @NotNull final String path,
        @NotNull final MiniMessage serializer,
        final @Nullable TagResolver tagResolver, @Nullable Pointered target
    ) {
        return getComponent(config, path, serializer, tagResolver, target, new ArrayList<>());
    }

    private static @NotNull Component getComponent(
        @NotNull final ConfigurationSection config, @NotNull final String path,
        @NotNull final MiniMessage serializer,
        final @Nullable TagResolver tagResolver, @Nullable Pointered target,
        @NotNull List<String> vistedSubSections
    ) {
        vistedSubSections.add(path);
        if (config.isString(path)) {
            return parseComponent(config, config.getString(path), serializer, tagResolver, target, vistedSubSections);
        } else if (config.isList(path)) {
            List<String> strings = config.getStringList(path);
            if (strings.isEmpty()) return Component.empty();
            return parseComponent(config, strings, serializer, tagResolver, target, vistedSubSections);
        } else {
            return Component.empty();
        }
    }

    /**
     * Parses a text into a Component and tries to parse any references ({@code <ref:'<path>'>}) that it finds
     *
     * @param config      The config to take the paths from
     * @param text        The text to parse
     * @param tagResolver An optional TagResolver to use
     * @param target      An optional target to use
     * @return the parsed Component
     */
    public static @NotNull Component parseComponent(
        @NotNull final ConfigurationSection config, @NotNull final String text,
        @Nullable TagResolver tagResolver, @Nullable Pointered target
    ) {
        return parseComponent(config, text, MiniMsgLegacyHybridSerializer.INSTANCE, tagResolver, target, new ArrayList<>());
    }

    private static @NotNull Component parseComponent(
        @NotNull final ConfigurationSection config, @NotNull final String text,
        @NotNull final MiniMessage serializer,
        @Nullable TagResolver tagResolver, @Nullable Pointered target,
        @NotNull List<String> vistedSubSections
    ) {
        tagResolver = createSubSectionResolver(config, tagResolver);
        if (target == null) {
            return serializer.deserialize(text, tagResolver);
        } else {
            return serializer.deserialize(text, target, tagResolver);
        }
    }


    /**
     * Parses a list of texts into a Component seperated by new lines and tries to parse any references ({@code <ref:'<path>'>}) that it finds
     *
     * @param config      The config to take the paths from
     * @param text        The List of texts to parse
     * @param tagResolver An optional TagResolver to use
     * @param target      An optional target to use
     * @return the parsed Component
     */
    public static @NotNull Component parseComponent(
        @NotNull final ConfigurationSection config, @NotNull final List<String> text,
        final @Nullable TagResolver tagResolver, @Nullable Pointered target
    ) {
        return parseComponent(config, text, MiniMsgLegacyHybridSerializer.INSTANCE, tagResolver, target, new ArrayList<>());
    }

    /**
     * Parses a list of texts into a List of Components seperated by new lines and tries to parse any references ({@code <ref:'<path>'>}) that it finds
     *
     * @param config      The config to take the paths from
     * @param text        The List of texts to parse
     * @param tagResolver An optional TagResolver to use
     * @param target      An optional target to use
     * @return the List of parsed Component
     */
    public static @NotNull List<Component> parseComponentList(
        @NotNull final ConfigurationSection config, @NotNull final List<String> text,
        final @Nullable TagResolver tagResolver, @Nullable Pointered target
    ) {
        return text.stream()
            .map(s -> parseComponent(config, s, MiniMsgLegacyHybridSerializer.INSTANCE, tagResolver, target, new ArrayList<>()))
            .toList();
    }

    /**
     * Parses a list of texts into a List of Components seperated by new lines and tries to parse any references ({@code <ref:'<path>'>}) that it finds
     *
     * @param config      The config to take the paths from
     * @param text        The List of texts to parse
     * @param tagResolver An optional TagResolver to use
     * @param target      An optional target to use
     * @return the List of parsed Component
     */
    public static @NotNull List<? extends Component> parseLoreComponentList(
        @NotNull final ConfigurationSection config, @NotNull final List<String> text,
        final @Nullable TagResolver tagResolver, @Nullable Pointered target
    ) {
        return text.stream()
            .map(s -> Component.empty()
                .color(NamedTextColor.WHITE)
                .decoration(TextDecoration.ITALIC, false)
                .append(
                    parseComponent(config, s, MiniMsgLegacyHybridSerializer.INSTANCE, tagResolver, target, new ArrayList<>())
                )
            )
            .toList();
    }

    private static @NotNull Component parseComponent(
        @NotNull final ConfigurationSection config, @NotNull final List<String> text,
        @NotNull final MiniMessage serializer,
        final @Nullable TagResolver tagResolver, @Nullable Pointered target,
        @NotNull List<String> vistedSubSections
    ) {
        return text.stream()
            .map(s -> parseComponent(config, s, serializer, tagResolver, target, vistedSubSections))
            .reduce((c1, c2) -> c1.append(Component.newline()).append(c2))
            .orElse(Component.empty());
    }

    private static @NotNull TagResolver createSubSectionResolver(
        @NotNull ConfigurationSection config, @Nullable TagResolver tagResolver
    ) {
        final TagResolver subSectionResolver = TagResolver.resolver("ref", (argumentQueue, context) -> {
            final String reference = argumentQueue.popOr("reference expected").value();
            if(!config.isString(reference)) throw context.newException("reference not found");
            return Tag.preProcessParsed(config.getString(reference));
        });
        if (tagResolver != null) {
            return TagResolver.resolver(tagResolver, subSectionResolver);
        } else {
            return subSectionResolver;
        }
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
    public static @NotNull ItemStack getItemStack(@NotNull final ConfigurationSection config) {
        final ItemType itemType = getItemType(config, "material");
        if (itemType == null) return ItemStack.empty();
        final ItemStack itemStack = itemType.createItemStack(config.getInt("amount", 1));
        final ItemMeta itemMeta = itemStack.getItemMeta();
        if (config.isBoolean("enchantment-glint"))
            itemMeta.setEnchantmentGlintOverride(config.getBoolean("enchantment-glint"));
        if (config.isString("name"))
            itemMeta.displayName(Component.empty().decoration(TextDecoration.ITALIC, false).append(ConfigUtils.getComponent(config, "name")));
        if (config.isList("lore"))
            itemMeta.lore(config.getStringList("lore").stream()
                .map(MiniMsgLegacyHybridSerializer.INSTANCE::deserialize)
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
