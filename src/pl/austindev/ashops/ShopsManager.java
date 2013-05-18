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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import pl.austindev.ashops.data.DataAccessException;
import pl.austindev.ashops.data.LoadResult;
import pl.austindev.ashops.keys.ASConfigurationPath;
import pl.austindev.ashops.keys.ASMessage;
import pl.austindev.ashops.keys.ASPermission;
import pl.austindev.ashops.shops.Offer;
import pl.austindev.ashops.shops.OfferBuilder;
import pl.austindev.ashops.shops.OfferType;
import pl.austindev.ashops.shops.Owner;
import pl.austindev.ashops.shops.Shop;
import pl.austindev.mc.BlockUtils;
import pl.austindev.mc.ItemUtil;
import pl.austindev.mc.PlayerUtil;

public class ShopsManager {
	private final AShops plugin;
	private final OffersRegister offersRegister;

	private final Set<Integer> excludedItems = new HashSet<Integer>();
	private final Set<String> repairingPlayers = Collections
			.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

	private final Set<String> allowedRegions = Collections
			.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

	public ShopsManager(AShops plugin) throws DataAccessException {
		this.plugin = plugin;
		ShopUtils.setClosedShopMessage(plugin.$(ASMessage.SIGN_LINE_CLOSED));
		loadDescriptionMessages();
		loadConfigProperties();
		loadExcludedItems();
		offersRegister = OffersRegister.newInstance(plugin);
		LoadResult result = plugin.getDataManager().start();
		restoreShops(result.getLoadedOwners(), result.getLoadedServerShops());
	}

	public void loadConfigProperties() {
		ShopUtils.setServerAccountName(plugin.getConfiguration().getString(
				ASConfigurationPath.SERVER_ACCOUNT_NAME));
		ShopUtils.setTaxesAccountName(plugin.getConfiguration().getString(
				ASConfigurationPath.TAXES_ACCOUNT_NAME));
		allowedRegions.clear();
		for (String region : plugin.getConfiguration().getStringList(
				ASConfigurationPath.REGIONS))
			if (region != null && region.length() > 0)
				allowedRegions.add(region);
		excludedItems.clear();
		loadExcludedItems();
	}

	public void loadDescriptionMessages() {
		String sellMsg = plugin.getConfiguration().getString(
				ASConfigurationPath.SELL_DESCRIPTION);
		String buyMsg = plugin.getConfiguration().getString(
				ASConfigurationPath.BUY_DESCRIPTION);
		if (sellMsg == null || sellMsg.length() == 0)
			sellMsg = plugin.$(ASMessage.DEFAULT_SELL_DESCRIPTION);
		else
			sellMsg = ChatColor.translateAlternateColorCodes('&', sellMsg);
		if (buyMsg == null || buyMsg.length() == 0)
			buyMsg = plugin.$(ASMessage.DEFAULT_BUY_DESCRIPTION);
		else
			buyMsg = ChatColor.translateAlternateColorCodes('&', buyMsg);
		ShopUtils.setBuyDescription(buyMsg);
		ShopUtils.setSellDescription(sellMsg);
	}

	public boolean toggleRepairMode(String playerName) {
		if (repairingPlayers.contains(playerName)) {
			repairingPlayers.remove(playerName);
			return false;
		} else {
			repairingPlayers.add(playerName);
			return true;
		}
	}

	public void clearRepairMode(String playerName) {
		repairingPlayers.remove(playerName);
	}

	public boolean isRepairing(String playerName) {
		return repairingPlayers.contains(playerName);
	}

	public void createPlayerShop(Chest chest, String ownerName) {
		Inventory inventory = chest.getInventory();
		BlockUtils.closeForAll(inventory);
		plugin.getDataManager().addPlayerShop(chest.getLocation(), ownerName);
		inventory.clear();
		ShopUtils.setShopSigns(ShopUtils.getAttachedSigns(chest.getLocation()),
				ownerName);
	}

	public void createServerShop(Chest chest) {
		Inventory inventory = chest.getInventory();
		BlockUtils.closeForAll(inventory);
		plugin.getDataManager().addServerShop(chest.getLocation());
		inventory.clear();
		ShopUtils.setShopSigns(ShopUtils.getAttachedSigns(chest.getLocation()),
				ShopUtils.getServerShopOwnerLine());
	}

	public List<ItemStack> removePlayerShop(Chest chest, String ownerName) {
		plugin.getDataManager()
				.removePlayerShop(chest.getLocation(), ownerName);
		return releaseChest(chest);
	}

	public void removeServerShop(Chest chest) {
		releaseChest(chest);
		plugin.getDataManager().removeServerShop(chest.getLocation());
	}

	public void recreatePlayerShop(Chest chest, Set<Offer> offers,
			String ownerName) {
		plugin.getDataManager().addOffers(chest.getLocation(), offers);
		plugin.getDataManager().addPlayerShop(chest.getLocation(), ownerName);
		ShopUtils.setShopSigns(ShopUtils.getAttachedSigns(chest.getLocation()),
				ownerName);
	}

