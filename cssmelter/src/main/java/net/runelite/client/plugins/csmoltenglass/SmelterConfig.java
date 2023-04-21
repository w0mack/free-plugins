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
package net.runelite.client.plugins.csmoltenglass;

import lombok.Getter;
import net.runelite.client.config.*;

@ConfigGroup("CSSmelter")
public interface SmelterConfig extends Config {

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

    // Put your config here

    @ConfigSection(
            name = "Options",
            description = "",
            position = 10,
            keyName = "options"
    )
    String options = "Options";

    @ConfigItem(
            keyName = "furnace",
            name = "Furnace",
            description = "",
            position = 1,
            section = options
    )
    default Furnace furnace() {
        return Furnace.EDGEVILLE;
    }

    @ConfigItem(
            keyName = "method",
            name = "Method",
            description = "",
            position = 1,
            section = options
    )
    default Method method() {
        return Method.GOLD;
    }

    @ConfigItem(
            keyName = "invokes",
            name = "Use Invokes",
            description = "Use with caution",
            position = 998
    )
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

    enum Furnace {
        EDGEVILLE(16469, 10355),
        MT_KARUULM(34342, 34343),
        PT_PHASMATYS(24009, 16642);

        @Getter
        private final int furnaceID, bankID;

        Furnace(int furnaceID, int bankID) {
            this.furnaceID = furnaceID;
            this.bankID = bankID;
        }
    }

    enum Method {
        GOLD,
        MOLTEN_GLASS;

        Method() {

        }
    }
}