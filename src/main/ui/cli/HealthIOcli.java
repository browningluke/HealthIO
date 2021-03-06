package ui.cli;

import model.Mood;
import model.Timeline;
import model.activities.Activity;
import model.activities.DefaultActivities;
import persistence.CsvWriter;
import persistence.JsonReader;
import persistence.JsonWriter;
import ui.cli.enums.*;
import ui.cli.views.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

// The main Window that controls when displaying views and handling commands, when running from the CLI.
public class HealthIOcli {

    public static final String PROJECTNAME = "HealthIO";       // Contains the static application name
    public static final String JSONSTORE = "./data/timeline.json"; // The location of the JSON file.
    public static final String CSVSTORE = "./data/timeline.csv";   // The location of the CSV file.

    private Timeline timeline;                              // Timeline for storing Days and moving around.
    private Window currentWindow;                           // The currently selected window (combination of views).

    private JsonWriter jsonWriter;                          // The json writer to save timeline to file.
    private JsonReader jsonReader;                          // The json reader to read timeline from file.

    // MODIFIES: this
    // EFFECTS: creates a new Timeline and sets the current window to the Summary View.
    public HealthIOcli() {
        timeline = new Timeline();
        currentWindow = Window.MAIN;

        jsonWriter = new JsonWriter(JSONSTORE);
        jsonReader = new JsonReader(JSONSTORE);

        runHealthIO();
    }

    // MODIFIES: this
    // EFFECTS: starts the infinite loop and runs the application.
    public void runHealthIO() {
        while (true) {
            drawUI(); // Can exit when user enters "q", thus calls System.exit();
        }
    }

    // EFFECTS: calls the necessary functions to determine which commands can be called,
    //          print these on the screen, and then handle user input.
    public void drawUI() {
        drawViews();

        ArrayList<Actions> availableActions = determineAvailableCommands();
        printCommands(availableActions);
        handleCommands(availableActions);
    }

    // EFFECTS: create a new instance of one of the AbstractView subclasses depending on which
    //          window we have selected, then draw it.
    private void drawViews() {
        switch (currentWindow) {
            case MAIN:
                new SummaryView(timeline, SelectedStat.NONE).drawView();
                break;
            case EDIT_MOOD1:
                new EditMoodView(timeline, SelectedStat.MOOD1).drawView();
                break;
            case EDIT_MOOD2:
                new EditMoodView(timeline, SelectedStat.MOOD2).drawView();
                break;
            case EDIT_SLEEP:
                new EditSleepView(timeline, SelectedStat.SLEEP).drawView();
                break;
        }
    }

    /*
        Determining Actions
     */

    // EFFECTS: returns a list of Actions that determine what the user is able to do
    //          on the selected window.
    private ArrayList<Actions> determineAvailableCommands() {
        ArrayList<Actions> actions = new ArrayList<>();

        if (currentWindow == Window.MAIN) {
            actions.addAll(determineMainWindowCommands());
        } else if (currentWindow == Window.EDIT_MOOD1
                || currentWindow == Window.EDIT_MOOD2) {
            actions.add(Actions.UPDATEMOODVALUE);
            actions.add(Actions.UPDATEACTIVITIES);
            actions.add(Actions.BACK);
        } else if (currentWindow == Window.EDIT_SLEEP) {
            actions.add(Actions.UPDATESLEEPVALUE);
            actions.add(Actions.BACK);
        }
        return actions;
    }

    // EFFECTS: returns a list of Actions that determine what the user is able to do
    //          on the main window. Helper method for determineAvailableCommands.
    private ArrayList<Actions> determineMainWindowCommands() {
        ArrayList<Actions> actions = new ArrayList<>();

        actions.add(Actions.EDITSTATS);
        actions.add(Actions.SAVE);
        actions.add(Actions.LOAD);
        actions.add(Actions.DELETE);
        actions.add(Actions.EDITSTATS);
        actions.add(Actions.EXPORT);

        if (timeline.canGoBackOneDay()) {
            actions.add(Actions.GOBACKWARD);
        }

        if (timeline.canGoForwardOneDay()) {
            actions.add(Actions.GOFORWARD);
        }

        return actions;
    }

    /*
        Printing UI
     */

