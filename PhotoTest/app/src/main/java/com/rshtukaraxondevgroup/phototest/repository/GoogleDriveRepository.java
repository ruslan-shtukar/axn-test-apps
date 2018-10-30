package com.rshtukaraxondevgroup.phototest.repository;

import android.util.Log;

import com.google.api.services.drive.Drive;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.rshtukaraxondevgroup.phototest.Constants;
import com.rshtukaraxondevgroup.phototest.exception.CreateDirectoryException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class GoogleDriveRepository {
    private static final String TAG = GoogleDriveRepository.class.getCanonicalName();

    public GoogleDriveRepository() {
    }

    public void uploadFileInGoogleDrive(String mImageUri,
                                        GoogleAccountCredential credentials,
                                        File environmentFile,
                                        RepositoryListener listener) {
        HttpTransport HTTP_TRANSPORT = AndroidHttp.newCompatibleTransport();
        Drive service = new Drive.Builder(HTTP_TRANSPORT, Constants.JSON_FACTORY, credentials)
                .setApplicationName(Constants.APPLICATION_NAME)
                .build();

        File filePath = new File(mImageUri);
        FileContent mediaContent = new FileContent(Constants.FILE_TYPE, filePath);
        com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
        fileMetadata.setName(filePath.getName())
                .setMimeType(Constants.FILE_TYPE);

        File downloadFile = null;
        try {
            downloadFile = getOutputMediaFile(environmentFile);
        } catch (CreateDirectoryException e) {
            listener.downloadError(e);
            Log.d(TAG, e.getMessage());
        }

        File finalDownloadFile = downloadFile;
        Observable.fromCallable(() -> {
            com.google.api.services.drive.model.File fileUpload = null;
            try {
                fileUpload = service.files().create(fileMetadata, mediaContent)
                        .setFields("id")
                        .execute();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }

            String fileId = fileUpload.getId();
            try {
                OutputStream outputStream = new FileOutputStream(finalDownloadFile);
                service.files().get(fileId)
                        .executeMediaAndDownloadTo(outputStream);
            } catch (FileNotFoundException e) {
                Log.e(TAG, e.getMessage());
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
            return finalDownloadFile;
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> listener.downloadSuccessful(result),
                        throwable -> listener.downloadError(throwable));
    }

    private static File getOutputMediaFile(File environmentFile) throws CreateDirectoryException {
        File mediaStorageDir = new File(environmentFile, Constants.CHILD_FILE_DIRECTORY);
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                throw new CreateDirectoryException(Constants.FAILED_TO_CREATE_DIRECTORY);
            }
        }
        String timeStamp = new SimpleDateFormat(Constants.FILE_CREATION_DATE_FORMAT).format(new Date());
        return new File(mediaStorageDir.getPath() + File.separator +
                Constants.FILE_NAME_GD_DOWNLOAD + timeStamp + Constants.FILE_FORMAT);
    }
}
