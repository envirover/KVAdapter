/*
 * Copyright 2019 Envirover. All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"). 
 * You may not use this file except in compliance with
 * the License. A copy of the License is located at
 * 
 * https://github.com/envirover/KVAdapter/blob/master/LICENSE
 * 
 * or in the "license" file accompanying this file. This file is distributed on 
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either 
 * express or implied. See the License for the specific language governing 
 * permissions and limitations under the License.
 */

package com.envirover.video;

import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Kinesis Video Streams Adapter is a TCP server application that serves
 * video from a Amazon Kinesis Video stream to connected clients. It is
 * designed to connect Kinesis Video Streams with GStreamer multimedia framework.
 */
public final class KinesisVideoAdapter {

    private static final int SINK_PORT = 4000;

    /**
     * Do not allow cunstruction of the class instances.
     */
    private KinesisVideoAdapter() { };

    /**
     * Starts video server.
     *
     * Command line parameters:
     * --region,-r <AWS region>
     * --stream,-s <Kinesis Video stream name>
     * --port,-p <port>
     *
     * @param args command line arguments
     */
    public static void main(final String[] args) {
        final Options options = new Options();
        options.addOption(new Option("r", "region", true, "AWS region"));
        options.addOption(new Option("s", "stream", true, "Kinesis Video stream name"));
        options.addOption(new Option("p", "port", true, String.format("(optional) client video port. Default port is %d.", SINK_PORT)));

        CommandLineParser parser = new DefaultParser();

        try {
            CommandLine cmd = parser.parse(options, args);

            if (!cmd.hasOption("r") || !cmd.hasOption("s")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("kinesisvideo2gst <options>", options);
                System.exit(1);
            }

            int port = Integer.parseInt(cmd.getOptionValue("p", Integer.toString(SINK_PORT)));

            VideoServer server = new VideoServer(cmd.getOptionValue("s"), cmd.getOptionValue("r"), port);

            server.start();

            System.out.println(String.format("Kinesis Video Adapter listening on port %d.", SINK_PORT));

            try {
                while (true) {
                    Thread.sleep(1000);
                }
            } catch (InterruptedException ex) {
                System.out.println(ex.getMessage());
            }

            server.stop();

            System.out.println("Kinesis Video Adapter stopped.");
        } catch (ParseException | IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

}
