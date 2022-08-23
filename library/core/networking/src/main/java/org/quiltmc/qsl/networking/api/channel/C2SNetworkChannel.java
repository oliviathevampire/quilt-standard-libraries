package org.quiltmc.qsl.networking.api.channel;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

import org.quiltmc.qsl.networking.api.PacketSender;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;

/**
 * A network channel that can send messages from client to server.
 *
 * @param <T> the type of messages this channel can send
 */
public interface C2SNetworkChannel<T> extends NetworkChannel<T> {
	/**
	 * Send a message from the current client to the server.
	 *
	 * @param t the message to send
	 */
	@Environment(EnvType.CLIENT)
	default void send(T t) {
		ClientPlayNetworking.send(this.id(), this.codec().createBuffer(t));
	}

	/**
	 * Creates a {@link ServerPlayNetworking.ChannelReceiver} that uses this {@link NetworkChannel}'s
	 * {@linkplain org.quiltmc.qsl.networking.api.codec.NetworkCodec codec} to decode messages and can handle them.
	 *
	 * @return a {@link ServerPlayNetworking.ChannelReceiver} that can receive messages from this
	 * {@link NetworkChannel}
	 */
	ServerPlayNetworking.ChannelReceiver createServerReceiver();

	/**
	 * Interface used to handle messages on the server.
	 */
	@FunctionalInterface
	interface Handler {
		/**
		 * Handles a message on the server.
		 *
		 * @param server         the server
		 * @param sender         the player who sent the message
		 * @param handler        the {@link ServerPlayNetworkHandler} that received the message
		 * @param responseSender the {@link PacketSender} to use to send responses
		 * @apiNote This method is called on the main thread.
		 */
		void serverHandle(
				MinecraftServer server,
				ServerPlayerEntity sender,
				ServerPlayNetworkHandler handler,
				PacketSender responseSender
		);
	}
}
