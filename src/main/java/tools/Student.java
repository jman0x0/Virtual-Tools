package tools;


import java.util.Arrays;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Student {
    private final HashSet<String> firstNames = new HashSet<>();
    private final HashSet<String> lastNames  = new HashSet<>();
    private String fullName;

    public Student(String fullName) throws IllegalArgumentException {
        final String[] split = fullName.split(",");
        if (split.length != 2) {
            throw new IllegalArgumentException("Full must be composed of first and last names separated by a single comma");
        }
        final var list = Arrays.asList(lastNames, firstNames);
        final Pattern pattern = Pattern.compile("(\\w+)");

        StringBuilder realName = new StringBuilder();
        int active = 0;
        for (HashSet<String> category : list) {
            final String  data = split[active++];
            final Matcher matcher = pattern.matcher(data);

            boolean separate = false;
            while (matcher.find()) {
                category.add(matcher.group(0).toLowerCase());
                realName.append(matcher.group(0));
                realName.append(" ");
            }
            if (realName.length() > 0) {
                realName.setLength(realName.length() - 1);
            }
            realName.append(", ");
        }
        realName.setLength(realName.length() - 2);
        this.fullName = realName.toString();
    }

    public boolean firstMatches(String name) {
        return firstNames.contains(name.toLowerCase());
    }

    public boolean lastMatches(String name) {
        return lastNames.contains(name.toLowerCase());
    }

    public String getFullName() {
        return fullName;
    }

    public String getReversed() {
        return Utilities.reverseName(fullName);
    }

    @Override
    public String toString() {
        return getFullName();
    }
}
