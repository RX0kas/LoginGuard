package fr.ninjagoku4560.loginguard;

import fr.ninjagoku4560.loginguard.utils.FileUtil;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;


import java.io.File;
import java.util.Objects;

public class Registering {

    static private final String FolderName = "password";
    public static boolean register(ServerPlayerEntity player, String password){
        String path = FolderName+File.separator+player.getName().getString()+".txt";
        player.getDimensions(player.getPose());
        // save the password
        FileUtil.writeToFile(path,password);
        // verify the password
        if (Objects.equals(FileUtil.read(path), password)) {
            return true;
        } else {
            player.sendMessage(Text.of("ERROR WHILE REGISTERING"));
            LoginGuard.LOGGER.warn("Error while registering "+player.getName().getString()+":");
            LoginGuard.LOGGER.warn("Saved password = "+FileUtil.read(path));
            LoginGuard.LOGGER.warn("Entered password = "+password);
        }
        return false;
    }

    public static boolean Registered(ServerPlayerEntity player) {
        String f = FolderName+File.separator+player.getName().getString()+".txt";
        String FileText = FileUtil.read(f);
        assert FileText != null;
        return FileUtil.FileNotEmpty(f);
    }
}
