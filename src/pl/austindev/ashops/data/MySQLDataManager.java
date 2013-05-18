/*
f * AShops Bukkit Plugin
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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.bukkit.Location;

import pl.austindev.ashops.AShops;
import pl.austindev.ashops.keys.ASConfigurationPath;
import pl.austindev.ashops.shops.Offer;
import pl.austindev.ashops.shops.OfferType;
import pl.austindev.ashops.shops.Owner;
import pl.austindev.ashops.shops.PlayerShopBuyOffer;
import pl.austindev.ashops.shops.PlayerShopOffer;
import pl.austindev.ashops.shops.ServerShopBuyOffer;
import pl.austindev.ashops.shops.ServerShopOffer;
import pl.austindev.ashops.shops.Shop;

public class MySQLDataManager extends DataManager {
	private final ConcurrentMap<String, Integer> shopCounter = new ConcurrentHashMap<String, Integer>();
	private final String url;

	public MySQLDataManager(AShops plugin) {
		super(plugin);
		this.url = getUrl();
	}

	@Override
	public LoadResult start() throws DataAccessException {
		Set<Owner> owners = new HashSet<Owner>();
		Set<Shop> serverShop = new HashSet<Shop>();
		try {
			Connection connection = getConnection();
			try {
				Statement statement = connection.createStatement();
				try {
					ShopsDBUtils.createPlayerShopsTable(statement);
					ShopsDBUtils.createServerShopsTable(statement);
					ShopsDBUtils.createPSOffersTable(statement);
					ShopsDBUtils.createSSOffersTable(statement);
					owners.addAll(getOwners(statement));
					serverShop.addAll(getServerShops(statement));
				} finally {
					statement.close();
				}
			} finally {
				connection.close();
			}
		} catch (SQLException e) {
			throw new DataAccessException("Database error.", e);
		}
		for (Owner owner : owners)
			shopCounter.put(owner.getName().toLowerCase(), owner.getShops()
					.size());
		return new LoadResult(owners, serverShop);
	}

	@Override
	public int countShops(String ownerName) {
		Integer counter = shopCounter.get(ownerName.toLowerCase());
		return counter != null ? counter : 0;
	}

	@Override
	public void addPlayerShop(final Location location, final String ownerName) {
		new DBTask() {

			@Override
			protected void run(Statement statement) throws SQLException {
				ShopsDBUtils.removePlayerShop(statement, location);
				ShopsDBUtils.addPlayerShop(statement, location, ownerName);
				incrementCounter(ownerName);
			}

		}.perform(getPlugin(), this);
	}

	@Override
	public void addServerShop(final Location location) {
		new DBTask() {

			@Override
			protected void run(Statement statement) throws SQLException {
				ShopsDBUtils.removePlayerShop(statement, location);
				ShopsDBUtils.addServerShop(statement, location);
			}
		}.perform(getPlugin(), this);
	}

	@Override
	public void removePlayerShop(final Location location, final String ownerName) {
		new DBTask() {

			@Override
			protected void run(Statement statement) throws SQLException {
				ShopsDBUtils.removePlayerShop(statement, location);
				decrementCounter(ownerName);
			}
		}.perform(getPlugin(), this);
	}

	@Override
	public void removeServerShop(final Location location) {
		new DBTask() {

			@Override
			protected void run(Statement statement) throws SQLException {
				ShopsDBUtils.removeServerShop(statement, location);
			}
		}.perform(getPlugin(), this);
	}

	@Override
	public void addOffer(final Location location, final Offer offer) {
		new DBTask() {

			@Override
			protected void run(Statement statement) throws SQLException,
					DataAccessException {
				if (offer instanceof PlayerShopOffer) {
					ShopsDBUtils.addPlayerOffer(statement.getConnection(),
							offer instanceof PlayerShopBuyOffer ? OfferType.BUY
									: OfferType.SELL, (PlayerShopOffer) offer,
							location);
				} else {
					ShopsDBUtils.addServerOffer(statement.getConnection(),
							offer instanceof ServerShopBuyOffer ? OfferType.BUY
									: OfferType.SELL, (ServerShopOffer) offer,
							location);
				}
			}
		}.perform(getPlugin(), this);
	}

	@Override
	public void addOffers(final Location location, final Set<Offer> offers) {
		if (offers.size() > 0)
			new DBTask() {

				@Override
				protected void run(Statement statement) throws SQLException,
						DataAccessException {
					Offer offer = offers.iterator().next();
					if (offer instanceof PlayerShopOffer) {
						for (Offer o : offers)
							ShopsDBUtils
									.addPlayerOffer(
											statement.getConnection(),
											o instanceof PlayerShopBuyOffer ? OfferType.BUY
													: OfferType.SELL,
											(PlayerShopOffer) o, location);
					} else {
						for (Offer o : offers)
							ShopsDBUtils
									.addServerOffer(
											statement.getConnection(),
											o instanceof ServerShopBuyOffer ? OfferType.BUY
													: OfferType.SELL,
											(ServerShopOffer) o, location);
					}
				}
			}.perform(getPlugin(), this);
	}

	@Override
	public void removeOffer(final Location location, final Offer offer) {
		new DBTask() {

			@Override
			protected void run(Statement statement) throws SQLException,
					DataAccessException {
				ShopsDBUtils.removeOffer(statement, location, offer);
			}
		}.perform(getPlugin(), this);
	}

	@Override
	public void updateOffers(final Shop shop) {
		new DBTask() {

			@Override
			protected void run(Statement statement) throws SQLException,
					DataAccessException {
				Set<Offer> offers = new HashSet<Offer>(shop.getOffers()
						.values());
				if (offers.size() > 0) {
					Offer firstOffer = offers.iterator().next();
					boolean isPlayerShop = firstOffer instanceof PlayerShopOffer;
					if (isPlayerShop) {
						int shopId = ShopsDBUtils.getShopId(statement,
								firstOffer, shop);
						if (shopId > -1) {
							for (Offer offer : shop.getOffers().values())
								if (offer.isModified())
									ShopsDBUtils.updateOffer(statement, offer,
											shopId);
						} else {
							throw new DataAccessException(
									"Could not find a shop record for a location: "
											+ shop.getLocation());
						}
					}
				}
			}
		}.perform(getPlugin(), this);

	}

	@Override
	public void synchUpdateOffers(final Shop shop) throws DataAccessException {
		try {
			Connection connection = getConnection();
			try {
				Statement statement = connection.createStatement();
				try {
					Set<Offer> offers = new HashSet<Offer>(shop.getOffers()
							.values());
					if (offers.size() > 0) {
						Offer firstOffer = offers.iterator().next();
						boolean isPlayerShop = firstOffer instanceof PlayerShopOffer;
						if (isPlayerShop) {
							int shopId = ShopsDBUtils.getShopId(statement,
									firstOffer, shop);
							if (shopId > -1) {
								for (Offer offer : shop.getOffers().values())
									if (offer.isModified())
										ShopsDBUtils.updateOffer(statement,
												offer, shopId);
							} else {
								throw new DataAccessException(
										"Could not find a shop record for a location: "
												+ shop.getLocation());
							}
						}
					}
				} finally {
					statement.close();
				}
			} finally {
				connection.close();
			}
		} catch (SQLException e) {
			throw new DataAccessException("DatabaseError.", e);
		}
	}

	@Override
	public Set<Owner> getOwners() throws DataAccessException {
		try {
			Connection connection = getConnection();
			try {
				Statement statement = connection.createStatement();
				try {
					return getOwners(statement);
				} finally {
					statement.close();
				}
			} finally {
				connection.close();
			}
		} catch (SQLException e) {
			throw new DataAccessException("Database error.", e);
		}
	}

	@Override
	public Set<Shop> getServerShops() throws DataAccessException {
		try {
			Connection connection = getConnection();
			try {
				Statement statement = connection.createStatement();
				try {
					return getServerShops(statement);
				} finally {
					statement.close();
				}
			} finally {
				connection.close();
			}
		} catch (SQLException e) {
			throw new DataAccessException("Database error.", e);
		}
	}

	@Override
	public void clearPlayerShops(String ownerName) throws DataAccessException {
		try {
			Connection connection = getConnection();
			try {
				Statement statement = connection.createStatement();
				try {
					ShopsDBUtils.clearPlayerShops(statement, ownerName);
				} finally {
					statement.close();
				}
			} finally {
				connection.close();
			}
		} catch (SQLException e) {
			throw new DataAccessException("Database error.", e);
		}
		shopCounter.remove(ownerName.toLowerCase());
	}

	@Override
	public void clearPlayerShops() throws DataAccessException {
		try {
			Connection connection = getConnection();
			try {
				Statement statement = connection.createStatement();
				try {
					ShopsDBUtils.clearPlayerShops(statement);
					shopCounter.clear();
				} finally {
					statement.close();
				}
			} finally {
				connection.close();
			}
		} catch (SQLException e) {
			throw new DataAccessException("Database error.", e);
		}
	}

	@Override
	public void clearServerShops() throws DataAccessException {
		try {
			Connection connection = getConnection();
			try {
				Statement statement = connection.createStatement();
				try {
					ShopsDBUtils.clearServerShops(statement);
				} finally {
					statement.close();
				}
			} finally {
				connection.close();
			}
		} catch (SQLException e) {
			throw new DataAccessException("Database error.", e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public LoadResult loadFromFile() throws DataAccessException {
		try {
			Set<Owner> owners = new HashSet<Owner>();
			Set<Shop> serverShops = new HashSet<Shop>();
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(
					getShopsFile()));
			try {
				owners.addAll((Set<Owner>) in.readObject());
				serverShops.addAll((Set<Shop>) in.readObject());
				shopCounter.clear();
				Connection connection = getConnection();
				try {
					Statement statement = connection.createStatement();
					try {
						ShopsDBUtils.clearPlayerShops(statement);
						ShopsDBUtils.clearServerShops(statement);
						int idCounter = 1;
						for (Owner owner : owners) {
							idCounter = importShops(
									connection,
									statement,
									new HashSet<Shop>(owner.getShops().values()),
									owner.getName(), idCounter);
							shopCounter.put(owner.getName().toLowerCase(),
									owner.getShops().size());
						}
						importShops(connection, statement, serverShops, null, 1);
						return new LoadResult(owners, serverShops);
					} finally {
						statement.close();
					}
				} finally {
					connection.close();
				}
			} finally {
				in.close();
			}
		} catch (IOException e) {
			throw new DataAccessException("Couldn't read file.", e);
		} catch (ClassNotFoundException e) {
			throw new DataAccessException("Couldn't read file.", e);
		} catch (SQLException e) {
			throw new DataAccessException("Database error.", e);
		}
	}

	@Override
	public Owner getOwner(String ownerName) throws DataAccessException {
		try {
			Connection connection = getConnection();
			try {
				Statement statement = connection.createStatement();
				try {
					Owner owner = new Owner(ownerName);
					Map<Location, Shop> ownerShops = owner.getShops();
					Map<Integer, Shop> shops = ShopsDBUtils.getPlayerShops(
							statement, ownerName);
					for (Map.Entry<Integer, Shop> entry : shops.entrySet()) {
						ShopsDBUtils.insertPlayerOffers(statement,
								entry.getValue(), entry.getKey());
						ownerShops.put(entry.getValue().getLocation(),
								entry.getValue());
					}
					return owner;
				} finally {
					statement.close();
				}
			} finally {
				connection.close();
			}
		} catch (SQLException e) {
			throw new DataAccessException(e);
		}
	}

	@Override
	public void close() {
		// nothing to do.
	}

	public Connection getConnection() throws SQLException {
		return DriverManager.getConnection(url);
	}

	private int importShops(Connection connection, Statement statement,
			Set<Shop> shops, String ownerName, int idCounter)
			throws DataAccessException, SQLException {
		for (Shop shop : shops) {
			Location location = shop.getLocation();
			if (ownerName != null)
				ShopsDBUtils.addPlayerShop(statement, location, ownerName,
						idCounter);
			else
				ShopsDBUtils.addServerShop(statement, location, idCounter);
			for (Offer offer : shop.getOffers().values()) {
				if (offer instanceof PlayerShopOffer) {
					OfferType type = offer instanceof PlayerShopBuyOffer ? OfferType.BUY
							: OfferType.SELL;
					ShopsDBUtils.addPlayerOffer(connection, type,
							(PlayerShopOffer) offer, location);
				} else {
					OfferType type = offer instanceof ServerShopBuyOffer ? OfferType.BUY
							: OfferType.SELL;
					ShopsDBUtils.addServerOffer(connection, type,
							(ServerShopOffer) offer, location);
				}
			}
			idCounter++;
		}
		return idCounter;
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

	private Set<Owner> getOwners(Statement statement) throws SQLException {
		Map<Integer, Shop> shops = ShopsDBUtils.getPlayerShops(statement);
		ShopsDBUtils.insertPlayerOffers(statement, shops);
		Map<String, Owner> owners = new HashMap<String, Owner>();
		for (Shop shop : shops.values()) {
			String ownerName = shop.getOwnerName();
			if (!owners.containsKey(ownerName))
				owners.put(ownerName, new Owner(ownerName));
			owners.get(ownerName).getShops().put(shop.getLocation(), shop);
		}
		return new HashSet<Owner>(owners.values());
	}

	private Set<Shop> getServerShops(Statement statement) throws SQLException {
		Map<Integer, Shop> serverShops = ShopsDBUtils.getServerShops(statement);
		ShopsDBUtils.insertServerOffers(statement, serverShops);
		return new HashSet<Shop>(serverShops.values());
	}

	private String getUrl() {
		String host = getPlugin().getConfiguration().getString(
				ASConfigurationPath.DB_HOST);
		String name = getPlugin().getConfiguration().getString(
				ASConfigurationPath.DB_NAME);
		String user = getPlugin().getConfiguration().getString(
				ASConfigurationPath.DB_USER);
		String password = getPlugin().getConfiguration().getString(
				ASConfigurationPath.DB_PASSWORD);
		return "jdbc:mysql://" + host + "/" + name + "?" + "user=" + user
				+ "&password=" + password;
	}
}