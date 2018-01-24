package xyz.morecraft.dev.neural.mlp.neural;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.FileNotFoundException;
import java.io.FileReader;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InputOutputBundle {

    private transient final static Gson gson = new GsonBuilder().create();

    private String[] comments;
    private double[][] input;
    private double[][] output;

    public static InputOutputBundle fromFile(String path) throws FileNotFoundException {
        return gson.fromJson(new FileReader(path), InputOutputBundle.class);
    }

}
