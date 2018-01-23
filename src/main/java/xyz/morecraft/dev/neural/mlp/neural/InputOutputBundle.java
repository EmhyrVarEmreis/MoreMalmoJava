package xyz.morecraft.dev.neural.mlp.neural;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InputOutputBundle {

    private String[] comments;
    private double[][] input;
    private double[][] output;

}
