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

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;

public abstract class BlockUtils {
	public static Set<Block> getHorizontalNeighbours(Block block) {
		Set<Block> neighbours = new HashSet<Block>();
		for (BlockFace blockFace : getHorizontalBlockFaces()) {
			if (block != null) {
				Block neighbour = block.getRelative(blockFace);
				if (neighbour != null)
					neighbours.add(neighbour);
			}
		}
		return neighbours;
	}

	public static boolean isDoubleChest(Chest chest) {
		for (Block neighbour : getHorizontalNeighbours(chest.getBlock()))
			if (neighbour.getType().equals(Material.CHEST))
				return true;
		return false;
	}

	public static Set<BlockFace> getHorizontalBlockFaces() {
		return EnumSet.of(BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST,
				BlockFace.WEST);
	}

	public static Block getAttachedBlock(Sign sign) {
		org.bukkit.material.Sign signData = (org.bukkit.material.Sign) sign
				.getData();
		if (signData.isWallSign())
			return sign.getBlock().getRelative(signData.getAttachedFace());
		else
			return null;
	}

	public static void setLines(Sign sign, String firstLine, String secondLine,
			String thirdLine, String fourthLine) {
		if (firstLine != null)
			sign.setLine(0, firstLine);
		if (secondLine != null)
			sign.setLine(1, secondLine);
		if (thirdLine != null)
			sign.setLine(2, thirdLine);
		if (fourthLine != null)
			sign.setLine(3, fourthLine);
		sign.update();
	}

	public static boolean isClear(Sign sign) {
		for (String line : sign.getLines())
			if (line != null && !line.equals(""))
				return false;
		return true;
	}

	public static void closeForAll(Inventory inventory) {
		for (HumanEntity entity : new HashSet<HumanEntity>(
				inventory.getViewers()))
			entity.closeInventory();
	}
}
