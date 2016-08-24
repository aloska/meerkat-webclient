import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.biotools.meerkat.Action;
import com.biotools.meerkat.Card;
import com.biotools.meerkat.GameInfo;
import com.biotools.meerkat.util.Preferences;


public class MeerkatWebClient implements com.biotools.meerkat.Player {
    private GameInfo gameInfo;
    private Preferences prefs;

    private String serverAddress;


    public JPanel getSettingsPanel() {
        JPanel panel = new JPanel();

        panel.add(new JLabel("Bot server address:"));

        final JTextField addressTextField = new JTextField(serverAddress);

        addressTextField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                prefs.setPreference("SERVER_ADDRESS", addressTextField.getText());
                serverAddress = addressTextField.getText();
            }
        });

        panel.add(addressTextField);

        return panel;
    }


    public void holeCards(Card c1, Card c2, int seat) {
        Document document = newDocument();

        Element root = document.createElement("holecards");
        document.appendChild(root);

        Element cards = document.createElement("cards");
        root.appendChild(cards);

        Element card1 = document.createElement("card");
        card1.setTextContent(c1.toString());
        cards.appendChild(card1);

        Element card2 = document.createElement("card");
        card2.setTextContent(c2.toString());
        cards.appendChild(card2);

        Element playerSeat = document.createElement("seat");
        playerSeat.setTextContent(String.valueOf(seat));
        root.appendChild(playerSeat);

        postToServer(serverAddress + "holecards", getStringFromDocument(document));
    }


    public Action getAction() {
    	String actionString = getActionStringFromServer(serverAddress + "getaction");
    	
    	if(actionString.equals("FOLD")) {
    		 return new Action(Action.FOLD, 0, 0);
    	} else if(actionString.equals("CHECK")) {
    		 return new Action(Action.CHECK, 0, 0);
    	} else if(actionString.equals("RAISE")) {
    		 return new Action(Action.RAISE, 0, 0);
    	}
    	
    	return new Action(Action.FOLD, 0, 0);
    }


    public Preferences getPreferences() {
        return prefs;
    }


    public void init(Preferences playerPrefs) { //this is called by PokerAcademy
        this.prefs = playerPrefs;
        serverAddress = prefs.getPreference("SERVER_ADDRESS", "http://localhost:9000/");
    }


    public void actionEvent(int pos, Action act) {

    }

    public void dealHoleCardsEvent() {}

    public void gameOverEvent() {}

    public void gameStartEvent(GameInfo gameInfo) {
        this.gameInfo = gameInfo;
    }

    public void gameStateChanged() {}

    public void showdownEvent(int seat, Card c1, Card c2) {}

    public void stageEvent(int stage) {}

    public void winEvent(int pos, double amount, String handName) {}


    public static void postToServer(String url, String data) {
        URL serverUrl;

        try {
            HttpURLConnection connection;
            serverUrl = new URL(url);
            connection = (HttpURLConnection) serverUrl.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "text/xml");
            connection.setDoOutput(true);
            
            OutputStream stream = connection.getOutputStream();
            PrintWriter writer = new PrintWriter(stream);
            writer.write(data);
            writer.close();
            
            connection.getResponseCode();
        } catch(MalformedURLException e) {
            JOptionPane.showMessageDialog(null, "URL is invalid!");
            e.printStackTrace();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }


    public static String getActionStringFromServer(String url) {
    	String actionString = "FOLD";
    	try {
			Document document = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder()
					.parse(url);
			actionString = document.getDocumentElement().getTextContent();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
        return actionString;
    }


    public static String getStringFromDocument(Document doc) {
        DOMSource domSource = new DOMSource(doc);
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer;
        try {
            transformer = tf.newTransformer();
            transformer.transform(domSource, result);
        } catch(TransformerConfigurationException e) {
            e.printStackTrace();
        } catch(TransformerException e) {
            e.printStackTrace();
        }

        return writer.toString();
    }

    public static Document newDocument() {
        try {
            DocumentBuilder builder;
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            return builder.newDocument();
        } catch(ParserConfigurationException e) {
            e.printStackTrace();
        }
        return null;
    }


}
