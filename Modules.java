package com.example.floficlient;

import com.example.floficlient.mixin.ClientPlayerInteractionManagerAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;

public class Modules {

    public static class Fly extends Module {
        public Fly() { super("Fly", GLFW.GLFW_KEY_G); }
        @Override public void onTick() {
            var a = mc.player.getAbilities();
            a.allowFlying = true;
            a.flying = true;
            a.setFlySpeed(0.1f);
        }
        @Override public void onDisable() {
            var p = mc.player;
            if (p != null && !p.isCreative() && !p.isSpectator()) {
                p.getAbilities().allowFlying = false;
                p.getAbilities().flying = false;
            }
        }
    }

    /** Caminar sobre el agua (versión simple: flotas en la superficie). */
    public static class Jesus extends Module {
        public Jesus() { super("Jesus", GLFW.GLFW_KEY_J); }
        @Override public void onTick() {
            var p = mc.player;
            if (p.isTouchingWater() && !p.isSneaking()) {
                var v = p.getVelocity();
                p.setVelocity(v.x, 0.1, v.z);
            }
        }
    }

    public static class NoClip extends Module {
        public NoClip() { super("NoClip", GLFW.GLFW_KEY_N); }
        @Override public void onTick() {
            var p = mc.player;
            p.noClip = true;
            p.setOnGround(false);
            p.getAbilities().allowFlying = true;
            p.getAbilities().flying = true;
        }
        @Override public void onDisable() {
            if (mc.player != null) {
                mc.player.noClip = false;
                if (!mc.player.isCreative() && !mc.player.isSpectator()) {
                    mc.player.getAbilities().allowFlying = false;
                    mc.player.getAbilities().flying = false;
                }
            }
        }
    }

    public static class FastBreak extends Module {
        public FastBreak() { super("FastBreak", GLFW.GLFW_KEY_B); }
        @Override public void onTick() {
            ((ClientPlayerInteractionManagerAccessor) mc.interactionManager).setBlockBreakingCooldown(0);
        }
    }

    public static class KillAura extends Module {
        public static double range = 4.2;
        public KillAura() { super("KillAura", GLFW.GLFW_KEY_R); }
        @Override public void onTick() {
            var p = mc.player;
            if (mc.world == null || mc.interactionManager == null) return;
            if (p.getAttackCooldownProgress(0.5f) < 1.0f) return; // espera al golpe completo
            LivingEntity best = null;
            double bd = range * range;
            for (Entity e : mc.world.getEntities()) {
                if (!(e instanceof LivingEntity le) || e == p || !le.isAlive()) continue;
                if (!(e instanceof PlayerEntity || e instanceof Monster)) continue;
                double d = p.squaredDistanceTo(e);
                if (d < bd) { bd = d; best = le; }
            }
            if (best == null) return;
            face(best);
            mc.interactionManager.attackEntity(p, best);
            p.swingHand(Hand.MAIN_HAND);
        }
    }

    public static class AutoTotem extends Module {
        public AutoTotem() { super("AutoTotem", GLFW.GLFW_KEY_Y); }
        @Override public void onTick() {
            var p = mc.player;
            if (p.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING)) return;
            for (int i = 0; i < 36; i++) {
                if (p.getInventory().getStack(i).isOf(Items.TOTEM_OF_UNDYING)) {
                    int slot = i < 9 ? i + 36 : i;
                    mc.interactionManager.clickSlot(p.playerScreenHandler.syncId, slot, 40, SlotActionType.SWAP, p);
                    return;
                }
            }
        }
    }

    /** Bot PvP básico: persigue al jugador más cercano (combina con KillAura + AutoTotem). */
    public static class PvPBot extends Module {
        public PvPBot() { super("PvPBot", GLFW.GLFW_KEY_O); }
        @Override public void onTick() {
            var p = mc.player;
            PlayerEntity target = null;
            double bd = 32 * 32;
            for (PlayerEntity o : mc.world.getPlayers()) {
                if (o == p || !o.isAlive()) continue;
                double d = p.squaredDistanceTo(o);
                if (d < bd) { bd = d; target = o; }
            }
            if (target == null) { release(); return; }
            face(target);
            mc.options.forwardKey.setPressed(bd > 2.5 * 2.5);
            mc.options.sprintKey.setPressed(true);
            mc.options.jumpKey.setPressed(p.horizontalCollision && p.isOnGround());
        }
        @Override public void onDisable() { release(); }
        private void release() {
            mc.options.forwardKey.setPressed(false);
            mc.options.sprintKey.setPressed(false);
            mc.options.jumpKey.setPressed(false);
        }
    }
}
