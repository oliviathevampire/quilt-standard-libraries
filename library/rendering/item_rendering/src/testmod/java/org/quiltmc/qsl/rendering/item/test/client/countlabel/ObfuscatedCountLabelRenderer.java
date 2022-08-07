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

package org.quiltmc.qsl.rendering.item.test.client.countlabel;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Formatting;

import org.quiltmc.qsl.rendering.item.api.client.CountLabelRenderer;
import org.quiltmc.qsl.rendering.item.api.client.VanillaCountLabelRenderer;

@Environment(EnvType.CLIENT)
public class ObfuscatedCountLabelRenderer extends VanillaCountLabelRenderer {
	public static final CountLabelRenderer INSTANCE = new ObfuscatedCountLabelRenderer();

	@Override
	protected @Nullable String getCountLabel(ItemStack stack, @Nullable String override) {
		String label = super.getCountLabel(stack, override);
		if (label == null) {
			return null;
		} else {
			return Formatting.OBFUSCATED + label;
		}
	}
}
