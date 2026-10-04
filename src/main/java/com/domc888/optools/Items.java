package com.domc888.optools;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Map;

public final class Items {

    public static final List<String> IDS =
            List.of("stormbreaker", "eclipse", "helmet", "chestplate", "leggings", "boots");

    private final NamespacedKey stormbreakerKey;
    private final NamespacedKey eclipseKey;
    private final NamespacedKey helmetKey;
    private final NamespacedKey chestplateKey;
    private final NamespacedKey leggingsKey;
    private final NamespacedKey bootsKey;

    public Items(OPTools plugin) {
        this.stormbreakerKey = new NamespacedKey(plugin, "stormbreaker");
        this.eclipseKey = new NamespacedKey(plugin, "eclipse_blade");
        this.helmetKey = new NamespacedKey(plugin, "op_helmet");
        this.chestplateKey = new NamespacedKey(plugin, "op_chestplate");
        this.leggingsKey = new NamespacedKey(plugin, "op_leggings");
        this.bootsKey = new NamespacedKey(plugin, "op_boots");
    }

    public ItemStack create(String id) {
        return switch (id) {
            case "stormbreaker" -> build(Material.WOODEN_AXE, "Stormbreaker", stormbreakerKey, Map.of(
                    "sharpness", 5,
                    "efficiency", 5,
                    "fortune", 3,
                    "unbreaking", 3,
                    "mending", 1));
            case "eclipse" -> build(Material.WOODEN_SWORD, "Eclipse Blade", eclipseKey, Map.of(
                    "fire_aspect", 2,
                    "looting", 3,
                    "sweeping_edge", 3,
                    "unbreaking", 3,
                    "mending", 1));
            case "helmet" -> build(Material.NETHERITE_HELMET, "OP Helmet", helmetKey, Map.of(
                    "protection", 4,
                    "respiration", 3,
                    "aqua_affinity", 1,
                    "unbreaking", 3,
                    "mending", 1));
            case "chestplate" -> build(Material.NETHERITE_CHESTPLATE, "OP Chestplate", chestplateKey, Map.of(
                    "protection", 4,
                    "unbreaking", 3,
                    "mending", 1));
            case "leggings" -> build(Material.NETHERITE_LEGGINGS, "OP Leggings", leggingsKey, Map.of(
                    "protection", 4,
                    "unbreaking", 3,
                    "mending", 1));
            case "boots" -> build(Material.NETHERITE_BOOTS, "OP Boots", bootsKey, Map.of(
                    "protection", 4,
                    "feather_falling", 4,
                    "depth_strider", 3,
                    "unbreaking", 3,
                    "mending", 1));
            default -> null;
        };
    }

    private ItemStack build(Material material, String name, NamespacedKey key, Map<String, Integer> enchants) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text(name).decoration(TextDecoration.ITALIC, false));

        for (Map.Entry<String, Integer> entry : enchants.entrySet()) {
            Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(entry.getKey()));
            if (enchantment == null) {
                throw new IllegalStateException("Unknown enchantment: " + entry.getKey());
            }
            meta.addEnchant(enchantment, entry.getValue(), true);
        }

        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.values());
        meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);

        item.setItemMeta(meta);
        return item;
    }

    private boolean has(ItemStack stack, NamespacedKey key) {
        if (stack == null || stack.getType().isAir() || !stack.hasItemMeta()) {
            return false;
        }
        return stack.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.BYTE);
    }

    public boolean isStormbreaker(ItemStack stack) {
        return has(stack, stormbreakerKey);
    }

    public boolean isEclipseBlade(ItemStack stack) {
        return has(stack, eclipseKey);
    }

    public boolean isHelmet(ItemStack stack) {
        return has(stack, helmetKey);
    }

    public boolean isChestplate(ItemStack stack) {
        return has(stack, chestplateKey);
    }

    public boolean isLeggings(ItemStack stack) {
        return has(stack, leggingsKey);
    }

    public boolean isBoots(ItemStack stack) {
        return has(stack, bootsKey);
    }
}
