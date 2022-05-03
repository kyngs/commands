package co.aikar.commands;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Channel;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

public class JDACommandContexts extends CommandContexts<JDACommandExecutionContext> {

    private final JDA jda;

    JDACommandContexts(JDACommandManager manager) {
        super(manager);

        this.jda = manager.getJda();

        this.registerIssuerOnlyContext(JDACommandIssuer.class, CommandExecutionContext::getIssuer);
        this.registerIssuerOnlyContext(ChannelType.class, context -> context.issuer.getChannel().getType());
        this.registerIssuerOnlyContext(JDA.class, context -> jda);
        this.registerIssuerOnlyContext(Guild.class, context -> {
            Guild guild = context.issuer.getGuild();
            if (guild == null && !context.isOptional()) {
                throw new InvalidCommandArgument("This command can only be executed in a Guild.", false);
            } else {
                return guild;
            }
        });
        this.registerIssuerAwareContext(GuildChannel.class, context -> {
            if (context.hasAnnotation(Other.class)) {
                return jda.getGuildChannelById(context.popFirstArg());
            } else {
                Channel channel = context.issuer.getChannel();
                if (!(channel instanceof GuildChannel))
                    throw new InvalidCommandArgument("This command can only be executed in a Guild.", false);

                return (GuildChannel) channel;
            }
        });
        this.registerIssuerAwareContext(User.class, context -> {
            if (context.hasAnnotation(Other.class)) {
                return jda.getUserById(context.popFirstArg());
            } else {
                return context.issuer.getIssuer();
            }
        });
        this.registerIssuerAwareContext(Member.class, context -> {
            if (context.hasAnnotation(Other.class)) {
                Guild guild = (Guild) context.getResolvedArg(Guild.class);

                return guild.getMemberById(context.popFirstArg());
            } else {
                return context.issuer.getMember();
            }
        });
    }
}
