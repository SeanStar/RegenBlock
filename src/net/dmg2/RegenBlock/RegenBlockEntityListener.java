package net.dmg2.RegenBlock;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ExplosionPrimeEvent;

public class RegenBlockEntityListener implements  Listener {

	//============================================================
	private RegenBlock plugin;
	public RegenBlockEntityListener(RegenBlock instance) {
		this.plugin = instance;
	}
	//============================================================
	
	@EventHandler
	public void onExplosionPrime(ExplosionPrimeEvent event){
		if (event.isCancelled()) return; //=======================

		plugin.getServer().broadcastMessage("HI! TNT is set on fire! omg!!");
	}

}
