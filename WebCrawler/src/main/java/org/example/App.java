package org.example;

import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.swing.plaf.nimbus.State;
import java.io.IOException;
import java.net.UnknownHostException;
import java.sql.*;

public class App {
    static Connection con;
    static Statement statement;
    static Statement statement1;
    static final String baseURL = "https://www.chitkara.edu.in/";
    static void getLinks(String URL) throws SQLException, IOException, UnknownHostException {


        ResultSet resultSet = statement.executeQuery("select * from crawler_data where link='"+URL+"'");

        if(resultSet.next()){

            if(resultSet.getInt("is_visited") == 1 || resultSet.getInt("is_not_reachable")==1){
                return;
            }

        }

        Document doc = Jsoup.connect(URL).get();

        Elements element = doc.select("a[href]");

        for(Element page:element){

            String query = "select * from crawler_data where link = '"+page.attr("abs:href")+"';";

            ResultSet resultSet1 = statement.executeQuery(query);


            if(!resultSet1.next()){
                String sql ="insert into crawler_data values('"+page.attr("abs:href")+"',0,0);";

                statement.execute(sql);

            }

        }

    }

    public static void main(String[] args) throws SQLException, IOException {
        con = DriverManager.getConnection("jdbc:mysql://localhost:3306/<database_name>","<user_name>","<yourpassword>");

        statement = con.createStatement();
        statement1 = con.createStatement();

        if(con == null){
            System.out.println(false);
        }else{
            System.out.println(true);
        }
        Statement st1 = con.createStatement();
        st1.execute("CREATE TABLE IF NOT EXISTS crawler_data(link varchar(2038),is_visited int,is_not_reachable int);");


        getLinks(baseURL);

        ResultSet resultSet = statement1.executeQuery("select *  from crawler_data where is_visited=0;");

        while(resultSet.next()){

            String link = resultSet.getString("link");

            try {

                System.out.println("Fetching "+link+" ......");

                getLinks(link);

                statement1.execute("update crawler_data set is_visited = 1 where link = '"+link+"'");

                resultSet = statement1.executeQuery("select *  from crawler_data where is_visited=0;");
            } catch (UnknownHostException e){

                Statement st = con.createStatement();
                st.execute("update crawler_data set is_not_reachable = 1 where link = '"+link+"'");
            }
            catch (UnsupportedMimeTypeException e){
                Statement st = con.createStatement();
                st.execute("delete from crawler_data where link = '"+link+"'");
            }
            catch (Exception e){
                Statement st = con.createStatement();
                st.execute("update crawler_data set is_not_reachable = 1 where link = '"+link+"'");
            }

        }


    }
}