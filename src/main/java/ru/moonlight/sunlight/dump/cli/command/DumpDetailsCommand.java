package ru.moonlight.sunlight.dump.cli.command;

import lombok.AllArgsConstructor;
import ru.moonlight.sunlight.dump.SunlightDump;
import ru.moonlight.sunlight.dump.cli.CommandExecutor;
import ru.moonlight.sunlight.dump.service.SunlightItemDetailsService;

@AllArgsConstructor
public final class DumpDetailsCommand implements CommandExecutor {

    private final SunlightItemDetailsService itemDetailsService;

    public DumpDetailsCommand(SunlightDump application) {
        this.itemDetailsService = application.getItemDetailsService();
    }

    @Override
    public void execute() throws Exception {
        itemDetailsService.runService();
    }

}
