package test;

import xyz.morecraft.dev.malmo.util.MazeGenerator;

public class Maze {

    public static void main(String[] args) {
        int x = args.length >= 1 ? (Integer.parseInt(args[0])) : 5;
        int y = args.length == 2 ? (Integer.parseInt(args[1])) : 7;
        MazeGenerator maze = new MazeGenerator(x, y);
        boolean[][] m = maze.convertMaze();
        for (boolean[] row : m) {
            for (boolean b : row) {
                System.out.print(b ? 'X' : ' ');
            }
            System.out.println();
        }
    }

}
