package net.dmg2.RegenBlock;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.LeavesDecayEvent;

public class RegenBlockBlockListener implements  Listener {
	
//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ CONSTRUCTOR @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	//============================================================
	private RegenBlock plugin;
	private Random rnd = new Random();
	public RegenBlockBlockListener(RegenBlock instance) {
		this.plugin = instance;
	}
	//============================================================
//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ CONSTRUCTOR @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

	

	
	
//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ EVENTS @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	//##########################################################################################################
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event){
		if(event.isCancelled()) return; //========================
		
		//Check if player is in editor mode
		if(isPlayerEditor(event.getPlayer().getName())) return;
		
		//Restore the block
		regenBlock(event.getBlock(), event.getBlock().getType(), event.getPlayer());
	}
	//##########################################################################################################
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		if(event.isCancelled()) return; //========================
		
		//Check if player is in editor mode
		if(isPlayerEditor(event.getPlayer().getName())) return;

		//Restore the block
		event.getEventName();
		regenBlock(event.getBlock(), Material.AIR, event.getPlayer());
	}
	//##########################################################################################################
	@EventHandler
	public void onLeavesDecay(LeavesDecayEvent event) {
		if(event.isCancelled()) return; //========================
		regenBlock(event.getBlock(), Material.LEAVES, null);
	}
	//##########################################################################################################
