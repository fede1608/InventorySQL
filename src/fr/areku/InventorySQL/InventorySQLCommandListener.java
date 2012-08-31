package fr.areku.InventorySQL;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;

import fr.areku.InventorySQL.database.CoreSQLItem;

public class InventorySQLCommandListener implements CommandExecutor {
	public InventorySQL plugin;

	public InventorySQLCommandListener(InventorySQL plugin) {
		this.plugin = plugin;
	}

	public boolean onCommand(CommandSender cs, Command cmnd, String label,
			String[] args) {
		boolean isNotPlayer = true;

		if (cs instanceof Player) {
			isNotPlayer = false;
		}

		if ("invsql".equals(label)
				&& ((args.length == 0) || (args.length == 1 && "help"
						.equals(args[0])))) {
			sendMessage(cs, ChatColor.GREEN + "Usage :");
			sendMessage(cs, ChatColor.GREEN
					+ " * /invSQL check : update yourself (alias: /ichk)");
			if (cs.isOp()) {
				sendMessage(cs, ChatColor.GREEN
						+ " * /invSQL check all : update all players");
				sendMessage(
						cs,
						ChatColor.GREEN
								+ " * /invSQL check <player>, <player>, <player>, .. : update specified players");
				sendMessage(cs, ChatColor.GREEN
						+ " * /invSQL reload : reload config");
			}
			return true;
		}

		if (!this.plugin.ready) {
			sendMessage(cs, ChatColor.RED
					+ "Error in config, please check and use /invsql reload");
			return true;
		} else if ("ichk".equals(label)) {
			if (isNotPlayer) {
				sendMessage(cs, ChatColor.RED
						+ "You cannot check yourself as a Console !");
			} else {
				sendMessage(
						cs,
						ChatColor.GREEN
								+ InventorySQL.getMessage("check-yourself"));
				InventorySQL
						.getCoreSQLProcess()
						.runCheckThisTask(
								new CoreSQLItem(new Player[] { (Player) cs })
										.setCommandSender(cs),
								"Command", true, 0);
			}
			return true;
		}
		if ("invsql".equals(label)) {
			if (cs.isOp() && isNotPlayer
					&& (args.length == 1 && "reload".equals(args[0]))) {
				sendMessage(cs, ChatColor.YELLOW + "Reloading InventorySQL");
				this.plugin.reload();
				return true;
			}
			if ("pw".equals(args[0]) && (!isNotPlayer)) {
				if (args.length != 2) {
					sendMessage(cs, ChatColor.GREEN + "Usage :");
					sendMessage(cs, ChatColor.GREEN
							+ " * /invSQL pw <password> : change your password");
					return true;
				}
				if (InventorySQL.getCoreSQLProcess().updatePlayerPassword(
						cs.getName(), combine(args, " "))) {
					sendMessage(cs, ChatColor.BLUE + "Password changed");
				} else {
					sendMessage(cs, ChatColor.RED + "Unable to change password");
				}
			}
			if ("check".equals(args[0])) {
				if (args.length >= 2) {
					if (!cs.isOp()) {
						sendMessage(cs, ChatColor.RED
								+ "You cannot use this command");
						return true;
					}
					if ("all".equals(args[1])) {
						sendMessage(
								cs,
								ChatColor.GREEN
										+ InventorySQL
												.getMessage("check-all-players"));
						InventorySQL.getCoreSQLProcess().runCheckAllTask(0);
						return true;
					}
					Player pT;
					List<Player> p = new ArrayList<Player>();

					for (int i = 1; i < args.length; i++) {
						pT = Bukkit.getPlayerExact(args[i]);
						if (pT != null) {
							if (!p.contains(pT))
								p.add(pT);
						}
					}
					if (p.size() > 0) {
						sendMessage(
								cs,
								ChatColor.GREEN
										+ InventorySQL.getMessage(
												"check-n-players", p.size()));
						InventorySQL
								.getCoreSQLProcess()
								.runCheckThisTask(
										new CoreSQLItem(p
												.toArray(new Player[] {}))
												.setCommandSender(cs),
										"Command", true, 0);
					} else {
						sendMessage(
								cs,
								ChatColor.GREEN
										+ InventorySQL.getMessage("no-online"));
					}
				} else {
					if (isNotPlayer) {
						sendMessage(cs, ChatColor.RED
								+ "You cannot check yourself as a Console !");
					} else {
						sendMessage(
								cs,
								ChatColor.GREEN
										+ InventorySQL
												.getMessage("check-yourself"));
						InventorySQL
								.getCoreSQLProcess()
								.runCheckThisTask(
										new CoreSQLItem(
												new Player[] { (Player) cs })
												.setCommandSender(cs),
										"Command", true, 0);
					}
				}
			}

			if ("backup".equals(args[0]) & (args.length == 2)) {
				if (!cs.isOp()) {
					sendMessage(cs, ChatColor.RED
							+ "You cannot use this command");
					return true;
				}
				if ("clean".equals(args[1])) {
					sendMessage(cs, ChatColor.GREEN + "Cleaning backup..");
					InventorySQL.getCoreSQLProcess().runBackupClean();
				}

			}

			if ("showidlist".equals(args[0]) & (Config.debug)) {
				// debug code to pring pretty-formated ids
				// used to update the webui
				if (Config.debug) {
					for (Material m : Material.values()) {
						System.out.println("$items[" + m.getId() + "] = '"
								+ m.toString() + "';");
					}
					System.out.println("//----------------");
					for (Enchantment e : Enchantment.values()) {
						System.out.println("$ench[" + e.getId()
								+ "] = array('name' => '" + e.getName()
								+ "', 'startlevel' => " + e.getStartLevel()
								+ ", 'maxlevel' => " + e.getMaxLevel() + ");");
					}
				}

			}
		}
		return true;
	}

	public void sendMessage(CommandSender cs, String m) {
		cs.sendMessage("[InventorySQL] " + m);
	}

	public static String combine(String[] s, String glue) {
		int k = s.length;
		if (k == 0)
			return null;
		StringBuilder out = new StringBuilder();
		out.append(s[1]);
		for (int x = 2; x < k; ++x)
			out.append(glue).append(s[x]);
		return out.toString();
	}

}