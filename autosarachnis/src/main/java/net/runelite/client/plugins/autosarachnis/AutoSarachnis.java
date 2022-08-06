package net.runelite.client.plugins.autosarachnis;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
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
import net.runelite.client.plugins.tutils.*;
import net.runelite.client.plugins.tutils.auth.AntiTamper;
import net.runelite.client.plugins.tutils.auth.AuthUtil;
import net.runelite.client.plugins.tutils.game.Game;
import net.runelite.client.plugins.tutils.ui.Chatbox;
import net.runelite.client.plugins.tutils.ui.Equipment;
import net.runelite.client.plugins.tutils.util.LegacyInventoryAssistant;
import net.runelite.client.ui.overlay.OverlayManager;
import org.pf4j.Extension;

import javax.inject.Inject;
import java.awt.*;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.stream.IntStream;

@Extension
@PluginDependency(tUtils.class)
@PluginDescriptor(
		name = "CS-Sarachnis",
		description = "Squishes the spider with a newspaper",
		enabledByDefault = false,
		tags = {"Tea", "fw", "spider", "sarachnis"}
)
@Slf4j
public class AutoSarachnis extends Plugin
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
	private tUtils utils;
	@Inject
	private WalkUtils walk;
	@Inject
	private InventoryUtils inv;
	@Inject
	private PlayerUtils playerUtils;
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
	ASO overlay;
	@Inject
	private ASC config;

	private Player player;
	private Rectangle bounds;
	LegacyMenuEntry targetMenu;
	ASS state;
	ASS lastState;
	boolean startPlugin;
	Instant botTimer;
	int timeout;

	WorldPoint targetLoc;

	boolean deposited;
	boolean withdrawn;
	boolean inInstance;
	WorldArea xericsGlade;
	WorldArea hosidiusBank;
	WorldPoint walkToDungeon1;
	WorldPoint walkToDungeon2;
	WorldPoint walkToDungeon3;
	WorldPoint firstWeb;
	WorldPoint secondWeb;
	WorldPoint thirdWeb;
	WorldPoint bossWeb;
	WorldPoint bottomStairs;
	WorldArea instance;
	WorldPoint travelBank;

	Set<Integer> web1;
	Set<Integer> web2;
	Set<Integer> web3;

	static List<Integer> regions;
	List<Integer> dungeon;

	NPC sarachnis;
	int kills;
	List<TileItem> toLoot = new ArrayList<>();
	List<String> includedItems = new ArrayList<>();
	List<String> excludedItems = new ArrayList<>();
	String[] included;
	String[] excluded;
	boolean prayRange;
	boolean attack;
	int lootValue;
	boolean looted;

	int startCooldown;

	public AutoSarachnis() {
		botTimer = null;
		startPlugin = false;
		state = ASS.TIMEOUT;
		lastState = state;
	}

	@Provides
	ASC provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ASC.class);
	}


	private void reset() {
		timeout = 0;
		startPlugin = false;
		botTimer = null;
		state = ASS.TIMEOUT;
		lastState = state;
		overlayManager.remove(overlay);
	}

	@Override
	protected void startUp() {
	}

	@Override
	protected void shutDown(){ reset(); }

	@Subscribe
	private void onConfigButtonPressed(ConfigButtonClicked configButtonClicked) throws IOException, InterruptedException {
		if (!configButtonClicked.getGroup().equalsIgnoreCase("AutoSarachnis")) {
			return;
		}
		switch (configButtonClicked.getKey()) {
			case "startPlugin":
				if (!startPlugin) {
					player = client.getLocalPlayer();
					if (player != null && client != null) {
						startPlugin = true;
						botTimer = Instant.now();
						state = ASS.TIMEOUT;
						overlayManager.add(overlay);
						withdrawn = false;
						deposited = false;
						xericsGlade = new WorldArea(new WorldPoint(1749, 3563, 0), new WorldPoint(1757, 3570, 0));
						hosidiusBank = new WorldArea(new WorldPoint(1748, 3598, 0), new WorldPoint(1750, 3600, 0));
						walkToDungeon1 = new WorldPoint(1732, 3591, 0);
						walkToDungeon2 = new WorldPoint(1726, 3586, 0);
						walkToDungeon3 = new WorldPoint(1705, 3582, 0);
						firstWeb = new WorldPoint(1833, 9944, 0); // 733 // 734 open
						secondWeb = new WorldPoint(1841, 9934, 0); // 733
						thirdWeb = new WorldPoint(1847, 9919, 0); // 34898
						bossWeb = new WorldPoint(1842, 9912, 0); // 34858
						bottomStairs = new WorldPoint(1830, 9973, 0);
						travelBank = new WorldPoint(1757, 3582, 0);
						web1 = Set.of(733, 734);
						web2 = Set.of(34898, 34899); // close and open webs
						web3 = Set.of(34858, -1); // close and open webs
						dungeon = Arrays.asList(7323, 7322);
						instance = new WorldArea(new WorldPoint(1833, 9892, 0), new WorldPoint(1850, 9912, 0));
						inInstance = false;
						prayRange = false;
						attack = false;
						toLoot.clear();
						excluded = config.excludedItems().toLowerCase().split("\\s*,\\s*");
						excludedItems.clear();
						included = config.includedItems().toLowerCase().split("\\s*,\\s*");
						includedItems.clear();
						excludedItems.addAll(Arrays.asList(excluded));
						includedItems.addAll(Arrays.asList(included));
						lootValue = 0;
						looted = true;
						regions = Arrays.asList(7513, 7514, 7769, 7770, 8025, 8026);
						timeout = 3;
						kills = 0;
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
		if (event.getMessage().toLowerCase().contains("you fail to cut"))
			timeout = 0;

		String killComplete = "Your Sarachnis kill count is";

		if (event.getMessage().contains(killComplete)) {
			attack = true;
			looted = false;
			timeout = 2;
			kills++;
		}
	}

	@Subscribe
	private void onGameTick(GameTick event)
	{
		if (!startPlugin)
			return;
		player = client.getLocalPlayer();
		if (player != null && client != null) {
			state = getStates();
			if (config.debug() && state != lastState && state != ASS.TIMEOUT) {
				utils.sendGameMessage(this.getClass().getSimpleName() + ": " + state.toString());
			}
			if (state != ASS.TIMEOUT)
				lastState = state;
			looted = toLoot.isEmpty();
			inInstance = player.getWorldLocation().getY() < 9912 && player.getWorldLocation().getX() >= 1832;
			WidgetItem item;
			switch (state) {
				case TIMEOUT:
					if (timeout <= 0)
						timeout = 0;
					else
						timeout--;
					break;
				case FIND_BANK:
					openBank();
					timeout = 2 + tickDelay();
					break;
				case DEPOSIT_ALL:
					bank.depositAll();
					deposited = true;
					timeout = tickDelay();
					break;
				case WITHDRAW_FOOD:
					bank.withdrawItemAmount(config.food().getId(), config.withdrawFood());
					timeout = 3 + tickDelay();
					break;
				case WITHDRAW_MAINHAND:
					bank.withdrawItem(config.mainhand().getItemID());
					timeout = tickDelay();
					break;
				case WITHDRAW_OFFHAND:
					bank.withdrawItem(config.offhand().getItemID());
					timeout = 1 + tickDelay();
					break;
				case WITHDRAW_SPEC_WEAPON:
					bank.withdrawItem(config.specWeapon().getItemID());
					timeout = tickDelay();
					break;
				case WITHDRAW_HOUSE_TELE:
					bank.withdrawItem(config.houseTele().getId());
					timeout = tickDelay();
					break;
				case WITHDRAW_PRAYER_RESTORE:
					bank.withdrawItemAmount(config.prayer().getDose4(), config.prayerAmount());
					timeout = 1 + tickDelay();
					break;
				case WITHDRAW_COMBAT_POTION:
					bank.withdrawItem(config.combatPotion().getDose4());
					timeout = tickDelay();
					break;
				case WITHDRAW_KNIFE:
					bank.withdrawItem(ItemID.KNIFE);
					timeout = tickDelay();
					break;
				case EQUIP_MAINHAND:
				case EQUIP_OFFHAND:
					item = inv.getWidgetItem(state == ASS.EQUIP_MAINHAND ? config.mainhand().getItemID() : config.offhand().getItemID());
					if (item == null)
						break;
					targetMenu = new LegacyMenuEntry("", "", 9, MenuAction.CC_OP_LOW_PRIORITY, item.getIndex(), WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER.getId(), false);
					if (!config.invokes())
						utils.doActionMsTime(targetMenu, item.getCanvasBounds(), sleepDelay());
					else
						utils.doInvokeMsTime(targetMenu, sleepDelay());
					timeout = tickDelay();
					break;
				case FINISHED_WITHDRAWING:
					withdrawn = true;
					timeout = tickDelay();
					break;
				case DESCEND_STAIRS:
					GameObject stairs = objectUtils.findNearestGameObject(34862);
					if (stairs != null) {
						actionObject(stairs.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION, (int)sleepDelay());
					}
					timeout = 1 + tickDelay();
					break;
				case WALK_TO_DUNGEON:
					if (player.getWorldArea().intersectsWith(hosidiusBank)) {
						walk.sceneWalk(walkToDungeon1, 3, sleepDelay());
					} else if (player.getWorldLocation().distanceTo(walkToDungeon1) <= 6) {
						walk.sceneWalk(walkToDungeon3, 3, sleepDelay());
					}
					timeout = 1 + tickDelay();
					break;
				case CUT_FIRST_WEB:
					if (objectUtils.findWallObjectWithin(new WorldPoint(1833, 9944, 0), 0, 733) != null) {
						WallObject web = objectUtils.findNearestWallObject(733);
						targetMenu = new LegacyMenuEntry("", "", 733, MenuAction.GAME_OBJECT_FIRST_OPTION, web.getLocalLocation().getSceneX(), web.getLocalLocation().getSceneY(), false);
						utils.doActionMsTime(targetMenu, web.getConvexHull().getBounds(), sleepDelay());
					} else {
						walk.sceneWalk(secondWeb, 0, sleepDelay());
					}
					timeout = 2;
					break;
				case CUT_SECOND_WEB:
					if (objectUtils.findWallObjectWithin(new WorldPoint(1841, 9933, 0), 0, 733) != null) {
						WallObject web = objectUtils.findNearestWallObject(733);
						targetMenu = new LegacyMenuEntry("", "", 733, MenuAction.GAME_OBJECT_FIRST_OPTION, web.getLocalLocation().getSceneX(), web.getLocalLocation().getSceneY(), false);
						utils.doActionMsTime(targetMenu, web.getConvexHull().getBounds(), sleepDelay());
					} else {
						walk.sceneWalk(thirdWeb, 0, sleepDelay());
					}
					timeout = 2;
					break;
				case CUT_THIRD_WEB:
					if (objectUtils.findWallObjectWithin(new WorldPoint(1847, 9919, 0), 0, 34898) != null) {
						WallObject web = objectUtils.findNearestWallObject(34898); // object.getSceneMinLocation().getX()
						targetMenu = new LegacyMenuEntry("", "", 34898, MenuAction.GAME_OBJECT_FIRST_OPTION, web.getLocalLocation().getSceneX(), web.getLocalLocation().getSceneY(), false);
						utils.doActionMsTime(targetMenu, web.getConvexHull().getBounds(), sleepDelay());
					} else {
						walk.sceneWalk(thirdWeb, 0, sleepDelay());
					}
					timeout = 2;
					break;
				case WALK_TO_WEB: // now the real fun begins
					if (!player.getLocalLocation().equals(bossWeb)) {
						walk.sceneWalk(bossWeb, 0, sleepDelay());
					}
					break;
				case ENTER_BOSS_ROOM:
					WallObject web = objectUtils.findNearestWallObject(34858);
					if (web != null) {
						targetMenu = new LegacyMenuEntry("", "", 34858, MenuAction.GAME_OBJECT_SECOND_OPTION, web.getLocalLocation().getSceneX(), web.getLocalLocation().getSceneY(), false);
						utils.doActionMsTime(targetMenu, web.getConvexHull().getBounds(), sleepDelay());
					}
					timeout = 2;
					break;
				case PROTECT_FROM_MISSILES:
					pray.toggle(Prayer.PROTECT_FROM_MISSILES, sleepDelay());
					prayRange = false;
					attack = true;
					timeout = 1;
					break;
				case PROTECT_FROM_MELEE:
					pray.toggle(Prayer.PROTECT_FROM_MELEE, sleepDelay());
					prayRange = false;
					attack = true;
					break;
				case TOGGLE_PIETY:
				case TOGGLE_CHIVALRY:
					pray.toggle(state == ASS.TOGGLE_PIETY ? Prayer.PIETY : Prayer.CHIVALRY, sleepDelay());
					break;
				case EAT_FOOD:
					eatFood();
					attack = true;
					timeout = 1;
					break;
				case DRINK_COMBAT_POT:
					drinkCombatPotion();
					timeout = 1;
					attack = true;
					break;
				case RESTORE_PRAYER:
					drinkPrayer();
					timeout = 1;
					attack = true;
					break;
				case ATTACK_SARACHNIS:
					if (actionNPC(sarachnis.getId(), MenuAction.NPC_SECOND_OPTION)) {
						timeout = 0;
						attack = false;
						toLoot.clear();
					}
					break;
				case LOOT_SARACHNIS:
					if (!toLoot.isEmpty())
						lootItem(toLoot);
					attack = true;
					break;
				case TELE_TO_POH:
					teleToPOH();
					timeout = 4 + tickDelay();
					break;
				case USE_POOL:
					actionObject(config.poolID(), MenuAction.GAME_OBJECT_FIRST_OPTION);
					timeout = 3;
					break;
				case TRAVEL_BANK:
					walkTo(travelBank.getX(), travelBank.getY());
					timeout = tickDelay();
					break;
				case WALK_TO_BOOTH:
					walkTo(1748, 3598);
					timeout = tickDelay();
					break;
				case SPECIAL_ATTACK:
					Widget widget = client.getWidget(WidgetInfo.MINIMAP_SPEC_CLICKBOX);

					if (widget != null) {
						bounds = widget.getBounds();
					}
					if (equip.isEquipped(config.specWeapon().getItemID())) {
						if (client.getVar(VarPlayer.SPECIAL_ATTACK_ENABLED) == 0) {
							targetMenu = new LegacyMenuEntry("<col=ff9040>Special Attack</col>", "", 1, MenuAction.CC_OP.getId(), -1, WidgetInfo.MINIMAP_SPEC_CLICKBOX.getId(), false);
							if (config.invokes())
								utils.doInvokeMsTime(targetMenu, (int)sleepDelay());
							else
								utils.doActionMsTime(targetMenu, bounds.getBounds(), (int)sleepDelay());
						} else {
							if (actionNPC(sarachnis.getId(), MenuAction.NPC_SECOND_OPTION)) {
								timeout = 1;
								attack = false;
								toLoot.clear();
							}
						}
					}
					break;
				case EQUIP_SPEC_WEAPON:
					if (inv.containsItem(config.specWeapon().getItemID())) {
						if (config.specWeapon().is2Handed()) {
							if (inv.isFull()) {
								if (inv.containsItem(config.food().getId()))
									eatFood();
								break;
							}
						}
						actionItem(config.specWeapon().getItemID(),"wear", "equip", "wield");
						if (!config.specWeapon().is2Handed() && config.offhand() != ASC.Offhand.NONE) {
							actionItem(config.offhand().getItemID(), "wear", "equip", "wield");
						}
						attack = true;
					}
					break;
				case EQUIP_WEAPONS:
					equipWeapons();
					break;
				default:
					timeout = 1;
					break;
			}
		}
	}

	WorldPoint walkTo(int x, int y) {
		targetLoc = new WorldPoint(x, y, player.getWorldLocation().getPlane());
		return targetLoc;
	}

	@Subscribe
	private void onItemSpawned(ItemSpawned event) {
		if (!startPlugin)
			return;
		if (isLootableItem(event.getItem())) {
			toLoot.add(event.getItem());
			if (config.debug())
				utils.sendGameMessage("toLoot added: " + event.getItem().getId() + ", qty: " + event.getItem().getQuantity());
		}
	}
	@Subscribe
	private void onItemDespawned(ItemDespawned event) {
		if (!startPlugin)
			return;
		if (toLoot.remove(event.getItem())) {
			int value = utils.getItemPrice(event.getItem().getId(), true) * event.getItem().getQuantity();
			lootValue += value;
		}
		if (toLoot.isEmpty()) {
			looted = true;
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
			sarachnis = npc;
			attack = true;
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
			sarachnis = null;
		}
	}

	@Subscribe
	private void onAnimationChanged(AnimationChanged event) {
		if (!startPlugin)
			return;
		Actor actor = event.getActor();

		if (actor == sarachnis) {

		}
	}

	ASS withdrawInvent() {
		if (!equip.isEquipped(config.mainhand().getItemID())) {
			if (inv.containsItem(config.mainhand().getItemID())) {
				log.info("Equipping mainhand from bank");
				return ASS.EQUIP_MAINHAND;
			} else {
				log.info("Withdrawing mainhand");
				return ASS.WITHDRAW_MAINHAND;
			}
		}
		if (!config.mainhand().is2Handed() && config.offhand() != ASC.Offhand.NONE) {
			if (!equip.isEquipped(config.offhand().getItemID())) {
				if (inv.containsItem(config.offhand().getItemID())) {
					log.info("Equipping offhand from bank");
					return ASS.EQUIP_OFFHAND;
				} else {
					log.info("Withdrawing offhand");
					return ASS.WITHDRAW_OFFHAND;
				}
			}
		}
		if (config.specWeapon() != ASC.Special.NONE) { // if a spec weapon is selected
			if (!equip.isEquipped(config.specWeapon().getItemID()) && !inv.containsItem(config.specWeapon().getItemID())) { // if not equipped and not in invent
				log.info("Withdrawing spec weapon");
				return ASS.WITHDRAW_SPEC_WEAPON;
			}
			if (!config.specWeapon().is2Handed() && config.offhand() != ASC.Offhand.NONE) {
				if (!equip.isEquipped(config.offhand().getItemID()) && !inv.containsItem(config.offhand().getItemID())) {
					log.info("Withdrawing offhand");
					return ASS.WITHDRAW_OFFHAND;
				}
			}
		}
		if (!inv.containsItem(config.combatPotion().getDose4()) && config.combatPotion() != ASC.SuperCombat.NONE)
			return ASS.WITHDRAW_COMBAT_POTION;
		if (inv.getItemCount(config.prayer().getDose4(), false) < config.prayerAmount())
			return ASS.WITHDRAW_PRAYER_RESTORE;
		if (config.houseTele() != ASC.HouseTele.NONE && !inv.containsItem(config.houseTele().getId()))
			return ASS.WITHDRAW_HOUSE_TELE;
		if (!inv.containsItem(ItemID.KNIFE))
			return ASS.WITHDRAW_KNIFE; // should probably check if weapon has slash but im lazy rn
		if (inv.getItemCount(config.food().getId(), false) < config.withdrawFood())
			if (!inv.isFull())
				return ASS.WITHDRAW_FOOD;
		return ASS.FINISHED_WITHDRAWING;
	}

	ASS getBankState() {
		if (!deposited && !inv.isEmpty())
			return ASS.DEPOSIT_ALL;
		else if (inv.isEmpty())
			deposited = true;
		if (!withdrawn)
			return withdrawInvent();
		return ASS.TIMEOUT;
	}

	ASS getStates() {
		if (timeout != 0)
			return ASS.TIMEOUT;
		if (targetLoc != null && player.getWorldLocation().distanceTo(targetLoc) <= 0) {
			log.info("cleared targetLoc");
			targetLoc = null;
		} else if (targetLoc != null) {
			log.info(targetLoc.toString());
			walk.webWalk(targetLoc, 0, player.isMoving(), sleepDelay());
			return ASS.TIMEOUT;
		}
		if (bank.isOpen()) {
			if (withdrawn && player.getWorldArea().intersectsWith(hosidiusBank)) // double checking
				return ASS.WALK_TO_DUNGEON;
			return getBankState();
		}
		if (!deposited) {
			if (player.getWorldArea().intersectsWith(hosidiusBank)) {
				return ASS.FIND_BANK;
			}
		}
		return getState();
	}

	ASS getState() {
		if (!isAtDungeon()) {
			if (isInPOH(client)) {
				if (pray.isActive(Prayer.PROTECT_FROM_MISSILES))
					return ASS.PROTECT_FROM_MISSILES;
				if (pray.isActive(Prayer.PROTECT_FROM_MELEE))
					return ASS.PROTECT_FROM_MELEE;
				if (pray.isActive(Prayer.PIETY)) {
					return ASS.TOGGLE_PIETY;
				} else if (pray.isActive(Prayer.CHIVALRY)) {
					return ASS.TOGGLE_CHIVALRY;
				}
				if (config.usePool() && (client.getBoostedSkillLevel(Skill.PRAYER) != client.getRealSkillLevel(Skill.PRAYER) || client.getBoostedSkillLevel(Skill.HITPOINTS) > client.getRealSkillLevel(Skill.HITPOINTS))) {
					return ASS.USE_POOL;
				}
				if (config.hosidius() == ASC.Hosidius.MOUNTED_XERICS) {
					if (client.getWidget(187, 3) != null && !client.getWidget(187, 3).isHidden()) {
						targetMenu = new LegacyMenuEntry("", "", 0, MenuAction.WIDGET_CONTINUE, 1, client.getWidget(187, 3).getId(), false);
						utils.doActionMsTime(targetMenu, client.getWidget(187, 3).getBounds(), sleepDelay());
					} else {
						DecorativeObject talisman = objectUtils.findNearestDecorObject(33412);
						if (talisman == null)
							talisman = objectUtils.findNearestDecorObject(33419);
						targetMenu = new LegacyMenuEntry("", "", talisman.getId(), MenuAction.GAME_OBJECT_SECOND_OPTION, talisman.getLocalLocation().getSceneX(), talisman.getLocalLocation().getSceneY(), false);
						utils.doActionMsTime(targetMenu, talisman.getConvexHull().getBounds(), sleepDelay());
					}
				} else {
					GameObject pohPortal = objectUtils.findNearestGameObject(4525);
					if (pohPortal != null) {
						actionObject(pohPortal, MenuAction.GAME_OBJECT_FIRST_OPTION);
					}
				}
				timeout = 3;
				return ASS.TIMEOUT;
			} else if (player.getWorldLocation().distanceTo(new WorldPoint(1741, 3517, 0)) <= 4) {
				return ASS.WALK_TO_BOOTH;
			} else if (player.getWorldLocation().distanceTo(walkToDungeon3) <= 4)
				return ASS.DESCEND_STAIRS;
			else if (player.getWorldArea().intersectsWith(xericsGlade)) {
				return ASS.TRAVEL_BANK;
			} else if (player.getWorldArea().distanceTo(travelBank) <= 3) {
				return ASS.WALK_TO_BOOTH;
			}
			return ASS.WALK_TO_DUNGEON;
		} else {
			if (!inInstance) {
				if (player.getWorldLocation().equals(bottomStairs)) {
					walk.sceneWalk(firstWeb, 0, sleepDelay());
				} else if (player.getWorldLocation().equals(firstWeb)) {
					return ASS.CUT_FIRST_WEB;
				} else if (player.getWorldLocation().equals(new WorldPoint(1833, 9943, 0))) {
					walk.sceneWalk(secondWeb, 0, sleepDelay());
					timeout = 2;
					return ASS.TIMEOUT;
				} else if (player.getWorldLocation().equals(secondWeb)) {
					return ASS.CUT_SECOND_WEB;
				} else if (player.getWorldLocation().equals(new WorldPoint(1847, 9920, 0))) {
					return ASS.CUT_THIRD_WEB;
				} else if (player.getWorldLocation().equals(new WorldPoint(1847, 9919, 0))) {
					return ASS.WALK_TO_WEB;
				} else if (player.getWorldLocation().equals(bossWeb)) {
					if (!pray.isActive(Prayer.PROTECT_FROM_MISSILES))
						return ASS.PROTECT_FROM_MISSILES;
					return ASS.ENTER_BOSS_ROOM;
				}
				return ASS.TIMEOUT;
			} else {
				// kill logic comes here
				// if the boss is alive
				if (sarachnis != null && sarachnis.getAnimation() != 8318) {
					if (client.getBoostedSkillLevel(Skill.PRAYER) >= 1) {
						if (!inMeleeDistance(sarachnis.getId(), client)) {// range
							attack = true;
							if (!pray.isActive(Prayer.PROTECT_FROM_MISSILES))
								return ASS.PROTECT_FROM_MISSILES;
						} else {
							if (!pray.isActive(Prayer.PROTECT_FROM_MELEE))
								return ASS.PROTECT_FROM_MELEE;
						}
					}
					// turn run back on?
					if (!playerUtils.isRunEnabled() && client.getEnergy() >= 30) {
						playerUtils.handleRun(25, 0);
						return ASS.TIMEOUT;
					}
					// need to restore prayer?
					if (client.getBoostedSkillLevel(Skill.PRAYER) <= config.restoreAt()) {
						attack = true;
						return ASS.RESTORE_PRAYER;
					}
					// need to heal?
					if (client.getBoostedSkillLevel(Skill.HITPOINTS) <= config.eatAt()) {
						if (inv.containsItem(config.food().getId())) {
							attack = true;
							return ASS.EAT_FOOD;
						} else
							return ASS.TELE_TO_POH;
					}
					if (config.usePiety() && !pray.isActive(Prayer.PIETY)) {
						return ASS.TOGGLE_PIETY;
					} else if (config.useChivalry() && !pray.isActive(Prayer.CHIVALRY)) {
						return ASS.TOGGLE_CHIVALRY;
					}

					// need to reboost stats?
					if (needsRepot() && config.combatPotion() != ASC.SuperCombat.NONE) {
						attack = true;
						return ASS.DRINK_COMBAT_POT;
					}

					if (attack) {
						if (config.specWeapon() != ASC.Special.NONE && client.getVar(VarPlayer.SPECIAL_ATTACK_PERCENT) >= config.specWeapon().getSpecAmt() * 10) {
							if (!equip.isEquipped(config.specWeapon().getItemID())) {
								if (inv.isFull() && config.specWeapon().is2Handed())
									return ASS.EAT_FOOD;
								actionItem(config.specWeapon().getItemID(), "wear", "equip", "wield");
								timeout = 0;
								return ASS.TIMEOUT;
							}
							if (!config.specWeapon().is2Handed() && !equip.isEquipped(config.offhand().getItemID())) {
								actionItem(config.offhand().getItemID(), "wear", "equip", "wield");
								timeout = 0;
								return ASS.TIMEOUT;
							}
							if (client.getVar(VarPlayer.SPECIAL_ATTACK_ENABLED) == 0) {
								bounds = client.getWidget(WidgetInfo.MINIMAP_SPEC_CLICKBOX).getBounds();
								targetMenu = new LegacyMenuEntry("<col=ff9040>Special Attack</col>", "", 1, MenuAction.CC_OP.getId(), -1, WidgetInfo.MINIMAP_SPEC_CLICKBOX.getId(), false);
								if (config.invokes())
									utils.doInvokeMsTime(targetMenu, (int)sleepDelay());
								else
									utils.doActionMsTime(targetMenu, bounds.getBounds(), (int)sleepDelay());
								timeout = 0;
								return ASS.TIMEOUT;
							} else {
								return ASS.ATTACK_SARACHNIS;
							}
						} else {
							WidgetItem item;
							int mh = config.mainhand().getItemID();
							int oh = config.offhand().getItemID();
							if (!equip.isEquipped(mh) || (!config.mainhand().is2Handed() && !equip.isEquipped(oh)
									&& config.offhand() != ASC.Offhand.NONE))
								return ASS.EQUIP_WEAPONS;
							else
								return ASS.ATTACK_SARACHNIS;
						}
					} else {
						WidgetItem item;
						int mh = config.mainhand().getItemID();
						int oh = config.offhand().getItemID();
						if (!equip.isEquipped(mh) || (!config.mainhand().is2Handed() && !equip.isEquipped(oh)
								&& config.offhand() != ASC.Offhand.NONE))
							attack = true;
						if (sarachnis != null && player.getInteracting() != sarachnis)
							attack = true;
						return ASS.TIMEOUT;
					}
				} else { // if boss is dead or dying
					if (pray.isActive(Prayer.PROTECT_FROM_MISSILES))
						return ASS.PROTECT_FROM_MISSILES;
					if (pray.isActive(Prayer.PROTECT_FROM_MELEE))
						return ASS.PROTECT_FROM_MELEE;
					if (pray.isActive(Prayer.PIETY)) {
						return ASS.TOGGLE_PIETY;
					} else if (pray.isActive(Prayer.CHIVALRY)) {
						return ASS.TOGGLE_CHIVALRY;
					}
					if (inv.containsItem(ItemID.VIAL)) {
						actionItem(ItemID.VIAL, "drop");
						return ASS.TIMEOUT;
					}

					if (client.getBoostedSkillLevel(Skill.HITPOINTS) <= (client.getRealSkillLevel(Skill.HITPOINTS) - 18)) {
						if (inv.containsItem(config.food().getId())) {
							attack = true;
							return ASS.EAT_FOOD;
						}
					}

					if (!looted && !toLoot.isEmpty()) {
						if (inv.isFull()) {
							if (!config.eatLoot()) {
								toLoot.clear();
							} else {
								if (inv.containsItem(config.food().getId()))
									return ASS.EAT_FOOD;
								else
									return ASS.TELE_TO_POH;
							}
						} else {
							return ASS.LOOT_SARACHNIS;
						}
					}
				}
			}
		}
		return ASS.TIMEOUT;
	}

	void equipWeapons() {
		equipWeapons(true);
	}

	void equipWeapons(boolean att) {
		WidgetItem item;
		if (!equip.isEquipped(config.mainhand().getItemID()) && timeout <= 1) {
			actionItem(config.mainhand().getItemID(), (int)sleepDelay(), "wear", "equip", "wield");
			attack = att;
		}
		if (!equip.isEquipped(config.offhand().getItemID()) && timeout <= 1 && config.offhand() != ASC.Offhand.NONE && !config.mainhand().is2Handed()) {
			actionItem(config.offhand().getItemID(), (int)sleepDelay(), "wear", "equip", "wield");
			attack = att;
		}
	}

	boolean inMeleeDistance(int id, Client client) {
		player = client.getLocalPlayer();
		if (player != null) {
			NPC target = npcs.findNearestNpc(NpcID.SARACHNIS);
			if (target != null) {
				//utils.sendGameMessage("distance: " + player.getWorldLocation().distanceTo(new WorldPoint(target.getWorldLocation().getX() + 2, target.getWorldLocation().getY() + 2, 0)));
				return player.getWorldLocation().distanceTo(new WorldPoint(target.getWorldLocation().getX() + 2, target.getWorldLocation().getY() + 2, 0)) <= 3;
			}
		}
		return false;
	}

	boolean teleToPOH() {
		if (config.houseTele().getId() == ItemID.CONSTRUCT_CAPET || config.houseTele().getId() == ItemID.CONSTRUCT_CAPE)
			actionItem(ItemID.CONSTRUCT_CAPET, "tele to poh");
		else if (config.houseTele().getId() == ItemID.TELEPORT_TO_HOUSE)
			actionItem(ItemID.TELEPORT_TO_HOUSE, "break");
		else if (config.houseTele().getId() == ItemID.RUNE_POUCH) {
			Widget widget = client.getWidget(218, 29);
			if (widget != null) {
				targetMenu = new LegacyMenuEntry("Cast", "<col=00ff00>Teleport to House</col>", 1 , MenuAction.CC_OP, -1, widget.getId(), false);
				utils.doActionMsTime(targetMenu, widget.getBounds(), (int)sleepDelay());
			}
		}
		withdrawn = false;
		deposited = false;
		toLoot.clear();
		return true;
	}

	public static boolean isInPOH(Client client) {
		IntStream stream = Arrays.stream(client.getMapRegions());
		List<Integer> regions = AutoSarachnis.regions;
		Objects.requireNonNull(regions);
		return stream.anyMatch(regions::contains);
	}

	void eatFood() {
		actionItem(config.food().getId(), "eat", "drink");
	}

	private boolean needsRepot() {
		int boost = client.getBoostedSkillLevel(Skill.STRENGTH);
		int repot = config.boostLevel();
		return boost <= repot;
	}

	void drinkPrayer() {
		int pot = -1;
		if (inv.containsItem(config.prayer().getDose4()))
			pot = config.prayer().getDose4();
		if (inv.containsItem(config.prayer().getDose3()))
			pot = config.prayer().getDose3();
		if (inv.containsItem(config.prayer().getDose2()))
			pot = config.prayer().getDose2();
		if (inv.containsItem(config.prayer().getDose1()))
			pot = config.prayer().getDose1();
		if (pot == -1) {
			teleToPOH();
			return;
		}
		actionItem(pot, "drink", "eat");
	}

	void drinkCombatPotion() {
		int pot = -1;
		if (inv.containsItem(config.combatPotion().getDose4()))
			pot = config.combatPotion().getDose4();
		if (inv.containsItem(config.combatPotion().getDose3()))
			pot = config.combatPotion().getDose3();
		if (inv.containsItem(config.combatPotion().getDose2()))
			pot = config.combatPotion().getDose2();
		if (inv.containsItem(config.combatPotion().getDose1()))
			pot = config.combatPotion().getDose1();
		if (pot == -1) {
			teleToPOH();
			return;
		}
		actionItem(pot, "drink", "eat");
	}

	boolean isAtDungeon() {
		return inRegion(client, dungeon);
	}

	boolean inRegion(Client client, List<Integer> region) {
		return Arrays.stream(client.getMapRegions()).anyMatch(region::contains);
	}

	private void openBank() {
		GameObject bank = objectUtils.findNearestBank();
		if (bank == null)
			return;
		actionObject(bank.getId(), MenuAction.GAME_OBJECT_SECOND_OPTION);
	}

	int getPrayRestoreDoses() {
		int count = 0;
		count += (inv.getItemCount(config.prayer().getDose1(), false));
		count += (inv.getItemCount(config.prayer().getDose2(), false) * 2);
		count += (inv.getItemCount(config.prayer().getDose3(), false) * 3);
		count += (inv.getItemCount(config.prayer().getDose4(), false) * 4);
		if (config.debug())
			utils.sendGameMessage("Current prayer restore doses: " + count);
		return count;
	}

	boolean isLootableItem(TileItem item) {
		String name = client.getItemDefinition(item.getId()).getName().toLowerCase();
		int value = utils.getItemPrice(item.getId(), true) * item.getQuantity();
		if (includedItems != null && includedItems.stream().anyMatch(name.toLowerCase()::contains))
			return true;
		if (excludedItems != null && excludedItems.stream().anyMatch(name.toLowerCase()::contains))
			return false;
		return value >= config.lootValue();
	}

	void lootItem(java.util.List<TileItem> itemList) {
		TileItem lootItem = this.getNearestTileItem(itemList);
		if (lootItem != null) {
			this.clientThread.invoke(() -> this.client.invokeMenuAction("", "", lootItem.getId(), MenuAction.GROUND_ITEM_THIRD_OPTION.getId(), lootItem.getTile().getSceneLocation().getX(), lootItem.getTile().getSceneLocation().getY()));
		}
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

	private boolean actionObject(GameObject obj, MenuAction action) {
		if (obj == null)
			return false;
		targetMenu = new LegacyMenuEntry("", "", obj.getId(), action, obj.getSceneMinLocation().getX(), obj.getSceneMinLocation().getY(), false);
		if (!config.invokes())
			utils.doGameObjectActionMsTime(obj, action.getId(), sleepDelay());
		else
			utils.doInvokeMsTime(targetMenu, sleepDelay());
		return true;
	}

	private boolean actionItem(int id, int delay, String... action) {
		if (inv.containsItem(id)) {
			WidgetItem item = inv.getWidgetItem(id);
			targetMenu = inventoryAssistant.getLegacyMenuEntry(item.getId(), action);
			if (config.invokes()) {
				utils.doInvokeMsTime(targetMenu, delay);
			} else {
				utils.doActionMsTime(targetMenu, item.getCanvasBounds(), delay);
			}
			return true;
		}
		return false;
	}

	private boolean actionItem(int id, String... action) {
		return actionItem(id, (int)sleepDelay(), action);
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