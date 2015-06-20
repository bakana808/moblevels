package com.hyperfresh.moblevels;

import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitTask;

public class Mob
{
	private static final double LV_MULT_DIST = 1.0 / 100.0;	// +1 level per 100 distance
	private static final double LV_MULT_DEPTH = 1.0 / 8.0;	// +1 level per 4 blocks down

	private static final double MAX_HEALTH = 2048.0;
	private static final int GROUND_LEVEL = 64;
	private static final String BAR = "\u258C";

	private LivingEntity handle;
	private MobLevels plugin;

	private int level;
	private final double maxHealth;
	private final double attackMultiplier;

	public Mob(MobLevels plugin, LivingEntity entity)
	{
		this.handle = entity;
		this.plugin = plugin;

		Location loc = entity.getLocation();
		loc.setY(0);

		// X/Z distance
		double distance = loc.distance(entity.getWorld().getSpawnLocation());

		// Y distance below ground level
		double depth = Math.max(0, GROUND_LEVEL - entity.getLocation().getY());

		this.level = floor(depth * LV_MULT_DEPTH);
		this.level += floor(distance * LV_MULT_DIST);

		// set max health
		this.maxHealth = calculateMaxHealth(entity.getMaxHealth(), level);
		handle.setMaxHealth(maxHealth);
		handle.setHealth(maxHealth);

		// set attack
		this.attackMultiplier = calculateAttackMultiplier(level);

		handle.setCustomNameVisible(true);
		updateName();
	}

	private static double calculateMaxHealth(double baseMaxHealth, int level)
	{
		double healthBoost = level * baseMaxHealth * 0.25;

		return Math.min(MAX_HEALTH, baseMaxHealth + healthBoost);
	}

	private static double calculateAttackMultiplier(int level)
	{
		double attackBoost = level * 0.2;
		return 1 + attackBoost;
	}

	private int floor(Double d)
	{
		return ((Double)Math.floor(d)).intValue();
	}

	private BukkitTask flashHealthTask = null;

	public void flashHealth(int health)
	{
		if(flashHealthTask != null)
		{
			flashHealthTask.cancel();
		}
		handle.setCustomName(ChatColor.BOLD + "" + health + " / " + (int)maxHealth + " HP");
		Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, this::updateName, 10);
	}

	public void updateName()
	{
		handle.setCustomName(
			ChatColor.DARK_GRAY + "[" + levelColor() + "Lv." + (level + 1) + ChatColor.DARK_GRAY + "] " +
			ChatColor.WHITE + WordUtils.capitalize(handle.getType().name().toLowerCase())
		);
	}

	private ChatColor levelColor()
	{
		if(level < 5)
		{
			return ChatColor.AQUA;
		}
		if(level < 10)
		{
			return ChatColor.GREEN;
		}
		if(level < 25)
		{
			return ChatColor.YELLOW;
		}
		if(level < 40)
		{
			return ChatColor.GOLD;
		}
		return ChatColor.RED;
	}

	public double getAttackMultiplier()
	{
		return attackMultiplier;
	}

	public int getLevel()
	{
		return level;
	}
}
