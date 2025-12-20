package com.itu.demo;

public class UploadedFile {
    private String fileName;
    private byte[] content;
    private String contentType;
    private long size;

    public UploadedFile(String fileName, byte[] content, String contentType) {
        this.fileName = fileName;
        this.content = content;
        this.contentType = contentType;
        this.size = content != null ? content.length : 0;
    }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public byte[] getContent() { return content; }
    public void setContent(byte[] content) { this.content = content; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public long getSize() { return size; }

    public String getExtension() {
        if (fileName == null || !fileName.contains(".")) return "";
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }
}