package com.baidu.mobads.demo.main.mediaExamples.novel.presenter;

import com.baidu.mobads.demo.main.mediaExamples.novel.base.RxPresenter;
import com.baidu.mobads.demo.main.mediaExamples.novel.presenter.contract.ReadContract;
import com.baidu.mobads.demo.main.mediaExamples.novel.widget.page.TxtChapter;
import org.reactivestreams.Subscription;
import java.util.List;


public class ReadPresenter extends RxPresenter<ReadContract.View>
        implements ReadContract.Presenter {
    private static final String TAG = "ReadPresenter";

    private Subscription mChapterSub;

    @Override
    public void loadCategory(String bookId) {
    }

    @Override
    public void loadChapter(String bookId, List<TxtChapter> bookChapters) {
    }

    @Override
    public void detachView() {
        super.detachView();
        if (mChapterSub != null) {
            mChapterSub.cancel();
        }
    }

}
