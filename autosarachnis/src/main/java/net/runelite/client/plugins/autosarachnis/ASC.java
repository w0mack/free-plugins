/*
 * Copyright (c) 2018, Andrew EP | ElPinche256 <https://github.com/ElPinche256>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.autosarachnis;

import lombok.Getter;
import net.runelite.api.ItemID;
import net.runelite.client.config.*;

@ConfigGroup("AutoSarachnis")
public interface ASC extends Config {

    @ConfigItem(keyName = "startPlugin", name = "Start/Stop", description = "", position = 1, title = "startPlugin")
    default Button startPlugin() {
        return new Button();
    }

    @ConfigItem(
            keyName = "showOverlay",
            name = "Show UI",
            description = "Show the UI on screen",
            position = 2
    )
    default boolean showOverlay() {
        return true;
    }

    @ConfigSection(
            name = "Sleep Delays",
            description = "",
            position = 3,
            keyName = "sleepDelays"
    )
    String sleepDelays = "Sleep Delays";

    @Range(
            min = 0,
            max = 550
    )
    @ConfigItem(
            keyName = "sleepMin",
            name = "Sleep Min",
            description = "",
            position = 2,
            section = sleepDelays
    )
    default int sleepMin() {
        return 20;
    }

    @Range(
            min = 0,
            max = 550
    )
    @ConfigItem(
            keyName = "sleepMax",
            name = "Sleep Max",
            description = "",
            position = 3,
            section = sleepDelays
    )
    default int sleepMax() {
        return 200;
    }

    @Range(
            min = 0,
            max = 550
    )
    @ConfigItem(
            keyName = "sleepTarget",
            name = "Sleep Target",
            description = "",
            position = 4,
            section = sleepDelays
    )
    default int sleepTarget() {
        return 50;
    }

    @Range(
            min = 0,
            max = 550
    )
    @ConfigItem(
            keyName = "sleepDeviation",
            name = "Sleep Deviation",
            description = "",
            position = 5,
            section = sleepDelays
    )
    default int sleepDeviation() {
        return 10;
    }

    @ConfigItem(
            keyName = "sleepWeightedDistribution",
            name = "Sleep Weighted Distribution",
            description = "Shifts the random distribution towards the lower end at the target, otherwise it will be an even distribution",
            position = 6,
            section = sleepDelays
    )
    default boolean sleepWeightedDistribution() {
        return false;
    }

    @ConfigSection(
            name = "Tick Delays",
            description = "",
            position = 4,
            keyName = "tickDelays"
    )
    String tickDelays = "Tick Delays";

    @Range(
            min = 0,
            max = 10
    )
    @ConfigItem(
            keyName = "tickDelaysMin",
            name = "Game Tick Min",
            description = "",
            position = 8,
            section = tickDelays
    )
    default int tickDelaysMin() {
        return 1;
    }

    @Range(
            min = 0,
            max = 10
    )
    @ConfigItem(
            keyName = "tickDelaysMax",
            name = "Game Tick Max",
            description = "",
            position = 9,
            section = tickDelays
    )
    default int tickDelaysMax() {
        return 3;
    }

    @Range(
            min = 0,
            max = 10
    )
    @ConfigItem(
            keyName = "tickDelaysTarget",
            name = "Game Tick Target",
            description = "",
            position = 10,
            section = tickDelays
    )
    default int tickDelaysTarget() {
        return 2;
    }

    @Range(
            min = 0,
            max = 10
    )
    @ConfigItem(
            keyName = "tickDelaysDeviation",
            name = "Game Tick Deviation",
            description = "",
            position = 11,
            section = tickDelays
    )
    default int tickDelaysDeviation() {
        return 1;
    }

    @ConfigItem(
            keyName = "tickDelaysWeightedDistribution",
            name = "Game Tick Weighted Distribution",
            description = "Shifts the random distribution towards the lower end at the target, otherwise it will be an even distribution",
            position = 12,
            section = tickDelays
    )
    default boolean tickDelaysWeightedDistribution() {
        return false;
    }

    @ConfigSection(
            name = "Weapons",
            description = "",
            position = 5,
            keyName = "weapons"
    )
    String weapons = "Weapons";

    @ConfigItem(keyName = "mainhand", name = "Mainhand", description = "Mainhand item", section = weapons, position = 1)
    default Mainhand mainhand() {
        return Mainhand.ABYSSAL_BLUDGEON;
    }

    @ConfigItem(keyName = "offhand", name = "Offhand", description = "Offhand item", section = weapons, position = 2)
    default Offhand offhand() {
        return Offhand.AVERNIC_DEFENDER;
    }

    @ConfigItem(keyName = "specWeapon", name = "Special", description = "Spec weapon (will use offhand if item is 1 handed)", section = weapons, position = 3)
    default Special specWeapon() {
        return Special.DRAGON_MACE;
    }

    @ConfigSection(
            name = "Inventory + Prayer",
            description = "",
            position = 6,
            keyName = "inventory"
    )
    String inventory = "Inventory";

    @ConfigItem(keyName = "foodID", name = "Food", description = "The name of your food", position = 1, section = inventory)
    default Food food() {
        return Food.KARAMBWAN;
    }

    @Range(min = 1, max = 28)
    @ConfigItem(keyName = "withdrawFood", name = "Withdraw food", description = "Quantity of food to bring", position = 2, section = inventory)
    default int withdrawFood() {
        return 14;
    }

    @Range(min = 1, max = 99)
    @ConfigItem(keyName = "eatAt", name = "Eat food at", description = "Eat food when under this HP", position = 4, section = inventory)
    default int eatAt() {
        return 45;
    }

    @ConfigItem(keyName = "prayerID", name = "Prayer restore", description = "The name of your prayer restore", position = 5, section = inventory)
    default Prayer prayer() {
        return Prayer.PRAYER_POTION;
    }

    @Range(min = 1, max = 8)
    @ConfigItem(keyName = "prayerAmount", name = "Prayer pots", description = "Quantity of prayer restores to bring", position = 6, section = inventory)
    default int prayerAmount() {
        return 5;
    }

    @Range(min = 1, max = 99)
    @ConfigItem(keyName = "restoreAt", name = "Drink prayer at", description = "Drink prayer restore when under this amount of prayer", position = 8, section = inventory)
    default int restoreAt() {
        return 20;
    }

    @ConfigItem(keyName = "piety", name = "Use Piety", description = "Use piety during the kill", position = 9, section = inventory)
    default boolean usePiety() {
        return true;
    }

    @ConfigItem(keyName = "chivalry", name = "Use Chivalry", description = "Use chivalry during the kill", position = 10, section = inventory)
    default boolean useChivalry() {
        return false;
    }

    @ConfigItem(keyName = "superCombatID", name = "Boost", description = "The name of your combat pot", position = 11, section = inventory)
    default SuperCombat combatPotion() {
        return SuperCombat.SUPER_COMBAT;
    }

    @Range(min = 50, max = 120)
    @ConfigItem(keyName = "boostLevel", name = "Re-boost at", description = "The level to drink re-pot at - checks strength level", position = 12, section = inventory)
    default int boostLevel() {
        return 107;
    }

    @ConfigSection(
            name = "Teleports + PoH",
            description = "",
            position = 7,
            keyName = "teleports"
    )
    String teleports = "Teleports";

    @ConfigItem(keyName = "hosidius", name = "Hosidius", description = "The method to tele to Hosidius", position = 1, section = teleports)
    default Hosidius hosidius() {
        return Hosidius.MOUNTED_XERICS;
    }

    @ConfigItem(keyName = "houseTele", name = "PoH", description = "The method to tele to your PoH (if using)", position = 2, section = teleports)
    default HouseTele houseTele() {
        return HouseTele.HOUSE_TELEPORT;
    }

    @ConfigItem(keyName = "usePool", name = "Use PoH pool", description = "Restore stats", position = 3, section = teleports)
    default boolean usePool() {
        return true;
    }

    @ConfigItem(keyName = "poolID", name = "Pool ID", description = "Game Object ID for pool in PoH", position = 4, hidden = true, unhide = "usePool", section = teleports)
    default int poolID() {
        return 29241;
    }

    @ConfigSection(
            name = "Loot",
            description = "",
            position = 8,
            keyName = "lootSection"
    )
    String lootSection = "Loot";

    @ConfigItem(keyName = "eatLoot", name = "Eat food to loot", description = "", position = 1, section = lootSection)
    default boolean eatLoot() {
        return true;
    }

    @ConfigItem(keyName = "lootValue", name = "Item value to loot", description = "Loot items over this value", position = 2, section = lootSection)
    default int lootValue() {
        return 5000;
    }

    @ConfigItem(keyName = "includedItems", name = "Included items", description = "Full or partial names of items to loot regardless of value<br>Seperate with a comma", position = 3, section = lootSection)
    default String includedItems() {
        return "coins";
    }

    @ConfigItem(keyName = "excludedItems", name = "Excluded items", description = "Full or partial names of items to NOT loot<br>Seperate with a comma", position = 4, section = lootSection)
    default String excludedItems() {
        return "mithril arrow";
    }


    /* END OF MAIN CONFIG */

    @ConfigItem(keyName = "invokes", name = "Use invokes (use with caution)", description = "Potentially detected; use with caution", position = 998)
    default boolean invokes() {
        return false;
    }

    @ConfigItem(
            keyName = "debug",
            name = "Debug Messages",
            description = "",
            position = 999
    )
    default boolean debug() {
        return false;
    }

    enum Hosidius {
        MOUNTED_XERICS(33412),
        POH_TELEPORT(ItemID.HOSIDIUS_TELEPORT);
        @Getter
        private final int ID;

        Hosidius(int ID) {
            this.ID = ID;
        }
    }

    enum Mainhand {
        SCYTHE_OF_VITUR(ItemID.SCYTHE_OF_VITUR, true),
        INQUISITORS_MACE(ItemID.INQUISITORS_MACE, false),
        ABYSSAL_BLUDGEON(ItemID.ABYSSAL_BLUDGEON, true),
        GHRAZI_RAPIER(ItemID.GHRAZI_RAPIER, false),
        SARACHNIS_CUDGEL(ItemID.SARACHNIS_CUDGEL, false),
        ZAMORAKIAN_HASTA(ItemID.ZAMORAKIAN_HASTA, false),
        ABYSSAL_WHIP(ItemID.ABYSSAL_WHIP, false),
        LEAFBLADED_BATTLEAXE(ItemID.LEAFBLADED_BATTLEAXE, false),
        VIGGORAS_CHAINMACE_U(ItemID.VIGGORAS_CHAINMACE_U, false),
        DRAGON_MACE(ItemID.DRAGON_MACE, false),
        DINHS_BULWARK(ItemID.DINHS_BULWARK, true);
        @Getter
        private final int itemID;
        @Getter
        private final boolean is2Handed;

        Mainhand(int itemID, boolean bothHands) {
            this.itemID = itemID;
            this.is2Handed = bothHands;
        }
    }

    enum Offhand {
        NONE(-1),
        AVERNIC_DEFENDER(ItemID.AVERNIC_DEFENDER),
        AVERNIC_DEFENDER_L(ItemID.AVERNIC_DEFENDER_L),
        DRAGON_DEFENDER(ItemID.DRAGON_DEFENDER),
        SPECTRAL_SPIRIT_SHIELD(ItemID.SPECTRAL_SPIRIT_SHIELD),
        DRAGONFIRE_SHIELD(ItemID.DRAGONFIRE_SHIELD),
        RUNE_DEFENDER(ItemID.RUNE_DEFENDER),
        TOKTZ_KET_XIL(ItemID.TOKTZKETXIL);
        @Getter
        private final int itemID;

        Offhand(int itemID) {
            this.itemID = itemID;
        }
    }

    enum Special {
        NONE(-1, 1000, false),
        DRAGON_CLAWS(ItemID.DRAGON_CLAWS, 50, true),
        DRAGON_WARHAMMER(ItemID.DRAGON_WARHAMMER, 50, false),
        BANDOS_GODSWORD(ItemID.BANDOS_GODSWORD, 50, true),
        DRAGON_MACE(ItemID.DRAGON_MACE, 25, false);
        @Getter
        private final int itemID, specAmt;
        @Getter
        private final boolean is2Handed;

        Special(int itemID, int specAmt, boolean is2Handed) {
            this.itemID = itemID;
            this.specAmt = specAmt;
            this.is2Handed = is2Handed;
        }
    }

    enum Food {
        MANTA_RAY(ItemID.MANTA_RAY),
        TUNA_POTATO(ItemID.TUNA_POTATO),
        DARK_CRAB(ItemID.DARK_CRAB),
        ANGLERFISH(ItemID.ANGLERFISH),
        SEA_TURTLE(ItemID.SEA_TURTLE),
        MUSHROOM_POTATO(ItemID.MUSHROOM_POTATO),
        SHARK(ItemID.SHARK),
        KARAMBWAN(ItemID.COOKED_KARAMBWAN);
        @Getter
        private final int id;

        Food(int id) {
            this.id = id;
        }
    }

    enum Prayer {
        PRAYER_POTION(ItemID.PRAYER_POTION4, ItemID.PRAYER_POTION3, ItemID.PRAYER_POTION2, ItemID.PRAYER_POTION1),
        SUPER_RESTORE(ItemID.SUPER_RESTORE4, ItemID.SUPER_RESTORE3, ItemID.SUPER_RESTORE2, ItemID.SUPER_RESTORE1);
        @Getter
        private final int dose4, dose3, dose2, dose1;

        Prayer(int dose4, int dose3, int dose2, int dose1) {
            this.dose1 = dose1;
            this.dose2 = dose2;
            this.dose3 = dose3;
            this.dose4 = dose4;
        }
    }

    enum HouseTele {
        NONE(-1),
        CONSTRUCTION_CAPE_T(ItemID.CONSTRUCT_CAPET),
        CONSTRUCTION_CAPE(ItemID.CONSTRUCT_CAPE),
        HOUSE_TABLET(ItemID.TELEPORT_TO_HOUSE),
        HOUSE_TELEPORT(ItemID.RUNE_POUCH);
        @Getter
        private final int id;

        HouseTele(int id) {
            this.id = id;
        }
    }

    enum SuperCombat {
        NONE(-1, -1, -1, -1),
        DIVINE_SUPER_COMBAT(ItemID.DIVINE_SUPER_COMBAT_POTION4, ItemID.DIVINE_SUPER_COMBAT_POTION3, ItemID.DIVINE_SUPER_COMBAT_POTION2, ItemID.DIVINE_SUPER_COMBAT_POTION1),
        SUPER_COMBAT(ItemID.SUPER_COMBAT_POTION4, ItemID.SUPER_COMBAT_POTION3, ItemID.SUPER_COMBAT_POTION2, ItemID.SUPER_COMBAT_POTION1);
        @Getter
        private final int dose4, dose3, dose2, dose1;

        SuperCombat(int dose4, int dose3, int dose2, int dose1) {
            this.dose4 = dose4;
            this.dose3 = dose3;
            this.dose2 = dose2;
            this.dose1 = dose1;
        }
    }
}