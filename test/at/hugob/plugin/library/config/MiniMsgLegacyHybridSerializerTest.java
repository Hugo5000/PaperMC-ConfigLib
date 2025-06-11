package at.hugob.plugin.library.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class MiniMsgLegacyHybridSerializerTest {
    private final MiniMsgLegacyHybridSerializer serializer = MiniMsgLegacyHybridSerializer.INSTANCE;


    @ParameterizedTest
    @MethodSource("colorProvider")
    void colorTests(String text, TextColor color) {
        assertEquals(serializer.deserialize(text).color(), color);
        assertEquals(serializer.deserialize(text.toUpperCase()).color(), color);
    }

    static Stream<Arguments> colorProvider() {
        return Stream.of(
            Arguments.of("&0", NamedTextColor.BLACK),
            Arguments.of("&1", NamedTextColor.DARK_BLUE),
            Arguments.of("&2", NamedTextColor.DARK_GREEN),
            Arguments.of("&3", NamedTextColor.DARK_AQUA),
            Arguments.of("&4", NamedTextColor.DARK_RED),
            Arguments.of("&5", NamedTextColor.DARK_PURPLE),
            Arguments.of("&6", NamedTextColor.GOLD),
            Arguments.of("&7", NamedTextColor.GRAY),
            Arguments.of("&8", NamedTextColor.DARK_GRAY),
            Arguments.of("&9", NamedTextColor.BLUE),
            Arguments.of("&a", NamedTextColor.GREEN),
            Arguments.of("&b", NamedTextColor.AQUA),
            Arguments.of("&c", NamedTextColor.RED),
            Arguments.of("&d", NamedTextColor.LIGHT_PURPLE),
            Arguments.of("&e", NamedTextColor.YELLOW),
            Arguments.of("&f", NamedTextColor.WHITE),
            Arguments.of("&#FF0000", TextColor.color(255, 0, 0)),
            Arguments.of("&#00FF00", TextColor.color(0, 255, 0)),
            Arguments.of("&#0000FF", TextColor.color(0, 0, 255))
        );
    }

    @ParameterizedTest
    @MethodSource("decoratorProvider")
    void decoratorTests(String text, TextDecoration decoration) {
        assertTrue(serializer.deserialize(text).hasDecoration(decoration));
    }

    static Stream<Arguments> decoratorProvider() {
        return Stream.of(
            Arguments.of("&k", TextDecoration.OBFUSCATED),
            Arguments.of("&l", TextDecoration.BOLD),
            Arguments.of("&m", TextDecoration.STRIKETHROUGH),
            Arguments.of("&n", TextDecoration.UNDERLINED),
            Arguments.of("&o", TextDecoration.ITALIC)
        );
    }
}
