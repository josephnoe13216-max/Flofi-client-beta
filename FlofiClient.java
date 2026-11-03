package com.example.floficlient;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import org.lwjgl.glfw.GLFW;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.screen.TitleScreen;

import java.util.List;

public class FlofiClient implements ClientModInitializer {
    public static final List<Module> MODULES = List.of(
            new Modules.Fly(), new Modules.Jesus(), new Modules.NoClip(),
            new Modules.FastBreak(), new Modules.KillAura(),
            new Modules.AutoTotem(), new Modules.PvPBot());

    private static KeyBinding menuKey;

    @Override
    public void onInitializeClient() {
        menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.floficlient.menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, "category.floficlient"));
        // Menú principal: logo FLOFI CLIENT en lugar del de Minecraft
        ScreenEvents.AFTER_INIT.register((client, screen, w, h) -> {
            if (screen instanceof TitleScreen)
                ScreenEvents.afterRender(screen).register((s, ctx, mx, my, delta) -> FlofiLogo.draw(ctx, s.width));
        });

        for (Module m : MODULES) {
            m.bind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                    "key.floficlient." + m.name.toLowerCase(),
                    InputUtil.Type.KEYSYM, m.key, "category.floficlient"));
        }

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (mc.player == null || mc.world == null) return;
            while (menuKey.wasPressed()) mc.setScreen(new ModMenuScreen());
            AutoBuild.tick();
            for (Module m : MODULES) {
                while (m.bind.wasPressed()) m.toggle();
                if (m.enabled) m.onTick();
            }
        });

        // /cgive <item> -> te da 64 del item (solo modo creativo)
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(ClientCommandManager.literal("cgive")
                        .then(ClientCommandManager.argument("item", ItemStackArgumentType.itemStack(registryAccess))
                                .executes(ctx -> {
                                    MinecraftClient mc = MinecraftClient.getInstance();
                                    if (!mc.player.isCreative()) {
                                        mc.player.sendMessage(Text.literal("Solo funciona en creativo"), false);
                                        return 0;
                                    }
                                    ItemStack s = ItemStackArgumentType.getItemStackArgument(ctx, "item").createStack(64, false);
                                    mc.interactionManager.clickCreativeStack(s, 36 + mc.player.getInventory().selectedSlot);
                                    return 1;
                                }))));
    }
}
