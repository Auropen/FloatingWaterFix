package floatingwaterfix;

import floatingwaterfix.config.Config;
import floatingwaterfix.worldgen.WorldGeneration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod(modid = FloatingWaterFix.MODID, name = FloatingWaterFix.NAME, version = FloatingWaterFix.VERSION)
public class FloatingWaterFix
{
    public static final String MODID = "floatingwaterfix";
    public static final String NAME = "Floating Water Fix";
    public static final String VERSION = "1.0";
    
    @EventHandler
	public void preInit(FMLPreInitializationEvent event) {	
		Config.configInit(event);
	}
    
    @EventHandler
    public void init(FMLInitializationEvent event) {
    	GameRegistry.registerWorldGenerator(new WorldGeneration(), 10);
    }
}
