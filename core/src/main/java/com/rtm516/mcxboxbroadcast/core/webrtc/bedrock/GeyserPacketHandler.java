package com.rtm516.mcxboxbroadcast.core.webrtc.bedrock;

import com.rtm516.mcxboxbroadcast.core.Constants;
import com.rtm516.mcxboxbroadcast.core.SessionInfo;
import com.rtm516.mcxboxbroadcast.core.webrtc.MinecraftDataHandler;
import com.rtm516.mcxboxbroadcast.core.webrtc.Utils;
import com.rtm516.mcxboxbroadcast.core.webrtc.nethernet.NethernetBedrockServerSession;
import com.rtm516.mcxboxbroadcast.core.webrtc.nethernet.NethernetBedrockSessionFactory;
import com.rtm516.mcxboxbroadcast.core.webrtc.nethernet.NethernetChannel;

import io.netty.buffer.AbstractByteBufAllocator;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandler;
import io.netty.channel.EventLoop;
import io.netty.channel.nio.NioEventLoopGroup;

import java.util.UUID;
import org.cloudburstmc.math.vector.Vector2f;
import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.math.vector.Vector3i;
import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.protocol.bedrock.BedrockDisconnectReasons;
import org.cloudburstmc.protocol.bedrock.BedrockPeer;
import org.cloudburstmc.protocol.bedrock.BedrockServerSession;
import org.cloudburstmc.protocol.bedrock.codec.BaseBedrockCodecHelper;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodec;
import org.cloudburstmc.protocol.bedrock.codec.BedrockPacketSerializer;
import org.cloudburstmc.protocol.bedrock.codec.v766.BedrockCodecHelper_v766;
import org.cloudburstmc.protocol.bedrock.data.AuthoritativeMovementMode;
import org.cloudburstmc.protocol.bedrock.data.ChatRestrictionLevel;
import org.cloudburstmc.protocol.bedrock.data.EduSharedUriResource;
import org.cloudburstmc.protocol.bedrock.data.GamePublishSetting;
import org.cloudburstmc.protocol.bedrock.data.GameRuleData;
import org.cloudburstmc.protocol.bedrock.data.GameType;
import org.cloudburstmc.protocol.bedrock.data.PacketCompressionAlgorithm;
import org.cloudburstmc.protocol.bedrock.data.PlayerPermission;
import org.cloudburstmc.protocol.bedrock.data.SpawnBiomeType;
import org.cloudburstmc.protocol.bedrock.netty.BedrockPacketWrapper;
import org.cloudburstmc.protocol.bedrock.netty.codec.batch.BedrockBatchDecoder;
import org.cloudburstmc.protocol.bedrock.netty.codec.packet.BedrockPacketCodec;
import org.cloudburstmc.protocol.bedrock.netty.codec.packet.BedrockPacketCodec_v3;
import org.cloudburstmc.protocol.bedrock.packet.*;
import org.cloudburstmc.protocol.common.PacketSignal;
import org.cloudburstmc.protocol.common.util.OptionalBoolean;
import org.geysermc.geyser.GeyserImpl;
import org.geysermc.geyser.network.GeyserBedrockPeer;
import org.geysermc.geyser.network.GeyserServerInitializer;
import org.geysermc.geyser.network.UpstreamPacketHandler;
import org.geysermc.geyser.session.GeyserSession;

public class GeyserPacketHandler implements BedrockPacketHandler {
    private final MinecraftDataHandler dataHandler;
    private final SessionInfo sessionInfo;

    /**
     * In Protocol V554 and above, RequestNetworkSettingsPacket is sent before LoginPacket.
     */
    private boolean networkSettingsRequested = false;

    public GeyserPacketHandler(MinecraftDataHandler dataHandler, SessionInfo sessionInfo) {
        this.dataHandler = dataHandler;
        this.sessionInfo = sessionInfo;
    }

    @Override
    public void onDisconnect(String reason) {
        // TODO
    }

    private void disconnect(String message) {
        DisconnectPacket disconnectPacket = new DisconnectPacket();
        if (message == null) {
            disconnectPacket.setMessageSkipped(true);
            message = BedrockDisconnectReasons.DISCONNECTED;
        }
        disconnectPacket.setKickMessage(message);
        dataHandler.sendPacket(disconnectPacket);
    }

    private void disconnect() {
        disconnect(null);
    }

    @Override
    public PacketSignal handlePacket(BedrockPacket packet) {
        BedrockPacketHandler.super.handlePacket(packet);
        return PacketSignal.HANDLED; // Avoids warning spam about all the packets we ignore and don't handle
    }

