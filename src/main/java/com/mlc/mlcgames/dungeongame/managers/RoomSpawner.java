package com.mlc.mlcgames.dungeongame.managers;

import com.mlc.mlcgames.dungeongame.rooms.Room;
import com.mlc.mlcgames.dungeongame.rooms.RoomType;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/** Implements the same chunk-grid layout rules used by the web prototype. */
public final class RoomSpawner {
    private static final int MAP_LIMIT = 44;
    private static final int[][] DELTAS = {{0, -1}, {1, 0}, {0, 1}, {-1, 0}};

    public static final class Locpoint {
        public final int x;
        public final int y; // Logical Z coordinate; retained for compatibility.

        public Locpoint(int x, int z) {
            this.x = x;
            this.y = z;
        }

        @Override public boolean equals(Object other) {
            return other instanceof Locpoint point && x == point.x && y == point.y;
        }
        @Override public int hashCode() { return Objects.hash(x, y); }
    }

    public record Footprint(int width, int depth) { }
    public record Connection(Room from, Room to, Roommanager.BridgeAxis axis,
                             int directionX, int directionZ, List<Locpoint> corridor) { }
    private record PlannedBridge(Room room, Connection connection) { }

    private enum Kind { START, NORMAL, SPECIAL, END, BOSS }
    private enum Direction {
        N(0, -1), E(1, 0), S(0, 1), W(-1, 0);
        final int dx;
        final int dz;
        Direction(int dx, int dz) { this.dx = dx; this.dz = dz; }
        Roommanager.BridgeAxis axis() { return dx != 0 ? Roommanager.BridgeAxis.X : Roommanager.BridgeAxis.Z; }
    }

    private record LayoutRoom(Kind kind, int x, int z, int width, int depth) { }
    private record LayoutLink(LayoutRoom from, LayoutRoom to, Direction direction, List<Locpoint> corridor) { }
    private record Candidate(LayoutRoom room, Direction direction, List<Locpoint> corridor) { }
    private record Plan(List<LayoutRoom> rooms, List<Locpoint> corridors, List<LayoutLink> links) {
        Plan add(LayoutRoom source, Candidate candidate) {
            List<LayoutRoom> nextRooms = new ArrayList<>(rooms);
            nextRooms.add(candidate.room());
            List<Locpoint> nextCorridors = new ArrayList<>(corridors);
            nextCorridors.addAll(candidate.corridor());
            List<LayoutLink> nextLinks = new ArrayList<>(links);
            nextLinks.add(new LayoutLink(source, candidate.room(), candidate.direction(), candidate.corridor()));
            return new Plan(nextRooms, nextCorridors, nextLinks);
        }
    }

    public static int floortype;
    public static int mapsize;
    public static int specialroomcount;
    public static int[][] roomMap;
    public static final List<Room> roomList = new ArrayList<>();
    public static final Map<Room, Locpoint> roompointMap = new LinkedHashMap<>();
    public static final Map<Room, Footprint> roomSizeMap = new LinkedHashMap<>();
    public static final List<Connection> connections = new ArrayList<>();
    private static int nextRoomId;

    private RoomSpawner() { }

    public static void generateRooms(int normalRoomCount, int floorType) {
        generateRooms(normalRoomCount, floorType, 65, ThreadLocalRandom.current());
    }

