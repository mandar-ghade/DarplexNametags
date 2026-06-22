package com.darplex.darplexNametags.component;

import com.darplex.darplexNametags.DarplexNametags;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

public class ColoredComponent extends DarplexComponent {

    @Getter NamedTextColor color;

    public ColoredComponent(@NotNull String text,
                            @NotNull NamedTextColor color) {
        super(text);
        this.color = color;
        this.componentResolver = () -> Component.text(text).color(color);
    }

}
