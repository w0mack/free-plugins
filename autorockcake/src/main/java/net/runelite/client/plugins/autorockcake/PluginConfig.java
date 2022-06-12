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
package net.runelite.client.plugins.autorockcake;

import lombok.Getter;
import net.runelite.api.Prayer;
import net.runelite.client.config.*;

@ConfigGroup("AutoRockCake")
public interface PluginConfig extends Config
{

	@ConfigItem(keyName = "startPlugin", name = "Start/Stop", description = "", position = 0, title = "startPlugin")
	default Button startPlugin() {
		return new Button();
	}

	@ConfigItem(
		keyName = "showOverlay",
		name = "Show UI",
		description = "Show the UI on screen",
		position = 1
	)
	default boolean showOverlay() {
		return true;
	}
	@ConfigItem(keyName = "onlyNMZ", name = "In NMZ Only", description = "", position = 3)
	default boolean onlyNMZ() {
		return true;
	}
	@ConfigItem(keyName = "lowerHP", name = "Lower HP to 1", description = "", position = 4)
	default boolean lowerHP() {
		return true;
	}
	@ConfigItem(keyName = "drinkOvl", name = "Drink Overload", description = "Drink overload", position = 5)
	default boolean drinkOvl() {
		return true;
	}
	@ConfigItem(keyName = "drinkAbs", name = "Drink Absorption", description = "Drink Absorption", position = 6)
	default boolean drinkAbs() {
		return true;
	}
	@ConfigItem(keyName = "prayMethod", name = "Method", description = "", position = 7)
	default PrayType prayMethod() { return PrayType.QUICK_PRAYERS; }
	@ConfigItem(keyName = "prayer", name = "", description = "", position = 8)
	default OffensivePray prayer() { return OffensivePray.PIETY; }


	enum PrayType {
		NONE,
		QUICK_PRAYERS,
		CUSTOM
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

	@ConfigItem(
		keyName = "debug",
		name = "Debug Messages",
		description = "",
		position = 999,
		hidden = true
	)
	default boolean debug() {
		return false;
	}
}