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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import pl.austindev.ashops.data.DataAccessException;
import pl.austindev.ashops.shops.Offer;
import pl.austindev.ashops.shops.PlayerShopBuyOffer;
import pl.austindev.ashops.shops.PlayerShopOffer;
import pl.austindev.ashops.shops.PlayerShopSellOffer;
import pl.austindev.ashops.shops.Shop;
import pl.austindev.mc.BlockUtils;

public class OffersRegister {
	private final AShops plugin;

	private final ConcurrentMap<Location, Future<Shop>> openedShops = new ConcurrentHashMap<Location, Future<Shop>>();
	private final ExecutorService executor = Executors
			.newSingleThreadExecutor();

	public OffersRegister(AShops plugin) {
		this.plugin = plugin;
	}

	public static OffersRegister newInstance(AShops plugin) {
		final OffersRegister register = new OffersRegister(plugin);
		Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
			@Override
			public void run() {
				try {
					register.removeClosed();
				} catch (OfferLoadingException e) {
					e.printStackTrace();
				}
			}
		}, 720000, 720000);
		return register;
	}

	public Future<Shop> load(Chest chest) {
		final Location location = chest.getLocation();
		Future<Shop> shopFuture = openedShops.get(location);
		if (shopFuture == null) {
			final ItemStack[] items = chest.getInventory().getContents();
			Callable<Shop> shopTask = new Callable<Shop>() {
				@Override
				public Shop call() throws Exception {
					return loadOffers(location, items);
				}
			};
			FutureTask<Shop> shopFutureTask = new FutureTask<Shop>(shopTask);
			shopFuture = openedShops.putIfAbsent(location, shopFutureTask);
			if (shopFuture == null) {
				shopFuture = shopFutureTask;
				executor.submit(shopFutureTask);
			}
		}
		return shopFuture;
	}

	public void unload(Chest chest) throws OfferLoadingException {
		Future<Shop> future = openedShops.remove(chest.getLocation());
		if (future != null) {
			try {
				Shop shop = future.get();
				if (shop.isModified())
					plugin.getDataManager().updateOffers(shop);
			} catch (InterruptedException e) {
				throw new OfferLoadingException(e);
			} catch (ExecutionException e) {
				throw new OfferLoadingException(e);
			}
		}

	}

	public Shop getShop(Chest chest) throws OfferLoadingException {
		while (true) {
			Future<Shop> shopFuture = load(chest);
			try {
				return shopFuture.get();
			} catch (CancellationException e) {
				openedShops.remove(chest.getLocation(), shopFuture);
			} catch (InterruptedException e) {
				throw new OfferLoadingException(e, chest.getLocation());
			} catch (ExecutionException e) {
				throw new OfferLoadingException(e, chest.getLocation());
			}
		}
	}

	public void addOffer(Chest chest, int slot, Offer offer)
			throws OfferLoadingException {
		Future<Shop> shopFuture = openedShops.get(chest.getLocation());
		if (shopFuture != null)
			try {
				Shop shop = shopFuture.get();
				shop.addOffer(slot, offer);
				if (offer instanceof PlayerShopOffer) {
					linkOppositeOffers(shop, (PlayerShopOffer) offer);
				}
			} catch (InterruptedException e) {
				throw new OfferLoadingException(e, chest.getLocation());
			} catch (ExecutionException e) {
				throw new OfferLoadingException(e, chest.getLocation());
			}
	}

	public void removeOffer(Chest chest, int slot) throws OfferLoadingException {
		Future<Shop> shopFuture = openedShops.get(chest.getLocation());
		if (shopFuture != null)
			try {
				Shop shop = shopFuture.get();
				Offer offer = shop.removeOffer(slot);
				if (offer != null && offer instanceof PlayerShopSellOffer) {
					PlayerShopSellOffer sellOffer = (PlayerShopSellOffer) offer;
					for (Offer shopOffer : shop.getOffers().values()) {
						if (shopOffer instanceof PlayerShopBuyOffer) {
							PlayerShopBuyOffer buyOffer = (PlayerShopBuyOffer) shopOffer;
							Iterator<PlayerShopSellOffer> iterator = buyOffer
									.getOppositeOffers().iterator();
							while (iterator.hasNext()) {
								PlayerShopSellOffer oppositeOffer = iterator
										.next();
								if (oppositeOffer.equals(sellOffer))
									iterator.remove();
							}
						}
					}
				}
			} catch (InterruptedException e) {
				throw new OfferLoadingException(e, chest.getLocation());
			} catch (ExecutionException e) {
				throw new OfferLoadingException(e, chest.getLocation());
			}
	}

	private Shop loadOffers(Location location, ItemStack[] items) {
		Shop shop = new Shop(location);
		for (int i = 0; i < items.length; i++)
			if (items[i] != null) {
				Offer offer = Offer.getOffer(items[i], i);
				if (offer != null) {
					if (offer instanceof PlayerShopOffer) {
						linkOppositeOffers(shop, (PlayerShopOffer) offer);
					}
					shop.addOffer(i, offer);
				}
			}
		return shop;
	}

	private void linkOppositeOffers(final Shop shop, PlayerShopOffer offer) {
		final Set<Offer> offersToUpdate = new HashSet<Offer>();
		if (offer instanceof PlayerShopBuyOffer) {
			PlayerShopBuyOffer buyOffer = (PlayerShopBuyOffer) offer;
			for (Offer shopOffer : shop.getOffers().values()) {
				if (shopOffer instanceof PlayerShopSellOffer) {
					PlayerShopSellOffer sellOffer = (PlayerShopSellOffer) shopOffer;
					if (sellOffer.getItem().isSimilar(buyOffer.getItem())) {
						buyOffer.getOppositeOffers().add(sellOffer);
						if (transferItems(buyOffer, sellOffer)) {
							offersToUpdate.add(sellOffer);
							offersToUpdate.add(buyOffer);
						}
					}
				}
			}
		} else {
			PlayerShopSellOffer sellOffer = (PlayerShopSellOffer) offer;
			for (Offer shopOffer : shop.getOffers().values()) {
				if (shopOffer instanceof PlayerShopBuyOffer) {
					PlayerShopBuyOffer buyOffer = (PlayerShopBuyOffer) shopOffer;
					if (buyOffer.getItem().isSimilar(sellOffer.getItem())) {
						buyOffer.getOppositeOffers().add(sellOffer);
						if (transferItems(buyOffer, sellOffer)) {
							offersToUpdate.add(sellOffer);
							offersToUpdate.add(buyOffer);
						}
					}
				}
			}
		}
		Bukkit.getScheduler().runTask(plugin, new Runnable() {

			@Override
			public void run() {
				Inventory inventory = ((Chest) shop.getLocation().getBlock()
						.getState()).getInventory();
				for (Offer offer : offersToUpdate)
					offer.updateOfferTag(inventory);
			}
		});
	}

	private boolean transferItems(PlayerShopBuyOffer buyOffer,
			PlayerShopSellOffer sellOffer) {
		if (buyOffer.getAmount() > 0) {
			int leftSpace = sellOffer.getMaxAmount() - sellOffer.getAmount();
			int amount = Math.min(leftSpace, buyOffer.getAmount());
			if (amount > 0) {
				sellOffer.setAmount(sellOffer.getAmount() + amount);
				buyOffer.setAmount(buyOffer.getAmount() - amount);
				return true;
			}
		}
		return false;
	}

	private void removeClosed() throws OfferLoadingException {
		for (Future<Shop> shopFuture : openedShops.values()) {
			if (shopFuture.isDone()) {
				try {
					Shop shop = shopFuture.get();
					Block block = shop.getLocation().getBlock();
					if (block != null && block.getType().equals(Material.CHEST)
							&& !BlockUtils.isDoubleChest(block)) {
						Chest chest = (Chest) block.getState();
						if (chest.getInventory().getViewers().size() == 0)
							openedShops.remove(shop.getLocation(), shopFuture);
					}
				} catch (InterruptedException e) {
					throw new OfferLoadingException(e);
				} catch (ExecutionException e) {
					throw new OfferLoadingException(e);
				}
			}
		}
	}

	public void close() throws OfferLoadingException, DataAccessException {
		for (Future<Shop> future : openedShops.values()) {
			try {
				Shop shop = future.get();
				if (shop.isModified()) {
					plugin.getDataManager().synchUpdateOffers(shop);
				}
			} catch (InterruptedException e) {
				throw new OfferLoadingException(e);
			} catch (ExecutionException e) {
				throw new OfferLoadingException(e);
			}
		}
	}
}
