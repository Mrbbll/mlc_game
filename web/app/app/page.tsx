'use client';

import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Slider } from '@/components/ui/slider';
import {
  ChevronLeft,
  ChevronRight,
  CircleCheck,
  MapPin,
  Pause,
  Play,
  Route,
  ShieldCheck,
  Shuffle,
  Sparkles,
} from 'lucide-react';
import { useEffect, useMemo, useState } from 'react';

type RoomKind = 'start' | 'normal' | 'special' | 'exit' | 'boss';
type Phase = 'start' | 'main' | 'backtrack' | 'special' | 'validate';
type Direction = 'N' | 'E' | 'S' | 'W';

type Cell = { x: number; z: number };

type Room = {
  id: string;
  kind: RoomKind;
  x: number;
  z: number;
  width: number;
  depth: number;
  label: string;
};

type Link = {
  from: string;
  to: string;
  corridor: Cell[];
};

type Plan = {
  rooms: Room[];
  corridors: Cell[];
  links: Link[];
};

type Snapshot = {
  plan: Plan;
  phase: Phase;
  title: string;
  detail: string;
  highlight: string[];
};

type Candidate = {
  room: Room;
  corridor: Cell[];
};

const DIRECTIONS: Record<Direction, Cell> = {
  N: { x: 0, z: -1 },
  E: { x: 1, z: 0 },
  S: { x: 0, z: 1 },
  W: { x: -1, z: 0 },
};

const NEIGHBORS = Object.values(DIRECTIONS);
const MAP_LIMIT = 44;
const MAX_RECORDED_STEPS = 96;

function key(cell: Cell) {
  return `${cell.x},${cell.z}`;
}

function hashSeed(input: string) {
  let hash = 2166136261;
  for (let index = 0; index < input.length; index += 1) {
    hash ^= input.charCodeAt(index);
    hash = Math.imul(hash, 16777619);
  }
  return hash >>> 0;
}

function makeRandom(seed: number) {
  let value = seed >>> 0;
  return () => {
    value += 0x6d2b79f5;
    let result = value;
    result = Math.imul(result ^ (result >>> 15), result | 1);
    result ^= result + Math.imul(result ^ (result >>> 7), result | 61);
    return ((result ^ (result >>> 14)) >>> 0) / 4294967296;
  };
}

function shuffle<T>(items: T[], random: () => number) {
  const copy = [...items];
  for (let index = copy.length - 1; index > 0; index -= 1) {
    const swapIndex = Math.floor(random() * (index + 1));
    [copy[index], copy[swapIndex]] = [copy[swapIndex], copy[index]];
  }
  return copy;
}

function roomCells(room: Room) {
  const cells: Cell[] = [];
  for (let x = room.x; x < room.x + room.width; x += 1) {
    for (let z = room.z; z < room.z + room.depth; z += 1) {
      cells.push({ x, z });
    }
  }
  return cells;
}

function clonePlan(plan: Plan): Plan {
  return {
    rooms: plan.rooms.map((room) => ({ ...room })),
    corridors: plan.corridors.map((cell) => ({ ...cell })),
    links: plan.links.map((link) => ({
      from: link.from,
      to: link.to,
      corridor: link.corridor.map((cell) => ({ ...cell })),
    })),
  };
}

function sourcePort(room: Room, direction: Direction, offset: number): Cell {
  switch (direction) {
    case 'N':
      return { x: room.x + offset, z: room.z };
    case 'S':
      return { x: room.x + offset, z: room.z + room.depth - 1 };
    case 'E':
      return { x: room.x + room.width - 1, z: room.z + offset };
    case 'W':
      return { x: room.x, z: room.z + offset };
  }
}

function roomFromEndpoint(
  endpoint: Cell,
  direction: Direction,
  targetOffset: number,
  room: Omit<Room, 'x' | 'z'>,
): Room {
  const boundary = {
    x: endpoint.x + DIRECTIONS[direction].x,
    z: endpoint.z + DIRECTIONS[direction].z,
  };

  switch (direction) {
    case 'N':
      return {
        ...room,
        x: boundary.x - targetOffset,
        z: boundary.z - room.depth + 1,
      };
    case 'S':
      return { ...room, x: boundary.x - targetOffset, z: boundary.z };
    case 'E':
      return { ...room, x: boundary.x, z: boundary.z - targetOffset };
    case 'W':
      return {
        ...room,
        x: boundary.x - room.width + 1,
        z: boundary.z - targetOffset,
      };
  }
}