    public static void generateRooms(int normalRoomCount, int floorType, int specialChance, Random random) {
        if (normalRoomCount < 4) throw new IllegalArgumentException("A dungeon needs at least four normal rooms");
        if (specialChance < 0 || specialChance > 100) throw new IllegalArgumentException("specialChance must be between 0 and 100");
        Objects.requireNonNull(random, "random");

        floortype = floorType;
        mapsize = normalRoomCount;
        nextRoomId = 1;
        LayoutRoom start = new LayoutRoom(Kind.START, -1, -1, 2, 2);
        Plan initial = new Plan(List.of(start), List.of(), List.of());

        Plan plan = buildMainRoute(initial, start, normalRoomCount, floorType == 5, 0, 2, random);
        if (plan == null) plan = buildMainRoute(initial, start, normalRoomCount, floorType == 5, 0, 3, random);
        if (plan == null) throw new IllegalStateException("Unable to generate the dungeon main route");

        List<LayoutRoom> normalRooms = plan.rooms().stream().filter(room -> room.kind() == Kind.NORMAL).toList();
        if (floorType < 5) {
            Plan mainPlan = plan;
            plan = attachExit(mainPlan, normalRooms, 2, random);
            if (plan == null) plan = attachExit(mainPlan, normalRooms, 3, random);
            if (plan == null) throw new IllegalStateException("Unable to attach the floor exit");
            normalRooms = plan.rooms().stream().filter(room -> room.kind() == Kind.NORMAL).toList();
        }

        int requestedSpecials = Math.min(normalRoomCount, 2);
        for (int index = 2; index < normalRoomCount; index++) {
            if (random.nextInt(100) < specialChance) requestedSpecials++;
        }
        requestedSpecials = Math.min(requestedSpecials, normalRoomCount);

        Plan basePlan = plan;
        Plan withSpecials = null;
        int actualSpecials = requestedSpecials;
        while (withSpecials == null && actualSpecials >= 2) {
            withSpecials = attachSpecials(basePlan, normalRooms, 0, actualSpecials, 2, random);
            if (withSpecials == null) withSpecials = attachSpecials(basePlan, normalRooms, 0, actualSpecials, 3, random);
            if (withSpecials == null) actualSpecials--;
        }
        if (withSpecials == null) throw new IllegalStateException("Unable to place the two required special rooms");
        specialroomcount = actualSpecials;
        materialize(withSpecials, floorType, random);
    }

    private static Plan buildMainRoute(Plan plan, LayoutRoom current, int normalCount, boolean bossFloor,
                                       int index, int maxCorridor, Random random) {
        int targetCount = normalCount + (bossFloor ? 1 : 0);
        if (index >= targetCount) return plan;
        Kind kind = bossFloor && index == normalCount ? Kind.BOSS : Kind.NORMAL;
        for (Candidate candidate : createOptions(current, kind, maxCorridor, random)) {
            if (!canPlace(plan, current, candidate)) continue;
            Plan solved = buildMainRoute(plan.add(current, candidate), candidate.room(), normalCount,
                    bossFloor, index + 1, maxCorridor, random);
            if (solved != null) return solved;
        }
        return null;
    }

    private static Plan attachExit(Plan plan, List<LayoutRoom> normalRooms, int maxCorridor, Random random) {
        if (plan == null) return null;
        List<LayoutRoom> sources = shuffled(normalRooms, random);
        for (LayoutRoom source : sources) {
            for (Candidate candidate : createOptions(source, Kind.END, maxCorridor, random)) {
                if (canPlace(plan, source, candidate)) return plan.add(source, candidate);
            }
        }
        return null;
    }

    private static Plan attachSpecials(Plan plan, List<LayoutRoom> normals, int index, int count,
                                       int maxCorridor, Random random) {
        if (index >= count) return plan;
        Set<LayoutRoom> used = new HashSet<>();
        for (LayoutLink link : plan.links()) if (link.to().kind() == Kind.SPECIAL) used.add(link.from());
        List<LayoutRoom> sources = shuffled(normals, random);
        sources.sort(Comparator.comparing(used::contains));
        for (LayoutRoom source : sources) {
            for (Candidate candidate : createOptions(source, Kind.SPECIAL, maxCorridor, random)) {
                if (!canPlace(plan, source, candidate)) continue;
                Plan solved = attachSpecials(plan.add(source, candidate), normals, index + 1, count, maxCorridor, random);
                if (solved != null) return solved;
            }
        }
        return null;
    }

