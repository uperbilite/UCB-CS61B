package gitlet;

import java.io.Serializable;
import java.util.Date;
import java.util.TreeMap;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author Bilite Deng
 */
public class Commit implements Serializable {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private String message;

    /** The SHA-1 hash of this commit.
     * It's also the file name for this commit in commits directory. */
    private String id;

    /** The timestamp for this Commit. */
    private Date timestamp;

    /** The previous commit id of this commit. */
    private String parentCommit;

    /** The second previous commit id (used for merge) of this commit. */
    private String secondParentCommit;

    /** A mapping of file names to blob references (file's sha-1 id). */
    private TreeMap<String, String> hashByFileName;

    // TODO: commit tree?

    /** Make the initial commit. */
    public Commit() {
        this.message = "initial commit";
        this.timestamp = new Date();
        this.parentCommit = null;
        this.secondParentCommit = null;
        this.hashByFileName = new TreeMap<>();
        this.id = Utils.sha1(message, timestamp.toString());
    }

    /** Make a commit with needed information */
    public Commit(String message, String parentCommit, String secondParentCommit,
                  TreeMap<String, String> hashByFileName) {
        this.message = message;
        this.timestamp = new Date();
        this.parentCommit = parentCommit;
        this.secondParentCommit = secondParentCommit;
        this.hashByFileName = hashByFileName;
        this.id = Utils.sha1(message, timestamp.toString(), parentCommit);
        // TODO: add all file to make hash
    }

    public String getMessage() {
        return this.message;
    }

    public Date getTimestamp() {
        return this.timestamp;
    }

    public String getParentCommit() {
        return this.parentCommit;
    }

    public String getSecondParentCommit() {
        return this.secondParentCommit;
    }

    public String getId() {
        return this.id;
    }

    public TreeMap<String, String> getHashMap() {
        return this.hashByFileName;
    }

}
