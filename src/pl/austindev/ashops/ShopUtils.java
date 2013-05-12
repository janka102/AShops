/*
 * AShops Bukkit Plugin
 * Copyright 2013 Austin Reuter (_austinho)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package pl.austindev.ashops;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;

import pl.austindev.ashops.keys.ASPermission;
import pl.austindev.ashops.shops.Offer;
import pl.austindev.ashops.shops.PlayerShopOffer;
import pl.austindev.mc.BlockUtils;
import pl.austindev.mc.ImprovedOfflinePlayer;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class ShopUtils {
	private static final String SIGN_TITLE = ChatColor.DARK_GRAY.toString()
			+ ChatColor.BOLD + "(AShops)";
	private static final String SERVER_SHOP_LINE = ChatColor.BOLD
			+ "Server Shop";
	private static final String SIGN_TAG_LINE = ":as:";
	private static volatile String CLOSED_SHOP_MESSAGE = ChatColor.DARK_GRAY
			+ "- closed -";
	private static volatile String SERVER_ACCOUNT_NAME = null;
	private static volatile String TAXES_ACCOUNT_NAME = null;

	private static final Map<String, Integer> groupTaxes = new HashMap<String, Integer>();

	private ShopUtils() {
	}

	public static void setServerAccountName(String name) {
		if (name != null && name.length() > 0) {
			SERVER_ACCOUNT_NAME = name;
		}
	}

	public static void setTaxesAccountName(String name) {
		if (name != null && name.length() > 0) {
			TAXES_ACCOUNT_NAME = name;
		}
	}

	public static void setTaxes(String group, int value) {
		groupTaxes.put(group, value);
	}

	public static String getServerAccountName() {
		return SERVER_ACCOUNT_NAME;
	}

	public static String getTaxesAccountname() {
		return TAXES_ACCOUNT_NAME;
	}

	public static void setClosedShopMessage(String closedShopMessage) {
		ShopUtils.CLOSED_SHOP_MESSAGE = ChatColor.DARK_GRAY + closedShopMessage;
	}

	public static void applyLines(Set<Sign> signs, SignChangeEvent event) {
		for (Sign sign : signs)
			if (ShopUtils.isShopSign(sign))
				for (int i = 0; i < 4; i++)
					event.setLine(i, sign.getLine(i));
	}

	public static int countShopSigns(Set<Sign> signs) {
		int counter = 0;
		for (Sign sign : signs)
			if (isShopSign(sign))
				counter++;
		return counter;
	}

	public static String getOwner(Set<Sign> signs) {
		for (Sign sign : signs)
			if (isShopSign(sign))
				return getOwner(sign);
		return null;
	}

	public static boolean isOpen(Set<Sign> signs) {
		for (Sign sign : signs)
			if (isShopSign(sign))
				return isOpen(sign);
		return false; // maybe exception would be better idea.
	}

	public static boolean toggleShopMode(Set<Sign> signs) {
		boolean open = false;
		boolean set = false;
		for (Sign sign : signs)
			if (isShopSign(sign)) {
				String modeLine = sign.getLine(3);
				if (modeLine.length() == 0) {
					sign.setLine(3, CLOSED_SHOP_MESSAGE);
					if (!set) {
						open = false;
						set = true;
					}
				} else {
					sign.setLine(3, "");
					if (!set) {
						open = true;
						set = true;
					}
				}
				sign.update();
			}
		return open;
	}

	public static boolean isOpen(Sign sign) {
		return sign.getLine(3).length() == 0;
	}

	public static String getOwner(Sign sign) {
		return sign.getLine(2);
	}

	public static void setShopSigns(Set<Sign> signs, String ownerName) {
		for (Sign sign : signs)
			if (isTagSign(sign))
				updateSign(sign, ownerName, true);
			else if (isShopSign(sign))
				if (sign.getLine(3).length() > 0)
					updateSign(sign, ownerName, false);
				else
					updateSign(sign, ownerName, true);
	}

	public static void setShopOwner(Set<Sign> signs, String ownerName) {
		for (Sign sign : signs)
			if (isShopSign(sign)) {
				sign.setLine(2, ownerName);
				sign.update();
			}
	}

	public static void clearShopSigns(Set<Sign> signs) {
		for (Sign sign : signs)
			if (isShopSign(sign))
				BlockUtils.setLines(sign, "", "", "", "");
	}

	private static void updateSign(Sign sign, String ownerName, boolean open) {
		BlockUtils.setLines(sign, "", SIGN_TITLE, ownerName, open ? ""
				: CLOSED_SHOP_MESSAGE);
	}

	public static boolean hasShopSign(Set<Sign> signs) {
		for (Sign sign : signs)
			if (isShopSign(sign))
				return true;
		return false;
	}

	public static boolean hasTagSign(Set<Sign> signs) {
		for (Sign sign : signs)
			if (isTagSign(sign))
				return true;
		return false;
	}

	public static boolean isShopSign(Sign sign) {
		return sign.getLine(0).equals("") && sign.getLine(1).equals(SIGN_TITLE)
				&& !sign.getLine(2).equals("");
	}

	public static boolean isShopSign(SignChangeEvent event) {
		return event.getLine(0).equals("")
				&& event.getLine(1).equals(SIGN_TITLE)
				&& !event.getLine(2).equals("");
	}

	public static boolean isTagSign(Sign sign) {
		return sign.getLine(0).equalsIgnoreCase(SIGN_TAG_LINE);
	}

	public static boolean isTagSign(SignChangeEvent event) {
		return event.getLine(0).equalsIgnoreCase(SIGN_TAG_LINE);
	}

	public static Set<Sign> getAttachedSigns(Location location) {
		Set<Sign> signs = new HashSet<Sign>();
		for (BlockFace blockFace : BlockUtils.getHorizontalBlockFaces()) {
			Block neighbour = location.getBlock().getRelative(blockFace);
			if (neighbour != null) {
				if (neighbour.getType().equals(Material.WALL_SIGN)) {
					Sign sign = (Sign) neighbour.getState();
					if (((org.bukkit.material.Sign) sign.getData()).getFacing()
							.equals(blockFace)) {
						signs.add(sign);
					}
				}
			}
		}
		Block upperBlock = location.getBlock().getRelative(BlockFace.UP);
		if (upperBlock != null) {
			if (upperBlock.getType().equals(Material.WALL_SIGN)) {
				Sign sign = (Sign) upperBlock.getState();
				signs.add(sign);
			}
		}
		return signs;
	}

	public static boolean hasShopNeighbours(Block block) {
		Set<Block> neighbours = BlockUtils.getHorizontalNeighbours(block);
		neighbours.addAll(BlockUtils.getHorizontalNeighbours(block
				.getRelative(BlockFace.UP)));
		neighbours.addAll(BlockUtils.getHorizontalNeighbours(block
				.getRelative(BlockFace.DOWN)));
		for (Block neighbour : neighbours) {
			if (neighbour.getType().equals(Material.CHEST)) {
				if (ShopUtils.hasShopSign(ShopUtils.getAttachedSigns(neighbour
						.getLocation()))) {
					return true;
				}
			}
		}
		return false;
	}

	public static Set<ItemStack> getContents(Chest chest) {
		Set<ItemStack> items = new HashSet<ItemStack>();
		for (ItemStack offerTag : chest.getInventory().getContents()) {
			if (offerTag != null && offerTag.getTypeId() > 0) {
				Offer offer = Offer.getOffer(offerTag, 0);
				if (offer instanceof PlayerShopOffer) {
					for (ItemStack stack : ((PlayerShopOffer) offer)
							.takeContents())
						items.add(stack);
				}
			}
		}
		return items;
	}

	public static String getServerShopOwnerLine() {
		return SERVER_SHOP_LINE;
	}

	public static void applyTaxes(AShops plugin, Player player, double value) {
		applyTaxes(plugin, player.getWorld(), player, value);
	}
	
	public static void applyTaxes(AShops plugin, World world, String playerName, double value) {
		Player player = Bukkit.getPlayer(playerName);
		if (player != null) {
			applyTaxes(plugin, player.getWorld(), player, value);
		} else {
			ImprovedOfflinePlayer imprOfflinePlayer = new ImprovedOfflinePlayer(playerName);
			if(!imprOfflinePlayer.exists()) return;
			Player offlinePlayer = (Player) imprOfflinePlayer;
			applyTaxes(plugin, world, offlinePlayer, value);
		}
	}

	public static void applyTaxes(AShops plugin, World world,
			Player player, double value) {
		String playerName = player.getName();
		if (!plugin.getPermissions().has(playerName, world,
				ASPermission.NO_TAXES)) {
			int taxes = 0;
			for (String group : plugin.getPermissions().getGroups(playerName,
					world)) {
				if (groupTaxes.containsKey(group)) {
					int gTaxes = groupTaxes.get(group);
					if (gTaxes > taxes)
						taxes = gTaxes;
				}
			}
			if (taxes > 0) {
				double toTake = value * ((double) taxes / 100);
				if (TAXES_ACCOUNT_NAME != null) {
					plugin.getEconomy().transfer(player,
							TAXES_ACCOUNT_NAME, toTake);
				} else {
					plugin.getEconomy().takeFrom(player, toTake);
				}
			}
		}
	}

	public static Set<String> getRegions(WorldGuardPlugin wg, Location location) {
		Set<String> regions = new HashSet<String>();
		for (ProtectedRegion region : wg.getRegionManager(location.getWorld())
				.getApplicableRegions(location))
			regions.add(region.getId());
		return regions;
	}
}