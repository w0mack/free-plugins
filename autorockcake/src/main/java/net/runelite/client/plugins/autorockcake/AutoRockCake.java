package net.runelite.client.plugins.autorockcake;

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
import java.util.Objects;
import java.util.Set;
import java.util.stream.IntStream;

@Extension
@PluginDependency(iUtils.class)
@PluginDescriptor(
        name = "tNightmareZone",
        description = "Keeps you going in NMZ",
        tags = {"Tea", "Tea", "rock", "cake", "locator", "orb", "nmz", "overload", "ovl"}
)
@Slf4j
public class AutoRockCake extends Plugin {
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
    private InventoryUtils inv;
    @Inject
    LegacyInventoryAssistant inventoryAssistant;
    @Inject
    private PrayerUtils pray;
    @Inject
    private MouseUtils mouse;
    ChatMessage message;

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
    List<Integer> bankRegion;

    boolean abs;

    Set<Integer> cakes = Set.of(ItemID.DWARVEN_ROCK_CAKE_7510, ItemID.LOCATOR_ORB);
    Set<Integer> overload = Set.of(ItemID.OVERLOAD_1, ItemID.OVERLOAD_2, ItemID.OVERLOAD_3, ItemID.OVERLOAD_4);
    Set<Integer> absorption = Set.of(ItemID.ABSORPTION_1, ItemID.ABSORPTION_2, ItemID.ABSORPTION_3, ItemID.ABSORPTION_4);

    public AutoRockCake() {
        bankRegion = Arrays.asList();
        botTimer = null;
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
        if (!configButtonClicked.getGroup().equalsIgnoreCase("AutoRockCake")) {
            return;
        }
        switch (configButtonClicked.getKey()) {
            case "startPlugin":
                if (!startPlugin) {
                    startPlugin = true;
                    timeout = 2;
                    botTimer = Instant.now();
                    state = PluginState.TIMEOUT;
                    overlayManager.add(overlay);
                    abs = false;
                } else {
                    reset();
                }
                break;
        }
    }

    @Subscribe
    private void onChatMessage(ChatMessage event) {
        if (event.getType() == ChatMessageType.CONSOLE)
            return;

        if (event.getMessage().contains("You drink some of your overload potion.")
                && event.getType() == ChatMessageType.SPAM)
            timeout = 12 + (calc.getRandomIntBetweenRange(1, 4));
        if (event.getMessage().contains("You can't absorb"))
            abs = false;
        if (event.getMessage().contains("Oh dear, you are dead!"))
            shutDown();
    }

    boolean atNMZ() {
        IntStream stream = Arrays.stream(client.getMapRegions());
        List<Integer> regions = List.of(9033);
        Objects.requireNonNull(regions);
        return stream.anyMatch(regions::contains);
    }

    @Subscribe
    private void onGameTick(GameTick event) {
        if (!startPlugin)
            return;
        player = client.getLocalPlayer();
        if (player != null && client != null) {
            state = getState();
            if (config.onlyNMZ() && !atNMZ())
                state = PluginState.NOT_IN_NMZ;
            if (state != PluginState.TIMEOUT)
                lastState = state;
            if (client.getVarbitValue(Varbits.NMZ_ABSORPTION) < 100)
                abs = true;
            if (client.getVarbitValue(Varbits.NMZ_ABSORPTION) >= 900)
                abs = false;
            switch (state) {
                case NOT_IN_NMZ:
                case TIMEOUT:
                    if (timeout <= 0)
                        timeout = 0;
                    else
                        timeout--;
                    break;
                case LOWER_HP:
                    log.info("lowering hp");
                    WidgetItem item = inv.getWidgetItem(cakes);
                    if (item != null) {
                        useItem(item, "guzzle", "feel");
                    }
                    timeout = 0;
                    break;
                case DRINK_OVERLOAD:
                    log.info("drinking overload");
                    useItem(inv.getWidgetItem(overload), "drink");
                    break;
                case DRINK_ABSORPTION:
                    log.info("drinking absorption");
                    useItem(inv.getWidgetItem(absorption), "drink");
                    break;
                case HANDLE_PRAYER:
                    Widget widget = client.getWidget(10485775);
                    assert widget != null;
                    targetMenu = new LegacyMenuEntry("", "Quick-prayers", 1, MenuAction.CC_OP.getId(), -1, widget.getId(), false);
                    switch (config.prayMethod()) {
                        case CUSTOM:
                            if (pray.isActive(config.prayer().getPrayer()))
                                toggle(config.prayer().getPrayer(), 0);
                            if (!pray.isActive(config.prayer().getPrayer()))
                                toggle(config.prayer().getPrayer(), calc.getRandomIntBetweenRange(200, 400));
                            break;
                        case QUICK_PRAYERS:
                            if (pray.isQuickPrayerActive())
                                utils.doInvokeMsTime(targetMenu, 0);
                            if (!pray.isQuickPrayerActive())
                                utils.doInvokeMsTime(targetMenu, calc.getRandomIntBetweenRange(200, 400));
                            break;
                    }
                    break;
            }
        }
    }

    void toggle(Prayer p, long sleepDelay) {
        Widget widget = client.getWidget(p.getWidgetInfo());
        targetMenu = new LegacyMenuEntry("", "", 1, MenuAction.CC_OP.getId(), -1, widget.getId(), false);
        utils.doInvokeMsTime(targetMenu, sleepDelay);
    }

    boolean canLowerHP() {
        if (config.lowerHP() && client.getBoostedSkillLevel(Skill.HITPOINTS) >= 2) {
            if (config.onlyNMZ() && !atNMZ())
                return false;
            if ((config.drinkOvl() && atNMZ()) && client.getVarbitValue(3955) == 0)
                return false;
            return true;
        } else {
            return false;
        }
    }

    PluginState getState() {
        if (timeout != 0)
            return PluginState.TIMEOUT;
        if (atNMZ() && config.drinkOvl() && client.getVarbitValue(3955) == 0 && inv.containsItem(overload))
            return PluginState.DRINK_OVERLOAD;
        if (atNMZ() && config.drinkAbs() && abs && inv.containsItem(absorption))
            return PluginState.DRINK_ABSORPTION;
        if (canLowerHP())
            return PluginState.LOWER_HP;
        if (client.getBoostedSkillLevel(Skill.PRAYER) >= 1 && config.prayMethod() != PluginConfig.PrayType.NONE)
            return PluginState.HANDLE_PRAYER;
        return PluginState.TIMEOUT;
    }

    private void useItem(WidgetItem item, String... actions) {
        if (item != null) {
            targetMenu = inventoryAssistant.getLegacyMenuEntry(item.getId(), actions);
            int sleepTime = calc.getRandomIntBetweenRange(25, 200);
            utils.doActionMsTime(targetMenu, item.getCanvasBounds(), sleepTime);
        }
    }

}