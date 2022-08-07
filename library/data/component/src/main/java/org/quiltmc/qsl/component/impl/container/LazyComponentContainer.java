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

package org.quiltmc.qsl.component.impl.container;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.function.BiConsumer;

import org.jetbrains.annotations.Nullable;

import org.quiltmc.qsl.base.api.util.Lazy;
import org.quiltmc.qsl.base.api.util.Maybe;
import org.quiltmc.qsl.component.api.ComponentType;
import org.quiltmc.qsl.component.api.container.ComponentContainer;
import org.quiltmc.qsl.component.api.injection.ComponentEntry;
import org.quiltmc.qsl.component.api.provider.ComponentProvider;
import org.quiltmc.qsl.component.api.sync.SyncChannel;

public class LazyComponentContainer extends AbstractComponentContainer {
	public static final ComponentContainer.Factory<LazyComponentContainer> FACTORY = LazyComponentContainer::new;

	private final Map<ComponentType<?>, Lazy<Maybe<? super Object>>> components;

	protected LazyComponentContainer(
			ComponentProvider provider,
			List<ComponentEntry<?>> entries,
			@Nullable Runnable saveOperation,
			boolean ticking,
			@Nullable SyncChannel<?, ?> syncChannel
	) {
		super(saveOperation, ticking, syncChannel);
		this.components = this.createLazyMap(entries, provider);
	}

	public static void move(LazyComponentContainer from, LazyComponentContainer into) {
		from.components.forEach((id, componentLazy) -> componentLazy.ifFilled(component -> {
			into.components.put(id, componentLazy); // Directly overriding our value.
		}));

		into.nbtComponents.addAll(from.nbtComponents);
		into.pendingSync.ifJust(intoPending -> from.pendingSync.ifJust(intoPending::addAll));
		into.ticking.ifJust(intoTicking -> from.ticking.ifJust(intoTicking::addAll));

		from.components.clear();
		from.nbtComponents.clear();
		from.ticking.ifJust(List::clear);
		from.pendingSync.ifJust(Queue::clear);
	}

	@Override
	public <C> Maybe<C> expose(ComponentType<C> type) {
		return this.components.containsKey(type) ? this.components.get(type).unwrap().castUnchecked() :
			   Maybe.nothing();
	}

	@Override
	public void forEach(BiConsumer<ComponentType<?>, ? super Object> action) {
		// unwrap will work here since all Lazies are Just instances for this.
		this.components.forEach(
				(type, componentLazy) -> componentLazy.ifFilled(component -> action.accept(type, component.unwrap()))
		);
	}

	@Override
	protected <COMP> void addComponent(ComponentType<COMP> type, COMP component) { }

	// TODO: Add provider as a parameter for the ComponentFactory create method.
	private Map<ComponentType<?>, Lazy<Maybe<? super Object>>> createLazyMap(List<ComponentEntry<?>> entries, ComponentProvider ignoredProvider) {
		var map = new IdentityHashMap<ComponentType<?>, Lazy<Maybe<? super Object>>>();
		entries.forEach(componentEntry -> map.put(componentEntry.type(), this.createLazy(componentEntry)));
		return map;
	}

	private Lazy<Maybe<? super Object>> createLazy(ComponentEntry<?> componentEntry) {
		ComponentType<?> type = componentEntry.type();

		if (type.isStatic() || type.isInstant()) {
			var component = this.initializeComponent(componentEntry);
			return Lazy.filled(Maybe.just(component)); // this cast will obviously never fail
		}

		return Lazy.of(() -> (Maybe.just(this.initializeComponent(componentEntry)))); // same here
	}
}
