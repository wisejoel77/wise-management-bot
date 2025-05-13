package com.wisemanagement;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.interactions.modals.components.TextInput;
import net.dv8tion.jda.api.interactions.modals.components.TextInputStyle;
import net.dv8tion.jda.api.requests.GatewayIntent;

import javax.annotation.Nonnull;
import java.util.List;

public class WiseBot extends ListenerAdapter {

    private static final List<String> ALLOWED_ROLE_IDS = List.of("123456789012345678"); // Replace with actual role ID

    public static void main(String[] args) throws Exception {
        String token = System.getenv("DISCORD_TOKEN");

        var jda = JDABuilder.createDefault(token)
                .enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
                .addEventListeners(new WiseBot())
                .build();

        jda.updateCommands().addCommands(
                Commands.slash("wise-echo", "Send a professional announcement using a popup modal")
        ).queue();
    }

    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {
        if (event.getName().equals("wise-echo")) {
            boolean hasPermission = event.getMember().getRoles().stream()
                    .anyMatch(role -> ALLOWED_ROLE_IDS.contains(role.getId()));

            if (!hasPermission) {
                event.reply("🚫 You do not have permission to use this command. Only allowed roles can access it.")
                        .setEphemeral(true).queue();
                return;
            }

            TextInput channelIdInput = TextInput.create("channel_id", "Channel ID", TextInputStyle.SHORT)
                    .setPlaceholder("Paste the Channel ID").setRequired(true).build();

            TextInput titleInput = TextInput.create("title", "Announcement Title", TextInputStyle.SHORT)
                    .setPlaceholder("e.g. Important Update").setRequired(true).build();

            TextInput messageInput = TextInput.create("message", "Message Content", TextInputStyle.PARAGRAPH)
                    .setPlaceholder("Write the full announcement here...").setRequired(true).build();

            Modal modal = Modal.create("wise-echo-form", "📢 Send Announcement")
                    .addActionRow(channelIdInput)
                    .addActionRow(titleInput)
                    .addActionRow(messageInput)
                    .build();

            event.replyModal(modal).queue();
        }
    }

    @Override
    public void onModalInteraction(@Nonnull ModalInteractionEvent event) {
        if (event.getModalId().equals("wise-echo-form")) {
            String channelId = event.getValue("channel_id").getAsString().trim();
            String title = event.getValue("title").getAsString().trim();
            String message = event.getValue("message").getAsString().trim();

            TextChannel channel = event.getJDA().getTextChannelById(channelId);

            if (channel != null) {
                String finalMessage = "**" + title + "**\n\n" + message;
                channel.sendMessage(finalMessage).queue();

                event.reply("✅ Announcement sent to <#" + channelId + ">!")
                        .setEphemeral(true).queue();
            } else {
                event.reply("❌ Invalid Channel ID! Make sure the bot has access to that channel.")
                        .setEphemeral(true).queue();
            }
        }
    }
}
