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
    @NotNull @Getter RainbowGradient.Speed speed;

    public RainbowComponent(@NotNull DarplexNametags plugin,
                            @NotNull String text,
                            @NotNull UUID userUUID,
                            @NotNull RainbowGradient.Speed speed) {
        super(text);
        this.userUUID = userUUID;
        this.plugin = plugin;
        this.componentResolver = () -> mm.deserialize(getTextWithTags());
        this.speed = speed;
    }

    private RainbowGradient tickRainbowIfAbsent() {
//        log("Ticking new rainbow!");
        RainbowGradient rainbow = new RainbowGradient(getPlugin(), getSpeed());
        getPlugin().getCounterManager().add(userUUID, rainbow);
        rainbow.start();
        return rainbow;
    }

    private RainbowGradient getGradient() {
        return getPlugin().getCounterManager().get(userUUID, RainbowGradient.class)
                .orElse(tickRainbowIfAbsent());
    }

//    private void stopIfRunning(RainbowGradient gradient) {
//        if (!gradient.isCancelled()) {
//            gradient.stop();
//        }
//    }
//
//    public void setSpeed(RainbowGradient.Speed newSpeed) {
//        if (speed == newSpeed) {
//            return;
//        }
//
//        RainbowGradient gradient = getGradient();
//        stopIfRunning(gradient);
//        gradient.setSpeed(newSpeed);
//        gradient.start();
//
//        speed = newSpeed;
//    }
//
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
