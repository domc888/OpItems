package com.domc888.optools;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public final class OPTools extends JavaPlugin implements TabExecutor {

    private Items items;
    private boolean toolsEnabled = true;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        items = new Items(this);

        getServer().getPluginManager().registerEvents(new CombatListener(this, items), this);
        new EffectTask(this, items).runTaskTimer(this, 20L, 20L);

        Objects.requireNonNull(getCommand("optools")).setExecutor(this);
    }

    public boolean isToolsEnabled() {
        return toolsEnabled;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("optools.admin")) {
            sender.sendMessage(Component.text("You do not have permission."));
            return true;
        }
        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "give" -> handleGive(sender, args);
            case "toggle" -> {
                toolsEnabled = !toolsEnabled;
                sender.sendMessage(Component.text("OPTools " + (toolsEnabled ? "enabled." : "disabled.")));
            }
            case "reload" -> {
                reloadConfig();
                sender.sendMessage(Component.text("OPTools config reloaded."));
            }
            default -> sendUsage(sender);
        }
        return true;
    }

    private void handleGive(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sendUsage(sender);
            return;
        }

        ItemStack item = items.create(args[1].toLowerCase(Locale.ROOT));
        if (item == null) {
            sender.sendMessage(Component.text("Unknown item: " + args[1]));
            return;
        }

        Player target;
        if (args.length >= 3) {
            target = Bukkit.getPlayerExact(args[2]);
        } else if (sender instanceof Player self) {
            target = self;
        } else {
            sender.sendMessage(Component.text("Specify a player."));
            return;
        }

        if (target == null) {
            sender.sendMessage(Component.text("Player not found."));
            return;
        }

        Map<Integer, ItemStack> leftovers = target.getInventory().addItem(item);
        for (ItemStack left : leftovers.values()) {
            target.getWorld().dropItemNaturally(target.getLocation(), left);
        }
        sender.sendMessage(Component.text("Gave " + args[1].toLowerCase(Locale.ROOT) + " to " + target.getName() + "."));
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage(Component.text("/optools give <stormbreaker|eclipse|helmet|chestplate|leggings|boots> [player]"));
        sender.sendMessage(Component.text("/optools toggle"));
        sender.sendMessage(Component.text("/optools reload"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("optools.admin")) {
            return List.of();
        }
        if (args.length == 1) {
            return filter(List.of("give", "toggle", "reload"), args[0]);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            return filter(Items.IDS, args[1]);
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            List<String> names = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                names.add(p.getName());
            }
            return filter(names, args[2]);
        }
        return List.of();
    }

    private List<String> filter(List<String> options, String prefix) {
        String lower = prefix.toLowerCase(Locale.ROOT);
        List<String> out = new ArrayList<>();
        for (String option : options) {
            if (option.toLowerCase(Locale.ROOT).startsWith(lower)) {
                out.add(option);
            }
        }
        return out;
    }
}
