/*
 * Copyright 2022 QuiltMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.quiltmc.qsl.rendering.item.mixin.client;

import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.rendering.item.api.client.QuadBatchManager;
import org.quiltmc.qsl.rendering.item.impl.client.ItemRendererThreadData;
import org.quiltmc.qsl.rendering.item.impl.client.QuadBatchManagerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;

import org.quiltmc.qsl.rendering.item.api.client.ItemBarRenderer;
import org.quiltmc.qsl.rendering.item.api.client.QuiltItemRendering;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {
	@Unique private final ThreadLocal<ItemRendererThreadData> quilt$threadData
			= ThreadLocal.withInitial(ItemRendererThreadData::new);

	@Shadow public float zOffset;

	@Inject(method = "renderGuiItemOverlay(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V",
			at = @At("HEAD"), cancellable = true)
	private void quilt$customizeItemOverlay(TextRenderer renderer, ItemStack stack, int x, int y, String countLabel,
											CallbackInfo ci) {
		if (stack.isEmpty()) {
			return;
		}

		var item = stack.getItem();

		var threadData = quilt$threadData.get();

		var matrices = threadData.matrices();
		matrices.push();
		matrices.translate(x, y, 0);

		var quadBatchManager = threadData.quadBatchManager();
		quadBatchManager.enter();

		if (item.preRenderOverlay(matrices, quadBatchManager, renderer, this.zOffset, stack)) {
			if (QuiltItemRendering.areOverlayComponentsCustomized(item)) {
				ci.cancel();
				renderCustomGuiItemOverlay(matrices, quadBatchManager, renderer, stack, countLabel);
			}
		} else {
			ci.cancel();
		}

		matrices.pop();
	}

	@Redirect(method = "renderGuiItemOverlay(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V",
			at = @At(value = "NEW", target = "net/minecraft/client/util/math/MatrixStack"))
	private MatrixStack quilt$avoidMatrixStackAllocation() {
		return quilt$threadData.get().matrices(); // no need to push now, matrices are only modified when count label is rendered
	}

	@Redirect(method = "renderGuiItemOverlay(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;translate(DDD)V"))
	private void quilt$renderCountLabel_pushMatrixStack(MatrixStack instance, double x, double y, double z) {
		instance.push();
		instance.translate(x, y, z);
	}

	@Inject(method = "renderGuiItemOverlay(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;draw()V",
					shift = At.Shift.AFTER))
	private void quilt$renderCountLabel_popMatrixStack(TextRenderer renderer, ItemStack stack, int x, int y, String countLabel,
													   CallbackInfo ci) {
		quilt$threadData.get().matrices().pop();
	}

	@Inject(method = "renderGuiItemOverlay(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V",
			at = @At("TAIL"))
	private void quilt$invokePostRenderOverlay(TextRenderer renderer, ItemStack stack, int x, int y, String countLabel,
											   CallbackInfo ci) {
		var threadData = quilt$threadData.get();
		var matrices = threadData.matrices();
		var quadBatchManager = threadData.quadBatchManager();

		quadBatchManager.enter();

		matrices.push();
		matrices.translate(x, y, 0);
		stack.getItem().postRenderOverlay(matrices, quadBatchManager, renderer, this.zOffset, stack);
		matrices.pop();

		quadBatchManager.endCurrentBatch();
		quadBatchManager.exit();
	}

	@Unique
	private void renderCustomGuiItemOverlay(MatrixStack matrices, QuadBatchManagerImpl quadBatchManager,
											TextRenderer textRenderer, ItemStack stack, @Nullable String countLabel) {
		if (stack.isEmpty()) {
			return;
		}

		var item = stack.getItem();

		item.getCountLabelRenderer().renderCountLabel(matrices, quadBatchManager, textRenderer, this.zOffset, stack, countLabel);

		var itemBarRenderers = item.getItemBarRenderers();

		int itemBarY = 13;
		if (itemBarRenderers == null || itemBarRenderers.length == 1) {
			var itemBar = ItemBarRenderer.VANILLA;
			if (itemBarRenderers != null) {
				itemBar = itemBarRenderers[0];
			}

			if (itemBar.isItemBarVisible(stack)) {
				matrices.push();
				matrices.translate(0, itemBarY, 0);
				itemBar.renderItemBar(matrices, quadBatchManager, textRenderer, this.zOffset, stack);
				matrices.pop();
			}
		} else if (itemBarRenderers.length > 0) {
			boolean anyItemBarsVisible = false;
			for (var itemBar : itemBarRenderers) {
				if (itemBar.isItemBarVisible(stack)) {
					itemBarY -= 2;
					anyItemBarsVisible = true;
				}
			}

			if (anyItemBarsVisible) {
				for (var itemBar : itemBarRenderers) {
					if (itemBar.isItemBarVisible(stack)) {
						itemBarY += 2;

						matrices.push();
						matrices.translate(0, itemBarY, 0);
						itemBar.renderItemBar(matrices, quadBatchManager, textRenderer, this.zOffset, stack);
						matrices.pop();
					}
				}
			}
		}

		item.getCooldownOverlayRenderer().renderCooldownOverlay(matrices, quadBatchManager, textRenderer, this.zOffset, stack);

		item.postRenderOverlay(matrices, quadBatchManager, textRenderer, this.zOffset, stack);

		quadBatchManager.endCurrentBatch();
		quadBatchManager.exit();
	}
}