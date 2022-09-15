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

package org.quiltmc.qsl.rendering.entity.mixin.client;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.item.ArmorMaterial;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.rendering.entity.api.client.QuiltArmorMaterialExtensions;
import org.quiltmc.qsl.rendering.entity.impl.client.FallbackArmorTextureProvider;

@Mixin(ArmorMaterial.class)
public interface ArmorMaterialMixin extends QuiltArmorMaterialExtensions {
	@Override
	@NotNull
	default Identifier getTexture() {
		return FallbackArmorTextureProvider.getArmorTexture((ArmorMaterial) this);
	}
}