    private boolean setCorrectCodec(int protocolVersion) {
        // TODO: Implement this?
//        BedrockCodec packetCodec = BedrockVersionUtils.bedrockCodec(protocolVersion);
//        if (packetCodec == null) {
//            // Protocol version is not supported
//            PlayStatusPacket status = new PlayStatusPacket();
//            if (protocolVersion > BedrockVersionUtils.latestProtocolVersion()) {
//                status.setStatus(PlayStatusPacket.Status.LOGIN_FAILED_SERVER_OLD);
//            } else {
//                status.setStatus(PlayStatusPacket.Status.LOGIN_FAILED_CLIENT_OLD);
//            }
//
//            session.sendPacketImmediately(status);
//            session.disconnect();
//            return false;
//        }
//
//        session.setCodec(packetCodec);
        return true;
    }

    public void sendPacketToGeyser(BedrockPacket packet) {
        System.out.println("GeyserPacketHandler.sendPacketToGeyser " + packet.getClass().getSimpleName());

        for(GeyserSession session : GeyserImpl.getInstance().getSessionManager().getSessions().values()){
            System.out.println("GeyserPacketHandler.sendPacketToGeyser " + session.getUpstream().getAddress());

            int packetId = session.getUpstream().getSession().getCodec().getPacketDefinition(packet.getClass()).getId(); // has serializer?

            ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
            BedrockPacketSerializer serializer = session.getUpstream().getSession().getCodec().getPacketDefinition(packet.getClass()).getSerializer();
            System.out.println(serializer.getClass().getName());

            serializer.serialize(buffer, session.getUpstream().getSession().getCodec().createHelper(), packet);

            BedrockPacketWrapper packetWrapper = BedrockPacketWrapper.create(packetId, 0, 0, packet, buffer);
            ((NethernetBedrockServerSession) session.getUpstream().getSession()).receiveClientPacket(packetWrapper);
        }
    }

