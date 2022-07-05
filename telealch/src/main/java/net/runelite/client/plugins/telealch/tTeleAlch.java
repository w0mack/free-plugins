package net.runelite.client.plugins.telealch;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.iutils.*;
import net.runelite.client.plugins.iutils.game.Game;
import net.runelite.client.plugins.iutils.scripts.ReflectBreakHandler;
import net.runelite.client.plugins.iutils.ui.Chatbox;
import net.runelite.client.plugins.iutils.ui.Equipment;
import net.runelite.client.plugins.iutils.util.LegacyInventoryAssistant;
import net.runelite.client.ui.overlay.OverlayManager;
import org.pf4j.Extension;

import javax.inject.Inject;
import java.awt.*;
import java.io.IOException;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.ExecutorService;

@Extension
@PluginDependency(iUtils.class)
@PluginDescriptor(
		name = "tTeleAlch",
		description = "Tele-alch ur shit bish",
		enabledByDefault = false,
		tags = {"tea", "tele", "alch"}
)
@Slf4j
public class tTeleAlch extends Plugin
{
	@Inject
	private OverlayManager overlayManager;
	@Inject
	private Client client;
	@Inject
	private Game game;
	@Inject
	private ClientThread clientThread;
	@Inject
	private iUtils utils;
	@Inject
	private WalkUtils walk;
	@Inject
	private InventoryUtils inv;
	@Inject
	private Equipment equip;
	@Inject
	private ObjectUtils objectUtils;
	@Inject
	private CalculationUtils calc;
	@Inject
	private NPCUtils npcs;
	@Inject
	private BankUtils bank;
	@Inject
	private Chatbox chat;
	@Inject
	private PrayerUtils pray;
	@Inject
	private KeyboardUtils keyb;
	@Inject
	private LegacyInventoryAssistant inventoryAssistant;
	@Inject
	private ExecutorService executorService;
	@Inject
	private MenuUtils menu;
	@Inject
	public MouseUtils mouse;
	@Inject
	private ReflectBreakHandler chinBreakHandler;

	@Inject
	TeleAlchOverlay overlay;
	@Inject
	TeleAlchConfig config;

	private Player player;
	private Rectangle bounds;
	LegacyMenuEntry targetMenu;
	TeleAlchState state;
	TeleAlchState lastState;
	boolean startPlugin;
	Instant botTimer;
	int timeout;
	int teleTimeout;
	int alchTimeout;
	int tick;
	private long sleepLength;

	public tTeleAlch() {
		botTimer = null;
		startPlugin = false;
		state = TeleAlchState.TIMEOUT;
		lastState = state;
	}

