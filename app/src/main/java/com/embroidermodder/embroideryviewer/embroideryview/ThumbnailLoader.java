package com.embroidermodder.embroideryviewer.embroideryview;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;


import com.embroidermodder.embroideryviewer.EmmPattern;
import com.embroidermodder.embroideryviewer.EmmPatternQuickView;

import org.embroideryio.embroideryio.EmbPattern;
import org.embroideryio.embroideryio.EmbroideryIO;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.Runtime.getRuntime;

/**
 * Created by Tat on 3/22/2018.
 */

public class ThumbnailLoader implements Runnable {
    @NonNull
    ConcurrentLinkedQueue<ThumbnailView> process = new ConcurrentLinkedQueue<>();
    ExecutorService service;

    public ThumbnailLoader() {
        int channels = getRuntime().availableProcessors();
        service = Executors.newFixedThreadPool(channels);
    }

    public void add(ThumbnailView request) {
        process.add(request);
    }

    public void remove(ThumbnailView view) {
        process.remove(view);
    }

    public void start() {
        service.execute(this);
    }

    public void run() {
        InputStream inputStream = null;
        ThumbnailView request;
        while (!process.isEmpty()) {
            request = process.poll();
            if (request == null) continue;
            File file = request.file;
            int dim = request.getDim();

            EmbroideryIO.Reader iRead = EmbroideryIO.getReaderByFilename(file.getPath());
            if (iRead == null) {
                return;
            } else {
                try {
                    inputStream = new BufferedInputStream(new FileInputStream(file));
                    EmbPattern base = new EmbPattern();
                    iRead.read(base, inputStream);
                    EmmPattern p = new EmmPattern();
                    p.fromEmbPattern(base);
                    EmmPatternQuickView patternQuickView = new EmmPatternQuickView(p);
                    Bitmap bitmap = patternQuickView.squareThumbnail(dim);
                    if (request.file == file) {
                        request.setBitmap(bitmap);
                    }
                } catch (IOException ignored) {
                } catch (OutOfMemoryError ignored) {
                } finally {
                    if (inputStream != null) try {
                        inputStream.close();
                    } catch (IOException ignored) {
                    }
                    //iRead.close();
                }
            }
        }
    }
}
