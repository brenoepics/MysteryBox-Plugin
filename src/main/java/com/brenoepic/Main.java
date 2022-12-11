package com.brenoepic;

import com.brenoepic.core.MysteryManager;
import com.brenoepic.core.messages.incoming.Incoming;
import com.brenoepic.core.messages.incoming.MysteryBoxWaitingCanceledEvent;
import com.brenoepic.interactions.InteractionMysteryBox;
import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.ItemInteraction;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.plugin.EventHandler;
import com.eu.habbo.plugin.EventListener;
import com.eu.habbo.plugin.HabboPlugin;
import com.eu.habbo.plugin.events.emulator.EmulatorLoadItemsManagerEvent;
import com.eu.habbo.plugin.events.emulator.EmulatorLoadedEvent;
import com.eu.habbo.plugin.events.users.UserLoginEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main extends HabboPlugin implements EventListener {

    Main INSTANCE;
    public static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    public static MysteryManager core;

    public void onEnable() {
        Emulator.getPluginManager().registerEvents(this, this);
        INSTANCE = this;

        core = new MysteryManager();
    }

    public void onDisable() {
        core.dispose();
        LOGGER.info("[Mystery Boxes 1.0] was successfully Unloaded!");
    }

    public boolean hasPermission(Habbo habbo, String s) {
        return false;
    }

    @EventHandler
    public void onEmulatorLoaded(EmulatorLoadedEvent event) throws Exception {
        //register incoming message
        Emulator.getGameServer().getPacketManager().registerHandler(Incoming.mysteryBoxWaitingCanceledMessageComposer, MysteryBoxWaitingCanceledEvent.class);

        Emulator.getConfig().register("mysterybox.challenge.enabled", "1");


        LOGGER.info("[MysteryBox Plugin 1.0] was successfully Loaded!");
    }

    @EventHandler
    public void onUserLoginEvent(UserLoginEvent e) {
        core.updateChallenge(e.habbo);
    }

    @EventHandler
    public void onLoadItemsManager(EmulatorLoadItemsManagerEvent e) {
        Emulator.getGameEnvironment().getItemManager().addItemInteraction(new ItemInteraction("mysterybox", InteractionMysteryBox.class));
    }

    public static MysteryManager getMysteryManager() {
        return core;
    }


    public static void main(String[] args) {
        System.out.println("Hello world!");
    }

}
