package co.aikar.commands;

import co.aikar.commands.apachecommonslang.ApacheCommonsExceptionUtil;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An attempt to implement ACF using Discord's Slash Command API.
 * <br>
 * This implementation is far from perfect and introduces a lot of constraints. (See Issue #366)
 *
 * @author kyngs
 */
@SuppressWarnings("rawtypes") //Thanks aikar
public class JDACommandManager extends CommandManager<
        SlashCommandInteractionEvent,
        JDACommandIssuer,
        String,
        JDAMessageFormatter,
        JDACommandExecutionContext,
        JDAConditionContext
        > {

    private final JDA jda;
    private final JDAOptions options;
    private final JDACommandCompletions completions;
    private final JDACommandContexts contexts;
    private final JDALocales locales;
    private final Map<String, JDARootCommand> commands;
    private final Set<CommandData> toPropagate;
    private final Logger logger;

    public JDACommandManager(JDA jda, JDAOptions options) {
        this.jda = jda;
        this.options = options;

        this.logger = Logger.getLogger(getClass().getSimpleName());

        commands = new HashMap<>();
        toPropagate = new HashSet<>();

        contexts = new JDACommandContexts(this);
        completions = new JDACommandCompletions(this);
        locales = new JDALocales(this);
        locales.loadLanguages();

        jda.addEventListener(new JDAListener(this));
    }

    protected JDA getJda() {
        return jda;
    }

    @Override
    public CommandContexts<?> getCommandContexts() {
        return contexts;
    }

    @Override
    public CommandCompletions<?> getCommandCompletions() {
        return completions;
    }

    @Override
    public void registerCommand(BaseCommand command) {
        command.onRegister(this);

        command.registeredCommands.forEach((name, cmd) -> {
            BaseCommand def = cmd.getDefCommand();

            if (def == null) throw new IllegalArgumentException("The command " + name + " has no default command");

            commands.put(name, (JDARootCommand) cmd);

            registerInternally(def, name);
        });
    }

    private void registerInternally(BaseCommand command, String cmdName) {

        JDACommandData def = null;
        Set<JDACommandData> subs = new HashSet<>();
        Multimap<String, JDACommandData> groups = HashMultimap.create();

        for (Map.Entry<String, RegisteredCommand> entry : command.subCommands.entries()) {
            String name = entry.getKey();
            RegisteredCommand cmd = entry.getValue();

            if (!BaseCommand.isSpecialSubcommand(name)) {
                String[] split = name.split(" ");

                if (split.length > 2)
                    throw new IllegalArgumentException("You can only chain subcommands up to two levels deep");

                if (split.length == 2) {
                    groups.put(split[0], new JDACommandData(split[1], cmd.scope.description, cmd.parameters));
                } else {
                    if (groups.containsKey(split[0]))
                        throw new IllegalArgumentException("You can only have one subcommand with the name " + split[0]);
                    subs.add(new JDACommandData(split[0], cmd.scope.description, cmd.parameters));
                }
            } else if (name.equals("__default")) {
                def = new JDACommandData(null, cmd.scope.description, cmd.parameters);
            }
        }

        SlashCommandData data = Commands.slash(cmdName, command.description == null ? "No description." : command.description);

        if (def != null) {
            if (subs.isEmpty() && groups.isEmpty()) {
                JDAUtils.propagateParameters(data, def.getParameters());
            } else {
                throw new InvalidCommandArgument("You cannot have a default command with subcommands");
            }
        }

        for (JDACommandData cmd : subs) {
            SubcommandData subData = new SubcommandData(cmd.getName(), cmd.getDescription());

            JDAUtils.propagateParameters(subData, cmd.getParameters());

            data.addSubcommands(subData);
        }

        groups.asMap().forEach((group, cmds) -> {
            SubcommandGroupData groupData = new SubcommandGroupData(group, "Susbcommands for " + group);

            for (JDACommandData cmd : cmds) {
                SubcommandData subData = new SubcommandData(cmd.getName(), cmd.getDescription());

                JDAUtils.propagateParameters(subData, cmd.getParameters());

                groupData.addSubcommands(subData);
            }

            data.addSubcommandGroups(groupData);
        });

        toPropagate.add(data);
    }

    public void propagate() {
        CommandListUpdateAction action = updateCommands();
        action.addCommands(toPropagate);
        action.complete();
        toPropagate.clear();
    }

    @Override
    public boolean hasRegisteredCommands() {
        return !commands.isEmpty();
    }

    @Override
    public boolean isCommandIssuer(Class<?> type) {
        return JDACommandIssuer.class.isAssignableFrom(type);
    }

    @Override
    public JDACommandIssuer getCommandIssuer(Object issuer) {
        if (!(issuer instanceof JDACommandIssuer)) {
            throw new IllegalArgumentException("The issuer must be a JDACommandIssuer");
        }

        return (JDACommandIssuer) issuer;
    }

    @Override
    public RootCommand createRootCommand(String cmd) {
        return new JDARootCommand(this, cmd);
    }

    @Override
    public Locales getLocales() {
        return locales;
    }

    @Override
    public JDACommandExecutionContext createCommandContext(RegisteredCommand command, CommandParameter parameter, CommandIssuer sender, List<String> args, int i, Map<String, Object> passedArgs) {
        return new JDACommandExecutionContext(command, parameter, (JDACommandIssuer) sender, args, i, passedArgs);
    }

    @Override
    public JDACommandCompletionContext createCompletionContext(RegisteredCommand command, CommandIssuer sender, String input, String config, String[] args) {
        return new JDACommandCompletionContext(command, (JDACommandIssuer) sender, input, config, args);
    }

    @Override
    public void log(LogLevel level, String message, Throwable throwable) {
        Level logLevel = level == LogLevel.INFO ? Level.INFO : Level.SEVERE;
        logger.log(logLevel, LogLevel.LOG_PREFIX + message);
        if (throwable != null) {
            for (String line : ACFPatterns.NEWLINE.split(ApacheCommonsExceptionUtil.getFullStackTrace(throwable))) {
                logger.log(logLevel, LogLevel.LOG_PREFIX + line);
            }
        }
    }

    @Override
    public Collection<RootCommand> getRegisteredRootCommands() {
        return Collections.unmodifiableCollection(commands.values());
    }

    private CommandListUpdateAction updateCommands() {
        long guildId = options.getGuildId();

        if (guildId == 0) {
            return jda.updateCommands();
        } else {
            Guild guild = jda.getGuildById(guildId);

            if (guild == null) throw new IllegalArgumentException("Invalid guild id: " + guildId);

            return guild.updateCommands();
        }

    }


}
