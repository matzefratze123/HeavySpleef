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
package me.matzefratze123.heavyspleef.listener;

import me.matzefratze123.heavyspleef.HeavySpleef;
import me.matzefratze123.heavyspleef.utility.Permissions;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class UpdateListener implements Listener {

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player player = e.getPlayer();
		
		if(!player.hasPermission(Permissions.CREATE_GAME.getPerm()))
			return;
		if (!HeavySpleef.updateAvaible)
			return;
		
		player.sendMessage(HeavySpleef.PREFIX + ChatColor.DARK_PURPLE + " Your version is outdated! " + ChatColor.GOLD + HeavySpleef.updateName + ChatColor.DARK_PURPLE + " (" + HeavySpleef.updateSize + " bytes)");
		player.sendMessage(HeavySpleef.PREFIX + ChatColor.DARK_PURPLE + " If you want to download a new version of HeavySpleef type /spleef update");
		player.sendMessage(HeavySpleef.PREFIX + ChatColor.DARK_PURPLE + " You may have to" + ChatColor.UNDERLINE + " delete " + ChatColor.RESET + ChatColor.DARK_PURPLE + "your config.yml for a new one!");
	}

	
}