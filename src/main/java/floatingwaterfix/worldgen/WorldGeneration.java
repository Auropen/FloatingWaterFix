package floatingwaterfix.worldgen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import floatingwaterfix.FloatingWaterFix;
import floatingwaterfix.config.Config;
import floatingwaterfix.util.MiscUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.common.registry.GameData;

public class WorldGeneration implements IWorldGenerator {
	//Pre-defined relative block-positions, with up excluded
	private final static BlockPos[] POSITIONS = new BlockPos[] 
			{
					BlockPos.ORIGIN.down(), 	                    //0,-1,0
					BlockPos.ORIGIN.north(), BlockPos.ORIGIN.east(),//0,0,-1 & 1,0,0
					BlockPos.ORIGIN.south(), BlockPos.ORIGIN.west()	//0,0,1 & -1,0,0
			};

	//Top layer blocks for specific biomes
	private static Map<String, IBlockState> topLayerBlock = new HashMap<String, IBlockState>();



	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {
		switch (Config.fixMethod) {
		case "SMART":
			floatingWaterFixSmart(random, chunkX, chunkZ, world, chunkProvider, Config.depthCheck);
			break;
		case "FORCE":
			floatingWaterFixForce(random, chunkX, chunkZ, world, chunkProvider, Config.depthCheck, false);
			break;
		case "FORCESECURE":
			floatingWaterFixForce(random, chunkX, chunkZ, world, chunkProvider, Config.depthCheck, true);
			break;
		}
	}

