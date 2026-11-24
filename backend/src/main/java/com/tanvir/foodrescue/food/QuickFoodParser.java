package com.tanvir.foodrescue.food;

import com.tanvir.foodrescue.util.TimeUtil;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Turns Tanvir's own words into structured data, e.g.
 * "about 30 plates rice and dal, 2 trays chicken curry, can hold until 11:30pm, pickup from back gate"
 */
@Component
public class QuickFoodParser {

    static final int PORTIONS_PER_TRAY = 15;
    static final int DEFAULT_PORTIONS = 25;
    static final int DEFAULT_HOLD_MINUTES = 23 * 60 + 30;

    private static final Pattern PLATES =
            Pattern.compile("(\\d+)\\s*(?:plates?|portions?|packets?|packs?|boxes?|servings?|meals?)");
    private static final Pattern TRAYS =
            Pattern.compile("(\\d+)\\s*(?:large\\s+|big\\s+|full\\s+)?trays?");
    private static final Pattern NO_TRAYS =
            Pattern.compile("\\b(?:no|without|zero|0)\\s+trays?");
    private static final Pattern ANY_TIME_AMPM =
            Pattern.compile("(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm)");
    private static final Pattern HOLD_KEYWORD =
            Pattern.compile("\\b(?:until|till|upto|up to|by|before)\\s*(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm)?");
    private static final Pattern PICKUP =
            Pattern.compile("pick\\s?-?up\\s*(?:from|at|:)?\\s*([^,.;]+)");

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

        // 1. Trays ("no trays" / "without trays" must NOT count as trays)
        boolean negated = NO_TRAYS.matcher(lower).find();
        result.hasTrays = !negated && lower.contains("tray");

        // 2. Portions = plates/packs + trays * 15
        int portions = 0;
        Matcher plates = PLATES.matcher(lower);
        while (plates.find()) portions += Integer.parseInt(plates.group(1));
        if (result.hasTrays) {
            Matcher trays = TRAYS.matcher(lower);
            boolean foundQty = false;
            while (trays.find()) {
                portions += Integer.parseInt(trays.group(1)) * PORTIONS_PER_TRAY;
                foundQty = true;
            }
            if (!foundQty) portions += PORTIONS_PER_TRAY; // "trays of curry" -> assume one tray
        }
        if (portions == 0) {
            // last resort: first plain number, ignoring anything that is part of a clock time
            String withoutTimes = ANY_TIME_AMPM.matcher(lower).replaceAll(" ");
            Matcher any = Pattern.compile("(\\d+)").matcher(withoutTimes);
            if (any.find()) {
                int candidate = Integer.parseInt(any.group(1));
                if (candidate >= 5 && candidate <= 300) portions = candidate;
            }
        }
        result.estimatedPortions = portions > 0 ? portions : DEFAULT_PORTIONS;

        // 3. Hold-until time: prefer "until/till/by 11:30pm", else any am/pm time, else default
        Matcher hold = HOLD_KEYWORD.matcher(lower);
        Matcher anyTime = ANY_TIME_AMPM.matcher(lower);
        if (hold.find()) {
            result.holdUntilMinutes = toMinutes(hold.group(1), hold.group(2), hold.group(3));
        } else if (anyTime.find()) {
            result.holdUntilMinutes = toMinutes(anyTime.group(1), anyTime.group(2), anyTime.group(3));
        } else {
            result.holdUntilMinutes = DEFAULT_HOLD_MINUTES;
        }
        result.holdUntilText = TimeUtil.format(result.holdUntilMinutes);

        // 4. Pickup note: whatever follows "pickup from ..."
        Matcher pickup = PICKUP.matcher(lower);
        if (pickup.find() && !pickup.group(1).isBlank()) {
            String note = pickup.group(1).trim();
            result.pickupNote = Character.toUpperCase(note.charAt(0)) + note.substring(1);
        } else if (lower.contains("back gate")) {
            result.pickupNote = "Back gate";
        } else if (lower.contains("front")) {
            result.pickupNote = "Front counter";
        } else {
            result.pickupNote = "Ask at the restaurant";
        }
        return result;
    }

    /** No am/pm is read as evening (1-11 -> PM), because this is a closing-time tool. */
    private int toMinutes(String h, String m, String ampm) {
        int hour = Integer.parseInt(h);
        int min = m != null ? Integer.parseInt(m) : 0;
        if ("pm".equals(ampm)) {
            if (hour != 12) hour += 12;
        } else if ("am".equals(ampm)) {
            if (hour == 12) hour = 0;
        } else if (hour >= 1 && hour <= 11) {
            hour += 12;
        }
        int minutes = hour * 60 + min;
        return hour < 5 ? minutes + 1440 : minutes; // 12:30am = 24:30, after midnight
    }
}
