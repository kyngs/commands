package co.aikar.commands;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("rawtypes") //Thanks aikar
public class JDARootCommand implements RootCommand {

    private final String name;
    private final JDACommandManager manager;
    private final SetMultimap<String, RegisteredCommand> subCommands;
    private final List<BaseCommand> children;
    boolean registered = false;
    private BaseCommand defCommand;

    JDARootCommand(JDACommandManager manager, String name) {
        this.manager = manager;
        this.name = name;
        subCommands = HashMultimap.create();
        children = new ArrayList<>();
    }

    @Override
    public void addChild(BaseCommand command) {
        if (this.defCommand == null || !command.subCommands.get(BaseCommand.DEFAULT).isEmpty()) {
            this.defCommand = command;
        }
        addChildShared(this.children, this.subCommands, command);
    }

    @Override
    public JDACommandManager getManager() {
        return this.manager;
    }

    @Override
    public SetMultimap<String, RegisteredCommand> getSubCommands() {
        return this.subCommands;
    }

    @Override
    public List<BaseCommand> getChildren() {
        return this.children;
    }

    @Override
    public String getCommandName() {
        return this.name;
    }

    @Override
    public BaseCommand getDefCommand() {
        return defCommand;
    }

}
