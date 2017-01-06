package club.nsdn.nyasamaoptics.Event;

import net.minecraftforge.common.MinecraftForge;

/**
 * Created by drzzm32 on 2017.1.6.
 */

public class EventRegister {

    public static void registerCommon() {
        MinecraftForge.EVENT_BUS.register(ToolHandler.instance());
    }

    public static void registerServer() {

    }

    public static void registerClient() {

    }

}
