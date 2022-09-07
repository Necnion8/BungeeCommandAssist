package com.gmail.necnionch.myplugin.bungeecommandassist.common.dataio;

import java.io.DataOutputStream;
import java.io.IOException;

public interface Packet {
    void encode(DataOutputStream output) throws IOException;
}