    public PacketSignal handle(AdventureSettingsPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(AnimatePacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(AnvilDamagePacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(AvailableEntityIdentifiersPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(BlockEntityDataPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(BlockPickRequestPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(BookEditPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(ClientCacheBlobStatusPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(ClientCacheMissResponsePacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(ClientCacheStatusPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(ClientToServerHandshakePacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(CommandBlockUpdatePacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(CommandRequestPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(CompletedUsingItemPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(ContainerClosePacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(CraftingEventPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(EducationSettingsPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(EmotePacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(EntityEventPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(EntityFallPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(EntityPickRequestPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(EventPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(FilterTextPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(InteractPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(InventoryContentPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(InventorySlotPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(InventoryTransactionPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(ItemFrameDropItemPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(LabTablePacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(LecternUpdatePacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(LevelEventGenericPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(LevelSoundEvent1Packet packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(LevelSoundEventPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(LoginPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(MapInfoRequestPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(MobArmorEquipmentPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(MobEquipmentPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(ModalFormResponsePacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(MoveEntityAbsolutePacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(MovePlayerPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(MultiplayerSettingsPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(NetworkStackLatencyPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(PhotoTransferPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(PlayerActionPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(PlayerAuthInputPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(PlayerHotbarPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(PlayerInputPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(PlayerSkinPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(PurchaseReceiptPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(RequestChunkRadiusPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(ResourcePackChunkRequestPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(ResourcePackClientResponsePacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(RiderJumpPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(ServerSettingsRequestPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(SetDefaultGameTypePacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(SetLocalPlayerAsInitializedPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(SetPlayerGameTypePacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(SubClientLoginPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(AddBehaviorTreePacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(AddEntityPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(AddHangingEntityPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(AddItemEntityPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(AddPaintingPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(AddPlayerPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(AvailableCommandsPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(BlockEventPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(BossEventPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(CameraPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(ChangeDimensionPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(ChunkRadiusUpdatedPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(ClientboundMapItemDataPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(CommandOutputPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(ContainerOpenPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(ContainerSetDataPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(CraftingDataPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(DisconnectPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(ExplodePacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(LevelChunkPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(GameRulesChangedPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(GuiDataPickItemPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(HurtArmorPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(AutomationClientConnectPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(LevelEventPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(MapCreateLockedCopyPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(MobEffectPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(ModalFormRequestPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(MoveEntityDeltaPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(NetworkSettingsPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(NpcRequestPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(OnScreenTextureAnimationPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(PlayerListPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(PlaySoundPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(PlayStatusPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(RemoveEntityPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(RemoveObjectivePacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(ResourcePackChunkDataPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(ResourcePackDataInfoPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(ResourcePacksInfoPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(ResourcePackStackPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(RespawnPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(ScriptCustomEventPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(ServerSettingsResponsePacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(ServerToClientHandshakePacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(SetCommandsEnabledPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(SetDifficultyPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(SetDisplayObjectivePacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(SetEntityDataPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(SetEntityLinkPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(SetEntityMotionPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(SetHealthPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(SetLastHurtByPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(SetScoreboardIdentityPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(SetScorePacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(SetSpawnPositionPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(SetTimePacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(SettingsCommandPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(SetTitlePacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(ShowCreditsPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(ShowProfilePacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(ShowStoreOfferPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(SimpleEventPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(SpawnExperienceOrbPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(SpawnParticleEffectPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(StartGamePacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(StopSoundPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(StructureBlockUpdatePacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(StructureTemplateDataRequestPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(StructureTemplateDataResponsePacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(TakeItemEntityPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(TextPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(TickSyncPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(TransferPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(UpdateAttributesPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(UpdateBlockPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(UpdateBlockPropertiesPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(UpdateBlockSyncedPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(UpdateEquipPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(UpdateSoftEnumPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(UpdateTradePacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(BiomeDefinitionListPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(LevelSoundEvent2Packet packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(NetworkChunkPublisherUpdatePacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(VideoStreamConnectPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(CodeBuilderPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(EmoteListPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(ItemStackRequestPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(ItemStackResponsePacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(PlayerArmorDamagePacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(PlayerEnchantOptionsPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(CreativeContentPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(UpdatePlayerGameTypePacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(PositionTrackingDBServerBroadcastPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(PositionTrackingDBClientRequestPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(PacketViolationWarningPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(DebugInfoPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(MotionPredictionHintsPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(AnimateEntityPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(CameraShakePacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(CorrectPlayerMovePredictionPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(PlayerFogPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(ItemComponentPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(ClientboundDebugRendererPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(SyncEntityPropertyPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(AddVolumeEntityPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(RemoveVolumeEntityPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(NpcDialoguePacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(SimulationTypePacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(EduUriResourcePacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(CreatePhotoPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(UpdateSubChunkBlocksPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(SubChunkPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(SubChunkRequestPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(PhotoInfoRequestPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(PlayerStartItemCooldownPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(ScriptMessagePacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(CodeBuilderSourcePacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(TickingAreasLoadStatusPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(DimensionDataPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(AgentActionEventPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(ChangeMobPropertyPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(LessonProgressPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(RequestAbilityPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(RequestPermissionsPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(ToastRequestPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(UpdateAbilitiesPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(UpdateAdventureSettingsPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(DeathInfoPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(EditorNetworkPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(FeatureRegistryPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(ServerStatsPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(RequestNetworkSettingsPacket packet) {
        /*
         * BedrockServerSession <- BedrockSession#onPacket (protected)
         * need to extend BedrockServerSession to use onPacket
         * done
        */

        EventLoop eventLoop = new NioEventLoopGroup().next();
        NethernetChannel channel = new NethernetChannel(dataHandler.getSctpStream(), eventLoop);
        // channel.pipeline().addLast(BedrockPacketCodec.NAME, dataHandler.getCodec().getClass());

        // channel.pipeline().addLast(new MinecraftDataHandler(dataHandler.getSctpStream(), Constants.BEDROCK_CODEC, sessionInfo, null, "ReliableDataChannel"));
        BedrockPacketCodec_v3 packetCodec = new BedrockPacketCodec_v3();
        packetCodec.setCodec(Constants.BEDROCK_CODEC);

        BedrockBatchDecoder decoder = new BedrockBatchDecoder();

        channel.pipeline().addLast("bedrock-packet-codec", packetCodec);
        channel.pipeline().addLast("bedrock-batch-decoder", decoder);

        GeyserBedrockPeer peer = new GeyserBedrockPeer(channel, NethernetBedrockSessionFactory.Instance);
        // BedrockServerSession session = new BedrockServerSession(peer, 0);
        NethernetBedrockServerSession session = new NethernetBedrockServerSession(peer, 0);

        new GeyserServerInitializer(GeyserImpl.getInstance()).initSession(session);

        GeyserSession geyserSession = new GeyserSession(GeyserImpl.getInstance(), session, eventLoop);
        // geyserSession.connect();

        channel.session = geyserSession; 
        channel.dataHandler = this.dataHandler;

        GeyserImpl.getInstance().getSessionManager().addSession(UUID.randomUUID(), geyserSession);

        System.out.println("created!");

        sendPacketToGeyser(packet);

        PacketCompressionAlgorithm algorithm = PacketCompressionAlgorithm.ZLIB;
        dataHandler.enableCompression(algorithm, 512);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(GameTestRequestPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(GameTestResultsPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(UpdateClientInputLocksPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(ClientCheatAbilityPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(CameraPresetsPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(CameraInstructionPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(UnlockedRecipesPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(CompressedBiomeDefinitionListPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(TrimDataPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(OpenSignPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(AgentAnimationPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(RefreshEntitlementsPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(ToggleCrafterSlotRequestPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(SetPlayerInventoryOptionsPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(SetHudPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(AwardAchievementPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(ClientboundCloseFormPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(ServerboundLoadingScreenPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(JigsawStructureDataPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(CurrentStructureFeaturePacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(ServerboundDiagnosticsPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(CameraAimAssistPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(ContainerRegistryCleanupPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(MovementEffectPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(SetMovementAuthorityPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }

    public PacketSignal handle(CameraAimAssistPresetsPacket packet) {
        sendPacketToGeyser(packet);

        return PacketSignal.HANDLED;
    }
}
