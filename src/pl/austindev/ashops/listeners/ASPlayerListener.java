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
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import pl.austindev.ashops.AShops;
import pl.austindev.ashops.OfferLoadingException;
import pl.austindev.ashops.ShopUtils;
import pl.austindev.ashops.keys.ASCommand;
import pl.austindev.ashops.keys.ASMessage;
import pl.austindev.ashops.keys.ASPermission;
import pl.austindev.ashops.shops.Offer;
import pl.austindev.ashops.shops.OfferBuilder;
import pl.austindev.ashops.shops.OfferType;
import pl.austindev.ashops.shops.PlayerShopOffer;
import pl.austindev.mc.BlockUtils;
import pl.austindev.mc.PlayerUtil;
import pl.austindev.mc.TemporaryValues.TemporaryValue;
import pl.austindev.mc.TempsSource;

public class ASPlayerListener extends ASListener {

	public ASPlayerListener(AShops plugin) {
		super(plugin);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
	public void onPlayerQuit(PlayerQuitEvent event) {
		event.getPlayer().closeInventory();
		getShopsManager().clearRepairMode(event.getPlayer().getName());
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		Block block = event.getClickedBlock();
		if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
			onRightClickBlock(event, player, block);
		else if (event.getAction().equals(Action.LEFT_CLICK_BLOCK))
			onLeftClickBlock(event, player, block);
	}

	private void onRightClickBlock(PlayerInteractEvent event, Player player,
			Block block) {
		TemporaryValue temporaryValue = getPlugin().getTemporaryValues().take(
				player.getName());
		if (temporaryValue != null) {
			if (!event.isCancelled()) {
				if (block.getType().equals(Material.CHEST)
						|| !BlockUtils.isDoubleChest(block)) {
					Chest chest = (Chest) block.getState();
					TempsSource source = temporaryValue.getSource();
					if (source.equals(ASCommand.ASHOP))
						tryCreatePlayerShop(player, chest, temporaryValue);
					else if (source.equals(ASCommand.ASSHOP))
						tryCreateServerShop(player, chest, temporaryValue);
					else if (source.equals(ASCommand.AREMOVE))
						tryRemoveServerShop(player, chest, temporaryValue);
					else if (source.equals(ASCommand.ABUY))
						tryAddOfferBuy(player, chest, temporaryValue);
					else if (source.equals(ASCommand.ASELL))
						tryAddOfferSell(player, chest, temporaryValue);
					else if (source.equals(ASCommand.ATOGGLE))
						tryToggleShopMode(player, chest);
				} else {
					tell(player, ASMessage.ABORTED);
				}
				event.setUseInteractedBlock(Result.DENY);
				event.setCancelled(true);
			}
		} else {
			if (block.getType().equals(Material.CHEST)) {
				tryAccessFromChest(event, player, block);
			} else if (block.getType().equals(Material.WALL_SIGN)) {
				tryAccessFromWallSign(event, player, (Sign) block.getState());
			}
		}
	}

	private void onLeftClickBlock(PlayerInteractEvent event, Player player,
			Block block) {
		TemporaryValue temporaryValue = getPlugin().getTemporaryValues().take(
				player.getName());
		if (temporaryValue != null) {
			tell(player, ASMessage.RIGHT_CLICK_EXPECTED);
			event.setCancelled(true);
		} else {
			if (getShopsManager().isRepairing(player.getName())) {
				event.setCancelled(true);
				if (block.getType().equals(Material.CHEST))
					if (!(block.getState() instanceof DoubleChest))
						tryRecreateShop(player, (Chest) block.getState());
					else
						tell(player, ASMessage.DOUBLE_CHEST);
				else
					tell(player, ASMessage.NOT_CHEST);
			}
		}
	}

	private void tryCreatePlayerShop(Player player, Chest chest,
			TemporaryValue temporaryValue) {
		Set<Sign> signs = ShopUtils.getAttachedSigns(chest.getLocation());
		if (!ShopUtils.hasShopSign(signs)) {
			if (!ShopUtils.hasShopNeighbours(chest.getBlock())) {
				if (ShopUtils.hasTagSign(signs)) {
					if (getPermissions().has(player, ASPermission.ANY_REGION)
							|| getShopsManager().isShopRegion(
									chest.getLocation())) {
						Integer shopPrice = temporaryValue.get(Integer.class);
						if (shopPrice != null) {
							if (getEconomy().takeFrom(player,
									shopPrice)) {
								getShopsManager().createPlayerShop(chest,
										player.getName());
								tell(player, ASMessage.CREATED);
							} else {
								tell(player, ASMessage.NO_MONEY, shopPrice);
							}
						} else {
							String ownerName = temporaryValue.get(String.class);
							if (ownerName != null) {
								getShopsManager().createPlayerShop(chest,
										ownerName);
								tell(player, ASMessage.CREATED);
							}
						}
					} else {
						tell(player, ASMessage.NOT_SHOP_REGION);
					}
				} else {
					tell(player, ASMessage.NO_SIGN);
				}
			} else {
				tell(player, ASMessage.SHOP_NEIGHBOUR);
			}
		} else {
			tell(player, ASMessage.ALREADY_SHOP);
		}
	}

	private void tryAccessFromChest(PlayerInteractEvent event, Player player,
			Block block) {
		if (!BlockUtils.isDoubleChest(block)) {
			Chest chest = (Chest) block.getState();
			Set<Sign> signs = ShopUtils.getAttachedSigns(block.getLocation());
			if (ShopUtils.hasShopSign(signs)) {
				if (!player.getGameMode().equals(GameMode.CREATIVE)) {
					boolean isOwner = ShopUtils.getOwner(signs)
							.equalsIgnoreCase(player.getName());
					if (isOwner
							|| getPermissions().hasOneOf(player,
									ASPermission.BUY_FROM_SHOP,
									ASPermission.SELL_TO_SHOP)) {
						if (isOwner
								|| ShopUtils.isOpen(signs)
								|| getPermissions().hasOneOf(player,
										ASPermission.OTHERS_BUY_SHOP,
										ASPermission.OTHERS_SELL_SHOP)) {
							event.setCancelled(false);
							event.setUseInteractedBlock(Result.ALLOW);
							event.setUseItemInHand(Result.DENY);
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
					event.setCancelled(true);
					tell(player, ASMessage.CREATIVE_ACCESS);
				}
			}
		}
	}

	private void tryAccessFromWallSign(PlayerInteractEvent event,
			Player player, Sign sign) {
		if (ShopUtils.isShopSign(sign)) {
			Block shopBlock = BlockUtils.getAttachedBlock(sign);
			if (shopBlock == null
					|| !shopBlock.getType().equals(Material.CHEST))
				shopBlock = sign.getBlock().getRelative(BlockFace.DOWN);
			if (shopBlock != null && shopBlock.getType().equals(Material.CHEST)) {
				if (!BlockUtils.isDoubleChest(shopBlock)) {
					Chest chest = (Chest) shopBlock.getState();
					if (!player.getGameMode().equals(GameMode.CREATIVE)) {
						boolean isOwner = ShopUtils.getOwner(sign)
								.equalsIgnoreCase(player.getName());
						if (isOwner
								|| getPermissions().hasOneOf(player,
										ASPermission.BUY_FROM_SHOP,
										ASPermission.SELL_TO_SHOP)) {
							if (ShopUtils.isOpen(sign)
									|| isOwner
									|| getPermissions().hasOneOf(player,
											ASPermission.OTHERS_BUY_SHOP,
											ASPermission.OTHERS_SELL_SHOP)) {
								event.setUseInteractedBlock(Result.DENY);
								event.setUseItemInHand(Result.DENY);
								event.setCancelled(false);
								player.openInventory(chest.getInventory());
							} else {
								tell(player, ASMessage.SHOP_CLOSED);
								event.setCancelled(true);
							}
						} else {
							tell(player, ASMessage.NO_PERMISSION);
							event.setCancelled(true);
						}
					} else {
						tell(player, ASMessage.CREATIVE_ACCESS);
						event.setCancelled(true);
					}
				}
			}
		}
	}

	private void tryCreateServerShop(Player player, Chest chest,
			TemporaryValue temporaryValue) {
		Set<Sign> signs = ShopUtils.getAttachedSigns(chest.getLocation());
		if (!ShopUtils.hasShopSign(signs)) {
			if (ShopUtils.hasTagSign(signs)) {
				getShopsManager().createServerShop(chest);
				tell(player, ASMessage.CREATED);
			} else {
				tell(player, ASMessage.NO_SIGN);
			}
		} else {
			tell(player, ASMessage.ALREADY_SHOP);
		}
	}

	private void tryRemoveServerShop(Player player, Chest chest,
			TemporaryValue temporaryValue) {
		Set<Sign> signs = ShopUtils.getAttachedSigns(chest.getLocation());
		if (ShopUtils.hasShopSign(signs)) {
			String ownerName = ShopUtils.getOwner(signs);
			if (PlayerUtil.isValidPlayerName(ownerName)) {
				if (ownerName.equalsIgnoreCase(player.getName())
						|| getPermissions().hasOneOf(player,
								ASPermission.OTHERS_BUY_SHOP,
								ASPermission.OTHERS_SELL_SHOP)) {
					for (ItemStack item : getShopsManager().removePlayerShop(
							chest, ownerName))
						chest.getWorld().dropItemNaturally(chest.getLocation(),
								item);
					tell(player, ASMessage.REMOVED);
				} else {
					tell(player, ASMessage.NOT_OWNER);
				}
			} else {
				if (getPermissions().hasOneOf(player,
						ASPermission.SERVER_BUY_SHOP,
						ASPermission.SERVER_SELL_SHOP)) {
					getShopsManager().removeServerShop(chest);
					tell(player, ASMessage.REMOVED);
				} else {
					tell(player, ASMessage.NO_PERMISSION);
				}
			}
		} else {
			tell(player, ASMessage.NOT_SHOP);
		}
	}

	private void tryAddOfferBuy(Player player, Chest chest,
			TemporaryValue temporaryValue) {
		Set<Sign> signs = ShopUtils.getAttachedSigns(chest.getLocation());
		if (ShopUtils.hasShopSign(signs)) {
			String ownerName = ShopUtils.getOwner(signs);
			OfferBuilder offerBuilder = temporaryValue.get(OfferBuilder.class);
			try {
				if (ownerName.equalsIgnoreCase(player.getName())) {
					offerBuilder.setOwnerName(ownerName);
					if (getShopsManager().addOffer(chest, offerBuilder,
							OfferType.BUY)) {
						tell(player, ASMessage.OFFER_ADDED);
					} else {
						tell(player, ASMessage.NO_SLOTS);
					}
				} else if (!PlayerUtil.isValidPlayerName(ownerName)) {
					if (getPermissions().has(player,
							ASPermission.SERVER_BUY_SHOP)) {
						getShopsManager().addOffer(chest, offerBuilder,
								OfferType.BUY);
						tell(player, ASMessage.OFFER_ADDED);
					} else {
						tell(player, ASMessage.NO_PERMISSION);
					}
				} else {
					tell(player, ASMessage.NOT_OWNER);
				}
			} catch (OfferLoadingException e) {
				e.printStackTrace();
				tell(player, ASMessage.ERROR);
			}
		} else {
			tell(player, ASMessage.NOT_SHOP);
		}

	}

	private void tryAddOfferSell(Player player, Chest chest,
			TemporaryValue temporaryValue) {
		Set<Sign> signs = ShopUtils.getAttachedSigns(chest.getLocation());
		if (ShopUtils.hasShopSign(signs)) {
			String ownerName = ShopUtils.getOwner(signs);
			OfferBuilder offerBuilder = temporaryValue.get(OfferBuilder.class);
			try {
				if (ownerName.equalsIgnoreCase(player.getName())) {
					offerBuilder.setOwnerName(ownerName);
					if (getShopsManager().addOffer(chest, offerBuilder,
							OfferType.SELL)) {
						tell(player, ASMessage.OFFER_ADDED);
					} else {
						tell(player, ASMessage.NO_SLOTS);
					}
				} else if (!PlayerUtil.isValidPlayerName(ownerName)) {
					if (getPermissions().has(player,
							ASPermission.SERVER_SELL_SHOP)) {
						getShopsManager().addOffer(chest, offerBuilder,
								OfferType.SELL);
						tell(player, ASMessage.OFFER_ADDED);
					} else {
						tell(player, ASMessage.NO_PERMISSION);
					}
				} else {
					tell(player, ASMessage.NOT_OWNER);
				}
			} catch (OfferLoadingException e) {
				e.printStackTrace();
				tell(player, ASMessage.ERROR);
			}
		} else {
			tell(player, ASMessage.NOT_SHOP);
		}
	}

	private void tryToggleShopMode(Player player, Chest chest) {
		Set<Sign> signs = ShopUtils.getAttachedSigns(chest.getLocation());
		if (ShopUtils.hasShopSign(signs)) {
			String ownerName = ShopUtils.getOwner(signs);
			if (PlayerUtil.isValidPlayerName(ownerName)) {
				if (getPermissions().hasOneOf(player,
						ASPermission.OWN_BUY_SHOP, ASPermission.OWN_SELL_SHOP)) {
					if (ownerName.equalsIgnoreCase(player.getName())) {
						if (ShopUtils.toggleShopMode(signs))
							tell(player, ASMessage.SHOP_ACTIVATED);
						else
							tell(player, ASMessage.SHOP_DEACTIVATED);
					} else {
						tell(player, ASMessage.NOT_OWNER);
					}
				} else {
					tell(player, ASMessage.NOT_OWNER);
				}
			} else {
				if (getPermissions().hasOneOf(player,
						ASPermission.SERVER_BUY_SHOP,
						ASPermission.SERVER_SELL_SHOP)) {
					if (ShopUtils.toggleShopMode(signs))
						tell(player, ASMessage.SHOP_ACTIVATED);
					else
						tell(player, ASMessage.SHOP_DEACTIVATED);
				} else {
					tell(player, ASMessage.NO_PERMISSION);
				}
			}
		} else {
			tell(player, ASMessage.NOT_SHOP);
		}
	}

	private void tryRecreateShop(Player player, Chest chest) {
		Set<Sign> signs = ShopUtils.getAttachedSigns(chest.getLocation());
		if (ShopUtils.hasShopSign(signs) || ShopUtils.hasTagSign(signs)) {
			Inventory inventory = chest.getInventory();
			String ownerName = null;
			boolean isServerShop = false;
			boolean firstItemFound = false;
			if (ShopUtils.hasShopSign(signs)) {
				ownerName = ShopUtils.getOwner(signs);
				if (ShopUtils.getServerShopOwnerLine().equals(ownerName))
					isServerShop = true;
				firstItemFound = true;
			}
			try {
				for (int i = 0; i < inventory.getSize(); i++) {
					ItemStack item = inventory.getItem(i);
					if (item != null && item.getTypeId() > 0) {
						firstItemFound = true;
						Offer offer = Offer.getOffer(item, i);
						if (offer instanceof PlayerShopOffer) {
							String itemOwner = ((PlayerShopOffer) offer)
									.getOwnerName();
							if (isServerShop) {
								tell(player, ASMessage.REPAIR_FAILURE);
								return;
							}
							if (ownerName != null) {
								if (!itemOwner.equalsIgnoreCase(ownerName)) {
									tell(player, ASMessage.REPAIR_FAILURE);
									return;
								}
							} else {
								ownerName = itemOwner;
							}
						} else {
							if (firstItemFound && !isServerShop) {
								tell(player, ASMessage.REPAIR_FAILURE);
								return;
							}
							isServerShop = true;
						}
					}
				}
			} catch (Exception e) {
				tell(player, ASMessage.REPAIR_FAILURE);
				return;
			}
			if (firstItemFound) {
				if (isServerShop) {
					if (getPermissions().hasOneOf(player,
							ASPermission.SERVER_BUY_SHOP,
							ASPermission.SERVER_SELL_SHOP)) {
						getShopsManager().recreateServerShop(chest);
						tell(player, ASMessage.SHOP_RECREATED);
					} else {
						tell(player, ASMessage.NO_PERMISSION);
					}
				} else {
					if (getPermissions().hasOneOf(player,
							ASPermission.OTHERS_BUY_SHOP,
							ASPermission.OTHERS_SELL_SHOP)) {
						getShopsManager().recreatePlayerShop(chest, ownerName);
						tell(player, ASMessage.SHOP_RECREATED);
					} else {
						tell(player, ASMessage.NO_PERMISSION);
					}
				}
			} else {
				tell(player, ASMessage.REPAIR_FAILURE);
			}
		} else {
			tell(player, ASMessage.NO_SIGN);
		}
	}

}
