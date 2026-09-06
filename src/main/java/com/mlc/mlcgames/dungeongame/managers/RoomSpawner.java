package com.mlc.mlcgames.dungeongame.managers;

import com.mlc.mlcgames.dungeongame.rooms.Room;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Builds a complete connected layout before placing templates in the world.
 * The main route is backtracked and non-overlapping; start, exit and special
 * rooms are leaves, so special rooms never become part of the main route.
 */
public final class RoomSpawner {
    public static final class Locpoint {
        public final int x;
        public final int y;

        public Locpoint(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof Locpoint point && x == point.x && y == point.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }

    /** A logical door-to-door connection for bridge placement or a minimap. */
    public record Connection(Room from, Room to) { }

    public static int floortype;
    /** Requested number of normal rooms for the current floor. */
    public static int mapsize;
    public static int specialroomcount;
    public static int[][] roomMap;
    public static final List<Room> roomList = new ArrayList<>();
    public static final Map<Room, Locpoint> roompointMap = new LinkedHashMap<>();
    public static final List<Connection> connections = new ArrayList<>();

    private static final int[][] DIRECTIONS = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};
    private static int layoutSize;
    private static int nextRoomId;

    private RoomSpawner() { }

    public static void generateRooms(int normalRoomCount, int floorType) {
        generateRooms(normalRoomCount, floorType, 65, ThreadLocalRandom.current());
    }

    /**
     * A supplied seeded Random makes the layout reproducible. specialChance is
     * applied to each normal room after the first two; two special branches are
     * still guaranteed whenever there are at least two normal rooms.
     */
    public static void generateRooms(int normalRoomCount, int floorType, int specialChance, Random random) {
        if (normalRoomCount < 2) {
            throw new IllegalArgumentException("A dungeon needs at least two normal rooms");
        }
        if (specialChance < 0 || specialChance > 100) {
            throw new IllegalArgumentException("specialChance must be between 0 and 100");
        }
        Objects.requireNonNull(random, "random");

        reset(normalRoomCount, floorType);
        Locpoint startPoint = new Locpoint(layoutSize / 2, layoutSize / 2);
        occupy(startPoint);

        List<Locpoint> mainPath = new ArrayList<>();
        if (!extendMainPath(startPoint, normalRoomCount, mainPath, random)) {
            throw new IllegalStateException("Unable to find a non-overlapping main route");
        }

        Room start = addRoom(Roommanager.getStartRoom(floorType, random), startPoint);
        Room previous = start;
        List<Room> normalRooms = new ArrayList<>();
        for (Locpoint point : mainPath) {
            Room normal = addRoom(Roommanager.getNormalRoom(floorType, random), point);
            normalRooms.add(normal);
            link(previous, normal);
            previous = normal;
        }

        if (floorType == 5) {
            addLeaf(previous, Roommanager.getBossRoom(floorType, random), random, "boss room");
        } else {
            addRandomLeaf(normalRooms, Roommanager.getEndRoom(floorType, random), random, "end room");
        }

        specialroomcount = requestedSpecialCount(normalRoomCount, specialChance, random);
        Set<Room> usedSpecialParents = new HashSet<>();
        for (int index = 0; index < specialroomcount; index++) {
            List<Room> candidates = new ArrayList<>(normalRooms);
            Collections.shuffle(candidates, random);
            candidates.sort(Comparator.comparing(usedSpecialParents::contains));
            Room parent = addRandomLeaf(candidates, Roommanager.getSpecialRoom(floorType, random), random, "special room");
            usedSpecialParents.add(parent);
        }

        // Do not leave a partially built dungeon in the world if planning fails.
        if (Worldmanager.dungeonWorld != null) {
            for (Room room : roomList) {
                Worldmanager.putRoomInWorld(room, roompointMap.get(room));
            }
        }
    }

    private static void reset(int normalRoomCount, int floorType) {
        mapsize = normalRoomCount;
        floortype = floorType;
        specialroomcount = 0;
        nextRoomId = 1;
        layoutSize = Math.max(11, normalRoomCount * 3 + 7);
        roomMap = new int[layoutSize][layoutSize];
        roomList.clear();
        roompointMap.clear();
        connections.clear();
    }

    private static boolean extendMainPath(Locpoint current, int remaining, List<Locpoint> path, Random random) {
        if (remaining == 0) return true;
        for (Locpoint option : freeNeighbours(current, random)) {
            occupy(option);
            path.add(option);
            if (extendMainPath(option, remaining - 1, path, random)) return true;
            path.removeLast();
            roomMap[option.x][option.y] = 0;
        }
        return false;
    }

    private static Room addRandomLeaf(List<Room> parents, Room template, Random random, String description) {
        for (Room parent : parents) {
            Optional<Locpoint> point = firstFreeNeighbour(roompointMap.get(parent), random);
            if (point.isPresent()) return addLeafAt(parent, template, point.get());
        }
        throw new IllegalStateException("Unable to attach " + description + "; layout has no free neighbour");
    }

    private static void addLeaf(Room parent, Room template, Random random, String description) {
        Optional<Locpoint> point = firstFreeNeighbour(roompointMap.get(parent), random);
        if (point.isEmpty()) {
            throw new IllegalStateException("Unable to attach " + description + "; layout has no free neighbour");
        }
        addLeafAt(parent, template, point.get());
    }

    private static Room addLeafAt(Room parent, Room template, Locpoint point) {
        occupy(point);
        Room child = addRoom(template, point);
        link(parent, child);
        return parent;
    }

    private static Room addRoom(Room template, Locpoint point) {
        Room placed = template.createInstance(nextRoomId++);
        roomList.add(placed);
        roompointMap.put(placed, point);
        return placed;
    }

    private static void link(Room from, Room to) {
        connections.add(new Connection(from, to));
    }

    private static int requestedSpecialCount(int normalRoomCount, int specialChance, Random random) {
        int count = Math.min(2, normalRoomCount);
        for (int index = 2; index < normalRoomCount; index++) {
            if (random.nextInt(100) < specialChance) count++;
        }
        return Math.min(count, normalRoomCount);
    }

    private static List<Locpoint> freeNeighbours(Locpoint point, Random random) {
        List<int[]> directions = new ArrayList<>(Arrays.asList(DIRECTIONS));
        Collections.shuffle(directions, random);
        List<Locpoint> result = new ArrayList<>(4);
        for (int[] direction : directions) {
            Locpoint candidate = new Locpoint(point.x + direction[0], point.y + direction[1]);
            if (isRoomMapfree(candidate.x, candidate.y)) result.add(candidate);
        }
        return result;
    }

    private static Optional<Locpoint> firstFreeNeighbour(Locpoint point, Random random) {
        return freeNeighbours(point, random).stream().findFirst();
    }

    private static void occupy(Locpoint point) {
        if (!isRoomMapfree(point.x, point.y)) {
            throw new IllegalStateException("Tried to place two rooms at " + point.x + "," + point.y);
        }
        roomMap[point.x][point.y] = 1;
    }

    public static boolean isRoomMapfree(int x, int y) {
        return x >= 0 && y >= 0 && x < layoutSize && y < layoutSize && roomMap[x][y] == 0;
    }
}
