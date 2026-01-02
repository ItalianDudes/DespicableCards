package it.italiandudes.despicable_cards.utils;

import it.italiandudes.despicable_cards.javafx.JFXDefs;
import javafx.scene.image.Image;

import java.util.Calendar;

public final class CalendarEventManager {

    private static final Calendar CALENDAR = Calendar.getInstance();

    // Event Checkers
    public static boolean isHalloweenEvent() {
        int month = CALENDAR.get(Calendar.MONTH);
        int day = CALENDAR.get(Calendar.DAY_OF_MONTH);
        return month == Calendar.OCTOBER && day >= 25;
    }
    public static boolean isXmasEvent() {
        int month = CALENDAR.get(Calendar.MONTH);
        int day = CALENDAR.get(Calendar.DAY_OF_MONTH);
        return month == Calendar.DECEMBER || (month == Calendar.JANUARY && day <= 8);
    }

    // Logo Selector
    public static Image getEventLogo() {
        if (isXmasEvent()) {
            return new Image(Defs.Resources.get(JFXDefs.Resources.Image.Logo.LOGO_XMAS).toString());
        } else if (isHalloweenEvent()) {
            return new Image(Defs.Resources.get(JFXDefs.Resources.Image.Logo.LOGO_HALLOWEEN).toString());
        } else {
            return new Image(Defs.Resources.get(JFXDefs.Resources.Image.Logo.LOGO_MAIN).toString());
        }
    }
}
