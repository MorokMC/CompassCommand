package de.dustplanet.compasscommand;

import de.dustplanet.compasscommand.CompassCommandListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class CompassCommand extends JavaPlugin implements CommandExecutor, Listener {

   public Inventory compassInv;
   public Inventory compassInvAdmin;
   public ItemStack compassItem;
   public ItemStack compassItemAdmin;
   public HashMap<String, String> itemsAndCommands;
   public HashMap<String, String> itemsAndCommandsAdmin = new HashMap<String, String>();
   public FileConfiguration config;
   public FileConfiguration admin;
   private File adminFile;
   private File configFile;


   public void onEnable() {
      this.loadConfig();
      this.config = this.getConfig();
      this.saveConfig();
      this.admin = YamlConfiguration.loadConfiguration(this.adminFile);

      try {
         this.admin.save(this.adminFile);
      } catch (IOException var2) {
         this.getLogger().warning("Failed to save the admin.yml");
         var2.printStackTrace();
      }

      this.getServer().getPluginManager().registerEvents(new CompassCommandListener(this), this);
      this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
      this.compassInv = this.makeInventory(this.config);
      this.compassInvAdmin = this.makeInventory(this.admin);
      this.compassItem = this.makeItem(this.config);
      this.compassItemAdmin = this.makeItem(this.admin);
      this.itemsAndCommands = this.makeCommandList(this.config, this.compassInv);
      this.itemsAndCommandsAdmin = this.makeCommandList(this.admin, this.compassInvAdmin);
   }

   private void loadConfig() {
      this.configFile = new File(this.getDataFolder(), "config.yml");
      if(!this.configFile.exists() && !this.getDataFolder().exists() && !this.getDataFolder().mkdirs()) {
         this.getLogger().severe("The config folder could NOT be created, make sure it\'s writable!");
         this.getLogger().severe("Disabling now!");
         this.setEnabled(false);
      } else {
         if(!this.configFile.exists()) {
            this.copy(this.getResource("config.yml"), this.configFile);
         }

         this.adminFile = new File(this.getDataFolder(), "admin.yml");
         if(!this.adminFile.exists()) {
            this.copy(this.getResource("admin.yml"), this.adminFile);
         }

      }
   }

   private Inventory makeInventory(FileConfiguration fileConf) {
      int slots = fileConf.getInt("global.slots");
      if(slots % 9 != 0) {
         slots = 9;
      }

      String title = ChatColor.translateAlternateColorCodes('&', fileConf.getString("global.invTitle"));
      return this.getServer().createInventory((InventoryHolder)null, slots, title);
   }

   private ItemStack makeItem(FileConfiguration fileConf) {
      ItemStack item = new ItemStack(Material.COMPASS, 1);
      Material mat = Material.matchMaterial(fileConf.getString("item.type"));
      if(mat != null) {
         item.setType(mat);
      }

      ItemMeta meta = item.getItemMeta();
      meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', fileConf.getString("item.title")));
      meta.setLore(fileConf.getStringList("item.lore"));
      item.setItemMeta(meta);
      return item;
   }

   private HashMap<String, String> makeCommandList(FileConfiguration fileConf, Inventory inv) {
      HashMap<String, String> map = new HashMap<String, String>();
      Iterator<?> var5 = fileConf.getConfigurationSection("commands").getKeys(false).iterator();

      while(var5.hasNext()) {
         String key = (String)var5.next();
         Material mat = Material.matchMaterial(key.toUpperCase());
         if(mat == null) {
            this.getLogger().info("The item " + key + " is unkown and was skipped!");
         } else {
            map.put(mat.name(), fileConf.getString("commands." + key + ".command"));
            ItemStack item = new ItemStack(mat, 1);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', fileConf.getString("commands." + key + ".title")));
            meta.setLore(fileConf.getStringList("commands." + key + ".lore"));
            item.setItemMeta(meta);
            if(inv.firstEmpty() != -1) {
               inv.addItem(new ItemStack[]{item});
            }
         }
      }

      return map;
   }

   private void copy(InputStream in, File file) {
      FileOutputStream out = null;

      try {
         out = new FileOutputStream(file);
         byte[] e = new byte[1024];

         int len;
         while((len = in.read(e)) > 0) {
            out.write(e, 0, len);
         }
      } catch (IOException var18) {
         this.getLogger().warning("Failed to copy the default config! (I/O)");
         var18.printStackTrace();
      } finally {
         try {
            if(out != null) {
               out.flush();
               out.close();
            }
         } catch (IOException var17) {
            this.getLogger().warning("Failed to close the streams! (I/O -> Output)");
            var17.printStackTrace();
         }

         try {
            if(in != null) {
               in.close();
            }
         } catch (IOException var16) {
            this.getLogger().warning("Failed to close the streams! (I/O -> Input)");
            var16.printStackTrace();
         }

      }

   }

   public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
      if(sender instanceof Player) {
         Player p = (Player)sender;
         if(args.length == 1 && args[0].equalsIgnoreCase("admin")) {
            if(p.hasPermission("compasscommand.admin")) {
               if(p.getInventory().firstEmpty() != -1) {
                  p.getInventory().addItem(new ItemStack[]{this.compassItemAdmin});
               }

               sender.sendMessage(ChatColor.GREEN + "Item added!");
            } else {
               sender.sendMessage(ChatColor.RED + "You do not have the permission to do this!");
            }
         } else if(p.hasPermission("compasscommand.get")) {
            if(p.getInventory().firstEmpty() != -1) {
               p.getInventory().addItem(new ItemStack[]{this.compassItem});
            }

            sender.sendMessage(ChatColor.GREEN + "Item added!");
         } else {
            sender.sendMessage(ChatColor.RED + "You do not have the permission to do this!");
         }
      } else {
         sender.sendMessage("This command can only be used ingame");
      }

      return true;
   }
}
