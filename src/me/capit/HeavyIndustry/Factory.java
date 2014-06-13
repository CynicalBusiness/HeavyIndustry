package me.capit.HeavyIndustry;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.block.Chest;
import org.bukkit.block.Furnace;
import org.bukkit.configuration.ConfigurationSection;

public class Factory {
	
	public class UnableToProcessException extends Exception{
		private static final long serialVersionUID = -6334373081575247030L;
		public UnableToProcessException(String msg){
			super(msg);
		}
	}
	
	Location sLocation;
	
	List<Location> furnaces = new ArrayList<Location>();
	List<FactoryRecipe> recipes = new ArrayList<FactoryRecipe>();
	FactoryLayout layout;
	HeavyIndustry plugin;
	String name;
	
	public Factory(HeavyIndustry plugin, ConfigurationSection data){
		this.plugin = plugin;
		this.name = data.getString("name");
	}
	
	public void setLayout(FactoryLayout layout){
		this.layout = layout;
	}
	
	public void process(Location loc, Player p) throws UnableToProcessException{
		if (createable(loc)){
			Inventory inv = ((Chest)loc.getBlock().getState()).getBlockInventory();
			if (parseInventory(inv, loc, p)){
				return;
			} else {
				throw new UnableToProcessException("Materials within chest are not a valid recpie for "+getDisplayName()+"!");
			}
		} else {
			throw new UnableToProcessException("This is not a valid factory!");
		}
	}
	
	protected void addRecipe(FactoryRecipe recipe){
		recipes.add(recipe);
	}
	
	public boolean createable(Location loc){
		if (layout!=null){
			if (layout.chestMatchesLayout(loc)){
				furnaces = layout.getFurnaces();
				return true;
			}
		}
		return false;
	}
	
	public String getDisplayName(){
		return name;
	}
	
	
	protected boolean parseInventory(final Inventory inv, final Location loc, Player p) {
		ItemStack[] toutput = null;
		ItemStack[] tinput = null;
		FactoryRecipe re = null;
		for (FactoryRecipe fr : recipes){
			if (plugin.itemArraysMatch(fr.getInputAsArray(), inv.getContents())){
				toutput = fr.getOutputAsArray();
				tinput = fr.getInputAsArray();
				re = fr;
				break;
			}
		}
		if (toutput!=null && tinput!=null){
			final ItemStack[] output = toutput.clone();
			final ItemStack[] input = tinput.clone();
			final long time = re.getTime();
			//plugin.log.info("Factory "+getDisplayName()+" started at "+loc+".");
			p.sendMessage(ChatColor.GREEN + re.getName()+" started for the "+getDisplayName()+".");
			new BukkitRunnable(){
				@Override
				public void run() {
					
					lightFurnaces(time);
					try {
						TimeUnit.SECONDS.sleep(1);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					for (int i = 0; i < time; i++){
						lightFurnaces(time);
						if (createable(loc)){
							if (plugin.itemArraysMatch(input, inv.getContents())){
								try {
									TimeUnit.SECONDS.sleep(1);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							} else {
								System.out.println("Failed contents check");
								extinguishFurnaces();
								return;
							}
						} else {
							System.out.println("Failed 'createable' check.");
							extinguishFurnaces();
							return;
						}
					}
					extinguishFurnaces();
					inv.setContents(output);
					return;
				}
			}.runTaskLaterAsynchronously(plugin, 1L);
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	protected final void lightFurnaces(long time){
		for (Location l : furnaces){
			if (l.getBlock().getType()==Material.FURNACE || l.getBlock().getType()==Material.BURNING_FURNACE){
				Furnace f = (Furnace) l.getBlock().getState();
				byte data = f.getRawData();
				l.getBlock().setType(Material.BURNING_FURNACE);
				l.getBlock().setData(data);
				f.update();
			} else {
				plugin.log.info("Found object "+l.getBlock().getType()+" that wasn't a furnace.");
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	protected final void extinguishFurnaces(){
		for (Location l : furnaces){
			if (l.getBlock().getType()==Material.FURNACE || l.getBlock().getType()==Material.BURNING_FURNACE){
				Furnace f = (Furnace) l.getBlock().getState();
				byte data = f.getRawData();
				l.getBlock().setType(Material.FURNACE);
				l.getBlock().setData(data);
				f.update();
			} else {
				plugin.log.info("Found object "+l.getBlock().getType()+" that wasn't a furnace.");
			}
		}
	}
	
	protected ItemStack[] trimEmptySlots(ItemStack[] stack){
		List<ItemStack> temp = new ArrayList<ItemStack>();
		for (ItemStack is : stack){
			if (is!=null && is.getType()!=Material.AIR){
				temp.add(is);
			}
		}
		return temp.toArray(new ItemStack[0]);
	}
	
}
