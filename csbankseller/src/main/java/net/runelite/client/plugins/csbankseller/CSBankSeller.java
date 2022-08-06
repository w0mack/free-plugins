package net.runelite.client.plugins.csbankseller;

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
import net.runelite.client.plugins.iutils.ui.Chatbox;
import net.runelite.client.plugins.iutils.ui.Equipment;
import net.runelite.client.plugins.iutils.util.LegacyInventoryAssistant;
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
		name = "tBankSeller",
		description = "Sells your bank.",
		enabledByDefault = false,
		tags = {"Tea", "Tea", "fw", "bank", "seller"}
)
@Slf4j
public class CSBankSeller extends Plugin
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
	private LegacyInventoryAssistant inventoryAssistant;
	@Inject
	private ExecutorService executorService;
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
	BankSellerOverlay overlay;
	@Inject
	BankSellerConfig config;

	private Player player;
	private Rectangle bounds;
	LegacyMenuEntry targetMenu;
	BankSellerState state;
	BankSellerState lastState;
	boolean startPlugin;
	Instant botTimer;
	int timeout;
	private long sleepLength;

	Widget[] bankItems;
	List<Widget> items = new ArrayList<>();
	boolean last;

	public CSBankSeller() {
		botTimer = null;
		startPlugin = false;
		state = BankSellerState.TIMEOUT;
		lastState = state;
	}

	@Provides
	BankSellerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(BankSellerConfig.class);
	}


	private void reset() {
		timeout = 0;
		startPlugin = false;
		botTimer = null;
		state = BankSellerState.TIMEOUT;
		lastState = state;
		overlayManager.remove(overlay);
	}

	@Override
	protected void startUp() { }

	@Override
	protected void shutDown(){ reset(); }

	@Subscribe
	private void onConfigButtonPressed(ConfigButtonClicked configButtonClicked) throws IOException, InterruptedException {
		if (!configButtonClicked.getGroup().equalsIgnoreCase("CS-BankSeller")) {
			return;
		}
		switch (configButtonClicked.getKey()) {
			case "startPlugin":
				if (!startPlugin) {
					player = client.getLocalPlayer();
					if (player != null && client != null) {
						startPlugin = true;
						botTimer = Instant.now();
						state = BankSellerState.TIMEOUT;
						overlayManager.add(overlay);
						timeout = 1;
						items.clear();
						bankItems = null;
						last = false;
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
			if (config.debug() && state != lastState && state != BankSellerState.TIMEOUT) {
				utils.sendGameMessage(this.getClass().getName() + ": " + state.toString());
			}
			if (state != BankSellerState.TIMEOUT)
				lastState = state;

			Widget widget;
			WidgetItem item;
			NPC target;
			GameObject gameObj;

			switch (state) {
				case TIMEOUT:
					if (timeout <= 0)
						timeout = 0;
					else
						timeout--;
					break;
				case OPEN_BANK:
					target = npcs.findNearestNpc("Banker");
					if (target != null) {
						actionNPC(target.getId(), MenuAction.NPC_THIRD_OPTION);
						timeout = 2 + tickDelay();
					}
					break;
				case OPEN_GE:
					target = npcs.findNearestNpc("Grand Exchange Clerk");
					if (target != null) {
						actionNPC(target.getId(), MenuAction.NPC_THIRD_OPTION);
						timeout = 2 + tickDelay();
					}
					break;
				case WITHDRAW_ITEMS:
					if (items.size() > 0) {
						int id = items.get(0).getItemId();
						boolean noted = !utils.getCompositionItem(id).isStackable();
						log.info("bank: " + id + ", noted: " + utils.getCompositionItem(id).getLinkedNoteId());

						if (noted && client.getVarbitValue(3958) == 0) {
							targetMenu = new LegacyMenuEntry("", "", 1, MenuAction.CC_OP, -1, client.getWidget(12, 24).getId(), false);
							utils.doInvokeMsTime(targetMenu, 0);
							break;
						}

						bank.withdrawAllItem(id);
						items.remove(0);
					} else {
						log.info("Out of items to withdraw");
						last = true;
						target = npcs.findNearestNpc("Grand Exchange Clerk");
						if (target != null) {
							actionNPC(target.getId(), MenuAction.NPC_THIRD_OPTION);
							timeout = 2 + tickDelay();
						}
					}
					break;
				case GET_ALL_ITEMS:
					widget = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);
					if (widget != null) {
						bankItems = widget.getChildren();
					}
					if (bankItems != null) {
						int price;
						for (Widget w : bankItems) {
							if (w.getItemId() == -1 || w.getItemId() == 995)
								continue;
							if (!utils.getCompositionItem(w.getItemId()).isTradeable())
								continue;
							price = config.stackValue() ?
									(utils.getItemPrice(w.getItemId(), true) * w.getItemQuantity())
									: utils.getItemPrice(w.getItemId(), true);
							if (price <= config.maxSell() && price >= config.minSell()) {
								log.info("Found tradeable item: " + w.getItemId() + ", price: " + price);
								items.add(w);
							}
						}
					}
					break;
				case OFFER_ITEM:
					int slot = -1;
					Widget[] c = Objects.requireNonNull(getChildren(467, 0));
					for (Widget w : c) {
						if (w.getItemId() == -1 || w.getItemId() == 6512 || w.getItemId() == 995) {
							continue;
						}
						slot = getIndex(w, c);
						log.info("Making offer for id: " + w.getItemId() + ", qty: " + w.getItemQuantity() + ", inv-slot: " + slot);
						break;
					}

					if (slot != -1) {
						targetMenu = new LegacyMenuEntry("Offer", "", 1, MenuAction.CC_OP, slot, WidgetInfo.GRAND_EXCHANGE_INVENTORY_ITEMS_CONTAINER.getId(), false);
						utils.doInvokeMsTime(targetMenu, 0);
						timeout = tickDelay();
					}
					break;
				case SELL_ITEM:
					if (Objects.requireNonNull(Objects.requireNonNull(client.getWidget(465, 25)).getChildren())[21].getItemId() != 6512) {
						int price = Integer.parseInt(Objects.requireNonNull(client.getWidget(465, 27)).getText().replace(",", ""));
						int cPrice = Integer.parseInt(Objects.requireNonNull(Objects.requireNonNull(client.getWidget(465, 25)).getChildren())[39].getText().replace(",", "").replace(" coins", "").replace(" coin", ""));
						if (config.instasellAll() && cPrice != 1) {
							targetMenu = new LegacyMenuEntry("", "", 1, MenuAction.CC_OP, 12, client.getWidget(465, 25).getId(), false);
							utils.doInvokeMsTime(targetMenu, 0);
						} else if (!config.instasellAll() && cPrice >= price) {
							targetMenu = new LegacyMenuEntry("", "", 1, MenuAction.CC_OP, 10, client.getWidget(465, 25).getId(), false);
							utils.doInvokeMsTime(targetMenu, 0);
							utils.doInvokeMsTime(targetMenu, 300);
						} else {
							targetMenu = new LegacyMenuEntry("", "", 1, MenuAction.CC_OP, -1, client.getWidget(465, 29).getId(), false);
							utils.doInvokeMsTime(targetMenu, 0);
							timeout = 1 + tickDelay();
						}
					}
					break;
				case COLLECT:
					targetMenu = new LegacyMenuEntry("", "", 2, MenuAction.CC_OP, 0, Objects.requireNonNull(Objects.requireNonNull(client.getWidget(465, 6)).getChildren())[1].getId(), false);
					utils.doInvokeMsTime(targetMenu, 0);
					timeout = 1 + tickDelay();
					break;
				case SELL_FOR_1GP:
					typeAmount(1);
					timeout = tickDelay();
					break;
				default:
					timeout = 1;
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

	private boolean hidden(int parent, int sub, int child) {
		return (Objects.requireNonNull(Objects.requireNonNull(client.getWidget(parent, sub)).getChildren())[child] == null || Objects.requireNonNull(Objects.requireNonNull(client.getWidget(parent, sub)).getChildren())[child].isHidden());
	}

	private boolean hidden(int parent, int sub) {
		return client.getWidget(parent, sub) == null || Objects.requireNonNull(client.getWidget(parent, sub)).isHidden();
	}

	private boolean slotsFilled() {
		if (hidden(465, 7, 18))
			return false;
		if (hidden(465, 8, 18))
			return false;
		if (hidden(465, 9, 18))
			return false;
		if (hidden(465, 10, 18))
			return false;
		if (hidden(465, 11, 18))
			return false;
		if (hidden(465, 12, 18))
			return false;
		if (hidden(465, 13, 18))
			return false;
		if (hidden(465, 14, 18))
			return false;
		return true;
	}

	private boolean geInventEmpty() {
		boolean empty = true;
		Widget[] items = client.getWidget(467, 0).getDynamicChildren();
		for (int slot = 0; slot <= 27; slot++) {
			if (items[slot] != null && items[slot].getItemId() != 6512 && items[slot].getItemId() != 995) {
				return false;
			}
		}
		return empty;
	}

	void typeAmount(int amount, boolean enter) {
		this.executorService.submit(() -> {
			this.keyb.typeString(String.valueOf(amount));
			utils.sleep(this.calc.getRandomIntBetweenRange(80, 250));
			if (enter)
				this.keyb.pressKey(10);
		});
	}

	void typeAmount(int amount) {
		typeAmount(amount, true);
	}

	boolean canType() {
		if (client.getWidget(162, 42) != null && !client.getWidget(162, 42).isHidden()) {
			return true;
		}
		return false;
	}

	BankSellerState getGeState() {
		if (config.instasellAll() && canType())
			return BankSellerState.SELL_FOR_1GP;
		if (slotsFilled() && !hidden(465, 6, 1))
			return BankSellerState.COLLECT;
		if (geInventEmpty() && !last)
			return BankSellerState.OPEN_BANK;
		if (last)
			shutDown();
		if (Objects.requireNonNull(Objects.requireNonNull(client.getWidget(465, 2)).getChildren())[1].getText().toLowerCase().contains("grand exchange: set up offer"))
			return BankSellerState.SELL_ITEM;
		if (Objects.requireNonNull(Objects.requireNonNull(client.getWidget(465, 2)).getChildren())[1].getText().toLowerCase().contains("grand exchange"))
			return BankSellerState.OFFER_ITEM;
		return BankSellerState.TIMEOUT;
	}

	BankSellerState getState() {
		if (timeout != 0)
			return BankSellerState.TIMEOUT;
		if (bank.isOpen()) {
			if (bankItems == null)
				return BankSellerState.GET_ALL_ITEMS;
			if (!inv.isFull())
				return BankSellerState.WITHDRAW_ITEMS;
		}
		if (inv.isEmpty())
			return BankSellerState.OPEN_BANK;
		if (!isGeOpen())
			return BankSellerState.OPEN_GE;
		if (isGeOpen())
			return getGeState();
		return BankSellerState.TIMEOUT;
	}

	private Widget[] getChildren(int a, int b) {
		if (client.getWidget(a, b) == null)
			return null;
		return client.getWidget(a, b).getChildren();
	}

	private Item[] getItems(WidgetInfo container) {
		return client.getItemContainer(container.getId()).getItems();
	}

	private boolean isGeOpen() {
		return (client.getWidget(WidgetInfo.GRAND_EXCHANGE_WINDOW_CONTAINER) != null && !client.getWidget(WidgetInfo.GRAND_EXCHANGE_WINDOW_CONTAINER).isHidden());
	}

	private boolean actionObject(int id, MenuAction action) {
		GameObject obj = objectUtils.findNearestGameObject(id);
		if (obj != null) {
			targetMenu = new LegacyMenuEntry("", "", obj.getId(), action, obj.getSceneMinLocation().getX(), obj.getSceneMinLocation().getY(), false);
			if (!config.invokes())
				utils.doGameObjectActionMsTime(obj, action.getId(), (int)sleepDelay());
			else
				utils.doInvokeMsTime(targetMenu, (int)sleepDelay());
			return true;
		}
		return false;
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
		sleepLength = calc.randomDelay(config.sleepWeightedDistribution(), config.sleepMin(), config.sleepMax(), config.sleepDeviation(), config.sleepTarget());
		return sleepLength;
	}

	private int tickDelay() {
		int tickLength = (int) calc.randomDelay(config.tickDelaysWeightedDistribution(), config.tickDelaysMin(), config.tickDelaysMax(), config.tickDelaysDeviation(), config.tickDelaysTarget());
		log.debug("tick delay for {} ticks", tickLength);
		return tickLength;
	}
}