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

	//transform(String s) permet d'effacer les balises html pour avoir en retour un String 
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

		//connection à la base de données
		DB db =new DB();
		
		//variable stock resultat de la requete sql TRUNCATE TABLE
		boolean test,test1;
		
		//vider la base de données
		String sql0 = "truncate record;";
		String sql1 = "truncate garde;";
		test = db.runSql2(sql0);
		test1 = db.runSql2(sql1);

		//initialisation des langitude & latitude & l'indice des pages url
		String langitude= null;
		String latitude= null;
		int i=0;
		
		//test D'execution
		if(!test && !test1){
			System.out.println(sql0 + "  DONE !!");
			System.out.println(sql1 + "  DONE !!");
		}else{
			System.out.println("ERREUR");
		}
		
		                //trouver toutes les pharmacies de la ville d'agadir

		//On se connecte au site http://www.anahna.com ,on charge le document html
		Document doc = Jsoup.connect("http://www.anahna.com/pharmacies-agadir-ca7-qa0.html").timeout(10*1000).get();

		//On récupère dans ce document les balises ayant comme nom div et pour attribut class="right"
		Elements links = doc.select("div .right"); 
		for(Element link: links){

			//on récupere le 1er l'element fils qui est le nom de la pharmacie	
			System.out.println(link.child(0));
			String nomPharmacie =  link.child(0).text();
			transform(nomPharmacie);
			System.out.println(nomPharmacie);
			
			//on récupere le 2eme element fils qui est le l'adress de la pharmacie	
			System.out.println(link.child(1));
			String  adress=  link.child(1).text();
			transform(adress);
			System.out.println(adress);

			//on récupere le 3eme element fils qui est le num de tel de la pharmacie	
			System.out.println(link.child(2));
			String tel =  link.child(2).text();
			transform(tel);
			System.out.println(tel);

			//On récupère dans ce document la premiere balise ayant comme nom div et pour attribut class="left"
			Elements shs = doc.select("div .left"); 

			//on récupère la balise a [href] qui contient les urls des cordonnées lat et long
			Element maps = shs.select("a").get(i); 

			//Deux représentations de l'url
			String relHref = maps.attr("href"); // == "/"
			String absHref = maps.attr("abs:href"); // "http://jsoup.org/"

			//System.out.println("absHref =\t"+absHref);
			//System.out.println("relHref =\t"+relHref);

			//trouver coordonner depuis script
			Document docs = Jsoup.connect(absHref).timeout(10000).get();

			//on vise le dernier tag 'script' de la page
			Element scriptElement = docs.select("script").last();

			//charger tout le script
			String jsCode = scriptElement.html();
			//System.out.println(jsCode);

			//extraire la ligne qui nous interesse,dans ce cas les coordonées de la pharmacie
			jsCode = jsCode.substring(jsCode.indexOf('['),jsCode.indexOf(']'));	
			//System.out.println(jsCode);

			//il existe quelque pharmacies sont coordonnées et qui déclanche une exeption dans le traitement
			//ce traitement consiste à tester la présence de la bonne information sur la ligne viser par la longeur de la chaine
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

			//inserer les valeurs dans l'ordre
			Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3333/convertisseur","root", "taha");
			PreparedStatement preparedStatement = conn.prepareStatement(sql);
			preparedStatement.setString(1, nomPharmacie);
			preparedStatement.setString(2, tel);
			preparedStatement.setString(3, adress);
			preparedStatement.setString(4, absHref);
			preparedStatement.setString(5, langitude);
			preparedStatement.setString(6, langitude);
			preparedStatement.executeUpdate();

			//incrementer l'indice get(i) pour les pages url
			i++;
		}
				
		             //trouver les pharmacies de garde dans la ville d'agadir

		//On se connecte au site http://www.blanee.com ,on charge le document html 
		Document gard = Jsoup.connect("http://www.blanee.com/guides/pharmacies-de-garde-a-agadir-du-24-au-30-decembre-agadir").timeout(10*1000).get();

		//On récupère dans ce document les balise ayant comme nom div et pour attribut class="info"
		Elements gos = gard.select("div .info"); 
		for(Element go: gos){
			
			//on récupère la balise a [href] qui contient les noms des pharmacies de gardes
			String nomPharmacie2 = go.select("a[href]").get(0).text();
			
			//System.out.println(nomPharmacie2);

			//on récupère la balise a [href] qui contient les urls des cordonnées de Geolocalisation
			//String urlgard = go.select("a[href]").get(0).text();
			String urlgard = go.select("a").attr("href");
			//System.out.println(urlgard);

			//trouver coordonnés depuis url crawler
			Document phppage = Jsoup.connect("http://www.blanee.com"+urlgard).timeout(10000).get();

			//on recupere l'element adress de la pharmacie sur le 12eme tag <li>	
			Element addr = phppage.select("li").get(12);
			String adress2 = addr.text();
			transform(adress2);
			System.out.println(adress2);

			//on recupere l'element telephone de la pharmacie sur le 15eme tag <li>
			Element telp = phppage.select("li").get(15);
			String tell = telp.text();
			transform(tell);
			System.out.println(tell);

			//on vise le 7eme tag 'script' de la page pour recuperer la Geolocalisation
			Element tagscript = phppage.select("script").get(7);

			//charger tout le script
			String jsCode1 = tagscript.html();
			//System.out.println(jsCode);

			//condition sur l'existance
			if(jsCode1.length()>100){ 

				//preparation des string latitude & langitude
				latitude = jsCode1.substring(20,jsCode1.indexOf(','));
				langitude = jsCode1.substring(jsCode1.indexOf(','));
				langitude = langitude.substring(2,16);

				//System.out.println("langitude\t"+langitude);
				//System.out.println("latitude\t"+latitude);
			
			}else{
				
				//le site ne fournie pas toujour la Geolocalisation
				latitude = "00.00000";
				langitude ="00.00000";
				System.out.println("Aucune donnée de Geolocalisation disponible");
				
				//System.out.println("langitude\t"+langitude);
				//System.out.println("latitude\t"+latitude);
			
			}

			//preparation de la requet sql pour inserer dans la base de donnée
			String sql3 = "insert into garde (nom,tel,adress,URL,lati,lang)" + "values(?,?,?,?,?,?)";
			
			//inserer les valeurs dans la table
			Connection connec = DriverManager.getConnection("jdbc:mysql://localhost:3333/convertisseur","root", "taha");
			PreparedStatement preparedStatement0 = connec.prepareStatement(sql3);
			preparedStatement0.setString(1, nomPharmacie2);
			preparedStatement0.setString(2, tell);
			preparedStatement0.setString(3, adress2);
			preparedStatement0.setString(4, urlgard);
			preparedStatement0.setString(5, latitude);
			preparedStatement0.setString(6, langitude);
			preparedStatement0.executeUpdate();

		}

	}
	
}




