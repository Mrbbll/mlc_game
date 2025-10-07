package com.mlc.mlcgames;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import static com.mlc.mlcgames.Mlcgames.*;


public class Invmanger {
    public final Inventory ainv;
    public final Inventory binv;


    public Invmanger(){
        ainv = Bukkit.createInventory(null,6*9,miniMessage.deserialize("<!i><color:#38deff>a</color>"));
        ainv.setItem(2, itemmanger.a_itemStack1);
        ainv.setItem(11, itemmanger.a_itemStack2);
        ainv.setItem(20, itemmanger.a_itemStack3);
        ainv.setItem(29, itemmanger.a_itemStack4);
        ainv.setItem(38, itemmanger.a_itemStack5);


        binv = Bukkit.createInventory(null,6*9,miniMessage.deserialize("<!i><color:#38deff>b</color>"));
        binv.setItem(2, itemmanger.b_itemStack1);
        binv.setItem(11, itemmanger.b_itemStack2);
        binv.setItem(20, itemmanger.b_itemStack3);
        binv.setItem(29, itemmanger.b_itemStack4);
        binv.setItem(38, itemmanger.b_itemStack5);


    }

    public void openinv(Player player, String team){
        Inventory inventory = Bukkit.createInventory(player,9*5);
        switch (team){
            case "a":{
                player.openInventory(ainv);
            }
            case "b":{
                player.openInventory(binv);
            }
        }
    }

}
