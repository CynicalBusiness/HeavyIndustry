package me.capit.HeavyIndustry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.material.Chest;

public class FactoryLayout {
	
	private List<String> row1=null,row2=null,row3=null;
	private Map<Character, Material> binds = new HashMap<Character, Material>();
	private List<Location> furnaces = new ArrayList<Location>();
	
	Location chestLoc, behindLoc;
	
	public FactoryLayout(){
		binds.put('@', Material.CHEST);
		binds.put(' ', Material.AIR);
		binds.put('*', Material.NETHER_STAR);
	}
	
	public Location getChestLocation(){return chestLoc;}
	public Location getBehindLocation(){return behindLoc;}
	public List<Location> getFurnaces(){return furnaces;}
	
	public void addRow(List<String> strings, int row){
		row1 = row==1?strings:row1;
		row2 = row==2?strings:row2;
		row3 = row==3?strings:row3;
	}
	
	public void addBind(char symbol, Material mat){
		if (!binds.containsKey(symbol)){
			binds.put(symbol, mat);
		}
	}
	
	@SuppressWarnings("deprecation")
	public boolean chestMatchesLayout(Location loc){
		if (loc.getBlock().getType()==Material.CHEST){
			Block chest = loc.getBlock();
			Chest c = new Chest(0, chest.getData());
			BlockFace facing = c.getFacing();
			Block behindChest = chest.getRelative(facing.getOppositeFace());
			chestLoc = chest.getLocation();
			behindLoc = behindChest.getLocation();
			int x = 0,z = 0;
			switch(facing.getOppositeFace()){
			case EAST:
				z=1;
				break;
			case NORTH:
				x=1;
				break;
			case SOUTH:
				x=-1;
				break;
			case WEST:
				z=-1;
				break;
			default:
				break;
			}
			if (row1.get(0).charAt(2)!='@'){System.out.println("char '@' failed"); return false;}
			furnaces = new ArrayList<Location>();
			for (int h=0; h<3; h++){
				Block right = chest.getLocation().add(x, h, z).getBlock();
				Block right2 = chest.getLocation().add(x*2, h, z*2).getBlock();
				Block left = chest.getLocation().add(0-x, h, 0-z).getBlock();
				Block left2 = chest.getLocation().add(0-(x*2), h, 0-(z*2)).getBlock();
				Block behind = behindChest.getLocation().add(0,h,0).getBlock();
				Block bright = behind.getLocation().add(x, 0, z).getBlock();
				Block bright2 = behind.getLocation().add(x*2, 0, z*2).getBlock();
				Block bleft = behind.getLocation().add(0-x, 0, 0-z).getBlock();
				Block bleft2 = behind.getLocation().add(0-(x*2), 0, 0-(z*2)).getBlock();
				
				if (right.getType()==Material.FURNACE || right.getType()==Material.BURNING_FURNACE){furnaces.add(right.getLocation());}
				if (right2.getType()==Material.FURNACE || right2.getType()==Material.BURNING_FURNACE){furnaces.add(right2.getLocation());}
				if (left.getType()==Material.FURNACE || left.getType()==Material.BURNING_FURNACE){furnaces.add(left.getLocation());}
				if (left2.getType()==Material.FURNACE || left2.getType()==Material.BURNING_FURNACE){furnaces.add(left2.getLocation());}
				if (behind.getType()==Material.FURNACE || behind.getType()==Material.BURNING_FURNACE){furnaces.add(behind.getLocation());}
				if (bright.getType()==Material.FURNACE || bright.getType()==Material.BURNING_FURNACE){furnaces.add(bright.getLocation());}
				if (bright2.getType()==Material.FURNACE || bright2.getType()==Material.BURNING_FURNACE){furnaces.add(bright2.getLocation());}
				if (bleft.getType()==Material.FURNACE || bleft.getType()==Material.BURNING_FURNACE){furnaces.add(bleft.getLocation());}
				if (bleft2.getType()==Material.FURNACE || bleft2.getType()==Material.BURNING_FURNACE){furnaces.add(bleft2.getLocation());}
				
				List<String> row = h==0 ? row1 : (h==1 ? row2 : row3);
				Material right2M = binds.get(row.get(0).charAt(0));
				Material right1M = binds.get(row.get(0).charAt(1));
				Material left1M = binds.get(row.get(0).charAt(3));
				Material left2M = binds.get(row.get(0).charAt(4));
				Material behindM = binds.get(row.get(1).charAt(2));
				Material right2MB = binds.get(row.get(1).charAt(0));
				Material right1MB = binds.get(row.get(1).charAt(1));
				Material left1MB = binds.get(row.get(1).charAt(3));
				Material left2MB = binds.get(row.get(1).charAt(4));
				
				if (!checkMaterialAgainstBlock(right2M, right2)){return false;}
				if (!checkMaterialAgainstBlock(right1M, right)){return false;}
				if (!checkMaterialAgainstBlock(left1M, left)){return false;}
				if (!checkMaterialAgainstBlock(left2M, left2)){return false;}
				if (!checkMaterialAgainstBlock(behindM, behind)){return false;}
				if (!checkMaterialAgainstBlock(right2MB, bright2)){return false;}
				if (!checkMaterialAgainstBlock(right1MB, bright)){return false;}
				if (!checkMaterialAgainstBlock(left1MB, bleft)){return false;}
				if (!checkMaterialAgainstBlock(left2MB, bleft2)){return false;}
			}
		} else {
			return false;
		}
		
		return true;
	}
	
	public boolean checkMaterialAgainstBlock(Material mat, Block b){
		if (mat != Material.NETHER_STAR){
			if (mat == Material.FURNACE){
				if (b.getType()!=Material.FURNACE && b.getType()!=Material.BURNING_FURNACE){
					System.out.println("Failed furnace check at "+b.toString());
					return false;
				}
			} else {
				if (mat != b.getType()){
					System.out.println("Failed check at "+b.toString());
					return false;
				}
			}
		}
		return true;
	}
	
}
