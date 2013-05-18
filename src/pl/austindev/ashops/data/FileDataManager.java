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
package pl.austindev.ashops.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import pl.austindev.ashops.AShops;
import pl.austindev.ashops.shops.Offer;
import pl.austindev.ashops.shops.Owner;
import pl.austindev.ashops.shops.PlayerShopOffer;
import pl.austindev.ashops.shops.ServerShopOffer;
import pl.austindev.ashops.shops.Shop;

public class FileDataManager extends DataManager {
	private static final String SERVER_SHOPS_FILE_NAME = "server-shops"
			+ ".dat";
	private final ConcurrentMap<String, Integer> shopCounter = new ConcurrentHashMap<String, Integer>();

	public FileDataManager(AShops plugin) {
		super(plugin);
		File shopsFolder = getShopsFolder();
		if (!shopsFolder.exists())
			shopsFolder.mkdir();
	}

	@Override
	public LoadResult start() throws DataAccessException {
		Set<Owner> owners = getOwners();
		synchronized (shopCounter) {
			for (Owner owner : owners)
				shopCounter.put(owner.getName(), owner.getShops().size());
		}
		return new LoadResult(owners, getServerShops());
	}

	@Override
	public Owner getOwner(String ownerName) throws DataAccessException {
		return readOwner(ownerName);
	}

	@Override
	public int countShops(String ownerName) {
		synchronized (shopCounter) {
			Integer counter = shopCounter.get(ownerName);
			return counter != null ? counter : 0;
		}
	}

	@Override
	public void addPlayerShop(final Location location, final String ownerName) {
		schedule(new Runnable() {

			@Override
			public void run() {
				try {
					Owner owner = readOwner(ownerName);
					owner.addShop(location);
					saveOwner(owner);
					incrementCounter(ownerName);
				} catch (DataAccessException e) {
					e.printStackTrace();
				}

			}
		});
	}

	@Override
	public void addServerShop(final Location location) {
		schedule(new Runnable() {

			@Override
			public void run() {
				try {
					Map<Location, Shop> serverShops = readServerShops();
					serverShops.put(location, new Shop(location));
					saveSeverShops(serverShops);
				} catch (DataAccessException e) {
					e.printStackTrace();
				}

			}
		});
	}

	@Override
	public void removePlayerShop(final Location location, final String ownerName) {
		schedule(new Runnable() {

			@Override
			public void run() {
				try {
					Owner owner = readOwner(ownerName);
					owner.removeShop(location);
					saveOwner(owner);
					decrementCounter(ownerName);
				} catch (DataAccessException e) {
					e.printStackTrace();
				}

			}
		});
	}

	@Override
	public void removeServerShop(final Location location) {
		schedule(new Runnable() {

			@Override
			public void run() {
				try {
					Map<Location, Shop> serverShops = readServerShops();
					serverShops.remove(location);
					saveSeverShops(serverShops);
				} catch (DataAccessException e) {
					e.printStackTrace();
				}

			}
		});
	}

	@Override
	public void addOffer(final Location location, final Offer offer) {
		schedule(new Runnable() {

			@Override
			public void run() {
				try {
					if (offer instanceof PlayerShopOffer) {
						String ownerName = ((PlayerShopOffer) offer)
								.getOwnerName();
						Owner owner = readOwner(ownerName);
						Map<Location, Shop> shops = owner.getShops();
						if (!shops.containsKey(location))
							shops.put(location, new Shop(location));
						shops.get(location).addOffer(offer.getSlot(), offer);
						saveOwner(owner);
					} else if (offer instanceof ServerShopOffer) {
						Map<Location, Shop> serverShops = readServerShops();
						if (!serverShops.containsKey(location))
							serverShops.put(location, new Shop(location));
						serverShops.get(location).addOffer(offer.getSlot(),
								offer);
						saveSeverShops(serverShops);
					}
				} catch (DataAccessException e) {
					e.printStackTrace();
				}
			}
		});
	}

