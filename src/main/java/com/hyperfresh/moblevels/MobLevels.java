package com.hyperfresh.moblevels;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.HashMap;
import java.util.Map;

public class MobLevels extends JavaPlugin implements Listener
{
	@Override
	public void onEnable()
	{
		Bukkit.getPluginManager().registerEvents(this, this);

		for(World world: Bukkit.getWorlds())
		{
			for(LivingEntity entity: world.getLivingEntities())
			{
				if(entity instanceof Monster)
				{
					entityCache.put(entity.getEntityId(), new Mob(this, entity));
				}
			}
		}
	}

	@Override
	public void onDisable()
	{

	}

	private static final Map<Integer, Mob> entityCache = new HashMap<>();

	@EventHandler
	public void onEntitySpawn(CreatureSpawnEvent event)
	{
		EntityType type = event.getEntityType();

		LivingEntity entity = event.getEntity();
		if(entity instanceof Monster)
		{
			entityCache.put(entity.getEntityId(), new Mob(this, entity));
		}
	}

	@EventHandler
	public void onEntityDeath(EntityDeathEvent event)
	{
		LivingEntity entity = event.getEntity();

		if(entityCache.containsKey(entity.getEntityId()))
		{
			Mob mob = entityCache.remove(entity.getEntityId());

			int baseExp = event.getDroppedExp();
			event.setDroppedExp(baseExp * mob.getLevel());
		}
	}

	@EventHandler
	public void onEntityAttack(EntityDamageByEntityEvent event)
	{
		LivingEntity damager = null;

		if(event.getDamager().getType() == EntityType.ARROW)
		{
			Arrow arrow = (Arrow)event.getDamager();

			if(arrow.getShooter() instanceof LivingEntity)
			{
				damager = (LivingEntity)arrow.getShooter();
			}
		}

		if(event.getDamager() instanceof LivingEntity)
		{
			damager = (LivingEntity)event.getDamager();
		}

		if(damager != null)
		{
			if(entityCache.containsKey(damager.getEntityId()))
			{
				Mob mob = entityCache.get(damager.getEntityId());

				event.setDamage(mob.getAttackMultiplier() * event.getDamage());
			}
		}

		if(event.getEntity() instanceof LivingEntity)
		{
			LivingEntity victim = (LivingEntity)event.getEntity();

			double postHealth = victim.getHealth() - event.getDamage();

			if(entityCache.containsKey(victim.getEntityId()) && postHealth > 0)
			{
				entityCache.get(victim.getEntityId()).flashHealth((int)postHealth);
			}
			else if(damager != null && postHealth <= 0)
			{
				entityCache.get(damager.getEntityId()).updateName();
			}
		}

	}
}
