package net.runelite.client.plugins.oofiekittengrower;

import net.runelite.client.config.Button;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

@ConfigGroup("OofieKittenGrower")
public interface OofieKittenGrowerConfig extends Config
{

    @ConfigSection(
        keyName = "instructionsTitle",
        name = "Instructions",
        description = "",
        position = 0
    )
    String instructionsTitle = "instructionsTitle";

    @ConfigItem(
            keyName = "instruction",
            name = "",
            description = "Instructions. Don't enter anything into this field",
            position = 1,
            title = "instructionsTitle"
    )
    default String instruction()
    {
        return "Updated by Gerwin#7413\nOriginally by Oofie\nDefault food is Karambwanji.";
    }

    @ConfigItem(
            keyName = "catFood",
            name = "Cat Food",
            description = "What shall you feed'eth the kitten?",
            position = 2
    )
    default int catFood() { return 3150;}

    @ConfigItem(
        keyName = "enableUI",
        name = "Enable UI",
        description = "Enable to turn on in game UI",
        position = 95
    )
    default boolean enableUI()
    {
        return true;
    }

    @ConfigItem(
        keyName = "startButton",
        name = "Start/Stop",
        description = "Test button that changes variable value",
        position = 100
    )
    default Button startButton()
    {
        return new Button();
    }

    @ConfigSection(
            name = "Sleep Delays",
            description = "",
            position = 111,
            keyName = "sleepDelays",
            closedByDefault = true
    )
    String sleepDelays = "Sleep Delays";

    @Range(
            min = 0,
            max = 550
    )
    @ConfigItem(
            keyName = "sleepMin",
            name = "Sleep Min",
            description = "",
            position = 2,
            section = sleepDelays
    )
    default int sleepMin() {
        return 20;
    }

    @Range(
            min = 0,
            max = 550
    )
    @ConfigItem(
            keyName = "sleepMax",
            name = "Sleep Max",
            description = "",
            position = 3,
            section = sleepDelays
    )
    default int sleepMax() {
        return 200;
    }

    @Range(
            min = 0,
            max = 550
    )
    @ConfigItem(
            keyName = "sleepTarget",
            name = "Sleep Target",
            description = "",
            position = 4,
            section = sleepDelays
    )
    default int sleepTarget() {
        return 50;
    }

    @Range(
            min = 0,
            max = 550
    )
    @ConfigItem(
            keyName = "sleepDeviation",
            name = "Sleep Deviation",
            description = "",
            position = 5,
            section = sleepDelays
    )
    default int sleepDeviation() {
        return 10;
    }

    @ConfigItem(
            keyName = "sleepWeightedDistribution",
            name = "Sleep Weighted Distribution",
            description = "Shifts the random distribution towards the lower end at the target, otherwise it will be an even distribution",
            position = 6,
            section = sleepDelays
    )
    default boolean sleepWeightedDistribution() {
        return false;
    }

    @ConfigSection(
            name = "Tick Delays",
            description = "",
            position = 112,
            keyName = "tickDelays",
            closedByDefault = true
    )
    String tickDelays = "Tick Delays";

    @Range(
            min = 0,
            max = 10
    )
    @ConfigItem(
            keyName = "tickDelaysMin",
            name = "Game Tick Min",
            description = "",
            position = 8,
            section = tickDelays
    )
    default int tickDelayMin() {
        return 1;
    }

    @Range(
            min = 0,
            max = 10
    )
    @ConfigItem(
            keyName = "tickDelaysMax",
            name = "Game Tick Max",
            description = "",
            position = 9,
            section = tickDelays
    )
    default int tickDelayMax() {
        return 3;
    }

    @Range(
            min = 0,
            max = 10
    )
    @ConfigItem(
            keyName = "tickDelaysTarget",
            name = "Game Tick Target",
            description = "",
            position = 10,
            section = tickDelays
    )
    default int tickDelayTarget() {
        return 2;
    }

    @Range(
            min = 0,
            max = 10
    )
    @ConfigItem(
            keyName = "tickDelaysDeviation",
            name = "Game Tick Deviation",
            description = "",
            position = 11,
            section = tickDelays
    )
    default int tickDelaysDeviation() {
        return 1;
    }

    @ConfigItem(
            keyName = "tickDelaysWeightedDistribution",
            name = "Game Tick Weighted Distribution",
            description = "Shifts the random distribution towards the lower end at the target, otherwise it will be an even distribution",
            position = 12,
            section = tickDelays
    )
    default boolean tickDelayWeightedDistribution() {
        return false;
    }
}
