package pl.austindev.mc;

/**
 * ImprovedOfflinePlayer, a library for Bukkit.
 * Copyright (C) 2012 one4me@github.com
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.UUID;

import net.minecraft.server.v1_5_R3.NBTCompressedStreamTools;
import net.minecraft.server.v1_5_R3.NBTTagCompound;
import net.minecraft.server.v1_5_R3.NBTTagDouble;
import net.minecraft.server.v1_5_R3.NBTTagFloat;
import net.minecraft.server.v1_5_R3.NBTTagList;
import net.minecraft.server.v1_5_R3.PlayerInventory;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_5_R3.inventory.CraftInventoryPlayer;
import org.bukkit.entity.Player;
import com.google.common.io.Files;

/**
 * @name ImprovedOfflinePlayer
 * @version 1.5.0
 * @author one4me
 */
public class ImprovedOfflinePlayer {
  private String player;
  private File file;
  private NBTTagCompound compound;
  private boolean exists = false;
  private boolean autosave = true;
  public ImprovedOfflinePlayer(String playername) {
    this.exists = loadPlayerData(playername);
  }
  public ImprovedOfflinePlayer(OfflinePlayer offlineplayer) {
    this.exists = loadPlayerData(offlineplayer.getName());
  }
  private boolean loadPlayerData(String name) {
    try {
      this.player = name;
      for(World w : Bukkit.getWorlds()) {
        this.file = new File(w.getWorldFolder(), "players" + File.separator + this.player + ".dat");
        if(this.file.exists()){
          this.compound = NBTCompressedStreamTools.a(new FileInputStream(this.file));
          this.player = this.file.getCanonicalFile().getName().replace(".dat", "");
          return true;
        }
      }
    }
    catch(Exception e) {
      e.printStackTrace();
    }
    return false;
  }
  public void savePlayerData() {
    if(this.exists) {
      try {
        NBTCompressedStreamTools.a(this.compound, new FileOutputStream(this.file));
      }
      catch(Exception e) {
        e.printStackTrace();
      }
    }
  }
  public boolean exists() {
    return this.exists;
  }
  public boolean getAutoSave() {
    return this.autosave;
  }
  public void setAutoSave(boolean autosave) {
    this.autosave = autosave;
  }
  /**@param Incomplete**/
  public void copyDataTo(String playername) {
    try {
      if(!playername.equalsIgnoreCase(this.player)) {
        Player to = Bukkit.getPlayerExact(playername);
        Player from = Bukkit.getPlayerExact(this.player);
        if(from != null) {
          from.saveData();
        }
        Files.copy(this.file, new File(this.file.getParentFile(), playername + ".dat"));
        if(to != null) {
          to.teleport(from == null ? getLocation() : from.getLocation());
          to.loadData();
        }
      }
      else {
        Player player = Bukkit.getPlayerExact(this.player);
        if(player != null) {
          player.saveData();
        }
      }
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }
//  public PlayerAbilities getAbilities() {
//    PlayerAbilities pa = new PlayerAbilities();
//    pa.a(this.compound);
//    return pa;
//  }
//  public void setAbilities(PlayerAbilities abilities) {
//    abilities.a(this.compound);
//    savePlayerData();
//  }

  /* <3 md_5 */
  public org.bukkit.inventory.PlayerInventory getInventory() {
    PlayerInventory inventory = new PlayerInventory(null);
    inventory.b(this.compound.getList("Inventory"));
    return new CraftInventoryPlayer(inventory);
  }
  /* <3 md_5 */
  public void setInventory(org.bukkit.inventory.PlayerInventory inventory) {
    this.compound.set("Inventory", ((CraftInventoryPlayer)inventory).getInventory().a(new NBTTagList()));
    if(this.autosave) savePlayerData();
  }
  public Location getLocation() {
    NBTTagList position = this.compound.getList("Pos");
    NBTTagList rotation = this.compound.getList("Rotation");
    return new Location(
      Bukkit.getWorld(new UUID(this.compound.getLong("WorldUUIDMost"), this.compound.getLong("WorldUUIDLeast"))),
      ((NBTTagDouble)position.get(0)).data,
      ((NBTTagDouble)position.get(1)).data,
      ((NBTTagDouble)position.get(2)).data,
      ((NBTTagFloat)rotation.get(0)).data,
      ((NBTTagFloat)rotation.get(1)).data
    );
  }
  public void setLocation(Location location) {
    World w = location.getWorld();
    UUID uuid = w.getUID();
    this.compound.setLong("WorldUUIDMost", uuid.getMostSignificantBits());
    this.compound.setLong("WorldUUIDLeast", uuid.getLeastSignificantBits());
    this.compound.setInt("Dimension", w.getEnvironment().getId());
    NBTTagList position = new NBTTagList();
    position.add(new NBTTagDouble(null, location.getX()));
    position.add(new NBTTagDouble(null, location.getY()));
    position.add(new NBTTagDouble(null, location.getZ()));
    this.compound.set("Pos", position);
    NBTTagList rotation = new NBTTagList();
    rotation.add(new NBTTagFloat(null, location.getYaw()));
    rotation.add(new NBTTagFloat(null, location.getPitch()));
    this.compound.set("Rotation", rotation);
    if(this.autosave) savePlayerData();
  }
  public String getName() {
    return this.player;
  }
}
/*
 * Copyright (C) 2012 one4me@github.com
 */