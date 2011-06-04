package org.bukkitcontrib.player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import net.minecraft.server.ContainerChest;
import net.minecraft.server.ContainerPlayer;
import net.minecraft.server.ContainerWorkbench;
import net.minecraft.server.Entity;
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.ICrafting;
import net.minecraft.server.IInventory;
import net.minecraft.server.NetServerHandler;
import net.minecraft.server.Packet100OpenWindow;
import net.minecraft.server.TileEntityDispenser;
import net.minecraft.server.TileEntityFurnace;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.PlayerInventory;
import org.bukkitcontrib.ContribNetServerHandler;
import org.bukkitcontrib.event.inventory.InventoryCloseEvent;
import org.bukkitcontrib.event.inventory.InventoryOpenEvent;
import org.bukkitcontrib.inventory.ContribCraftInventory;
import org.bukkitcontrib.inventory.ContribCraftInventoryPlayer;
import org.bukkitcontrib.inventory.ContribCraftingInventory;
import org.bukkitcontrib.inventory.ContribInventory;
import org.bukkitcontrib.keyboard.Keyboard;

@SuppressWarnings("unused")
public class ContribCraftPlayer extends CraftPlayer implements ContribPlayer{
    protected ContribCraftInventoryPlayer inventory;
    protected Keyboard forward = Keyboard.KEY_UNKNOWN;
    protected Keyboard back = Keyboard.KEY_UNKNOWN;
    protected Keyboard left = Keyboard.KEY_UNKNOWN;
    protected Keyboard right = Keyboard.KEY_UNKNOWN;
    protected Keyboard jump = Keyboard.KEY_UNKNOWN;
    protected Keyboard inventoryKey = Keyboard.KEY_UNKNOWN;
    protected Keyboard drop = Keyboard.KEY_UNKNOWN;
    protected Keyboard chat = Keyboard.KEY_UNKNOWN;
    protected Keyboard togglefog = Keyboard.KEY_UNKNOWN;
    protected Keyboard sneak = Keyboard.KEY_UNKNOWN;
    public ContribCraftPlayer(CraftServer server, EntityPlayer entity) {
        super(server, entity);
        this.inventory = new ContribCraftInventoryPlayer(this.getHandle().inventory, new ContribCraftingInventory(((ContainerPlayer)this.getHandle().activeContainer).a, ((ContainerPlayer)this.getHandle().activeContainer).b));
    }

    @Override
    public PlayerInventory getInventory() {
    	if ((!(this.inventory instanceof ContribCraftInventoryPlayer))) {
    		this.inventory = new ContribCraftInventoryPlayer(this.getHandle().inventory, new ContribCraftingInventory(((ContainerPlayer)this.getHandle().activeContainer).a, ((ContainerPlayer)this.getHandle().activeContainer).b));
    	}
    	else if (!((ContribCraftInventoryPlayer)this.inventory).getHandle().equals(this.getHandle().inventory)) {
    		String temp = this.inventory.getName();
    		this.inventory = new ContribCraftInventoryPlayer(this.getHandle().inventory, new ContribCraftingInventory(((ContainerPlayer)this.getHandle().activeContainer).a, ((ContainerPlayer)this.getHandle().activeContainer).b));
    		((ContribCraftInventoryPlayer)this.inventory).setName(temp);
    	}
        return this.inventory;
    }

