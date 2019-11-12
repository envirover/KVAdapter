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
import java.net.ServerSocket;
import java.net.Socket;
import java.text.MessageFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * On client connection to the sink port starts retrieveing media content from
 * the specified Kinesis video stream using GetMedia API and sends it to the
 * client.
 */
class VideoServer {
    private final String streamName;
    private final String region;
    private final Integer port;
    private final ExecutorService threadPool;
    private ServerSocket serverSocket;
    private Thread listenerThread;

    /**
     * Streams video from AWS Kinesis Video stream to TCP/IP port.
     *
     * @param streamName AWS Kinesis Video stream name
     * @param region     AWS region id
     * @param port       TCP port
     */
    public VideoServer(final String streamName, final String region, final Integer port) {
        this.streamName = streamName;
        this.region = region;
        this.port = port;
        this.threadPool = Executors.newCachedThreadPool();
    }

    /**
     * Starts GCSTcpServer.
     *
     * @throws IOException Signals that an I/O exception of some sort has occurred.
     */
    public void start() throws IOException {
        serverSocket = new ServerSocket(port);

        listenerThread = new Thread(new ConnectionListener());
        listenerThread.start();
    }

    /**
     * Stops video server.
     *
     * @throws InterruptedException
     */
    public void stop() throws InterruptedException {
        listenerThread.interrupt();
        listenerThread.join();
    }

    /**
     * Accepts socket connections.
     *
     * @author pavel
     *
     */
    class ConnectionListener implements Runnable {

        @Override
        public void run() {
            while (serverSocket.isBound()) {
                try {
                    Socket socket = serverSocket.accept();

                    Runnable streamingTask = new Runnable() {
                        @Override
                        public void run() {
                            try {
                                System.out.println(MessageFormat.format("Client ''{0}'' connected.", socket.getInetAddress()));
                                KinesisVideoSocket videoSocket = new KinesisVideoSocket(streamName, region, socket);
                                videoSocket.startStreaming();
                            } catch (IOException | InterruptedException ex) {
                                try {
                                    socket.close();
                                    System.out.println(MessageFormat.format("Client ''{0}'' disconnected.", socket.getInetAddress()));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    };

                    threadPool.submit(streamingTask);
    
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }

}