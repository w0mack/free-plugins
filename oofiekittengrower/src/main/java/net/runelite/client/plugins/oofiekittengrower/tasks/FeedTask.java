package net.runelite.client.plugins.oofiekittengrower.tasks;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.Point;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.plugins.iutils.TimeoutUntil;
import net.runelite.client.plugins.oofiekittengrower.OofieKittenGrowerPlugin;
import net.runelite.client.plugins.oofiekittengrower.Task;
import net.runelite.client.plugins.iutils.*;
import net.runelite.api.MenuAction.*;
import net.runelite.api.MenuEntry;

import java.awt.*;



@Slf4j
public class FeedTask extends Task {
    Rectangle clickBounds;


    @Override
    public boolean validate()
    {
        return OofieKittenGrowerPlugin.needToFeed; //if this boolean defined in CatGrowerPlugin is true
    }

    @Override
    public String getTaskDescription() { return "Feeding Kitten: " + OofieKittenGrowerPlugin.timeout; }

    @Override
    public void onGameTick(GameTick event)
    {
        OofieKittenGrowerPlugin.timeout--;

        NPC kitten = npc.findNearestNpc("Kitten"); //Using NPC String name to avoid making list of all the Kittens
        WidgetItem catfood = inventory.getWidgetItem(config.catFood());

        if (kitten != null) //make sure kitten exists
        {
            if (client.getLocalPlayer().getWorldLocation().distanceTo(kitten.getWorldLocation()) > 2)
            {
                pssPssPss();
                return;
            } else {
                if (inventory.containsItem(config.catFood())) //If we have food
                {
                    if (kitten.getInteracting() == client.getLocalPlayer()) //If Kitten is visible + interacting with us
                    {
                        log.info("feeding cat 5");

                        targetMenu = new LegacyMenuEntry("", "", kitten.getIndex(), MenuAction.WIDGET_TARGET, 0, 0, false);
                        menu.setModifiedEntry(targetMenu, catfood.getId(), catfood.getIndex(), MenuAction.WIDGET_TARGET_ON_NPC.getId());
                        mouse.delayMouseClick(kitten.getConvexHull().getBounds(), sleepDelay());
                        OofieKittenGrowerPlugin.conditionTimeout = new TimeoutUntil(
                                ()-> playerUtils.isAnimating(),
                                        2);
                        OofieKittenGrowerPlugin.needToFeed = false;
                    }
                }
                //OTHERWISEEEEEEEE
                else if (!inventory.containsItem(config.catFood())) //If we don't have any food, pick up cat and shutdown
                {
                    targetMenu = new LegacyMenuEntry("Pick-up", "<col=ffff00>Kitten", kitten.getIndex(), MenuAction.NPC_FIRST_OPTION.getId(), 0, 0, false);
                    utils.doActionMsTime(targetMenu, new Point(0, 0),  sleepDelay());
                    OofieKittenGrowerPlugin.timeout = 1 + tickDelay();
                    OofieKittenGrowerPlugin.startBot = false;
                    log.info("No Food in Inventory.");
                }
                OofieKittenGrowerPlugin.needToFeed = false;
                log.info("false feed 3");
            }
        }
//        else if (!inventory.containsItem(config.catFood())) //If we don't have any food, pick up cat and shutdown
//        {
//            targetMenu = new LegacyMenuEntry("", "", kitten.getIndex(), MenuAction.NPC_FIRST_OPTION.getId(), 0, 0, false);
//            utils.doActionMsTime(targetMenu, new Point(0, 0),  sleepDelay());
//            OofieKittenGrowerPlugin.timeout = 1 + tickDelay();
//            OofieKittenGrowerPlugin.startBot = false;
//            log.info("No Food in Inventory.");
//        }
//        OofieKittenGrowerPlugin.needToFeed = false;
//        log.info("false feed 3");
    }

    LegacyMenuEntry targetMenu;
}