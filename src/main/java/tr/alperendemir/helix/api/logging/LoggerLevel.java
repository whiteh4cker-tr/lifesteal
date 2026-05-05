package tr.alperendemir.helix.api.logging;

public enum LoggerLevel {

    INFO(   "\r[ <cyan>INFO<reset>    ] ",         "§7[ §bINFO§7    ]§r "),
    OK(     "\r[ <green>OK<reset>      ] ",        "§7[ §aOK§7      ]§r "),
    DEBUG(  "\r[ <light:blue>DEBUG<reset>   ] ",   "§7[ §9DEBUG§7   ]§r "),
    WARNING("\r[ <light:yellow>WARNING<reset> ] ", "§7[ §eWARNING§7 ]§r "),
    ERROR(  "\r[ <red>ERROR<reset>   ] ",          "§7[ §4ERROR§7   ]§r ");

    // TODO: Make error paths relative to the plugins folder.

    private final String consoleText;
    private final String playerText;

    LoggerLevel(String consoleText, String playerText) {
        this.consoleText = consoleText;
        this.playerText = playerText;
    }

    public String getConsoleText() {
        return this.consoleText;
    }

    public String getPlayerText() {
        return this.playerText;
    }
}
