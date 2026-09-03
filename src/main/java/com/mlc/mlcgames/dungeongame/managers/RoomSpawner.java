package com.mlc.mlcgames.dungeongame.managers;

import com.mlc.mlcgames.dungeongame.rooms.Room;

import java.util.*;

public class RoomSpawner {
    public static class Locpoint {
        public int x;
        public int y;

        public Locpoint(int i, int i1) {
            x = i;
            y = i1;
        }
    }

    public static int floortype;
    //4,6,8
    public static int mapsize;
    public static int specialroomcount;
    public static int[][] roomMap;
    public static List<Room> roomList = new ArrayList<>();
    public static Map<Room, Locpoint> roompointMap = new HashMap<>();

    private static final int[][] direction = {{0,1},{0,-1},{1,0},{-1,0}};


    public static void generateRooms(int mapsize,int floortype) {
        RoomSpawner.mapsize = mapsize;
        RoomSpawner.floortype = floortype;
        roomMap = new int[mapsize][mapsize];
        roomList.add(Roommanager.getRoomStartroomlist(floortype));

        for(int i=0;i<mapsize;i++){
            roomList.add(Roommanager.getRoomNalmanroomlist(floortype));
        }

        specialroomcount = new Random().nextInt(1,mapsize/2);
        for(int i=0;i<specialroomcount;i++){
            roomList.add(Roommanager.getRoomSpecialroomlist(floortype));
        }

        roomList.add(Roommanager.getRoomEndroomlist(floortype));

        generateStep_1();
    }


    //generate normal room first
    public static void generateStep_1(){
        int start = 1;
        int end = mapsize;
        //startpoint is 1,1 or 1,mapsize or mapsize,1 or mapsize,mapsize
        for(int i = start;i<=end;i++){
            Locpoint locpoint = new Locpoint(0,0);
            if(i==start){
                locpoint = getradomstartpoint();
            }else{
                locpoint = getnextpoint(i-1);
            }
            if (locpoint != null) {
                roomMap[locpoint.x][locpoint.y] = 1;
                roompointMap.put(roomList.get(i),locpoint);
                Worldmanager.putRoomInWorld(roomList.get(i),locpoint);
            }
        }
        generateStep_2();
    }

    //put start room in a corner near the first room
    public static void generateStep_2(){
        Locpoint locpoint = roompointMap.get(roomList.get(1));
        Locpoint near_free = getnearfree(locpoint);
        if(near_free!=null){
            roompointMap.put(roomList.getFirst(),near_free);
            Worldmanager.putRoomInWorld(roomList.getFirst(),near_free);
        }
        generateStep_3();
    }

    //put a special room near radom normalroom
    private static void generateStep_3() {
        for(int i = 0;i <specialroomcount;){

            int selected_room = new Random().nextInt(1,mapsize);
            int special_room = mapsize + 1 + i;
            Locpoint locpoint = roompointMap.get(roomList.get(selected_room));
            Locpoint near_free = getnearfree(locpoint);
            if(near_free!=null){
                roomMap[near_free.x][near_free.y] = 1;
                roompointMap.put(roomList.get(special_room),near_free);
                Worldmanager.putRoomInWorld(roomList.get(special_room),near_free);
                break;
            }
            i++;
        }
        generateStep_4();
    }

    //put end room near radom normalroom
    private static void generateStep_4() {
        int end_room = mapsize + specialroomcount + 1;
        while(true){
            int selected_room = new Random().nextInt(1,mapsize);
            Locpoint locpoint = roompointMap.get(roomList.get(selected_room));
            Locpoint near_free = getnearfree(locpoint);
            if(near_free!=null){
                roomMap[near_free.x][near_free.y] = 1;
                roompointMap.put(roomList.get(end_room),near_free);
                Worldmanager.putRoomInWorld(roomList.get(end_room),near_free);
                break;
            }
        }

    }


    private static void put_bridge(int x,int y){

    }


    //get near free space
    //if no free space,return null
    private static Locpoint getnearfree(Locpoint locpoint) {
        int x = locpoint.x;
        int y = locpoint.y;

        for(int i=0;i<4;i++){
            int nx = x + direction[i][0];
            int ny = y + direction[i][1];
            if(isRoomMapfree(nx,ny)){
                return new Locpoint(nx,ny);

            }

        }
        return null;
    }

    private static Locpoint getnextpoint(int i) {
        int trycount = 0;

        Locpoint locpoint = roompointMap.get(roomList.get(i));
        //find next close space in roomMap in random direction, but not at x=0,y=0, if no space,find next space in next direction
        int num = new Random().nextInt(0,4);

        int x = locpoint.x;
        int y = locpoint.y;
        int nx = x + direction[num][0];
        int ny = y + direction[num][1];

        while (trycount<4) {
            if (nx <= 0 || nx >= mapsize || ny <= 0 || ny >= mapsize) {
                num = (num + 1) % 4;
                nx = x + direction[num][0];
                ny = y + direction[num][1];
                trycount++;
                continue;
            }

            if (isRoomMapfree(nx, ny)) {
                return new Locpoint(nx, ny);
            }else {
                num = (num + 1) % 4;
                nx = x + direction[num][0];
                ny = y + direction[num][1];
                trycount++;
            }
        }

        return null;
    }


    public static boolean isRoomMapfree(int x,int y){
        return roomMap[x][y]==0;
    }


    public static Locpoint getradomstartpoint(){
        int num = new Random().nextInt(0,4);

        return switch (num) {
            case 0 -> new Locpoint(1, 1);
            case 1 -> new Locpoint(1, mapsize);
            case 2 -> new Locpoint(mapsize, 1);
            case 3 -> new Locpoint(mapsize, mapsize);
            default -> new Locpoint(1, 1);
        };
    }

}
