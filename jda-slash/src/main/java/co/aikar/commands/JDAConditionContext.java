package co.aikar.commands;

public class JDAConditionContext extends ConditionContext<JDACommandIssuer> {
    JDAConditionContext(JDACommandIssuer issuer, String config) {
        super(issuer, config);
    }
}
