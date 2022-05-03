package co.aikar.commands;

public class JDACommandData {

    private final String name, description;
    private final CommandParameter[] parameters;

    public JDACommandData(String name, String description, CommandParameter[] parameters) {
        this.name = name;
        this.description = description == null ? "No description." : description;
        this.parameters = parameters;
    }

    public CommandParameter[] getParameters() {
        return parameters;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
