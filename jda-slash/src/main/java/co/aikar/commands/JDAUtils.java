package co.aikar.commands;

import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.util.HashMap;
import java.util.Map;

public class JDAUtils {

    public static final Map<Class<?>, OptionType> DEFINITIONS;

    static {
        DEFINITIONS = new HashMap<>();

        DEFINITIONS.put(String.class, OptionType.STRING);
        DEFINITIONS.put(Integer.class, OptionType.INTEGER);
        DEFINITIONS.put(Boolean.class, OptionType.BOOLEAN);
        DEFINITIONS.put(User.class, OptionType.USER);
        DEFINITIONS.put(Member.class, OptionType.USER);
        DEFINITIONS.put(GuildChannel.class, OptionType.CHANNEL);
        DEFINITIONS.put(Role.class, OptionType.ROLE);
        DEFINITIONS.put(IMentionable.class, OptionType.MENTIONABLE);
        DEFINITIONS.put(Double.class, OptionType.NUMBER);
        DEFINITIONS.put(Long.class, OptionType.NUMBER);
        DEFINITIONS.put(Message.Attachment.class, OptionType.ATTACHMENT);
    }

    @SafeVarargs
    public static void propagateParameters(SubcommandData subcommandData, CommandParameter<JDACommandExecutionContext>... parameters) {
        for (CommandParameter<JDACommandExecutionContext> parameter : parameters) {
            if (!parameter.canConsumeInput()) continue;

            OptionType type = DEFINITIONS.get(parameter.getType());

            // Not ideal, but it works.
            if ((type == OptionType.USER || type == OptionType.CHANNEL) && !parameter.getParameter().isAnnotationPresent(Other.class)) {
                continue;
            }

            if (type == null) type = OptionType.STRING;

            subcommandData.addOption(
                    type,
                    parameter.getName(),
                    parameter.getDescription().isEmpty() ? "No description." : parameter.getDescription(),
                    !parameter.isOptional()
            );
        }
    }

    @SafeVarargs
    public static void propagateParameters(SlashCommandData commandData, CommandParameter<JDACommandExecutionContext>... parameters) {
        for (CommandParameter<JDACommandExecutionContext> parameter : parameters) {
            if (!parameter.canConsumeInput()) continue;

            OptionType type = DEFINITIONS.get(parameter.getType());

            // Not ideal, but it works.
            if ((type == OptionType.USER || type == OptionType.CHANNEL) && !parameter.getParameter().isAnnotationPresent(Other.class)) {
                continue;
            }
            if (type == null) type = OptionType.STRING;

            commandData.addOption(
                    type,
                    parameter.getName(),
                    parameter.getDescription().isEmpty() ? "No description." : parameter.getDescription(),
                    !parameter.isOptional()
            );
        }
    }

}
