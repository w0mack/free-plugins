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
package net.runelite.client.plugins.automedclue;

import lombok.Getter;
import net.runelite.api.ItemID;
import net.runelite.client.config.*;

@ConfigGroup("AutoMedClue")
public interface AutoClueConfig extends Config {

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
            name = "Inventory",
            description = "",
            position = 3,
            keyName = "inventory"
    )
    String inventory = "Inventory";

    @ConfigItem(
            keyName = "runepouch",
            name = "Use Rune pouch",
            description = "Use a rune pouch with LAW, LAVA and MIST runes",
            position = 1,
            section = inventory
    )
    default boolean useRunePouch() {
        return false;
    }

    @ConfigItem(
            keyName = "combirunes",
            name = "Use lava and mist runes",
            description = "Use lava and mist runes for elemental",
            position = 2,
            section = inventory,
            hide = "runepouch"
    )
    default boolean useCombinationRunes() {
        return false;
    }

    @ConfigItem(
            keyName = "weapon",
            name = "Weapon ID",
            description = "",
            position = 3,
            section = inventory
    )
    default int weaponID() {
        return ItemID.ADAMANT_SCIMITAR;
    }

    @ConfigItem(
            keyName = "useFood",
            name = "Bring food",
            description = "",
            position = 4,
            section = inventory
    )
    default boolean useFood() {
        return false;
    }

    @ConfigItem(
            keyName = "food",
            name = "Food",
            description = "",
            position = 5,
            section = inventory,
            hidden = true,
            unhide = "useFood"
    )
    default Food food() {
        return Food.MONKFISH;
    }

    @Range(min = 5, max = 99)
    @ConfigItem(
            keyName = "eatAt",
            name = "HP threshold",
            description = "",
            position = 6,
            section = inventory,
            hidden = true,
            unhide = "useFood"
    )
    default int eatAt() {
        return 22;
    }

    @ConfigItem(keyName = "invokes", name = "Use invokes (use with caution)", description = "Potentially detected; use with caution", position = 998)
    default boolean invokes() {
        return false;
    }

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

    @ConfigItem(
            keyName = "debug",
            name = "Debug Messages",
            description = "",
            position = 999
    )
    default boolean debug() {
        return false;
    }

    enum Food {
        MANTA_RAY(ItemID.MANTA_RAY),
        TUNA_POTATO(ItemID.TUNA_POTATO),
        DARK_CRAB(ItemID.DARK_CRAB),
        ANGLERFISH(ItemID.ANGLERFISH),
        SEA_TURTLE(ItemID.SEA_TURTLE),
        MUSHROOM_POTATO(ItemID.MUSHROOM_POTATO),
        SHARK(ItemID.SHARK),
        COOKED_KARAMBWAN(ItemID.COOKED_KARAMBWAN),
        MONKFISH(ItemID.MONKFISH),
        LOBSTER(ItemID.LOBSTER);
        @Getter
        private final int food;

        Food(int food) {
            this.food = food;
        }
    }

}