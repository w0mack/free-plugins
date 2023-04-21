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
package net.runelite.client.plugins.tbluedragons;

import lombok.Getter;
import net.runelite.api.ItemID;
import net.runelite.api.Prayer;
import net.runelite.client.config.*;

import java.util.List;
import java.util.Set;

@ConfigGroup("tBlueDragons")
public interface tBlueDragonsConfig extends Config {
    @ConfigSection(
            name = "Instructions",
            description = "",
            position = 1,
            keyName = "guide",
            closedByDefault = true
    )
    String guide = "Instructions";
    @ConfigSection(
            name = "Inventory",
            description = "",
            position = 4,
            keyName = "inventory",
            closedByDefault = true
    )
    String inventory = "Inventory";
    @ConfigSection(
            name = "Teleports",
            description = "",
            position = 5,
            keyName = "teleports",
            closedByDefault = true
    )
    String teleports = "Teleports";
    @ConfigSection(
            name = "Combat",
            description = "",
            position = 6,
            keyName = "combat",
            closedByDefault = true
    )
    String combat = "Combat";

    /*

    Start of config

     */
    @ConfigSection(
            name = "Prayers",
            description = "",
            position = 7,
            keyName = "prayers",
            closedByDefault = true
    )
    String prayers = "Prayers";
    @ConfigSection(
            name = "Loot",
            description = "",
            position = 8,
            keyName = "loot",
            closedByDefault = true
    )
    String loot = "Loot";
    @ConfigSection(
            name = "Sleep Delays",
            description = "",
            position = 990,
            keyName = "sleepDelays",
            closedByDefault = true
    )
    String sleepDelays = "Sleep Delays";
    @ConfigSection(
            name = "Tick Delays",
            description = "",
            position = 991,
            keyName = "tickDelays",
            closedByDefault = true
    )
    String tickDelays = "Tick Delays";

    @ConfigItem(
            keyName = "startPlugin",
            name = "Start/Stop",
            description = "",
            position = 0,
            title = "startPlugin"
    )
    default Button startPlugin() {
        return new Button();
    }

