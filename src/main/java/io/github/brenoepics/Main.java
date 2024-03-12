package io.github.brenoepics;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.ItemInteraction;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.plugin.EventHandler;
import com.eu.habbo.plugin.EventListener;
import com.eu.habbo.plugin.HabboPlugin;
import com.eu.habbo.plugin.events.emulator.EmulatorLoadItemsManagerEvent;
import com.eu.habbo.plugin.events.emulator.EmulatorLoadedEvent;
import com.eu.habbo.plugin.events.users.UserLoginEvent;
import io.github.brenoepics.core.MysteryManager;
import io.github.brenoepics.interactions.InteractionMysteryBox;
import io.github.brenoepics.messages.Incoming;
import io.github.brenoepics.messages.incoming.WaitingCanceledEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main extends HabboPlugin implements EventListener {
  public static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
  public static final MysteryManager core = new MysteryManager();

  public void onEnable() {
    Emulator.getPluginManager().registerEvents(this, this);
  }

  public void onDisable() {
    core.dispose();
    LOGGER.info("[MysteryBox Plugin] was successfully Unloaded!");
  }

  public boolean hasPermission(Habbo habbo, String s) {
    return false;
  }

  @EventHandler
  public void onEmulatorLoaded(EmulatorLoadedEvent event) throws Exception {
    // register incoming message
    Emulator.getGameServer()
        .getPacketManager()
        .registerHandler(
            Incoming.MYSTERY_BOX_WAITING_CANCELED_MESSAGE_COMPOSER, WaitingCanceledEvent.class);

    Emulator.getConfig().register("mysterybox.challenge.enabled", "1");
    Emulator.getConfig().register("mysterybox.key.onlogin", "1");

    LOGGER.info("[MysteryBox Plugin] was successfully Loaded!");
  }

  @EventHandler
  public void onUserLoginEvent(UserLoginEvent e) {
    if (e.habbo == null) return;
    if (Emulator.getConfig().getBoolean("mysterybox.key.onlogin", true)) {
      core.createKey(e.habbo.getHabboInfo().getId());
    }
    core.updateChallenge(e.habbo);
  }

  @EventHandler
  public void onLoadItemsManager(EmulatorLoadItemsManagerEvent e) {
    Emulator.getGameEnvironment()
        .getItemManager()
        .addItemInteraction(new ItemInteraction("mysterybox", InteractionMysteryBox.class));
  }

  public static MysteryManager getMysteryManager() {
    return core;
  }

  public static void main(String[] args) {
    System.out.println("Hello world!");
  }
}
