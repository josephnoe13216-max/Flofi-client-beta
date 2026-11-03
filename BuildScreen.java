package com.example.floficlient;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class BuildScreen extends Screen {
    public BuildScreen() { super(Text.literal("Auto Build")); }

    private void add(int col, int row, String label, Runnable action) {
        addDrawableChild(ButtonWidget.builder(Text.literal(label), b -> {
            client.setScreen(null);
            action.run();
        }).dimensions(width / 2 - 155 + col * 160, 44 + row * 24, 150, 20).build());
    }

    @Override
    protected void init() {
        add(0, 0, "Colina", () -> AutoBuild.build("Colina", AutoBuild.hill()));
        add(1, 0, "Puente", () -> AutoBuild.build("Puente", AutoBuild.bridge()));
        add(0, 1, "Casa", () -> AutoBuild.build("Casa", AutoBuild.house()));
        add(1, 1, "Torre", () -> AutoBuild.build("Torre", AutoBuild.tower()));
        add(0, 2, "Wither (estructura)", () -> AutoBuild.build("Wither", AutoBuild.witherFrame()));
        add(1, 2, "Invocar Wither", AutoBuild::summonWither);
        addDrawableChild(ButtonWidget.builder(Text.literal("Atrás"),
                b -> client.setScreen(new ModMenuScreen()))
                .dimensions(width / 2 - 155, 44 + 3 * 24 + 8, 310, 20).build());
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        super.render(ctx, mx, my, delta);
        ctx.drawCenteredTextWithShadow(textRenderer, title, width / 2, 20, 0xFFFFFFFF);
        int y = 44 + 3 * 24 + 40;
        ctx.drawCenteredTextWithShadow(textRenderer, Text.literal("Se construye delante de ti, hacia donde miras."), width / 2, y, 0xFFAAAAAA);
        ctx.drawCenteredTextWithShadow(textRenderer, Text.literal("Singleplayer: instantáneo. Servidor: necesitas OP."), width / 2, y + 12, 0xFFAAAAAA);
        ctx.drawCenteredTextWithShadow(textRenderer, Text.literal("Wither: pon tú la calavera del centro para invocarlo."), width / 2, y + 24, 0xFFAAAAAA);
    }
}
