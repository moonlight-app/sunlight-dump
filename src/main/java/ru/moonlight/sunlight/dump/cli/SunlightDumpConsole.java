package ru.moonlight.sunlight.dump.cli;

import lombok.Getter;
import ru.moonlight.sunlight.dump.SunlightDump;
import ru.moonlight.sunlight.dump.cli.command.DumpCatalogCommand;
import ru.moonlight.sunlight.dump.cli.command.DumpDetailsCommand;
import ru.moonlight.sunlight.dump.cli.command.LoadCatalogCommand;
import ru.moonlight.sunlight.dump.cli.command.LoadDetailsCommand;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

@Getter
public final class SunlightDumpConsole implements AutoCloseable {

    private static final String HEADER = """
            
            ###  SUNLIGHT DUMP  ###
          
            """;

    private static final String HELP_MESSAGE = """
            Available commands:
              h   help           :   print this message
              dc  dump-catalog   :   make dump of the catalog items
              dd  dump-details   :   make dump of items details
              lc  load-catalog   :   load dump of the catalog items
              ld  load-details   :   load dump of items details
              md  merge-dumps    :   merge data from both dumps
              gs  generate-sql   :   generate inserting SQL script
              e   exit           :   close the application
            """;

    private final SunlightDump application;
    private final Scanner scanner;
    private final Map<CommandEnum, CommandExecutor> registeredCommands;

    public SunlightDumpConsole(SunlightDump application) {
        this.application = application;
        this.scanner = new Scanner(System.in);
        this.registeredCommands = new HashMap<>();
        registerDefaultCommands();
    }

    public void openAndWait() {
        System.out.print(HEADER);
        System.out.println(HELP_MESSAGE);

        inputLoop: while (true) {
            System.out.print("sunlight-dump > ");
            String input = scanner.nextLine();
            System.out.println();

            switch (input) {
                case "h", "help" -> System.out.print(HELP_MESSAGE);
                case "dc", "dump-catalog" -> runCommand(CommandEnum.DUMP_CATALOG);
                case "dd", "dump-details" -> runCommand(CommandEnum.DUMP_DETAILS);
                case "lc", "load-catalog" -> runCommand(CommandEnum.LOAD_CATALOG);
                case "ld", "load-details" -> runCommand(CommandEnum.LOAD_DETAILS);
                case "md", "merge-dumps" -> runCommand(CommandEnum.MERGE_DUMPS);
                case "gs", "generate-sql" -> runCommand(CommandEnum.GENERATE_SQL);
                case "e", "exit" -> {
                    break inputLoop;
                }
            }
        }
    }

    @Override
    public void close() {
        scanner.close();
    }

    private void runCommand(CommandEnum command) {
        CommandExecutor executor = registeredCommands.get(command);
        if (executor != null) {
            try {
                executor.execute();
            } catch (Exception ex) {
                System.err.println("An error occurred while trying to execute command:");
                ex.printStackTrace(System.err);
            }
        } else {
            System.err.println("This command isn't implemented yet!");
        }

        System.out.println();
    }

    private void registerDefaultCommands() {
        registeredCommands.put(CommandEnum.DUMP_CATALOG, new DumpCatalogCommand(application));
        registeredCommands.put(CommandEnum.DUMP_DETAILS, new DumpDetailsCommand(application));
        registeredCommands.put(CommandEnum.LOAD_CATALOG, new LoadCatalogCommand(application));
        registeredCommands.put(CommandEnum.LOAD_DETAILS, new LoadDetailsCommand(application));
    }

}
