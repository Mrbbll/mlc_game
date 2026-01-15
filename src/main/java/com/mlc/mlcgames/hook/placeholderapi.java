package com.mlc.mlcgames.hook;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static com.mlc.mlcgames.Mlcgames.bankgame;

public class placeholderapi extends PlaceholderExpansion {

    @Override
    public @NotNull String getIdentifier() {
        return "mlcgame";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Mr_bl";
    }

    @Override
    public @NotNull String getVersion() {
        return "";
    }

    @Override
    public @NotNull String onPlaceholderRequest(Player player, @NotNull String params){
        if(player==null){
            return "";
        }

        switch (params){
            case "bankgame_remaintime": return String.valueOf(bankgame.remainTime);



            default: return "";
        }
    }

}
