package cz.iocb.chemweb.client.services.downloader;



public class FileDownloaderDataScheme extends FileDownloader
{
    @Override
    public void download(String name, String mime, String data)
    {
        download(name, "data:" + mime + ";base64," + data);
    }

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