    public boolean closeActiveWindow() {
        InventoryCloseEvent event = new InventoryCloseEvent(this, getActiveInventory(), getDefaultInventory());
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }
        getHandle().x();
        getNetServerHandler().setActiveInventory(false);
        getNetServerHandler().setActiveInventoryLocation(null);
        return true;
    }

    public boolean openInventoryWindow(Inventory inventory) {
        return openInventoryWindow(inventory, null, false);
    }
    
    public boolean openInventoryWindow(Inventory inventory, Location location) {
        return openInventoryWindow(inventory, location, false);
    }

    public boolean openInventoryWindow(Inventory inventory, Location location, boolean ignoreDistance) {
        InventoryOpenEvent event = new InventoryOpenEvent(this, inventory, this.inventory, location);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }
        getNetServerHandler().setActiveInventory(true);
        getNetServerHandler().setActiveInventoryLocation(location);
        IInventory dialog = ((CraftInventory)event.getInventory()).getInventory();
        if (dialog instanceof TileEntityDispenser) {
            getHandle().a((TileEntityDispenser)dialog);
        }
        else if (dialog instanceof TileEntityFurnace) {
            getHandle().a((TileEntityFurnace)dialog);
        }
        else {
            getHandle().a(dialog);
        }
        return true;
        /*int id;
        if (dialog instanceof TileEntityDispenser) {
            id = 3;
        }
        else if (dialog instanceof TileEntityFurnace) {
            id = 2;
        }
        else {
            id = 0;
        }
        String title = dialog.getName();
        if (inventory instanceof ContribInventory) {
            title = ((ContribInventory)inventory).getTitle();
        }
        
        updateWindowId();
        getNetServerHandler().sendPacket(new Packet100OpenWindow(getActiveWindowId(), id, title, dialog.getSize()));
        getHandle().activeContainer = new ContainerChest(getHandle().inventory, dialog);
        getHandle().activeContainer.f = getActiveWindowId();
        getHandle().activeContainer.a((ICrafting) this);
        return true;*/
    }

    public boolean openWorkbenchWindow(Location location) {
        if (location.getBlock().getType() != Material.WORKBENCH) {
            throw new UnsupportedOperationException("Must be a valid workbench!");
        }
        else {
            ContainerWorkbench temp = new ContainerWorkbench(getHandle().inventory, ((CraftWorld)location.getWorld()).getHandle(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
            IInventory inventory = temp.b;
            InventoryOpenEvent event = new InventoryOpenEvent(this, new ContribCraftInventory(inventory), this.inventory, location);
            Bukkit.getServer().getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return false;
            }
            getNetServerHandler().setActiveInventory(true);
            getNetServerHandler().setActiveInventoryLocation(location);
            getHandle().a(location.getBlockX(), location.getBlockY(), location.getBlockZ());
            return true;
        }
    }
    
    public int getActiveWindowId() {
         Field id;
         try {
             id = EntityPlayer.class.getDeclaredField("bI");
             id.setAccessible(true);
             return (Integer)id.get(getHandle());
         } catch (Exception e) {
             e.printStackTrace();
         }
         return 0;
    }
    
    public void updateWindowId() {
        Method id;
        try {
            id = EntityPlayer.class.getDeclaredMethod("af");
            id.setAccessible(true);
            id.invoke(getHandle());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Inventory getActiveInventory() {
        return getNetServerHandler().getActiveInventory();
    }
    
    public Inventory getDefaultInventory() {
        return getNetServerHandler().getDefaultInventory();
    }

    public ContribNetServerHandler getNetServerHandler() {
        return (ContribNetServerHandler) getHandle().netServerHandler;
    }

    public static boolean resetNetServerHandler(Player player) {
        CraftPlayer cp = (CraftPlayer)player;
        CraftServer server = (CraftServer)Bukkit.getServer();
        
        if (!(cp.getHandle().netServerHandler instanceof ContribNetServerHandler)) {
            Location loc = player.getLocation();
            NetServerHandler handler = new NetServerHandler(server.getHandle().server, cp.getHandle().netServerHandler.networkManager, cp.getHandle());
            handler.a(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
            cp.getHandle().netServerHandler = handler;
            return true;
        }
        return false;
    }

    public static boolean updateNetServerHandler(Player player) {
        CraftPlayer cp = (CraftPlayer)player;
        CraftServer server = (CraftServer)Bukkit.getServer();
        
        if (!(cp.getHandle().netServerHandler instanceof ContribNetServerHandler)) {
            Location loc = player.getLocation();
            ContribNetServerHandler handler = new ContribNetServerHandler(server.getHandle().server, cp.getHandle().netServerHandler.networkManager, cp.getHandle());
            handler.a(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
            cp.getHandle().netServerHandler = handler;
            return true;
        }
        return false;
    }

    public static boolean updateBukkitEntity(Player player) {
        if (!(player instanceof ContribCraftPlayer)) {
            CraftPlayer cp = (CraftPlayer)player;
            EntityPlayer ep = cp.getHandle();
            Field bukkitEntity;
            try {
                bukkitEntity = Entity.class.getDeclaredField("bukkitEntity");
                bukkitEntity.setAccessible(true);
                bukkitEntity.set(ep, new ContribCraftPlayer((CraftServer)Bukkit.getServer(), ep));
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
           return false;
    }

    public static void removeBukkitEntity(Player player) {
        CraftPlayer cp = (CraftPlayer)player;
        EntityPlayer ep = cp.getHandle();
        Field bukkitEntity;
        try {
            bukkitEntity = Entity.class.getDeclaredField("bukkitEntity");
            bukkitEntity.setAccessible(true);
            bukkitEntity.set(ep, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ContribPlayer getContribPlayer(Player player) {
        if (player instanceof ContribCraftPlayer) {
            return (ContribCraftPlayer)player;
        }
        updateBukkitEntity(player);
        return (ContribCraftPlayer)((CraftPlayer)player).getHandle().getBukkitEntity();
    }
    
    public void updateKeys(short[] keys) {
        this.forward = Keyboard.getKey(keys[0]);
        this.back = Keyboard.getKey(keys[2]);
        this.left = Keyboard.getKey(keys[1]);
        this.right = Keyboard.getKey(keys[3]);
        this.jump = Keyboard.getKey(keys[4]);
        this.inventoryKey = Keyboard.getKey(keys[5]);
        this.drop = Keyboard.getKey(keys[6]);
        this.chat = Keyboard.getKey(keys[7]);
        this.togglefog = Keyboard.getKey(keys[8]);
        this.sneak = Keyboard.getKey(keys[9]);
    }

    @Override
    public boolean isEnabledBukkitContribSinglePlayerMod() {
        return forward != Keyboard.KEY_UNKNOWN;
    }

    @Override
    public Keyboard getForwardKey() {
        return forward;
    }

    @Override
    public Keyboard getBackwardKey() {
        return back;
    }

    @Override
    public Keyboard getLeftKey() {
        return left;
    }

    @Override
    public Keyboard getRightKey() {
        return right;
    }

    @Override
    public Keyboard getJumpKey() {
        return jump;
    }

    @Override
    public Keyboard getInventoryKey() {
        return inventoryKey;
    }

    @Override
    public Keyboard getDropItemKey() {
        return drop;
    }

    @Override
    public Keyboard getChatKey() {
        return chat;
    }

    @Override
    public Keyboard getToggleFogKey() {
        return togglefog;
    }

    @Override
    public Keyboard getSneakKey() {
        return sneak;
    }

}
