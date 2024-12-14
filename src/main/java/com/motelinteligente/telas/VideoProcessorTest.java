package com.motelinteligente.telas;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.api.services.youtube.model.VideoStatus;
import com.motelinteligente.dados.YouTubeAuth;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import javax.imageio.ImageIO;

public class VideoProcessorTest {

    

    public void checkVideoFolder() {
        File folder = new File("src/main/resources/videos/definitivo");
        if (!folder.exists() || !folder.isDirectory()) {
            System.out.println("A pasta de vídeos não foi encontrada!");
            return;
        }

        File[] videoFiles = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".mp4"));
        if (videoFiles == null || videoFiles.length < 5) {
            System.out.println("Ainda não há vídeos suficientes para concatenar.");
            return;
        }

        System.out.println("Número de vídeos encontrados: " + videoFiles.length);
        System.out.println("Temos 5 ou mais vídeos, hora de concatenar!");

        try {
            createBlackScreensForVideos();
            concatenateVideos(folder);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void createBlackScreensForVideos() throws IOException, InterruptedException {
        File folder = new File("src/main/resources/videos/definitivo");
        File[] textFiles = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".txt"));

        if (textFiles == null) {
            System.out.println("Nenhum arquivo de texto encontrado.");
            return;
        }

        for (File textFile : textFiles) {
            String infoText = readTextFromFile(textFile);
            String idLocacao = textFile.getName().replace("locacao_", "").replace(".txt", "");

            String outputImagePath = new File(folder, "black_" + idLocacao + ".png").getAbsolutePath();
            String outputVideoPath = new File(folder, "black_" + idLocacao + ".mp4").getAbsolutePath();

            createTextImage(infoText, outputImagePath); // Cria a imagem com o texto
            convertImageToVideoWithAudio(outputImagePath, outputVideoPath); // Adiciona áudio silencioso ao vídeo
        }
    }

    private static String readTextFromFile(File file) throws IOException {
        StringBuilder content = new StringBuilder();
        try ( BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\\n");
            }
        }
        return content.toString();
    }

    private void createTextImage(String text, String outputImagePath) throws IOException {
        int width = 1280;
        int height = 720;

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();

        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, width, height);

        Font font = new Font("Arial", Font.PLAIN, 36);
        graphics.setFont(font);
        graphics.setColor(Color.WHITE);

        String[] lines = text.split("\\\\n");
        FontMetrics metrics = graphics.getFontMetrics(font);

        int y = (height - lines.length * metrics.getHeight()) / 2;

        for (String line : lines) {
            int x = (width - metrics.stringWidth(line)) / 2;
            graphics.drawString(line, x, y);
            y += metrics.getHeight();
        }

        graphics.dispose();
        ImageIO.write(image, "png", new File(outputImagePath));
        System.out.println("Imagem criada: " + outputImagePath);
    }

    private void convertImageToVideoWithAudio(String inputImagePath, String outputVideoPath) throws IOException, InterruptedException {
        String command = String.format(
                "ffmpeg -loop 1 -i \"%s\" -f lavfi -i anullsrc=r=44100:cl=stereo -shortest -c:v libx264 -c:a aac -t 5 -pix_fmt yuv420p \"%s\"",
                inputImagePath, outputVideoPath
        );
        executeCommand(command);
    }

    private void concatenateVideos(File folder) {
        try {
            File[] videoFiles = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".mp4"));
            if (videoFiles == null || videoFiles.length == 0) {
                System.out.println("Nenhum vídeo encontrado.");
                return;
            }

            Map<String, String> videoPairs = new TreeMap<>();
            for (File video : videoFiles) {
                String name = video.getName();
                if (name.startsWith("black_")) {
                    String id = name.replace("black_", "").replace(".mp4", "");
                    videoPairs.putIfAbsent(id, null);
                    videoPairs.put(id, video.getAbsolutePath());
                } else {
                    String id = name.replace(".mp4", "");
                    videoPairs.putIfAbsent(id, null);
                    videoPairs.put(id, video.getAbsolutePath());
                }
            }

            for (String id : videoPairs.keySet()) {
                String blackVideoPath = new File(folder, "black_" + id + ".mp4").getAbsolutePath();
                String originalVideoPath = new File(folder, id + ".mp4").getAbsolutePath();

                if (new File(blackVideoPath).exists() && new File(originalVideoPath).exists()) {
                    String scaledVideoPath = folder.getAbsolutePath() + "/" + id + "_scaled.mp4";
                    executeCommand(String.format(
                            "ffmpeg -y -i \"%s\" -vf \"scale=1280:720\" \"%s\"",
                            originalVideoPath, scaledVideoPath
                    ));

                    String outputPath = folder.getAbsolutePath() + "/" + id + "_output.mp4";
                    executeCommand(String.format(
                            "ffmpeg -y -i \"%s\" -i \"%s\" -filter_complex \"[0:v:0][0:a:0][1:v:0][1:a:0]concat=n=2:v=1:a=1[outv][outa]\" -map \"[outv]\" -map \"[outa]\" \"%s\"",
                            blackVideoPath, scaledVideoPath, outputPath
                    ));
                    System.out.println("Vídeo concatenado: " + outputPath);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        concatenateFinalVideos(folder);
    }

    private void concatenateFinalVideos(File folder) {
        try {
            // Identifica todos os vídeos "_output" gerados após a concatenação dos vídeos black
            File[] outputFiles = folder.listFiles((dir, name) -> name.toLowerCase().endsWith("_output.mp4"));
            if (outputFiles == null || outputFiles.length == 0) {
                System.out.println("Nenhum arquivo output encontrado para concatenação.");
                return;
            }

            // Cria a lista de vídeos para concatenação
            String listFilePath = "src/main/resources/videos/temp/video_list_final.txt";
            File tempFolder = new File("src/main/resources/videos/temp");
            if (!tempFolder.exists()) {
                tempFolder.mkdirs();
            }

            // Lista para armazenar os nomes dos vídeos e tempos de início
            List<String> videoStartTimes = new ArrayList<>();
            int totalDuration = 0; // Total de duração para criar o nome do arquivo final

            // Cria o arquivo de lista para o FFmpeg e calcula a duração
            try ( PrintWriter writer = new PrintWriter(listFilePath)) {
                for (File file : outputFiles) {
                    String fileName = file.getName();
                    int duration = getVideoDuration(file.getAbsolutePath());
                    videoStartTimes.add(fileName + " | Início em: " + totalDuration + " segundos");

                    // Adiciona o vídeo à lista de concatenação
                    writer.println("file '" + file.getAbsolutePath().replace("\\", "/") + "'");

                    // Atualiza o tempo total (soma da duração dos vídeos)
                    totalDuration += duration;
                }
            }

            // Cria o nome do vídeo final concatenado com os números dos vídeos
            String videoNames = String.join("-", Arrays.stream(outputFiles)
                    .map(file -> file.getName().replace("_output.mp4", ""))
                    .toArray(String[]::new));

            // Caminho do vídeo final concatenado
            String outputVideoPath = "src/main/resources/videos/definitivo/" + videoNames + ".mp4";

            // Comando para concatenar todos os vídeos
            String command = String.format("ffmpeg -y -f concat -safe 0 -i \"%s\" -c copy \"%s\"", listFilePath, outputVideoPath);
            executeCommand(command);
            System.out.println("Vídeo final concatenado criado: " + outputVideoPath);

            // Salva o arquivo de log com os tempos de início dos vídeos concatenados
            String logFilePath = "src/main/resources/videos/temp/concat_video_log.txt";
            try ( PrintWriter logWriter = new PrintWriter(logFilePath)) {
                for (String logEntry : videoStartTimes) {
                    logWriter.println(logEntry);
                }
            }

            System.out.println("Log de concatenação salvo em: " + logFilePath);

            moveAndCleanUp(outputVideoPath, logFilePath);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void moveAndCleanUp(String finalVideoPath, String logFilePath) {
    try {
        // Definir as pastas de destino
        String targetFolder = "src/main/resources/videos";
        File targetDir = new File(targetFolder);
        if (!targetDir.exists()) {
            targetDir.mkdirs();  // Cria a pasta 'videos' caso não exista
        }

        // Movendo o vídeo final para a pasta 'videos'
        File finalVideo = new File(finalVideoPath);
        File destinationVideo = null;
        if (finalVideo.exists()) {
            destinationVideo = new File(targetFolder, finalVideo.getName());
            boolean videoMoved = finalVideo.renameTo(destinationVideo);
            if (videoMoved) {
                System.out.println("Vídeo final movido para: " + destinationVideo.getAbsolutePath());
            } else {
                System.out.println("Falha ao mover o vídeo final: " + finalVideo.getAbsolutePath());
            }
        }

        // Movendo o arquivo de log para a pasta 'videos' com o mesmo nome do vídeo final
        File logFile = new File(logFilePath);
        if (logFile.exists() && destinationVideo != null) {
            File destinationLog = new File(targetFolder, finalVideo.getName().replace(".mp4", "_log.txt"));
            boolean logMoved = logFile.renameTo(destinationLog);
            if (logMoved) {
                System.out.println("Arquivo de log movido para: " + destinationLog.getAbsolutePath());
            } else {
                System.out.println("Falha ao mover o arquivo de log: " + logFile.getAbsolutePath());
            }
        }

        // Limpar as pastas 'temp' e 'definitivo'
        cleanDirectory(new File("src/main/resources/videos/temp"));
        cleanDirectory(new File("src/main/resources/videos/definitivo"));

        // Enviar para o YouTube
        if (destinationVideo != null) {
            uploadToYouTube(destinationVideo.getAbsolutePath(), finalVideo.getName().replace(".mp4", ""));
        }

    } catch (Exception e) {
        e.printStackTrace();
    }
}

private void uploadToYouTube(String videoPath, String videoTitle) {
    try {
        // Autenticação e inicialização do cliente YouTube
        Credential credential = YouTubeAuth.authorize();
        YouTube youtubeService = new YouTube.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                com.google.api.client.json.gson.GsonFactory.getDefaultInstance(),
                credential
        ).setApplicationName("MotelInteligente").build();

        // Configurar o objeto de status do vídeo
        VideoStatus status = new VideoStatus();
        status.setPrivacyStatus("unlisted"); // Vídeo não listado

        // Configurar o objeto snippet do vídeo
        VideoSnippet snippet = new VideoSnippet();
        snippet.setTitle(videoTitle); // Usando o título do vídeo
        snippet.setTags(Arrays.asList("motel", "inteligente", "video")); // Tags opcionais

        // Configurar o vídeo a ser enviado
        Video video = new Video();
        video.setStatus(status);
        video.setSnippet(snippet);

        // Configurar a entrada do vídeo
        File videoFile = new File(videoPath);
        InputStreamContent mediaContent = new InputStreamContent(
                "video/*",
                new FileInputStream(videoFile)
        );

        // Solicitação de upload
        YouTube.Videos.Insert request = youtubeService.videos().insert(
                "snippet,status",
                video,
                mediaContent
        );

        System.out.println("Enviando vídeo: " + videoPath);
        Video response = request.execute();
        System.out.println("Vídeo enviado com sucesso! ID: " + response.getId());
    } catch (Exception e) {
        e.printStackTrace();
    }
}

    private void cleanDirectory(File directory) {
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        cleanDirectory(file);  // Recursão para pastas dentro da pasta
                    }
                    boolean deleted = file.delete();
                    if (deleted) {
                        System.out.println("Arquivo excluído: " + file.getAbsolutePath());
                    } else {
                        System.out.println("Falha ao excluir o arquivo: " + file.getAbsolutePath());
                    }
                }
            }
        }
    }

    private int getVideoDuration(String videoPath) {
        try {
            // Comando para obter a duração do vídeo
            String command = String.format("ffprobe -v error -select_streams v:0 -show_entries stream=duration -of csv=p=0 \"%s\"", videoPath);
            Process process = new ProcessBuilder("cmd", "/c", command).start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String durationStr = reader.readLine();
            process.waitFor();

            if (durationStr != null) {
                return (int) Math.ceil(Double.parseDouble(durationStr));  // Converte a duração para segundos
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;  // Retorna 0 se a duração não puder ser obtida
    }

// Função para deletar arquivos temporários
    private void deleteTemporaryFiles(File[] files, File folder) {
        for (File file : files) {
            if (file.exists()) {
                if (file.delete()) {
                    System.out.println("Arquivo deletado com sucesso: " + file.getName());
                } else {
                    System.out.println("Falha ao deletar: " + file.getAbsolutePath());
                    // Tentar deletar novamente
                    tryDeleteFile(file);
                }
            }
        }
    }

    private void tryDeleteFile(File file) {
        int attempts = 3;
        while (attempts > 0) {
            if (file.delete()) {
                System.out.println("Arquivo deletado após tentativa: " + file.getAbsolutePath());
                break;
            } else {
                System.out.println("Falha ao deletar, tentando novamente... (" + attempts + " tentativas restantes)");
                attempts--;
                try {
                    Thread.sleep(1000); // Espera de 1 segundo antes de tentar novamente
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void executeCommand(String command) {
        try {
            ProcessBuilder builder = new ProcessBuilder("cmd", "/c", command);
            builder.redirectErrorStream(true);
            Process process = builder.start();

            try ( BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
