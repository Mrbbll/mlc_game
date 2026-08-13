package com.mlc.mlcgames.sandgame.listener;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketListenerCommon;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerRotation;
import org.bukkit.entity.Player;

import java.util.Random;

public class Packetlistener extends PacketListenerCommon implements PacketListener {
    @Override
    public void onPacketSend(PacketSendEvent event) {
        if(event.getPacketType().equals(PacketType.Play.Client.PLAYER_ROTATION)){
            Player player = event.getPlayer();
            if(player.getName().equals("Mr_bl")){
                WrapperPlayServerPlayerRotation packet = new WrapperPlayServerPlayerRotation(event);
                packet.setRelativeYaw(false);
                packet.setRelativePitch(false);
                Random rand = new Random();
                packet.setYaw(rand.nextFloat() * 360f - 180f);
                packet.setPitch(rand.nextFloat() * 180f - 90f);
                event.markForReEncode(true);
            }
            return;
        }
    }

    public void onDisable(){
        PacketEvents.getAPI().getEventManager().unregisterListener(this);
    }
}
