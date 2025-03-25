package com.rtm516.mcxboxbroadcast.core.webrtc.nethernet;

import org.cloudburstmc.protocol.bedrock.BedrockPeer;
import org.cloudburstmc.protocol.bedrock.BedrockServerSession;
import org.cloudburstmc.protocol.bedrock.BedrockSession;
import org.cloudburstmc.protocol.bedrock.BedrockSessionFactory;

public class NethernetBedrockSessionFactory implements BedrockSessionFactory {
  public static NethernetBedrockSessionFactory Instance = new NethernetBedrockSessionFactory();

  @Override
  public BedrockSession createSession(BedrockPeer peer, int subClientId) {
    return new BedrockServerSession(peer, subClientId);
  }
}
