package org.scrum.psd.battleship.ascii;

import org.scrum.psd.battleship.controller.GameController;
import org.scrum.psd.battleship.controller.dto.Letter;
import org.scrum.psd.battleship.controller.dto.Position;
import org.scrum.psd.battleship.controller.dto.Ship;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.diogonunes.jcolor.Ansi.colorize;
import static com.diogonunes.jcolor.Attribute.*;

public class Main {
    private static List<Ship> myFleet;
    private static List<Ship> enemyFleet;
    private static List<Position> myMissedPositions = new ArrayList<>();
    private static List<Position> myHitPositions = new ArrayList<>();
    private static List<Position> computerPositions = new ArrayList<>();

    private static int maxGridRow = 8;
    private static char maxGridColumn = 'h';


    private static final Telemetry telemetry = new Telemetry();

    public static void main(String[] args) {
        telemetry.trackEvent("ApplicationStarted", "Technology", "Java");
        System.out.println(colorize("                                     |__", MAGENTA_TEXT()));
        System.out.println(colorize("                                     |\\/", MAGENTA_TEXT()));
        System.out.println(colorize("                                     ---", MAGENTA_TEXT()));
        System.out.println(colorize("                                     / | [", MAGENTA_TEXT()));
        System.out.println(colorize("                              !      | |||", MAGENTA_TEXT()));
        System.out.println(colorize("                            _/|     _/|-++'", MAGENTA_TEXT()));
        System.out.println(colorize("                        +  +--|    |--|--|_ |-", MAGENTA_TEXT()));
        System.out.println(colorize("                     { /|__|  |/\\__|  |--- |||__/", MAGENTA_TEXT()));
        System.out.println(colorize("                    +---------------___[}-_===_.'____                 /\\", MAGENTA_TEXT()));
        System.out.println(colorize("                ____`-' ||___-{]_| _[}-  |     |_[___\\==--            \\/   _", MAGENTA_TEXT()));
        System.out.println(colorize(" __..._____--==/___]_|__|_____________________________[___\\==--____,------' .7", MAGENTA_TEXT()));
        System.out.println(colorize("|                        Welcome to Battleship                         BB-61/", MAGENTA_TEXT()));
        System.out.println(colorize(" \\_________________________________________________________________________|", MAGENTA_TEXT()));
        System.out.println("");

        InitializeGame();

        StartGame();
    }

    private static void StartGame() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("-------------------------------------------------------------------------------");
        System.out.println("|------------------------------ENEMY-FLEET-FOUND------------------------------|");
        System.out.println("-------------------------------------------------------------------------------");

        System.out.print("\033[2J\033[;H");
        System.out.println("                  __");
        System.out.println("                 /  \\");
        System.out.println("           .-.  |    |");
        System.out.println("   *    _.-'  \\  \\__/");
        System.out.println("    \\.-'       \\");
        System.out.println("   /          _/");
        System.out.println("  |      _  /\" \"");
        System.out.println("  |     /_\'");
        System.out.println("   \\    \\_/");
        System.out.println("    \" \"\" \"\" \"\" \"");

