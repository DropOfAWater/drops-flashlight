package drop.flashlight.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.BlockPos;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.BlockAndLightGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static drop.flashlight.client.DropsFlashlightClient.*;

@Mixin(LightCoordsUtil.BrightnessGetter.class)
public interface BrightnessGetterMixin {

	@ModifyReturnValue(at = @At("RETURN"), method = "lambda$static$0(Lnet/minecraft/world/level/BlockAndLightGetter;Lnet/minecraft/core/BlockPos;)I")
	private static int modifyLightmapReturnValue(int defaultValue, BlockAndLightGetter level, BlockPos pos) {

		if (!isEnabled) return defaultValue;
		if (client.player == null) return defaultValue;

		boolean lightTheBlock = false;
		Int3D posInt3D = new Int3D(pos.getX(), pos.getY(), pos.getZ());

		toBeLitSemaphore.acquireUninterruptibly();
		if (toBeLit.contains(posInt3D)) lightTheBlock = true;
		toBeLitSemaphore.release();

		if (lightTheBlock) return (11 << 4) | (11 << 20);
		return defaultValue;
	}
}