function canPlace(plan: Plan, source: Room, candidate: Candidate) {
  const roomByCell = new Map<string, string>();
  for (const room of plan.rooms) {
    for (const cell of roomCells(room)) roomByCell.set(key(cell), room.id);
  }
  const oldCorridors = new Set(plan.corridors.map(key));
  const newCorridors = new Set<string>();

  for (let index = 0; index < candidate.corridor.length; index += 1) {
    const cell = candidate.corridor[index];
    const cellKey = key(cell);
    if (
      Math.abs(cell.x) > MAP_LIMIT ||
      Math.abs(cell.z) > MAP_LIMIT ||
      roomByCell.has(cellKey) ||
      oldCorridors.has(cellKey) ||
      newCorridors.has(cellKey)
    ) {
      return false;
    }
    newCorridors.add(cellKey);

    for (const delta of NEIGHBORS) {
      const neighborKey = key({ x: cell.x + delta.x, z: cell.z + delta.z });
      const touchingRoom = roomByCell.get(neighborKey);
      if (touchingRoom && !(index === 0 && touchingRoom === source.id)) {
        return false;
      }
      if (oldCorridors.has(neighborKey)) return false;
    }
  }

  const lastCorridor = key(candidate.corridor[candidate.corridor.length - 1]);
  for (const cell of roomCells(candidate.room)) {
    const cellKey = key(cell);
    if (
      Math.abs(cell.x) > MAP_LIMIT ||
      Math.abs(cell.z) > MAP_LIMIT ||
      roomByCell.has(cellKey) ||
      oldCorridors.has(cellKey) ||
      newCorridors.has(cellKey)
    ) {
      return false;
    }

    for (const delta of NEIGHBORS) {
      const neighborKey = key({ x: cell.x + delta.x, z: cell.z + delta.z });
      if (roomByCell.has(neighborKey) || oldCorridors.has(neighborKey)) {
        return false;
      }
      if (newCorridors.has(neighborKey) && neighborKey !== lastCorridor) {
        return false;
      }
    }
  }

  return true;
}

function validatePlan(plan: Plan, requiredSpecials: number) {
  const start = plan.rooms.find((room) => room.kind === 'start');
  const goal = plan.rooms.find(
    (room) => room.kind === 'exit' || room.kind === 'boss',
  );
  if (!start || !goal) return false;

  const graph = new Map<string, string[]>();
  for (const room of plan.rooms) graph.set(room.id, []);
  for (const link of plan.links) {
    graph.get(link.from)?.push(link.to);
    graph.get(link.to)?.push(link.from);
  }

  const queue = [start.id];
  const visited = new Set(queue);
  while (queue.length > 0) {
    const current = queue.shift()!;
    for (const next of graph.get(current) ?? []) {
      if (!visited.has(next)) {
        visited.add(next);
        queue.push(next);
      }
    }
  }

  const specialsAreLeaves = plan.rooms
    .filter((room) => room.kind === 'special')
    .every((room) => (graph.get(room.id)?.length ?? 0) === 1);

  const specialCount = plan.rooms.filter(
    (room) => room.kind === 'special',
  ).length;
  const normalCount = plan.rooms.filter(
    (room) => room.kind === 'normal',
  ).length;
  const allCorridorsAreShort = plan.links.every(
    (link) => link.corridor.length >= 1 && link.corridor.length <= 3,
  );
  const goalLink = plan.links.find((link) => link.to === goal.id);
  const goalSource = goalLink
    ? plan.rooms.find((room) => room.id === goalLink.from)
    : undefined;
  const goalIsLeaf = (graph.get(goal.id)?.length ?? 0) === 1;
  const exitIsBesideNormal = goal.kind !== 'exit' || goalSource?.kind === 'normal';
  const boss = plan.rooms.find((room) => room.kind === 'boss');
  const bossLink = boss ? goalLink : undefined;
  const bossEntranceIsCentered = !boss || Boolean(
    bossLink && (() => {
      const last = bossLink.corridor[bossLink.corridor.length - 1];
      const centerX = boss.x + 1;
      const centerZ = boss.z + 1;
      return (
        (last.x === boss.x - 1 && last.z === centerZ) ||
        (last.x === boss.x + boss.width && last.z === centerZ) ||
        (last.z === boss.z - 1 && last.x === centerX) ||
        (last.z === boss.z + boss.depth && last.x === centerX)
      );
    })(),
  );

  return (
    visited.has(goal.id) &&
    specialsAreLeaves &&
    specialCount >= Math.max(2, requiredSpecials) &&
    specialCount <= normalCount &&
    allCorridorsAreShort &&
    goalIsLeaf &&
    exitIsBesideNormal &&
    bossEntranceIsCentered
  );
}

