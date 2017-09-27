/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package telebot;
//import com.vdurmont.emoji.EmojiParser;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.send.SendPhoto;
import org.telegram.telegrambots.api.objects.PhotoSize;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
/**
 *
 * @author admin
 */
public class TeleBot extends TelegramLongPollingBot {

    /**
     * @param args the command line arguments
     */
    
    public TeleBot()
    {
        super();
        Timer timer = new Timer();
        timer.schedule(new timingCheck("", this), 0, 900000);
    }
    
    public void sendText(String text, long chat_id)
    {
        SendMessage Message = new SendMessage()
                .setChatId(chat_id)
                .setText(text);
        try { sendMessage(Message); }
        catch (TelegramApiException e)
        {
            if(e.getMessage().equals("Error sending message"))
            {
                try
                {
                    Connection con;
                    String username="root";
                    String password="";
                    String url="jdbc:mysql://127.0.0.1/botBase";

                    con=DriverManager.getConnection(url, username, password);

                    String query="DELETE FROM users WHERE id=?";
                    PreparedStatement ps = con.prepareStatement(query);
                    ps.setLong(1, chat_id);                
                    ps.executeUpdate();                    
                }
                catch(Exception ex){}
            }
            else e.printStackTrace();            
        }
        log(text,chat_id);
    }
    
    public void sendPhoto(String caption, String f_id, long chat_id)
    {
        SendPhoto Message=new SendPhoto()
                .setChatId(chat_id)
                .setPhoto(f_id)
                .setCaption(caption);
        
        try {sendPhoto(Message);}
        catch (TelegramApiException e) {e.printStackTrace();}
    }
    
    public void showKeyboard(ReplyKeyboardMarkup markup, long chat_id, String text)
    {
        SendMessage Message=new SendMessage().setChatId(chat_id)
                .setText(text);
        Message.setReplyMarkup(markup);
        try { sendMessage(Message); }
        catch (TelegramApiException e) {e.printStackTrace();}
    }
    
    public void log(Update update)
    {        
        if(update.hasMessage() && update.getMessage().hasText())
        {
            String first_name = update.getMessage().getChat().getFirstName();
            String last_name = update.getMessage().getChat().getLastName();
            long user_id = update.getMessage().getChat().getId();
            String message_text = update.getMessage().getText();
            long chat_id = update.getMessage().getChatId();
            
            System.out.println("\n ----------------------------");
            System.out.println("Message received ");
            DateFormat dateFormat=new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Date date=new Date();
            System.out.println(dateFormat.format(date));
            System.out.println("Message from " + first_name + " " + last_name + ". (id = " + user_id + ") \n Text - " + message_text);
            System.out.println("ChatID:"+chat_id);          
            
        }
    }
    
