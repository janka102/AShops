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

import java.util.Iterator;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.ItemStack;

import pl.austindev.ashops.AShops;
import pl.austindev.ashops.ShopUtils;
import pl.austindev.ashops.keys.ASConfigurationPath;
import pl.austindev.ashops.keys.ASMessage;
import pl.austindev.ashops.keys.ASPermission;
import pl.austindev.mc.BlockUtils;
import pl.austindev.mc.PlayerUtil;

public class ASBlockListener extends ASListener {

	public ASBlockListener(AShops plugin) {
		super(plugin);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onEntityChangeBlock(EntityChangeBlockEvent event) {
		Block block = event.getBlock();
		if (block != null
				&& !event.getEntity().getType().equals(EntityType.PLAYER)) {
			if (block.getType().equals(Material.CHEST)) {
				event.setCancelled(!canDestroyChest(null, block));
			} else if (block.getType().equals(Material.WALL_SIGN)) {
				event.setCancelled(!canDestroyWallSign(null, block));
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		Block block = event.getBlock();
		Player player = event.getPlayer();
		if (block.getType().equals(Material.CHEST)) {
			event.setCancelled(!canDestroyChest(player, block));
		} else if (block.getType().equals(Material.WALL_SIGN)) {
			event.setCancelled(!canDestroyWallSign(player, block));
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onEntityExplode(EntityExplodeEvent event) {
		Iterator<Block> iterator = event.blockList().iterator();
		while (iterator.hasNext()) {
			Block block = iterator.next();
			if (block.getType().equals(Material.CHEST)) {
				if (!canDestroyChest(null, block))
					iterator.remove();
			} else if (block.getType().equals(Material.WALL_SIGN)) {
				if (!canDestroyWallSign(null, block))
					iterator.remove();
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockIgnate(BlockIgniteEvent event) {
		Block block = event.getBlock();
		if (block.getType().equals(Material.CHEST)) {
			event.setCancelled(!canDestroyChest(null, block));
		} else if (block.getType().equals(Material.WALL_SIGN)) {
			event.setCancelled(!canDestroyWallSign(null, block));
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {
		Block block = event.getBlock();
		if (block.getType().equals(Material.CHEST)) {
			if (ShopUtils.hasShopNeighbours(block)) {
				tell(event.getPlayer(), ASMessage.SHOP_NEIGHBOUR);
				event.setCancelled(true);
				event.getPlayer().updateInventory();
			}
		} else if (block.getType().equals(Material.HOPPER)) {
			if (ShopUtils.hasShopNeighbours(block)) {
				tell(event.getPlayer(), ASMessage.SHOP_NEIGHBOUR);
				event.setCancelled(true);
				event.getPlayer().updateInventory();
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockPistonExtend(BlockPistonExtendEvent event) {
		for (Block block : event.getBlocks()) {
			if (block.getType().equals(Material.CHEST)) {
				event.setCancelled(!canDestroyChest(null, block));
			} else if (block.getType().equals(Material.WALL_SIGN)) {
				event.setCancelled(!canDestroyWallSign(null, block));
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockPistonRetract(BlockPistonRetractEvent event) {
		Block block = event.getRetractLocation().getBlock();
		if (block != null) {
			if (block.getType().equals(Material.CHEST)) {
				event.setCancelled(!canDestroyChest(null, block));
			} else if (block.getType().equals(Material.WALL_SIGN)) {
				event.setCancelled(!canDestroyWallSign(null, block));
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
	public void onSingChange(SignChangeEvent event) {
		Player player = event.getPlayer();
		Block signBlock = event.getBlock();
		if (signBlock.getType().equals(Material.WALL_SIGN)) {
			Sign sign = (Sign) signBlock.getState();
			if (ShopUtils.isTagSign(event)) {
				Block block = BlockUtils.getAttachedBlock(sign);
				if (!applyShopSignLines(event, player, block)) {
					block = signBlock.getRelative(BlockFace.DOWN);
					applyShopSignLines(event, player, block);
				}
			} else if (ShopUtils.isShopSign(event)) {
				event.setCancelled(true);
				event.getBlock().breakNaturally();
			}
		}
	}

	private boolean canDestroyChest(Player player, Block block) {
		Chest chest = (Chest) block.getState();
		Set<Sign> signs = ShopUtils.getAttachedSigns(chest.getLocation());
		if (ShopUtils.hasShopSign(signs)) {
			if (player != null) {
				String ownerName = ShopUtils.getOwner(signs);
				if (PlayerUtil.isValidPlayerName(ownerName)) {
					if (ownerName.equalsIgnoreCase(player.getName())
							|| getPermissions().hasOneOf(player,
									ASPermission.OTHERS_BUY_SHOP,
									ASPermission.OTHERS_SELL_SHOP)
							|| !getPlugin().getConfiguration().getBoolean(
									ASConfigurationPath.PROTECTION)) {
						for (ItemStack item : getShopsManager()
								.removePlayerShop(chest, ownerName)) {
							chest.getWorld().dropItemNaturally(
									chest.getLocation(), item);
						}
						tell(player, ASMessage.REMOVED);
						return true;
					} else {
						tell(player, ASMessage.NOT_OWNER);
						return false;
					}
				} else {
					if (getPermissions().hasOneOf(player,
							ASPermission.SERVER_BUY_SHOP,
							ASPermission.SERVER_SELL_SHOP)) {
						getShopsManager().removeServerShop(chest);
						tell(player, ASMessage.REMOVED);
					} else {
						tell(player, ASMessage.NO_PERMISSION);
						return false;
					}
				}
			} else {
				return false;
			}
		}
		return true;
	}

	public boolean canDestroyWallSign(Player player, Block block) {
		Sign sign = (Sign) block.getState();
		if (ShopUtils.isShopSign(sign)) {
			if (player != null) {
				Block shopBlock = BlockUtils.getAttachedBlock(sign);
				if (shopBlock != null
						&& shopBlock.getType().equals(Material.CHEST)
						&& !BlockUtils.isDoubleChest(shopBlock)) {
					Chest chest = (Chest) shopBlock.getState();
					Set<Sign> signs = ShopUtils.getAttachedSigns(chest
							.getLocation());
					if (ShopUtils.hasShopSign(signs)) {
						return checkShopSignBreak(player, chest, signs);
					}
				} else {
					shopBlock = sign.getBlock().getRelative(BlockFace.DOWN);
					if (shopBlock != null
							&& shopBlock.getType().equals(Material.CHEST)
							&& !BlockUtils.isDoubleChest(shopBlock)) {
						Chest chest = (Chest) shopBlock.getState();
						Set<Sign> signs = ShopUtils.getAttachedSigns(chest
								.getLocation());
						if (ShopUtils.hasShopSign(signs)) {
							return checkShopSignBreak(player, chest, signs);
						}
					}
				}
			} else {
				return false;
			}
		}
		return true;
	}

	private boolean checkShopSignBreak(Player player, Chest chest,
			Set<Sign> signs) {
		if (ShopUtils.getOwner(signs).equalsIgnoreCase(player.getName())
				|| getPermissions().hasOneOf(player,
						ASPermission.OTHERS_BUY_SHOP,
						ASPermission.OTHERS_SELL_SHOP)) {
			if (ShopUtils.countShopSigns(signs) < 2) {
				tell(player, ASMessage.NO_SIGN);
				return false;
			}
		} else {
			tell(player, ASMessage.NO_PERMISSION);
			return false;
		}
		return true;
	}

	private boolean applyShopSignLines(SignChangeEvent event, Player player,
			Block block) {
		if (block != null && block.getType().equals(Material.CHEST)
				&& !BlockUtils.isDoubleChest(block)) {
			Chest chest = (Chest) block.getState();
			Set<Sign> signs = ShopUtils.getAttachedSigns(chest.getLocation());
			if (ShopUtils.hasShopSign(signs)) {
				if (player.getName()
						.equalsIgnoreCase(ShopUtils.getOwner(signs))
						|| getPermissions().hasOneOf(player,
								ASPermission.OTHERS_BUY_SHOP,
								ASPermission.OTHERS_SELL_SHOP))
					ShopUtils.applyLines(signs, event);
				else {
					event.setCancelled(true);
					event.getBlock().breakNaturally();
					tell(player, ASMessage.NOT_OWNER);
				}
				return true;
			}
		}
		return false;
	}
}
