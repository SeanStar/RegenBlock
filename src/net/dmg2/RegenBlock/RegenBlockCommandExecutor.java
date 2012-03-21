package net.dmg2.RegenBlock;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RegenBlockCommandExecutor implements CommandExecutor{

	//============================================================
	private RegenBlock plugin;
	public RegenBlockCommandExecutor(RegenBlock instance) {
		this.plugin = instance;
	}
	//============================================================
	
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		//Make sure command was not sent from console
		if (sender instanceof Player == false){
			plugin.log.info("/regenblock is only available in game.");
			return true;
		}
		
		//If no arguments display the default help message
		if (args.length == 0) return false;
		//Convert sender to Player
		Player player = (Player) sender;
		
		//=============================================================================================================================
		//First argument - RELOAD
		if (args[0].equalsIgnoreCase("reload")) {
			if (this.plugin.configFile.exists()) {
				this.plugin.blockListener.requeueOldBlocks();
				this.plugin.config.load();
				this.plugin.log.sendPlayerNormal(player, "RegenBlock is restoring blocks in queue and realoading settings.");
			} else {
				this.plugin.log.sendPlayerWarn(player, "RegenBlock was unable to find settings file.");
			}
			return true;
		}
		
		//=============================================================================================================================
		//First argument - blacklist
		if (args[0].equalsIgnoreCase("blacklist")) {
			if (args.length < 3) {
				this.plugin.log.sendPlayerNormal(player, "" + this.plugin.config.listBlacklistBlockId());
				return true;
			}
			
			if (args[1].equalsIgnoreCase("add")) {
				for (int i = 2 ; i < args.length ; i++) { this.plugin.config.addBlacklistBlockId(Integer.parseInt(args[i])); }
			} else if (args[1].equalsIgnoreCase("remove")) {
				for (int i = 2 ; i < args.length ; i++) { this.plugin.config.removeBlacklistBlockId(Integer.parseInt(args[i])); }
			} else { return false; }

			this.plugin.log.sendPlayerNormal(player, "Blacklist updated.");
			
			return true;
		}
		
		//=============================================================================================================================
		//First argument - LIST
		if (args[0].equalsIgnoreCase("list")) {
			//Make sure both locations are recorded
			if (plugin.playerSelectionLeft.containsKey(player.getName()) && plugin.playerSelectionRight.containsKey(player.getName())) {
				//If both locations has been set - Message player
				String right = plugin.playerSelectionRight.get(player.getName()).getBlockX() + " " + plugin.playerSelectionRight.get(player.getName()).getBlockY() + " " + plugin.playerSelectionRight.get(player.getName()).getBlockZ();
				String left = plugin.playerSelectionLeft.get(player.getName()).getBlockX() + " " + plugin.playerSelectionLeft.get(player.getName()).getBlockY() + " " + plugin.playerSelectionLeft.get(player.getName()).getBlockZ();
				plugin.log.sendPlayerNormal(player, "Right: " + right + " Left: " + left);
			} else {
				//Warn Player if not both locations are set
				plugin.log.sendPlayerWarn(player, "You need to set both spots first");
			}			
			return true;
		}
		
		//=============================================================================================================================
		//First argument - EDIT
		if (args[0].equalsIgnoreCase("edit")) {
			//Check player's status
			if (plugin.playerEditStatus.contains(player.getName())) {
				//In editor mode
				plugin.playerEditStatus.remove(player.getName());
				//Message player
				plugin.log.sendPlayerNormal(player, "Edit mode is OFF");
			} else {
				//Not in editor mode
				plugin.playerEditStatus.add(player.getName());
				//Message player
				plugin.log.sendPlayerNormal(player, "Edit mode is ON");
			}
			return true;
		}
		
		//=============================================================================================================================
		//First argument - DEBUG
		if (args[0].equalsIgnoreCase("debug")) {
			//Check status
			if (plugin.doDebug) {
				plugin.doDebug = false;
				//Message player
				plugin.log.sendPlayerNormal(player, "Debug mode is OFF");
				plugin.log.info("Debug mode is turned OFF by " + player.getName());
			} else {
				plugin.doDebug = true;
				//Message player
				plugin.log.sendPlayerNormal(player, "Debug mode is ON");
				plugin.log.info("Debug mode is turned ON by " + player.getName());
			}
			return true;
		}
		
		//=============================================================================================================================
		//First Argument - SELECT  
		if (args[0].equalsIgnoreCase("select")) {
			//Check if player's status
			if (plugin.playerSelectionStatus.contains(player.getName())) {
				//If it does - remove from the list - turn off the mode
				plugin.playerSelectionStatus.remove(player.getName());
				//Message player
				plugin.log.sendPlayerNormal(player, "Selection mode is OFF");
			} else {
				//If it does - remove from the list - turn off the mode
				plugin.playerSelectionStatus.add(player.getName());
				//Message player
				plugin.log.sendPlayerNormal(player, "Selection mode is ON");
			}
			return true;
		}

		//=============================================================================================================================
		//First Argument - REGION
		if (args[0].equalsIgnoreCase("region")) {
			//If nothing after region is entered, display usage
			if(args.length == 1) return false;
			
			//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			// /regenblock region create
			if(args[1].equalsIgnoreCase("create")) {
				//Check if both points are set
				if (plugin.playerSelectionLeft.containsKey(player.getName()) && plugin.playerSelectionRight.containsKey(player.getName())) {
					//We have two points
					if (args.length == 2) {
						plugin.log.sendPlayerWarn(player, "Usage: /regenblock region create (name) [re-spawn time] - assigns selection to a region.");
						return true;
					}
					
					//Get region name
					String regionName = args[2].toLowerCase();
					
					//Check if region name is already taken
					if (plugin.config.getRegionName(regionName) != null) {
						plugin.log.sendPlayerWarn(player, "Region name is already in use.");
						return true;
					}
					
					//Get re-spawn time for the region
					int respawnTime;
					if (args.length == 4) {
						respawnTime = Integer.parseInt(args[3]);
						if (respawnTime < 1) respawnTime = plugin.config.getRegionDefaultRespawnTime();
					} else {
						respawnTime = plugin.config.getRegionDefaultRespawnTime();
					}
					
					//Record region to configuration file
					plugin.config.setRegion(regionName, respawnTime, plugin.playerSelectionRight.get(player.getName()), plugin.playerSelectionLeft.get(player.getName()));
					
					//Print out the result to the player
					this.plugin.log.sendPlayerRegionCreate(player, regionName, respawnTime);
					return true;

				}
				//We do not have both points selected
				plugin.log.sendPlayerWarn(player, "You need to select two points before creating a region.");
				return true;
			}
			
			//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			// /regenblock region remove
			if(args[1].equalsIgnoreCase("remove")) {
				if (args.length == 2) { //Did not specify region name
					plugin.log.sendPlayerWarn(player, "Usage: /regenblock region remove (name) - removes region from the list.");
					return true;
				}
				//Get region name
				String regionName = args[2].toLowerCase();
				//Check if region exists
				if (plugin.config.getRegionName(regionName) == null) {
					plugin.log.sendPlayerWarn(player, "Region " + regionName + " does not exist.");
					return true;
				}
				//Remove region
				plugin.config.removeRegion(regionName);
				//Message player
				plugin.log.sendPlayerNormal(player, "Region " + regionName + " was removed.");
				//Log event
				plugin.log.info(player.getName() + " removed region " + regionName);
				return true;
			}
			
			//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			// /regenblock region modify
			if(args[1].equalsIgnoreCase("modify")) {
				if(args.length == 2) { //Did not specify anything
					plugin.log.sendPlayerWarn(player, "Usage: /regenblock region modify (name) [re-spawn time] - modify existing region.");
					return true;
				}
				
				if (args[2].equalsIgnoreCase("time")){
					// /regenblock region modify time
					if (args.length < 5) { //Did not actually enter name/time
						plugin.log.sendPlayerWarn(player, "Usage: /regenblock region modify time (name) (re-spawn time) - modify existing region's re-spawn time.");
						return true;
					}
					
					//Get name and time
					String regionName = args[3].toLowerCase();
					int respawnTime = Integer.parseInt(args[4]);
					if (respawnTime < 1) respawnTime = plugin.config.getRegionDefaultRespawnTime();
					
					//Update time property for the region
					plugin.config.setRegionRespawnTime(regionName, respawnTime);
					//Message player
					plugin.log.sendPlayerNormal(player, "Region " + regionName + " was updated to respawn time of " + respawnTime +"s.");
					//Log
					plugin.log.info(player + " updated region " + regionName + " to respawn time of " + respawnTime + "s.");
					return true;
					
				} else {
					//Get region name
					String regionName = args[2].toLowerCase();
					
					//Check if region name exists
					if (plugin.config.getRegionName(regionName) == null) {
						plugin.log.sendPlayerWarn(player, "Region name does not exist.");
						return true;
					}
					
					//Get re-spawn time for the region
					int respawnTime;
					if (args.length == 4) {
						respawnTime = Integer.parseInt(args[3]);
						if (respawnTime < 1) respawnTime = plugin.config.getRegionRespawnTime(regionName);
					} else {
						respawnTime = plugin.config.getRegionRespawnTime(regionName);
					}
					
					//Record region to configuration file
					plugin.config.setRegion(regionName, respawnTime, plugin.playerSelectionRight.get(player.getName()), plugin.playerSelectionLeft.get(player.getName()));
					
					//Print out the result to the player
					this.plugin.log.sendPlayerRegionCreate(player, regionName, respawnTime);
					return true;

				}

			}

			//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			// /regenblock region list
			if(args[1].equalsIgnoreCase("list")) {
				if (plugin.config.listRegions() != null) {
					plugin.log.listRegion(player, plugin.config.listRegions());
				} else {
					plugin.log.sendPlayerNormal(player, "There are no regions.");
				}
				return true;
			}

			//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			// /regenblock region blacklist regionName add/remove
			if(args[1].equalsIgnoreCase("blacklist")) {
				if (args.length < 3) return false;

				//Get region name
				String regionName = args[2].toLowerCase();
				//Check if region exists
				if (plugin.config.getRegionName(regionName) == null) {
					plugin.log.sendPlayerWarn(player, "Region " + regionName + " does not exist.");
					return true;
				}

				if (args.length < 4 && args.length > 2) {
					this.plugin.log.sendPlayerNormal(player, "" + this.plugin.config.listRegionBlacklistBlockId(args[2]));
					return true;
				}

				if (args[3].equalsIgnoreCase("add")) {
					for (int i = 4 ; i < args.length ; i++) { this.plugin.config.addRegionBlacklistBlockId(args[2], Integer.parseInt(args[i])); }
				} else if (args[3].equalsIgnoreCase("remove")) {
					for (int i = 4 ; i < args.length ; i++) { this.plugin.config.removeRegionBlacklistBlockId(args[2], Integer.parseInt(args[i])); }
				} else { return false; }

				this.plugin.log.sendPlayerNormal(player, "Region's blacklist updated.");
					
				return true;
			}

			//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			// /regenblock region type name typeId
			if(args[1].equalsIgnoreCase("type")) {
				if (args.length < 3) return false;
				
				//Get region name
				String regionName = args[2].toLowerCase();
				//Check if region exists
				if (plugin.config.getRegionName(regionName) == null) {
					plugin.log.sendPlayerWarn(player, "Region " + regionName + " does not exist.");
					return true;
				}
				
				if (args.length < 4) {
					this.plugin.log.sendPlayerNormal(player, "Region [" + regionName + "] type is " + this.plugin.config.getRegionType(args[2]));
					return true;
				}
				
				int type = 0;
				try { type = Integer.parseInt(args[3]);	}
				catch (NumberFormatException e) {}
				
				this.plugin.config.setRegionType(regionName, type);
				
				if (type == 1) {
					this.plugin.config.regionAddSpawnBlocks(regionName);
				}

				this.plugin.log.sendPlayerNormal(player, "Region [" + regionName + "] type was set to " + type);
					
				return true;
			}

			//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			// /regenblock region feedback
			if(args[1].equalsIgnoreCase("feedback")) {
				if (args.length < 4) { //Did not specify region name and or type
					plugin.log.sendPlayerWarn(player, "Usage: /regenblock region feedback (name) (feedback type [0,1,2])- changes the region's feedback type.");
					return true;
				}
				if(args[2].equalsIgnoreCase("set")) {
					String feedbackString = "";
					for (int i = 3 ; i < args.length ; i++) {
						feedbackString += args[i] + " ";
					}
					if (feedbackString.length() > 0) {
						this.plugin.config.setFeedbackString(feedbackString);
						this.plugin.log.sendPlayerNormal(player, "Feedback string was set to [" + feedbackString + "]");
					} else {
						this.plugin.log.sendPlayerWarn(player, "Feedback string was not changed.");
					}
					return true;
				}
				//Get region name
				String regionName = args[2].toLowerCase();
				//Check if region exists
				if (plugin.config.getRegionName(regionName) == null) {
					plugin.log.sendPlayerWarn(player, "Region " + regionName + " does not exist.");
					return true;
				}
				
				//Get region new feedback type
				int feedbackId = Integer.parseInt(args[3]);
				//Set region's feedback type
				feedbackId = this.plugin.config.setRegionFeedbackID(regionName, feedbackId);
				//Message player
				plugin.log.sendPlayerNormal(player, "Region " + regionName + " feedback type was set to " + feedbackId);
				return true;
			}
			
		}

		return false;
	}
	
}
