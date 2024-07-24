package cz.iocb.chemweb.client.services.downloader;



public abstract class FileDownloader
{
    public abstract void download(String name, String mime, String data);
}
