package floatingwaterfix.worldgen;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import floatingwaterfix.config.Config;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class LoadedWorldGeneration {
	private List<Point> fixedChunkList;
	private WorldGeneration wGen;

	public LoadedWorldGeneration(WorldGeneration wGen) {
		this.wGen = wGen;
		fixedChunkList = new ArrayList<Point>();
	}

	@SubscribeEvent
	public void onLoadChunk(ChunkEvent.Load event) {
		Point p = new Point(event.getChunk().xPosition, event.getChunk().zPosition);
		if (!fixedChunkList.contains(p)) {
			fixedChunkList.add(p);
			World world = event.world;
			Random random = world.rand;
			IChunkProvider chunkProvider = world.getChunkProvider();
			switch (Config.fixMethod) {
			case "SMART":
				wGen.floatingWaterFixSmart(random, (int) p.getX(), (int) p.getY(), world, chunkProvider, Config.depthCheck);
				break;
			case "FORCE":
				wGen.floatingWaterFixForce(random, (int) p.getX(), (int) p.getY(), world, chunkProvider, Config.depthCheck, false);
				break;
			case "FORCESECURE":
				wGen.floatingWaterFixForce(random, (int) p.getX(), (int) p.getY(), world, chunkProvider, Config.depthCheck, true);
				break;
			}
		}
	}

	public List<Point> getpList() {
		return fixedChunkList;
	}
}
