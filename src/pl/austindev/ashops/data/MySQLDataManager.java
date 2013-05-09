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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import pl.austindev.ashops.AShops;
import pl.austindev.ashops.shops.Offer;
import pl.austindev.ashops.shops.OfferType;
import pl.austindev.ashops.shops.Owner;
import pl.austindev.ashops.shops.PlayerShopBuyOffer;
import pl.austindev.ashops.shops.PlayerShopOffer;
import pl.austindev.ashops.shops.PlayerShopSellOffer;
import pl.austindev.ashops.shops.ServerShopBuyOffer;
import pl.austindev.ashops.shops.ServerShopOffer;
import pl.austindev.ashops.shops.ServerShopSellOffer;
import pl.austindev.ashops.shops.Shop;
import pl.austindev.mc.SerializationUtils;

public class MySQLDataManager extends DataManager {
	private final SimpleMySQLManager mysqlManager;

	private final Map<String, Integer> shopCounter = new HashMap<String, Integer>();

	private static final String PLAYER_SHOPS_TABLE_NAME = "as_player_shops";
	private static final String SERVER_SHOPS_TABLE_NAME = "as_server_shops";
	private static final String PLAYER_OFFERS_TABLE_NAME = "as_ps_offers";
	private static final String SERVER_OFFERS_TABLE_NAME = "as_ss_offers";

	public MySQLDataManager(AShops plugin) throws DataAccessException {
		super(plugin);
		try {
			mysqlManager = new SimpleMySQLManager(plugin);
		} catch (InstantiationException e) {
			throw new DataAccessException(e);
		} catch (IllegalAccessException e) {
			throw new DataAccessException(e);
		} catch (ClassNotFoundException e) {
			throw new DataAccessException(e);
		} catch (SQLException e) {
			throw new DataAccessException(e);
		}
	}

	@Override
	public LoadResult start() throws DataAccessException {
		Set<Owner> owners = new HashSet<Owner>();
		Set<Shop> serverShops = new HashSet<Shop>();
		try {
			Connection connection = mysqlManager.getConnection();
			try {
				Statement statement = connection.createStatement();
				try {
					createTables(statement);
					owners.addAll(getOwners(statement));
					serverShops.addAll(getServerShops(statement));
				} finally {
					statement.close();
				}
			} finally {
				connection.close();
			}
		} catch (SQLException e) {
			throw new DataAccessException(
					"Could not connect to a database. Check the plugin's config file.",
					e);
		} catch (IOException e) {
			throw new DataAccessException(
					"Could not connect to a database. Check the plugin's config file.",
					e);
		} catch (ClassNotFoundException e) {
			throw new DataAccessException(
					"Could not connect to a database. Check the plugin's config file.",
					e);
		}
		synchronized (shopCounter) {
			for (Owner owner : owners) {
				shopCounter.put(owner.getName(), owner.getShops().size());
			}
		}
		return new LoadResult(owners, serverShops);
	}

	@Override
	public int countShops(String ownerName) {
		synchronized (shopCounter) {
			Integer counter = shopCounter.get(ownerName);
			return counter != null ? counter : 0;
		}
	}

	@Override
	public void addPlayerShop(Location location, String ownerName) {
		mysqlManager.scheduleQuery("INSERT INTO " + PLAYER_SHOPS_TABLE_NAME
				+ " VALUES(NULL, '" + ownerName + "', '"
				+ location.getWorld().getName() + "', " + location.getBlockX()
				+ " ," + location.getBlockY() + " , " + location.getBlockZ()
				+ " );");
		synchronized (shopCounter) {
			Integer counter = shopCounter.get(ownerName);
			shopCounter.put(ownerName,
					counter != null ? shopCounter.get(ownerName) + 1 : 1);
		}
	}

	@Override
	public void addServerShop(Location location) {
		mysqlManager.scheduleQuery("INSERT INTO " + SERVER_SHOPS_TABLE_NAME
				+ " VALUES(NULL, '" + location.getWorld().getName() + "', "
				+ location.getBlockX() + " ," + location.getBlockY() + " , "
				+ location.getBlockZ() + " );");
	}

