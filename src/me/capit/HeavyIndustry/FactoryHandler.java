package me.capit.HeavyIndustry;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import me.capit.HeavyIndustry.Factory.UnableToProcessException;
import me.capit.HeavyIndustry.HeavyIndustry.SetupReturn;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;

public class FactoryHandler implements Listener,CommandExecutor {
	
	HeavyIndustry plugin;
	public FactoryHandler(HeavyIndustry plugin){
		this.plugin = plugin;
	}
	
	public void setRecipes(){
		ItemStack wrench = new ItemStack(Material.IRON_HOE);
		wrench.setItemMeta(plugin.wrench);
		ShapedRecipe wrenchR = new ShapedRecipe(wrench);
		wrenchR.shape("IS", "IS", " S");
		wrenchR.setIngredient('I', Material.IRON_INGOT);
		wrenchR.setIngredient('S', Material.STICK);
		plugin.getServer().addRecipe(wrenchR);
		plugin.log.info(" Added custom recipe for FACTORY_WRENCH.");
		
		ItemStack exp = new ItemStack(Material.EXP_BOTTLE, 9);
		ShapelessRecipe expR = new ShapelessRecipe(exp);
		expR.addIngredient(1, Material.EMERALD);
		plugin.getServer().addRecipe(expR);
		plugin.log.info(" Added custom recipe for EXP_BOTTLE.");
		
		ItemStack emerald = new ItemStack(Material.EMERALD, 1);
		ShapelessRecipe emeraldR = new ShapelessRecipe(emerald);
		emeraldR.addIngredient(9, Material.EXP_BOTTLE);
		plugin.getServer().addRecipe(emeraldR);
		plugin.log.info(" Added custom recipe for EMERALD.");
		
		ConfigurationSection items = plugin.getConfig().getConfigurationSection("items");
		for (String itemStr : items.getKeys(false)){
			ConfigurationSection item = items.getConfigurationSection(itemStr);
			ItemMeta meta = new ItemStack(Material.valueOf(item.getString("item"))).getItemMeta();
			meta.setDisplayName(item.getString("name"));
			meta.setLore(Arrays.asList(item.getString("lore", ChatColor.YELLOW+"Heavy Industry")));
			plugin.log.info(" Added meta for "+itemStr+".");
			plugin.metas.put(item.getName(), meta);
		}
	}
	
	public SetupReturn registerFactories(FileConfiguration config) {
		SetupReturn val = SetupReturn.OK;
		
		Set<String> factories = config.getConfigurationSection("factories").getKeys(false);
		for (String fac : factories){
			ConfigurationSection fc = config.getConfigurationSection("factories."+fac);
			if (fc.getString("name")==""){fc.set("name", fac);}
			plugin.log.info(" Adding "+fc.getString("name")+"...");
			Factory f = new Factory(plugin, fc);
			
			plugin.log.info("  Rendering layout...");
			ConfigurationSection layoutCfg = fc.getConfigurationSection("layout");
			FactoryLayout layout = new FactoryLayout();
			layout.addRow(layoutCfg.getStringList("blocks.1"), 1);
			layout.addRow(layoutCfg.getStringList("blocks.2"), 2);
			layout.addRow(layoutCfg.getStringList("blocks.3"), 3);
			for (String key : layoutCfg.getConfigurationSection("binds").getKeys(false)){
				layout.addBind(key.charAt(0), Material.valueOf(layoutCfg.getString("binds."+key)));
			}
			f.setLayout(layout);
			plugin.log.info("  Layout OK!");
			
			plugin.log.info("  Rendering recipes...");
			Set<String> recipes = fc.getConfigurationSection("recipes").getKeys(false);
			for (String recipeStr : recipes){
				ConfigurationSection rec = fc.getConfigurationSection("recipes."+recipeStr);
				if (rec.getString("name")==""){rec.set("name", fac);}
				plugin.log.info("   Adding "+rec.getString("name")+"...");
				
				FactoryRecipe fr = new FactoryRecipe(rec.getString("name"), rec.getInt("time"));
				
				List<String> input = rec.getStringList("input");
				if (input.size()==3){
					try {
						fr.setInput(input.get(0), input.get(1), input.get(2));
					} catch (IllegalArgumentException e){
						plugin.log.info("    Skipped input due to bad lengths.");
					}
				} else {
					plugin.log.info("    Skipped input due to bad format.");
				}
				
				List<String> output = rec.getStringList("output");
				if (output.size()==3){
					try {
						fr.setOutput(output.get(0), output.get(1), output.get(2));
					} catch (IllegalArgumentException e){
						plugin.log.info("    Skipped output due to bad lengths.");
					}
				} else {
					plugin.log.info("    Skipped output due to bad format.");
				}
				
				Set<String> binds = rec.getConfigurationSection("binds").getKeys(false);
				for (String bindStr : binds){
					ConfigurationSection bind = rec.getConfigurationSection("binds."+bindStr);
					String istr = bind.getString("item");
					if (istr.startsWith("MOD:")){
						String ci = istr.substring(4);
						ConfigurationSection ccs = plugin.getConfig().getConfigurationSection("items."+ci);
						ItemStack is = new ItemStack(Material.valueOf(ccs.getString("item")));
						is.setAmount(bind.contains("num") ? bind.getInt("num") : 1);
						is.setDurability(bind.contains("dmg") ? (byte)bind.getInt("dmg") : (byte)0);
						is.setItemMeta(plugin.metas.get(ci));
						fr.assignChar(bind.getName().charAt(0), is);
					} else {
						ItemStack is = new ItemStack(Material.valueOf(istr));
						is.setAmount(bind.contains("num") ? bind.getInt("num") : 1);
						is.setDurability(bind.contains("dmg") ? (byte)bind.getInt("dmg") : (byte)0);
						fr.assignChar(bind.getName().charAt(0), is);
					}
				}
				f.addRecipe(fr);
			}
			plugin.log.info("  Recipes OK!");
			plugin.factories.add(f);
		}
		
		return val;
	}
	
