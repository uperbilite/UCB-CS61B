package gitlet;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author Bilite Deng
 */
public class Repository {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));

    /** The .gitlet directory. */
    public static final File GITLET_DIR = Utils.join(CWD, ".gitlet");

    /** The stagina area directory. */
    public static final File STAGING_AREA_DIR = Utils.join(GITLET_DIR, "staging area");

    /** The removed area directory. */
    public static final File REMOVED_AREA_DIR = Utils.join(GITLET_DIR, "removed area");

    /** The commits directory. */
    public static final File COMMITS_DIR = Utils.join(GITLET_DIR, "commits");

    /** The blobs directory. */
    public static final File BLOBS_DIR = Utils.join(GITLET_DIR, "blobs");

    /** The branches directory. */
    public static final File HEADS_DIR = Utils.join(GITLET_DIR, "heads");

    /** The HEAD file. */
    public static final File HEAD = Utils.join(GITLET_DIR, "HEAD");

    /**
     * .gitlet/ - restore the information of a repository
     *      - staging area/ - added files will be in staging area
     *      - removed area/ - stage files for removal
     *      - commits/ - all commits in commits directory
     *      - blobs/ - the saved contents of files
     *      - heads/ - save all branches
     *      - HEAD - store the branch name which currently point to
     */
    public static void initCommand() {
        if (Repository.isInitialized()) {
            Utils.exitWithMessage("%s", "A Gitlet version-control system " +
                    "already exists in the current directory.");
        }
        GITLET_DIR.mkdir();
        STAGING_AREA_DIR.mkdir();
        REMOVED_AREA_DIR.mkdir();
        COMMITS_DIR.mkdir();
        BLOBS_DIR.mkdir();
        HEADS_DIR.mkdir();
        Commit initialCommit = new Commit();
        Utils.writeObject(Utils.join(COMMITS_DIR, initialCommit.getId()), initialCommit);
        Utils.writeObject(Utils.join(HEADS_DIR, "master"), initialCommit);
        Utils.writeContents(HEAD, "master");
    }

    public static boolean isInitialized() {
        return GITLET_DIR.exists();
    }

    public static void addCommand(String addFileName) {
        File addFile = Utils.join(CWD, addFileName);
        if (!addFile.exists()) {
            Utils.exitWithMessage("File does not exist.");
        }
        List<String> removedFileNames = Utils.plainFilenamesIn(REMOVED_AREA_DIR);
        if (removedFileNames.contains(addFileName)) {
            File removedFile = Utils.join(REMOVED_AREA_DIR, addFileName);
            removedFile.delete();
        }
        Commit currentCommit = Utils.readHeadCommit(HEAD);
        var hashByFileName = currentCommit.getHashMap();
        if (hashByFileName.containsKey(addFileName)) {
            String addFileHash = Utils.sha1(Utils.readContents(addFile));
            String commitFileHash = hashByFileName.get(addFileName);
            if (addFileHash.equals(commitFileHash)) {
                File stagingFile = Utils.join(STAGING_AREA_DIR, addFileName);
                if (stagingFile.exists()) {
                    stagingFile.delete();
                }
                return;
            }
        }
        Utils.writeContents(Utils.join(STAGING_AREA_DIR, addFileName),
                Utils.readContents(addFile));
    }

    public static void commitCommand(String msg) {
        if (msg.length() == 0) {
            Utils.exitWithMessage("Please enter a commit message.");
        }
        List<String> stagingFileNames = Utils.plainFilenamesIn(STAGING_AREA_DIR);
        List<String> removedFileNames = Utils.plainFilenamesIn(REMOVED_AREA_DIR);
        if (stagingFileNames.isEmpty() && removedFileNames.isEmpty()) {
            Utils.exitWithMessage("No changes added to the commit.");
        }
        Commit currentCommit = Utils.readHeadCommit(HEAD);
        var hashByFileName = currentCommit.getHashMap();
        for (var stagingName : stagingFileNames) {
            File stagingFile = Utils.join(STAGING_AREA_DIR, stagingName);
            String stagingHash = Utils.sha1(Utils.readContents(stagingFile));
            // if the key is existed in map, then overwrite it. Otherwise, it will insert a new one
            hashByFileName.put(stagingName, stagingHash);
            Utils.writeContents(Utils.join(BLOBS_DIR, stagingHash),
                    Utils.readContents(stagingFile));
        }
        for (var removedName : removedFileNames) {
            assert hashByFileName.containsKey(removedName);
            hashByFileName.remove(removedName);
        }
        Commit newCommit = new Commit(msg, currentCommit.getId(), null,
                hashByFileName);
        Utils.writeObject(Utils.join(COMMITS_DIR, newCommit.getId()), newCommit);
        File currentBranch = Utils.readHeadBranch(HEAD);
        Utils.writeObject(currentBranch, newCommit);
        clearStagingArea();
        clearRemovedArea();
    }

    public static void rmCommand(String rmFileName) {
        List<String> stagingFileNames = Utils.plainFilenamesIn(STAGING_AREA_DIR);
        Commit currentCommit = Utils.readHeadCommit(HEAD);
        if (!stagingFileNames.contains(rmFileName) &&
                !currentCommit.getHashMap().containsKey(rmFileName)) {
            Utils.exitWithMessage("No reason to remove the file.");
        }
        if (stagingFileNames.contains(rmFileName)) {
            Utils.join(STAGING_AREA_DIR, rmFileName).delete();
        }
        if (currentCommit.getHashMap().containsKey(rmFileName)) {
            File rmFile = Utils.join(REMOVED_AREA_DIR, rmFileName);
            // don't need the removedFile's content, only it's name
            Utils.writeObject(rmFile, "");
            if (Utils.join(CWD, rmFileName).exists()) {
                Utils.restrictedDelete(rmFileName);
            }
        }
    }

    public static void logCommand() {
        Commit c = Utils.readHeadCommit(HEAD);
        while (true) {
            if (c.getParentCommit() == null) {
                printLogMessage(c);
                break;
            }
            printLogMessage(c);
            c = Utils.readObject(
                    Utils.join(COMMITS_DIR, c.getParentCommit()), Commit.class);
        }
    }

    public static void globalLogCommand() {
        List<String> allCommitIds = Utils.plainFilenamesIn(COMMITS_DIR);
        for (var commitId : allCommitIds) {
            Commit c = Utils.readObject(Utils.join(COMMITS_DIR, commitId), Commit.class);
            printLogMessage(c);
        }
    }

    private static void printLogMessage(Commit c) {
        System.out.println("===");
        System.out.println("commit " + c.getId());
        // TODO: handle merged commit
        SimpleDateFormat f =
                new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z", Locale.ENGLISH);
        System.out.println("Date: " + f.format(c.getTimestamp()));
        System.out.println(c.getMessage());
        System.out.println();
    }

    public static void findCommand(String msg) {
        List<String> allCommitIds = Utils.plainFilenamesIn(COMMITS_DIR);
        boolean isFound = false;
        for (var commitId : allCommitIds) {
            Commit c = Utils.readObject(Utils.join(COMMITS_DIR, commitId), Commit.class);
            if (c.getMessage().equals(msg)) {
                isFound = true;
                System.out.println(c.getId());
            }
        }
        if (!isFound) {
            Utils.exitWithMessage("Found no commit with that message.");
        }
    }

    public static void statusCommand() {
        System.out.println("=== Branches ===");
        List<String> allBranchNames = Utils.plainFilenamesIn(HEADS_DIR);
        String currentBranchName = Utils.readContentsAsString(HEAD);
        Collections.sort(allBranchNames);
        for (var branchName : allBranchNames) {
            if (branchName.equals(currentBranchName)) {
                System.out.println("*" + branchName);
            } else {
                System.out.println(branchName);
            }
        }
        System.out.println();
        System.out.println("=== Staged Files ===");
        List<String> stagingFileNames = Utils.plainFilenamesIn(STAGING_AREA_DIR);
        Collections.sort(stagingFileNames);
        stagingFileNames.forEach(System.out::println);
        System.out.println();
        System.out.println("=== Removed Files ===");
        List<String> removedFileNames = Utils.plainFilenamesIn(REMOVED_AREA_DIR);
        Collections.sort(removedFileNames);
        removedFileNames.forEach(System.out::println);
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        // TODO
        System.out.println();
        System.out.println("=== Untracked Files ===");
        // TODO
        System.out.println();
    }

    public static void checkoutFile(String fileName) {
        Commit headCommit = Utils.readHeadCommit(HEAD);
        if (!headCommit.getHashMap().containsKey(fileName)) {
            Utils.exitWithMessage("File does not exist in that commit.");
        }
        File currentFile = Utils.join(CWD, fileName);
        String headFileHash = headCommit.getHashMap().get(fileName);
        Utils.writeContents(currentFile, Utils.readContents(Utils.join(BLOBS_DIR, headFileHash)));
    }

    public static void checkoutFile(String commitId, String fileName) {
        // TODO: abbreviate commit id
        List<String> allCommitIds = Utils.plainFilenamesIn(COMMITS_DIR);
        if (!allCommitIds.contains(commitId)) {
            Utils.exitWithMessage("No commit with that id exists.");
        }
        Commit c = Utils.readObject(Utils.join(COMMITS_DIR, commitId), Commit.class);
        if (!c.getHashMap().containsKey(fileName)) {
            Utils.exitWithMessage("File does not exist in that commit.");
        }
        String fileHash = c.getHashMap().get(fileName);
        File currentFile = Utils.join(CWD, fileName);
        Utils.writeContents(currentFile, Utils.readContents(Utils.join(BLOBS_DIR, fileHash)));
    }

    public static void checkoutBranch(String branchName) {
        List<String> allBranchNames = Utils.plainFilenamesIn(HEADS_DIR);
        if (!allBranchNames.contains(branchName)) {
            Utils.exitWithMessage("No such branch exists.");
        }
        String currentBranchName = Utils.readContentsAsString(HEAD);
        if (branchName.equals(currentBranchName)) {
            Utils.exitWithMessage("No need to checkout the current branch.");
        }
        Commit currentCommit = Utils.readHeadCommit(HEAD);
        List<String> allFileNames = Utils.plainFilenamesIn(CWD);
        for (var fileName : allFileNames) {
            if (!currentCommit.getHashMap().containsKey(fileName)) {
                Utils.exitWithMessage("There is an untracked file in the way; " +
                        "delete it, or add and commit it first.");
            }
        }
        Commit branchHead = Utils.readObject(Utils.join(HEADS_DIR, branchName), Commit.class);
        for (var fileName : allFileNames) {
            if (!branchHead.getHashMap().containsKey(fileName)) {
                Utils.restrictedDelete(fileName);
            }
        }
        for (var entry : branchHead.getHashMap().entrySet()) {
            File currentFile = Utils.join(CWD, entry.getKey());
            Utils.writeContents(currentFile,
                    Utils.readContents(Utils.join(BLOBS_DIR, entry.getValue())));
        }
        clearStagingArea();
        clearRemovedArea();
        Utils.writeContents(HEAD, branchName);
    }

    public static void branchCommand(String branchName) {
        List<String> allBranchNames = Utils.plainFilenamesIn(HEADS_DIR);
        if (allBranchNames.contains(branchName)) {
            Utils.exitWithMessage("A branch with that name already exists.");
        }
        Commit currentCommit = Utils.readHeadCommit(HEAD);
        Utils.writeObject(Utils.join(HEADS_DIR, branchName), currentCommit);
    }

    public static void rmBranchCommand(String branchName) {
        List<String> allBranchNames = Utils.plainFilenamesIn(HEADS_DIR);
        if (!allBranchNames.contains(branchName)) {
            Utils.exitWithMessage("A branch with that name does not exist.");
        }
        String currentBranchName = Utils.readContentsAsString(HEAD);
        if (branchName.equals(currentBranchName)) {
            Utils.exitWithMessage("Cannot remove the current branch.");
        }
        File branch = Utils.join(HEADS_DIR, branchName);
        branch.delete();
    }

    public static void resetCommand(String commitId) {
        List<String> allCommitIds = Utils.plainFilenamesIn(COMMITS_DIR);
        if (!allCommitIds.contains(commitId)) {
            Utils.exitWithMessage("No commit with that id exists.");
        }
        Commit currentCommit = Utils.readHeadCommit(HEAD);
        List<String> allFileNames = Utils.plainFilenamesIn(CWD);
        for (var fileName : allFileNames) {
            if (!currentCommit.getHashMap().containsKey(fileName)) {
                Utils.exitWithMessage("There is an untracked file in the way; " +
                        "delete it, or add and commit it first.");
            }
        }
        Commit commit = Utils.readObject(Utils.join(COMMITS_DIR, commitId), Commit.class);
        for (var fileName : allFileNames) {
            if (!commit.getHashMap().containsKey(fileName)) {
                Utils.restrictedDelete(fileName);
            }
        }
        for (var entry : commit.getHashMap().entrySet()) {
            File f = Utils.join(CWD, entry.getKey());
            Utils.writeContents(f, Utils.readContents(Utils.join(BLOBS_DIR, entry.getValue())));
        }
    }

    private static void clearStagingArea() {
        List<String> stagingFiles = Utils.plainFilenamesIn(STAGING_AREA_DIR);
        for (var stagingName : stagingFiles) {
            Utils.join(STAGING_AREA_DIR, stagingName).delete();
        }
    }

    private static void clearRemovedArea() {
        List<String> removedFiles = Utils.plainFilenamesIn(REMOVED_AREA_DIR);
        for (var removedName : removedFiles) {
            Utils.join(REMOVED_AREA_DIR, removedName).delete();
        }
    }
}
