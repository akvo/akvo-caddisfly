/*
 * Copyright (C) Stichting Akvo (Akvo Foundation)
 *
 * This file is part of Akvo Caddisfly.
 *
 * Akvo Caddisfly is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Akvo Caddisfly is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Akvo Caddisfly. If not, see <http://www.gnu.org/licenses/>.
 */

package org.akvo.caddisfly.helper;

import org.akvo.caddisfly.model.Instruction;
import org.akvo.caddisfly.model.TestInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Installation related utility methods.
 */
public final class InstructionHelper {

    private InstructionHelper() {
    }

    public static void setupInstructions(TestInfo testInfo, ArrayList<Instruction> instructions) {
        int instructionIndex = 1;

        instructions.clear();
        for (int i = 0; i < testInfo.getInstructions().size(); i++) {
            Instruction instruction;
            try {
                instruction = testInfo.getInstructions().get(i).clone();
                if (instruction != null) {
                    List<String> section = instruction.section;
                    boolean indent = false;
                    for (int i1 = 0; i1 < section.size(); i1++) {
                        String item = section.get(i1);
                        if (item.startsWith("/")) {
                            if (indent) {
                                section.set(i1, item.replace("/", "."));
                            } else {
                                section.set(i1, item.replace("/", ""));
                            }
                        } else if (!item.startsWith(".") && !item.startsWith("image:")) {
                            section.set(i1, instructionIndex + ". " + item);
                            instructionIndex++;
                            indent = true;
                        }
                    }
                }
                instructions.add(instruction);
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }
    }
}
