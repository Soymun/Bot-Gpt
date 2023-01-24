package com.example.demo.Service;

import com.example.demo.Conrig.BotConfig;
import com.theokanning.openai.OpenAiApi;
import com.theokanning.openai.OpenAiResponse;
import com.theokanning.openai.OpenAiService;
import com.theokanning.openai.completion.CompletionChoice;
import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.model.Model;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {


    private final BotConfig config;

    @Value("${gpt.key}")
    private String token;

    @Autowired
    public TelegramBot(BotConfig config) {
        this.config = config;
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {

            OpenAiService service = new OpenAiService(token);
            SendMessage sendMessage1 = new SendMessage();
            sendMessage1.setText("Подождите немного, я подумаю...");
            sendMessage1.setChatId(update.getMessage().getChatId().toString());
            try {
                execute(sendMessage1);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(update.getMessage().getChatId().toString());
            CompletionRequest completionRequest = CompletionRequest.builder()
                    .prompt(update.getMessage().getText())
                    .maxTokens(4000)
                    .temperature(0.9)
                    .model("text-davinci-003")
                    .echo(false)
                    .build();
            List<CompletionChoice> choices = service.createCompletion(completionRequest).getChoices();
            sendMessage.setText(choices.get(Math.max(choices.size() - 1, 0)).getText());
            try {
                execute(sendMessage);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }
        catch (Exception e){
            SendMessage sendMessage1 = new SendMessage();
            sendMessage1.setText("Ой, а я не знаю(");
            sendMessage1.setChatId(update.getMessage().getChatId().toString());
            try {
                execute(sendMessage1);
            } catch (TelegramApiException j) {
                throw new RuntimeException(j);
            }
        }
    }
}