    private static List<Candidate> createOptions(LayoutRoom source, Kind kind, int maxLength, Random random) {
        int size = kind == Kind.BOSS ? 3 : 2;
        List<Candidate> options = new ArrayList<>();
        for (Direction direction : shuffled(Arrays.asList(Direction.values()), random)) {
            int sourceSpan = direction.dx != 0 ? source.depth() : source.width();
            for (int sourceOffset : shuffled(range(sourceSpan), random)) {
                for (int length : shuffled(rangeFromOne(maxLength), random)) {
                    Locpoint port = sourcePort(source, direction, sourceOffset);
                    List<Locpoint> corridor = new ArrayList<>();
                    for (int step = 1; step <= length; step++) {
                        corridor.add(new Locpoint(port.x + direction.dx * step, port.y + direction.dz * step));
                    }
                    List<Integer> targetOffsets = kind == Kind.BOSS ? List.of(1) : range(size);
                    for (int targetOffset : shuffled(targetOffsets, random)) {
                        options.add(new Candidate(roomFromEndpoint(corridor.getLast(), direction, targetOffset, kind, size),
                                direction, List.copyOf(corridor)));
                    }
                }
            }
        }
        List<Candidate> shortOptions = options.stream().filter(option -> option.corridor().size() <= 2).toList();
        List<Candidate> longOptions = options.stream().filter(option -> option.corridor().size() == 3).toList();
        List<Candidate> ordered = new ArrayList<>(shuffled(shortOptions, random));
        ordered.addAll(shuffled(longOptions, random));
        return ordered;
    }

    private static Locpoint sourcePort(LayoutRoom room, Direction direction, int offset) {
        return switch (direction) {
            case N -> new Locpoint(room.x() + offset, room.z());
            case S -> new Locpoint(room.x() + offset, room.z() + room.depth() - 1);
            case E -> new Locpoint(room.x() + room.width() - 1, room.z() + offset);
            case W -> new Locpoint(room.x(), room.z() + offset);
        };
    }

    private static LayoutRoom roomFromEndpoint(Locpoint endpoint, Direction direction, int targetOffset, Kind kind, int size) {
        int boundaryX = endpoint.x + direction.dx;
        int boundaryZ = endpoint.y + direction.dz;
        return switch (direction) {
            case N -> new LayoutRoom(kind, boundaryX - targetOffset, boundaryZ - size + 1, size, size);
            case S -> new LayoutRoom(kind, boundaryX - targetOffset, boundaryZ, size, size);
            case E -> new LayoutRoom(kind, boundaryX, boundaryZ - targetOffset, size, size);
            case W -> new LayoutRoom(kind, boundaryX - size + 1, boundaryZ - targetOffset, size, size);
        };
    }

    private static boolean canPlace(Plan plan, LayoutRoom source, Candidate candidate) {
        Map<Long, LayoutRoom> roomByCell = new HashMap<>();
        for (LayoutRoom room : plan.rooms()) for (Locpoint cell : roomCells(room)) roomByCell.put(key(cell), room);
        Set<Long> oldCorridors = new HashSet<>();
        for (Locpoint cell : plan.corridors()) oldCorridors.add(key(cell));
        Set<Long> newCorridors = new HashSet<>();

        for (int index = 0; index < candidate.corridor().size(); index++) {
            Locpoint cell = candidate.corridor().get(index);
            long cellKey = key(cell);
            if (outside(cell) || roomByCell.containsKey(cellKey) || oldCorridors.contains(cellKey) || !newCorridors.add(cellKey)) return false;
            for (int[] delta : DELTAS) {
                long neighbour = key(cell.x + delta[0], cell.y + delta[1]);
                LayoutRoom touching = roomByCell.get(neighbour);
                if (touching != null && !(index == 0 && touching == source)) return false;
                if (oldCorridors.contains(neighbour)) return false;
            }
        }

        long lastCorridor = key(candidate.corridor().getLast());
        for (Locpoint cell : roomCells(candidate.room())) {
            long cellKey = key(cell);
            if (outside(cell) || roomByCell.containsKey(cellKey) || oldCorridors.contains(cellKey)
                    || newCorridors.contains(cellKey)) return false;
            for (int[] delta : DELTAS) {
                long neighbour = key(cell.x + delta[0], cell.y + delta[1]);
                if (roomByCell.containsKey(neighbour) || oldCorridors.contains(neighbour)) return false;
                if (newCorridors.contains(neighbour) && neighbour != lastCorridor) return false;
            }
        }
        return true;
    }

