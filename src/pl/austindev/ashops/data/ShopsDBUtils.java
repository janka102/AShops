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

import java.io.IOException;
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

import pl.austindev.ashops.shops.Offer;
import pl.austindev.ashops.shops.OfferType;
import pl.austindev.ashops.shops.PlayerShopBuyOffer;
import pl.austindev.ashops.shops.PlayerShopOffer;
import pl.austindev.ashops.shops.PlayerShopSellOffer;
import pl.austindev.ashops.shops.ServerShopBuyOffer;
import pl.austindev.ashops.shops.ServerShopOffer;
import pl.austindev.ashops.shops.ServerShopSellOffer;
import pl.austindev.ashops.shops.Shop;
import pl.austindev.mc.SerializationUtils;

public class ShopsDBUtils {
	private ShopsDBUtils() {
	}

	public static int addPlayerShop(Statement statement, Location location,
			String ownerName) throws SQLException {
		return statement.executeUpdate("INSERT INTO " + TableName.PLAYER_SHOPS
				+ " VALUES(NULL, '" + ownerName + "', "
				+ toCommaSeparated(location) + " );");
	}

	public static int addPlayerShop(Statement statement, Location location,
			String ownerName, int id) throws SQLException {
		return statement.executeUpdate("INSERT INTO " + TableName.PLAYER_SHOPS
				+ " VALUES(" + id + ", '" + ownerName + "', "
				+ toCommaSeparated(location) + " );");
	}

	public static int addServerShop(Statement statement, Location location)
			throws SQLException {
		return statement.executeUpdate("INSERT INTO " + TableName.SERVER_SHOPS
				+ " VALUES(NULL, " + toCommaSeparated(location) + " );");
	}

	public static int addServerShop(Statement statement, Location location,
			int id) throws SQLException {
		return statement.executeUpdate("INSERT INTO " + TableName.SERVER_SHOPS
				+ " VALUES(" + id + ", " + toCommaSeparated(location) + " );");
	}

	public static int createPlayerShopsTable(Statement statement)
			throws SQLException {
		return statement
				.executeUpdate("CREATE TABLE IF NOT EXISTS "
						+ TableName.PLAYER_SHOPS
						+ " (ps_id INT(10) NOT NULL AUTO_INCREMENT PRIMARY KEY, owner CHAR(17) NOT NULL, world CHAR(30) NOT NULL, x INT(6) NOT NULL, y INT(6) NOT NULL, z INT(6) NOT NULL);");
	}

	public static int createServerShopsTable(Statement statement)
			throws SQLException {
		return statement
				.executeUpdate("CREATE TABLE IF NOT EXISTS "
						+ TableName.SERVER_SHOPS
						+ " (ss_id INT(10) NOT NULL AUTO_INCREMENT PRIMARY KEY, world CHAR(30) NOT NULL, x INT(6) NOT NULL, y INT(6) NOT NULL, z INT(6) NOT NULL);");
	}

	public static int removePlayerShop(Statement statement, Location location)
			throws SQLException {
		statement.executeUpdate("DELETE FROM " + TableName.PLAYER_OFFERS
				+ " WHERE ps_id IN (SELECT ps_id FROM "
				+ TableName.PLAYER_SHOPS + " WHERE "
				+ toAmpersandSeparated(location) + ");");
		return statement.executeUpdate("DELETE FROM " + TableName.PLAYER_SHOPS
				+ " WHERE " + toAmpersandSeparated(location) + ";");
	}

	public static int removeServerShop(Statement statement, Location location)
			throws SQLException {
		statement.executeUpdate("DELETE FROM " + TableName.SERVER_OFFERS
				+ " WHERE ss_id IN (SELECT ss_id FROM "
				+ TableName.SERVER_SHOPS + " WHERE "
				+ toAmpersandSeparated(location) + ");");
		return statement.executeUpdate("DELETE FROM " + TableName.SERVER_SHOPS
				+ " WHERE " + toAmpersandSeparated(location) + ";");
	}

