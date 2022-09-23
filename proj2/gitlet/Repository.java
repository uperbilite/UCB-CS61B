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

    /** The HEAD file. */
    public static final File HEAD = Utils.join(GITLET_DIR, "HEAD");

    /**
     * .gitlet/ - restore the information of a repository
     *      - staging area/ - added files will be in staging area
     *      - removed area/ - stage files for removal
     *      - commits/ - all commits in commits directory
     *      - blobs/ - the saved contents of files
     *      - HEAD - store the commit object points to the current working directory
     */
    public static void initCommand() {
        GITLET_DIR.mkdir();
        STAGING_AREA_DIR.mkdir();
        REMOVED_AREA_DIR.mkdir();
        COMMITS_DIR.mkdir();
        BLOBS_DIR.mkdir();
        Commit initialCommit = new Commit();
        Utils.writeObject(Utils.join(COMMITS_DIR, initialCommit.getId()), initialCommit);
        Utils.writeObject(HEAD, initialCommit);
        // TODO: handle branch, default branch is master
    }

    public static boolean isInitialized() {
        return GITLET_DIR.exists();
    }

    public static void addCommand(String addName) {
        File addFile = Utils.join(CWD, addName);
        if (!addFile.exists()) {
            Utils.exitWithMessage("File does not exist.");
        }
        Commit currentCommit = Utils.readObject(HEAD, Commit.class);
        TreeMap<String, String> hashByFileName = currentCommit.getHashMap();
        if (hashByFileName.get(addName) != null) {
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
        List<String> stagingFiles = Utils.plainFilenamesIn(STAGING_AREA_DIR);
        Commit parentCommit = Utils.readObject(HEAD, Commit.class);
        TreeMap<String, String> hashByFileName = parentCommit.getHashMap();
        if (stagingFiles.isEmpty()) {
            // TODO: abort? or make a new commit with new message and timestamp?
            Commit newCommit = new Commit(message, parentCommit.getId(), null,
                    hashByFileName);
            Utils.writeObject(Utils.join(COMMITS_DIR, newCommit.getId()), newCommit);
            Utils.writeObject(HEAD, newCommit);
            return;
        }
        for (var stagingName : stagingFiles) {
            File stagingFile = Utils.join(STAGING_AREA_DIR, stagingName);
            String stagingHash = Utils.sha1(Utils.readContents(stagingFile));
            // if the key is existed in map, then overwrite it. Otherwise, it will insert a new one
            hashByFileName.put(stagingName, stagingHash);
            Utils.writeContents(Utils.join(BLOBS_DIR, stagingHash),
                    Utils.readContents(stagingFile));
        }
        List<String> removedFiles = Utils.plainFilenamesIn(REMOVED_AREA_DIR);
        for (var removedName : removedFiles) {
            assert hashByFileName.containsKey(removedName);
            hashByFileName.remove(removedName);
        }
        Commit newCommit = new Commit(message, parentCommit.getId(), null,
                hashByFileName);
        Utils.writeObject(Utils.join(COMMITS_DIR, newCommit.getId()), newCommit);
        Utils.writeObject(HEAD, newCommit);
        // TODO: handle with commit tree
        for (var stagingName : stagingFiles) {
            File stagingFile = Utils.join(STAGING_AREA_DIR, stagingName);
            stagingFile.delete();
        }
        for (var removedName : removedFiles) {
            File removedFile = Utils.join(REMOVED_AREA_DIR, removedName);
            removedFile.delete();
        }
    }

    //TODO: untested
    public static void rmCommand(String fileName) {
        List<String> stagingFiles = Utils.plainFilenamesIn(STAGING_AREA_DIR);
        Commit currentCommit = Utils.readObject(HEAD, Commit.class);
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
        Commit currentCommit = Utils.readObject(HEAD, Commit.class);
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

    // TODO: untested
    public static void globalLogCommand() {
        List<String> allCommits = Utils.plainFilenamesIn(COMMITS_DIR);
        if (allCommits == null) {
            return;
        }
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

    // TODO: untested
    public static void findCommand(String message) {
        List<String> allCommits = Utils.plainFilenamesIn(COMMITS_DIR);
        if (allCommits == null) {
            Utils.exitWithMessage("Found no commit with that message.");
        }
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

    }

    public static void checkoutFile(String fileName) {
        Commit headCommit = Utils.readObject(HEAD, Commit.class);
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
        assert commitIdList != null;
        if (!commitIdList.contains(commitId)) {
            Utils.exitWithMessage("No commit with that id exists.");
        }
        Commit commit = Utils.readObject(Utils.join(COMMITS_DIR, commitId), Commit.class);
        String fileHash = commit.getHashMap().get(fileName);
        if (fileHash == null) {
            Utils.exitWithMessage("File does not exist in that commit.");
        }
        File CWDFile = Utils.join(CWD, fileName);
        Utils.writeContents(CWDFile, Utils.readContents(Utils.join(BLOBS_DIR, fileHash)));
    }

    public static void checkoutBranch(String branchName) {
        // TODO
    }
}
