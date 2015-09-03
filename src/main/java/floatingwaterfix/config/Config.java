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
	
	public static void configInit(FMLPreInitializationEvent event) {
		Configuration config = new Configuration(new File("config/FloatingWaterFix.cfg"));
		config.load();
		smooth = config.get("Generation", "SmoothWaterBottom", false, "Set if the bottom of the water should be smooth.").getBoolean();
		maxHeight = config.get("Generation", "WaterBottomHeight", 5, "The number of layers of dirt/sand under the bottom of the water. Should be at least 4, as the top 3 layers is sand.").getInt();
		depthCheck = config.get("Generation", "DeepestWaterCheck", 55, "The depth of the \"y\" check, and checks up to y-level 63. Keep it under 63, the lower number is the more performance it uses on chunk generation").getInt();
		config.save();
	}
}
