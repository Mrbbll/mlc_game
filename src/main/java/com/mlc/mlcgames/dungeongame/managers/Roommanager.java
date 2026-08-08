package com.mlc.mlcgames.dungeongame.managers;

import com.mlc.mlcgames.dungeongame.rooms.Room;

import java.util.List;
import java.util.Map;
import java.util.Random;


public class Roommanager {
    public static Map<Integer,List<Room>> NormanlroomMap;
    public static Map<Integer,List<Room>> SpecialroomMap;
    public static Map<Integer,List<Room>> EndroomMap;
    public static Map<Integer,List<Room>> StartroomMap;
    public static Map<Integer,List<Room>> BossroomMap;

    public static Room getRoomNalmanroomlist(int floortype){
        return NormanlroomMap.get(floortype).get(new Random().nextInt(NormanlroomMap.get(floortype).size()));
    }

    public static Room getRoomSpecialroomlist(int floortype){
        return SpecialroomMap.get(floortype).get(new Random().nextInt(SpecialroomMap.get(floortype).size()));
    }

    public static Room getRoomEndroomlist(int floortype){
        return EndroomMap.get(floortype).get(new Random().nextInt(EndroomMap.get(floortype).size()));
    }

    public static Room getRoomStartroomlist(int floortype){
        return StartroomMap.get(floortype).get(new Random().nextInt(StartroomMap.get(floortype).size()));
    }

    public static Room getRoomBossroomlist(int floortype){
        return BossroomMap.get(floortype).get(new Random().nextInt(BossroomMap.get(floortype).size()));
    }
}
