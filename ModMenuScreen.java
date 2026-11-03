package com.example.floficlient;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ModMenuScreen extends Screen {
    public ModMenuScreen() { super(Text.literal("Flofi Client")); }

    private static Text label(Module m) {
        return Text.literal(m.name + ": " + (m.enabled ? "ON" : "OFF"))
                .formatted(m.enabled ? Formatting.GREEN : Formatting.RED);
    }

    @Override
    protected void init() {
        int cx = width / 2, y = 40;
        for (int i = 0; i < FlofiClient.MODULES.size(); i++) {
            Module m = FlofiClient.MODULES.get(i);
            int x = cx - 155 + (i % 2) * 160;
            int yy = y + (i / 2) * 24;
            addDrawableChild(ButtonWidget.builder(label(m), b -> {
                m.toggle();
                b.setMessage(label(m));
            }).dimensions(x, yy, 150, 20).build());
        }
        int rows = (FlofiClient.MODULES.size() + 1) / 2;
        int by = y + rows * 24 + 8;
        addDrawableChild(ButtonWidget.builder(Text.literal("Auto Build"),
                b -> client.setScreen(new BuildScreen())).dimensions(cx - 155, by, 310, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Inspeccionar jugadores"),
                b -> client.setScreen(new InspectScreen())).dimensions(cx - 155, by + 24, 310, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Cerrar"),
                b -> close()).dimensions(cx - 155, by + 48, 310, 20).build());
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        super.render(ctx, mx, my, delta);
        ctx.drawCenteredTextWithShadow(textRenderer, title, width / 2, 20, 0xFFFFFFFF);
    }
}
