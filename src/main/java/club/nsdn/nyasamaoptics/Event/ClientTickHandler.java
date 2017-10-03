package club.nsdn.nyasamaoptics.Event;

import club.nsdn.nyasamaoptics.Util.Util;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

/**
 * Created by drzzm32 on 2017.10.3.
 */
public class ClientTickHandler {
    private static ClientTickHandler instance;

    public static ClientTickHandler instance() {
        if (instance == null)
            instance = new ClientTickHandler();
        return instance;
    }

    @SubscribeEvent
    public void tick(TickEvent.ClientTickEvent event) {
        Util.setTitle();
    }
}