	/**
	 * Fix for when water floats in the air, when caverns generate through rivers and such,
	 * creates sand under the water, suspended by dirt
	 * *SMART* Only checks if the block at level 64 is water, saving performance.
	 */
	public void floatingWaterFixSmart(Random rand, int chunkX, int chunkZ, World world, IChunkProvider chunkProvider, int minWaterDepth) {
		List<BlockPos> posFix = new ArrayList<BlockPos>();
		boolean nearLand = false;
		long sTime = System.currentTimeMillis();

		//Searches for floating water in the chunk
		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				BlockPos pos = new BlockPos(chunkX*16+x,62,chunkZ*16+z);
				if (world.getBlockState(pos) == Blocks.water.getStateFromMeta(0)) {
					for (int y = 62; y >= minWaterDepth; y--) {
						if (world.getBlockState(pos) == Blocks.water.getStateFromMeta(0)) {
							for (BlockPos dir : POSITIONS) {
								if (world.getBlockState(pos.add(dir)).getBlock() == Blocks.air ||
										(world.getBlockState(pos.add(dir)).getBlock().getUnlocalizedName().equals("tile.water") &&
												world.getBlockState(pos.add(dir)) != Blocks.water.getStateFromMeta(0)))
									posFix.add(pos.add(dir));
								else if (!nearLand &&
										Arrays.asList(Blocks.dirt, Blocks.sand, Blocks.grass, Blocks.gravel, Blocks.stone).contains(world.getBlockState(pos.add(dir)).getBlock()))
									nearLand = true;
							}
						}
						else if (world.getBlockState(pos).getBlock() instanceof BlockFalling)
							if (world.getBlockState(pos.down()).getBlock() == Blocks.air)
								posFix.add(pos);
							else break;
					}
				}
			}
		}

		//If the floating water wasn't near land
		if (!nearLand) {
			//Checks if the fix was in the ocean, then it fills the ocean bubbles.
			for (BlockPos pos : posFix)
				if (Config.fillOceanBubbles &&	(world.getBiomeGenForCoords(pos) == BiomeGenBase.ocean ||
				world.getBiomeGenForCoords(pos) == BiomeGenBase.deepOcean))
					fillOceanBubbles(world, rand, pos);
			return;
		}

		long eTime = System.currentTimeMillis();
		if (posFix.size() > 0 && Config.debugMessages)
			FMLLog.info("Number of fixes at (%d,%d): %d, took %d ms", (chunkX*16+8), (chunkZ*16+8), posFix.size(), eTime-sTime);

		//Fixes the chunk for floating water
		for (BlockPos pos : posFix)
			createSeaFloorWall(world, rand, pos);
	}

	/**
	 * Fix for when water floats in the air, when caverns generate through rivers and such,
	 * creates sand under the water, suspended by dirt
	 * *FORCE* Checks all blocks in the desired height, will fill water in different levels.
	 */
	public void floatingWaterFixForce(Random rand, int chunkX, int chunkZ, World world, IChunkProvider chunkProvider, int minWaterDepth, boolean secure) {
		List<BlockPos> posFix = new ArrayList<BlockPos>();
		boolean nearLand = false;
		long sTime = System.currentTimeMillis();

		//Searches for floating water in the chunk
		for (int y = minWaterDepth; y < 63; y++) {
			for (int x = 0; x < 16; x++) {
				for (int z = 0; z < 16; z++) {
					BlockPos pos = new BlockPos(chunkX*16+x,y,chunkZ*16+z);
					if (world.getBlockState(pos) == Blocks.water.getStateFromMeta(0)) {
						for (BlockPos dir : POSITIONS) {
							if ((world.getBlockState(pos.add(dir)).getBlock() == Blocks.air) ||
									(!secure && world.getBlockState(pos.add(dir)).getBlock().getUnlocalizedName().equals("tile.water") &&
											world.getBlockState(pos.add(dir)) != Blocks.water.getStateFromMeta(0))) {
								posFix.add(pos.add(dir));
							}
							else if (!nearLand &&
									Arrays.asList(Blocks.dirt, Blocks.sand, Blocks.grass, Blocks.gravel, Blocks.stone).contains(world.getBlockState(pos.add(dir)).getBlock()))
								nearLand = true;
						}
					}
				}
			}
		}

		//If the floating water wasn't near land
		if (!nearLand) {
			//Checks if the fix was in the ocean, then it fills the ocean bubbles.
			for (BlockPos pos : posFix)
				if (Config.fillOceanBubbles &&	(world.getBiomeGenForCoords(pos) == BiomeGenBase.ocean ||
				world.getBiomeGenForCoords(pos) == BiomeGenBase.deepOcean))
					fillOceanBubbles(world, rand, pos);
			return;
		}

		long eTime = System.currentTimeMillis();
		if (posFix.size() > 0 && Config.debugMessages)
			FMLLog.info("Number of fixes at (%d,%d): %d, took %d ms", (chunkX*16+8), (chunkZ*16+8), posFix.size(), eTime-sTime);

		//Fixes the chunk for floating water
		for (BlockPos pos : posFix)
			createSeaFloorWall(world, rand, pos);
	}

	/**
	 * Creates seafloor or walling, at the desired block position, blocks used
	 * is defined in the config. Creating bottom first and up.
	 */
	private void createSeaFloorWall(World world, Random rand, BlockPos pos) {
		int sandHeight = 3;
		int height = Config.maxHeight;
		IBlockState topLayer = Blocks.sand.getDefaultState();
		IBlockState surfaceBlock = null;

		if (topLayerBlock.containsKey(world.getBiomeGenForCoords(pos).biomeName))
			topLayer = topLayerBlock.get(world.getBiomeGenForCoords(pos).biomeName);

		if (topLayerBlock.containsKey(world.getBiomeGenForCoords(pos).biomeName + "*"))
			surfaceBlock = topLayerBlock.get(world.getBiomeGenForCoords(pos).biomeName + "*");


		if (height < 4)
			sandHeight = height - 1;
		else if (height > 4 && !Config.smooth)
			height = rand.nextInt(height - sandHeight) + 1;

		for (int i = 1; i <= height; i++){
			if (i <= height - sandHeight)
				world.setBlockState(pos.down(height - i), Blocks.stone.getDefaultState());
			else if (surfaceBlock != null && pos.down(height - i).getY() == 62)
				world.setBlockState(pos.down(height - i), surfaceBlock);
			else
				world.setBlockState(pos.down(height - i), topLayer);
		}
	}

	/**
	 * Fills the ocean bubble at the given block position.
	 */
	private void fillOceanBubbles(World world, Random rand, BlockPos pos) {
		IBlockState waterState = Blocks.water.getStateFromMeta(0);
		BlockPos currentPos = pos;
		while (world.getBlockState(currentPos).getBlock() == Blocks.air) {
			world.setBlockState(currentPos, waterState);
			currentPos = currentPos.down();
		}
	}

	/**
	 * Reads the configured top blocks/surface blocks and stores into usable data.
	 */
	public static void compileBiomesConfig() {
		FMLLog.info("Initializing biomes block registering for %s", FloatingWaterFix.NAME);
		for (String s : Config.biomes) {
			String[] data = s.split(";");
			String biomeName = data[0];
			boolean flag = false;
			for (BiomeGenBase bgb : BiomeGenBase.getBiomeGenArray()) {
				if (bgb != null && bgb.biomeName.equals(biomeName)) {
					flag = true;
					break;
				}
			}
			if (!flag) {
				FMLLog.info("Tried to register %s to an invalid biome (%s)", data[1], data[0]);
				continue;
			}
			for (int i = 1; i < data.length; i++) {
				String[] blockData = data[i].split(":");
				if (i == 2) biomeName += "*";
				if (blockData.length > 1) {
					Block b = GameData.getBlockRegistry().getObject(blockData[0] + ":" + blockData[1]);
					FMLLog.info((b == null) 
							? "No Blocks found for %s!" 
									: "Registered %s for biome " + biomeName, data[i]);
					if (b != null)
						topLayerBlock.put(biomeName, (blockData.length == 3) ? b.getStateFromMeta(MiscUtil.stringToInt(blockData[2])) : b.getDefaultState());
				}
			}
		}
	}
}