package at.hugob.plugin.library.config;

import net.kyori.adventure.pointer.Pointered;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tree.Node;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

/**
 * A Hybrid serializer that substitutes all legacy tags with MiniMessage Tags and then deserializes via MiniMessage
 */
public class MiniMsgLegacyHybridSerializer implements MiniMessage {
    private final static Pattern LEGACY_HEX_PATTERN = Pattern.compile("&([0-9a-fA-F]{6})");
    private final static Pattern LEGACY_PATTERN = Pattern.compile("&([0-9a-fA-FklmnorKLMNOR])");

    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    /**
     * The instance of this class
     */
    public final static MiniMsgLegacyHybridSerializer INSTANCE = new MiniMsgLegacyHybridSerializer();

    private MiniMsgLegacyHybridSerializer() {}


    @Override
    public @NotNull String serialize(@NotNull Component component) {
        return miniMessage.serialize(component);
    }

    @Override
    public @NotNull String escapeTags(@NotNull String input) {
        return miniMessage.escapeTags(input);
    }

    @Override
    public @NotNull String escapeTags(@NotNull String input, @NotNull TagResolver tagResolver) {
        return miniMessage.escapeTags(input, tagResolver);
    }

    @Override
    public @NotNull String stripTags(@NotNull String input) {
        return miniMessage.stripTags(input);
    }

    @Override
    public @NotNull String stripTags(@NotNull String input, @NotNull TagResolver tagResolver) {
        return miniMessage.stripTags(input, tagResolver);
    }

    @Override
    public @NotNull Component deserialize(@NotNull String input) {
        return miniMessage.deserialize(parseLegacy(input));
    }

    @Override
    public @NotNull Component deserialize(@NotNull String input, @NotNull Pointered target) {
        return miniMessage.deserialize(parseLegacy(input), target);
    }

    @Override
    public @NotNull Component deserialize(@NotNull String input, @NotNull TagResolver tagResolver) {
        return miniMessage.deserialize(parseLegacy(input), tagResolver);
    }

    @Override
    public @NotNull Component deserialize(@NotNull String input, @NotNull Pointered target, @NotNull TagResolver tagResolver) {
        return miniMessage.deserialize(parseLegacy(input), target, tagResolver);
    }

    @Override
    public Node.Root deserializeToTree(@NotNull String input) {
        return miniMessage.deserializeToTree(parseLegacy(input));
    }

    @Override
    public Node.Root deserializeToTree(@NotNull String input, @NotNull Pointered target) {
        return miniMessage.deserializeToTree(parseLegacy(input), target);
    }

    @Override
    public Node.Root deserializeToTree(@NotNull String input, @NotNull TagResolver tagResolver) {
        return miniMessage.deserializeToTree(parseLegacy(input), tagResolver);
    }

    @Override
    public Node.Root deserializeToTree(@NotNull String input, @NotNull Pointered target, @NotNull TagResolver tagResolver) {
        return miniMessage.deserializeToTree(parseLegacy(input), target, tagResolver);
    }

    @Override
    public boolean strict() {
        return miniMessage.strict();
    }

    @Override
    public @NotNull TagResolver tags() {
        return miniMessage.tags();
    }

    private String parseLegacy(String input) {
        input = LEGACY_HEX_PATTERN.matcher(input).replaceAll(matchResult -> "<%s>".formatted(matchResult.group(1)));
        return LEGACY_PATTERN.matcher(input).replaceAll(matchResult -> switch (matchResult.group(1).toLowerCase()) {
                case "0" -> "<black>";
                case "1" -> "<dark_blue>";
                case "2" -> "<dark_green>";
                case "3" -> "<dark_aqua>";
                case "4" -> "<dark_red>";
                case "5" -> "<dark_purple>";
                case "6" -> "<gold>";
                case "7" -> "<gray>";
                case "8" -> "<dark_gray>";
                case "9" -> "<blue>";
                case "a" -> "<green>";
                case "b" -> "<aqua>";
                case "c" -> "<red>";
                case "d" -> "<light_purple>";
                case "e" -> "<yellow>";
                case "f" -> "<white>";

                case "k" -> "<obfuscated>";
                case "l" -> "<bold>";
                case "m" -> "<strikethrough>";
                case "n" -> "<underlined>";
                case "o" -> "<italic>";
                case "r" -> "<reset>";

                default -> matchResult.group(1);
            }
        );
    }
}
