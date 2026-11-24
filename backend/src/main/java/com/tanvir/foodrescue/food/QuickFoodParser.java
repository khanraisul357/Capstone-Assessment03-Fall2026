package com.tanvir.foodrescue.food;

import org.springframework.stereotype.Component;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class QuickFoodParser {

    public static class ParsedFood {
        public int estimatedPortions;
        public boolean hasTrays;
        public String holdUntilText;
        public int holdUntilMinutes;
        public String pickupNote;
    }

    public ParsedFood parse(String rawText) {
        ParsedFood result = new ParsedFood();
        String lower = rawText == null ? "" : rawText.toLowerCase();

        // 1. Detect trays
        result.hasTrays = lower.contains("tray");

        // 2. Estimate portions from numbers before plates/portions/packs + trays
        int portions = 0;
        Matcher plateMatcher = Pattern.compile("(\d+)\s*(plates?|portions?|packets?|packs?|boxes?)").matcher(lower);
        while (plateMatcher.find()) {
            portions += Integer.parseInt(plateMatcher.group(1));
        }
        Matcher trayMatcher = Pattern.compile("(\d+)\s*trays?").matcher(lower);
        while (trayMatcher.find()) {
            portions += Integer.parseInt(trayMatcher.group(1)) * 15; // ~15 portions per catering tray
        }
        if (portions == 0) {
            Matcher anyNum = Pattern.compile("(\d+)").matcher(lower);
            if (anyNum.find()) {
                int candidate = Integer.parseInt(anyNum.group(1));
                if (candidate >= 5 && candidate <= 300) {
                    portions = candidate;
                }
            }
        }
        result.estimatedPortions = portions > 0 ? portions : 25;

        // 3. Extract time like 11:30pm, 10pm, 11:00 pm
        Matcher timeMatcher = Pattern.compile("(\d{1,2})(?::(\d{2}))?\s*(am|pm)").matcher(lower);
        if (timeMatcher.find()) {
            int hour = Integer.parseInt(timeMatcher.group(1));
            int min = timeMatcher.group(2) != null ? Integer.parseInt(timeMatcher.group(2)) : 0;
            String ampm = timeMatcher.group(3);
            if ("pm".equals(ampm) && hour != 12) hour += 12;
            if ("am".equals(ampm) && hour == 12) hour = 24; // Midnight closing
            result.holdUntilMinutes = hour * 60 + min;
            result.holdUntilText = timeMatcher.group(0).toUpperCase();
        } else {
            result.holdUntilMinutes = 23 * 60 + 30; // Default 11:30 PM
            result.holdUntilText = "11:30 PM";
        }

        // 4. Pickup note hint
        if (lower.contains("back gate")) {
            result.pickupNote = "Back gate";
        } else if (lower.contains("kitchen")) {
            result.pickupNote = "Kitchen door";
        } else if (lower.contains("front")) {
            result.pickupNote = "Front counter";
        } else {
            result.pickupNote = "Main entrance / check note";
        }

        return result;
    }
}
