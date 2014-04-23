/*
 * If my code looks messy or something like that, it's my code, it's like handwriting.
 * We all code differently  and this is how I code.
 * If there is anything that really could be done better, I welcome suggestions.
 */

package io.github.pure1.mobstreak;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class mobStreak extends JavaPlugin{

	/* This update:
	 *  
	 *  
	 *  
	 * 
	 * 
	 */
	
	/*
	 * TODO 
	 * 
	 * COMMAND TO SET STREAKS INGAME.
	 * 
	 */
	
	//Variables
	public final mobStreakListener psl = new mobStreakListener(this);
	public final Logger logger = Logger.getLogger("Minecraft");
	static File configFile;
	static File streaksFile;
	static File highscoresFile;
	static FileConfiguration config;
	static FileConfiguration streaks;
	static FileConfiguration highscores;
	public static String step;
	public static List<String> commands;
    public static boolean tellkills;
	public static List<String> killables;
	@SuppressWarnings("unchecked")
	@Override
	//when plugin is enabled
	public void onEnable() {

		//config stuff thanks to someone who I can't remember.
		configFile = new File(getDataFolder(), "config.yml");
		streaksFile = new File(getDataFolder(), "streaks.yml");
		highscoresFile = new File(getDataFolder(), "highscores.yml");
		try {
			firstRun();
		} catch (Exception e) {
			e.printStackTrace();
		}
		//load configs.
		config = new YamlConfiguration();
		streaks = new YamlConfiguration();
		highscores = new YamlConfiguration();
		loadYamls();
		PluginManager pm = getServer().getPluginManager();
		saveYamls();
		
		pm.registerEvents(this.psl, this);
		//Initiate variables.
		step = config.getString("command-step");
		commands = config.getStringList("commands");
		killables = config.getStringList("mobs");
		tellkills = config.getBoolean("tell-streak");
		if(config.getBoolean("Save-streaks")){
			try{
				List<String> hlist = (List<String>) highscores.getList("Highscores");
				List<String> list = (List<String>) streaks.getList("Streaks");
					if(list != null){
						mobStreakListener.ls = (List<String>) streaks.getList("Streaks");
					}
					if(hlist != null){
						mobStreakListener.hs = (List<String>) highscores.getList("Highscores");
					}
			} catch (Exception e){
				e.printStackTrace();
			}
		}
		try {
		    Metrics metrics = new Metrics(this);
		    metrics.start();
		} catch (IOException e) {
		    // Failed to submit the stats :-(
		}

		this.logger.info("[Mob Streak] Mob Streak Loaded.");
		//[END] config stuff thanks to someone who I can't remember [END].
	}

	@Override
	//when plugin is disabled
	public void onDisable() {
		//save streaks BUT NOT CONFIG.
		streaks.set("Streaks", mobStreakListener.ls);
		highscores.set("Highscores", mobStreakListener.hs);
		saveStreaks();
		saveHs();
		this.logger.info("[Mob Streak] Mob Streak Disabled");
	}
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		//if command is root command.
		if(cmd.getName().equalsIgnoreCase("mobstreak") || cmd.getName().equalsIgnoreCase("ms")){
			if(args.length == 0){
				//print their streak.
				if(sender.hasPermission("mobstreak.mobstreak")){
					String name = sender.getName().toLowerCase();
					List<String> ls = mobStreakListener.ls;
					List<String> hs = mobStreakListener.hs;
					if(mobStreakListener.nameInList(name, ls)){
						int index = mobStreakListener.getNameIndex(name, ls);
						int kills = Integer.parseInt(ls.get(index).replace(name+"-", ""));
						int hindex = mobStreakListener.getNameIndex(name, hs);
						int hkills = Integer.parseInt(hs.get(hindex).replace(name+"-", ""));
						if(kills == 1){
							sender.sendMessage(ChatColor.GREEN + "[Mob Streak] You have a streak of 1 kill, and a highest Streak of " + hkills);
						}else{
							sender.sendMessage(ChatColor.GREEN + "[Mob Streak] You have a streak of " + kills + " kills, and a highest Streak of " + hkills);
						}
					}else{
						sender.sendMessage(ChatColor.GREEN + "[Mob Streak] You have no kills...");
					}
				}else{
					sender.sendMessage(ChatColor.RED + "You do not have permission to do that...");
				}
			}
			if (args.length == 1){
				//Help Information
				if(args[0].equalsIgnoreCase("Help")){
					if(sender.hasPermission("mobstreak.help")){
						sender.sendMessage(ChatColor.GREEN + "[Mob Streak Help]");
						sender.sendMessage(ChatColor.GREEN + "Mob Streak very simply keeps track of kills, kill things, build up a streak.");
						sender.sendMessage(ChatColor.YELLOW + "--Commands--");
						sender.sendMessage(ChatColor.GREEN + "mobstreak" + ChatColor.YELLOW + " -- " + ChatColor.GREEN + "shows your current streak. aliases: ms");
						sender.sendMessage(ChatColor.GREEN + "mobstreak streak <name>" + ChatColor.YELLOW + " -- " + ChatColor.GREEN + "Shows the leaderboard, with an added name it shows only that person.");
						sender.sendMessage(ChatColor.GREEN + "mobstreak hs" + ChatColor.YELLOW + " -- " + ChatColor.GREEN + "Show the highscore board.");
						sender.sendMessage(ChatColor.GREEN + "mobstreak set [name] [amount]" + ChatColor.YELLOW + " -- " + ChatColor.GREEN + "set a players streak.");
						sender.sendMessage(ChatColor.GREEN + "mobstreak clear <name>" + ChatColor.YELLOW + " -- " + ChatColor.GREEN + "clears all/a players streak(s).");
						//sender.sendMessage(ChatColor.GREEN + "mobstreak setcmd [number] [command]" + ChatColor.YELLOW + " -- " + ChatColor.GREEN + "Sets command [number] to [command].");
						sender.sendMessage(ChatColor.GREEN + "mobstreak commands" + ChatColor.YELLOW + " -- " + ChatColor.GREEN + "Prints all commands in config.");
						sender.sendMessage(ChatColor.GREEN + "mobstreak reload" + ChatColor.YELLOW + " -- " + ChatColor.GREEN + "reloads the config.");
						sender.sendMessage(ChatColor.GREEN + "mobstreak help" + ChatColor.YELLOW + " -- " + ChatColor.GREEN + "shows this.");
					}else{
						sender.sendMessage(ChatColor.RED + "You do not have permission to do that...");
					}
				}
				if(args[0].equalsIgnoreCase("reload")){
					//Reload Stuff
					if(sender.hasPermission("mobstreak.reload")){
						sender.sendMessage(ChatColor.GREEN + "[Mob Streak] Reloading Config...");
						configFile = new File(getDataFolder(), "config.yml");
						try {
							firstRun();
						} catch (Exception e) {
							e.printStackTrace();
						}
						config = new YamlConfiguration();
						loadYamls();
						saveYamls();
						step = config.getString("command-step");
						commands = config.getStringList("commands");
						killables = config.getStringList("mobs");
						tellkills = config.getBoolean("tell-streak");
						sender.sendMessage(ChatColor.GREEN + "[Mob Streak] Mob Streak config reloaded.");
						this.logger.info("[Mob Streak] Mob Streak config reloaded.");
					}else{
						sender.sendMessage(ChatColor.RED + "You do not have permission to do that...");
					}
				}
				if(args[0].equalsIgnoreCase("streak")){

					//LeaderBoard stuff
					if(sender.hasPermission("mobstreak.streak")){
						List<String> ls = OrderList(mobStreakListener.ls);
						Iterator<String> itr = ls.iterator();
						sender.sendMessage(ChatColor.YELLOW + "============= Mob Streak Leaderboard ================");
						int i = 1;
						while(itr.hasNext()) {
							String itrNext = itr.next();
							String name = itrNext.split("-")[0];
							String kills = itrNext.split("-")[1];
							sender.sendMessage(ChatColor.GREEN + "    " + i + ". " + name +" : " + ChatColor.GREEN + kills + " kills.");
							i++;
						}
						sender.sendMessage(ChatColor.YELLOW + "==================================================");

					}else{
						sender.sendMessage(ChatColor.RED + "You do not have permission to do that...");
					}
				}
				if(args[0].equalsIgnoreCase("highscore") || args[0].equalsIgnoreCase("hs")){

					//LeaderBoard stuff
					if(sender.hasPermission("mobstreak.highscore")){
						List<String> ls = OrderList(mobStreakListener.hs);
						Iterator<String> itr = ls.iterator();
						sender.sendMessage(ChatColor.YELLOW + "============= Mob Streak Highscore Board ============");
						int i = 1;
						while(itr.hasNext()) {
							String itrNext = itr.next();
							String name = itrNext.split("-")[0];
							String kills = itrNext.split("-")[1];
							sender.sendMessage(ChatColor.GREEN + "    " + i + ". " + name +" : " + ChatColor.GREEN + kills +" kills.");
							i++;
						}
						sender.sendMessage(ChatColor.YELLOW + "==================================================");

					}else{
						sender.sendMessage(ChatColor.RED + "You do not have permission to do that...");
					}
				}
				if(args[0].equalsIgnoreCase("clear")){
					// clear a players streak.
					if(sender.hasPermission("mobstreak.clear")){
						List<String> lst = mobStreakListener.ls;
						List<String> hlst = mobStreakListener.hs;
						int sze = lst.size();
						int hsze = hlst.size();
						int index = 1;
						while(index <= sze){
							mobStreakListener.ls.remove(0);
							index = index + 1;
						}
						index = 1;
						while(index <= hsze){
							mobStreakListener.hs.remove(0);
							index = index + 1;
						}
						sender.sendMessage(ChatColor.GREEN + "[Mob Streak] Streaks cleared");
					}else{
						sender.sendMessage(ChatColor.RED + "You do not have permission to do that...");
					}
				}
				if(args[0].equalsIgnoreCase("commands")){
					//List all commands
					if(sender.hasPermission("mobstreak.commands")){
						List<String> lst = config.getStringList("commands");
						int sze = lst.size();
						int index = 0;
						sender.sendMessage(ChatColor.GREEN + "[Mob Streak Commands]");
						while(index < sze){
							sender.sendMessage(ChatColor.GREEN + "" + (index + 1) + ": " + lst.get(index));
							
							index = index + 1;
						}
					}else{
						sender.sendMessage(ChatColor.RED + "You do not have permission to do that...");
					}
				}
			}
			if (args.length == 2){
				//someones streak
				if(args[0].equalsIgnoreCase("streak")){
					if(sender.hasPermission("mobstreak.streak")){
						String name = args[1];
						List<String> ls = mobStreakListener.ls;
						Iterator<String> itr = ls.iterator();
						while(itr.hasNext()){
							String itrNext = itr.next();
							if(itrNext.contains(name)){
								String kills = itrNext.replace(name+"-", "");
								sender.sendMessage(ChatColor.YELLOW + "=====================================================");
								sender.sendMessage(ChatColor.YELLOW + "=============="+ ChatColor.GREEN + "   Mob Streak Leaderboard   "+ChatColor.YELLOW + "==============");
								sender.sendMessage(ChatColor.YELLOW + "=====================================================");
								sender.sendMessage(ChatColor.YELLOW + "|" + ChatColor.GREEN +"                  Name                  " + ChatColor.YELLOW + "|" + ChatColor.GREEN +"               Streak             " + ChatColor.YELLOW + "|");
								sender.sendMessage(ChatColor.YELLOW + "=====================================================");
								int spaces = 16 - name.length();
								int i = 1;
								String space = "";
								while(i <= spaces){
									space = space + " ";
									i = i + 1;
								}
								name = name + space;
								
								spaces = 16 - kills.length();
								i = 1;
								space = "";
								while(i <= spaces){
									space = space + " ";
									i = i + 1;
								}
								kills = kills + space;
								sender.sendMessage(ChatColor.GREEN + "  " + name +"                          " + ChatColor.GREEN + kills);
								sender.sendMessage(ChatColor.YELLOW + "=====================================================");
							}
						}
					}else{
						sender.sendMessage(ChatColor.RED + "You do not have permission to do that...");
					}
				}
				else if(args[0].equalsIgnoreCase("clear")){
					if(sender.hasPermission("mobstreak.clear")){
						String name = args[1];
						List<String> ls = mobStreakListener.ls;
						List<String> hs = mobStreakListener.hs;
						if(mobStreakListener.nameInList(name, ls)){
							int index = mobStreakListener.getNameIndex(name, ls);
							ls.remove(index);
							sender.sendMessage(ChatColor.GREEN + "[Mob Streak] " + name + "'s streak has been cleared...");
						}
						if(mobStreakListener.nameInList(name, hs)){
							int index = mobStreakListener.getNameIndex(name, hs);
							ls.remove(index);
						}
					}else{
						sender.sendMessage(ChatColor.RED + "You do not have permission to do that...");
					}
				}
			
			}
			if(args.length >= 3){
				/*if(args[0].equalsIgnoreCase("setcmd")){
					if(sender.hasPermission("mobstreak.setcmd")){
						//try{
							List<String> ls = config.getStringList("commands");
							int commandN = ls.size()+1;
							
							try{
								commandN = Integer.parseInt(args[1]);
								String command = null;
								if(args.length == 3){
									command = args[2];
								}else{
									int i = 1;
									int n = args.length;
									
									while(i <= (n - 2)){
										if (command == null){
											command = args[2];
										}else{
											command = command + " " + args[i + 1];
										}
										i=i+1;
									}
									//        0     1    2    3      4
									// ms setcmd 4 give &name 235
									// 1:5 give
									// 2:5 &name
									// 3:5 235
									// 4:5
									// 5:5
								}
								loadYamls();
								int l = ls.size();
								if(commandN <= l){
									ls.set(commandN -1, command);
									config.set("commands", ls);
									sender.sendMessage(ChatColor.GREEN + "[Mob Streak] Command " + commandN  + " has been replaced...");
								}else{
									ls.add(commandN - 1, command);
									sender.sendMessage(ChatColor.GREEN + "[Mob Streak] Command added");
									config.set("commands", ls);
								}
							}catch(Exception e){
								String command = null;
								if(args.length == 3){
									command = args[1];
								}else{
									int i = 1;
									int n = args.length;
									
									while(i <= (n - 1)){
										if (command == null){
											command = args[1];
										}else{
											command = command + " " + args[i];
										}
										i=i+1;
									}
									//        0     1    2    3      4
									// ms setcmd 4 give &name 235
									// 1:5 give
									// 2:5 &name
									// 3:5 235
									// 4:5
									// 5:5
								}
								loadYamls();
								int l = ls.size();
								if(commandN <= l){
									ls.set(commandN -1, command);
									config.set("commands", ls);
									sender.sendMessage(ChatColor.GREEN + "[Mob Streak] Command " + commandN  + " has been replaced...");
								}else{
									ls.add(commandN - 1, command);
									sender.sendMessage(ChatColor.GREEN + "[Mob Streak] Command added");
									config.set("commands", ls);
								}
							}
							
						//}catch(Exception e){}
						saveYamls();
						loadYamls();
						commands = config.getStringList("commands");
					}else{
						sender.sendMessage(ChatColor.RED + "You do not have permission to do that...");
					}
			}*/
				//set a players streak.
				if(args[0].equalsIgnoreCase("set")){
					if(sender.hasPermission("mobstreak.set")){
						String name = args[1];
						int kills = Integer.parseInt(args[2]);
						List<String> ls = mobStreakListener.ls;
						if(kills >= 0){
							if(mobStreakListener.nameInList(name, ls)){
								int index = mobStreakListener.getNameIndex(name, ls);
								ls.set(index, name + "-" + kills);
							}else{
								ls.add(name + "-" + kills);
							}
							sender.sendMessage(ChatColor.GREEN + "[Mob Streak] " + name + "'s streak has been set to " + args[2]);
						}else{
							sender.sendMessage(ChatColor.GREEN + "[Mob Streak] Error: Negative numbers are not accepted.");

						}
												
					}else{
						sender.sendMessage(ChatColor.RED + "You do not have permission to do that...");
					}
				}
			}
		}
		return false;
	}
	private List<String> OrderList(List<String> ls) {
		List<String>names = new ArrayList<String>();
		List<String>scores = new ArrayList<String>();
		
		Integer[] scrs = new Integer[ls.size()];
		
		for(int i = 0; i < scrs.length; i++){
			String name = ls.get(i).split("-")[0];
			String score = ls.get(i).split("-")[1];
		
			scrs[i] = Integer.parseInt(score);
			names.add(name);
			scores.add(score);	
		}
		Arrays.sort(scrs, Collections.reverseOrder());

		List<String>nls = new ArrayList<String>();
		List<String>added = new ArrayList<String>();
		for (int i = 0; i < scrs.length; i++){
			for(int j = 0; j < scrs.length; j++){
				if(scrs[i] == Integer.parseInt(scores.get(j))){
					if(!added.contains(names.get(j))){
						added.add(names.get(j));
						nls.add(names.get(j)+"-"+scores.get(j));
					}
				}
			}
		}
		return nls;
	}

	//config stuff thanks to someone who I can't remember.
	private void firstRun() throws Exception {
		if (!configFile.exists()) {
			configFile.getParentFile().mkdirs();
			copy(getResource("config.yml"), configFile);
		}
		if (!streaksFile.exists()) {
			streaksFile.getParentFile().mkdirs();
			copy(getResource("streaks.yml"), streaksFile);
		}	
		if (!highscoresFile.exists()) {
			highscoresFile.getParentFile().mkdirs();
			copy(getResource("highscores.yml"), highscoresFile);
		}	
	}
	private void copy(InputStream in, File file) {
		try {
			OutputStream out = new FileOutputStream(file);
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			out.close();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void loadYamls() {
		try {
			config.load(configFile);
			streaks.load(streaksFile);
			highscores.load(highscoresFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void saveYamls() {
		try {
			config.save(configFile);
			streaks.save(streaksFile);
			highscores.save(highscoresFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	//[END] config stuff thanks to someone who I can't remember [END].
	public static void saveStreaks() {
		try {
			streaks.save(streaksFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static void saveHs() {
		try {
			highscores.save(highscoresFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
