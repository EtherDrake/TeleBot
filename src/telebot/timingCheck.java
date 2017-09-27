/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package telebot;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 *
 * @author admin
 */
public class timingCheck extends TimerTask{
    private String prevDate;
    private TeleBot tbot;
    public timingCheck(String str, TeleBot tb)
    {
        super();
        prevDate=str;
        tbot=tb;
    }
    
    @Override    
    public void run() {
        try
        {
            Connection con;
            String username="root";
            String password="";
            String url="jdbc:mysql://127.0.0.1/botBase";
                        
            con=DriverManager.getConnection(url, username, password);
            String query="SELECT * FROM Updates";
            Statement stmt = con.createStatement();
            ResultSet res = stmt.executeQuery(query);
            res.next();
            String dbUpdate=res.getString("LastUpdate");
            String currentDate=tbot.getDay();
            if(!dbUpdate.equals(currentDate))
            {
                query="UPDATE Updates SET LastUpdate=?";
                PreparedStatement ps = con.prepareStatement(query);
                ps.setString(1, currentDate);
                ps.executeUpdate();
                
                query="SELECT * FROM users";
                res = stmt.executeQuery(query);
                
                

                

                Document doc=Jsoup.connect("http://hpk.edu.ua/replacements").get();            
                String prevGroup=null;                

                Elements body=doc.getElementsByClass("news-body");
                Elements header=body.first().getElementsByTag("p");
                Elements firstline=header.first().getElementsByTag("strong");
                Elements secondline=header.eq(1).first().getElementsByTag("strong");
                int count=0;
                String date="";
                for(int i=0;i<firstline.size();i++)
                {
                    String tmp=firstline.eq(i).html();
                    tmp=tmp.replace("&nbsp;", "");                 
                    date+=tmp;
                    if(i==1 || i==3) date+=" ";
                }
                date=date.replace("&nbsp;", "");;
                String day=secondline.eq(0).html()+secondline.eq(1).html();
                day=day.replace("&nbsp;","");
                Elements rows=doc.getElementsByTag("table").first().getElementsByTag("tr");
                
                
                while(res.next())
                {
                    long id=res.getLong("id");
                    String group=res.getString("tracking");
                    String line=""+group+":\n";
                    group=group.toUpperCase();
                    line+="\t"+date+"\n\t"+day+"\n";
                    String anouncements="";
                    
                    for(int i=1;i<rows.size();i++)
                    {
                        Elements row=rows.eq(i);
                        Elements cell=row.first().getElementsByTag("td");                        

                        if(cell.hasAttr("colspan")) 
                        {                            
                            String tmp=row.first().getElementsByTag("td").first().html();
                            anouncements+="\t"+tmp
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
                            String pair=cells.eq(1).html().replace("<strong>", "")
                                    .replace("</strong>", "")
                                    .replace("&nbsp;", "");
                            String subject=cells.eq(3).html().replace("<strong>", "")
                                    .replace("</strong>", "")
                                    .replace("&nbsp;", "");
                            String teacher=cells.eq(4).html().replace("<strong>", "")
                                    .replace("</strong>", "")
                                    .replace("&nbsp;", "");
                            String room=cells.eq(5).html().replace("<strong>", "")
                                    .replace("</strong>", "")
                                    .replace("&nbsp;", "");
                            line+="\t"+pair+"\t"+subject+"\t"+teacher+"\t"+room+"\n";
                        }
                        if(!"".equals(currentGroup)) prevGroup=currentGroup;                
                    }
                    if(count==0) line+="Замін немає";
                    if(anouncements.length()==0) anouncements="Оголошень немає";
                    else anouncements="Оголошення:\n"+anouncements+"\n";
                    line=anouncements+line;
                    tbot.sendText(line, id);
                }
            }
        }
        catch(Exception e){e.printStackTrace();}
    }    
}
