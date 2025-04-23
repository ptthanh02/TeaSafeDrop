package dev.nighter.teaSafeDrop;

import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ItemProtectionListener implements Listener {

    private final TeaSafeDrop plugin;

    public ItemProtectionListener(TeaSafeDrop plugin) {
        this.plugin = plugin;
    }

    /**
     * Handles entity damage events to protect items from fire and other damage
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();

        // Check if the entity is an item
        if (!(entity instanceof Item)) {
            return;
        }

        Item item = (Item) entity;
        ItemStack itemStack = item.getItemStack();
        World world = item.getWorld();

        // Check damage causes that we want to protect against
        EntityDamageEvent.DamageCause cause = event.getCause();
        if (cause == EntityDamageEvent.DamageCause.FIRE ||
                cause == EntityDamageEvent.DamageCause.FIRE_TICK ||
                cause == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION ||
                cause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {

            // Check if the item should be protected
            if (plugin.isItemProtected(itemStack.getType(), world)) {
                event.setCancelled(true);

                // For fire, also extinguish the item
                if (cause == EntityDamageEvent.DamageCause.FIRE ||
                        cause == EntityDamageEvent.DamageCause.FIRE_TICK) {
                    item.setFireTicks(0);
                }

                if (plugin.isDebug()) {
                    plugin.getLogger().info("Protected " + itemStack.getType() + " from " + cause + " in " + world.getName());
                }
            }
        }
    }

    /**
     * Handles explosion events to prevent protected items from being destroyed
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        World world = event.getLocation().getWorld();

        // We need to scan all dropped items in the explosion radius to protect them
        List<Entity> nearbyEntities = event.getEntity().getNearbyEntities(5, 5, 5);

        for (Entity entity : nearbyEntities) {
            if (entity instanceof Item) {
                Item item = (Item) entity;
                ItemStack itemStack = item.getItemStack();

                if (plugin.isItemProtected(itemStack.getType(), world)) {
                    // In Folia, we need to run entity operations on the entity's thread
                    // Use the entity scheduler instead of the global scheduler
                    Scheduler.runEntityTask(item, () -> {
                        item.setInvulnerable(true);

                        // Make it vulnerable again after the explosion
                        Scheduler.runEntityTaskLater(item, () -> {
                            item.setInvulnerable(false);
                        }, 10);
                    });

                    if (plugin.isDebug()) {
                        plugin.getLogger().info("Protected " + itemStack.getType() + " from explosion in " + world.getName());
                    }
                }
            }
        }
    }

    /**
     * Handles item despawn events to potentially cancel them for protected items
     * Useful for items that have survived but might despawn due to fire/explosion damage
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onItemDespawn(ItemDespawnEvent event) {
        Item item = event.getEntity();
        ItemStack itemStack = item.getItemStack();
        World world = item.getWorld();

        // If the item is on fire and it's protected, prevent it from despawning
        if (item.getFireTicks() > 0 && plugin.isItemProtected(itemStack.getType(), world)) {
            event.setCancelled(true);
            item.setFireTicks(0);

            if (plugin.isDebug()) {
                plugin.getLogger().info("Prevented " + itemStack.getType() + " from despawning due to fire in " + world.getName());
            }
        }
    }
}