# MLCGame

MLCGame 是一个面向 Paper 服务端的 Minecraft 小游戏插件，包含既有玩法、独立派对小游戏、地牢生成玩法和 Blue Archive 风格的战斗属性相性系统。

## 功能

- **派对小游戏**：通过 `/party` 管理独立小游戏会话；可用游戏 ID 和分类在游戏内查看。
- **既有玩法**：银行游戏、僵尸末日、沙子游戏和地牢游戏。
- **战斗相性**：按攻击类型与头盔防御类型计算伤害倍率；支持配置相性矩阵、伤害浮字和管理命令。
- **地牢生成实验台**：`web/` 下的网页原型用于演示地牢布局算法，不会修改插件 Java 源码。

派对小游戏的地图配置模板会在插件启动时生成。新地图的坐标默认留空，配置并通过地图检查前不能开局；插件不附带可直接游玩的派对地图。

## 环境要求


- Paper 服务端；插件元数据中的 `api-version` 为 `26.3`。
- Java 25。
- **FastAsyncWorldEdit（FAWE）**：插件声明为必需依赖。

Gradle 使用 Paper API 26.3 构建。其他软依赖请以 `src/main/resources/plugin.yml` 中的声明为准。

## 构建

在仓库根目录运行：

```powershell
.\gradlew.bat shadowJar
```

插件 JAR 输出到：

```text
build/libs/mlcgames-1.0-SNAPSHOT-all.jar
```

## 安装与首次配置

1. 在服务器安装 FAWE。
2. 将 `mlcgames-1.0-SNAPSHOT-all.jar` 放入服务器的 `plugins/` 目录并启动服务器。
3. 编辑插件数据目录中的配置。派对小游戏配置位于 `party/<分类>/<游戏 ID>/`，每个游戏包含 `language.yml`、`locations.yml` 和 `game.yml`。
4. 按对应玩法填写地图世界、边界、出生点和特殊区域。用 `/party check <游戏 ID> [地图名]` 检查配置，再加入队列并开局。

更多地图坐标规范、玩法特殊配置和实测步骤见[派对小游戏使用指南](docs/party/USAGE.md)。`/party reload` 只能在没有进行中会话时执行；地图改动后建议先结束当前局，再检查配置。

## 常用命令

### 派对小游戏

```text
/party list
/party join <游戏 ID> [地图名]
/party leave
/party check <游戏 ID> [地图名]
/party start
/party stop
/party reload
```

一次只运行一个派对会话。普通玩家需要 `mlcgames.party.play`（默认所有人）；检查配置、开局、结束和重载需要 `mlcgames.party.admin`（默认 OP）。地图名省略时使用 `arena_1`。

### 战斗属性管理

```text
/mlc armor <get|set|clear>
/mlc attacktype <get|set|clear>
/mlc combat debug
```

`armor` 操作准星指向的 10 格内实体的头盔属性；`attacktype` 操作主手物品，类型为 `none`、`explosive`、`piercing`、`mystic` 或 `sonic`。需要 `mlcgames.combat.admin`（默认 OP）。

## 配置与文档

以下默认配置以 `src/main/resources/` 为模板，运行时从插件数据目录读取：

- `config.yml`：通用配置和银行游戏设置。
- `combat.yml`：战斗相性开关、默认攻击类型、相性矩阵和倍率。
- `damage-indicator.yml`：相性伤害浮字样式与显示范围。
- `zombieday.yml`、`sandgame.yml`、`dungeongame.yml`：对应玩法配置。
- `docs/party/USAGE.md`：派对小游戏安装、地图配置和命令说明。
- `docs/party/IMPLEMENTATION.md`：当前派对模块实现范围与验证边界。
- `Dungeongame.md`：地牢流程、配置和地图资源说明。
- `Damage.md`：战斗伤害计算说明。
- `web/README.md`：地牢布局网页原型的启动方法。