	public static void addPlayerOffer(Connection connection, OfferType type,
			PlayerShopOffer offer, Location location) throws SQLException,
			DataAccessException {
		String query = "INSERT INTO "
				+ TableName.PLAYER_OFFERS
				+ " (ps_id, type, item, slot, amount, max_amount, price) SELECT ps_id, '"
				+ type + "', ?, " + offer.getSlot() + ", " + offer.getAmount()
				+ ", " + offer.getMaxAmount() + ", "
				+ ((int) offer.getPrice() * 100) + " FROM "
				+ TableName.PLAYER_SHOPS + " WHERE "
				+ toAmpersandSeparated(location) + ";";
		PreparedStatement preparedStatement = connection
				.prepareStatement(query);
		try {
			preparedStatement.setObject(1,
					SerializationUtils.toByteArray(offer.getItem()));
			preparedStatement.executeUpdate();
		} catch (IOException e) {
			throw new DataAccessException("Couldn't serialize item: "
					+ offer.getItem().getType(), e);
		} finally {
			preparedStatement.close();
		}
	}

	public static void addServerOffer(Connection connection, OfferType type,
			ServerShopOffer offer, Location location) throws SQLException,
			DataAccessException {
		String query = "INSERT INTO " + TableName.SERVER_OFFERS
				+ " (ss_id, type, item, slot, price) SELECT ss_id, '" + type
				+ "', ?, " + offer.getSlot() + ", "
				+ ((int) offer.getPrice() * 100) + " FROM "
				+ TableName.SERVER_SHOPS + " WHERE "
				+ toAmpersandSeparated(location) + ";";
		PreparedStatement preparedStatement = connection
				.prepareStatement(query);
		try {
			preparedStatement.setObject(1,
					SerializationUtils.toByteArray(offer.getItem()));
			preparedStatement.executeUpdate();
		} catch (IOException e) {
			throw new DataAccessException("Couldn't serialize item: "
					+ offer.getItem().getType(), e);
		} finally {
			preparedStatement.close();
		}
	}

	public static int removeOffer(Statement statement, Location location,
			Offer offer) throws SQLException {
		if (offer instanceof PlayerShopOffer) {
			return statement.executeUpdate("DELETE FROM "
					+ TableName.PLAYER_OFFERS + " WHERE slot="
					+ offer.getSlot() + " && ps_id IN (SELECT ps_id FROM "
					+ TableName.PLAYER_SHOPS + " WHERE "
					+ toAmpersandSeparated(location) + ")");
		} else {
			return statement.executeUpdate("DELETE FROM "
					+ TableName.SERVER_OFFERS + " WHERE slot="
					+ offer.getSlot() + " && ss_id IN (SELECT ss_id FROM "
					+ TableName.SERVER_SHOPS + " WHERE "
					+ toAmpersandSeparated(location) + ")");

		}
	}

	public static void updateOffer(Statement statement, Offer offer, int shopId)
			throws SQLException {
		if (offer instanceof PlayerShopOffer) {
			PlayerShopOffer playerOffer = (PlayerShopOffer) offer;
			statement.executeUpdate("UPDATE " + TableName.PLAYER_OFFERS
					+ " SET amount=" + playerOffer.getAmount() + " WHERE slot="
					+ playerOffer.getSlot() + " && ps_id=" + shopId + ";");
		}
	}

	static int getShopId(Statement statement, Offer offer, Shop shop)
			throws SQLException {
		boolean isPlayerShop = offer instanceof PlayerShopOffer;
		Location location = shop.getLocation();
		ResultSet result = statement.executeQuery("SELECT "
				+ (isPlayerShop ? "ps_id" : "ss_id")
				+ " FROM "
				+ (isPlayerShop ? TableName.PLAYER_SHOPS
						: TableName.SERVER_SHOPS) + " WHERE "
				+ toAmpersandSeparated(location) + ";");
		try {
			if (result.next())
				return result.getInt(isPlayerShop ? "ps_id" : "ss_id");
			else
				return -1;
		} finally {
			result.close();
		}
	}

	public static int createPSOffersTable(Statement statement)
			throws SQLException {
		return statement
				.executeUpdate("CREATE TABLE IF NOT EXISTS "
						+ TableName.PLAYER_OFFERS
						+ " (ps_offer_id INT(10) NOT NULL AUTO_INCREMENT PRIMARY KEY, ps_id INT(10) NOT NULL, type ENUM('BUY', 'SELL') NOT NULL, item BLOB NOT NULL, slot INT(2) NOT NULL, amount INT(3) NOT NULL, max_amount INT(3) NOT NULL, price INT(8) NOT NULL);");
	}

	public static int createSSOffersTable(Statement statement)
			throws SQLException {
		return statement
				.executeUpdate("CREATE TABLE IF NOT EXISTS "
						+ TableName.SERVER_OFFERS
						+ " (ss_offer_id INT(10) NOT NULL AUTO_INCREMENT PRIMARY KEY, ss_id INT(10) NOT NULL, type ENUM('BUY', 'SELL') NOT NULL, item BLOB NOT NULL, slot INT(2) NOT NULL, price INT(8) NOT NULL);");
	}

