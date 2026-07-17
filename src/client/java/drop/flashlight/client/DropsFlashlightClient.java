package drop.flashlight.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec2;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class DropsFlashlightClient implements ClientModInitializer {

	public record Int3D(int x, int y, int z) {
		public Int3D offset(int x, int y, int z) { return new Int3D(this.x + x, this.y + y, this.z + z); }
		public BlockPos toBlockPos() { return new BlockPos(this.x, this.y, this.z); }
	}

	public static boolean isEnabled = false;
	public static int toggleKeyDownTickTime = 0;

	public static final String MOD_ID = "drops-flashlight";
	// public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static Minecraft client = Minecraft.getInstance();

	public static ArrayList<Int3D> chunksToRebuild = new ArrayList<>();
	public static ArrayList<Int3D> oldChunksToRebuild = new ArrayList<>();
	public static ArrayList<Int3D> toBeLit = new ArrayList<>();
	public static Semaphore toBeLitSemaphore = new Semaphore(1);

	KeyMapping.Category CATEGORY = KeyMapping.Category.register(
		Identifier.fromNamespaceAndPath(MOD_ID, "drops_flashlight")
	);

	KeyMapping toggleFlashlightKey = KeyMappingHelper.registerKeyMapping(
		new KeyMapping(
				"key.drops-flashlight.toggle_flashlight", 	// The translation key for the key mapping.
				InputConstants.Type.KEYSYM, 						// The type of the keybinding; KEYSYM for keyboard, MOUSE for mouse.
				GLFW.GLFW_KEY_EQUAL, 								// The GLFW keycode of the key.
				CATEGORY 											// The category of the mapping.
		)
	);

    public static Int3D rayCastBlockPos(Minecraft client, double distance, int Ox, int Oy, int Oz) {

		if (client.player == null) return new Int3D(0, -65, 0);

		BlockPos playerBlockPos = client.player.blockPosition().offset(0, 1, 0);
		int x = playerBlockPos.getX() + Ox, y = playerBlockPos.getY() + Oy, z = playerBlockPos.getZ() + Oz;

		Vec2 rotation = client.player.getRotationVector();
		double cameraX = -rotation.x;
		double cameraY = -rotation.y;

		double dy = distance * Math.sin(Math.toRadians(cameraX));
		double dz = distance * Math.cos(Math.toRadians(cameraX)) * Math.cos(Math.toRadians(cameraY));
		double dx = distance * Math.cos(Math.toRadians(cameraX)) * Math.sin(Math.toRadians(cameraY));

		return new Int3D(x + (int)Math.round(dx), y + (int)Math.round(dy), z + (int)Math.round(dz));
	}

	void appendArrays(Int3D posInt3D) {

		if (client.level == null) return;
		if (!toBeLit.contains(posInt3D)) toBeLit.add(posInt3D);

		for (int x = -1; x <= 1; x++) {
			for (int z = -1; z <= 1; z++) {

				Int3D offsetposInt3D = posInt3D.offset(x, 0, z);
				ChunkPos calculatedChunkPos = client.level.getChunk(offsetposInt3D.toBlockPos()).getPos();
				int approxChunkY = offsetposInt3D.y / 16;

				for (int y = approxChunkY - 2; y <= approxChunkY + 2; y++) {
					Int3D chunkPosInt3D = new Int3D(calculatedChunkPos.x(), y, calculatedChunkPos.z());
					if (!chunksToRebuild.contains(chunkPosInt3D)) chunksToRebuild.add(chunkPosInt3D);
				}
			}
		}
	}

	@Override
	public void onInitializeClient() {

		ClientTickEvents.END_CLIENT_TICK.register(_ -> {

			if (client.player == null) return;
			if (toggleFlashlightKey.isDown()) { toggleKeyDownTickTime += 1; return; }

			if (toggleKeyDownTickTime > 0) {

				isEnabled = !isEnabled;
				toggleKeyDownTickTime = 0;

				client.player.playSound(SoundEvents.COMPARATOR_CLICK, 0.5f, 1.0f);
				if (isEnabled) client.player.sendOverlayMessage(Component.literal("Flashlight [").append(Component.literal("ON").withColor(0xff00ff00)).append(Component.literal("]")));
				else client.player.sendOverlayMessage(Component.literal("Flashlight [").append(Component.literal("OFF").withColor(0xffff0000)).append(Component.literal("]")));
			}
		});

		ClientTickEvents.START_LEVEL_TICK.register(_ -> {

			if (!isEnabled) return;
			if (client.level == null) return;
			if (client.player == null) return;

			float expansionRate = 5;
			int lightRange = 16;

			toBeLitSemaphore.acquireUninterruptibly();
			toBeLit.clear();

			for (int i = 0; i < lightRange; i++) {

				int minY = Math.round((-i - 2) / expansionRate);
				int maxDistance = Math.round(i / expansionRate);
				int maxDistanceSquared = maxDistance * maxDistance;

				for (int x = -maxDistance; x <= maxDistance; x++)
					for (int y = minY; y <= maxDistance; y++)
						for (int z = -maxDistance; z <= maxDistance; z++)
							if (x * x + y * y + z * z <= maxDistanceSquared)
								appendArrays(rayCastBlockPos(client, i, x, y, z));
			}

			toBeLitSemaphore.release();

			oldChunksToRebuild.removeAll(chunksToRebuild);
			for (Int3D chunkPos : oldChunksToRebuild) client.levelExtractor.setSectionDirty(chunkPos.x(), chunkPos.y(), chunkPos.z());
			oldChunksToRebuild.clear();
			oldChunksToRebuild.addAll(chunksToRebuild);

			for (Int3D chunkPos : chunksToRebuild) client.levelExtractor.setSectionDirty(chunkPos.x(), chunkPos.y(), chunkPos.z());
			chunksToRebuild.clear();
		});
	}
}