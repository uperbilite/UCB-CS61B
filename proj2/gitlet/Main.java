package gitlet;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author Bilite Deng
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            Utils.exitWithMessage("Please enter a command.");
        }
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                validateNumArgs(args, 1);
                Repository.initCommand();
                break;
            case "add":
                validateNumArgs(args, 2);
                validateInitialized();
                Repository.addCommand(args[1]);
                break;
            case "commit":
                validateNumArgs(args, 2);
                validateInitialized();
                Repository.commitCommand(args[1], null);
                break;
            case "checkout":
                validateInitialized();
                if (args.length == 2) { // switch branch
                    Repository.checkoutBranch(args[1]);
                } else if (args.length == 3) { // replace file to head file
                    if (!args[1].equals("--")) {
                        Utils.exitWithMessage("Incorrect operands.");
                    }
                    Repository.checkoutFile(args[2]);
                } else if (args.length == 4) { // replace file to specific commit file
                    if (!args[2].equals("--")) {
                        Utils.exitWithMessage("Incorrect operands.");
                    }
                    Repository.checkoutFile(args[1], args[3]);
                } else {
                    Utils.exitWithMessage("Incorrect operands.");
                }
                break;
            case "rm":
                validateNumArgs(args, 2);
                validateInitialized();
                Repository.rmCommand(args[1]);
                break;
            case "log":
                validateNumArgs(args, 1);
                validateInitialized();
                Repository.logCommand();
                break;
            case "global-log":
                validateNumArgs(args, 1);
                validateInitialized();
                Repository.globalLogCommand();
                break;
            case "find":
                validateNumArgs(args, 2);
                validateInitialized();
                Repository.findCommand(args[1]);
                break;
            case "status":
                validateNumArgs(args, 1);
                validateInitialized();
                Repository.statusCommand();
                break;
            case "branch":
                validateNumArgs(args, 2);
                validateInitialized();
                Repository.branchCommand(args[1]);
                break;
            case "rm-branch":
                validateNumArgs(args, 2);
                validateInitialized();
                Repository.rmBranchCommand(args[1]);
                break;
            case "reset":
                validateNumArgs(args, 2);
                validateInitialized();
                Repository.resetCommand(args[1]);
                break;
            case "merge":
                validateNumArgs(args, 2);
                validateInitialized();
                Repository.mergeCommand(args[1]);
                break;
            default:
                Utils.exitWithMessage("No command with that name exists.");
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
            Utils.exitWithMessage("Incorrect operands.");
        }
    }

    /**
     * Checks whether the repository has been initialized
     */
    public static void validateInitialized() {
        if (!Repository.isInitialized()) {
            Utils.exitWithMessage("Not in an initialized Gitlet directory.");
        }
    }
}
