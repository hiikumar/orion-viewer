package com.artifex.mupdfdemo;

import universe.constellation.orion.viewer.Common;
import universe.constellation.orion.viewer.DocInfo;
import universe.constellation.orion.viewer.PageInfo;

import java.util.Date;

public class MuPDFCore
{
	/* load our native library */
	static {
		System.loadLibrary("mupdf");
	}

	/* Readable members */
	public int numPages;
    public int lastPage = -1;
    private DocInfo info;

    private long globals;

	/* The native functions */
	private native long openFile(String filename, DocInfo info);
	private native void gotoPageInternal(int localActionPageNum);
	private native void getPageInfo(int pageNum, PageInfo info);
    private native byte[] textAsHtml();

    public native boolean needsPasswordInternal();
    public native boolean authenticatePasswordInternal(String password);

	public native int [] drawPage(float zoom, int pageW, int pageH,
			int patchX, int patchY,
			int patchW, int patchH);

    public native String getText(int page, int absoluteX, int absoluteY, int width, int height);

	public native void destroying();

	public MuPDFCore(String filename) throws Exception
	{
        info = new DocInfo();
        globals = openFile(filename, info);
        if (globals == 0){
            throw new Exception("Failed to open " + filename);
        }
        numPages = info.pageCount;
		if (numPages <= 0) {
			throw new Exception("Failed to open " + filename);
		}
	}

	private synchronized void gotoPage(int page) {
        if(lastPage != page) {
            Common.d("Changing page...");
            Date date = new Date();
            if (page > numPages-1)
                page = numPages-1;
            else if (page < 0)
                page = 0;
            gotoPageInternal(page);

            Date date2 = new Date();

            lastPage = page;
            Common.d("Page changing takes " + page + " = " + 0.001 * (date2.getTime() - date.getTime()) + " s");
        }
	}

    public synchronized int[] renderPage(int n, double zoom, int left, int top, int w, int h) {
        gotoPage(n);
        Common.d("MuPDFCore starts rendering...");
        Date date = new Date();
        int [] res =  drawPage((float)zoom, w, h, left, top, w, h);
        Date date2 = new Date();
        Common.d("MuPDFCore render time takes " + n + " = " + 0.001 * (date2.getTime() - date.getTime()) + " s");
        return res;
    }

	public synchronized void onDestroy() {
        Common.d("Destroying document...");
		destroying();
        Common.d("Document destoyed!");
	}

    public int getPageCount() {
        return numPages;
    }

    public synchronized PageInfo getPageInfo(int page) {
        PageInfo info = new PageInfo();
        getPageInfo(page, info);
        System.out.println("Page info: " + info.width + "x" + info.height);
        return info;
    }

    public void freeMemory() {
        onDestroy();
    }

    public DocInfo getInfo() {
        return info;
    }

    public synchronized byte[] html(int page) {
   		gotoPage(page);
   		return textAsHtml();
   	}

	public native com.artifex.mupdfdemo.OutlineItem[] getOutlineInternal();
    public native void setContrast(int contrast);
	public native void setThreshold(int threshold);
}
