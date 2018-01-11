package club.nsdn.nyasamaoptics;

/**
 * Created by drzzm32 on 2017.1.6.
 */

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.*;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.Mod.EventHandler;
import club.nsdn.nyasamaoptics.proxy.CommonProxy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = NyaSamaOptics.MODID, version = NyaSamaOptics.VERSION)
public class NyaSamaOptics {

    @Mod.Instance("NyaSamaOptics")
    public static NyaSamaOptics instance;
    public static final String MODID = "NyaSamaOptics";
    public static final String VERSION = "0.2";
    public static final boolean isDebug = false;
    public static Logger log = LogManager.getLogger(MODID);

    @SidedProxy(clientSide = "club.nsdn.nyasamaoptics.proxy.ClientProxy",
                serverSide = "club.nsdn.nyasamaoptics.proxy.ServerProxy")
    public static CommonProxy proxy;

    public static NyaSamaOptics getInstance() { return instance; }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        proxy.preInit(event);
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        proxy.init(event);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        proxy.postInit(event);
    }

}