	@Provides
	TeleAlchConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(TeleAlchConfig.class);
	}


	private void reset() {
		timeout = 0;
		startPlugin = false;
		botTimer = null;
		state = TeleAlchState.TIMEOUT;
		lastState = state;
		overlayManager.remove(overlay);
		chinBreakHandler.stopPlugin(this);
	}

	@Override
	protected void startUp() {
		chinBreakHandler.registerPlugin(this);
	}

	@Override
	protected void shutDown() {
		reset();
		chinBreakHandler.unregisterPlugin(this);
	}

	@Subscribe
	private void onConfigButtonPressed(ConfigButtonClicked configButtonClicked) throws IOException, InterruptedException {
		if (!configButtonClicked.getGroup().equalsIgnoreCase("TeleAlchConfig")) {
			return;
		}
		switch (configButtonClicked.getKey()) {
			case "startPlugin":
				if (!startPlugin) {
					player = client.getLocalPlayer();
					if (player != null && client != null) {
						startPlugin = true;
						botTimer = Instant.now();
						state = TeleAlchState.TIMEOUT;
						overlayManager.add(overlay);
						timeout = 1;
						alchTimeout = 1;
						teleTimeout = 1;
						tick = 0;
						chinBreakHandler.startPlugin(this);
					}
				} else {
					reset();
				}
				break;
		}
	}

	@Subscribe
	private void onChatMessage(ChatMessage event) {
		if (!startPlugin)
			return;
		if (event.getType() == ChatMessageType.CONSOLE)
			return;
	}

	@Subscribe
	private void onGameTick(GameTick event)
	{
		if (!startPlugin || chinBreakHandler.isBreakActive(this)) {
			return;
		}
		player = client.getLocalPlayer();
		if (player != null && client != null) {
			state = getStates();
			if (state != TeleAlchState.TIMEOUT)
				lastState = state;
			if (alchTimeout > 0)
				alchTimeout--;
			if (teleTimeout > 0)
				teleTimeout--;
			tick++;
			switch (state) {
				case TIMEOUT:
					if (timeout <= 0)
						timeout = 0;
					else
						timeout--;
					break;
				case ALCH:
					castAlch(config.alch());
					alchTimeout = 5;
					break;
				case TELEPORT:
					teleport(config.teleport());
					teleTimeout = 4;
					break;
				case HANDLE_BREAK:
					chinBreakHandler.startBreak(this);
					timeout = tickDelay();
					break;
			}
		}
	}

	private boolean hasRune(int it) {
		if (it == ItemID.FIRE_RUNE) {
			if (equip.isEquipped(ItemID.TOME_OF_FIRE) || equip.isEquipped(ItemID.STAFF_OF_FIRE) || (inv.getItemCount(it, true) >= 5))
				return true;
		}
		if (it == ItemID.AIR_RUNE) {
			if (equip.isEquipped(ItemID.STAFF_OF_AIR) || (inv.getItemCount(it, true) >= 5))
				return true;
		}
		if (it == ItemID.EARTH_RUNE) {
			if (equip.isEquipped(ItemID.STAFF_OF_EARTH) || (inv.getItemCount(it, true) >= 5))
				return true;
		}
		if (it == ItemID.WATER_RUNE) {
			if (equip.isEquipped(ItemID.STAFF_OF_WATER) || (inv.getItemCount(it, true) >= 5))
				return true;
		}
		utils.sendGameMessage("Missing item: " + client.getItemDefinition(it).getName());
		return false;
	}

	private void castAlch(int itemID) {
		if (!hasRune(ItemID.FIRE_RUNE) || hasRune(ItemID.NATURE_RUNE)) {
			reset();
			return;
		}
		WidgetItem alchItem = inv.getWidgetItem(itemID);
		if (alchItem == null) {
			utils.sendGameMessage("Missing item: " + client.getItemDefinition(alchItem.getId()).getName());
			return;
		}
		boolean high = client.getBoostedSkillLevel(Skill.MAGIC) >= 55;
		log.debug("Alching item: {}", alchItem.getId());
		targetMenu = new LegacyMenuEntry("Cast", (high ? "High" : "Low") + " Level Alchemy -> Item",
				0,
				MenuAction.WIDGET_TARGET_ON_WIDGET.getId(),
				alchItem.getIndex(), WidgetInfo.INVENTORY.getId(),
				false);
		utils.oneClickCastSpell(high ? WidgetInfo.SPELL_HIGH_LEVEL_ALCHEMY : WidgetInfo.SPELL_LOW_LEVEL_ALCHEMY, targetMenu, alchItem.getCanvasBounds().getBounds(), calc.getRandomIntBetweenRange(20, 180));
	}

	void teleport(TeleAlchConfig.Tele tele) {
		for(Item i : config.teleport().getRunes()) {
			if (!hasRune(i.getId())) {
				reset();
				return;
			}
		}
		targetMenu = new LegacyMenuEntry("", "", 1, MenuAction.CC_OP, -1, client.getWidget(config.teleport().getWidget()).getId(), false);
		utils.doActionMsTime(targetMenu, client.getWidget(config.teleport().getWidget()).getBounds(), calc.getRandomIntBetweenRange(240, 340));
	}

	@Subscribe
	private void onAnimationChanged(AnimationChanged event) {
		if (!startPlugin)
			return;
		Actor actor = event.getActor();
	}

	TeleAlchState getStates() {
		if (timeout != 0)
			return TeleAlchState.TIMEOUT;
		if (chinBreakHandler.shouldBreak(this))
			return TeleAlchState.HANDLE_BREAK;
		if (alchTimeout <= 0)
			return TeleAlchState.ALCH;
		if (teleTimeout <= 0)
			return TeleAlchState.TELEPORT;
		return TeleAlchState.TIMEOUT;
	}

	private boolean actionItem(int id, int delay, String... action) {
		if (inv.containsItem(id)) {
			WidgetItem item = inv.getWidgetItem(id);
			targetMenu = inventoryAssistant.getLegacyMenuEntry(item.getId(), action);
			utils.doActionMsTime(targetMenu, item.getCanvasBounds(), delay);
			return true;
		}
		return false;
	}

	private boolean actionItem(int id, String... action) {
		return actionItem(id, (int)sleepDelay(), action);
	}

	private boolean actionObject(int id, MenuAction action, int delay) {
		GameObject obj = objectUtils.findNearestGameObject(id);
		if (obj != null) {
			targetMenu = new LegacyMenuEntry("", "", obj.getId(), action, obj.getSceneMinLocation().getX(), obj.getSceneMinLocation().getY(), false);
			utils.doInvokeMsTime(targetMenu, delay);
			return true;
		}
		return false;
	}

	private boolean actionObject(int id, MenuAction action) {
		return actionObject(id, action, (int)sleepDelay());
	}

	private boolean actionNPC(int id, MenuAction action, int delay) {
		NPC target = npcs.findNearestNpc(id);
		if (target != null) {
			targetMenu = new LegacyMenuEntry("", "", target.getIndex(), action, target.getIndex(), 0, false);
			utils.doInvokeMsTime(targetMenu, delay);
			return true;
		}
		return false;
	}

	private boolean actionNPC(int id, MenuAction action) {
		return actionNPC(id, action, (int)sleepDelay());
	}

	private long sleepDelay() {
		sleepLength = calc.randomDelay(config.sleepWeightedDistribution(), config.sleepMin(), config.sleepMax(), config.sleepDeviation(), config.sleepTarget());
		return sleepLength;
	}

	private int tickDelay() {
		int tickLength = (int) calc.randomDelay(config.tickDelaysWeightedDistribution(), config.tickDelaysMin(), config.tickDelaysMax(), config.tickDelaysDeviation(), config.tickDelaysTarget());
		log.debug("tick delay for {} ticks", tickLength);
		return tickLength;
	}
}