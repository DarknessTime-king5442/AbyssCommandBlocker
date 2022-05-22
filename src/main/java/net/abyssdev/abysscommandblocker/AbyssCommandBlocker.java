package net.abyssdev.abysscommandblocker;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import net.abyssdev.abysscommandblocker.listeners.JoinListener;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class AbyssCommandBlocker extends JavaPlugin implements Listener {

    private final String[] blockedCommands = this.getConfig().getStringList("blocked-commands").toArray(new String[0]);
    private final String[] blockedMessage = new String[this.getConfig().getStringList("blocked-message").size()];

    @Override
    public void onEnable() {
        this.saveDefaultConfig();

        int index = 0;

        for (final String line : this.getConfig().getStringList("blocked-message")) {
            this.blockedMessage[index] = ChatColor.translateAlternateColorCodes('&', line);
            index++;
        }

        this.getServer().getPluginManager().registerEvents(this, this);
        this.readCommandTabComplete();

        this.getServer().getPluginManager().registerEvents(new JoinListener(this), this);
    }

    @EventHandler
    public void onCommand(final PlayerCommandPreprocessEvent event) {

        final Player player = event.getPlayer();

        if (player.hasPermission("abysscommandblocker.bypass")) {
            return;
        }

        final String command = event.getMessage();

        for (final String cmd : this.blockedCommands) {
            if (!command.toLowerCase().startsWith(cmd)) {
                continue;
            }

            event.setCancelled(true);

            for (final String line : this.blockedMessage) {
                player.sendMessage(line);
            }
        }

    }

    private void readCommandTabComplete() {
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(
                this,
                ListenerPriority.HIGHEST,
                PacketType.Play.Server.TAB_COMPLETE,
                PacketType.Play.Client.TAB_COMPLETE) {

            public void onPacketSending(final PacketEvent event) {}

            public void onPacketReceiving(final PacketEvent event) {
                if (event.getPacketType() != PacketType.Play.Client.TAB_COMPLETE) {
                    return;
                }

                final String command = event.getPacket().getSpecificModifier(String.class).read(0).toLowerCase();

                for (final String cmd : blockedCommands) {
                    if (!command.startsWith(cmd)) {
                        continue;
                    }

                    event.setCancelled(true);
                }
            }
        });
    }

}
