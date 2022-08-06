package net.runelite.client.plugins.csstathider;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.Skill;
import net.runelite.api.events.BeforeRender;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import org.pf4j.Extension;

import javax.inject.Inject;
import java.util.Objects;

@Extension
@PluginDescriptor(
		name = "tStatHider",
		description = "Hides your stats",
		enabledByDefault = false,
		tags = {"Tea", "Tea", "fw", "stat", "hide", "skills", "stream"}
)
@Slf4j
public class CSStatHider extends Plugin
{
	@Inject
	private Client client;
	@Inject
	private ClientThread clientThread;
	@Inject
	StatHiderConfig config;

	Widget hp;
	Widget pray;
	Widget run;
	Widget spec;
	Widget skills;

	private Player player;

	@Provides
	StatHiderConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(StatHiderConfig.class);
	}


	private void reset() {
		hp = null;
		pray = null;
		run = null;
		spec = null;
		skills = null;
	}

	@Override
	protected void startUp() {
		if (client.getWidget(160, 5) != null)
			hp = client.getWidget(160, 5);
		if (client.getWidget(WidgetInfo.MINIMAP_PRAYER_ORB_TEXT) != null)
			pray = client.getWidget(WidgetInfo.MINIMAP_PRAYER_ORB_TEXT);
		if (client.getWidget(WidgetInfo.MINIMAP_RUN_ORB_TEXT) != null)
			run = client.getWidget(WidgetInfo.MINIMAP_RUN_ORB_TEXT);
		if (client.getWidget(160, 32) != null)
			spec = client.getWidget(160, 32);
		if (client.getWidget(WidgetInfo.SKILLS_CONTAINER) != null)
			skills = client.getWidget(WidgetInfo.SKILLS_CONTAINER);
		setLevels();
	}

	@Override
	protected void shutDown(){ reset(); }

	void setLevels() {
		int n = config.level();
		if (hp != null && config.setHP())
			hp.setText("<col=00ff00>" + config.hpLevel() + "</col>");
		if (pray != null && config.setPray())
			pray.setText("<col=00ff00>" + config.prayLevel() + "</col>");
		if (run != null && config.setRun())
			run.setText("<col=00ff00>" + config.runLevel() + "</col>");
		if (spec != null && config.setSpec())
			spec.setText("<col=00ff00>" + config.specLevel() + "</col>");
		if (skills != null && config.setSkills()) {
			for (int i = 1; i <= 23; i++) {
				if (client.getWidget(320, i) != null) {
					if (i == 5 && !config.setPray())
						continue;
					if (i == 9 && !config.setHP())
						continue;
					Objects.requireNonNull(client.getWidget(320, i)).getChild(3).setText("<col=ffff00>" + (i == 5 ? config.prayLevel() : (i == 9 ? config.hpLevel() : n)) + "</col>");
					Objects.requireNonNull(client.getWidget(320, i)).getChild(4).setText("<col=ffff00>" + (i == 5 ? config.prayLevel() : (i == 9 ? config.hpLevel() : n)) + "</col>");
				}
			}
			Objects.requireNonNull(client.getWidget(320, 27)).setText("<col=ffff00>Total level:<br>" + (n * 21
					+ (config.setPray() ? config.prayLevel() : client.getRealSkillLevel(Skill.PRAYER))
					+ (config.setHP() ? config.hpLevel() : client.getRealSkillLevel(Skill.HITPOINTS)))
					+ "</col>");
		}
	}

	@Subscribe
	private void on(ConfigChanged event) {
		if (!event.getGroup().equals("CSStatHider"))
			return;
		setLevels();
	}

	@Subscribe
	private void on(BeforeRender event) {
		if (client.getGameState() != GameState.LOGGED_IN)
			return;
		setLevels();
	}

	@Subscribe
	private void onGameTick(GameTick event)
	{
		player = client.getLocalPlayer();
		if (player != null && client != null) {
			if (hp == null)
				hp = client.getWidget(160, 5);
			if (pray == null)
				pray = client.getWidget(WidgetInfo.MINIMAP_PRAYER_ORB_TEXT);
			if (run == null)
				run = client.getWidget(WidgetInfo.MINIMAP_RUN_ORB_TEXT);
			if (spec == null)
				spec = client.getWidget(160, 32);
			if (skills == null)
				skills = client.getWidget(WidgetInfo.SKILLS_CONTAINER);
		}
	}
}