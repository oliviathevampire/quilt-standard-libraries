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

package org.quiltmc.qsl.key.binds.mixin.client.config;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.client.option.KeyBind;
import org.quiltmc.qsl.key.binds.impl.config.QuiltKeyBindsConfig;
import org.quiltmc.qsl.key.binds.impl.config.QuiltKeyBindsConfigManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
	// You can't INVOKE_ASSIGN at GameOptions for some reason
	@Inject(
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/tutorial/TutorialManager;<init>(Lnet/minecraft/client/MinecraftClient;Lnet/minecraft/client/option/GameOptions;)V"),
			method = "<init>"
	)
	private void handleQuiltKeyBindsConfig(RunArgs runArgs, CallbackInfo ci) {
		new QuiltKeyBindsConfig();
		QuiltKeyBindsConfigManager.updateConfig(true);
		KeyBind.updateBoundKeys();
	}
}