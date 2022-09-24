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

    public static void addCommand(String addName) {
        File addFile = Utils.join(CWD, addName);
        if (!addFile.exists()) {
            Utils.exitWithMessage("File does not exist.");
        }
        List<String> removedFiles = Utils.plainFilenamesIn(REMOVED_AREA_DIR);
        if (removedFiles.contains(addName)) {
            File removedFile = Utils.join(REMOVED_AREA_DIR, addName);
            removedFile.delete();
        }
        Commit currentCommit = Utils.readHeadCommit(HEAD);
        TreeMap<String, String> hashByFileName = currentCommit.getHashMap();
        if (hashByFileName.containsKey(addName)) {
            String addHash = Utils.sha1(Utils.readContents(addFile));
            String commitHash = hashByFileName.get(addName);
            if (addHash.equals(commitHash)) {
                File stagingFile = Utils.join(STAGING_AREA_DIR, addName);
                if (stagingFile.exists()) {
                    stagingFile.delete();
                }
                return;
            }
        }
        Utils.writeContents(Utils.join(STAGING_AREA_DIR, addName),
                Utils.readContents(addFile));
    }

    public static void commitCommand(String message) {
        if (message.length() == 0) {
            Utils.exitWithMessage("Please enter a commit message.");
        }
        List<String> stagingFiles = Utils.plainFilenamesIn(STAGING_AREA_DIR);
        List<String> removedFiles = Utils.plainFilenamesIn(REMOVED_AREA_DIR);
        if (stagingFiles.isEmpty() && removedFiles.isEmpty()) {
            Utils.exitWithMessage("No changes added to the commit.");
        }
        Commit parentCommit = Utils.readHeadCommit(HEAD);
        TreeMap<String, String> hashByFileName = parentCommit.getHashMap();
        for (var stagingName : stagingFiles) {
            File stagingFile = Utils.join(STAGING_AREA_DIR, stagingName);
            String stagingHash = Utils.sha1(Utils.readContents(stagingFile));
            // if the key is existed in map, then overwrite it. Otherwise, it will insert a new one
            hashByFileName.put(stagingName, stagingHash);
            Utils.writeContents(Utils.join(BLOBS_DIR, stagingHash),
                    Utils.readContents(stagingFile));
        }
        for (var removedName : removedFiles) {
            assert hashByFileName.containsKey(removedName);
            hashByFileName.remove(removedName);
        }
        Commit newCommit = new Commit(message, parentCommit.getId(), null,
                hashByFileName);
        Utils.writeObject(Utils.join(COMMITS_DIR, newCommit.getId()), newCommit);
        File branch = Utils.readHeadBranch(HEAD);
        Utils.writeObject(branch, newCommit);
        clearStagingArea();
        clearRemovedArea();
    }

    public static void rmCommand(String fileName) {
        List<String> stagingFiles = Utils.plainFilenamesIn(STAGING_AREA_DIR);
        Commit currentCommit = Utils.readHeadCommit(HEAD);
        if (!stagingFiles.contains(fileName)
                && !currentCommit.getHashMap().containsKey(fileName)) {
            Utils.exitWithMessage("No reason to remove the file.");
        }
        if (stagingFiles.contains(fileName)) {
            Utils.join(STAGING_AREA_DIR, fileName).delete();
        }
        if (currentCommit.getHashMap().containsKey(fileName)) {
            File removedFile = Utils.join(REMOVED_AREA_DIR, fileName);
            // don't need the removedFile's content, only it's name
            Utils.writeObject(removedFile, "");
            if (Utils.join(CWD, fileName).exists()) {
                Utils.restrictedDelete(fileName);
            }
        }
    }

    public static void logCommand() {
        Commit currentCommit = Utils.readHeadCommit(HEAD);
        while (true) {
            if (currentCommit.getParentCommit() == null) {
                printLogMessage(currentCommit);
                break;
            }
            printLogMessage(currentCommit);
            currentCommit = Utils.readObject(
                    Utils.join(COMMITS_DIR, currentCommit.getParentCommit()), Commit.class);
        }
    }

    public static void globalLogCommand() {
        List<String> allCommits = Utils.plainFilenamesIn(COMMITS_DIR);
        for (var commitId : allCommits) {
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

    public static void findCommand(String message) {
        List<String> allCommits = Utils.plainFilenamesIn(COMMITS_DIR);
        boolean isFound = false;
        for (var commitId : allCommits) {
            Commit c = Utils.readObject(Utils.join(COMMITS_DIR, commitId), Commit.class);
            if (c.getMessage().equals(message)) {
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
        List<String> allBranches = Utils.plainFilenamesIn(HEADS_DIR);
        String currentBranch = Utils.readContentsAsString(HEAD);
        Collections.sort(allBranches);
        for (var branchName : allBranches) {
            if (branchName.equals(currentBranch)) {
                System.out.println("*" + branchName);
            } else {
                System.out.println(branchName);
            }
        }
        System.out.println();
        System.out.println("=== Staged Files ===");
        List<String> stagingFiles = Utils.plainFilenamesIn(STAGING_AREA_DIR);
        Collections.sort(stagingFiles);
        for (var fileName : stagingFiles) {
            System.out.println(fileName);
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        List<String> removedFiles = Utils.plainFilenamesIn(REMOVED_AREA_DIR);
        Collections.sort(removedFiles);
        for (var fileName : removedFiles) {
            System.out.println(fileName);
        }
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
        String headFileHash = headCommit.getHashMap().get(fileName);
        if (headFileHash == null) {
            Utils.exitWithMessage("File does not exist in that commit.");
        }
        File CWDFile = Utils.join(CWD, fileName);
        Utils.writeContents(CWDFile, Utils.readContents(Utils.join(BLOBS_DIR, headFileHash)));
    }

    public static void checkoutFile(String commitId, String fileName) {
        // TODO: abbreviate commit id
        List<String> commitIdList = Utils.plainFilenamesIn(COMMITS_DIR);
        if (!commitIdList.contains(commitId)) {
            Utils.exitWithMessage("No commit with that id exists.");
        }
        Commit commit = Utils.readObject(Utils.join(COMMITS_DIR, commitId), Commit.class);
        if (!commit.getHashMap().containsKey(fileName)) {
            Utils.exitWithMessage("File does not exist in that commit.");
        }
        String fileHash = commit.getHashMap().get(fileName);
        File CWDFile = Utils.join(CWD, fileName);
        Utils.writeContents(CWDFile, Utils.readContents(Utils.join(BLOBS_DIR, fileHash)));
    }

    public static void checkoutBranch(String branchName) {
        List<String> branchNames = Utils.plainFilenamesIn(HEADS_DIR);
        if (!branchNames.contains(branchName)) {
            Utils.exitWithMessage("No such branch exists.");
        }
        String currentBranch = Utils.readContentsAsString(HEAD);
        if (branchName.equals(currentBranch)) {
            Utils.exitWithMessage("No need to checkout the current branch.");
        }
        Commit currentCommit = Utils.readHeadCommit(HEAD);
        List<String> fileNames = Utils.plainFilenamesIn(CWD);
        for (var fileName : fileNames) {
            if (!currentCommit.getHashMap().containsKey(fileName)) {
                Utils.exitWithMessage("There is an untracked file in the way; " +
                        "delete it, or add and commit it first.");
            }
        }
        Commit branchHead = Utils.readObject(Utils.join(HEADS_DIR, branchName), Commit.class);
        for (var fileName : fileNames) {
            if (!branchHead.getHashMap().containsKey(fileName)) {
                Utils.restrictedDelete(fileName);
            }
        }
        for (var entry : branchHead.getHashMap().entrySet()) {
            File branchFile = Utils.join(CWD, entry.getKey());
            Utils.writeContents(branchFile,
                    Utils.readContents(Utils.join(BLOBS_DIR, entry.getValue())));
        }
        clearStagingArea();
        clearRemovedArea();
        Utils.writeContents(HEAD, branchName);
    }

    public static void branchCommand(String branchName) {
        List<String> branchNames = Utils.plainFilenamesIn(HEADS_DIR);
        if (branchNames.contains(branchName)) {
            Utils.exitWithMessage("A branch with that name already exists.");
        }
        Commit currentCommit = Utils.readHeadCommit(HEAD);
        Utils.writeObject(Utils.join(HEADS_DIR, branchName), currentCommit);
    }

    public static void rmBranchCommand(String branchName) {
        List<String> branchNames = Utils.plainFilenamesIn(HEADS_DIR);
        if (!branchNames.contains(branchName)) {
            Utils.exitWithMessage("A branch with that name does not exist.");
        }
        String currentBranch = Utils.readContentsAsString(HEAD);
        if (branchName.equals(currentBranch)) {
            Utils.exitWithMessage("Cannot remove the current branch.");
        }
        File branch = Utils.join(HEADS_DIR, branchName);
        branch.delete();
    }

    public static void resetCommand(String commitId) {
        List<String> allCommitId = Utils.plainFilenamesIn(COMMITS_DIR);
        if (!allCommitId.contains(commitId)) {
            Utils.exitWithMessage("No commit with that id exists.");
        }
        Commit currentCommit = Utils.readHeadCommit(HEAD);
        List<String> fileNames = Utils.plainFilenamesIn(CWD);
        for (var fileName : fileNames) {
            if (!currentCommit.getHashMap().containsKey(fileName)) {
                Utils.exitWithMessage("There is an untracked file in the way; " +
                        "delete it, or add and commit it first.");
            }
        }
        Commit commit = Utils.readObject(Utils.join(COMMITS_DIR, commitId), Commit.class);
        for (var fileName : fileNames) {
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
