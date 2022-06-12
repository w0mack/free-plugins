package net.runelite.client.plugins.oofiekittengrower.tasks;


import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.Point;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.Widget;
import net.runelite.client.plugins.iutils.TimeoutUntil;
import net.runelite.client.plugins.oofiekittengrower.OofieKittenGrowerPlugin;
import net.runelite.client.plugins.oofiekittengrower.Task;
import net.runelite.client.plugins.iutils.*;


@Slf4j
public class PetTask extends Task {
    @Override
    public boolean validate() {
        return
                OofieKittenGrowerPlugin.needToPet; //if this boolean defined in CatGrowerPlugin is true
    }

    @Override
    public String getTaskDescription() {
        return "Giving Cat Some Scritches: " + OofieKittenGrowerPlugin.timeout;
    }

    @Override
    public void onGameTick(GameTick event) {
        OofieKittenGrowerPlugin.timeout--;


        Widget dialog = client.getWidget(219, 1);
        NPC kitten = npc.findNearestNpc("Kitten");

        if (kitten != null)
        {
            if (client.getLocalPlayer().getWorldLocation().distanceTo(kitten.getWorldLocation()) > 2)
            {
               pssPssPss();
                return;
            } else {
                if (kitten.getInteracting() == client.getLocalPlayer() && client.getLocalPlayer().getWorldLocation().distanceTo(kitten.getWorldLocation()) < 2)
                {
                    if (dialog == null)
                    {
                        clickKitty();
                        OofieKittenGrowerPlugin.conditionTimeout = new TimeoutUntil(
                                ()-> dialog != null,
                                2);
                        return;
                    }
                    if (dialog != null && dialog.getChildren()[1].getText().toLowerCase().contains("stroke")) {
                        strokeKitty();
                    }
                    OofieKittenGrowerPlugin.needToPet = false;
                    log.info("false pet 1");
                }
                OofieKittenGrowerPlugin.needToPet = false;
                log.info("false pet 2");
            }
            OofieKittenGrowerPlugin.needToPet = false;
        }
    }

    private void clickKitty()
    {
        NPC kitten = npc.findNearestNpc("Kitten");

        targetMenu = new LegacyMenuEntry("", "", kitten.getIndex(), 13, 0, 0, false);
        utils.doActionMsTime(targetMenu, new Point(0, 0), sleepDelay());
        log.info("clicked on pet");
    }

    private void strokeKitty() {
        LegacyMenuEntry targetMenu = new LegacyMenuEntry("", "", 0, 30, 1, 14352385, false);
        utils.doActionMsTime(targetMenu, new Point(0, 0), sleepDelay());
        log.info("Selected stroke pet");
        OofieKittenGrowerPlugin.conditionTimeout = new TimeoutUntil(
                ()-> playerUtils.isAnimating(),
                4);
    }

    LegacyMenuEntry targetMenu;


}