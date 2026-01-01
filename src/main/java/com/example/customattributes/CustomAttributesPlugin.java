package com.example.customattributes;

import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class CustomAttributesPlugin extends JavaPlugin {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        if (args.length != 3 || !args[0].equalsIgnoreCase("set")) {
            sender.sendMessage("Usage: /attributeedit set <durability|armor|armour|toughness|knockbackres> <value>");
            return true;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() == Material.AIR) {
            sender.sendMessage("Hold the armor piece you want to edit in your main hand.");
            return true;
        }

        EquipmentSlot slot = getArmorSlot(item.getType());
        if (slot == null) {
            sender.sendMessage("That item is not recognized as armor.");
            return true;
        }

        double value;
        try {
            value = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage("Value must be a number.");
            return true;
        }

        String target = args[1].toLowerCase(Locale.ROOT);
        if (target.equals("durability")) {
            if (!applyDurability(item, value)) {
                sender.sendMessage("That item does not support durability changes.");
                return true;
            }
            sender.sendMessage("Durability updated.");
            return true;
        }

        Attribute attribute = parseAttribute(target);
        if (attribute == null) {
            sender.sendMessage("Unknown attribute. Use armor, toughness, or knockbackres.");
            return true;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            sender.sendMessage("Unable to edit that item.");
            return true;
        }

        meta.removeAttributeModifier(attribute);
        AttributeModifier modifier = new AttributeModifier(
            UUID.randomUUID(),
            attribute.name().toLowerCase(Locale.ROOT),
            value,
            AttributeModifier.Operation.ADD_NUMBER,
            slot
        );
        meta.addAttributeModifier(attribute, modifier);
        item.setItemMeta(meta);

        sender.sendMessage("Attribute updated.");
        return true;
    }

    private Attribute parseAttribute(String target) {
        return switch (target) {
            case "armor", "armour" -> Attribute.GENERIC_ARMOR;
            case "toughness" -> Attribute.GENERIC_ARMOR_TOUGHNESS;
            case "knockbackres", "knockbackresistance", "knockback" -> Attribute.GENERIC_KNOCKBACK_RESISTANCE;
            default -> null;
        };
    }

    private boolean applyDurability(ItemStack item, double desired) {
        ItemMeta meta = item.getItemMeta();
        if (!(meta instanceof Damageable damageable)) {
            return false;
        }

        int maxDurability = item.getType().getMaxDurability();
        int desiredDurability = (int) Math.round(desired);
        if (desiredDurability < 0) {
            desiredDurability = 0;
        }
        if (desiredDurability > maxDurability) {
            desiredDurability = maxDurability;
        }

        int damage = Math.max(0, maxDurability - desiredDurability);
        damageable.setDamage(damage);
        item.setItemMeta((ItemMeta) damageable);
        return true;
    }

    private EquipmentSlot getArmorSlot(Material material) {
        String name = material.name();
        if (name.endsWith("HELMET") || name.endsWith("HEAD") || name.equals("TURTLE_HELMET")) {
            return EquipmentSlot.HEAD;
        }
        if (name.endsWith("CHESTPLATE") || name.endsWith("ELYTRA")) {
            return EquipmentSlot.CHEST;
        }
        if (name.endsWith("LEGGINGS")) {
            return EquipmentSlot.LEGS;
        }
        if (name.endsWith("BOOTS")) {
            return EquipmentSlot.FEET;
        }
        return null;
    }
}
