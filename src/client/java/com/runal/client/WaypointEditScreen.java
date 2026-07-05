package com.runal.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
//? if 1.21.4 {
//?} else {
import net.minecraft.client.input.MouseButtonEvent;
//?}
import net.minecraft.network.chat.Component;

import java.awt.Color;
import java.util.Random;

public class WaypointEditScreen extends Screen {
    private final Screen parent;
    private final Waypoint waypoint;

    private EditBox nameBox;
    private EditBox xBox;
    private EditBox zBox;
    private EditBox yBox;
    private EditBox rBox;
    private EditBox gBox;
    private EditBox bBox;

    private int panelX;
    private int panelY;
    private static final int PANEL_WIDTH = 600;
    private static final int WHEEL_RADIUS = 34;
    private static final int WHEEL_CELL = 3;

    private int wheelCenterX;
    private int wheelCenterY;

    private boolean enabled;
    private boolean nether;
    private boolean overworld;
    private boolean end;

    public WaypointEditScreen(Screen parent, Waypoint waypoint) {
        super(Component.literal("Edit Waypoint"));
        this.parent = parent;
        this.waypoint = waypoint;
        this.enabled = waypoint.enabled;
        this.nether = waypoint.nether;
        this.overworld = waypoint.overworld;
        this.end = waypoint.end;
    }

    @Override
    protected void init() {
        panelX = (width - PANEL_WIDTH) / 2;
        panelY = 60;

        nameBox = addEditBox(panelX, panelY + 24, 340, waypoint.name);
        xBox = addEditBox(panelX, panelY + 74, 90, String.valueOf(waypoint.x));
        zBox = addEditBox(panelX + 100, panelY + 74, 90, String.valueOf(waypoint.z));
        yBox = addEditBox(panelX + 200, panelY + 74, 90, String.valueOf(waypoint.y));

        rBox = addEditBox(panelX, panelY + 126, 56, String.valueOf(waypoint.r));
        gBox = addEditBox(panelX + 62, panelY + 126, 56, String.valueOf(waypoint.g));
        bBox = addEditBox(panelX + 124, panelY + 126, 56, String.valueOf(waypoint.b));

        wheelCenterX = panelX + 220 + WHEEL_RADIUS;
        wheelCenterY = panelY + 126 + WHEEL_RADIUS;
    }

    private EditBox addEditBox(int x, int y, int w, String value) {
        EditBox box = new EditBox(font, x, y, w, 20, Component.literal(""));
        box.setValue(value);
        box.setMaxLength(64);
        addRenderableWidget(box);
        return box;
    }

