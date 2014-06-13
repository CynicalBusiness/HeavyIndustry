package me.capit.HeavyIndustry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.inventory.ItemStack;

public class FactoryRecipe {
	
	private HashMap<Character, ItemStack> data = new HashMap<Character, ItemStack>();
	private char[] rrout = new char[27];
	private char[] rrin = new char[27];
	private final String name;
	private final long time;
	
	public FactoryRecipe(String name, long time){
		this.name = name;
		this.time = time;
	}
	
	public void setInput(String r1, String r2, String r3) throws IllegalArgumentException{
		if (r1.length()==9 && r2.length()==9 && r3.length()==9){
			char[] r1c = r1.toCharArray();
			char[] r2c = r2.toCharArray();
			char[] r3c = r3.toCharArray();
			System.arraycopy(r1c, 0, rrin, 0, 9);
			System.arraycopy(r2c, 0, rrin, 9, 9);
			System.arraycopy(r3c, 0, rrin, 18, 9);
			for (char c : rrin){
				if (!data.containsKey(c)){
					data.put(c, null);
				}
			}
			
		} else {
			throw new IllegalArgumentException("Rows were not of correct size of 9.");
		}
	}
	
	public void setOutput(String r1, String r2, String r3) throws IllegalArgumentException{
		if (r1.length()==9 && r2.length()==9 && r3.length()==9){
			char[] r1c = r1.toCharArray();
			char[] r2c = r2.toCharArray();
			char[] r3c = r3.toCharArray();
			System.arraycopy(r1c, 0, rrout, 0, 9);
			System.arraycopy(r2c, 0, rrout, 9, 9);
			System.arraycopy(r3c, 0, rrout, 18, 9);
			for (char c : rrout){
				if (!data.containsKey(c)){
					data.put(c, null);
				}
			}
			
		} else {
			throw new IllegalArgumentException("Rows were not of correct size of 9.");
		}
	}
	
	public void assignChar(char c, ItemStack stack){
		if (data.containsKey(c)){
			data.put(c, stack);
		}
	}
	
	public ItemStack[] getInputAsArray(){
		List<ItemStack> list = new ArrayList<ItemStack>();
		for (char c : rrin){
			if (c==' '){
				list.add(null);
			} else if (data.containsKey(c)){
				list.add(data.get(c));
			} else {
				list.add(null);
			}
		}
		return list.toArray(new ItemStack[0]);
	}
	
	public ItemStack[] getOutputAsArray(){
		List<ItemStack> list = new ArrayList<ItemStack>();
		for (char c : rrout){
			if (c==' '){
				list.add(null);
			} else if (data.containsKey(c)){
				list.add(data.get(c));
			} else {
				list.add(null);
			}
		}
		return list.toArray(new ItemStack[0]);
	}

	public String getName() {
		return name;
	}

	public long getTime() {
		return time;
	}
	
}
