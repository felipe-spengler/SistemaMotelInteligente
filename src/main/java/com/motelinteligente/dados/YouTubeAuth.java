/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motelinteligente.dados;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import javax.swing.JOptionPane;

public class YouTubeAuth {
    private static final String CLIENT_SECRETS = "/credenciais.json";
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    public static Credential authorize() throws Exception {
        InputStream in = YouTubeAuth.class.getResourceAsStream(CLIENT_SECRETS);
        if (in == null) {
            throw new IOException("Arquivo de credenciais não encontrado: " + CLIENT_SECRETS);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
                GsonFactory.getDefaultInstance(), new InputStreamReader(in));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                clientSecrets,
                Collections.singletonList("https://www.googleapis.com/auth/youtube.upload")
        ).setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
         .setAccessType("offline")
         .build();

        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
    }

    public static void main(String[] args) {
        try {
            Credential credential = authorize();
            if (credential != null && credential.getAccessToken() != null) {
                JOptionPane.showMessageDialog(null, "Autenticação concluída com sucesso!");
            } else {
                System.out.println("Falha na autenticação.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}