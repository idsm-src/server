package cz.iocb.chemweb.client.services.downloader;



/**
 * Downloader that lets the browser download the data from a {@code data:} URL without a round trip to the server.
 */
public class FileDownloaderDataScheme extends FileDownloader
{
    /**
     * Creates the downloader.
     */
    public FileDownloaderDataScheme()
    {
    }


    @Override
    public void download(String name, String mime, String data)
    {
        download(name, "data:" + mime + ";base64," + data);
    }


    /**
     * Downloads the URL as a file by clicking a temporary link with the {@code download} attribute.
     *
     * @param filename the name of the file
     * @param url the URL of the file content
     */
    private static native void download(String filename, String url)
    /*-{
        var a = $wnd.document.createElement('a');
        $wnd.document.body.appendChild(a);
        a.href = url;
        a.target = '_blank';
        a.download = filename;
        a.title = filename;
        a.click();
        a.parentNode.removeChild(a);
    }-*/;
}
