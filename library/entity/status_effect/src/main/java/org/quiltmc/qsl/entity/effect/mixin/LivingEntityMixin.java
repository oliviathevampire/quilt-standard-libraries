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

package org.quiltmc.qsl.entity.effect.mixin;

import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;

import org.quiltmc.qsl.entity.effect.api.QuiltLivingEntityExtensions;
import org.quiltmc.qsl.entity.effect.api.StatusEffectRemovalReason;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements QuiltLivingEntityExtensions {
	@SuppressWarnings("ConstantConditions")
	public LivingEntityMixin() {
		super(null, null);
		throw new IllegalStateException("Mixin ctor called??");
	}

	@Shadow
	@Final
	private Map<StatusEffect, StatusEffectInstance> activeStatusEffects;

	@Shadow
	protected abstract void onStatusEffectRemoved(StatusEffectInstance effect);

	@SuppressWarnings("ConstantConditions")
	@Override
	public boolean removeStatusEffect(@NotNull StatusEffect type, @NotNull StatusEffectRemovalReason reason) {
		var effect = this.activeStatusEffects.get(type);
		if (effect == null) {
			return false;
		}

		if (type.shouldRemove((LivingEntity) (Object) this, effect, reason)) {
			this.activeStatusEffects.remove(type);
			this.onStatusEffectRemoved(effect);
			return true;
		} else {
			return false;
		}
	}

	@SuppressWarnings("ConstantConditions")
	@Override
	public int clearStatusEffects(@NotNull StatusEffectRemovalReason reason) {
		if (this.world.isClient) {
			return 0;
		}

		int removed = 0;
		var it = this.activeStatusEffects.values().iterator();
		while (it.hasNext()) {
			var effect = it.next();
			if (effect.getEffectType().shouldRemove((LivingEntity) (Object) this, effect, reason)) {
				it.remove();
				this.onStatusEffectRemoved(effect);
				removed++;
			}
		}

		return removed;
	}

	/**
	 * @author QuiltMC
	 * @reason Adding removal reason
	 */
	@Overwrite
	public boolean removeStatusEffect(StatusEffect type) {
		return removeStatusEffect(type, StatusEffectRemovalReason.GENERIC_SPECIFIC);
	}

	/**
	 * @author QuiltMC
	 * @reason Adding removal reason
	 */
	@Overwrite
	public boolean clearStatusEffects() {
		return clearStatusEffects(StatusEffectRemovalReason.GENERIC_ALL) > 0;
	}
}