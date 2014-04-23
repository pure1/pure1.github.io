package io.github.pure1.mobstreak;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

public class mobStreakListener implements Listener {

	public static List<String> ls = new ArrayList<String>();
	public static List<String> hs = new ArrayList<String>();
	public static FileConfiguration config = mobStreak.config;
	public mobStreak ms;

	public mobStreakListener(mobStreak instance) {
		ms = instance;
	}

	/**
	 * add a kill to the killers score. requires the players name (String), the
	 * player (Player), and the event (Event).
	 */
	public void addKill(String name, Player p, Event event) {
		if (nameInList(name, ls)) {
			// if the name is in the streak list...

			// index = the index of the player in the list and kills = the
			// streak
			int index = getNameIndex(name, ls);
			int kills = Integer.parseInt(ls.get(index).replace(name + "-", ""));

			// adds one to their streak
			ls.set(index, name + "-" + (kills + 1));

			if (mobStreak.tellkills) {
				// if tellkills is true tell player their streak
				p.sendMessage(ChatColor.GREEN + "" + (kills + 1) + " Kills.");
			}
			// reward the player
			rewardPlayer(p, name, kills, event);
		} else {
			// if name is not in streak adds player to the streak list with a
			// streak of 1
			ls.add(name.toLowerCase() + "-1");
			if (mobStreak.tellkills) {
				// if tellKills is true tell player they have 1 kill
				p.sendMessage(ChatColor.GREEN + "1 Kill.");
			}
			// reward the player
			rewardPlayer(p, name, 1, event);
		}
		if(nameInList(name, hs)){
			int index = getNameIndex(name, ls);
			int kills = Integer.parseInt(ls.get(index).replace(name + "-", ""));
			int hindex = getNameIndex(name, hs);
			int hkills = Integer.parseInt(hs.get(hindex).replace(name + "-", ""));
			if(kills > hkills){
				hs.set(hindex, name+ "-" +kills);
			}
		}else{
			int index = getNameIndex(name, ls);
			int kills = Integer.parseInt(ls.get(index).replace(name + "-", ""));
			hs.add(name.toLowerCase() + "-" + kills);
		}
	}

	/** work out what streak the player needs, if they're on it, reward them. */
	public void rewardPlayer(Player p, String name, int kills, Event event) {

		// the step and the number of commands(rewards) in the config.
		int step = Integer.parseInt(mobStreak.step);
		int ncommands = mobStreak.commands.size();

		CommandSender console = Bukkit.getConsoleSender();
		int i = 1;

		while (!(i > ncommands)) {
			// could use a for loop here; can't be fucked to re work it
			// set what the number of kills required for the i command.
			int rewardAt = (i * step) - 1;

			if (kills == rewardAt) {
				// if kills is the same as the number required for i command

				// get the command and replace &name with player name and &kills
				// with the player kills.
				String command = mobStreak.commands.get(i - 1)
						.replace("&name", name)
						.replace("&kills", "" + (kills + 1));

				if (command.contains("msd")) {

					// if the command contains msd
					// commands[] = the command split by &cmd (returns the
					// command if &cmd isn't found)
					String[] commands = command.split("&cmd ");

					// length = how many commands there are in commands[]
					int length = commands.length;
					int x = 1;

					// could be done with a for loop
					while (x <= length && length > 0) {

						if (commands[x - 1].startsWith("msd")) {
							// if the command starts with msd
							// the msd command is split via " ", amount
							// initialisation, item id = msd[1]
							String[] msd = commands[x - 1].split(" ");
							int amount = 1;
							int item = Integer.parseInt(msd[1]);

							if (msd.length == 3) {
								// if msd is three long the amount = the second
								// argument
								amount = Integer.parseInt(msd[2]);
							}

							// create stack and add it to the entity killed's
							// drops.
							ItemStack stack = new ItemStack(item, amount);
							((EntityDeathEvent) event).getDrops().add(stack);
						} else {
							// if command isn't msd just dispatch the command as
							// console
							Bukkit.getServer().dispatchCommand(console,
									commands[x - 1]);
						}
						// stupid shit the for loop would handle.
						x++;
					}

				} else {
					// if the command doesn't contain msd, split on &cmd, length
					// = number of commands, x initialisation
					String[] commands = command.split("&cmd ");
					int length = commands.length;
					int x = 1;

					while (x <= length && length > 0) {

						// dispatch command as console
						Bukkit.getServer().dispatchCommand(console,
								commands[x - 1]);

						// shit that the for loop WOULD handle
						x++;
					}
				}
			}
			// more shit a for loop would handle
			i++;
		}

	}

