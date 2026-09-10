package com.mlc.mlcgames.dungeongame.managers;

import com.mlc.mlcgames.Teammanager;
import com.mlc.mlcgames.dungeongame.Dungeongame;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * 连接记分板队伍与单局地牢参与者。
 *
 * <p>{@code dungeongame_team} 是“谁有资格参加”的长期来源，
 * {@link Dungeongame#participants} 则记录本局实际被传送过的玩家，供结束游戏时统一撤离。
 * 两者分开后，房间和波次服务不需要了解记分板实现，也不会把已经离队但仍在地牢世界中的玩家漏在卸载世界里。</p>
 */
final class DungeonPartyService {
    /** 围绕触发者安排队友，减少所有人落在同一个坐标后的碰撞。 */
    private static final int[][] RALLY_OFFSETS = {
            {0, 0}, {1, 0}, {-1, 0}, {0, 1}, {0, -1},
            {1, 1}, {-1, 1}, {1, -1}, {-1, -1},
            {2, 0}, {-2, 0}, {0, 2}, {0, -2}
    };

    /** 设置菜单和开始命令只接受准备队或正式地牢队成员。 */
    boolean canAccessLobby(Player player) {
        return dungeonTeam().hasEntry(player.getName()) || prepareTeam().hasEntry(player.getName());
    }

    /**
     * 开局时把准备队的全部条目一次性迁移到正式队伍，包括暂时离线的玩家名。
     * 使用条目快照是为了避免遍历过程中修改记分板队伍集合。
     */
    int promotePreparedPlayers() {
        Team prepared = prepareTeam();
        Team active = dungeonTeam();
        List<String> entries = new ArrayList<>(prepared.getEntries());
        for (String entry : entries) {
            prepared.removeEntry(entry);
            active.addEntry(entry);
            Player player = Bukkit.getPlayerExact(entry);
            if (player != null) player.setScoreboard(Teammanager.scoreboard);
        }
        return entries.size();
    }

    /** 只有记分板地牢队伍成员可以触发房间、传送门和胜利流程。 */
    boolean isMember(Player player) {
        return dungeonTeam().hasEntry(player.getName());
    }

    /** 只把已经实际进入本局、仍在地牢世界中的玩家视为可承担战斗的队友。 */
    Player findLivingTeammate(Player excluded, Set<UUID> awaitingRevival) {
        for (Player player : onlineMembers()) {
            if (player.equals(excluded)
                    || awaitingRevival.contains(player.getUniqueId())
                    || !Dungeongame.participants.contains(player)
                    || player.getWorld() != Worldmanager.dungeonWorld
                    || player.isDead()
                    || player.getGameMode() == GameMode.SPECTATOR) {
                continue;
            }
            return player;
        }
        return null;
    }

    /**
     * 地牢生成完成后，将当前在线的全部队员送入第一层。
     * 返回成功传送的玩家快照，调用方可以在下一 tick 对他们执行位置检测。
     */
    List<Player> enterOnlineTeam(Location start) {
        List<Player> entered = new ArrayList<>();
        for (Player player : onlineMembers()) {
            if (!player.teleport(start.clone())) continue;
            Dungeongame.participants.add(player);
            player.sendMessage("§a队伍已进入地牢第 1 关，第 1 层。");
            entered.add(player);
        }
        return List.copyOf(entered);
    }

    /** 允许中途上线的队员通过进入命令补进当前局，但拒绝非队员。 */
    boolean enterMember(Player player, Location start) {
        if (!isMember(player)) return false;
        if (!player.teleport(start.clone())) return false;
        Dungeongame.participants.add(player);
        return true;
    }

    /**
     * 新遭遇激活前，把所有在线队员强制集结到触发者所在房间。
     * 已经位于该房间的玩家保持原位；其余玩家优先落在触发者附近可站立的位置，
     * 找不到合适偏移时回退到触发者的原坐标，确保不会被留在门外或其他世界。
     */
    void rallyForEncounter(Player trigger, Worldmanager.RoomBounds room) {
        Location anchor = trigger.getLocation().clone();
        Dungeongame.participants.add(trigger);

        int destinationIndex = 0;
        for (Player member : onlineMembers()) {
            if (member.equals(trigger) || room.contains(member.getLocation())) {
                Dungeongame.participants.add(member);
                continue;
            }

            Location destination = findRallyLocation(anchor, room, destinationIndex++);
            if (member.teleport(destination)) {
                Dungeongame.participants.add(member);
                member.sendMessage("§e队友触发了新房间，你已被传送到队伍身边。");
            } else {
                member.sendMessage("§c无法传送到新房间，请重新进入地牢。");
            }
        }
    }

    /** 只解析精确在线玩家名，避免 Bukkit 的模糊名称匹配把其他玩家错误拉入地牢。 */
    private List<Player> onlineMembers() {
        List<Player> players = new ArrayList<>();
        for (String entry : dungeonTeam().getEntries()) {
            Player player = Bukkit.getPlayerExact(entry);
            if (player != null && player.isOnline()) players.add(player);
        }
        return players;
    }

    private Location findRallyLocation(Location anchor, Worldmanager.RoomBounds room, int index) {
        for (int attempt = 0; attempt < RALLY_OFFSETS.length; attempt++) {
            int[] offset = RALLY_OFFSETS[(index + attempt) % RALLY_OFFSETS.length];
            Location candidate = anchor.clone().add(offset[0], 0.0, offset[1]);
            if (room.contains(candidate) && isSafeStandingLocation(candidate)) return candidate;
        }
        return anchor;
    }

    /** 脚下必须有实体方块，脚部和头部空间必须可通过。 */
    private boolean isSafeStandingLocation(Location location) {
        Block feet = location.getBlock();
        return feet.isPassable()
                && feet.getRelative(0, 1, 0).isPassable()
                && feet.getRelative(0, -1, 0).getType().isSolid();
    }

    private Team dungeonTeam() {
        Team team = Teammanager.dungeongame_team;
        if (team == null) {
            throw new IllegalStateException("Teammanager 尚未初始化 dungeongame_team");
        }
        return team;
    }

    private Team prepareTeam() {
        Team team = Teammanager.dungeongame_prepareteam;
        if (team == null) {
            throw new IllegalStateException("Teammanager 尚未初始化 dungeongame_prepareteam");
        }
        return team;
    }
}
