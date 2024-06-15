package fr.ninjagoku4560.loginguard;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import fr.ninjagoku4560.loginguard.utils.FileUtil;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class LoginGuard implements ModInitializer {
    public static final String MOD_ID = "loginguard";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    // Set to keep track of immobilized players
    private static final Set<ServerPlayerEntity> immobilizedPlayers = new HashSet<>();

    @Override
    public void onInitialize() {
        // Log a message when the mod is initialized
        LOGGER.info("Initializing LoginGuard");

        // Register commands
        Registering.init();

        // Register the player join event
        ServerPlayConnectionEvents.JOIN.register(this::onPlayerJoin);

        // Register server tick event to handle immobilized players
        ServerTickEvents.START_SERVER_TICK.register(this::onServerTick);

        CommandRegistrationCallback.EVENT.register(this::registerCommands);
    }

    private void onPlayerJoin(ServerPlayNetworkHandler handler, PacketSender sender, MinecraftServer server) {
        ServerPlayerEntity player = handler.getPlayer();

        // Immobilize the player
        immobilizedPlayers.add(player);
        if (!FileUtil.fileExists("password\\" + player.getName().getString() + ".txt")) {
            FileUtil.writeToFile("password\\" + player.getName().getString() + ".txt", "");
        }
        player.sendMessage(Text.translatable("FreezeMessage"));
    }

    private void onServerTick(MinecraftServer server) {
        BlockPos spawn = server.getOverworld().getSpawnPos();
        // Iterate over immobilized players and prevent movement
        for (ServerPlayerEntity player : immobilizedPlayers) {
            player.setVelocity(0, 0, 0);
            player.teleport(spawn.getX(), spawn.getY(), spawn.getZ(), false);
        }
    }

    public static void releasePlayer(ServerPlayerEntity player) {
        immobilizedPlayers.remove(player);
    }

    private void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(CommandManager.literal("register")
                .then(CommandManager.argument("password", StringArgumentType.word())
                        .then(CommandManager.argument("confirmPassword", StringArgumentType.word())
                                .executes(context -> {
                                    String password = StringArgumentType.getString(context, "password");
                                    String confirmPassword = StringArgumentType.getString(context, "confirmPassword");
                                    ServerPlayerEntity player = context.getSource().getPlayer();
                                    assert player != null && password != null && confirmPassword != null;

                                    String playerFileName = "password\\" + player.getName().getString() + ".txt";

                                    LOGGER.info(FileUtil.FileNotEmpty(playerFileName));
                                    // Verify if the player is already registered
                                    if (FileUtil.FileNotEmpty(playerFileName)) {
                                        player.sendMessage(Text.translatable("AlreadyRegister"));
                                        return 1;
                                    }
                                    LOGGER.info("Not register");
                                    if (password.equals(confirmPassword)) {
                                        boolean registered = Registering.register(player, password);
                                        if (registered) {
                                            // Player can move
                                            releasePlayer(player);
                                            player.sendMessage(Text.translatable("RegisterDone"));
                                        }
                                    } else {
                                        player.sendMessage(Text.translatable("difPassword"));
                                        player.sendMessage(Text.translatable("TryAgain"));
                                    }
                                    return 1;
                                })
                        )
                )
        );

        dispatcher.register(CommandManager.literal("login")
                .then(CommandManager.argument("password", StringArgumentType.word())
                        .executes(context -> {
                            String password = StringArgumentType.getString(context, "password");
                            ServerPlayerEntity player = context.getSource().getPlayer();
                            assert player != null && password != null;
                            // 0 = Wrong Password
                            // 1 = Login Done
                            // 2 = Not Register
                            int LoginCode = Login.check(player, password);
                            if (LoginCode == 1) {
                                // Player can move
                                releasePlayer(player);
                                player.sendMessage(Text.translatable("LoginDone"));
                            } else if (LoginCode == 0) {
                                player.sendMessage(Text.translatable("WrongPassword"));
                                player.sendMessage(Text.translatable("TryAgain"));
                            } else if (LoginCode == 2) {
                                player.sendMessage(Text.translatable("NotRegister"));
                            }
                            return 1;
                        })
                )
        );
    }
}