	public void addOffers(final Location location, final Set<Offer> offers) {
		if (offers.size() > 0)
			schedule(new Runnable() {

				@Override
				public void run() {
					try {
						Offer offer = offers.iterator().next();
						if (offer instanceof PlayerShopOffer) {
							String ownerName = ((PlayerShopOffer) offer)
									.getOwnerName();
							Owner owner = readOwner(ownerName);
							Map<Location, Shop> shops = owner.getShops();
							if (!shops.containsKey(location))
								shops.put(location, new Shop(location));
							Shop shop = shops.get(location);
							for (Offer o : offers)
								shop.addOffer(o.getSlot(), o);
							saveOwner(owner);
						} else if (offer instanceof ServerShopOffer) {
							Map<Location, Shop> serverShops = readServerShops();
							if (!serverShops.containsKey(location))
								serverShops.put(location, new Shop(location));
							Shop shop = serverShops.get(location);
							for (Offer o : offers)
								shop.addOffer(o.getSlot(), o);
							saveSeverShops(serverShops);
						}
					} catch (DataAccessException e) {
						e.printStackTrace();
					}
				}
			});
	}

	@Override
	public void removeOffer(final Location location, final Offer offer) {
		schedule(new Runnable() {

			@Override
			public void run() {
				try {
					if (offer instanceof PlayerShopOffer) {
						String ownerName = ((PlayerShopOffer) offer)
								.getOwnerName();
						Owner owner = readOwner(ownerName);
						Map<Location, Shop> shops = owner.getShops();
						if (shops.containsKey(location)) {
							shops.get(location).removeOffer(offer.getSlot());
							saveOwner(owner);
						}
					} else if (offer instanceof ServerShopOffer) {
						Map<Location, Shop> serverShops = readServerShops();
						if (serverShops.containsKey(location)) {
							serverShops.get(location).removeOffer(
									offer.getSlot());
							saveSeverShops(serverShops);
						}
					}
				} catch (DataAccessException e) {
					e.printStackTrace();
				}
			}
		});
	}

	@Override
	public void updateOffers(final Shop shop) {
		schedule(new Runnable() {

			@Override
			public void run() {
				Map<Integer, Offer> offers = shop.getOffers();
				Offer testOffer = offers.values().iterator().next();
				try {
					if (testOffer instanceof PlayerShopOffer) {
						String ownerName = ((PlayerShopOffer) testOffer)
								.getOwnerName();
						Owner owner = readOwner(ownerName);
						owner.getShops().put(shop.getLocation(), shop);
						saveOwner(owner);
					} else {
						Map<Location, Shop> serverShops = readServerShops();
						serverShops.put(shop.getLocation(), shop);
						saveSeverShops(serverShops);
					}
				} catch (DataAccessException e) {
					e.printStackTrace();
				}

			}
		});
	}

