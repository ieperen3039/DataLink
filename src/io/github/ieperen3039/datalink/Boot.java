package io.github.ieperen3039.datalink;

import io.github.ieperen3039.datalink.Core.Main;
import io.github.ieperen3039.ngn.Tools.FlagManager;
import io.github.ieperen3039.ngn.Settings.Settings;
import io.github.ieperen3039.ngn.Tools.Logger;
import org.lwjgl.system.Configuration;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Tags the Flags, Boots the Roots
 * @author Geert van Ieperen. Created on 13-9-2018.
 */
public class Boot {
    public static void main(String[] args) throws Exception {
        Settings settings = new Settings();
//        Logger.setLoggingLevel(Logger.INFO);
        Logger.setLoggingLevel(Logger.DEBUG);

        new File("logs").mkdir();
        File defaultLog = new File("logs/output1.log");
        for (int i = 2; defaultLog.exists(); i++) {
            defaultLog = new File("logs/output" + i + ".log");
        }

        new FlagManager()
                .addFlag("debug", () -> Logger.doPrintCallsites = true,
                        "Sets logging to DEBUG level")
                .addFlag("quiet", () -> Logger.setLoggingLevel(Logger.INFO),
                        "Sets logging to INFO level")
                .addFlag("silent", () -> Logger.setLoggingLevel(Logger.ERROR),
                        "Sets logging to ERROR level")
                .addExclusivity("debug", "quiet", "silent")

                .addParameterFlag("log", defaultLog.getAbsolutePath(),
                        file -> Logger.setOutputStream(new FileOutputStream(file)),
                        "Sets logging to write to the file with the given name. If the file exists, it is overwritten. " +
                                "By default, it writes to a generated new file"
                )
                .addFlag("lwjglDebug", () -> Configuration.DEBUG.set(true),
                        "Activates logging of underlying libraries")
                .addFlag("no-timing", () -> Logger.doPrintTimeStamps = false,
                        "Removes timestamps from logging")

                .addFlag("enableRenderTiming", () -> settings.ACCURATE_RENDER_TIMING = true,
                        "enable measuring the runtime of the rendering procedure")
                .parse(args);

        new Main(settings).root();
    }
}