	public static Map<Integer, Shop> getPlayerShops(Statement statement)
			throws SQLException {
		ResultSet result = statement.executeQuery("SELECT * FROM "
				+ TableName.PLAYER_SHOPS);
		Map<Integer, Shop> shops = new HashMap<Integer, Shop>();
		try {
			while (result.next()) {
				int id = result.getInt("ps_id");
				String ownerName = result.getString("owner");
				Location location = getLocation(result);
				shops.put(id, new Shop(location, ownerName));
			}
		} finally {
			result.close();
		}
		return shops;
	}

	public static Map<Integer, Shop> getPlayerShops(Statement statement,
			String ownerName) throws SQLException {
		Map<Integer, Shop> shops = new HashMap<Integer, Shop>();
		ResultSet result = statement.executeQuery("SELECT * FROM "
				+ TableName.PLAYER_SHOPS + " WHERE owner='" + ownerName + "';");
		while (result.next())
			shops.put(result.getInt("ps_id"), new Shop(getLocation(result),
					ownerName));
		return shops;
	}

	public static Map<Integer, Shop> getServerShops(Statement statement)
			throws SQLException {
		ResultSet result = statement.executeQuery("SELECT * FROM "
				+ TableName.SERVER_SHOPS);
		Map<Integer, Shop> shops = new HashMap<Integer, Shop>();
		try {
			while (result.next()) {
				int id = result.getInt("ss_id");
				Location location = getLocation(result);
				shops.put(id, new Shop(location));
			}
		} finally {
			result.close();
		}
		return shops;
	}

	public static void insertPlayerOffers(Statement statement,
			Map<Integer, Shop> shops) throws SQLException {
		ResultSet result = statement.executeQuery("SELECT * FROM "
				+ TableName.PLAYER_OFFERS);
		try {
			Set<Integer> offersToRemove = new HashSet<Integer>();
			while (result.next()) {
				int offerId = result.getInt("ps_offer_id");
				Shop shop = shops.get(result.getInt("ps_id"));
				if (shop != null) {
					OfferType type = OfferType.valueOf(result.getString("type")
							.toUpperCase());
					ItemStack item = SerializationUtils.toItemStack(result
							.getBytes("item"));
					int slot = result.getInt("slot");
					int amount = result.getInt("amount");
					int maxAmount = result.getInt("max_amount");
					double price = (double) result.getInt("price") / 100;
					shop.addOffer(
							slot,
							type.equals(OfferType.BUY) ? new PlayerShopBuyOffer(
									item, price, slot, amount, maxAmount, shop
											.getOwnerName())
									: new PlayerShopSellOffer(item, price,
											slot, amount, maxAmount, shop
													.getOwnerName()));
				} else {
					offersToRemove.add(offerId);
				}
			}
			removePlayerOffers(statement, offersToRemove);
		} catch (ClassNotFoundException e) {
			new DataAccessException("Corrupted offer records.", e);
		} catch (IOException e) {
			new DataAccessException("Corrupted offer records.", e);
		} finally {
			result.close();
		}
	}

	public static void insertPlayerOffers(Statement statement, Shop shop,
			int shopId) throws SQLException {
		try {
			ResultSet result = statement.executeQuery("SELECT * FROM "
					+ TableName.PLAYER_OFFERS + " WHERE ps_id=" + shopId);
			while (result.next()) {
				OfferType type = OfferType.valueOf(result.getString("type")
						.toUpperCase());
				ItemStack item = SerializationUtils.toItemStack(result
						.getBytes("item"));
				int slot = result.getInt("slot");
				int amount = result.getInt("amount");
				int maxAmount = result.getInt("max_amount");
				double price = (double) result.getInt("price") / 100;
				shop.addOffer(
						slot,
						type.equals(OfferType.BUY) ? new PlayerShopBuyOffer(
								item, price, slot, amount, maxAmount, shop
										.getOwnerName())
								: new PlayerShopSellOffer(item, price, slot,
										amount, maxAmount, shop.getOwnerName()));
			}
		} catch (ClassNotFoundException e) {
			new DataAccessException("Corrupted offer records.", e);
		} catch (IOException e) {
			new DataAccessException("Corrupted offer records.", e);
		}
	}

