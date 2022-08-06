package net.runelite.client.plugins.cssarachnishelper;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.iutils.*;
import net.runelite.client.plugins.iutils.game.Game;
import net.runelite.client.plugins.iutils.ui.Chatbox;
import net.runelite.client.plugins.iutils.ui.Equipment;
import net.runelite.client.plugins.iutils.util.LegacyInventoryAssistant;
import net.runelite.client.ui.overlay.OverlayManager;
import org.pf4j.Extension;

import javax.inject.Inject;
import java.awt.*;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.*;
import java.util.concurrent.ExecutorService;

@Extension
@PluginDependency(iUtils.class)
@PluginDescriptor(
		name = "tSarachnisHelper",
		description = "Assists you while at Sarachnis",
		enabledByDefault = false,
		tags = {"Tea", "Tea", "fw", "sarachnis", "eggs", "spider"}
)
@Slf4j
public class SarachnisHelper extends Plugin
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
	private ItemManager itemManager;
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
	private ExecutorService executorService;
	@Inject
	private LegacyInventoryAssistant inventoryAssistant;

	@Inject
	SarachnisHelperOverlay overlay;
	@Inject
	SarachnisHelperConfig config;

	private Player player;
	private Rectangle bounds;
	LegacyMenuEntry targetMenu;
	SarachnisHelperState state;
	SarachnisHelperState lastState;
	boolean startPlugin;
	Instant botTimer;
	int timeout;

	Set<Integer> PRAYER_RESTORE = Set.of(ItemID.PRAYER_POTION1, ItemID.PRAYER_POTION2, ItemID.PRAYER_POTION3, ItemID.PRAYER_POTION4,
			ItemID.SUPER_RESTORE1, ItemID.SUPER_RESTORE2, ItemID.SUPER_RESTORE3, ItemID.SUPER_RESTORE4, ItemID.BLIGHTED_SUPER_RESTORE1,
			ItemID.BLIGHTED_SUPER_RESTORE2, ItemID.BLIGHTED_SUPER_RESTORE3, ItemID.BLIGHTED_SUPER_RESTORE4, ItemID.EGNIOL_POTION_1,
			ItemID.EGNIOL_POTION_2, ItemID.EGNIOL_POTION_3, ItemID.EGNIOL_POTION_4);

	NPC bossNPC;
	boolean attack;

	boolean atSarachnis = false;

	public SarachnisHelper() {
		botTimer = null;
		startPlugin = false;
		state = SarachnisHelperState.TIMEOUT;
		lastState = state;
	}

	@Provides
	SarachnisHelperConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(SarachnisHelperConfig.class);
	}


	private void reset() {
		timeout = 0;
		startPlugin = false;
		botTimer = null;
		state = SarachnisHelperState.TIMEOUT;
		lastState = state;
		overlayManager.remove(overlay);
	}

	@Override
	protected void startUp() { }

	@Override
	protected void shutDown(){ reset(); }

	@Subscribe
	private void onConfigButtonPressed(ConfigButtonClicked configButtonClicked) throws IOException, InterruptedException {
		if (!configButtonClicked.getGroup().equalsIgnoreCase("SarachnisHelper")) {
			return;
		}
		switch (configButtonClicked.getKey()) {
			case "startPlugin":
				if (!startPlugin) {
					player = client.getLocalPlayer();
					if (player != null && client != null) {
						startPlugin = true;
						botTimer = Instant.now();
						state = SarachnisHelperState.TIMEOUT;
						overlayManager.add(overlay);
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
		if (!startPlugin)
			return;
		player = client.getLocalPlayer();
		if (player != null && client != null) {
			state = getState();
			if (config.debug() && state != lastState && state != SarachnisHelperState.TIMEOUT) {
				utils.sendGameMessage(this.getClass().getSimpleName() + ": " + state.toString());
			}
			if (state != SarachnisHelperState.TIMEOUT)
				lastState = state;
			WidgetItem item;
			if (bossNPC == null) {
				bossNPC = npcs.findNearestNpc(NpcID.SARACHNIS);
			}
			atSarachnis = player.getWorldLocation().getY() < 9912 && player.getWorldLocation().getX() >= 1832;
			switch (state) {
				case TIMEOUT:
					if (timeout <= 0)
						timeout = 0;
					else
						timeout--;
					break;
				case PROTECT_FROM_MISSILES:
					toggle(Prayer.PROTECT_FROM_MISSILES, sleepDelay());
					break;
				case PROTECT_FROM_MELEE:
					toggle(Prayer.PROTECT_FROM_MELEE, sleepDelay());
					break;
				case PROTECT_FROM_MAGIC:
					toggle(Prayer.PROTECT_FROM_MAGIC, sleepDelay());
					break;
				case TOGGLE_PIETY:
				case TOGGLE_CHIVALRY:
				case TOGGLE_RIGOUR:
				case TOGGLE_AUGURY:
					Prayer offensive = (state == SarachnisHelperState.TOGGLE_PIETY ? Prayer.PIETY :
							(state == SarachnisHelperState.TOGGLE_CHIVALRY ? Prayer.CHIVALRY :
									(state == SarachnisHelperState.TOGGLE_RIGOUR ? Prayer.RIGOUR : Prayer.AUGURY)));
					toggle(offensive, sleepDelay());
					break;
				case ATTACK_BOSS:
					actionNPC(bossNPC.getId(), MenuAction.NPC_SECOND_OPTION);
					break;
				case EAT_FOOD:
					eatFood();
					break;
				case RESTORE_PRAYER:
					drinkPrayer();
					break;
				case DRINK_COMBAT:
					drinkCombatPot();
					break;
				case TELEPORT:
					if (config.teleport().getItems().contains(ItemID.CONSTRUCT_CAPET) || config.teleport().getItems().contains(ItemID.CONSTRUCT_CAPE))
						useItem(ItemID.CONSTRUCT_CAPET, "tele to poh");
					else if (config.teleport().getItems().contains(ItemID.TELEPORT_TO_HOUSE))
						useItem(ItemID.TELEPORT_TO_HOUSE, "break");
					else if (config.teleport().getItems().contains(ItemID.RUNE_POUCH) || config.teleport().getItems().contains(ItemID.RUNE_POUCH_L)) {
						Widget widget = client.getWidget(218, 29);
						if (widget != null) {
							targetMenu = new LegacyMenuEntry("Cast", "<col=00ff00>Teleport to House</col>", 1 , MenuAction.CC_OP, -1, widget.getId(), false);
							utils.doActionMsTime(targetMenu, widget.getBounds(), (int)sleepDelay());
						}
					}
					break;
				default:
					timeout = 1;
					break;
			}
		}
	}

	@Subscribe
	private void onNpcSpawned(NpcSpawned event) {
		if (!startPlugin)
			return;
		final NPC npc = event.getNpc();
		if (npc.getName() == null) {
			return;
		}
		if (npc.getId() == NpcID.SARACHNIS) {
			bossNPC = npc;
		}
	}

	@Subscribe
	private void onNpcDespawned(NpcDespawned event) {
		if (!startPlugin)
			return;
		final NPC npc = event.getNpc();
		if (npc.getName() == null) {
			return;
		}
		if (npc.getId() == NpcID.SARACHNIS) {
			bossNPC = null;
		}
	}

	@Subscribe
	private void onAnimationChanged(AnimationChanged event) {
		if (!startPlugin)
			return;
		Actor actor = event.getActor();
	}

	SarachnisHelperState getState() {
		if (bossNPC != null && atSarachnis) {
			if (client.getBoostedSkillLevel(Skill.PRAYER) >= 1) {
				if (!inMeleeDistance(bossNPC.getId(), client)) {
					if (!pray.isActive(Prayer.PROTECT_FROM_MISSILES))
						return SarachnisHelperState.PROTECT_FROM_MISSILES;
				} else {
					if (!pray.isActive(Prayer.PROTECT_FROM_MELEE))
						return SarachnisHelperState.PROTECT_FROM_MELEE;
				}
				if (config.offensivePray() != SarachnisHelperConfig.OffensivePrayer.NONE && !pray.isActive(config.offensivePray().getPray())) {
					if (config.offensivePray().getPray() == Prayer.PIETY)
						return SarachnisHelperState.TOGGLE_PIETY;
					if (config.offensivePray().getPray() == Prayer.CHIVALRY)
						return SarachnisHelperState.TOGGLE_CHIVALRY;
					if (config.offensivePray().getPray() == Prayer.AUGURY)
						return SarachnisHelperState.TOGGLE_AUGURY;
					if (config.offensivePray().getPray() == Prayer.RIGOUR)
						return SarachnisHelperState.TOGGLE_RIGOUR;
				}
			}
			if (config.eatFood() && (client.getBoostedSkillLevel(Skill.HITPOINTS) <= config.eatAt())) {
				if (hasFood())
					return SarachnisHelperState.EAT_FOOD;
				else
					return SarachnisHelperState.TELEPORT;
			}
			if (config.drinkPray() && (client.getBoostedSkillLevel(Skill.PRAYER) <= config.restoreAt())) {
				if (hasPrayer())
					return SarachnisHelperState.RESTORE_PRAYER;
				else
					return SarachnisHelperState.TELEPORT;
			}
			if (config.drinkCombatBoost()
					&& (client.getBoostedSkillLevel(
							config.combatPot() == SarachnisHelperConfig.CombatPotion.SUPER_COMBAT ? Skill.STRENGTH :
									(config.combatPot() == SarachnisHelperConfig.CombatPotion.DIVINE_SUPER_COMBAT ? Skill.STRENGTH :
											Skill.RANGED)) <= config.boostLevel())) {
				if (hasCombatPot())
					return SarachnisHelperState.DRINK_COMBAT;
				else
					return SarachnisHelperState.TELEPORT;
			}
			if (bossNPC != null && player.getInteracting() != bossNPC && config.attackBoss())
				return SarachnisHelperState.ATTACK_BOSS;
		}
		if (bossNPC == null) {
			if (pray.isActive(Prayer.PROTECT_FROM_MELEE))
				return SarachnisHelperState.PROTECT_FROM_MELEE;
			if (pray.isActive(Prayer.PROTECT_FROM_MISSILES))
				return SarachnisHelperState.PROTECT_FROM_MISSILES;
			if (pray.isActive(Prayer.PROTECT_FROM_MAGIC))
				return SarachnisHelperState.PROTECT_FROM_MAGIC;
			if (pray.isActive(Prayer.PIETY))
				return SarachnisHelperState.TOGGLE_PIETY;
			if (pray.isActive(Prayer.CHIVALRY))
				return SarachnisHelperState.TOGGLE_CHIVALRY;
			if (pray.isActive(Prayer.AUGURY))
				return SarachnisHelperState.TOGGLE_AUGURY;
			if (pray.isActive(Prayer.RIGOUR))
				return SarachnisHelperState.TOGGLE_RIGOUR;
		}
		return SarachnisHelperState.TIMEOUT;
	}

	void toggle(Prayer p) {
		toggle(p, sleepDelay());
	}

	void toggle(Prayer p, long sleepDelay) {
		Widget widget = client.getWidget(p.getWidgetInfo());
		targetMenu = new LegacyMenuEntry("", "", 1, MenuAction.CC_OP.getId(), -1, widget.getId(), false);
		if (!config.invokes()) {
			utils.doActionMsTime(targetMenu, widget.getBounds(), sleepDelay);
		} else {
			utils.doInvokeMsTime(targetMenu, sleepDelay);
		}
	}
	boolean hasCombatPot() {
		WidgetItem pot = inv.getWidgetItem(config.combatPot().getPots());
		return pot != null;
	}

	void drinkCombatPot() {
		WidgetItem pot = inv.getWidgetItem(config.combatPot().getPots());
		if (pot != null) {
			useItem(pot, "drink");
		}
	}

	boolean hasPrayer() {
		WidgetItem pray = inv.getWidgetItem(PRAYER_RESTORE);
		return pray != null;
	}

	void drinkPrayer() {
		WidgetItem pray = inv.getWidgetItem(PRAYER_RESTORE);
		if (pray != null) {
			useItem(pray, "drink");
		}
	}

	boolean hasFood() {
		WidgetItem eat = inv.getItemMenu(itemManager, "Eat", 33,
				Set.of(ItemID.DWARVEN_ROCK_CAKE, ItemID.DWARVEN_ROCK_CAKE_7510));
		return eat != null;
	}

	void eatFood() {
		WidgetItem food = inv.getItemMenu(itemManager, "Eat", 33,
				Set.of(ItemID.DWARVEN_ROCK_CAKE, ItemID.DWARVEN_ROCK_CAKE_7510));
		if (food != null) {
			useItem(food, "eat");
		}
	}

	boolean inMeleeDistance(int id, Client client) {
		player = client.getLocalPlayer();
		if (player != null) {
			NPC target = npcs.findNearestNpc(id);
			if (target != null) {
				return player.getWorldLocation().distanceTo(
						new WorldPoint(target.getWorldLocation().getX() + 2, target.getWorldLocation().getY() + 2, 0)) <= 3;
			}
		}
		return false;
	}

	boolean inRegion(Client client, List<Integer> region) {
		return Arrays.stream(client.getMapRegions()).anyMatch(region::contains);
	}

	private TileItem getNearestTileItem(List<TileItem> tileItems) {
		int currentDistance;
		TileItem closestTileItem = tileItems.get(0);
		int closestDistance = closestTileItem.getTile().getWorldLocation().distanceTo(player.getWorldLocation());
		for (TileItem tileItem : tileItems) {
			currentDistance = tileItem.getTile().getWorldLocation().distanceTo(player.getWorldLocation());
			if (currentDistance < closestDistance) {
				closestTileItem = tileItem;
				closestDistance = currentDistance;
			}
		}
		return closestTileItem;
	}

	private boolean actionObject(int id, MenuAction action, int delay) {
		GameObject obj = objectUtils.findNearestGameObject(id);
		if (obj != null) {
			targetMenu = new LegacyMenuEntry("", "", obj.getId(), action, obj.getSceneMinLocation().getX(), obj.getSceneMinLocation().getY(), false);
			if (!config.invokes())
				utils.doGameObjectActionMsTime(obj, action.getId(), delay);
			else
				utils.doInvokeMsTime(targetMenu, delay);
			return true;
		}
		return false;
	}

	private boolean actionObject(int id, MenuAction action) {
		return actionObject(id, action, (int)sleepDelay());
	}

	private void useItem(WidgetItem item, String... actions) {
		if (item != null) {
			targetMenu = inventoryAssistant.getLegacyMenuEntry(item.getId(), actions);
			int sleepTime = calc.getRandomIntBetweenRange(25, 200);
			utils.doActionMsTime(targetMenu, item.getCanvasBounds(), sleepTime);
		}
	}

	private void useItem(int id, String... actions) {
		WidgetItem item = inv.getWidgetItem(id);
		if (item != null) {
			useItem(item, actions);
		}
	}

	private boolean actionNPC(int id, MenuAction action, int delay) {
		NPC target = npcs.findNearestNpc(id);
		if (target != null) {
			targetMenu = new LegacyMenuEntry("", "", target.getIndex(), action, target.getIndex(), 0, false);
			if (!config.invokes())
				utils.doNpcActionMsTime(target, action.getId(), delay);
			else
				utils.doInvokeMsTime(targetMenu, delay);
			return true;
		}
		return false;
	}

	private boolean actionNPC(int id, MenuAction action) {
		return actionNPC(id, action, (int)sleepDelay());
	}

	private long sleepDelay() {
		long sleepLength = calc.randomDelay(config.sleepWeightedDistribution(), config.sleepMin(), config.sleepMax(), config.sleepDeviation(), config.sleepTarget());
		return sleepLength;
	}

	private int tickDelay() {
		int tickLength = (int) calc.randomDelay(config.tickDelaysWeightedDistribution(), config.tickDelaysMin(), config.tickDelaysMax(), config.tickDelaysDeviation(), config.tickDelaysTarget());
		log.debug("tick delay for {} ticks", tickLength);
		return tickLength;
	}
}