	public void recreateServerShop(Chest chest, Set<Offer> offers) {
		plugin.getDataManager().addOffers(chest.getLocation(), offers);
		plugin.getDataManager().addServerShop(chest.getLocation());
		ShopUtils.setShopSigns(ShopUtils.getAttachedSigns(chest.getLocation()),
				ShopUtils.getServerShopOwnerLine());
	}

	public boolean clearPlayerShops(String ownerName)
			throws DataAccessException {
		Owner owner = plugin.getDataManager().getOwner(ownerName);
		if (owner.getShops().size() > 0) {
			for (Shop shop : owner.getShops().values()) {
				Block block = shop.getLocation().getBlock();
				if (block != null && block.getType().equals(Material.CHEST)
						&& !BlockUtils.isDoubleChest(block)) {
					Chest chest = (Chest) block.getState();
					releaseChest(chest);
				}
			}
			plugin.getDataManager().clearPlayerShops(ownerName);
			return true;
		}
		return false;
	}

	public void clearPlayerShops() throws DataAccessException {
		for (Owner owner : plugin.getDataManager().getOwners()) {
			for (Shop shop : owner.getShops().values()) {
				Block block = shop.getLocation().getBlock();
				if (block != null && block.getType().equals(Material.CHEST)) {
					Chest chest = (Chest) block.getState();
					releaseChest(chest);
				}
			}
		}
		plugin.getDataManager().clearPlayerShops();
	}

	public void clearServerShops() throws DataAccessException {
		for (Shop shop : plugin.getDataManager().getServerShops()) {
			Block block = shop.getLocation().getBlock();
			if (block != null && block.getType().equals(Material.CHEST)) {
				Chest chest = (Chest) block.getState();
				releaseChest(chest);
			}
		}
		plugin.getDataManager().clearServerShops();
	}

	public Offer getOffer(Chest chest, int slot) throws OfferLoadingException {
		return offersRegister.getShop(chest).getOffer(slot);
	}

	public boolean addOffer(Chest chest, OfferBuilder offerBuilder,
			OfferType offerType) throws OfferLoadingException {
		Inventory inventory = chest.getInventory();
		int slot = offerType.equals(OfferType.SELL) ? inventory.firstEmpty()
				: ItemUtil.lastEmpty(inventory);
		if (slot >= 0) {
			boolean isEmpty = ItemUtil.isEmpty(inventory);
			offerBuilder.setSlot(slot);
			Offer offer = offerBuilder.build(offerType);
			offersRegister.addOffer(chest, slot, offer);
			offer.updateOfferTag(inventory);
			plugin.getDataManager().addOffer(chest.getLocation(), offer);
			if (isEmpty
					&& !ShopUtils.isOpen(ShopUtils.getAttachedSigns(chest
							.getLocation())))
				ShopUtils.toggleShopMode(ShopUtils.getAttachedSigns(chest
						.getLocation()));
			return true;
		} else {
			return false;
		}
	}

	public void removeOffer(Chest chest, int slot) throws OfferLoadingException {
		Inventory inventory = chest.getInventory();
		Offer offer = Offer.getOffer(inventory.getItem(slot), slot);
		inventory.setItem(slot, null);
		offersRegister.removeOffer(chest, slot);
		plugin.getDataManager().removeOffer(chest.getLocation(), offer);
		Set<Sign> signs = ShopUtils.getAttachedSigns(chest.getLocation());
		if (ItemUtil.isEmpty(inventory) && ShopUtils.isOpen(signs))
			ShopUtils.toggleShopMode(signs);
	}

	public void loadOffers(Chest chest) {
		offersRegister.load(chest);
	}

	public void unloadOffers(Chest chest) throws OfferLoadingException {
		offersRegister.unload(chest);
	}

	public boolean isShopRegion(Location location) {
		if (plugin.getWorldGuard() != null && allowedRegions.size() > 0) {
			for (String region : ShopUtils.getRegions(plugin.getWorldGuard(),
					location))
				if (allowedRegions.contains(region))
					return true;
			return false;
		} else {
			return true;
		}
	}

	public boolean canHaveMoreShops(Player player) {
		return plugin.getPermissions().has(player, ASPermission.NO_LIMIT)
				|| plugin.getDataManager().countShops(player.getName()) < getGroupLimit(player);
	}

	public int getShopPrice(Player player) {
		return plugin.getPermissions().has(player, ASPermission.FREE) ? 0
				: getGroupShopPrice(player);
	}

	public double getMinimalPrice(Player player, ItemStack item) {
		return plugin.getPermissions().has(player, ASPermission.ANY_PRICE_SELL) ? 0
				: plugin.getConfiguration().getDouble(
						ASConfigurationPath.MINIMAL_PRICE,
						"" + item.getTypeId());
	}

