package com.example.floficlient.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.LogoDrawer;
import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.gui.DrawContext;

@Mixin(LogoDrawer.class)
public class LogoDrawerMixin {
    @Inject(method = "draw(Lnet/minecraft/client/gui/DrawContext;IFI)V", at = @At("HEAD"), cancellable = true, require = 0)
    private void flofi$hideLogo4(DrawContext ctx, int w, float a, int y, CallbackInfo ci) {
        if (MinecraftClient.getInstance().currentScreen instanceof TitleScreen) ci.cancel();
    }

    @Inject(method = "draw(Lnet/minecraft/client/gui/DrawContext;IF)V", at = @At("HEAD"), cancellable = true, require = 0)
    private void flofi$hideLogo3(DrawContext ctx, int w, float a, CallbackInfo ci) {
        if (MinecraftClient.getInstance().currentScreen instanceof TitleScreen) ci.cancel();
    }
}
