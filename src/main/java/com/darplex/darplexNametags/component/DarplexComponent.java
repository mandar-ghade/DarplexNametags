package com.darplex.darplexNametags.component;

import com.darplex.darplexNametags.DarplexNametags;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class DarplexComponent {
//    @NotNull @Getter DarplexNametags plugin;
    // Text contains <tags>!
    @NotNull @Getter String text;
    @Getter List<DarplexComponent> componentList = new ArrayList<>();
    @Getter MiniMessage mm = MiniMessage.miniMessage();
    // DarplexComponent.text("test", plugin).append(Cmp.rainbow("hi"))
    // todo: this is a little strange
    TextAsComponent componentResolver = () -> mm.deserialize(text) ;

    // TODO: Work on better serialization!
    public static DarplexComponent from(@NotNull MiniMessage mm, Component component) {
        return DarplexComponent.text(mm.serialize(component));
    }

    public static DarplexComponent text(String text) {
        return new DarplexComponent(text);
    }

    public interface TextAsComponent {
        Component get();
    }

    public DarplexComponent rainbow(@NotNull DarplexNametags plugin, UUID ownerUUID) {
        return new RainbowComponent(plugin, text, ownerUUID);
    }

    public DarplexComponent color(NamedTextColor color) {
        String colorName = "<" + color.toString().toLowerCase() + ">";
        return DarplexComponent.text(colorName + text);
    }

    public DarplexComponent bold() {
        return DarplexComponent.text("<bold>" + text);
    }

    private void addSpace() {
        componentList.add(DarplexComponent.space());
    }

    public DarplexComponent append(DarplexComponent other) {
        addSpace();
        componentList.add(other);
        return this;
    }

    public static DarplexComponent space() {
        return new DarplexComponent(" ");
    }

    public Component resolve() {
        var base = Component.text().append(componentResolver.get());
        base.append(componentList.stream().map(DarplexComponent::resolve).collect(Collectors.toList()));
        return base.build(); // returns built Component!
    }
}
