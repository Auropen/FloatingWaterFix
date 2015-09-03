package floatingwaterfix.worldgen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import floatingwaterfix.config.Config;
import floatingwaterfix.util.MiscUtil;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.common.registry.GameData;

public class WorldGeneration implements IWorldGenerator {
	//Pre-defined relative block-positions, with up excluded
	private final static BlockPos[] POSITIONS = new BlockPos[] 
			{
					BlockPos.ORIGIN.down(), 	//0,-1,0
					BlockPos.ORIGIN.north(), BlockPos.ORIGIN.east(),//0,0,-1 & 1,0,0
					BlockPos.ORIGIN.south(), BlockPos.ORIGIN.west()	//0,0,1 & -1,0,0
			};

	//List of chunks that is marked for fixing
	private List<Chunk> checkChunk = new ArrayList<Chunk>(); 
	
	//Top layer blocks for specific biomes
	private static Map<String, IBlockState> topLayerBlock = new HashMap<String, IBlockState>();

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {
		floatingWaterFix(random, chunkX, chunkZ, world, chunkProvider, Config.depthCheck);
	}

	/**
	 * Fix for when water floats in the air, when caverns generate through rivers and such,
	 * creates sand under the water, suspended by dirt
	 */
	private void floatingWaterFix(Random rand, int chunkX, int chunkZ, World world, IChunkProvider chunkProvider, int minWaterDepth) {
		List<BlockPos> posFix = new ArrayList<BlockPos>();
		boolean nearLand = false;

		//Searches for floating water in the chunk
		for (int y = minWaterDepth; y < 63; y++) {
			for (int x = 0; x < 16; x++) {
				for (int z = 0; z < 16; z++) {
					BlockPos pos = new BlockPos(chunkX*16+x,y,chunkZ*16+z);
					if (world.getBlockState(pos) == Blocks.water.getStateFromMeta(0)) {
						for (BlockPos dir : POSITIONS) {
							if (world.getBlockState(pos.add(dir)).getBlock() == Blocks.air) {
								posFix.add(pos.add(dir));
								
								//In case the fix needs to extend to neighbour chunks
								switch (x) {
								case 0: 
									checkChunk.add(chunkProvider.provideChunk(chunkX - 1, chunkZ));
								case 15: 
									checkChunk.add(chunkProvider.provideChunk(chunkX + 1, chunkZ));
								}
								switch (z) {
								case 0: 
									checkChunk.add(chunkProvider.provideChunk(chunkX, chunkZ - 1));
								case 15: 
									checkChunk.add(chunkProvider.provideChunk(chunkX, chunkZ + 1));
								}
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
		if (!nearLand)
			//If the chunk wasn't mark for fixing then skip fixing
			if (!checkChunk.contains(chunkProvider.provideChunk(chunkX, chunkZ)))
				return; 
			else //Cleans up used chunk
				checkChunk.remove(chunkProvider.provideChunk(chunkX, chunkZ));

		//Fixes the chunk for floating water
		for (BlockPos pos : posFix)
			createDirtSand(world, rand, pos);
	}

	private void createDirtSand(World world, Random rand, BlockPos pos) {
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
				world.setBlockState(pos.down(height - i), Blocks.dirt.getDefaultState());
			else if (surfaceBlock != null && pos.down(height - i).getY() == 62)
				world.setBlockState(pos.down(height - i), surfaceBlock);
			else
				world.setBlockState(pos.down(height - i), topLayer);
		}
	}
	
	public static void compileBiomesConfig() {
		for (String s : Config.biomes) {
			String[] data = s.split(";");
			String biomeName = data[0];
			for (int i = 1; i < data.length; i++) {
				String[] blockData = data[i].split(":");
				if (i == 2) biomeName += "*";
				if (blockData.length > 1) {
					Block b = GameData.getBlockRegistry().getObject(blockData[0] + ":" + blockData[1]);
					System.out.println((b == null) 
							? "No Blocks found for " + data[i] + "!" 
							: "Registered toplayer block " + data[i] + " for biome " + biomeName);
					if (b != null)
						topLayerBlock.put(biomeName, (blockData.length == 3) ? b.getStateFromMeta(MiscUtil.stringToInt(blockData[2])) : b.getDefaultState());
				}
			}
		}
	}
}