	/** search a list for the index of the String given. Returns its index. */
	@SuppressWarnings("null")
	public static int getNameIndex(String name, List<String> list) {

		// iterator and index initialisation
		Iterator<String> itr = list.iterator();
		int index = 0;

		while (itr.hasNext()) {

			// listN = the next thing in the iterator.
			String listN = itr.next();

			if (listN.toLowerCase().contains(name)) {
				// if listN contains the given string return its index.
				return index;
			} else {
				// else index ++
				index++;
			}

		}
		// if itterator finds shit, return a integer equivalent of null.
		return (Integer) null;
	}

	/** search the list for the String given. If found, returns true. */
	public static boolean nameInList(String name, List<String> list) {
// 		same as getNameIndex, but only returns true if string is found.
		
		Iterator<String> itr = list.iterator();
		while (itr.hasNext()) {
			String listN = itr.next();
			if (listN.toLowerCase().contains(name)) {
				return true;
			}

		}
		return false;
	}

	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		
//		p = player who's died, name = their name
		Player p = event.getEntity();
		String name = p.getName().toLowerCase();
		

		if (!p.hasPermission("mobstreak.noStreak")) {
//			if player has permission to have mobstreak			
			
			if (nameInList(name, ls)) {
//				if the player is in the streak list... index = the their index in the list, kills = their streak
				int index = getNameIndex(name, ls);
				int kills = Integer.parseInt(ls.get(index).replace(name + "-",""));
				
				if (kills == 1) {
//					if they only had one kill
//					tell them the only had 1 kill.
					p.sendMessage(ChatColor.GREEN + "[Mob Streak] you got 1 kill!");
					
					if (mobStreak.config.getBoolean("broadcast-on-death")) {
//						if config option broadcast-on-death is true...
//						send broadcast to everyone saying bobby died with one kill.
						Bukkit.getServer().broadcastMessage(ChatColor.GREEN + "[Mob Streak] " + name + " died with a streak of 1 kill");
					}
//					remove them from the streak list
					ls.remove(index);
					
				} else {
					
//					if they got more than one kill use plural of kill when sending message.
					p.sendMessage(ChatColor.GREEN + "[Mob Streak] you got " + kills + " kills!");
					
					if (mobStreak.config.getBoolean("broadcast-on-death")) {
						Bukkit.getServer().broadcastMessage(ChatColor.GREEN + "[Mob Streak] " + name + " died with a streak of " + kills + " kills.");
					}
//					remove them from the streak list
					ls.remove(index);
				}
			} else {
				
//				if they're not in the streak list (didn't get any kills) 
				p.sendMessage(ChatColor.GREEN + "[Mob Streak] Bad luck, You didn't get a kill");
				
				if (mobStreak.config.getBoolean("broadcast-on-death")) {
//					if config option broadcast-on-death is true...
//					send broadcast to everyone saying bobby died with x kill.
					Bukkit.getServer().broadcastMessage(ChatColor.GREEN + "[Mob Streak] " + name + " died without killing anything, or anyone, worthy of notice...");
				}
			}
		}
//		>>>>>>POSSIBLE BUG<<<<<<
		if (event.getEntity().getKiller() instanceof Player) {
			Player p2 = event.getEntity().getKiller();
			if (!p2.hasPermission("mobstreak.noStreak")) {
				if (killable("player")) {
					String name2 = p2.getName().toLowerCase();
					addKill(name2, p2, event);
				}
			}
		}

	}

	@EventHandler
	public void onEntityDeath(EntityDeathEvent event) {
		Player p = event.getEntity().getKiller();
		String eName = event.getEntity().toString().toLowerCase()
				.replace("craft", "");
		try {
			if (!p.hasPermission("mobstreak.noStreak")) {
				if (event.getEntity().getKiller() instanceof Player) {
					if (killable(eName)) {
						String name = p.getName().toLowerCase();
						p.sendMessage(eName);
						addKill(name, p, event);
					}
				}
			}
		} catch (Exception e) {
		}
	}

	/** searches the killables list for the given entities name */
	private boolean killable(String eName) {
		List<String> killables = mobStreak.killables;
		for(int i = 0; i < killables.size(); i++){
			if(killables.get(i).equalsIgnoreCase(eName)){
				return true;
			}
		}
		return false;
	}
}