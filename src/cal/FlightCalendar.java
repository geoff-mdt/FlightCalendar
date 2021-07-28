package cal;

import config.Site;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FlightCalendar {

    private Site site;
    /**
     * This ID is the one of the calendar of
     * the script author (to REPLACE with yours cf. Google API doc to list calendars by ID)
     */
    private static final String CALENDAR_ID = "";

    private static final String APPLICATION_NAME = "FlightCalendar";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    /**
     * Global instance of the scopes required by this quickstart. If modifying these
     * scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR);
    private static final String CREDENTIALS_FILE_PATH = "credentials.json";

    /**
     * Constructor of the FlightCalendar
     * @param site
     */
    public FlightCalendar(Site site) {
        this.site = site;
    }

    /**
     * Creates an authorized Credential object.
     * 
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = FlightCalendar.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
                clientSecrets, SCOPES)
                        .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                        .setAccessType("offline").build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    /** 
     * Update the Google Calendar of CALENDAR_ID with a list of reservations
     * @throws IOException
     * @throws GeneralSecurityException
     * @throws InterruptedException
     */
    public void update() throws IOException, GeneralSecurityException, InterruptedException {
        
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Calendar service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME).build();

        DateTime now = new DateTime(System.currentTimeMillis());

        Events events = service.events().list(CALENDAR_ID).setTimeMin(now).setOrderBy("startTime")
                .setSingleEvents(true).execute();

        List<Event> listeEvents = events.getItems();
        List<Reservation> listeReservations = this.listResa();
        System.out.println(listeReservations);

        
        for (Event event : listeEvents) {
            Reservation eventConverted = new Reservation(event);
            Boolean invariant = false;
            for (Reservation reservation : listeReservations) {
                if (eventConverted.compareTo(reservation)) {
                    invariant = true;
                }
            }
            if (!invariant) {
                service.events().delete(CALENDAR_ID, event.getId()).execute();
            }
        }
        // Check if some reservations (oflyers) do not match an google event and create a new one
        for (Reservation reservation : listeReservations) {
            Boolean invariant = false;
            for (Event event : listeEvents) {
                Reservation eventConverted = new Reservation(event);
                if (eventConverted.compareTo(reservation)) {
                    invariant = true;
                }
            }
            if (!invariant) {
                Event eventCreated = reservation.createEvent();
                eventCreated = service.events().insert(CALENDAR_ID, eventCreated).execute();
            }
        }
        System.out.println("FlightCalendar updated successfully.");
    }

    /**
     * Open the website and make a list of all user's future reservations
     * @return A list of all made reservations on the website oflyers under the form of an ArrayList<Reservation>
     * @throws InterruptedException
     */
    private ArrayList<Reservation> listResa() throws InterruptedException {

        site.connect();
        
        
        WebDriver driver = site.getDriver();

        // Wait for the main page containing a class bookDescriptor to be loaded
        site.waitClass("bookDescriptor"); 
        WebElement menu4 = driver.findElement(By.id("menu4"));
        menu4.click();

        // Waits for the booking page to charge
        site.waitClass("dateBookTitle");

        WebElement tableau = driver.findElement(By.tagName("tbody"));
        List<WebElement> lignes = tableau.findElements(By.tagName("tr"));
        ArrayList<Reservation> listeReservations = new ArrayList<Reservation>();
        
        for (WebElement ligne : lignes) {
            // Returns th and all td inside the row
            String header = ligne.findElement(By.tagName("th")).getText();
            String avion = ligne.findElement(By.xpath("td[2]")).getText();
            String instructeur = ligne.findElement(By.xpath("td[3]")).getText();
            listeReservations.add(new Reservation(header, avion, instructeur));
        }
        return listeReservations;
    }

    /** 
     * @param args
     * @throws IOException
     * @throws GeneralSecurityException
     * @throws InterruptedException
     */
    public static void main(String[] args) throws IOException, GeneralSecurityException, InterruptedException {
        Site site = new Site();
        FlightCalendar flightCalendar = new FlightCalendar(site);
        flightCalendar.update();
        site.quit();
    }
}