package net.runelite.client.plugins.automedclue;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.tutils.*;
import net.runelite.client.plugins.tutils.api.EquipmentSlot;
import net.runelite.client.plugins.tutils.game.Game;
import net.runelite.client.plugins.tutils.ui.Chatbox;
import net.runelite.client.plugins.tutils.ui.Equipment;
import net.runelite.client.plugins.tutils.util.LegacyInventoryAssistant;
import net.runelite.client.plugins.tutils.walking.Walking;
import net.runelite.client.ui.overlay.OverlayManager;
import org.pf4j.Extension;

import javax.inject.Inject;
import java.awt.*;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutorService;

@Extension
@PluginDependency(tUtils.class)
@PluginDescriptor(
        name = "CS-MediumClues",
        description = "Ranger bewts",
        enabledByDefault = false,
        tags = {"Tea", "fw", "clue", "med"}
)
@Slf4j
public class AutoMedClue extends Plugin {
    @Inject
    AutoClueOverlay overlay;
    @Inject
    AutoClueConfig config;
    Clues med;
    LegacyMenuEntry targetMenu;
    AutoClueState state;
    AutoClueState lastState;
    boolean startPlugin;
    Instant botTimer;
    int timeout;
    /*
    Teleports
     */
    Set<Integer> duelingRing;
    Set<Integer> gamesNecklace;
    Set<Integer> passageNecklace;
    Set<Integer> gloryAmulet;
    Set<Integer> skillsNecklace;
    WorldPoint burthorpeTeleport;
    WorldPoint faladorTeleport;
    WorldPoint camelotTeleport;
    WorldPoint arceuusTeleport;
    WorldPoint brimhavenTeleport;
    WorldPoint draynorTeleport;
    WorldPoint karamjaTeleport;
    WorldPoint lumbridgeTeleport;
    WorldPoint varrockTeleport;
    WorldPoint craftingGuildTeleport;
    WorldPoint fishingGuildTeleport;
    WorldPoint farmingGuildTeleport;
    WorldPoint woodcuttingGuildTeleport;
    WorldPoint salveGraveyardTeleport;
    WorldPoint castleWarsTeleport;
    WorldPoint duelArenaTeleport;
    WorldPoint salveGraveyardFairyRing;
    WorldPoint outpostTeleport;
    WorldPoint wizardsTowerTeleport;
    WorldPoint barbarianOutpostTeleport;
    WorldPoint westArdougneTeleport;
    WorldPoint blrFairyRing;
    WorldPoint alpFairyRing;
    WorldPoint ciqFairyRing;
    WorldPoint ckrFairyRing;
    WorldPoint dlqFairyRing;
    WorldPoint djrFairyRing;
    WorldPoint alsFairyRing;
    WorldPoint blpFairyRing;
    WorldPoint ajrFairyRing;
    WorldPoint djpFairyRing;
    WorldPoint akqFairyRing;
    WorldPoint aksFairyRing;
    WorldPoint cipFairyRing;
    /*
    Clue related
     */
    int clue;
    int lastClue;
    int stage;
    WorldPoint targetLoc;
    int rand;
    List<Integer> emotesToDo;
    boolean song;
    boolean teleported;
    Set<Integer> clues;
    List<TileItem> groundItems;
    List<NPC> validNPCs;
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
    private Walking walking;
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
    private PlayerUtils playerUtils;
    private Player player;
    private Rectangle bounds;

    public AutoMedClue() {
        botTimer = null;
        startPlugin = false;
        state = AutoClueState.TIMEOUT;
        lastState = state;
    }

