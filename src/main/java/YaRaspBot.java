import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class YaRaspBot extends TelegramLongPollingBot {

    private static HttpURLConnection getConnection(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setConnectTimeout(500);
        connection.setReadTimeout(500);
        connection.connect();
        return connection;
    }

    public static String getResponse(String request) throws IOException {
        HttpURLConnection connection = null;
        try {
            final URL url = new URL(request);
            connection = getConnection(url);
            if (HttpURLConnection.HTTP_OK == connection.getResponseCode()) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                StringBuilder stringBuilder = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    stringBuilder.append(inputLine);
                }

                return stringBuilder.toString();
            } else {
                System.out.println("fail" + connection.getResponseCode() + ", " + connection.getResponseMessage());
                return null;
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private boolean checkText(Update update) {
        return update.hasMessage() && update.getMessage().hasText();
    }

    private static KeyboardRow createKeyboardRow() {
        KeyboardRow keyboardRow = new KeyboardRow();
        keyboardRow.add("Из Фурманова");
        keyboardRow.add("Из Иванова");
        return keyboardRow;
    }

    private static void chooseAnswer(String s, SendMessage sendMessage) throws IOException {
        switch (s) {
            case "Из Фурманова": {
                String request = "https://api.rasp.yandex.net/v3.0/search/?apikey=" + Config.yandexToken + Config.format + "&from=" + Config.furmanov + "&to=" + Config.ivanovo + "&transport_types=bus&lang=ru_RU&page=1";
                ParseYandex.json = getResponse(request);
                sendMessage.setText(ParseYandex.parse());
                break;
            }
            case "Из Иванова": {
                String request = "https://api.rasp.yandex.net/v3.0/search/?apikey=" + Config.yandexToken + Config.format + "&to=" + Config.furmanov + "&from=" + Config.ivanovo + "&transport_types=bus&lang=ru_RU&page=1";
                ParseYandex.json = getResponse(request);
                sendMessage.setText(ParseYandex.parse());
                break;
            }
            default: {
                sendMessage.setText("Не пытайся меня удивить");
            }
        }
    }

    private static ReplyKeyboardMarkup createReplyKeyboardMarkup(SendMessage sendMessage) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);
        return replyKeyboardMarkup;
    }

    private synchronized void sendMsg(String chatId, String s) throws IOException {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(chatId);

        ReplyKeyboardMarkup replyKeyboardMarkup = createReplyKeyboardMarkup(sendMessage);

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow keyboardFirstRow = createKeyboardRow();
        keyboard.add(keyboardFirstRow);

        replyKeyboardMarkup.setKeyboard(keyboard);

        chooseAnswer(s, sendMessage);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (checkText(update)) {
            String chatId = update.getMessage().getChatId().toString();
            String s = update.getMessage().getText();
            try {
                sendMsg(chatId, s);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String getBotUsername() {
        return "YaRaspBot";
    }

    @Override
    public String getBotToken() {
        return Config.telegramToken;
    }
}
