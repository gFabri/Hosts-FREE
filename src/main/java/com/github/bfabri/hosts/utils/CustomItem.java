package com.github.bfabri.hosts.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

public class CustomItem {

    private final ItemStack itemStack;
    private final ItemMeta itemMeta;

    public CustomItem(Material material, int amount, int shorts) {
        if (material.name().equalsIgnoreCase("POTION")) {
            this.itemStack = new ItemStack(material, amount, (short) 0);
            this.itemStack.setDurability((short) shorts);
        } else {
            this.itemStack = new ItemStack(material, amount, (short) shorts);
        }
        this.itemMeta = this.itemStack.getItemMeta();
    }

    public CustomItem setName(String displayName) {
        if (displayName == null){
            return this;
        }
        this.itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName).replace("<", "«").replace(">", "»"));
        return this;
    }

    public CustomItem addLore(List<String> strings) {
        if (strings == null) {
            return this;
        }
        List<String> newLore = new ArrayList<>();
        strings.forEach(nowLore -> newLore.add(ChatColor.translateAlternateColorCodes('&', nowLore.replace("<", "«").replace(">", "»"))));
        this.itemMeta.setLore(newLore);
        return this;
    }

    public CustomItem addEnchantments(ArrayList<String> enchantments) {
        if (enchantments == null){
            return this;
        }
        for (Object object : enchantments) {
            String enchantment = (String) object;
            String[] args = enchantment.split(":");
            int level = 1;
            if (args.length > 1) {
                level = Integer.parseInt(args[1]);
            }
            Enchantment enchant = Utils.getEnchantmentFromNiceName(args[0].toUpperCase());
            if (enchant == null) {
                Bukkit.getLogger().warning("Invalid enchantment " + args[0].toUpperCase());
                continue;
            }
            this.itemMeta.addEnchant(enchant, level, true);
        }
        return this;
    }

    public CustomItem addLore(String... strings) {
        List<String> newLore = new ArrayList<>();
        for (String nowLore : strings) {
            newLore.add(ChatColor.translateAlternateColorCodes('&', nowLore.replace("<", "\u00AB").replace(">", "\u00BB")));
        }
        this.itemMeta.setLore(newLore);
        return this;
    }

    public ItemStack createHead(Player player, String name) {
        SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();
        if (GameUtils.getVersion() > 18) {
            skullMeta.setOwnerProfile(player.getPlayerProfile());
        } else {
            skullMeta.setOwner(player.getName());
        }
        skullMeta.setDisplayName(name);
        this.itemStack.setItemMeta(skullMeta);
        return this.itemStack;
    }

    public ItemStack create() {
        this.itemStack.setItemMeta(this.itemMeta);
        return this.itemStack;
    }
}
