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
package pl.austindev.ashops.listeners;

import java.util.Set;

import org.bukkit.GameMode;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import pl.austindev.ashops.AShops;
import pl.austindev.ashops.InventoryUtils;
import pl.austindev.ashops.OfferLoadingException;
import pl.austindev.ashops.ShopUtils;
import pl.austindev.ashops.keys.ASMessage;
import pl.austindev.ashops.keys.ASPermission;
import pl.austindev.ashops.shops.Offer;
import pl.austindev.ashops.shops.PlayerShopOffer;
import pl.austindev.ashops.shops.PlayerShopSellOffer;
import pl.austindev.mc.BlockUtils;
import pl.austindev.mc.PlayerUtil;

public class ASInventoryListener extends ASListener {

	public ASInventoryListener(AShops plugin) {
		super(plugin);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
	public void onInvnetoryMoveItem(InventoryMoveItemEvent event) {
		Inventory source = event.getSource();
		if (source.getType().equals(InventoryType.CHEST)) {
			Chest chest = (Chest) source.getHolder();
			Set<Sign> signs = ShopUtils.getAttachedSigns(chest);
			if (ShopUtils.hasShopSign(signs) || ShopUtils.hasTagSign(signs)) {
				event.setCancelled(true);
			}
		} else {
			Inventory destination = event.getDestination();
			if (destination.getType().equals(InventoryType.CHEST)) {
				Chest chest = (Chest) destination.getHolder();
				Set<Sign> signs = ShopUtils.getAttachedSigns(chest);
				if (ShopUtils.hasShopSign(signs) || ShopUtils.hasTagSign(signs)) {
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
	public void onInventoryOpen(InventoryOpenEvent event) {
		Inventory inventory = event.getInventory();
		if (PlayerUtil.isPlayer(event.getPlayer())) {
			Player player = (Player) event.getPlayer();
			if (inventory.getType().equals(InventoryType.CHEST)) {
				Chest chest = (Chest) inventory.getHolder();
				Set<Sign> signs = ShopUtils.getAttachedSigns(chest);
				if (ShopUtils.hasShopSign(signs)) {
					if (!BlockUtils.isDoubleChest(chest)) {
						if (ShopUtils.countShopSigns(signs) > 0
								|| getPermissions().hasOneOf(player,
										ASPermission.OTHERS_BUY_SHOP,
										ASPermission.OTHERS_SELL_SHOP)) {
							if (!player.getGameMode().equals(GameMode.CREATIVE)) {
								boolean isOwner = ShopUtils.getOwner(signs)
										.equalsIgnoreCase(player.getName());
								if (isOwner
										|| getPermissions().hasOneOf(player,
												ASPermission.BUY_FROM_SHOP,
												ASPermission.SELL_TO_SHOP)) {
									if (isOwner
											|| ShopUtils.isOpen(signs)
											|| getPermissions()
													.hasOneOf(
															player,
															ASPermission.OTHERS_BUY_SHOP,
															ASPermission.OTHERS_SELL_SHOP)) {
										event.setCancelled(false);
										getShopsManager().loadOffers(chest);
									} else {
										event.setCancelled(true);
										tell(player, ASMessage.SHOP_CLOSED);
									}
								} else {
									event.setCancelled(true);
									tell(player, ASMessage.NO_PERMISSION);
								}
							} else {
								tell(player, ASMessage.CREATIVE_ACCESS);
								event.setCancelled(true);
							}
						} else {
							tell(player, ASMessage.NO_SIGN);
							event.setCancelled(true);
						}
					} else {
						tell(player, ASMessage.DOUBLE_CHEST_ACCESS);
						event.setCancelled(true);
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onInventoryClose(InventoryCloseEvent event) {
		Inventory inventory = event.getInventory();
		if (PlayerUtil.isPlayer(event.getPlayer())) {
			if (inventory.getType().equals(InventoryType.CHEST)) {
				Chest chest = (Chest) inventory.getHolder();
				if (ShopUtils.hasShopSign(ShopUtils.getAttachedSigns(chest))) {
					if (inventory.getViewers().size() <= 1) {
						try {
							getShopsManager().unloadOffers(chest);
						} catch (OfferLoadingException e) {
							if (PlayerUtil.isPlayer(event.getPlayer()))
								tell((Player) event.getPlayer(),
										ASMessage.ERROR);
							e.printStackTrace();
						}
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onInventoryClickEvent(InventoryClickEvent event) {
		if (event.getCurrentItem() != null
				&& event.getCurrentItem().getTypeId() > 0) {
			Inventory inventory = event.getInventory();
			if (PlayerUtil.isPlayer(event.getWhoClicked())) {
				Player player = (Player) event.getWhoClicked();
				if (inventory.getType().equals(InventoryType.CHEST)) {
					Chest chest = (Chest) inventory.getHolder();
					Set<Sign> signs = ShopUtils.getAttachedSigns(chest);
					if (ShopUtils.hasShopSign(signs)) {
						event.setCancelled(true);
						event.setResult(Event.Result.DENY);
						String ownerName = ShopUtils.getOwner(signs);
						try {
							if (PlayerUtil.isValidPlayerName(ownerName)) {
								if (ownerName
										.equalsIgnoreCase(player.getName())) {
									tryAccessPlayerShopAsOwner(event, chest,
											inventory, player);
								} else {
									tryAcesssPlayerShopAsClient(event, chest,
											inventory, player);
								}
							} else {
								if (getPermissions().has(player,
										ASPermission.SERVER_BUY_SHOP)
										|| getPermissions().has(player,
												ASPermission.SERVER_BUY_SHOP)) {
									tryAccessServerShopAsOperator(event, chest,
											inventory, player);
								} else {
									tryAccessServerShopAsClient(event, chest,
											inventory, player);
								}
							}
						} catch (OfferLoadingException e) {
							e.printStackTrace();
							tell(player, ASMessage.ERROR);
						}
					}
				}
			}
		}
	}

	private void tryAccessPlayerShopAsOwner(InventoryClickEvent event,
			Chest chest, Inventory inventory, Player player)
			throws OfferLoadingException {
		int slot = event.getSlot();
		if (slot == event.getRawSlot()) {
			if (event.isShiftClick()) {
				PlayerShopOffer offer = (PlayerShopOffer) getShopsManager()
						.getOffer(chest, slot);
				if (offer != null) {
					if (offer.getAmount() == 0)
						getShopsManager().removeOffer(chest, slot);
					else
						tell(player, ASMessage.COLLECT_ITEMS);
				}
			} else {
				Offer offer = getShopsManager().getOffer(chest, slot);
				if (offer != null) {
					if (event.isLeftClick()) {
						if (offer instanceof PlayerShopSellOffer)
							assertFailureMessage(
									((PlayerShopSellOffer) offer).load(player,
											inventory), player);
					} else if (event.isRightClick()) {
						if (offer instanceof PlayerShopOffer)
							assertFailureMessage(
									((PlayerShopOffer) offer).collect(player,
											inventory), player);
					}
				}
			}
		} else {
			if (event.isShiftClick()) {
				event.setCancelled(true);
				event.setResult(Event.Result.DENY);
			}
		}
	}

	private void tryAcesssPlayerShopAsClient(InventoryClickEvent event,
			Chest chest, Inventory inventory, Player player)
			throws OfferLoadingException {
		int slot = event.getSlot();
		if (slot == event.getRawSlot()) {
			int amount = InventoryUtils.getClickedAmount(event);
			Offer offer = getShopsManager().getOffer(chest, slot);
			if (offer != null
					&& getPermissions().has(player, offer.getPermission()))
				assertFailureMessage(
						offer.trade(getPlugin(), player, inventory, amount),
						player);
		}

	}

	private void tryAccessServerShopAsOperator(InventoryClickEvent event,
			Chest chest, Inventory inventory, Player player)
			throws OfferLoadingException {
		int slot = event.getSlot();
		if (slot == event.getRawSlot()) {
			if (event.isShiftClick()) {
				getShopsManager().removeOffer(chest, slot);
			} else {
				int amount = InventoryUtils.getClickedAmount(event);
				Offer offer = getShopsManager().getOffer(chest, slot);
				if (offer != null
						&& getPermissions().has(player, offer.getPermission()))
					assertFailureMessage(
							offer.trade(getPlugin(), player, inventory, amount),
							player);
			}
		}

	}

	private void tryAccessServerShopAsClient(InventoryClickEvent event,
			Chest chest, Inventory inventory, Player player)
			throws OfferLoadingException {
		int slot = event.getSlot();
		if (slot == event.getRawSlot()) {
			int amount = InventoryUtils.getClickedAmount(event);
			Offer offer = getShopsManager().getOffer(chest, slot);
			if (offer != null
					&& getPermissions().has(player, offer.getPermission()))
				assertFailureMessage(
						offer.trade(getPlugin(), player, inventory, amount),
						player);
		}
	}

	private void assertFailureMessage(ASMessage failureMessage, Player player) {
		if (failureMessage != null)
			tell(player, failureMessage);
	}
}
