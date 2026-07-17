package drop.flashlight.client.mixin;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.world.entity.Entity;

import static drop.flashlight.client.DropsFlashlightClient.*;
import static drop.flashlight.client.DropsFlashlightClient.toBeLitSemaphore;


@Mixin(EntityRenderer.class)
public class EntityRendererMixin<T extends Entity> {

	@Inject(at = @At("HEAD"), method = "getBlockLightLevel", cancellable = true)
	void lightEntitiesUp(T entity, BlockPos blockPos, CallbackInfoReturnable<Integer> cir) {

		if (!isEnabled) return;
		if (client.player == null) return;
		if (entity.is(client.player.getLivingEntity())) { cir.setReturnValue(11); return; }

		boolean lightTheEntity = false;
		Int3D posInt3D = new Int3D(blockPos.getX(), blockPos.getY(), blockPos.getZ());
		Int3D offsetPosInt3D = posInt3D.offset(0, 1, 0);

		toBeLitSemaphore.acquireUninterruptibly();

		for (Int3D currentPos : toBeLit)
			if (currentPos.equals(posInt3D) || currentPos.equals(offsetPosInt3D))
				{ lightTheEntity = true; break; }

		toBeLitSemaphore.release();

		if (lightTheEntity) cir.setReturnValue(11);
	}
}