	public static void insertServerOffers(Statement statement,
			Map<Integer, Shop> shops) throws SQLException {
		ResultSet result = statement.executeQuery("SELECT * FROM "
				+ TableName.SERVER_OFFERS);
		try {
			Set<Integer> offersToRemove = new HashSet<Integer>();
			while (result.next()) {
				int offerId = result.getInt("ss_offer_id");
				Shop shop = shops.get(result.getInt("ss_id"));
				if (shop != null) {
					OfferType type = OfferType.valueOf(result.getString("type")
							.toUpperCase());
					ItemStack item = SerializationUtils.toItemStack(result
							.getBytes("item"));
					int slot = result.getInt("slot");
					double price = (double) result.getInt("price") / 100;
					shop.addOffer(
							slot,
							type.equals(OfferType.BUY) ? new ServerShopBuyOffer(
									item, price, slot)
									: new ServerShopSellOffer(item, price, slot));
				} else {
					offersToRemove.add(offerId);
				}
			}
			removeServerOffers(statement, offersToRemove);
		} catch (ClassNotFoundException e) {
			new DataAccessException("Corrupted offer records.", e);
		} catch (IOException e) {
			new DataAccessException("Corrupted offer records.", e);
		} finally {
			result.close();
		}
	}

	public static void clearPlayerShops(Statement statement, String ownerName)
			throws SQLException {
		statement.executeUpdate("DELETE FROM " + TableName.PLAYER_OFFERS
				+ " WHERE ps_id IN(SELECT ps_id FROM " + TableName.PLAYER_SHOPS
				+ " WHERE owner='" + ownerName + "');");
		statement.executeUpdate("DELETE FROM " + TableName.PLAYER_SHOPS
				+ " WHERE owner='" + ownerName + "';");
	}

	public static void clearPlayerShops(Statement statement)
			throws SQLException {
		statement.executeUpdate("TRUNCATE TABLE " + TableName.PLAYER_SHOPS
				+ ";");
		statement.executeUpdate("TRUNCATE TABLE " + TableName.PLAYER_OFFERS
				+ ";");
		statement.executeUpdate("ALTER TABLE " + TableName.PLAYER_SHOPS
				+ " AUTO_INCREMENT = 0;");
		statement.executeUpdate("ALTER TABLE " + TableName.PLAYER_OFFERS
				+ " AUTO_INCREMENT = 0;");
	}

	public static void clearServerShops(Statement statement)
			throws SQLException {
		statement.executeUpdate("TRUNCATE TABLE " + TableName.SERVER_SHOPS
				+ ";");
		statement.executeUpdate("TRUNCATE TABLE " + TableName.SERVER_OFFERS
				+ ";");
		statement.executeUpdate("ALTER TABLE " + TableName.SERVER_SHOPS
				+ " AUTO_INCREMENT = 0;");
		statement.executeUpdate("ALTER TABLE " + TableName.SERVER_OFFERS
				+ " AUTO_INCREMENT = 0;");
	}

	private static void removePlayerOffers(Statement statement,
			Set<Integer> offers) throws SQLException {
		for (Integer offerId : offers)
			statement.executeUpdate("DELETE FROM " + TableName.PLAYER_OFFERS
					+ " WHERE ps_offer_id=" + offerId + ";");
	}

	private static void removeServerOffers(Statement statement,
			Set<Integer> offers) throws SQLException {
		for (Integer offerId : offers)
			statement.executeUpdate("DELETE FROM " + TableName.SERVER_OFFERS
					+ " WHERE ss_offer_id=" + offerId + ";");
	}

	private static Location getLocation(ResultSet result) throws SQLException {
		return new Location(Bukkit.getWorld(result.getString("world")),
				result.getInt("x"), result.getInt("y"), result.getInt("z"));
	}

	private static String toCommaSeparated(Location location) {
		return "'" + location.getWorld().getName() + "', "
				+ location.getBlockX() + " ," + location.getBlockY() + " , "
				+ location.getBlockZ();
	}

	private static String toAmpersandSeparated(Location location) {
		return "world='" + location.getWorld().getName() + "' && x="
				+ location.getBlockX() + " && y=" + location.getBlockY()
				+ " && z=" + location.getBlockZ();
	}

	private enum TableName {
		PLAYER_SHOPS("as_player_shops"), SERVER_SHOPS("as_server_shops"), PLAYER_OFFERS(
				"as_ps_offers"), SERVER_OFFERS("as_ss_offers");

		private final String tableName;

		private TableName(String tableName) {
			this.tableName = tableName;
		}

		public String toString() {
			return tableName;
		}
	}

}
