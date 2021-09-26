package com.baidu.mobads.demo.main.mediaExamples.novel.presenter.contract;



import com.baidu.mobads.demo.main.mediaExamples.novel.base.BaseContract;
import com.baidu.mobads.demo.main.mediaExamples.novel.model.bean.BookChapterBean;
import com.baidu.mobads.demo.main.mediaExamples.novel.widget.page.TxtChapter;
import java.util.List;



public interface ReadContract extends BaseContract{
    interface View extends BaseContract.BaseView {
        void showCategory(List<BookChapterBean> bookChapterList);
        void finishChapter();
        void errorChapter();
    }

    interface Presenter extends BaseContract.BasePresenter<View>{
        void loadCategory(String bookId);
        void loadChapter(String bookId, List<TxtChapter> bookChapterList);
    }
}
