package com.dikahdoff.resourceswitch;

import com.dikahdoff.resourceswitch.reference.Reference;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import com.dikahdoff.resourceswitch.WorldLoadListener;

@Mod(modid = Reference.MODID, name = Reference.NAME, version = Reference.VERSION)
public class ResSwitch
{
    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        System.out.println("ResourceSwitcher started the preInit process...");
    }
    
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        System.out.println("ResourceSwitcher started the init process...");
        MinecraftForge.EVENT_BUS.register(new com.dikahdoff.resourceswitch.WorldLoadListener());
    }
    
    @EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        System.out.println("ResourceSwitcher started the postInit process...");
    }
}
