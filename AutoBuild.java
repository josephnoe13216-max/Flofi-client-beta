package com.example.floficlient;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

/**
 * Construye estructuras delante del jugador.
 * - Singleplayer: coloca los bloques directamente (instantáneo).
 * - Servidor: usa /fill, y necesitas OP (nivel 2).
 * Coordenadas locales: r = derecha, u = arriba (0 = bloque del suelo), f = adelante.
 */
public class AutoBuild {
    public record Box(int r1, int u1, int f1, int r2, int u2, int f2, String block) {}
    private record WBox(int x1, int y1, int z1, int x2, int y2, int z2, String block) {}

    private static final ArrayDeque<String> queue = new ArrayDeque<>();
    private static int cooldown;

    private static final MinecraftClient mc = MinecraftClient.getInstance();

    private static void msg(String s) {
        if (mc.player != null) mc.player.sendMessage(Text.literal("[Flofi] " + s), false);
    }

    private static Box b(int r1, int u1, int f1, int r2, int u2, int f2, String id) {
        return new Box(r1, u1, f1, r2, u2, f2, "minecraft:" + id);
    }

    // ---------- Ejecución ----------

    public static void build(String name, List<Box> boxes) {
        var p = mc.player;
        Direction dir = p.getHorizontalFacing();
        int fx = dir.getOffsetX(), fz = dir.getOffsetZ();
        int rx = -fz, rz = fx;
        int ox = p.getBlockX(), oy = p.getBlockY() - 1, oz = p.getBlockZ();

        List<WBox> list = new ArrayList<>();
        for (Box bx : boxes) {
            int xa = ox + bx.r1() * rx + bx.f1() * fx, za = oz + bx.r1() * rz + bx.f1() * fz;
            int xb = ox + bx.r2() * rx + bx.f2() * fx, zb = oz + bx.r2() * rz + bx.f2() * fz;
            list.add(new WBox(Math.min(xa, xb), oy + Math.min(bx.u1(), bx.u2()), Math.min(za, zb),
                    Math.max(xa, xb), oy + Math.max(bx.u1(), bx.u2()), Math.max(za, zb), bx.block()));
        }
        run(name, list);
    }

    private static void run(String name, List<WBox> list) {
        var server = mc.getServer(); // solo existe en singleplayer
        if (server != null) {
            var key = mc.world.getRegistryKey();
            server.execute(() -> {
                ServerWorld w = server.getWorld(key);
                if (w == null) return;
                for (WBox bx : list) {
                    BlockState st = Registries.BLOCK.get(Identifier.of(bx.block())).getDefaultState();
                    for (int x = bx.x1(); x <= bx.x2(); x++)
                        for (int y = bx.y1(); y <= bx.y2(); y++)
                            for (int z = bx.z1(); z <= bx.z2(); z++)
                                w.setBlockState(new BlockPos(x, y, z), st, Block.NOTIFY_ALL);
                }
            });
            msg(name + " construido.");
        } else if (mc.player.hasPermissionLevel(2)) {
            for (WBox bx : list)
                queue.add("fill " + bx.x1() + " " + bx.y1() + " " + bx.z1() + " "
                        + bx.x2() + " " + bx.y2() + " " + bx.z2() + " " + bx.block());
            msg("Construyendo " + name + " (" + list.size() + " comandos, uno cada ~0.7s)...");
        } else {
            msg("En servidores necesitas OP para construir.");
        }
    }

    /** Se llama cada tick: envía los comandos en cola sin que el servidor te expulse por spam. */
    public static void tick() {
        if (mc.player == null) { queue.clear(); return; }
        if (cooldown > 0) { cooldown--; return; }
        String c = queue.poll();
        if (c != null) {
            mc.player.networkHandler.sendChatCommand(c);
            cooldown = 14;
        }
    }