	@EventHandler
	public void playerInteract(PlayerInteractEvent e){
		if (e.getAction()==Action.LEFT_CLICK_BLOCK && e.hasItem()){
			Player p = e.getPlayer();
			Block b = e.getClickedBlock();
			ItemStack i = p.getItemInHand();
			if (i.getType()==Material.IRON_HOE && i.getItemMeta()!=null && 
					i.getItemMeta().getDisplayName().equalsIgnoreCase("Factory Wrench")){
				if (b.getType()==Material.CHEST){
					plugin.log.info("Player "+p.getName()+" started possible working factory.");
					i.setDurability((short) (i.getDurability()+1));
					createFactory(b.getLocation(), p);
					e.setCancelled(true);
				}
			}
		} else if (e.getAction()==Action.RIGHT_CLICK_AIR || e.getAction()==Action.RIGHT_CLICK_BLOCK) {
			Player p = e.getPlayer();
			ItemStack i = p.getItemInHand();
			if (e.getAction()==Action.RIGHT_CLICK_BLOCK && e.getClickedBlock().getType()==Material.ANVIL){
				e.setCancelled(true);
			}
			if (e.hasItem() && i.getType()==Material.EXP_BOTTLE && !e.isCancelled()){
				e.setCancelled(true);
				p.giveExp(20);
				if (i.getAmount()>1){
					i.setAmount(i.getAmount()-1);
				} else {
					i.setType(Material.AIR);
				}
			}
		}
	}
	
	@EventHandler
	public void expDropped(PlayerExpChangeEvent e){
		if (e.getAmount()!=20) e.setAmount(0);
	}
	
	@EventHandler
	public void pistonPush(BlockPistonExtendEvent e){
		Block p = e.getBlock();
		Block b = p.getRelative(e.getDirection());
		if (b.getType()==Material.POWERED_RAIL || b.getType()==Material.DETECTOR_RAIL || 
				b.getType()==Material.ACTIVATOR_RAIL) e.setCancelled(true);
	}
	
	public void createFactory(Location loc, Player p){
		Factory fac = null;
		System.out.println("checking against "+plugin.factories.size()+" factories");
		for (Factory f : plugin.factories){
			if (f.createable(loc)){
				fac = f;
			}
		}
		if (fac!=null){
			try {
				fac.process(loc, p);
				p.sendMessage(ChatColor.GREEN + "Factory has started!");
			} catch (UnableToProcessException e) {
				p.sendMessage(ChatColor.RED + e.getMessage());
			}
		} else {
			plugin.log.info("There doesn't appear to be a factory here.");
		}
	}

	@Override
	public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {
		if (cmd.equals("industry")){
			if (args.length==0){
				onCommand(s,cmd,label,new String[]{"reload"});
			} else if (args[0].equalsIgnoreCase("reload")){
				if (s.hasPermission("industry.reload")){
					plugin.reloadConfig();
					s.sendMessage(ChatColor.YELLOW+"Successfully reloaded HeavyIndustry configuration.");
					return true;
				} else {
					s.sendMessage(ChatColor.RED+"You do not have permission to reload HeavyIndustry.");
					return true;
				}
			}
		}
		return false;
	}

	
	
}
