package net.runelite.client.plugins.autobasalt;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
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
import net.runelite.client.plugins.iutils.ui.Chatbox;
import net.runelite.client.plugins.iutils.util.LegacyInventoryAssistant;
import net.runelite.client.ui.overlay.OverlayManager;
import org.pf4j.Extension;

import javax.inject.Inject;
import java.awt.*;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@Extension
@PluginDependency(iUtils.class)
@PluginDescriptor(
        name = "tBasalt",
        description = "Automatically mines basalt and salt",
        tags = {"Tea", "Tea", "basalt", "salt", "weiss", "mining", "mine", "miner"}
)
@Slf4j
public class AutoBasalt extends Plugin {
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
    private ObjectUtils objectUtils;
    @Inject
    private CalculationUtils calc;
    @Inject
    private NPCUtils npcUtils;
    @Inject
    private BankUtils bank;
    @Inject
    private Chatbox chat;
    @Inject
    private LegacyInventoryAssistant inventoryAssistant;
    @Inject
    private MenuUtils menu;
    @Inject
    public MouseUtils mouse;

    @Inject
    PluginOverlay overlay;
    @Inject
    PluginConfig config;

    private Player player;
    private Rectangle bounds;
    LegacyMenuEntry targetMenu;
    PluginState state;
    PluginState lastState;
    boolean startPlugin;
    Instant botTimer;
    int timeout;
    List<Integer> mineRegion;
    List<Integer> weissRegion;


    int basalt;
    int urtSalt;
    int efhSalt;
    int teSalt;
    int snowflake;
    int descStairs;
    int ascStairs;

    public AutoBasalt() {
        mineRegion = Arrays.asList(11425);
        weissRegion = Arrays.asList(11325);
        botTimer = null;
        snowflake = NpcID.SNOWFLAKE;
        descStairs = 33234;
        ascStairs = 33261;
        startPlugin = false;
        state = PluginState.TIMEOUT;
        lastState = state;
    }

