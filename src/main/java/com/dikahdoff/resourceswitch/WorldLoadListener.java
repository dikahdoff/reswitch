package com.dikahdoff.resourceswitch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.*;

import org.apache.logging.log4j.Level;

import com.dikahdoff.resourceswitch.utils.LogHelper;
import com.sun.javafx.geom.Vec3d;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.ResourcePackRepository;
import net.minecraft.client.resources.ResourcePackRepository.Entry;
import net.minecraft.command.CommandBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class WorldLoadListener {
	public static String packName = "DwarvenMinesPack (DON'T RENAME ME)";
	public static String packURL = "https://pastebin.com/raw/jskED8Wt";
	public static String gameDir = Minecraft.getMinecraft().mcDataDir.getAbsolutePath() + "/";
	
	public boolean testedsky = false;
	public boolean skyblock = false;
	public boolean dwarven = false;
	int i = 0;
	public EntityPlayer player;
	
	@SubscribeEvent
	public void onWorldLoad(WorldEvent.Load event) {
		testedsky = false;
		skyblock = false;
		dwarven = false;
		unloadResource();
	}
	
	@SubscribeEvent
	public void onPlayerTick(TickEvent.PlayerTickEvent event) {
		ItemStack item = event.player.inventory.mainInventory[8];
		if(!testedsky) {
			if(item != null) {
				testedsky = true;
				player = event.player;
				if(item.getDisplayName().contains("SkyBlock Menu")) {
					player.addChatMessage(new ChatComponentText(EnumChatFormatting.GRAY + "You are in a SkyBlock Server."));
					skyblock = true;
					LogHelper.log(Level.INFO, "YOU_ARE_IN_SKYBLOCK");
				}
			} else {
				i++;
				if(i > 120) {
					testedsky = true;
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onEntity(RenderLivingEvent.Post event) {
		if(skyblock && !dwarven && event.entity.getName().contains("Royal Guard")) {
			player.addChatMessage(new ChatComponentText(EnumChatFormatting.AQUA + "You are in the Dwarven Mines"));
			dwarven = true;
			loadResource();
		}
	}
	
	public void loadResource() {
		LogHelper.log(Level.INFO, "Loading resourcepack...");
		ResourcePackRepository repo = Minecraft.getMinecraft().getResourcePackRepository();
		List<ResourcePackRepository.Entry> rpEnabled = repo.getRepositoryEntries();
		ResourcePackRepository.Entry pack = null;
		for(ResourcePackRepository.Entry rp : repo.getRepositoryEntriesAll()){
			if(rp.getResourcePackName().equals(packName + ".zip")){
				pack = rp;
				break;
			}
		}
		boolean canContinue = false;
		int i = 0;
		while(!canContinue) {
			if (pack==null){
				LogHelper.log(Level.ERROR, "ERROR: Resource pack not found.");
				String resourcePackURL = null;
				try {
					URL url = new URL(packURL);
					URLConnection connection = url.openConnection();
					BufferedReader reader = new BufferedReader(new InputStreamReader(
						connection.getInputStream()));
					String line = null, data = "";
					while ((line = reader.readLine()) != null) {
						data += line + "\n";
					}
					int j = 0;
					for (String retval: data.split("|")) {
						if(j == 0) {
							// Automatic resourcepack update feature, will be added in the future.
						} else {
							resourcePackURL = retval;
						}
				        j++;
				    }
				} catch(Exception ex) {
					LogHelper.log(Level.ERROR, "ERROR: Couldn't get resourcepack URL. " + ex);
				}
				if(resourcePackURL != null) {
					LogHelper.log(Level.INFO, "Got URL: " + resourcePackURL);
					try {
						download(resourcePackURL, gameDir + "resourcepacks/" + packName + ".zip");
					} catch (Exception e) {
						LogHelper.log(Level.FATAL, "Couldn't download resourcepack." + e);
					}
				}
				if(i > 4)  {
					canContinue = true;
					LogHelper.log(Level.FATAL, "ERROR: Couldn't get resourcepack.");
				}
				i++;
			}
			else{
				LogHelper.log(Level.INFO, "Resource pack found... enabling...");
				List<ResourcePackRepository.Entry> newlist = new ArrayList<ResourcePackRepository.Entry>();
				newlist.addAll(rpEnabled);
				if (!newlist.contains(pack)){
					LogHelper.log(Level.WARN, "Crash Warning: Enabled the resource pack. If this is the first time the game has been run to generate the options.txt file, it will crash. This crash can be safely ignored afterwards, just restart the game and it should be fine.");
					newlist.add(pack);
					repo.setRepositories(newlist);
					repo.updateRepositoryEntriesAll();
					Minecraft.getMinecraft().refreshResources();
				}
				canContinue = true;
			}
		}
	}
	
	public void unloadResource() {
		LogHelper.log(Level.INFO, "Unloading resourcepack if loaded...");
		ResourcePackRepository repo = Minecraft.getMinecraft().getResourcePackRepository();
		List<ResourcePackRepository.Entry> rpEnabled = repo.getRepositoryEntries();
		ResourcePackRepository.Entry pack = null;
		for(ResourcePackRepository.Entry rp : rpEnabled){
			if(rp.getResourcePackName().equals(packName + ".zip")){
				pack = rp;
				break;
			}
		}
		if (pack==null){
			LogHelper.log(Level.INFO, "Resource pack not loaded.");
		} else {
			LogHelper.log(Level.INFO, "Resource pack IS LOADED. Unloading...");
			List<ResourcePackRepository.Entry> newlist = new ArrayList<ResourcePackRepository.Entry>();
			newlist.addAll(rpEnabled);
			if (newlist.contains(pack)){
				newlist.remove(pack);
				repo.setRepositories(newlist);
				repo.updateRepositoryEntriesAll();
				Minecraft.getMinecraft().refreshResources();
			}
		}
	}
	
	public void download(String url, String localFile) throws Exception {
		LogHelper.log(Level.INFO, "Downloading " + localFile + "...");
		ReadableByteChannel in = Channels.newChannel(new URL(url).openStream());
		FileOutputStream fos = new FileOutputStream(localFile);
		FileChannel channel = fos.getChannel();
		channel.transferFrom(in, 0, Long.MAX_VALUE);
		channel.close();
		fos.close();
		LogHelper.log(Level.INFO, "Download " + localFile + " completed.");
	}
}
