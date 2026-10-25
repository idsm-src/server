package cz.iocb.chemweb.client.services.downloader;



/**
 * Offers data created in the client to the user as a downloaded file. The implementation is chosen by deferred binding:
 * {@link FileDownloaderDataScheme} for browsers that can download a data URL, {@link FileDownloaderServer} otherwise.
 */
public abstract class FileDownloader
{
    /**
     * Creates the downloader.
     */
    protected FileDownloader()
    {
    }


    /**
     * Offers the data to the user as a downloaded file.
     *
     * @param name the name of the file
     * @param mime the MIME type of the file
     * @param data the content of the file encoded in Base64
     */
    public abstract void download(String name, String mime, String data);
}
