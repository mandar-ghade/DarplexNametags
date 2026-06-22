package com.darplex.darplexNametags.component;

import com.darplex.darplexNametags.DarplexNametags;
import com.darplex.darplexNametags.counters.RainbowGradient;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.logging.Level;

// lazily ticking rainbow!
public class RainbowComponent extends DarplexComponent {

    @Getter UUID userUUID;
    @NotNull @Getter DarplexNametags plugin;

    public RainbowComponent(@NotNull DarplexNametags plugin, @NotNull String text, @NotNull UUID userUUID) {
        super(text);
        this.plugin = plugin;
        this.userUUID = userUUID;
        this.componentResolver = () -> mm.deserialize(getTextWithTags());
    }

    private RainbowGradient tickRainbowIfAbsent() {
//        log("Ticking new rainbow!");
        RainbowGradient rainbow = new RainbowGradient(getPlugin());
        getPlugin().getCounterManager().add(userUUID, rainbow);
        rainbow.start();
        return rainbow;
    }

    private RainbowGradient getGradient() {
        return getPlugin().getCounterManager().get(userUUID, RainbowGradient.class)
                .orElse(tickRainbowIfAbsent());
    }

    private String getRainbowReplacementStr(RainbowGradient r) {
        return "<rainbow:" + r.getTick() + ">";
    }

    private void log(String msg) {
        getPlugin().getLogger().log(Level.INFO, "RainbowComponent>> " + msg);
    }

    private String getTextWithTags() {
        return getRainbowReplacementStr(getGradient()) + text;
    }
}