        do {
            System.out.println("-------------------------------------------------------------------------------");
            System.out.println("|-----------------------------------PLAYER------------------------------------|");
            System.out.println("-------------------------------------------------------------------------------");
            System.out.println("Player, it's your turn");

            // display grid for user to select
            displayGrid();

            System.out.println("Enter coordinates for your shot :");
            boolean isValidShot = false;
            // check my positions already used
            Position position = null;
            while (!isValidShot)
            {
                try{
                    position = parsePosition(scanner.next());
                    isValidShot = validateShot(position);
                }
                catch (IllegalArgumentException exception){
                    System.out.println("Position cant be parsed.");
                }
            }

            boolean isHit = GameController.checkIsHit(enemyFleet, position);
            if (isHit) {
                beep();


                System.out.println("          _ ._  _ , _ ._");
                System.out.println("        (_ ' ( `  )_  .__)");
                System.out.println("      ( (  (    )   `)  ) _)");
                System.out.println("     (__ (_   (_ . _) _) ,__)");
                System.out.println("         `~~`\\ ' . /`~~`");
                System.out.println("              ;   ;");
                System.out.println("              /   \\");
                System.out.println("_____________/_ __ \\_____________");
            }

            if (isHit) {
                // add to hit positions
                myHitPositions.add(position);
                printHit("Yeah ! Nice hit !");
            }
            else{
                // add to missed positions
                myMissedPositions.add(position);
                printMiss("Miss");
            }
            telemetry.trackEvent("Player_ShootPosition", "Position", position.toString(), "IsHit", Boolean.valueOf(isHit).toString());
            
            // computer playing
            position = getRandomPosition();
            while (computerPositions.contains(position))
            {
                System.out.println("Position already used. Computer is retrying");
                position = getRandomPosition();
            }

            computerPositions.add(position);

            isHit = GameController.checkIsHit(myFleet, position);
            System.out.println("");
            System.out.println("-------------------------------------------------------------------------------");
            System.out.println("|----------------------------------COMPUTER-----------------------------------|");
            System.out.println("-------------------------------------------------------------------------------");

            if (isHit) {
                printHit(String.format("Computer shoot in %s%s and hit your ship !", position.getColumn(), position.getRow()));
            }
            else{
                printMiss(String.format("Computer shoot in %s%s and miss", position.getColumn(), position.getRow()));
            }

            telemetry.trackEvent("Computer_ShootPosition", "Position", position.toString(), "IsHit", Boolean.valueOf(isHit).toString());
            if (isHit) {
                beep();

                System.out.println("          _ ._  _ , _ ._");
                System.out.println("        (_ ' ( `  )_  .__)");
                System.out.println("      ( (  (    )   `)  ) _)");
                System.out.println("     (__ (_   (_ . _) _) ,__)");
                System.out.println("         `~~`\\ ' . /`~~`");
                System.out.println("              ;   ;");
                System.out.println("              /   \\");
                System.out.println("_____________/_ __ \\_____________");
            }
        } while (true);
    }

    private static boolean validateShot(Position position){
        List<Position> myUsedPositions = Stream.concat(myHitPositions.stream(), myMissedPositions.stream()).collect(Collectors.toList());
        if (myUsedPositions.contains(position)){
            System.out.println("Position already used. Enter new coordinates for your shot:");
            return false;
        }
        if (position.getRow() < 1 || position.getRow() > maxGridRow || position.getColumn().toString().charAt(0) > maxGridColumn){
            System.out.println("Your shot is outside the grid, try again:");
            return false;
        }
        return true;
    }

    private static void printMiss(String text){
        System.out.println(colorize(text, BLUE_TEXT()));
    }

    private static void printHit(String text){
        System.out.println(colorize(text, RED_TEXT()));
    }

    private static void displayGrid() {
        // a - h
        System.out.print("   |");
        for (char c = 'a'; c <= maxGridColumn; c++) {
            System.out.print(" " + c + "  |");
        }
        System.out.println();

        for (int i = 1; i <= maxGridRow; i++) {
            System.out.println("--------------------------------------------");

            // 1 - 8
            System.out.print(" " + i + " |");

            // board
            for (char c = 'a'; c <= maxGridColumn; c++) {
                Letter columnLetter = Letter.values()[c - 'a']; // Convert char to Letter enum
                Position current = new Position(columnLetter, i);
                if (myMissedPositions.contains(current)) {
                    System.out.print(colorize(" x ", CYAN_TEXT()));
                }
                else if (myHitPositions.contains(current)) {
                    System.out.print(colorize(" x ", RED_TEXT()));
                }
                else {
                    System.out.print("   "); // Use empty space
                }
                System.out.print(" |");
            }
            System.out.println();
        }
    }

    private static void displayShipGrid(List<Ship> enemyFleet) {
        System.out.print("   |");
        for (char c = 'a'; c <= maxGridColumn; c++) {
            System.out.print(" " + c + "  |");
        }
        System.out.println();
        for (int i = 1; i <= maxGridRow; i++) {
            System.out.println("--------------------------------------------");
            System.out.print(" " + i + " |");
            for (char c = 'a'; c <= maxGridColumn; c++) {
                Letter columnLetter = Letter.values()[c - 'a'];
                Position current = new Position(columnLetter, i);
                boolean isShipPosition = false;
                for (Ship ship : enemyFleet) {
                    for (Position shipPosition : ship.getPositions()) {
                        if (shipPosition.equals(current)) {
                            isShipPosition = true;
                            break;
                        }
                    }
                    if (isShipPosition) {
                        break;
                    }
                }
                if (isShipPosition) {
                    System.out.print(colorize(" x ", GREEN_TEXT()));
                } else {
                    System.out.print("   "); // Use empty space
                }
                System.out.print(" |");
            }
            System.out.println();
        }
    }

    private static void beep() {
        System.out.print("\007");
    }

    protected static Position parsePosition(String input) {
        Letter letter = Letter.valueOf(input.toUpperCase().substring(0, 1));
        int number = Integer.parseInt(input.substring(1));
        return new Position(letter, number);
    }

    private static Position getRandomPosition() {
        int rows = 8;
        int lines = 8;
        Random random = new Random();
        Letter letter = Letter.values()[random.nextInt(lines)];
        int number = random.nextInt(rows);
        Position position = new Position(letter, number);
        return position;
    }

    private static void InitializeGame() {
        InitializeMyFleet();

        InitializeEnemyFleet();
    }

    private static void InitializeMyFleet() {
        Scanner scanner = new Scanner(System.in);
        myFleet = GameController.initializeShips();
        System.out.println("-------------------------------------------------------------------------------");
        System.out.println("|-------------------------------COORDINATE-FLEET-------------------------------|");
        System.out.println("-------------------------------------------------------------------------------");
        System.out.println("Please position your fleet (Game board has size from A to H and 1 to 8) :");

        for (Ship ship : myFleet) {
            System.out.println("");
            System.out.println(String.format("Please enter the positions for the %s (size: %s)", ship.getName(), ship.getSize()));
            for (int i = 1; i <= ship.getSize(); i++) {
                System.out.println(String.format("Enter position %s of %s (i.e A3):", i, ship.getSize()));

                String positionInput = scanner.next();
                ship.addPosition(positionInput);
                telemetry.trackEvent("Player_PlaceShipPosition", "Position", positionInput, "Ship", ship.getName(), "PositionInShip", Integer.valueOf(i).toString());
            }
        }

        displayShipGrid(myFleet);
    }

    private static void InitializeEnemyFleet() {
        enemyFleet = GameController.initializeShips();

        enemyFleet.get(0).getPositions().add(new Position(Letter.B, 4));
        enemyFleet.get(0).getPositions().add(new Position(Letter.B, 5));
        enemyFleet.get(0).getPositions().add(new Position(Letter.B, 6));
        enemyFleet.get(0).getPositions().add(new Position(Letter.B, 7));
        enemyFleet.get(0).getPositions().add(new Position(Letter.B, 8));

        enemyFleet.get(1).getPositions().add(new Position(Letter.E, 6));
        enemyFleet.get(1).getPositions().add(new Position(Letter.E, 7));
        enemyFleet.get(1).getPositions().add(new Position(Letter.E, 8));
        enemyFleet.get(1).getPositions().add(new Position(Letter.E, 9));

        enemyFleet.get(2).getPositions().add(new Position(Letter.A, 3));
        enemyFleet.get(2).getPositions().add(new Position(Letter.B, 3));
        enemyFleet.get(2).getPositions().add(new Position(Letter.C, 3));

        enemyFleet.get(3).getPositions().add(new Position(Letter.F, 8));
        enemyFleet.get(3).getPositions().add(new Position(Letter.G, 8));
        enemyFleet.get(3).getPositions().add(new Position(Letter.H, 8));

        enemyFleet.get(4).getPositions().add(new Position(Letter.C, 5));
        enemyFleet.get(4).getPositions().add(new Position(Letter.C, 6));
    }
}
