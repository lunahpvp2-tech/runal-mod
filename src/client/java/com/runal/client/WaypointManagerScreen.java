package com.runal.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
//? if 1.21.4 {
//?} else {
import net.minecraft.client.input.MouseButtonEvent;
//?}
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class WaypointManagerScreen extends Screen {
    private static final int ROW_HEIGHT = 22;
    private static final int LIST_TOP = 40;
    private static final String[] DIMENSION_FILTERS = {"All", Waypoint.OVERWORLD, Waypoint.NETHER, Waypoint.END};

    private final List<ActionButton> actionButtons = new ArrayList<>();
    private double scroll;
    private int dimensionFilterIndex = 0;

    public WaypointManagerScreen() {
        super(Component.literal("Manage Waypoints"));
    }

    private record ActionButton(String label, int x, int y, int w, int h, Runnable onClick) {
    }

    private List<Waypoint> visibleWaypoints() {
        List<Waypoint> result = new ArrayList<>(WaypointManagerState.INSTANCE.getWaypoints());
        String filter = DIMENSION_FILTERS[dimensionFilterIndex];
        if (!"All".equals(filter)) {
            result.removeIf(w -> !w.isEnabledInDimension(filter));
        }
        double px = 0, py = 0, pz = 0;
        if (minecraft != null && minecraft.player != null) {
            px = minecraft.player.getX();
            py = minecraft.player.getY();
            pz = minecraft.player.getZ();
        }
        double fpx = px, fpy = py, fpz = pz;
        result.sort(Comparator.comparingDouble(w -> distanceSq(w, fpx, fpy, fpz)));
        return result;
    }

    private double distanceSq(Waypoint w, double px, double py, double pz) {
        double dx = w.x - px;
        double dy = w.y - py;
        double dz = w.z - pz;
        return dx * dx + dy * dy + dz * dz;
    }

    private int listWidth() {
        return Math.min(560, width - 40);
    }

    private int listX() {
        return (width - listWidth()) / 2;
    }

    private int visibleHeight() {
        return height - LIST_TOP - 44;
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
        actionButtons.clear();
        context.fillGradient(0, 0, width, height, 0xBB000000, 0xDD000000);
        context.centeredText(font, "Manage Waypoints", width / 2, 12, 0xFF35D77A);

        int listWidth = listWidth();
        int listX = listX();
        int visibleHeight = visibleHeight();
        List<Waypoint> waypoints = visibleWaypoints();

        context.enableScissor(listX, LIST_TOP, listX + listWidth, LIST_TOP + visibleHeight);

        if (waypoints.isEmpty()) {
            context.centeredText(font, "No waypoints yet - click New below", width / 2, LIST_TOP + 10, 0xFF8B8D97);
        }

        double px = minecraft != null && minecraft.player != null ? minecraft.player.getX() : 0;
        double py = minecraft != null && minecraft.player != null ? minecraft.player.getY() : 0;
        double pz = minecraft != null && minecraft.player != null ? minecraft.player.getZ() : 0;

        for (int i = 0; i < waypoints.size(); i++) {
            int rowY = LIST_TOP + i * ROW_HEIGHT - (int) scroll;
            if (rowY + ROW_HEIGHT < LIST_TOP || rowY > LIST_TOP + visibleHeight) continue;

            Waypoint waypoint = waypoints.get(i);
            int distance = (int) Math.round(Math.sqrt(distanceSq(waypoint, px, py, pz)));
            boolean hovered = mouseX >= listX && mouseX <= listX + listWidth && mouseY >= rowY && mouseY <= rowY + ROW_HEIGHT - 2;

            context.fill(listX, rowY, listX + listWidth, rowY + ROW_HEIGHT - 2, hovered ? 0x552A2D34 : 0x33101216);
            context.outline(listX, rowY, listWidth, ROW_HEIGHT - 2, waypoint.color());
            context.fill(listX + 4, rowY + 6, listX + 12, rowY + 14, waypoint.color());
            context.text(font, waypoint.name + " (" + distance + "m)", listX + 18, rowY + 6, waypoint.enabled ? 0xFFFFFFFF : 0xFF8B8D97, true);

            int bx = listX + listWidth - 6;
            bx = drawAction(context, "Chat", bx, rowY, mouseX, mouseY, () -> openChatWith(waypoint));
            bx = drawAction(context, "Edit", bx, rowY, mouseX, mouseY, () -> minecraft.setScreen(new WaypointEditScreen(this, waypoint)));
            bx = drawAction(context, "Remove", bx, rowY, mouseX, mouseY, () -> WaypointManagerState.INSTANCE.remove(waypoint));
            bx = drawAction(context, waypoint.enabled ? "On" : "Off", bx, rowY, mouseX, mouseY, () -> waypoint.enabled = !waypoint.enabled);
            drawAction(context, "Teleport", bx, rowY, mouseX, mouseY, () -> teleportTo(waypoint));
        }

        context.disableScissor();

        int bottomY = height - 28;
        drawBottomButton(context, "New", listX, bottomY, mouseX, mouseY, () -> {
            String dim = WaypointManagerState.currentDimensionKey(Minecraft.getInstance());
            double x = minecraft.player != null ? minecraft.player.getX() : 0;
            double y = minecraft.player != null ? minecraft.player.getY() : 0;
            double z = minecraft.player != null ? minecraft.player.getZ() : 0;
            Waypoint created = WaypointManagerState.INSTANCE.create("Waypoint", (int) x, (int) y, (int) z, dim);
            minecraft.setScreen(new WaypointEditScreen(this, created));
        });
        drawBottomButton(context, "Dimension: " + DIMENSION_FILTERS[dimensionFilterIndex], listX + 90, bottomY, mouseX, mouseY,
                () -> dimensionFilterIndex = (dimensionFilterIndex + 1) % DIMENSION_FILTERS.length);
        drawBottomButton(context, "Close", listX + listWidth - 80, bottomY, mouseX, mouseY, this::onClose);
    }

    private int drawAction(GuiGraphicsExtractor context, String label, int rightX, int rowY, int mouseX, int mouseY, Runnable onClick) {
        int w = font.width(label) + 12;
        int x = rightX - w;
        int y = rowY + 3;
        int h = ROW_HEIGHT - 8;
        boolean hovered = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
        context.fill(x, y, x + w, y + h, hovered ? 0xFF35D77A : 0x66202226);
        context.text(font, label, x + 6, y + 2, hovered ? 0xFFFFFFFF : 0xFFDADCE3, false);
        actionButtons.add(new ActionButton(label, x, y, w, h, onClick));
        return x - 4;
    }

    private void drawBottomButton(GuiGraphicsExtractor context, String label, int x, int y, int mouseX, int mouseY, Runnable onClick) {
        int w = Math.max(76, font.width(label) + 16);
        int h = 20;
        boolean hovered = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
        context.fill(x, y, x + w, y + h, hovered ? 0xFF35D77A : 0x66202226);
        context.outline(x, y, w, h, 0x33FFFFFF);
        context.centeredText(font, label, x + w / 2, y + 6, hovered ? 0xFFFFFFFF : 0xFFDADCE3);
        actionButtons.add(new ActionButton(label, x, y, w, h, onClick));
    }

    private void teleportTo(Waypoint waypoint) {
        if (minecraft == null || minecraft.getConnection() == null) return;
        minecraft.getConnection().sendCommand("tp " + waypoint.x + " " + waypoint.y + " " + waypoint.z);
    }

    private void openChatWith(Waypoint waypoint) {
        if (minecraft == null) return;
        String text = waypoint.name + " @ " + waypoint.x + ", " + waypoint.y + ", " + waypoint.z;
        //? if 1.21.4 {
        /*minecraft.setScreen(new ChatScreen(text));
        *///?} else {
        minecraft.setScreen(new ChatScreen(text, false));
        //?}
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
        for (ActionButton button : actionButtons) {
            if (mouseX >= button.x && mouseX <= button.x + button.w && mouseY >= button.y && mouseY <= button.y + button.h) {
                button.onClick.run();
                if (WaypointManagerState.INSTANCE.getWaypoints() != null) WaypointManagerState.INSTANCE.save();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int contentHeight = visibleWaypoints().size() * ROW_HEIGHT;
        double maxScroll = Math.max(0, contentHeight - visibleHeight());
        scroll = Math.max(0, Math.min(maxScroll, scroll - scrollY * ROW_HEIGHT));
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
