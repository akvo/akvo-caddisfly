package org.akvo.akvoqr.ui;

import org.akvo.akvoqr.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by linda on 9/5/15.
 */
public class Instructions {


    public static List<Instruction> instructions;
    public static Map<String, Instruction> INSTRUCTION_MAP = new HashMap<String, Instruction>();

    static
    {
        instructions = new ArrayList<>();

        addInstructions(new Instruction("1", R.drawable.instruction_place_strip,"Place strip inside black area"));

        addInstructions(new Instruction("2", R.drawable.instruction_light,"Make sure light is good"));

        addInstructions(new Instruction("3", R.drawable.instruction_finder_pattern,
                "Click start button and hold device over test. Make sure all finder patterns are in sight."));

        addInstructions(new Instruction("4", R.raw.futurebeep2,
                "When you hear a sound, you can move the camera away from the test."));
    }
    private static void addInstructions(Instruction instruction)
    {
        instructions.add(instruction);
        INSTRUCTION_MAP.put(instruction.id, instruction);

    }

    public List<Instruction> getInstructions() {
        return instructions;
    }

    public static class Instruction
    {
        public String id;
        public String instruction;
        public int imageResId;

        public Instruction(String id, int imageResId, String instruction)
        {
            this.id = id;
            this.instruction = instruction;
            this.imageResId = imageResId;
        }

        public String getInstruction() {
            return instruction;
        }

        public int getImageResId() {
            return imageResId;
        }
    }
}
