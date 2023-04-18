# GUILib

```java
public class ExamplePlugin extends JavaPlugin {
    private final static String commandName = "pumpkintrees";
    private YamlFileConfig messageConfig;
    private CommandManager commandManager;

    @Override
    public void onEnable() {
        messageConfig = new YamlFileConfig(this, "messages.yml");
        reloadConfig();

        try {
            commandManager = new CommandManager(this,
                    component -> messageConfig.getComponent("prefix").append(text(" ")).append(component),
                    "/" + commandName + " help",
                    new CommandConfirmationManager<>(
                            30L,
                            TimeUnit.SECONDS,
                            context -> context.getCommandContext().getSender().sendMessage(messageConfig.getComponent("commands.confirm.needed")),
                            sender -> sender.sendMessage(messageConfig.getComponent("commands.confirm.nothing"))
                    )
            );
        } catch (InstantiationException e) {
            e.printStackTrace();
            setEnabled(false);
            return;
        }
        createCommands();
    }

    @Override
    public void reloadConfig() {
        saveDefaultConfig();
        super.reloadConfig();

        messageConfig.reload();
    }

    private void createCommands() {
        var builder = commandManager.manager().commandBuilder(commandName);

        commandManager.command(builder.literal("help", ArgumentDescription.of("The main help command"))
                .permission("ah.commands.help")
                .argument(StringArgument.<CommandSender>builder("query").greedy().asOptional().withSuggestionsProvider((context, string) ->
                        commandManager.manager().createCommandHelpHandler().queryRootIndex(context.getSender()).getEntries().stream()
                                .map(CommandHelpHandler.VerboseHelpEntry::getSyntaxString).collect(Collectors.toList())
                ).withDefaultDescription(ArgumentDescription.of("The start of the command to query")))
                .handler(commandContext -> {
                    String query = commandContext.getOrDefault("query", "");
                    commandManager.queryCommands(query == null ? "" : query, commandContext.getSender());
                })
        );
        commandManager.command(builder.literal("reload", ArgumentDescription.of("Reloads this plugin"))
                .permission("ah.admin.reload")
                .handler(commandContext -> {
                    final var sender = commandContext.getSender();
                    sender.sendMessage(this.getMessageConfig().getComponent("commands.reload.start"));
                    this.reloadConfig();
                    sender.sendMessage(this.getMessageConfig().getComponent("commands.reload.finish"));
                })
        );
        commandManager.command(builder.literal("version")
                .permission("ah.admin.version")
                .handler(commandContext ->
                        commandContext.getSender().sendMessage(Component.text(getName() + " version " + getPluginMeta().getVersion()))
                ));
        commandManager.command(builder
                .senderType(Player.class)
                .permission("ah.commands.open")
                .handler(commandContext -> {
                    final Player sender = (Player) commandContext.getSender();
                    guiManager.openAuctionsGUI(sender);
                }));
    }
}

```
