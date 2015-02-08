package ma.ensa.CrawlerJsoupFinal;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * 
 * @author EL BOUFARISSI AHMED TAHA
 *
 */

public class MyPageParser {

//transform(String s) permet d'effacer les balises et / pour avoir en retour un String 
	//prét a etre stocker dans la base de données
	private static String transform(String s)
	{
		StringBuffer temp = new StringBuffer();
		int n = s.length();
		for (int i = 0; i < n; i++)
		{
			if (s.charAt(i) =='\'')
			{
				temp.append("\'\'");
			}
			else
			{
				temp.append(s.charAt(i));
			}

			if (s.charAt(i) ==' ')
			{
				temp.append("");
			}
			else
			{
				temp.append(s.charAt(i));
			}
		}
		return temp.toString();
	}
	
	public static void main(String[] args) throws IOException, SQLException {

		//connection a la base de données
		DB db =new DB();
		//variable stock resultat de la requete sql TRUNCATE TABLE
		boolean test;
		//vider la base de données
		String sql0 = "truncate record;";
		test = db.runSql2(sql0);
		
		int i=0;
		//test D'execution
		if(test){
			System.out.println(sql0 + "  DONE !!");
		}else{
			System.out.println("ERREUR");
		}
		
		
		//On se connecte au site et on charge le document html
		Document doc = Jsoup.connect("http://www.anahna.com/pharmacies-agadir-ca7-qa0.html").get();

		//On récupère dans ce document la premiere balise ayant comme nom div et pour attribut class="right"
		Elements links = doc.select("div .right"); 
		for(Element link: links){

			
			//link = doc.select("h1").first();

			//on recupere le 1er element fils qui est le l'adress de la pharmacie	
			System.out.println(link.child(1));
			String  adress=  link.child(1).text();
			transform(adress);
			System.out.println(adress);

			//on recupere le 2er element fils qui est le num de tel de la pharmacie	
			System.out.println(link.child(2));
			String tel =  link.child(2).text();
			transform(tel);
			System.out.println(tel);

			//on recupere l'element fils qui est le nom de tel de la pharmacie	
			System.out.println(link.child(0));
			String nomPharmacie =  link.child(0).text();
			transform(nomPharmacie);
			System.out.println(nomPharmacie);

			//On récupère dans ce document la premiere balise ayant comme nom div et pour attribut class="left"
			Elements shs = doc.select("div .left"); 

			//on récupère la balise a [href] qui contient les urls des cordonnées lat et long
			Element maps = shs.select("a").get(i); 

			String relHref = maps.attr("href"); // == "/"
			String absHref = maps.attr("abs:href"); // "http://jsoup.org/"

			System.out.println("absHref =\t"+absHref);
			System.out.println("relHref =\t"+relHref);


			//preparere la requet sql pour stocker dans la base de donnée

			String sql = "insert into record (nom,tel,adress,URL)" + "values(?,?,?,?)";
			
			//String sqlName = "INSERT INTO record " + "(nom,tel,adress) VALUES " + "(`nomPharmacie`,`tel`,`adress`);";
			//test = db.runSql2(sql);


			Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3333/convertisseur","root", "taha");
			PreparedStatement preparedStatement = conn.prepareStatement(sql);
			preparedStatement.setString(1, nomPharmacie);
			preparedStatement.setString(2, tel);
			preparedStatement.setString(3, adress);
			preparedStatement.setString(4, absHref);
			preparedStatement.executeUpdate();

			//i++;
			//			if (test){
			//				System.out.println("nom stocker dans la table avec succé");
			//			}else{
			//				System.out.println("ERREUR SQL");
			//			}
			//System.out.println(titre);
		}

	}
}




