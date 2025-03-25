package com.rtm516.mcxboxbroadcast.core.webrtc.nethernet;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.unix.PreferredDirectByteBufAllocator;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import pe.pi.sctp4j.sctp.SCTPStream;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.cloudburstmc.protocol.bedrock.codec.BedrockPacketDefinition;
import org.cloudburstmc.protocol.bedrock.netty.BedrockPacketWrapper;
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket;
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacketType;
import org.cloudburstmc.protocol.bedrock.packet.ServerToClientHandshakePacket;
import org.geysermc.geyser.network.netty.DefaultChannelPipelinePublic;
import org.geysermc.geyser.session.GeyserSession;

import com.rtm516.mcxboxbroadcast.core.webrtc.MinecraftDataHandler;

public class NethernetChannel implements Channel {
  public NethernetChannel(SCTPStream sctpStream, EventLoop eventLoop){
    this.sctpStream = sctpStream;
    this.eventLoop = eventLoop;

    this.allocator = new PreferredDirectByteBufAllocator();
    this.pipeline = new DefaultChannelPipelinePublic(this);
  }

  public SCTPStream sctpStream;
  public EventLoop eventLoop;

  public GeyserSession session;
  public MinecraftDataHandler dataHandler;

  public ByteBufAllocator allocator;
  public ChannelPipeline pipeline;

  public SCTPStream getSCTPStream(){
    return sctpStream;
  }

  @Override
  public EventLoop eventLoop() {
    return eventLoop;
  }

  @Override
  public Channel parent() {
    return null;
  }

  // --- Implementation of Channel interface methods ---
  @Override
  public boolean isActive() {
    // return sctpStream.InboundIsOpen() && sctpStream.OutboundIsOpen();
    return this.isOpen();
  }

  @Override
  public ChannelFuture closeFuture() {
    return null;
  }

  @Override
  public boolean isWritable() {
    return true; // Assuming it's always writable for now
  }

  @Override
  public ChannelConfig config() {
    return null;
  }

  @Override
  public ChannelMetadata metadata() {
    return null;
  }

  @Override
  public ChannelId id() {
    return null;
  }

  @Override
  public boolean isRegistered() {
    return true;
  }

  @Override
  public boolean isOpen() {
    return sctpStream.InboundIsOpen() && sctpStream.OutboundIsOpen();
  }

  @Override
  public ChannelPipeline pipeline() {
    return this.pipeline;
  }

  @Override
  public ChannelFuture bind(SocketAddress socketAddress) {
    return null;
  }

  @Override
  public ChannelFuture connect(SocketAddress socketAddress) {
    return null;
  }

  @Override
  public ChannelFuture connect(SocketAddress socketAddress, SocketAddress socketAddress1) {
    return null;
  }

  @Override
  public ChannelFuture disconnect() {
    try{
      sctpStream.close();
    }catch(Exception e){
      e.printStackTrace();
    }

    return null;
  }

  @Override
  public ChannelFuture close() {
    return null;
  }

  @Override
  public ChannelFuture deregister() {
    return null;
  }

  @Override
  public ChannelFuture bind(SocketAddress socketAddress, ChannelPromise channelPromise) {
    return null;
  }

  @Override
  public ChannelFuture connect(SocketAddress socketAddress, ChannelPromise channelPromise) {
    return null;
  }

  @Override
  public ChannelFuture connect(SocketAddress socketAddress, SocketAddress socketAddress1, ChannelPromise channelPromise) {
    return null;
  }

  @Override
  public ChannelFuture disconnect(ChannelPromise channelPromise) {
    return null;
  }

  @Override
  public ChannelFuture close(ChannelPromise channelPromise) {
    return null;
  }

  @Override
  public ChannelFuture deregister(ChannelPromise channelPromise) {
    return null;
  }

  @Override
  public ByteBufAllocator alloc() {
    return allocator;
  }

  @Override
  public Channel read() {
    return null;
  }

  @Override
  public ChannelFuture write(Object o) {
    System.out.println("write! " + o.getClass().getSimpleName());

    if(!(o instanceof BedrockPacketWrapper bedrockPacket)){
      System.out.println("not a bedrock packet");
      return null;
    }

    BedrockPacket packet = bedrockPacket.getPacket();

    System.out.println("packet " + packet.toString());

    if(packet instanceof ServerToClientHandshakePacket serverToClientHandshake){
      // this.dataHandler.enableEncryption(serverToClientHandshake.);
    }

    try{
      // BedrockPacketDefinition<BedrockPacket> packetDefinition = (BedrockPacketDefinition<BedrockPacket>) this.session.getUpstream().getSession().getCodec().getPacketDefinition(bedrockPacket.getPacket().getClass());

      // ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();
      // packetDefinition.getSerializer().serialize(buf, this.session.getUpstream().getCodecHelper(), bedrockPacket.getPacket());

      // // sctpStream.send(bedrockPacket.getPacketBuffer().array());
      // byte[] bytes = new byte[buf.readableBytes()];
      // buf.readBytes(bytes);
      // sctpStream.send(bytes);

      this.dataHandler.sendPacket(bedrockPacket.getPacket());
    }catch(Exception e){
      e.printStackTrace();
    }

    return null;
  }

  @Override
  public ChannelFuture write(Object o, ChannelPromise channelPromise) {
    return write(o);
  }

  @Override
  public ChannelFuture writeAndFlush(Object o, ChannelPromise channelPromise) {
    return write(o);
  }

  @Override
  public ChannelFuture writeAndFlush(Object o) {
    return write(o);
  }

  @Override
  public Channel flush() {
    System.out.println("flush!");
    return this;
  }

  @Override
  public long bytesBeforeUnwritable() {
    return 0;
  }

  @Override
  public long bytesBeforeWritable() {
    return 0;
  }

  @Override
  public Unsafe unsafe() {
    return null;
  }

  @Override
  public ChannelPromise voidPromise() {
    return null;
  }

  @Override
  public SocketAddress localAddress() {
    return null;
  }

  @Override
  public SocketAddress remoteAddress() {
    return new InetSocketAddress("127.0.0.1", 1234); // test address
  }

  @Override
  public ChannelPromise newPromise() {
    return null;
  }

  @Override
  public ChannelProgressivePromise newProgressivePromise() {
    return null;
  }

  @Override
  public ChannelFuture newSucceededFuture() {
    return null;
  }

  @Override
  public ChannelFuture newFailedFuture(Throwable throwable) {
    return null;
  }

  @Override
  public <T> Attribute<T> attr(AttributeKey<T> attributeKey) {
    return null;
  }

  @Override
  public <T> boolean hasAttr(AttributeKey<T> attributeKey) {
    return false;
  }

  @Override
  public int compareTo(Channel o) {
    return 0;
  }
}