function generateDungeon(
  seedText: string,
  floor: number,
  normalCount: number,
  specialChance: number,
) {
  const random = makeRandom(
    hashSeed(`${seedText}|${floor}|${normalCount}|${specialChance}`),
  );
  let idCounter = 0;
  const snapshots: Snapshot[] = [];
  const makeId = (kind: RoomKind) => `${kind}-${idCounter++}`;

  const pushSnapshot = (
    plan: Plan,
    phase: Phase,
    title: string,
    detail: string,
    highlight: string[] = [],
    force = false,
  ) => {
    if (snapshots.length < MAX_RECORDED_STEPS || force) {
      snapshots.push({
        plan: clonePlan(plan),
        phase,
        title,
        detail,
        highlight,
      });
    }
  };

  const createOptions = (
    source: Room,
    kind: RoomKind,
    label: string,
    maxLength: number,
  ) => {
    const size = kind === 'boss' ? 3 : 2;
    const candidates: Candidate[] = [];

    for (const direction of shuffle<Direction>(['N', 'E', 'S', 'W'], random)) {
      const sourceSpan = direction === 'N' || direction === 'S'
        ? source.width
        : source.depth;
      const targetSpan = size;

      for (const sourceOffset of shuffle(
        Array.from({ length: sourceSpan }, (_, index) => index),
        random,
      )) {
        for (const length of shuffle(
          Array.from({ length: maxLength }, (_, index) => index + 1),
          random,
        )) {
          const port = sourcePort(source, direction, sourceOffset);
          const corridor = Array.from({ length }, (_, index) => ({
            x: port.x + DIRECTIONS[direction].x * (index + 1),
            z: port.z + DIRECTIONS[direction].z * (index + 1),
          }));
          const endpoint = corridor[corridor.length - 1];

          const targetOffsets = kind === 'boss'
            ? [1]
            : Array.from({ length: targetSpan }, (_, index) => index);

          for (const targetOffset of shuffle(targetOffsets, random)) {
            const room = roomFromEndpoint(endpoint, direction, targetOffset, {
              id: makeId(kind),
              kind,
              width: size,
              depth: size,
              label,
            });
            candidates.push({ room, corridor });
          }
        }
      }
    }

    const preferred = candidates.filter(
      (candidate) => candidate.corridor.length <= 2,
    );
    const fallback = candidates.filter(
      (candidate) => candidate.corridor.length === 3,
    );

    return [...shuffle(preferred, random), ...shuffle(fallback, random)];
  };

  const start: Room = {
    id: makeId('start'),
    kind: 'start',
    x: -1,
    z: -1,
    width: 2,
    depth: 2,
    label: '初始房',
  };
  const initialPlan: Plan = { rooms: [start], corridors: [], links: [] };
  pushSnapshot(
    initialPlan,
    'start',
    '放置初始房',
    '先占用 2×2 个区块，它是这一层所有生成工作的起点。',
    [start.id],
  );

  const targets: Array<{ kind: RoomKind; label: string }> = Array.from(
    { length: normalCount },
    (_, index) => ({ kind: 'normal', label: `普通 ${index + 1}` }),
  );
  if (floor === 5) targets.push({ kind: 'boss', label: 'BOSS' });

  const search = (
    plan: Plan,
    current: Room,
    targetIndex: number,
    allowLengthThree: boolean,
  ): Plan | null => {
    if (targetIndex >= targets.length) return plan;
    const target = targets[targetIndex];
    const options = createOptions(
      current,
      target.kind,
      target.label,
      allowLengthThree ? 3 : 2,
    );

    for (const candidate of options) {
      if (!canPlace(plan, current, candidate)) continue;

      const nextPlan: Plan = {
        rooms: [...plan.rooms, candidate.room],
        corridors: [...plan.corridors, ...candidate.corridor],
        links: [
          ...plan.links,
          {
            from: current.id,
            to: candidate.room.id,
            corridor: candidate.corridor,
          },
        ],
      };

      const isGoal = target.kind === 'exit' || target.kind === 'boss';
      pushSnapshot(
        nextPlan,
        'main',
        isGoal ? `放置${target.label}` : `延长主路线：${target.label}`,
        isGoal
          ? target.kind === 'boss'
            ? 'BOSS 房被固定为主路线终点，走廊只连接 3×3 边缘的正中格。'
            : `${target.label}被固定为主路线的最后一个房间。`
          : `从上一个房间的空闲门口铺设 ${candidate.corridor.length} 个 1×1 走廊区块，再放置一个 2×2 普通房。`,
        [candidate.room.id],
      );

      const solved = search(
        nextPlan,
        candidate.room,
        targetIndex + 1,
        allowLengthThree,
      );
      if (solved) return solved;

      pushSnapshot(
        plan,
        'backtrack',
        '空间冲突，执行回溯',
        `后续房间已经无处可放，因此撤销 ${target.label} 和刚才的走廊，换一个门口重试。`,
        [current.id],
      );
    }
    return null;
  };

  let plan = search(initialPlan, start, 0, false);
  if (!plan) {
    pushSnapshot(
      initialPlan,
      'backtrack',
      '短走廊方案无法完成，开放 3 格走廊',
      '算法已经尝试完所有 1～2 格方案；只有在这种情况下，才允许使用 3 格走廊。',
      [start.id],
    );
    plan = search(initialPlan, start, 0, true);
  }
  if (!plan) {
    throw new Error('当前参数下无法生成地牢，请更换种子。');
  }

  const normalRooms = plan.rooms.filter((room) => room.kind === 'normal');
  if (floor < 5) {
    const exitSourceOrder = shuffle(normalRooms, random);
    const attachExit = (maxCorridorLength: number): Plan | null => {
      for (const normalRoom of exitSourceOrder) {
        const candidates = createOptions(
          normalRoom,
          'exit',
          '下层入口',
          maxCorridorLength,
        );
        for (const candidate of candidates) {
          if (!canPlace(plan!, normalRoom, candidate)) continue;

          const nextPlan: Plan = {
            rooms: [...plan!.rooms, candidate.room],
            corridors: [...plan!.corridors, ...candidate.corridor],
            links: [
              ...plan!.links,
              {
                from: normalRoom.id,
                to: candidate.room.id,
                corridor: candidate.corridor,
              },
            ],
          };
          pushSnapshot(
            nextPlan,
            'main',
            `在${normalRoom.label}旁生成下层入口`,
            `入口不再固定在普通房主干末端，而是随机挂在任意普通房旁；本次通过 ${candidate.corridor.length} 格走廊连接，玩家可以提前找到它进行速通。`,
            [candidate.room.id, normalRoom.id],
          );
          return nextPlan;
        }
      }
      return null;
    };

    let planWithExit = attachExit(2);
    if (!planWithExit) {
      pushSnapshot(
        plan,
        'backtrack',
        '短走廊无法放置下层入口',
        '所有普通房旁的 1～2 格入口方案都发生冲突，因此允许尝试 3 格连接。',
        normalRooms.map((room) => room.id),
      );
      planWithExit = attachExit(3);
    }
    if (!planWithExit) {
      throw new Error('无法在普通房旁放置下一层入口，请更换种子。');
    }
    plan = planWithExit;
  }

  let requestedSpecials = 2;
  for (let index = 2; index < normalCount; index += 1) {
    if (random() * 100 < specialChance) requestedSpecials += 1;
  }
  requestedSpecials = Math.min(normalCount, requestedSpecials);

  const attachSpecialRooms = (
    currentPlan: Plan,
    specialIndex: number,
    targetCount: number,
    maxCorridorLength: number,
  ): Plan | null => {
    if (specialIndex >= targetCount) return currentPlan;

    const specialRoomIds = new Set(
      currentPlan.rooms
        .filter((room) => room.kind === 'special')
        .map((room) => room.id),
    );
    const usedSourceIds = new Set(
      currentPlan.links
        .filter((link) => specialRoomIds.has(link.to))
        .map((link) => link.from),
    );
    const unusedSources = shuffle(
      normalRooms.filter((room) => !usedSourceIds.has(room.id)),
      random,
    );
    const reusedSources = shuffle(
      normalRooms.filter((room) => usedSourceIds.has(room.id)),
      random,
    );
    const specialLabel = `特殊 ${specialIndex + 1}`;

    for (const normalRoom of [...unusedSources, ...reusedSources]) {
      const candidates = createOptions(
        normalRoom,
        'special',
        specialLabel,
        maxCorridorLength,
      );

      for (const candidate of candidates) {
        if (!canPlace(currentPlan, normalRoom, candidate)) continue;

        const nextPlan: Plan = {
          rooms: [...currentPlan.rooms, candidate.room],
          corridors: [...currentPlan.corridors, ...candidate.corridor],
          links: [
            ...currentPlan.links,
            {
              from: normalRoom.id,
              to: candidate.room.id,
              corridor: candidate.corridor,
            },
          ],
        };
        pushSnapshot(
          nextPlan,
          'special',
          `生成支路：${specialLabel}`,
          `它通过 ${candidate.corridor.length} 格走廊连接普通房“${normalRoom.label}”；算法始终先尝试 1～2 格，只有放不下时才使用 3 格。`,
          [candidate.room.id, normalRoom.id],
        );

        const solved = attachSpecialRooms(
          nextPlan,
          specialIndex + 1,
          targetCount,
          maxCorridorLength,
        );
        if (solved) return solved;

        pushSnapshot(
          currentPlan,
          'special',
          '特殊支路冲突，重新选择',
          `撤销 ${specialLabel}，改从其他普通房或其他门口继续尝试。`,
          [normalRoom.id],
        );
      }
    }

    return null;
  };

  let requiredSpecials = requestedSpecials;
  let planWithSpecials: Plan | null = null;
  while (!planWithSpecials && requiredSpecials >= 2) {
    planWithSpecials = attachSpecialRooms(plan, 0, requiredSpecials, 2);
    if (!planWithSpecials) {
      planWithSpecials = attachSpecialRooms(plan, 0, requiredSpecials, 3);
    }
    if (!planWithSpecials) requiredSpecials -= 1;
  }
  if (planWithSpecials) plan = planWithSpecials;

  const valid = validatePlan(plan, requiredSpecials);
  pushSnapshot(
    plan,
    'validate',
    valid ? '连通性验证通过' : '连通性验证失败',
    valid
      ? floor === 5
        ? `从初始房可以到达 BOSS 房，入口位于 3×3 正中；本层生成了 ${requiredSpecials} 个特殊房。`
        : `从初始房可以到达随机挂在普通房旁的下一层入口；本层生成了 ${requiredSpecials} 个特殊房。`
      : '布局不符合规则，需要丢弃并重新生成。',
    plan.rooms.map((room) => room.id),
    true,
  );

  return { snapshots, plan, valid };
}