    @ConfigItem(
            keyName = "instructions",
            name = "Instructions",
            description = "",
            position = 1,
            section = guide
    )
    default String instructions() {
        return "Start at Falador west bank.\nRequires 70+ agility.\nSet autocast before starting.";
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

    @ConfigItem(
            name = "Mode",
            description = "",
            position = 3,
            keyName = "mode"
    )
    default mode mode() {
        return mode.KILL_DRAGONS;
    }

    @ConfigItem(
            keyName = "usePrayPot",
            name = "Use Prayer Pot",
            description = "",
            position = 0,
            section = inventory
    )
    default boolean usePrayPot() {
        return true;
    }

    @Range(min = 1, max = 8)
    @ConfigItem(
            keyName = "prayAmount",
            name = "Amount",
            description = "",
            position = 1,
            section = inventory,
            hidden = true,
            unhide = "usePrayPot"
    )
    default int prayAmount() {
        return 2;
    }

    @ConfigItem(
            keyName = "prayPot",
            name = "",
            description = "",
            position = 2,
            section = inventory,
            hidden = true,
            unhide = "usePrayPot"
    )
    default PrayerPot prayPot() {
        return PrayerPot.PRAYER_POTION;
    }

    @ConfigItem(
            keyName = "useCombatPot",
            name = "Use Combat Pot",
            description = "",
            position = 3,
            section = inventory
    )
    default boolean useCombatPot() {
        return true;
    }

    @Range(min = 1, max = 2)
    @ConfigItem(
            keyName = "combatAmount",
            name = "Amount",
            description = "",
            position = 5,
            section = inventory,
            hidden = true,
            unhide = "useCombatPot"
    )
    default int combatAmount() {
        return 1;
    }

    @ConfigItem(
            keyName = "combatPot",
            name = "",
            description = "",
            position = 6,
            section = inventory,
            hidden = true,
            unhide = "useCombatPot"
    )
    default CombatPot combatPot() {
        return CombatPot.BASTION;
    }

    @ConfigItem(
            keyName = "useAntifire",
            name = "Use Antifire",
            description = "",
            position = 7,
            section = inventory
    )
    default boolean useAntifire() {
        return true;
    }

    @Range(min = 1, max = 2)
    @ConfigItem(
            keyName = "antifireAmount",
            name = "Amount",
            description = "",
            position = 8,
            section = inventory,
            hidden = true,
            unhide = "useAntifire"
    )
    default int antifireAmount() {
        return 1;
    }

    @ConfigItem(
            keyName = "antifire",
            name = "",
            description = "",
            position = 9,
            section = inventory,
            hidden = true,
            unhide = "useAntifire"
    )
    default AntifirePot antifirePot() {
        return AntifirePot.EXT_SUPER_ANTIFIRE;
    }

    @ConfigItem(
            keyName = "useFood",
            name = "Use Food",
            description = "",
            position = 10,
            section = inventory
    )
    default boolean useFood() {
        return true;
    }

    @Range(min = 1, max = 16)
    @ConfigItem(
            keyName = "foodAmount",
            name = "Amount",
            description = "",
            position = 11,
            section = inventory,
            hidden = true,
            unhide = "useFood"
    )
    default int foodAmount() {
        return 0;
    }

    @ConfigItem(
            keyName = "food",
            name = "",
            description = "",
            position = 12,
            section = inventory,
            hidden = true,
            unhide = "useFood"
    )
    default Food food() {
        return Food.KARAMBWAN;
    }

    @ConfigItem(
            keyName = "stamina",
            name = "Drink Stamina at bank",
            description = "",
            position = 13,
            section = inventory
    )
    default boolean useStamina() {
        return true;
    }

    @ConfigItem(
            keyName = "runePouch",
            name = "Withdraw Rune Pouch",
            description = "Bring a rune pouch",
            position = 14,
            section = inventory
    )
    default boolean useRunePouch() {
        return true;
    }

    @ConfigItem(
            keyName = "takeRunes",
            name = "Withdraw Runes",
            description = "",
            position = 15,
            section = inventory
    )
    default Set<Runes> takeRunes() {
        return Set.of();
    }

    @ConfigItem(
            keyName = "useBonecrusher",
            name = "Use Bonecrusher",
            description = "",
            position = 50,
            section = inventory
    )
    default boolean useBonecrusher() {
        return false;
    }

    @ConfigItem(
            keyName = "bank",
            name = "Bank Method",
            description = "",
            position = 1,
            section = teleports
    )
    default Banking bankLoc() {
        return Banking.FALADOR_TELEPORT;
    }

    @ConfigItem(
            keyName = "panicTele",
            name = "Panic HP",
            description = "",
            position = 3,
            section = teleports
    )
    default int panicTele() {
        return 15;
    }

    @ConfigItem(
            keyName = "safespot",
            name = "Safespot",
            description = "Safespot the blue dragons",
            position = 0,
            section = combat
    )
    default boolean safespot() {
        return false;
    }

    @Range(min = 1, max = 99)
    @ConfigItem(
            keyName = "eatAt",
            name = "Eat Food at",
            description = "",
            position = 8,
            section = combat,
            hidden = true,
            unhide = "useFood"
    )
    default int eatAt() {
        return 20;
    }

    @Range(min = 1, max = 99)
    @ConfigItem(
            keyName = "restoreAt",
            name = "Drink Pray at",
            description = "",
            position = 9,
            section = combat,
            hidden = true,
            unhide = "usePrayPot"
    )
    default int restoreAt() {
        return 20;
    }

    @Range(min = 1, max = 130)
    @ConfigItem(
            keyName = "boostAtt",
            name = "Repot attack at",
            description = "",
            position = 10,
            section = combat,
            hidden = true,
            unhide = "useCombatPot"
    )
    default int boostAtt() {
        return 107;
    }

    @Range(min = 1, max = 130)
    @ConfigItem(
            keyName = "boostStr",
            name = "Repot strength at",
            description = "",
            position = 11,
            section = combat,
            hidden = true,
            unhide = "useCombatPot"
    )
    default int boostStr() {
        return 107;
    }

    @Range(min = 1, max = 130)
    @ConfigItem(
            keyName = "boostDef",
            name = "Repot defence at",
            description = "",
            position = 12,
            section = combat,
            hidden = true,
            unhide = "useCombatPot"
    )
    default int boostDef() {
        return 107;
    }

    /* start of prayers */

    @Range(min = 1, max = 130)
    @ConfigItem(
            keyName = "boostRan",
            name = "Repot ranged at",
            description = "",
            position = 13,
            section = combat,
            hidden = true,
            unhide = "useCombatPot"
    )
    default int boostRan() {
        return 107;
    }

    @Range(min = 1, max = 130)
    @ConfigItem(
            keyName = "boostMag",
            name = "Repot magic at",
            description = "",
            position = 14,
            section = combat,
            hidden = true,
            unhide = "useCombatPot"
    )
    default int boostMag() {
        return 107;
    }

    @ConfigItem(keyName = "prayType", name = "Method", description = "", position = 1, section = prayers)
    default PrayType prayType() {
        return PrayType.CUSTOM;
    }

    @ConfigItem(
            keyName = "flickQuickPrayers",
            name = "Flick Quick Prayers",
            description = "",
            position = 2,
            section = prayers,
            hidden = true,
            unhide = "prayType",
            unhideValue = "QUICK_PRAYERS"
    )
    default boolean flickQuickPrayers() {
        return false;
    }

    @ConfigItem(
            keyName = "useMeleeProt",
            name = "Use Protect from Melee",
            description = "",
            position = 3,
            section = prayers,
            hidden = true,
            unhide = "prayType",
            unhideValue = "CUSTOM"
    )
    default boolean useMeleeProt() {
        return false;
    }

    @ConfigItem(
            keyName = "flickMelee",
            name = "Flick Protect from Melee",
            description = "",
            position = 4,
            section = prayers,
            hidden = true,
            unhide = "prayType",
            unhideValue = "CUSTOM"
    )
    default boolean flickMelee() {
        return false;
    }

    @ConfigItem(
            keyName = "useOffensive",
            name = "Use Offensive Prayer",
            description = "Use an attack boosting prayer",
            position = 5,
            section = prayers,
            hidden = true,
            unhide = "prayType",
            unhideValue = "CUSTOM"
    )
    default boolean useOffPray() {
        return false;
    }

    /* end of prayers */

    @ConfigItem(
            keyName = "flickOffensive",
            name = "Flick Offensive Prayer",
            description = "",
            position = 6,
            section = prayers,
            hidden = true,
            unhide = "prayType",
            unhideValue = "CUSTOM"

    )
    default boolean flickOffensive() {
        return false;
    }

    @ConfigItem(
            keyName = "offPray",
            name = "",
            description = "",
            position = 7,
            section = prayers,
            hidden = true,
            unhide = "prayType",
            unhideValue = "CUSTOM"
    )
    default OffensivePray offPray() {
        return OffensivePray.RIGOUR;
    }

    @ConfigItem(keyName = "telegrab", name = "Telegrab loot", description = "", position = 1, section = loot, hidden = true, unhide = "safespot")
    default boolean telegrabLoot() {
        return true;
    }

    @ConfigItem(keyName = "lootBones", name = "Always loot Dragon bones", description = "", position = 2, section = loot)
    default boolean lootBones() {
        return true;
    }

    @ConfigItem(keyName = "lootHides", name = "Always loot Blue dragonhide", description = "", position = 3, section = loot)
    default boolean lootHides() {
        return false;
    }

    @ConfigItem(keyName = "lootValue", name = "Item value to loot", description = "Loot items over this value", position = 4, section = loot)
    default int lootValue() {
        return 10000;
    }

    @ConfigItem(keyName = "includedItems", name = "Included items", description = "Full or partial names of items to loot regardless of value<br>Seperate with a comma", position = 5, section = loot)
    default String includedItems() {
        return "rune longsword";
    }


	/*

	End of config

	 */

    @ConfigItem(keyName = "excludedItems", name = "Excluded items", description = "Full or partial names of items to NOT loot<br>Seperate with a comma", position = 6, section = loot)
    default String excludedItems() {
        return "ruby bolt,diamond bolt,emerald bolt,dragonstone bolt";
    }

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

    enum mode {
        KILL_DRAGONS,
        LOOT_SCALES
    }

    enum Runes {
        AIR(ItemID.AIR_RUNE),
        WATER(ItemID.WATER_RUNE),
        EARTH(ItemID.EARTH_RUNE),
        FIRE(ItemID.FIRE_RUNE),
        MIND(ItemID.MIND_RUNE),
        CHAOS(ItemID.CHAOS_RUNE),
        DEATH(ItemID.DEATH_RUNE),
        BLOOD(ItemID.BLOOD_RUNE),
        WRATH(ItemID.WRATH_RUNE),
        LAW(ItemID.LAW_RUNE);
        @Getter
        private final int ID;

        Runes(int ID) {
            this.ID = ID;
        }
    }

    enum PrayType {
        NONE,
        QUICK_PRAYERS,
        CUSTOM
    }

    enum Skills {
        ATTACK,
        STRENGTH,
        DEFENCE,
        MAGIC,
        RANGED
    }

    enum OffensivePray {
        SHARP_EYE(Prayer.SHARP_EYE),
        HAWK_EYE(Prayer.HAWK_EYE),
        EAGLE_EYE(Prayer.EAGLE_EYE),
        RIGOUR(Prayer.RIGOUR),
        MYSTIC_WILL(Prayer.MYSTIC_WILL),
        MYSTIC_LORE(Prayer.MYSTIC_LORE),
        MYSTIC_MIGHT(Prayer.MYSTIC_MIGHT),
        AUGURY(Prayer.AUGURY),
        CHIVALRY(Prayer.CHIVALRY),
        PIETY(Prayer.PIETY);
        @Getter
        private final Prayer prayer;

        OffensivePray(Prayer prayer) {
            this.prayer = prayer;
        }
    }

    enum Banking {
        FALADOR_TELEPORT,
        FALADOR_TELETAB
    }

    enum CombatPot {
        RANGING(
                List.of(ItemID.RANGING_POTION4, ItemID.RANGING_POTION3, ItemID.RANGING_POTION2, ItemID.RANGING_POTION1)
        ),
        DIVINE_RANGING(
                List.of(ItemID.DIVINE_RANGING_POTION4, ItemID.DIVINE_RANGING_POTION3, ItemID.DIVINE_RANGING_POTION2, ItemID.DIVINE_RANGING_POTION1)
        ),
        BASTION(
                List.of(ItemID.BASTION_POTION4, ItemID.BASTION_POTION3, ItemID.BASTION_POTION2, ItemID.BASTION_POTION1)
        ),
        DIVINE_BASTION(
                List.of(ItemID.DIVINE_BASTION_POTION4, ItemID.DIVINE_BASTION_POTION3, ItemID.DIVINE_BASTION_POTION2, ItemID.DIVINE_BASTION_POTION1)
        ),
        MAGIC(
                List.of(ItemID.MAGIC_POTION4, ItemID.MAGIC_POTION3, ItemID.MAGIC_POTION2, ItemID.MAGIC_POTION1)
        ),
        DIVINE_MAGIC(
                List.of(ItemID.DIVINE_MAGIC_POTION4, ItemID.DIVINE_MAGIC_POTION3, ItemID.DIVINE_MAGIC_POTION2, ItemID.DIVINE_MAGIC_POTION1)
        ),
        SUPER_ATT_STR(
                List.of(ItemID.SUPER_ATTACK4, ItemID.SUPER_ATTACK3, ItemID.SUPER_ATTACK2, ItemID.SUPER_ATTACK1),
                List.of(ItemID.SUPER_STRENGTH4, ItemID.SUPER_STRENGTH3, ItemID.SUPER_STRENGTH2, ItemID.SUPER_STRENGTH1)
        ),
        SUPER_ATT_STR_DEF(
                List.of(ItemID.SUPER_ATTACK4, ItemID.SUPER_ATTACK3, ItemID.SUPER_ATTACK2, ItemID.SUPER_ATTACK1),
                List.of(ItemID.SUPER_STRENGTH4, ItemID.SUPER_STRENGTH3, ItemID.SUPER_STRENGTH2, ItemID.SUPER_STRENGTH1),
                List.of(ItemID.SUPER_DEFENCE4, ItemID.SUPER_DEFENCE3, ItemID.SUPER_DEFENCE2, ItemID.SUPER_DEFENCE1)
        ),
        SUPER_COMBAT(
                List.of(ItemID.SUPER_COMBAT_POTION4, ItemID.SUPER_COMBAT_POTION3, ItemID.SUPER_COMBAT_POTION2, ItemID.SUPER_COMBAT_POTION1)
        ),
        DIVINE_SUPER_COMBAT(
                List.of(ItemID.DIVINE_SUPER_COMBAT_POTION4, ItemID.DIVINE_SUPER_COMBAT_POTION3, ItemID.DIVINE_SUPER_COMBAT_POTION2, ItemID.DIVINE_SUPER_COMBAT_POTION1)
        );

        @Getter
        private final List<Integer>[] combatPots;

        @SafeVarargs
        CombatPot(List<Integer>... combatPots) {
            this.combatPots = combatPots;
        }
    }

    enum PrayerPot {
        PRAYER_POTION(List.of(ItemID.PRAYER_POTION4, ItemID.PRAYER_POTION3, ItemID.PRAYER_POTION2, ItemID.PRAYER_POTION1)),
        SUPER_RESTORE(List.of(ItemID.SUPER_RESTORE4, ItemID.SUPER_RESTORE3, ItemID.SUPER_RESTORE2, ItemID.SUPER_RESTORE1));

        @Getter
        private final List<Integer> prayerPot;

        PrayerPot(List<Integer> prayerPot) {
            this.prayerPot = prayerPot;
        }
    }

    enum AntifirePot {
        ANTIFIRE_POTION(List.of(ItemID.ANTIFIRE_POTION4, ItemID.ANTIFIRE_POTION3, ItemID.ANTIFIRE_POTION2, ItemID.ANTIFIRE_POTION1)),
        EXT_ANTIFIRE_POTION(List.of(ItemID.EXTENDED_ANTIFIRE4, ItemID.EXTENDED_ANTIFIRE3, ItemID.EXTENDED_ANTIFIRE2, ItemID.EXTENDED_ANTIFIRE1)),
        SUPER_ANTIFIRE(List.of(ItemID.SUPER_ANTIFIRE_POTION4, ItemID.SUPER_ANTIFIRE_POTION3, ItemID.SUPER_ANTIFIRE_POTION2, ItemID.SUPER_ANTIFIRE_POTION1)),
        EXT_SUPER_ANTIFIRE(List.of(ItemID.EXTENDED_SUPER_ANTIFIRE4, ItemID.EXTENDED_SUPER_ANTIFIRE3, ItemID.EXTENDED_SUPER_ANTIFIRE2, ItemID.EXTENDED_SUPER_ANTIFIRE1));

        @Getter
        private final List<Integer> antifirePot;

        AntifirePot(List<Integer> antifirePot) {
            this.antifirePot = antifirePot;
        }
    }

    enum Food {
        TUNA(List.of(ItemID.TUNA), 10),
        CAKE(List.of(ItemID.CAKE, ItemID.CAKE + 2, ItemID.CAKE + 4), 12),
        LOBSTER(List.of(ItemID.LOBSTER), 12),
        SWORDFISH(List.of(ItemID.SWORDFISH), 14),
        POTATO_BUTTER(List.of(ItemID.POTATO_WITH_BUTTER), 14),
        CHOCOLATE_CAKE(List.of(ItemID.CHOCOLATE_CAKE, ItemID.CHOCOLATE_CAKE + 2, ItemID.CHOCOLATE_CAKE + 4), 15),
        MONKFISH(List.of(ItemID.MONKFISH), 16),
        KARAMBWAN(List.of(ItemID.COOKED_KARAMBWAN), 18),
        MUSHROOM_POTATO(List.of(ItemID.MUSHROOM_POTATO), 20),
        SHARK(List.of(ItemID.SHARK), 20);

        @Getter
        private final List<Integer> food;
        @Getter
        private final int heal;

        Food(List<Integer> food, int heal) {
            this.food = food;
            this.heal = heal;
        }
    }
}