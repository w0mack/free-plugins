package net.runelite.client.plugins.csardyminer;

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
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutorService;

@Extension
@PluginDependency(iUtils.class)
@PluginDescriptor(
		name = "CS-ArdyMiner",
		description = "Powermines at the iron spot east of Ardougne",
		enabledByDefault = false,
		tags = {"Tea", "fw", "cs", "ardy", "mine", "iron"}
)
@Slf4j
public class CSArdyMiner extends Plugin
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
	private ReflectBreakHandler chinBreakHandler;

	@Inject
	ArdyMinerOverlay overlay;
	@Inject
	ArdyMinerConfig config;

	private Player player;
	private Rectangle bounds;
	LegacyMenuEntry targetMenu;
	ArdyMinerState state;
	ArdyMinerState lastState;
	boolean startPlugin;
	Instant botTimer;
	int timeout;
	private long sleepLength;

	boolean drop;
	int dropAt;

	public CSArdyMiner() {
		botTimer = null;
		startPlugin = false;
		state = ArdyMinerState.TIMEOUT;
		lastState = state;
	}

	@Provides
	ArdyMinerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ArdyMinerConfig.class);
	}


	private void reset() {
		timeout = 0;
		startPlugin = false;
		botTimer = null;
		state = ArdyMinerState.TIMEOUT;
		lastState = state;
		overlayManager.remove(overlay);
		chinBreakHandler.stopPlugin(this);
	}

	@Override
	protected void startUp() {
		chinBreakHandler.registerPlugin(this);
	}

	@Override
	protected void shutDown(){
		chinBreakHandler.unregisterPlugin(this);
		reset();
	}

	@Subscribe
	private void onConfigButtonPressed(ConfigButtonClicked configButtonClicked) throws IOException, InterruptedException {
		if (!configButtonClicked.getGroup().equalsIgnoreCase("CSArdyMiner")) {
			return;
		}
		switch (configButtonClicked.getKey()) {
			case "startPlugin":
				if (!startPlugin) {
					player = client.getLocalPlayer();
					if (player != null && client != null) {
						startPlugin = true;
						botTimer = Instant.now();
						state = ArdyMinerState.TIMEOUT;
						overlayManager.add(overlay);
						chinBreakHandler.startPlugin(this);
						timeout = 1;
						drop = false;
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
		if (!startPlugin || chinBreakHandler.isBreakActive(this))
			return;
		player = client.getLocalPlayer();
		if (player != null && client != null) {
			state = getStates();
			if (state != ArdyMinerState.TIMEOUT)
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
					timeout = 10;
					break;
				case WALK_TO_SPOT:
					walk.sceneWalk(new WorldPoint(2692, 3329, 0), 0, sleepDelay());
					break;
				case MINING:
					WorldPoint p = player.getWorldLocation();

					GameObject rock = null;
					GameObject rockE = objectUtils.getGameObjectAtWorldPoint(new WorldPoint(p.getX() + 1, p.getY(), p.getPlane()));
					GameObject rockS = objectUtils.getGameObjectAtWorldPoint(new WorldPoint(p.getX(), p.getY() - 1, p.getPlane()));
					GameObject rockW = objectUtils.getGameObjectAtWorldPoint(new WorldPoint(p.getX() - 1, p.getY(), p.getPlane()));

					List<Integer> iron = Arrays.asList(11364, 11365);

					if (rockE.getId() == iron.get(0))
						rock = rockE;
					if (rock == null && rockS.getId() == iron.get(0))
						rock = rockS;
					if (rock == null && rockW.getId() == iron.get(1))
						rock = rockW;

					if (rock != null)
						actionObject(rock, MenuAction.GAME_OBJECT_FIRST_OPTION, sleepDelay());
					break;
				case DROP_ITEMS:
					if (inv.containsItem(ItemID.IRON_ORE))
						drop();
					else
						drop = false;
					break;
				default:
					timeout = 1;
					break;
			}
		}
	}

	ArdyMinerState getStates() {
		if (chinBreakHandler.shouldBreak(this))
			return ArdyMinerState.HANDLE_BREAK;
		if (timeout != 0 || player.isMoving() || player.getAnimation() != -1)
			return ArdyMinerState.TIMEOUT;
		if (distance(2692, 3329) > 0)
			return ArdyMinerState.WALK_TO_SPOT;
		if (!inv.containsItem(ItemID.IRON_ORE))
			dropAt = (calc.getRandomIntBetweenRange(0, inv.getEmptySlots()));
		if (drop || inv.isFull() || inv.getEmptySlots() <= dropAt) {
			drop = true;
			return ArdyMinerState.DROP_ITEMS;
		}
		if (!inv.isFull())
			return ArdyMinerState.MINING;
		return ArdyMinerState.TIMEOUT;
	}

	int distance(int x, int y) {
		return distance(new WorldPoint(x, y, player.getWorldLocation().getPlane()));
	}

	int distance(WorldPoint location) {
		return player.getWorldLocation().distanceTo(location);
	}

	void drop() {
		List<Integer> slots = new ArrayList<>(List.of());
		Widget[] c = Objects.requireNonNull(getChildren(149, 0));
		for (Widget w : c) {
			if (w.getItemId() == ItemID.IRON_ORE
					|| w.getItemId() == ItemID.UNCUT_SAPPHIRE
					|| w.getItemId() == ItemID.UNCUT_EMERALD
					|| w.getItemId() == ItemID.UNCUT_RUBY) {
				slots.add(getIndex(w, c));
			}
		}
		int loop = Math.min(inv.getItemCount(ItemID.IRON_ORE, false), calc.getRandomIntBetweenRange(2, 3));
		int t = loop;
		for(int i : slots) {
			if (loop > 0) {
				targetMenu = new LegacyMenuEntry("", "", 1, MenuAction.CC_OP, i, WidgetInfo.INVENTORY.getId(), false);
				utils.doInvokeMsTime(targetMenu, calc.getRandomIntBetweenRange(0, (600/t)));
				loop--;
			} else {
				break;
			}
		}
	}

	private int getIndex(Object o, Object[] arr) {
		for (int i = 0; i < arr.length; i++) {
			if (arr[i].equals(o))
				return i;
		}
		return -1;
	}

	private Widget[] getChildren(int a, int b) {
		if (client.getWidget(a, b) == null)
			return null;
		return Objects.requireNonNull(client.getWidget(a, b)).getChildren();
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

	private boolean actionObject(GameObject obj, MenuAction action, long delay) {
		if (obj != null) {
			targetMenu = new LegacyMenuEntry("", "", obj.getId(), action, obj.getSceneMinLocation().getX(), obj.getSceneMinLocation().getY(), false);
			utils.doInvokeMsTime(targetMenu, delay);
			timeout = 1;
			return true;
		}
		return false;
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

	private long sleepDelay() {
		return (long)calc.getRandomIntBetweenRange(0, 350);
	}

	private int tickDelay() {
		return calc.getRandomIntBetweenRange(0, 3);
	}
}