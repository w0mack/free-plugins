package net.runelite.client.plugins.automedclue;

import lombok.Getter;
import net.runelite.api.ItemID;

public enum CluesTele {
    BURTHORPE(Type.JEWELLERY, -1, 219, 1, 1),
    BARBARIAN_OUTPOST(Type.JEWELLERY, -1, 219, 1, 2),
    CORPOREAL_BEAST(Type.JEWELLERY, -1, 219, 1, 3),
    TEARS_OF_GUTHIX(Type.JEWELLERY, -1, 219, 1, 4),
    WINTERTODT_CAMP(Type.JEWELLERY, -1, 219, 1, 5),
    DUEL_ARENA(Type.JEWELLERY, -1, 219, 1, 1),
    CASTLE_WARS(Type.JEWELLERY, -1, 219, 1, 2),
    FEROX_ENCLAVE(Type.JEWELLERY, -1, 219, 1, 3),
    EDGEVILLE(Type.JEWELLERY, -1, 219, 1, 1),
    KARAMJA(Type.JEWELLERY, -1, 219, 1, 2),
    DRAYNOR_VILLAGE(Type.JEWELLERY, -1, 219, 1, 3),
    AL_KHARID(Type.JEWELLERY, -1, 219, 1, 4),
    WIZARDS_TOWER(Type.JEWELLERY, -1, 219, 1, 1),
    THE_OUTPOST(Type.JEWELLERY, -1, 219, 1, 2),
    EAGLES_EYRIE(Type.JEWELLERY, -1, 219, 1, 3),
    FISHING_GUILD(Type.JEWELLERY, -1, 187, 3, 0),
    MINING_GUILD(Type.JEWELLERY, -1, 187, 3, 1),
    CRAFTING_GUILD(Type.JEWELLERY, -1, 187, 3, 2),
    COOKING_GUILD(Type.JEWELLERY, -1, 187, 3, 3),
    WOODCUTTING_GUILD(Type.JEWELLERY, -1, 187, 3, 4),
    FARMING_GUILD(Type.JEWELLERY, -1, 187, 3, 5),
    VARROCK(Type.SPELL, -1, 218, 21, 1),
    LUMBRIDGE(Type.SPELL, -1, 218, 24, 1),
    FALADOR(Type.SPELL, -1, 218, 27, 1),
    HOUSE(Type.SPELL, -1, 218, 29, 2),
    CAMELOT(Type.SPELL, -1, 218, 32, 1),
    ARCEUUS_LIBRARY(Type.ITEM, ItemID.ARCEUUS_LIBRARY_TELEPORT, -1, -1, -1),
    SALVE_GRAVEYARD(Type.ITEM, ItemID.SALVE_GRAVEYARD_TELEPORT, -1, -1, -1),
    WEST_ARDOUGNE(Type.ITEM, ItemID.WEST_ARDOUGNE_TELEPORT, -1, -1, -1);

    @Getter
    final Type type;
    @Getter
    private final int itemID, parentID, childID, param0;

    CluesTele(Type type, int itemID, int parentID, int childID, int param0) {
        this.type = type;
        this.itemID = itemID;
        this.parentID = parentID;
        this.childID = childID;
        this.param0 = param0;
    }

    enum Type {
        SPELL,
        ITEM,
        JEWELLERY
    }
}

/*

    ARCEUUS_LIBRARY(Type.ITEM, ItemID.ARCEUUS_LIBRARY_TELEPORT, -1, -1),
    SALVE_GRAVEYARD(Type.ITEM, ItemID.SALVE_GRAVEYARD_TELEPORT, -1, -1),
    FALADOR(Type.SPELL, -1, 218, 27),
 */