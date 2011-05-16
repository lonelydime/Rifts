package com.lonelydime.Rifts;

//import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerListener;
import org.bukkit.plugin.Plugin;
import com.iConomy.*;

public class server extends ServerListener {
    private Rifts plugin;

    public server(Rifts plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPluginDisable(PluginDisableEvent event) {
        if (Rifts.iConomy != null) {
            if (event.getPlugin().getDescription().getName().equals("iConomy")) {
                Rifts.iConomy = null;
                System.out.println("[Rifts] un-hooked from iConomy.");
            }
        }
    }

    @Override
    public void onPluginEnable(PluginEnableEvent event) {
        if (Rifts.iConomy == null) {
            Plugin iConomy = plugin.getServer().getPluginManager().getPlugin("iConomy");

            if (iConomy != null) {
                if (iConomy.isEnabled()) {
                    Rifts.iConomy = (iConomy)iConomy;
                    System.out.println("[Rifts] hooked into iConomy.");
                }
            }
        }
    }
}