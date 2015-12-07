package floatingwaterfix;

import floatingwaterfix.config.Config;
import floatingwaterfix.worldgen.LoadedWorldGeneration;
import floatingwaterfix.worldgen.WorldGeneration;
import net.minecraftforge.fml.common.FMLCommonHandler;
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
	public static final String VERSION = "1.4";

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {	
		Config.configInit(event);
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		WorldGeneration.compileBiomesConfig();
		WorldGeneration wGen = new WorldGeneration();
		GameRegistry.registerWorldGenerator(wGen, 0);
		if (Config.checkLoadedChunks) {
			LoadedWorldGeneration lWGen = new LoadedWorldGeneration(wGen);
			FMLCommonHandler.instance().bus().register(lWGen);
		}
	}
}
