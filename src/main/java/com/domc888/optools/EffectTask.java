package com.domc888.optools;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public final class EffectTask extends BukkitRunnable {

    private static final int DURATION_TICKS = 100;
    private static final int REFRESH_BELOW_TICKS = 60;

    private final OPTools plugin;
    private final Items items;

    public EffectTask(OPTools plugin, Items items) {
        this.plugin = plugin;
        this.items = items;
    }

    @Override
    public void run() {
        if (!plugin.isToolsEnabled()) {
            return;
        }

        FileConfiguration config = plugin.getConfig();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.isDead()) {
                continue;
            }
            handleEclipseBlade(player, config);
            handleArmorSet(player, config);
        }
    }

    private void handleEclipseBlade(Player player, FileConfiguration config) {
        World world = player.getWorld();
        if (world.getEnvironment() != World.Environment.NORMAL) {
            return;
        }
        if (!carriesEclipseBlade(player)) {
            return;
        }

        long time = world.getTime();
        boolean night = time >= 12300L && time <= 23850L;
        String path = night ? "weapons.eclipse_blade.night" : "weapons.eclipse_blade.day";

        apply(player, PotionEffectType.SPEED, config.getInt(path + ".speed_level", 1));
        apply(player, PotionEffectType.STRENGTH, config.getInt(path + ".strength_level", night ? 2 : 1));
        apply(player, PotionEffectType.FIRE_RESISTANCE, config.getInt(path + ".fire_resistance_level", 1));
    }

    private boolean carriesEclipseBlade(Player player) {
        for (ItemStack stack : player.getInventory().getContents()) {
            if (items.isEclipseBlade(stack)) {
                return true;
            }
        }
        return false;
    }

    private void handleArmorSet(Player player, FileConfiguration config) {
        PlayerInventory inventory = player.getInventory();
        boolean fullSet = items.isHelmet(inventory.getHelmet())
                && items.isChestplate(inventory.getChestplate())
                && items.isLeggings(inventory.getLeggings())
                && items.isBoots(inventory.getBoots());
        if (!fullSet) {
            return;
        }
        apply(player, PotionEffectType.RESISTANCE, config.getInt("armor.op_set.full_set_resistance_level", 1));
    }

    private void apply(Player player, PotionEffectType type, int level) {
        if (level <= 0) {
            return;
        }
        int amplifier = level - 1;

        PotionEffect current = player.getPotionEffect(type);
        if (current != null
                && current.getAmplifier() == amplifier
                && (current.isInfinite() || current.getDuration() >= REFRESH_BELOW_TICKS)) {
            return;
        }

        player.addPotionEffect(new PotionEffect(type, DURATION_TICKS, amplifier, false, false, true));
    }
}
