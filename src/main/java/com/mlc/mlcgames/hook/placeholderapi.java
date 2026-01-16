package com.mlc.mlcgames.hook;

import com.mlc.mlcgames.Teammanager;
import com.mlc.mlcgames.bank.utils.Jobs;
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

            //%mlcgame_bankgame_remaintime% 游戏剩余时间
            case "bankgame_remaintime": return String.valueOf(bankgame.remainTime);

            //%mlcgame_bankgame_team% 玩家所在队伍
            case "bankgame_team": if(Teammanager.isPlayerInTeam(player, Teammanager.bankgame_thiefteam)){
                return "红队";
            }else if(Teammanager.isPlayerInTeam(player, Teammanager.bankgame_policeteam)){
                return "蓝队";
            }else if(Teammanager.isPlayerInTeam(player, Teammanager.bankgame_spectateteam)){
                return "观战队";
            }else{
                return "未加入队伍";
            }
            //%mlcgame_bankgame_nums% 游戏玩家数量
            case "bankgame_nums": return String.valueOf(bankgame.players.size());

            //%mlcgame_bankgame_thifeteamnum% 贼队玩家数量
            case "bankgame_thifeteamnum": return String.valueOf(Teammanager.bankgame_thiefteam.getEntries().size());

            //%mlcgame_bankgame_pliceteamnum% 警队玩家数量
            case "bankgame_pliceteamnum": return String.valueOf(Teammanager.bankgame_policeteam.getEntries().size());

            //%mlcgame_bankgame_spectateteamnum% 观战队玩家数量
            case "bankgame_spectateteamnum": return String.valueOf(Teammanager.bankgame_spectateteam.getEntries().size());

            //%mlcgame_bankgame_job% 玩家岗位
            case "bankgame_job": return bankgame.playerJobs.getOrDefault(player, Jobs.NONE).toString();

            //%mlcgame_bankgame_gamemode% 游戏模式
            case "bankgame_gamemode": return bankgame.gamemode.toString();

            //%mlcgame_bankgame_policescore% 红队分数
            case "bankgame_policescore": return String.valueOf(bankgame.policescore);

            //%mlcgame_bankgame_thiefscore% 蓝队分数
            case "bankgame_thiefscore": return String.valueOf(bankgame.thiefscore);

            default: return "";
        }
    }

}
