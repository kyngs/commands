package co.aikar.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import org.jetbrains.annotations.NotNull;

public class JDAListener extends ListenerAdapter {

    private final JDACommandManager manager;

    public JDAListener(JDACommandManager manager) {
        this.manager = manager;
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        InteractionHook hook = event.deferReply().complete();
        SlashCommandInteraction interaction = event.getInteraction();

        JDARootCommand command = (JDARootCommand) manager.getRootCommand(interaction.getName());

        if (command == null) return;

        String[] args = interaction.getOptions()
                .stream()
                .map(OptionMapping::getAsString)
                .toArray(String[]::new);

        JDACommandIssuer issuer = new JDACommandIssuer(interaction, manager);

        command.execute(issuer, interaction.getName(), args);

        issuer.complete(hook);

    }
}
