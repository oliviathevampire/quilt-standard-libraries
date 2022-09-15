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

package org.quiltmc.qsl.networking.api.codec;

import net.minecraft.network.PacketByteBuf;

import org.quiltmc.qsl.networking.api.codec.NetworkCodec;

public class NamedNetworkCodec<A> implements NetworkCodec<A> {
	private final String name;
	private final NetworkCodec<A> delegate;

	public NamedNetworkCodec(String name, NetworkCodec<A> delegate) {
		this.name = name;
		this.delegate = delegate;
	}

	@Override
	public A decode(PacketByteBuf buf) {
		return this.delegate.decode(buf);
	}

	@Override
	public void encode(PacketByteBuf buf, A data) {
		this.delegate.encode(buf, data);
	}

	@Override
	public String toString() {
		return this.name;
	}
}