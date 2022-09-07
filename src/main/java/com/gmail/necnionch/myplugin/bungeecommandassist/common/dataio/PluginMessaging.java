package com.gmail.necnionch.myplugin.bungeecommandassist.common.dataio;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.*;

public class PluginMessaging {
    public static final String CHANNEL_NAME = "bacmd:channel";

    private PluginMessaging() {}


    public static abstract class BukkitReceiver {

        private final JavaPlugin owner;

        public BukkitReceiver(JavaPlugin owner) {
            this.owner = owner;
        }

        public void registerChannel() {
            owner.getServer().getMessenger().registerIncomingPluginChannel(owner, CHANNEL_NAME, this::onPluginMessageReceived);
        }

        public void unregisterChannel() {
            owner.getServer().getMessenger().unregisterOutgoingPluginChannel(owner, CHANNEL_NAME);
        }

        private void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte[] message) {
            if (channel.equals(CHANNEL_NAME))
                try (ByteArrayInputStream is = new ByteArrayInputStream(message);
                     DataInputStream dis = new DataInputStream(is)) {
                    String dataName = dis.readUTF();
                    onMessageReceived(player, dataName, dis);

                } catch (Throwable e) {
                    e.printStackTrace();
                }
        }

        public abstract void onMessageReceived(Player player, String dataName, DataInputStream input) throws IOException;
    }


    public static class BungeeSender {

        private final Plugin owner;

        public BungeeSender(Plugin owner) {
            this.owner = owner;
        }

        public void registerChannel() {
            owner.getProxy().registerChannel(CHANNEL_NAME);
        }

        public void unregisterChannel() {
            owner.getProxy().unregisterChannel(CHANNEL_NAME);
        }

        public void sendData(ProxiedPlayer player, String dataName, Packet packet) {
            try (ByteArrayOutputStream os = new ByteArrayOutputStream();
                 DataOutputStream dos = new DataOutputStream(os)) {
                dos.writeUTF(dataName);
                packet.encode(dos);
                player.getServer().sendData(CHANNEL_NAME, os.toByteArray());

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
