package net.dmg2.RegenBlock;

public class RegenBlockTBlock {
	private int x;
	private int y;
	private int z;
	private byte data;
	private int typeId;
	private String worldName;
	private long respawnTime;
	private String regionName;
	
	public RegenBlockTBlock(int x, int y, int z, byte data, int typeId, long respawnTime, String worldName, String regionName){
		this.x = x;
		this.y = y;
		this.z = z;
		this.data = data;
		this.typeId = typeId;
		this.respawnTime = respawnTime;
		this.worldName = worldName;
		this.regionName = regionName;
	}
	
	public void setBlock(int x, int y, int z, byte data, int typeId, long respawnTime, String worldName, String regionName) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.data = data;
		this.typeId = typeId;
		this.respawnTime = respawnTime;
		this.worldName = worldName;
		this.regionName = regionName;
	}

	public int getX() { return this.x; }
	public void setX(int x) { this.x = x; }

	public int getY() { return this.y; }
	public void setY(int y) { this.y = y; }

	public int getZ() { return this.z; }
	public void setZ(int z) { this.z = z; }

	public byte getData() { return this.data; }
	public void setData(byte data) { this.data = data; }

	public int getTypeId() { return this.typeId; }
	public void setTypeId(int typeId) { this.typeId = typeId; }

	public String getWorldName() { return this.worldName; }
	public void setWorldName(String worldName) { this.worldName = worldName; }

	public String getRegionName() { return this.regionName; }
	public void setRegionName(String regionName) { this.regionName = regionName; }

	public long getRespawnTime() { return this.respawnTime; }
	public void setRespawnTime(long respawnTime) { this.respawnTime = respawnTime; }

}
