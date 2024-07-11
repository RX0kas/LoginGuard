package fr.ninjagoku4560.loginguard;

import fr.ninjagoku4560.loginguard.utils.FileUtil;
import net.minecraft.server.network.ServerPlayerEntity;
import org.sqlite.SQLiteLimits;

import java.io.File;
import java.sql.SQLException;
import java.util.Objects;

public class Login {
    static private final String FolderName = "password";
    public static int check(ServerPlayerEntity player, String password) {

        String Truepassword = "NULL";
        try {
            Truepassword = SQLiteHandler.retrieveRegData(player.getName().getString());
        } catch (SQLException e) {}



        if(Truepassword.equals("NULL")){
            return 2;
        } else if (password.equals(Truepassword)) {
            return 1;
        }
        else {
            return 0;
        }
    }
}
