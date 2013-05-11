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

import pl.austindev.ashops.AShops;
import pl.austindev.commands.ABuyCommandExecutor;
import pl.austindev.commands.AClearCommandExecutor;
import pl.austindev.commands.ALoadCommandExecutor;
import pl.austindev.commands.AReloadCommandExecutor;
import pl.austindev.commands.ARemoveCommandExecutor;
import pl.austindev.commands.ARepairCommandExecutor;
import pl.austindev.commands.ASShopCommandExecutor;
import pl.austindev.commands.ASaveCommandExecutor;
import pl.austindev.commands.ASellCommandExecutor;
import pl.austindev.commands.AShopCommandExecutor;
import pl.austindev.commands.AShopsCommandExecutor;
import pl.austindev.commands.AToggleCommandExecutor;
import pl.austindev.mc.ACommand;
import pl.austindev.mc.ACommandExecutor;
import pl.austindev.mc.AMessage;
import pl.austindev.mc.APermission;

public enum ASCommand implements ACommand {
	ASHOP(AShopCommandExecutor.class, ASMessage.CMD_ASHOP, true, 0, 1, ASPermission.OWN_BUY_SHOP,
			ASPermission.OWN_SELL_SHOP),
	AREMOVE(ARemoveCommandExecutor.class, ASMessage.CMD_AREMOVE, true, 0, 0,
			ASPermission.OWN_BUY_SHOP, ASPermission.OWN_SELL_SHOP),
	ASSHOP(ASShopCommandExecutor.class, ASMessage.CMD_ASSHOP, true, 0, 0,
			ASPermission.SERVER_BUY_SHOP, ASPermission.SERVER_SELL_SHOP),
	ABUY(ABuyCommandExecutor.class, ASMessage.CMD_ABUY, true, 2, 3,
			ASPermission.OWN_BUY_SHOP),
	ASELL(ASellCommandExecutor.class,
			ASMessage.CMD_ASELL, true, 1, 2, ASPermission.OWN_SELL_SHOP),
	ASHOPS(AShopsCommandExecutor.class, ASMessage.CMD_ASHOPS, false, 0, 0,
			ASPermission.SELL_TO_SHOP, ASPermission.BUY_FROM_SHOP),
	ASAVE(ASaveCommandExecutor.class, ASMessage.CMD_ASAVE, false, 0, 0,
			ASPermission.OPERATOR),
	ALOAD(ALoadCommandExecutor.class,
			ASMessage.CMD_ALOAD, false, 0, 0, ASPermission.OPERATOR),
	ACLEAR(AClearCommandExecutor.class, ASMessage.CMD_ACLEAR, false, 1, 1,
			ASPermission.OPERATOR),
	AREPAIR(ARepairCommandExecutor.class,
			ASMessage.CMD_AREPAIR, true, 0, 0, ASPermission.OTHERS_BUY_SHOP,
			ASPermission.OTHERS_SELL_SHOP),
	ATOGGLE(AToggleCommandExecutor.class,
			ASMessage.CMD_ATOGGLE, true, 0, 0, ASPermission.OWN_BUY_SHOP,
			ASPermission.OWN_SELL_SHOP),
	ARELOAD(AReloadCommandExecutor.class, ASMessage.CMD_ARELOAD, false, 0,
			0, ASPermission.OPERATOR);

	private final Class<? extends ACommandExecutor<AShops>> executor;
	private final AMessage description;
	private final APermission[] permissions;
	private final boolean forPlayer;
	private final int minArgumentsNumber;
	private final int maxArgumentsNumber;

	private ASCommand(Class<? extends ACommandExecutor<AShops>> executor,
			AMessage description, boolean forPlayer, int minArgumentsNumber,
			int maxArgumentsNumber, APermission... permissions) {
		this.executor = executor;
		this.description = description;
		this.permissions = permissions;
		this.forPlayer = forPlayer;
		this.minArgumentsNumber = minArgumentsNumber;
		this.maxArgumentsNumber = maxArgumentsNumber;
	}

	@Override
	public Class<? extends ACommandExecutor<AShops>> getExecutor() {
		return executor;
	}

	@Override
	public AMessage getDescription() {
		return description;
	}

	@Override
	public int getMinArgumentsNumber() {
		return minArgumentsNumber;
	}

	@Override
	public int getMaxArgumentsNumber() {
		return maxArgumentsNumber;
	}

	@Override
	public APermission[] getPermissions() {
		return permissions;
	}

	@Override
	public boolean isForPlayer() {
		return forPlayer;
	}
}
