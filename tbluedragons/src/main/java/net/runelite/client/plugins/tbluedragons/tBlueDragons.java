package net.runelite.client.plugins.tbluedragons;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.api.queries.NPCQuery;
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
import net.runelite.client.plugins.tutils.game.Game;
import net.runelite.client.plugins.tutils.game.iGroundItem;
import net.runelite.client.plugins.tutils.scripts.ReflectBreakHandler;
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
import java.util.function.Predicate;

@Extension
@PluginDependency(tUtils.class)
@PluginDescriptor(
        name = "tBlueDragons",
        description = "Kills blue dragooons (not the babies though wtf)",
        enabledByDefault = false,
        tags = {"fw", "Tea", "tea", "blue", "dragon", "scales", "dust", "bd"}
)
@Slf4j
public class tBlueDragons extends Plugin {
    @Inject
    tBlueDragonsOverlay overlay;
    @Inject
    tBlueDragonsTileOverlay tileOverlay;
    @Inject
    tBlueDragonsConfig config;
    tBlueDragonsState state;
    tBlueDragonsState lastState;
    LegacyMenuEntry targetMenu;
    boolean startPlugin;
    Instant botTimer;
    int timeout;
    WorldPoint faladorTeleport;
    WorldPoint safeSpot;
    WorldArea blueDragons;
    WorldArea faladorBank;
    boolean deposited;
    WorldPoint targetLoc;
    int rand;
    List<TileItem> loot;
    int kills;
    int lootValue;
    NPC target;
    WorldPoint deathLocation;
    TileItem attempt;
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
    private ReflectBreakHandler chinBreakHandler;
    @Inject
    private ExecutorService executorService;
    @Inject
    private ConfigManager configManager;
    @Inject
    private PlayerUtils playerUtils;
    private Player player;
    private Rectangle bounds;
    private long sleepLength;

    public tBlueDragons() {
        botTimer = null;
        startPlugin = false;
        state = tBlueDragonsState.TIMEOUT;
        lastState = state;
    }

    static Predicate<NPC> nameOrIdEquals(String... namesorids) {
        return (NPC n) -> Arrays.stream(namesorids).anyMatch(str -> {
            if (n.getTransformedComposition() != null && n.getTransformedComposition().getName().equalsIgnoreCase(str))
                return true;
            try {
                int id = Integer.parseInt(str);
                return n.getTransformedComposition() != null && n.getTransformedComposition().getId() == id;
            } catch (NumberFormatException ignored) {
            }
            return false;
        });
    }

