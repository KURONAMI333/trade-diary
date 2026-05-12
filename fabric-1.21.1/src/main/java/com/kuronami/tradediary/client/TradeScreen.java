package com.kuronami.tradediary.client;

import com.kuronami.tradediary.data.TradeEntry;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.PageButton;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Trade Diary — spread-page chronicle of villager trades (Fabric 1.21.1).
 *
 * <p>Identical to the NeoForge 1.21.1 viewer: same Patchouli {@code book_brown.png}
 * geometry, same ink palette, same filter cycle.
 */
public final class TradeScreen extends Screen {

    private static final ResourceLocation BOOK_TEX =
            ResourceLocation.parse("patchouli:textures/gui/book_brown.png");
    private static final int BOOK_WIDTH = 272;
    private static final int BOOK_HEIGHT = 180;
    private static final int TEX_SHEET = 512;
    private static final int TEX_SHEET_H = 256;
    private static final int LEFT_PAGE_X = 15;
    private static final int RIGHT_PAGE_X = 141;
    private static final int PAGE_WIDTH = 116;
    private static final int PAGE_HEIGHT = 156;
    private static final int PAGE_TOP_PADDING = 18;
    private static final int LINE_HEIGHT = 9;
    private static final int PAGE_BUTTON_Y = 157;
    private static final int PAGE_BACK_BUTTON_X = 43;
    private static final int PAGE_FORWARD_BUTTON_X = 116;

    private static final int ICON_SIZE = 16;
    private static final int ICON_GAP = 4;
    private static final int ENTRY_TEXT_X_OFFSET = ICON_SIZE + ICON_GAP;
    private static final int ENTRY_TEXT_WIDTH = PAGE_WIDTH - ENTRY_TEXT_X_OFFSET - 4;
    private static final int LINES_PER_ENTRY = 5;
    private static final int ENTRIES_PER_PAGE = 2;
    private static final int ENTRIES_PER_SPREAD = ENTRIES_PER_PAGE * 2;

    private static final int INK_PRIMARY = 0xFF000000;
    private static final int INK_NUMBER = 0xFF2E7D32;
    private static final int INK_SECONDARY = 0xFF2E4D2E;
    private static final int INK_TERTIARY = 0xFF6B8B6B;
    private static final int INK_RULE = 0x402E7D32;
    private static final int INK_ENCHANT = 0xFF8B2B6C;

