package fr.ninjagoku4560.loginguard;

import fr.ninjagoku4560.loginguard.utils.FileUtil;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;


import java.util.Objects;

public class Registering {

    static private final String FolderName = "password";
    public static boolean register(ServerPlayerEntity player, String password){
        String path = FolderName+"\\"+player.getName().getString()+".txt";
        // save the password
        FileUtil.writeToFile(path,password);
        // verify the password
        if (Objects.equals(FileUtil.read(path), password)) {
            return true;
        } else {
            player.sendMessage(Text.of("ERROR WHILE REGISTERING"));
            LoginGuard.LOGGER.warn("DEBUG DATA:");
            LoginGuard.LOGGER.warn("Saved password = "+FileUtil.read(path));
            LoginGuard.LOGGER.warn("Entered password = "+password);
        }
        return false;
    }

    public static boolean Registered(ServerPlayerEntity player) {
        String File = FolderName+"\\"+player.getName().getString()+".txt";
        String FileText = FileUtil.read(File);
        assert FileText != null;
        return FileUtil.FileNotEmpty(File);
    }
}
