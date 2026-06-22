package com.darplex.darplexNametags.component;

import com.darplex.darplexNametags.DarplexNametags;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class DarplexComponent {
//    @NotNull @Getter DarplexNametags plugin;
    // Text contains <tags>!
    @NotNull @Getter String text;
    @Getter List<DarplexComponent> componentList = List.of();
    @Getter MiniMessage mm = MiniMessage.miniMessage();
    // DarplexComponent.text("test", plugin).append(Cmp.rainbow("hi"))
    // todo: this is a little strange
    TextAsComponent componentResolver = () -> mm.deserialize(text) ;

    public interface TextAsComponent {
        Component get();
    }

    public DarplexComponent rainbow(@NotNull DarplexNametags plugin, UUID ownerUUID) {
        return new RainbowComponent(plugin, text, ownerUUID);
    }

    public DarplexComponent colored(NamedTextColor color) {
        return new ColoredComponent(text, color);
    }

    public DarplexComponent append(DarplexComponent other) {
        componentList.add(other);
        return this;
    }

    public DarplexComponent space() {
        return new DarplexComponent(" ");
    }

    // Component resolver may change what the raw text should look like!
    public Component resolve() {
        Component base = componentResolver.get();
        for (var comp : componentList) {
            base = base.append(comp.resolve());
        }
        return base;
    }
}