//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ EVENTS @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	

	
//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ FUNCTIONS @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ FUNCTIONS @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ FUNCTIONS @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ FUNCTIONS @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	
	//============================================================
	public boolean isPlayerEditor(String playerName) {
		if (plugin.playerEditStatus.contains(playerName)) return true;
		return false;
	}
	//============================================================

	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	public void regenBlock(Block block, Material material, Player player) {
		//Make sure the block is not blacklisted, if it is we just ignore the event
		if (this.plugin.config.listBlacklistBlockId() != null && this.plugin.config.listBlacklistBlockId().contains(String.valueOf(block.getTypeId()))) return;
		
		//Make sure mat passed in is not fire ... happens sometimes it seems =.=
		if (material == Material.FIRE) material = Material.AIR;
		byte data = block.getData();
		
		//Get region name
		String regionName = this.getBlockRegion(block);
		
		//Check if we should ignore the block
		if (this.ignoreBlock(block, regionName)) return;

		//Get region's re-spawn time
		long respawnTime = this.plugin.config.getRegionRespawnTime(regionName); 
		//Get region type
		int regionType = this.plugin.config.getRegionType(regionName);
		//Adjust re-spawn time to milliseconds system time for region types 0 and 1
		if(regionType != 2) {
		respawnTime = System.currentTimeMillis() + respawnTime*1000;
		}
		//----------------------------------------------------------------------------
		// Check what region type this is. Normal will be 0 or null if nothing present
		// but we never check for that.
		if (regionType == 1) {
			//Mine region
			//Randomize regen block if not air
			if (material != Material.AIR) {
				//Get list of spawnBlocks for the region
				HashMap<Integer, Integer> spawnBlocksId = this.plugin.config.getRegionSpawnBlocks(regionName);
				
				Iterator<Integer> i = spawnBlocksId.keySet().iterator();
				int totalChance = 0;
				while (i.hasNext()) { totalChance += spawnBlocksId.get(i.next()); }
				
				if (totalChance == 0) return;
				
				int roll = rnd.nextInt(totalChance);
				
				int typeId = 1;
				i = spawnBlocksId.keySet().iterator();
				totalChance = 0;
				
				while (i.hasNext()) {
					int blockId = i.next();
					int blockIdChance = spawnBlocksId.get(blockId);
					totalChance += blockIdChance;
					if (roll <= totalChance && roll >= totalChance - blockIdChance) {
						typeId = blockId;
						break;
					}
				}
				
				material = Material.getMaterial(typeId);
				data = 0;
			}
			
		}
		//If the region type is 2, set to regenerate all at once
		if (regionType == 2) {
			//Set block respawn time to the next closest time divisible by respawn time set
			respawnTime = ((System.currentTimeMillis()/(respawnTime*1000)) +1)*respawnTime*1000;
			
		}
		//----------------------------------------------------------------------------
		

		//Message the player based on region's feedback type
		if ((this.plugin.config.getRegionFeedbackID(regionName) == 1 && material == Material.AIR) || this.plugin.config.getRegionFeedbackID(regionName) == 2) {
			Pattern pat = Pattern.compile("TIME");
			Matcher mat = pat.matcher(this.plugin.config.getFeedbackString());
			this.plugin.log.sendPlayerWarn(player, mat.replaceAll(String.valueOf(plugin.config.getRegionRespawnTime(regionName))));					
		}

		//Queue the block for regeneration
		this.queueBlock(block, material, data, regionName, respawnTime);

	}
	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	public String getBlockRegion(Block block) {
		
		if (plugin.config.listRegions() == null) return null;

		for (String regionName : plugin.config.listRegions()) {

			//Get world name
			String worldName = plugin.config.getRegionWorldName(regionName);

			//Make sure block is in this region's world before checking further
			if (block.getWorld().getName().equalsIgnoreCase(worldName) == false) continue;

			//Get region coordinates
			int leftX = plugin.config.getRegionLeftX(regionName);
			int leftY = plugin.config.getRegionLeftY(regionName);
			int leftZ = plugin.config.getRegionLeftZ(regionName);

			int rightX = plugin.config.getRegionRightX(regionName);
			int rightY = plugin.config.getRegionRightY(regionName);
			int rightZ = plugin.config.getRegionRightZ(regionName);
			
			//Check if block is within the region
			if (Math.abs(leftX - rightX) == Math.abs(leftX - block.getX()) + Math.abs(rightX - block.getX()) &&
					Math.abs(leftY - rightY) == Math.abs(leftY - block.getY()) + Math.abs(rightY - block.getY()) &&
					Math.abs(leftZ - rightZ) == Math.abs(leftZ - block.getZ()) + Math.abs(rightZ - block.getZ())) {
				
				//Return the re-spawn time
		    	return regionName;

			}
			
		}	
		return null;
	}
	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	public boolean ignoreBlock(Block block, String regionName) {
		
		//Check if we block was even in a region
		if (regionName == null) return true;
		
		//Check if block TypeId is blacklisted in the region
		if (this.plugin.config.listRegionBlacklistBlockId(regionName) != null && this.plugin.config.listRegionBlacklistBlockId(regionName).contains(String.valueOf(block.getTypeId()))) return true;

		//Check if the block is already being regenerated
		if ( plugin.config.getBlockToRegen(this.plugin.config.getRegionWorldName(regionName), block) != null) return true;

		return false;
	}
	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	public void queueBlock(Block block, Material material, byte data, String regionName, long respawnTime) {
		//Save block to configuration in case we crash
		plugin.config.setBlock(block, material.getId(), data, regionName, respawnTime);

		//Store block's properties
		RegenBlockTBlock tb = new RegenBlockTBlock(block.getX(), block.getY(), block.getZ(), data, material.getId(), respawnTime, block.getWorld().getName(), regionName);
		plugin.blocksToRegen.add(tb);

		if (plugin.doDebug) plugin.log.info("[" + block.getWorld().getName() + "][" + block.getX() + "," + block.getY() + "," + block.getZ()
												+ "][" + material + "][" + block.getData() + "]");
		
	}
	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	

	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	public void requeueOldBlocks() {
		//Make sure there are worlds listed
		if (this.plugin.config.listWorldsToRegen() != null) {
			//Go through world names
			for (String worldName : plugin.config.listWorldsToRegen()) {
				if (this.plugin.config.listBlocksToRegen(worldName) != null) {
					this.plugin.log.info("Adding old blocks to regen queue.");
					for (String blockName : plugin.config.listBlocksToRegen(worldName)) {
						//Get XYZ and TypeId
						int x = this.plugin.config.getBlockX(worldName, blockName);
						int y = this.plugin.config.getBlockY(worldName, blockName);
						int z = this.plugin.config.getBlockZ(worldName, blockName);
						int typeId = this.plugin.config.getBlockTypeId(worldName, blockName);
						byte data = this.plugin.config.getBlockData(worldName, blockName);
						String regionName = this.plugin.config.getBlockRegionName(worldName, blockName);
						long respawnTime = this.plugin.config.getBlockRespawnTime(worldName, blockName);
						
						//Get block from the world
						Block block = this.plugin.getServer().getWorld(worldName).getBlockAt(x, y, z);

						//Add block back to the queue
						this.queueBlock(block, Material.getMaterial(typeId), data, regionName, respawnTime);

					}
					
				}
				
			}

		}
		
	}
	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	public void regenOldBlocks() {
		//Make sure there are worlds listen
		if (plugin.config.listWorldsToRegen() != null) {
			//Go through world names
			for (String worldName : plugin.config.listWorldsToRegen()) {
				if (plugin.config.listBlocksToRegen(worldName) != null) {
					plugin.log.info("Restoring Old Regen Blocks.");
					for (String blockName : plugin.config.listBlocksToRegen(worldName)) {
						//Get XYZ and TypeId
						int x = plugin.config.getBlockX(worldName, blockName);
						int y = plugin.config.getBlockY(worldName, blockName);
						int z = plugin.config.getBlockZ(worldName, blockName);
						int typeId = plugin.config.getBlockTypeId(worldName, blockName);
						byte data = plugin.config.getBlockData(worldName, blockName);
						//Get block from the world
						Block block = plugin.getServer().getWorld(worldName).getBlockAt(x, y, z);
						//Set its type to what it should be
						block.setTypeId(typeId);
						block.setData(data);
						//Remove entry from configuration file
						plugin.config.removeBlock(block);
						if (plugin.doDebug) plugin.log.info("Restore[" + block.getWorld().getName()
								+ "][" + block.getX() + "," + block.getY() + "," + block.getZ()
								+ "][" + typeId + "][" + block.getData() + "]");
					}
					
				}
				
			}

		}
		
	}
	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

}
