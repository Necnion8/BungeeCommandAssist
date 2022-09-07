package com.gmail.necnionch.myplugin.bungeecommandassist.bukkit;

import com.gmail.necnionch.myplugin.bungeecommandassist.common.dataio.BukkitPlayerCommandPacket;
import com.gmail.necnionch.myplugin.bungeecommandassist.common.dataio.PluginMessaging;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.DataInputStream;
import java.io.IOException;

public class BACmdBridgePlugin extends JavaPlugin {
    private PluginMessaging.BukkitReceiver messagingReceiver;

    @Override
    public void onEnable() {
        messagingReceiver = new PluginMessaging.BukkitReceiver(this) {
            @Override
            public void onMessageReceived(Player player, String dataName, DataInputStream data) throws IOException {
                if ("playerCommand".equals(dataName)) {
                    BukkitPlayerCommandPacket req = BukkitPlayerCommandPacket.decode(data);
                    Bukkit.getScheduler().runTask(BACmdBridgePlugin.this, () -> {
                        getLogger().info(player.getName() + " issued server command: /" + req.getCommand());
                        Bukkit.dispatchCommand(player, req.getCommand());
                    });
                }
            }
        };
        messagingReceiver.registerChannel();
    }

    @Override
    public void onDisable() {
        if (messagingReceiver != null)
            messagingReceiver.unregisterChannel();
    }

}
