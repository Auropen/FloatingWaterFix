package floatingwaterfix.config;

import java.io.File;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class Config
{
	//Boolean
	public static boolean smooth;
	public static boolean fillOceanBubbles;
	public static boolean debugMessages;
	
	//Integer
	public static int maxHeight;
	public static int depthCheck;
	
	//String
	public static String[] biomes;
	public static String fixMethod;
	
	public static void configInit(FMLPreInitializationEvent event) {
		Configuration config = new Configuration(new File("config/FloatingWaterFix.cfg"));
		config.load();
		smooth = config.get("Generation", "SmoothWaterBottom", true, "Set to true if the bottom of the water should be generated at a fixed height.").getBoolean();
		debugMessages = config.get("Generation", "Debug", false, "Set to true to write out in console the location of a fix and the number of blocks that was fixed").getBoolean();
		fillOceanBubbles = config.get("Generation", "FillOceanBubbles", true, "Set to true if the mod should fill ocean bubbles.").getBoolean();
		maxHeight = config.get("Generation", "WaterBottomHeight", 5, "The number of layers of stone/sand under the bottom of the water.").getInt();
		depthCheck = config.get("Generation", "DeepestWaterCheck", 45, "The depth of the \"y\" check, and checks up to y-level 62. Keep it under 62, \nthe lower number is the more performance it uses on chunk generation").getInt();
		biomes = config.get("Generation", "BiomeTopLayerBlock", 
				new String[] 
						{
								"Swampland;minecraft:dirt;minecraft:grass",
								"Forest;minecraft:dirt;minecraft:grass",
								"Savanna;minecraft:dirt;minecraft:grass",
								"Plains;minecraft:dirt;minecraft:grass",
								"Stone Beach;minecraft:stone",
								"Mesa Plateau;minecraft:stained_hardened_clay:1;minecraft:sand:1",
								"Mesa;minecraft:stained_hardened_clay:1;minecraft:sand:1"
						}, "Configure the blocks that should be placed as top layers for specific biomes, instead of sand as default.\nFirst block given is the top 3 layers, and if second block is specified, it will be placed at water surface level.\nSyntax: Biome;modid:block[:meta][;modid:block[:meta]]").getStringList();
		fixMethod = config.getString("FixMethod", "Generation", "SMART", "Following values are accepted:"
				+ "\nSMART\t\t Only checks water that starts from y = 62 and then checks down to next non water source block"
				+ "\nFORCE\t\t Force checks all blocks from in the whole chunk between y = 62 to the configured depthCheck for water, CAUTION: Will try to fix unbroken water also"
				+ "\nFORCESECURE\t Force checks all blocks from in the whole chunk between y = 62 to the configured depthCheck, still WIP"
				+ "\nThe way it should fix the floating waters.");
		config.save();
	}
}
