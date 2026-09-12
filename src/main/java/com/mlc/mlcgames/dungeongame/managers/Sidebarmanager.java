package com.mlc.mlcgames.dungeongame.managers;

import com.mlc.mlcgames.Gamesidebar;
import com.mlc.mlcgames.Mlcgames;
import com.mlc.mlcgames.combat.integration.craftengine.CraftEngineAttributeBridge;
import com.mlc.mlcgames.combat.integration.craftengine.CraftEngineHook;
import com.mlc.mlcgames.dungeongame.Dungeongame;
import io.papermc.paper.scoreboard.numbers.NumberFormat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.RenderType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.UUID;

/**
 * 地牢专用的“每玩家独立”侧边栏。
 *
 * <p>其他小游戏直接修改主计分板，因此所有玩家只能看到同一份内容。地牢属性来自玩家自身，
 * 必须为每位玩家创建独立 {@link Scoreboard}。调用 {@link Player#setScoreboard(Scoreboard)} 后，
 * Bukkit 会只向该玩家发送目标和分数数据包，效果等价于手写发包，同时避免绑定具体协议版本。</p>
 *
 * <p>玩家离开地牢或游戏结束时，通过 {@link Gamesidebar#showsidebar(Player)} 恢复项目默认侧边栏。
 * 属性行从 {@code dungeongame.yml/sidebar.attributes} 读取，可继续扩展任意 CraftEngine 属性 ID。</p>
 */
public final class Sidebarmanager {
    private static final int MAX_LINES = 15;
    private static final int FIXED_LINES = 3;

    private final JavaPlugin plugin;
    private final CraftEngineAttributeBridge attributes;
    private final Map<UUID, SidebarView> views = new HashMap<>();
    private List<AttributeLine> attributeLines = List.of();
    private Component title = Component.text("地牢属性", NamedTextColor.GOLD);
    private BukkitTask refreshTask;
    private long refreshTicks = 10L;

