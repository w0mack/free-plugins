package net.runelite.client.plugins.tbluedragons;

import net.runelite.client.ui.overlay.OverlayMenuEntry;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import static net.runelite.api.MenuAction.RUNELITE_OVERLAY_CONFIG;
import static net.runelite.client.ui.overlay.OverlayManager.OPTION_CONFIGURE;

public class tBlueDragonsOverlay extends OverlayPanel {

    private final tBlueDragons plugin;
    private final tBlueDragonsConfig config;
    String name = "";

    @Inject
    private tBlueDragonsOverlay(final tBlueDragons plugin, final tBlueDragonsConfig config) {
        super(plugin);
        setPosition(OverlayPosition.ABOVE_CHATBOX_RIGHT);
        this.plugin = plugin;
        name = plugin.getName();
        this.config = config;
        getMenuEntries().add(new OverlayMenuEntry(RUNELITE_OVERLAY_CONFIG, OPTION_CONFIGURE, name));
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (!config.showOverlay())
            return null;
        panelComponent.getChildren().clear();

        /* Title and width */
        panelComponent.getChildren().add(TitleComponent.builder().text(name).color(Color.YELLOW).build());
        //panelComponent.setBackgroundColor(Color.DARK_GRAY);
        panelComponent.setPreferredSize(new Dimension(graphics.getFontMetrics().stringWidth(name) + 80, 0));
        panelComponent.getChildren().add(LineComponent.builder().left("").build());

        /* Runtime */
        Duration duration = Duration.between(plugin.botTimer, Instant.now());
        panelComponent.getChildren().add(LineComponent.builder().left("Runtime: ").right((duration.toHours() > 0 ? (duration.toHours() + ":") : ("")) + (new SimpleDateFormat("mm:ss").format(new Date(duration.toMillis())))).build());

        /* Content */
        panelComponent.getChildren().add(LineComponent.builder().left("State: ").right(plugin.lastState.toString().toLowerCase().replace("_", " ").replace("handle prayer", "attack dragon")).build());
        //panelComponent.getChildren().add(LineComponent.builder().left("Timeout: ").right(Integer.toString(plugin.timeout)).build());
        if (plugin.kills > 0)
            panelComponent.getChildren().add(LineComponent.builder().left("Dragons slain: ").right(Integer.toString(plugin.kills)).build());
        if (plugin.lootValue > 0)
            panelComponent.getChildren().add(LineComponent.builder().left("Loot: ").right(formatValue()).build());


        /* Render */
        return panelComponent.render(graphics);
    }

    String formatValue() {
        int value = plugin.lootValue;
        String profit = Integer.toString(value);
        if (value >= 1000 && value <= 99999)
            profit = new DecimalFormat("##,###").format(value);
        if (value >= 100000 && value <= 9999999) {
            if (value >= 1000000)
                profit = new DecimalFormat("#,######").format(value);
            profit = profit.substring(0, profit.length() - 3) + "K";
        }
        if (value >= 10000000)
            profit = String.format("%.3fM", value / 1000000.0);
        return profit;
    }

}
