package TextBlocks;

/**
 * Demonstrate multi-line Strings, a.k.a. Text Blocks
 */
public class TextBlocksDemo {

    public static void main(String[] args) {

        var oldHelpText = "Help?\n" +
                "This help file will help you learn\n" +
                "to use our great new software package\n" +
                "LockInVar.";

        var newHelpText = """
                Help?
                This help file will help you learn
                to use our great new software package
                LockInVar.""";

        IO.println("oldHelpText = " + oldHelpText);
        IO.println("newHelpText = " + newHelpText);
        IO.println(oldHelpText.equals(newHelpText));
    }
}