package test;

import xyz.morecraft.dev.malmo.util.MazeGenerator;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

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

        Double[] a = new Double[]{1.0, 2.0, 3.0};
        Double[] b = new Double[]{1.0, 2.0, 3.0};
        Set<Double[]> s = new TreeSet<>((o1, o2) -> {
            HashSet<Double> set1 = new HashSet<>(Arrays.asList(o1));
            HashSet<Double> set2 = new HashSet<>(Arrays.asList(o2));
            return set1.equals(set2) ? 0 : 1;
        });
        s.add(a);
        s.add(b);
        System.out.println(s.size());
    }

}
