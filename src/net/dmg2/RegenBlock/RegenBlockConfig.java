package net.dmg2.RegenBlock;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

public class RegenBlockConfig {
	
	private YamlConfiguration config;
	private File configFile;
	private HashMap<String, Object> configDefaultsHash = new HashMap<String, Object>();
	
	public RegenBlockConfig(File configFile) {
		this.config = new YamlConfiguration();
		this.configFile = configFile;
		
		//Some default settings
		this.configDefaultsHash.put("settings.defaultSpawnTime", 5);
		this.configDefaultsHash.put("settings.feedbackString", "This block will be restored to its original state in TIMEs.");
		this.configDefaultsHash.put("settings.defaultSpawnBlocks.1", 80);
		this.configDefaultsHash.put("settings.defaultSpawnBlocks.15", 15);
		this.configDefaultsHash.put("settings.defaultSpawnBlocks.14", 5);
		this.configDefaultsHash.put("settings.enableResourceSaver", false);
		this.configDefaultsHash.put("settings.checkTime", 300);
		this.configDefaultsHash.put("settings.enableLeavesDecayRegen", true);
		this.configDefaultsHash.put("settings.selectionToolID", 268);
		
		//Check if configuration file exists
		if (configFile.exists()){
			//If does, load it
			try { this.config.load(this.configFile); } catch (FileNotFoundException e) {e.printStackTrace();} catch (IOException e) {e.printStackTrace();} catch (InvalidConfigurationException e) {e.printStackTrace();}

			if (this.config.getString("settings.feedbackString") == null) {
				this.config.set("settings.feedbackString", this.configDefaultsHash.get("settings.feedbackString"));
			}
			
			if (this.config.getString("settings.defaultSpawnTime") == null) {
				this.config.set("settings.defaultSpawnTime", this.configDefaultsHash.get("settings.defaultSpawnTime"));
			}
			
			if (this.config.getString("settings.defaultSpawnBlocks") == null) {
				this.config.set("settings.defaultSpawnBlocks.1", this.configDefaultsHash.get("settings.defaultSpawnBlocks.1"));
				this.config.set("settings.defaultSpawnBlocks.15", this.configDefaultsHash.get("settings.defaultSpawnBlocks.15"));
				this.config.set("settings.defaultSpawnBlocks.14", this.configDefaultsHash.get("settings.defaultSpawnBlocks.14"));
			}
			
			if (this.config.getString("settings.enableResourceSaver") == null) {
				this.config.set("settings.enableResourceSaver", this.configDefaultsHash.get("settings.enableResourceSaver"));
			}
			
			if (this.config.getString("settings.checkTime") == null) {
				this.config.set("settings.checkTime", this.configDefaultsHash.get("settings.checkTime"));
			}
			
			if (this.config.getString("settings.enableLeavesDecayRegen") == null) {
				this.config.set("settings.enableLeavesDecayRegen", this.configDefaultsHash.get("settings.enableLeavesDecayRegen"));
			}
			
			if (this.config.getString("settings.selectionToolID") == null) {
				this.config.set("settings.selectionToolID", this.configDefaultsHash.get("settings.selectionToolID"));
			}

			try { this.config.save(this.configFile); } catch (IOException e) { e.printStackTrace(); }
			
		} else {
			//Otherwise create and populate default values
			for (String key : this.configDefaultsHash.keySet()) {
				this.config.set(key, this.configDefaultsHash.get(key));
			}

			try { this.config.save(this.configFile); } catch (IOException e) { e.printStackTrace(); }
		}
		
	}

