package me.capit.HeavyIndustry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class HeavyIndustry extends JavaPlugin {
	
	public List<Factory> factories = new ArrayList<Factory>();
	public HashMap<String,ItemMeta> metas = new HashMap<String,ItemMeta>();
	public Logger log;
	
	public ItemMeta reforgedIron, wrench, corruptionEssence;
	
	private FactoryHandler fh;
	
	public enum SetupReturn{
		OK, ISSUE, FATAL
	}
	
	@Override
	public void onEnable(){
		log = this.getLogger();
		
		log.info("#=============================================#");
		log.info("           Starting HeavyIndustry...");
		
		log.info("Verifying files...");
			saveDefaultConfig();
		log.info("Files OK!");
		
		log.info("Registering events and handlers...");
			fh = new FactoryHandler(this);
			this.getServer().getPluginManager().registerEvents(fh, this);
		log.info("Registration OK!");
		
		log.info("Setting up custom items...");
			wrench = new ItemStack(Material.IRON_HOE).getItemMeta();
			wrench.setDisplayName("Factory Wrench");
			wrench.setLore(Arrays.asList("Manages Factories"));
			log.info(" Added meta for FACTORY_WRENCH.");
			
			reforgedIron = new ItemStack(Material.IRON_INGOT).getItemMeta();
			reforgedIron.setDisplayName("Reforged Iron");
			reforgedIron.setLore(Arrays.asList("Crafting Ingredient"));
			log.info(" Added meta for REFORGED_IRON.");
			
			corruptionEssence = new ItemStack(Material.FIREBALL).getItemMeta();
			corruptionEssence.setDisplayName("Essence of Corruption");
			corruptionEssence.setLore(Arrays.asList("Crafting Ingredient"));
			log.info(" Added meta for ESSENCE_OF_CORRUPTION.");
			
			fh.setRecipes();
		log.info("Items OK!");
			
		log.info("Adding factories to list...");
			SetupReturn ret = fh.registerFactories(this.getConfig());
			switch(ret){
			case OK:
				log.info("Factories OK!");
				break;
			case ISSUE:
				log.info("Factories ERROR! Start-up attempted anyway!");
				break;
			case FATAL:
				log.info("Factories ERROR! Could not attempt start-up!");
				log.info("#=============================================#");
				this.getServer().getPluginManager().disablePlugin(this);
				break;
			}
		log.info("#=============================================#");
		
	}
	
	public boolean itemArraysMatch(ItemStack[] s1, ItemStack[] s2){
		boolean match = true;
		if (s1.length==s2.length){
			for (int i = 0; i < s1.length; i++){
				ItemStack i1 = s1[i];
				ItemStack i2 = s2[i];
				if (i1!=null){
					if (i2==null){
						match=false;
					} else {
						if (!(i1.getType()==i2.getType())) match=false;
						if (!(i1.getAmount()==i2.getAmount())) match=false;
						//if (!(i1.getData().getData()==i2.getData().getData())) match=false;
						ItemMeta im1 = i1.getItemMeta();
						ItemMeta im2 = i2.getItemMeta();
						if (im1!=null){
							if (im2==null) match=false;
							if (im1.getDisplayName()!=null){
								if (im2.getDisplayName()==null) match=false;
								if (!(im1.getDisplayName().equalsIgnoreCase(im2.getDisplayName()))) match = false;
							} else {
								if (im2.getDisplayName()!=null) match=false;
							}
						} else {
							if (im2!=null) match=false;
						}
						
					}
				} else {
					if (i2!=null) match=false;
				}
			}
		} else {
			match=false;
		}
		return match;
	}
	
	@Override
	public void onDisable(){
		this.getServer().clearRecipes();
	}
}
