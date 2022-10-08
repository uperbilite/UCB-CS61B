package gitlet;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Represents a gitlet repository.
 *  @author Bilite Deng
 */
public class Repository {
    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));

    /** The .gitlet directory. Can be changed by remote command. */
    public static final File GITLET_DIR = Utils.join(CWD, ".gitlet");

    /** The staging area directory. Store the file name and file content for stage addition. */
    public static final File STAGING_AREA_DIR = Utils.join(GITLET_DIR, "staging area");

    /** The removed area directory. Store the file name for removal. */
    public static final File REMOVED_AREA_DIR = Utils.join(GITLET_DIR, "removed area");

    /** The commit directory. File name is commit id, file content is serialized commit object. */
    public static final File COMMITS_DIR = Utils.join(GITLET_DIR, "commits");

    /** The blob directory. File name is blob id. */
    public static final File BLOBS_DIR = Utils.join(GITLET_DIR, "blobs");

    /** The branch directory. File name is branch name, file content is commit id. */
    public static final File HEADS_DIR = Utils.join(GITLET_DIR, "heads");

    /** The remote repository. File name is remote repository name, file content is remote path. */
    public static final File REMOTES_DIR = Utils.join(GITLET_DIR, "remotes");

    /** The HEAD file. File content is the current branch name. */
    public static final File HEAD = Utils.join(GITLET_DIR, "HEAD");

    /**
     * .gitlet/ - restore the information of a repository
     *      - staging area/ - stage files for addition
     *      - removed area/ - stage files for removal
     *      - commits/ - all commits in commits directory
     *      - blobs/ - the saved contents of files
     *      - heads/ - save all branches name and branch head id
     *      - remotes/ - all remote repository name and path
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
        REMOTES_DIR.mkdir();
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
        Utils.writeContents(Utils.join(STAGING_AREA_DIR, addFileName), Utils.readContents(addFile));
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
            c = Utils.readObject(Utils.join(COMMITS_DIR, c.getParentCommitId()), Commit.class);
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
        if (c.getSecondParentCommitId() != null) {
            System.out.println("Merge: "
                    + c.getParentCommitId().substring(0, 7)
                    + " "
                    + c.getSecondParentCommitId().substring(0, 7));
        }
        SimpleDateFormat f = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z", Locale.ENGLISH);
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
        printBranchesStatus();
        printStagedStatus();
        printRemovedStatus();
        printModificationsStatus();
        printUntrackedStatus();
    }

    private static void printBranchesStatus() {
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
    }

    private static void printStagedStatus() {
        System.out.println("=== Staged Files ===");
        List<String> stagingFileNames = Utils.plainFilenamesIn(STAGING_AREA_DIR);
        Collections.sort(stagingFileNames);
        stagingFileNames.forEach(System.out::println);
        System.out.println();
    }

    private static void printRemovedStatus() {
        System.out.println("=== Removed Files ===");
        List<String> removedFileNames = Utils.plainFilenamesIn(REMOVED_AREA_DIR);
        Collections.sort(removedFileNames);
        removedFileNames.forEach(System.out::println);
        System.out.println();
    }

    private static void printModificationsStatus() {
        System.out.println("=== Modifications Not Staged For Commit ===");
        Commit currentCommit = Utils.readHeadCommit(HEAD);
        List<String> allStagingFileNames = Utils.plainFilenamesIn(STAGING_AREA_DIR);
        List<String> allRemovedFileNames = Utils.plainFilenamesIn(REMOVED_AREA_DIR);
        List<String> allFileNamesInCWD = Utils.plainFilenamesIn(CWD);
        List<String> allFileNames = Stream.of(
                        new ArrayList<>(currentCommit.getHashMap().keySet()),
                        allStagingFileNames,
                        allRemovedFileNames,
                        allFileNamesInCWD)
                .flatMap(Collection::stream)
                .distinct()
                .collect(Collectors.toList());
        Collections.sort(allFileNames);
        for (var fileName : allFileNames) {
            if (isModifiedNotStaged(fileName)) {
                System.out.println(fileName + " (modified)");
            } else if (isDeletedNotStaged(fileName)) {
                System.out.println(fileName + " (deleted)");
            }
        }
        System.out.println();
    }

    private static void printUntrackedStatus() {
        System.out.println("=== Untracked Files ===");
        List<String> allFileNamesInCWD = Utils.plainFilenamesIn(CWD);
        Collections.sort(allFileNamesInCWD);
        for (var fileName : allFileNamesInCWD) {
            if (isUntracked(fileName)) {
                System.out.println(fileName);
            }
        }
        System.out.println();
    }

    /**
     * Tracked in the current commit, changed in the working directory, but not staged; or
     * Staged for addition, but with different contents than in the working directory;
     */
    private static boolean isModifiedNotStaged(String fileName) {
        Commit currentCommit = Utils.readHeadCommit(HEAD);
        List<String> allStagingFileNames = Utils.plainFilenamesIn(STAGING_AREA_DIR);
        List<String> allFileNamesInCWD = Utils.plainFilenamesIn(CWD);
        return allFileNamesInCWD.contains(fileName)
                && ((currentCommit.getHashMap().containsKey(fileName)
                && !currentCommit.getHashMap().get(fileName).equals(
                Utils.sha1(Utils.readContents(Utils.join(CWD, fileName))))
                && !allStagingFileNames.contains(fileName))
                || (allStagingFileNames.contains(fileName)
                && !Utils.sha1(Utils.readContents(Utils.join(STAGING_AREA_DIR, fileName))).equals(
                Utils.sha1(Utils.readContents(Utils.join(CWD, fileName))))));
    }

    /**
     * Staged for addition, but deleted in the working directory; or
     * Not staged for removal, but tracked in the current commit and deleted from the
     * working directory.
     */
    private static boolean isDeletedNotStaged(String fileName) {
        Commit currentCommit = Utils.readHeadCommit(HEAD);
        List<String> allStagingFileNames = Utils.plainFilenamesIn(STAGING_AREA_DIR);
        List<String> allRemovedFileNames = Utils.plainFilenamesIn(REMOVED_AREA_DIR);
        List<String> allFileNamesInCWD = Utils.plainFilenamesIn(CWD);
        return (allStagingFileNames.contains(fileName)
                && !allFileNamesInCWD.contains(fileName))
                || (!allRemovedFileNames.contains(fileName)
                && currentCommit.getHashMap().containsKey(fileName)
                && !allFileNamesInCWD.contains(fileName));
    }

    /**
     * files present in the working directory but neither staged for addition nor tracked
     */
    private static boolean isUntracked(String fileName) {
        Commit currentCommit = Utils.readHeadCommit(HEAD);
        List<String> allStagingFileNames = Utils.plainFilenamesIn(STAGING_AREA_DIR);
        return !allStagingFileNames.contains(fileName)
                && !currentCommit.getHashMap().containsKey(fileName);
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
            if (isCase1(splitPointMap, currentBranchMap, givenBranchMap, fileName)) {
                checkoutFile(givenBranch.getId(), fileName);
                addCommand(fileName);
            } else if (isCase2(splitPointMap, currentBranchMap, givenBranchMap, fileName)) {
                continue;
            } else if (isCase3(splitPointMap, currentBranchMap, givenBranchMap, fileName)) {
                continue;
            } else if (isCase4(splitPointMap, currentBranchMap, givenBranchMap, fileName)) {
                continue;
            } else if (isCase5(splitPointMap, currentBranchMap, givenBranchMap, fileName)) {
                checkoutFile(givenBranch.getId(), fileName);
                addCommand(fileName);
            } else if (isCase6(splitPointMap, currentBranchMap, givenBranchMap, fileName)) {
                rmCommand(fileName);
            } else if (isCase7(splitPointMap, currentBranchMap, givenBranchMap, fileName)) {
                continue;
            } else {
                isConflictMerge = true;
                String mergedContent = "";
                if (isCase8(splitPointMap, currentBranchMap, givenBranchMap, fileName)) {
                    mergedContent = getMergedContent(currentBranchMap, givenBranchMap, fileName);
                } else if (isCase9(splitPointMap, currentBranchMap, givenBranchMap, fileName)) {
                    mergedContent = getMergedContent(currentBranchMap, null, fileName);
                } else if (isCase10(splitPointMap, currentBranchMap, givenBranchMap, fileName)) {
                    mergedContent = getMergedContent(null, givenBranchMap, fileName);
                } else {
                    mergedContent = getMergedContent(currentBranchMap, givenBranchMap, fileName);
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

    public static void addRemoteCommand(String remoteName, String remotePath) {
        checkRemoteNameNotExist(remoteName);
        File remoteFile = Utils.join(REMOTES_DIR, remoteName);
        Utils.writeContents(remoteFile, remotePath);
    }

    public static void rmRemoteCommand(String remoteName) {
        checkRemoteNameExist(remoteName);
        File remoteFile = Utils.join(REMOTES_DIR, remoteName);
        remoteFile.delete();
    }

    public static void pushCommand(String remoteName, String remoteBranchName) {
        checkRemoteDirExist(remoteName);
        checkHistoryOfCurrentHead(remoteName, remoteBranchName);
        Stack<Commit> appendCommits = getAppendCommits(remoteName, remoteBranchName);
        // TODO
    }

    private static Stack<Commit> getAppendCommits(String remoteName, String remoteBranchName) {
        File localDir = GITLET_DIR;
        Commit c = Utils.readHeadCommit(HEAD);
        String remotePath = Utils.readContentsAsString(Utils.join(REMOTES_DIR, remoteName));
        File remoteDir = Utils.join(remotePath);
        changeGitletDir(remoteDir);
        Commit remoteBranchHeadCommit = Utils.readBranchHeadCommit(remoteBranchName);
        changeGitletDir(localDir);
        Stack<Commit> appendCommits = new Stack<>();
        while (!c.getId().equals(remoteBranchHeadCommit.getId())) {
            appendCommits.push(c);
            c = Utils.readObject(Utils.join(COMMITS_DIR, c.getParentCommitId()), Commit.class);
        }
        return appendCommits;
    }

    public static void fetchCommand(String remoteName, String remoteBranchName) {
        checkRemoteDirExist(remoteName);
        checkRemoteBranchExist(remoteName, remoteBranchName);
        // TODO
    }

    public static void pullCommand(String remoteName, String remoteBranchName) {
        // TODO
    }

    /**
     * Any files that have been modified in the given branch since the split point,
     * but not modified in the current branch since the split point should be changed
     * to their versions in the given branch
     */
    private static boolean isCase1(TreeMap<String, String> splitPointMap,
                                   TreeMap<String, String> currentBranchMap,
                                   TreeMap<String, String> givenBranchMap,
                                   String fileName) {
        return splitPointMap.containsKey(fileName)
                && currentBranchMap.containsKey(fileName)
                && givenBranchMap.containsKey(fileName)
                && !givenBranchMap.get(fileName).equals(splitPointMap.get(fileName))
                && currentBranchMap.get(fileName).equals(splitPointMap.get(fileName));
    }

    /**
     * Any files that have been modified in the current branch but not in the given
     * branch since the split point should stay as they are.
     */
    private static boolean isCase2(TreeMap<String, String> splitPointMap,
                                   TreeMap<String, String> currentBranchMap,
                                   TreeMap<String, String> givenBranchMap,
                                   String fileName) {
        return splitPointMap.containsKey(fileName)
                && currentBranchMap.containsKey(fileName)
                && givenBranchMap.containsKey(fileName)
                && givenBranchMap.get(fileName).equals(splitPointMap.get(fileName))
                && !currentBranchMap.get(fileName).equals(splitPointMap.get(fileName));
    }

    /**
     * Any files that have been modified in both the current and given branch in the
     * same way (i.e., both files now have the same content or were both removed) are
     * left unchanged by the merge.
     */
    private static boolean isCase3(TreeMap<String, String> splitPointMap,
                                   TreeMap<String, String> currentBranchMap,
                                   TreeMap<String, String> givenBranchMap,
                                   String fileName) {
        return (splitPointMap.containsKey(fileName)
                && currentBranchMap.containsKey(fileName)
                && givenBranchMap.containsKey(fileName)
                && !givenBranchMap.get(fileName).equals(splitPointMap.get(fileName))
                && !currentBranchMap.get(fileName).equals(splitPointMap.get(fileName))
                && givenBranchMap.get(fileName).equals(currentBranchMap.get(fileName)))
                || (splitPointMap.containsKey(fileName)
                && !givenBranchMap.containsKey(fileName)
                && !currentBranchMap.containsKey(fileName));
    }

    /**
     * Any files that were not present at the split point and are present only in
     * the current branch should remain as they are.
     */
    private static boolean isCase4(TreeMap<String, String> splitPointMap,
                                   TreeMap<String, String> currentBranchMap,
                                   TreeMap<String, String> givenBranchMap,
                                   String fileName) {
        return !splitPointMap.containsKey(fileName)
                && currentBranchMap.containsKey(fileName)
                && !givenBranchMap.containsKey(fileName);
    }

    /**
     * Any files that were not present at the split point and are present only in
     * the given branch should be checked out and staged.
     */
    private static boolean isCase5(TreeMap<String, String> splitPointMap,
                                   TreeMap<String, String> currentBranchMap,
                                   TreeMap<String, String> givenBranchMap,
                                   String fileName) {
        return !splitPointMap.containsKey(fileName)
                && !currentBranchMap.containsKey(fileName)
                && givenBranchMap.containsKey(fileName);
    }

    /**
     * Any files present at the split point, unmodified in the current branch,
     * and absent in the given branch should be removed (and untracked).
     */
    private static boolean isCase6(TreeMap<String, String> splitPointMap,
                                   TreeMap<String, String> currentBranchMap,
                                   TreeMap<String, String> givenBranchMap,
                                   String fileName) {
        return splitPointMap.containsKey(fileName)
                && currentBranchMap.containsKey(fileName)
                && !givenBranchMap.containsKey(fileName)
                && currentBranchMap.get(fileName).equals(splitPointMap.get(fileName));
    }

    /**
     * Any files present at the split point, unmodified in the given branch,
     * and absent in the current branch should remain absent.
     */
    private static boolean isCase7(TreeMap<String, String> splitPointMap,
                                   TreeMap<String, String> currentBranchMap,
                                   TreeMap<String, String> givenBranchMap,
                                   String fileName) {
        return splitPointMap.containsKey(fileName)
                && !currentBranchMap.containsKey(fileName)
                && givenBranchMap.containsKey(fileName)
                && givenBranchMap.get(fileName).equals(splitPointMap.get(fileName));
    }

    /**
     * Any files modified in different ways in the current and given branches,
     * the contents of both are changed and different from other
     */
    private static boolean isCase8(TreeMap<String, String> splitPointMap,
                                   TreeMap<String, String> currentBranchMap,
                                   TreeMap<String, String> givenBranchMap,
                                   String fileName) {
        return splitPointMap.containsKey(fileName)
                && currentBranchMap.containsKey(fileName)
                && givenBranchMap.containsKey(fileName)
                && !givenBranchMap.get(fileName).equals(splitPointMap.get(fileName))
                && !currentBranchMap.get(fileName).equals(splitPointMap.get(fileName))
                && !givenBranchMap.get(fileName).equals(currentBranchMap.get(fileName));
    }

    /**
     * Any files modified in different ways in the current and given branches,
     * the contents of one are changed and the other file is deleted, the file
     * in given branch is deleted
     */
    private static boolean isCase9(TreeMap<String, String> splitPointMap,
                                   TreeMap<String, String> currentBranchMap,
                                   TreeMap<String, String> givenBranchMap,
                                   String fileName) {
        return splitPointMap.containsKey(fileName)
                && currentBranchMap.containsKey(fileName)
                && !givenBranchMap.containsKey(fileName)
                && !currentBranchMap.get(fileName).equals(splitPointMap.get(fileName));
    }

    /**
     * Any files modified in different ways in the current and given branches,
     * the contents of one are changed and the other file is deleted, the file
     * in current branch is deleted
     */
    private static boolean isCase10(TreeMap<String, String> splitPointMap,
                                    TreeMap<String, String> currentBranchMap,
                                    TreeMap<String, String> givenBranchMap,
                                    String fileName) {
        return splitPointMap.containsKey(fileName)
                && !currentBranchMap.containsKey(fileName)
                && givenBranchMap.containsKey(fileName)
                && !givenBranchMap.get(fileName).equals(splitPointMap.get(fileName));
    }

    private static String getMergedContent(TreeMap<String, String> currentBranchMap,
                                           TreeMap<String, String> givenBranchMap,
                                           String fileName) {
        String mergedContent = "";
        mergedContent += "<<<<<<< HEAD\n";
        if (currentBranchMap != null) {
            mergedContent += Utils.readContentsAsString(
                    Utils.join(BLOBS_DIR, currentBranchMap.get(fileName)));
        }
        mergedContent += "=======\n";
        if (givenBranchMap != null) {
            mergedContent += Utils.readContentsAsString(
                    Utils.join(BLOBS_DIR, givenBranchMap.get(fileName)));
        }
        mergedContent += ">>>>>>>\n";
        return mergedContent;
    }

    private static void checkCommitExist(String commitId) {
        // commitId will be null because of the return value of getFullCommitId.
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

    private static void checkRemoteNameExist(String remoteName) {
        List<String> allRemoteNames = Utils.plainFilenamesIn(REMOTES_DIR);
        if (!allRemoteNames.contains(remoteName)) {
            Utils.exitWithMessage("A remote with that name does not exist.");
        }
    }

    private static void checkRemoteNameNotExist(String remoteName) {
        List<String> allRemoteNames = Utils.plainFilenamesIn(REMOTES_DIR);
        if (allRemoteNames.contains(remoteName)) {
            Utils.exitWithMessage("A remote with that name already exists.");
        }
    }

    private static void checkRemoteDirExist(String remoteName) {
        String remotePath = Utils.readContentsAsString(Utils.join(REMOTES_DIR, remoteName));
        File remoteDir = Utils.join(remotePath);
        if (!remoteDir.exists()) {
            Utils.exitWithMessage("Remote directory not found.");
        }
    }

    private static void checkHistoryOfCurrentHead(String remoteName, String remoteBranchName) {
        File localDir = GITLET_DIR;
        Commit c = Utils.readHeadCommit(HEAD);
        String remotePath = Utils.readContentsAsString(Utils.join(REMOTES_DIR, remoteName));
        File remoteDir = Utils.join(remotePath);
        changeGitletDir(remoteDir);
        Commit remoteBranchHeadCommit = Utils.readBranchHeadCommit(remoteBranchName);
        changeGitletDir(localDir);
        while (true) {
            if (c.getParentCommitId() == null) {
                if (c.getId().equals(remoteBranchHeadCommit.getId())) {
                    return;
                }
                break;
            }
            if (c.getId().equals(remoteBranchHeadCommit.getId())) {
                return;
            }
            c = Utils.readObject(Utils.join(COMMITS_DIR, c.getParentCommitId()), Commit.class);
        }
        Utils.exitWithMessage("Please pull down remote changes before pushing.");
    }

    private static void checkRemoteBranchExist(String remoteName, String remoteBranchName) {
        File localDir = GITLET_DIR;
        String remotePath = Utils.readContentsAsString(Utils.join(REMOTES_DIR, remoteName));
        File remoteDir = Utils.join(remotePath);
        changeGitletDir(remoteDir);
        List<String> allRemoteBranchNames = Utils.plainFilenamesIn(REMOTES_DIR);
        if (!allRemoteBranchNames.contains(remoteBranchName)) {
            Utils.exitWithMessage("That remote does not have that branch.");
        }
        changeGitletDir(localDir);
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

    private static void changeGitletDir(File targetGitletDir) {
        /*
        GITLET_DIR = targetGitletDir;
        STAGING_AREA_DIR = Utils.join(GITLET_DIR, "staging area");
        REMOVED_AREA_DIR = Utils.join(GITLET_DIR, "removed area");
        COMMITS_DIR = Utils.join(GITLET_DIR, "commits");
        BLOBS_DIR = Utils.join(GITLET_DIR, "blobs");
        HEADS_DIR = Utils.join(GITLET_DIR, "heads");
        REMOTES_DIR = Utils.join(GITLET_DIR, "remotes");
        HEAD = Utils.join(GITLET_DIR, "HEAD");
         */
        // TODO
    }
}
