package com.github.bfabri.hosts.utils;

import com.github.bfabri.hosts.ConfigHandler;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.*;
import java.util.stream.Collectors;

public class Utils {

    public static String PREFIX = translate(ConfigHandler.Configs.LANG.getConfig().getString("PREFIX"));

    @Getter
    private final HashMap<UUID, Long> cooldowns = new HashMap<>();
    private final Hashtable<String, String> soundByString = new Hashtable<>();

    public static String translate(String input) {
        return ChatColor.translateAlternateColorCodes('&', input.replace("<", "\u00AB").replace(">", "\u00BB"));
    }

    public static List<String> translate(List<String> input) {
        List<String> newInput = new ArrayList<String>();
        input.forEach(line -> newInput.add(ChatColor.translateAlternateColorCodes('&', line)));
        return newInput;
    }

    public static boolean isEmpty(Inventory inventory, boolean checkArmour) {
        boolean result = true;

        for (int i = 0; i < inventory.getContents().length; i++) {
            ItemStack itemStack = inventory.getContents()[i];
            if (itemStack != null && itemStack.getType() != Material.AIR) {
                result = false;
                break;
            }
        }
        if (!result) {
            return false;
        }
        if (checkArmour && inventory instanceof PlayerInventory) {
            for (int i = 0; i < ((PlayerInventory) inventory).getArmorContents().length; i++) {
                ItemStack itemStack = ((PlayerInventory) inventory).getArmorContents()[i];
                if (itemStack != null && itemStack.getType() != Material.AIR) {
                    result = false;
                    break;
                }
            }
        }
        return result;
    }

    public static Enchantment getEnchantmentFromNiceName(String name) {
        Enchantment enchantment = null;
        try {
            enchantment = Enchantment.getByName(name);
        } catch (Exception ignored) {
        }

        if (enchantment != null) {
            return enchantment;
        }
        switch (name.toLowerCase()) {
            case "sharpness":
                enchantment = Enchantment.DAMAGE_ALL;
                break;
            case "unbreaking":
                enchantment = Enchantment.DURABILITY;
                break;
            case "efficiency":
                enchantment = Enchantment.DIG_SPEED;
                break;
            case "protection":
                enchantment = Enchantment.PROTECTION_ENVIRONMENTAL;
                break;
            case "power":
                enchantment = Enchantment.ARROW_DAMAGE;
                break;
            case "punch":
                enchantment = Enchantment.ARROW_KNOCKBACK;
                break;
            case "infinite":
                enchantment = Enchantment.ARROW_INFINITE;
                break;
        }

        return enchantment;
    }

    public static List<String> getCompletions(String[] args, List<String> input) {
        return getCompletions(args, input, 80);
    }

    public static List<String> getCompletions(String[] args, String... input) {
        return getCompletions(args, 80, input);
    }

    public static List<String> getCompletions(String[] args, List<String> input, int limit) {
        Preconditions.checkNotNull((Object) args);
        Preconditions.checkArgument(args.length != 0);
        String argument = args[args.length - 1];
        return input.stream().filter(string -> string.regionMatches(true, 0, argument, 0, argument.length())).limit(limit).collect(Collectors.toList());
    }

    public static List<String> getCompletions(String[] args, int limit, String... input) {
        Preconditions.checkNotNull((Object) args);
        Preconditions.checkArgument(args.length != 0);
        String argument = args[args.length - 1];
        return Arrays.stream(input).collect(Collectors.toList()).stream().filter(string -> string.regionMatches(true, 0, argument, 0, argument.length())).limit(limit).collect(Collectors.toList());
    }
}