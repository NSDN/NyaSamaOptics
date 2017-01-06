package club.nsdn.nyasamaoptics;

/**
 * Created by drzzm32 on 2017.1.6.
 */

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.*;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.Mod.EventHandler;
import club.nsdn.nyasamaoptics.Proxy.CommonProxy;

import java.io.PrintStream;
import java.io.FileDescriptor;
import java.io.FileOutputStream;

@Mod(modid = NyaSamaOptics.MODID, version = NyaSamaOptics.VERSION)
public class NyaSamaOptics {

    @Mod.Instance("NyaSamaOptics")
    public static NyaSamaOptics instance;
    public static final String MODID = "NyaSamaOptics";
    public static final String VERSION = "0.1";
    public static final boolean isDebug = false;
    public static PrintStream console = new PrintStream(new FileOutputStream(FileDescriptor.out));

    @SidedProxy(clientSide = "club.nsdn.nyasamaoptics.Proxy.ClientProxy",
                serverSide = "club.nsdn.nyasamaoptics.Proxy.ServerProxy")
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
