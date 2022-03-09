package com.tuana9a.chatserver.utils;

import javax.swing.*;
import java.awt.*;

public class Utils {
    public static void createUIs(int startX, int startY, int width, int height,
                                 int colNum, int rowNum, JFrame jFrame, JComponent[] uis, int[] code) {

        int heightEach = height / rowNum;
        int widthEach = width / colNum;
        int currentRow = 0;
        int currentCol = 0;
        int length = code.length;

        for (int i = 0; i < length; ++i) {
            if (uis[i] != null) {
                if (uis[i] instanceof JLabel) {
                    ((JLabel) uis[i]).setHorizontalAlignment(SwingConstants.CENTER);
                    uis[i].setBorder(BorderFactory.createLineBorder(Color.BLACK, 0));
                } else if (uis[i] instanceof JTextField) {
                    ((JTextField) uis[i]).setHorizontalAlignment(SwingConstants.CENTER);
                    uis[i].setBorder(BorderFactory.createLineBorder(Color.BLACK, 0));
                    uis[i].setBackground(Color.CYAN);
                } else if (uis[i] instanceof JButton) {
                    uis[i].setBorder(BorderFactory.createLineBorder(Color.BLACK, 0));
                }
                uis[i].setBounds(
                        startX + currentCol * widthEach, startY + currentRow * heightEach,
                        code[i] * widthEach, heightEach);
                jFrame.add(uis[i]);

            } else {
                JLabel spaceLabel = new JLabel();
                spaceLabel.setBounds(
                        startX + currentCol * widthEach, startY + currentRow * heightEach,
                        code[i] * widthEach, heightEach);
                spaceLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 0));
                jFrame.add(spaceLabel);
            }
            currentCol += code[i];
            if (currentCol == colNum) {
                currentCol = 0;
                currentRow++;
            }
        }
        if (startX == 0 && startY == 0) {
            jFrame.getContentPane().setPreferredSize(new Dimension(width, height));
            jFrame.pack();
        }
    }
}