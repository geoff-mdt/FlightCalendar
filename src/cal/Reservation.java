package cal;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.ZoneOffset;
import java.util.Arrays;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;

public class Reservation {

    private LocalTime startTime;
    private LocalTime endTime;
    private LocalDate day;
    private String avion;
    private String instructeur;

    public Reservation(String header, String avion, String instructeur) {
        // Plane name and number
        this.avion = avion;
        // Trigram
        this.instructeur = instructeur.replace(" ", "");

        // Header format : " le DD/MM/YYYY de hhHmm à hhHmm"
        String[] tab = header.split(" ");
        // Parsing string to LocalDate/Time
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/uuuu");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH'H'mm");
        day = LocalDate.parse(tab[2], dateFormatter);
        startTime = LocalTime.parse(tab[4], timeFormatter);
        endTime = LocalTime.parse(tab[6], timeFormatter);
    }

    public Reservation(Event event) {
        DateTime eventStart = event.getStart().getDateTime();
        DateTime eventEnd = event.getEnd().getDateTime();

        // Precise the used format to get time from the toStringRfc3339
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        LocalDateTime localStart = LocalDateTime.parse(eventStart.toStringRfc3339(), formatter);
        LocalDateTime localEnd = LocalDateTime.parse(eventEnd.toStringRfc3339(), formatter);
        day = localStart.toLocalDate();
        startTime = localStart.toLocalTime();
        endTime = localEnd.toLocalTime();

        String[] title = event.getSummary().split("- ");
        instructeur = title[1];
        avion = title[2];
    }

    /**
     * @return Event
     */
    public Event createEvent() {
        Event event = new Event().setSummary("Créneau PPL - " + instructeur + " - " + avion)
                .setLocation("Aérodrome Toulouse-Lasbordes, 31130 Balma");

        DateTime startDateTime = new DateTime(formatTime(day, startTime));
        EventDateTime start = new EventDateTime().setDateTime(startDateTime);
        event.setStart(start);

        DateTime endDateTime = new DateTime(formatTime(day, endTime));
        EventDateTime end = new EventDateTime().setDateTime(endDateTime);
        event.setEnd(end);

        EventReminder[] reminderOverrides = new EventReminder[] { new EventReminder().setMethod("popup").setMinutes(60),
                new EventReminder().setMethod("popup").setMinutes(1440) };
        Event.Reminders reminders = new Event.Reminders().setUseDefault(false)
                .setOverrides(Arrays.asList(reminderOverrides));
        event.setReminders(reminders);
        return event;
    }

    /**
     * Format Time from LocalDate and LocalTime as Rfc3339 normed String
     * 
     * @param date
     * @param time
     * @return String
     */
    public String formatTime(LocalDate date, LocalTime time) {
        // Format to be : 2021-04-02T16:01:00+02:00
        String formattedDateTime = date.toString() + "T" + time.toString() + ":00"
                + ZoneOffset.systemDefault().getRules().getOffset(Instant.now()).toString();
        return formattedDateTime;
    }

    /**
     * @param resa
     * @return Boolean
     */
    public Boolean compareTo(Reservation resa) {
        if (this.startTime == resa.getStartTime() && this.endTime == resa.getEndTime() && this.day == resa.getDay()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * @return startTime as a LocalTime
     */
    public LocalTime getStartTime() {
        return this.startTime;
    }

    /**
     * @return LocalTime
     */
    public LocalTime getEndTime() {
        return this.endTime;
    }

    /**
     * @return LocalDate
     */
    public LocalDate getDay() {
        return this.day;
    }

    /**
     * @return String
     */
    public String getAvion() {
        return this.avion;
    }

    /**
     * @return String
     */
    public String getInstructeur() {
        return this.instructeur;
    }

    /**
     * @return String
     */
    @Override
    public String toString() {
        return "{" + " day='" + getDay() + " startTime='" + getStartTime() + "'" + ", endTime='" + getEndTime() + "'"
                + ", '" + ", avion='" + getAvion() + "'" + ", instructeur='" + getInstructeur() + "'" + "}\n";
    }

}
