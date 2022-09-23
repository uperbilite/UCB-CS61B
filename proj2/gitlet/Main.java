package gitlet;

import java.io.IOException;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author Bilite Deng
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            Utils.exitWithMessage("%s", "Please enter a command.");
        }
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                validateNumArgs(args, 1);
                if (Repository.isInitialized()) {
                    Utils.exitWithMessage("%s", "A Gitlet version-control system " +
                            "already exists in the current directory.");
                }
                Repository.initCommand();
                break;
            case "add":
                validateNumArgs(args, 2);
                validateInitialized();
                Repository.addCommand(args[1]);
                break;
            case "commit":
                if (args.length == 1) {
                    Utils.exitWithMessage("Please enter a commit message.");
                }
                validateNumArgs(args, 2);
                validateInitialized();
                Repository.commitCommand(args[1]);
                break;
            case "checkout":
                validateInitialized();
                if (args.length == 2) { // switch branch
                    Repository.checkoutBranch(args[1]);
                } else if (args.length == 3) { // replace file to head file
                    Repository.checkoutFile(args[2]);
                } else if (args.length == 4) { // replace file to specific commit file
                    Repository.checkoutFile(args[1], args[3]);
                } else {
                    Utils.exitWithMessage("%s", "Incorrect operands.");
                }
                break;
            case "log":
                validateNumArgs(args, 1);
                validateInitialized();
                Repository.logCommand();
                break;
            // TODO: FILL THE REST IN
            default:
                Utils.exitWithMessage("%s", "No command with that name exists.");
        }
    }

    /**
     * Checks the number of arguments versus the expected number
     *
     * @param args Argument array from command line
     * @param n Number of expected arguments
     */
    public static void validateNumArgs(String[] args, int n) {
        if (args.length != n) {
            Utils.exitWithMessage("%s", "Incorrect operands.");
        }
    }

    /**
     * Checks whether the repository has been initialized
     */
    public static void validateInitialized() {
        if (!Repository.isInitialized()) {
            Utils.exitWithMessage("%s", "Not in an initialized Gitlet directory.");
        }
    }
}