    public static void summonWither() {
        var p = mc.player;
        Direction dir = p.getHorizontalFacing();
        double x = p.getBlockX() + dir.getOffsetX() * 6 + 0.5;
        double y = p.getBlockY();
        double z = p.getBlockZ() + dir.getOffsetZ() * 6 + 0.5;
        String cmd = "summon minecraft:wither " + x + " " + y + " " + z;
        var server = mc.getServer();
        if (server != null) {
            server.execute(() -> server.getCommandManager().executeWithPrefix(server.getCommandSource(), cmd));
            msg("Wither invocado. Cuidado!");
        } else if (p.hasPermissionLevel(2)) {
            p.networkHandler.sendChatCommand(cmd);
            msg("Wither invocado. Cuidado!");
        } else {
            msg("En servidores necesitas OP para invocar.");
        }
    }

    // ---------- Planos ----------

    public static List<Box> hill() {
        List<Box> l = new ArrayList<>();
        for (int y = 0; y <= 6; y++) {
            int w = 7 - y;
            int c = w >= 2 ? Math.max(1, w / 3) : 0;
            l.add(b(-w, y, 9 - (w - c), w, y, 9 + (w - c), "grass_block"));
            if (c > 0) l.add(b(-(w - c), y, 9 - w, w - c, y, 9 + w, "grass_block"));
        }
        return l;
    }

    public static List<Box> bridge() {
        List<Box> l = new ArrayList<>();
        l.add(b(-1, 0, 1, 1, 0, 20, "oak_planks"));
        l.add(b(-1, 1, 1, -1, 1, 20, "oak_fence"));
        l.add(b(1, 1, 1, 1, 1, 20, "oak_fence"));
        for (int f : new int[]{3, 9, 15, 20})
            for (int r : new int[]{-1, 1})
                l.add(b(r, -4, f, r, -1, f, "oak_log"));
        return l;
    }

    public static List<Box> house() {
        List<Box> l = new ArrayList<>();
        l.add(b(-3, 0, 2, 3, 0, 8, "cobblestone"));           // suelo
        l.add(b(-3, 1, 2, 3, 3, 8, "oak_planks"));            // paredes
        l.add(b(-2, 1, 3, 2, 3, 7, "air"));                   // interior
        l.add(b(-4, 4, 1, 4, 4, 9, "spruce_planks"));         // techo
        l.add(b(-3, 5, 2, 3, 5, 8, "spruce_planks"));
        l.add(b(0, 1, 2, 0, 2, 2, "air"));                    // puerta
        l.add(b(-3, 2, 5, -3, 2, 5, "glass"));                // ventanas
        l.add(b(3, 2, 5, 3, 2, 5, "glass"));
        l.add(b(0, 2, 8, 0, 2, 8, "glass"));
        l.add(b(-2, 1, 7, -2, 1, 7, "torch"));                // luz
        l.add(b(2, 1, 7, 2, 1, 7, "torch"));
        return l;
    }

    public static List<Box> tower() {
        List<Box> l = new ArrayList<>();
        l.add(b(-2, 0, 3, 2, 11, 7, "stone_bricks"));
        l.add(b(-1, 1, 4, 1, 10, 6, "air"));
        l.add(b(0, 1, 3, 0, 2, 3, "air"));                    // puerta
        l.add(b(-2, 6, 5, -2, 7, 5, "air"));                  // ventanas
        l.add(b(2, 6, 5, 2, 7, 5, "air"));
        l.add(b(0, 6, 7, 0, 7, 7, "air"));
        l.add(b(-2, 12, 3, 2, 12, 7, "stone_bricks"));        // parapeto
        l.add(b(-1, 12, 4, 1, 12, 6, "air"));
        for (int r : new int[]{-2, 2})
            for (int f : new int[]{3, 7})
                l.add(b(r, 13, f, r, 13, f, "stone_bricks"));
        return l;
    }

    /** Estructura del Wither: la calavera del centro la pones tú para invocarlo. */
    public static List<Box> witherFrame() {
        List<Box> l = new ArrayList<>();
        l.add(b(-2, 1, 2, 2, 5, 4, "air"));
        l.add(b(0, 1, 3, 0, 3, 3, "soul_sand"));
        l.add(b(-1, 3, 3, 1, 3, 3, "soul_sand"));
        l.add(b(-1, 4, 3, -1, 4, 3, "wither_skeleton_skull"));
        l.add(b(1, 4, 3, 1, 4, 3, "wither_skeleton_skull"));
        return l;
    }
}
