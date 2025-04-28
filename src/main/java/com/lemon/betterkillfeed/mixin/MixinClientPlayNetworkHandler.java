package com.lemon.betterkillfeed.mixin;

import com.lemon.betterkillfeed.BetterKillfeedClient;
import com.lemon.betterkillfeed.CombatEvent;
import com.lemon.betterkillfeed.PopEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(ClientPlayNetworkHandler.class)
public class MixinClientPlayNetworkHandler {

    @Inject(method = "onEntityStatus", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/ParticleManager;addEmitter(Lnet/minecraft/entity/Entity;Lnet/minecraft/particle/ParticleEffect;I)V"))
    private void onEntityStatus(EntityStatusS2CPacket packet, CallbackInfo ci) {
        if (packet.getStatus() == 35) { // 35 = Totem Pop
            MinecraftClient client = MinecraftClient.getInstance();
            client.execute(() -> {
                Entity entity = packet.getEntity(client.world);
                if (entity instanceof PlayerEntity player) {
                    CombatEvent event = new PopEvent(player.getUuid(), Util.getMeasuringTimeMs());
                    BetterKillfeedClient.registerEvent(event);
                }
            });
        }
    }
}