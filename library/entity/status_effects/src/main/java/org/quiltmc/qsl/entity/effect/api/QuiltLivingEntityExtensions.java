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

package org.quiltmc.qsl.entity.effect.api;

import org.jetbrains.annotations.NotNull;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;

import org.quiltmc.qsl.base.api.util.InjectedInterface;

@InjectedInterface(LivingEntity.class)
public interface QuiltLivingEntityExtensions {
	boolean removeStatusEffect(@NotNull StatusEffect type, @NotNull StatusEffectRemovalReason reason);
	int clearStatusEffects(@NotNull StatusEffectRemovalReason reason);
}