	public int getTax(String playerName, World world) {
		int tax = 0;
		for (String group : plugin.getPermissions()
				.getGroups(playerName, world)) {
			int groupTax = plugin.getConfiguration().getInt(
					ASConfigurationPath.TAXES, group);
			if (groupTax > tax)
				tax = groupTax;
		}
		return tax;
	}

	public boolean canTrade(Player player, ItemStack item) {
		return plugin.getPermissions().has(player, ASPermission.ANY_ITEM_SELL)
				|| !excludedItems.contains(item.getTypeId());
	}

	public void close() throws OfferLoadingException, DataAccessException {
		offersRegister.close();
		for (Owner owner : plugin.getDataManager().getOwners())
			for (Shop shop : owner.getShops().values()) {
				Block block = shop.getLocation().getBlock();
				if (block != null && block.getType().equals(Material.CHEST)
						&& !BlockUtils.isDoubleChest(block)) {
					((Chest) block.getState()).getInventory().clear();
				}
			}
		for (Shop shop : plugin.getDataManager().getServerShops()) {
			Block block = shop.getLocation().getBlock();
			if (block != null && block.getType().equals(Material.CHEST)
					&& !BlockUtils.isDoubleChest(block)) {
				((Chest) block.getState()).getInventory().clear();
			}
		}
	}

	public void save() throws DataAccessException {
		plugin.getDataManager().saveToFile();
	}

	public void load() throws DataAccessException {
		LoadResult result = plugin.getDataManager().loadFromFile();
		restoreShops(result.getLoadedOwners(), result.getLoadedServerShops());
	}

	private void restoreShops(Set<Owner> owners, Set<Shop> serverShops) {
		for (Owner owner : owners) {
			for (Shop shop : owner.getShops().values()) {
				restoreOffers(owner.getName(), shop);
			}
		}
		for (Shop shop : serverShops) {
			restoreOffers(ShopUtils.getServerShopOwnerLine(), shop);
		}
	}

	private void restoreOffers(String ownerName, Shop shop) {
		Block block = shop.getLocation().getBlock();
		boolean isPlayerShop = PlayerUtil.isValidPlayerName(ownerName);
		if (block != null && block.getType().equals(Material.CHEST)
				&& !BlockUtils.isDoubleChest(block)) {
			Chest chest = (Chest) block.getState();
			Set<Sign> signs = ShopUtils.getAttachedSigns(chest.getLocation());
			ShopUtils.setShopSigns(signs, ownerName);
			boolean noSign = false;
			if (!ShopUtils.hasShopSign(signs)) {
				Sign emptySign = ShopUtils.getEmptySign(signs);
				if (emptySign != null)
					ShopUtils.setShopSign(emptySign, ownerName);
				else
					noSign = true;
			}
			if (!noSign) {
				ShopUtils.setShopOwner(signs, ownerName);
				Inventory inventory = chest.getInventory();
				BlockUtils.closeForAll(inventory);
				inventory.clear();
				for (Offer offer : shop.getOffers().values()) {
					offer.updateOfferTag(inventory);
				}
			} else {
				if (isPlayerShop)
					plugin.getDataManager().removePlayerShop(
							block.getLocation(), ownerName);
				else
					plugin.getDataManager().removeServerShop(
							block.getLocation());
			}
		} else {
			if (isPlayerShop)
				plugin.getDataManager().removePlayerShop(block.getLocation(),
						ownerName);
			else
				plugin.getDataManager().removeServerShop(block.getLocation());
		}
	}

	private int getGroupLimit(Player player) {
		int limit = -1;
		int groupLimit;
		for (String group : plugin.getPermissions().getGroups(player)) {
			groupLimit = plugin.getConfiguration().getInt(
					ASConfigurationPath.SHOPS_LIMIT, group);
			if (groupLimit > limit)
				limit = groupLimit;
		}
		return limit >= 0 ? limit : 0;
	}

	private int getGroupShopPrice(Player player) {
		int costs = -1;
		int groupCosts;
		for (String group : plugin.getPermissions().getGroups(player)) {
			groupCosts = plugin.getConfiguration().getInt(
					ASConfigurationPath.SHOP_PRICE, group);
			if (groupCosts > costs)
				costs = groupCosts;
		}
		return costs >= 0 ? costs : 0;
	}

	private List<ItemStack> releaseChest(Chest chest) {
		Inventory inventory = chest.getInventory();
		BlockUtils.closeForAll(inventory);
		List<ItemStack> contents = ShopUtils.getContents(chest);
		inventory.clear();
		ShopUtils
				.clearShopSigns(ShopUtils.getAttachedSigns(chest.getLocation()));
		try {
			offersRegister.unload(chest);
		} catch (OfferLoadingException e) {
			e.printStackTrace();
		}
		return contents;
	}

	private void loadExcludedItems() {
		for (Integer id : plugin.getConfiguration().getIntegerList(
				ASConfigurationPath.EXCLUDED_ITEMS_LIST))
			excludedItems.add(id);
	}
}