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
import org.akvo.caddisfly.model.PageIndex;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Installation related utility methods.
 */
public final class InstructionHelper {

    private InstructionHelper() {
    }

    public static int setupInstructions(List<Instruction> testInstructions,
                                        ArrayList<Instruction> instructions,
                                        PageIndex pageIndex, boolean skip) {
        int instructionIndex = 1;
        int subSequenceIndex;
        instructions.clear();
        pageIndex.clear();
        int index = 0;

        String[] subSequenceNumbers = {"i", "ii", "iii"};
        boolean alphaSequence = false;

        for (int i = 0; i < testInstructions.size(); i++) {
            Instruction instruction;
            try {
                instruction = testInstructions.get(i).clone();
                if (instruction != null) {
                    List<String> section = instruction.section;
                    boolean indent = false;
                    subSequenceIndex = 0;

                    boolean leaveOut = false;
                    if (skip) {
                        for (int i1 = 0; i1 < section.size(); i1++) {
                            String item = section.get(i1);
                            if (item.contains("~skippable~")) {
                                leaveOut = true;
                            }
                        }
                    }
                    if (leaveOut) {
                        continue;
                    }

                    instruction.setIndex(instructionIndex);

                    for (int i1 = 0; i1 < section.size(); i1++) {
                        String item = section.get(i1);

                        if (item.contains("~photo~")) {
                            pageIndex.setPhotoIndex(index);
                            if (pageIndex.getSkipToIndex() < 0) {
                                pageIndex.setSkipToIndex(index);
                            } else if (pageIndex.getSkipToIndex2() < 0) {
                                pageIndex.setSkipToIndex2(index);
                            }
                        } else if (item.contains("~input~")) {
                            pageIndex.setInputIndex(index);
                            if (pageIndex.getSkipToIndex() < 0) {
                                pageIndex.setSkipToIndex(index);
                            }
                        } else if (item.contains("~result~")) {
                            pageIndex.setResultIndex(index);
                        }

                        Matcher m = Pattern.compile("^(\\d+?\\.\\s*)(.*)").matcher(item);
                        Matcher m1 = Pattern.compile("^([a-zA-Z]\\.\\s*)(.*)").matcher(item);

                        if (subSequenceIndex > 0 || item.startsWith("i.")) {
                            section.set(i1, subSequenceNumbers[subSequenceIndex] + ". " +
                                    item.replace("i.", ""));
                            subSequenceIndex++;
                            indent = true;
                        } else if (m1.find()) {
                            section.set(i1, instructionIndex + item);
                            alphaSequence = true;
                            indent = true;
                        } else {
                            if (alphaSequence) {
                                instructionIndex++;
                                alphaSequence = false;
                            } else if (m.find()) {
                                section.set(i1, item);
                            } else if (item.startsWith("stage")) {
                                section.set(i1, item);
                                indent = true;
                            } else if (item.startsWith("~")) {
                                section.set(i1, item);
                            } else if (item.startsWith("/")) {
                                if (item.startsWith("/-")) {
                                    section.set(i1, item.substring(2));
                                } else if (indent) {
                                    section.set(i1, "." + item.substring(1));
                                } else {
                                    section.set(i1, item.substring(1));
                                }
                            } else if (!item.startsWith(".") && !item.startsWith("image:")) {
                                section.set(i1, instructionIndex + ". " + item);
                                instructionIndex++;
                                indent = true;
                            }
                        }
                    }
                }
                index++;
                instructions.add(instruction);
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }

        if (pageIndex.getSkipToIndex() < 0) {
            pageIndex.setSkipToIndex(testInstructions.size());
        }

        return instructionIndex;
    }
}