    @Provides
    AutoClueConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(AutoClueConfig.class);
    }


    private void reset() {
        timeout = 0;
        startPlugin = false;
        botTimer = null;
        state = AutoClueState.TIMEOUT;
        lastState = state;
        overlayManager.remove(overlay);
    }

    @Override
    protected void startUp() {
    }

    @Override
    protected void shutDown() {
        reset();
    }

    @Subscribe
    private void onConfigButtonPressed(ConfigButtonClicked configButtonClicked) throws IOException, InterruptedException {
        if (!configButtonClicked.getGroup().equalsIgnoreCase("AutoMedClue")) {
            return;
        }
        switch (configButtonClicked.getKey()) {
            case "startPlugin":
                if (!startPlugin) {
                    player = client.getLocalPlayer();
                    if (player != null && client != null) {
                        startPlugin = true;
                        botTimer = Instant.now();
                        state = AutoClueState.TIMEOUT;
                        overlayManager.add(overlay);
                        clue = -1;
                        clues = Clues.scrolls;
                        groundItems = new ArrayList<>();
                        validNPCs = new ArrayList<>();
                        /*
                        Teleports
                         */
                        duelingRing = Set.of(ItemID.RING_OF_DUELING1, ItemID.RING_OF_DUELING2, ItemID.RING_OF_DUELING3, ItemID.RING_OF_DUELING4, ItemID.RING_OF_DUELING5, ItemID.RING_OF_DUELING6, ItemID.RING_OF_DUELING7, ItemID.RING_OF_DUELING8);
                        gamesNecklace = Set.of(ItemID.GAMES_NECKLACE1, ItemID.GAMES_NECKLACE2, ItemID.GAMES_NECKLACE3, ItemID.GAMES_NECKLACE4, ItemID.GAMES_NECKLACE5, ItemID.GAMES_NECKLACE6, ItemID.GAMES_NECKLACE7, ItemID.GAMES_NECKLACE8);
                        passageNecklace = Set.of(ItemID.NECKLACE_OF_PASSAGE1, ItemID.NECKLACE_OF_PASSAGE2, ItemID.NECKLACE_OF_PASSAGE3, ItemID.NECKLACE_OF_PASSAGE4, ItemID.NECKLACE_OF_PASSAGE5);
                        gloryAmulet = Set.of(ItemID.AMULET_OF_ETERNAL_GLORY, ItemID.AMULET_OF_GLORY1, ItemID.AMULET_OF_GLORY2, ItemID.AMULET_OF_GLORY3, ItemID.AMULET_OF_GLORY4, ItemID.AMULET_OF_GLORY5, ItemID.AMULET_OF_GLORY6, ItemID.AMULET_OF_GLORY);
                        skillsNecklace = Set.of(ItemID.SKILLS_NECKLACE6, ItemID.SKILLS_NECKLACE5, ItemID.SKILLS_NECKLACE4, ItemID.SKILLS_NECKLACE3, ItemID.SKILLS_NECKLACE2, ItemID.SKILLS_NECKLACE1);

                        salveGraveyardFairyRing = new WorldPoint(3446, 3470, 0);
                        blrFairyRing = new WorldPoint(2740, 3351, 0);
                        alpFairyRing = new WorldPoint(2503, 3636, 0);
                        ciqFairyRing = new WorldPoint(2528, 3127, 0);
                        ckrFairyRing = new WorldPoint(2801, 3003, 0);
                        dlqFairyRing = new WorldPoint(3423, 3016, 0);
                        djrFairyRing = new WorldPoint(1455, 3658, 0);
                        alsFairyRing = new WorldPoint(2644, 3495, 0);
                        blpFairyRing = new WorldPoint(2437, 5126, 0);
                        ajrFairyRing = new WorldPoint(2780, 3613, 0);
                        djpFairyRing = new WorldPoint(2658, 3230, 0);
                        akqFairyRing = new WorldPoint(2319, 3619, 0);
                        aksFairyRing = new WorldPoint(2571, 2956, 0);
                        cipFairyRing = new WorldPoint(2513, 3884, 0);

                        burthorpeTeleport = new WorldPoint(2898, 3552, 0);
                        draynorTeleport = new WorldPoint(3105, 3251, 0);
                        karamjaTeleport = new WorldPoint(2918, 3176, 0);
                        craftingGuildTeleport = new WorldPoint(2934, 3295, 0);
                        fishingGuildTeleport = new WorldPoint(2612, 3390, 0);
                        farmingGuildTeleport = new WorldPoint(1248, 3725, 0);
                        woodcuttingGuildTeleport = new WorldPoint(1662, 3505, 0);
                        castleWarsTeleport = new WorldPoint(2440, 3092, 0);
                        duelArenaTeleport = new WorldPoint(3317, 3235, 0);
                        outpostTeleport = new WorldPoint(2428, 3348, 0);
                        wizardsTowerTeleport = new WorldPoint(3113, 3178, 0);
                        barbarianOutpostTeleport = new WorldPoint(2519, 3570, 0);

                        faladorTeleport = new WorldPoint(2965, 3382, 0);
                        camelotTeleport = new WorldPoint(2757, 3478, 0);
                        lumbridgeTeleport = new WorldPoint(3222, 3218, 0);
                        varrockTeleport = new WorldPoint(3213, 3424, 0);
                        brimhavenTeleport = new WorldPoint(2757, 3178, 0);

                        arceuusTeleport = new WorldPoint(1633, 3837, 0);
                        salveGraveyardTeleport = new WorldPoint(3433, 3461, 0);
                        westArdougneTeleport = new WorldPoint(2500, 3291, 0);

                        teleported = false;
                        targetLoc = null;
                        rand = 0;

                        emotesToDo = new ArrayList();
                        song = false;

                        stage = 0;
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
        log.info("Type: " + event.getType().toString() + " -> " + event.getMessage());
        if (event.getType() == ChatMessageType.GAMEMESSAGE
                && (event.getMessage().contains("Your items cannot be stored in the bank.")
                || event.getMessage().contains("Your Magic level is not high enough for this spell.")
                || event.getMessage().contains("You have not unlocked this piece of music yet!"))) {
            shutDown();
        }
    }

    @Subscribe
    private void on(ItemSpawned event) {
        if (!startPlugin)
            return;
        // String name = client.getItemDefinition(item.getId()).getName().toLowerCase();
        if (!groundItems.contains(event.getItem()) && client.getItemDefinition(event.getItem().getId()).getName().contains("medium")) {
            log.info("added ground item");
            groundItems.add(event.getItem());
        }
    }

    @Subscribe
    private void on(ItemDespawned event) {
        if (!startPlugin)
            return;
        if (groundItems.contains(event.getItem())) {
            log.info("removed ground item");
            groundItems.remove(event.getItem());
        }
    }

    @Subscribe
    private void onGameTick(GameTick event) {
        if (!startPlugin)
            return;
        player = client.getLocalPlayer();
        if (player != null && client != null) {
            if (inv.containsItem(clues))
                clue = inv.getWidgetItem(clues).getId();
            state = getState();
            if (config.debug() && state != lastState && state != AutoClueState.TIMEOUT) {
                utils.sendGameMessage(this.getClass().getSimpleName() + ": " + state.toString());
            }
            if (state != AutoClueState.TIMEOUT)
                lastState = state;
            Widget item;
            if (stage == 6) {
                if (hasStashItemsEquipped(lastClue)) {
                    ItemContainer equipmentContainer = client.getItemContainer(InventoryID.EQUIPMENT);
                    if (equipmentContainer != null) {
                        Item[] items = equipmentContainer.getItems();
                        for(EquipmentSlot slot : EquipmentSlot.values()) {
                            for (int i : getClueItems(lastClue)) {
                                if (equip.slot(slot).getId() == i) {
                                    targetMenu = new LegacyMenuEntry("", "", 1, MenuAction.CC_OP, -1, client.getWidget(slot.widgetID, slot.widgetChild).getId(), false);
                                    utils.doInvokeMsTime(targetMenu, sleepDelay());
                                }
                            }
                        }
                    }
                } else if (inv.containsAllOf(getClueItems(lastClue))) {
                    actionObject(getStashID(lastClue), MenuAction.GAME_OBJECT_SECOND_OPTION);
                    timeout = 3 + tickDelay();
                    stage = 0;
                } else {
                    stage = 0;
                }
                return;
            }

            switch (state) {
                case SOLVING_CLUE:
                case TIMEOUT:
                    if (timeout <= 0)
                        timeout = 0;
                    else
                        timeout--;
                    break;
                case OPEN_BANK:
                    GameObject booth = objectUtils.findNearestBank();
                    if (booth != null && booth.getId() == 4483 && booth.getWorldLocation().equals(new WorldPoint(2444, 3083, 0))) {
                        actionObject(booth.getId(), booth.getId() == 4483 ? MenuAction.GAME_OBJECT_FIRST_OPTION : MenuAction.GAME_OBJECT_SECOND_OPTION);
                        teleported = false;
                    } else {
                        teleport(CluesTele.CASTLE_WARS);
                        break;
                    }
                    timeout = 3;
                    break;
                case WITHDRAW_JARS:
                    Widget jars = bank.getBankItemWidget(ItemID.ECLECTIC_IMPLING_JAR);
                    if (jars != null) {
                        bank.withdrawItemAmount(ItemID.ECLECTIC_IMPLING_JAR, 14);
                    } else {
                        utils.sendGameMessage("No eclectic jars.");
                        shutDown();
                        break;
                    }
                    timeout = 2 + tickDelay();
                    break;
                case OPEN_JAR:
                    if (bank.isOpen()) {
                        bank.close();
                        return;
                    }
                    actionItem(ItemID.ECLECTIC_IMPLING_JAR, "loot");
                    teleported = false;
                    break;
                case DEPOSIT_ALL:
                    bank.depositAll();
                    timeout = 1 + tickDelay();
                    break;
                case SETUP_INVENT:
                    if (!inv.containsItem(gloryAmulet)) {
                        bank.withdrawItem(bank.contains(ItemID.AMULET_OF_ETERNAL_GLORY, 1) ? ItemID.AMULET_OF_ETERNAL_GLORY :
                                (bank.contains(ItemID.AMULET_OF_GLORY6, 1) ? ItemID.AMULET_OF_GLORY6 :
                                        (bank.contains(ItemID.AMULET_OF_GLORY5, 1) ? ItemID.AMULET_OF_GLORY5 : ItemID.AMULET_OF_GLORY4)));
                        break;
                    }
                    if (!inv.containsItem(duelingRing)) {
                        bank.withdrawItem(ItemID.RING_OF_DUELING8);
                        break;
                    }
                    if (!inv.containsItem(gamesNecklace)) {
                        bank.withdrawItem(ItemID.GAMES_NECKLACE8);
                        break;
                    }
                    if (!inv.containsItem(passageNecklace)) {
                        bank.withdrawItem(ItemID.NECKLACE_OF_PASSAGE5);
                        break;
                    }
                    if (!inv.containsItem(skillsNecklace)) {
                        bank.withdrawItem(ItemID.SKILLS_NECKLACE6);
                        break;
                    }
                    if (!inv.containsItem(ItemID.WEST_ARDOUGNE_TELEPORT)) {
                        bank.withdrawAllItem(ItemID.WEST_ARDOUGNE_TELEPORT);
                        break;
                    }
                    if (!inv.containsItem(ItemID.ARCEUUS_LIBRARY_TELEPORT)) {
                        bank.withdrawAllItem(ItemID.ARCEUUS_LIBRARY_TELEPORT);
                        break;
                    }
                    if (!inv.containsItem(ItemID.SALVE_GRAVEYARD_TELEPORT)) {
                        bank.withdrawAllItem(ItemID.SALVE_GRAVEYARD_TELEPORT);
                        break;
                    }
                    if (!inv.containsItem(ItemID.SPADE)) {
                        bank.withdrawItem(ItemID.SPADE);
                        break;
                    }
                    if (config.useRunePouch() && !inv.containsItem(ItemID.RUNE_POUCH)) {
                        bank.withdrawItem(ItemID.RUNE_POUCH);
                        break;
                    }
                    if (config.useRunePouch() && (!inv.runePouchContains(ItemID.LAW_RUNE)
                            || !inv.runePouchContains(ItemID.MIST_RUNE)
                            || !inv.runePouchContains(ItemID.LAVA_RUNE))) {
                        utils.sendGameMessage("Your rune pouch needs to have LAW, MIST and LAVA runes.");
                        shutDown();
                        break;
                    }
                    if (!config.useRunePouch()) {
                        if (!inv.containsItem(ItemID.LAW_RUNE)) {
                            bank.withdrawAllItem(ItemID.LAW_RUNE);
                            break;
                        }
                        if (config.useCombinationRunes()) {
                            if (!inv.containsItem(ItemID.MIST_RUNE)) {
                                bank.withdrawAllItem(ItemID.MIST_RUNE);
                                break;
                            }
                            if (!inv.containsItem(ItemID.LAVA_RUNE)) {
                                bank.withdrawAllItem(ItemID.LAVA_RUNE);
                                break;
                            }
                        } else {
                            if (!inv.containsItem(ItemID.AIR_RUNE)) {
                                bank.withdrawAllItem(ItemID.AIR_RUNE);
                                break;
                            }
                            if (!inv.containsItem(ItemID.WATER_RUNE)) {
                                bank.withdrawAllItem(ItemID.WATER_RUNE);
                                break;
                            }
                            if (!inv.containsItem(ItemID.EARTH_RUNE)) {
                                bank.withdrawAllItem(ItemID.EARTH_RUNE);
                                break;
                            }
                            if (!inv.containsItem(ItemID.FIRE_RUNE)) {
                                bank.withdrawAllItem(ItemID.FIRE_RUNE);
                                break;
                            }
                        }
                    }
                    if (!inv.containsItem(ItemID.DRAMEN_STAFF) && !equip.isEquipped(ItemID.DRAMEN_STAFF)) {
                        bank.withdrawItem(ItemID.DRAMEN_STAFF);
                        break;
                    }
                    if (!inv.containsItem(clues)) {
                        item = bank.getBankItemWidgetAnyOf(clues);
                        if (item != null) {
                            bank.withdrawItem(item.getItemId());
                        } else {
                            clue = -1;
                        }
                        break;
                    }
                    if (!inv.containsItem(config.weaponID()) && !equip.isEquipped(config.weaponID())) {
                        bank.withdrawItem(config.weaponID());
                        break;
                    }
                    if (config.useFood() && inv.getItemCount(config.food().getFood(), false) <= 4) {
                        bank.withdrawItemAmount(config.food().getFood(), 10);
                        break;
                    }
                    if (clue == 7303 && !inv.containsItem(ItemID.METAL_KEY)) {
                        bank.withdrawItem(ItemID.METAL_KEY);
                        break;
                    }
                    log.info("Inventory setup is complete");
                    break;
                case EAT_FOOD:
                    actionItem(config.food().getFood(), "eat", "consume");
                    break;
                case EMOTES:
                    if (emotesToDo != null && !emotesToDo.isEmpty()) {
                        utils.sendGameMessage(emotesToDo.toString());
                        int e = emotesToDo.get(0);
                        targetMenu = new LegacyMenuEntry("", "", 1, MenuAction.CC_OP, e, WidgetInfo.EMOTE_CONTAINER.getId(), false);
                        utils.doInvokeMsTime(targetMenu, sleepDelay());
                        emotesToDo.remove(emotesToDo.get(0));
                        timeout = 3 + tickDelay();
                    }
                    if (emotesToDo == null || emotesToDo.isEmpty())
                        stage ++;
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
        if (Objects.equals(npc.getName(), "Uri") && !validNPCs.contains(event.getNpc())) {
            validNPCs.add(event.getNpc());
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
        validNPCs.remove(event.getNpc());
    }

    @Subscribe
    private void onAnimationChanged(AnimationChanged event) {
        if (!startPlugin)
            return;
        Actor actor = event.getActor();
    }

    @Subscribe
    private void on(ItemContainerChanged event) {
        if (!startPlugin)
            return;
        if (event.getContainerId() == WidgetInfo.INVENTORY.getId()) {
            if (stage == 5)
                stage = 6;
        }
    }

    AutoClueState getClueState() {
        WorldPoint digSpot;
        WallObject door;
        NPC target;
        Widget widget;
        /*if (getStashID(clue) != -1) {
            actionItem(clue, "drop");
            return AutoClueState.SOLVING_CLUE;
        }*/
        switch (clue) {
            case 3586: {
                // 11deg    41n
                // 14deg    58e
                // burthorpe pub garden
                digSpot = new WorldPoint(2920, 3534, 0);
                if (player.getWorldLocation().equals(digSpot)) {
                    dig();
                } else if (player.getWorldLocation().equals(new WorldPoint(2915, 3537, 0))) {
                    // open door INSIDE pub
                    if (!handleDoor(1540, new WorldPoint(2916, 3537, 0), MenuAction.GAME_OBJECT_FIRST_OPTION)) {
                        walk(digSpot, 0, sleepDelay());
                    }
                    break;
                } else if (player.getWorldLocation().equals(new WorldPoint(2907, 3544, 0))) {
                    // open door OUTSIDE pub
                    if (!handleDoor(1540, new WorldPoint(2907, 3544, 0), MenuAction.GAME_OBJECT_FIRST_OPTION)) {
                        walk(new WorldPoint(2915, 3537, 0), 0, sleepDelay());
                    }
                    break;
                } else if (distance(burthorpeTeleport) <= 7) {
                    walk(new WorldPoint(2907, 3544, 0), 0, sleepDelay());
                    break;
                } else {
                    teleport(CluesTele.BURTHORPE);
                }
                break;
            }
            case 2856: {
                if (!teleported) {
                    teleport(CluesTele.FALADOR);
                } else if (targetLoc == null) {
                    if (distance(faladorTeleport) <= 5) {
                        w(3045, 3371, 1);
                    } else if (distance(3045, 3371) <= 1) {
                        if (!handleDoor(24059, new WorldPoint(3045, 3371, 0), MenuAction.GAME_OBJECT_FIRST_OPTION)) {
                            actionNPC(NpcID.PARTY_PETE, MenuAction.NPC_FIRST_OPTION);
                            timeout = 2 + tickDelay();
                        }
                    }
                }
                break;
            }
            case 19752: {
                // Flax keeper - south of seers
                if (canType(676)) {
                    break;
                } else if (widgetOpen(193, 0)) {
                    actionNPC(NpcID.FLAX_KEEPER, MenuAction.NPC_FIRST_OPTION);
                    timeout = 2 + tickDelay();
                } else if (distance(camelotTeleport) <= 5) {
                    walk(new WorldPoint(2744, 3445, 0), 4, sleepDelay());
                } else if (distance(new WorldPoint(2744, 3445, 0)) <= 4) {
                    actionNPC(NpcID.FLAX_KEEPER, MenuAction.NPC_FIRST_OPTION);
                    timeout = 2 + tickDelay();
                } else {
                    teleport(CluesTele.CAMELOT);
                    break;
                }
                break;
            }
            case 19762: {
                if (canType(9)) {
                    break;
                } else if (widgetOpen(193, 0)) {
                    actionNPC(NpcID.PROFESSOR_GRACKLEBONE, MenuAction.NPC_FIRST_OPTION);
                    timeout = 2 + tickDelay();
                } else if (distance(arceuusTeleport) <= 7) {
                    walk(new WorldPoint(1633, 3820, 0), 2, sleepDelay());
                } else if (distance(new WorldPoint(1625, 3807, 0)) <= 3) {
                    actionNPC(NpcID.PROFESSOR_GRACKLEBONE, MenuAction.NPC_FIRST_OPTION);
                } else if (distance(new WorldPoint(1633, 3820, 0)) <= 3) {
                    if (handleDoor(28460, new WorldPoint(1633, 3817, 0), MenuAction.GAME_OBJECT_FIRST_OPTION)) {
                        walk(new WorldPoint(1625, 3807, 0), 2, sleepDelay());
                    }
                } else {
                    teleport(CluesTele.ARCEUUS_LIBRARY);
                }
                break;
            }
            case 2821: {
                if (distance(faladorTeleport) <= 7) {
                    walk(new WorldPoint(2965, 3413, 0), 4, sleepDelay());
                } else if (distance(new WorldPoint(2965, 3413, 0)) <= 4) {
                    walk(new WorldPoint(2947, 3427, 0), 4, sleepDelay());
                } else if (distance(new WorldPoint(2947, 3427, 0)) <= 4) {
                    walk(new WorldPoint(2938, 3450, 0), 4, sleepDelay());
                } else if (distance(new WorldPoint(2938, 3450, 0)) <= 4) {
                    if (!handleDoor(1728, new WorldPoint(2935, 3450, 0), MenuAction.GAME_OBJECT_FIRST_OPTION)) {
                        walk(new WorldPoint(2920, 3403, 0), 1, sleepDelay());
                    }
                } else if (distance(new WorldPoint(2920, 3403, 0)) <= 1) {
                    dig();
                } else {
                    teleport(CluesTele.FALADOR);
                }
                break;
            }
            case 3605: {
                if (inv.containsItem(3606)) {
                    if (distance(new WorldPoint(2806, 3162, 1)) <= 4) {
                        actionObject(348, MenuAction.GAME_OBJECT_FIRST_OPTION);
                        timeout = 1 + tickDelay();
                    } else if (distance(new WorldPoint(2806, 3163, 0)) <= 0) {
                        if (handleDoor(1540, new WorldPoint(2807, 3163, 0), MenuAction.GAME_OBJECT_FIRST_OPTION)) {

                        } else {
                            GameObject obj = objectUtils.findNearestGameObject(16683);
                            if (obj != null) {
                                if (obj.getWorldLocation().equals(new WorldPoint(2808, 3161, 0))) {
                                    targetMenu = new LegacyMenuEntry("", "", obj.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION, obj.getSceneMinLocation().getX(), obj.getSceneMinLocation().getY(), false);
                                    if (!config.invokes())
                                        utils.doGameObjectActionMsTime(obj, MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), sleepDelay());
                                    else
                                        utils.doInvokeMsTime(targetMenu, sleepDelay());
                                }
                            }
                        }
                    } else if (distance(new WorldPoint(2802, 3170, 0)) <= 3) {
                        walk(new WorldPoint(2806, 3163, 0), 0, sleepDelay());
                    } else {
                        walk(new WorldPoint(2802, 3170, 0), 3, sleepDelay());
                    }
                } else if (distance(brimhavenTeleport) <= 16) {
                    killAndLoot(522);
                } else {
                    teleport(CluesTele.HOUSE);
                }
                break;
            }
            case 7315: {
                if (distance(new WorldPoint(2735, 3638, 0)) <= 1) {
                    dig();
                } else if (distance(new WorldPoint(2780, 3613, 0)) <= 2) {
                    walk(new WorldPoint(2735, 3638, 0), 1, sleepDelay());
                } else if (distance(salveGraveyardFairyRing) <= 1) {
                    if (widgetOpen(162, 37)) {
                        fairyRing("a", "j", "r");
                    }
                } else if (distance(salveGraveyardTeleport) <= 4) {
                    if (equip.isEquipped(ItemID.DRAMEN_STAFF))
                        actionObject(29495, MenuAction.GAME_OBJECT_SECOND_OPTION);
                    else {
                        actionItem(ItemID.DRAMEN_STAFF, "wield");
                    }
                } else {
                    teleport(CluesTele.SALVE_GRAVEYARD);
                }
                break;
            }
            case 3596: {
                if (distance(new WorldPoint(2907, 3295, 0)) <= 1) {
                    dig();
                } else if (distance(craftingGuildTeleport) <= 5) {
                    walk(new WorldPoint(2907, 3295, 0), 1, sleepDelay());
                } else {
                    teleport(CluesTele.CRAFTING_GUILD);
                }
                break;
            }
            case 2841: {
                if (distance(new WorldPoint(2677, 3088, 1)) <= 2) {
                    if (!canType(6859)) {
                        actionNPC(NpcID.HAZELMERE, MenuAction.NPC_FIRST_OPTION);
                    }
                } else if (distance(new WorldPoint(2682, 3081, 0)) <= 1
                        || distance(new WorldPoint(2677, 3089, 0)) <= 1) {
                    if (handleDoor(1543, new WorldPoint(2677, 3088, 0), MenuAction.GAME_OBJECT_FIRST_OPTION)) {
                        actionObject(16683, MenuAction.GAME_OBJECT_FIRST_OPTION);
                    }
                } else if (distance(salveGraveyardFairyRing) <= 1) {
                    if (widgetOpen(162, 37)) {
                        fairyRing("c", "l", "s");
                    }
                } else if (distance(salveGraveyardTeleport) <= 4) {
                    if (equip.isEquipped(ItemID.DRAMEN_STAFF))
                        actionObject(29495, MenuAction.GAME_OBJECT_SECOND_OPTION);
                    else {
                        actionItem(ItemID.DRAMEN_STAFF, "wield");
                    }
                } else {
                    teleport(CluesTele.SALVE_GRAVEYARD);
                }
                break;
            }
            case 12043: {
                if (distance(new WorldPoint(3121, 3384, 0)) <= 1) {
                    dig();
                } else if (distance(new WorldPoint(3132, 3375, 0)) <= 6) {
                    walk(new WorldPoint(3121, 3384, 0), 1, sleepDelay());
                } else if (distance(new WorldPoint(3130, 3336, 0)) <= 6) {
                    walk(new WorldPoint(3132, 3375, 0), 2, sleepDelay());
                } else if (distance(new WorldPoint(3109, 3295, 0)) <= 6) {
                    walk(new WorldPoint(3130, 3336, 0), 2, sleepDelay());
                } else if (distance(draynorTeleport) <= 5) {
                    walk(new WorldPoint(3109, 3295, 0), 2, sleepDelay());
                } else {
                    teleport(CluesTele.DRAYNOR_VILLAGE);
                }
            }
            case 7303: {
                if (distance(new WorldPoint(3274, 3029, 0)) <= 2) {
                    actionObject(18889, new WorldPoint(3289, 3022, 0), MenuAction.GAME_OBJECT_FIRST_OPTION);
                } else if (distance(new WorldPoint(3257, 3051, 0)) <= 5) {
                    if (equip.isEquipped(config.weaponID()) || equip.isEquipped(ItemID.DRAMEN_STAFF) && widgetOpen(387, 18)) {
                        targetMenu = new LegacyMenuEntry("", "", 1, MenuAction.CC_OP, -1, client.getWidget(387, 18).getId(), false);
                        if (config.invokes()) {
                            utils.doInvokeMsTime(targetMenu, sleepDelay());
                        } else {
                            utils.doActionMsTime(targetMenu, client.getWidget(387, 18).getBounds(), sleepDelay());
                        }
                    } else {
                        if (handleDoor(2673, new WorldPoint(3273, 3029, 0), MenuAction.GAME_OBJECT_FIRST_OPTION)) {

                        }
                    }
                } else if (distance(new WorldPoint(3251, 3095, 0)) <= 1) {
                    walk(new WorldPoint(3257, 3051, 0), 5, sleepDelay());
                } else if (distance(salveGraveyardFairyRing) <= 1) {
                    if (widgetOpen(162, 37)) {
                        fairyRing("b", "i", "q");
                    }
                } else if (distance(salveGraveyardTeleport) <= 4) {
                    if (equip.isEquipped(ItemID.DRAMEN_STAFF))
                        actionObject(29495, MenuAction.GAME_OBJECT_SECOND_OPTION);
                    else {
                        actionItem(ItemID.DRAMEN_STAFF, "wield");
                    }
                } else {
                    teleport(CluesTele.SALVE_GRAVEYARD);
                }
                break;
            }
            case 19768: {
                if (distance(new WorldPoint(3354, 2975, 0)) <= 4) {
                    if (!canType(399)) {
                        actionNPC(3536, MenuAction.NPC_FIRST_OPTION);
                    }
                } else if (distance(new WorldPoint(3356, 2987, 0)) <= 2) {
                    actionNPC(3536, MenuAction.NPC_FIRST_OPTION);
                } else if (distance(new WorldPoint(3335, 3014, 0)) <= 2) {
                    walk(new WorldPoint(3356, 2987, 0), 1, sleepDelay());
                } else if (distance(new WorldPoint(3323, 3027, 0)) <= 4) {
                    walk(new WorldPoint(3335, 3014, 0), 2, sleepDelay());
                } else if (distance(new WorldPoint(3297, 3050, 0)) <= 4) {
                    walk(new WorldPoint(3323, 3027, 0), 4, sleepDelay());
                } else if (distance(new WorldPoint(3283, 3066, 0)) <= 4) {
                    walk(new WorldPoint(3297, 3050, 0), 3, sleepDelay());
                } else if (distance(new WorldPoint(3251, 3095, 0)) <= 1) {
                    walk(new WorldPoint(3283, 3066, 0), 4, sleepDelay());
                } else if (distance(salveGraveyardFairyRing) <= 1) {
                    if (widgetOpen(162, 37)) {
                        fairyRing("b", "i", "q");
                    }
                } else if (distance(salveGraveyardTeleport) <= 4) {
                    if (equip.isEquipped(ItemID.DRAMEN_STAFF))
                        actionObject(29495, MenuAction.GAME_OBJECT_SECOND_OPTION);
                    else {
                        actionItem(ItemID.DRAMEN_STAFF, "wield");
                    }
                } else {
                    teleport(CluesTele.SALVE_GRAVEYARD);
                }
                break;
            }
            case 2843: {
                if (distance(new WorldPoint(3208, 3215, 0)) <= 4) {
                    if (!canType(9)) {
                        actionNPC(NpcID.COOK_4626, MenuAction.NPC_FIRST_OPTION);
                    }
                } else if (distance(lumbridgeTeleport) <= 3) {
                    actionNPC(NpcID.COOK_4626, MenuAction.NPC_FIRST_OPTION);
                } else {
                    teleport(CluesTele.LUMBRIDGE);
                }
                break;
            }
            case 2813: {
                if (distance(new WorldPoint(2643, 3252, 0)) <= 1) {
                    dig();
                } else if (distance(djpFairyRing) <= 1) {
                    walk(new WorldPoint(2643, 3252, 0), 1, sleepDelay());
                } else if (distance(salveGraveyardFairyRing) <= 1) {
                    if (widgetOpen(162, 37)) {
                        fairyRing("d", "j", "p");
                    }
                } else if (distance(salveGraveyardTeleport) <= 4) {
                    if (equip.isEquipped(ItemID.DRAMEN_STAFF))
                        actionObject(29495, MenuAction.GAME_OBJECT_SECOND_OPTION);
                    else {
                        actionItem(ItemID.DRAMEN_STAFF, "wield");
                    }
                } else {
                    teleport(CluesTele.SALVE_GRAVEYARD);
                }
                break;
            }
            case 10262: {
                // actionObject(28991, MenuAction.GAME_OBJECT_SECOND_OPTION);
                if (distance(new WorldPoint(2440, 3092, 0)) <= 0) {
                    if (!validNPCs.isEmpty()) {
                        target = validNPCs.stream().findFirst().get();
                        actionNPC(target.getId(), MenuAction.NPC_FIRST_OPTION);
                    }
                    timeout = 6 + tickDelay();
                    // add emotes
                    lastClue = clue;
                } else if (distance(castleWarsTeleport) <= 10) {
                    if (!hasStashItemsEquipped(clue)) {
                        if (inv.containsAllOf(getClueItems(clue))) {
                            for (int i : getClueItems(clue)) {
                                WidgetItem item = inv.getWidgetItem(i);
                                targetMenu = inventoryAssistant.getLegacyMenuEntry(item.getId(), "wear", "wield", "equip");
                                utils.doInvokeMsTime(targetMenu, 0);
                            }
                        } else {
                            actionObject(getStashID(clue), MenuAction.GAME_OBJECT_SECOND_OPTION);
                            timeout = 3 + tickDelay();
                        }
                    } else {
                        walk(new WorldPoint(2440, 3092, 0), 0, sleepDelay());
                    }
                } else {
                    teleport(CluesTele.CASTLE_WARS);
                }
                break;
            }
            case 10254: {
                if (distance(new WorldPoint(3491, 3488, 0)) <= 0) {
                    utils.sendGameMessage("stage: " + stage);
                    switch (stage) {
                        case 5: {
                            if (!validNPCs.isEmpty()) {
                                target = validNPCs.get(0);
                                actionNPC(target.getId(), MenuAction.NPC_FIRST_OPTION);
                            }
                            lastClue = clue;
                            break;
                        }
                        case 4: {
                            if (emotesToDo.isEmpty())
                                stage = 5;
                            break;
                        }
                        case 3: {
                            utils.sendGameMessage("setting emotes");
                            emotesToDo.add(12);
                            emotesToDo.add(2);
                            break;
                        }
                        case 2: {
                            if (hasStashItemsEquipped(clue)) {
                                log.info("has all items equipped");
                                stage ++;
                            }
                            break;
                        }
                        case 1: {
                            for (int i : getClueItems(clue)) {
                                WidgetItem item = inv.getWidgetItem(i);
                                targetMenu = inventoryAssistant.getLegacyMenuEntry(item.getId(), "wear", "wield", "equip");
                                utils.doInvokeMsTime(targetMenu, 0);
                            }
                            stage ++;
                            break;
                        }
                        case 0:  {
                            actionObject(getStashID(clue), MenuAction.GAME_OBJECT_SECOND_OPTION);
                            timeout = 3 + tickDelay();
                            stage ++;
                            break;
                        }

                    }
                } else if (distance(new WorldPoint(3480, 3477, 0)) <= 3) {
                    walk(new WorldPoint(3491, 3488, 0), 0, sleepDelay());
                } else if (distance(new WorldPoint(3465, 3474, 0)) <= 4) {
                    walk(new WorldPoint(3480, 3477, 0), 2, sleepDelay());
                } else if (distance(salveGraveyardTeleport) <= 10) {
                    walk(new WorldPoint(3465, 3474, 0), 2, sleepDelay());
                } else {
                    teleport(CluesTele.SALVE_GRAVEYARD);
                    stage = 0;
                }
                break;
            }
            case 3612: {
                if (distance(new WorldPoint(2409, 9812, 0)) <= 15) {
                    actionNPC(NpcID.BRIMSTAIL_11431, MenuAction.NPC_FIRST_OPTION);
                } else if (distance(new WorldPoint(2415, 3419, 0)) <= 3) {
                    actionObject(17209, MenuAction.GAME_OBJECT_FIRST_OPTION);
                    timeout = 3 + tickDelay();
                } else if (distance(new WorldPoint(2432, 3408, 0)) <= 3) {
                    walk(new WorldPoint(2415, 3419, 0), 1, sleepDelay());
                } else if (distance(new WorldPoint(2461, 3385, 0)) <= 0) {
                    walk(new WorldPoint(2432, 3408, 0), 3, sleepDelay());
                } else if (distance(new WorldPoint(2454, 3368, 0)) <= 3) {
                    actionObject(190, MenuAction.GAME_OBJECT_FIRST_OPTION);
                    timeout = 6 + tickDelay();
                } else if (distance(outpostTeleport) <= 10) {
                    walk(new WorldPoint(2454, 3368, 0), 3, sleepDelay());
                } else {
                    teleport(CluesTele.THE_OUTPOST);
                }
                break;
            }
            case 7307: {
                if (distance(new WorldPoint(2583, 2990, 0)) <= 1) {
                    dig();
                } else if (distance(new WorldPoint(2576, 2979, 0)) <= 2) {
                    walk(new WorldPoint(2583, 2990, 0), 1, sleepDelay());
                } else if (distance(new WorldPoint(2571, 2956, 0)) <= 3) {
                    walk(new WorldPoint(2576, 2979, 0), 2, sleepDelay());
                } else if (distance(salveGraveyardFairyRing) <= 1) {
                    if (widgetOpen(162, 37)) {
                        fairyRing("a", "k", "s");
                    }
                } else if (distance(salveGraveyardTeleport) <= 4) {
                    if (equip.isEquipped(ItemID.DRAMEN_STAFF))
                        actionObject(29495, MenuAction.GAME_OBJECT_SECOND_OPTION);
                    else {
                        actionItem(ItemID.DRAMEN_STAFF, "wield");
                    }
                } else {
                    teleport(CluesTele.SALVE_GRAVEYARD);
                }
                break;
            }
            case 2847: {
                if (distance(new WorldPoint(3232, 3424, 0)) <= 2) {
                    actionNPC(NpcID.LOWE, MenuAction.NPC_FIRST_OPTION);
                } else if (distance(varrockTeleport) <= 5) {
                    walk(new WorldPoint(3232, 3424, 0), 2, sleepDelay());
                } else {
                    teleport(CluesTele.VARROCK);
                }
                break;
            }
            case 12041: {
                if (distance(new WorldPoint(2322, 3061, 0)) <= 1) {
                    dig();
                } else if (distance(new WorldPoint(2350, 3053, 0)) <= 5) {
                    walk(new WorldPoint(2322, 3061, 0), 1, sleepDelay());
                } else if (distance(new WorldPoint(2385, 3035, 0)) <= 2) {
                    walk(new WorldPoint(2350, 3053, 0), 5, sleepDelay());
                } else if (distance(salveGraveyardFairyRing) <= 1) {
                    if (widgetOpen(162, 37)) {
                        fairyRing("b", "k", "p");
                    }
                } else if (distance(salveGraveyardTeleport) <= 4) {
                    if (equip.isEquipped(ItemID.DRAMEN_STAFF))
                        actionObject(29495, MenuAction.GAME_OBJECT_SECOND_OPTION);
                    else {
                        actionItem(ItemID.DRAMEN_STAFF, "wield");
                    }
                } else {
                    teleport(CluesTele.SALVE_GRAVEYARD);
                }
                break;
            }
            case 2845: {
                if (distance(new WorldPoint(2612, 3269, 0)) <= 5) {
                    if (!canType(40)) {
                        actionNPC(3302, MenuAction.NPC_FIRST_OPTION);
                    }
                } else if (distance(new WorldPoint(2613, 3244, 0)) <= 3) {
                    walk(new WorldPoint(2612, 3269, 0), 3, sleepDelay());
                } else if (distance(new WorldPoint(2634, 3242, 0)) <= 3) {
                    walk(new WorldPoint(2613, 3244, 0), 3, sleepDelay());
                } else if (distance(new WorldPoint(2658, 3230, 0)) <= 1) {
                    walk(new WorldPoint(2634, 3242, 0), 3, sleepDelay());
                } else if (distance(salveGraveyardFairyRing) <= 1) {
                    if (widgetOpen(162, 37)) {
                        fairyRing("d", "j", "p");
                    }
                } else if (distance(salveGraveyardTeleport) <= 4) {
                    if (equip.isEquipped(ItemID.DRAMEN_STAFF))
                        actionObject(29495, MenuAction.GAME_OBJECT_SECOND_OPTION);
                    else {
                        actionItem(ItemID.DRAMEN_STAFF, "wield");
                    }
                } else {
                    teleport(CluesTele.SALVE_GRAVEYARD);
                }
                break;
            }
            case 2825: {
                if (distance(new WorldPoint(3179, 3344, 0)) <= 5) {
                    dig();
                } else if (distance(new WorldPoint(3176, 3315, 0)) <= 5) {
                    if (handleDoor(883, new WorldPoint(3176, 3315, 0), MenuAction.GAME_OBJECT_FIRST_OPTION)) {

                    } else {
                        walk(new WorldPoint(3179, 3344, 0), 1, sleepDelay());
                    }
                } else if (distance(new WorldPoint(3153, 3310, 0)) <= 5) {
                    walk(new WorldPoint(3176, 3315, 0), 3, sleepDelay());
                } else if (distance(new WorldPoint(3141, 3312, 0)) <= 5) {
                    walk(new WorldPoint(3153, 3310, 0), 3, sleepDelay());
                } else if (distance(new WorldPoint(3109, 3295, 0)) <= 5) {
                    walk(new WorldPoint(3141, 3312, 0), 3, sleepDelay());
                } else if (distance(draynorTeleport) <= 5) {
                    walk(new WorldPoint(3109, 3295, 0), 2, sleepDelay());
                } else {
                    teleport(CluesTele.DRAYNOR_VILLAGE);
                }
                break;
            }
            case 7278: {
                if (distance(new WorldPoint(2537, 3305, 0)) <= 7) {
                    if (!canType(38)) {
                        actionNPC(NpcID.JETHICK_8974, MenuAction.NPC_FIRST_OPTION);
                    }
                } else if (distance(westArdougneTeleport) <= 5) {
                    walk(new WorldPoint(2537, 3305, 0), 3, sleepDelay());
                } else {
                    teleport(CluesTele.WEST_ARDOUGNE);
                }
                break;
            }
            case 3609: {
                if (distance(new WorldPoint(3482, 3477, 0)) <= 5) {
                    actionObject(24344, new WorldPoint(3498, 3507, 0), MenuAction.GAME_OBJECT_FIRST_OPTION);
                    timeout = 3 + tickDelay();
                } else if (distance(new WorldPoint(3468, 3474, 0)) <= 5) {
                    walk(new WorldPoint(3482, 3477, 0), 3, sleepDelay());
                } else if (distance(salveGraveyardTeleport) <= 5) {
                    walk(new WorldPoint(3468, 3474, 0), 3, sleepDelay());
                } else {
                    teleport(CluesTele.SALVE_GRAVEYARD);
                }
                break;
            }
            case 7300: {
                if (distance(new WorldPoint(2774, 3273, 0)) <= 12) {
                    utils.sendGameMessage("c");
                    if (!handleDoor(18168, new WorldPoint(2768, 3276, 0), MenuAction.GAME_OBJECT_FIRST_OPTION)) {
                        actionObject(18204, new WorldPoint(2764, 3273, 0), MenuAction.GAME_OBJECT_FIRST_OPTION);
                        timeout = 3 + tickDelay();
                    }
                } else
                if (distance(new WorldPoint(2717, 3307, 0)) <= 3) {
                    actionNPC(NpcID.HOLGART_7789, MenuAction.NPC_THIRD_OPTION);
                    timeout = 12 + tickDelay();
                } else if (distance(blrFairyRing) <= 1) {
                    walk(new WorldPoint(2717, 3307, 0), 3, sleepDelay());
                } else if (distance(salveGraveyardFairyRing) <= 1) {
                    if (widgetOpen(162, 37)) {
                        fairyRing("b", "l", "r");
                    }
                } else if (distance(salveGraveyardTeleport) <= 4) {
                    if (equip.isEquipped(ItemID.DRAMEN_STAFF))
                        actionObject(29495, MenuAction.GAME_OBJECT_SECOND_OPTION);
                    else {
                        actionItem(ItemID.DRAMEN_STAFF, "wield");
                    }
                } else {
                    teleport(CluesTele.SALVE_GRAVEYARD);
                }
                break;
            }
            case 12037: {
                if (distance(new WorldPoint(3548, 3560, 0)) <= 2) {
                    dig();
                } else if (distance(new WorldPoint(3548, 3557, 0)) <= 2) {
                    if (!handleDoor(5186, new WorldPoint(3548, 3558, 0), MenuAction.GAME_OBJECT_FIRST_OPTION)) {
                        walk(new WorldPoint(3548, 3560, 0), 1, sleepDelay());
                    }
                } else if (distance(new WorldPoint(3548, 3551, 0)) <= 2) {
                    if (!handleDoor(5183, new WorldPoint(3548, 3551, 0), MenuAction.GAME_OBJECT_FIRST_OPTION)) {
                        walk(new WorldPoint(3548, 3557, 0), 1, sleepDelay());
                    }
                } else if (distance(new WorldPoint(3548, 3543, 0)) <= 2) {
                    if (!handleDoor(5183, new WorldPoint(3548, 3543, 0), MenuAction.GAME_OBJECT_FIRST_OPTION)) {
                        walk(new WorldPoint(3548, 3551, 0), 1, sleepDelay());
                    }
                } else if (distance(new WorldPoint(3548, 3528, 0)) <= 4) {
                    if (!handleDoor(5183, new WorldPoint(3548, 3535, 0), MenuAction.GAME_OBJECT_FIRST_OPTION)) {
                        walk(new WorldPoint(3548, 3543, 0), 1, sleepDelay());
                    }
                } else if (distance(new WorldPoint(3517, 3518, 0)) <= 4) {
                    walk(new WorldPoint(3548, 3528, 0), 2, sleepDelay());
                } else if (distance(new WorldPoint(3475, 3477, 0)) <= 4) {
                    walk(new WorldPoint(3517, 3518, 0), 2, sleepDelay());
                } else if (distance(salveGraveyardTeleport) <= 4) {
                    walk(new WorldPoint(3475, 3477, 0), 2, sleepDelay());
                } else {
                    teleport(CluesTele.SALVE_GRAVEYARD);
                }
                break;
            }
            case 3588: {
                if (distance(new WorldPoint(2887, 3154, 0)) <= 1) {
                    dig();
                } else if (distance(karamjaTeleport) <= 5) {
                    walk(new WorldPoint(2887, 3154, 0), 1, sleepDelay());
                } else {
                    teleport(CluesTele.KARAMJA);
                }
                break;
            }
            case 7292: {
                if (distance(new WorldPoint(2578, 3597, 0)) <= 1) {
                    dig();
                } else if (distance(new WorldPoint(2554, 3614, 0)) <= 3) {
                    walk(new WorldPoint(2578, 3597, 0), 1, sleepDelay());
                } else if (distance(new WorldPoint(2542, 3624, 0)) <= 2) {
                    walk(new WorldPoint(2554, 3614, 0), 3, sleepDelay());
                } else if (distance(alpFairyRing) <= 1) {
                    walk(new WorldPoint(2542, 3624, 0), 2, sleepDelay());
                } else if (distance(salveGraveyardFairyRing) <= 1) {
                    if (widgetOpen(162, 37)) {
                        fairyRing("a", "l", "p");
                    }
                } else if (distance(salveGraveyardTeleport) <= 4) {
                    if (equip.isEquipped(ItemID.DRAMEN_STAFF))
                        actionObject(29495, MenuAction.GAME_OBJECT_SECOND_OPTION);
                    else {
                        actionItem(ItemID.DRAMEN_STAFF, "wield");
                    }
                } else {
                    teleport(CluesTele.SALVE_GRAVEYARD);
                }
                break;
            }
            case 3615: {
                if (!teleported) {
                    teleport(CluesTele.SALVE_GRAVEYARD);
                } else {
                    if (targetLoc == null) {
                        if (distance(3490, 3472) <= 4) {
                            actionNPC(6527, MenuAction.NPC_FIRST_OPTION);
                        } else {
                            w(3490, 3472, 4);
                        }
                    }
                }
                break;
            }
            case 2835: {
                if (distance(new WorldPoint(2617, 3324, 1)) <= 10) {
                    actionObject(348, new WorldPoint(2611, 3324, 1), MenuAction.GAME_OBJECT_FIRST_OPTION);
                    timeout = 3 + tickDelay();
                } else if (distance(new WorldPoint(2610, 3324, 0)) <= 1) {
                    if (!handleDoor(1535, new WorldPoint(2610, 3324, 0), MenuAction.GAME_OBJECT_FIRST_OPTION)) {
                        actionObject(16683, new WorldPoint(2617, 3323, 0), MenuAction.GAME_OBJECT_FIRST_OPTION);
                        timeout = 4;
                    }
                } else if (distance(new WorldPoint(2636, 3341, 0)) <= 4) {
                    if (!inv.containsItem(2836)) {
                        killAndLoot(5418);
                    } else {
                        walk(new WorldPoint(2610, 3324, 0), 0, sleepDelay());
                    }
                } else if (distance(new WorldPoint(2613, 3347, 0)) <= 2) {
                    if (inv.containsItem(2836)) {
                        walk(new WorldPoint(2610, 3324, 0), 0, sleepDelay());
                    } else {
                        walk(new WorldPoint(2636, 3341, 0), 2, sleepDelay());
                    }
                } else if (distance(fishingGuildTeleport) <= 10) {
                    walk(new WorldPoint(2613, 3347, 0), 2, sleepDelay());
                } else {
                    teleport(CluesTele.FISHING_GUILD);
                }
                break;
            }
            case 2839: {
                if (distance(new WorldPoint(2597, 3106, 1)) <= 6) {
                    actionObject(375, new WorldPoint(2593, 3108, 1), MenuAction.GAME_OBJECT_FIRST_OPTION);
                    timeout = 4 + tickDelay();
                } else if (distance(new WorldPoint(2594, 3102, 0)) <= 6) {
                    if (!handleDoor(17089, new WorldPoint(2594, 3102, 0), MenuAction.GAME_OBJECT_FIRST_OPTION)) {
                        if (!inv.containsItem(2840)) {
                            killAndLoot(3106);
                        } else {
                            actionObject(16683, new WorldPoint(2597, 3107, 0), MenuAction.GAME_OBJECT_FIRST_OPTION);
                            timeout = 4 + tickDelay();
                        }
                    }
                } else if (distance(new WorldPoint(2576, 3090, 0)) <= 3) {
                    walk(new WorldPoint(2594, 3102, 0), 3, sleepDelay());
                } else if (distance(new WorldPoint(2538, 3092, 0)) <= 1) {
                    if (!handleDoor(17093, new WorldPoint(2539, 3092, 0), MenuAction.GAME_OBJECT_FIRST_OPTION)) {
                        walk(new WorldPoint(2576, 3090, 0), 1, sleepDelay());
                    }
                } else if (distance(new WorldPoint(2532, 3092, 0)) <= 1) {
                    if (!handleDoor(17091, new WorldPoint(2532, 3092, 0), MenuAction.GAME_OBJECT_FIRST_OPTION)) {
                        walk(new WorldPoint(2538, 3092, 0), 1, sleepDelay());
                    }
                } else if (distance(ciqFairyRing) <= 1) {
                    walk(new WorldPoint(2532, 3092, 0), 1, sleepDelay());
                } else if (distance(salveGraveyardFairyRing) <= 1) {
                    if (widgetOpen(162, 37)) {
                        fairyRing("c", "i", "q");
                    }
                } else if (distance(salveGraveyardTeleport) <= 4) {
                    if (equip.isEquipped(ItemID.DRAMEN_STAFF))
                        actionObject(29495, MenuAction.GAME_OBJECT_SECOND_OPTION);
                    else {
                        actionItem(ItemID.DRAMEN_STAFF, "wield");
                    }
                } else {
                    teleport(CluesTele.SALVE_GRAVEYARD);
                }
                break;
            }
            case 3613: {
                if (distance(new WorldPoint(2269, 4756, 0)) <= 6) {
                    actionNPC(4093, MenuAction.NPC_FIRST_OPTION);
                    timeout = 3 + tickDelay();
                } else if (distance(new WorldPoint(2856, 3571, 0)) <= 4) {
                    actionObject(3735, MenuAction.GAME_OBJECT_FIRST_OPTION);
                    timeout = 3 + tickDelay();
                } else if (distance(burthorpeTeleport) <= 6) {
                    walk(new WorldPoint(2856, 3571, 0), 4, sleepDelay());
                } else {
                    teleport(CluesTele.BURTHORPE);
                }
                break;
            }
            case 7317: {
                if (distance(new WorldPoint(2875, 3046, 0)) <= 1) {
                    dig();
                } else if (distance(new WorldPoint(2844, 3031, 0)) <= 4) {
                    walk(new WorldPoint(2875, 3046, 0), 1, sleepDelay());
                } else if (distance(ckrFairyRing) <= 1) {
                    walk(new WorldPoint(2844, 3031, 0), 4, sleepDelay());
                } else if (distance(salveGraveyardFairyRing) <= 1) {
                    if (widgetOpen(162, 37)) {
                        fairyRing("c", "k", "r");
                    }
                } else if (distance(salveGraveyardTeleport) <= 4) {
                    if (equip.isEquipped(ItemID.DRAMEN_STAFF))
                        actionObject(29495, MenuAction.GAME_OBJECT_SECOND_OPTION);
                    else {
                        actionItem(ItemID.DRAMEN_STAFF, "wield");
                    }
                } else {
                    teleport(CluesTele.SALVE_GRAVEYARD);
                }
                break;
            }
            case 3616: {
                if (distance(new WorldPoint(3355, 3265, 0)) <= 2) {
                    actionNPC(3344, MenuAction.NPC_FIRST_OPTION);
                } else if (distance(duelArenaTeleport) <= 7) {
                    walk(new WorldPoint(3355, 3265, 0), 2, sleepDelay());
                } else {
                    teleport(CluesTele.DUEL_ARENA);
                }
                break;
            }
            case 12035: {
                if (distance(new WorldPoint(3510, 3074, 0)) <= 1) {
                    dig();
                } else if (distance(new WorldPoint(3475, 3072, 0)) <= 1) {
                    walk(new WorldPoint(3510, 3074, 0), 0, sleepDelay());
                } else if (distance(new WorldPoint(3439, 3061, 0)) <= 5) {
                    walk(new WorldPoint(3475, 3072, 0), 0, sleepDelay());
                } else if (distance(dlqFairyRing) <= 1) {
                    walk(new WorldPoint(3439, 3061, 0), 4, sleepDelay());
                } else if (distance(salveGraveyardFairyRing) <= 1) {
                    if (widgetOpen(162, 37)) {
                        fairyRing("d", "l", "q");
                    }
                } else if (distance(salveGraveyardTeleport) <= 4) {
                    if (equip.isEquipped(ItemID.DRAMEN_STAFF))
                        actionObject(29495, MenuAction.GAME_OBJECT_SECOND_OPTION);
                    else {
                        actionItem(ItemID.DRAMEN_STAFF, "wield");
                    }
                } else {
                    teleport(CluesTele.SALVE_GRAVEYARD);
                }
                break;
            }
            case 7282: {
                if (!teleported) {
                    teleport(CluesTele.FISHING_GUILD);
                } else {
                    if (distance(fishingGuildTeleport) <= 10) {
                        w(2566, 3332, 1);
                    } else if (distance(2566, 3332) <= 3) {
                        if (!canType(3)) {
                            actionNPC(NpcID.EDMOND_4256, MenuAction.NPC_FIRST_OPTION);
                        }
                    }
                }
                break;
            }
            case 23141: {
                if (distance(new WorldPoint(2990, 3384, 0)) <= 1) {
                    if (song) {
                        actionNPC(NpcID.CECILIA, MenuAction.NPC_FIRST_OPTION);
                        timeout = 1 + tickDelay();
                    } else {
                        song = playSong("catch me if you can");
                        timeout = 3 + tickDelay();
                    }
                } else if (distance(faladorTeleport) <= 10) {
                    walk(new WorldPoint(2990, 3384, 0), 1, sleepDelay());
                } else {
                    teleport(CluesTele.FALADOR);
                }
                break;
            }
            case 23135: {
                if (distance(new WorldPoint(1247, 3726, 0)) <= 1) {
                    dig();
                } else if (distance(farmingGuildTeleport) <= 2) {
                    walk(new WorldPoint(1247, 3726, 0), 1, sleepDelay());
                } else {
                    teleport(CluesTele.FARMING_GUILD);
                }
                break;
            }
            case 19758: {
                if (distance(1814, 3857) <= 12) {
                    if (!canType(2)) {
                        actionNPC(NpcID.ARETHA, MenuAction.NPC_FIRST_OPTION);
                    }
                } else if (distance(1800, 3892) <= 2) {
                    walk(1814, 3857, 2);
                } else if (distance(1780, 3894) <= 2) {
                    walk(1800, 3892, 2);
                } else if (distance(1760, 3896) <= 2) {
                    walk(1780, 3894, 2);
                } else if (distance(1743, 3892) <= 2) {
                    walk(1760, 3896, 2);
                } else if (distance(1720, 3883) <= 2) {
                    walk(1743, 3892, 2);
                } else if (distance(1700, 3880) <= 2) {
                    walk(1720, 3883, 2);
                } else if (distance(1675, 3879) <= 2) {
                    walk(1700, 3880, 2);
                } else if (distance(1644, 3879) <= 3) {
                    walk(1675, 3879, 2);
                } else if (distance(arceuusTeleport) <= 10) {
                    walk(1644, 3879, 2);
                } else {
                    teleport(CluesTele.ARCEUUS_LIBRARY);
                }
                break;
            }
            case 19770: {
                if (distance(lumbridgeTeleport) <= 20) {
                    if (!canType(666)) {
                        actionNPC(NpcID.IRON_MAN_TUTOR, MenuAction.NPC_FIRST_OPTION);
                    }
                } else {
                    teleport(CluesTele.LUMBRIDGE);
                }
                break;
            }
            case 7280: {
                if (distance(2719, 3309) <= 12) {
                    if (!canType(11)) {
                        actionNPC(NpcID.CAROLINE, MenuAction.NPC_FIRST_OPTION);
                    }
                } else if (distance(blrFairyRing) <= 1) {
                    walk(2719, 3309, 3);
                } else if (distance(salveGraveyardFairyRing) <= 1) {
                    if (widgetOpen(162, 37)) {
                        fairyRing("b", "l", "r");
                    }
                } else if (distance(salveGraveyardTeleport) <= 4) {
                    if (equip.isEquipped(ItemID.DRAMEN_STAFF))
                        actionObject(29495, MenuAction.GAME_OBJECT_SECOND_OPTION);
                    else {
                        actionItem(ItemID.DRAMEN_STAFF, "wield");
                    }
                } else {
                    teleport(CluesTele.SALVE_GRAVEYARD);
                }
                break;
            }
            case 7296: {
                if (inv.containsItem(7297)) {
                    if (!teleported) {
                        teleport(CluesTele.VARROCK);
                    } else {
                        if (targetLoc == null) {
                            if (distance(3352, 3339) <= 1) {
                                if (!handleDoor(17316, new WorldPoint(3352, 3337, 0), MenuAction.GAME_OBJECT_FIRST_OPTION)) {
                                    actionObject(375, new WorldPoint(3353, 3332, 0), MenuAction.GAME_OBJECT_FIRST_OPTION);
                                }
                            } else if (distance(3308, 3332) <= 4) {
                                if (!handleDoor(11766, new WorldPoint(3312, 3331, 0), MenuAction.GAME_OBJECT_FIRST_OPTION)) {
                                    w(3352, 3339, 1);
                                }
                            } else if (distance(varrockTeleport) <= 10) {
                                w(3308, 3332, 2);
                            }
                        }
                    }
                } else {
                    if (!teleported) {
                        teleport(CluesTele.BARBARIAN_OUTPOST);
                    } else {
                        if (targetLoc == null) {
                            if (distance(barbarianOutpostTeleport) <= 10) {
                                w(2542, 3572, 10);
                            } else {
                                killAndLoot(3062);
                            }
                        }
                    }
                }
                break;
            }
            case 3617: {
                if (!teleported) {
                    teleport(CluesTele.HOUSE);
                } else {
                    if (targetLoc == null) {
                        if (distance(brimhavenTeleport) <= 10) {
                            w(2794, 3178, 2);
                        } else if (distance(2794, 3178) <= 5) {
                            if (!handleDoor(1540, new WorldPoint(2794, 3181, 0), MenuAction.GAME_OBJECT_FIRST_OPTION)) {
                                actionNPC(NpcID.KANGAI_MAU, MenuAction.NPC_FIRST_OPTION);
                            }
                        }
                    }
                }
                break;
            }
            case 23138: {
                if (!teleported) {
                    teleport(CluesTele.FALADOR);
                } else {
                    if (targetLoc == null) {
                        if (distance(2991, 3383) <= 9) {
                            if (!song) {
                                playSong("karamja jam");
                            } else {
                                actionNPC(NpcID.CECILIA, MenuAction.NPC_FIRST_OPTION);
                            }
                        }
                        if (distance(faladorTeleport) <= 8) {
                            w(2991, 3383, 6);
                        }
                    }
                }
                break;
            }
            case 19756: {
                if (!teleported) {
                    teleport(CluesTele.WOODCUTTING_GUILD);
                } else {
                    if (targetLoc == null) {
                        if (distance(1741, 3554) <= 7) {
                            if (!canType(5)) {
                                actionNPC(NpcID.MARISI, MenuAction.NPC_FIRST_OPTION);
                            }
                        } else if (distance(woodcuttingGuildTeleport) <= 8) {
                            w(1741, 3554, 4);
                        }
                    }
                }
                break;
            }
            case 2855: {
                if (!teleported) {
                    teleport(CluesTele.CAMELOT);
                    // teleported = true;
                } else {
                    if (targetLoc == null) {
                        if (distance(camelotTeleport) <= 8) {
                            w(2740, 3553, 2);
                        } else if (distance(2740, 3553) <= 4) {
                            if (!handleDoor(26130, new WorldPoint(2741, 3555, 0), MenuAction.GAME_OBJECT_FIRST_OPTION)) {
                                w(2741, 3572, 0);
                            }
                        } else if (distance(2741, 3572) <= 0) {
                            door = objectUtils.findWallObjectWithin(new WorldPoint(2741, 3572, 0), 0, 25750);
                            if (door != null) {
                                targetMenu = new LegacyMenuEntry("", "", door.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION, door.getLocalLocation().getSceneX(), door.getLocalLocation().getSceneY(), false);
                                if (config.invokes()) {
                                    utils.doInvokeMsTime(targetMenu, sleepDelay());
                                } else {
                                    utils.doActionMsTime(targetMenu, door.getConvexHull().getBounds(), sleepDelay());
                                }
                            }
                        } else if (distance(2741, 3573) <= 0) {
                            actionObject(25682, MenuAction.GAME_OBJECT_FIRST_OPTION);
                        }  else if (distance(new WorldPoint(2736, 3580, 1)) <= 20) {
                            if (!handleDoor(25718, new WorldPoint(2744, 3577, 1), MenuAction.GAME_OBJECT_FIRST_OPTION)) {
                                if (!handleDoor(25718, new WorldPoint(2745, 3578, 1), MenuAction.GAME_OBJECT_FIRST_OPTION)) {
                                    actionNPC(NpcID.DONOVAN_THE_FAMILY_HANDYMAN, MenuAction.NPC_FIRST_OPTION);
                                }
                            }
                        }
                    }
                }
                break;
            }
            case 19776: {
                if (!teleported) {
                    teleport(CluesTele.SALVE_GRAVEYARD);
                    teleported = true;
                    stage = 0;
                } else {
                    if (targetLoc == null) {
                        if (distance(1459, 3654) <= 1) {
                            w(1544, 3630, 0);
                        } else if (distance(djrFairyRing) <= 1) {
                            walk(1459, 3654, 1);
                        } else if (distance(salveGraveyardFairyRing) <= 1) {
                            if (widgetOpen(162, 37)) {
                                fairyRing("d", "j", "r");
                            }
                        } else if (distance(salveGraveyardTeleport) <= 4) {
                            if (equip.isEquipped(ItemID.DRAMEN_STAFF))
                                actionObject(29495, MenuAction.GAME_OBJECT_SECOND_OPTION);
                            else {
                                actionItem(ItemID.DRAMEN_STAFF, "wield");
                            }
                        }
                    }
                }
                break;
            }
            case 10260: {
                if (!teleported) {
                    teleport(CluesTele.HOUSE);
                    stage = 0;
                } else {
                    if (targetLoc == null) {
                        if (distance(2803, 3073) <= 15) {
                            utils.sendGameMessage("stage: " + stage);
                            switch (stage) {
                                case 5: {
                                    if (!validNPCs.isEmpty()) {
                                        target = validNPCs.get(0);
                                        actionNPC(target.getId(), MenuAction.NPC_FIRST_OPTION);
                                    }
                                    lastClue = clue;
                                    timeout = 4 + tickDelay();
                                    break;
                                }
                                case 4: {
                                    if (emotesToDo.isEmpty())
                                        stage = 5;
                                    timeout = 4 + tickDelay();
                                    break;
                                }
                                case 3: {
                                    walk(2803, 3073, 0);
                                    utils.sendGameMessage("setting emotes");
                                    emotesToDo.add(8);
                                    emotesToDo.add(3);
                                    timeout = 4 + tickDelay();
                                    break;
                                }
                                case 2: {
                                    if (hasStashItemsEquipped(clue)) {
                                        log.info("has all items equipped");
                                        stage ++;
                                    } else {
                                        for (int i : getClueItems(clue)) {
                                            WidgetItem item = inv.getWidgetItem(i);
                                            targetMenu = inventoryAssistant.getLegacyMenuEntry(item.getId(), "wear", "wield", "equip");
                                            utils.doInvokeMsTime(targetMenu, 0);
                                        }
                                    }
                                    timeout = 4 + tickDelay();
                                    break;
                                }
                                case 1: {
                                    for (int i : getClueItems(clue)) {
                                        WidgetItem item = inv.getWidgetItem(i);
                                        targetMenu = inventoryAssistant.getLegacyMenuEntry(item.getId(), "wear", "wield", "equip");
                                        utils.doInvokeMsTime(targetMenu, 0);
                                        break;
                                    }
                                    stage ++;
                                    break;
                                }
                                case 0:  {
                                    actionObject(getStashID(clue), MenuAction.GAME_OBJECT_SECOND_OPTION);
                                    timeout = 3 + tickDelay();
                                    stage ++;
                                    break;
                                }
                            }
                        } else {
                            if (distance(brimhavenTeleport) <= 7) {
                                w(2803, 3073, 4);
                            }
                        }
                    }
                }
                break;
            }
            case 19764: {
                if (!teleported) {
                    teleport(CluesTele.WIZARDS_TOWER);
                } else {
                    if (distance(wizardsTowerTeleport) <= 35) {
                        if (!handleDoor(23972, new WorldPoint(3109, 3167, 0), MenuAction.GAME_OBJECT_FIRST_OPTION)) {
                            if (!handleDoor(23972, new WorldPoint(3107, 3162, 0), MenuAction.GAME_OBJECT_FIRST_OPTION)) {
                                actionObject(12536, MenuAction.GAME_OBJECT_FIRST_OPTION);
                            }
                        }
                    } else if (player.getWorldLocation().getPlane() == 1) {
                        if (!handleDoor(23972, new WorldPoint(3109, 3162, 1), MenuAction.GAME_OBJECT_FIRST_OPTION)) {
                            if (!canType(3150)) {
                                actionNPC(5081, MenuAction.NPC_FIRST_OPTION);
                            }
                        }
                    }
                }
                break;
            }
            case 3598: {
                if (!teleported) {
                    teleport(CluesTele.SALVE_GRAVEYARD);
                    teleported = true;
                    stage = 0;
                } else {
                    if (targetLoc == null) {
                        if (distance(alsFairyRing) <= 1) {
                            actionObject(357, MenuAction.GAME_OBJECT_FIRST_OPTION);
                        } else if (distance(salveGraveyardFairyRing) <= 1) {
                            if (widgetOpen(162, 37)) {
                                fairyRing("a", "l", "s");
                            }
                        } else if (distance(salveGraveyardTeleport) <= 4) {
                            if (equip.isEquipped(ItemID.DRAMEN_STAFF))
                                actionObject(29495, MenuAction.GAME_OBJECT_SECOND_OPTION);
                            else {
                                actionItem(ItemID.DRAMEN_STAFF, "wield");
                            }
                        }
                    }
                }
                break;
            }
            case 19748: {
                if (!teleported) {
                    teleport(CluesTele.BURTHORPE);
                } else {
                    if (distance(2920, 3568) <= 8) {
                        if (!handleDoor(1535, new WorldPoint(2921, 3571, 0), MenuAction.GAME_OBJECT_FIRST_OPTION)) {
                            if (!canType(8)) {
                                actionNPC(NpcID.DUNSTAN, MenuAction.NPC_FIRST_OPTION);
                            }
                        }
                    } else if (distance(burthorpeTeleport) <= 8) {
                        walk(2920, 3568, 0, 2);
                    }
                }
                break;
            }
            case 2827: {
                if (!teleported) {
                    teleport(CluesTele.DRAYNOR_VILLAGE);
                } else if (targetLoc == null) {
                    if (distance(3093, 3226) <= 1) {
                        dig();
                    } else {
                        w(3093, 3226, 1);
                    }
                }
                break;
            }
            case 7288: {
                if (!teleported) {
                    teleport(CluesTele.SALVE_GRAVEYARD);
                } else {
                    if (targetLoc == null) {
                        if (distance(salveGraveyardTeleport) <= 4) {
                            if (!handleDoor(3507, new WorldPoint(3443, 3458, 0), MenuAction.GAME_OBJECT_FIRST_OPTION)) {
                                w(3434, 3265, 0);
                            }
                        } else if (distance(3434, 3265) <= 0) {
                            dig();
                        } else if (distance(3443, 3457) <= 0) {
                            w(3434, 3265, 0);
                        }
                    }
                }
                break;
            }
            case 2833: {
                if (!teleported) {
                    teleport(CluesTele.FISHING_GUILD);
                } else if (targetLoc == null) {
                    if (!inv.containsItem(2834)) {
                        if (distance(fishingGuildTeleport) <= 8) {
                            w(2635, 3305, 3);
                        } else if (distance(2635, 3305) <= 3) {
                            if (!handleDoor(1568, new WorldPoint(2635, 3307, 0), MenuAction.GAME_OBJECT_FIRST_OPTION)) {
                                killAndLoot(114);
                            }
                        } else {
                            killAndLoot(114);
                        }
                    } else {
                        if (distance(fishingGuildTeleport) <= 8) {
                            w(2577, 3320, 2);
                        } else if (distance(new WorldPoint(2577, 3320, 0)) <= 12) {
                            if (!handleDoor(1540, new WorldPoint(2576, 3320, 0), MenuAction.GAME_OBJECT_FIRST_OPTION)) {
                                actionObject(16671, MenuAction.GAME_OBJECT_FIRST_OPTION);
                            }
                        } else if (distance(new WorldPoint(2574, 3325, 1)) <= 15) {
                            actionObject(348, MenuAction.GAME_OBJECT_FIRST_OPTION);
                        }
                    }
                }
                break;
            }
            case 23142: {
                if (!teleported) {
                    teleport(CluesTele.FALADOR);
                } else {
                    if (targetLoc == null) {
                        if (distance(2991, 3383) <= 2) {
                            if (!song) {
                                playSong("cave of beasts");
                            } else {
                                actionNPC(NpcID.CECILIA, MenuAction.NPC_FIRST_OPTION);
                            }
                        } else {
                            w(2991, 3383, 2);
                        }
                    }
                }
                break;
            }
            case 2815: {
                if (!teleported) {
                    teleport(CluesTele.SALVE_GRAVEYARD);
                    teleported = true;
                    stage = 0;
                } else {
                    if (targetLoc == null) {
                        if (distance(2848, 3296) <= 1) {
                            dig();
                        } else if (distance(2834, 3258) <= 4) {
                            w(2848, 3296, 1);
                        } else if (distance(2832, 9657) <= 2) {
                            actionObject(25213, MenuAction.GAME_OBJECT_FIRST_OPTION);
                        } else if (distance(2836, 9600) <= 0) {
                            w(2832, 9657, 2);
                        } else if (distance(2862, 9572) <= 3) {
                            if (!handleDoor(2606, new WorldPoint(2836, 9600, 0), MenuAction.GAME_OBJECT_FIRST_OPTION)) {

                            }
                        } else if (distance(2480, 5173) <= 2) {
                            actionObject(11836, MenuAction.GAME_OBJECT_FIRST_OPTION);
                        } else if (distance(2437, 5130) <= 1) {
                            w(2480, 5173, 2);
                        } else if (distance(blpFairyRing) <= 1) {
                            walk(2437, 5130, 1);
                        } else if (distance(salveGraveyardFairyRing) <= 1) {
                            if (widgetOpen(162, 37)) {
                                fairyRing("b", "l", "p");
                            }
                        } else if (distance(salveGraveyardTeleport) <= 4) {
                            if (equip.isEquipped(ItemID.DRAMEN_STAFF))
                                actionObject(29495, MenuAction.GAME_OBJECT_SECOND_OPTION);
                            else {
                                actionItem(ItemID.DRAMEN_STAFF, "wield");
                            }
                        }
                    }
                }
                break;
            }
            case 2837: {
                if (!teleported) {
                    if (!inv.containsItem(2838)) {
                        teleport(CluesTele.LUMBRIDGE);
                    } else {
                        //teleport(CluesTele.CAMELOT);
                        teleported = true;
                    }
                } else if (targetLoc == null) {
                    if (!inv.containsItem(2838)) {
                        if (distance(3237, 3295) <= 8) {
                            if (!handleDoor(1560, new WorldPoint(3236, 3295, 0), MenuAction.GAME_OBJECT_FIRST_OPTION)) {
                                killAndLoot(1174);
                            }
                        } else if (distance(lumbridgeTeleport) <= 10) {
                            w(3237, 3295, 0);
                        }
                    } else {
                        if (distance(2713, 3484) <= 8) {
                            if (!handleDoor(25819, new WorldPoint(2713, 3483, 0), MenuAction.GAME_OBJECT_FIRST_OPTION)) {
                                actionObject(25766, MenuAction.GAME_OBJECT_FIRST_OPTION);
                            }
                        } else if (distance(camelotTeleport) <= 10) {
                            w(2713, 3484, 2);
                        }
                    }
                }
                break;
            }
            case 7313: {
                if (!teleported) {
                    teleport(CluesTele.LUMBRIDGE);
                } else if (targetLoc == null) {
                    if (distance(3184, 3150) <= 1) {
                        dig();
                    } else if (distance(lumbridgeTeleport) <= 10) {
                        w(3184, 3150, 1);
                    }
                }
                break;
            }
            case 19742: {
                if (!teleported) {
                    teleport(CluesTele.ARCEUUS_LIBRARY);
                } else if (targetLoc == null) {
                    if (distance(arceuusTeleport) <= 5) {
                        if (!handleDoor(28460, new WorldPoint(1633, 3817, 0), MenuAction.GAME_OBJECT_FIRST_OPTION)) {
                            walk(1638, 3813, 2);
                        }
                    } else if (!canType(1)) {
                        actionNPC(NpcID.HORPHIS, MenuAction.NPC_FIRST_OPTION);
                    }
                }
                break;
            }
            case 7298: {
                if (!teleported) {
                    teleport(CluesTele.SALVE_GRAVEYARD);
                } else {
                    if (targetLoc == null) {
                        if (!inv.containsItem(7299)) {
                            if (distance(2642, 3677) <= 12) {
                                killAndLoot(3949);
                            } else if (distance(ajrFairyRing.getX() - 10, ajrFairyRing.getY()) <= 2) {
                                w(2642, 3677, 3);
                            } else if (distance(ajrFairyRing) <= 1) {
                                walk(ajrFairyRing.getX() - 10, ajrFairyRing.getY(), 2);
                            } else if (distance(salveGraveyardFairyRing) <= 1) {
                                if (widgetOpen(162, 37)) {
                                    fairyRing("a", "j", "r");
                                }
                            } else if (distance(salveGraveyardTeleport) <= 4) {
                                if (equip.isEquipped(ItemID.DRAMEN_STAFF))
                                    actionObject(29495, MenuAction.GAME_OBJECT_SECOND_OPTION);
                                else {
                                    actionItem(ItemID.DRAMEN_STAFF, "wield");
                                }
                            }
                        } else {
                            if (distance(new WorldPoint(2509, 3636, 1)) <= 10) {
                                actionObject(350, MenuAction.GAME_OBJECT_FIRST_OPTION);
                            } else if (distance(new WorldPoint(2509, 3636, 0)) <= 0) {
                                actionObject(4568, MenuAction.GAME_OBJECT_FIRST_OPTION);
                            } else if (distance(alpFairyRing) <= 1) {
                                handleDoor(4577, new WorldPoint(2509, 3636, 0), MenuAction.GAME_OBJECT_FIRST_OPTION);
                            } else if (distance(salveGraveyardFairyRing) <= 1) {
                                if (widgetOpen(162, 37)) {
                                    fairyRing("a", "l", "p");
                                }
                            } else if (distance(salveGraveyardTeleport) <= 4) {
                                if (equip.isEquipped(ItemID.DRAMEN_STAFF))
                                    actionObject(29495, MenuAction.GAME_OBJECT_SECOND_OPTION);
                                else {
                                    actionItem(ItemID.DRAMEN_STAFF, "wield");
                                }
                            }
                        }
                    }
                }
                break;
            }
            case 3601: {
                if (!teleported) {
                    teleport(CluesTele.SALVE_GRAVEYARD);
                } else if (targetLoc == null) {
                    if (distance(2565, 3248) <= 1) {
                        actionObject(354, MenuAction.GAME_OBJECT_FIRST_OPTION);
                    } else if (distance(djpFairyRing) <= 0) {
                        walk(2632, 3237, 1);
                    } else if (distance(salveGraveyardFairyRing) <= 1) {
                        if (widgetOpen(162, 37)) {
                            fairyRing("d", "j", "p");
                        }
                    } else if (distance(salveGraveyardTeleport) <= 4) {
                        if (equip.isEquipped(ItemID.DRAMEN_STAFF))
                            actionObject(29495, MenuAction.GAME_OBJECT_SECOND_OPTION);
                        else {
                            actionItem(ItemID.DRAMEN_STAFF, "wield");
                        }
                    } else {
                        w(2565, 3248, 1);
                    }
                }
                timeout = 3 + tickDelay();
                break;
            }
            case 12069: {
                if (!teleported) {
                    // teleport(CluesTele.CAMELOT);
                    log.info("teleport");
                    teleported = true;
                } else {
                    if (targetLoc == null) {
                        if (distance(new WorldPoint(2758, 3496, 0)) <= 8) {
                            log.info("a");
                            if (!canType(6)) {
                                actionNPC(NpcID.SIR_KAY, MenuAction.NPC_FIRST_OPTION);
                                log.info("b");
                            }
                        } else if (distance(new WorldPoint(2757, 3483, 0)) <= 0) {
                            walk(2758, 3496, 2);
                        } else if (distance(camelotTeleport) <= 3) {
                            handleDoor(26081, new WorldPoint(2757, 3482, 0), MenuAction.GAME_OBJECT_FIRST_OPTION);
                        }
                    }
                }
                break;
            }
            case 3614: {
                if (!teleported) {
                    teleport(CluesTele.SALVE_GRAVEYARD);
                } else if (targetLoc == null) {
                    if (distance(salveGraveyardTeleport) <= 10) {
                        actionNPC(NpcID.ULIZIUS, MenuAction.NPC_FIRST_OPTION);
                    }
                }
                break;
            }
            case 7301: {
                if (!teleported) {
                    teleport(CluesTele.WIZARDS_TOWER);
                } else {
                    if (distance(wizardsTowerTeleport) <= 35) {
                        if (!handleDoor(23972, new WorldPoint(3109, 3167, 0), MenuAction.GAME_OBJECT_FIRST_OPTION)) {
                            GameObject d = objectUtils.findNearestGameObjectWithin(new WorldPoint(3107, 3162, 0), 0, 23972);
                            if (d != null) {
                                targetMenu = new LegacyMenuEntry("", "", d.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION, d.getLocalLocation().getSceneX(), d.getLocalLocation().getSceneY(), false);
                                if (config.invokes()) {
                                    utils.doInvokeMsTime(targetMenu, sleepDelay());
                                } else {
                                    utils.doActionMsTime(targetMenu, d.getConvexHull().getBounds(), sleepDelay());
                                }
                            } else {
                                actionObject(2147, new WorldPoint(3104, 3162, 0), MenuAction.GAME_OBJECT_FIRST_OPTION);
                            }
                        }
                    } else if (distance(new WorldPoint(3104, 9576, 0)) <= 35) {
                        if (!inv.containsItem(7302)) {
                            if (distance(3109, 9559) >= 4) {
                                walk(3109, 9559, 2);
                            } else {
                                killAndLoot(3257, false);
                            }
                        } else if (!handleDoor(1535, new WorldPoint(3111, 9559, 0), MenuAction.GAME_OBJECT_FIRST_OPTION)) {
                            actionObject(350, MenuAction.GAME_OBJECT_FIRST_OPTION);
                        }
                    }
                }
                break;
            }
            case 2858: {
                if (!teleported) {
                    teleport(CluesTele.KARAMJA);
                } else if (targetLoc == null) {
                    if (distance(2937, 3148) <= 6) {
                        actionNPC(NpcID.LUTHAS, MenuAction.NPC_FIRST_OPTION);
                    } else if (distance(karamjaTeleport) <= 6) {
                        w(2937, 3148, 2);
                    }
                }
                break;
            }
            case 19736: {
                if (!teleported) {
                    teleport(CluesTele.SALVE_GRAVEYARD);
                } else if (targetLoc == null) {
                    if (distance(2603, 3116) <= 5) {
                        if (!canType(9500)) {
                            actionNPC(NpcID.DOMINIC_ONION, MenuAction.NPC_FIRST_OPTION);
                        }
                    } else if (distance(new WorldPoint(2576, 3090, 0)) <= 3) {
                        w(2603, 3116, 2);
                    } else if (distance(new WorldPoint(2538, 3092, 0)) <= 1) {
                        if (!handleDoor(17093, new WorldPoint(2539, 3092, 0), MenuAction.GAME_OBJECT_FIRST_OPTION)) {
                            walk(new WorldPoint(2576, 3090, 0), 1, sleepDelay());
                        }
                    } else if (distance(new WorldPoint(2532, 3092, 0)) <= 1) {
                        if (!handleDoor(17091, new WorldPoint(2532, 3092, 0), MenuAction.GAME_OBJECT_FIRST_OPTION)) {
                            walk(new WorldPoint(2538, 3092, 0), 1, sleepDelay());
                        }
                    } else if (distance(ciqFairyRing) <= 1) {
                        walk(new WorldPoint(2532, 3092, 0), 1, sleepDelay());
                    } else if (distance(salveGraveyardFairyRing) <= 1) {
                        if (widgetOpen(162, 37)) {
                            fairyRing("c", "i", "q");
                        }
                    } else if (distance(salveGraveyardTeleport) <= 4) {
                        if (equip.isEquipped(ItemID.DRAMEN_STAFF))
                            actionObject(29495, MenuAction.GAME_OBJECT_SECOND_OPTION);
                        else {
                            actionItem(ItemID.DRAMEN_STAFF, "wield");
                        }
                    }
                }
                break;
            }
            case 2853: {
                if (!teleported) {
                    teleport(CluesTele.THE_OUTPOST);
                } else if (targetLoc == null) {
                    log.info("1");
                    target = npcs.findNearestNpc(3157);
                    if (target != null && player.getWorldLocation().getX() > 2382 && player.getWorldLocation().getX() < 2390) {
                        log.info("2");
                        if (distance(target.getWorldLocation()) <= 5) {
                            log.info("3");
                            if (!canType(5096)) {
                                log.info("4");
                                actionNPC(NpcID.GNOME_BALL_REFEREE, MenuAction.NPC_FIRST_OPTION);
                            }
                        }
                    } else if (distance(2383, 3488) <= 0) {
                        log.info("12");
                        actionNPC(3157, MenuAction.NPC_FIRST_OPTION);
                    } else if (distance(2382, 3488) <= 0) {
                        log.info("13");
                        handleDoor(2394, new WorldPoint(2383, 3488, 0), MenuAction.GAME_OBJECT_FIRST_OPTION);
                    } else if (distance(2380, 3494) <= 4) {
                        log.info("14");
                        walk(2382, 3488, 0);
                    } else if (distance(2461, 3385) <= 0) {
                        log.info("15");
                        w(2380, 3494, 1);
                    } else if (distance(2461, 3379) <= 5) {
                        log.info("16");
                        actionObject(190, MenuAction.GAME_OBJECT_FIRST_OPTION);
                        timeout = 6 + tickDelay();
                    } else if (distance(outpostTeleport) <= 10) {
                        log.info("7");
                        w(2461, 3379, 2);
                    }
                }
                break;
            }
            case 12059: {
                if (!teleported) {
                    teleport(CluesTele.THE_OUTPOST);
                } else if (targetLoc == null) {
                    if (distance(2435, 3424) <= 6) {
                        target = npcs.findNearestNpc(6797);
                        if (target == null)
                            target = npcs.findNearestNpc(NpcID.STEVE);
                        if (target == null)
                            break;
                        if (!canType(2)) {
                            actionNPC(target.getId(), MenuAction.NPC_FIRST_OPTION);
                        }
                    } else if (distance(2461, 3385) <= 0) {
                        w(2435, 3424, 2);
                    } else if (distance(2461, 3379) <= 5) {
                        actionObject(190, MenuAction.GAME_OBJECT_FIRST_OPTION);
                        timeout = 6 + tickDelay();
                    } else if (distance(outpostTeleport) <= 10) {
                        w(2461, 3379, 2);
                    }
                }
                break;
            }
            case 3604: {
                if (!teleported) {
                    teleport(CluesTele.HOUSE);
                } else if (targetLoc == null) {
                    if (distance(brimhavenTeleport) <= 5) {
                        w(2800, 3074, 2);
                    } else {
                        if (distance(2800, 3075) <= 5) {
                            actionObject(356, MenuAction.GAME_OBJECT_FIRST_OPTION);
                        }
                    }
                }
                break;
            }
            case 23139: {
                if (!teleported) {
                    teleport(CluesTele.FALADOR);
                } else {
                    if (targetLoc == null) {
                        if (distance(2991, 3383) <= 2) {
                            if (!song) {
                                playSong("faerie");
                            } else {
                                actionNPC(NpcID.CECILIA, MenuAction.NPC_FIRST_OPTION);
                            }
                        } else {
                            if (distance(2991, 3383) >= 50)
                                teleported = false;
                            else
                                w(2991, 3383, 2);
                        }
                    }
                }
                break;
            }
            case 2805: {
                if (!teleported) {
                    teleport(CluesTele.HOUSE);
                } else if (targetLoc == null) {
                    if (distance(brimhavenTeleport) <= 5) {
                        w(2711, 3204, 0);
                        timeout = 6 + tickDelay();
                    } else {
                        if (distance(2711, 3204) <= 1) {
                            actionObject(23568, MenuAction.GAME_OBJECT_FIRST_OPTION);
                            timeout = 4 + tickDelay();
                        } else if (distance(2704, 3209) <= 1) {
                            walk(2697, 3207, 1);
                        } else if (distance(2697, 3207) <= 1) {
                            dig();
                        }
                    }
                }
                break;
            }
            case 3590: {
                if (!teleported) {
                    teleport(CluesTele.HOUSE);
                } else if (targetLoc == null) {
                    if (distance(brimhavenTeleport) <= 5) {
                        w(2743, 3151, 0);
                        timeout = 6 + tickDelay();
                    } else {
                        if (distance(2743, 3151) <= 1) {
                            dig();
                        }
                    }
                }
                break;
            }
            case 12047: {
                if (!teleported) {
                    teleport(CluesTele.SALVE_GRAVEYARD);
                } else if (targetLoc == null) {
                    if (distance(2363, 3531) <= 1) {
                        dig();
                    } else if (distance(akqFairyRing.getX(), akqFairyRing.getY() - 5) <= 1) {
                        w(2363, 3531, 1);
                    } else if (distance(akqFairyRing) <= 1) {
                        walk(akqFairyRing.getX(), akqFairyRing.getY() - 5, 0);
                    } else if (distance(salveGraveyardFairyRing) <= 1) {
                        if (widgetOpen(162, 37)) {
                            fairyRing("a", "k", "q");
                        }
                    } else if (distance(salveGraveyardTeleport) <= 4) {
                        if (equip.isEquipped(ItemID.DRAMEN_STAFF))
                            actionObject(29495, MenuAction.GAME_OBJECT_SECOND_OPTION);
                        else {
                            actionItem(ItemID.DRAMEN_STAFF, "wield");
                        }
                    }
                }
                break;
            }
            case 12033: {
                if (!teleported) {
                    teleport(CluesTele.SALVE_GRAVEYARD);
                } else if (targetLoc == null) {
                    if (distance(2594, 2899) <= 1) {
                        dig();
                    } else if (distance(aksFairyRing.getX(), aksFairyRing.getY() - 2) <= 1) {
                        w(2594, 2899, 1);
                    } else if (distance(aksFairyRing) <= 1) {
                        walk(aksFairyRing.getX(), aksFairyRing.getY() - 2, 0);
                    } else if (distance(salveGraveyardFairyRing) <= 1) {
                        if (widgetOpen(162, 37)) {
                            fairyRing("a", "k", "s");
                        }
                    } else if (distance(salveGraveyardTeleport) <= 4) {
                        if (equip.isEquipped(ItemID.DRAMEN_STAFF))
                            actionObject(29495, MenuAction.GAME_OBJECT_SECOND_OPTION);
                        else {
                            actionItem(ItemID.DRAMEN_STAFF, "wield");
                        }
                    }
                }
                break;
            }
            case 19740: {
                if (!teleported) {
                    teleport(CluesTele.ARCEUUS_LIBRARY);
                } else if (targetLoc == null) {
                    if (distance(arceuusTeleport) <= 5) {
                        w(1761, 3850, 1);
                    } else if (distance(1761, 3850) <= 8) {
                        if (!canType(738)) {
                            actionNPC(NpcID.CLERRIS, MenuAction.NPC_FIRST_OPTION);
                        }
                    }
                }
                break;
            }
            case 2823: {
                if (!teleported) {
                    teleport(CluesTele.LUMBRIDGE);
                } else if (targetLoc == null) {
                    if (distance(lumbridgeTeleport) <= 6) {
                        w(3217, 3177, 1);
                    } else if (distance(3217, 3177) <= 1) {
                        dig();
                    }
                }
                break;
            }
            case 2809: {
                if (distance(2479, 3158) <= 1) {
                    dig();
                } else if (distance(ciqFairyRing.getX(), ciqFairyRing.getY() -5) <= 2) {
                    w(2479, 3158, 1);
                } else if (distance(ciqFairyRing) <= 1) {
                    walk(new WorldPoint(ciqFairyRing.getX(), ciqFairyRing.getY() -5, 0), 1, sleepDelay());
                } else if (distance(salveGraveyardFairyRing) <= 1) {
                    if (widgetOpen(162, 37)) {
                        fairyRing("c", "i", "q");
                    }
                } else if (distance(salveGraveyardTeleport) <= 4) {
                    if (equip.isEquipped(ItemID.DRAMEN_STAFF))
                        actionObject(29495, MenuAction.GAME_OBJECT_SECOND_OPTION);
                    else {
                        actionItem(ItemID.DRAMEN_STAFF, "wield");
                    }
                } else {
                    teleport(CluesTele.SALVE_GRAVEYARD);
                }
                break;
            }
            case 12067: {
                if (!teleported) {
                    //teleport(CluesTele.CAMELOT);
                    teleported = true;
                } else if (targetLoc == null) {
                    if (distance(camelotTeleport) <= 6) {
                        w(2822, 3442, 1);
                    } else if (distance(2822, 3442) <= 1) {
                        if (!canType(2)) {
                            actionNPC(NpcID.HICKTON, MenuAction.NPC_FIRST_OPTION);
                        }
                    }
                }
                break;
            }
            case 3610: {
                if (distance(2614, 3209) <= 4) {
                    actionObject(354, MenuAction.GAME_OBJECT_FIRST_OPTION);
                } else if (distance(djpFairyRing.getX(), djpFairyRing.getY() -5) <= 2) {
                    w(2614, 3209, 1);
                } else if (distance(djpFairyRing) <= 1) {
                    walk(new WorldPoint(djpFairyRing.getX(), djpFairyRing.getY() -5, 0), 1, sleepDelay());
                } else if (distance(salveGraveyardFairyRing) <= 1) {
                    if (widgetOpen(162, 37)) {
                        fairyRing("d", "j", "p");
                    }
                } else if (distance(salveGraveyardTeleport) <= 4) {
                    if (equip.isEquipped(ItemID.DRAMEN_STAFF))
                        actionObject(29495, MenuAction.GAME_OBJECT_SECOND_OPTION);
                    else {
                        actionItem(ItemID.DRAMEN_STAFF, "wield");
                    }
                } else {
                    teleport(CluesTele.SALVE_GRAVEYARD);
                }
                break;
            }
            case 12065: {
                if (!teleported) {
                    teleport(CluesTele.FALADOR);
                } else if (targetLoc == null) {
                    if (!canType(18)) {
                        actionNPC(NpcID.KAYLEE, MenuAction.NPC_FIRST_OPTION);
                    } else if (distance(faladorTeleport) <= 4) {
                        walk(2957, 3370, 1);
                    }
                }
                break;
            }
            case 23143: {
                if (!teleported) {
                    teleport(CluesTele.FALADOR);
                } else {
                    if (targetLoc == null) {
                        if (distance(2991, 3383) <= 2) {
                            if (!song) {
                                playSong("devils may care");
                            } else {
                                actionNPC(NpcID.CECILIA, MenuAction.NPC_FIRST_OPTION);
                            }
                        } else {
                            if (distance(2991, 3383) >= 50)
                                teleported = false;
                            else
                                w(2991, 3383, 2);
                        }
                    }
                }
                break;
            }
            case 12063: {
                if (!teleported) {
                    teleport(CluesTele.CRAFTING_GUILD);
                } else if (targetLoc == null) {
                    if (distance(craftingGuildTeleport) <= 5) {
                        w(2940, 3223, 2);
                    } else if (distance(2940, 3223) <= 3) {
                        if (!canType(7)) {
                            actionNPC(NpcID.TARIA, MenuAction.NPC_FIRST_OPTION);
                        }
                    }
                }
                break;
            }
            case 19754: {
                if (!teleported) {
                    teleport(CluesTele.FALADOR);
                } else if (targetLoc == null) {
                    if (distance(faladorTeleport) <= 6) {
                        w(2974, 3342, 2);
                    } else if (distance(2974, 3342) <= 6) {
                        if (!canType(654)) {
                            actionNPC(NpcID.SQUIRE_4737, MenuAction.NPC_FIRST_OPTION);
                        }
                    }
                }
                break;
            }
            case 19774: {
                if (distance(1476, 3566) <= 1) {
                    dig();
                } else if (distance(djrFairyRing.getX(), djrFairyRing.getY() -5) <= 2) {
                    w(1476, 3566, 1);
                } else if (distance(djrFairyRing) <= 1) {
                    walk(new WorldPoint(djrFairyRing.getX(), djrFairyRing.getY() -5, 0), 1, sleepDelay());
                } else if (distance(salveGraveyardFairyRing) <= 1) {
                    if (widgetOpen(162, 37)) {
                        fairyRing("d", "j", "r");
                    }
                } else if (distance(salveGraveyardTeleport) <= 4) {
                    if (equip.isEquipped(ItemID.DRAMEN_STAFF))
                        actionObject(29495, MenuAction.GAME_OBJECT_SECOND_OPTION);
                    else {
                        actionItem(ItemID.DRAMEN_STAFF, "wield");
                    }
                } else {
                    teleport(CluesTele.SALVE_GRAVEYARD);
                }
                break;
            }
            case 7286: {
                if (distance(2536, 3865) <= 1) {
                    dig();
                } else if (distance(cipFairyRing) <= 1) {
                    walk(2536, 3865, 1);
                } else if (distance(salveGraveyardFairyRing) <= 1) {
                    if (widgetOpen(162, 37)) {
                        fairyRing("c", "i", "p");
                    }
                } else if (distance(salveGraveyardTeleport) <= 4) {
                    if (equip.isEquipped(ItemID.DRAMEN_STAFF))
                        actionObject(29495, MenuAction.GAME_OBJECT_SECOND_OPTION);
                    else {
                        actionItem(ItemID.DRAMEN_STAFF, "wield");
                    }
                } else {
                    teleport(CluesTele.SALVE_GRAVEYARD);
                }
                break;
            }
            case 25784: {
                if (!teleported) {
                    teleport(CluesTele.SALVE_GRAVEYARD);
                } else {
                    if (targetLoc == null) {
                        if (distance(1559, 3560) <= 20) {
                            if (!handleDoor(42116, new WorldPoint(1554, 3564, 0), MenuAction.GAME_OBJECT_FIRST_OPTION)) {
                                if (!canType(598)) {
                                    actionNPC(NpcID.DRUNKEN_SOLDIER, MenuAction.NPC_FIRST_OPTION);
                                }
                            }
                        } else if (distance(1459, 3654) <= 1) {
                            w(1559, 3560, 0);
                        } else if (distance(djrFairyRing) <= 1) {
                            walk(1459, 3654, 1);
                        } else if (distance(salveGraveyardFairyRing) <= 1) {
                            if (widgetOpen(162, 37)) {
                                fairyRing("d", "j", "r");
                            }
                        } else if (distance(salveGraveyardTeleport) <= 4) {
                            if (equip.isEquipped(ItemID.DRAMEN_STAFF))
                                actionObject(29495, MenuAction.GAME_OBJECT_SECOND_OPTION);
                            else {
                                actionItem(ItemID.DRAMEN_STAFF, "wield");
                            }
                        }
                    }
                }
                break;
            }
            default:
                utils.sendGameMessage("unhandled clue: " + clue);
                break;
        }
        return AutoClueState.SOLVING_CLUE;
    }

    AutoClueState getState() {
        if (clue == -1 && bank.isOpen() && inv.getItemCount(ItemID.ECLECTIC_IMPLING_JAR, false) == 14) {
            bank.close();
            timeout = 1;
            return AutoClueState.TIMEOUT;
        }
        if (timeout > 0 || (player.isMoving() && targetLoc == null))
            return lastState == AutoClueState.SOLVING_CLUE ? AutoClueState.SOLVING_CLUE : AutoClueState.TIMEOUT;
        if (clue == -1) {
            if (!bank.isOpen()) {
                if (inv.containsItem(ItemID.ECLECTIC_IMPLING_JAR))
                    return AutoClueState.OPEN_JAR;
                return AutoClueState.OPEN_BANK;
            } else {
                if (bank.containsAnyOf(clues)) {
                    return AutoClueState.SETUP_INVENT;
                }
                if (inv.getItemCount(ItemID.ECLECTIC_IMPLING_JAR, false) > 0) {
                    if (inv.containsItem(ItemID.ECLECTIC_IMPLING_JAR))
                        return AutoClueState.OPEN_JAR;
                } else {
                    if (inv.isEmpty())
                        return AutoClueState.WITHDRAW_JARS;
                    else
                        return AutoClueState.DEPOSIT_ALL;
                }
            }
        } else {
            if (emotesToDo != null && !emotesToDo.isEmpty() && stage == 3)
                return AutoClueState.EMOTES;
            if (!hasClueItems()) {
                if (bank.isOpen()) {
                    if (inv.containsItem(ItemID.IMPLING_JAR))
                        return AutoClueState.DEPOSIT_ALL;
                    return AutoClueState.SETUP_INVENT;
                } else {
                    return AutoClueState.OPEN_BANK;
                }
            } else {
                if (bank.isOpen()) {
                    bank.close();
                    return AutoClueState.TIMEOUT;
                }
                if (config.useFood() && client.getBoostedSkillLevel(Skill.HITPOINTS) <= config.eatAt()) {
                    if (inv.containsItem(config.food().getFood()))
                        return AutoClueState.EAT_FOOD;
                }
            }
            if (widgetOpen(231, 5)) {
                song = false;
                targetMenu = new LegacyMenuEntry("Continue", "", 0, MenuAction.WIDGET_CONTINUE, -1, client.getWidget(231, 5).getId(), false);
                if (config.invokes()) {
                    utils.doInvokeMsTime(targetMenu, sleepDelay());
                } else {
                    utils.doActionMsTime(targetMenu, client.getWidget(231, 5).getBounds(), sleepDelay());
                }
            } else if (widgetOpen(217, 5)) {
                song = false;
                targetMenu = new LegacyMenuEntry("Continue", "", 0, MenuAction.WIDGET_CONTINUE, -1, client.getWidget(217, 5).getId(), false);
                if (config.invokes()) {
                    utils.doInvokeMsTime(targetMenu, sleepDelay());
                } else {
                    utils.doActionMsTime(targetMenu, client.getWidget(217, 5).getBounds(), sleepDelay());
                }
            } else if (targetLoc != null && distance(targetLoc) <= rand) {
                log.info("cleared targetLoc");
                targetLoc = null;
            } else if (targetLoc != null) {
                log.info(targetLoc.toString());
                walk.webWalk(targetLoc, rand, player.isMoving(), sleepDelay());
                return AutoClueState.TIMEOUT;
            } else
                return getClueState();
        }
        return AutoClueState.TIMEOUT;
    }

    boolean hasStashItemsEquipped(int c) {
        boolean equipped = true;
        for (int i : getClueItems(c)) {
            if (!equip.isEquipped(i)) {
                equipped = false;
                break;
            }
        }
        return equipped;
    }

    int getStashID(int c) {
        switch (c) {
            case 10262:
                return 28991;
            case 10254:
            case 12063:
                return 28987;
            case 19776:
                return 29006;
            case 10260:
                return 28990;
            case 12025:
            case 10258:
            case 10274:
            case 12057:
                // unknown
                return 0;
            default:
                return -1;
        }
    }

    Set<Integer> getClueItems(int c) {
        switch (c) {
            case 10262:
                return Set.of(1698, 1329, 4315);
            case 10254:
                return Set.of(ItemID.GREEN_ROBE_TOP, ItemID.MITHRIL_PLATELEGS, ItemID.IRON_2H_SWORD);
            case 19776:
                return Set.of(ItemID.ADAMANT_PLATELEGS, ItemID.ADAMANT_PLATEBODY, ItemID.ADAMANT_FULL_HELM);
            case 10260:
                return Set.of(ItemID.GREEN_DHIDE_CHAPS, ItemID.RING_OF_DUELING6, ItemID.MITHRIL_MED_HELM);
            default:
                return Set.of(-1);
        }
    }

    void killAndLoot(int npcID, boolean teleport) {
        NPC target;
        if (!groundItems.isEmpty()) {
            TileItem lootItem = this.getNearestTileItem(groundItems);
            if (lootItem != null) {
                this.clientThread.invoke(() -> this.client.invokeMenuAction("", "", lootItem.getId(), MenuAction.GROUND_ITEM_THIRD_OPTION.getId(), lootItem.getTile().getSceneLocation().getX(), lootItem.getTile().getSceneLocation().getY()));
                teleported = teleport;
            }
        } else {
            target = npcs.findNearestNpc(npcID);
            if (player.getInteracting() == null) {
                if (!equip.isEquipped(config.weaponID())) {
                    actionItem(config.weaponID(), "wear", "wield", "equip");
                    return;
                }
                if (target != null) {
                    actionNPC(target.getId(), MenuAction.NPC_SECOND_OPTION);
                }
            }
        }
    }

    void killAndLoot(int npcID) {
        killAndLoot(npcID, true);
    }

    WorldPoint w(int x, int y, int random) {
        targetLoc = new WorldPoint(x, y, player.getWorldLocation().getPlane());
        rand = random;
        return targetLoc;
    }

    void walk(int x, int y, int random) {
        walk(new WorldPoint(x, y, player.getWorldLocation().getPlane()), random);
    }

    void walk(int x, int y, int z, int random) {
        walk(new WorldPoint(x, y, z), random);
    }

    void walk(WorldPoint location, int random) {
        walk.sceneWalk(location, random, sleepDelay());
    }

    void walk(WorldPoint location, int random, long delay) {
        walk.sceneWalk(location, random, delay);
    }

    int distance(int x, int y) {
        return distance(new WorldPoint(x, y, player.getWorldLocation().getPlane()));
    }
    
    int distance(WorldPoint location) {
        return player.getWorldLocation().distanceTo(location);
    }

    boolean canType(int amount) {
        if (widgetOpen(162, 42) && client.getWidget(162, 42).getText().equals("*")) {
            typeAmount(amount);
            timeout = 5 + tickDelay();
            return true;
        }
        return false;
    }

    void typeAmount(int amount, boolean enter) {
        this.executorService.submit(() -> {
            tUtils.sleep(this.calc.getRandomIntBetweenRange(1000, 1500));
            this.keyb.typeString(String.valueOf(amount));
            tUtils.sleep(this.calc.getRandomIntBetweenRange(80, 250));
            if (enter)
                this.keyb.pressKey(10);
        });
    }

    void typeAmount(int amount) {
        typeAmount(amount, true);
    }

    boolean playSong(String title) {
        client.runScript(915, 13);

        Widget widget = client.getWidget(239, 3);
        targetMenu = null;
        if (widget != null) {
            for (Widget w : widget.getChildren()) {
                if (w.getText().equalsIgnoreCase(title)) {
                    targetMenu = new LegacyMenuEntry("", "", 1, MenuAction.CC_OP, w.getIndex(), WidgetInfo.MUSICTAB_ALL_SONGS.getId(), false);
                }
            }
        }
        if (targetMenu != null) {
            if (config.invokes()) {
                utils.doInvokeMsTime(targetMenu, sleepDelay());
            } else {
                utils.doActionMsTime(targetMenu, widget.getBounds(), sleepDelay());
            }
            client.runScript(915, 3);
            return true;
        }
        return false;
    }

    boolean handleDoor(int id, WorldPoint doorLoc, MenuAction action) {
        WallObject door = objectUtils.findWallObjectWithin(doorLoc, 0, id);
        if (door != null) {
            targetMenu = new LegacyMenuEntry("", "", id, action, door.getLocalLocation().getSceneX(), door.getLocalLocation().getSceneY(), false);
            if (config.invokes()) {
                utils.doInvokeMsTime(targetMenu, sleepDelay());
            } else {
                utils.doActionMsTime(targetMenu, door.getConvexHull().getBounds(), sleepDelay());
            }
            return true;
        } else {
            return false;
        }
    }

    void dig() {
        actionItem(ItemID.SPADE, "dig");
        timeout = 1 + tickDelay();
        teleported = false;
    }

    void fairyRing(String a, String b, String c) {
        a = a.toLowerCase();
        b = b.toLowerCase();
        c = c.toLowerCase();
        int[] rotation = {0, 512, 1024, 1536};
        String[] ring1 = {"a", "b", "c", "d"};
        String[] ring2 = {"i", "j", "k", "l"};
        String[] ring3 = {"p", "q", "r", "s"};
        Widget first = client.getWidget(398, 3);
        Widget second = client.getWidget(398, 4);
        Widget third = client.getWidget(398, 5);
        if (first == null || second == null || third == null)
            return;
        targetMenu = null;
        // first.getRotationY();
        if (first.getRotationY() != (rotation[Arrays.asList(ring1).indexOf(a)])) {
            targetMenu = new LegacyMenuEntry("", "", 1, MenuAction.CC_OP, -1, WidgetInfo.FAIRY_RING_LEFT_ORB_COUNTER_CLOCKWISE.getId(), false);
        } else if (second.getRotationY() != (rotation[Arrays.asList(ring2).indexOf(b)])) {
            targetMenu = new LegacyMenuEntry("", "", 1, MenuAction.CC_OP, -1, WidgetInfo.FAIRY_RING_MIDDLE_ORB_COUNTER_CLOCKWISE.getId(), false);
        } else if (third.getRotationY() != (rotation[Arrays.asList(ring3).indexOf(c)])) {
            targetMenu = new LegacyMenuEntry("", "", 1, MenuAction.CC_OP, -1, WidgetInfo.FAIRY_RING_RIGHT_ORB_COUNTER_CLOCKWISE.getId(), false);
        } else {
            targetMenu = new LegacyMenuEntry("", "", 1, MenuAction.CC_OP, -1, client.getWidget(398, 26).getId(), false);
            timeout = 6 + tickDelay();
        }
        if (!config.invokes()) {
            utils.doActionMsTime(targetMenu, client.getWidget(targetMenu.getParam1()).getBounds(), sleepDelay());
        } else {
            utils.doInvokeMsTime(targetMenu, sleepDelay());
        }
        if (timeout > 2)
            return;
        timeout = 2;
    }

    void teleport(CluesTele tele) {
        switch (tele.type) {
            case SPELL: {
                if (widgetOpen(tele.getParentID(), tele.getChildID())) {
                    targetMenu = new LegacyMenuEntry("", "", tele.getParam0(), MenuAction.CC_OP, -1, client.getWidget(tele.getParentID(), tele.getChildID()).getId(), false);
                    utils.doActionMsTime(targetMenu, client.getWidget(tele.getParentID(), tele.getChildID()).getBounds(), (int) sleepDelay());
                    teleported = true;
                    timeout = 5 + tickDelay();
                } else {
                    shutDown();
                    break;
                }
                break;
            }
            case ITEM: {
                actionItem(tele.getItemID(), "break", "teleport");
                teleported = true;
                timeout = 5 + tickDelay();
                break;
            }
            case JEWELLERY: {
                if (widgetOpen(tele.getParentID(), tele.getChildID())) {
                    targetMenu = new LegacyMenuEntry("", "", 0, MenuAction.WIDGET_CONTINUE, tele.getParam0(), client.getWidget(tele.getParentID(), tele.getChildID()).getId(), false);
                    if (!config.invokes())
                        utils.doActionMsTime(targetMenu, client.getWidget(tele.getParentID(), tele.getChildID()).getBounds(), sleepDelay());
                    else
                        utils.doInvokeMsTime(targetMenu, 0);
                    teleported = true;
                    timeout = 5 + tickDelay();
                } else if (inv.containsItem(getJewellery(tele))) {
                    actionItem(inv.getWidgetItem(getJewellery(tele)).getId(), "rub");
                    timeout = 0;
                    break;
                }
                break;
            }
        }
    }

    Set<Integer> getJewellery(CluesTele tele) {
        Set<Integer> set = null;
        switch (tele) {
            case BURTHORPE:
            case BARBARIAN_OUTPOST:
            case CORPOREAL_BEAST:
            case TEARS_OF_GUTHIX:
            case WINTERTODT_CAMP:
                set = gamesNecklace;
                break;
            case DUEL_ARENA:
            case CASTLE_WARS:
            case FEROX_ENCLAVE:
                set = duelingRing;
                break;
            case EDGEVILLE:
            case KARAMJA:
            case DRAYNOR_VILLAGE:
            case AL_KHARID:
                set = gloryAmulet;
                break;
            case WIZARDS_TOWER:
            case THE_OUTPOST:
            case EAGLES_EYRIE:
                set = passageNecklace;
                break;
            case FISHING_GUILD:
            case MINING_GUILD:
            case CRAFTING_GUILD:
            case COOKING_GUILD:
            case WOODCUTTING_GUILD:
            case FARMING_GUILD:
                set = skillsNecklace;
                break;
        }
        return set;
    }

    boolean hasClueItems() {
        // check items here
        // fuckin logic killing my braincell
        boolean setup = inv.containsItem(gloryAmulet);
        if (!inv.containsItem(duelingRing)) {
            setup = false;
        }
        if (!inv.containsItem(gamesNecklace)) {
            setup = false;
        }
        if (!inv.containsItem(passageNecklace)) {
            setup = false;
        }
        if (!inv.containsItem(skillsNecklace)) {
            setup = false;
        }
        if (!inv.containsItem(ItemID.SPADE)) {
            setup = false;
        }
        if (!inv.containsItem(ItemID.ARCEUUS_LIBRARY_TELEPORT)) {
            setup = false;
        }
        if (!inv.containsItem(ItemID.SALVE_GRAVEYARD_TELEPORT)) {
            setup = false;
        }
        if (!inv.containsItem(ItemID.DRAMEN_STAFF) && !equip.isEquipped(ItemID.DRAMEN_STAFF)) {
            setup = false;
        }
        if (config.useRunePouch() && !inv.containsItem(ItemID.RUNE_POUCH)) {
            setup = false;
        } else {
            if (!inv.containsItem(ItemID.LAW_RUNE)) {
                setup = false;
            }
            if (config.useCombinationRunes()) {
                if (!inv.containsItem(ItemID.MIST_RUNE)) {
                    setup = false;
                }
                if (!inv.containsItem(ItemID.LAVA_RUNE)) {
                    setup = false;
                }
            } else {
                if (!inv.containsItem(ItemID.AIR_RUNE)) {
                    setup = false;
                }
                if (!inv.containsItem(ItemID.WATER_RUNE)) {
                    setup = false;
                }
                if (!inv.containsItem(ItemID.EARTH_RUNE)) {
                    setup = false;
                }
                if (!inv.containsItem(ItemID.FIRE_RUNE)) {
                    setup = false;
                }
            }
        }
        if (!inv.containsItem(clues)) {
            setup = false;
        }
        if (!inv.containsItem(config.weaponID()) && !equip.isEquipped(config.weaponID())) {
            setup = false;
        }
        if (config.useFood() && !inv.containsItem(config.food().getFood())) {
            setup = false;
        }
        if (clue == 7303 && !inv.containsItem(ItemID.METAL_KEY)) {
            setup = false;
        }
        return setup;
    }

    boolean widgetOpen(int parent, int child) {
        return (client.getWidget(parent, child) != null);
    }

    boolean inRegion(Client client, List<Integer> region) {
        return !Arrays.stream(client.getMapRegions()).anyMatch(region::contains);
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

    private boolean actionGroundObject(int id, WorldPoint location, MenuAction action) {
        GroundObject obj = objectUtils.findNearestGroundObject(id);
        if (obj != null) {
            targetMenu = new LegacyMenuEntry("", "", obj.getId(), action, obj.getLocalLocation().getX(), obj.getLocalLocation().getY(), false);
                utils.doTileObjectActionMsTime(obj, action.getId(), sleepDelay());
            return true;
        }
        return false;
    }

    private boolean actionObject(int id, WorldPoint location, MenuAction action) {
        GameObject obj = objectUtils.findNearestGameObjectWithin(location, 0, id);
        if (obj != null) {
            targetMenu = new LegacyMenuEntry("", "", obj.getId(), action, obj.getSceneMinLocation().getX(), obj.getSceneMinLocation().getY(), false);
            if (!config.invokes())
                utils.doGameObjectActionMsTime(obj, action.getId(), sleepDelay());
            else
                utils.doInvokeMsTime(targetMenu, sleepDelay());
            return true;
        }
        return false;
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
        return actionObject(id, action, (int) sleepDelay());
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
        return actionItem(id, (int) sleepDelay(), action);
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
        return actionNPC(id, action, (int) sleepDelay());
    }

    private long sleepDelay() {
        return calc.randomDelay(config.sleepWeightedDistribution(), config.sleepMin(), config.sleepMax(), config.sleepDeviation(), config.sleepTarget());
    }

    private int tickDelay() {
        int tickLength = (int) calc.randomDelay(config.tickDelaysWeightedDistribution(), config.tickDelaysMin(), config.tickDelaysMax(), config.tickDelaysDeviation(), config.tickDelaysTarget());
        log.debug("tick delay for {} ticks", tickLength);
        return tickLength;
    }
}