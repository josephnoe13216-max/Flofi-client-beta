package com.example.floficlient;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Muestra armadura, manos y durabilidad de otros jugadores. */
public class InspectScreen extends Screen {
    private static final EquipmentSlot[] SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS,
            EquipmentSlot.FEET, EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND};
    private static final String[] NAMES = {"Casco", "Peto", "Pantalón", "Botas", "Mano", "Mano izq."};

    private PlayerEntity selected;

    public InspectScreen() { super(Text.literal("Inspector")); }

    @Override public boolean shouldPause() { return false; }

    @Override
    protected void init() {
        var me = client.player;
        List<AbstractClientPlayerEntity> list = new ArrayList<>(client.world.getPlayers());
        list.removeIf(p -> p == me);
        list.sort(Comparator.comparingDouble(p -> p.squaredDistanceTo(me)));
        int y = 30;
        for (var p : list) {
            if (y > height - 56) break;
            addDrawableChild(ButtonWidget.builder(Text.literal(p.getName().getString()),
                    b -> selected = p).dimensions(10, y, 120, 20).build());
            y += 22;
        }
        addDrawableChild(ButtonWidget.builder(Text.literal("Atrás"),
                b -> client.setScreen(new ModMenuScreen())).dimensions(10, height - 28, 120, 20).build());
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        super.render(ctx, mx, my, delta);
        ctx.drawText(textRenderer, "Jugadores cercanos", 10, 14, 0xFFFFFFFF, true);
        if (selected == null) {
            ctx.drawText(textRenderer, "Elige un jugador de la lista", 150, 40, 0xFFAAAAAA, true);
            return;
        }
        if (selected.isRemoved()) {
            ctx.drawText(textRenderer, "El jugador salió de rango", 150, 40, 0xFFFF5555, true);
            return;
        }

        int x = 150, y = 30;
        ctx.drawText(textRenderer, selected.getName().getString() + "   HP " + Math.round(selected.getHealth())
                + "   " + (int) selected.distanceTo(client.player) + "m", x, y, 0xFFFFFF55, true);
        y += 20;

        for (int i = 0; i < SLOTS.length; i++) {
            ItemStack s = selected.getEquippedStack(SLOTS[i]);
            int ry = y + i * 24;
            ctx.drawText(textRenderer, NAMES[i], x, ry + 5, 0xFFAAAAAA, true);
            if (s.isEmpty()) {
                ctx.drawText(textRenderer, "-", x + 94, ry + 5, 0xFF666666, true);
                continue;
            }
            ctx.drawItem(s, x + 70, ry);
            ctx.drawStackOverlay(textRenderer, s, x + 70, ry);
            ctx.drawText(textRenderer, s.getName(), x + 94, ry, 0xFFFFFFFF, true);
            if (s.isDamageable()) {
                int max = s.getMaxDamage();
                int left = max - s.getDamage();
                int pct = left * 100 / max;
                int color = pct > 50 ? 0xFF55FF55 : pct > 20 ? 0xFFFFFF55 : 0xFFFF5555;
                ctx.drawText(textRenderer, "Durabilidad: " + left + "/" + max + " (" + pct + "%)",
                        x + 94, ry + 11, color, true);
            }
        }

        int fy = y + SLOTS.length * 24 + 6;

        // Inventario completo: solo posible en singleplayer (servidor integrado)
        ServerPlayerEntity sp = client.getServer() == null ? null
                : client.getServer().getPlayerManager().getPlayer(selected.getUuid());
        if (sp != null) {
            int gx = Math.min(x + 250, width - 170);
            ctx.drawText(textRenderer, "Inventario", gx, 30, 0xFFFFFFFF, true);
            for (int n = 0; n < 36; n++) {
                int idx = n < 27 ? n + 9 : n - 27; // primero la mochila, luego la hotbar
                ItemStack s = sp.getInventory().getStack(idx);
                int sx = gx + (n % 9) * 18;
                int sy = 46 + (n / 9) * 18 + (n >= 27 ? 4 : 0);
                if (s.isEmpty()) continue;
                ctx.drawItem(s, sx, sy);
                ctx.drawStackOverlay(textRenderer, s, sx, sy);
            }
        } else {
            ctx.drawText(textRenderer, "En servidores solo se ve armadura y manos:", x, fy, 0xFF888888, true);
            ctx.drawText(textRenderer, "el servidor no envia el resto del inventario.", x, fy + 10, 0xFF888888, true);
        }
    }
}
