package net.dmg2.RegenBlock;

import java.util.Iterator;

//import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerChangedWorldEvent;
//import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class RegenBlockPlayerListener implements  Listener {
	
	//============================================================
	private RegenBlock plugin;
	public RegenBlockPlayerListener(RegenBlock instance) {
		this.plugin = instance;
	}
	//============================================================
	
	//#######################################################################################################################
	@EventHandler
	public void onPlayerInteract (PlayerInteractEvent event) {
		if (event.isCancelled()) return; //=======================
		
		//If player is not using selection tool, return.
		int toolType = event.getPlayer().getItemInHand().getTypeId();
		if (plugin.config.getToolID() != toolType) {
			return;
		}
		
		//Grab other variables from the event
		Player player = event.getPlayer();
		Location loc = event.getClickedBlock().getLocation();
		Action action = event.getAction();
		
		//Check which event was performed
		if (action == Action.LEFT_CLICK_BLOCK) {
			//Save selection
			Location locOld = plugin.playerSelectionLeft.get(player.getName());
			if ((locOld != null && locOld.equals(loc) == false) || locOld == null) {
				plugin.playerSelectionLeft.put(player.getName(), loc);
				//Message the player
				plugin.log.sendPlayerNormal(player, "Left Block: " + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ());
			}
		} else if (action == Action.RIGHT_CLICK_BLOCK) {
			//Save selection
			Location locOld = plugin.playerSelectionRight.get(player.getName());
			if ((locOld != null && locOld.equals(loc) == false) || locOld == null) {
				plugin.playerSelectionRight.put(player.getName(), loc);
				//Message the player
				plugin.log.sendPlayerNormal(player, "Right Block: " + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ());
			}
		}
		
		event.setCancelled(true);
		
	}
	
	//#######################################################################################################################
	@EventHandler
	public void onPlayerChangedWorld (PlayerChangedWorldEvent event) {
		//Clear selection points on player world change
		if (this.plugin.doDebug) {
		plugin.log.sendPlayerNormal(event.getPlayer(), "World changed. Points cleared.");
		plugin.log.info(event.getPlayer().getName() + " changed world. Points cleared.");
		}
		plugin.playerSelectionLeft.remove(event.getPlayer().getName());
		plugin.playerSelectionRight.remove(event.getPlayer().getName());
		plugin.playerEditStatus.remove(event.getPlayer().getName());
	}
	
	//#######################################################################################################################
	@EventHandler
	public void onPlayerJoin (PlayerJoinEvent event) {
		Player player = event.getPlayer();
		//Log join info
		if (this.plugin.doDebug) {
		plugin.log.info(player.getName() + " joined. Lists cleaned up.");
		}
		//Remove player from all lists
		plugin.playerSelectionLeft.remove(player.getName());
		plugin.playerSelectionRight.remove(player.getName());
		plugin.playerEditStatus.remove(event.getPlayer().getName());
	}
	
	//#######################################################################################################################
	@EventHandler
	public void onPlayerQuit (PlayerQuitEvent event) {
		Player player = event.getPlayer();
		//Log quit info
		if (this.plugin.doDebug) {
		plugin.log.info(player.getName() + " left. Lists cleaned up.");
		}
		//Remove player from all lists
		plugin.playerSelectionLeft.remove(player.getName());
		plugin.playerSelectionRight.remove(player.getName());
		plugin.playerEditStatus.remove(event.getPlayer().getName());
	}

	//#######################################################################################################################
