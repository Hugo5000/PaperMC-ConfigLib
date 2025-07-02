package at.hugob.plugin.library.config.configurate;

import at.hugob.plugin.library.config.MiniMsgLegacyHybridSerializer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.Arrays;

final class ComponentSerializer implements TypeSerializer<Component> {
    public static final ComponentSerializer INSTANCE = new ComponentSerializer();

    private ComponentSerializer() {
    }

    private ConfigurationNode nonVirtualNode(final ConfigurationNode source, final Object... path) throws SerializationException {
        if (!source.hasChild(path)) {
            throw new SerializationException("Required field " + Arrays.toString(path) + " was not present in node");
        }
        return source.node(path);
    }

    @Override
    public Component deserialize(final Type type, final ConfigurationNode source) throws SerializationException {
        final String string = source.getString();
        var parent = source;
        while (parent.parent() != null) parent = parent.parent();
        final var finalParent = parent;
        return MiniMsgLegacyHybridSerializer.INSTANCE.deserialize(string, TagResolver
            .resolver("ref", (argumentQueue, context) -> {
                final String reference = argumentQueue.popOr("reference expected").value();
                final var value = finalParent.getString(reference);
                if (value == null) throw context.newException("reference not found");
                return Tag.preProcessParsed(MiniMsgLegacyHybridSerializer.parseLegacy(value));
            })
        );
    }

    @Override
    public void serialize(final Type type, final @Nullable Component component, final ConfigurationNode target) throws SerializationException {
        if (component == null) {
            target.raw(null);
            return;
        }
        target.set(MiniMsgLegacyHybridSerializer.INSTANCE.serialize(component));
    }
}
