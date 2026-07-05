package com.runal.client;

public class AutoTrashState {
    public static final AutoTrashState INSTANCE = new AutoTrashState();

    private boolean enabled = false;
    public boolean trashCommon = false;
    public boolean trashUncommon = false;
    public boolean trashRare = false;
    public boolean trashEpic = false;
    public boolean trashLegendary = false;
    public boolean trashMythical = false;

    private AutoTrashState() {}

    public boolean isEnabled() {
        return enabled;
    }

    public void toggle() {
        enabled = !enabled;
    }

    public boolean shouldTrash(String rarity) {
        return switch (rarity) {
            case "COMMON SHARD" -> trashCommon;
            case "UNCOMMON SHARD" -> trashUncommon;
            case "RARE SHARD" -> trashRare;
            case "EPIC SHARD" -> trashEpic;
            case "LEGENDARY SHARD" -> trashLegendary;
            case "MYTHICAL SHARD" -> trashMythical;
            default -> false;
        };
    }
}
