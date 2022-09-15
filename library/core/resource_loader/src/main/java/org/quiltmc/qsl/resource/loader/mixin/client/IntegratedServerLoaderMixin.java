/*
 * Copyright 2021-2022 QuiltMC
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

package org.quiltmc.qsl.resource.loader.mixin.client;

import com.mojang.serialization.Lifecycle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.resource.pack.ResourcePackManager;
import net.minecraft.server.WorldLoader;
import net.minecraft.server.WorldStem;
import net.minecraft.server.integrated.IntegratedServerLoader;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.level.storage.LevelStorage;

import org.quiltmc.qsl.base.api.util.TriState;
import org.quiltmc.qsl.resource.loader.api.ResourceLoaderEvents;

@Mixin(IntegratedServerLoader.class)
public abstract class IntegratedServerLoaderMixin {
	@Shadow
	private static void close(LevelStorage.Session storageSession, String worlName) {
		throw new IllegalStateException("Mixin injection failed.");
	}

	@Shadow
	protected abstract void start(Screen parentScreen, String worldName, boolean safeMode, boolean requireBackup);

	@Unique
	private static final TriState EXPERIMENTAL_SCREEN_OVERRIDE = TriState.fromProperty("quilt.resource_loader.experimental_screen_override");

	@Inject(
			method = "loadWorldStem(Lnet/minecraft/server/WorldLoader$PackConfig;Lnet/minecraft/server/WorldLoader$LoadContextSupplier;)Lnet/minecraft/server/WorldStem;",
			at = @At("HEAD")
	)
	private void onStartDataPackLoad(WorldLoader.PackConfig dataPackConfig, WorldLoader.LoadContextSupplier<SaveProperties> savePropertiesSupplier,
			CallbackInfoReturnable<WorldStem> cir) {
		ResourceLoaderEvents.START_DATA_PACK_RELOAD.invoker().onStartDataPackReload(null, null);
	}

	@Inject(
			method = "loadWorldStem(Lnet/minecraft/server/WorldLoader$PackConfig;Lnet/minecraft/server/WorldLoader$LoadContextSupplier;)Lnet/minecraft/server/WorldStem;",
			at = @At("RETURN")
	)
	private void onEndDataPackLoad(WorldLoader.PackConfig dataPackConfig, WorldLoader.LoadContextSupplier<SaveProperties> savePropertiesSupplier,
			CallbackInfoReturnable<WorldStem> cir) {
		ResourceLoaderEvents.END_DATA_PACK_RELOAD.invoker().onEndDataPackReload(null, cir.getReturnValue().resourceManager(), null);
	}

	@ModifyArg(
			method = {"createAndStart", "start(Lnet/minecraft/client/gui/screen/Screen;Ljava/lang/String;ZZ)V"},
			at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Throwable;)V", remap = false),
			index = 1
	)
	private Throwable onFailedDataPackLoad(Throwable throwable) {
		ResourceLoaderEvents.END_DATA_PACK_RELOAD.invoker().onEndDataPackReload(null, null, throwable);
		return throwable; // noop
	}

	@Inject(
			method = "start(Lnet/minecraft/client/gui/screen/Screen;Ljava/lang/String;ZZ)V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/server/integrated/IntegratedServerLoader;askForBackup(Lnet/minecraft/client/gui/screen/Screen;Ljava/lang/String;ZLjava/lang/Runnable;)V"
			),
			locals = LocalCapture.CAPTURE_FAILHARD,
			cancellable = true
	)
	private void onBackupExperimentalWarning(Screen parentScreen, String worldName, boolean safeMode, boolean requireBackup, CallbackInfo ci,
			LevelStorage.Session session, ResourcePackManager resourcePackManager, WorldStem worldStem) {
		if (EXPERIMENTAL_SCREEN_OVERRIDE.toBooleanOrElse(true)
				&& !worldStem.saveProperties().getGeneratorOptions().isLegacyCustomizedType()) {
			worldStem.close();
			close(session, worldName);
			this.start(parentScreen, worldName, safeMode, false);
			ci.cancel();
		}
	}

	@Inject(
			method = "tryLoad",
			at = @At(value = "CONSTANT", args = "stringValue=selectWorld.import_worldgen_settings.experimental.title"),
			cancellable = true
	)
	private static void onExperimentalWarning(MinecraftClient client, CreateWorldScreen parentScreen,
			Lifecycle dynamicRegistryLifecycle, Runnable successCallback,
			CallbackInfo ci) {
		if (EXPERIMENTAL_SCREEN_OVERRIDE.toBooleanOrElse(true)) {
			successCallback.run();
			ci.cancel();
		}
	}
}