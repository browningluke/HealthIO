package model.persistence;

import static org.junit.jupiter.api.Assertions.*;

import model.DateCode;
import model.Day;
import model.activities.DefaultActivities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import persistence.CsvWriter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class CsvWriterTest {

    public static final String CSVHEADER = "date, mood1, mood2, sleep-time, mood1-activities, mood2-activities\n";

    CsvWriter csv;
    ArrayList<Day> dayList;

    @BeforeEach
    void createCSV() {
        dayList = new ArrayList<>();
        dayList.add(new Day(
                new DateCode(2020, 11, 12)));
        dayList.add(new Day(
                new DateCode(2020, 11, 13)));

        csv = new CsvWriter(dayList);
    }

    @Test
    void testSaveToFile() {
        csv.convertListToString();
        try {
            csv.open("./data/timeline-test.csv");
            csv.write();
            csv.close();
        } catch (IOException e) {
            fail();
        }
        assertTrue(new File("./data/timeline-test.csv").exists());
    }

    @Test
    void testConvertListToStringNoActivities() {
        csv.convertListToString();
        String csvString1 = CSVHEADER + "2020-11-12, x, x, x, , \n"
                          + "2020-11-13, x, x, x, , \n";

        assertEquals(csvString1, csv.getCsvString());

        dayList.get(0).setSleepHours(5);
        dayList.get(0).getMood(0).setMoodScore(4);

        String csvString2 = CSVHEADER + "2020-11-12, 4, x, 5, , \n"
                + "2020-11-13, x, x, x, , \n";

        csv.convertListToString();
        assertEquals(csvString2, csv.getCsvString());

        dayList.get(1).setSleepHours(8);
        dayList.get(1).getMood(1).setMoodScore(2);
        String csvString3 = CSVHEADER + "2020-11-12, 4, x, 5, , \n"
                + "2020-11-13, x, 2, 8, , \n";

        csv.convertListToString();
        assertEquals(csvString3, csv.getCsvString());


    }

    @Test
    void testConvertListToStringActivities() {
        csv.convertListToString();
        String csvString1 = CSVHEADER + "2020-11-12, x, x, x, , \n"
                + "2020-11-13, x, x, x, , \n";

        assertEquals(csvString1, csv.getCsvString());

        DefaultActivities da = DefaultActivities.getInstance();
        dayList.get(0).getMood(0).addActivity(
                da.getActivity("Gaming"));
        dayList.get(0).getMood(0).addActivity(
                da.getActivity("Friends")
        );

        csv.convertListToString();
        String csvString2 = CSVHEADER + "2020-11-12, x, x, x, Gaming;Friends;, \n"
                + "2020-11-13, x, x, x, , \n";


        assertEquals(csvString2, csv.getCsvString());

        dayList.get(1).getMood(0).addActivity(
                da.getActivity("Friends"));
        dayList.get(1).getMood(1).addActivity(
                da.getActivity("Party"));
        dayList.get(1).getMood(1).addActivity(
                da.getActivity("Music")
        );

        csv.convertListToString();
        String csvString3 = CSVHEADER + "2020-11-12, x, x, x, Gaming;Friends;, \n"
                + "2020-11-13, x, x, x, Friends;, Party;Music;\n";

        assertEquals(csvString3, csv.getCsvString());

    }

}
