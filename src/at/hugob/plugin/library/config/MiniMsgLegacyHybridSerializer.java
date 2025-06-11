package at.hugob.plugin.library.config;

import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.regex.Pattern;

/**
 * A Hybrid serializer that substitutes all legacy tags with MiniMessage Tags and then deserializes via MiniMessage
 */
public class MiniMsgLegacyHybridSerializer {
    private final static Pattern LEGACY_HEX_PATTERN = Pattern.compile("&(#[0-9a-fA-F]{6})");
    private final static Pattern LEGACY_PATTERN = Pattern.compile("&([0-9a-fA-FklmnorKLMNOR])");

    /**
     * The instance of this class
     */
    public final static MiniMessage INSTANCE = MiniMessage.builder()
        .preProcessor(MiniMsgLegacyHybridSerializer::parseLegacy)
        .build();

    private MiniMsgLegacyHybridSerializer() {}

    public static String parseLegacy(String input) {
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
