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

package org.quiltmc.qsl.recipe.impl;

import net.minecraft.block.entity.BrewingStandBlockEntity;
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.item.Item;
import net.minecraft.recipe.RecipeType;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.recipe.api.brewing.AbstractBrewingRecipe;
import org.quiltmc.qsl.recipe.api.brewing.CustomPotionBrewingRecipe;
import org.quiltmc.qsl.recipe.api.brewing.PotionBrewingRecipe;
import org.quiltmc.qsl.recipe.api.brewing.PotionItemBrewingRecipe;

@ApiStatus.Internal
public class RecipeImpl implements ModInitializer {
	public static final String NAMESPACE = "quilt_recipe";
	public static final RecipeType<AbstractBrewingRecipe<?>> BREWING = RecipeType.register(NAMESPACE + ":brewing");
	public static final PotionBrewingRecipe.Serializer<PotionBrewingRecipe> POTION_SERIALIZER = Registry.register(Registry.RECIPE_SERIALIZER, new Identifier(NAMESPACE, "potion_brewing"), new PotionBrewingRecipe.Serializer<>(PotionBrewingRecipe::new));
	public static final CustomPotionBrewingRecipe.Serializer CUSTOM_POTION_SERIALIZER = Registry.register(Registry.RECIPE_SERIALIZER, new Identifier(NAMESPACE, "custom_potion_brewing"), new CustomPotionBrewingRecipe.Serializer(CustomPotionBrewingRecipe::new));
	public static final PotionItemBrewingRecipe.Serializer POTION_ITEM_SERIALIZER = Registry.register(Registry.RECIPE_SERIALIZER, new Identifier(NAMESPACE, "potion_item_brewing"), new PotionItemBrewingRecipe.Serializer(PotionItemBrewingRecipe::new));
	/**
	 * Represents what items can be put into the potion slots of a {@link BrewingStandBlockEntity}.
	 */
	public static final TagKey<Item> VALID_INPUTS = TagKey.of(Registry.ITEM_KEY, new Identifier("quilt", "brewing_stand/inputs"));

	@Override
	public void onInitialize(ModContainer mod) {}
}
