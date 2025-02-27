package com.plr.flighthud.mixin;

import com.mojang.authlib.GameProfile;
import com.plr.flighthud.api.IFlies;
import com.plr.flighthud.common.config.SettingsConfig;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.ProfilePublicKey;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class MixinLocalPlayer extends AbstractClientPlayer implements IFlies {
    @Unique
    private long flighthud$lastElytraFlyTime = -1;

    public MixinLocalPlayer(ClientLevel clientLevel, GameProfile gameProfile,@Nullable ProfilePublicKey profilePublicKey) {
        super(clientLevel, gameProfile,profilePublicKey);
    }

    @Override
    public boolean flighthud$isActuallyFlying() {
        return flighthud$lastElytraFlyTime != -1 &&
                System.currentTimeMillis() - flighthud$lastElytraFlyTime > SettingsConfig.hudInitializationDelay.get();
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void inject$tick(CallbackInfo ci) {
        if (isFallFlying()) {
            if (flighthud$lastElytraFlyTime == -1) {
	            flighthud$lastElytraFlyTime = System.currentTimeMillis();
            }
        } else {
	        flighthud$lastElytraFlyTime = -1;
        }
    }
}
