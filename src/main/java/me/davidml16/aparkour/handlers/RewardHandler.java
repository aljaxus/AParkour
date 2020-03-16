package me.davidml16.aparkour.handlers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import me.davidml16.aparkour.Main;
import me.davidml16.aparkour.data.RewardData;
import me.davidml16.aparkour.managers.ColorManager;

public class RewardHandler {

	private List<RewardData> rewards;
	private File rewardFile;
	private FileConfiguration rewardConfig;

	public RewardHandler() {
		this.rewards = new ArrayList<RewardData>();
		this.rewardFile = new File(Main.getInstance().getDataFolder() + "/rewards.yml");

		if (!rewardFile.exists()) {
			Main.getInstance().saveResource("rewards.yml", false);
		}

		this.rewardConfig = YamlConfiguration.loadConfiguration(rewardFile);
	}

	public List<RewardData> getRewards() {
		return rewards;
	}

	public File getRewardFile() {
		return rewardFile;
	}

	public FileConfiguration getRewardConfig() {
		return rewardConfig;
	}

	public void saveConfig() {
		try {
			if (!rewardConfig.contains("rewards"))
				rewardConfig.createSection("rewards");

			rewardConfig.save(rewardFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void loadRewards() {
		rewards.clear();
		Main.log.sendMessage(ColorManager.translate("  &eLoading rewards:"));
		for (String id : rewardConfig.getConfigurationSection("rewards").getKeys(false)) {
			if (validRewardData(id)) {
				String permission = rewardConfig.getString("rewards." + id + ".permission");
				String command = rewardConfig.getString("rewards." + id + ".command");

				rewards.add(new RewardData(permission, command));
			}
		}
		Main.log.sendMessage(ColorManager.translate("    " + (rewards.size() > 0 ? "&a" : "&c") + rewards.size() + " rewards loaded!"));
		Main.log.sendMessage(ColorManager.translate(""));
	}

	public boolean validRewardData(String id) {
		return rewardConfig.contains("rewards." + id + ".permission")
				&& rewardConfig.contains("rewards." + id + ".command");
	}

	public void giveRewards(Player p) {
		for (RewardData reward : rewards) {
			if(!reward.getPermission().equalsIgnoreCase("*")) {
				if (p.hasPermission(reward.getPermission()) || p.isOp()) {
					Main.getInstance().getServer().dispatchCommand(Main.getInstance().getServer().getConsoleSender(), reward.getCommand().replaceAll("%player%", p.getName()));
				}
			} else {
				Main.getInstance().getServer().dispatchCommand(Main.getInstance().getServer().getConsoleSender(), reward.getCommand().replaceAll("%player%", p.getName()));
			}
		}
	}

}