    public void log(String bot_answer, long chat_id)
    {       

        System.out.println("\n ----------------------------");
        System.out.println("Message sent");
        DateFormat dateFormat=new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date=new Date();
        System.out.println(dateFormat.format(date));
        System.out.println("ChatID:"+chat_id);
        System.out.println("Bot answer: \n" + bot_answer);
    }
    
    
    public String getReplacements(String group)
    {
        String test="^\\D\\D-\\d\\d\\d$";
        Pattern p = Pattern.compile(test);  
        Matcher m = p.matcher(group);
        if(m.matches())
        {
            try 
            {            
                Document doc=Jsoup.connect("http://hpk.edu.ua/replacements").get();            
                String prevGroup=null, line=group+":\n";
                group=group.toUpperCase();
                
                Elements body=doc.getElementsByClass("news-body");
                Elements header=body.first().getElementsByTag("p");
                Elements firstline=header.first().getElementsByTag("strong");
                Elements secondline=header.eq(1).first().getElementsByTag("strong");
                String date="";
                for(int i=0;i<firstline.size();i++)
                {
                    String tmp=firstline.eq(i).html();
                    tmp=tmp.replace("&nbsp;", "");                 
                    date+=tmp;
                    if(i==1 || i==3) date+=" ";
                }
                date=date.replace("&nbsp;", "");
                String day=secondline.eq(0).html()+secondline.eq(1).html();
                day=day.replace("&nbsp;","");
                line+="\t"+date+"\n\t"+day+"\n";
                int count=0;
                String anouncements="";

                Elements rows=doc.getElementsByTag("table").first().getElementsByTag("tr");
                for(int i=1;i<rows.size();i++)
                {
                    Elements row=rows.eq(i);
                    Elements cell=row.first().getElementsByTag("td");                
               
                    if(cell.hasAttr("colspan"))
                    {
                        String tmp=row.first().getElementsByTag("td").first().html();
                            anouncements+="\t\t"+tmp
                                    .replace("<strong>","")
                                    .replace("</strong>", "")
                                    .replace("&nbsp;", "")+"\n";
                    }
               
                    String currentGroup=row.first().getElementsByTag("td").first().html();
                    currentGroup=currentGroup.replace("&nbsp;", "");
                    currentGroup=currentGroup.replace("<strong>", "").replace("</strong>", "");
                    currentGroup=currentGroup.toUpperCase();
                    
                    if(currentGroup.equals(group) || ("".equals(currentGroup) && group.equals(prevGroup)))
                    {
                        count++;
                        Elements cells=row.first().getElementsByTag("td");
                        String pair=cells.eq(1).html().replace("<strong>", "").replace("</strong>", "").replace("&nbsp;", "");
                        String subject=cells.eq(3).html().replace("<strong>", "").replace("</strong>", "").replace("&nbsp;", "");
                        String teacher=cells.eq(4).html().replace("<strong>", "").replace("</strong>", "").replace("&nbsp;", "");
                        String room=cells.eq(5).html().replace("<strong>", "").replace("</strong>", "").replace("&nbsp;", "");
                        line+="\t\t"+pair+"\t"+subject+"\t"+teacher+"\t"+room+"\n";
                    }
                    if(!"".equals(currentGroup)) prevGroup=currentGroup;                
                }
                if(count==0) line+="Замін немає";
                if(anouncements.length()!=0)anouncements="Оголошення:\n"+anouncements+"\n";
                line=anouncements+line;
                return line;
            } catch (IOException ex) {}
        }
        return "Некорректна назва групи";        
    }
    
