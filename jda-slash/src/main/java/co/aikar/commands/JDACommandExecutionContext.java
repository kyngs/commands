package co.aikar.commands;

import java.util.List;
import java.util.Map;

public class JDACommandExecutionContext extends CommandExecutionContext<JDACommandExecutionContext, JDACommandIssuer> {
    JDACommandExecutionContext(RegisteredCommand cmd, CommandParameter param, JDACommandIssuer sender, List<String> args, int index, Map<String, Object> passedArgs) {
        super(cmd, param, sender, args, index, passedArgs);
    }
}