    public Sidebarmanager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.attributes = new CraftEngineAttributeBridge(plugin, new CraftEngineHook());
    }

    /** 读取最新配置并开始刷新；重复调用不会创建第二个任务。 */
    public void start() {
        if (refreshTask != null) return;
        loadConfiguration();
        refreshTask = Bukkit.getScheduler().runTaskTimer(plugin, this::refreshAll, 0L, refreshTicks);
    }

    /** `/dungeongame reload` 后重建现有私人视图，使属性行和刷新间隔立即生效。 */
    public void reloadConfiguration() {
        boolean running = refreshTask != null;
        if (refreshTask != null) refreshTask.cancel();
        refreshTask = null;
        loadConfiguration();

        List<Player> visiblePlayers = new ArrayList<>();
        for (UUID uuid : views.keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()
                    && player.getWorld() == Worldmanager.dungeonWorld) {
                visiblePlayers.add(player);
            } else if (player != null && player.isOnline()) {
                Gamesidebar.showsidebar(player);
            }
        }
        views.clear();
        if (!running) return;

        refreshTask = Bukkit.getScheduler().runTaskTimer(plugin, this::refreshAll, 0L, refreshTicks);
        for (Player player : visiblePlayers) show(player);
    }

    /** 立即为一个已经进入地牢的玩家显示并填充其私人侧边栏。 */
    public void show(Player player) {
        if (!player.isOnline() || player.getWorld() != Worldmanager.dungeonWorld) return;
        SidebarView view = views.computeIfAbsent(player.getUniqueId(), ignored -> createView());
        if (player.getScoreboard() != view.scoreboard()) player.setScoreboard(view.scoreboard());
        update(player, view);
    }

    /** 玩家主动离开地牢时删除私人视图并恢复默认 MLC Games 侧边栏。 */
    public void hide(Player player) {
        if (views.remove(player.getUniqueId()) == null || !player.isOnline()) return;
        Gamesidebar.showsidebar(player);
    }

    /** 停止刷新，并为仍在线的所有地牢玩家恢复默认侧边栏。 */
    public void stopAndRestore() {
        if (refreshTask != null) {
            refreshTask.cancel();
            refreshTask = null;
        }
        for (UUID uuid : new ArrayList<>(views.keySet())) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) Gamesidebar.showsidebar(player);
        }
        views.clear();
    }

    private void refreshAll() {
        // participants 只保存实际进入过本局的玩家；世界判断确保被传送出去后立即恢复默认显示。
        for (Player player : new ArrayList<>(Dungeongame.participants)) {
            if (player.isOnline() && player.getWorld() == Worldmanager.dungeonWorld) show(player);
        }
        for (UUID uuid : new ArrayList<>(views.keySet())) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline()) {
                views.remove(uuid);
            } else if (player.getWorld() != Worldmanager.dungeonWorld) {
                hide(player);
            }
        }
    }

    private SidebarView createView() {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective(
                "dungeon_stats", Criteria.DUMMY, title, RenderType.INTEGER);
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.numberFormat(NumberFormat.blank());

        int lineCount = Math.min(MAX_LINES, FIXED_LINES + attributeLines.size());
        List<Team> teams = new ArrayList<>(lineCount);
        for (int index = 0; index < lineCount; index++) {
            String entry = "§" + Integer.toHexString(index);
            Team team = scoreboard.registerNewTeam("dg_line_" + index);
            team.addEntry(entry);
            objective.getScore(entry).setScore(lineCount - index);
            teams.add(team);
        }
        return new SidebarView(scoreboard, List.copyOf(teams));
    }

    private void update(Player player, SidebarView view) {
        List<Team> teams = view.lineTeams();
        teams.get(0).prefix(Component.text("玩家：", NamedTextColor.GRAY)
                .append(Component.text(player.getName(), NamedTextColor.WHITE)));
        teams.get(1).prefix(Component.text("进度：", NamedTextColor.GRAY)
                .append(Component.text(Dungeongame.currentLevel + "-" + Dungeongame.currentFloor,
                        NamedTextColor.AQUA)));
        teams.get(2).prefix(Component.empty());

        int availableRows = Math.min(attributeLines.size(), teams.size() - FIXED_LINES);
        for (int index = 0; index < availableRows; index++) {
            AttributeLine row = attributeLines.get(index);
            // 展示实际攻击读数：常驻 entity 修饰符 + 当前主手 weapon 修饰符。
            // 切换快捷栏物品后，下一次定时刷新就会显示新武器的 CE 属性。
            OptionalDouble value = attributes.getCombatValue(player, row.attributeId());
            Component renderedValue = value.isPresent()
                    ? Component.text(row.format().render(value.getAsDouble()), NamedTextColor.AQUA)
                    : Component.text("--", NamedTextColor.DARK_GRAY);
            teams.get(FIXED_LINES + index).prefix(row.label().append(renderedValue));
        }
    }

    private void loadConfiguration() {
        FileConfiguration config = Mlcgames.dungeonConfiguration;
        refreshTicks = clamp(config.getLong("sidebar.refresh-ticks", 10L), 5L, 200L);
        title = Mlcgames.miniMessage.deserialize(
                config.getString("sidebar.title", "<gold><bold>地牢属性"));

        List<AttributeLine> loaded = new ArrayList<>();
        for (Map<?, ?> row : config.getMapList("sidebar.attributes")) {
            String id = stringValue(row.get("id"));
            if (id.isBlank()) continue;
            String label = stringValue(row.get("label"));
            if (label.isBlank()) label = id + "：";
            ValueFormat format = ValueFormat.parse(stringValue(row.get("format")));
            loaded.add(new AttributeLine(Mlcgames.miniMessage.deserialize(label), id, format));
            if (loaded.size() >= MAX_LINES - FIXED_LINES) break;
        }
        attributeLines = loaded.isEmpty() ? defaultAttributeLines() : List.copyOf(loaded);
    }

    private List<AttributeLine> defaultAttributeLines() {
        return List.of(
                line("<gray>命中值：", "mlcgame:accuracy", ValueFormat.NUMBER),
                line("<gray>闪避值：", "mlcgame:evasion", ValueFormat.NUMBER),
                line("<gray>暴击率：", "mlcgame:critical_chance", ValueFormat.PERCENT),
                line("<gray>暴击抗性：", "mlcgame:critical_resistance", ValueFormat.PERCENT),
                line("<gray>暴击倍率：", "mlcgame:critical_multiplier", ValueFormat.MULTIPLIER),
                line("<gray>伤害倍率：", "mlcgame:damage_bonus", ValueFormat.BONUS_MULTIPLIER),
                line("<gray>伤害减免：", "mlcgame:damage_reduction", ValueFormat.PERCENT)
        );
    }

    private AttributeLine line(String label, String id, ValueFormat format) {
        return new AttributeLine(Mlcgames.miniMessage.deserialize(label), id, format);
    }

    private static String stringValue(Object value) {
        return value == null ? "" : value.toString().trim();
    }

    private static long clamp(long value, long min, long max) {
        return Math.max(min, Math.min(max, value));
    }

    private record SidebarView(Scoreboard scoreboard, List<Team> lineTeams) { }

    private record AttributeLine(Component label, String attributeId, ValueFormat format) { }

    /** 配置中的数值仅负责显示换算，不会参与 CraftEngine 的实际伤害公式。 */
    private enum ValueFormat {
        NUMBER {
            @Override String render(double value) { return decimal(value); }
        },
        PERCENT {
            @Override String render(double value) { return decimal(value * 100.0) + "%"; }
        },
        MULTIPLIER {
            @Override String render(double value) { return "x" + decimal(value); }
        },
        BONUS_MULTIPLIER {
            @Override String render(double value) { return "x" + decimal(1.0 + value); }
        };

        abstract String render(double value);

        static ValueFormat parse(String value) {
            if (value.isBlank()) return NUMBER;
            try {
                return valueOf(value.toUpperCase(Locale.ROOT).replace('-', '_'));
            } catch (IllegalArgumentException ignored) {
                return NUMBER;
            }
        }

        static String decimal(double value) {
            if (!Double.isFinite(value)) return "--";
            return String.format(Locale.ROOT, "%.2f", value)
                    .replaceAll("0+$", "")
                    .replaceAll("\\.$", "");
        }
    }
}
