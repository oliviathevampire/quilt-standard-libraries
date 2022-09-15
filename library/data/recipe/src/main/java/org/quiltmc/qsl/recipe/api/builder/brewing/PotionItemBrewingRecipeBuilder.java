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

package org.quiltmc.qsl.recipe.api.builder.brewing;

import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.recipe.api.brewing.PotionItemBrewingRecipe;
import org.quiltmc.qsl.recipe.api.builder.BrewingRecipeBuilder;

/**
 * Builder to build potion recipes
 */
public class PotionItemBrewingRecipeBuilder extends BrewingRecipeBuilder<PotionItemBrewingRecipeBuilder, Item, PotionItemBrewingRecipe> {

	/**
	 * Creates a new potion brewing recipe builder.
	 *
	 * @param input the input {@link Item}
	 * @param output the resulting {@link Item}
	 */
	public PotionItemBrewingRecipeBuilder(Item input, Item output) {
		super(input, output);
	}

	/**
	 * Builds the recipe.
	 *
	 * @param id    the identifier of the recipe
	 * @param group the group of the recipe
	 * @return the potion recipe
	 */
	public PotionItemBrewingRecipe build(Identifier id, String group) {
		return new PotionItemBrewingRecipe(id, group, this.input, this.ingredient, this.output, this.fuel, this.brewTime);
	}
}
