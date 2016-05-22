/*
 * This file is part of Alfred, licensed under the MIT License (MIT).
 *
 * Copyright (c) InspireNXE <http://github.com/InspireNXE>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.inspirenxe.alfred;

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.kitteh.irc.client.library.Client;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    private static ConfigurationLoader<CommentedConfigurationNode> loader;
    private static CommentedConfigurationNode node;
    private static Client IRC_CLIENT;

    public static void main(String[] args) throws IOException, ObjectMappingException {
        // ./config.conf
        final Path configPath = Paths.get("config.conf");
        if (Files.notExists(configPath)) {
            InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(configPath.toString());
            Files.copy(stream, configPath);
        }
        loader = HoconConfigurationLoader.builder().setPath(configPath).build();
        node = loader.load();
        IRC_CLIENT = generateClient();
        IRC_CLIENT.getEventManager().registerEventListener(new Listener());
        for (String channel : node.getNode("irc", "channels").getList(TypeToken.of(String.class))) {
            IRC_CLIENT.addChannel(channel);
        }
    }

    private static Client generateClient() {
        final String bindHost = node.getNode("bot", "bind-host").getString(null);
        final int bindPort = node.getNode("bot", "bind-port").getInt(0);
        final String username = node.getNode("irc", "username").getString("Alfred");
        final String password = node.getNode("irc", "password").getString(null);
        final String nickname = node.getNode("irc", "nickname").getString("Alfred");
        final String serverHost = node.getNode("irc", "server-host").getString("irc.esper.net");
        final int serverPort = node.getNode("irc", "server-port").getInt(6697);
        final boolean serverSsl = node.getNode("irc", "server-ssl").getBoolean(true);

        final Client.Builder builder = Client.builder()
                .bindPort(bindPort)
                .user(username)
                .nick(nickname)
                .serverHost(serverHost)
                .serverPort(serverPort)
                .secure(serverSsl);

        if (bindHost != null && !bindHost.equalsIgnoreCase("null")) {
            builder.bindHost(bindHost);
        }

        if (password != null && !password.equalsIgnoreCase("null")) {
            builder.serverPassword(password);
        }

        return builder.build();
    }
}
