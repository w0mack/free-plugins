package net.runelite.client.plugins.automedclue;

import net.runelite.client.ui.overlay.OverlayMenuEntry;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import static net.runelite.api.MenuAction.RUNELITE_OVERLAY_CONFIG;
import static net.runelite.client.ui.overlay.OverlayManager.OPTION_CONFIGURE;

public class AutoClueOverlay extends OverlayPanel {

    private final AutoMedClue plugin;
    private final AutoClueConfig config;

    @Inject
    private AutoClueOverlay(final AutoMedClue plugin, final AutoClueConfig config) {
        super(plugin);
        setPosition(OverlayPosition.ABOVE_CHATBOX_RIGHT);
        this.plugin = plugin;
        this.config = config;
        getMenuEntries().add(new OverlayMenuEntry(RUNELITE_OVERLAY_CONFIG, OPTION_CONFIGURE, "AutoMedClue"));
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (!config.showOverlay())
            return null;
        panelComponent.getChildren().clear();

        /* Title and width */
        String title = "AutoMedClue";
        panelComponent.getChildren().add(TitleComponent.builder().text(title).color(Color.YELLOW).build());
        panelComponent.setPreferredSize(new Dimension(graphics.getFontMetrics().stringWidth(title) + 80, 0));
        panelComponent.getChildren().add(LineComponent.builder().left("").build());

        /* Runtime */
        Duration duration = Duration.between(plugin.botTimer, Instant.now());
        panelComponent.getChildren().add(LineComponent.builder().left("Runtime: ").right((duration.toHours() > 0 ? (duration.toHours() + ":") : ("")) + (new SimpleDateFormat("mm:ss").format(new Date(duration.toMillis())))).build());

        /* Content */
        panelComponent.getChildren().add(LineComponent.builder().left("State: ").right(plugin.state.toString().toLowerCase().replace("_", " ")).build());
        panelComponent.getChildren().add(LineComponent.builder().left("Timeout: ").right(Integer.toString(plugin.timeout)).build());
        if (plugin.clue > -1) {
            panelComponent.getChildren().add(LineComponent.builder().left("").build());
            panelComponent.getChildren().add(LineComponent.builder().left("Clue ID: ").right(Integer.toString(plugin.clue)).build());
        }

        /* Render */
        return panelComponent.render(graphics);
    }

}
