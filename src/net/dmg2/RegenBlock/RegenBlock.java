package net.dmg2.RegenBlock;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import org.bukkit.Location;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class RegenBlock extends JavaPlugin {
	
	//=============================================================================================
	protected RegenBlockLogHandler log;
	private RegenBlockCommandExecutor commandExecutor;
	public String pluginPath;
	public File configFile;
	public RegenBlockConfig config;
	public final RegenBlockBlockListener blockListener = new RegenBlockBlockListener(this);
	private final RegenBlockPlayerListener playerListener = new RegenBlockPlayerListener(this);
	
	public ArrayList<RegenBlockTBlock> blocksToRegen = new ArrayList<RegenBlockTBlock>();
	public long processRespawnQueueTime = System.currentTimeMillis();
	public boolean doDebug = false;
	public boolean isDecayEventBlock = false; 
	//=============================================================================================
	
	//---------------------------------------------------------------------------------------------
	//HashMaps to store player's selections
	public HashMap<String, Location> playerSelectionLeft = new HashMap<String, Location>();
	public HashMap<String, Location> playerSelectionRight = new HashMap<String, Location>();
	public ArrayList<String> playerSelectionStatus = new ArrayList<String>();
	public ArrayList<String> playerEditStatus = new ArrayList<String>();
	//---------------------------------------------------------------------------------------------
	
	
	public void onEnable(){
    	//Log
    	this.log = new RegenBlockLogHandler(this);
    	this.log.info("Enabled. Good Day.");
    	
    	//Events handler
    	PluginManager pm = this.getServer().getPluginManager();
    	
    	pm.registerEvents(this.blockListener, this);
    	pm.registerEvents(this.playerListener, this);

    	//Settings file - [Update API]
    	this.pluginPath = this.getDataFolder().getAbsolutePath();
    	this.configFile = new File(this.pluginPath + File.separator + "config.yml");
    	this.config = new RegenBlockConfig(this.configFile);
    	
    	
    	//Commands handler
    	this.commandExecutor = new RegenBlockCommandExecutor(this);
    	this.getCommand("regenblock").setExecutor(this.commandExecutor);
    	this.getCommand("rb").setExecutor(this.commandExecutor);
    	
    	//Restore blocks from old sessions
    	blockListener.requeueOldBlocks();
    	
    }

    public void onDisable(){
    	//Restore blocks from this session before shut down
    	//blockListener.regenOldBlocks();

    	//Save configuration file
    	this.config.save();

    	//Log
    	this.log.info("Disabled. Good Bye.");
    	
    }
    
}
