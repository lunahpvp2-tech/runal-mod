package com.runal.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
//? if 1.21.4 {
//?} else {
import net.minecraft.client.input.MouseButtonEvent;
//?}
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TeamTrackerScreen extends Screen {
    private static final int ROW_HEIGHT = 20;
    private static final int LIST_TOP = 40;

    private List<PlayerInfo> players = List.of();
    private double scroll;

    public TeamTrackerScreen() {
        super(Component.literal("Team Tracker"));
    }

    @Override
    protected void init() {
        refreshPlayers();
    }

    private void refreshPlayers() {
        List<PlayerInfo> result = new ArrayList<>();
        if (minecraft != null && minecraft.getConnection() != null) {
            result.addAll(minecraft.getConnection().getListedOnlinePlayers());
            result.removeIf(info -> info.getProfile().name().isBlank());
            if (minecraft.player != null) {
                result.removeIf(info -> info.getProfile().id().equals(minecraft.player.getUUID()));
            }
            result.sort((a, b) -> a.getProfile().name().compareToIgnoreCase(b.getProfile().name()));
        }
        players = result;
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
        context.fillGradient(0, 0, width, height, 0xBB000000, 0xDD000000);
        context.centeredText(font, "Team Tracker", width / 2, 12, 0xFFFFFFFF);
        context.centeredText(font, "Click a player to toggle teammate status. Press Escape to close.", width / 2, 24, 0xFFA7A8B2);

        int listWidth = Math.min(280, width - 40);
        int listX = (width - listWidth) / 2;
        int visibleHeight = height - LIST_TOP - 20;

        context.enableScissor(listX, LIST_TOP, listX + listWidth, LIST_TOP + visibleHeight);

        if (players.isEmpty()) {
            context.centeredText(font, "No other players online", width / 2, LIST_TOP + 10, 0xFF8B8D97);
        }

        for (int i = 0; i < players.size(); i++) {
            int rowY = LIST_TOP + i * ROW_HEIGHT - (int) scroll;
            if (rowY + ROW_HEIGHT < LIST_TOP || rowY > LIST_TOP + visibleHeight) continue;

            PlayerInfo info = players.get(i);
            UUID id = info.getProfile().id();
            boolean teammate = TeamTrackerState.INSTANCE.isTeammate(id);
            boolean hovered = mouseX >= listX && mouseX <= listX + listWidth && mouseY >= rowY && mouseY <= rowY + ROW_HEIGHT - 2;

            int bg = teammate ? 0x5535D77A : (hovered ? 0x55202226 : 0x33101216);
            context.fill(listX, rowY, listX + listWidth, rowY + ROW_HEIGHT - 2, bg);
            context.outline(listX, rowY, listWidth, ROW_HEIGHT - 2, teammate ? 0xFF35D77A : 0x33FFFFFF);
            context.text(font, info.getProfile().name(), listX + 8, rowY + 5, 0xFFFFFFFF, true);
            context.text(font, teammate ? "Teammate" : "", listX + listWidth - 60, rowY + 5, 0xFF35D77A, true);
        }

        context.disableScissor();
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
        int listWidth = Math.min(280, width - 40);
        int listX = (width - listWidth) / 2;
        int visibleHeight = height - LIST_TOP - 20;

        if (mouseX >= listX && mouseX <= listX + listWidth && mouseY >= LIST_TOP && mouseY <= LIST_TOP + visibleHeight) {
            for (int i = 0; i < players.size(); i++) {
                int rowY = LIST_TOP + i * ROW_HEIGHT - (int) scroll;
                if (mouseY >= rowY && mouseY <= rowY + ROW_HEIGHT - 2) {
                    UUID id = players.get(i).getProfile().id();
                    TeamTrackerState.INSTANCE.setTeammate(id, !TeamTrackerState.INSTANCE.isTeammate(id));
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int visibleHeight = height - LIST_TOP - 20;
        int contentHeight = players.size() * ROW_HEIGHT;
        double maxScroll = Math.max(0, contentHeight - visibleHeight);
        scroll = Math.max(0, Math.min(maxScroll, scroll - scrollY * ROW_HEIGHT));
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
