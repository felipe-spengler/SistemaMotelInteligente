package com.motelinteligente.dados;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class NetworkUtils {
    public static boolean checkInternetConnection() {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("google.com", 80), 3000);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
