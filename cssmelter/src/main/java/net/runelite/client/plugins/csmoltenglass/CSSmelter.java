package net.runelite.client.plugins.csmoltenglass;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.tutils.*;
import net.runelite.client.plugins.tutils.game.Game;
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

@Extension
@PluginDependency(tUtils.class)
@PluginDescriptor(
        name = "CS-Smelter",
        description = "Smelt gold and molten glass",
        enabledByDefault = false,
        tags = {"fw", "Tea", "molten", "furnace", "gold"}
)
@Slf4j
public class CSSmelter extends Plugin {
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
    private LegacyInventoryAssistant inventoryAssistant;
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
    private ReflectBreakHandler chinBreakHandler;

    @Inject
    SmelterOverlay overlay;
    @Inject
    SmelterConfig config;

    private Player player;
    private Rectangle bounds;
    LegacyMenuEntry targetMenu;
    SmelterState state;
    SmelterState lastState;
    boolean startPlugin;
    Instant botTimer;
    int timeout;
    private long sleepLength;
    int created;

    public CSSmelter() {
        botTimer = null;
        startPlugin = false;
        state = SmelterState.TIMEOUT;
        lastState = state;
    }

    @Provides
    SmelterConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(SmelterConfig.class);
    }


    private void reset() {
        timeout = 0;
        startPlugin = false;
        botTimer = null;
        state = SmelterState.TIMEOUT;
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
        chinBreakHandler.unregisterPlugin(this);
        reset();
    }

    @Subscribe
    private void onConfigButtonPressed(ConfigButtonClicked configButtonClicked) throws IOException, InterruptedException {
        if (!configButtonClicked.getGroup().equalsIgnoreCase("CSSmelter")) {
            return;
        }
        switch (configButtonClicked.getKey()) {
            case "startPlugin":
                if (!startPlugin) {
                    player = client.getLocalPlayer();
                    if (player != null && client != null) {
                        startPlugin = true;
                        botTimer = Instant.now();
                        state = SmelterState.TIMEOUT;
                        overlayManager.add(overlay);
                        chinBreakHandler.startPlugin(this);
                        timeout = 1;
                        created = 0;
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
        String moltenGlass = "You heat the sand and soda ash in the furnace";
        String gold = "You retrieve a bar of gold from the furnace";
        if (event.getType() == ChatMessageType.SPAM && (event.getMessage().contains(moltenGlass) || event.getMessage().contains(gold))) {
            timeout = config.method() == SmelterConfig.Method.GOLD ? 5 : 2;
            created++;
        }
    }

    @Subscribe
    private void onGameTick(GameTick event) {
        if (!startPlugin || chinBreakHandler.isBreakActive(this))
            return;
        player = client.getLocalPlayer();
        if (player != null && client != null) {
            state = getStates();
            if (config.debug() && state != lastState && state != SmelterState.TIMEOUT) {
                utils.sendGameMessage(this.getClass().getSimpleName() + ": " + state.toString());
            }
            if (state != SmelterState.TIMEOUT)
                lastState = state;
            switch (state) {
                case TIMEOUT:
                    if (timeout <= 0)
                        timeout = 0;
                    else
                        timeout--;
                    break;
                case MAKE_X:
                    Widget widget = null;
                    if (config.method() == SmelterConfig.Method.MOLTEN_GLASS) {
                        widget = client.getWidget(270, 14);
                    } else if (config.method() == SmelterConfig.Method.GOLD) {
                        widget = client.getWidget(270, 19);
                    }
                    if (widget == null)
                        break;
                    targetMenu = new LegacyMenuEntry("Make", "", 1, MenuAction.CC_OP, -1, widget.getId(), false);
                    utils.doActionMsTime(targetMenu, widget.getBounds(), calc.getRandomIntBetweenRange(25, 200));
                    timeout = 3 + tickDelay();
                    break;
                case OPEN_BANK:
                    GameObject booth = objectUtils.findNearestGameObject(config.furnace().getBankID());
                    if (booth != null) {
                        actionObject(booth.getId(),
                                booth.getId() == 34343 ?
                                        MenuAction.GAME_OBJECT_FIRST_OPTION : MenuAction.GAME_OBJECT_SECOND_OPTION);
                    } else {
                        utils.sendGameMessage("Unable to find bank");
                        shutDown();
                        break;
                    }
                    timeout = tickDelay();
                    break;
                case DEPOSIT_ALL:
                    bank.depositAll();
                    timeout = tickDelay();
                    break;
                case WITHDRAW:
                    if (config.method() == SmelterConfig.Method.MOLTEN_GLASS) {
                        if (!inv.containsItem(ItemID.SODA_ASH)) {
                            bank.withdrawItemAmount(ItemID.SODA_ASH, 14);
                            break;
                        }
                        if (!inv.containsItem(ItemID.BUCKET_OF_SAND))
                            bank.withdrawItemAmount(ItemID.BUCKET_OF_SAND, 14);
                    }
                    if (config.method() == SmelterConfig.Method.GOLD) {
                        if (!inv.containsItem(ItemID.GOLD_ORE)) {
                            bank.withdrawAllItem(ItemID.GOLD_ORE);
                            break;
                        }
                    }
                    timeout = tickDelay();
                    break;
                case SMELT_FURNACE:
                    actionObject(config.furnace().getFurnaceID(), MenuAction.GAME_OBJECT_SECOND_OPTION);
                    // click the second option (first is walk here)
                    timeout = tickDelay();
                    if (timeout == 0)
                        timeout = 1;
                    break;
                case DIALOGUE:
                    targetMenu = new LegacyMenuEntry("Continue", "", 0, MenuAction.WIDGET_CONTINUE, -1, client.getWidget(229, 2).getId(), false);
                    if (config.invokes()) {
                        utils.doInvokeMsTime(targetMenu, 0);
                    } else {
                        utils.doActionMsTime(targetMenu, client.getWidget(229, 2).getBounds(), sleepDelay());
                    }
                    break;
                case HANDLE_BREAK:
                    chinBreakHandler.startBreak(this);
                    timeout = 10;
                    break;
                default:
                    timeout = 1;
                    break;
            }
        }
    }

    SmelterState getStates() {
        if (player.isMoving())
            return SmelterState.TIMEOUT;
        if (timeout != 0)
            return SmelterState.TIMEOUT;
        return getState();
    }

    SmelterState getState() {
        if (chinBreakHandler.shouldBreak(this))
            return SmelterState.HANDLE_BREAK;
        if (openDialogue())
            return SmelterState.DIALOGUE;
        if (openMakeX())
            return SmelterState.MAKE_X;
        if (config.method() == SmelterConfig.Method.MOLTEN_GLASS) {
            if (inv.containsItem(ItemID.SODA_ASH) && inv.containsItem(ItemID.BUCKET_OF_SAND))
                return SmelterState.SMELT_FURNACE;
        } else if (config.method() == SmelterConfig.Method.GOLD) {
            if (inv.containsItem(ItemID.GOLD_ORE))
                return SmelterState.SMELT_FURNACE;
        }
        if (bank.isOpen()) {
            if (config.method() == SmelterConfig.Method.MOLTEN_GLASS) {
                if (!inv.isEmpty() && inv.getItemCount(ItemID.SODA_ASH, false) != 14 && inv.getItemCount(ItemID.BUCKET_OF_SAND, false) != 14)
                    return SmelterState.DEPOSIT_ALL;
                if (!inv.containsItem(ItemID.SODA_ASH) || !inv.containsItem(ItemID.BUCKET_OF_SAND))
                    return SmelterState.WITHDRAW;
            } else if (config.method() == SmelterConfig.Method.GOLD) {
                if (inv.containsItem(ItemID.GOLD_BAR))
                    return SmelterState.DEPOSIT_ALL;
                if (!inv.containsItem(ItemID.GOLD_ORE))
                    return SmelterState.WITHDRAW;
            }
        } else
            return SmelterState.OPEN_BANK;
        return SmelterState.TIMEOUT;
    }

    boolean openMakeX() {
        Widget widget = client.getWidget(270, 14);
        return (widget != null && !widget.isHidden());
    }

    boolean openDialogue() {
        Widget widget = client.getWidget(229, 2);
        return (widget != null && !widget.isHidden());
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