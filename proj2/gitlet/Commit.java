package gitlet;

import java.io.Serializable;
import java.util.Date;
import java.util.TreeMap;

/** Represents a gitlet commit object.
 *  @author Bilite Deng
 */
public class Commit implements Serializable {
    /** The message of this Commit. */
    private String message;
    /** The SHA-1 hash of this commit.
     * It's also the file name for this commit in commits directory. */
    private String id;
    /** The timestamp for this Commit. */
    private Date timestamp;
    /** The previous commit id of this commit. */
    private String parentCommitId;
    /** The second previous commit id (used for merge) of this commit. */
    private String secondParentCommitId;
    /** A mapping of file names to blob references (file's sha-1 id). */
    private TreeMap<String, String> hashByFileName;

    /** Make the initial commit. */
    public Commit() {
        this.message = "initial commit";
        this.timestamp = new Date();
        this.parentCommitId = null;
        this.secondParentCommitId = null;
        this.hashByFileName = new TreeMap<>();
        this.id = Utils.sha1(message, timestamp.toString());
    }

    /** Make a commit with needed information */
    public Commit(String message, String parentCommitId, String secondParentCommitId,
                  TreeMap<String, String> hashByFileName) {
        this.message = message;
        this.timestamp = new Date();
        this.parentCommitId = parentCommitId;
        this.secondParentCommitId = secondParentCommitId;
        this.hashByFileName = hashByFileName;
        this.id = Utils.sha1(message, timestamp.toString(), parentCommitId);
        // add all file to make hash, but it's ok
    }

    public String getMessage() {
        return this.message;
    }

    public Date getTimestamp() {
        return this.timestamp;
    }

    public String getParentCommitId() {
        return this.parentCommitId;
    }

    public String getSecondParentCommitId() {
        return this.secondParentCommitId;
    }

    public String getId() {
        return this.id;
    }

    public TreeMap<String, String> getHashMap() {
        return this.hashByFileName;
    }

}
