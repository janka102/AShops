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
package pl.austindev.mc;

import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;

import org.bukkit.ChatColor;


public class MessageTranslator {
	private final ResourceBundle translations;

	public MessageTranslator(String resourcePath, Locale locale) {
		translations = ResourceBundle.getBundle(resourcePath, locale);
	}

	public MessageTranslator(String resourcePath, Locale locale,
			boolean debug) {
		translations = debug ? null : ResourceBundle.getBundle(resourcePath,
				locale);
	}

	public String translate(AMessage key, Object... arguments) {
		String message;
		if (translations != null) {
			message = translations.getString(key.name()).replace("&&",
					key.getColors());
			message = ChatColor.translateAlternateColorCodes('&', message);
			message = String.format(Locale.ENGLISH, message, arguments);
		} else {
			message = "[" + key.name() + ": " + Arrays.asList(arguments) + "]";
		}
		return key.getColors() + message;
	}

	public ResourceBundle getResourceBundle() {
		return translations;
	}
}
