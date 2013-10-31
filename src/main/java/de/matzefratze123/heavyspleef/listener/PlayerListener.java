/**
 *   HeavySpleef - The simple spleef plugin for bukkit
 *   
 *   Copyright (C) 2013 matzefratze123
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package de.matzefratze123.heavyspleef.listener;

import static de.matzefratze123.heavyspleef.core.flag.FlagType.SHOVELS;

import java.util.ArrayList;
import java.util.List;


import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Egg;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.util.BlockIterator;

import de.matzefratze123.heavyspleef.HeavySpleef;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.GameManager;
import de.matzefratze123.heavyspleef.core.LoseCause;
import de.matzefratze123.heavyspleef.core.QueuesManager;
import de.matzefratze123.heavyspleef.core.flag.FlagType;
import de.matzefratze123.heavyspleef.core.flag.enums.Difficulty;
import de.matzefratze123.heavyspleef.core.region.LoseZone;
import de.matzefratze123.heavyspleef.util.Permissions;
import de.matzefratze123.heavyspleef.util.PlayerStateManager;
import de.matzefratze123.heavyspleef.util.SimpleBlockData;

public class PlayerListener implements Listener {

	private ArrayList<String> isCheckOut = new ArrayList<String>();
	private List<String> dead = new ArrayList<String>();
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		final Player p = e.getPlayer();
		
		Location to = e.getTo();
		
		if (!GameManager.isActive(p)) {
			return;
		}
		
		Game game = GameManager.fromPlayer(p);
		
		if (!game.isCounting() && !game.isIngame()) {
			return;
		}
		
		if (to.getBlock().getType() == Material.WATER || to.getBlock().getType() == Material.LAVA || to.getBlock().getType() == Material.STATIONARY_WATER || to.getBlock().getType() == Material.STATIONARY_LAVA) {
			out(p, game);
			return;
		}
			
		List<LoseZone> loseZones = game.getLoseZones();
		
		for (int i = 0; i < loseZones.size(); i++) {
			LoseZone zone = loseZones.get(i);
			
			if (zone.contains(to)) {
				out(p, game);
				return;
			}
		}
		
	}
	
	private void out(final Player player, Game game) {
		if (isCheckOut.contains(player.getName())) {
			return;
		}
		
		game.leave(player, LoseCause.LOSE);
		isCheckOut.add(player.getName());
		
		Bukkit.getScheduler().scheduleSyncDelayedTask(HeavySpleef.getInstance(), new Runnable() {
			@Override
			public void run() {
				isCheckOut.remove(player.getName());
			}
		}, 20L);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerInteract(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		Block block = e.getClickedBlock();
		
		if (p == null)
			return;
		if (block == null)
			return;
		if (!GameManager.isActive(p))
			return;
		if (e.getAction() != Action.LEFT_CLICK_BLOCK)
			return;
		
		Game game = GameManager.fromPlayer(p);
		if (!game.canSpleef(block, p))
			return;
		
		boolean shovels = game.getFlag(SHOVELS);
		if (shovels)
			return;
		
		if (game.getFlag(FlagType.DIFFICULTY) != Difficulty.MEDIUM)
			return;
		if (game.getFlag(FlagType.BOWSPLEEF))
			return;
		if (game.getFlag(FlagType.SPLEGG))
		
		game.addBrokenBlock(p, block);
		
		if (game.getFlag(FlagType.BLOCKBREAKEFFECT)) {
			block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, block.getType());
		}
		
		block.setType(Material.AIR);
	}
	
	@EventHandler
	public void onSpleggGunClick(PlayerInteractEvent e) {
		Player player = e.getPlayer();
		
		if (player == null)
			return;
		if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		if (!GameManager.isActive(player))
			return;
		
		Game game = GameManager.fromPlayer(player);
		if (!game.isIngame())
			return;
		if (!game.getFlag(FlagType.SPLEGG))
			return;
		if (player.getItemInHand().getType() != Material.DIAMOND_SPADE)
			return;
		
		//Launch egg
		player.launchProjectile(Egg.class);
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {
		Player p = e.getPlayer();
		Block block = e.getBlock();
		
		if (!GameManager.isActive(p)) {
			for (Game game : GameManager.getGames()) {
				if (game.contains(block)) {
					if (p.hasPermission(Permissions.BUILD_BYPASS.getPerm()))
						return;
					if (!HeavySpleef.getSystemConfig().getBoolean("general.protectArena", true))
						return;
					
					e.setCancelled(true);
					fixBlockGlitch(p, block);
					p.sendMessage(Game._("notAllowedToBuild"));
					return;
				}
			}
			return;
		}
		
		
		Game game = GameManager.fromPlayer(p);
		
		if (!game.canSpleef(block, p)) {
			e.setCancelled(true);
			fixBlockGlitch(p, block);
			p.sendMessage(Game._("notAllowedToBuild"));
			return;
		}
		
		if (game.getFlag(FlagType.BOWSPLEEF)) {
			e.setCancelled(true);
			return;
		}
		
		game.addBrokenBlock(p, block);
	}
	
	private void fixBlockGlitch(Player p, Block b) {
		Location bLoc = b.getLocation();
		Location pLoc = p.getLocation();
		
		if (shouldFix(pLoc, bLoc)) {
			if (!SimpleBlockData.isSolid(b.getTypeId()))
				return;
			
			bLoc.setY(bLoc.getY() + 1);
			bLoc.setX(pLoc.getX());
			bLoc.setZ(pLoc.getZ());
			bLoc.setPitch(pLoc.getPitch());
			bLoc.setYaw(pLoc.getYaw());
			
			p.teleport(bLoc);
		}
	}
	
	@EventHandler
	public void onProjectileHit(ProjectileHitEvent e) {
		if (!(e.getEntity() instanceof Arrow) && !(e.getEntity() instanceof Egg))
			return;
		
		Projectile projectile = (Projectile) e.getEntity();
		
		if (!(projectile.getShooter() instanceof Player))
			return;
		
		Player player = (Player)projectile.getShooter();
		if (!GameManager.isActive(player))
			return;
		
		Game game = GameManager.fromPlayer(player);
		
		if (projectile instanceof Arrow) {
			Arrow arrow = (Arrow) projectile;
			
			if (!game.getFlag(FlagType.BOWSPLEEF))
				return;
			
			//Use BlockIterator to detect the hit block
			BlockIterator iterator = new BlockIterator(arrow.getWorld(), arrow.getLocation().toVector(), arrow.getVelocity().normalize(), 0, 4);
		    Block hitBlock = null;
		    
		    while (iterator.hasNext()) {
		    	hitBlock = iterator.next();
		    	
		    	if (hitBlock.getType() != Material.AIR)
		    		break;
		    }
		    
		    if (!game.canSpleef(hitBlock, player))
		    	return;
		    
		    World world = arrow.getWorld();
		    
		    arrow.remove();
		    if (hitBlock.getType() == Material.TNT) {
		    	return;
		    }
		    
		    game.addBrokenBlock(player, hitBlock);
		    FallingBlock block = world.spawnFallingBlock(hitBlock.getLocation(), hitBlock.getType(), hitBlock.getData());
		    block.setMetadata("bowspleef", new FixedMetadataValue(HeavySpleef.getInstance(), true));
		    
		    if (game.getFlag(FlagType.BLOCKBREAKEFFECT)) {
		    	world.playEffect(hitBlock.getLocation(), Effect.STEP_SOUND, hitBlock.getType());
		    }
		    hitBlock.setType(Material.AIR);
		} else if (projectile instanceof Egg) {
			Egg egg = (Egg) projectile;
			
			if (!game.getFlag(FlagType.SPLEGG))
				return;
			
			//Use BlockIterator to detect the hit block
			BlockIterator iterator = new BlockIterator(egg.getWorld(), egg.getLocation().toVector(), egg.getVelocity().normalize(), 0, 4);
		    Block hitBlock = null;
		    
		    while (iterator.hasNext()) {
		    	hitBlock = iterator.next();
		    	
		    	if (hitBlock.getType() != Material.AIR)
		    		break;
		    }
		    
		    if (!game.canSpleef(hitBlock, player))
		    	return;
		    
		    game.addBrokenBlock(player, hitBlock);
		    hitBlock.setType(Material.AIR);
		    egg.remove();
		}
	}
	
	/**
	 * Used to cancel the land of "bowspleefed" falling blocks
	 */
	@EventHandler
	public void onEntityChangeBlock(EntityChangeBlockEvent e) {
		if (!(e.getEntity() instanceof FallingBlock))
			return;
		
		boolean bowspleefEntity = false;
		
		List<MetadataValue> metadatas = e.getEntity().getMetadata("bowspleef");
		for (MetadataValue metadata : metadatas) {
			if (metadata.getOwningPlugin() != HeavySpleef.getInstance())
				continue;
			
			if (metadata.asBoolean()) {
				bowspleefEntity = true;
				break;
			}
		}
		
		if (!bowspleefEntity)
			return;
		
		e.setCancelled(true);
		e.getEntity().remove();
	}
	
	@EventHandler
	public void onFoodChange(FoodLevelChangeEvent e) {
		if (!(e.getEntity() instanceof Player))
			return;
		Player p = (Player)e.getEntity();
		if (GameManager.isActive(p))
			e.setFoodLevel(20);
	}
	
	@EventHandler
	public void onItemPickup(PlayerPickupItemEvent e) {
		Player player = e.getPlayer();
		
		if (!GameManager.isActive(player))
			return;
		
		e.setCancelled(true);
	}
	
	@EventHandler
	public void onEntitySpawn(CreatureSpawnEvent e) {
		for (Game game : GameManager.getGames()) {
			if (game.contains(e.getLocation())) {
				e.setCancelled(true);
				return;
			}
		}
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageEvent e) {
		if (!(e.getEntity() instanceof Player))
			return;
		Player player = (Player)e.getEntity();
		if (GameManager.isActive(player))
			e.setCancelled(true);
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onGamemodeChange(PlayerGameModeChangeEvent e) {
		Player p = e.getPlayer();
		if (!GameManager.isActive(p))
			return;
		if (e.getNewGameMode() == GameMode.SURVIVAL)
			return;
		p.sendMessage(Game._("cantChangeGamemode"));
		e.setCancelled(true);
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent e) {
		for (Game game : GameManager.getGames()) {
			if (!game.contains(e.getBlock()))
				return;
			if (e.getPlayer().hasPermission(Permissions.BUILD_BYPASS.getPerm()))
				return;
			if (!HeavySpleef.getSystemConfig().getBoolean("general.protectArena", true))
				return;
			e.setCancelled(true);
			e.getPlayer().sendMessage(Game._("notAllowedToBuild"));
		}
	}
	
	@EventHandler
	public void onPlayerCommand(PlayerCommandPreprocessEvent e) {
		if (!GameManager.isActive(e.getPlayer()) && !GameManager.isSpectating(e.getPlayer()))
			return;
		if (e.getPlayer().hasPermission(Permissions.COMMAND_WHITELISTED.getPerm()))
			return;
		
		String[] split = e.getMessage().split(" ");
		String cmd = split[0];
		if (cmd.equalsIgnoreCase("/spleef") || cmd.equalsIgnoreCase("/hs") || cmd.equalsIgnoreCase("/hspleef"))
			return;
		List<String> whitelist = HeavySpleef.getSystemConfig().getStringList("general.commandWhitelist");
		for (String c : whitelist) {
			if (c.equalsIgnoreCase(cmd))
				return;
		}
		e.setCancelled(true);
		e.getPlayer().sendMessage(Game._("cantUseCommands"));
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerQuit(PlayerQuitEvent e) {
		handleQuit(e);
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerKick(PlayerKickEvent e) {
		handleQuit(e);
	}
	
	//This event shouldn't be fired because player is in god mode
	//But we don't know if someone types /kill player while he's playing spleef...
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {
		Player player = e.getEntity();
		
		if (GameManager.isActive(player)) {
			
			Game game = GameManager.fromPlayer(player);
			game.leave(player, LoseCause.UNKNOWN);
			
			dead.add(player.getName());
		} else if (GameManager.isSpectating(player)) {
			GameManager.fromPlayer(player).leaveSpectate(player);
		}
	}
	
	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent e) {
		final Player player = e.getPlayer();
		
		if (!dead.contains(player.getName()))
			return;
		
		//Player died while spleefing, restore his inventory
		Bukkit.getScheduler().scheduleSyncDelayedTask(HeavySpleef.getInstance(), new Runnable() {
			
			@Override
			public void run() {
				if (player != null && player.isOnline())
					PlayerStateManager.restorePlayerState(player);
			}
		}, 10L);
		
		dead.remove(player.getName());
	}
	
	@EventHandler
	public void onItemDrop(PlayerDropItemEvent e) {
		if (!GameManager.isActive(e.getPlayer()))
			return;
		
		e.setCancelled(true);
	}
	
	@EventHandler
	public void onEntityExplode(EntityExplodeEvent e) {
		if (!HeavySpleef.getSystemConfig().getBoolean("general.protectArena", true))
			return;
		
		for (Game game : GameManager.getGames()) {
			if (game.contains(e.getLocation())) {
				e.blockList().clear();
				return;
			}
		}
	}
	
	private void handleQuit(PlayerEvent e) {
		QueuesManager.removeFromQueue(e.getPlayer());
		
		if (!GameManager.isActive(e.getPlayer()))
			return;
		
		Game game = GameManager.fromPlayer(e.getPlayer());
		game.leave(e.getPlayer(), LoseCause.QUIT);
	}
	
	private boolean shouldFix(Location pLoc, Location bLoc) {
		return pLoc.getBlockX() == bLoc.getBlockX() && pLoc.getBlockZ() == bLoc.getBlockZ() && pLoc.getY() > bLoc.getY();
	}
	
}