function DungeonMap({ snapshot }: { snapshot: Snapshot }) {
  const { plan } = snapshot;
  const occupied = [
    ...plan.corridors,
    ...plan.rooms.flatMap((room) => roomCells(room)),
  ];
  const minX = Math.min(...occupied.map((cell) => cell.x)) - 2;
  const maxX = Math.max(...occupied.map((cell) => cell.x)) + 2;
  const minZ = Math.min(...occupied.map((cell) => cell.z)) - 2;
  const maxZ = Math.max(...occupied.map((cell) => cell.z)) + 2;
  const cellSize = 36;
  const columns = maxX - minX + 1;
  const rows = maxZ - minZ + 1;
  const width = columns * cellSize;
  const height = rows * cellSize;
  const highlighted = new Set(snapshot.highlight);

  return (
    <svg
      viewBox={`0 0 ${width} ${height}`}
      className="dungeon-map"
      role="img"
      aria-label={`当前地牢布局：${snapshot.title}`}
    >
      <defs>
        <pattern
          id="chunk-grid"
          width={cellSize}
          height={cellSize}
          patternUnits="userSpaceOnUse"
        >
          <path
            d={`M ${cellSize} 0 L 0 0 0 ${cellSize}`}
            className="grid-line"
            fill="none"
          />
        </pattern>
      </defs>
      <rect width={width} height={height} fill="url(#chunk-grid)" />

      {plan.corridors.map((cell) => (
        <rect
          key={`corridor-${key(cell)}`}
          x={(cell.x - minX) * cellSize + 5}
          y={(cell.z - minZ) * cellSize + 5}
          width={cellSize - 10}
          height={cellSize - 10}
          rx="5"
          className="corridor-cell map-piece-enter"
        >
          <title>{`每个走廊格占用 1×1 区块，坐标 ${cell.x}, ${cell.z}`}</title>
        </rect>
      ))}

      {plan.rooms.map((room) => {
        const x = (room.x - minX) * cellSize;
        const y = (room.z - minZ) * cellSize;
        const roomWidth = room.width * cellSize;
        const roomHeight = room.depth * cellSize;
        const lightLabel = room.kind === 'boss' || room.kind === 'special';

        return (
          <g key={room.id} className="map-piece-enter">
            <rect
              x={x + 2}
              y={y + 2}
              width={roomWidth - 4}
              height={roomHeight - 4}
              rx="8"
              className={`room room-${room.kind} ${highlighted.has(room.id) ? 'is-highlighted' : ''}`}
            >
              <title>{`${room.label}，占用 ${room.width}×${room.depth} 区块，左上角坐标 ${room.x}, ${room.z}`}</title>
            </rect>
            {Array.from({ length: room.width - 1 }, (_, index) => (
              <line
                key={`v-${index}`}
                x1={x + (index + 1) * cellSize}
                x2={x + (index + 1) * cellSize}
                y1={y + 5}
                y2={y + roomHeight - 5}
                className="room-seam"
              />
            ))}
            {Array.from({ length: room.depth - 1 }, (_, index) => (
              <line
                key={`h-${index}`}
                x1={x + 5}
                x2={x + roomWidth - 5}
                y1={y + (index + 1) * cellSize}
                y2={y + (index + 1) * cellSize}
                className="room-seam"
              />
            ))}
            <text
              x={x + roomWidth / 2}
              y={y + roomHeight / 2 - 6}
              className={`room-label ${lightLabel ? 'room-label-light' : ''}`}
            >
              {room.label}
            </text>
            <text
              x={x + roomWidth / 2}
              y={y + roomHeight / 2 + 11}
              className={`room-size-label ${lightLabel ? 'room-label-light' : ''}`}
            >
              {room.width}×{room.depth}
            </text>
          </g>
        );
      })}
    </svg>
  );
}

