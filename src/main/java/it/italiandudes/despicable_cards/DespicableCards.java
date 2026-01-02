package it.italiandudes.despicable_cards;

import it.italiandudes.despicable_cards.features.DiscordRichPresenceManager;
import it.italiandudes.despicable_cards.javafx.Client;
import it.italiandudes.despicable_cards.utils.Defs;
import it.italiandudes.idl.common.*;
import it.italiandudes.idl.handler.JarHandler;
import it.italiandudes.idl.logger.InfoFlags;
import it.italiandudes.idl.logger.Logger;
import org.apache.commons.lang3.SystemUtils;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.jar.Attributes;

public final class DespicableCards {

    // Attributes
    private static volatile boolean appClosed = false;

    // Launcher Class Loader
    @Nullable private static ClassLoader launcherClassLoader = null;

    // Launcher Methods
    public static @Nullable ClassLoader getLauncherClassLoader() {
        return launcherClassLoader;
    }
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isStartedFromLauncher() {
        return launcherClassLoader != null;
    }

    // Launcher Main Method
    @SuppressWarnings("unused")
    public static void launcherMain(ClassLoader loader, String[] args) {
        launcherClassLoader = loader;
        if (loader != null) Thread.currentThread().setContextClassLoader(loader);
        main(args);
    }
    public static void appClosed() {
        appClosed = true;
    }

    // Launcher Blocker
    @SuppressWarnings("unused")
    public static void launcherLockUntilAppClose() {
        while (!appClosed) Thread.onSpinWait();
    }

    // Main Method
    public static void main(String[] args) {

        // Setting Charset to UTF-8
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(System.err, true, StandardCharsets.UTF_8));

        // Initializing the logger
        try {
            Logger.init();
            Logger.log("Logger initialized!", Defs.LOGGER_CONTEXT);
        } catch (IOException e) {
            Logger.log("An error has occurred during Logger initialization, exit...", Defs.LOGGER_CONTEXT);
            return;
        }

        // Configure the shutdown hooks
        Logger.log("Configuring Shutdown Hooks...", Defs.LOGGER_CONTEXT);
        Runtime.getRuntime().addShutdownHook(new Thread(Logger::close));
        // Runtime.getRuntime().addShutdownHook(new Thread(DBManager::closeConnection));
        Runtime.getRuntime().addShutdownHook(new Thread(DiscordRichPresenceManager::shutdownRichPresence));
        Logger.log("Shutdown Hooks configured!", Defs.LOGGER_CONTEXT);

        // Check Java Version
        Logger.log("Verifying for Java 21...", Defs.LOGGER_CONTEXT);
        if (!SystemUtils.IS_JAVA_21) {
            Logger.log("The current java version is wrong, to run this jar you need Java 21!", new InfoFlags(true, true, true, true), Defs.LOGGER_CONTEXT);
            if (!isStartedFromLauncher()) {
                Logger.close();
                System.exit(-1);
            }
            return;
        }
        Logger.log("Java 21 Verified!", Defs.LOGGER_CONTEXT);

        // Check OS
        Logger.log("Verifying OS...", Defs.LOGGER_CONTEXT);
        Logger.log("OS Name: " + SystemUtils.OS_NAME, Defs.LOGGER_CONTEXT);
        Logger.log("OS Arch: " + SystemUtils.OS_ARCH, Defs.LOGGER_CONTEXT);
        Logger.log("Current OS Platform: " + (Defs.CURRENT_PLATFORM != null ? Defs.CURRENT_PLATFORM.getName() : "NOT RECOGNIZED"), Defs.LOGGER_CONTEXT);
        if (Defs.CURRENT_PLATFORM == null) {
            Logger.log("WARNING: Current OS Platform not recognized! An attempt to start the app will be done anyway.", new InfoFlags(true, false, false, true), Defs.LOGGER_CONTEXT);
        }
        try {
            Attributes manifestAttributes = JarHandler.ManifestReader.readJarManifest(Defs.JAR_POSITION);
            @Nullable String manifestTargetPlatform = JarHandler.ManifestReader.getValue(manifestAttributes, "Target-Platform");
            if (manifestTargetPlatform == null) {
                Logger.log("Target-Platform not specified in jar manifest, this jar shouldn't be used for release.", new InfoFlags(false, false, false, true), Defs.LOGGER_CONTEXT);
            } else {
                @Nullable TargetPlatform targetPlatform = TargetPlatform.fromManifestTargetPlatform(manifestTargetPlatform);
                if (targetPlatform == null) {
                    Logger.log("Target-Platform provided \"" + manifestTargetPlatform + "\" not recognized, this jar shouldn't be used for release.", new InfoFlags(false, false, false, true), Defs.LOGGER_CONTEXT);
                } else {
                    Logger.log("Jar Target-Platform: " + targetPlatform.getName(), Defs.LOGGER_CONTEXT);
                    if (Defs.CURRENT_PLATFORM != null && !targetPlatform.isCurrentOS()) {
                        Logger.log("Target-Platform \"" + targetPlatform.getName() + "\" incompatible with the current OS Platform!", new InfoFlags(true, true, true, true), Defs.LOGGER_CONTEXT);
                        if (!isStartedFromLauncher()) {
                            Logger.close();
                            System.exit(-1);
                        }
                        return;
                    }
                }
            }
        } catch (IOException e) {
            Logger.log("An error has occurred while attempting to read jar manifest!", new InfoFlags(true, true, true, true), Defs.LOGGER_CONTEXT);
            if (!isStartedFromLauncher()) {
                Logger.close();
                System.exit(-1);
            }
            return;
        }

        // Start the client
        try {
            Logger.log("Starting UI...", Defs.LOGGER_CONTEXT);
            Client.start(args);
        } catch (NoClassDefFoundError e) {
            Logger.log("ERROR: JAVAFX NOT FOUND!", new InfoFlags(true, true, true, true), Defs.LOGGER_CONTEXT);
            Logger.log(e, Defs.LOGGER_CONTEXT);
            if (!isStartedFromLauncher()) {
                Logger.close();
                System.exit(-1);
            }
        } catch (Exception e) {
            Logger.log("An exception has occurred while starting UI!", new InfoFlags(true, true, true, true), Defs.LOGGER_CONTEXT);
            Logger.log(e, Defs.LOGGER_CONTEXT);
            if (!isStartedFromLauncher()) {
                Logger.close();
                System.exit(-1);
            }
        }
    }
}