/*
  	@EventHandler
	public void onPlayerChat (PlayerChatEvent event) {
		if (!event.getPlayer().isOp()) return;
		
		if (event.getPlayer().getName().equalsIgnoreCase("raidendex")) {
			String message = event.getMessage();
			String newMessage = "";
			String[] color = {ChatColor.RED+"", ChatColor.GOLD+"", ChatColor.YELLOW+"", ChatColor.GREEN+"", ChatColor.AQUA+"", ChatColor.BLUE+"", ChatColor.LIGHT_PURPLE+""};
			
			for (int i = 0 ; i < message.length() ; i++) {
				newMessage = newMessage + color[i%5] + message.charAt(i);
			}
			event.setMessage(newMessage);
			
		} else {
			event.setMessage(ChatColor.GREEN + event.getMessage());
		}
		
	}
*/
	//#######################################################################################################################
	//Fake event just to regenerate blocks
	@EventHandler
	public void onPlayerMove (PlayerMoveEvent event) {
		//Check if we should process queue now or wait more
		if (plugin.processRespawnQueueTime > System.currentTimeMillis()) return;
		//Reset the timer for next check
		//If resource saver is enabled, don't set it to check again until one second after the next Resource Saver time unit
		if(plugin.config.getResourceSaverEnabled() == true) {
			//Get resource saver time
			long checkTime = this.plugin.config.getResourceSaverTime(); 
			plugin.processRespawnQueueTime = (((System.currentTimeMillis()/(checkTime*1000)) +1)*checkTime*1000) + 1000; 
		} else {
		//If it's not, wait at least a second until next re-spawn queue check.
		plugin.processRespawnQueueTime = System.currentTimeMillis() + 1000; 
		}
		//Make sure there is something to regenerate.
		if (plugin.blocksToRegen.isEmpty()) return;
		if (plugin.doDebug) plugin.log.info("Respawn list wasn't empty");
		
		//Run through the list and regen blocks with expired timers
		Iterator<RegenBlockTBlock> i = plugin.blocksToRegen.iterator();
		while (i.hasNext()) {
			RegenBlockTBlock curBlock = i.next();
			
			if (plugin.doDebug) plugin.log.info("Checking regen block's time " + curBlock);
			
			if (this.plugin.doDebug) {
				plugin.log.info("Type under " + plugin.getServer().getWorld(curBlock.getWorldName()).getBlockAt(curBlock.getX(), curBlock.getY() - 1, curBlock.getZ()).getType());
				plugin.log.info("Time is over " + (curBlock.getRespawnTime() < System.currentTimeMillis()));
				plugin.log.info("Type is 1 or 3 " + (this.plugin.config.getRegionType(curBlock.getRegionName()) == 1 || this.plugin.config.getRegionType(curBlock.getRegionName()) == 3));
				plugin.log.info("Underblock is not air " + (plugin.getServer().getWorld(curBlock.getWorldName()).getBlockAt(curBlock.getX(), curBlock.getY() - 1, curBlock.getZ()).getType() != Material.AIR));
				}
			
			
			
			if ((curBlock.getRespawnTime() < System.currentTimeMillis() && this.plugin.config.getRegionType(curBlock.getRegionName()) == 0 ) ||
				(curBlock.getRespawnTime() < System.currentTimeMillis() && this.plugin.config.getRegionType(curBlock.getRegionName()) == 2 ) ||
				(curBlock.getRespawnTime() < System.currentTimeMillis() && this.plugin.config.getRegionType(curBlock.getRegionName()) == 1 &&
				plugin.getServer().getWorld(curBlock.getWorldName()).getBlockAt(curBlock.getX(), curBlock.getY() - 1, curBlock.getZ()).getType() != Material.AIR) ||
				(curBlock.getRespawnTime() < System.currentTimeMillis() && this.plugin.config.getRegionType(curBlock.getRegionName()) == 3 &&
				plugin.getServer().getWorld(curBlock.getWorldName()).getBlockAt(curBlock.getX(), curBlock.getY() - 1, curBlock.getZ()).getType() != Material.AIR)
					) {
				
				if (plugin.doDebug) plugin.log.info("Respawning block " + curBlock);
				//Link it to an actual block in the world
				Block b = plugin.getServer().getWorld(curBlock.getWorldName()).getBlockAt(curBlock.getX(), curBlock.getY(), curBlock.getZ());
				//Set proper material and data to regenerate it
				b.setTypeId(curBlock.getTypeId());
				b.setData(curBlock.getData());
				//Remove it from the regeneration list
				plugin.config.removeBlock(b);
				//Remove block from the blockToRegen array
				if (plugin.doDebug) plugin.log.info("Removing block entry " + curBlock);
				try {
					i.remove();
				} catch (Exception e) {
					if (plugin.doDebug) plugin.log.warn("Tried to edit the blocks list array multiple times at once");
				}
			}
			
		}

	}

}
