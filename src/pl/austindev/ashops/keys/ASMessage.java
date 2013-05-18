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
package pl.austindev.ashops.keys;

import org.bukkit.ChatColor;

import pl.austindev.mc.AMessage;

public enum ASMessage implements AMessage {
	CMD_ASSHOP(Level.NONE), CMD_ASHOP(Level.NONE), CMD_AREMOVE(Level.NONE), CMD_ATOGGLE(
			Level.NONE), CMD_ASELL(Level.NONE), CMD_ABUY(Level.NONE), CMD_AREPAIR(
			Level.NONE), CMD_ASAVE(Level.NONE), CMD_ALOAD(Level.NONE), CMD_ACLEAR(
			Level.NONE), CMD_ASHOPS(Level.NONE), CMD_ARELOAD(Level.NONE), NOT_PLAYER(
			Level.FAILURE), SELECT_CHEST(Level.INFO), NO_MONEY(Level.FAILURE) /* int */, LIMIT(
			Level.FAILURE), NO_PERMISSION(Level.FAILURE), INVALID_PLAYER(
			Level.FAILURE), ABORTED(Level.FAILURE), CREATED(Level.SUCCESS), ALREADY_SHOP(
			Level.FAILURE), ERROR(Level.ERROR), SHOP_NEIGHBOUR(Level.FAILURE), DOUBLE_CHEST(
			Level.FAILURE), REMOVED(Level.SUCCESS), NOT_OWNER(Level.FAILURE), NOT_SHOP(
			Level.FAILURE), WRONG_ITEM(Level.FAILURE), WRONG_AMOUNT(
			Level.FAILURE), WRONG_PRICE(Level.FAILURE), NO_SLOTS(Level.FAILURE), OFFER_ADDED(
			Level.SUCCESS), MINIMAL_PRICE(Level.FAILURE) /* double */, ITEM_EXCLUDED(
			Level.FAILURE), CLIENT_NO_ITEM(Level.FAILURE), CLIENT_NO_MONEY(
			Level.FAILURE), CLIENT_NO_SPACE(Level.FAILURE), OWNER_NO_ITEMS(
			Level.FAILURE), OWNER_NO_MONEY(Level.FAILURE), OWNER_NO_SPACE(
			Level.FAILURE), DOUBLE_CHEST_ACCESS(Level.FAILURE), FILE_SAVED(
			Level.SUCCESS), CREATIVE_ACCESS(Level.FAILURE), NO_ITEMS_TO_ADD(
			Level.FAILURE), NO_ITEMS_TO_COLLECT(Level.FAILURE), COLLECT_ITEMS(
			Level.FAILURE), NO_SPACE_FOR_ITEMS(Level.FAILURE), SHOPS_CLEARED(
			Level.SUCCESS), FILE_LOADED(Level.SUCCESS), RIGHT_CLICK_EXPECTED(
			Level.FAILURE), REPAIR_MODE(Level.INFO), NORMAL_MODE(Level.INFO), SHOP_CLOSED(
			Level.FAILURE), SHOP_ACTIVATED(Level.SUCCESS), SHOP_DEACTIVATED(
			Level.SUCCESS), NOT_CHEST(Level.FAILURE), REPAIR_FAILURE(
			Level.FAILURE), SHOP_RECREATED(Level.SUCCESS), NO_SIGN(
			Level.FAILURE), SIGN_LINE_CLOSED(Level.NONE), NOT_SHOP_REGION(
			Level.FAILURE), RELOADED(Level.SUCCESS), NOT_OWNER_NAME(
			Level.FAILURE), DEFAULT_SELL_DESCRIPTION(Level.NONE), DEFAULT_BUY_DESCRIPTION(
			Level.NONE), CHEST_NEIGHBOUR(Level.FAILURE);
	private String colors;

	private ASMessage(Level level, ChatColor... colors) {
		StringBuilder colorsString = new StringBuilder(level.colors);
		for (ChatColor color : colors)
			colorsString.append(color);
		this.colors = colorsString.toString();
	}

	@Override
	public String getColors() {
		return colors;
	}

	private enum Level {
		SUCCESS(ChatColor.GREEN), INFO(ChatColor.AQUA), FAILURE(ChatColor.RED), ERROR(
				ChatColor.DARK_RED), NONE();
		private final String colors;

		private Level(ChatColor... colors) {
			StringBuilder colorsString = new StringBuilder();
			for (ChatColor color : colors)
				colorsString.append(color);
			this.colors = colorsString.toString();
		}
	}
}
