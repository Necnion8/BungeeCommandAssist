package com.gmail.necnionch.myplugin.bungeecommandassist.common.dataio;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class BukkitPlayerCommandPacket implements Packet {

    private final String command;

    public BukkitPlayerCommandPacket(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

    @Override
    public void encode(DataOutputStream output) throws IOException  {
        output.writeUTF(command);
    }

    public static BukkitPlayerCommandPacket decode(DataInputStream input) throws IOException {
        return new BukkitPlayerCommandPacket(input.readUTF());
    }

}
