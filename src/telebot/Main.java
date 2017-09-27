/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package telebot;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiException;
/**
 *
 * @author admin
 */
public class Main {
    public static void main(String[] args) {

        // TODO Initialize Api Context
        ApiContextInitializer.init();
        // TODO Instantiate Telegram Bots API
        TelegramBotsApi botsApi = new TelegramBotsApi();
        // TODO Register our bot
        try { botsApi.registerBot(new TeleBot());}
        catch (TelegramApiException e) { e.printStackTrace();}
    }    
}
