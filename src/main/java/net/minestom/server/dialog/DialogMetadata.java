package net.minestom.server.dialog;

import net.kyori.adventure.text.Component;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

public record DialogMetadata(
        Component title,
        @Nullable Component externalTitle,
        boolean canCloseWithEscape,
        boolean pause,
        DialogAfterAction afterAction,
        List<DialogBody> body,
        List<DialogInput> inputs
) {
    public static final StructCodec<DialogMetadata> CODEC = StructCodec.struct(
            "title", Codec.COMPONENT, DialogMetadata::title,
            "external_title", Codec.COMPONENT.optional(), DialogMetadata::externalTitle,
            "can_close_with_escape", StructCodec.BOOLEAN.optional(true), DialogMetadata::canCloseWithEscape,
            "pause", StructCodec.BOOLEAN.optional(true), DialogMetadata::pause,
            "after_action", DialogAfterAction.CODEC.optional(DialogAfterAction.CLOSE), DialogMetadata::afterAction,
            "body", DialogBody.CODEC.listOrSingle().optional(List.of()), DialogMetadata::body,
            "inputs", DialogInput.CODEC.list().optional(List.of()), DialogMetadata::inputs,
            DialogMetadata::new);

    public DialogMetadata {
        Check.argCondition(pause && afterAction == DialogAfterAction.NONE,
                "Dialog may not have pause=true and afterAction=NONE");
    }

    public List<Component> components() {
        List<Component> components = new ArrayList<>();

        components.add(title);
        if (externalTitle != null) {
            components.add(externalTitle);
        }

        components.addAll(body.stream().flatMap(it -> it.components().stream()).toList());
        components.addAll(inputs.stream().flatMap(it -> it.components().stream()).toList());

        return components;
    }

    public DialogMetadata copyWithOperator(UnaryOperator<Component> operator) {
        return new DialogMetadata(
                operator.apply(title),
                externalTitle == null ? null : operator.apply(externalTitle),
                canCloseWithEscape,
                pause,
                afterAction,
                body.stream().map(it -> it.copyWithOperator(operator)).toList(),
                inputs.stream().map(it -> it.copyWithOperator(operator)).toList()
        );
    }
}
