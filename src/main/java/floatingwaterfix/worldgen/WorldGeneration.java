package floatingwaterfix.worldgen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import floatingwaterfix.config.Config;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.fml.common.IWorldGenerator;

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
		for (int y = minWaterDepth; y < 64; y++) {
			for (int x = 0; x < 16; x++) {
				for (int z = 0; z < 16; z++) {
					BlockPos pos = new BlockPos(chunkX*16+x,y,chunkZ*16+z);
					if (world.getBlockState(pos) == Blocks.water.getStateFromMeta(0)) {
						for (BlockPos dir : POSITIONS) {
							if (world.getBlockState(pos.add(dir)).getBlock() == Blocks.air) {
								posFix.add(pos.add(dir));
								switch (x) {
								case 0: 
									checkChunk.add(chunkProvider.provideChunk(chunkX - 1, chunkZ));
								case 16: 
									checkChunk.add(chunkProvider.provideChunk(chunkX + 1, chunkZ));
								}
								switch (z) {
								case 0: 
									checkChunk.add(chunkProvider.provideChunk(chunkX, chunkZ - 1));
								case 16: 
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
			else
				//Cleans up used chunk
				checkChunk.remove(chunkProvider.provideChunk(chunkX, chunkZ));

		//Fixes the chunk for floating water
		for (BlockPos pos : posFix)
			createDirtSand(world, rand, pos);
	}

	private void createDirtSand(World world, Random rand, BlockPos pos) {
		int height;
		int sandHeight = 3;
		if (Config.maxHeight < 5) {
			height = 1;
			sandHeight = Config.maxHeight - 1;
		}
		else if (Config.smooth)
			height = Config.maxHeight;
		else 
			height = rand.nextInt(Config.maxHeight - 2) + 3;

		for (int i = 0; i < height; i++) {
			if (i <= height - sandHeight)
				world.setBlockState(pos.down(height-i), Blocks.dirt.getDefaultState());
			else
				world.setBlockState(pos.down(height-i), Blocks.sand.getDefaultState());
		}
	}
}