	@Override
	public void removePlayerShop(Location location, String ownerName) {
		mysqlManager.scheduleQuery(
				"DELETE FROM " + PLAYER_OFFERS_TABLE_NAME
						+ " WHERE ps_id IN (SELECT ps_id FROM "
						+ PLAYER_SHOPS_TABLE_NAME + " WHERE world='"
						+ location.getWorld().getName() + "' && x="
						+ location.getBlockX() + " && y="
						+ location.getBlockY() + " && z="
						+ location.getBlockZ() + ");",
				"DELETE FROM " + PLAYER_SHOPS_TABLE_NAME + " WHERE owner='"
						+ ownerName + "' && world='"
						+ location.getWorld().getName() + "' && x="
						+ location.getBlockX() + " && y="
						+ location.getBlockY() + " && z="
						+ location.getBlockZ() + ";");
		synchronized (shopCounter) {
			Integer counter = shopCounter.get(ownerName);
			if (counter != null) {
				if (counter > 1)
					shopCounter.put(ownerName, counter - 1);
				else
					shopCounter.remove(ownerName);
			}
		}
	}

	@Override
	public void removeServerShop(Location location) {
		mysqlManager.scheduleQuery(
				"DELETE FROM " + SERVER_OFFERS_TABLE_NAME
						+ " WHERE ss_id IN (SELECT ss_id FROM "
						+ SERVER_SHOPS_TABLE_NAME + " WHERE world='"
						+ location.getWorld().getName() + "' && x="
						+ location.getBlockX() + " && y="
						+ location.getBlockY() + " && z="
						+ location.getBlockZ() + ");",
				"DELETE FROM " + SERVER_SHOPS_TABLE_NAME + " WHERE world='"
						+ location.getWorld().getName() + "' && x="
						+ location.getBlockX() + " && y="
						+ location.getBlockY() + " && z="
						+ location.getBlockZ() + ";");
	}

	@Override
	public void addOffer(Location location, Offer offer) {
		if (offer instanceof PlayerShopOffer) {
			addPlayerOffer(offer instanceof PlayerShopBuyOffer ? OfferType.BUY
					: OfferType.SELL, (PlayerShopOffer) offer, location);
		} else {
			addServerOffer(offer instanceof ServerShopBuyOffer ? OfferType.BUY
					: OfferType.SELL, (ServerShopOffer) offer, location);
		}
	}

	@Override
	public void removeOffer(Location location, Offer offer) {
		boolean isPlayerOffer = offer instanceof PlayerShopOffer;
		mysqlManager.scheduleQuery("DELETE FROM "
				+ (isPlayerOffer ? PLAYER_OFFERS_TABLE_NAME
						: SERVER_OFFERS_TABLE_NAME)
				+ " WHERE slot="
				+ offer.getSlot()
				+ " && "
				+ (isPlayerOffer ? "ps_id" : "ss_id")
				+ " IN (SELECT "
				+ (isPlayerOffer ? "ps_id" : "ss_id")
				+ " FROM "
				+ (isPlayerOffer ? PLAYER_SHOPS_TABLE_NAME
						: SERVER_SHOPS_TABLE_NAME) + " WHERE world='"
				+ location.getWorld().getName() + "' && x="
				+ location.getBlockX() + " && y=" + location.getBlockY()
				+ " && z=" + location.getBlockZ() + ")");
	}

