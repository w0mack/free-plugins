package net.runelite.client.plugins.autochompykiller;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.widgets.Widget;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

@Extension
@PluginDependency(tUtils.class)
@PluginDescriptor(
        name = "AutoChompyKiller",
        description = "Kills da birdy",
        tags = {"Tea", "fw", "chompy", "jubbly"}
)
@Slf4j
public class AutoChompyKiller extends Plugin {
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
    private ExecutorService executorService;
    @Inject
    private LegacyInventoryAssistant inventoryAssistant;

    @Inject
    ChompyOverlay overlay;
    @Inject
    ChompyConfig config;

    private Player player;
    private Rectangle bounds;
    LegacyMenuEntry targetMenu;
    ChompyState state;
    ChompyState lastState;
    boolean startPlugin;
    Instant botTimer;
    int timeout;
    private long sleepLength;

    List<Integer> bellows;
    int kills;
    int charges;
    int maxCharges;
    int invToads;
    int maxInvToads;
    boolean needToMove;
    boolean fillingBellows;
    boolean attack;
    NPC targetChompy;
    List<NPC> chompies;

    static List<Integer> regions;

    public AutoChompyKiller() {
        botTimer = null;
        startPlugin = false;
        state = ChompyState.TIMEOUT;
        lastState = state;
    }

