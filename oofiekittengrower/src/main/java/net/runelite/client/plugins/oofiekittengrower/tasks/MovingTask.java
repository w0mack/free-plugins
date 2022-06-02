package net.runelite.client.plugins.oofiekittengrower.tasks;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Player;
import net.runelite.api.events.GameTick;
import net.runelite.client.plugins.oofiekittengrower.Task;
import net.runelite.client.plugins.oofiekittengrower.OofieKittenGrowerPlugin;

@Slf4j
public class MovingTask extends Task
{

    @Override
    public boolean validate()
    {
        return playerUtils.isMoving(OofieKittenGrowerPlugin.beforeLoc);
    }

    @Override
    public String getTaskDescription()
    {
        return OofieKittenGrowerPlugin.status;
    }

    @Override
    public void onGameTick(GameTick event)
    {
        Player player = client.getLocalPlayer();
        if (player != null)
        {
            playerUtils.handleRun(20, 30);
            OofieKittenGrowerPlugin.timeout = tickDelay();
        }
    }
}