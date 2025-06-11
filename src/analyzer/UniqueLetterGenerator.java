package analyzer;

public class UniqueLetterGenerator {
    private int counter = 0;

    public String getNextLetter() {
        String alphabet = "abcdefghijklmnopqrstuvwxyz";

        StringBuilder result = new StringBuilder();

        int num = counter++;

        while (num >= 0) {
            result.insert(0, alphabet.charAt(num % 26));
            num = num / 26 - 1;
            if (num < 0) {
                break;
            }
        }

        return result.toString();
    }

    public void reset() {
        counter = 0;
    }
}
