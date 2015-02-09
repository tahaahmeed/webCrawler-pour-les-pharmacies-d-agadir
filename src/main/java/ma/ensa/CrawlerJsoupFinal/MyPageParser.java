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
		
		String langitude= null;
		String latitude= null;
		
		int i=0;
		//test D'execution
		if(!test){
			System.out.println(sql0 + "  DONE !!");
		}else{
			System.out.println("ERREUR");
		}
		
		
		//On se connecte au site et on charge le document html
		Document doc = Jsoup.connect("http://www.anahna.com/pharmacies-agadir-ca7-qa0.html").timeout(10*1000).get();

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


			    //trouver coordonner depuis script
				Document docs = Jsoup.connect(absHref).timeout(10000).get();
				//on vise le dernier tag  'script'de la page
				Element scriptElement = docs.select("script").last();

				//charger tout le script
				String jsCode = scriptElement.html();
			    //System.out.println(jsCode);
				
			    //extraire la ligne qui nous interesse,dans ce cas les coordonées de la pharmacie
			    jsCode = jsCode.substring(jsCode.indexOf('['),jsCode.indexOf(']'));	
			    //System.out.println(jsCode);
			    
			    //il existe quelque pharmacies sont coordonnées qui declanche une exeption dans le traitement
			    if (jsCode.length()> 6)
			    {
			    
			     langitude = jsCode.substring(1,jsCode.indexOf(','));
			     latitude = jsCode.substring(jsCode.indexOf(','));
			    
			     latitude = latitude.substring(2);
			    
			    //System.out.println("langitude"+langitude);
			    
			    //System.out.println("latitude"+latitude);
			    }else
			    	System.out.println("impossible d'extraire,contenu introuvale !!!!!!!!!!!!!!!!!!!!!!!!!");
			    
			
			//preparere la requet sql pour stocker dans la base de donnée

			String sql = "insert into record (nom,tel,adress,URL,lati,lang)" + "values(?,?,?,?,?,?)";
			
			
			Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3333/convertisseur","root", "taha");
			PreparedStatement preparedStatement = conn.prepareStatement(sql);
			preparedStatement.setString(1, nomPharmacie);
			preparedStatement.setString(2, tel);
			preparedStatement.setString(3, adress);
			preparedStatement.setString(4, absHref);
			preparedStatement.setString(5, langitude);
			preparedStatement.setString(6, langitude);
			preparedStatement.executeUpdate();

                        //incrementation de l'indice de page get(i) du document charger des URLs qui contiennent des
                        //coordonées de geolocalisation des pharmacies
			i++;
			
		}

		
	}
}




