package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.example.views.DisplayUI;
import org.nocrala.tools.texttablefmt.BorderStyle;
import org.nocrala.tools.texttablefmt.CellStyle;
import org.nocrala.tools.texttablefmt.ShownBorders;
import org.nocrala.tools.texttablefmt.Table;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        DisplayUI displayUI = new DisplayUI();
        List<Map<String, Object>> menuItems = new ArrayList<>();
        displayUI.displayUI();


    }
}