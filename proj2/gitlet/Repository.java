package gitlet;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    /** The staging area directory. */
    public static final File STAGING_AREA_DIR = Utils.join(GITLET_DIR, "staging area");

    /** The removed area directory. */
    public static final File REMOVED_AREA_DIR = Utils.join(GITLET_DIR, "removed area");

    /** The commit directory. */
    public static final File COMMITS_DIR = Utils.join(GITLET_DIR, "commits");

    /** The blob directory. */
    public static final File BLOBS_DIR = Utils.join(GITLET_DIR, "blobs");

    /** The branch directory. */
    public static final File HEADS_DIR = Utils.join(GITLET_DIR, "heads");

    /** The HEAD file. */
    public static final File HEAD = Utils.join(GITLET_DIR, "HEAD");

    /**
     * .gitlet/ - restore the information of a repository
     *      - staging area/ - stage files for addition
     *      - removed area/ - stage files for removal
     *      - commits/ - all commits in commits directory
     *      - blobs/ - the saved contents of files
     *      - heads/ - save all branches
     *      - HEAD - store the branch name which currently point to
     */
    public static void initCommand() {
        if (Repository.isInitialized()) {
            Utils.exitWithMessage("A Gitlet version-control system "
                    + "already exists in the current directory.");
        }
        GITLET_DIR.mkdir();
        STAGING_AREA_DIR.mkdir();
        REMOVED_AREA_DIR.mkdir();
        COMMITS_DIR.mkdir();
        BLOBS_DIR.mkdir();
        HEADS_DIR.mkdir();
        Commit initialCommit = new Commit();
        Utils.writeObject(Utils.join(COMMITS_DIR, initialCommit.getId()), initialCommit);
        Utils.writeContents(Utils.join(HEADS_DIR, "master"), initialCommit.getId());
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

    public static void commitCommand(String msg, String secondParentCommitId) {
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
        Commit newCommit = new Commit(msg, currentCommit.getId(), secondParentCommitId,
                hashByFileName);
        Utils.writeObject(Utils.join(COMMITS_DIR, newCommit.getId()), newCommit);
        File currentBranch = Utils.readHeadBranch(HEAD);
        Utils.writeContents(currentBranch, newCommit.getId());
        clearStagingArea();
        clearRemovedArea();
    }

    public static void rmCommand(String rmFileName) {
        List<String> stagingFileNames = Utils.plainFilenamesIn(STAGING_AREA_DIR);
        Commit currentCommit = Utils.readHeadCommit(HEAD);
        if (!stagingFileNames.contains(rmFileName)
                && !currentCommit.getHashMap().containsKey(rmFileName)) {
            Utils.exitWithMessage("No reason to remove the file.");
        }
        if (stagingFileNames.contains(rmFileName)) {
            Utils.join(STAGING_AREA_DIR, rmFileName).delete();
        }
        if (currentCommit.getHashMap().containsKey(rmFileName)) {
            File rmFile = Utils.join(REMOVED_AREA_DIR, rmFileName);
            // don't need the removedFile's content, only it's name
            Utils.writeContents(rmFile, "");
            if (Utils.join(CWD, rmFileName).exists()) {
                Utils.restrictedDelete(rmFileName);
            }
        }
    }

    public static void logCommand() {
        Commit c = Utils.readHeadCommit(HEAD);
        while (true) {
            if (c.getParentCommitId() == null) {
                printLogMessage(c);
                break;
            }
            printLogMessage(c);
            c = Utils.readObject(
                    Utils.join(COMMITS_DIR, c.getParentCommitId()), Commit.class);
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
        checkFileExist(HEAD, fileName);
        Commit headCommit = Utils.readHeadCommit(HEAD);
        File currentFile = Utils.join(CWD, fileName);
        String headFileHash = headCommit.getHashMap().get(fileName);
        Utils.writeContents(currentFile, Utils.readContents(Utils.join(BLOBS_DIR, headFileHash)));
    }

    public static void checkoutFile(String commitId, String fileName) {
        commitId = getFullCommitId(commitId);
        checkCommitExist(commitId);
        checkFileExist(COMMITS_DIR, commitId, fileName);
        Commit commit = Utils.readObject(Utils.join(COMMITS_DIR, commitId), Commit.class);
        File currentFile = Utils.join(CWD, fileName);
        String fileHash = commit.getHashMap().get(fileName);
        Utils.writeContents(currentFile, Utils.readContents(Utils.join(BLOBS_DIR, fileHash)));
    }

    public static void checkoutBranch(String branchName) {
        checkBranchExist(branchName);
        checkCheckoutNotCurrentBranch(branchName);
        checkUntrackedFileNotChanged(HEADS_DIR, branchName);
        replaceFilesInCWD(HEADS_DIR, branchName);
        clearStagingArea();
        clearRemovedArea();
        Utils.writeContents(HEAD, branchName);
    }

    public static void branchCommand(String branchName) {
        checkBranchNameNotExist(branchName);
        Commit currentCommit = Utils.readHeadCommit(HEAD);
        Utils.writeContents(Utils.join(HEADS_DIR, branchName), currentCommit.getId());
    }

    public static void rmBranchCommand(String branchName) {
        checkBranchNameExist(branchName);
        checkRemoveNotCurrentBranch(branchName);
        File branch = Utils.join(HEADS_DIR, branchName);
        branch.delete();
    }

    public static void resetCommand(String commitId) {
        commitId = getFullCommitId(commitId);
        checkCommitExist(commitId);
        checkUntrackedFileNotChanged(COMMITS_DIR, commitId);
        replaceFilesInCWD(COMMITS_DIR, commitId);
        Commit commit = Utils.readObject(Utils.join(COMMITS_DIR, commitId), Commit.class);
        File currentBranch = Utils.readHeadBranch(HEAD);
        Utils.writeContents(currentBranch, commit.getId());
        clearStagingArea();
        clearRemovedArea();
    }

    public static void mergeCommand(String branchName) {
        checkUncommittedChanges();
        checkBranchNameExist(branchName);
        checkNotMergeItself(branchName);
        checkUntrackedFileNotChanged(HEADS_DIR, branchName);
        checkAncestorBranch(branchName);
        checkFastForward(branchName);
        Commit splitPoint = getSplitPoint(branchName);
        Commit currentBranch = Utils.readHeadCommit(HEAD);
        Commit givenBranch = Utils.readBranchHeadCommit(branchName);
        List<String> allFileNames = getAllFileNamesInMerge(splitPoint, currentBranch, givenBranch);
        var splitPointMap = splitPoint.getHashMap();
        var currentBranchMap = currentBranch.getHashMap();
        var givenBranchMap = givenBranch.getHashMap();
        boolean isConflictMerge = false;
        for (var fileName : allFileNames) {
            if (splitPointMap.containsKey(fileName)
                    && currentBranchMap.containsKey(fileName)
                    && givenBranchMap.containsKey(fileName)
                    && !givenBranchMap.get(fileName).equals(splitPointMap.get(fileName))
                    && currentBranchMap.get(fileName).equals(splitPointMap.get(fileName))) {
                checkoutFile(givenBranch.getId(), fileName);
                addCommand(fileName);
            }
            else if (splitPointMap.containsKey(fileName)
                    && currentBranchMap.containsKey(fileName)
                    && givenBranchMap.containsKey(fileName)
                    && givenBranchMap.get(fileName).equals(splitPointMap.get(fileName))
                    && !currentBranchMap.get(fileName).equals(splitPointMap.get(fileName))) {
                continue;
            }
            else if ((splitPointMap.containsKey(fileName)
                    && currentBranchMap.containsKey(fileName)
                    && givenBranchMap.containsKey(fileName)
                    && !givenBranchMap.get(fileName).equals(splitPointMap.get(fileName))
                    && !currentBranchMap.get(fileName).equals(splitPointMap.get(fileName))
                    && givenBranchMap.get(fileName).equals(currentBranchMap.get(fileName)))
                    || (splitPointMap.containsKey(fileName)
                    && !givenBranchMap.containsKey(fileName)
                    && !currentBranchMap.containsKey(fileName))) {
                continue;
            }
            else if (!splitPointMap.containsKey(fileName)
                    && currentBranchMap.containsKey(fileName)
                    && !givenBranchMap.containsKey(fileName)) {
                continue;
            }
            else if (!splitPointMap.containsKey(fileName)
                    && !currentBranchMap.containsKey(fileName)
                    && givenBranchMap.containsKey(fileName)) {
                checkoutFile(givenBranch.getId(), fileName);
                addCommand(fileName);
            }
            else if (splitPointMap.containsKey(fileName)
                    && currentBranchMap.containsKey(fileName)
                    && !givenBranchMap.containsKey(fileName)
                    && currentBranchMap.get(fileName).equals(splitPointMap.get(fileName))) {
                rmCommand(fileName);
            }
            else if (splitPointMap.containsKey(fileName)
                    && !currentBranchMap.containsKey(fileName)
                    && givenBranchMap.containsKey(fileName)
                    && givenBranchMap.get(fileName).equals(splitPointMap.get(fileName))) {
                continue;
            }
            else {
                isConflictMerge = true;
                String mergedContent = "";
                if (splitPointMap.containsKey(fileName)
                        && currentBranchMap.containsKey(fileName)
                        && givenBranchMap.containsKey(fileName)
                        && !givenBranchMap.get(fileName).equals(splitPointMap.get(fileName))
                        && !currentBranchMap.get(fileName).equals(splitPointMap.get(fileName))
                        && !givenBranchMap.get(fileName).equals(currentBranchMap.get(fileName))) {
                    mergedContent += "<<<<<<< HEAD\n";
                    mergedContent += Utils.readContentsAsString(
                            Utils.join(BLOBS_DIR, currentBranchMap.get(fileName)));
                    mergedContent += "=======\n";
                    mergedContent += Utils.readContentsAsString(
                            Utils.join(BLOBS_DIR, givenBranchMap.get(fileName)));
                    mergedContent += ">>>>>>>\n";
                }
                else if (splitPointMap.containsKey(fileName)
                        && currentBranchMap.containsKey(fileName)
                        && !givenBranchMap.containsKey(fileName)
                        &&!currentBranchMap.get(fileName).equals(splitPointMap.get(fileName))) {
                    mergedContent += "<<<<<<< HEAD\n";
                    mergedContent += Utils.readContentsAsString(
                            Utils.join(BLOBS_DIR, currentBranchMap.get(fileName)));
                    mergedContent += "=======\n";
                    mergedContent += ">>>>>>>\n";
                }
                else if (splitPointMap.containsKey(fileName)
                        && !currentBranchMap.containsKey(fileName)
                        && givenBranchMap.containsKey(fileName)
                        &&!givenBranchMap.get(fileName).equals(splitPointMap.get(fileName))) {
                    mergedContent += "<<<<<<< HEAD\n";
                    mergedContent += "=======\n";
                    mergedContent += Utils.readContentsAsString(
                            Utils.join(BLOBS_DIR, givenBranchMap.get(fileName)));
                    mergedContent += ">>>>>>>\n";
                }
                else {
                    mergedContent += "<<<<<<< HEAD\n";
                    mergedContent += Utils.readContentsAsString(
                            Utils.join(BLOBS_DIR, currentBranchMap.get(fileName)));
                    mergedContent += "=======\n";
                    mergedContent += Utils.readContentsAsString(
                            Utils.join(BLOBS_DIR, givenBranchMap.get(fileName)));
                    mergedContent += ">>>>>>>\n";
                }
                File mergedFile = Utils.join(CWD, fileName);
                Utils.writeContents(mergedFile, mergedContent);
                addCommand(fileName);
            }
        }
        commitCommand("Merged " + branchName + " into " + Utils.readContentsAsString(HEAD) + ".",
                givenBranch.getId());
        if (isConflictMerge) {
            System.out.println("Encountered a merge conflict.");
        }
    }

    private static void checkCommitExist(String commitId) {
        List<String> allCommitIds = Utils.plainFilenamesIn(COMMITS_DIR);
        if (commitId == null || !allCommitIds.contains(commitId)) {
            Utils.exitWithMessage("No commit with that id exists.");
        }
    }

    private static void checkFileExist(File head, String fileName) {
        assert HEAD.equals(head);
        Commit headCommit = Utils.readHeadCommit(head);
        if (!headCommit.getHashMap().containsKey(fileName)) {
            Utils.exitWithMessage("File does not exist in that commit.");
        }
    }

    private static void checkFileExist(File commitsDir, String commitId, String fileName) {
        Commit commit = Utils.readObject(Utils.join(commitsDir, commitId), Commit.class);
        if (!commit.getHashMap().containsKey(fileName)) {
            Utils.exitWithMessage("File does not exist in that commit.");
        }
    }

    private static void checkBranchExist(String branchName) {
        List<String> allBranchNames = Utils.plainFilenamesIn(HEADS_DIR);
        if (!allBranchNames.contains(branchName)) {
            Utils.exitWithMessage("No such branch exists.");
        }
    }

    private static void checkCheckoutNotCurrentBranch(String branchName) {
        String currentBranchName = Utils.readContentsAsString(HEAD);
        if (branchName.equals(currentBranchName)) {
            Utils.exitWithMessage("No need to checkout the current branch.");
        }
    }

    private static void checkRemoveNotCurrentBranch(String branchName) {
        String currentBranchName = Utils.readContentsAsString(HEAD);
        if (branchName.equals(currentBranchName)) {
            Utils.exitWithMessage("Cannot remove the current branch.");
        }
    }

    private static void checkBranchNameExist(String branchName) {
        List<String> allBranchNames = Utils.plainFilenamesIn(HEADS_DIR);
        if (!allBranchNames.contains(branchName)) {
            Utils.exitWithMessage("A branch with that name does not exist.");
        }
    }

    private static void checkBranchNameNotExist(String branchName) {
        List<String> allBranchNames = Utils.plainFilenamesIn(HEADS_DIR);
        if (allBranchNames.contains(branchName)) {
            Utils.exitWithMessage("A branch with that name already exists.");
        }
    }
    private static void checkUncommittedChanges() {
        List<String> allStagingFileNames = Utils.plainFilenamesIn(STAGING_AREA_DIR);
        List<String> allRemovedFileNames = Utils.plainFilenamesIn(REMOVED_AREA_DIR);
        if (!allStagingFileNames.isEmpty() || !allRemovedFileNames.isEmpty()) {
            Utils.exitWithMessage("You have uncommitted changes.");
        }
    }

    private static void checkNotMergeItself(String branchName) {
        String branchHeadName = Utils.readContentsAsString(HEAD);
        if (branchHeadName.equals(branchName)) {
            Utils.exitWithMessage("Cannot merge a branch with itself.");
        }
    }

    /** Check if an untracked file in the current commit would be
     * overwritten or deleted by the specific commit.
     * The commit can be read from COMMITS_DIR or HEADS_DIR. */
    private static void checkUntrackedFileNotChanged(File dir, String name) {
        Commit currentCommit = Utils.readHeadCommit(HEAD);
        List<String> allFileNames = Utils.plainFilenamesIn(CWD);
        Commit commit = null;
        if (dir.equals(COMMITS_DIR)) {
            commit = Utils.readObject(Utils.join(dir, name), Commit.class);
        } else if (dir.equals(HEADS_DIR)) {
            commit = Utils.readBranchHeadCommit(name);
        }
        for (var fileName : allFileNames) {
            if (!currentCommit.getHashMap().containsKey(fileName)
                    && commit.getHashMap().containsKey(fileName)) {
                Utils.exitWithMessage("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
            }
        }
    }

    private static void checkAncestorBranch(String branchName) {
        Commit branchHead = Utils.readBranchHeadCommit(branchName);
        Commit currentCommit = Utils.readHeadCommit(HEAD);
        while (true) {
            if (currentCommit.getParentCommitId() == null) {
                if (currentCommit.equals(branchHead)) {
                    Utils.exitWithMessage("Given branch is an ancestor of the current branch.");
                }
                break;
            }
            if (currentCommit.getId().equals(branchHead.getId())) {
                Utils.exitWithMessage("Given branch is an ancestor of the current branch.");
            }
            currentCommit = Utils.readObject(
                    Utils.join(COMMITS_DIR, currentCommit.getParentCommitId()), Commit.class);
        }
    }

    private static void checkFastForward(String branchName) {
        Commit branchHead = Utils.readBranchHeadCommit(branchName);
        Commit currentCommit = Utils.readHeadCommit(HEAD);
        while (true) {
            if (branchHead.getParentCommitId() == null) {
                if (branchHead.equals(currentCommit)) {
                    Repository.checkoutBranch(branchName);
                    Utils.exitWithMessage("Current branch fast-forwarded.");
                }
                break;
            }
            if (branchHead.getId().equals(currentCommit.getId())) {
                Repository.checkoutBranch(branchName);
                Utils.exitWithMessage("Current branch fast-forwarded.");
            }
            branchHead = Utils.readObject(
                    Utils.join(COMMITS_DIR, branchHead.getParentCommitId()), Commit.class);
        }
    }

    private static String getFullCommitId(String commitId) {
        List<String> allCommitIds = Utils.plainFilenamesIn(COMMITS_DIR);
        for (var fullCommitId : allCommitIds) {
            if (fullCommitId.startsWith(commitId)) {
                return fullCommitId;
            }
        }
        return null;
    }

    private static Commit getSplitPoint(String branchName) {
        Commit currentBranch = Utils.readHeadCommit(HEAD);
        Commit givenBranch = Utils.readBranchHeadCommit(branchName);
        // record the commit that can be traversed from currentBranch
        Set<String> s = new HashSet<>();
        Queue<Commit> q = new LinkedList<>();
        s.add(currentBranch.getId());
        q.offer(currentBranch);
        while (!q.isEmpty()) {
            Commit c = q.poll();
            if (c.getParentCommitId() != null) {
                Commit t = Utils.readObject(
                        Utils.join(COMMITS_DIR, c.getParentCommitId()), Commit.class);
                s.add(t.getId());
                q.offer(t);
            }
            if (c.getSecondParentCommitId() != null) {
                Commit t = Utils.readObject(
                        Utils.join(COMMITS_DIR, c.getSecondParentCommitId()), Commit.class);
                s.add(t.getId());
                q.offer(t);
            }
        }
        q.offer(givenBranch);
        while (!q.isEmpty()) {
            Commit c = q.poll();
            if (c.getParentCommitId() != null) {
                Commit t = Utils.readObject(
                        Utils.join(COMMITS_DIR, c.getParentCommitId()), Commit.class);
                if (s.contains(t.getId())) {
                    return t;
                }
                q.offer(t);
            }
            if (c.getSecondParentCommitId() != null) {
                Commit t = Utils.readObject(
                        Utils.join(COMMITS_DIR, c.getSecondParentCommitId()), Commit.class);
                if (s.contains(t.getId())) {
                    return t;
                }
                q.offer(t);
            }
        }
        assert false;
        return null;
    }

    private static List<String> getAllFileNamesInMerge(Commit splitPoint,
                                                       Commit currentHead,
                                                       Commit branchHead) {
        List<String> splitPointFileNames = new ArrayList<>(splitPoint.getHashMap().keySet());
        List<String> currentHeadFileNames = new ArrayList<>(currentHead.getHashMap().keySet());
        List<String> branchHeadFileNames = new ArrayList<>(branchHead.getHashMap().keySet());
        return Stream.of(splitPointFileNames, currentHeadFileNames, branchHeadFileNames)
                .flatMap(Collection::stream)
                .distinct()
                .collect(Collectors.toList());
    }

    /** Replace file in CWD by the files in specific commit.
     * The commit can be read from COMMITS_DIR or HEADS_DIR. */
    private static void replaceFilesInCWD(File dir, String name) {
        Commit commit = null;
        if (dir.equals(COMMITS_DIR)) {
            commit = Utils.readObject(Utils.join(dir, name), Commit.class);
        } else if (dir.equals(HEADS_DIR)) {
            commit = Utils.readBranchHeadCommit(name);
        }
        List<String> allFileNames = Utils.plainFilenamesIn(CWD);
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