	//#############################################################################################
	public void save() {
		try {
			this.config.save(this.configFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void load() {
		try {
			this.config.load(this.configFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}
	
	//############################################################################################
	public void setRegion(String regionName, int respawnTime, Location right, Location left) {
		//Record all the properties
		this.config.set("region." + regionName + ".respawnTime", respawnTime);
		this.config.set("region." + regionName + ".left.X", left.getBlockX());
		this.config.set("region." + regionName + ".left.Y", left.getBlockY());
		this.config.set("region." + regionName + ".left.Z", left.getBlockZ());
		this.config.set("region." + regionName + ".right.X", right.getBlockX());
		this.config.set("region." + regionName + ".right.Y", right.getBlockY());
		this.config.set("region." + regionName + ".right.Z", right.getBlockZ());
		this.config.set("region." + regionName + ".world", right.getWorld().getName());
		this.config.set("region." + regionName + ".feedbackID", 0);
		this.config.set("region." + regionName + ".type", 0);
		//Save the configuration file
		this.save();
	}
	
	public void removeRegion(String regionName) {
		this.config.set("region." + regionName, null);
		//Save the configuration file
		this.save();
	}
	
	public void setBlock(Block block, int typeId, byte data, String regionName, long respawnTime) {
		//Save block to configuration in case we crash before restoring
		String blockName = "x" + block.getX() + "y" + block.getY() + "z" + block.getZ();
		String worldName = block.getWorld().getName();
		
		this.config.set("blocksToRegen." + worldName + "." + blockName + ".X", block.getX());
		this.config.set("blocksToRegen." + worldName + "." + blockName + ".Y", block.getY());
		this.config.set("blocksToRegen." + worldName + "." + blockName + ".Z", block.getZ());

		this.config.set("blocksToRegen." + worldName + "." + blockName + ".TypeID", typeId);
		this.config.set("blocksToRegen." + worldName + "." + blockName + ".Data", data);
		
		this.config.set("blocksToRegen." + worldName + "." + blockName + ".RegionName", regionName);
		this.config.set("blocksToRegen." + worldName + "." + blockName + ".RespawnTime", respawnTime);
		
		this.save();
	}

	public void removeBlock(Block block) {
		//Remove block after we have restored it from the configuration file
		String blockName = "x" + block.getX() + "y" + block.getY() + "z" + block.getZ();
		this.config.set("blocksToRegen." + block.getWorld().getName() + "." + blockName, null);
		//Remove world entry is it's empty of blocks
		if (this.config.getString("blocksToRegen." + block.getWorld().getName()) == "{}") {
			this.config.set("blocksToRegen." + block.getWorld().getName(), null);
		}
		this.save();
	}
	//############################################################################################

	//############################################################################################
	public boolean getResourceSaverEnabled() { return this.config.getBoolean("settings.enableResourceSaver"); }
	public long getResourceSaverTime() { return this.config.getLong("settings.checkTime"); }
	public boolean getLeavesDecayRegenEnabled() { return this.config.getBoolean("settings.enableLeavesDecayRegen"); }
	public int getToolID() { return this.config.getInt("settings.selectionToolID"); }
	public int getBlockX(String worldName, String blockName) { return this.config.getInt("blocksToRegen." + worldName + "." + blockName + ".X"); }
	public int getBlockY(String worldName, String blockName) { return this.config.getInt("blocksToRegen." + worldName + "." + blockName + ".Y"); }
	public int getBlockZ(String worldName, String blockName) { return this.config.getInt("blocksToRegen." + worldName + "." + blockName + ".Z"); }
	public int getBlockTypeId(String worldName, String blockName) { return this.config.getInt("blocksToRegen." + worldName + "." + blockName + ".TypeID"); }
	public byte getBlockData(String worldName, String blockName) { return (byte)this.config.getInt("blocksToRegen." + worldName + "." + blockName + ".Data"); }
	public String getBlockRegionName(String worldName, String blockName) { return this.config.getString("blocksToRegen." + worldName + "." + blockName + ".RegionName"); }
	public long getBlockRespawnTime(String worldName, String blockName) { return this.config.getLong("blocksToRegen." + worldName + "." + blockName + ".RespawnTime"); }
	
	public String getBlockToRegen(String worldName, Block block) { return this.config.getString("blocksToRegen." + worldName + "." + "x" + block.getX() + "y" + block.getY() + "z" + block.getZ()); }
	//############################################################################################

	//############################################################################################
	public String getRegionWorldName(String regionName) { return this.config.getString("region." + regionName + ".world"); }
	public String getRegionName(String regionName) { return this.config.getString("region." + regionName); }
	
	public void setRegionRespawnTime(String regionName, int respawnTime) { this.config.set("region." + regionName + ".respawnTime", respawnTime); }
	public int getRegionRespawnTime(String regionName) { return this.config.getInt("region." + regionName + ".respawnTime"); }
	public int getRegionDefaultRespawnTime() { return this.config.getInt("settings.defaultSpawnTime"); }
	
	public int getRegionFeedbackID(String regionName) { return this.config.getInt("region." + regionName + ".feedbackID"); }
	public int setRegionFeedbackID(String regionName, int feedbackId) {
		if (feedbackId < 0 || feedbackId > 2) feedbackId = 0;
		this.config.set("region." + regionName + ".feedbackID", feedbackId);
		this.save();
		return feedbackId;
	}
	
	public String getFeedbackString() { return this.config.getString("settings.feedbackString"); }
	public void setFeedbackString(String feedbackString) { this.config.set("settings.feedbackString", feedbackString); this.save(); }

	public String getRegionLeft(String regionName) {return this.config.getString("region." + regionName + ".left.X") + " " + this.config.getString("region." + regionName + ".left.Y") + " "+ this.config.getString("region." + regionName + ".left.Z"); }
	public int getRegionLeftX(String regionName) { return this.config.getInt("region." + regionName + ".left.X"); }
	public int getRegionLeftY(String regionName) { return this.config.getInt("region." + regionName + ".left.Y"); }
	public int getRegionLeftZ(String regionName) { return this.config.getInt("region." + regionName + ".left.Z"); }
	
	public String getRegionRight(String regionName) {return this.config.getString("region." + regionName + ".right.X") + " " + this.config.getString("region." + regionName + ".right.Y") + " "+ this.config.getString("region." + regionName + ".right.Z"); }
	public int getRegionRightX(String regionName) { return this.config.getInt("region." + regionName + ".right.X"); }
	public int getRegionRightY(String regionName) { return this.config.getInt("region." + regionName + ".right.Y"); }
	public int getRegionRightZ(String regionName) { return this.config.getInt("region." + regionName + ".right.Z"); }

	public Set<String> listRegionBlacklistBlockId(String regionName) { return this.list("region." + regionName + ".blacklist.TypeId"); }

	public void addRegionBlacklistBlockId(String regionName, int id) {
		this.config.set("region." + regionName + ".blacklist.TypeId." + id, id);
		this.save();
	}

	public void removeRegionBlacklistBlockId(String regionName, int id) {
		this.config.set("region." + regionName + ".blacklist.TypeId." + id, null);
		this.save();
	}
	
	public void setRegionType(String regionName, int type) {
		this.config.set("region." + regionName + ".type", type);
		this.save();
	}
	
	public int getRegionType(String regionName) { 
		return this.config.getInt("region." + regionName + ".type", 0);
	}

	public HashMap<Integer, Integer> getRegionSpawnBlocks(String regionName) {
		HashMap<Integer, Integer> spawnBlocks = new HashMap<Integer, Integer>();
		Set<String> spawnBlocksId = this.list("region." + regionName + ".spawnBlocks");
		if (spawnBlocksId != null) {
			for (String spawnBlockId : spawnBlocksId) {
				spawnBlocks.put(Integer.parseInt(spawnBlockId), this.config.getInt("region." + regionName + ".spawnBlocks." + spawnBlockId));
			}
			
		}
		return spawnBlocks;
	}
	
	public void setRegionSpawnBlock(String regionName, int typeId, int chance) {
		this.config.set("region." + regionName + ".spawnBlocks." + typeId, chance);
		this.save();
	}

	public int getRegionSpawnBlock(String regionName, int typeId) { return this.config.getInt("region." + regionName + ".spawnBlocks." + typeId); }

	public void removeRegionSpawnBlock(String regionName, int typeId) {
		this.config.set("region." + regionName + ".spawnBlocks." + typeId, null);
		this.save();
	}
	
	public void regionAddSpawnBlocks(String regionName) {
		if (this.config.getString("region." + regionName + ".spawnBlocks") == null) {
			Set<String> defaultSpawnBlocks = this.list("settings.defaultSpawnBlocks");
			if (defaultSpawnBlocks != null) {
				for (String typeId : defaultSpawnBlocks) {
					this.setRegionSpawnBlock(regionName, Integer.parseInt(typeId), this.config.getInt("settings.defaultSpawnBlocks." + typeId, 50));
					
				}

			}
		
		}
	
	}
	//############################################################################################

	//############################################################################################
	public Set<String> list(String path) {
		//String elements[] = { "-1337" };
	    //Set<String> set = new HashSet<String>(Arrays.asList(elements));

	    if (this.config.getConfigurationSection(path) != null && this.config.getConfigurationSection(path).getKeys(false) != null) {
		    return this.config.getConfigurationSection(path).getKeys(false);
	    } else {
	    	return null;
	    }

	}

	public Set<String> listRegions() { return this.list("region"); }
	public Set<String> listWorldsToRegen() { return this.list("blocksToRegen"); }
	public Set<String> listBlacklistBlockId() { return this.list("blacklist.TypeId"); } 
	public Set<String> listBlocksToRegen(String worldName) { return this.list("blocksToRegen." + worldName); }

	public void removeBlacklistBlockId(int id) {
		this.config.set("blacklist.TypeId." + id, null);
		this.save();
	}

	public void addBlacklistBlockId(int id) {
		this.config.set("blacklist.TypeId." + id, id);
		this.save();
	}
	//############################################################################################
	
}
