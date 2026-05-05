package tr.alperendemir.helix.repo;

public interface RepoDownloadCallback {

    void onSizeReceived(long size);

    void onProgressUpdate(double progressUpdate);

    void onFailure();

    void onSuccess();

}
