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
package net.runelite.client.plugins.cssarachnishelper;

import lombok.Getter;
import net.runelite.api.ItemID;
import net.runelite.api.Prayer;
import net.runelite.client.config.*;

import java.util.Set;

@ConfigGroup("SarachnisHelper")
public interface SarachnisHelperConfig extends Config {

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
            name = "Boss Config",
            description = "",
            position = 3,
            keyName = "bosses"
    )
    String bosses = "Boss Config";

    @ConfigItem(keyName = "attackBoss", name = "Attack boss", description = "", position = 1, section = bosses)
    default boolean attackBoss() {
        return true;
    }

    @ConfigItem(keyName = "offensivePray", name = "Offensive Pray", description = "", position = 2, section = bosses)
    default OffensivePrayer offensivePray() {
        return OffensivePrayer.NONE;
    }

    @ConfigSection(
            name = "Inventory",
            description = "",
            position = 4,
            keyName = "inventory"
    )
    String inventory = "Inventory";

    @ConfigItem(keyName = "eatFood", name = "Eat food", description = "", position = 3, section = inventory)
    default boolean eatFood() {
        return true;
    }

    @ConfigItem(keyName = "eatAt", name = "Eat food at", description = "", position = 4, section = inventory, hidden = true, unhide = "eatFood")
    default int eatAt() {
        return 20;
    }

    @ConfigItem(keyName = "drinkPray", name = "Restore prayer", description = "", position = 5, section = inventory)
    default boolean drinkPray() {
        return true;
    }

    @ConfigItem(keyName = "restoreAt", name = "Restore prayer at", description = "", position = 6, section = inventory, hidden = true, unhide = "drinkPray")
    default int restoreAt() {
        return 20;
    }

    @ConfigItem(keyName = "drinkCombatBoost", name = "Drink combat pots", description = "", position = 7, section = inventory)
    default boolean drinkCombatBoost() {
        return true;
    }

    @ConfigItem(keyName = "combatPot", name = "Pot", description = "", position = 8, section = inventory)
    default CombatPotion combatPot() {
        return CombatPotion.SUPER_COMBAT;
    }

    @Range(min = 50, max = 120)
    @ConfigItem(keyName = "boostLevel", name = "Re-boost at", description = "The level to drink re-pot at - checks strength level", position = 9, section = inventory)
    default int boostLevel() {
        return 105;
    }

    @ConfigItem(keyName = "teleport", name = "Teleport", description = "", position = 7, section = inventory)
    default Teleport teleport() {
        return Teleport.HOUSE_TELEPORT;
    }



    /* END OF MAIN CONFIG */

    @ConfigSection(
            name = "Sleep Delays",
            description = "",
            position = 5,
            keyName = "sleepDelays",
            closedByDefault = true
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
            position = 6,
            keyName = "tickDelays",
            closedByDefault = true
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

    enum Boss {
        SARACHNIS,
        GIANT_MOLE;
    }

    enum OffensivePrayer {
        NONE(null),
        CHIVALRY(Prayer.CHIVALRY),
        PIETY(Prayer.PIETY),
        RIGOUR(Prayer.RIGOUR),
        AUGURY(Prayer.AUGURY);

        @Getter
        final Prayer pray;

        OffensivePrayer(Prayer pray) {
            this.pray = pray;
        }
    }

    enum Teleport {
        CONSTRUCTION_CAPE(Set.of(ItemID.CONSTRUCT_CAPE)),
        CONSTRUCTION_CAPE_T(Set.of(ItemID.CONSTRUCT_CAPET)),
        HOUSE_TELEPORT(Set.of(ItemID.RUNE_POUCH, ItemID.RUNE_POUCH_L)),
        HOUSE_TELETAB(Set.of(ItemID.TELEPORT_TO_HOUSE));

        @Getter
        final Set<Integer> items;

        Teleport(Set<Integer> items) {
            this.items = items;
        }
    }

    enum CombatPotion {
        DIVINE_SUPER_COMBAT(Set.of(ItemID.DIVINE_SUPER_COMBAT_POTION1, ItemID.DIVINE_SUPER_COMBAT_POTION2,
                ItemID.DIVINE_SUPER_COMBAT_POTION3, ItemID.DIVINE_SUPER_COMBAT_POTION4)),
        SUPER_COMBAT(Set.of(ItemID.SUPER_COMBAT_POTION1, ItemID.SUPER_COMBAT_POTION2,
                ItemID.SUPER_COMBAT_POTION3, ItemID.SUPER_COMBAT_POTION4)),
        DIVINE_BASTION(Set.of(ItemID.DIVINE_BASTION_POTION1, ItemID.DIVINE_BASTION_POTION2,
                ItemID.DIVINE_BASTION_POTION3, ItemID.DIVINE_BASTION_POTION4)),
        BASTION(Set.of(ItemID.BASTION_POTION1, ItemID.BASTION_POTION2,
                ItemID.BASTION_POTION3, ItemID.BASTION_POTION4)),
        DIVINE_RANGING(Set.of(ItemID.DIVINE_RANGING_POTION1, ItemID.DIVINE_RANGING_POTION2,
                ItemID.DIVINE_RANGING_POTION3, ItemID.DIVINE_RANGING_POTION4)),
        RANGING(Set.of(ItemID.RANGING_POTION1, ItemID.RANGING_POTION2,
                ItemID.RANGING_POTION3, ItemID.RANGING_POTION4));

        @Getter
        private final Set<Integer> pots;

        CombatPotion(Set<Integer> pots) {
            this.pots = pots;
        }
    }

}