    @Provides
    ChompyConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(ChompyConfig.class);
    }


    private void reset() {
        timeout = 0;
        startPlugin = false;
        botTimer = null;
        state = ChompyState.TIMEOUT;
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
        if (!configButtonClicked.getGroup().equalsIgnoreCase("AutoChompyKiller")) {
            return;
        }
        switch (configButtonClicked.getKey()) {
            case "startPlugin":
                if (!startPlugin) {
                    player = client.getLocalPlayer();
                    if (player != null && client != null) {
                        startPlugin = true;
                        botTimer = Instant.now();
                        state = ChompyState.TIMEOUT;
                        overlayManager.add(overlay);
                        regions = Arrays.asList(9263, 9264);
                        bellows = Arrays.asList(ItemID.OGRE_BELLOWS_3, ItemID.OGRE_BELLOWS_2, ItemID.OGRE_BELLOWS_1, ItemID.OGRE_BELLOWS);
                        charges = countCharges();
                        maxCharges = countMaxCharges();
                        invToads = inv.getItemCount(ItemID.BLOATED_TOAD, false);
                        maxInvToads = 3;
                        needToMove = false;
                        fillingBellows = false;
                        attack = false;
                        chompies = new ArrayList<>();
                        targetChompy = null;
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
        if (event.getMessage().toLowerCase().contains("there is a bloated toad already")) {
            needToMove = true;
        }
    }

    @Subscribe
    private void onGameStateChanged(GameStateChanged event) {
        if (!startPlugin)
            return;

        GameState gm = event.getGameState();
        if (gm == GameState.HOPPING || gm == GameState.LOADING) {
            reset();
            player = client.getLocalPlayer();
            if (player != null && client != null) {
                startPlugin = true;
                botTimer = Instant.now();
                state = ChompyState.TIMEOUT;
                overlayManager.add(overlay);
                regions = Arrays.asList(9263, 9264);
                bellows = Arrays.asList(ItemID.OGRE_BELLOWS_3, ItemID.OGRE_BELLOWS_2, ItemID.OGRE_BELLOWS_1, ItemID.OGRE_BELLOWS);
                charges = countCharges();
                maxCharges = countMaxCharges();
                invToads = inv.getItemCount(ItemID.BLOATED_TOAD, false);
                maxInvToads = 3;
                needToMove = false;
                fillingBellows = false;
                attack = false;
                chompies = new ArrayList<>();
                targetChompy = null;
                timeout = 5;
            }
        }
    }

    @Subscribe
    private void onGameTick(GameTick event) {
        if (!startPlugin)
            return;
        player = client.getLocalPlayer();
        if (player != null && client != null) {
            state = getState();
            if (config.debug() && state != lastState && state != ChompyState.TIMEOUT) {
                utils.sendGameMessage(this.getClass().getSimpleName() + ": " + state.toString());
            }
            if (state != ChompyState.TIMEOUT)
                lastState = state;
            if (player.isMoving())
                return;
            if (!inRegion(client, regions)) {
                utils.sendGameMessage("Please move to the spot south-west of Castle Wars.");
                shutDown();
                return;
            }

            charges = countCharges();

            if (kills >= config.stopAfter()) {
                log.info("Reached target kills");
                shutDown();
                return;
            }

            if (targetChompy != null) {
                if (npcs.findNearestNpcWithin(targetChompy.getWorldLocation(), 0, Set.of(targetChompy.getId())) == null) {
                    chompies.remove(targetChompy);
                    targetChompy = null;
                }
            }

            if (targetChompy != null && targetChompy.getId() == 1476) {
                chompies.remove(targetChompy);
                targetChompy = null;
            }

            if (targetChompy == null && !chompies.isEmpty()) {
                targetChompy = chompies.get(0);
                attack = true;
            }

            WidgetItem item;
            NPC npc;
            Widget widget;
            GameObject obj;
            int i;
            switch (state) {
                case TIMEOUT:
                    if (timeout <= 0)
                        timeout = 0;
                    else
                        timeout--;
                    break;
                case FILL_BELLOWS:
                    obj = objectUtils.findNearestGameObject(ObjectID.SWAMP_BUBBLES);
                    if (obj == null)
                        break;
                    actionObject(ObjectID.SWAMP_BUBBLES, MenuAction.GAME_OBJECT_FIRST_OPTION);
                    fillingBellows = true;
                    timeout = 3 + tickDelay();
                    break;
                case DROP_TOAD:
                    actionItem(ItemID.BLOATED_TOAD, "drop");
                    timeout = 2;
                    break;
                case CHASE_TOAD:
                    actionNPC(NpcID.SWAMP_TOAD, MenuAction.NPC_FIRST_OPTION);
                    timeout = 3 + tickDelay();
                    break;
                case MOVE_TILE:
                    needToMove = false;
                    walk.sceneWalk(player.getWorldLocation(), 4, sleepLength);
                    timeout = tickDelay();
                    break;
                case ATTACK_CHOMPY:
                    if (player.getAnimation() == -1 && targetChompy != null) {
                        actionNPC(targetChompy.getId(), MenuAction.NPC_FIFTH_OPTION, tickDelay());
                        attack = false;
                        timeout = 2;
                    }
                    break;
                default:
                    timeout = 1;
                    break;
            }
        }
    }

    @Subscribe
    private void onItemSpawned(ItemSpawned event) {
        if (!startPlugin)
            return;
    }

    @Subscribe
    private void onItemDespawned(ItemDespawned event) {
        if (!startPlugin)
            return;
    }

    @Subscribe
    private void onNpcSpawned(NpcSpawned event) {
        if (!startPlugin)
            return;
        final NPC npc = event.getNpc();
        if (npc.getName() == null) {
            return;
        }

        if (npc.getId() == NpcID.CHOMPY_BIRD) {
            chompies.add(npc);
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

        if (npc.getId() == NpcID.CHOMPY_BIRD) {
            chompies.remove(npc);
        }
    }

    @Subscribe
    private void onNpcChanged(NpcChanged event) {
        if (!startPlugin)
            return;
        final NPC npc = event.getNpc();
        if (npc.getName() == null)
            return;
        if (chompies.contains(npc)) {
            chompies.remove(npc);
            kills++;
            timeout = tickDelay();
        }
    }

    @Subscribe
    private void onAnimationChanged(AnimationChanged event) {
        if (!startPlugin)
            return;
        Actor actor = event.getActor();

        if (actor == player) {
            if (actor.getAnimation() == 1026) {
                timeout = 3;
                return;
            }
        } else if (event.getActor().getAnimation() == 6762) {
            targetChompy = null;
        }
    }

    private int calculateHealth(NPC target, Integer maxHealth) {
        if (target == null || target.getName() == null) {
            return -1;
        }
        int healthScale = target.getHealthScale();
        int healthRatio = target.getHealthRatio();
        if (healthRatio < 0 || healthScale <= 0 || maxHealth == null) {
            return -1;
        }
        return (int) (maxHealth * healthRatio / healthScale + 0.5f);
    }

    ChompyState getState() {
        if (targetChompy != null) {
            if (attack || player.getInteracting() != targetChompy)
                return ChompyState.ATTACK_CHOMPY;
        } else {
            if (fillingBellows) {
                if (charges != maxCharges)
                    return ChompyState.TIMEOUT;
                else
                    fillingBellows = false;
            }
            if (timeout != 0)
                return ChompyState.TIMEOUT;
            if (needToMove)
                return ChompyState.MOVE_TILE;
            if (countCharges() == 0 && inv.containsItem(ItemID.OGRE_BELLOWS))
                return ChompyState.FILL_BELLOWS;
            if (inv.containsItem(ItemID.BLOATED_TOAD))
                return ChompyState.DROP_TOAD;
            if (charges > 0)
                return ChompyState.CHASE_TOAD;
        }
        return ChompyState.TIMEOUT;
    }

    boolean inRegion(Client client, List<Integer> region) {
        return Arrays.stream(client.getMapRegions()).anyMatch(region::contains);
    }

    int countCharges() {
        int charge = 0;
        charge += (inv.getItemCount(ItemID.OGRE_BELLOWS_3, false) * 3);
        charge += (inv.getItemCount(ItemID.OGRE_BELLOWS_2, false) * 2);
        charge += (inv.getItemCount(ItemID.OGRE_BELLOWS_1, false));
        if (config.debug())
            utils.sendGameMessage("Current bellows charges: " + charge);
        return charge;
    }

    int countMaxCharges() {
        int charge = 0;
        charge += inv.getItemCount(ItemID.OGRE_BELLOWS_3, false);
        charge += inv.getItemCount(ItemID.OGRE_BELLOWS_2, false);
        charge += inv.getItemCount(ItemID.OGRE_BELLOWS_1, false);
        charge += inv.getItemCount(ItemID.OGRE_BELLOWS, false);
        charge *= 3;
        return charge;
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
        sleepLength = calc.randomDelay(config.sleepWeightedDistribution(), config.sleepMin(), config.sleepMax(), config.sleepDeviation(), config.sleepTarget());
        return sleepLength;
    }

    private int tickDelay() {
        int tickLength = (int) calc.randomDelay(config.tickDelaysWeightedDistribution(), config.tickDelaysMin(), config.tickDelaysMax(), config.tickDelaysDeviation(), config.tickDelaysTarget());
        log.debug("tick delay for {} ticks", tickLength);
        return tickLength;
    }
}