package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.zxing.WriterException;
import org.example.views.DisplayUI;


public class Main {
    public static void main(String[] args) throws WriterException {
        DisplayUI displayUI = new DisplayUI();
        List<Map<String, Object>> menuItems = new ArrayList<>();
        displayUI.displayUI();
    }
}