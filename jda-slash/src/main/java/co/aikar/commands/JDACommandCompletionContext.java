package co.aikar.commands;

public class JDACommandCompletionContext extends CommandCompletionContext<JDACommandIssuer> {
    JDACommandCompletionContext(RegisteredCommand command, JDACommandIssuer issuer, String input, String config, String[] args) {
        super(command, issuer, input, config, args);
    }
}
