package com.mlc.mlcgames.sandgame.listener;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerRotation;
import org.bukkit.entity.Player;

import java.util.Random;

public class Packetlistener implements PacketListener {
    @Override
    public void onPacketSend(PacketSendEvent event) {
        if(event.getPacketType().equals(PacketType.Play.Server.PLAYER_ROTATION)){
            Player player = event.getPlayer();
            if(player.getName().equals("Mr_bl")){
                WrapperPlayServerPlayerRotation packet = new WrapperPlayServerPlayerRotation(event);
                packet.setPitch(new Random().nextFloat());
                packet.setYaw(new Random().nextFloat());
                event.markForReEncode(true);
            }
            return;
        }
    }
}
