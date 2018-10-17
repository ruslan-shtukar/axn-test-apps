package com.rshtukaraxondevgroup.bookstest.presenter;

import android.util.Log;

import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;
import com.rshtukaraxondevgroup.bookstest.repository.BookRepository;
import com.rshtukaraxondevgroup.bookstest.view.DetailsView;

@InjectViewState
public class DetailsPresenter extends MvpPresenter<DetailsView> {
    private BookRepository dataStoreFactory;

    public void setDataStoreFactory(BookRepository dataStoreFactory) {
        this.dataStoreFactory = dataStoreFactory;
    }

    public void init(String bookUrl) {
        dataStoreFactory.getBookDetails(bookUrl)
                .subscribe(bookModel -> {
                    Log.d("getBooksList", bookModel.getNumberOfPages().toString());
                    getViewState().initView(bookModel);
                });
    }
}