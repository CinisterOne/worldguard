// $Id$
/*
 * WorldGuard
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.worldguard.bukkit.commands;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.bukkit.commands.CommandHandler.CommandHandlingException;
import com.sk89q.worldguard.protection.regionmanager.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.io.IOException;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Michael
 */
public class CommandRegionRemoveMember extends WgCommand {

    public boolean handle(CommandSender sender, String senderName, String command, String[] args, CommandHandler ch, WorldGuardPlugin wg) throws CommandHandlingException {

        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players may use this command");
            return true;
        }
        Player player = (Player) sender;
        if (!wg.hasPermission(player, "/regionclaim") && !wg.hasPermission(player, "/regionmembership")) {
            ch.checkRegionPermission(player, "/regiondefine");
        }
        ch.checkArgs(args, 2, -1, "/region removeowner <id> [owner1 [owner2 [owners...]]]");

        String action = command;
        boolean isOwner = action.equalsIgnoreCase("removeowner");
        RegionManager mgr = wg.getGlobalRegionManager().getRegionManager(player.getWorld().getName());

        String id = args[0].toLowerCase();
        if (!mgr.hasRegion(id)) {
            player.sendMessage(ChatColor.RED + "A region with ID '"
                    + id + "' doesn't exist.");
            return true;
        }

        ProtectedRegion existing = mgr.getRegion(id);

        if (!ch.canUseRegionCommand(player, "/regiondefine")
                && !existing.isOwner(wg.wrapPlayer(player))) {
            player.sendMessage(ChatColor.RED + "You don't own this region.");
            return true;
        }

        if (isOwner) {
            ch.removeFromDomain(existing.getOwners(), args, 1);
        } else {
            ch.removeFromDomain(existing.getMembers(), args, 1);
        }

        try {
            mgr.save();
            player.sendMessage(ChatColor.YELLOW + "Region updated!");
            player.sendMessage(ChatColor.GRAY + "Current owners: "
                    + existing.getOwners().toUserFriendlyString());
            player.sendMessage(ChatColor.GRAY + "Current members: "
                    + existing.getMembers().toUserFriendlyString());
        } catch (IOException e) {
            player.sendMessage(ChatColor.RED + "Region database failed to save: "
                    + e.getMessage());
        }

        return true;
    }
}