package co.aikar.commands;

import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Channel;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class JDACommandIssuer implements CommandIssuer {

    private final SlashCommandInteraction interaction;
    private final JDACommandManager manager;
    private final List<String> messages;
    private Message message;

    public JDACommandIssuer(SlashCommandInteraction interaction, JDACommandManager manager) {
        this.interaction = interaction;
        this.manager = manager;
        messages = new ArrayList<>();
    }

    @Override
    public User getIssuer() {
        return interaction.getUser();
    }

    public Member getMember() {
        return interaction.getMember();
    }

    public Channel getChannel() {
        return interaction.getChannel();
    }

    public Guild getGuild() {
        return interaction.getGuild();
    }

    public void message(Message message) {
        this.message = message;
    }

    public void embed(MessageEmbed... embeds) {
        this.message = new MessageBuilder().setEmbeds(embeds).build();
    }

    @Override
    public JDACommandManager getManager() {
        return manager;
    }

    @Override
    public boolean isPlayer() {
        return false;
    }

    // Kindly stolen from original impl.
    @Override
    public @NotNull UUID getUniqueId() {
        // Discord id only have 64 bit width (long) while UUIDs have twice the size.
        // In order to keep it unique we use 0L for the first 64 bit.
        long authorId = interaction.getUser().getIdLong();
        return new UUID(0, authorId);
    }

    @Override
    public boolean hasPermission(String permission) {
        return false;
    }

    @Override
    public void sendMessageInternal(String message) {
        messages.add(message);
    }

    protected void complete(InteractionHook hook) {
        if (message == null) {
            String message = String.join("\n", messages);
            if (message.isEmpty()) message = "No response.";
            hook.sendMessage(message).complete();
        } else {
            hook.sendMessage(message).complete();
        }

    }
}
