package org.quiltmc.qsl.enchantment.test;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;

public class EnchantmentTestMod implements ModInitializer {
	@Override
	public void onInitialize(ModContainer mod) {
		Registry.register(Registry.ENCHANTMENT, new Identifier("reaping"), new ReapingEnchantment());
	}
}