	@Override
	public void updateOffers(final Shop shop) {
		Bukkit.getScheduler().runTaskAsynchronously(getPlugin(),
				new Runnable() {

					@Override
					public void run() {
						try {
							Connection connection = mysqlManager
									.getConnection();
							try {
								Statement statement = connection
										.createStatement();
								try {
									Set<Offer> offers = new HashSet<Offer>(shop
											.getOffers().values());
									if (offers.size() > 0) {
										Offer firstOffer = offers.iterator()
												.next();
										boolean isPlayerShop = firstOffer instanceof PlayerShopOffer;
										if (isPlayerShop) {
											int shopId = getShopId(statement,
													firstOffer, shop,
													isPlayerShop);
											if (shopId > -1) {
												for (Offer offer : shop
														.getOffers().values()) {
													if (offer.isModified()) {
														PlayerShopOffer playerOffer = (PlayerShopOffer) offer;
														statement
																.executeUpdate("UPDATE "
																		+ (isPlayerShop ? PLAYER_OFFERS_TABLE_NAME
																				: SERVER_OFFERS_TABLE_NAME)
																		+ " SET amount="
																		+ playerOffer
																				.getAmount()
																		+ " WHERE slot="
																		+ playerOffer
																				.getSlot()
																		+ " && ps_id="
																		+ shopId
																		+ ";");
													}
												}
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
							new DataAccessException(e).printStackTrace();
						}
					}
				});
	}

	@Override
	public Set<Owner> getOwners() throws DataAccessException {
		try {
			Connection connection = mysqlManager.getConnection();
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
			throw new DataAccessException(e);
		} catch (IOException e) {
			throw new DataAccessException(e);
		} catch (ClassNotFoundException e) {
			throw new DataAccessException(e);
		}
	}

	private Set<Owner> getOwners(Statement statement) throws SQLException,
			ClassNotFoundException, IOException {
		Map<Integer, Shop> playerShops = readPlayerShops(statement);
		insertPlayerOffers(statement, playerShops);
		Map<String, Owner> owners = new HashMap<String, Owner>();
		for (Shop shop : playerShops.values()) {
			String ownerName = shop.getOwnerName();
			if (!owners.containsKey(ownerName))
				owners.put(ownerName, new Owner(ownerName));
			owners.get(ownerName).getShops().put(shop.getLocation(), shop);
		}
		return new HashSet<Owner>(owners.values());
	}

	@Override
	public Set<Shop> getServerShops() throws DataAccessException {
		try {
			Connection connection = mysqlManager.getConnection();
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
			throw new DataAccessException(e);
		} catch (IOException e) {
			throw new DataAccessException(e);
		} catch (ClassNotFoundException e) {
			throw new DataAccessException(e);
		}
	}

	private Set<Shop> getServerShops(Statement statement) throws SQLException,
			ClassNotFoundException, IOException {
		Map<Integer, Shop> serverShops = readServerShops(statement);
		insertServerOffers(statement, serverShops);
		return new HashSet<Shop>(serverShops.values());
	}

	@Override
	public void close() {
		// nothing to do.
	}

	@Override
	public void clearPlayerShops(String ownerName) {
		mysqlManager.scheduleQuery("DELETE FROM " + PLAYER_OFFERS_TABLE_NAME
				+ " WHERE ps_id IN(SELECT ps_id FROM "
				+ PLAYER_SHOPS_TABLE_NAME + " WHERE owner='" + ownerName
				+ "');", "DELETE FROM " + PLAYER_SHOPS_TABLE_NAME
				+ " WHERE owner='" + ownerName + "';");
		synchronized (shopCounter) {
			shopCounter.remove(ownerName);
		}
	}

	@Override
	public void clearPlayerShops() {
		mysqlManager.scheduleQuery("TRUNCATE TABLE " + PLAYER_SHOPS_TABLE_NAME
				+ ";", "TRUNCATE TABLE " + PLAYER_OFFERS_TABLE_NAME + ";",
				"ALTER TABLE " + PLAYER_SHOPS_TABLE_NAME
						+ " AUTO_INCREMENT = 0", "ALTER TABLE "
						+ PLAYER_OFFERS_TABLE_NAME + " AUTO_INCREMENT = 0");
		synchronized (shopCounter) {
			shopCounter.clear();
		}
	}

	@Override
	public void clearServerShops() {
		mysqlManager.scheduleQuery("TRUNCATE TABLE " + SERVER_SHOPS_TABLE_NAME
				+ ";", "TRUNCATE TABLE " + SERVER_OFFERS_TABLE_NAME + ";",
				"ALTER TABLE " + SERVER_SHOPS_TABLE_NAME
						+ " AUTO_INCREMENT = 0", "ALTER TABLE "
						+ SERVER_OFFERS_TABLE_NAME + " AUTO_INCREMENT = 0");
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
			} finally {
				in.close();
			}
			synchronized (shopCounter) {
				shopCounter.clear();
			}
			Connection connection = mysqlManager.getConnection();
			try {
				Statement statement = connection.createStatement();
				try {
					statement.executeUpdate("TRUNCATE TABLE "
							+ PLAYER_SHOPS_TABLE_NAME + ";");
					statement.executeUpdate("TRUNCATE TABLE "
							+ PLAYER_OFFERS_TABLE_NAME + ";");
					statement.executeUpdate("TRUNCATE TABLE "
							+ SERVER_SHOPS_TABLE_NAME + ";");
					statement.executeUpdate("TRUNCATE TABLE "
							+ SERVER_OFFERS_TABLE_NAME + ";");
					statement.executeUpdate("ALTER TABLE "
							+ PLAYER_SHOPS_TABLE_NAME + " AUTO_INCREMENT=0");
					statement.executeUpdate("ALTER TABLE "
							+ PLAYER_OFFERS_TABLE_NAME + " AUTO_INCREMENT=0");
					statement.executeUpdate("ALTER TABLE "
							+ SERVER_SHOPS_TABLE_NAME + " AUTO_INCREMENT=0");
					statement.executeUpdate("ALTER TABLE "
							+ SERVER_OFFERS_TABLE_NAME + " AUTO_INCREMENT=0");
					int idCounter = 1;
					for (Owner owner : owners) {
						idCounter = importShops(connection, statement,
								new HashSet<Shop>(owner.getShops().values()),
								owner.getName(), idCounter);
						shopCounter.put(owner.getName(), owner.getShops()
								.size());
					}
					importShops(connection, statement, serverShops, null, 1);
				} finally {
					statement.close();
				}
			} finally {
				connection.close();
			}
			return new LoadResult(owners, serverShops);
		} catch (SQLException e) {
			throw new DataAccessException(e);
		} catch (IOException e) {
			throw new DataAccessException(e);
		} catch (ClassNotFoundException e) {
			throw new DataAccessException(e);
		}
	}

	@Override
	public Owner getOwner(String ownerName) throws DataAccessException {
		Owner owner = new Owner(ownerName);
		try {
			Connection connection = mysqlManager.getConnection();
			try {
				Statement shopStatement = connection.createStatement();
				try {
					Statement offerStatement = connection.createStatement();
					try {
						Map<Location, Shop> shops = owner.getShops();
						ResultSet shopsResult = shopStatement
								.executeQuery("SELECT * FROM "
										+ PLAYER_SHOPS_TABLE_NAME
										+ " WHERE owner='" + ownerName + "';");
						try {
							while (shopsResult.next()) {
								Shop shop = new Shop(new Location(
										Bukkit.getWorld(shopsResult
												.getString("world")),
										shopsResult.getInt("x"),
										shopsResult.getInt("y"),
										shopsResult.getInt("z")));
								ResultSet offersResult = offerStatement
										.executeQuery("SELECT * FROM "
												+ PLAYER_OFFERS_TABLE_NAME
												+ " WHERE ps_id="
												+ shopsResult.getInt("ps_id"));
								try {
									while (offersResult.next()) {
										OfferType type = OfferType
												.valueOf(offersResult
														.getString("type")
														.toUpperCase());
										int slot = offersResult.getInt("slot");
										ItemStack item = SerializationUtils
												.toItemStack(offersResult
														.getBytes("item"));
										int amount = offersResult
												.getInt("amount");
										int maxAmount = offersResult
												.getInt("max_amount");
										double price = (double) offersResult
												.getInt("price") / 100;
										shop.addOffer(
												slot,
												type.equals(OfferType.BUY) ? new PlayerShopBuyOffer(
														item, price, slot,
														amount, maxAmount,
														ownerName)
														: new PlayerShopSellOffer(
																item, price,
																slot, amount,
																maxAmount,
																ownerName));
									}
								} finally {
									offersResult.close();
								}
								shops.put(shop.getLocation(), shop);
							}
						} finally {
							shopsResult.close();
						}
					} finally {
						offerStatement.close();
					}
				} finally {
					shopStatement.close();
				}
			} finally {
				connection.close();
			}
		} catch (SQLException e) {
			throw new DataAccessException(e);
		} catch (IOException e) {
			throw new DataAccessException(e);
		} catch (ClassNotFoundException e) {
			throw new DataAccessException(e);
		}
		return owner;
	}

	private void createTables(Statement statement) throws SQLException {
		statement
				.executeUpdate("CREATE TABLE IF NOT EXISTS "
						+ PLAYER_SHOPS_TABLE_NAME
						+ " (ps_id INT(10) NOT NULL AUTO_INCREMENT PRIMARY KEY, owner CHAR(17) NOT NULL, world CHAR(30), x INT(6), y INT(6), z INT(6));");
		statement
				.executeUpdate("CREATE TABLE IF NOT EXISTS "
						+ SERVER_SHOPS_TABLE_NAME
						+ " (ss_id INT(10) NOT NULL AUTO_INCREMENT PRIMARY KEY, world CHAR(30), x INT(6), y INT(6), z INT(6));");
		statement
				.executeUpdate("CREATE TABLE IF NOT EXISTS "
						+ PLAYER_OFFERS_TABLE_NAME
						+ " (ps_offer_id INT(10) NOT NULL AUTO_INCREMENT PRIMARY KEY, ps_id INT(10) NOT NULL, type ENUM('BUY', 'SELL'), item BLOB NOT NULL, slot INT(2) NOT NULL, amount INT(3) NOT NULL, max_amount INT(3) NOT NULL, price INT(8) NOT NULL);");
		statement
				.executeUpdate("CREATE TABLE IF NOT EXISTS "
						+ SERVER_OFFERS_TABLE_NAME
						+ " (ss_offer_id INT(10) NOT NULL AUTO_INCREMENT PRIMARY KEY, ss_id INT(10) NOT NULL, type ENUM('BUY', 'SELL'), item BLOB NOT NULL, slot INT(2) NOT NULL, price INT(8) NOT NULL);");
	}

	private Map<Integer, Shop> readPlayerShops(Statement statement)
			throws SQLException {
		ResultSet psResult = statement.executeQuery("SELECT * FROM "
				+ PLAYER_SHOPS_TABLE_NAME);
		Map<Integer, Shop> shops = new HashMap<Integer, Shop>();
		try {
			while (psResult.next()) {
				int id = psResult.getInt("ps_id");
				String ownerName = psResult.getString("owner");
				Location location = new Location(Bukkit.getWorld(psResult
						.getString("world")), psResult.getInt("x"),
						psResult.getInt("y"), psResult.getInt("z"));
				shops.put(id, new Shop(location, ownerName));
			}
		} finally {
			psResult.close();
		}
		return shops;
	}

	private Map<Integer, Shop> readServerShops(Statement statement)
			throws SQLException {
		ResultSet psResult = statement.executeQuery("SELECT * FROM "
				+ SERVER_SHOPS_TABLE_NAME);
		Map<Integer, Shop> shops = new HashMap<Integer, Shop>();
		try {
			while (psResult.next()) {
				int id = psResult.getInt("ss_id");
				Location location = new Location(Bukkit.getWorld(psResult
						.getString("world")), psResult.getInt("x"),
						psResult.getInt("y"), psResult.getInt("z"));
				shops.put(id, new Shop(location));
			}
		} finally {
			psResult.close();
		}
		return shops;
	}

	private void insertPlayerOffers(Statement statement,
			Map<Integer, Shop> shops) throws SQLException,
			ClassNotFoundException, IOException {
		ResultSet psoResult = statement.executeQuery("SELECT * FROM "
				+ PLAYER_OFFERS_TABLE_NAME);
		try {
			while (psoResult.next()) {
				Shop shop = shops.get(psoResult.getInt("ps_id"));
				if (shop != null) {
					OfferType type = OfferType.valueOf(psoResult.getString(
							"type").toUpperCase());
					ItemStack item = SerializationUtils.toItemStack(psoResult
							.getBytes("item"));
					int slot = psoResult.getInt("slot");
					int amount = psoResult.getInt("amount");
					int maxAmount = psoResult.getInt("max_amount");
					double price = (double) psoResult.getInt("price") / 100;
					shop.addOffer(
							slot,
							type.equals(OfferType.BUY) ? new PlayerShopBuyOffer(
									item, price, slot, amount, maxAmount, shop
											.getOwnerName())
									: new PlayerShopSellOffer(item, price,
											slot, amount, maxAmount, shop
													.getOwnerName()));
				}
			}
		} finally {
			psoResult.close();
		}
	}

	private void insertServerOffers(Statement statement,
			Map<Integer, Shop> shops) throws SQLException,
			ClassNotFoundException, IOException {
		ResultSet ssoResult = statement.executeQuery("SELECT * FROM "
				+ SERVER_OFFERS_TABLE_NAME);
		try {
			while (ssoResult.next()) {
				Shop shop = shops.get(ssoResult.getInt("ss_id"));
				if (shop != null) {
					OfferType type = OfferType.valueOf(ssoResult.getString(
							"type").toUpperCase());
					ItemStack item = SerializationUtils.toItemStack(ssoResult
							.getBytes("item"));
					int slot = ssoResult.getInt("slot");
					double price = (double) ssoResult.getInt("price") / 100;
					shop.addOffer(
							slot,
							type.equals(OfferType.BUY) ? new ServerShopBuyOffer(
									item, price, slot)
									: new ServerShopSellOffer(item, price, slot));
				}
			}
		} finally {
			ssoResult.close();
		}
	}

	private void addPlayerOffer(final OfferType type,
			final PlayerShopOffer offer, final Location location) {
		Bukkit.getScheduler().runTaskAsynchronously(getPlugin(),
				new Runnable() {

					@Override
					public void run() {
						try {
							Connection connection = mysqlManager
									.getConnection();
							try {
								String query = "INSERT INTO "
										+ PLAYER_OFFERS_TABLE_NAME
										+ " (ps_id, type, item, slot, amount, max_amount, price) SELECT ps_id, '"
										+ type + "', ?, " + offer.getSlot()
										+ ", " + offer.getAmount() + ", "
										+ offer.getMaxAmount() + ", "
										+ ((int) offer.getPrice() * 100)
										+ " FROM " + PLAYER_SHOPS_TABLE_NAME
										+ " WHERE world='"
										+ location.getWorld().getName()
										+ "' && x=" + location.getBlockX()
										+ " && y=" + location.getBlockY()
										+ " && z=" + location.getBlockZ() + ";";
								PreparedStatement statement = connection
										.prepareStatement(query);
								statement.setObject(1, SerializationUtils
										.toByteArray(offer.getItem()));
								statement.executeUpdate();
							} finally {
								connection.close();
							}
						} catch (SQLException e) {
							new DataAccessException(e).printStackTrace();
						} catch (IOException e) {
							new DataAccessException(e).printStackTrace();
						}
					}
				});
	}

	private void addServerOffer(final OfferType type,
			final ServerShopOffer offer, final Location location) {
		Bukkit.getScheduler().runTaskAsynchronously(getPlugin(),
				new Runnable() {

					@Override
					public void run() {
						try {
							Connection connection = mysqlManager
									.getConnection();
							try {
								String query = "INSERT INTO "
										+ SERVER_OFFERS_TABLE_NAME
										+ " (ss_id, type, item, slot, price) SELECT ss_id, '"
										+ type + "', ?, " + offer.getSlot()
										+ ", " + ((int) offer.getPrice() * 100)
										+ " FROM " + SERVER_SHOPS_TABLE_NAME
										+ " WHERE world='"
										+ location.getWorld().getName()
										+ "' && x=" + location.getBlockX()
										+ " && y=" + location.getBlockY()
										+ " && z=" + location.getBlockZ() + ";";
								PreparedStatement statement = connection
										.prepareStatement(query);
								statement.setObject(1, SerializationUtils
										.toByteArray(offer.getItem()));
								statement.executeUpdate();
							} finally {
								connection.close();
							}
						} catch (SQLException e) {
							new DataAccessException(e).printStackTrace();
						} catch (IOException e) {
							new DataAccessException(e).printStackTrace();
						}
					}
				});
	}

	private int getShopId(Statement statement, Offer offer, Shop shop,
			boolean isPlayerShop) throws SQLException {
		Location location = shop.getLocation();
		ResultSet result = statement.executeQuery("SELECT "
				+ (isPlayerShop ? "ps_id" : "ss_id")
				+ " FROM "
				+ (isPlayerShop ? PLAYER_SHOPS_TABLE_NAME
						: SERVER_SHOPS_TABLE_NAME) + " WHERE world='"
				+ location.getWorld().getName() + "' && x="
				+ location.getBlockX() + " && y=" + location.getBlockY()
				+ " && z=" + location.getBlockZ() + ";");
		try {
			if (result.next())
				return result.getInt(isPlayerShop ? "ps_id" : "ss_id");
			else
				return -1;
		} finally {
			result.close();
		}
	}

	private int importShops(Connection connection, Statement statement,
			Set<Shop> shops, String ownerName, int idCounter)
			throws SQLException, IOException {
		for (Shop shop : shops) {
			Location location = shop.getLocation();
			String insertQuery = "INSERT INTO "
					+ (ownerName != null ? PLAYER_SHOPS_TABLE_NAME
							: SERVER_SHOPS_TABLE_NAME) + " VALUES(" + idCounter
					+ ", " + (ownerName != null ? "'" + ownerName + "', " : "")
					+ "'" + location.getWorld().getName() + "', "
					+ location.getBlockX() + ", " + location.getBlockY() + ", "
					+ location.getBlockZ() + ");";
			statement.executeUpdate(insertQuery);
			for (Offer offer : shop.getOffers().values()) {
				String query;
				if (ownerName != null) {
					OfferType type = offer instanceof PlayerShopBuyOffer ? OfferType.BUY
							: OfferType.SELL;
					PlayerShopOffer playerOffer = (PlayerShopOffer) offer;
					query = "INSERT INTO " + PLAYER_OFFERS_TABLE_NAME
							+ " VALUES(NULL, " + idCounter + ", '" + type
							+ "', ?, " + playerOffer.getSlot() + ", "
							+ playerOffer.getAmount() + ", "
							+ playerOffer.getMaxAmount() + ", "
							+ playerOffer.getPrice() * 100 + ");";
				} else {
					OfferType type = offer instanceof ServerShopBuyOffer ? OfferType.BUY
							: OfferType.SELL;
					ServerShopOffer serverOffer = (ServerShopOffer) offer;
					query = "INSERT INTO " + SERVER_OFFERS_TABLE_NAME
							+ " VALUES(NULL, " + idCounter + ", '" + type
							+ "', ?, " + serverOffer.getSlot() + ", "
							+ serverOffer.getPrice() * 100 + ");";
				}
				PreparedStatement preparedStatement = connection
						.prepareStatement(query);
				try {
					preparedStatement.setObject(1,
							SerializationUtils.toByteArray(offer.getItem()));
					preparedStatement.execute();
				} finally {
					preparedStatement.close();
				}
			}
			idCounter++;
		}
		return idCounter;
	}
}
