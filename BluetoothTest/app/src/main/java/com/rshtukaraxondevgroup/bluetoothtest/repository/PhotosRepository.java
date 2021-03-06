package com.rshtukaraxondevgroup.bluetoothtest.repository;

import com.rshtukaraxondevgroup.bluetoothtest.App;
import com.rshtukaraxondevgroup.bluetoothtest.database.PhotoDao;
import com.rshtukaraxondevgroup.bluetoothtest.model.PhotoModel;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static com.rshtukaraxondevgroup.bluetoothtest.Utils.convertByteToList;

public class PhotosRepository {
    private PhotosApi mApi;
    private PhotoDao mPhotoDao;

    public PhotosRepository() {
        this.mApi = PhotoServiceProvider.getInstance().getClient().create(PhotosApi.class);
        this.mPhotoDao = App.getDatabase().photosDao();
    }

    public Single<List<PhotoModel>> getPhotosList() {
        return mApi.getPhotos()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public void saveReceivedPhotos(byte[] photoModels) {
        List<PhotoModel> modelList = convertByteToList(photoModels);
        if (modelList.size() > 0) {
            Completable.fromAction(() -> mPhotoDao.insertList(modelList))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe();
        }
    }
}