    @Provides
    tBlueDragonsConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(tBlueDragonsConfig.class);
    }

    private void reset() {
        timeout = 0;
        startPlugin = false;
        botTimer = null;
        state = tBlueDragonsState.TIMEOUT;
        lastState = state;
        overlayManager.remove(overlay);
        overlayManager.remove(tileOverlay);
        chinBreakHandler.stopPlugin(this);
    }

    @Override
    protected void startUp() {
        reset();
        chinBreakHandler.registerPlugin(this);
    }

    @Override
    protected void shutDown() {
        chinBreakHandler.unregisterPlugin(this);
    }

    @Subscribe
    private void on(ConfigButtonClicked configButtonClicked) throws IOException, InterruptedException {
        if (!configButtonClicked.getGroup().equalsIgnoreCase("tBlueDragons")) {
            return;
        }
        switch (configButtonClicked.getKey()) {
            case "startPlugin":
                if (!startPlugin) {
                    player = client.getLocalPlayer();
                    if (player != null && client != null) {
                        startPlugin = true;
                        botTimer = Instant.now();
                        state = tBlueDragonsState.TIMEOUT;
                        overlayManager.add(overlay);
                        overlayManager.add(tileOverlay);
                        chinBreakHandler.startPlugin(this);
                        timeout = 1;
                        deposited = false;
                        targetLoc = null;
                        loot = new ArrayList<>();
                        kills = 0;
                        lootValue = 0;
                        // WorldPoint swLocation, WorldPoint neLocation
                        faladorTeleport = new WorldPoint(2965, 3380, 0);
                        faladorBank = new WorldArea(new WorldPoint(2943, 3368, 0), new WorldPoint(2949, 3373, 0));
                        blueDragons = new WorldArea(new WorldPoint(2892, 9773, 0), new WorldPoint(2927, 9814, 0));
                        safeSpot = new WorldPoint(2901, 9809, 0);
                    }
                } else {
                    reset();
                }
                break;
        }
    }

    @Subscribe
    private void on(ChatMessage event) {
        if (!startPlugin)
            return;
        if (event.getType() == ChatMessageType.CONSOLE)
            return;
        if (event.getType() == ChatMessageType.SPAM) {
            if (event.getMessage().equalsIgnoreCase("i'm already under attack."))
                target = null;
        }
        if (event.getType() == ChatMessageType.GAMEMESSAGE) {
            if (event.getMessage().contains("You do not have enough") && event.getMessage().contains("to cast this spell.")) {
                teleport();
                reset();
            }
            if (event.getMessage().contains("so you can't take items")) {
                loot.remove(attempt);
                log.info("Can't loot that item, removing from list: {}", attempt.toString());
                attempt = null;
            }
        }
    }

    @Subscribe
    private void on(GameTick event) {
        if (!startPlugin || chinBreakHandler.isBreakActive(this))
            return;
        player = client.getLocalPlayer();
        if (player != null && client != null) {
            int id = -1;
            state = getStates();
            if (state != tBlueDragonsState.TIMEOUT)
                lastState = state;
            switch (state) {
                case TIMEOUT:
                    if (timeout <= 0)
                        timeout = 0;
                    else
                        timeout--;
                    break;
                case HANDLE_BREAK:
                    chinBreakHandler.startBreak(this);
                    timeout = calc.getRandomIntBetweenRange(2, 20);
                    break;
                case OPEN_BANK:
                    actionObject(24101, MenuAction.GAME_OBJECT_SECOND_OPTION);
                    break;
                case WITHDRAW_ITEMS:
                    if (!deposited) {
                        deposited = true;
                        if (inv.isEmpty())
                            break;
                        bank.depositAll();
                        break;
                    }
                    if (config.useCombatPot() && !hasCombatPots()) {
                        for (List<Integer> pots : config.combatPot().getCombatPots()) {
                            if (inv.getItemCount(pots.get(0), false) != config.combatAmount()) {
                                withdraw(pots.get(0), config.combatAmount());
                                break;
                            }
                        }
                    } else if (config.usePrayPot() && !hasPrayPots()) {
                        if (inv.getItemCount(config.prayPot().getPrayerPot().get(0), false) != config.prayAmount()) {
                            withdraw(config.prayPot().getPrayerPot().get(0), config.prayAmount());
                            break;
                        }
                    } else if (config.useAntifire() && !hasAntifirePots()) {
                        if (inv.getItemCount(config.antifirePot().getAntifirePot().get(0), false) != config.antifireAmount()) {
                            withdraw(config.antifirePot().getAntifirePot().get(0), config.antifireAmount());
                            break;
                        }
                    } else if (config.useFood() && !hasFood()) {
                        if (inv.getItemCount(config.food().getFood().get(0), false) < config.foodAmount()) {
                            /*int amt = config.foodAmount();
                            int real = client.getRealSkillLevel(Skill.HITPOINTS);
                            int boost = client.getBoostedSkillLevel(Skill.HITPOINTS);
                            int foodToFull = (int) Math.ceil(((real - boost) / config.food().getHeal()));
                            log.info("Need " + foodToFull + " to get to full HP. Withdrawing: " + (foodToFull + amt + (foodToFull > 0 ? 1 : 0)));*/
                            withdraw(config.food().getFood().get(0), (config.foodAmount() - inv.getItemCount(config.food().getFood().get(0), false)));
                            break;
                        }
                    } else if (config.useRunePouch() && !inv.containsItem(ItemID.RUNE_POUCH)) {
                        withdraw(ItemID.RUNE_POUCH);
                        break;
                    } else if (selectedRunes() && !hasAllRunes()) {
                        for (tBlueDragonsConfig.Runes rune : config.takeRunes()) {
                            if (config.useRunePouch()) {
                                if (!inv.runePouchContains(rune.getID()) && !inv.containsItem(rune.getID())) {
                                    withdraw(rune.getID(), true);
                                    break;
                                }
                            } else {
                                if (!inv.containsItem(rune.getID())) {
                                    withdraw(rune.getID(), true);
                                    break;
                                }
                            }
                        }
                    } else if (config.useBonecrusher()) {
                        if (!inv.containsItem(ItemID.BONECRUSHER)) {
                            withdraw(ItemID.BONECRUSHER);
                            break;
                        }
                    } else {
                        if (config.bankLoc() == tBlueDragonsConfig.Banking.FALADOR_TELETAB) {
                            if (!inv.containsItem(ItemID.FALADOR_TELEPORT)) {
                                withdraw(ItemID.FALADOR_TELEPORT);
                                break;
                            }
                        }
                    }
                    break;
                case CLOSE_BANK:
                    bank.close();
                    break;
                case USE_OBSTACLE:
                    actionObject(24222, MenuAction.GAME_OBJECT_FIRST_OPTION);
                    break;
                case WALK_TO_DUNGEON:
                    if (distance(new WorldPoint(2934, 3355, 0), 0)) {
                        walk.sceneWalk(new WorldPoint(2917, 3372, 0), 2, sleepDelay());
                        timeout = tickDelay();
                    } else if (distance(new WorldPoint(2917, 3372, 0), 2)) {
                        actionObject(16680, MenuAction.GAME_OBJECT_FIRST_OPTION);
                        timeout = tickDelay();
                    }
                    break;
                case CLIMB_DOWN:
                    actionObject(16680, MenuAction.GAME_OBJECT_FIRST_OPTION);
                    timeout = tickDelay();
                    break;
                case USE_TUNNEL:
                    actionObject(16509, MenuAction.GAME_OBJECT_FIRST_OPTION);
                    timeout = tickDelay();
                    break;
                case EAT_FOOD:
                    id = inv.getWidgetItem(config.food().getFood()).getId();
                    if (id != -1) {
                        actionItem(id, "eat");
                    }
                    break;
                case DRINK_ANTIFIRE:
                    id = inv.getWidgetItem(config.antifirePot().getAntifirePot()).getId();
                    if (id != -1) {
                        actionItem(id, "drink");
                    }
                    break;
                case DRINK_COMBAT_POT:
                    if (config.combatPot() == tBlueDragonsConfig.CombatPot.SUPER_ATT_STR
                            || config.combatPot() == tBlueDragonsConfig.CombatPot.SUPER_ATT_STR_DEF) {
                        if (client.getBoostedSkillLevel(Skill.ATTACK) <= config.boostAtt()) {
                            id = inv.getWidgetItem(List.of(ItemID.SUPER_ATTACK4, ItemID.SUPER_ATTACK3, ItemID.SUPER_ATTACK2, ItemID.SUPER_ATTACK1)).getId();
                            if (id != -1) {
                                actionItem(id, "drink");
                                break;
                            }
                        }
                        if (client.getBoostedSkillLevel(Skill.STRENGTH) <= config.boostStr()) {
                            id = inv.getWidgetItem(List.of(ItemID.SUPER_STRENGTH4, ItemID.SUPER_STRENGTH3, ItemID.SUPER_STRENGTH2, ItemID.SUPER_STRENGTH1)).getId();
                            if (id != -1) {
                                actionItem(id, "drink");
                                break;
                            }
                        }
                        if (config.combatPot() == tBlueDragonsConfig.CombatPot.SUPER_ATT_STR_DEF) {
                            if (client.getBoostedSkillLevel(Skill.DEFENCE) <= config.boostDef()) {
                                id = inv.getWidgetItem(List.of(ItemID.SUPER_DEFENCE4, ItemID.SUPER_DEFENCE3, ItemID.SUPER_DEFENCE2, ItemID.SUPER_DEFENCE1)).getId();
                                if (id != -1) {
                                    actionItem(id, "drink");
                                    break;
                                }
                            }
                        }
                    } else {
                        for (List<Integer> pots : config.combatPot().getCombatPots()) {
                            id = inv.getWidgetItem(pots).getId();
                            if (id != -1) {
                                actionItem(id, "drink");
                            }
                        }
                    }
                    break;
                case DRINK_PRAYER_POTS:
                    id = inv.getWidgetItem(config.prayPot().getPrayerPot()).getId();
                    if (id != -1) {
                        actionItem(id, "drink");
                    }
                    break;
                case ATTACK_DRAGON:
                    if (target == null || (target.getInteracting() != null && target.getInteracting() != player)) {
                        target = null;
                        break;
                    }
                    actionNPC(target, MenuAction.NPC_SECOND_OPTION);
                    break;
                case HANDLE_PRAYER:
                    if (client.getBoostedSkillLevel(Skill.PRAYER) >= 1) {
                        if (config.prayType() == tBlueDragonsConfig.PrayType.QUICK_PRAYERS) {
                            Widget widget = client.getWidget(10485775);
                            targetMenu = new LegacyMenuEntry("", "Quick-prayers", 1, MenuAction.CC_OP.getId(), -1, widget.getId(), false);
                            if (pray.isQuickPrayerActive() && config.flickQuickPrayers())
                                utils.doInvokeMsTime(targetMenu, 0);
                            if (!pray.isQuickPrayerActive())
                                utils.doInvokeMsTime(targetMenu, 200);
                        } else if (config.prayType() == tBlueDragonsConfig.PrayType.CUSTOM) {
                            if (config.useMeleeProt()) {
                                if (config.flickMelee() && pray.isActive(Prayer.PROTECT_FROM_MELEE)) {
                                    toggle(Prayer.PROTECT_FROM_MELEE, 0);
                                }
                            }
                            if (config.useOffPray()) {
                                if (config.flickOffensive() && pray.isActive(config.offPray().getPrayer())) {
                                    toggle(config.offPray().getPrayer(), 0);
                                }
                            }
                            if (config.useMeleeProt()) {
                                if (!pray.isActive(Prayer.PROTECT_FROM_MELEE)) {
                                    toggle(Prayer.PROTECT_FROM_MELEE, 120);
                                }
                            }
                            if (config.useOffPray()) {
                                if (!pray.isActive(config.offPray().getPrayer())) {
                                    toggle(config.offPray().getPrayer(), 120);
                                }
                            }
                        }
                    }
                    break;
                case TELEPORT_OUT:
                    teleport();
                    break;
                case LOOT_ITEMS:
                    if (inv.getEmptySlots() >= 1) {
                        lootItem(loot);
                    } else {
                        teleport();
                    }
                    break;
				case LOOT_SCALES:
					if (inv.getEmptySlots() >= 1) {
						lootScales();
					} else {
						teleport();
					}
					break;
                case MOVING:
                    if (!bank.isOpen() && !playerUtils.isRunEnabled()) {
                        playerUtils.handleRun(15, 30);
                    }
                    break;
                case SAFESPOT:
                    walk.sceneWalk(safeSpot, 0, sleepDelay());
                    break;
            }
        }
    }

    boolean selectedRunes() {
        return !config.takeRunes().isEmpty();
    }

    boolean hasAllRunes() {
        for (tBlueDragonsConfig.Runes rune : config.takeRunes()) {
            if (config.useRunePouch()) {
                if (!inv.runePouchContains(rune.getID()) && !inv.containsItem(rune.getID())) {
                    return false;
                }
            } else {
                if (!inv.containsItem(rune.getID())) {
                    return false;
                }
            }
        }
        return true;
    }

    boolean withdraw(int id) {
        return withdraw(id, 1);
    }

    boolean withdraw(int id, boolean all) {
        if (bank.contains(id, 1)) {
            log.info("Withdrawing: " + client.getItemDefinition(id).getName() + "");
            bank.withdrawAllItem(id);
            return true;
        } else {
            log.info("Unable to find item in bank: " + client.getItemDefinition(id).getName());
            utils.sendGameMessage("Unable to find item in bank: " + client.getItemDefinition(id).getName());
            reset();
            return false;
        }
    }

    boolean withdraw(int id, int qty) {
        if (bank.contains(id, qty)) {
            log.info("Withdrawing: " + client.getItemDefinition(id).getName() + "");
            bank.withdrawItemAmount(id, qty);
            return true;
        } else {
            log.info("Unable to find item in bank: " + client.getItemDefinition(id).getName());
            utils.sendGameMessage("Unable to find item in bank: " + client.getItemDefinition(id).getName());
            reset();
            return false;
        }
    }

    void teleport() {
        if (config.bankLoc() == tBlueDragonsConfig.Banking.FALADOR_TELEPORT) {
            targetMenu = new LegacyMenuEntry("", "", 1, MenuAction.CC_OP, -1, client.getWidget(218, 27).getId(), false);
            utils.doActionMsTime(targetMenu, client.getWidget(218, 27).getBounds(), sleepDelay());
        } else if (config.bankLoc() == tBlueDragonsConfig.Banking.FALADOR_TELETAB) {
            actionItem(ItemID.FALADOR_TELEPORT, "break");
        }
        timeout = 5;
        loot.clear();
    }

    @Subscribe
    private void on(ItemSpawned event) {
        if (!startPlugin)
            return;
        String name = client.getItemDefinition(event.getItem().getId()).getName();
        if (isLootableItem(event.getItem())) {
            timeout = 0;
            if (!loot.contains(event.getItem()) && distance(event.getTile().getWorldLocation(), event.getItem().getId() == ItemID.BLUE_DRAGON_SCALE ? 99 : 10))
                loot.add(event.getItem());
        }
    }

    @Subscribe
    private void on(ItemDespawned event) {
        if (!startPlugin)
            return;
        if (loot.contains(event.getItem()))
            lootValue += utils.getItemPrice(event.getItem().getId(), true) * event.getItem().getQuantity();
        loot.remove(event.getItem());
    }

    boolean isLootableItem(TileItem item) {
		if (config.mode().toString().equalsIgnoreCase("kill_dragons")) {
			String name = client.getItemDefinition(item.getId()).getName().toLowerCase();
			int value = utils.getItemPrice(item.getId(), true) * item.getQuantity();
			String[] included = config.includedItems().toLowerCase().split("\\s*,\\s*");
			List<String> include = new ArrayList<>();
			include.addAll(List.of(included));
			String[] excluded = config.includedItems().toLowerCase().split("\\s*,\\s*");
			List<String> exclude = new ArrayList<>();
			exclude.addAll(List.of(included));
			if (include.stream().anyMatch(name.toLowerCase()::contains))
				return true;
			if (exclude.stream().anyMatch(name.toLowerCase()::contains))
				return false;
			if (name.equalsIgnoreCase("dragon bones") && config.lootBones())
				return true;
			if (name.equalsIgnoreCase("blue dragonhide") && config.lootHides())
				return true;
			return value >= config.lootValue();
		} else
			return item.getId() == ItemID.BLUE_DRAGON_SCALE;
    }

    @Subscribe
    private void on(NpcSpawned event) {
        if (!startPlugin)
            return;
        final NPC npc = event.getNpc();
        if (npc.getName() == null) {
            return;
        }
    }

    @Subscribe
    private void on(NpcDespawned event) {
        if (!startPlugin)
            return;
        final NPC npc = event.getNpc();
        if (npc.getName() == null) {
            return;
        }
        if (npc == target)
            target = null;
    }

    @Subscribe
    private void onActorDeath(ActorDeath event) {
        if (!startPlugin)
            return;
        Actor actor = event.getActor();
        if (actor.equals(target)) {
            deathLocation = actor.getWorldLocation();
            log.info("Target died, setting deathLocation to {}", deathLocation.toString());
            if (!config.safespot())
                walk.sceneWalk(target.getWorldLocation(), 0, sleepDelay());
            target = null;
            kills++;
        }
    }

    @Subscribe
    private void on(AnimationChanged event) {
        if (!startPlugin)
            return;
        Actor actor = event.getActor();
    }

    tBlueDragonsState getStates() {
        if (player.isMoving())
            return tBlueDragonsState.MOVING;
        if (timeout != 0
                || (client.getWidget(WidgetInfo.BANK_PIN_CONTAINER) != null
                && !client.getWidget(WidgetInfo.BANK_PIN_CONTAINER).isHidden()))
            return tBlueDragonsState.TIMEOUT;
        if (targetLoc != null && distance(targetLoc, rand)) {
            log.info("cleared targetLoc");
            targetLoc = null;
            timeout = 2 + tickDelay();
            return tBlueDragonsState.TIMEOUT;
        } else if (targetLoc != null) {
            log.info(targetLoc.toString());
            walk.webWalk(targetLoc, rand, true, sleepDelay());
            timeout = 2 + tickDelay();
            return tBlueDragonsState.TIMEOUT;
        }
        if (client.getWidget(162, 41) != null && !Objects.requireNonNull(client.getWidget(162, 41)).isHidden())
            return tBlueDragonsState.TIMEOUT;
        if (distance(faladorTeleport, 4)) {
            if (pray.isQuickPrayerActive()) {
                Widget widget = client.getWidget(10485775);
                targetMenu = new LegacyMenuEntry("", "Quick-prayers", 1, MenuAction.CC_OP.getId(), -1, widget.getId(), false);
                utils.doInvokeMsTime(targetMenu, 0);
                return tBlueDragonsState.TIMEOUT;
            }
            if (pray.isActive(Prayer.PROTECT_FROM_MELEE)) {
                toggle(Prayer.PROTECT_FROM_MELEE, 0);
                return tBlueDragonsState.TIMEOUT;
            }
            if (pray.isActive(config.offPray().getPrayer())) {
                toggle(config.offPray().getPrayer(), 0);
                return tBlueDragonsState.TIMEOUT;
            }
            return tBlueDragonsState.OPEN_BANK;
        }
        if (player.getWorldLocation().isInArea(faladorBank)) {
            if (bank.isOpen())
                return getBankStates();
            if (!inventoryReady())
                return tBlueDragonsState.OPEN_BANK;
            if (inventoryReady()) {
                if (config.useFood() && client.getBoostedSkillLevel(Skill.HITPOINTS) < client.getRealSkillLevel(Skill.HITPOINTS)) {
                    if (inv.containsItem(config.food().getFood())) {
                        return tBlueDragonsState.EAT_FOOD;
                    }
                }
                return tBlueDragonsState.USE_OBSTACLE;
            }
        }
        if (!player.getWorldLocation().isInArea(blueDragons)) {
            if (distance(new WorldPoint(2884, 9797, 0), 1)) {
                if (client.getRealSkillLevel(Skill.AGILITY) >= 70) {
                    if (config.useAntifire() && needsAntifire()) {
                        if (hasAnyAntifirePots())
                            return tBlueDragonsState.DRINK_ANTIFIRE;
                        else
                            return tBlueDragonsState.TELEPORT_OUT;
                    }
                    return tBlueDragonsState.USE_TUNNEL;
                }
                return tBlueDragonsState.TIMEOUT;
            }
            if (distance(new WorldPoint(2884, 3397, 0), 2))
                return tBlueDragonsState.CLIMB_DOWN;
            return tBlueDragonsState.WALK_TO_DUNGEON;
        }
        if (inv.containsItem(ItemID.VIAL)) {
            actionItem(ItemID.VIAL, "Drop");
            return tBlueDragonsState.TIMEOUT;
        }
        if (player.getWorldLocation().isInArea(blueDragons)) { // if we're in the dragon area
            return config.mode() == tBlueDragonsConfig.mode.KILL_DRAGONS ? getCombatStates() : getScaleStates();
        }
        return tBlueDragonsState.TIMEOUT;
    }

	tBlueDragonsState getScaleStates() {
		if (client.getBoostedSkillLevel(Skill.HITPOINTS) <= config.panicTele())
			return tBlueDragonsState.TELEPORT_OUT;
		if (config.useAntifire() && needsAntifire()) {
			if (hasAnyAntifirePots())
				return tBlueDragonsState.DRINK_ANTIFIRE;
			else
				return tBlueDragonsState.TELEPORT_OUT;
		}
		if (config.usePrayPot() && client.getBoostedSkillLevel(Skill.PRAYER) <= config.restoreAt()) {
			if (hasAnyPrayPots())
				return tBlueDragonsState.DRINK_PRAYER_POTS;
			else
				return tBlueDragonsState.TELEPORT_OUT;
		}
		if (config.useCombatPot() && needsRepot()) {
			if (hasAnyCombatPots())
				return tBlueDragonsState.DRINK_COMBAT_POT;
			else
				return tBlueDragonsState.TELEPORT_OUT;
		}
		if (config.useFood() && client.getBoostedSkillLevel(Skill.HITPOINTS) <= config.eatAt()) {
			if (hasAnyFood())
				return tBlueDragonsState.EAT_FOOD;
			else
				return tBlueDragonsState.TELEPORT_OUT;
		}
		if (inv.isFull())
			return tBlueDragonsState.TELEPORT_OUT;
		if (!loot.isEmpty())
			return tBlueDragonsState.LOOT_SCALES;
		return tBlueDragonsState.TIMEOUT;
	}

    tBlueDragonsState getCombatStates() {
        if (client.getBoostedSkillLevel(Skill.HITPOINTS) <= config.panicTele())
            return tBlueDragonsState.TELEPORT_OUT;
        if (!config.safespot()) {
            if (!loot.isEmpty())
                return tBlueDragonsState.LOOT_ITEMS;
        }
        if (target == null && !loot.isEmpty() && config.safespot())
            return tBlueDragonsState.LOOT_ITEMS;
        if (inv.isFull())
            return tBlueDragonsState.TELEPORT_OUT;
        if (config.safespot() && !player.getWorldLocation().equals(safeSpot))
            return tBlueDragonsState.SAFESPOT;
        if (config.useAntifire() && needsAntifire()) {
            if (hasAnyAntifirePots())
                return tBlueDragonsState.DRINK_ANTIFIRE;
            else
                return tBlueDragonsState.TELEPORT_OUT;
        }
        if (config.usePrayPot() && client.getBoostedSkillLevel(Skill.PRAYER) <= config.restoreAt()) {
            if (hasAnyPrayPots())
                return tBlueDragonsState.DRINK_PRAYER_POTS;
            else
                return tBlueDragonsState.TELEPORT_OUT;
        }
        if (config.useCombatPot() && needsRepot()) {
            if (hasAnyCombatPots())
                return tBlueDragonsState.DRINK_COMBAT_POT;
            else
                return tBlueDragonsState.TELEPORT_OUT;
        }
        if (config.useFood() && client.getBoostedSkillLevel(Skill.HITPOINTS) <= config.eatAt()) {
            if (hasAnyFood())
                return tBlueDragonsState.EAT_FOOD;
            else
                return tBlueDragonsState.TELEPORT_OUT;
        }
        if (player.getInteracting() == null) { // if player isn't in combat
            if (inv.isFull())
                return tBlueDragonsState.TELEPORT_OUT;
            if (target == null) {
                if (config.prayType() == tBlueDragonsConfig.PrayType.CUSTOM && pray.isActive(config.offPray().getPrayer())) {
                    toggle(config.offPray().getPrayer(), 0);
                }
                target = getNewTarget();
            }
            if (target != null && target.getAnimation() == 92)
                return tBlueDragonsState.HANDLE_PRAYER;
            return tBlueDragonsState.ATTACK_DRAGON;
        } else {
            // prayer shit here
            if (player.getInteracting() != target)
                target = (NPC) player.getInteracting();
            if (config.prayType() == tBlueDragonsConfig.PrayType.QUICK_PRAYERS || config.prayType() == tBlueDragonsConfig.PrayType.CUSTOM)
                return tBlueDragonsState.HANDLE_PRAYER;
        }
        return tBlueDragonsState.TIMEOUT;
    }

    tBlueDragonsState getBankStates() {
        if (inv.containsItem(List.of(ItemID.DRAGON_BONES, ItemID.BLUE_DRAGONHIDE, ItemID.BLUE_DRAGON_SCALE))) {
            bank.depositAll();
            return tBlueDragonsState.TIMEOUT;
        }
        Set<Integer> stam = Set.of(ItemID.STAMINA_POTION1, ItemID.STAMINA_POTION2, ItemID.STAMINA_POTION3, ItemID.STAMINA_POTION4);
        if (config.useStamina()) {
            if (!isStaminaBoosted()) {
                if (inv.getWidgetItem(stam) != null && inv.getWidgetItem(stam).getId() != -1) {
                    actionItem(inv.getWidgetItem(stam).getId(), "drink");
                    deposited = false;
                    return tBlueDragonsState.TIMEOUT;
                } else {
                    if (bank.containsAnyOf(stam)) {
                        bank.withdrawItem(bank.getBankItemWidgetAnyOf(stam));
                    } else {
                        configManager.setConfiguration("tBlueDragons", "stamina", false);
                    }
                    return tBlueDragonsState.TIMEOUT;
                }
            }
        }
        if (inv.containsItem(stam)) {
            if (!isStaminaBoosted()) {

            }
        }
        if (!inventoryReady()) {
            return tBlueDragonsState.WITHDRAW_ITEMS;
        } else {
            return tBlueDragonsState.CLOSE_BANK;
        }
    }

    boolean isStaminaBoosted()
    {
        return client.getVarbitValue(Varbits.RUN_SLOWED_DEPLETION_ACTIVE) == 1;
    }

    boolean needsRepot() {
        if (config.combatPot() == tBlueDragonsConfig.CombatPot.DIVINE_BASTION
                || config.combatPot() == tBlueDragonsConfig.CombatPot.BASTION
                || config.combatPot() == tBlueDragonsConfig.CombatPot.DIVINE_RANGING
                || config.combatPot() == tBlueDragonsConfig.CombatPot.RANGING) {
            return client.getBoostedSkillLevel(Skill.RANGED) <= config.boostRan();
        }
        if (config.combatPot() == tBlueDragonsConfig.CombatPot.DIVINE_MAGIC
                || config.combatPot() == tBlueDragonsConfig.CombatPot.MAGIC) {
            return client.getBoostedSkillLevel(Skill.MAGIC) <= config.boostMag();
        }
        if (config.combatPot() == tBlueDragonsConfig.CombatPot.DIVINE_SUPER_COMBAT
                || config.combatPot() == tBlueDragonsConfig.CombatPot.SUPER_COMBAT) {
            if (client.getBoostedSkillLevel(Skill.ATTACK) <= config.boostAtt())
                return true;
            if (client.getBoostedSkillLevel(Skill.STRENGTH) <= config.boostStr())
                return true;
            if (client.getBoostedSkillLevel(Skill.DEFENCE) <= config.boostDef())
                return true;
        }
        if (config.combatPot() == tBlueDragonsConfig.CombatPot.SUPER_ATT_STR
                || config.combatPot() == tBlueDragonsConfig.CombatPot.SUPER_ATT_STR_DEF) {
            if (client.getBoostedSkillLevel(Skill.ATTACK) <= config.boostAtt())
                return true;
            if (client.getBoostedSkillLevel(Skill.STRENGTH) <= config.boostStr())
                return true;
			return config.combatPot() == tBlueDragonsConfig.CombatPot.SUPER_ATT_STR_DEF
					&& client.getBoostedSkillLevel(Skill.DEFENCE) <= config.boostDef();
        }
        return false;
    }

    boolean inventoryReady() {
        if (config.useCombatPot()) {
            for (List<Integer> pots : config.combatPot().getCombatPots()) {
                if (inv.getItemCount(pots.get(0), false) != config.combatAmount())
                    return false;
            }
        }
        if (config.usePrayPot()) {
            if (inv.getItemCount(config.prayPot().getPrayerPot().get(0), false) != config.prayAmount())
                return false;
        }
        if (config.useAntifire()) {
            if (inv.getItemCount(config.antifirePot().getAntifirePot().get(0), false) != 1)
                return false;
        }
        if (config.useFood()) {
            if (inv.getItemCount(config.food().getFood().get(0), false) < config.foodAmount())
                return false;
        }
        if (config.useRunePouch()) {
            if (!inv.containsItem(ItemID.RUNE_POUCH) && !inv.containsItem(ItemID.RUNE_POUCH_L))
                return false;
        }
        if (selectedRunes()) {
            for (tBlueDragonsConfig.Runes rune : config.takeRunes()) {
                if (config.useRunePouch()) {
                    if (!inv.runePouchContains(rune.getID()) && !inv.containsItem(rune.getID())) {
                        log.info(rune + " runes not in inventory or pouch");
                        return false;
                    }
                } else {
                    if (!inv.containsItem(rune.getID())) {
                        log.info(rune + " runes not in inventory");
                        return false;
                    }
                }
            }
        }
        if (config.bankLoc() == tBlueDragonsConfig.Banking.FALADOR_TELETAB)
            return inv.getItemCount(ItemID.FALADOR_TELEPORT, false) != 0;
		return !config.useBonecrusher() || inv.containsItem(ItemID.BONECRUSHER);
	}

	void lootScales() {
		iGroundItem item = game.groundItems().withId(ItemID.BLUE_DRAGON_SCALE).nearestPath();
        if (item != null) {
            this.clientThread.invoke(() ->
                    this.client.invokeMenuAction("", "", item.id(), MenuAction.GROUND_ITEM_THIRD_OPTION.getId(), item.tileItem().getTile().getSceneLocation().getX(), item.tileItem().getTile().getSceneLocation().getY())
            );
        }
	}

    void lootItem(List<TileItem> itemList) {
        TileItem lootItem = this.getNearestTileItem(itemList);
        if (lootItem != null) {
            attempt = lootItem;
            if (config.safespot() && config.telegrabLoot()) {
                log.info("telegrab");
                targetMenu = new LegacyMenuEntry("", "", lootItem.getId(), MenuAction.WIDGET_TARGET_ON_GROUND_ITEM, lootItem.getTile().getSceneLocation().getX(), lootItem.getTile().getSceneLocation().getY(), false);
                utils.oneClickCastSpell(WidgetInfo.SPELL_TELEKINETIC_GRAB, targetMenu, 100);
                timeout = 5;
            } else {
                this.clientThread.invoke(() ->
                        this.client.invokeMenuAction("", "", lootItem.getId(), MenuAction.GROUND_ITEM_THIRD_OPTION.getId(), lootItem.getTile().getSceneLocation().getX(), lootItem.getTile().getSceneLocation().getY())
                );
            }
        } else {
            loot.remove(lootItem);
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

    NPC getNewTarget() {
        assert this.client.isClientThread();
        if (!config.safespot())
            return (new NPCQuery()).idEquals(265, 266, 267, 268, 269).filter(createValidTargetFilter()).result(this.client).nearestTo(this.client.getLocalPlayer());
        return (new NPCQuery()).idEquals(265).filter(createValidTargetFilter()).result(this.client).nearestTo(this.client.getLocalPlayer());
    }

    synchronized Predicate<NPC> createValidTargetFilter() {
        Predicate<NPC> filter = (NPC n) -> n.getWorldLocation().distanceToPath(client, player.getWorldLocation()) <= 15
                && (n.getInteracting() == null || n.getInteracting().equals(player))
                && !n.isDead();
        return filter;
    }

    void toggle(Prayer p) {
        toggle(p, sleepDelay());
    }

    void toggle(Prayer p, long sleepDelay) {
        Widget widget = client.getWidget(p.getWidgetInfo());
        targetMenu = new LegacyMenuEntry("", "", 1, MenuAction.CC_OP.getId(), -1, widget.getId(), false);
        utils.doInvokeMsTime(targetMenu, sleepDelay);
    }

    boolean needsAntifire() {
        int varbit = 0;
        if (config.antifirePot() == tBlueDragonsConfig.AntifirePot.SUPER_ANTIFIRE
                || config.antifirePot() == tBlueDragonsConfig.AntifirePot.EXT_SUPER_ANTIFIRE)
            varbit = 6101;
        else
            varbit = 3981;
        return client.getVarbitValue(varbit) == 0;
    }

    boolean hasAnyCombatPots() {
        for (List<Integer> pots : config.combatPot().getCombatPots()) {
            for (int pot : pots) {
                if (inv.getItemCount(pot, false) >= 1)
                    return true;
            }
            return false;
        }
        return true;
    }

    boolean hasAnyPrayPots() {
        for (int pot : config.prayPot().getPrayerPot()) {
            if (inv.getItemCount(pot, false) >= 1)
                return true;
        }
        return false;
    }

    boolean hasAnyAntifirePots() {
        for (int pot : config.antifirePot().getAntifirePot()) {
            if (inv.getItemCount(pot, false) >= 1)
                return true;
        }
        return false;
    }

    boolean hasAnyFood() {
        for (int foods : config.food().getFood()) {
            if (inv.getItemCount(foods, false) >= 1)
                return true;
        }
        return false;
    }

    boolean hasCombatPots() {
        for (List<Integer> pots : config.combatPot().getCombatPots()) {
            if (inv.getItemCount(pots.get(0), false) != config.combatAmount())
                return false;
        }
        return true;
    }

    boolean hasPrayPots() {
		return inv.getItemCount(config.prayPot().getPrayerPot().get(0), false) == config.prayAmount();
	}

    boolean hasAntifirePots() {
		return inv.getItemCount(config.antifirePot().getAntifirePot().get(0), false) == config.antifireAmount();
	}

    boolean hasFood() {
		return inv.getItemCount(config.food().getFood().get(0), false) >= config.foodAmount();
	}

    boolean distance(WorldPoint loc, int dist) {
        return player.getWorldLocation().distanceTo(loc) <= dist;
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
        return actionItem(id, (int) sleepDelay(), action);
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
        return actionObject(id, action, (int) sleepDelay());
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
        return actionNPC(id, action, (int) sleepDelay());
    }

    private boolean actionNPC(NPC npc, MenuAction action, int delay) {
        NPC target = npc;
        if (target != null) {
            targetMenu = new LegacyMenuEntry("", "", target.getIndex(), action, target.getIndex(), 0, false);
            utils.doInvokeMsTime(targetMenu, delay);
            return true;
        }
        return false;
    }

    private boolean actionNPC(NPC npc, MenuAction action) {
        return actionNPC(npc, action, (int) sleepDelay());
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