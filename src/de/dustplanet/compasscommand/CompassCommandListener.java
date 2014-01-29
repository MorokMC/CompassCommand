package de.dustplanet.compasscommand;

import de.dustplanet.compasscommand.CompassCommand;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

public class CompassCommandListener implements Listener {

   private CompassCommand plugin;


   public CompassCommandListener(CompassCommand instance) {
      this.plugin = instance;
   }

   @EventHandler
   public void onInventoryClick(InventoryClickEvent event) {
      if(event.getInventory().getTitle().equals(this.plugin.compassInv.getTitle())) {
         this.fireGUI(event, this.plugin.itemsAndCommands);
      } else if(event.getInventory().getTitle().equals(this.plugin.compassInvAdmin.getTitle())) {
         this.fireGUI(event, this.plugin.itemsAndCommandsAdmin);
      }

   }

   private void fireGUI(InventoryClickEvent event, HashMap<String, String> map) {
      Player player = (Player)event.getWhoClicked();
      if(event.getCurrentItem() == null) {
         event.setCancelled(true);
      } else {
         String item = event.getCurrentItem().getType().name();
         if(map.containsKey(item)) {
            String command = (String)map.get(item);
            event.setCancelled(true);
            player.closeInventory();
            if(!command.startsWith("server")) {
               player.performCommand(command);
            } else {
               ByteArrayOutputStream b = new ByteArrayOutputStream();
               DataOutputStream out = new DataOutputStream(b);
               String server = command.substring(command.indexOf(32) + 1);

               try {
                  out.writeUTF("Connect");
                  out.writeUTF(server);
               } catch (IOException var10) {
                  ;
               }

               player.sendPluginMessage(this.plugin, "BungeeCord", b.toByteArray());
            }
         }

      }
   }

   @EventHandler
   public void onPlayerJoin(PlayerJoinEvent event) {
      Player player = event.getPlayer();
      if(!player.hasPlayedBefore()) {
         player.getInventory().addItem(new ItemStack[]{this.plugin.compassItem});
      } else if(!player.getInventory().contains(this.plugin.compassItem)) {
         event.getPlayer().getInventory().addItem(new ItemStack[]{this.plugin.compassItem});
      }

   }

   @EventHandler
   public void onItemDrop(PlayerDropItemEvent event) {
      if(event.getItemDrop().getItemStack().equals(this.plugin.compassItem) || event.getItemDrop().getItemStack().equals(this.plugin.compassItemAdmin)) {
         event.setCancelled(true);
      }

   }

   @EventHandler
   public void onPlayerInteract(PlayerInteractEvent event) {
      if(event.hasItem() && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
         Player player = event.getPlayer();
         if(event.getItem().equals(this.plugin.compassItem)) {
            player.openInventory(this.plugin.compassInv);
         } else if(event.getItem().equals(this.plugin.compassItemAdmin) && player.hasPermission("compasscommand.admin")) {
            player.openInventory(this.plugin.compassInvAdmin);
         }
      }

   }
}
