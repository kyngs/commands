package co.aikar.commands;

import net.dv8tion.jda.api.JDA;

public class JDAOptions {

    private final long guildId;

    private JDAOptions(long guildId) {
        this.guildId = guildId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public long getGuildId() {
        return guildId;
    }

    public static class Builder {

        private long guildId = 0L;

        private Builder() {
        }

        /**
         * Set the guild id to use for the command manager.
         * <br>
         * If you supply a guild id, the command manager will only register commands for that guild. <b>This is reccomended for testing purposes</b>
         * <br>
         * If you supply 0 (default), the command manager will register commands for all guilds globally, this might take up to an hour to propagate.
         *
         * @param guildId The guild id to use.
         * @return This builder.
         */
        public Builder setGuildId(long guildId) {
            this.guildId = guildId;
            return this;
        }

        public JDAOptions build() {
            return new JDAOptions(guildId);
        }

        public JDACommandManager buildManager(JDA jda) {
            return new JDACommandManager(jda, build());
        }
    }

}