    // EFFECTS: print all available commands to the screen.
    private void printCommands(ArrayList<Actions> availableActions) {
        String message = "";

        if (availableActions.contains(Actions.UPDATEMOODVALUE)) {
            message += "Press u to update the mood value\n";
        }

        if (availableActions.contains(Actions.UPDATEACTIVITIES)) {
            message += "Press a to edit activities.\n";
        }

        if (availableActions.contains(Actions.UPDATESLEEPVALUE)) {
            message += "Press u to update the sleep value\n";
        }

        if (availableActions.contains(Actions.BACK)) {
            message += "Press b to go back to the main menu\n";
        }

        if (currentWindow == Window.MAIN) {
            message += printMainWindowCommands(availableActions);
        }

        message += "Press q to quit\n";
        System.out.println(message);
    }

    // EFFECTS: print all available commands relating to the Main (Summary) window to the screen.
    private String printMainWindowCommands(ArrayList<Actions> availableActions) {
        String message = "";

        message += "Press < to " + (availableActions.contains(Actions.GOBACKWARD)
               ? "go back one day.\n" : "create a day one day back.\n");

        message += "Press > to " + (availableActions.contains(Actions.GOFORWARD)
                ? "go forward one day.\n" : "create a day one day forward.\n");

        if (availableActions.contains(Actions.EDITSTATS)) {
            message += "Press 1 to edit mood 1.\n"
                    + "Press 2 to edit mood 2. \n"
                    + "Press 3 to edit sleep. \n";
        }

        if (availableActions.contains(Actions.SAVE)) {
            message += "Press s to save to file.\n";
        }

        if (availableActions.contains(Actions.LOAD)) {
            message += "Press l to load from file.\n";
        }

        if (availableActions.contains(Actions.DELETE)) {
            message += "Press d to delete to file.\n";
        }

        if (availableActions.contains(Actions.EXPORT)) {
            message += "Press x to export to csv.\n";
        }

        return message;
    }

    /*
        Handlers
     */

    // EFFECTS: determines which command to run based on available actions and user input.
    //          Quit is always available, and other helper functions are called.
    private void handleCommands(ArrayList<Actions> availableActions) {
        Scanner in = new Scanner(System.in);
        String s = in.nextLine();

        if (s.equals("q")) {
            saveTimeline(); // Save on quit.
            System.exit(0); // Exit no matter what
        }

        handleMovement(availableActions, s);
        handleUpdateValues(availableActions, s);
        handleIO(availableActions, s);
    }

    // MODIFIES: this, timeline
    // EFFECTS: if the user entered a movement command, either move around the timeline or
    //          call a helper function to change active window.
    private void handleMovement(ArrayList<Actions> availableActions, String s) {
        if (currentWindow == Window.MAIN && s.equals("<")) {
            if (!availableActions.contains(Actions.GOBACKWARD)) {
                timeline.createDayOneDayBack();
            }
            timeline.goBackOneDay();

        } else if (currentWindow == Window.MAIN && s.equals(">")) {
            if (!availableActions.contains(Actions.GOFORWARD)) {
                timeline.createDayOneDayForward();
            }
            timeline.goForwardOneDay();
        } else {
            handleChangeWindow(availableActions, s);
        }
    }

    // MODIFIES: this, timeline
    // EFFECTS: if the user entered a movement command, change active window.
    private void handleChangeWindow(ArrayList<Actions> availableActions, String s) {
        if (availableActions.contains(Actions.BACK) && s.equals("b")) {
            currentWindow = Window.MAIN;
        } else if (availableActions.contains(Actions.EDITSTATS)) {
            switch (s) {
                case "1":
                    currentWindow = Window.EDIT_MOOD1;
                    break;
                case "2":
                    currentWindow = Window.EDIT_MOOD2;
                    break;
                case "3":
                    currentWindow = Window.EDIT_SLEEP;
                    break;
            }
        }
    }

    // EFFECTS: if the user entered an update command, move to the appropriate update window.
    private void handleUpdateValues(ArrayList<Actions> availableActions, String s) {
        if (availableActions.contains(Actions.UPDATEMOODVALUE) && s.equals("u")) {
            handleEditMood((currentWindow == Window.EDIT_MOOD1) ? 0 : 1);

        } else if (availableActions.contains(Actions.UPDATESLEEPVALUE) && s.equals("u")) {
            handleEditSleep();

        } else if (availableActions.contains(Actions.UPDATEACTIVITIES) && s.equals("a")) {
            handleEditActivities((currentWindow == Window.EDIT_MOOD1) ? 0 : 1);
        }
    }

