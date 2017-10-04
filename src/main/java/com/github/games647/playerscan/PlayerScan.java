package com.github.games647.playerscan;

import net.md_5.bungee.api.plugin.Plugin;

public class PlayerScan extends Plugin {

    @Override
    public void onEnable() {
        String pluginName = getDescription().getName().toLowerCase();
        getProxy().getPluginManager().registerCommand(this, new ScanCommand(pluginName));
    }
}
