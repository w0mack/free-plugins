package net.runelite.client.plugins.tbluedragons;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.TileItem;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;

@Slf4j
@Singleton
public class tBlueDragonsTileOverlay extends Overlay {

    private final Client client;
    private final tBlueDragons plugin;
    private final tBlueDragonsConfig config;

    @Inject
    private tBlueDragonsTileOverlay(final Client client, final tBlueDragons plugin, final tBlueDragonsConfig config) {
        this.client = client;
        this.plugin = plugin;
        this.config = config;
        setPosition(OverlayPosition.DYNAMIC);
        setPriority(OverlayPriority.LOW);
        setLayer(OverlayLayer.ABOVE_SCENE);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        /* Render */
        if (!config.showOverlay())
            return null;
        for (TileItem item : plugin.loot) {
            drawTile(graphics, item.getTile().getWorldLocation(), new Color(255, 32, 102, 63), Color.BLACK);
        }
        return null;
    }

    private void drawTile(Graphics2D graphics, WorldPoint tile, Color fillColor, Color borderColor)
    {
        if (tile.getPlane() != client.getPlane())
            return;

        LocalPoint lp = LocalPoint.fromWorld(client, tile);
        if (lp == null)
            return;

        Polygon poly = Perspective.getCanvasTilePoly(client, lp);
        if (poly == null)
            return;
        final Stroke originalStroke = graphics.getStroke();
        graphics.setStroke(new BasicStroke(1));
        graphics.setColor(fillColor);
        graphics.fillPolygon(poly);
        graphics.setColor(borderColor);
        graphics.drawPolygon(poly);
        graphics.setStroke(originalStroke);
    }

}
