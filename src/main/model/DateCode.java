package model;

// Represents a specific date, which can be used as an ID for the Day object.
public class DateCode {

    private final int date;     // The date of the month that the DateCode represents.
    private final int month;    // The month of the year that the DateCode represents.
    private final int year;     // The year that the DateCode represents.


    // MODIFIES: this
    // EFFECTS: creates a new DateCode instance with a specified year, month and date.
    public DateCode(int year, int month, int date) {
        this.date = date;
        this.month = month;
        this.year = year;
    }

    // MODIFIES: this
    // EFFECTS: creates a new DateCode instance by parsing a DateCode string (yyyy-mm-dd).
    public DateCode(String dateCodeString) {
        String[] dateCodeParts = dateCodeString.split("-");

        year = Integer.parseInt(dateCodeParts[0]);
        month = Integer.parseInt(dateCodeParts[1]);
        date = Integer.parseInt(dateCodeParts[2]);
    }

    @Override
    // EFFECTS: returns the hashCode of this object, calculated to be an integer but look like yyyymmdd.
    public int hashCode() {
        return (10000 * year) + (100 * month) + date;
    }

    @Override
    // EFFECTS: returns the true if obj is equal to this object.
    //          true if obj is the same object or all id variables (date, month, year)
    //          are the the same for both this and obj.
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }

        DateCode dc = (DateCode) obj;

        return dc.date == date && dc.month == month && dc.year == year;
    }

    // EFFECTS: returns the full DateCode converted to a string,
    //          using the following pattern: yyyy-mm-dd
    public String toString() {
        return year
                + "-" + formatMonthAndDate(month)
                + "-" + formatMonthAndDate(date);
    }

    // EFFECTS: returns a partial DateCode string,
    //          using the following pattern: dd/mm
    public String getDateAndMonth() {
        return formatMonthAndDate(date) + "/" + formatMonthAndDate(month);
    }

    // REQUIRES: i is a valid date (32 > i > 0)
    // EFFECTS: pads a single digit string to 2 digits
    //          using zeros: (1 -> 01)
    private String formatMonthAndDate(Integer i) {
        String convertedString = Integer.toString(i);

        if (convertedString.length() == 1) {
            return "0" + convertedString;
        }
        return convertedString;
    }

    // EFFECTS: returns the date as an integer.
    public int getDate() {
        return date;
    }

    // EFFECTS: returns the month as an integer.
    public int getMonth() {
        return month;
    }

    // EFFECTS: returns the year as an integer.
    public int getYear() {
        return year;
    }

}
