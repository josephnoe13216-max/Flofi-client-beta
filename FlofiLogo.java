package com.example.floficlient;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.Map;

/** Dibuja "FLOFI CLIENT" con letras de bloques tipo piedra, estilo logo de Minecraft. */
public class FlofiLogo {
    private static final Map<Character, String[]> FONT = Map.of(
            'F', new String[]{"#####", "#....", "#....", "####.", "#....", "#....", "#...."},
            'L', new String[]{"#....", "#....", "#....", "#....", "#....", "#....", "#####"},
            'O', new String[]{".###.", "#...#", "#...#", "#...#", "#...#", "#...#", ".###."},
            'I', new String[]{"#####", "..#..", "..#..", "..#..", "..#..", "..#..", "#####"},
            'C', new String[]{".####", "#....", "#....", "#....", "#....", "#....", ".####"},
            'E', new String[]{"#####", "#....", "#....", "####.", "#....", "#....", "#####"},
            'N', new String[]{"#...#", "##..#", "#.#.#", "#..##", "#...#", "#...#", "#...#"},
            'T', new String[]{"#####", "..#..", "..#..", "..#..", "..#..", "..#..", "..#.."});

    private static final int[] STONE = {0xFF8B8B8B, 0xFF7C7C7C, 0xFF989898};
    private static final String TEXT = "FLOFI CLIENT";

    private interface Cell { void run(int px, int py, int seed); }

    private static void forEachCell(int s, int x0, int y0, Cell fn) {
        int cx = 0;
        for (int li = 0; li < TEXT.length(); li++) {
            char ch = TEXT.charAt(li);
            if (ch == ' ') { cx += 3; continue; }
            String[] g = FONT.get(ch);
            for (int row = 0; row < 7; row++)
                for (int col = 0; col < 5; col++)
                    if (g[row].charAt(col) == '#')
                        fn.run(x0 + (cx + col) * s, y0 + row * s, col * 7 + row * 13 + li * 5);
            cx += 6;
        }
    }

    public static void draw(DrawContext ctx, int screenWidth) {
        int cells = 0;
        for (char c : TEXT.toCharArray()) cells += (c == ' ') ? 3 : 6;
        cells -= 1;
        int s = Math.max(2, Math.min(5, (screenWidth - 30) / cells));
        int x0 = (screenWidth - cells * s) / 2;
        int y0 = 30;

        // Pasada 1: profundidad y contorno
        forEachCell(s, x0, y0, (px, py, seed) -> {
            for (int d = 1; d <= 2; d++)
                ctx.fill(px + d, py + d, px + s + d, py + s + d, 0xFF2E2E2E);
            ctx.fill(px - 1, py - 1, px + s + 3, py + s + 3, 0xFF000000);
        });
        // Pasada 2: cara de piedra
        forEachCell(s, x0, y0, (px, py, seed) -> {
            ctx.fill(px, py, px + s, py + s, STONE[seed % 3]);
            ctx.fill(px, py, px + s, py + 1, 0xFFB8B8B8);              // brillo arriba
            ctx.fill(px, py + s - 1, px + s, py + s, 0xFF5A5A5A);      // sombra abajo
        });

        var tr = MinecraftClient.getInstance().textRenderer;
        ctx.drawCenteredTextWithShadow(tr, Text.literal("Client para Minecraft 1.21.4"),
                screenWidth / 2, y0 + 7 * s + 8, 0xFFFFFF55);
    }
}