	@Override
	public void synchUpdateOffers(final Shop shop) {
		Map<Integer, Offer> offers = shop.getOffers();
		Offer testOffer = offers.values().iterator().next();
		try {
			if (testOffer instanceof PlayerShopOffer) {
				String ownerName = ((PlayerShopOffer) testOffer).getOwnerName();
				Owner owner = readOwner(ownerName);
				owner.getShops().put(shop.getLocation(), shop);
				saveOwner(owner);
			} else {
				Map<Location, Shop> serverShops = readServerShops();
				serverShops.put(shop.getLocation(), shop);
				saveSeverShops(serverShops);
			}
		} catch (DataAccessException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Set<Owner> getOwners() throws DataAccessException {
		Set<Owner> owners = new HashSet<Owner>();
		for (String ownerName : getOwnerFiles()) {
			Owner owner = readOwner(ownerName);
			owners.add(owner);
		}
		return owners;
	}

	@Override
	public Set<Shop> getServerShops() throws DataAccessException {
		return new HashSet<Shop>(readServerShops().values());
	}

	@Override
	public void close() {
		// Nothing to do.
	}

	@Override
	public void clearPlayerShops(String ownerName) {
		File file = getFile(ownerName);
		if (file.exists())
			file.delete();
		shopCounter.remove(ownerName);
	}

	@Override
	public void clearPlayerShops() {
		for (File file : getShopsFolder().listFiles(OwnersFilesFilter.INSTANCE))
			file.delete();
		shopCounter.clear();
	}

	@Override
	public void clearServerShops() {
		getServerShopsFile().delete();
	}

	@SuppressWarnings("unchecked")
	@Override
	public LoadResult loadFromFile() throws DataAccessException {
		Set<Owner> owners = new HashSet<Owner>();
		Map<Location, Shop> serverShops = new HashMap<Location, Shop>();
		try {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(
					getShopsFile()));
			try {
				owners = (Set<Owner>) in.readObject();
				for (Shop shop : ((Set<Shop>) in.readObject()))
					serverShops.put(shop.getLocation(), shop);
			} finally {
				in.close();
			}
		} catch (IOException e) {
			throw new DataAccessException(e);
		} catch (ClassNotFoundException e) {
			throw new DataAccessException(e);
		}
		for (Owner owner : owners) {
			saveOwner(owner);
		}
		saveSeverShops(serverShops);
		synchronized (shopCounter) {
			shopCounter.clear();
			for (Owner owner : owners)
				shopCounter.put(owner.getName(), owner.getShops().size());
		}
		return new LoadResult(owners, new HashSet<Shop>(serverShops.values()));
	}

	private void schedule(Runnable runnable) {
		Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), runnable);
	}

	private Owner readOwner(String ownerName) throws DataAccessException {
		File ownerFile = getFile(ownerName);
		if (ownerFile.exists()) {
			try {
				ObjectInputStream fis = new ObjectInputStream(
						new FileInputStream(ownerFile));
				try {
					return (Owner) fis.readObject();
				} finally {
					fis.close();
				}
			} catch (IOException e) {
				throw new DataAccessException(e);
			} catch (ClassNotFoundException e) {
				throw new DataAccessException(e);
			}
		}
		return new Owner(ownerName);
	}

	@SuppressWarnings("unchecked")
	private Map<Location, Shop> readServerShops() throws DataAccessException {
		File serverShopsFile = getServerShopsFile();
		Map<Location, Shop> serverShops = new HashMap<Location, Shop>();
		if (serverShopsFile.exists()) {
			try {
				ObjectInputStream fis = new ObjectInputStream(
						new FileInputStream(serverShopsFile));
				try {
					for (Shop shop : (HashSet<Shop>) fis.readObject())
						serverShops.put(shop.getLocation(), shop);
					return serverShops;
				} finally {
					fis.close();
				}
			} catch (IOException e) {
				throw new DataAccessException(e);
			} catch (ClassNotFoundException e) {
				throw new DataAccessException(e);
			}
		}
		return serverShops;
	}

	private void saveOwner(Owner owner) throws DataAccessException {
		File ownerFile = getFile(owner.getName());
		if (owner.getShops().size() > 0) {
			try {
				ObjectOutputStream fis = new ObjectOutputStream(
						new FileOutputStream(getFile(owner.getName())));
				try {
					fis.writeObject(owner);
				} finally {
					fis.close();
				}
			} catch (IOException e) {
				throw new DataAccessException(e);
			}
		} else {
			ownerFile.delete();
		}
	}

	private void saveSeverShops(Map<Location, Shop> serverShops)
			throws DataAccessException {
		File serverShopsFile = getServerShopsFile();
		if (serverShops.size() > 0) {
			try {
				ObjectOutputStream fis = new ObjectOutputStream(
						new FileOutputStream(serverShopsFile));
				try {
					fis.writeObject(new HashSet<Shop>(serverShops.values()));
				} finally {
					fis.close();
				}
			} catch (IOException e) {
				throw new DataAccessException(e);
			}
		} else {
			serverShopsFile.delete();
		}
	}

	private File getFile(String playerName) {
		return new File(getShopsFolder(), playerName + ".dat");
	}

	private File getServerShopsFile() {
		return new File(getShopsFolder(), SERVER_SHOPS_FILE_NAME);
	}

	private File getShopsFolder() {
		return new File(getPlugin().getDataFolder() + File.separator + "data");
	}

	private Set<String> getOwnerFiles() {
		File shopsFolder = getShopsFolder();
		File[] files = shopsFolder.listFiles(OwnersFilesFilter.INSTANCE);
		Set<String> ownersNames = new HashSet<String>();
		for (File file : files)
			ownersNames.add(file.getName().split("\\.")[0]);
		return ownersNames;
	}

	private enum OwnersFilesFilter implements FilenameFilter {
		INSTANCE;

		@Override
		public boolean accept(File dir, String name) {
			String fileName[] = name.split("\\.");
			return fileName.length == 2 && fileName[1].equalsIgnoreCase("dat")
					&& !name.equals(SERVER_SHOPS_FILE_NAME);
		}

	}

	private void incrementCounter(String ownerName) {
		Integer value = shopCounter.putIfAbsent(ownerName.toLowerCase(), 1);
		if (value != null)
			shopCounter.put(ownerName.toLowerCase(), value + 1);
	}

	private void decrementCounter(String ownerName) {
		Integer value = shopCounter.remove(ownerName.toLowerCase());
		if (value != null && value > 1)
			shopCounter.put(ownerName.toLowerCase(), value - 1);
	}

}