    private static void materialize(Plan plan, int floorType, Random random) {
        roomList.clear();
        roompointMap.clear();
        roomSizeMap.clear();
        connections.clear();
        Map<LayoutRoom, Room> instances = new IdentityHashMap<>();
        for (LayoutRoom layout : plan.rooms()) {
            Room template = switch (layout.kind()) {
                case START -> Roommanager.getStartRoom(floorType, random);
                case NORMAL -> Roommanager.getNormalRoom(floorType, random);
                case SPECIAL -> Roommanager.getSpecialRoom(floorType, random);
                case END -> Roommanager.getEndRoom(floorType, random);
                case BOSS -> Roommanager.getBossRoom(floorType, random);
            };
            Room room = template.createInstance(nextRoomId++);
            instances.put(layout, room);
            roomList.add(room);
            roompointMap.put(room, new Locpoint(layout.x(), layout.z()));
            roomSizeMap.put(room, new Footprint(layout.width(), layout.depth()));
        }
        for (LayoutLink link : plan.links()) {
            connections.add(new Connection(instances.get(link.from()), instances.get(link.to()),
                    link.direction().axis(), link.direction().dx, link.direction().dz, link.corridor()));
        }
        rebuildLegacyMap(plan);

        List<PlannedBridge> bridges = new ArrayList<>();
        Worldmanager.validatePassageConfig();
        for (Room room : roomList) Worldmanager.validateRoomTemplate(room);
        for (Connection connection : connections) {
            Room bridge = Roommanager.getBridgeRoom(floorType, connection.axis(), random);
            Worldmanager.validateBridgeTemplate(bridge, connection);
            bridges.add(new PlannedBridge(bridge, connection));
        }
        if (Worldmanager.dungeonWorld != null) {
            for (Room room : roomList) Worldmanager.putRoomInWorld(room, roompointMap.get(room));
            for (PlannedBridge bridge : bridges) Worldmanager.putBridgeInWorld(bridge.room(), bridge.connection());
            for (Connection connection : connections) Worldmanager.openConnection(connection);
        }
    }

    private static void rebuildLegacyMap(Plan plan) {
        roomMap = new int[MAP_LIMIT * 2 + 1][MAP_LIMIT * 2 + 1];
        for (LayoutRoom room : plan.rooms()) for (Locpoint cell : roomCells(room)) mark(cell);
        for (Locpoint cell : plan.corridors()) mark(cell);
    }

    private static void mark(Locpoint cell) { roomMap[cell.x + MAP_LIMIT][cell.y + MAP_LIMIT] = 1; }
    public static boolean isRoomMapfree(int x, int z) {
        return Math.abs(x) <= MAP_LIMIT && Math.abs(z) <= MAP_LIMIT && roomMap[x + MAP_LIMIT][z + MAP_LIMIT] == 0;
    }
    public static Optional<Room> getGeneratedStartRoom() {
        return roomList.stream().filter(room -> room.getType() == RoomType.Start).findFirst();
    }

    private static List<Locpoint> roomCells(LayoutRoom room) {
        List<Locpoint> cells = new ArrayList<>(room.width() * room.depth());
        for (int x = room.x(); x < room.x() + room.width(); x++)
            for (int z = room.z(); z < room.z() + room.depth(); z++) cells.add(new Locpoint(x, z));
        return cells;
    }
    private static long key(Locpoint cell) { return key(cell.x, cell.y); }
    private static long key(int x, int z) { return ((long) x << 32) ^ (z & 0xffffffffL); }
    private static boolean outside(Locpoint cell) { return Math.abs(cell.x) > MAP_LIMIT || Math.abs(cell.y) > MAP_LIMIT; }
    private static List<Integer> range(int count) { List<Integer> values = new ArrayList<>(); for (int i = 0; i < count; i++) values.add(i); return values; }
    private static List<Integer> rangeFromOne(int count) { List<Integer> values = new ArrayList<>(); for (int i = 1; i <= count; i++) values.add(i); return values; }
    private static <T> List<T> shuffled(Collection<T> values, Random random) { List<T> result = new ArrayList<>(values); Collections.shuffle(result, random); return result; }
}
