package mealplanner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MealPlanner {
    static Connection connection = null;
    static int mealId = 0;

    static Scanner scanner = new Scanner(System.in);

    public static void run() throws SQLException {
        String DB_URL = "jdbc:postgresql:meals_db";
        String USER = "postgres";
        String PASS = "1111";

        connection = DriverManager.getConnection(DB_URL, USER, PASS);
        connection.setAutoCommit(true);

        Statement statement = connection.createStatement();
//        statement.executeUpdate("drop table if exists meals");
        statement.executeUpdate("create table if not exists meals (" +
                "category varchar(1024) NOT NULL," +
                "meal varchar(1024) NOT NULL," +
                "meal_id integer NOT NULL" +
                ")");

//        statement.executeUpdate("drop table if exists ingredients");
        statement.executeUpdate("create table if not exists ingredients (" +
                "ingredient varchar(1024) NOT NULL," +
                "ingredient_id integer NOT NULL," +
                "meal_id integer NOT NULL" +
                ")");

//        statement.executeUpdate("drop table if exists plan");
        statement.executeUpdate("create table if not exists plan (" +
                "meal varchar(1024) NOT NULL," +
                "category varchar(1024) NOT NULL," +
                "meal_id integer NOT NULL" +
                ")");

        ResultSet rs = statement.executeQuery("select * from meals order by meal_id desc limit 1");
        while (rs.next()) {
            mealId = rs.getInt("meal_id");
        }
        statement.close();

        while (true) {
            askAction();
        }
    }

    public static void askAction() throws SQLException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("What would you like to do (add, show, plan, save, exit)?");
        switch (scanner.nextLine()) {
            case "add" -> add();
            case "show" -> show();
            case "plan" -> plan();
            case "save" -> save();
            case "exit" -> {
                System.out.println("Bye!");
                connection.close();
                System.exit(0);
            }
        }
    }

    public static void add() throws SQLException {
        System.out.println("Which meal do you want to add (breakfast, lunch, dinner)?");
        String category = askCategory();

        String mealName = null;
        while (mealName == null || !mealName.matches("[A-Za-z\\s]+(,\\s*[A-Za-z\\s]+)*")) {
            if (mealName != null) System.out.println("Wrong format. Use letters only!");
            System.out.println("Input the meal's name:");
            mealName = scanner.nextLine();
        }

        String ingredientsRegex = "[A-Za-z\\s]+(,[A-Za-z\\s]+)*";
        System.out.println("Input the ingredients:");
        String ingredients = scanner.nextLine();
        while (!ingredients.matches(ingredientsRegex)) {
            System.out.println("Wrong format. Use letters only!");
            ingredients = scanner.nextLine();
        }

        String[] ingredientsArray = ingredients.split(",\\s|,");
        System.out.println("The meal has been added!");
        mealId++;

        Statement statement = connection.createStatement();
        statement.executeUpdate(String.format("insert into meals (category, meal, meal_id) values ('%s', '%s', %d)", category, mealName, mealId));
        for (
                int i = 0;
                i < ingredientsArray.length; i++) {
            statement.executeUpdate(String.format("insert into ingredients (ingredient, ingredient_id, meal_id) values ('%s', '%d', %d)", ingredientsArray[i], i + 1, mealId));
        }
        statement.close();
    }

    public static void show() throws SQLException {
        System.out.println("Which category do you want to print (breakfast, lunch, dinner)?");
        String category = askCategory();

        Statement statement1 = connection.createStatement();
        ResultSet rs1 = statement1.executeQuery("SELECT * FROM meals WHERE category = " + "'" + category + "'");

        if (!rs1.next()) {
            System.out.println("No meals found.");
        } else {
            System.out.println("Category: " + category + "\n");
            do {
                printMeals(rs1);
            } while (rs1.next());
        }

        rs1.close();
        statement1.close();
    }

    private static String askCategory() {
        String category = null;
        boolean proceed = false;
        while (!proceed) {
            switch (scanner.nextLine()) {
                case "breakfast" -> {
                    category = "breakfast";
                    proceed = true;
                }
                case "lunch" -> {
                    category = "lunch";
                    proceed = true;
                }
                case "dinner" -> {
                    category = "dinner";
                    proceed = true;
                }
                default -> System.out.println("Wrong meal category! Choose from: breakfast, lunch, dinner.");
            }
        }
        return category;
    }

    private static void printMeals(ResultSet rs1) throws SQLException {
        int mealId = rs1.getInt("meal_id");
        System.out.println("Name: " + rs1.getString("meal"));
        System.out.println("Ingredients:");

        Statement statement2 = connection.createStatement();
        ResultSet rs2 = statement2.executeQuery("select ingredient from ingredients where meal_id = " + rs1.getInt("meal_id"));

        while (rs2.next()) {
            System.out.println(rs2.getString("ingredient"));
        }

        rs2.close();
        statement2.close();

        System.out.println();
    }

    private static void save() throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet rs1 = statement.executeQuery("SELECT * FROM plan");

        if (!rs1.next()) {
            System.out.println("Unable to save. Plan your meals first.");
            statement.close();
        } else {
            statement.close();

            System.out.println("Input a filename:");
            String fileName = scanner.nextLine();
            File file = new File(fileName);
            PrintWriter writer = null;
            try {
                writer = new PrintWriter(file);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }

            statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(
                    "SELECT i.ingredient, COUNT(i.ingredient_id) AS ingredient_count " +
                            "FROM ingredients i " +
                            "JOIN plan p ON i.meal_id = p.meal_id " +
                            "GROUP BY i.ingredient"
            );

            while (resultSet.next()) {
                String ingredient = resultSet.getString("ingredient");
                int ingredientCount = resultSet.getInt("ingredient_count");
                if(ingredientCount == 1) {
                    writer.println(ingredient);
                } else {
                writer.println(ingredient + " x" + ingredientCount);
                }
            }
            writer.close();
            resultSet.close();
            statement.close();
            System.out.println("Saved!");
        }
    }


    private static void plan() throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate("truncate table plan");
        statement.close();

        planDay("Monday");
        planDay("Tuesday");
        planDay("Wednesday");
        planDay("Thursday");
        planDay("Friday");
        planDay("Saturday");
        planDay("Sunday");

        showPlan();
    }

    private static void showPlan() throws SQLException {
        Statement statement1 = connection.createStatement();
        ResultSet rs1 = statement1.executeQuery("SELECT meal FROM plan");


        if (rs1 != null) {
            List<String> days = List.of("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday");
            for (String day : days) {
                System.out.println(day);

                System.out.print("Breakfast: ");
                rs1.next();
                System.out.println(rs1.getString("meal"));
                System.out.println();

                System.out.print("Lunch: ");
                rs1.next();
                System.out.println(rs1.getString("meal"));
                System.out.println();

                System.out.print("Dinner: ");
                rs1.next();
                System.out.println(rs1.getString("meal"));
                System.out.println();
            }
        }
    }

    private static void planDay(String day) throws SQLException {
        System.out.println(day);
        String breakfastMeal = planMealForCategory(day, "breakfast");
        Statement statement = connection.createStatement();
        ResultSet rsOfMealId = statement.executeQuery("select meal_id from meals WHERE meal = '" + breakfastMeal + "'");
        if (rsOfMealId.next()) {
            statement.executeUpdate(String.format("insert into plan (meal, category, meal_id) values ('%s', '%s', %d)", breakfastMeal, "breakfast", rsOfMealId.getInt("meal_id")));
        }
        statement.close();

        String lunchMeal = planMealForCategory(day, "lunch");
        statement = connection.createStatement();
        rsOfMealId = statement.executeQuery("select meal_id from meals WHERE meal = '" + lunchMeal + "'");
        if (rsOfMealId.next()) {
            statement.executeUpdate(String.format("insert into plan (meal, category, meal_id) values ('%s', '%s', %d)", lunchMeal, "lunch", rsOfMealId.getInt("meal_id")));
        }
        statement.close();

        String dinnerMeal = planMealForCategory(day, "dinner");
        statement = connection.createStatement();
        rsOfMealId = statement.executeQuery("select meal_id from meals WHERE meal = '" + dinnerMeal + "'");
        if (rsOfMealId.next()) {
            statement.executeUpdate(String.format("insert into plan (meal, category, meal_id) values ('%s', '%s', %d)", dinnerMeal, "dinner", rsOfMealId.getInt("meal_id")));
        }
        statement.close();

        System.out.println("Yeah! We planned the meals for " + day + ".");
    }

    private static String planMealForCategory(String day, String category) throws SQLException {
        Statement statement1 = connection.createStatement();
        ResultSet rs1 = statement1.executeQuery("SELECT * FROM meals WHERE category = " + "'" + category + "' ORDER BY meal");

        List<String> mealsList = new ArrayList<>();

        if (!rs1.next()) {
            System.out.println("No meals found.");
        } else {
            do {
                mealsList.add(rs1.getString("meal"));
                printMealNames(rs1);
            } while (rs1.next());
        }

        System.out.println("Choose the " + category + " for " + day + " from the list above:");

        rs1.close();
        statement1.close();

        String choice;
        while (true) {
            choice = scanner.nextLine();

            boolean valueEqualsMeal = mealsList.stream().anyMatch(choice::equals);
            if (valueEqualsMeal) break;

            System.out.println("This meal doesn’t exist. Choose a meal from the list above.");
        }

        return choice;
    }

    private static void printMealNames(ResultSet rs1) throws SQLException {
        int mealId = rs1.getInt("meal_id");
        System.out.println(rs1.getString("meal"));
    }

}