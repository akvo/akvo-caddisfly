package org.akvo.akvoqr.instructions_app;

import android.content.res.TypedArray;

import org.akvo.akvoqr.R;
import org.akvo.akvoqr.util.App;

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
    private static String[] instruction_text;
    private static TypedArray instruction_res;

    static
    {
        instruction_text = App.getMyApplicationContext().getResources().getStringArray(R.array.instructions);
        instruction_res = App.getMyApplicationContext().getResources().obtainTypedArray(R.array.instructions_res);

        instructions = new ArrayList<>();

        int res;
        for(int i=0;i < instruction_text.length;i++)
        {
            res = instruction_res.getResourceId(i, -1);

            addInstructions(new Instruction(String.valueOf(i+1), res, instruction_text[i]));
        }
        instruction_res.recycle();
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
