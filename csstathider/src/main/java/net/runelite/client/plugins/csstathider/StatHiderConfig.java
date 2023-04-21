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
package net.runelite.client.plugins.csstathider;

import net.runelite.client.config.*;

@ConfigGroup("CSStatHider")
public interface StatHiderConfig extends Config {

    @ConfigItem(
            keyName = "setSkills",
            name = "Set Skills Tab",
            description = "",
            position = 0
    )
    default boolean setSkills() {
        return true;
    }

    @ConfigItem(
            keyName = "level",
            name = "Skills",
            description = "",
            position = 1,
            hidden = true,
            unhide = "setSkills"
    )
    default int level() {
        return 99;
    }

    @ConfigItem(
            keyName = "setHP",
            name = "Set HP",
            description = "",
            position = 2
    )
    default boolean setHP() {
        return true;
    }

    @ConfigItem(
            keyName = "hpLevel",
            name = "HP",
            description = "",
            position = 3,
            hidden = true,
            unhide = "setHP"
    )
    default int hpLevel() {
        return 99;
    }

    @ConfigItem(
            keyName = "setPray",
            name = "Set Prayer",
            description = "",
            position = 4
    )
    default boolean setPray() {
        return true;
    }

    @ConfigItem(
            keyName = "prayLevel",
            name = "Prayer",
            description = "",
            position = 5,
            hidden = true,
            unhide = "setPray"
    )
    default int prayLevel() {
        return 99;
    }

    @ConfigItem(
            keyName = "setRun",
            name = "Set Run Energy",
            description = "",
            position = 6
    )
    default boolean setRun() {
        return true;
    }

    @ConfigItem(
            keyName = "runLevel",
            name = "Run Energy",
            description = "",
            position = 7,
            hidden = true,
            unhide = "setRun"
    )
    default int runLevel() {
        return 100;
    }

    @ConfigItem(
            keyName = "setSpec",
            name = "Set Spec Energy",
            description = "",
            position = 8
    )
    default boolean setSpec() {
        return true;
    }

    @ConfigItem(
            keyName = "specLevel",
            name = "Spec Energy",
            description = "",
            position = 9,
            hidden = true,
            unhide = "setSpec"
    )
    default int specLevel() {
        return 100;
    }

}