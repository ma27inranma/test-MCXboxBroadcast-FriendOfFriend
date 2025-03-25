package com.rtm516.mcxboxbroadcast.core.webrtc.nethernet;

import org.cloudburstmc.protocol.bedrock.BedrockPeer;
import org.cloudburstmc.protocol.bedrock.BedrockServerSession;
import org.cloudburstmc.protocol.bedrock.netty.BedrockPacketWrapper;

public class NethernetBedrockServerSession extends BedrockServerSession {
  public NethernetBedrockServerSession(BedrockPeer peer, int subClientId) {
    super(peer, subClientId);
  }

  public void receiveClientPacket(BedrockPacketWrapper bedrockPacket){
    this.onPacket(bedrockPacket);
  }
}
