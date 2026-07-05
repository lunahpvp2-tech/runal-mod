package com.runal.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
//? if 1.21.4 {
//?} else {
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.MouseButtonEvent;
//?}
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
//? if 1.21.4 {
//?} else {
import net.minecraft.network.chat.FontDescription;
//?}
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RunalScreen extends Screen {

    private static final Identifier SCEPTER_FONT =
            Identifier.fromNamespaceAndPath("scepterutils", "scepter_font");

    private static final Map<String, int[]> SAVED_POSITIONS = new HashMap<>();
    private static final Set<Module> EXPANDED = new HashSet<>();
    private static final Set<String> COLLAPSED_CATEGORIES = new HashSet<>();

    private static final Map<Module, Float> TOGGLE_ANIM = new HashMap<>();
    private static final Map<Module, Float> HOVER_ANIM = new HashMap<>();
    private static final Map<Module, Float> EXPAND_ANIM = new HashMap<>();
    private static final Map<String, Float> COLLAPSE_ANIM = new HashMap<>();
    private static final Map<String, Float> SIDEBAR_HOVER_ANIM = new HashMap<>();

    private static final String[] CATEGORY_ORDER = { "Combat", "Visual", "Tracking", "Misc" };

    private static final int COLUMN_WIDTH = 124;
    private static final int COLUMN_GAP = 10;
    private static final int SIDEBAR_X = 10;
    private static final int SIDEBAR_WIDTH = 86;
    private static final int ROW_HEIGHT = 18;
    private static final int SUB_ROW_HEIGHT = 16;
    private static final int HEADER_HEIGHT = 20;
    private static final int PANEL_RADIUS = 8;
    private static final int ROW_RADIUS = 6;
    private static final int PANEL_BOTTOM_MARGIN = 44;
    private static final int SCROLL_SPEED = 12;

    private static final float TEXT_SCALE = 0.78f;
    private static final float SUB_TEXT_SCALE = 0.66f;

    private static final int TEXT_LEFT_PADDING = 14;
    private static final int SUB_TEXT_LEFT_PADDING = 17;

    private static final int COLOR_HEADER_BG = 0xF21A1B20;
    private static final int COLOR_HEADER_BG_HOVER = 0xF224252C;
    private static final int COLOR_PANEL_BG = 0xE70D0E12;
    private static final int COLOR_PANEL_BORDER = 0x703A3C44;
    private static final int COLOR_PANEL_BORDER_HOT = 0xAA35D77A;
    private static final int COLOR_ROW_OFF = 0xEA17181D;
    private static final int COLOR_ROW_HOVER = 0xF022242A;
    private static final int COLOR_SUB_ROW = 0xE8121317;
    private static final int COLOR_SUB_ROW_HOVER = 0xEA1C1E24;
    private static final int COLOR_TEXT = 0xFFEDEDF2;
    private static final int COLOR_DIM_TEXT = 0xFFA7A8B2;
    private static final int COLOR_HEADER_TEXT = 0xFFFFFFFF;
    private static final int COLOR_BACKDROP_TOP = 0xB0000000;
    private static final int COLOR_BACKDROP_BOTTOM = 0xDF000000;
    private static final int COLOR_ACCENT = 0xFF35D77A;
    private static final int COLOR_ACCENT_ROW = 0xF0183224;
    private static final int COLOR_SEARCH_BG = 0xE8111216;

    private static final long OPEN_ANIM_DURATION_MS = 1L;
    private long openTimeMs = 0L;

    private EditBox searchBox;
    private SliderModuleSetting draggingSlider;
    private int draggingSliderPanelX;
    private TextModuleSetting editingText;

    private static class Panel {
        final String category;
        final List<Module> modules;
        int x, y;
        int scroll = 0;
        boolean dragging = false;
        int dragOffsetX, dragOffsetY;

        Panel(String category, List<Module> modules, int x, int y) {
            this.category = category;
            this.modules = modules;
            this.x = x;
            this.y = y;
        }
    }

    private final List<Panel> panels = new ArrayList<>();

    public RunalScreen() {
        super(Component.literal("Runal"));
    }

    @Override
    protected void init() {
        panels.clear();

        Map<String, List<Module>> grouped = new LinkedHashMap<>();
        for (String cat : CATEGORY_ORDER) grouped.put(cat, new ArrayList<>());

        for (Module module : ModuleManager.getModules()) {
            grouped.computeIfAbsent(module.getCategory(), k -> new ArrayList<>()).add(module);
        }

        int startX = SIDEBAR_X + SIDEBAR_WIDTH + 16;
        int startY = 28;
        int columnIndex = 0;

        for (String category : grouped.keySet()) {
            List<Module> modules = grouped.get(category);
            if (modules.isEmpty()) continue;

            int defaultX = startX + columnIndex * (COLUMN_WIDTH + COLUMN_GAP);
            int defaultY = startY;

            int[] saved = SAVED_POSITIONS.get(category);
            int x = saved != null ? saved[0] : defaultX;
            int y = saved != null ? saved[1] : defaultY;

            panels.add(new Panel(category, modules, x, y));
            COLLAPSE_ANIM.putIfAbsent(category, COLLAPSED_CATEGORIES.contains(category) ? 0f : 1f);
            columnIndex++;
        }

        searchBox = new EditBox(
                this.font, (this.width - 220) / 2, this.height - 32, 220, 20,
                Component.literal("Search")
        );
        searchBox.setBordered(false);
        //? if 1.21.4 {
        //?} else {
        searchBox.setCentered(true);
        //?}
        this.addRenderableWidget(searchBox);

        openTimeMs = System.currentTimeMillis();
    }

    private boolean matchesSearch(Module module) {
        String query = searchBox.getValue().trim().toLowerCase();
        return query.isEmpty() || module.getName().toLowerCase().contains(query);
    }

    private List<Module> visibleModules(Panel panel) {
        List<Module> result = new ArrayList<>();
        for (Module module : panel.modules) {
            if (matchesSearch(module)) result.add(module);
        }
        return result;
    }

    private Component styled(String text) {
        //? if 1.21.4 {
        /*return Component.literal(text).withStyle(Style.EMPTY.withFont(SCEPTER_FONT));
        *///?} else {
        return Component.literal(text).withStyle(
                Style.EMPTY.withFont(new FontDescription.Resource(SCEPTER_FONT))
        );
        //?}
    }

    private float easeOutCubic(float t) {
        float f = t - 1f;
        return f * f * f + 1f;
    }

    private float easeOutQuart(float t) {
        float f = 1f - t;
        return 1f - f * f * f * f;
    }

    private float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    private float anim(Map<Module, Float> map, Module module) {
        return map.getOrDefault(module, 0f);
    }

    private float animCategory(Map<String, Float> map, String category) {
        return map.getOrDefault(category, 0f);
    }

    private void animate(Map<Module, Float> map, Module module, float target, float speed) {
        float current = anim(map, module);
        if (Math.abs(current - target) < 0.002f) {
            map.put(module, target);
            return;
        }
        map.put(module, lerp(current, target, speed));
    }

    private void animateCategory(Map<String, Float> map, String category, float target, float speed) {
        float current = animCategory(map, category);
        if (Math.abs(current - target) < 0.002f) {
            map.put(category, target);
            return;
        }
        map.put(category, lerp(current, target, speed));
    }

    private int clamp255(int v) {
        return Math.max(0, Math.min(255, v));
    }

    private int alpha(int color, float alpha) {
        int a = clamp255((int) (((color >>> 24) & 255) * alpha));
        return (color & 0x00FFFFFF) | (a << 24);
    }

    private int accentColor() {
        return RunalSettings.accentColor;
    }

    private int accentRowColor() {
        return mixColor(0xF0183224, accentColor(), 0.18f);
    }

    private int mixColor(int c1, int c2, float t) {
        t = Math.max(0f, Math.min(1f, t));

        int a1 = (c1 >>> 24) & 255;
        int r1 = (c1 >>> 16) & 255;
        int g1 = (c1 >>> 8) & 255;
        int b1 = c1 & 255;
        int a2 = (c2 >>> 24) & 255;
        int r2 = (c2 >>> 16) & 255;
        int g2 = (c2 >>> 8) & 255;
        int b2 = c2 & 255;

        int a = (int) lerp(a1, a2, t);
        int r = (int) lerp(r1, r2, t);
        int g = (int) lerp(g1, g2, t);
        int b = (int) lerp(b1, b2, t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private int brighten(int color, int amount) {
        int a = (color >>> 24) & 255;
        int r = clamp255(((color >>> 16) & 255) + amount);
        int g = clamp255(((color >>> 8) & 255) + amount);
        int b = clamp255((color & 255) + amount);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private void drawRoundedRect(GuiGraphicsExtractor context, int x, int y, int width, int height, int color) {
        drawRoundedRect(context, x, y, width, height, color, PANEL_RADIUS);
    }

    private void drawRoundedRect(GuiGraphicsExtractor context, int x, int y, int width, int height, int color, int radius) {
        if (width <= 0 || height <= 0) return;

        int r = Math.max(0, Math.min(radius, Math.min(width / 2, height / 2)));
        if (r <= 0) {
            context.fill(x, y, x + width, y + height, color);
            return;
        }

        context.fill(x, y + r, x + width, y + height - r, color);
        int rr = r * r;
        for (int py = 0; py < r; py++) {
            double dy = r - py - 0.5;
            int inset = (int) Math.ceil(r - Math.sqrt(Math.max(0, rr - dy * dy)));
            context.fill(x + inset, y + py, x + width - inset, y + py + 1, color);
            context.fill(x + inset, y + height - py - 1, x + width - inset, y + height - py, color);
        }
    }

    private void drawCorner(GuiGraphicsExtractor context, int cx, int cy, int radius, int color, boolean left, boolean top) {
        int samples = 4;

        for (int py = 0; py < radius; py++) {
            for (int px = 0; px < radius; px++) {
                int inside = 0;

                for (int sy = 0; sy < samples; sy++) {
                    for (int sx = 0; sx < samples; sx++) {
                        double sampleX = px + (sx + 0.5) / samples;
                        double sampleY = py + (sy + 0.5) / samples;
                        double dx = radius - sampleX;
                        double dy = radius - sampleY;

                        if (dx * dx + dy * dy <= radius * radius) inside++;
                    }
                }

                if (inside == 0) continue;
                float coverage = inside / (float) (samples * samples);
                int drawX = left ? cx - radius + px : cx + radius - px - 1;
                int drawY = top ? cy - radius + py : cy + radius - py - 1;
                context.fill(drawX, drawY, drawX + 1, drawY + 1, alpha(color, coverage));
            }
        }
    }

    private void drawRoundedOutline(GuiGraphicsExtractor context, int x, int y, int width, int height, int radius, int color) {
        drawRoundedRect(context, x, y, width, height, color, radius);
        drawRoundedRect(context, x + 1, y + 1, width - 2, height - 2, COLOR_PANEL_BG, Math.max(0, radius - 1));
    }

    private void drawPanelShadow(GuiGraphicsExtractor context, int x, int y, int w, int h) {
        drawRoundedRect(context, x + 3, y + 5, w, h, 0x36000000, PANEL_RADIUS + 1);
        drawRoundedRect(context, x + 1, y + 2, w, h, 0x26000000, PANEL_RADIUS);
    }

    private void drawPanelChrome(GuiGraphicsExtractor context, int x, int y, int w, int h) {
        drawPanelShadow(context, x, y, w, h);
        drawRoundedRect(context, x, y, w, h, COLOR_PANEL_BG, PANEL_RADIUS);
        drawRoundedOutline(context, x, y, w, h, PANEL_RADIUS, COLOR_PANEL_BORDER);
        if (!RunalSettings.roundedPanelBottoms) {
            int bottomY = y + h - PANEL_RADIUS - 1;
            context.fill(x, bottomY, x + w, y + h, COLOR_PANEL_BG);
            context.fill(x, y + h - 1, x + w, y + h, COLOR_PANEL_BORDER);
            context.fill(x, bottomY, x + 1, y + h, COLOR_PANEL_BORDER);
            context.fill(x + w - 1, bottomY, x + w, y + h, COLOR_PANEL_BORDER);
        }
    }

    private void drawHeader(GuiGraphicsExtractor context, int x, int y, int w, int h, boolean hovered, boolean collapsed) {
        int color = hovered ? COLOR_HEADER_BG_HOVER : COLOR_HEADER_BG;
        drawRoundedRect(context, x + 4, y + 4, w - 8, h - 7, color, ROW_RADIUS);
        context.fill(x + 12, y + h - 2, x + w - 12, y + h - 1, alpha(accentColor(), collapsed ? 0.28f : 0.60f));
        drawScaledLeftText(context, collapsed ? "+" : "-", x + w - 16, y + 1, h, COLOR_DIM_TEXT, TEXT_SCALE);
    }

    private void drawModuleRow(GuiGraphicsExtractor context, int x, int y, int w, int h, int color, float toggle) {
        drawRoundedRect(context, x + 4, y + 2, w - 8, h - 3, color, ROW_RADIUS);

        if (toggle > 0.02f) {
            int accent = alpha(accentColor(), 0.90f * toggle);
            int soft = alpha(accentColor(), 0.16f * toggle);
            drawRoundedRect(context, x + 3, y + 1, w - 6, h - 1, soft, ROW_RADIUS + 1);
            drawRoundedRect(context, x + 7, y + 5, 3, h - 9, accent, 2);
            context.fill(x + 12, y + h - 4, x + w - 12, y + h - 3, alpha(accentColor(), 0.35f * toggle));
        }
    }

    private void drawSubRow(GuiGraphicsExtractor context, int x, int y, int w, int h, int color, float expand) {
        int inset = (int) lerp(10f, 5f, expand);
        drawRoundedRect(context, x + inset, y + 1, w - inset - 4, h - 2, color, ROW_RADIUS - 1);
    }

    private void drawSlider(GuiGraphicsExtractor context, SliderModuleSetting slider, int x, int y, int h) {
        int trackX = x + 62;
        int trackY = y + h - 6;
        int trackW = COLUMN_WIDTH - 80;
        float value = Math.max(0f, Math.min(1f, slider.getNormalizedValue()));
        int fillW = (int) (trackW * value);
        drawRoundedRect(context, trackX, trackY, trackW, 3, 0xFF2A2D34, 2);
        drawRoundedRect(context, trackX, trackY, fillW, 3, accentColor(), 2);
        drawRoundedRect(context, trackX + fillW - 2, trackY - 2, 5, 7, 0xFFEDEDF2, 3);
    }

    private void drawScaledCenteredText(GuiGraphicsExtractor context, String text, int boxX, int boxY, int boxWidth, int boxHeight, int color, float scale) {
        Component comp = styled(text);
        int rawWidth = font.width(comp);
        float scaledWidth = rawWidth * scale;
        float scaledHeight = font.lineHeight * scale;
        float drawX = boxX + (boxWidth - scaledWidth) / 2f;
        float drawY = boxY + (boxHeight - scaledHeight) / 2f + 0.5f;

        //? if 1.21.4 {
        /*context.pose().pushPose();
        context.pose().translate(drawX, drawY, 0f);
        context.pose().scale(scale, scale, 1f);
        *///?} else {
        context.pose().pushMatrix();
        context.pose().translate(drawX, drawY);
        context.pose().scale(scale, scale);
        //?}
        context.text(font, comp, 0, 0, color, false);
        //? if 1.21.4 {
        /*context.pose().popPose();
        *///?} else {
        context.pose().popMatrix();
        //?}
    }

    private void drawScaledLeftText(GuiGraphicsExtractor context, String text, int x, int boxY, int boxHeight, int color, float scale) {
        Component comp = styled(text);
        float scaledHeight = font.lineHeight * scale;
        float drawY = boxY + (boxHeight - scaledHeight) / 2f + 0.5f;

        //? if 1.21.4 {
        /*context.pose().pushPose();
        context.pose().translate(x, drawY, 0f);
        context.pose().scale(scale, scale, 1f);
        *///?} else {
        context.pose().pushMatrix();
        context.pose().translate(x, drawY);
        context.pose().scale(scale, scale);
        //?}
        context.text(font, comp, 0, 0, color, false);
        //? if 1.21.4 {
        /*context.pose().popPose();
        *///?} else {
        context.pose().popMatrix();
        //?}
    }

    private int sidebarHeight() {
        return 30 + panels.size() * 20 + 8;
    }

    private void drawSidebar(GuiGraphicsExtractor context, int mouseX, int mouseY) {
        int y = 28;
        drawPanelChrome(context, SIDEBAR_X, y, SIDEBAR_WIDTH, sidebarHeight());
        drawScaledCenteredText(context, "Runal", SIDEBAR_X, y + 4, SIDEBAR_WIDTH, HEADER_HEIGHT, COLOR_HEADER_TEXT, TEXT_SCALE);
        context.fill(SIDEBAR_X + 12, y + 25, SIDEBAR_X + SIDEBAR_WIDTH - 12, y + 26, alpha(accentColor(), 0.55f));

        int itemY = y + 31;
        for (Panel panel : panels) {
            boolean hovered = mouseX >= SIDEBAR_X + 5
                    && mouseX <= SIDEBAR_X + SIDEBAR_WIDTH - 5
                    && mouseY >= itemY
                    && mouseY <= itemY + 17;
            animateCategory(SIDEBAR_HOVER_ANIM, panel.category, hovered ? 1f : 0f, 0.24f);

            boolean collapsed = COLLAPSED_CATEGORIES.contains(panel.category);
            float hover = easeOutQuart(animCategory(SIDEBAR_HOVER_ANIM, panel.category));
            int itemColor = mixColor(0x00111116, COLOR_ROW_HOVER, hover);
            if (!collapsed) itemColor = mixColor(itemColor, accentRowColor(), 0.38f);

            drawRoundedRect(context, SIDEBAR_X + 5, itemY, SIDEBAR_WIDTH - 10, 16, itemColor, ROW_RADIUS);
            if (!collapsed) drawRoundedRect(context, SIDEBAR_X + 8, itemY + 4, 2, 8, alpha(accentColor(), 0.92f), 1);
            drawScaledLeftText(context, panel.category, SIDEBAR_X + 14, itemY, 16, collapsed ? COLOR_DIM_TEXT : COLOR_TEXT, SUB_TEXT_SCALE);
            drawScaledLeftText(context, collapsed ? "+" : "-", SIDEBAR_X + SIDEBAR_WIDTH - 15, itemY, 16, COLOR_DIM_TEXT, SUB_TEXT_SCALE);
            itemY += 20;
        }
    }

    private boolean clickSidebar(int mouseX, int mouseY) {
        int itemY = 59;
        for (Panel panel : panels) {
            boolean inItem = mouseX >= SIDEBAR_X + 5
                    && mouseX <= SIDEBAR_X + SIDEBAR_WIDTH - 5
                    && mouseY >= itemY
                    && mouseY <= itemY + 17;
            if (inItem) {
                toggleCategory(panel.category);
                return true;
            }
            itemY += 20;
        }
        return false;
    }

    private void toggleCategory(String category) {
        if (COLLAPSED_CATEGORIES.contains(category)) {
            COLLAPSED_CATEGORIES.remove(category);
        } else {
            COLLAPSED_CATEGORIES.add(category);
        }
    }

    private void drawSearchChrome(GuiGraphicsExtractor context, int mouseX, int mouseY) {
        if (searchBox == null) return;

        int x = (this.width - 220) / 2;
        int y = this.height - 32;
        int w = 220;
        int h = 20;
        boolean hovered = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
        boolean focused = searchBox.isFocused();
        int border = focused ? accentColor() : (hovered ? 0x90686B76 : COLOR_PANEL_BORDER);

        drawRoundedRect(context, x - 2, y - 2, w + 4, h + 4, alpha(0xFF000000, 0.24f), PANEL_RADIUS);
        drawRoundedRect(context, x - 1, y - 1, w + 2, h + 2, border, PANEL_RADIUS);
        drawRoundedRect(context, x, y, w, h, COLOR_SEARCH_BG, PANEL_RADIUS - 1);
        if (focused || hovered) context.fill(x + 12, y + h - 2, x + w - 12, y + h - 1, alpha(accentColor(), focused ? 0.75f : 0.35f));
        if (searchBox.getValue().isEmpty()) {
            drawScaledCenteredText(context, "Search mods...", x, y, w, h, COLOR_DIM_TEXT, SUB_TEXT_SCALE);
        }
    }

    private List<ModuleSetting> visibleSettings(Module module) {
        List<ModuleSetting> result = new ArrayList<>();
        addVisibleSettings(result, module.getSettings());
        return result;
    }

    private void addVisibleSettings(List<ModuleSetting> result, List<ModuleSetting> settings) {
        for (ModuleSetting setting : settings) {
            result.add(setting);
            if (setting instanceof SettingGroup group && group.isExpanded()) {
                addVisibleSettings(result, group.getSettings());
            } else if (setting instanceof ColorModuleSetting color && color.isEditing()) {
                addVisibleSettings(result, color.getChannelSettings());
            }
        }
    }

    private int settingRowHeight(ModuleSetting setting) {
        if (setting instanceof ColorModuleSetting) return SUB_ROW_HEIGHT * 2;
        if (setting instanceof SliderModuleSetting) return SUB_ROW_HEIGHT + 6;
        return SUB_ROW_HEIGHT;
    }

    private int totalSettingsHeight(List<ModuleSetting> settings) {
        int total = 0;
        for (ModuleSetting setting : settings) total += settingRowHeight(setting);
        return total;
    }

    private void drawSettingControl(GuiGraphicsExtractor context, ModuleSetting setting, int x, int y, int h, boolean hovered) {
        int controlX = x + COLUMN_WIDTH - 48;
        int controlY = y + 4;

        if (setting instanceof ToggleModuleSetting toggle) {
            int bg = toggle.getValue() ? alpha(accentColor(), 0.75f) : 0xFF2A2D34;
            drawRoundedRect(context, controlX + 18, controlY, 20, 8, bg, 4);
            drawRoundedRect(context, controlX + (toggle.getValue() ? 30 : 20), controlY + 1, 6, 6, COLOR_TEXT, 3);
        } else if (setting instanceof ColorModuleSetting color) {
            drawRoundedRect(context, x + COLUMN_WIDTH - 25, controlY - 1, 14, 10, COLOR_PANEL_BORDER, 4);
            drawRoundedRect(context, x + COLUMN_WIDTH - 24, controlY, 12, 8, color.getColor(), 3);
        } else if (setting instanceof SettingGroup group) {
            context.fill(x + 12, y + h - 2, x + COLUMN_WIDTH - 12, y + h - 1, alpha(accentColor(), group.isExpanded() ? 0.55f : 0.22f));
        }
    }

    private int panelMaxHeight(Panel panel) {
        return Math.max(HEADER_HEIGHT + ROW_HEIGHT + SUB_ROW_HEIGHT, this.height - panel.y - PANEL_BOTTOM_MARGIN);
    }

    private void drawScrollbar(GuiGraphicsExtractor context, Panel panel, int panelHeight, int fullHeight) {
        int trackTop = panel.y + HEADER_HEIGHT + 2;
        int trackBottom = panel.y + panelHeight - 6;
        int trackHeight = trackBottom - trackTop;
        if (trackHeight <= 4) return;

        int maxScroll = Math.max(1, fullHeight - panelHeight);
        int thumbHeight = Math.min(trackHeight, Math.max(14, (int) (trackHeight * (panelHeight / (float) fullHeight))));
        float progress = maxScroll <= 0 ? 0f : panel.scroll / (float) maxScroll;
        int thumbY = trackTop + (int) ((trackHeight - thumbHeight) * progress);
        int barX = panel.x + COLUMN_WIDTH - 5;

        context.fill(barX, trackTop, barX + 2, trackBottom, alpha(0xFFFFFFFF, 0.08f));
        context.fill(barX, thumbY, barX + 2, thumbY + thumbHeight, alpha(accentColor(), 0.55f));
    }

    private void drawResetButton(GuiGraphicsExtractor context, int mouseX, int mouseY) {
        int[] bounds = resetButtonBounds();
        int x = bounds[0], y = bounds[1], w = bounds[2], h = bounds[3];
        boolean hovered = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;

        drawRoundedRect(context, x - 2, y - 2, w + 4, h + 4, alpha(0xFF000000, 0.24f), PANEL_RADIUS);
        drawRoundedRect(context, x - 1, y - 1, w + 2, h + 2, hovered ? accentColor() : COLOR_PANEL_BORDER, PANEL_RADIUS);
        drawRoundedRect(context, x, y, w, h, hovered ? COLOR_ROW_HOVER : COLOR_SEARCH_BG, PANEL_RADIUS - 1);
        drawScaledCenteredText(context, "Reset All Settings", x, y, w, h, hovered ? COLOR_TEXT : COLOR_DIM_TEXT, SUB_TEXT_SCALE);
    }

    private int[] resetButtonBounds() {
        int w = 118;
        int h = 20;
        int x = (this.width + 220) / 2 + 10;
        int y = this.height - 32;
        return new int[]{x, y, w, h};
    }

    private void resetAllSettings() {
        for (Module module : ModuleManager.getModules()) {
            module.resetSettings();
        }
        ModuleConfig.save();
        Minecraft.getInstance().getSoundManager().play(
                SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F)
        );
    }

    private int animatedPanelHeight(Panel panel, List<Module> visible) {
        int h = HEADER_HEIGHT + 5;
        float categoryOpen = COLLAPSED_CATEGORIES.contains(panel.category) ? 0f : 1f;

        for (Module module : visible) {
            h += (int) (ROW_HEIGHT * categoryOpen);
            float expand = (EXPANDED.contains(module) ? 1f : 0f) * categoryOpen;
            if (!module.getSettings().isEmpty()) h += (int) (totalSettingsHeight(visibleSettings(module)) * expand);
        }

        return h + 4;
    }

    private void setSliderFromMouse(SliderModuleSetting slider, int panelX, int mouseX) {
        int trackX = panelX + 62;
        int trackW = COLUMN_WIDTH - 80;
        slider.setNormalizedValue((mouseX - trackX) / (float) trackW);
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
        context.fillGradient(0, 0, this.width, this.height, COLOR_BACKDROP_TOP, COLOR_BACKDROP_BOTTOM);
        drawSidebar(context, mouseX, mouseY);

        long elapsed = System.currentTimeMillis() - openTimeMs;
        float openProgress = Math.min(1f, elapsed / (float) OPEN_ANIM_DURATION_MS);
        float easedOpen = easeOutCubic(openProgress);
        float scale = 0.88f + 0.12f * easedOpen;

        //? if 1.21.4 {
        /*context.pose().pushPose();
        context.pose().translate(this.width / 2f, this.height / 2f, 0f);
        context.pose().scale(scale, scale, 1f);
        context.pose().translate(-this.width / 2f, -this.height / 2f, 0f);
        *///?} else {
        context.pose().pushMatrix();
        context.pose().translate(this.width / 2f, this.height / 2f);
        context.pose().scale(scale, scale);
        context.pose().translate(-this.width / 2f, -this.height / 2f);
        //?}

        for (Panel panel : panels) {
            List<Module> visible = visibleModules(panel);
            if (visible.isEmpty()) continue;

            COLLAPSE_ANIM.put(panel.category, COLLAPSED_CATEGORIES.contains(panel.category) ? 0f : 1f);
            boolean collapsed = COLLAPSED_CATEGORIES.contains(panel.category);
            float categoryOpen = COLLAPSED_CATEGORIES.contains(panel.category) ? 0f : 1f;
            int fullHeight = animatedPanelHeight(panel, visible);
            int panelHeight = Math.min(fullHeight, panelMaxHeight(panel));
            int maxScroll = Math.max(0, fullHeight - panelHeight);
            panel.scroll = Math.max(0, Math.min(panel.scroll, maxScroll));
            boolean headerHovered = mouseX >= panel.x
                    && mouseX <= panel.x + COLUMN_WIDTH
                    && mouseY >= panel.y
                    && mouseY <= panel.y + HEADER_HEIGHT;

            drawPanelChrome(context, panel.x, panel.y, COLUMN_WIDTH, panelHeight);
            drawHeader(context, panel.x, panel.y, COLUMN_WIDTH, HEADER_HEIGHT, headerHovered, collapsed);
            drawScaledCenteredText(context, panel.category, panel.x, panel.y + 1, COLUMN_WIDTH, HEADER_HEIGHT, COLOR_HEADER_TEXT, TEXT_SCALE);

            if (categoryOpen <= 0.025f) continue;

            int contentTop = panel.y + HEADER_HEIGHT;
            int contentBottom = panel.y + panelHeight - 4;
            context.enableScissor(panel.x, contentTop, panel.x + COLUMN_WIDTH, contentBottom);

            int rowY = contentTop + 3 - panel.scroll;
            for (Module module : visible) {
                boolean hovered = mouseY >= contentTop && mouseY <= contentBottom
                        && mouseX >= panel.x + 4
                        && mouseX <= panel.x + COLUMN_WIDTH - 4
                        && mouseY >= rowY + 2
                        && mouseY <= rowY + ROW_HEIGHT - 1;

                TOGGLE_ANIM.put(module, module.isEnabled() ? 1f : 0f);
                animate(HOVER_ANIM, module, hovered ? 1f : 0f, 0.22f);
                EXPAND_ANIM.put(module, EXPANDED.contains(module) ? 1f : 0f);

                float toggle = module.isEnabled() ? 1f : 0f;
                float hover = easeOutQuart(anim(HOVER_ANIM, module));
                float expand = EXPANDED.contains(module) ? 1f : 0f;
                int rowColor = mixColor(COLOR_ROW_OFF, accentRowColor(), toggle);
                rowColor = mixColor(rowColor, mixColor(COLOR_ROW_HOVER, brighten(accentRowColor(), 12), toggle), hover);

                drawModuleRow(context, panel.x, rowY, COLUMN_WIDTH, ROW_HEIGHT, rowColor, toggle);
                if (!module.getSettings().isEmpty()) {
                    drawScaledLeftText(context, EXPANDED.contains(module) ? "-" : "+", panel.x + COLUMN_WIDTH - 15, rowY, ROW_HEIGHT, COLOR_DIM_TEXT, TEXT_SCALE);
                }
                int textColor = mixColor(COLOR_TEXT, 0xFFFFFFFF, Math.max(toggle, hover * 0.45f));
                drawScaledCenteredText(context, module.getName(), panel.x, rowY, COLUMN_WIDTH, ROW_HEIGHT, textColor, TEXT_SCALE);

                rowY += ROW_HEIGHT;
                if (expand > 0.025f && !module.getSettings().isEmpty()) {
                    List<ModuleSetting> settings = visibleSettings(module);
                    int visibleHeight = (int) (totalSettingsHeight(settings) * expand);
                    int subStart = rowY;
                    int drawn = 0;

                    for (ModuleSetting setting : settings) {
                        if (drawn >= visibleHeight) break;
                        int allowedHeight = Math.min(settingRowHeight(setting), visibleHeight - drawn);
                        boolean subHovered = mouseY >= contentTop && mouseY <= contentBottom
                                && mouseX >= panel.x + 7
                                && mouseX <= panel.x + COLUMN_WIDTH - 5
                                && mouseY >= rowY + 1
                                && mouseY <= rowY + allowedHeight - 1;
                        boolean listening = setting instanceof KeybindModuleSetting keybind && keybind.isListening();
                        int subColor = listening ? alpha(accentColor(), 0.25f) : (subHovered ? COLOR_SUB_ROW_HOVER : COLOR_SUB_ROW);
                        drawSubRow(context, panel.x, rowY, COLUMN_WIDTH, allowedHeight, subColor, expand);

                        if (allowedHeight >= 9) {
                            if (setting instanceof ColorModuleSetting colorSetting && allowedHeight >= SUB_ROW_HEIGHT * 2) {
                                int lineH = allowedHeight / 2;
                                drawScaledLeftText(context, colorSetting.getLabel(), panel.x + SUB_TEXT_LEFT_PADDING, rowY, lineH, COLOR_DIM_TEXT, SUB_TEXT_SCALE);
                                String valueText = colorSetting.getDisplayValue();
                                int valueWidth = (int) (font.width(styled(valueText)) * SUB_TEXT_SCALE);
                                drawScaledLeftText(context, valueText, panel.x + COLUMN_WIDTH - valueWidth - 30, rowY + lineH, lineH, listening ? accentColor() : COLOR_TEXT, SUB_TEXT_SCALE);
                                drawSettingControl(context, colorSetting, panel.x, rowY + lineH, lineH, subHovered);
                            } else {
                                drawScaledLeftText(context, setting.getLabel(), panel.x + SUB_TEXT_LEFT_PADDING, rowY, allowedHeight, COLOR_DIM_TEXT, SUB_TEXT_SCALE);
                                String valueText = setting.getDisplayValue();
                                int valueWidth = (int) (font.width(styled(valueText)) * SUB_TEXT_SCALE);
                                boolean textEditing = setting instanceof TextModuleSetting text && text.isEditing();
                                drawScaledLeftText(context, valueText, panel.x + COLUMN_WIDTH - valueWidth - 12, rowY, allowedHeight, listening || textEditing ? accentColor() : COLOR_TEXT, SUB_TEXT_SCALE);
                                drawSettingControl(context, setting, panel.x, rowY, allowedHeight, subHovered);
                                if (setting instanceof SliderModuleSetting slider) drawSlider(context, slider, panel.x, rowY, allowedHeight);
                            }
                        }

                        rowY += allowedHeight;
                        drawn += allowedHeight;
                    }

                    rowY = subStart + visibleHeight;
                }
            }

            context.disableScissor();
            if (maxScroll > 0) drawScrollbar(context, panel, panelHeight, fullHeight);
        }

        //? if 1.21.4 {
        /*context.pose().popPose();
        *///?} else {
        context.pose().popMatrix();
        //?}
        drawSearchChrome(context, mouseX, mouseY);
        drawResetButton(context, mouseX, mouseY);
    }

    //? if 1.21.4 {
    /*@Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (handleMouseClicked((int) mouseX, (int) mouseY, button)) return true;
        return super.mouseClicked(mouseX, mouseY, button);
    }
    *///?} else {
    @Override
    public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean doubled) {
        if (handleMouseClicked((int) mouseButtonEvent.x(), (int) mouseButtonEvent.y(), mouseButtonEvent.button())) return true;
        return super.mouseClicked(mouseButtonEvent, doubled);
    }
    //?}

    private boolean handleMouseClicked(int mouseX, int mouseY, int button) {
        if (button == 0 && clickSidebar(mouseX, mouseY)) return true;

        if (button == 0) {
            int[] resetBounds = resetButtonBounds();
            if (mouseX >= resetBounds[0] && mouseX <= resetBounds[0] + resetBounds[2]
                    && mouseY >= resetBounds[1] && mouseY <= resetBounds[1] + resetBounds[3]) {
                resetAllSettings();
                return true;
            }
        }

        for (Panel panel : panels) {
            if (mouseX >= panel.x
                    && mouseX <= panel.x + COLUMN_WIDTH
                    && mouseY >= panel.y
                    && mouseY <= panel.y + HEADER_HEIGHT) {
                if (button == 1 || mouseX >= panel.x + COLUMN_WIDTH - 24) {
                    toggleCategory(panel.category);
                    return true;
                }

                panel.dragging = true;
                panel.dragOffsetX = mouseX - panel.x;
                panel.dragOffsetY = mouseY - panel.y;
                return true;
            }

            if (COLLAPSED_CATEGORIES.contains(panel.category)) continue;

            List<Module> visible = visibleModules(panel);
            if (visible.isEmpty()) continue;

            int fullHeight = animatedPanelHeight(panel, visible);
            int panelHeight = Math.min(fullHeight, panelMaxHeight(panel));
            int contentTop = panel.y + HEADER_HEIGHT;
            int contentBottom = panel.y + panelHeight - 4;
            if (mouseY < contentTop || mouseY > contentBottom) continue;

            int rowY = contentTop + 3 - panel.scroll;

            for (Module module : visible) {
                boolean inRowBounds = mouseX >= panel.x + 4
                        && mouseX <= panel.x + COLUMN_WIDTH - 4
                        && mouseY >= rowY + 2
                        && mouseY <= rowY + ROW_HEIGHT - 1;

                if (inRowBounds) {
                    if (button == 1 || mouseX >= panel.x + COLUMN_WIDTH - 24) {
                        if (EXPANDED.contains(module)) EXPANDED.remove(module);
                        else if (!module.getSettings().isEmpty()) EXPANDED.add(module);
                    } else {
                        module.toggle();
                        ModuleConfig.save();
                    }
                    return true;
                }

                rowY += ROW_HEIGHT;
                if (EXPANDED.contains(module)) {
                    for (ModuleSetting setting : visibleSettings(module)) {
                        int rowHeight = settingRowHeight(setting);
                        boolean inSubBounds = mouseX >= panel.x + 7
                                && mouseX <= panel.x + COLUMN_WIDTH - 5
                                && mouseY >= rowY + 1
                                && mouseY <= rowY + rowHeight - 1;

                        if (inSubBounds) {
                            if (button == 1 && setting instanceof KeybindModuleSetting keybind) {
                                keybind.clear();
                                return true;
                            }

                            if (button == 0) {
                                if (setting instanceof SliderModuleSetting slider) {
                                    draggingSlider = slider;
                                    draggingSliderPanelX = panel.x;
                                    setSliderFromMouse(slider, panel.x, mouseX);
                                } else {
                                    setting.onClick();
                                    if (setting instanceof TextModuleSetting text) editingText = text;
                                }
                                return true;
                            }
                        }

                        rowY += rowHeight;
                    }
                }
            }
        }

        return false;
    }

    //? if 1.21.4 {
    /*@Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (handleMouseDragged((int) mouseX, (int) mouseY)) return true;
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }
    *///?} else {
    @Override
    public boolean mouseDragged(MouseButtonEvent mouseButtonEvent, double dragX, double dragY) {
        if (handleMouseDragged((int) mouseButtonEvent.x(), (int) mouseButtonEvent.y())) return true;
        return super.mouseDragged(mouseButtonEvent, dragX, dragY);
    }
    //?}

    private boolean handleMouseDragged(int mouseX, int mouseY) {
        if (draggingSlider != null) {
            setSliderFromMouse(draggingSlider, draggingSliderPanelX, mouseX);
            return true;
        }

        for (Panel panel : panels) {
            if (panel.dragging) {
                panel.x = mouseX - panel.dragOffsetX;
                panel.y = mouseY - panel.dragOffsetY;
                SAVED_POSITIONS.put(panel.category, new int[]{panel.x, panel.y});
                return true;
            }
        }

        return false;
    }

    //? if 1.21.4 {
    /*@Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        handleMouseReleased();
        return super.mouseReleased(mouseX, mouseY, button);
    }
    *///?} else {
    @Override
    public boolean mouseReleased(MouseButtonEvent mouseButtonEvent) {
        handleMouseReleased();
        return super.mouseReleased(mouseButtonEvent);
    }
    //?}

    private void handleMouseReleased() {
        draggingSlider = null;
        for (Panel panel : panels) {
            panel.dragging = false;
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        for (Panel panel : panels) {
            if (COLLAPSED_CATEGORIES.contains(panel.category)) continue;

            List<Module> visible = visibleModules(panel);
            if (visible.isEmpty()) continue;

            int fullHeight = animatedPanelHeight(panel, visible);
            int panelHeight = Math.min(fullHeight, panelMaxHeight(panel));
            int maxScroll = Math.max(0, fullHeight - panelHeight);
            if (maxScroll <= 0) continue;

            boolean inside = mouseX >= panel.x && mouseX <= panel.x + COLUMN_WIDTH
                    && mouseY >= panel.y && mouseY <= panel.y + panelHeight;
            if (inside) {
                panel.scroll = Math.max(0, Math.min(maxScroll, panel.scroll - (int) (scrollY * SCROLL_SPEED)));
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    //? if 1.21.4 {
    /*@Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (handleKeyPressed(keyCode, scanCode)) return true;
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    *///?} else {
    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyEvent keyEvent) {
        if (handleKeyPressed(keyEvent.key(), keyEvent.scancode())) return true;
        return super.keyPressed(keyEvent);
    }
    //?}

    private boolean handleKeyPressed(int keyCode, int scanCode) {
        if (editingText != null && editingText.isEditing()) {
            if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE || keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER) {
                editingText.stopEditing();
                editingText = null;
                return true;
            }
            if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_BACKSPACE) {
                editingText.backspace();
                return true;
            }
        }

        for (Module module : ModuleManager.getModules()) {
            for (ModuleSetting setting : visibleSettings(module)) {
                if (setting instanceof KeybindModuleSetting keybind && keybind.isListening()) {
                    if (keybind.handleKeyPress(keyCode, scanCode)) return true;
                }
            }
        }
        return false;
    }

    //? if 1.21.4 {
    /*@Override
    public boolean charTyped(char chr, int modifiers) {
        if (handleCharTyped(chr)) return true;
        return super.charTyped(chr, modifiers);
    }
    *///?} else {
    @Override
    public boolean charTyped(CharacterEvent event) {
        if (handleCharTyped((char) event.codepoint())) return true;
        return super.charTyped(event);
    }
    //?}

    private boolean handleCharTyped(char chr) {
        if (editingText != null && editingText.isEditing()) {
            editingText.append(chr);
            return true;
        }
        return false;
    }

    @Override
    public void onClose() {
        ModuleConfig.save();
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

