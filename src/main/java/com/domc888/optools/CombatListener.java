package com.domc888.optools;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class CombatListener implements Listener {

    private final OPTools plugin;
    private final Items items;

    // attacker UUID -> (target UUID -> hit count)
    private final Map<UUID, Map<UUID, Integer>> hits = new HashMap<>();

    public CombatListener(OPTools plugin, Items items) {
        this.plugin = plugin;
        this.items = items;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onHit(EntityDamageByEntityEvent event) {
        if (!plugin.isToolsEnabled()) {
            return;
        }
        if (event.getCause() != DamageCause.ENTITY_ATTACK) {
            return;
        }
        if (!(event.getDamager() instanceof Player attacker)) {
            return;
        }
        if (!(event.getEntity() instanceof LivingEntity target)) {
            return;
        }
        if (!items.isStormbreaker(attacker.getInventory().getItemInMainHand())) {
            return;
        }

        Map<UUID, Integer> perTarget = hits.computeIfAbsent(attacker.getUniqueId(), k -> new HashMap<>());
        int count = perTarget.merge(target.getUniqueId(), 1, Integer::sum);

        int required = Math.max(1, plugin.getConfig().getInt("weapons.stormbreaker.hits_required", 3));
        if (count < required) {
            return;
        }
        perTarget.remove(target.getUniqueId());

        double bonus = plugin.getConfig().getDouble("weapons.stormbreaker.lightning_damage", 6.0);
        int armorDamage = plugin.getConfig().getInt("weapons.stormbreaker.armor_durability_damage", 15);

        target.getWorld().strikeLightningEffect(target.getLocation());
        event.setDamage(event.getDamage() + bonus);
        damageArmor(target, armorDamage);
    }

    private void damageArmor(LivingEntity target, int amount) {
        EntityEquipment equipment = target.getEquipment();
        if (equipment == null) {
            return;
        }

        ItemStack[] armor = equipment.getArmorContents();
        boolean changed = false;

        for (int i = 0; i < armor.length; i++) {
            ItemStack piece = armor[i];
            if (piece == null || piece.getType().isAir()) {
                continue;
            }
            int max = piece.getType().getMaxDurability();
            if (max <= 0) {
                continue;
            }
            if (!(piece.getItemMeta() instanceof Damageable damageable)) {
                continue;
            }
            if (damageable.isUnbreakable()) {
                continue;
            }

            int newDamage = damageable.getDamage() + amount;
            if (newDamage >= max) {
                armor[i] = null;
            } else {
                damageable.setDamage(newDamage);
                piece.setItemMeta(damageable);
            }
            changed = true;
        }

        if (changed) {
            equipment.setArmorContents(armor);
        }
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        forget(event.getEntity().getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        forget(event.getPlayer().getUniqueId());
    }

    private void forget(UUID id) {
        hits.remove(id);
        for (Map<UUID, Integer> perTarget : hits.values()) {
            perTarget.remove(id);
        }
        hits.values().removeIf(Map::isEmpty);
    }
}
