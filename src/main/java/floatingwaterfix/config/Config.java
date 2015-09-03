package floatingwaterfix.config;

import java.io.File;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class Config
{
	//Boolean
	public static boolean smooth;
	
	//Integer
	public static int maxHeight;
	public static int depthCheck;
	
	//String
	public static String[] biomes;
	
	public static void configInit(FMLPreInitializationEvent event) {
		Configuration config = new Configuration(new File("config/FloatingWaterFix.cfg"));
		config.load();
		smooth = config.get("Generation", "SmoothWaterBottom", true, "Set to true if the bottom of the water should be generated at a fixed height.").getBoolean();
		maxHeight = config.get("Generation", "WaterBottomHeight", 5, "The number of layers of dirt/sand under the bottom of the water.").getInt();
		depthCheck = config.get("Generation", "DeepestWaterCheck", 52, "The depth of the \"y\" check, and checks up to y-level 63. Keep it under 63, \nthe lower number is the more performance it uses on chunk generation").getInt();
		biomes = config.get("Generation", "BiomeTopLayerBlock", 
				new String[] 
						{
								"Swampland;minecraft:dirt;minecraft:grass",
								"Forest;minecraft:dirt;minecraft:grass",
								"Mesa;minecraft:sand:1"
						}, "Configure the blocks that should be placed as top layers for specific biomes, instead of sand as default.\nFirst block given is the top 3 layers, and if second block is specified, it will be placed at water surface.\nSyntax: Biome;modid:block[:meta];[modid:block[:meta]]").getStringList();
		config.save();
	}
}