    private static final SimpleDateFormat WALL_CLOCK_FMT =
            new SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.ROOT);

    private final List<TradeEntry> source;
    private List<TradeEntry> filtered;

    private int currentSpread = 0;
    private int totalSpreads = 1;
    private int bookLeft;
    private int bookTop;

    private EditBox searchBox;
    private Button frameFilterBtn;
    private PageButton prevBtn;
    private PageButton nextBtn;

    private TradeFilter filter = TradeFilter.ALL;

    public TradeScreen(List<TradeEntry> entries) {
        super(Component.translatable("tradediary.screen.title"));
        this.source = new ArrayList<>(entries);
        this.source.sort((a, b) -> Long.compare(b.epochMillis(), a.epochMillis()));
        this.filtered = this.source;
        recomputePages();
    }

    private void recomputePages() {
        this.totalSpreads = Math.max(1,
                (filtered.size() + ENTRIES_PER_SPREAD - 1) / ENTRIES_PER_SPREAD);
        if (currentSpread >= totalSpreads) currentSpread = totalSpreads - 1;
    }

    @Override
    protected void init() {
        this.bookLeft = (this.width - BOOK_WIDTH) / 2;
        this.bookTop = 4;

        int arrowY = bookTop + PAGE_BUTTON_Y - 3;
        this.prevBtn = new PageButton(
                bookLeft + PAGE_BACK_BUTTON_X, arrowY,
                false, b -> turnSpread(-1), true);
        this.nextBtn = new PageButton(
                bookLeft + PAGE_FORWARD_BUTTON_X, arrowY,
                true, b -> turnSpread(1), true);
        this.addRenderableWidget(this.prevBtn);
        this.addRenderableWidget(this.nextBtn);

        int controlsY = bookTop + BOOK_HEIGHT + 12;

        this.searchBox = new EditBox(this.font,
                this.width / 2 - 120, controlsY,
                160, 18,
                Component.translatable("tradediary.screen.search"));
        this.searchBox.setHint(Component.translatable("tradediary.screen.search.hint"));
        this.searchBox.setResponder(s -> applyFilters());
        this.addRenderableWidget(this.searchBox);

        this.frameFilterBtn = Button.builder(
                        Component.literal(filter.label()),
                        b -> {
                            filter = filter.next();
                            b.setMessage(Component.literal(filter.label()));
                            applyFilters();
                        })
                .bounds(this.width / 2 + 44, controlsY, 76, 18)
                .build();
        this.addRenderableWidget(this.frameFilterBtn);

        this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.done"),
                        b -> this.onClose())
                .bounds(this.width / 2 - 100, controlsY + 24, 200, 20)
                .build());

        this.setInitialFocus(this.searchBox);
        updatePageButtons();
    }

    private void turnSpread(int delta) {
        int next = currentSpread + delta;
        if (next < 0 || next >= totalSpreads) return;
        currentSpread = next;
        updatePageButtons();
    }

    private void updatePageButtons() {
        if (prevBtn != null) prevBtn.visible = currentSpread > 0;
        if (nextBtn != null) nextBtn.visible = currentSpread < totalSpreads - 1;
    }

    private void applyFilters() {
        String q = searchBox != null ? searchBox.getValue().toLowerCase(Locale.ROOT) : "";
        List<TradeEntry> out = new ArrayList<>();
        for (TradeEntry e : source) {
            if (!filter.accept(e)) continue;
            if (!q.isEmpty()) {
                boolean hit = e.resultItem().toLowerCase(Locale.ROOT).contains(q)
                        || e.profession().toLowerCase(Locale.ROOT).contains(q)
                        || e.costAItem().toLowerCase(Locale.ROOT).contains(q)
                        || e.costBItem().toLowerCase(Locale.ROOT).contains(q);
                if (!hit) continue;
            }
            out.add(e);
        }
        this.filtered = out;
        this.currentSpread = 0;
        recomputePages();
        updatePageButtons();
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
        g.blit(BOOK_TEX, bookLeft, bookTop, 0, 0, BOOK_WIDTH, BOOK_HEIGHT, TEX_SHEET, TEX_SHEET_H);

        Component bookTitle = Component.literal("Trade Diary")
                .withStyle(Style.EMPTY.withItalic(true).withColor(INK_NUMBER));
        g.drawString(this.font, bookTitle,
                bookLeft + LEFT_PAGE_X, bookTop + 8, INK_PRIMARY, false);

        Component pageLabel = Component.translatable("tradediary.screen.page",
                currentSpread + 1, totalSpreads);
        int pageW = this.font.width(pageLabel);
        g.drawString(this.font, pageLabel,
                bookLeft + RIGHT_PAGE_X + PAGE_WIDTH - pageW,
                bookTop + 8, INK_PRIMARY, false);

        if (filtered.isEmpty()) {
            renderEmptyState(g);
        } else {
            int startIdx = currentSpread * ENTRIES_PER_SPREAD;
            renderPageEntries(g, startIdx, ENTRIES_PER_PAGE,
                    bookLeft + LEFT_PAGE_X, bookTop + PAGE_TOP_PADDING);
            renderPageEntries(g, startIdx + ENTRIES_PER_PAGE, ENTRIES_PER_PAGE,
                    bookLeft + RIGHT_PAGE_X, bookTop + PAGE_TOP_PADDING);
        }

        super.render(g, mouseX, mouseY, partial);
    }

    private void renderEmptyState(GuiGraphics g) {
        Component empty = Component.translatable("tradediary.screen.empty");
        List<FormattedCharSequence> lines = this.font.split(empty, ENTRY_TEXT_WIDTH);
        int y = bookTop + PAGE_TOP_PADDING + PAGE_HEIGHT / 2 - lines.size() * LINE_HEIGHT / 2;
        for (FormattedCharSequence line : lines) {
            int lw = this.font.width(line);
            g.drawString(this.font, line,
                    bookLeft + LEFT_PAGE_X + (PAGE_WIDTH - lw) / 2,
                    y, INK_SECONDARY, false);
            y += LINE_HEIGHT;
        }
    }

    private void renderPageEntries(GuiGraphics g, int startIdx, int count,
                                   int pageX, int pageY) {
        int endIdx = Math.min(startIdx + count, filtered.size());
        int shown = endIdx - startIdx;
        if (shown <= 0) return;

        int entryHeight = LINES_PER_ENTRY * LINE_HEIGHT;
        int entryGap = 12;
        int blockHeight = shown * entryHeight + (shown - 1) * entryGap;
        int bodyHeight = PAGE_HEIGHT - 24;
        int topPad = Math.max(0, (bodyHeight - blockHeight) / 2);
        int contentY = pageY + topPad;

        for (int i = startIdx; i < endIdx; i++) {
            TradeEntry e = filtered.get(i);
            int entryNumber = filtered.size() - i;
            int entryY = contentY + (i - startIdx) * (entryHeight + entryGap);
            renderEntry(g, this.font, e, entryNumber, pageX, entryY);

            if (i < endIdx - 1) {
                drawRule(g, pageX, entryY + entryHeight + entryGap / 2 - 1, PAGE_WIDTH - 4);
            }
        }
    }

    private static void drawRule(GuiGraphics g, int x, int y, int width) {
        g.fill(x, y, x + width, y + 1, INK_RULE);
    }

    private void renderEntry(GuiGraphics g, Font font, TradeEntry e, int entryNo,
                             int x, int y) {
        // Result icon on the left. The cost is conveyed prominently in the
        // text layout instead - two bold lines visually represent the trade
        // (what you gave, what you got).
        ItemStack icon = resolveStack(e.resultItem(), Math.max(1, e.resultCount()));
        if (!icon.isEmpty()) {
            g.renderItem(icon, x, y);
            if (e.resultCount() > 1) {
                g.renderItemDecorations(font, icon, x, y);
            }
        }

        int textX = x + ENTRY_TEXT_X_OFFSET;

        // L1: No. + Day (italic green, small)
        Component numberLine = Component.literal("No. " + entryNo + " · Day " + e.worldDay())
                .withStyle(Style.EMPTY.withColor(INK_NUMBER).withItalic(true));
        g.drawString(font, numberLine, textX, y, INK_NUMBER, false);

        // L2: Cost (bold) - what the player gave. Combines costA and optional
        // costB ("Book × 1 + Emerald × 5" style for librarians etc).
        MutableComponent costDisplay = describeItem(e.costAItem(), e.costACount());
        if (!e.isSingleCost()) {
            costDisplay.append(Component.literal(" + "));
            costDisplay.append(describeItem(e.costBItem(), e.costBCount()));
        }
        costDisplay = costDisplay.withStyle(Style.EMPTY.withColor(INK_PRIMARY).withBold(true));
        drawWrappedSingleLine(g, font, costDisplay, textX, y + LINE_HEIGHT, INK_PRIMARY);

        // L3: → Result (bold, accent for enchanted books) - what the player got
        int resultColor = e.isEnchantedBook() ? INK_ENCHANT : INK_PRIMARY;
        MutableComponent resultLine = Component.literal("→ ")
                .append(describeItem(e.resultItem(), e.resultCount()));
        resultLine = resultLine.withStyle(Style.EMPTY.withColor(resultColor).withBold(true));
        drawWrappedSingleLine(g, font, resultLine, textX, y + 2 * LINE_HEIGHT, resultColor);

        // L4: Profession Lv X (secondary)
        String professionLabel = prettyProfession(e.profession());
        String fromLine = e.professionLevel() > 0
                ? professionLabel + " Lv " + e.professionLevel()
                : professionLabel;
        g.drawString(font, truncate(font, fromLine, ENTRY_TEXT_WIDTH),
                textX, y + 3 * LINE_HEIGHT, INK_SECONDARY, false);

        // L5: coords (tertiary)
        String tail = String.format(Locale.ROOT,
                "(%d, %d, %d)",
                e.x(), e.y(), e.z());
        g.drawString(font, truncate(font, tail, ENTRY_TEXT_WIDTH),
                textX, y + 4 * LINE_HEIGHT, INK_TERTIARY, false);
    }

    /**
     * Renders a styled component on a single line, taking the first wrapped
     * segment if it overflows. Preserves bold/color formatting that the plain
     * String truncate() can't (since drawString(String, ...) takes a color int
     * rather than honoring component style).
     */
    private void drawWrappedSingleLine(GuiGraphics g, Font font, Component comp,
                                       int x, int y, int fallbackColor) {
        FormattedCharSequence seq = font.split(comp, ENTRY_TEXT_WIDTH).stream()
                .findFirst()
                .orElse(comp.getVisualOrderText());
        g.drawString(font, seq, x, y, fallbackColor, false);
    }

    private MutableComponent describeItem(String id, int count) {
        if (id == null || id.isEmpty()) return Component.literal("(unknown)");
        try {
            ResourceLocation rl = ResourceLocation.parse(id);
            Item item = BuiltInRegistries.ITEM.getOptional(rl).orElse(null);
            if (item == null) return Component.literal(prettifyKey(id));
            MutableComponent name = Component.translatable(item.getDescriptionId());
            if (count > 1) name.append(Component.literal(" x " + count));
            return name;
        } catch (Exception ignored) {
            return Component.literal(prettifyKey(id));
        }
    }

    private String formatCost(TradeEntry e) {
        StringBuilder sb = new StringBuilder();
        sb.append(prettifyKey(e.costAItem()));
        if (e.costACount() > 1) sb.append(" x ").append(e.costACount());
        if (!e.isSingleCost()) {
            sb.append(" + ").append(prettifyKey(e.costBItem()));
            if (e.costBCount() > 1) sb.append(" x ").append(e.costBCount());
        }
        return sb.toString();
    }

    private String prettyProfession(String prof) {
        if (prof == null || prof.isEmpty()) return "Villager";
        int colon = prof.indexOf(':');
        String body = colon >= 0 ? prof.substring(colon + 1) : prof;
        return prettifyKey(body);
    }

    private ItemStack resolveStack(String id, int count) {
        if (id == null || id.isEmpty()) return ItemStack.EMPTY;
        try {
            ResourceLocation rl = ResourceLocation.parse(id);
            Item item = BuiltInRegistries.ITEM.getOptional(rl).orElse(null);
            return item == null ? ItemStack.EMPTY : new ItemStack(item, count);
        } catch (Exception ignored) {
            return ItemStack.EMPTY;
        }
    }

    private static String truncate(Font font, String s, int maxWidth) {
        if (font.width(s) <= maxWidth) return s;
        String ellipsis = "...";
        int ellipsisW = font.width(ellipsis);
        while (s.length() > 0 && font.width(s) + ellipsisW > maxWidth) {
            s = s.substring(0, s.length() - 1);
        }
        return s + ellipsis;
    }

    private static String prettifyKey(String id) {
        if (id == null || id.isEmpty()) return "";
        int colon = id.indexOf(':');
        String body = colon >= 0 ? id.substring(colon + 1) : id;
        body = body.replace('_', ' ');
        StringBuilder out = new StringBuilder(body.length());
        boolean capNext = true;
        for (int i = 0; i < body.length(); i++) {
            char c = body.charAt(i);
            if (c == ' ') {
                out.append(' ');
                capNext = true;
            } else if (capNext) {
                out.append(Character.toUpperCase(c));
                capNext = false;
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY > 0) turnSpread(-1);
        else if (scrollY < 0) turnSpread(1);
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.searchBox != null && this.searchBox.isFocused()) {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
        if (keyCode == GLFW.GLFW_KEY_LEFT) {
            turnSpread(-1);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_RIGHT) {
            turnSpread(1);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // intentionally empty - suppress pause-screen blur
    }

    private enum TradeFilter {
        ALL("Filter: All", e -> true),
        ENCHANTED("Filter: Books", TradeEntry::isEnchantedBook),
        WANDERING("Filter: Wandering",
                e -> e.profession().startsWith("minecraft:wandering"));

        final String label;
        final java.util.function.Predicate<TradeEntry> pred;

        TradeFilter(String label, java.util.function.Predicate<TradeEntry> pred) {
            this.label = label;
            this.pred = pred;
        }

        boolean accept(TradeEntry e) { return pred.test(e); }
        String label()              { return label; }

        TradeFilter next() {
            TradeFilter[] vs = values();
            return vs[(this.ordinal() + 1) % vs.length];
        }
    }
}
