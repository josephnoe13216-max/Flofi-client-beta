package com.example.floficlient;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

public abstract class Module {
    protected static final MinecraftClient mc = MinecraftClient.getInstance();
    public final String name;
    public final int key;
    public boolean enabled;
    public KeyBinding bind;

    protected Module(String name, int key) {
        this.name = name;
        this.key = key;
    }

    public void toggle() {
        enabled = !enabled;
        if (enabled) onEnable(); else onDisable();
        if (mc.player != null) mc.player.sendMessage(Text.literal(name + ": " + (enabled ? "ON" : "OFF")), true);
    }

    public void onEnable() {}
    public void onDisable() {}
    public void onTick() {}

    /** Apunta la cámara hacia una entidad. */
    protected static void face(Entity t) {
        var p = mc.player;
        Vec3d d = t.getEyePos().subtract(p.getEyePos());
        double h = Math.sqrt(d.x * d.x + d.z * d.z);
        p.setYaw((float) (Math.toDegrees(Math.atan2(d.z, d.x)) - 90.0));
        p.setPitch((float) -Math.toDegrees(Math.atan2(d.y, h)));
    }
}
