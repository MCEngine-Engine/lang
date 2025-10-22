package io.github.mcengine.papermc.lang.engine;

import io.github.mcengine.api.core.MCEngineCoreApi;
import io.github.mcengine.api.core.Metrics;
import io.github.mcengine.common.lang.MCEngineLangCommon;
import io.github.mcengine.common.lang.command.MCEngineLangCommand;
import io.github.mcengine.common.lang.tabcompleter.MCEngineLangTabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main PaperMC plugin class for MCEngineLang.
 */
public class MCEngineLangPaperMC extends JavaPlugin {

    /**
     * Lang common API instance that wires the Bukkit plugin
     * to the selected database implementation and exposes
     * language preference operations and YAML lookups.
     */
    private MCEngineLangCommon api;

    /**
     * Called when the plugin is enabled.
     */
    @Override
    public void onEnable() {
        new Metrics(this, 27223); // keep existing metric id unless configured otherwise
        saveDefaultConfig(); // Save config.yml if it doesn't exist

        boolean enabled = getConfig().getBoolean("enable", false);
        if (!enabled) {
            getLogger().warning("Plugin is disabled in config.yml (enable: false). Disabling plugin...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        String license = getConfig().getString("licenses.license", "free"); 
        if (!license.equalsIgnoreCase("free")) { 
            getLogger().warning("Plugin is disabled in config.yml.");
            getLogger().warning("Invalid license.");
            getLogger().warning("Check license or use \"free\".");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        api = new MCEngineLangCommon(this);

        // Register /lang command + tab completion
        if (getCommand("lang") != null) {
            getCommand("lang").setExecutor(new MCEngineLangCommand(this, api));
            getCommand("lang").setTabCompleter(new MCEngineLangTabCompleter(this));
        } else {
            getLogger().warning("Command 'lang' not found in plugin.yml");
        }

        // Load extensions for Lang
        MCEngineCoreApi.loadExtensions(
            this,
            "io.github.mcengine.api.lang.extension.library.IMCEngineLangLibrary",
            "libraries",
            "Library"
        );
        MCEngineCoreApi.loadExtensions(
            this,
            "io.github.mcengine.api.lang.extension.api.IMCEngineLangAPI",
            "apis",
            "API"
        );
        MCEngineCoreApi.loadExtensions(
            this,
            "io.github.mcengine.api.lang.extension.agent.IMCEngineLangAgent",
            "agents",
            "Agent"
        );
        MCEngineCoreApi.loadExtensions(
            this,
            "io.github.mcengine.api.lang.extension.addon.IMCEngineLangAddOn",
            "addons",
            "AddOn"
        );
        MCEngineCoreApi.loadExtensions(
            this,
            "io.github.mcengine.api.lang.extension.dlc.IMCEngineLangDLC",
            "dlcs",
            "DLC"
        );

        // Check for plugin updates
        MCEngineCoreApi.checkUpdate(
            this,
            getLogger(),
            "github",
            "MCEngine-Engine",
            "lang",
            getConfig().getString("github.token", "null")
        );
    }

    /**
     * Called when the plugin is disabled.
     */
    @Override
    public void onDisable() {
        // No async pools to shut down in Lang common at this time.
    }
}