    public String getDay()
    {
        String date="";
        try
        {
            Document doc=Jsoup.connect("http://hpk.edu.ua/replacements").get();            
                
            Elements body=doc.getElementsByClass("news-body");
            Elements header=body.first().getElementsByTag("p");
            Elements firstline=header.first().getElementsByTag("strong");
            for(int i=0;i<firstline.size();i++)
            {
                String tmp=firstline.eq(i).html();
                tmp=tmp.replace("&nbsp;", "");                 
                date+=tmp;
                if(i==1 || i==3) date+=" ";
            }           
        }
        catch(Exception e){e.printStackTrace();}
        return date;        
    }  
    
       
    @Override
    public void onUpdateReceived(Update update) {
        long chat_id;        
        if(update.hasMessage() && update.getMessage().hasText())
        {      
            
            log(update);
            chat_id= update.getMessage().getChatId();
            SendMessage message=new SendMessage();
            if("/start".equals(update.getMessage().getText()) || "/help".equals(update.getMessage().getText()))
            {
                String line=
                        "Привіт! Я бот, який може відслідковувати заміни для студентів ХПК. Просто напиши мені назву групи, про заміни якої ти хочеш дізнатись.\n\n"
                        + "Якщо ти просто хочеш переглянути заміни на цю групу, натисни кнопку \"Переглянути заміни\".\n\n"
                        + "Якщо ж ти хочеш отримувати повідомлення кожен раз коли на сайті ХПК виходять заміни, натисни на кнопку \"Відслідковувати групу\".\n\n"
                        + "Також, у мене є деякі команди:\n"
                        + "/my - Переглянути мої заміни;\n"
                        + "/remove - Не відслідковувати групу;\n"
                        + "/help - Переглянути це повідомлення з інструкцією.\n\n"
                        + "Автор бота - @EtherDrake";
                sendText(line,chat_id);
            }
            else if("/remove".equals(update.getMessage().getText()))
            {
                try
                {
                    Connection con;
                    String username="root";
                    String password="";
                    String url="jdbc:mysql://127.0.0.1/botBase";

                    con=DriverManager.getConnection(url, username, password);

                    String query="DELETE FROM users WHERE id=?";
                    PreparedStatement ps = con.prepareStatement(query);
                    ps.setLong(1, chat_id);                
                    ps.executeUpdate();
                    sendText("Група успішно видалена", chat_id);
                }
                catch(Exception e){e.printStackTrace();}
            }                    
            else if("/my".equals(update.getMessage().getText()))
            {
                try
                {
                    Connection con;
                    String username="root";
                    String password="";
                    String url="jdbc:mysql://127.0.0.1/botBase";

                    con=DriverManager.getConnection(url, username, password);
                    String query="SELECT tracking FROM users WHERE id=?";
                    PreparedStatement ps = con.prepareStatement(query);
                    ps.setLong(1, chat_id);
                    ResultSet res=ps.executeQuery();
                    res.last();
                    if(res.getRow()==0) sendText("Ви не відслідковуєте жодної групи", chat_id);
                    else
                    {
                        
                        res.first();
                        String group=res.getString("tracking");
                        sendText(getReplacements(group),chat_id);
                    }
                }
                catch(Exception e) {e.printStackTrace();}
            }
            else
            {
                String test="^\\D\\D-\\d\\d\\d$";
                Pattern p = Pattern.compile(test);  
                Matcher m = p.matcher(update.getMessage().getText());
                if(m.matches())
                {
                    InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
                    List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
                    List<InlineKeyboardButton> rowInline = new ArrayList<>();
                    rowInline.add(new InlineKeyboardButton().setText("Переглянути заміни").setCallbackData("showRepl"));
                    rowInline.add(new InlineKeyboardButton().setText("Відслідковувати групу").setCallbackData("setTrack"));
                    
                    // Set the keyboard to the markup
                    rowsInline.add(rowInline);
                    // Add it to the message
                    markupInline.setKeyboard(rowsInline);                    
                    message.setReplyMarkup(markupInline);
                    
                    message.setChatId(chat_id)
                            .setText(update.getMessage().getText().toUpperCase());
                }
                else
                {
                    message.setChatId(chat_id)
                            .setText("Ви ввели некоректну назву групи");
                }
                try { sendMessage(message); }
                catch (TelegramApiException e) {e.printStackTrace();}                
            }
            /*switch(update.getMessage().getText())
            {
                case "KONO DIO DA":
                    sendPhoto("You expected ExperimentalBot, but it was me, Dio!", 
                            "AgADAgADpagxG1gSyUk2S420fksuNjwVSw0ABNjMHIgsmzn7xJIPAAEC", 
                            chat_id);
                    break;
                case "Hello darkness my old friend":
                    sendPhoto("I've come to talk to you again",
                            "AgADAgADpKgxG1gSyUnW6uwwz7rkdj7IDw4ABAlzgj5PnHMk444AAgI",
                            chat_id);
                    break;
                case "O kurwa":
                    sendPhoto("Ja pierdolę",
                            "AgADAgADKKgxGxvwyUndUuyClttfiRYUSw0ABPIglbLz8-VRbJYPAAEC",
                            chat_id);
                    break;
                case "Praise the sun":
                    sendPhoto("Jolly cooperation",
                            "AgADAgADKagxGxvwyUlh2t3N4nDxxarBDw4ABNjXaoVq9xw0No0AAgI",
                            chat_id);
                    break;
                case "Stayin' alive":
                    sendPhoto("Ha, ha, ha, ha, stayin' alive", 
                            "AgADAgADo6gxG1gSyUmJnAEr-kH4u9fTDw4ABIdG2O7VcwiO8IwAAgI", 
                            chat_id);
                    break;
                    
                case "ROAD ROLLAR DA":
                    message="WRYYYYYY"; sendText(message,chat_id);
                    break;
                    
                case "Send me some emojis":
                    message = EmojiParser.parseToUnicode("Here is a smile emoji: :smile:\n\n Here is alien emoji: :alien:");
                    sendText(message,chat_id);
                    break;
                    
                case "/keyboard":
                    ReplyKeyboardMarkup keyboardMarkup=new ReplyKeyboardMarkup();
                    List<KeyboardRow> keyboard=new ArrayList<>();
                    KeyboardRow row=new KeyboardRow();
                    
                    row.add("Answer to the question of the universe:");
                    row.add("Why do the birds fly?");
                    row.add("2x2=?");
                    keyboard.add(row);
                    
                    keyboardMarkup.setKeyboard(keyboard);
                    showKeyboard(keyboardMarkup, chat_id, "Here's your keyboard");
                    break;
                    
                case "/hide":
                    SendMessage msg = new SendMessage()
                        .setChatId(chat_id)
                        .setText("Keyboard hidden");
                    ReplyKeyboardRemove markup = new ReplyKeyboardRemove();
                    msg.setReplyMarkup(markup);
                    try {
                        sendMessage(msg); // Call method to send the photo
                    } catch (TelegramApiException e) {e.printStackTrace();}                    
                    break;
                    
                    
                case "Answer to the question of the universe:":
                    sendText("42", chat_id);
                    message="42";
                    break;
                    
                case "Why do the birds fly?":
                    sendText("Birds fly just because they want to, they don't need a reason.", chat_id);
                    message="Birds fly just because they want to, they don't need a reason.";
                    break;
                    
                case "2x2=?":
                    sendText("2x2=5",chat_id);
                    message="2x2=5";
                    break;
                    
                default:
                    message=update.getMessage().getText();
                    getReplacements(message,chat_id);
                    break;
            }
            
            */
        }
        
        else if(update.hasCallbackQuery())
        {
            chat_id= update.getCallbackQuery().getMessage().getChatId();
            String call_data=update.getCallbackQuery().getData();
            String messageText=update.getCallbackQuery().getMessage().getText();            
            switch(call_data)
            {
                case "setTrack":
                    try
                    {
                        Connection con;
                        String username="root";
                        String password="";
                        String url="jdbc:mysql://127.0.0.1/botBase";
                        
                        con=DriverManager.getConnection(url, username, password);
                        String uname=update.getCallbackQuery().getFrom().getFirstName()+" "+update.getCallbackQuery().getFrom().getLastName();
                        
                        String query="INSERT INTO users (id,name,tracking) VALUES (?,?,?) ON DUPLICATE KEY UPDATE tracking=?";
                        PreparedStatement ps = con.prepareStatement(query);
                        ps.setLong(1, chat_id);
                        ps.setString(2, uname);
                        ps.setString(3, messageText);
                        ps.setString(4, messageText);
                        ps.executeUpdate();      
                        
                        String line="Ви відслідковуєте групу "+messageText;                        
                        sendText(line, chat_id);
                        sendText(getReplacements(messageText), chat_id);                        
                    }catch(Exception ex){ ex.printStackTrace();}
                    break;
                case "showRepl":
                    sendText(getReplacements(messageText), chat_id);
                    break;
                default:break;
            }            
        }
        
        
        else if(update.hasMessage() && update.getMessage().hasPhoto())
        {
            chat_id= update.getMessage().getChatId();
            List<PhotoSize> photos=update.getMessage().getPhoto();
            
            String f_id=photos.stream()
                    .sorted(Comparator.comparing(PhotoSize::getFileSize).reversed())
                    .findFirst()
                    .orElse(null).getFileId();
            
            int f_width=photos.stream()
                    .sorted(Comparator.comparing(PhotoSize::getFileSize).reversed())
                    .findFirst()
                    .orElse(null).getWidth();
            
            int f_height=photos.stream()
                    .sorted(Comparator.comparing(PhotoSize::getFileSize).reversed())
                    .findFirst()
                    .orElse(null).getHeight();
            
            String caption="f_id:"+f_id+"\nwidth:"+Integer.toString(f_width)+"\nheight"+Integer.toString(f_height);            
            sendPhoto(caption, f_id, chat_id);
        }
        else
        {
            chat_id= update.getMessage().getChatId();
            String message="I can't understand you yet";
            sendText(message,chat_id);
        }  
    }

    @Override
    public String getBotUsername() {
        return "ExperimentalBot";
    }

    @Override
    public String getBotToken() {
        return "418440998:AAGGlVniBHyN9F1t2ckmOGm6CWel37p1G_A";
    }
    
}