    // MODIFIES: this, timeline
    // EFFECTS: prompts the user to enter a new mood value for the currently selected mood,
    //          will keep looping until user enters value between 1 and 5.
    private void handleEditMood(int moodIndex) {
        Scanner in =  new Scanner(System.in);
        System.out.println("Please enter a new mood score (between 1 and 5): ");
        String ms = in.nextLine();
        if (Integer.parseInt(ms) >= 1 && Integer.parseInt(ms) <= 5) {
            timeline.getDay().getMood(moodIndex).setMoodScore(Integer.parseInt(ms));
            return;
        }
        handleEditMood(moodIndex);
    }

    // MODIFIES: this, timeline
    // EFFECTS: prompts the user to enter a new sleep value for selected day,
    //          will keep looping until user enters value between 0 and 24.
    private void handleEditSleep() {
        Scanner in =  new Scanner(System.in);
        System.out.println("Please enter a new sleep time: ");
        String sleep = in.nextLine();
        if (Integer.parseInt(sleep) >= 0 && Integer.parseInt(sleep) <= 24) {
            timeline.getDay().setSleepHours(Integer.parseInt(sleep));
            return;
        }
        handleEditSleep();
    }

    // MODIFIES: this, timeline
    // EFFECTS: prompts the user to enter a comma separated list of activities they wish
    //          to toggle. Will keep looping until user enters valid activity.
    private void handleEditActivities(int moodIndex) {
        Scanner in =  new Scanner(System.in);
        System.out.println("Please enter all activities you wish to toggle, "
                + "separated by a comma: ");
        String activities = in.nextLine();
        activities = activities.replace(" ", "");

        String[] activitiesList = activities.split(",");

        for (String s : activitiesList) {
            Activity newActivity = DefaultActivities.getInstance().getActivity(s);

            if (newActivity == null) {
                System.out.println("You entered an invalid activity, try again.");
                handleEditActivities(moodIndex);
                return;
            }

            Mood selectedMood = timeline.getDay().getMood(moodIndex);

            if (selectedMood.containsActivity(newActivity.getActivityName())) {
                selectedMood.removeActivity(newActivity.getActivityName());
                continue;
            }

            selectedMood.addActivity(newActivity);
        }
    }

    // MODIFIES: this
    // EFFECTS: if the user entered a valid IO command, call the appropriate helper method.
    private void handleIO(ArrayList<Actions> availableActions, String s) {
        if (availableActions.contains(Actions.EXPORT) && s.equals("x")) {
            saveToCSV();
        } else if (availableActions.contains(Actions.SAVE) && s.equals("s")) {
            saveTimeline();
        } else if (availableActions.contains(Actions.LOAD) && s.equals("l")) {
            loadTimeline();
        } else if (availableActions.contains(Actions.DELETE) && s.equals("d")) {
            deleteTimeline();
        }
    }

    /*
        Persistence
     */

    // EFFECTS: gets a new CSV object from timeline, generates the csv string
    //          and then prints the result to a file at CSVSTORE.
    private void saveToCSV() {
        CsvWriter exportCSV = timeline.getCsvWriter();
        exportCSV.convertListToString();
        try {
            exportCSV.open(CSVSTORE);
            exportCSV.write();
            exportCSV.close();
        } catch (IOException e) {
            System.out.println("Unable to export to file.");
        }

        //System.out.println(exportCSV.save());
    }

    // EFFECTS: saves the Timeline instance to a file at JSONSTORE.
    private void saveTimeline() {
        try {
            jsonWriter.open();
            jsonWriter.write(timeline);
            jsonWriter.close();
            System.out.println("Saved timeline to: " + JSONSTORE);
        } catch (FileNotFoundException f) {
            System.out.println("Unable to write to file: " + JSONSTORE);
        }
    }

    // EFFECTS: loads the Timeline instance from a file at JSONSTORE.
    private void loadTimeline() {
        try {
            timeline = jsonReader.read();
            System.out.println("Loaded timeline from: " + JSONSTORE);
        } catch (IOException e) {
            System.out.println("Unable to read from file: " + JSONSTORE);
        }
    }

    // MODIFIES: this
    // EFFECTS: saves a blank string to a file at JSONSTORE,
    //          and creates a new timeline instance.
    private void deleteTimeline() {
        try {
            jsonWriter.open();
            jsonWriter.close();
            timeline = new Timeline();
            System.out.println("Deleted timeline at: " + JSONSTORE + "\nand created new one.");
        } catch (FileNotFoundException f) {
            System.out.println("Unable to delete timeline at: " + JSONSTORE);
        }
    }

    public static void main(String[] args) {
        new HealthIOcli();
    }
}