    //? if 1.21.4 || 1.21.11 {
    /*@Override
    public void render(GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks) {
        renderContent(context, mouseX, mouseY, deltaTicks);
        super.render(context, mouseX, mouseY, deltaTicks);
    }
    *///?} else {
    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks) {
        renderContent(context, mouseX, mouseY, deltaTicks);
        super.extractRenderState(context, mouseX, mouseY, deltaTicks);
    }
    //?}

    private void renderContent(GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks) {
        pendingButtons.clear();
        context.fillGradient(0, 0, width, height, 0xBB000000, 0xDD000000);
        context.centeredText(font, "Edit Waypoint", width / 2, 12, 0xFF35D77A);

        context.text(font, "Name", panelX, panelY + 10, 0xFF35D77A, true);

        context.text(font, "Location", panelX, panelY + 50, 0xFF35D77A, true);
        context.text(font, "X", panelX, panelY + 74 - 10, 0xFFA7A8B2, true);
        context.text(font, "Z", panelX + 100, panelY + 74 - 10, 0xFFA7A8B2, true);
        context.text(font, "Y", panelX + 200, panelY + 74 - 10, 0xFFA7A8B2, true);

        context.text(font, "Color", panelX, panelY + 102, 0xFF35D77A, true);
        context.text(font, "R", panelX, panelY + 126 - 10, 0xFFA7A8B2, true);
        context.text(font, "G", panelX + 62, panelY + 126 - 10, 0xFFA7A8B2, true);
        context.text(font, "B", panelX + 124, panelY + 126 - 10, 0xFFA7A8B2, true);

        drawColorWheel(context);
        int previewColor = 0xFF000000 | (currentColorComponent(rBox) << 16) | (currentColorComponent(gBox) << 8) | currentColorComponent(bBox);
        context.fill(panelX + 190, panelY + 126, panelX + 214, panelY + 150, previewColor);
        context.outline(panelX + 190, panelY + 126, 24, 24, 0xFFFFFFFF);

        drawButton(context, "Random Color", panelX, panelY + 156, 190, 20, mouseX, mouseY, this::randomizeColor);
        drawButton(context, "Enabled: " + (enabled ? "On" : "Off"), panelX, panelY + 182, 92, 20, mouseX, mouseY, () -> enabled = !enabled);
        drawButton(context, "Reset", panelX + 98, panelY + 182, 92, 20, mouseX, mouseY, this::resetFields);

        int dimX = panelX + 380;
        context.text(font, "Dimensions", dimX, panelY + 10, 0xFF35D77A, true);
        context.outline(dimX, panelY + 22, 200, 132, 0x33FFFFFF);
        drawButton(context, "the_nether: " + (nether ? "On" : "Off"), dimX + 8, panelY + 30, 184, 20, mouseX, mouseY, () -> nether = !nether);
        drawButton(context, "overworld: " + (overworld ? "On" : "Off"), dimX + 8, panelY + 54, 184, 20, mouseX, mouseY, () -> overworld = !overworld);
        drawButton(context, "the_end: " + (end ? "On" : "Off"), dimX + 8, panelY + 78, 184, 20, mouseX, mouseY, () -> end = !end);

        int bottomY = panelY + 212;
        drawButton(context, "Remove", panelX, bottomY, 190, 22, mouseX, mouseY, this::removeAndClose);
        drawButton(context, "Save", panelX + 200, bottomY, 190, 22, mouseX, mouseY, this::saveAndClose);
        drawButton(context, "Close", panelX + 400, bottomY, 190, 22, mouseX, mouseY, () -> minecraft.setScreen(parent));
    }

    private int currentColorComponent(EditBox box) {
        try {
            return Math.max(0, Math.min(255, Integer.parseInt(box.getValue().trim())));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void drawColorWheel(GuiGraphicsExtractor context) {
        for (int dx = -WHEEL_RADIUS; dx <= WHEEL_RADIUS; dx += WHEEL_CELL) {
            for (int dy = -WHEEL_RADIUS; dy <= WHEEL_RADIUS; dy += WHEEL_CELL) {
                double dist = Math.sqrt(dx * dx + dy * dy) / WHEEL_RADIUS;
                if (dist > 1.0) continue;
                float hue = (float) ((Math.atan2(dy, dx) + Math.PI) / (2 * Math.PI));
                int rgb = Color.HSBtoRGB(hue, (float) dist, 1.0f);
                int color = 0xFF000000 | (rgb & 0xFFFFFF);
                int cx = wheelCenterX + dx;
                int cy = wheelCenterY + dy;
                context.fill(cx, cy, cx + WHEEL_CELL, cy + WHEEL_CELL, color);
            }
        }
    }

    private void drawButton(GuiGraphicsExtractor context, String label, int x, int y, int w, int h, int mouseX, int mouseY, Runnable onClick) {
        boolean hovered = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
        context.fill(x, y, x + w, y + h, hovered ? 0xFF35D77A : 0x66202226);
        context.outline(x, y, w, h, 0x33FFFFFF);
        context.centeredText(font, label, x + w / 2, y + (h - 8) / 2, hovered ? 0xFFFFFFFF : 0xFFDADCE3);
        pendingButtons.add(new PendingButton(x, y, w, h, onClick));
    }

    private record PendingButton(int x, int y, int w, int h, Runnable onClick) {
    }

    private final java.util.List<PendingButton> pendingButtons = new java.util.ArrayList<>();

    private void randomizeColor() {
        Random random = new Random();
        rBox.setValue(String.valueOf(random.nextInt(256)));
        gBox.setValue(String.valueOf(random.nextInt(256)));
        bBox.setValue(String.valueOf(random.nextInt(256)));
    }

    private void resetFields() {
        nameBox.setValue(waypoint.name);
        xBox.setValue(String.valueOf(waypoint.x));
        zBox.setValue(String.valueOf(waypoint.z));
        yBox.setValue(String.valueOf(waypoint.y));
        rBox.setValue(String.valueOf(waypoint.r));
        gBox.setValue(String.valueOf(waypoint.g));
        bBox.setValue(String.valueOf(waypoint.b));
        enabled = waypoint.enabled;
        nether = waypoint.nether;
        overworld = waypoint.overworld;
        end = waypoint.end;
    }

    private void removeAndClose() {
        WaypointManagerState.INSTANCE.remove(waypoint);
        minecraft.setScreen(parent);
    }

    private void saveAndClose() {
        waypoint.name = nameBox.getValue().trim().isEmpty() ? "Waypoint" : nameBox.getValue().trim();
        waypoint.x = parseIntOr(xBox.getValue(), waypoint.x);
        waypoint.y = parseIntOr(yBox.getValue(), waypoint.y);
        waypoint.z = parseIntOr(zBox.getValue(), waypoint.z);
        waypoint.r = currentColorComponent(rBox);
        waypoint.g = currentColorComponent(gBox);
        waypoint.b = currentColorComponent(bBox);
        waypoint.enabled = enabled;
        waypoint.nether = nether;
        waypoint.overworld = overworld;
        waypoint.end = end;
        WaypointManagerState.INSTANCE.save();
        minecraft.setScreen(parent);
    }

    private int parseIntOr(String value, int fallback) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    //? if 1.21.4 {
    /*@Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (handleMouseClicked((int) mouseX, (int) mouseY)) return true;
        return super.mouseClicked(mouseX, mouseY, button);
    }
    *///?} else {
    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
        if (handleMouseClicked((int) event.x(), (int) event.y())) return true;
        return super.mouseClicked(event, doubled);
    }
    //?}

    private boolean handleMouseClicked(int mouseX, int mouseY) {
        double dist = Math.sqrt(Math.pow(mouseX - wheelCenterX, 2) + Math.pow(mouseY - wheelCenterY, 2));
        if (dist <= WHEEL_RADIUS) {
            double dx = mouseX - wheelCenterX;
            double dy = mouseY - wheelCenterY;
            float hue = (float) ((Math.atan2(dy, dx) + Math.PI) / (2 * Math.PI));
            float saturation = (float) Math.min(1.0, dist / WHEEL_RADIUS);
            int rgb = Color.HSBtoRGB(hue, saturation, 1.0f);
            rBox.setValue(String.valueOf((rgb >> 16) & 0xFF));
            gBox.setValue(String.valueOf((rgb >> 8) & 0xFF));
            bBox.setValue(String.valueOf(rgb & 0xFF));
            return true;
        }

        for (PendingButton button : pendingButtons) {
            if (mouseX >= button.x && mouseX <= button.x + button.w && mouseY >= button.y && mouseY <= button.y + button.h) {
                button.onClick.run();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }
}