    @Provides
    PluginConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(PluginConfig.class);
    }


    private void reset() {
        timeout = 0;
        startPlugin = false;
        botTimer = null;
        state = PluginState.TIMEOUT;
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
    private void onConfigButtonPressed(ConfigButtonClicked configButtonClicked) {
        if (!configButtonClicked.getGroup().equalsIgnoreCase("AutoBasalt")) {
            return;
        }
        switch (configButtonClicked.getKey()) {
            case "startPlugin":
                if (!startPlugin) {
                    startPlugin = true;
                    botTimer = Instant.now();
                    state = PluginState.TIMEOUT;
                    overlayManager.add(overlay);
                } else {
                    reset();
                }
                break;
        }
    }

    @Subscribe
    private void onGameTick(GameTick event) {
        if (!startPlugin)
            return;
        player = client.getLocalPlayer();
        if (player != null && client != null) {
            state = getState();
            if (config.debug() && state != lastState && state != PluginState.TIMEOUT) {
                utils.sendGameMessage(this.getClass().getName() + ": " + state.toString());
            }
            if (state != PluginState.TIMEOUT)
                lastState = state;
            if (player.isMoving())
                return;
            switch (state) {
                case TIMEOUT:
                    if (timeout <= 0)
                        timeout = 0;
                    else
                        timeout--;
                    break;
                case MINE:
                    timeout = calc.getRandomIntBetweenRange(6, 10);
                    if (player.getAnimation() != -1) {
                        break;
                    }
                    mine(config.mine().getObjectId());
                    break;
                case ASCEND_STAIRS:
                    actionObject(ascStairs, MenuAction.GAME_OBJECT_FIRST_OPTION);
                    timeout = calc.getRandomIntBetweenRange(1, 4);
                    break;
                case DESCEND_STAIRS:
                    actionObject(descStairs, MenuAction.GAME_OBJECT_FIRST_OPTION);
                    timeout = calc.getRandomIntBetweenRange(1, 4);
                    break;
                case NOTE_BASALT:
                    if (inv.containsItem(ItemID.BASALT)) {
                        itemOnNPC(ItemID.BASALT, snowflake);
                        timeout = calc.getRandomIntBetweenRange(2, 4);
                    }
                    break;
                case USE_ITEMS:
                    itemOnItem(ItemID.BASALT, ItemID.TE_SALT);
                    timeout = calc.getRandomIntBetweenRange(2, 4);
                    break;
                case MAKE_TELES:
                    if (inv.getItemCount(ItemID.EFH_SALT, true) >= 3
                            && inv.getItemCount(ItemID.URT_SALT, true) >= 3) {
                        targetMenu = new LegacyMenuEntry("Make", "", 1, MenuAction.CC_OP, -1, client.getWidget(270, config.craftTele() == PluginConfig.CraftTele.ICY_BASALT ? 14 : 15).getId(), false);
                        utils.doActionMsTime(targetMenu, client.getWidget(270, config.craftTele() == PluginConfig.CraftTele.ICY_BASALT ? 14 : 15).getBounds(), calc.getRandomIntBetweenRange(25, 200));
                    } else {
                        targetMenu = new LegacyMenuEntry("Make", "", 1, MenuAction.CC_OP, -1, client.getWidget(270, 14).getId(), false);
                        utils.doActionMsTime(targetMenu, client.getWidget(270, 14).getBounds(), calc.getRandomIntBetweenRange(25, 200));
                    }
                    break;
                default:
                    timeout = 1;
                    break;
            }
        }
    }

    PluginState getState() {
        if (timeout > 0 || player.isMoving())
            return PluginState.TIMEOUT;
        if (inRegion(client, mineRegion)) {
            if (player.getWorldLocation().equals(new WorldPoint(2845, 10351, 0))) {
                walk.sceneWalk(new WorldPoint(player.getWorldLocation().getX(), player.getWorldLocation().getY() - 5, 0), 4, 0);
                return PluginState.TIMEOUT;
            }
            if (config.mine() == PluginConfig.Mine.BASALT) {
                if (!inv.isFull())
                    return PluginState.MINE;
                else {
                    if (!config.makeTele())
                        return PluginState.ASCEND_STAIRS;
                    else {
                        if (chat.chatState() == Chatbox.ChatState.MAKE) {
                            return PluginState.MAKE_TELES;
                        } else {
                            if (inv.getItemCount(ItemID.BASALT, false) >= 1
                                    && inv.getItemCount(ItemID.TE_SALT, true) >= 1
                                    && inv.getItemCount((config.craftTele() == PluginConfig.CraftTele.ICY_BASALT ? ItemID.EFH_SALT : ItemID.URT_SALT), true) >= 3) {
                                return PluginState.USE_ITEMS;
                            } else {
                                utils.sendGameMessage("You do not have the materials to make this item.");
                                shutDown();
                                return null;
                            }
                        }
                    }
                }
            } else {
                return PluginState.MINE;
            }
        } else if (inRegion(client, weissRegion)) {
            if (config.mine() == PluginConfig.Mine.BASALT) {
                if (inv.isFull())
                    return PluginState.NOTE_BASALT;
                else
                    return PluginState.DESCEND_STAIRS;
            } else {
                return PluginState.DESCEND_STAIRS;
            }
        }
        return PluginState.TIMEOUT;
    }

    void mine(int objectId) {
        GameObject rock = objectUtils.findNearestGameObject(objectId);
        MenuAction action = MenuAction.GAME_OBJECT_FIRST_OPTION;
        if (rock != null) {
            targetMenu = new LegacyMenuEntry("", "", rock.getId(), action, rock.getSceneMinLocation().getX(), rock.getSceneMinLocation().getY(), false);
            if (!config.invokes())
                utils.doGameObjectActionMsTime(rock, action.getId(), calc.getRandomIntBetweenRange(25, 300));
            else
                utils.doInvokeMsTime(targetMenu, 0);
        }
    }

    boolean inRegion(Client client, List<Integer> region) {
        return Arrays.stream(client.getMapRegions()).anyMatch(region::contains);
    }

    void itemOnItem(int id1, int id2) {
        WidgetItem item1 = inv.getWidgetItem(id1);
        WidgetItem item2 = inv.getWidgetItem(id2);
        if (item1 == null || item2 == null)
            return;

        client.setSelectedSpellWidget(WidgetInfo.INVENTORY.getId());
        client.setSelectedSpellChildIndex(inventoryAssistant.getWidgetItem(Arrays.asList(id1)).getIndex());
        client.setSelectedSpellItemId(inventoryAssistant.getWidgetItem(Arrays.asList(id1)).getWidget().getItemId());

        targetMenu = new LegacyMenuEntry("", "", id2, MenuAction.WIDGET_TARGET_ON_WIDGET, inventoryAssistant.getWidgetItem(Arrays.asList(id2)).getIndex(), WidgetInfo.INVENTORY.getId(), false);
        utils.doActionMsTime(targetMenu, inventoryAssistant.getWidgetItem(Arrays.asList(id2)).getCanvasBounds(), calc.getRandomIntBetweenRange(25, 300));

    }

    void itemOnNPC(int itemId, int npcId) {
        WidgetItem item = inv.getWidgetItem(itemId);
        if (item == null)
            return;

        NPC npc = npcUtils.findNearestNpc(npcId);
        if (npc == null)
            return;

        targetMenu = new LegacyMenuEntry("", "", npc.getIndex(), MenuAction.WIDGET_TARGET, 0, 0, false);
        menu.setModifiedEntry(targetMenu, item.getId(), item.getIndex(), MenuAction.WIDGET_TARGET_ON_NPC.getId());
        mouse.delayMouseClick(npc.getConvexHull().getBounds(), calc.getRandomIntBetweenRange(25, 300));
    }

    void itemOnObject(int itemID, int objectID) {
        WidgetItem item = inv.getWidgetItem(itemID);
        if (item == null)
            return;

        GameObject gameObject = objectUtils.findNearestGameObject(objectID);
        if (gameObject == null)
            return;

        targetMenu = new LegacyMenuEntry("", "", gameObject.getId(), MenuAction.WIDGET_TARGET, 0, 0, false);
        menu.setModifiedEntry(targetMenu, item.getId(), item.getIndex(), MenuAction.WIDGET_TARGET_ON_GAME_OBJECT.getId());
        mouse.delayMouseClick(gameObject.getSceneMinLocation(), calc.getRandomIntBetweenRange(25, 300));
    }

    void actionObject(int id, MenuAction action) {
        GameObject obj = objectUtils.findNearestGameObject(id);
        if (obj != null) {
            targetMenu = new LegacyMenuEntry("", "", obj.getId(), action, obj.getSceneMinLocation().getX(), obj.getSceneMinLocation().getY(), false);
            if (!config.invokes())
                utils.doGameObjectActionMsTime(obj, action.getId(), calc.getRandomIntBetweenRange(25, 300));
            else
                utils.doInvokeMsTime(targetMenu, 0);
        }
    }
}