const stages = [
  { key: 'start', title: '放置初始房', detail: '占用 2×2 区块', icon: MapPin },
  { key: 'main', title: '延长主路线', detail: '优先使用 1～2 格走廊', icon: Route },
  { key: 'special', title: '尝试特殊支路', detail: '失败也不影响主线', icon: Sparkles },
  { key: 'validate', title: '检查连通性', detail: '确认一定可以通关', icon: ShieldCheck },
] as const;

function stageIndex(phase: Phase) {
  if (phase === 'backtrack') return 1;
  return stages.findIndex((stage) => stage.key === phase);
}

export default function Home() {
  const [seed, setSeed] = useState('824731');
  const [floor, setFloor] = useState(1);
  const [normalCount, setNormalCount] = useState(7);
  const [specialChance, setSpecialChance] = useState(65);
  const [step, setStep] = useState(0);
  const [playing, setPlaying] = useState(false);

  const generation = useMemo(
    () => generateDungeon(seed, floor, normalCount, specialChance),
    [seed, floor, normalCount, specialChance],
  );

  useEffect(() => {
    setStep(generation.snapshots.length - 1);
    setPlaying(false);
  }, [generation]);

  useEffect(() => {
    if (!playing) return;
    if (step >= generation.snapshots.length - 1) {
      setPlaying(false);
      return;
    }
    const timer = window.setTimeout(() => {
      setStep((current) => current + 1);
    }, 760);
    return () => window.clearTimeout(timer);
  }, [generation.snapshots.length, playing, step]);

  const safeStep = Math.min(step, generation.snapshots.length - 1);
  const snapshot = generation.snapshots[safeStep];
  const currentStage = stageIndex(snapshot.phase);
  const visibleNormalCount = snapshot.plan.rooms.filter(
    (room) => room.kind === 'normal',
  ).length;
  const visibleSpecialCount = snapshot.plan.rooms.filter(
    (room) => room.kind === 'special',
  ).length;
  const targetLabel = floor === 5
    ? '3×3 BOSS 房（中央入口）'
    : '2×2 下一层入口（随机普通房旁）';

  const changeSeed = () => {
    setSeed(String((hashSeed(`${seed}-${Date.now()}`) + 1) >>> 0));
  };

  const toggleDemo = () => {
    if (playing) {
      setPlaying(false);
      return;
    }
    if (safeStep >= generation.snapshots.length - 1) setStep(0);
    setPlaying(true);
  };

  return (
    <main className="min-h-screen px-4 py-5 sm:px-7 lg:px-9">
      <header className="mx-auto mb-5 flex max-w-[1540px] flex-wrap items-end justify-between gap-4">
        <div>
          <p className="mb-1 text-xs font-semibold tracking-[0.18em] text-primary uppercase">
            Minecraft dungeon lab
          </p>
          <h1 className="text-2xl font-semibold tracking-tight sm:text-3xl">
            地牢生成实验台
          </h1>
          <p className="mt-1 max-w-2xl text-sm text-muted-foreground">
            每个网格都是一个区块。切换楼层并逐步播放，观察主路线和特殊支路怎样被放出来。
          </p>
        </div>
        <Button size="lg" onClick={changeSeed}>
          <Shuffle data-icon="inline-start" />
          换一个种子
        </Button>
      </header>

      <section className="mx-auto grid max-w-[1540px] gap-4 lg:grid-cols-[270px_minmax(0,1fr)] xl:grid-cols-[278px_minmax(0,1fr)_300px]">
        <aside className="control-panel rounded-2xl border p-5">
          <div className="mb-5">
            <p className="eyebrow">生成参数</p>
            <h2 className="mt-1 text-lg font-semibold">选择要观察的规则</h2>
          </div>

          <fieldset>
            <legend className="mb-2 text-sm font-medium">楼层</legend>
            <div className="grid grid-cols-5 gap-1.5">
              {[1, 2, 3, 4, 5].map((value) => (
                <Button
                  key={value}
                  size="sm"
                  variant={floor === value ? 'default' : 'outline'}
                  aria-pressed={floor === value}
                  onClick={() => setFloor(value)}
                >
                  {value}
                </Button>
              ))}
            </div>
            <p className="mt-2 text-xs text-muted-foreground">
              {floor === 5
                ? '第 5 层终点固定为 BOSS 房，走廊对准 3×3 边缘正中格。'
                : `第 ${floor} 层入口会随机挂在任意普通房旁，可能很早被发现。`}
            </p>
          </fieldset>

          <div className="my-5 h-px bg-border" />

          <div className="space-y-5">
            <div>
              <div className="mb-3 flex items-center justify-between gap-3">
                <Label htmlFor="normal-count">普通房数量</Label>
                <strong className="tabular-nums text-sm">{normalCount} 间</strong>
              </div>
              <Slider
                id="normal-count"
                min={4}
                max={10}
                step={1}
                value={[normalCount]}
                onValueChange={(value) =>
                  setNormalCount(Array.isArray(value) ? value[0] : value)
                }
                aria-label="普通房数量"
              />
              <div className="mt-2 flex justify-between text-xs text-muted-foreground">
                <span>4</span><span>10</span>
              </div>
            </div>

            <div>
              <div className="mb-3 flex items-center justify-between gap-3">
                <Label htmlFor="special-chance">特殊房概率</Label>
                <strong className="tabular-nums text-sm">{specialChance}%</strong>
              </div>
              <Slider
                id="special-chance"
                min={0}
                max={100}
                step={5}
                value={[specialChance]}
                onValueChange={(value) =>
                  setSpecialChance(Array.isArray(value) ? value[0] : value)
                }
                aria-label="特殊房生成概率"
              />
              <div className="mt-2 flex justify-between text-xs text-muted-foreground">
                <span>0%</span><span>100%</span>
              </div>
              <p className="mt-2 text-xs text-muted-foreground">
                每层至少生成 2 个，最多生成与普通房相同的数量。
              </p>
            </div>

            <div>
              <Label htmlFor="seed" className="mb-2">地图种子</Label>
              <Input
                id="seed"
                value={seed}
                onChange={(event) => setSeed(event.target.value)}
                spellCheck={false}
                inputMode="numeric"
              />
              <p className="mt-2 text-xs text-muted-foreground">
                相同种子和参数会得到相同布局。
              </p>
            </div>
          </div>

          <div className="target-summary mt-5 rounded-xl border p-3">
            <span className="text-xs text-muted-foreground">
              {floor === 5 ? '本层固定终点' : '本层入口规则'}
            </span>
            <strong className="mt-1 block text-sm">{targetLabel}</strong>
          </div>

          <Button className="mt-4 w-full" variant="secondary" onClick={toggleDemo}>
            {playing ? <Pause data-icon="inline-start" /> : <Play data-icon="inline-start" />}
            {playing ? '暂停演示' : '从头自动演示'}
          </Button>
        </aside>

        <section className="map-panel min-w-0 overflow-hidden rounded-2xl border p-3 sm:p-5">
          <div className="mb-4 flex flex-wrap items-center justify-between gap-3 px-1">
            <div>
              <p className="text-sm font-semibold">第 {floor} 层 · 区块占用图</p>
              <p className="mt-0.5 text-xs text-muted-foreground">
                当前显示 {snapshot.plan.rooms.length} 个房间、{snapshot.plan.corridors.length} 个走廊区块
              </p>
            </div>
            <span className={`step-chip ${generation.valid ? 'is-valid' : ''}`}>
              步骤 {safeStep + 1} / {generation.snapshots.length}
            </span>
          </div>

          <div className="map-viewport">
            <DungeonMap snapshot={snapshot} />
          </div>

          <div className="mt-4 flex flex-wrap items-center justify-between gap-3">
            <div className="legend" aria-label="地图图例">
              <span><i className="legend-start" />初始</span>
              <span><i className="legend-normal" />普通</span>
              <span><i className="legend-special" />特殊</span>
              <span><i className={floor === 5 ? 'legend-boss' : 'legend-exit'} />{floor === 5 ? 'BOSS' : '入口'}</span>
              <span><i className="legend-corridor" />1×1 走廊</span>
            </div>

            <div className="flex items-center gap-2">
              <Button
                size="sm"
                variant="outline"
                disabled={safeStep === 0}
                onClick={() => { setPlaying(false); setStep((value) => Math.max(0, value - 1)); }}
              >
                <ChevronLeft data-icon="inline-start" />上一步
              </Button>
              <Button
                size="sm"
                disabled={safeStep >= generation.snapshots.length - 1}
                onClick={() => { setPlaying(false); setStep((value) => Math.min(generation.snapshots.length - 1, value + 1)); }}
              >
                下一步<ChevronRight data-icon="inline-end" />
              </Button>
            </div>
          </div>
        </section>

        <aside className="algorithm-panel rounded-2xl border p-5 lg:col-span-2 xl:col-span-1">
          <p className="eyebrow">算法正在做什么</p>
          <div className={`current-action mt-3 ${snapshot.phase === 'backtrack' ? 'is-backtrack' : ''}`} aria-live="polite">
            <h2 className="text-base font-semibold">{snapshot.title}</h2>
            <p className="mt-1.5 text-sm leading-6 text-muted-foreground">
              {snapshot.detail}
            </p>
          </div>

          <ol className="mt-5 space-y-2">
            {stages.map((stage, index) => {
              const Icon = stage.icon;
              const state = index < currentStage ? 'complete' : index === currentStage ? 'active' : 'waiting';
              return (
                <li key={stage.key} className="stage-row" data-state={state}>
                  <span className="stage-icon">
                    {state === 'complete' ? <CircleCheck /> : <Icon />}
                  </span>
                  <span>
                    <strong>{stage.title}</strong>
                    <small>{stage.detail}</small>
                  </span>
                </li>
              );
            })}
          </ol>

          <div className="my-5 h-px bg-border" />

          <p className="eyebrow">当前结果</p>
          <dl className="mt-3 grid grid-cols-3 gap-2 xl:grid-cols-1">
            <div className="result-stat"><dt>普通房</dt><dd>{visibleNormalCount}</dd></div>
            <div className="result-stat"><dt>特殊房</dt><dd>{visibleSpecialCount}</dd></div>
            <div className="result-stat"><dt>通关状态</dt><dd className="status-text">{snapshot.phase === 'validate' ? '可到达' : '生成中'}</dd></div>
          </dl>
        </aside>
      </section>

      <section className="mx-auto mt-4 grid max-w-[1540px] gap-3 md:grid-cols-3">
        <article className="rule-card rounded-xl border p-4">
          <span>01</span><h2>普通房先形成主干</h2>
          <p>1～4 层入口随机挂在任意普通房旁，可能形成短通关路线；第 5 层 BOSS 仍在主干末端。</p>
        </article>
        <article className="rule-card rounded-xl border p-4">
          <span>02</span><h2>每次先检查占用</h2>
          <p>优先尝试 1～2 格走廊；只有短走廊放不下时，才使用 3 格。</p>
        </article>
        <article className="rule-card rounded-xl border p-4">
          <span>03</span><h2>特殊房保证数量</h2>
          <p>每层至少 2 个、最多等于普通房数量，并且始终挂在主路线之外。</p>
        </article>
      </section>
    </main>
  );
}
